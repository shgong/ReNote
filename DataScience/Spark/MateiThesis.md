# An Architecture for Fast and General Data Processing on Large Clusters

## 1. Introduction

- Problem with Specialized System
    + Repeat Work to solve same underlying problem like distribution and fault tolerance repeatedly
    + Composition is expensive and unwieldly in different systems
    + Resource sharing across computing engines
    + More work for management and administration

+ Solution: RDD
    * a simple extension to the MR model
    * mapreduce lacks efficient data sharing across computation stage
        - just add data sharing greatly increase generality of MR
        - MR can emulate BSP model of parallel computing
        - RDD give enough control to optimize bottleneck (I/O)
    * compare fault-tolerant
        - MapReduce: Replication
        - Spark: Lineage recomputation

## 2. Resilient Distributed Datesets

### 2.1 Introduction

- Observation: most data flow models require efficient data sharing
    + model example
        - iterative algorithm like pagerank, k-means, logistic regression: make multiple pass over same dataset
        - interactive data mining: running multiple query in same subset
        - streaming application: maintain and share state across time
    + solution
        - general: write to external stable storage like HDFS, with I/O, serialization problems
        - Pregel: iterative graph computation that keep intermediate state in memory
        - HaLoop: iterative MR that keep data efficiently partitioned
        - above support only specific patterns & lack reuse
- RDD: direct control of data sharing
    + coarse-grained transformation interface
    + provide fault tolerance by loggin transformation by lineage

### 2.2 RDD Abstraction

user can control three aspect of RDDs: transformation, persistence and partitioning

Example: Console Log Mining
```scala
lines = spark.textFile("hdfs://...")
errors = lines.filter(_.startsWith("ERROR"))
errors.persist()

// Count errors mentioning MySQL:
errors.filter(_.contains("MySQL")).count()
// Return the time fields of errors mentioning HDFS as an array, assuming
// time is field number 3 in a tab-separated format:
errors.filter(_.contains("HDFS"))
     .map(_.split(’\t’)(3))
     .collect()
```

Advantage of RDD over DSM (distributed shared memory)

- fault tolerance
    + RDD can only be write through coarse-grained transformations
        * restrict to bulk write application
        * but allow efficient fault tolerance
    + DSM allow r/w to each memory location
        * fine grained
        * need checkpoints and program rollback
- mitigate slow notes
    + RDD allow slow nodes(straggers) running backup copies
    + two copies of DSM task would access same memory location, interfere
- RDD degrade gracefully when not enough memory

### 2.3 Spark Programming Interface

Developer write a driver program that connect cluster of workers. 

```scala
// logistic regression
val points = spark.textFile(...).map(parsePoint).persist()
var w = // random initial vector
for (i <- 1 to ITERATIONS) {
  val gradient = points.map { p =>
    p.x * (1 / (1 + exp(-p.y * (w dot p.x))) - 1) * p.y
  }.reduce((a,b) => a + b)
  w -= gradient
}

// --------- pagerank ----------
// Load graph as an RDD of (URL, outlinks) pairs
val links = spark.textFile(...).map(...).persist()
var ranks = // RDD of (URL, rank) pairs
for (i <- 1 to ITERATIONS) {
  // RDD (targetURL, float) pairs with contributions sent by each page
  val contribs = links.join(ranks).flatMap {
    case (url, (links, rank)) =>
     links.map(dest => (dest, rank/links.size))
  }
  // Sum contributions by URL and get new ranks
  ranks = contribs.reduceByKey((x,y) => x+y).mapValues(sum => a/N + (1-a)*sum)
}
```


### 2.4 Representing RDDs

- five piece of information
    + atomic pieces of dataset
    + dependencies on parent RDDs
    + function of computing based on parent
    + metadata about its partition scheme
    + metadata about data placement
