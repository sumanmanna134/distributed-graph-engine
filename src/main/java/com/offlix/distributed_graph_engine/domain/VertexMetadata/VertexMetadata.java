package com.offlix.distributed_graph_engine.domain.VertexMetadata;

public interface VertexMetadata {
    public void recordAccess();
    public VertexMetadata copy();
}
