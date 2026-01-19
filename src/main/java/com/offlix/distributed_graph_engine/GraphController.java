package com.offlix.distributed_graph_engine;

import com.offlix.distributed_graph_engine.domain.GraphType;
import com.offlix.distributed_graph_engine.domain.graph.Graph;
import com.offlix.distributed_graph_engine.graph.GraphManager;
import com.offlix.distributed_graph_engine.service.GraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class GraphController {

    private static final Logger log = LoggerFactory.getLogger(GraphController.class);


    @GetMapping
    public ResponseEntity<?> graph(){
        GraphService<String> graphService = new GraphService<>();
        Graph<String> graph = graphService.createGraph("social", "social description");

        GraphManager<String> graphManager = getStringGraphManager();


        log.info("Print: Add");
        graphManager.printGraph();

        boolean b = graphManager.containCycle();
        log.info("Contain Cycle: {}", b);

        log.info("cycles : {}", graphManager.findCycles());

        Map<String, Set<String>> stringSetMap = graphManager.reverseGraph();
        log.info("reverse: {}", stringSetMap);

        Map<String, Map<String, Double>> adj = graphManager.reverseGraphWithCost();

        graphManager.sccFind();







        return ResponseEntity.ok(graphManager.reverseGraphWithCost());
    }

    private static GraphManager<String> getStringGraphManager() {
        GraphManager<String> graphManager = new GraphManager<>(GraphType.UNDIRECTED);
        // 1. Long Distance Hub
        graphManager.addEdgeBetween("DEL", "BLR", 150.0);

        graphManager.addEdgeBetween("MUM", "GOA", 45.5);

        // 3. East to North Connection
        graphManager.addEdgeBetween("CCU", "DEL", 130.0);

        // 4. South Regional Hub
        graphManager.addEdgeBetween("BLR", "HYD", 60.0);

        // 5. Southeast Coast Route
        graphManager.addEdgeBetween("MAA", "CCU", 110.5);
        graphManager.addEdgeBetween("HYD", "MAA", 50.0);
        return graphManager;
    }

    @GetMapping("/metadata")
    public ResponseEntity<?> graphMetadata(){
        Graph<?> graph = new Graph<>();
        return ResponseEntity.status(HttpStatus.OK).body(graph.getProperties());
    }



}
