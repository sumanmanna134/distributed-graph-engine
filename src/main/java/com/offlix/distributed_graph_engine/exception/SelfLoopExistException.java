package com.offlix.distributed_graph_engine.exception;

public class SelfLoopExistException extends RuntimeException{
    private String message;
    public SelfLoopExistException(String message){
        super(message);
    }
}
