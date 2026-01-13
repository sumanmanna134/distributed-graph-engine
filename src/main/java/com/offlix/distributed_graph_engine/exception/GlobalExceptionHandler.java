package com.offlix.distributed_graph_engine.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(SelfLoopExistException.class)
    public ResponseEntity<ErrorResponse> handleSelfLoopExistException(SelfLoopExistException ex){
        ErrorResponse response = ErrorResponse.of("SELF_LOOP_EXCEPTION", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(EdgeAlreadyExist.class)
    public ResponseEntity<ErrorResponse> handleEdgeAlreadyException(EdgeAlreadyExist ex){
        ErrorResponse response = ErrorResponse.of("EDGE_ALREADY_EXCEPTION", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(NoSuchMethodExistForGraphException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchMethodExistForGraphException(NoSuchMethodExistForGraphException ex){
        ErrorResponse response = ErrorResponse.of("METHOD_NOT_SUPPORTED", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}
