# Architecture Deep Dive

## Executive Summary

This document explains the technical architecture of the Distributed Graph Engine, including design decisions, performance optimizations, and operational considerations for handling millions of nodes and edges.

---

## Table of Contents

1. [System Design Principles](#system-design-principles)
2. [Component Architecture](#component-architecture)
3. [Data Model](#data-model)
4. [Caching Strategy](#caching-strategy)
5. [Distributed Processing](#distributed-processing)
6. [Performance Optimization](#performance-optimization)
7. [Fault Tolerance](#fault-tolerance)
8. [Scalability Strategy](#scalability-strategy)
9. [Security Considerations](#security-considerations)
10. [Operational Excellence](#operational-excellence)

---

## System Design Principles

### SOLID Principles Application

**Single Responsibility Principle (SRP)**
- `GraphService`: Graph CRUD operations only
- `AlgorithmExecutionService`: Algorithm coordination
- `CacheStrategyService`: Multi-level caching logic
- `GraphRepository`: Data persistence abstraction

**Open/Closed Principle (OCP)**
- Algorithm interface allows new algorithms without modifying executor
- Pluggable cache strategies via strategy pattern
- Extensible through Spring configuration

**Liskov Substitution Principle (LSP)**
- All graph types (directed/undirected) implement same interface
- Cache levels are substitutable
- Repository implementations are interchangeable

**Interface Segregation Principle (ISP)**
- Separate interfaces for read vs. write operations
- Projection interfaces for lightweight queries
- Specific algorithm interfaces vs. generic execution

**Dependency Inversion Principle (DIP)**
- Services depend on abstractions (Repository interfaces)
- Configuration injected via Spring
- External systems accessed through clients (abstraction layer)

### Clean Architecture Layers

```
┌─────────────────────────────────────────┐
│        Presentation Layer               │
│  (REST Controllers, DTOs, Validation)   │
└───────────────┬─────────────────────────┘
                │
┌───────────────▼─────────────────────────┐
│        Application Layer                │
│  (Services, Use Cases, Orchestration)   │
└───────────────┬─────────────────────────┘
                │
┌───────────────▼─────────────────────────┐
│        Domain Layer                     │
│  (Graph, Algorithms, Business Logic)    │
└───────────────┬─────────────────────────┘
                │
┌───────────────▼─────────────────────────┐
│        Infrastructure Layer             │
│  (Neo4j, Redis, Hazelcast, Caching)     │
└─────────────────────────────────────────┘
```

**Layer Responsibilities:**

1. **Presentation**: HTTP, validation, serialization
2. **Application**: Orchestration, transactions, caching
3. **Domain**: Pure business logic, algorithms
4. **Infrastructure**: External systems, persistence

---

## Component Architecture

### Core Components

#### 1. Graph Manager

**Responsibility**: Lifecycle management of graph instances

```java
@Service
public class GraphService {
    private final GraphRepository repository;
    private final CacheStrategyService cache;
    private final EventPublisher eventPublisher;
    
    @Transactional
    public Graph createGraph(String name, String type) {
        // 1. Validate
        // 2. Create domain object
        // 3. Persist to Neo4j
        // 4. Warm cache
        // 5. Publish event
    }
}
```

**Design Choices:**
- `@Transactional` ensures atomicity
- Event-driven for cache invalidation
- Optimistic locking via version field

#### 2. Algorithm Executor

**Responsibility**: Coordinate algorithm execution

```java
@Service
public class AlgorithmExecutionService {
    private final Map<AlgorithmType, AlgorithmStrategy> strategies;
    
    public <R> CompletableFuture<R> execute(
        AlgorithmRequest request) {
        
        AlgorithmStrategy strategy = strategies.get(request.type);
        return strategy.executeAsync(request);
    }
}
```

**Design Choices:**
- Strategy pattern for algorithm selection
- Async execution via `CompletableFuture`
- Timeout protection via `@TimeLimiter`

#### 3. Distributed Coordinator

**Responsibility**: Manage distributed algorithm execution

**Components:**
- **Job Scheduler**: Distributes work to workers
- **Frontier Manager**: Maintains shared priority queue
- **Termination Detector**: Signals completion
- **Result Aggregator**: Combines worker results

**Redis Data Structures:**
```
ZSET astar:frontier:{jobId}     # Priority queue (score = f-score)
SET  astar:visited:{jobId}:{vertex}  # Visited nodes
STRING astar:result:{jobId}     # Final result
STRING astar:term:{jobId}       # Termination flag
```

---

## Data Model

### Graph Representation

**Adjacency List** (chosen over adjacency matrix)

**Rationale:**
- Space: O(V + E) vs. O(V²)
- Sparse graphs: Most real-world graphs have E << V²
- Neighbor iteration: O(degree(v)) vs. O(V)

**Implementation:**
```java
Map<T, Map<T, Double>> adjacencyList;
// T = vertex ID
// Map<T, Double> = neighbor -> weight
```

**Thread Safety:**
- `ConcurrentHashMap` for adjacency list
- `ReadWriteLock` for structural changes
- Atomic operations via `compute()` methods

### Neo4j Schema

```cypher
// Node
CREATE (g:Graph {
  graphId: 'uuid',
  name: 'string',
  type: 'DIRECTED|UNDIRECTED',
  createdAt: datetime(),
  stats: {
    vertexCount: int,
    edgeCount: int,
    density: float
  }
})

// Indexes
CREATE INDEX graph_id_idx FOR (g:Graph) ON (g.graphId);
CREATE INDEX graph_name_idx FOR (g:Graph) ON (g.name);
```

**Design Choices:**
- Composite stats object to reduce joins
- Denormalized for read performance
- Indexes on query patterns

### Serialization Strategy

**Graph Serialization:**
- **In-memory**: Java objects
- **Redis**: Java serialization + Base64 (compact)
- **Neo4j**: JSON (human-readable)
- **API**: JSON (standard)

**Why different formats?**
- Redis: Speed critical, binary is faster
- Neo4j: Queryability matters
- API: Interoperability with other services

---

## Caching Strategy

### Multi-Level Cache Architecture

```
Request
  │
  ├─► L1: ConcurrentHashMap (local)
  │    ├─ Hit: Return (0.5ms)
  │    └─ Miss ─┐
  │             │
  ├─► L2: Redis (distributed)
  │    ├─ Hit: Promote to L1, Return (3ms)
  │    └─ Miss ─┐
  │             │
  ├─► L3: Hazelcast (near-cache)
  │    ├─ Hit: Promote to L2+L1, Return (12ms)
  │    └─ Miss ─┐
  │             │
  └─► L4: Neo4j (database)
       └─ Load, populate all caches (80ms)
```

### Cache Eviction Policies

**L1 (Local Cache):**
- **Policy**: LRU (Least Recently Used)
- **Size**: 1000 entries
- **TTL**: 5 minutes
- **Rationale**: Hot data stays local, bounded memory

**L2 (Redis):**
- **Policy**: TTL + LRU
- **Size**: Unlimited (with maxmemory)
- **TTL**: 1 hour
- **Rationale**: Shared across instances, time-based expiry

**L3 (Hazelcast):**
- **Policy**: LRU + Near-cache
- **Size**: 70% heap
- **TTL**: 6 hours
- **Rationale**: Partition-aware, automatic rebalancing

### Cache Coherence Protocol

**Write-Through:**
```java
public void putGraph(String id, Graph graph) {
    // 1. Write to DB (source of truth)
    repository.save(graph);
    
    // 2. Async update all caches
    CompletableFuture.allOf(
        updateL1(id, graph),
        updateL2(id, graph),
        updateL3(id, graph)
    );
}
```

**Invalidation Strategy:**
```java
public void updateGraph(String id, Graph graph) {
    // 1. Invalidate all caches first
    cache.invalidateGraph(id);
    
    // 2. Update DB
    repository.save(graph);
    
    // 3. Caches repopulated on next read
}
```

**Why Invalidate vs. Update?**
- Simpler: No complex versioning
- Safer: No risk of stale data
- Lazy loading: Only cache what's used

### Cache Warming Strategies

**1. Predictive Warming:**
```java
@Scheduled(fixedRate = 300000) // 5 min
public void warmFrequentGraphs() {
    List<String> hotGraphs = metricsService
        .getTopAccessedGraphs(100);
    
    hotGraphs.forEach(id -> 
        cache.putGraph(id, repository.findById(id)));
}
```

**2. Lazy Loading with Stampede Protection:**
```java
public Graph getGraph(String id) {
    return cache.get(id, key -> {
        // Only one thread loads from DB
        synchronized(LOCKS.computeIfAbsent(key, k -> new Object())) {
            return repository.findById(key);
        }
    });
}
```

---

## Distributed Processing

### Distributed A* Algorithm

**Challenge**: Traditional A* is sequential (frontier exploration)

**Solution**: Partition frontier and coordinate via Redis

**Phases:**

**1. Initialization**
```java
// Master node
redis.zadd("frontier:jobId", hScore(start), start);
redis.set("term:jobId", "false");
```

**2. Worker Execution Loop**
```java
while (!isTerminated(jobId)) {
    // Claim batch
    List<Node> batch = redis.zpopmin("frontier:jobId", 100);
    
    for (Node node : batch) {
        // Check termination
        if (node == goal) {
            signalTermination(jobId);
            return reconstructPath();
        }
        
        // Mark visited (atomic)
        if (!redis.setnx("visited:" + node)) continue;
        
        // Expand neighbors
        for (Neighbor n : graph.neighbors(node)) {
            double fScore = gScore[node] + h(n, goal);
            redis.zadd("frontier:jobId", fScore, n);
        }
    }
}
```

**3. Termination**
```java
// First worker to reach goal
redis.set("term:jobId", "true");
redis.set("result:jobId", serializePath(path));

// Other workers check periodically
if ("true".equals(redis.get("term:jobId"))) {
    return deserialize(redis.get("result:jobId"));
}
```

### Work Stealing

**Problem**: Unbalanced work distribution

**Solution**:
```java
// If worker's batch is empty
if (myBatch.isEmpty()) {
    // Try to steal from global frontier
    List<Node> stolen = redis.zpopmin("frontier:jobId", 50);
    
    if (stolen.isEmpty()) {
        // Exponential backoff
        sleep(100 * attempts);
    }
}
```

### Coordination Overhead

**Metrics:**
- **Frontier operations**: O(log N) per insert/pop
- **Network latency**: ~1-3ms per Redis call
- **Synchronization**: Atomic operations via Redis

**Optimization Techniques:**

1. **Batching**: Claim 100 nodes at once (reduces round trips)
2. **Pipelining**: Use Redis pipelines for bulk operations
3. **Partitioning**: Shard frontier by hash(vertex)

### Fault Tolerance in Distributed Algorithms

**Worker Failure:**
- Unclaimed work remains in frontier
- Other workers will process it
- No explicit failure detection needed (eventually consistent)

**Redis Failure:**
- Circuit breaker trips
- Fall back to single-node execution
- Retry with exponential backoff

**Partial Results:**
- Workers periodically checkpoint to Redis
- On restart, resume from last checkpoint

---

## Performance Optimization

### JVM Tuning

**Garbage Collection:**
```bash
-XX:+UseG1GC                    # G1 for low-latency
-XX:MaxGCPauseMillis=200        # Target 200ms pauses
-XX:G1HeapRegionSize=16M        # Larger regions for big objects
-XX:InitiatingHeapOccupancyPercent=45  # Early mixed GC
```

**Memory:**
```bash
-Xms4g -Xmx8g                   # Heap size
-XX:MaxDirectMemorySize=2g      # Off-heap for Hazelcast
-XX:+AlwaysPreTouch             # Touch pages at startup
```

**Monitoring:**
```bash
-XX:+PrintGCDetails
-XX:+PrintGCDateStamps
-Xloggc:/var/log/gc.log
```

### Database Optimization

**Neo4j Configuration:**
```properties
# Memory
dbms.memory.heap.initial_size=4g
dbms.memory.heap.max_size=8g
dbms.memory.pagecache.size=4g

# Performance
dbms.query.cache.size=1000
cypher.planner=COST
dbms.checkpoint.interval.time=15m
```

**Connection Pooling:**
```yaml
spring.data.neo4j.pool:
  max-connections: 50
  connection-timeout: 30s
  max-lifetime: 1h
```

### Algorithm Optimization

**1. Early Termination:**
```java
// Bidirectional search
if (forwardFrontier.contains(node) && 
    backwardFrontier.contains(node)) {
    // Found meeting point
    return mergePaths();
}
```

**2. Pruning:**
```java
// A* pruning
if (fScore[neighbor] >= bestPathCost) {
    continue; // Can't improve, skip
}
```

**3. Memoization:**
```java
// Cache algorithm results
@Cacheable("algorithm-results")
public PathResult astar(String graphId, String src, String dst) {
    String cacheKey = graphId + ":" + src + ":" + dst;
    // ... algorithm logic
}
```

### Network Optimization

**HTTP/2:**
- Multiplexing: Multiple requests over single connection
- Header compression: Reduce overhead
- Server push: Proactive result streaming

**Compression:**
```yaml
server:
  compression:
    enabled: true
    mime-types: application/json
    min-response-size: 1024
```

**Connection Pooling:**
```java
@Bean
public RestTemplate restTemplate() {
    PoolingHttpClientConnectionManager cm = 
        new PoolingHttpClientConnectionManager();
    cm.setMaxTotal(200);
    cm.setDefaultMaxPerRoute(50);
    
    // ... configure
}
```

---

## Fault Tolerance

### Circuit Breaker Pattern

**Implementation:**
```java
@CircuitBreaker(
    name = "neo4j",
    fallbackMethod = "fallbackGetGraph"
)
public Graph getGraph(String id) {
    return repository.findById(id);
}

public Graph fallbackGetGraph(String id, Exception ex) {
    // Try cache
    Optional<Graph> cached = cache.getGraph(id);
    
    if (cached.isPresent()) {
        return cached.get();
    }
    
    // Return degraded response
    throw new ServiceUnavailableException(
        "Graph service temporarily unavailable");
}
```

**States:**
- **Closed**: Normal operation, tracking failures
- **Open**: Failing fast, not calling service
- **Half-Open**: Testing if service recovered

**Configuration:**
```yaml
resilience4j:
  circuitbreaker:
    instances:
      neo4j:
        failure-rate-threshold: 50      # Open at 50% failures
        wait-duration-in-open-state: 30s
        sliding-window-size: 10
        minimum-number-of-calls: 5
```

### Retry Strategy

**Exponential Backoff:**
```java
@Retry(
    name = "redis",
    fallbackMethod = "fallbackRedisOperation"
)
public String redisGet(String key) {
    return redis.get(key);
}
```

**Configuration:**
```yaml
resilience4j:
  retry:
    instances:
      redis:
        max-attempts: 3
        wait-duration: 100ms
        exponential-backoff-multiplier: 2
        retry-exceptions:
          - io.lettuce.core.RedisConnectionException
```

### Bulkhead Pattern

**Thread Pool Isolation:**
```java
@Bean(name = "graphAlgorithmExecutor")
public Executor graphAlgorithmExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(50);
    executor.setQueueCapacity(1000);
    
    // Reject policy: caller runs (backpressure)
    executor.setRejectedExecutionHandler(
        new ThreadPoolExecutor.CallerRunsPolicy());
    
    return executor;
}
```

**Why Isolate?**
- Long-running algorithms don't starve HTTP threads
- Failures isolated to specific thread pools
- Better observability (pool-specific metrics)

### Data Consistency

**Eventual Consistency:**
- Cache updates are async
- Acceptable for read-heavy workloads
- Strong consistency for writes (via Neo4j transactions)

**Conflict Resolution:**
- Last-write-wins (LWW) via timestamps
- Version numbers for optimistic locking
- Manual resolution for critical conflicts

---

## Scalability Strategy

### Horizontal Scaling

**Stateless API Nodes:**
```
┌─────────┐   ┌─────────┐   ┌─────────┐
│  API 1  │   │  API 2  │   │  API 3  │
└────┬────┘   └────┬────┘   └────┬────┘
     │            │            │
     └────────────┼────────────┘
                  │
          ┌───────▼────────┐
          │  Load Balancer │
          └────────────────┘
```

**Session Affinity:**
- Not required (stateless)
- Algorithm jobs tracked in Redis
- Sticky sessions for WebSocket (future)

### Database Scaling

**Neo4j Clustering:**
```
┌──────────┐  ┌──────────┐  ┌──────────┐
│  Core 1  │  │  Core 2  │  │  Core 3  │
│(Read/Wr) │  │(Read/Wr) │  │(Read/Wr) │
└──────────┘  └──────────┘  └──────────┘
     │            │            │
     └────────────┼────────────┘
                  │
          ┌───────▼────────┐
          │  Read Replicas │
          │  (Scale reads) │
          └────────────────┘
```

**Read Scaling:**
- Route reads to replicas
- Causal consistency for session
- Eventual consistency acceptable for analytics

### Cache Scaling

**Redis Cluster:**
- 16384 hash slots
- Auto-sharding by key hash
- Automatic rebalancing on node add/remove

**Hazelcast Scaling:**
```java
// Automatic partition rebalancing
Config config = new Config();
config.setProperty("hazelcast.partition.count", "271");
config.setProperty("hazelcast.partition.migration.interval", "0");
```

**When to Scale Each Layer:**

| Load Type | Scale Strategy |
|-----------|---------------|
| **Read-heavy** | Add API nodes + read replicas |
| **Write-heavy** | Scale Neo4j cores |
| **Algorithm-heavy** | Add Hazelcast nodes |
| **Cache pressure** | Add Redis cluster nodes |

### Partitioning Strategy

**Graph Partitioning** (future enhancement):

1. **Vertex Cut**: Partition edges, replicate vertices
2. **Edge Cut**: Partition vertices, minimize edge cuts
3. **Hybrid**: Combine both based on graph structure

**Partition Algorithms:**
- METIS: Minimize edge cuts
- Streaming: Online partitioning for dynamic graphs
- Hash-based: Simple, but unbalanced

---

## Security Considerations

### Authentication & Authorization

**JWT-based Auth:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
            .csrf().disable()
            .sessionManagement()
                .sessionCreationPolicy(STATELESS)
            .and()
            .authorizeRequests()
                .antMatchers("/api/v1/graphs/**").authenticated()
                .antMatchers("/api/v1/admin/**").hasRole("ADMIN")
            .and()
            .oauth2ResourceServer()
                .jwt();
        
        return http.build();
    }
}
```

### Data Encryption

**At Rest:**
- Neo4j: Transparent Data Encryption (TDE)
- Redis: Encrypted persistence (RDB)
- Hazelcast: Symmetric encryption

**In Transit:**
- TLS 1.3 for all HTTP traffic
- Bolt protocol over TLS for Neo4j
- Redis: TLS mode enabled

### Rate Limiting

**Token Bucket Algorithm:**
```java
@RateLimiter(name = "graphOperations")
public ResponseEntity<?> createGraph(...) {
    // Limited to N requests per time window
}
```

**Configuration:**
```yaml
resilience4j:
  ratelimiter:
    instances:
      graphOperations:
        limit-for-period: 100
        limit-refresh-period: 1s
        timeout-duration: 0
```

### Input Validation

**DTO Validation:**
```java
@Data
class CreateGraphRequest {
    @NotBlank
    @Size(min = 1, max = 200)
    @Pattern(regexp = "^[a-zA-Z0-9-_]+$")
    private String name;
    
    @Valid
    private Map<@NotBlank String, Object> metadata;
}
```

**SQL/Cypher Injection Prevention:**
- Parameterized queries only
- No dynamic query construction
- Input sanitization

---

## Operational Excellence

### Monitoring & Observability

**Metrics:**
```java
@Component
public class GraphMetrics {
    private final MeterRegistry registry;
    
    public void recordGraphOperation(String operation, long duration) {
        registry.timer("graph.operations",
            Tags.of("operation", operation))
            .record(duration, TimeUnit.MILLISECONDS);
    }
}
```

**Distributed Tracing:**
```yaml
spring:
  sleuth:
    sampler:
      probability: 0.1  # Sample 10% of requests
  zipkin:
    base-url: http://zipkin:9411
```

### Logging Strategy

**Structured Logging:**
```java
log.info("Graph operation completed",
    kv("graphId", graphId),
    kv("operation", "create"),
    kv("duration_ms", duration),
    kv("vertexCount", graph.getVertexCount()));
```

**Log Levels:**
- **ERROR**: Service failures, data corruption
- **WARN**: Degraded performance, fallbacks triggered
- **INFO**: Business events, major operations
- **DEBUG**: Detailed flow, caching behavior
- **TRACE**: Algorithm internals (disabled in prod)

### Capacity Planning

**Resource Estimation:**

```
# For 1M vertices, 5M edges graph:

Memory:
- Adjacency list: ~200 MB (avg 5 neighbors)
- Metadata: ~50 MB
- Hazelcast overhead: 2x → ~500 MB
- JVM overhead: 2x → ~1 GB per graph

CPU:
- Dijkstra: ~2s on 4 cores
- PageRank (100 iter): ~10s on 4 cores
- Distributed A* (8 workers): ~0.5s

Disk (Neo4j):
- Vertices: ~100 MB (with properties)
- Edges: ~400 MB
- Indexes: ~50 MB
- Total: ~600 MB per graph
```

**Scaling Guidelines:**
- 1 API node per 1000 concurrent users
- 1 Hazelcast node per 100 concurrent algorithms
- 1 Redis cluster node per 10K cache keys
- 1 Neo4j core per 500 write ops/sec

---

[//]: # (## Conclusion)

[//]: # ()
[//]: # (This architecture is designed for **production-grade operation** at scale, with:)

[//]: # ()
[//]: # (✅ **Performance**: Sub-100ms reads via multi-level caching)

[//]: # (✅ **Scalability**: Horizontal scaling at every layer)

[//]: # (✅ **Reliability**: Circuit breakers, retries, graceful degradation)

[//]: # (✅ **Maintainability**: Clean architecture, SOLID principles)

[//]: # (✅ **Observability**: Comprehensive metrics and tracing)

[//]: # ()
[//]: # (The system handles **millions of nodes and edges** while maintaining low latency and high availability, ready for deployment in large tech companies.)