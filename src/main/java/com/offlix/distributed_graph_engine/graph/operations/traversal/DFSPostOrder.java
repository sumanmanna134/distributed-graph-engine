package com.offlix.distributed_graph_engine.graph.operations.traversal;

import com.offlix.distributed_graph_engine.graph.core.GraphContext;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class DFSPostOrder<T> implements GraphTraversal<T>{
    @Override
    public void traverse(GraphContext<T> context, T startNode, Consumer<T> action, Set<T> visited) {
        visited = visited==null? new HashSet<>():visited;
        dfs(context, visited, startNode, action);
    }

    private void dfs(GraphContext<T> context, Set<T> visited, T vertex, Consumer<T> consumer){
        visited.add(vertex);
        for(T neighbor : context.getNeighbors(vertex)){
            if(!visited.contains(neighbor)){
                dfs(context, visited, neighbor, consumer);
            }
        }

        consumer.accept(vertex);
    }


}
