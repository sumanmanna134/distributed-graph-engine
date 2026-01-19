package com.offlix.distributed_graph_engine.graph.operations.traversal;

import com.offlix.distributed_graph_engine.graph.core.GraphContext;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

public class BFSRecursive<T> implements GraphTraversal<T> {
    @Override
    public void traverse(GraphContext<T> context, T startNode, Consumer<T> action, Set<T> visited) {
        Queue<T> queue = new LinkedList<>();
        queue.add(startNode);
        visited = visited==null? new HashSet<>():visited;
        visited.add(startNode);
        bfsHelper(context,queue,visited, action);
    }

    private void bfsHelper(GraphContext<T> context, Queue<T> queue,Set<T> visited, Consumer<T> action){
        if(queue.isEmpty()) return;
        T current = queue.poll();
        action.accept(current);
        for(T neighbor: context.getNeighbors(current)){
            if(!visited.contains(neighbor)){
                visited.add(neighbor);
                queue.add(neighbor);
            }
        }

        bfsHelper(context, queue, visited, action);
    }

}
