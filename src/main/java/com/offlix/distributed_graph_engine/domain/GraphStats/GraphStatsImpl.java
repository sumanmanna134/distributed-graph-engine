package com.offlix.distributed_graph_engine.domain.GraphStats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GraphStatsImpl implements GraphStats, Serializable {
    private static final long serialVersionUID=1L;
    private volatile int vertexCount=0;
    private volatile int edgeCount=0;

    private volatile double density=0.0;


    @Override
    public synchronized void incrementVertexCount() {
        this.vertexCount++;
        updateDensity();
    }

    @Override
    public void decrementVertexCount() {
        this.vertexCount = Math.max(0, vertexCount-1);
        updateDensity();
    }

    @Override
    public void incrementEdgeCount() {
        this.edgeCount++;
        updateDensity();
    }



    @Override
    public void decrementEdgeCount() {
        decrementEdgeCount(1);
    }

    @Override
    public void decrementEdgeCount(int count) {
        if(count<0) return;
        this.edgeCount = Math.max(0, vertexCount-count);
        updateDensity();
    }


    @Override
    public void updateDensity() {
        if(vertexCount<=1){
            density=0;
        }else {
            long maxEdges = (long)vertexCount * (vertexCount-1);
            density = (double) edgeCount/maxEdges;
        }

    }

    @Override
    public GraphStats copy() {
        GraphStatsImpl copy = new GraphStatsImpl();
        copy.edgeCount = this.edgeCount;
        copy.vertexCount = this.vertexCount;
        copy.density = this.density;
        return copy;
    }

}
