package com.offlix.distributed_graph_engine.graph.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.offlix.distributed_graph_engine.domain.GraphStats.GraphStats;
import com.offlix.distributed_graph_engine.domain.GraphStats.GraphStatsImpl;
import com.offlix.distributed_graph_engine.domain.GraphType;
import com.offlix.distributed_graph_engine.domain.VertexMetadata.VertexMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class GraphContext<T> {
    @JsonProperty("type")
    private final GraphType type;

    @JsonProperty("adjacencyList")
    @Builder.Default
    private final Map<T, Map<T, Double>> adjacencyList=new ConcurrentHashMap<>();

    @JsonProperty("metadata")
    @Builder.Default
    private final Map<String, Object> metadata = new ConcurrentHashMap<>();

    @JsonProperty("vertexMetadata")
    @Builder.Default
    private final Map<T, VertexMetadata> vertexMetadata = new ConcurrentHashMap<>();

    @Builder.Default
    private Instant updatedAt=Instant.now();
    @Builder.Default
    private int version=1;


    @Builder.Default
    private GraphStats stats=new GraphStatsImpl();


    public Set<T> getVertices(){
        return adjacencyList.keySet();
    }

    public Set<T> getNeighbors(T vertex){
        return adjacencyList.getOrDefault(vertex, Map.of()).keySet();
    }

    public Optional<Map<T, Double>> getNeighborsWithEdgeWeight(T vertex){
        return Optional.ofNullable(adjacencyList.getOrDefault(vertex, Map.of()));
    }

    public int getEdgeCount(T vertex){
        return adjacencyList.get(vertex).size();
    }

    public void removeVertexFromAdjacencyList(T vertex){
        adjacencyList.remove(vertex);
    }

    public void removeVertexFromVertexMetadata(T vertex){
        vertexMetadata.remove(vertex);
    }

    public void incrementEdgeCount(){
        stats.incrementEdgeCount();
    }

    public void incrementVertexCount(){
        stats.incrementVertexCount();
    }

    public void decrementVertexCount(){
        stats.decrementVertexCount();
    }

    public void decrementEdgeCount(int edgeCount){
        stats.decrementEdgeCount(edgeCount);
    }

    public void decrementEdgeCount(){
        stats.decrementEdgeCount(1);
    }

    public void incrementVersionAndTouch(){
        this.updatedAt = Instant.now();
        this.version++;
        log.info("Graph updated at {} (version {})", updatedAt, version);
    }

    public Collection<Map<T, Double>> getAllNeighborsWithWeights(){
        return adjacencyList.values();
    }
}
