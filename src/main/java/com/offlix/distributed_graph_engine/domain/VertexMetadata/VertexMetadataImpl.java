package com.offlix.distributed_graph_engine.domain.VertexMetadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VertexMetadataImpl implements VertexMetadata, Serializable {
    private static final long serialVersionUID=1L;

    private Map<String, Object> properties = new ConcurrentHashMap<>();
    private Instant lastAccessed=Instant.now();
    private long accessCount=0;

    @Override
    public void recordAccess(){
        this.lastAccessed = Instant.now();
        accessCount++;
    }

    @Override
    public VertexMetadataImpl copy(){
        VertexMetadataImpl copy = new VertexMetadataImpl();
        copy.properties = new ConcurrentHashMap<>(this.properties);
        copy.lastAccessed = this.lastAccessed;
        copy.accessCount = this.accessCount;
        return copy;
    }



}
