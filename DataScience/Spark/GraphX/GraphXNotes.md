# GraphX Guide

## 1. Ampcamp @ Berkeley

### 1.1 Property Graph
```scala
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD

val vertexArray = Array(
  (1L, ("Alice", 28)),
  (2L, ("Bob", 27)),
  (3L, ("Charlie", 65)),
  (4L, ("David", 42)),
  (5L, ("Ed", 55)),
  (6L, ("Fran", 50))
  )
val edgeArray = Array(
  Edge(2L, 1L, 7),
  Edge(2L, 4L, 2),
  Edge(3L, 2L, 4),
  Edge(3L, 6L, 3),
  Edge(4L, 1L, 1),
  Edge(5L, 2L, 2),
  Edge(5L, 3L, 8),
  Edge(5L, 6L, 3)
)

val vertexRDD: RDD[(Long, (String, Int))] = sc.parallelize(vertexArray)
val edgeRDD: RDD[Edge[Int]] = sc.parallelize(edgeArray)
val graph: Graph[(String, Int), Int] = Graph(vertexRDD, edgeRDD)
```

### 1.2 Graph Views
```scala
// 3 ways to display the names of the users that are at least 30 years old

// Use case class for understanding
graph.vertices.filter { case (id, (name, age)) => age > 30 }
    .collect.foreach {
        case (id, (name, age)) => println(s"$name is $age")}

// use tuple position
graph.vertices.filter(v => v._2._2 > 30)
    .collect.foreach(v => println(s"${v._2._1} is ${v._2._2}"))

// 
for ((id,(name,age)) <- graph.vertices.filter { 
    case (id,(name,age)) => age > 30 }.collect) {
        println(s"$name is $age")
}

// new String Interpolation feature in Scala 2.10
val name = "Joey"; println(s"$name is ${ 3 * 10 }")
```

### 1.3 Use Triplet to show who like/love who
```scala
for (triplet <- graph.triplets.collect) {
  println(s"${triplet.srcAttr._1} likes ${triplet.dstAttr._1}")
}

for (triplet <- graph.triplets.filter(t => t.attr > 5).collect) {
  println(s"${triplet.srcAttr._1} loves ${triplet.dstAttr._1}")
}
```

### 1.4 Graph Operator

Summary of fucnitonality
```scala
/** Summary of the functionality in the property graph */
class Graph[VD, ED] {
  // Information about the Graph
  val numEdges: Long
  val numVertices: Long
  val inDegrees: VertexRDD[Int]
  val outDegrees: VertexRDD[Int]
  val degrees: VertexRDD[Int]

  // Views of the graph as collections
  val vertices: VertexRDD[VD]
  val edges: EdgeRDD[ED]
  val triplets: RDD[EdgeTriplet[VD, ED]]

  // Change the partitioning heuristic
  def partitionBy(partitionStrategy: PartitionStrategy): Graph[VD, ED]

  // Transform vertex and edge attributes
  def mapVertices[VD2](map: (VertexID, VD) => VD2): Graph[VD2, ED]
  def mapEdges[ED2](map: Edge[ED] => ED2): Graph[VD, ED2]
  def mapEdges[ED2](map: (PartitionID, Iterator[Edge[ED]]) => Iterator[ED2]): Graph[VD, ED2]
  def mapTriplets[ED2](map: EdgeTriplet[VD, ED] => ED2): Graph[VD, ED2]

  // Modify the graph structure
  def reverse: Graph[VD, ED]
  def subgraph(
      epred: EdgeTriplet[VD,ED] => Boolean = (x => true),
      vpred: (VertexID, VD) => Boolean = ((v, d) => true))
    : Graph[VD, ED]
  def groupEdges(merge: (ED, ED) => ED): Graph[VD, ED]
  // Join RDDs with the graph
  def joinVertices[U](table: RDD[(VertexID, U)])(mapFunc: (VertexID, VD, U) => VD): Graph[VD, ED]
  def outerJoinVertices[U, VD2](other: RDD[(VertexID, U)])
      (mapFunc: (VertexID, VD, Option[U]) => VD2)
    : Graph[VD2, ED]

  // Aggregate information about adjacent triplets
  def collectNeighbors(edgeDirection: EdgeDirection): VertexRDD[Array[(VertexID, VD)]]
  def mapReduceTriplets[A: ClassTag](
      mapFunc: EdgeTriplet[VD, ED] => Iterator[(VertexID, A)],
      reduceFunc: (A, A) => A)
    : VertexRDD[A]

  // Iterative graph-parallel computation
  def pregel[A](initialMsg: A, maxIterations: Int, activeDirection: EdgeDirection)(
      vprog: (VertexID, VD, A) => VD,
      sendMsg: EdgeTriplet[VD, ED] => Iterator[(VertexID,A)],
      mergeMsg: (A, A) => A)
    : Graph[VD, ED]

  // Basic graph algorithms
  def pageRank(tol: Double, resetProb: Double = 0.15): Graph[Double, Double]
  def connectedComponents(): Graph[VertexID, ED]
  def triangleCount(): Graph[Int, ED]
  def stronglyConnectedComponents(numIter: Int): Graph[VertexID, ED]
}
```

