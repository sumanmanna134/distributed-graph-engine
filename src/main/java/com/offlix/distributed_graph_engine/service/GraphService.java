package com.offlix.distributed_graph_engine.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offlix.distributed_graph_engine.domain.GraphType;
import com.offlix.distributed_graph_engine.domain.graph.Graph;
import com.offlix.distributed_graph_engine.domain.graph.GraphManager;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class GraphService<T> {
    private final Map<T, Graph<T>> activeGraphs = new ConcurrentHashMap<>();

    public Graph<T> createGraph(String name, String description, String type) {
        Graph<T> graph = (Graph<T>) GraphManager.builder()
                .graphId(UUID.randomUUID().toString())
                .name(name)
                .description(description)
                .type(GraphType.valueOf(type))
                .build();


        return graph;
    }

    public void createEdge(Graph<T> graph,T source, T destination ){
        graph.addEdge(source, destination);

    }








}
