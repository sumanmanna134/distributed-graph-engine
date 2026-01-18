package com.offlix.distributed_graph_engine.graph.operations;

import com.offlix.distributed_graph_engine.domain.GraphType;
import com.offlix.distributed_graph_engine.graph.core.GraphContext;
import com.offlix.distributed_graph_engine.graph.operations.scc.DirectedSccFinderStrategy;
import com.offlix.distributed_graph_engine.graph.operations.scc.SccFinderStrategy;
import com.offlix.distributed_graph_engine.graph.operations.scc.UndirectedSccFinderStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SccFinder<T>{
    private static final Logger log = LoggerFactory.getLogger(SccFinder.class);
    private final GraphContext<T> context;
    private final Map<GraphType, SccFinderStrategy> strategyMap = new HashMap<>();

    public SccFinder(GraphContext<T> context) {
        this.context = context;
        this.strategyMap.put(GraphType.UNDIRECTED, new UndirectedSccFinderStrategy(context));
        this.strategyMap.put(GraphType.DIRECTED, new DirectedSccFinderStrategy(context));
    }

    public Map<Integer, Set<T>> find(){
        SccFinderStrategy<T> strategy = strategyMap.get(context.getType());
        if(strategy==null){
            throw new UnsupportedOperationException("No strategy for type: " + context.getType());
        }
        return strategy.find();
    }


}
