package com.offlix.distributed_graph_engine.domain.graph;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.offlix.distributed_graph_engine.graph.core.GraphContext;
import com.offlix.distributed_graph_engine.util.EngineProperties;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Slf4j
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class Graph<T> implements Serializable {
    private static final long serialVersionUID=1L;
    @JsonProperty("id")
    private Long id;

    @JsonProperty("graphId")
    private String graphId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("context")
    @JsonBackReference
    private GraphContext<T> context;


    public Map<String, Object> getProperties(){
        return EngineProperties.getProperties(this.getClass(), new HashSet<>(), List.of("name"));
    }
}