Example usecase
```scala
// get the inDegrees
val inDegrees: VertexRDD[Int] = graph.inDegrees

// Define a class to more clearly model the user property
case class User(name: String, age: Int, inDeg: Int, outDeg: Int)
// Create a user Graph
val initialUserGraph: Graph[User, Int] = graph.mapVertices{ case (id, (name, age)) => User(name, age, 0, 0) }

// Fill in the degree information
val userGraph = initialUserGraph.outerJoinVertices(initialUserGraph.inDegrees) {
  case (id, u, inDegOpt) => 
    User(u.name, u.age, inDegOpt.getOrElse(0), u.outDeg)
}.outerJoinVertices(initialUserGraph.outDegrees) {
  case (id, u, outDegOpt) => 
    User(u.name, u.age, u.inDeg, outDegOpt.getOrElse(0))
}

// output userGraph properties
for ((id, property) <- userGraph.vertices.collect) {
  println(s"User $id is called ${property.name} and is liked by ${property.inDeg} people.")
}

// who liked and beliked by same number of people
userGraph.vertices.filter {
  case (id, u) => u.inDeg == u.outDeg
}.collect.foreach {
  case (id, property) => println(property.name)
}
```

### 1.5 Neighborhood aggregation

like find the oldest follower
```scala
// Find the oldest follower for each user
val oldestFollower: VertexRDD[(String, Int)] = 
  userGraph.mapReduceTriplets[(String, Int)](
  // For each edge send a message to the destination vertex with the attribute of the source vertex
      edge => Iterator((edge.dstId, (edge.srcAttr.name, edge.srcAttr.age))),
      // To combine messages take the message for the older follower
      (a, b) => if (a._2 > b._2) a else b
  )
```

output their name
```scala
userGraph.vertices.leftJoin(oldestFollower) { 
  (id, user, optOldestFollower) =>
  optOldestFollower match {
    case None => s"${user.name} does not have any followers."
    case Some((name, age)) => s"${name} is the oldest follower of ${user.name}."
  }
}.collect.foreach { case (id, str) => println(str) }
```


average follower age
```scala
val averageAge: VertexRDD[Double] = userGraph.mapReduceTriplets[(Int, Double)](
  // map function returns a tuple of (1, Age)
  edge => Iterator((edge.dstId, (1, edge.srcAttr.age.toDouble))),
  // reduce function combines (sumOfFollowers, sumOfAge)
  (a, b) => ((a._1 + b._1), (a._2 + b._2))
  ).mapValues((id, p) => p._2 / p._1)

// Display the results
userGraph.vertices.leftJoin(averageAge) { (id, user, optAverageAge) =>
  optAverageAge match {
    case None => s"${user.name} does not have any followers."
    case Some(avgAge) => s"The average age of ${user.name}\'s followers is $avgAge."
  }
}.collect.foreach { case (id, str) => println(str) }
```

### 1.6 Subgraph

