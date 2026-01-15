package com.offlix.distributed_graph_engine.graph.operations.cycle;

import com.offlix.distributed_graph_engine.graph.core.GraphContext;

import java.util.*;

public class DirectedCycleStrategy<T> implements CycleStrategy{
    private final GraphContext<T> context;
    private List<List<T>> cycles;
    public DirectedCycleStrategy(GraphContext<T> context) {
        this.context = context;
        this.cycles = new ArrayList<>();
    }

    /**
     * <h3>Algorithm: DFS-Based Directed Cycle Detection</h3>
     * <p>
     * This algorithm uses a Depth First Search (DFS) approach to identify "Back-Edges."
     * A back-edge is an edge that points from a node to one of its ancestors in the DFS tree.
     * </p>
     * * <ul>
     * <li><b>Step 1:</b> Initialize a <b>visited</b> set to keep track of all nodes explored so far.</li>
     * <li><b>Step 2:</b> Initialize a <b>stack</b> (Recursion Stack) to keep track of nodes in the current path.</li>
     * <li><b>Step 3:</b> Iterate through every vertex in the graph. This ensures that disconnected components are also checked.</li>
     * <li><b>Step 4:</b> For each unvisited vertex, invoke the recursive DFS.</li>
     * <li><b>Step 5:</b> Inside DFS:
     * <ul>
     * <li>Mark the current node as <b>visited</b> and add it to the <b>stack</b>.</li>
     * <li>For every neighbor:
     * <ul>
     * <li>If the neighbor is already in the <b>stack</b>, a cycle exists (Back-edge found).</li>
     * <li>If the neighbor is not visited, recurse into it.</li>
     * </ul>
     * </li>
     * <li><b>Backtrack:</b> Once all neighbors are explored, remove the node from the <b>stack</b>.</li>
     * </ul>
     * </li>
     * </ul>
     */
    @Override
    public List<List<T>> findCycles() {
        cycles.clear();
        Set<T> visited = new HashSet<>();
        LinkedHashSet<T> stack = new LinkedHashSet<>();
        for(T vertex: context.getVertices()){
            if(!visited.contains(vertex)){
                dfs(visited, stack, vertex);
            }
        }

        return cycles;
    }

    private boolean dfs(Set<T> visited, LinkedHashSet<T> stack, T current){
        visited.add(current);
        stack.add(current);
        for(T neighbor: context.getNeighbors(current)){
            if(backEdgeExist(stack, neighbor)) {
                List<T> cycle = new ArrayList<>();
                boolean startExtracting = false;
                for(T node: stack){
                    if(node.equals(neighbor)) startExtracting=true;
                    if(startExtracting) cycle.add(node);
                }
                cycles.add(cycle);
                return true;
            }// back edge exist
            else if(!visited.contains(neighbor) && dfs(visited, stack, neighbor)){
                return true;
            }
        }

        stack.remove(current);
        return false;
    }

    private boolean backEdgeExist(LinkedHashSet<T> stack, T neighbor){
        return stack.contains(neighbor);
    }
}
