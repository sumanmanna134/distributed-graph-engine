package com.offlix.distributed_graph_engine.service;


import com.offlix.distributed_graph_engine.domain.GraphType;
import com.offlix.distributed_graph_engine.domain.graph.Graph;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class GraphService<T> {
    private final Map<T, Graph<T>> activeGraphs = new ConcurrentHashMap<>();

    public Graph<T> createGraph(String name, String description) {
        Graph<T> graph = (Graph<T>) Graph.builder()
                .graphId(UUID.randomUUID().toString())
                .name(name)
                .description(description)
                .build();


        return graph;
    }

    public void createEdge(Graph<T> graph,T source, T destination ){


    }








}
