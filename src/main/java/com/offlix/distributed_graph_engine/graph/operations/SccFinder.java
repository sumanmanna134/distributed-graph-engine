package com.offlix.distributed_graph_engine.graph.operations;

import com.offlix.distributed_graph_engine.domain.GraphType;
import com.offlix.distributed_graph_engine.exception.NoSuchMethodExistForGraphException;
import com.offlix.distributed_graph_engine.graph.core.GraphContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SccFinder<T>{
    private static final Logger log = LoggerFactory.getLogger(SccFinder.class);
    private final GraphContext<T> context;

    public SccFinder(GraphContext<T> context) {
        this.context = context;
    }

    public Map<Integer, Set<T>> find(){
        if(context.getType()== GraphType.UNDIRECTED){
            throw new NoSuchMethodExistForGraphException(context.getType());
        }

        Set<T> visited = new HashSet<>();
        Deque<T> stack = new ArrayDeque<>();
        Map<Integer, Set<T>> stronglyConnectedComponents= new HashMap<>();
        for(T vertex: context.getVertices()){
            if(!visited.contains(vertex)){
                dfsPostOrder(vertex, visited, stack);
            }
        }

        log.info("Visited: {}", visited);
        log.info("Stack: {}", stack);
        Map<T, Set<T>> reverseGraph = context.reverseGraph();
        visited.clear();
        int componentCount=0;
        while (!stack.isEmpty()){
            T v = stack.pop();
            if(!visited.contains(v)){
                Set<T> components = new HashSet<>();
                collectComponents(v, visited, components, reverseGraph);
                stronglyConnectedComponents.put(componentCount++, components);
            }
        }

        log.info("SCC: {}", stronglyConnectedComponents);

        return stronglyConnectedComponents;

    }

    // left - right - root
    public void dfsPostOrder(T vertex, Set<T> visited, Deque<T> stack){
        visited.add(vertex);
        for(T neighbor: context.getNeighbors(vertex)){
            if(!visited.contains(neighbor)){
                dfsPostOrder(neighbor, visited, stack);
            }
        }

        stack.push(vertex);
    }

    public void collectComponents(T vertex, Set<T> visited, Set<T> components, Map<T, Set<T>> reverseGraph){
        visited.add(vertex);
        components.add(vertex);
        for(T neighbor: reverseGraph.getOrDefault(vertex, Set.of())){
            if(!visited.contains(neighbor)){
                collectComponents(neighbor, visited, components, reverseGraph);
            }
        }
    }
}
