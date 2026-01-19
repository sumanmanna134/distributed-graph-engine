package com.offlix.distributed_graph_engine.graph.operations.traversal;

public class TraversalFactory {
    @SuppressWarnings("unchecked")
    public static <T> GraphTraversal<T> getTraversal(TraversalStrategy strategy){
        return switch (strategy){
            case BFS_ITERATIVE -> (GraphTraversal<T>) new BFSIterative<>();
            case BFS_RECURSIVE -> (GraphTraversal<T>) new BFSRecursive<>();
            case DFS_RECURSIVE -> (GraphTraversal<T>) new DFSRecursive<>();
            case DFS_ITERATIVE -> (GraphTraversal<T>) new DFSIterative<>();
            case DFS_POSTORDER -> (GraphTraversal<T>) new DFSPostOrder<>();
        };
    }
}
