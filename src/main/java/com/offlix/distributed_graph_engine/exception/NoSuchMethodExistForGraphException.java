package com.offlix.distributed_graph_engine.exception;

import com.offlix.distributed_graph_engine.domain.GraphType;

public class NoSuchMethodExistForGraphException extends RuntimeException{
    private String message;
    private GraphType type;

    public NoSuchMethodExistForGraphException(GraphType type){
        super(String.format("No Such Method exist for %s type graph", type.name()));
    }

    public NoSuchMethodExistForGraphException(String message){
        super(String.format(message));
    }
}
