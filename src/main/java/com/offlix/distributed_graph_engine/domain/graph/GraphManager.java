package com.offlix.distributed_graph_engine.domain.graph;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offlix.distributed_graph_engine.domain.GraphStats.GraphStats;
import com.offlix.distributed_graph_engine.domain.GraphStats.GraphStatsImpl;
import com.offlix.distributed_graph_engine.domain.GraphType;
import com.offlix.distributed_graph_engine.domain.VertexMetadata.VertexMetadata;
import com.offlix.distributed_graph_engine.domain.VertexMetadata.VertexMetadataImpl;
import com.offlix.distributed_graph_engine.exception.EdgeAlreadyExist;
import com.offlix.distributed_graph_engine.exception.SelfLoopExistException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
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

    @Override
    public void addVertex(T vertex) {
        lock.writeLock().lock();
        try{
            if(adjacencyList.putIfAbsent(vertex, new ConcurrentHashMap<>())==null){
                vertexMetadata.put(vertex, new VertexMetadataImpl());
                stats.incrementVertexCount();
                updateVersionWithTimestamp();
            }

        }finally {
            lock.writeLock().unlock();
        }

    }

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
            updateVersionWithTimestamp();
            return true;
        }finally {

            lock.writeLock().unlock();
        }
    }

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
            updateVersionWithTimestamp();
        }finally {
            lock.writeLock().unlock();
        }
    }

    private void checkSelfLoopExist(T source, T destination){
        if(source.equals(destination)){
            throw new SelfLoopExistException("Self-loop is not allowed: " + source);
        }
    }

    private void checkDuplicateEdge(T source, T destination){
        Optional<Map<T, Double>> neighbors = getNeighborsWithWeight(source);
        if(neighbors.isPresent() && neighbors.get().containsKey(destination)){
            throw new EdgeAlreadyExist(source, destination);
        }
    }


    public void addEdge(T source, T destination) {
        addEdge(source, destination, 1.0);
    }

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
                updateVersionWithTimestamp();

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

    private boolean removeOneWayEdge(T from, T to){
        Map<T, Double> neighbors = getNeighborsWithWeight(from).get();
        return neighbors!=null && neighbors.remove(to)!=null;
    }

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






    @Override
    public void printGraph() {

    }


    private void updateVersionWithTimestamp(){
        this.updatedAt = Instant.now();
        this.version++;
        log.info("Update Log: {updatedAt: {}, version: {}}", updatedAt, version);
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
