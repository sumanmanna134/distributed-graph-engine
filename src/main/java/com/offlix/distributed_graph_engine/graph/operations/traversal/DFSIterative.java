package com.offlix.distributed_graph_engine.graph.operations.traversal;

import com.offlix.distributed_graph_engine.graph.core.GraphContext;

import java.util.*;
import java.util.function.Consumer;

public class DFSIterative<T> implements GraphTraversal<T> {
    @Override
    public void traverse(GraphContext<T> context, T startNode, Consumer<T> action, Set<T> visited) {
        if(startNode==null) return;
        visited = visited==null? new HashSet<>():visited;
        Stack<T> stack = new Stack<>();
        stack.push(startNode);
        while (!stack.isEmpty()){
            T vertex = stack.pop();
            if(!visited.contains(vertex)){
                visited.add(vertex);
                action.accept(vertex);
                List<T> neighbors = new ArrayList<>(context.getNeighbors(vertex));
                Collections.reverse(neighbors);
                for(T neighbor: neighbors){
                    if(!visited.contains(neighbor)){
                        stack.push(neighbor);
                    }
                }
            }
        }

    }
}
