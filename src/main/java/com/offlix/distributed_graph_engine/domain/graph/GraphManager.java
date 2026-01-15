package com.offlix.distributed_graph_engine.domain.graph;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.offlix.distributed_graph_engine.domain.GraphStats.GraphStats;
import com.offlix.distributed_graph_engine.domain.GraphStats.GraphStatsImpl;
import com.offlix.distributed_graph_engine.domain.GraphType;
import com.offlix.distributed_graph_engine.domain.VertexMetadata.VertexMetadata;
import com.offlix.distributed_graph_engine.domain.VertexMetadata.VertexMetadataImpl;
import com.offlix.distributed_graph_engine.exception.EdgeAlreadyExist;
import com.offlix.distributed_graph_engine.exception.NoSuchMethodExistForGraphException;
import com.offlix.distributed_graph_engine.exception.SelfLoopExistException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Deprecated
public class GraphManager<T> implements Graph<T>,Serializable {
    private static final long serialVersionUID=1L;
    @JsonProperty("id")
    private Long id;
    @JsonProperty("graphId")
    private String graphId;

    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;

    @Builder.Default
    @JsonProperty("type")
    private GraphType type = GraphType.DIRECTED;

    @Builder.Default
    @JsonProperty("updatedAt")
    private Instant updatedAt = Instant.now();

    @Builder.Default
    @JsonProperty("adjacencyList")
    private Map<T, Map<T, Double>> adjacencyList = new ConcurrentHashMap<>();

    @Builder.Default
    @JsonProperty("metadata")
    private Map<String, Object> metadata = new ConcurrentHashMap<>();

    @Builder.Default
    @JsonProperty("vertexMetadata")
    private Map<T, VertexMetadata> vertexMetadata = new ConcurrentHashMap<>();

    @JsonIgnore
    @Builder.Default
    private transient ReadWriteLock lock = new ReentrantReadWriteLock();

    @Builder.Default
    @JsonProperty("stats")
    private GraphStats stats = new GraphStatsImpl();

    @Builder.Default
    @JsonProperty("version")
    private int version =1;

    @Deprecated
    @Override
    public void addVertex(T vertex) {
        lock.writeLock().lock();
        try{
            if(adjacencyList.putIfAbsent(vertex, new ConcurrentHashMap<>())==null){
                vertexMetadata.put(vertex, new VertexMetadataImpl());
                stats.incrementVertexCount();
                incrementVersionAndTouch();
            }

        }finally {
            lock.writeLock().unlock();
        }

    }

    @Deprecated
    @Override
    public boolean removeVertex(T vertex) {
        lock.writeLock().lock();
        try{
            if(!adjacencyList.containsKey(vertex)){
                return false;
            }
            for(Map<T, Double> neighbors: adjacencyList.values()){
                neighbors.remove(vertex);
            }

            int edgesRemoved = adjacencyList.get(vertex).size();
            if(type==GraphType.UNDIRECTED){
                edgesRemoved /=2;
            }
            adjacencyList.remove(vertex);
            vertexMetadata.remove(vertex);
            stats.decrementEdgeCount(edgesRemoved);
            stats.decrementVertexCount();
            incrementVersionAndTouch();
            return true;
        }finally {

            lock.writeLock().unlock();
        }
    }

    @Deprecated
    public void addEdge(T source, T destination, double weight) {
        lock.writeLock().lock();
        try{
            addVertex(source);
            addVertex(destination);
            checkSelfLoopExist(source, destination);
            checkDuplicateEdge(source, destination);
            adjacencyList.get(source).put(destination, weight);
            if(type==GraphType.UNDIRECTED){
                adjacencyList.get(destination).put(source, weight);
            }

            stats.incrementEdgeCount();
            incrementVersionAndTouch();
        }finally {
            lock.writeLock().unlock();
        }
    }

    @Deprecated
    private void checkSelfLoopExist(T source, T destination){
        if(source.equals(destination)){
            throw new SelfLoopExistException("Self-loop is not allowed: " + source);
        }
    }

