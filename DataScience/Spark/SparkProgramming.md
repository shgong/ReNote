# Spark in Scala

## 1. Getting Started

### 1.1 SparkContext

Create a SparkContext object, first build a spark conf

```
val conf = new SparkConf().setAppName(appName).setMaster(master)
new SparkContext(conf)
```

and then

```
bin/spark-submit
```

###1.2 Spark Shell

-  a special interpreter-aware SparkContext is already created for you, in the variable called sc. Making your own SparkContext will not work. 

```
spark-shell --master local[4]
spark-shell --master yarn-client
```

## 2. Input

Parallelize Collection
```
val data = Array(1,2,3,4,5)
val distData = sc.parallelize(data);
```

Load Data
```scala
val distFile = sc.textFile("data.txt")
val distFile = sc.textFile("directory/*.gz", 10)
```

- support running on directories, compressed files and wildcards
- can control partitions, 64MB by default
- other data formats
    + sc.wholeTextFile: read directory, return (filename, content) pairs
    + sc.sequenceFile[IntWritable, Text]
        * can also auto interpret [Int, String]
    + sc.hadoopRDD
    + sc.newAPIHadoopRDD: use new MR API formats like HBase


## 3. RDD

- two types: transformation, actions
- lazy: do not compute right away
    + enable to run more efficiently
    + each transformed RDD may recompute
    + use persist or cache for fast access

```scala
val lines = sc.textFile("data.txt")
val lineLengths = lines.map(s => s.length)
// If we also wanted to use lineLengths again later, we could add:
lineLengths.persist()
val totalLength = lineLengths.reduce((a, b) => a + b)
```

### Passing funtions to Spark

- two ways
    + Anonymous function syntax: for short code
    + Static methods in a global singleton object

```
object MyFunctions {
  def func1(s: String): String = { ... }
}
myRdd.map(MyFunctions.func1)
```

- or a class instance
    + require sending the object that contains the class along with method
    + but may need to send whole object to the cluster
        * when use method or instance
    + shouold better copy to local variable

```
class MyClass {
    val field = "Hello"
    def doStuff(rdd: RDD[String]): RDD[String] = { rdd.map(func1) }

    def func1(s: String): String = { ... }
    def doStuff2(rdd: RDD[String]): RDD[String] = { rdd.map(x => field + x) }

    def doStuff(rdd: RDD[String]): RDD[String] = {
      val field_ = this.field
      rdd.map(x => field_ + x)
    }
}
```


### Closure

RDD operations that modify variables outside of their scope can be a frequent source of confusion.

```scala
var counter = 0
var rdd = sc.parallelize(data)

// Wrong: Don't do this
rdd.foreach(x => counter += x)

println("Counter value: " + counter)
```

#### local vs cluster

- To execute jobs, Spark breaks up the processing of RDD operations into tasks, each of which is executed by an executor. 
- Closure
    + Spark compute task closure before execution.
    + closure: variables and methods which must be visible for executor to perform computations on RDD, in this case `foreach()`
    + this closure is serialized and send to each executor
- Counter
    + when counter is referenced within foreach func
    + executor only see copy from serialized closures
    + the final value of counter will still be zero (if not local mode)
- Solution
    + use Accumulator

#### printing

- rdd.foreach(println) or rdd.map(println)
    + in cluster mode, stdout won't show this
- use collect()
    + bring RDD to the driver node
    + `rdd.collect().foreach(println)`
    + but can make driver node out of memory
- print part
    + `rdd.collect().take(100).foreach(println)`


### Key-value pair

some operation only available on RDDs of key-value pairs
- distributed shuffle
    + group or aggregate by key
- PairRDDFunctions class
    + must define equals() and hashCode() when custom object

```
//line count
val lines = sc.textFile("data.txt")
val pairs = lines.map(s => (s, 1))
val counts = pairs.reduceByKey((a, b) => a + b)

//line sort
pairs.sortByKey()
counts.collect()
```

## 4. Transformations

- map(func)
    + map(func): pass each element through
    + flatmap(func): map that return Seq, flat output
    + mapPartitions(func): run separat on each partiiton, 
        * Iterator<T>=>Iterator<U>
    + mapPartitionsWithIndex(func): with partition index
        * type(Int, Iterator<T>) => Iterator<U>
- filter(func): select element on which func returns true
- sample(withReplacement, fraction, seed)
    + sample fraction, with/without, using given random seed
