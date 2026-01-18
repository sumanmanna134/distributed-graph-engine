package com.offlix.distributed_graph_engine.graph.operations;

import com.offlix.distributed_graph_engine.graph.core.GraphContext;

import java.util.List;

public class PathEnumerations<T> {
    private final GraphContext<T>context;

    public PathEnumerations(GraphContext<T> context) {
        this.context = context;
    }

    public List<List<T>> getPossiblePaths(T source, T destination){

        return null;
    }

    private void dfs(T source, T destination, List<T> path, List<List<T>> results){

    }
}