choose old people subgraph
```scala
val olderGraph = userGraph.subgraph(vpred = (id, user) => user.age >= 30)

// compute the connected components
val cc = olderGraph.connectedComponents

// display the component id of each user:
olderGraph.vertices.leftJoin(cc.vertices) {
  case (id, user, comp) => s"${user.name} is in component ${comp.get}"
}.collect.foreach{ case (id, str) => println(str) }
```

### 1.7 PageRank wiki example

## 2. Official

[Programming Guide](http://spark.apache.org/docs/latest/graphx-programming-guide.html)

Vertex RDDs

```scala
class VertexRDD[VD] extends RDD[(VertexID, VD)] {
  // Filter the vertex set but preserves the internal index
  def filter(pred: Tuple2[VertexId, VD] => Boolean): VertexRDD[VD]
  // Transform the values without changing the ids (preserves the internal index)
  def mapValues[VD2](map: VD => VD2): VertexRDD[VD2]
  def mapValues[VD2](map: (VertexId, VD) => VD2): VertexRDD[VD2]
  // Show only vertices unique to this set based on their VertexId's
  def minus(other: RDD[(VertexId, VD)])
  // Remove vertices from this set that appear in the other set
  def diff(other: VertexRDD[VD]): VertexRDD[VD]
  // Join operators that take advantage of the internal indexing to accelerate joins (substantially)
  def leftJoin[VD2, VD3](other: RDD[(VertexId, VD2)])(f: (VertexId, VD, Option[VD2]) => VD3): VertexRDD[VD3]
  def innerJoin[U, VD2](other: RDD[(VertexId, U)])(f: (VertexId, VD, U) => VD2): VertexRDD[VD2]
  // Use the index on this RDD to accelerate a `reduceByKey` operation on the input RDD.
  def aggregateUsingIndex[VD2](other: RDD[(VertexId, VD2)], reduceFunc: (VD2, VD2) => VD2): VertexRDD[VD2]
}
```

- Notice how the filter operator returns an VertexRDD. 
    + Filter is actually implemented using a BitSet
    + reusing the index and preserving the ability to do fast joins with other VertexRDDs. 
- the mapValues operators do not allow the map function to change the VertexID thereby enabling the same HashMap data structures to be reused. 
- Both the leftJoin and innerJoin are able to identify when joining two VertexRDDs derived from the same HashMap and implement the join by linear scan rather than costly point lookups.
- The aggregateUsingIndex operator is useful for efficient construction of a new VertexRDD from an RDD[(VertexID, A)]. Conceptually, if I have constructed a VertexRDD[B] over a set of vertices, which is a super-set of the vertices in some RDD[(VertexID, A)] then I can reuse the index to both aggregate and then subsequently index the RDD[(VertexID, A)]. 
- For example:

```scala
val setA: VertexRDD[Int] = VertexRDD(sc.parallelize(0L until 100L).map(id => (id, 1)))
val rddB: RDD[(VertexId, Double)] = sc.parallelize(0L until 100L).flatMap(id => List((id, 1.0), (id, 2.0)))
// There should be 200 entries in rddB
rddB.count

val setB: VertexRDD[Double] = setA.aggregateUsingIndex(rddB, _ + _)
// There should be 100 entries in setB
setB.count

// Joining A and B should now be fast!
val setC: VertexRDD[Double] = setA.innerJoin(setB)((id, a, b) => a + b)
```


EdgeRDDs
```scala
// Transform the edge attributes while preserving the structure
def mapValues[ED2](f: Edge[ED] => ED2): EdgeRDD[ED2]
// Reverse the edges reusing both attributes and structure
def reverse: EdgeRDD[ED]
// Join two `EdgeRDD`s partitioned using the same partitioning strategy.
def innerJoin[ED2, ED3](other: EdgeRDD[ED2])(f: (VertexId, VertexId, ED, ED2) => ED3): EdgeRDD[ED3]
```


### Popular Graph Algorithm

- PageRank
- TriangleCount
- Connected Components


### Example
Concurrent Topic: Check [Chapter 7](#/DataScience/Spark/AdvancedAnalytics.md)

