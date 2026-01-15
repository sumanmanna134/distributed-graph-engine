package com.offlix.distributed_graph_engine.graph.operations.cycle;

import com.offlix.distributed_graph_engine.graph.core.GraphContext;

import java.util.HashSet;
import java.util.Set;

public class UndirectedCycleStrategy<T> implements CycleStrategy{
    private final GraphContext<T> context;

    public UndirectedCycleStrategy(GraphContext<T> context) {
        this.context = context;
    }

    /**
     * <h3>Algorithm: DFS-Based Undirected Cycle Detection</h3>
     * <p>
     * This algorithm detects cycles in an undirected graph by performing a Depth First Search
     * and tracking the <b>parent</b> node of each vertex during exploration.
     * </p>
     * <ul>
     * <li><b>The Concept:</b> In an undirected graph, if we visit a neighbor that has
     * already been visited and is <i>not</i> the parent of the current node, then a cycle exists.</li>
     * <li><b>Step 1:</b> Initialize a <b>visited</b> set to keep track of explored vertices.</li>
     * <li><b>Step 2:</b> Iterate through all vertices in the graph to ensure all disconnected
     * components (islands) are covered.</li>
     * <li><b>Step 3:</b> For each unvisited vertex, start the recursive DFS with <code>parent = null</code>.</li>
     * <li><b>Step 4:</b> Inside DFS:
     * <ul>
     * <li>Mark the <b>current</b> node as visited.</li>
     * <li>Iterate through all neighbors of the current node.</li>
     * <li><b>Case A:</b> If the neighbor is not visited, recurse into it, setting
     * the <code>current</code> node as its parent.</li>
     * <li><b>Case B:</b> If the neighbor is already visited AND it is not the
     * parent, a cycle is confirmed.</li>
     * </ul>
     * </li>
     * </ul>
     */
    @Override
    public boolean containCycle() {
        Set<T> visited = new HashSet<>();
        for(T vertex: context.getVertices()){
            if(!visited.contains(vertex) && dfs(visited, vertex, null)){
                return true;
            }
        }

        return false;
    }


    private boolean dfs(Set<T> visited, T current, T parent){
        visited.add(current);
        for(T neighbor: context.getNeighbors(current)){
            if(!visited.contains(neighbor) && dfs(visited, neighbor, parent)){
                return true;
            }

            //visited but not parent, then cycle formed
            else if(!neighbor.equals(parent)){
                return true;
            }
        }

        return false;
    }
}
