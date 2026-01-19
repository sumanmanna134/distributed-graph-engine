package com.offlix.distributed_graph_engine.graph.operations.scc;

import com.offlix.distributed_graph_engine.graph.core.GraphContext;
import com.offlix.distributed_graph_engine.graph.operations.traversal.GraphTraversal;
import com.offlix.distributed_graph_engine.graph.operations.traversal.TraversalFactory;
import com.offlix.distributed_graph_engine.graph.operations.traversal.TraversalStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class DirectedSccFinderStrategy<T> implements SccFinderStrategy<T> {
    private final GraphContext<T> context;
    private final GraphTraversal<T> traversal;

    public DirectedSccFinderStrategy(GraphContext<T> context) {
        this.context = context;
        this.traversal = TraversalFactory.getTraversal(TraversalStrategy.DFS_RECURSIVE);
    }


    @Override
    public Map<Integer, Set<T>> find() {

        Set<T> visited = new HashSet<>();
        Deque<T> stack = new ArrayDeque<>();
        Map<Integer, Set<T>> stronglyConnectedComponents = new HashMap<>();
        GraphTraversal<T> postOrder = TraversalFactory.getTraversal(TraversalStrategy.DFS_POSTORDER);
        for (T vertex : context.getVertices()) {
            if (!visited.contains(vertex)) {
                dfsPostOrder(vertex, visited, stack);
                postOrder.traverse(context, vertex, stack::push, visited);
            }
        }

        log.info("Visited: {}", visited);
        log.info("Stack: {}", stack);
        Map<T, Map<T, Double>> reverseGraph = context.reverseGraphWithWeight();
        visited.clear();
        GraphContext<T> reverseContext = createReverseContext(reverseGraph);
        int componentCount = 0;
        while (!stack.isEmpty()) {
            T v = stack.pop();
            if (!visited.contains(v)) {
                Set<T> components = new HashSet<>();
//                collectComponents(v, visited, components, reverseGraph);
//                stronglyConnectedComponents.put(componentCount++, components);
                traversal.traverse(reverseContext, v, node->{
                    if(!visited.contains(node)){
                        visited.add(node);
                        components.add(node);
                    }
                }, new HashSet<>());
                stronglyConnectedComponents.put(componentCount++, components);
            }
        }
        return stronglyConnectedComponents;

    }

    private GraphContext<T> createReverseContext(Map<T, Map<T, Double>> reverseAdjMap){
        return GraphContext.<T>builder()
                .type(context.getType())
                .adjacencyList(reverseAdjMap)
                .build();
    }

    // left - right - root
    @Deprecated
    public void dfsPostOrder(T vertex, Set<T> visited, Deque<T> stack) {
        visited.add(vertex);
        for (T neighbor : context.getNeighbors(vertex)) {
            if (!visited.contains(neighbor)) {
                dfsPostOrder(neighbor, visited, stack);
            }
        }

        stack.push(vertex);
    }


    @Deprecated
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