- KeyValue
    + groupByKey([numTasks])
        * (K,Iterable<V>)
    + reduceByKey(func,[numTasks])
        * (V,V)=>V --- (K, V)
    + aggregateByKey(zeroValue)(seqOp,comboOp,[numTasks])
        * zero value, combine function
    + sortByKey([ascending],[numTasks])
        * (K,V)
        * true: ascend, false: descend
- Datasets
    + distinct([numTasks]): return distinct elements
    + union(other)
    + intersection(other)
    + join(other, [numTasks]):  (K, (V1,V2))
    + cogroup(other,[numTasks]): (K,(Iterable<V>,Iterable<W>))
    + cartesian(other): type T,U => (T,U) pairs
- Partition
    + pipe(command): pipe partition through shell command
    + coalesce(num): decrease partitions to num
        * more efficient after filtering large dataset
    + repartition(num): reshuffle all data
    + repartitionAndSortWithinPartitions(partitioner)

## 5. Actions

- Utility
    + reduce(): commutative & associative
    + collect(): return at driver program,  with take()
    + count(): count number
    + countByKey()
    + foreach(func): run on each element
        * only updating an accumulator (see closure)
        * interacting with external storage systems
- sample
    + first(): take(1)
    + take(n): take first n
    + takeSample(withReplace,num,[seed]): random sample
    + takeOrdered(n,[comparator]): first n natural order or custom
- save
    + saveAsTextFile(path)
    + saveAsSequenceFile(path)
    + saveAsObjectFile(path)
        * simple java serialization
        * load using SparkContext.objectFile()


## 6. Others

### 6.1 Shuffle

- Why Shuffle
    + Spark sometimes should re-distributing data
    + data is generally not distributed across partitions
    + but for all-to-all operation like reduceByKey, need organize all data
- Shuffle
    + all to one then one to all
    + operations
        * repartition, coalesce
        * groupByKey, reduceByKey, sortByKey
        * cogroup, join
    + element are not ordered
        * mapPartitions(sort)
        * repartitionAndSortWithinPartitions
        * sortBy
- Performance Impact
    + very expensive, disk I/O, serialization and network I/O
    + Spark generate sets of map reduce tasks, significant heap memory, large intermediate file
    + can configure Shuffle behavior

### 6.2 Persisence

- when persist an RDD, 
    + each node stores any partitions of it in memoery.
    + allow following actions much faster (x10)
- call 
    + persist(): choose storage level
    + cache(): use default level, StorageLevel.MEMEORY_ONLY
- fault-tolerant: rebuild by the RDD lineage
- can store using different level
    + MEMORY_ONLY: deserialized java objecs
    + MEMORY_AND_DISK: store on disk when not fit in memory
    + MEMORY_ONLY_SER: serialized Java objects, space efficient
    + MEMORY_AND_DISK_SER
    + DISK_ONLY
    + MEMORY_ONLY_2: replicate 
    + MEMORY_AND_DISK_2: replicate, etc
    + OFF-HEAP: Tachyon. reduce gc, share memory pool
- always pickle in python

### 6.3 Shared Variables

- Broadcast Variable
    + key read-only variable cached on each machine

```
scala> val broadcastVar = sc.broadcast(Array(1, 2, 3))
broadcastVar: org.apache.spark.broadcast.Broadcast[Array[Int]] = Broadcast(0)

scala> broadcastVar.value
res0: Array[Int] = Array(1, 2, 3)
```


- Accumulators
    + add only operations
    + will display in Spark UI in scala

Built in int accumulator
```
scala> val accum = sc.accumulator(0, "My Accumulator")
accum: spark.Accumulator[Int] = 0

scala> sc.parallelize(Array(1, 2, 3, 4)).foreach(x => accum += x)
...
10/09/29 18:41:08 INFO SparkContext: Tasks finished in 0.317106 s

scala> accum.value
res2: Int = 10
```

Customed accumulator
```
object VectorAccumulatorParam extends AccumulatorParam[Vector] {
  def zero(initialValue: Vector): Vector = {
    Vector.zeros(initialValue.size)
  }
  def addInPlace(v1: Vector, v2: Vector): Vector = {
    v1 += v2
  }
}

// Then, create an Accumulator of this type:
val vecAccum = sc.accumulator(new Vector(...))(VectorAccumulatorParam)
```
