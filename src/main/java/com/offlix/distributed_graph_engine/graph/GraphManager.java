package com.offlix.distributed_graph_engine.graph;

import com.offlix.distributed_graph_engine.domain.GraphType;
import com.offlix.distributed_graph_engine.graph.core.GraphContext;
import com.offlix.distributed_graph_engine.graph.core.GraphLock;
import com.offlix.distributed_graph_engine.graph.operations.CycleDetection;
import com.offlix.distributed_graph_engine.graph.operations.EdgeOperations;
import com.offlix.distributed_graph_engine.graph.operations.SccFinder;
import com.offlix.distributed_graph_engine.graph.operations.VertexOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GraphManager<T> {
    private static final Logger log = LoggerFactory.getLogger(GraphManager.class);
    private final GraphContext<T> context;
    private final GraphLock lock = new GraphLock();
    private final VertexOperations<T> vertexOps;

    private final EdgeOperations<T> edgeOps;
    private final CycleDetection<T> cycleOps;
    private final SccFinder<T> sccFinder;


    public GraphManager(GraphType type){
        this.context = (GraphContext<T>) GraphContext.builder()
                .type(type)
                .build();
        this.vertexOps = new VertexOperations<>(context);
        this.edgeOps = new EdgeOperations<>(context, vertexOps);
        this.cycleOps = new CycleDetection<>(context);
        this.sccFinder = new SccFinder<>(context);
    }
    public GraphManager(){
        this(GraphType.DIRECTED);
    }


    public void addVertex(T vertex){
        lock.writeLock(()-> vertexOps.addVertexIfAbsent(vertex));
    }

    public void removeVertex(T vertex){
        lock.writeLock(()-> vertexOps.removeVertexAndEdges(vertex));
    }
    public void addEdgeBetween(T source, T destination){
        lock.writeLock(()-> addEdgeBetween(source, destination, 1.0));
    }

    public void addEdgeBetween(T source, T destination, double weight){
        lock.writeLock(()->edgeOps.addWeightEdge(source, destination, weight));;
    }

    public boolean removeEdgeBetween(T source, T destination){
        final boolean[] result = new boolean[1];
        lock.writeLock(()-> result[0]=edgeOps.removeEdgeBetween(source, destination));
        return result[0];
    }
    public boolean containCycle(){
        final boolean[] formedCycle = new boolean[1];
        lock.writeLock(()-> formedCycle[0] = cycleOps.containCycle());
        return formedCycle[0];
     }

     public List<List<T>> findCycles(){
         final List<List<T>>[] cycles = new List[]{new ArrayList<>()};
        lock.writeLock(()-> cycles[0] = cycleOps.findCycles());
        return cycles[0];
     }

    public void printGraph(){
        context.getAdjacencyList().forEach((v, edges)->{
            log.info("{} -> {}", v, edges);
        });
    }

    public Map<T, Set<T>> reverseGraph(){
        return context.reverseGraph();
    }

    public Map<T, Map<T, Double>> reverseGraphWithCost(){
        return context.reverseGraphWithWeight();
    }

    public void sccFind(){
        sccFinder.find();
    }




}