    @Deprecated(since = "v1")
    private void checkDuplicateEdge(T source, T destination){
        Optional<Map<T, Double>> neighbors = getNeighborsWithWeight(source);
        if(neighbors.isPresent() && neighbors.get().containsKey(destination)){
            throw new EdgeAlreadyExist(source, destination);
        }
    }

    @Deprecated(since = "v1")
    public void addEdge(T source, T destination) {
        addEdge(source, destination, 1.0);
    }

    @Deprecated
    @Override
    public boolean removeEdge(T source, T destination) {
        lock.writeLock().lock();
        try{
            boolean isRemoved = removeOneWayEdge(source, destination);
            if(isRemoved && type==GraphType.UNDIRECTED){
                removeOneWayEdge(destination, source);
            }
            if(isRemoved){
                stats.decrementEdgeCount();
                incrementVersionAndTouch();

            }

            return isRemoved;

        }finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<Map<T, Double>> getNeighborsWithWeight(T vertex) {
        return Optional.ofNullable(adjacencyList.get(vertex));
    }

    @Deprecated
    private boolean removeOneWayEdge(T from, T to){
        Map<T, Double> neighbors = getNeighborsWithWeight(from).get();
        return neighbors!=null && neighbors.remove(to)!=null;
    }

    @Deprecated
    public Set<T> getNeighbors(T vertex){
        return Set.copyOf(adjacencyList.getOrDefault(vertex, Map.of()).keySet());
    }

    @Override
    public Set<T> getVertices() {
        return adjacencyList.keySet();
    }

    @Override
    public boolean hasCycle() {
        lock.readLock().lock();
        try{

            return type==GraphType.UNDIRECTED?
                    hasCycleUndirected():
                    hasCycleDirected();

        }finally {
            lock.readLock().unlock();
        }

    }

    @Override
    public GraphType getType() {
        return type;
    }

    @Override
    public Map<Integer, Set<T>> getStronglyConnectedComponents() {
        lock.readLock().lock();
        try{
            if(type==GraphType.UNDIRECTED){
                throw new NoSuchMethodExistForGraphException(type);
            }

            Map<Integer, Set<T>> components = new ConcurrentHashMap<>();
            Set<T> visited = new HashSet<>();
            Deque<T> stack = new ArrayDeque<>();
            //first we need to run dfs and store the order in stack
            //run for each unvisited vertex
            for(T vertex: adjacencyList.keySet()){
                if(!visited.contains(vertex)){
                    fillOrderUsingDFS(vertex,visited, stack);
                }

            }

            Map<T, Set<T>> reversedGraph = reverseGraph(true);

            visited.clear();
            int componentId=0;

            while (!stack.isEmpty()){
                T vertex = stack.pop();
                if(!visited.contains(vertex)){
                    Set<T> component = new HashSet<>();
                    dfsCollectComponent(vertex, component, reversedGraph,visited);
                    components.put(componentId++, component);
                }
            }



            log.info("Components: {}", components);






            return null;
        }catch (Exception ex){
            log.info("Error: ", ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Map<T, Map<T, Double>> getAdjList() {
        return adjacencyList;
    }

    private void fillOrderUsingDFS(T vertex, Set<T> visited, Deque<T> stack){
        visited.add(vertex);
        for(T neighbor: adjacencyList.get(vertex).keySet()){
            if(!visited.contains(neighbor)){
                fillOrderUsingDFS(neighbor, visited, stack);
            }
        }

        stack.push(vertex);
    }
    private Map<T, Set<T>> reverseGraph(boolean reverse){
        //A -> B
        //output will be A-> {}, B -> A
        //so first we have take one empty map
        Map<T, Set<T>> reversed = new HashMap<>();
        //put vertex on this map with empty value
        for(T vertex: getVertices()){
            reversed.putIfAbsent(vertex, new HashSet<>());
        }

        //then iterate each entry, get first value as from, and to, put to -> from
        for(Map.Entry<T, Map<T, Double>> entry: adjacencyList.entrySet()){
            T from = entry.getKey();
            for(T to: entry.getValue().keySet()){
                if(reverse){
                    reversed.putIfAbsent(to, new HashSet<>());
                    reversed.get(to).add(from);
                }else{
                    reversed.putIfAbsent(from, new HashSet<>());
                    reversed.get(from).add(to);
                }

            }
        }

        return reversed;

    }

    private void dfsCollectComponent(T vertex, Set<T> component, Map<T, Set<T>> reverseGraph, Set<T> visited){
        visited.add(vertex);
        component.add(vertex);
        for(T neighbor: reverseGraph.getOrDefault(vertex, Set.of())){
            if(!visited.contains(neighbor)){
                dfsCollectComponent(neighbor, component, reverseGraph, visited);
            }
        }
    }







    private boolean hasCycleDirected(){
        Set<T> visited = new HashSet<>();
        Set<T> inStack = new HashSet<>();
        for(T vertex: getVertices()){
            if(!visited.contains(vertex)){
                if(dfsDirected(vertex, visited, inStack)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Detects cycles in a directed graph using a Depth First Search (DFS) approach.
     * * Algorithm:
     * 1. Mark the current node as 'visited' and add it to the 'inStack' set (recursion stack).
     * 2. For every neighbor of the current node:
     * - If the neighbor is already in 'inStack', a back-edge exists, indicating a cycle.
     * - If the neighbor hasn't been visited, recursively call the function.
     * 3. After exploring all neighbors, remove the node from 'inStack' (backtracking).
     * 4. If no back-edges are found, return false.
     *
     * @param current   The node currently being explored.
     * @param visited   A set tracking all nodes explored across all DFS calls to prevent redundant work.
     * @param inStack   A set tracking nodes in the current recursion path to identify back-edges.
     * @return true if a cycle is detected, false otherwise.
     */
    private boolean dfsDirected(T current, Set<T> visited, Set<T> inStack){
        // Add current node to both visited and the active recursion stack
        visited.add(current);
        inStack.add(current);
        // Explore adjacent vertices
        for( T neighbor: getNeighbors(current)){

            // If neighbor is in the current recursion stack, we found a cycle
            if(inStack.contains(neighbor)){
                return true;
            }

            // If neighbor hasn't been visited, recurse into it
            if(!visited.contains(neighbor)){
                if(dfsDirected(neighbor, visited, inStack)){
                    return true;
                }
            }
        }

        inStack.remove(current);
        return false;
    }

    private boolean hasCycleUndirected(){
        Set<T> visited = new HashSet<>();
        for(T vertex: getVertices()){
            if(!visited.contains(vertex)){
                if(dfsUndirected(vertex,null, visited)){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean dfsUndirected(T current,T parent, Set<T> visited){
        visited.add(current); // add current in visited set
        // we have to check each neighbors
        for(T neighbor: adjacencyList.getOrDefault(current, Map.of()).keySet()){

            //case 1: visit unvisited neighbors
            if(!visited.contains(neighbor)){
                if(dfsUndirected(neighbor, current, visited)){
                    return true;
                }
            }

            // case 2: visited neighbors that is not parent ---> cycle
            else if(!neighbor.equals(parent)){
                return true;
            }
        }

        return false;
    }


    

     /* =========================
       Utility
       ========================= */

    private void incrementVersionAndTouch(){
        this.updatedAt = Instant.now();
        this.version++;
        log.info("Graph updated at {} (version {})", updatedAt, version);
    }

    @Override
    public void printGraph() {
        adjacencyList.forEach((v, edges)->{
            log.info("{} -> {}", v, edges);
        });
    }

    @Override
    public String toString() {
        return "GraphManager{" +
                "id=" + id +
                ", graphId='" + graphId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", updatedAt=" + updatedAt +
                ", adjacencyList=" + adjacencyList +
                ", metadata=" + metadata +
                ", vertexMetadata=" + vertexMetadata +
                ", stats=" + stats +
                ", version=" + version +
                '}';
    }
}
