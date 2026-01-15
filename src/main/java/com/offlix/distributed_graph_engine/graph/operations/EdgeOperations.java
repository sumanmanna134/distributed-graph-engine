package com.offlix.distributed_graph_engine.graph.operations;

import com.offlix.distributed_graph_engine.domain.GraphType;
import com.offlix.distributed_graph_engine.exception.EdgeAlreadyExist;
import com.offlix.distributed_graph_engine.exception.SelfLoopExistException;
import com.offlix.distributed_graph_engine.graph.core.GraphContext;

import java.util.Map;
import java.util.Optional;

public class EdgeOperations<T> {
    private final GraphContext<T> context;
    private final VertexOperations<T> vertexOps;

    public EdgeOperations(GraphContext<T> context, VertexOperations<T> vertexOps){
        this.context = context;
        this.vertexOps = vertexOps;
    }

    public void addWeightEdge(T source, T destination, double weight){
        vertexOps.addVertexIfAbsent(source);
        vertexOps.addVertexIfAbsent(destination);
        validateNoSelfLoop(source, destination);
        checkDuplicationEdge(source, destination);
        context.getAdjacencyList().get(source).put(destination, weight);
        if(context.getType()== GraphType.UNDIRECTED){
            context.getAdjacencyList().get(destination).put(source, weight);
        }
        context.incrementEdgeCount();
        context.incrementVersionAndTouch();
    }

    public boolean removeEdgeBetween(T source, T destination){
        boolean isRemoved = removeOneWayEdge(source, destination);
        if(isRemoved && context.getType()==GraphType.UNDIRECTED){
            removeOneWayEdge(destination, source);
        }

        if(isRemoved){
            context.decrementEdgeCount();
            context.incrementVersionAndTouch();
        }
        return isRemoved;
    }

    private boolean removeOneWayEdge(T from, T to){
        Optional<Map<T, Double>> neighbors = context.getNeighborsWithEdgeWeight(from);
        return neighbors.isPresent() && neighbors.get().remove(to)!=null;
    }

    //source == destination
    private void validateNoSelfLoop(T source, T destination){
        if(source.equals(destination)){
            throw new SelfLoopExistException("Self-loop is not allowed: " + source);
        }
    }

    private void checkDuplicationEdge(T source, T destination){
        Optional<Map<T, Double>> neighborsWithEdgeWeight = context.getNeighborsWithEdgeWeight(source);
        if(neighborsWithEdgeWeight.isPresent() && neighborsWithEdgeWeight.get().containsKey(destination)){
            throw new EdgeAlreadyExist(source, destination);
        }

    }

}
