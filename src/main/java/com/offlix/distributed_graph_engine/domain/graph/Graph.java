package com.offlix.distributed_graph_engine.domain.graph;

import com.offlix.distributed_graph_engine.domain.GraphType;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface Graph<T> {
    public void addVertex(T vertex);

    public boolean removeVertex(T vertex);

    public void addEdge(T source, T destination, double weight);

    public void addEdge(T source, T destination);

    public boolean removeEdge(T source, T destination);



    public Optional<Map<T, Double>> getNeighborsWithWeight(T vertex);

    public Set<T> getNeighbors(T vertex);

    public Set<T> getVertices();

    public boolean hasCycle();

    public GraphType getType();

    public Map<Integer, Set<T>> getStronglyConnectedComponents();

    public Map<T, Map<T, Double>> getAdjList();



    public void printGraph();

}
