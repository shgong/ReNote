# PySpark

## 1. Getting Started

Import
```
from pyspark import SparkContext, SparkConf
```

Setting Path
```
$ PYSPARK_PYTHON=python3.4 bin/pyspark
$ PYSPARK_PYTHON=/opt/pypy-2.5/bin/pypy bin/spark-submit examples/src/main/python/pi.py
```

### 1.1 SparkContext

```
conf = SparkConf().setAppName(appName).setMaster(master)
sc = SparkContext(conf=conf)
```

### 1.2 PySpark Shell

```sh
 ./bin/pyspark --master local[4]
```


## 2. Input

Parallelize Collection
```
data = [1,2,3,4,5]
distData = sc.parallelize(data);
```

Load Data

- text file
    + `distFile = sc.textFile("data.txt")`
    + `sc.wholeTextFiles`
- python class
    + pickle: python data serialization
    +  support saving an RDD in a simple format consisting of pickled Python objects.
        *  `RDD.saveAsPickleFile` 
        *  `SparkContext.pickleFile` 
- Writable support
    + types
        * unicode str: Text
        * int: IntWritable
        * float: FloatWritable, DoubleWritable
        * bool: BooleanWritable
        * bytearray: BytesWritable
        * None: NullWritable
        * dict: MapWritable
    + save & load
        * `rdd = sc.parallelize(range(1, 4)).map(lambda x: (x, "a" * x ))`
        * `rdd.saveAsSequenceFile("path/to/file")`
        * `sorted(sc.sequenceFile("path/to/file").collect())`
    + other input formats
        * example: ElasticSearch ESInput
        * custom serialized binary data (Cassandra / HBase)
            - convert scala/java side to Pyrolite's pickler
            - Converter trait
        
```py
$ SPARK_CLASSPATH=/path/to/elasticsearch-hadoop.jar ./bin/pyspark
>>> conf = {"es.resource" : "index/type"}   # assume Elasticsearch is running on localhost defaults
>>> rdd = sc.newAPIHadoopRDD("org.elasticsearch.hadoop.mr.EsInputFormat",\
    "org.apache.hadoop.io.NullWritable", "org.elasticsearch.hadoop.mr.LinkedMapWritable", conf=conf)
>>> rdd.first()         # the result is a MapWritable that is converted to a Python dict
(u'Elasticsearch ID',
 {u'field1': True,
  u'field2': u'Some Text',
  u'field3': 12345})
```


## 3. RDD Operation

- two types: transformation, actions
- lazy: do not compute right away
    + enable to run more efficiently
    + each transformed RDD may recompute
    + use persist or cache for fast access


```
lines = sc.textFile("data.txt")
lineLengths = lines.map(lambda s: len(s))
totalLength = lineLengths.reduce(lambda a, b: a + b)
```


### Passing funtions to Spark

- 3 ways
    + Lambda expression: for short code
    + Local defs in side function, for longer code
    + top level func in a module

```
"""MyScript.py"""
if __name__ == "__main__":
    def myFunc(s):
        words = s.split(" ")
        return len(words)

    sc = SparkContext(...)
    sc.textFile("file.txt").map(myFunc)
```

- or a class instance
    + require sending the object that contains the class along with method
    + but may need to send whole object to the cluster
        * when use method or instance
    + shouold better copy to local variable

```
class MyClass(object):
    def __init__(self):
        self.field = "Hello"
    def doStuff(self, rdd):
        return rdd.map(lambda s: self.field + s)

    def func(self, s):
        return s
    def doStuff2(self, rdd):
        return rdd.map(self.func)

    def doStuff3(self, rdd):
        field = self.field
        return rdd.map(lambda s: field + s)
```


### Closure

RDD operations that modify variables outside of their scope can be a frequent source of confusion.

```scala
counter = 0
rdd = sc.parallelize(data)

# Wrong: Don't do this!!
def increment_counter(x):
    global counter
    counter += x
rdd.foreach(increment_counter)

print("Counter value: ", counter)
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
    + `rdd.take(100).foreach(println)`


### Key-value pair

some operation only available on RDDs of key-value pairs
- distributed shuffle
    + group or aggregate by key
- PairRDDFunctions class
    + must define equals() and hashCode() when custom object

```
lines = sc.textFile("data.txt")
pairs = lines.map(lambda s: (s, 1))
counts = pairs.reduceByKey(lambda a, b: a + b)
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
    + collect(): return at driver program, usually with take()
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


## 6.

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
>>> broadcastVar = sc.broadcast([1, 2, 3])
<pyspark.broadcast.Broadcast object at 0x102789f10>

>>> broadcastVar.value
[1, 2, 3]
```


- Accumulators
    + add only operations
    + will display in Spark UI in scala

Built in int accumulator
```
>>> accum = sc.accumulator(0)
Accumulator<id=0, value=0>

>>> sc.parallelize([1, 2, 3, 4]).foreach(lambda x: accum.add(x))
...
10/09/29 18:41:08 INFO SparkContext: Tasks finished in 0.317106 s

scala> accum.value
10
```

Customed accumulator
```
class VectorAccumulatorParam(AccumulatorParam):
    def zero(self, initialValue):
        return Vector.zeros(initialValue.size)

    def addInPlace(self, v1, v2):
        v1 += v2
        return v1

# Then, create an Accumulator of this type:
vecAccum = sc.accumulator(Vector(...), VectorAccumulatorParam())
```
