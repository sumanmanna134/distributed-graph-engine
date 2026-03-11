package com.offlix.distributed_graph_engine.graph.operations.traversal;

import com.offlix.distributed_graph_engine.graph.core.GraphContext;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class DFSRecursive<T> implements GraphTraversal<T> {
    @Override
    public void traverse(GraphContext<T> context, T startNode, Consumer<T> action, Set<T> visited) {
        visited = visited==null? new HashSet<>():visited;
        dfsHelper(context, startNode, visited, action);
    }

    private void dfsHelper(GraphContext<T> context, T current, Set<T> visited, Consumer<T> action){
        if(current==null || visited.contains(current)) return;
        visited.add(current);
        action.accept(current);
        for(T neighbor : context.getNeighbors(current)){
            dfsHelper(context, neighbor, visited, action);
        }
    }
}
