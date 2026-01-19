package com.offlix.distributed_graph_engine.graph.operations.traversal;

import com.offlix.distributed_graph_engine.graph.core.GraphContext;

import java.util.*;
import java.util.function.Consumer;

public class BFSIterative<T> implements GraphTraversal<T> {
    @Override
    public void traverse(GraphContext<T> context, T startNode, Consumer<T> action, Set<T> visited) {
        if(startNode==null) return;
        visited = visited==null? new HashSet<>():visited;
        Queue<T> queue = new LinkedList<>();
        visited.add(startNode);
        queue.add(startNode);
        while (!queue.isEmpty()){
            T vertex = queue.poll();
            action.accept(vertex);
            for(T neighbor: context.getNeighbors(vertex)){
                if(!visited.contains(neighbor)){
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
    }
}
