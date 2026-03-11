package com.offlix.distributed_graph_engine.graph.operations.scc;

import com.offlix.distributed_graph_engine.graph.core.GraphContext;
import com.offlix.distributed_graph_engine.graph.operations.traversal.GraphTraversal;
import com.offlix.distributed_graph_engine.graph.operations.traversal.TraversalFactory;
import com.offlix.distributed_graph_engine.graph.operations.traversal.TraversalStrategy;

import java.util.*;

public class UndirectedSccFinderStrategy<T> implements SccFinderStrategy<T> {
    private final GraphContext<T> context;
    private final GraphTraversal<T> traversal;

    public UndirectedSccFinderStrategy(GraphContext<T> context) {
        this.context = context;
        this.traversal = TraversalFactory.getTraversal(TraversalStrategy.DFS_RECURSIVE);
    }

    /**
     * <p>
     * Implementation of a strategy to find Connected Components in an undirected graph.
     * In an undirected context, Strongly Connected Components (SCCs) are identical to
     * standard Connected Components.
     * </p>
     * * <h3>Algorithm Overview:</h3>
     * <div style="border: 1px solid #ccc; padding: 10px; background-color: #00000;">
     * <b>Step-by-Step Logic:</b>
     * <ol>
     * <li>Initialize a <code>visited</code> Set and a <code>componentsMap</code>.</li>
     * <li>Iterate through every <b>Vertex</b> in the graph context.</li>
     * <li>If the vertex has <b>not</b> been visited:
     * <ul>
     * <li>Increment the <code>componentId</code>.</li>
     * <li>Launch a <b>Depth First Search (DFS)</b> starting from this vertex.</li>
     * <li>During DFS, mark every reachable neighbor as visited and add them to the current component set.</li>
     * </ul>
     * </li>
     * <li>Repeat until all vertices have been processed.</li>
     * </ol>
     * </div>
     * <h3>Complexity:</h3>
     * <ul>
     * <li><b>Time Complexity:</b> O(V + E), where V is the number of vertices and E is the number of edges.</li>
     * <li><b>Space Complexity:</b> O(V) to store the visited status and the resulting sets.</li>
     * </ul>
     */
    @Override
    public Map<Integer, Set<T>> find() {
        Set<T> visited = new HashSet<>();
        Map<Integer, Set<T>> components = new HashMap<>();
        int componentId = 0;
        for (T vertex : context.getVertices()) {
            if (!visited.contains(vertex)) {
                Set<T> component = new HashSet<>();
//                dfs(visited, vertex, component);
                traversal.traverse(context, vertex, component::add, visited);
                components.put(componentId++, component);
            }


        }

        return components;

    }

    /**
     * <p>Recursive Depth First Search to explore all nodes within a single connected cluster.</p>
     * @param vertex Current node being explored.
     * @param visited Global set of nodes already processed.
     * @param components The set representing the current component being built.
     */
    @Deprecated
    private void dfs(Set<T> visited, T vertex, Set<T> components){
        visited.add(vertex);
        components.add(vertex);
        for(T neighbor: context.getNeighbors(vertex)){
            if(!visited.contains(neighbor)){
                dfs(visited, neighbor, components);
            }
        }

    }
}
