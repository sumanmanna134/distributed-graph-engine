package com.offlix.distributed_graph_engine.exception;

public class EdgeAlreadyExist extends RuntimeException{
    private String message;
    public EdgeAlreadyExist(Object source, Object destination){
        super(String.format("Edge already exist %s to %s", source.toString(), destination.toString()));
    }
}
