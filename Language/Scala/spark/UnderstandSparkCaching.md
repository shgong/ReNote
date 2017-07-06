# Understand Spark Caching

Spark excels at processing in-memory data.  We are going to look at various caching options and their effects, and (hopefully) provide some tips for optimizing Spark memory caching.

When caching in Spark, there are two options
- Raw storage
    + fast to process
    + take x2-x4 more space
    + pressure in JVM & JVM GC
    + `rdd.persist(StorageLevel.MEMORY_ONLY)` or `rdd.cache()`
- Serialized
    + slower but minimal overhead
    + `rdd.persist(StorageLevel.MEMORY_ONLY_SER)`



Apache Spark will unpersist the RDD when it's garbage collected.

In RDD.persist you can see: `sc.cleaner.foreach(_.registerRDDForCleanup(this))`
This puts a WeakReference to the RDD in a ReferenceQueue leading to `ContextCleaner.doCleanupRDD`when the RDD is garbage collected.



You can call `getStorageLevel.useMemory` on the RDD to find out if the dataset is in memory.

```
scala> val rdd = sc.parallelize(Seq(1,2))
rdd: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[1] at parallelize at <console>:21

scala> rdd.getStorageLevel.useMemory
res9: Boolean = false

scala> rdd.cache()
res10: rdd.type = ParallelCollectionRDD[1] at parallelize at <console>:21

scala> rdd.getStorageLevel.useMemory
res11: Boolean = true
```



Can I override campaign RDD?

```
scala> val testc = sc.parallelize(Array.range(0,1000))
testc: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[19] at parallelize at <console>:21

scala> testc.cache()
res38: testc.type = ParallelCollectionRDD[19] at parallelize at <console>:21

scala> testc.getStorageLevel.useMemory
res43: Boolean = true

scala> var mutc = testc
mutc: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[19] at parallelize at <console>:21


scala> mutc = sc.parallelize(Array.range(1500,1300))
mutc: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[20] at parallelize at <console>:25

scala> mutc.getStorageLevel.useMemory
res42: Boolean = false

// should unpersist mutc before assign new values
```

