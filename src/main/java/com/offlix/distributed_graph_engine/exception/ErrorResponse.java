package com.offlix.distributed_graph_engine.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        @JsonProperty("timestamp")
        Instant timestamp,
        @JsonProperty("error")
        String error,
        @JsonProperty("message")
        String message,
        @JsonProperty("details")
        List<String> details
        ) {

    public static ErrorResponse of(String error, String message){
        return new ErrorResponse(Instant.now(), error, message, Collections.emptyList());
    }

    public static ErrorResponse of(String error,String message, List<String> details){
        return new ErrorResponse(Instant.now(), error,message, details);
    }






}
