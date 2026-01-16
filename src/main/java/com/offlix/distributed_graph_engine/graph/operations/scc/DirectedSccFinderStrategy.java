package com.offlix.distributed_graph_engine.graph.operations.scc;

import com.offlix.distributed_graph_engine.graph.core.GraphContext;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class DirectedSccFinderStrategy<T> implements SccFinderStrategy<T> {
    private final GraphContext<T> context;

    public DirectedSccFinderStrategy(GraphContext<T> context) {
        this.context = context;
    }


    @Override
    public Map<Integer, Set<T>> find() {

        Set<T> visited = new HashSet<>();
        Deque<T> stack = new ArrayDeque<>();
        Map<Integer, Set<T>> stronglyConnectedComponents = new HashMap<>();
        for (T vertex : context.getVertices()) {
            if (!visited.contains(vertex)) {
                dfsPostOrder(vertex, visited, stack);
            }
        }

        log.info("Visited: {}", visited);
        log.info("Stack: {}", stack);
        Map<T, Set<T>> reverseGraph = context.reverseGraph();
        visited.clear();
        int componentCount = 0;
        while (!stack.isEmpty()) {
            T v = stack.pop();
            if (!visited.contains(v)) {
                Set<T> components = new HashSet<>();
                collectComponents(v, visited, components, reverseGraph);
                stronglyConnectedComponents.put(componentCount++, components);
            }
        }

        log.info("SCC: {}", stronglyConnectedComponents);

        return stronglyConnectedComponents;

    }

    // left - right - root
    public void dfsPostOrder(T vertex, Set<T> visited, Deque<T> stack) {
        visited.add(vertex);
        for (T neighbor : context.getNeighbors(vertex)) {
            if (!visited.contains(neighbor)) {
                dfsPostOrder(neighbor, visited, stack);
            }
        }

        stack.push(vertex);
    }

    public void collectComponents(T vertex, Set<T> visited, Set<T> components, Map<T, Set<T>> reverseGraph) {
        visited.add(vertex);
        components.add(vertex);
        for (T neighbor : reverseGraph.getOrDefault(vertex, Set.of())) {
            if (!visited.contains(neighbor)) {
                collectComponents(neighbor, visited, components, reverseGraph);
            }
        }
    }

}
