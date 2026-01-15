package com.offlix.distributed_graph_engine.graph.operations.cycle;

import com.offlix.distributed_graph_engine.graph.core.GraphContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UndirectedCycleStrategy<T> implements CycleStrategy{
    private final GraphContext<T> context;
    private final List<List<T>> cycles;

    public UndirectedCycleStrategy(GraphContext<T> context) {
        this.context = context;
        this.cycles = new ArrayList<>();
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
    public List<List<T>> findCycles() {
       cycles.clear();
       Set<T> visited = new HashSet<>();
       List<T> path = new ArrayList<>();

       for(T vertex: context.getVertices()){
           if(!visited.contains(vertex)){
               dfs(visited, vertex, null, path);
           }
        }

       return cycles;
    }


    private boolean dfs(Set<T> visited, T current, T parent, List<T> path){
        visited.add(current);
        path.add(current);
        boolean cycleFound = false;

        for(T neighbor: context.getNeighbors(current)){
            if(neighbor.equals(parent)) continue; // skip the edge back --> parent

            // if it's visited but not the parent and exist in path,
            if(path.contains(neighbor)){
                extractCycle(path,neighbor);
                cycleFound = true;

            }
            else if(!visited.contains(neighbor) && dfs(visited, neighbor, parent, path)){
                cycleFound=true;
            }


        }

        path.remove(path.size()-1);
        return cycleFound;
    }

    private void extractCycle(List<T> path, T neighbor){
        List<T> cycle = new ArrayList<>();
        int startIndex = path.indexOf(neighbor);
        for(int i=startIndex;i<path.size();i++){
            cycle.add(path.get(i));
        }

        if(!isDuplicate(cycle)){
            cycles.add(cycle);
        }
    }

    private boolean isDuplicate(List<T> cycle){
        return cycles.stream().anyMatch(c-> c.containsAll(cycle) && c.size()==cycle.size());
    }
}
