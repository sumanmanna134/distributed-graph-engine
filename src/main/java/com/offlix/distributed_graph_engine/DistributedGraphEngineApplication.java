package com.offlix.distributed_graph_engine;
import com.offlix.distributed_graph_engine.domain.GraphType;
import com.offlix.distributed_graph_engine.domain.graph.Graph;
import com.offlix.distributed_graph_engine.service.GraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DistributedGraphEngineApplication{

    private static final Logger log = LoggerFactory.getLogger(DistributedGraphEngineApplication.class);

    public static void main(String[] args) {
		SpringApplication.run(DistributedGraphEngineApplication.class, args);
	}
}
