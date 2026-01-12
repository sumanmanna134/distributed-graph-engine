package com.offlix.distributed_graph_engine;

import com.offlix.distributed_graph_engine.domain.GraphType;
import com.offlix.distributed_graph_engine.domain.graph.Graph;
import com.offlix.distributed_graph_engine.service.GraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
public class GraphController {

    private static final Logger log = LoggerFactory.getLogger(GraphController.class);

    @GetMapping
    public ResponseEntity<?> graph(){
        GraphService<String> graphService = new GraphService<>();
        Graph<String> graph = graphService.createGraph("social", "social description", GraphType.UNDIRECTED.name());
        graph.addEdge("mumbai", "kolkata", 5.0);
        graph.addEdge("kolkata", "chennai", 2.0);
        graph.addEdge("mumbai", "delhi", 1.5);
        graph.addEdge("kolkata", "bhubeneswar", 1.2);
        graph.addEdge("kolkata", "bhubeneswar", 1.2);
        log.info("Graph type: {}", graph.getType());
//        graph.addEdge("chennai", "hyderabad", 4.0);
//
//// additional edges
//        graph.addEdge("delhi", "jaipur", 1.0);
//        graph.addEdge("jaipur", "ahmedabad", 1.3);
//        graph.addEdge("ahmedabad", "mumbai", 1.1);
//
//        graph.addEdge("hyderabad", "bangalore", 0.9);
//        graph.addEdge("bangalore", "chennai", 1.4);
//
//        graph.addEdge("bhubeneswar", "kolkata", 1.2);   // cycle
//        graph.addEdge("delhi", "kolkata", 2.5);
//
//        graph.addEdge("pune", "mumbai", 0.5);
//        graph.addEdge("mumbai", "pune", 0.5);
//
//        graph.addEdge("nagpur", "hyderabad", 1.8);
//        graph.addEdge("nagpur", "bhopal", 2.2);
//        graph.addEdge("bhopal", "delhi", 1.9);


        HashMap<String, Object> resultObj = new HashMap<>();
        if(graph.hasCycle()){
            resultObj.put("HasCycle", true);

        }else{
            resultObj.put("HashCycle", false);
        }

        resultObj.put("graph", graph);




        return ResponseEntity.ok(resultObj);
    }
}
