package com.offlix.distributed_graph_engine.graph.operations;

import com.offlix.distributed_graph_engine.graph.core.GraphContext;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DegreeAnalyzer<T>{
    private final GraphContext<T> context;
    public DegreeAnalyzer(GraphContext<T> context){
        this.context = context;
    }

    public int getOutDegree(T node){
        return context.getAdjacencyList().getOrDefault(node, Map.of()).size();
    }
    public int getIndegree(T node){
        int count=0;
        for(Map<T, Double> neigh: context.getAllNeighborsWithWeights()){
            if(neigh.containsKey(node)) count++;
        }

        return count;
    }

    public int totalDegree(T node){
        return getIndegree(node)+getOutDegree(node);
    }

    public List<T> getSources(){
        return context.getVertices()
                .stream()
                .filter(node-> getIndegree(node)==0).collect(Collectors.toList());
    }

    //whose outdegree ==0
    public List<T> getSinks(){
        return context.getVertices()
                .stream().filter(node-> this.getOutDegree(node)==0).collect(Collectors.toList());
    }

    /**
     * Find the "Hub" node: The node with the highest out-degree.
     */
    public Optional<T> MostInfluentialNode(){
        return context.getVertices()
                .stream()
                .max(Comparator.comparingInt(this::getOutDegree));
    }

    /**
     * Find the "weak" node: The node with the min out-degree.
     */
    public Optional<T> leastInfluentialNode(){
        return context.getVertices()
                .stream()
                .min(Comparator.comparingInt(this::getOutDegree));
    }

    /**
     * Degree Centrality: Degree of node / (Total Nodes - 1).
     * Useful for comparing connectivity across different sized graphs.
     */
    public double getDegreeCentrality(T node){
        int totalNodes = context.totalNodes();
        if(totalNodes<=1) return 0.0;

        return (double) totalDegree(node)/(totalNodes-1);
    }

    public boolean isIsolated(T node){
        return getIndegree(node)==0 && getOutDegree(node)==0;
    }
}
