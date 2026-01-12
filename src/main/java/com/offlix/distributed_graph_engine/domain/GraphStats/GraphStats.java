package com.offlix.distributed_graph_engine.domain.GraphStats;

public interface GraphStats {
    public void incrementVertexCount();

    public void decrementVertexCount();

    public void incrementEdgeCount();

    public void decrementEdgeCount();
    public void decrementEdgeCount(int count);

    public void updateDensity();

    public GraphStats copy();

}
