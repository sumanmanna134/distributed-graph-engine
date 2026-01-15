package com.offlix.distributed_graph_engine;

import com.offlix.distributed_graph_engine.domain.GraphType;
import com.offlix.distributed_graph_engine.domain.graph.Graph;
import com.offlix.distributed_graph_engine.graph.GraphManager;
import com.offlix.distributed_graph_engine.graph.core.GraphContext;
import com.offlix.distributed_graph_engine.service.GraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
//        Graph<String> graph = graphService.createGraph("social", "social description", GraphType.UNDIRECTED.name());

        GraphManager<String> graphManager = new GraphManager<>(GraphType.UNDIRECTED);
        graphManager.addEdgeBetween("mumbai", "kolkata", 1.0);
        graphManager.addEdgeBetween("kolkata", "chennai", 4);
        graphManager.addEdgeBetween("mumbai", "delhi");

        log.info("Print: Add");
        graphManager.printGraph();

        graphManager.removeEdgeBetween("kolkata", "chennai");

        log.info("Print: Remove");
        graphManager.printGraph();

        boolean b = graphManager.containCycle();
        log.info("Contain Cycle: {}", b);
//        graph.addEdge("mumbai", "kolkata", 5.0);
//        graph.addEdge("kolkata", "chennai", 2.0);
//        graph.addEdge("mumbai", "delhi", 1.5);
//
//        log.info("Print: ADD");
//        graph.printGraph();
//
//        graph.removeEdge("kolkata", "chennai");
//        graph.printGraph();

//        graph.addEdge("kolkata", "bhubeneswar", 1.2);
//        graph.addEdge("chennai", "hyderabad", 4.0);
////
////// additional edges
//        graph.addEdge("delhi", "jaipur", 1.0);
//        graph.addEdge("jaipur", "ahmedabad", 1.3);
//        graph.addEdge("ahmedabad", "mumbai", 1.1);
//
//        graph.addEdge("hyderabad", "bangalore", 0.9);
//        graph.addEdge("bangalore", "chennai", 1.4);
//
////        graph.addEdge("bhubeneswar", "kolkata", 1.2);   // cycle
//        graph.addEdge("delhi", "kolkata", 2.5);
//
//        graph.addEdge("pune", "mumbai", 0.5);
//
//        graph.addEdge("nagpur", "hyderabad", 1.8);
//        graph.addEdge("nagpur", "bhopal", 2.2);
//        graph.addEdge("bhopal", "delhi", 1.9);




//        int edge = graph.getAdjList().get("mumbai").size();
//        System.out.println(edge);
//
//
//        HashMap<String, Object> resultObj = new HashMap<>();
//        if(graph.hasCycle()){
//            resultObj.put("HasCycle", true);
//
//        }else{
//            resultObj.put("HashCycle", false);
//        }
//
//        graph.getStronglyConnectedComponents();



        return ResponseEntity.ok(null);
    }

    private void fillOrder(String vertex,Set<String> visited, Deque<String> stack, Graph<String> graph){
        visited.add(vertex);
        for(String neighbor: graph.getNeighbors(vertex)){
            if(!visited.contains(neighbor)){
                fillOrder(neighbor, visited, stack, graph);
            }
        }

        stack.push(vertex);
    }


}
