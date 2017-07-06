# How-to: Tune Your Apache Spark Jobs

## 1. How Spark Executes Your Program

- Spark Application
    + Single Driver: high-level control flow
    + Set of executor: execute task, store data
        * each has slots for running tasks concurrently

- Process
    + invoke action
    + trigger Spark job
    + examine graph of RDD
    + formulate an execution plan
        * assemble job's transformation into stages
        * a stage correspond to collection of tasks execute same code without shuffling, or narrow transformations
        * must execute shuffle for wide transformations
    + shuffle (write to disk then transfer over network)
    
## 2. Picking the Right Operator

- primary goal when choosing operators
    + reduce the number of shuffles 
    + reduce amount of data shuffled
    + repartition, join, cogroup, \*By,\*ByKey
- Guidelines
    + Avoid groupByKey when performing associative reductive operation
        * `rdd.groupByKey().mapValues(_.sum)` same as `rdd.reduceByKey(_ + _).`
        * former will transfer entire dataset across network
        * latter will compute local sum for each key
    + Avoid reduceByKey when i/o value type different
        * find unique strings responding to each key
        * `rdd.map( kv => (kv._1, new Set[String]() + kv._2) ).reduceByKey(_++_)`
        * has tons of unnecessary object creation, a set for each record
        * `val zero = new collection.mutable.Set[String]()`
        * `def add {(set,v)=>set+=v}`
        * `rdd.aggregateByKey(zero)((set,v)=>set+=v,(set1,set2)=>set1++=set2)`
    + Avoid flatMap-join-groupBy pattern
        * use cogroup, avoid all overhead associated with upacking and repacking

## 3. When Shuffles Don't Happen

- Spark knows to avoid shuffle when necessary
    + previous operation already partitioned data according to same partitioner
    + `rdd1 = someRdd.reduceByKey(...)`
    + `rdd2 = someOtherRdd.reduceByKey(...)`
    + `rdd3 = rdd1.join(rdd2)`
    + two reduceByKey result in two shuffles
    + if RDDs have same partition, join will need no additional shuffling
    + if reduceByKey result have different partition, the rdd with fewer number of partitions will be reshuffled for the join
- take advantage of broadcast variable
    + when small enough to fit in memory in a single executor
    + can be loaded into a hashtable on drive

## 4. When More Shuffles are Better

- Sometimes an extra shuffle can be advantageous to performance when increase parallelism
- large unsplittable files
    + use repartition to generate higher number of partitions
- aggregate over high number of partitions
    + computation can be quickly bottlenecked
    + use reduceByKey or aggregateByKey to divide into smaller number set
    + value within each partition merged with each other
- especially useful when aggreagation already group by key
    + put word account into driver as map
    + aggregate map at each parition then merge
    + aggregateByKey then collectAsMap

## 5. Secondary Sort

- `repartitionAndSortWithinPartitions`
- large data spill efficiently and combine sort with other operation
- Hive on Spark use this transformation inside join implement
    + secondary sort pattern
    + both group by key and sort inside

## 6. Tuning Resource Allocation

- two main resource
    + CPU
    + memory
- Spark executors in an application 
    + has same fixed number of cores & heap sizes
    + core numbers
        * max task each executor can run at the same time
        * set `--executor-cores` flag when invoke spark-submit, spark-shell or pyspark
        * set `spark.executor.cores` in `spark-defaults.conf` file
        * set `spark.executor.cores` on `SparkConf` Object
    + heap size
        * `--executor-memory flag`
        * `spark.executor.memory`
    + num executors
        * `--num-executors` or `spark.executor.instances`
        * num of executors requested
        * can be avoided by turning dynamic allocation
            - `spark.dynamicAllocation.enabled`
- how resources fit into what YARN has available
    + `yarn.nodemanager.resource.memory-mb` controls the maximum sum of memory used by the containers on each node.
    + `yarn.nodemanager.resource.cpu-vcores` controls the maximum sum of cores used by the containers on each node.
    + Asking for five executor cores will result in a request to YARN for five virtual cores. 
- The memory requested from YARN is a little more complex:
    + inside YARN node
        * `yarn.nodemanager.resource.memory-mb`
        * will also round up following request a little by
            - `yarn.scheduler.minimum-allocation-mb`
            - `yarn.scheduler.increment-allocation-mb`
    + has multiple executor containers, within each:
    + `spark.yarn.executor.memoryOverhead`
        * determine full memory request to YARN
        * default to max(384,0.07*spark.executor.memory)
    + `--executor-memory/spark.executor.memory`: heap size
        * JVM can use off heap memory for interned string/direct byte buffer
        * `spark.shuffle.memoryFraction`
        * `spark.storage.memoryFraction`
    
- Example
    + a cluster with 6 nodes, each 16 cores and 64GB memory
    + NodeManager capacities
        * yarn.nodemanager.resource.memory-mb: 63*1024=64512 (mb)
        * yarn.nodemanager.resource.cpu-vcores = 15
        * avoid 100% usage: leave to OS & Hadoop daemon
    + wrong command
        * `--num-executors 6 --executor-cores 15 --executor-memory 63G`
        * 63GB + overhead won't fit 63GB capacity
        * node with application master will take up a core, won't fit 15 cores
        * too much cores per executor lead to bad I/O
    + better option
        * `--num-executors 17 --executor-cores 5 --executor-memory 19G`
        * five node with 3 executor, application master node with 2
        * 21GB for each executor
        * x + 0.07 * x < 21, x=19.6~19



## 7. Tuning Parallelism

- each Spark stage has a number of task, the number is probably the single most important parameter in determining performance
    + Spark group RDD into stages (part 1)
    + number of task in a stage is the same as number of partitions in last RDD
        * except: coalesce(smaller), union(sum), cartesian(product)
    + new RDD determined by underlying input format
        * a partition for each HDFS block being read
        * call `spark.default.parallelism`
    + check partitions by `rdd.partitions().size().`
- primary concern: the number of tasks will be too small. 
    + If there are fewer tasks than slots available to run them in, 
    + the stage won’t be taking advantage of all the CPU available
    + more memory pressure is placed on aggregation operation
        * join cogroup ByKey hold object in hashmap or buffer
- how to increase number of partition
    + repartition transformation (shuffle)
    + configure input format to create more splits
    + write to HDFS with smaller block size
    + between stages
        * numPartitions argument
        * `val rdd2 = rdd1.reduceByKey(_ + _, numPartitions = X)`
        * tune X by multiply parent RDD by 1.5 until performance stop improving

## 8. Slimming Down Your Data Structures

- A record has two representations
    + a deserialized Java object representation, for memory
    + a serialized binary representation, for disk and io
- `spark.serializer`
    + perfect option: `org.apache.spark.serializer.KryoSerializer`
    + not default due to early Spark compatibility
- problem
    + bloated deserialized object will spill data to disk more often
    + reduce number of records can cache per node memory
    + result in greater disk, network io
- solution
    + register any custom classes define and pass around
    + using `SparkConf#registerKryoClasses` API
- when store in disk
    + use Avro, Thrift or Protobuf
    + every time consider JSON
        * think about 
            * the conflicts that will be started in the Middle East, 
            * the beautiful rivers that will be dammed in Canada, 
            * or the radioactive fallout from the nuclear plants that will be built in the American heartland 
        * to power the CPU cycles spent parsing your files 
            * over and over and over again.
