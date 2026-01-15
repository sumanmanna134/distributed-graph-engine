package com.offlix.distributed_graph_engine.graph.operations;

import com.offlix.distributed_graph_engine.domain.GraphType;
import com.offlix.distributed_graph_engine.domain.VertexMetadata.VertexMetadataImpl;
import com.offlix.distributed_graph_engine.graph.core.GraphContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VertexOperations<T> {
    private final GraphContext<T> context;

    public VertexOperations(GraphContext<T> context){
        this.context = context;
    }

    public void addVertexIfAbsent(T vertex){
        if(context.getAdjacencyList().putIfAbsent(vertex, new ConcurrentHashMap<>())==null){
            context.getVertexMetadata().put(vertex, new VertexMetadataImpl());
            context.getStats().incrementVertexCount();
            context.incrementVersionAndTouch();
        }
    }

    public boolean removeVertexAndEdges(T vertex){
        for(Map<T, Double> neighbors: context.getAllNeighborsWithWeights()){
            neighbors.remove(vertex);
        }

        int edgesRemoved = context.getEdgeCount(vertex);

        if(context.getType()== GraphType.UNDIRECTED){
            edgesRemoved /=2;
        }
        context.removeVertexFromAdjacencyList(vertex);
        context.removeVertexFromVertexMetadata(vertex);
        context.decrementVertexCount();
        context.decrementEdgeCount(edgesRemoved);
        context.incrementVersionAndTouch();
        return true;
    }



}
