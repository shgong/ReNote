

# Spark

- Genera-purpose cluster in-memoery computing system
- fast data analytics
- abstracts APIs in Java, Scala and Python
- high level tools
    + Shark SQL: structured data, like hive query
    + Spark Streaming: analytical and interactive apps
    + MLlib: Machine Learning Lib built on top of spark
    + Spark R: R package
    + GraphX: Graph computation engine

- can be deployed through Apache Mesos, Apache Hadoop via Yarn or Spark's own manager
- spark's design goal is to generalize MR concept to support new apps within the same engine, more efficient for engine and simpler for users


- Driver Program: SparkContext
    + coordinate spark apps in the driver program
    + connect to cluster manager for resource allocation
    + send application code (jar/python) to executors provided by cluster manager
- Cluster Manager
    + 3 types: standalone, yarn, mesos
    + Worker Node
        * Executor: Cache, Task, Task, Task



# Terminology

- RDD: Risilient Distribution Datasets
    + immutable, distributed, fault-tolerant operational elements
    + call SparkContext `parallelize`
    + `val data=Array(4,6,7,8);val distData=sc.parallelize(data);`
    + `val inputfile = sc.textFile(“input.txt”)`
- Spark Engine
    + scheduling, distributing and monitoring the data application across the cluster.
- Spark Driver
    + declare transformation and actions on data RDDs
- Spark Context
    + main entry point for Spark functionality
    + connection to Spark Cluster, can be used to create RDDs, accumulators and broadcast variables on the cluster
- Partitions
    + similar to the “split” in Map Reduce.
    + `val someRDD = sc.parallelize( 1 to 100, 4)`
    + 100 elements in 4 partitions
- RDD support operation
    + Transformations
        * function(RDD->new RDD)
        * lazy
        * map(), filter()
    + Actions
        * execution all transaction
        * reduce(): sum up until one value
        * take(): export to local machine
- RDD Lineage
    + Spark do not replicate data in memory
    + it rebuild from the lineage when data loss
    - SparkSQL
        + SQL like interface to work with structured data
        + provide SchemaRDD
        + `var sqlContext=new SqlContext`
- parquet
    + columnar format file
- Spark vs MapR
    + speed
    + built-in libs
    + caching & in-memory
    + interative computation (reduce)
    + more storage space compared to Hadoop and MapReduce.
    + not actually streaming (Flink)
