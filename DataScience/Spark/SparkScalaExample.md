# Spark Example

## Compile Scala on CDH

[Maven](https://blog.cloudera.com/blog/2014/04/how-to-run-a-simple-apache-spark-app-in-cdh-5/)
[SBT](http://spark.apache.org/docs/latest/quick-start.html)

## Word filter and count example

```scala
// load file
    val content = sc.textFile("spark2/Content.txt")
    val remove = sc.textFile("spark2/Remove.txt") 

// flatmap filter words, trim spaces
    val removeRDD= remove.flatMap(x=> x.split(",") ).map(word=>word.trim) 

// broadcast aggregated removeList
    val bRemove = sc.broadcast(removeRDD.collect().toList)
    val words = content.flatMap(line => line.split(" "))

// filter{ case()=> rule } // case class
    val filtered = words.filter{case (word) => !bRemove.value.contains(word)}
    val pairRDD = filtered.map(word => (word,1))
    val wordCount = pairRDD.reduceByKey(_ + _)
    wordCount.saveAsTextFile("result.txt")

```

sort word count
```scala
// swap key-value, sort by key
val swapped = wordCount.map(item => item.swap)
val sortedOutput = swapped.sortByKey(false)
```

compress
```scala
    import org.apache.hadoop.io.compress.GzipCodec
    sortedOutput.saveAsTextFile("spark3/compressedresult", classOf[GzipCodec])
```

collect and group by key
```scala
    val joined = namePairRDD.join(salaryPairRDD).values
    val grpByKey = joined.map(_.swap).groupByKey().collect()
    // need seq all value for a key,  collect
    //case class, ouput key->value
    val rddByKey = grpByKey.map{case (k,v) => k->sc.makeRDD(v.toSeq)}  
    // foreach operation, need collect
    rddByKey.foreach{ case (k,rdd) => rdd.saveAsTextFile("spark/Employee"+k)}
```

removeHead and zip
```scala
// remove head
    val headerAndRows = csv.map(line => line.split(",").map(_.trim))
    val header = headerAndRows.first
    val data = headerAndRows.filter(_(0) != header(0))
// zip and map
    val maps = data.map(x => header.zip(x).toMap)
    val result = maps.filter(map => map("id") != "myself")
```

save to only one file: repartition
```
swappedBack.repartition(1).saveAsTextFile("spark7/result.txt")
```

combine by key
```scala
//     (video,youtube) (video,netflix)
// ::: list concaten, :: list append
val combinedOutput = namePairRDD.combineByKey(List(_), (x:List[String], y:String) =>y:: x, (x:List[String], y:List[String]) =>x ::: y)
//     (video, List(youtube,netflix))
```

save RDD as sequence file
```scala
import org.apache.hadoop.io.compress.GzipCodec
rdd.map( bytesArray => (NullWritable.get(), new BytesWritable(bytesArray))).saveAsSequenceFile("/output/path",classOf[GzipCodec])
```

map match
```scala
val one = sc.textFile("spark16/file1.txt").map{
    _.split(",",-1) match {
        case Array(a, b, c) => (a, (b, c))
    }
}
val sum = joined.map {
        case (_, ((_, num2), (_, _))) => num2.toInt
}.reduce(_ + _)

```

Blank replacement while maintain structure, double map
```scala
    val mapper = field.map(x=> x.split(",",-1))
    mapper.map(x => x.map(x=> {if(x.isEmpty) 0 else x})).collect
```

case class field
```scala
//create & load case class
case class Employee(dep: String, des: String, cost: Double, state: String)
val employees = rawlines.map( _.split(",")).map(row=>Employee(row(0), row(1), row(2).toDouble, row(3)))

val keyVals = employees.map( em => ((em.dep, em.des, em.state), (1 , em.cost)))
val results = keyVals.reduceByKey{ (a,b) => (a._1 + b._1, a._2 + b._2)} // (a.count + b.count, a.cost + b.cost)}
```

makeRDD
```scala
val file1word = sc.makeRDD(Array(file1.name+"->"+content1(0)._1+"-"+content1(0)._2))
val unionRDDs = file1word.union(file2word).union(file3word).union(file4word)
```

Join by first,last name tuple
```scala
val joined = technology.map(e=>((e(0),e(1)),e(2))).join(salary.map(e=>((e(0),e(1)),e(2))))
```

Partition Index
```scala
val z = sc.parallelize(List(1,2,3,4,5,6), 2)
//type(Int, Iterator<T>) => Iterator<U>
def myfunc(index: Int, iter: Iterator[(Int)]): Iterator[String] = {
    iter.toList.map(x => "[partID:" + index + ", val: " + x + "]").iterator
}
z.mapPartitionsWithIndex(myfunc).collect
// res28: Array[String] = Array([partID:0, val: 1], [partID:0, val: 2], [partID:0, val: 3], [partID:1, val: 4], [partID:1, val: 5], [partID:1, val: 6])
```

Aggregate
```scala
// aggregate(zeroValue)(seqOp,comboOp)
// aggregate function allows the user to apply two different reduce functions to the RDD. The first reduce function is applied within each partition to reduce the data within each partition into a single result. The second reduce function is used to combine the different reduced results of all partitions together to arrive at one final result. 
val z = sc.parallelize(List(1,2,3,4,5,6), 2)
z.aggregate(5)(math.max(_, _), _ + _) // 5,3->5  5,6->6  5+5+6=16
```

AggregateByKey
```scala
// inside partition, between partitions
    import scala.collection._

    val keysWithValuesList = Array("foo=A","foo=A","foo=A","foo=A","foo=B","bar=C","bar=D","bar=D");

    val data = sc.parallelize(keysWithValuesList)

    //Create key value pairs
    val kv = data.map(_.split("=")).map(v => (v(0), v(1))).cache()

    val initialCount = 0;
    val addToCounts = (n: Int, v: String) => n + 1
    val sumPartitionCounts = (p1: Int, p2: Int) => p1 + p2
    val countByKey = kv.aggregateByKey(initialCount)(addToCounts, sumPartitionCounts)
//  countByKey.collect
//  res3: Array[(String, Int)] = Array((foo,5), (bar,3))

    val initialSet = scala.collection.mutable.HashSet.empty[String]
    val addToSet = (s: mutable.HashSet[String], v: String) => s += v
    val mergePartitionSets = (p1: mutable.HashSet[String], p2: mutable.HashSet[String]) => p1 ++= p2
    val uniqueByKey = kv.aggregateByKey(initialSet)(addToSet, mergePartitionSets)
//  uniqueByKey.collect
//  res4: Array[(String, scala.collection.mutable.HashSet[String])] = Array((foo,Set(B, A)), (bar,Set(C, D)))
```


CombineByKey & Average Score
```scala

    type ScoreCollector = (Int, Double);
    type PersonScores = (String, (Int, Double));

    val initialScores = Array(("Fred", 88.0), ("Fred", 95.0), ("Fred", 91.0), ("Wilma", 93.0), ("Wilma", 95.0), ("Wilma", 98.0))
    val WilmaAndFredScores = sc.parallelize(initialScores).cache()

    val createScoreCombiner = (score: Double) => (1, score)

    val scoreCombiner = (collector: ScoreCollector, score: Double) => {
    val (numberScores, totalScore) = collector
    (numberScores + 1, totalScore + score)
    }

    val scoreMerger = (collector1: ScoreCollector, collector2: ScoreCollector) => {
    val (numScores1, totalScore1) = collector1
    val (numScores2, totalScore2) = collector2
    (numScores1 + numScores2, totalScorel + totalScore2)
    }

    val scores = WilmaAndFredScores.combineByKey(createScoreCombiner, scoreCombiner, scoreMerger)

    val averagingFunction = (personScore: PersonScores) => {
        val (name, (numberScores, totalScore)) = personScore (name, totalScore / numberScores)
    }

    val averageScores = scores.collectAsMap().map(averagingFunction)
    Expected output: averageScores: scala.collection.Map[String,Double] = Map(Fred -> 91.33333333333333, Wilma -> 95.33333333333333)

```

Cogroup
```scala
    val a = sc.parallelize(List(1, 2,1, 3), 1)
    val b = a.map((_, "b"))
    // (1,b) (2,b) (1,b) (3,b)
    val c = a.map((_, "c"))
    // (1,c) (2,c) (1,c) (3,c)
    b.cogroup(c).collect

    Array[(Int, (Iterable[String], Iterable[String]))] = Array(
        (2,(ArrayBuffer(b), ArrayBuffer(c))),
        (3,(ArrayBuffer(b), ArrayBuffer(c))),
        (1,(ArrayBuffer(b, b),ArrayBuffer(c, c)))
    )
```

CountByValue

foldByKey
```
val a = sc.parallelize(List("dog", "tiger", "lion", "cat", "panther", "eagle"))
val b = a.map(x => (x.length, x))
b.foldByKey("")(_ + _).collect
Array[(Int, String)] = Array((4,lion), (7,panther), (3,dogcat), (5,tigereagle)
```

a.glom.collect
```
// return array with content of collection
val a = sc.parallelize(1 to 100, 3)
a.glom.collect
```

groupby
```
val a = sc.parallelize(1 to 9, 3)
a.groupBy(x => {if (x % 2 == 0) "even" else "odd" }).collect
Array[(String, Seq[Int])] = Array((even,ArrayBuffer(2, 4, 6, 8)), (odd,ArrayBuffer(1, 3, 5, 7, 9)))
```

keyBy & groupByKey
```scala
    val a = sc.parallelize(List("dog", "tiger", "lion", "cat", "spider", "eagle"), 2)
    val b = a.keyBy(_.length)
    Array[(Int, Seq[String])] = Array((4,ArrayBuffer(lion)), (6,ArrayBuffer(spider)), (3,ArrayBuffer(dog, cat)), (5,ArrayBuffer(tiger, eagle)))
```

mapValues
```scala
val a = sc.parallelize(List("dog", "tiger", "lion", "cat", "panther", "eagle"), 2)
val b = a.map(x => (x.length, x))
b.mapValues("x" + _ + "x").collect
Array[(Int, String)] = Array((3,xdogx), (5,xtigerx), (4,xlionx), (3,xcatx), (7,xpantherx), (5,xeaglex))
```

reduceByKey
```scala
val a = sc.parallelize(List("dog", "tiger", "lion", "cat", "panther", "eagle"), 2)
val b = a.map(x => (x.length, x))
b.reduceByKey(_ + _).collect
Array[(Int, String)] = Array((4,lion), (3,dogcat), (7,panther), (5,tigereagle))
```

subtractByKey
```scala
val a = sc.parallelize(List("dog", "tiger", "lion", "cat", "spider", "eagle"), 2)
val b = a.keyBy(_.length)
val c = sc.parallelize(List("ant", "falcon", "squid"), 2)
val d = c.keyBy(_.length)
b.subtractByKey(d).collect
Array[(Int, String)]=Array((4,lion))
```