- dependency
    + narrow dependencies: allow pipelined execution on one cluster node
        * map, filter, union, co-partitioned join
    + wide dependencies: require data from all parents
        * groupByKey, non-co-partitioned join

## 3. Models Built over RDDs

### 3.1 Technique for implementation

- Efficient to store multiple items in same RDD record
    + RDD in memory: pointer can be used to read only relevant part of a composite record for each operation. (Int, Float) record may be implemented as two arrays, avoid scanning the second part
    + connect computation models: the low level interface to RDDs is based on iterators, which enable fast, pipelined conversion between formats. Iterator are efficient interface for in-memory data, they typically perform scans.
- Data Partitioning
    + speed up join, groupBy join
    + compound data structure, where some fields updated and some are not, one mutable and one immutable, like pages link/rank and graph vertex/edge
- Immutability
    + RDD are immutable, like link in PageRank
    + pointer can reuse state when internal structure are immutable
        * strings in java are immutable
        * (Int,String) change Int, can keep pointer to String without copy
- Custom Transformation
    + mapPartitions: give RDD[T], iterator[T]=>iterator[U], return RDD[U] by apply it to each partition. close to lowest level interface to RDDs

## 3.2 Shark SQL

- 3-step process similar to RDBMS
    + query parsing: Hive query compiler => abstract syntax tree
    + logical plan generation: from tree
    + physical plan generation
        * Hive: multiple MR stages
        * Shark: rule-based optimization, like pushing limit down to individual partitions, create trasformation plan instead of MR
- Memory Store
    + deserialization is the major bottleneck, default store as JVM object collection avoids deserialization
    + Shark stores all column of primitive types as JVM primitive arrays, complex types like hive map & array are serializaed and concatenated into a single byte arrays
    + so each column => only one JVM object, fast GC & compact representation
- Co-partition
    + for frequently joined together tables
    + common co-join key, add distribute by clause
- Partition Statistics & Map Pruning
    + data clustered on more columns, 
        * eg. entries in web traffic log may be grouped by physical location from each data center, and roughly chronological order inside centers.
    + map prune: prune data partitions based on natural clustering column. Shark memory split data into small block, avoid scanning block when values fall out of filter range.
    + Shark memory store collect statistics
        * range of each column
        * distinct value set (if small)
    + evaluate query's predicates against statistics
- Partial DAG Execution (PDE)
    + need of query fresh data without loading process (cannot use statistics)
        * support dynamic query optimization, alter query plan based on statistics at run-time 
        * apply PDE at blocking shuffle operator (most expensive)
    + PDE modify shuffle process
        * gather customizable statistics at global and per-partition granularies while materializing map outputs
        * allow DAG to be altered based on statistics, by choosing different operator or altering parameters
    + customizable using accumulator API
        * partition sizes and record counts
        * lists of heavy hitters, like frequent items
        * approximate histogram

## 4. Discretized Streams

- compute series of stateless, deterministic batch on small interval
    + instead of managing long-lived operators
- challenge
    + make latency low
        * build on RDD data structure, within 1 second
    + recover quickly from faults and stragglers
        * parallel recovery
        * each node in cluster works to recover part of lost node RDD
        * hard to implement in continuous system, but simple with full deterministic D-Stream model

### Computation Model

- series of deterministic batch computation
    + each interval data stored reliably across cluster form a input
    + DStream is sequence of immutable partitioned RDDs

```scala
pageViews = readStream("http://...", "1s")
ones = pageViews.map(event => (event.url, 1))
counts = ones.runningReduce((a, b) => a + b)
```

also support stateful transformation

```scala
// windowing
words.window("5s")

// incremental aggregating
pairs.reduceByWindow("5s", _+_)

// site tracking
// count session from stream of (ClientID, Event) pair
sessions = events.track(
  (key, ev) => 1, // initialize function
  (key, st, ev) => (ev == Exit ? null : 1), // update function
  "30s") // timeout
counts = sessions.count() // a stream of ints

```
