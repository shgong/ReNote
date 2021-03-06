# Hadoop Application Architecture

## Chapter 1. Data Modeling in Hadoop

- Hadoop is a distributed data store that provides a platform for powerful parallel processing frameworks. 
- Feature: Schema-on-Read, raw unprocessed data can be loaded into Hadoop.
- Need some consideration before dumping your data into Hadoop:

### 1.1 Data Storage Options
how data will be stored in Hadoop

#### 1.1.1 Standard File Format

- Text data
    + Example: web logs, server logs, CSV files, emails
    + Compression: Splittable formats
    + the use of a container format such as SequenceFiles or Avro will provide advantages
- Structured Text data
    + Example: XML / JSON
    + Problem: splitting XML and JSON is tricky for Hadoop
        * use a container format such as Avro
        * use special library
            - XMLLoader in PiggyBank library for Pig
            - LzoJsonInputFormat for Elephant Bird project
- Binary data
    + you can use Hadoop to process binary data like Image
- Hadoop File Types
    + Sequence files
    + serialization formats like Avro
    + columnar formats like RCFile and Parquet

#### 1.1.2 File-based data structure

SequenceFile (MapFiles, SetFiles, ArrayFiles, BloomMapFiles)
- specially designed to work with Hadoop MR, Pig, Hive
- store data as binary key-value pair
- three compression format
    + uncompressed, record-compressed, block-compressed
    + structure: header-n(sync marker, block)
- uses a common header format containing basic metadata about the file
    + the compression codec used, key and value class names, user-defined metadata, and a randomly generated sync marker
- Problems
    + Limited support outside Hadoop Ecosystem
    + Java Only
    + Small files cause efficiency issues except for Namenode

#### 1.1.3 Serialization Formats
- Default: Writables
    + Compact and fast
    + Java only
- Thrift & Protocol Buffers
    + Thrift: Facebook cross-language interface
    + Protocol Buffers: Google data exhcange language
    + Both:
        * defined via an IDL, need code generation
        * Not support internal compression
        * Not splittable
-  Avro
    +  described through a language-independent schema
    +  code generation is optional, store schema in header
        *  usually in JSON
        *  header also contain sync marker, separate blocks
    +  compressible and splitble

#### 1.1.4 Columnar Formats

- Traditional: row-oriented
    + good for fetching time range
    + efficient when writing data
- Columnar
    + skip I/O and decompression for non-query columns
    + good for fetching exact column (data warehousing type)
    + efficient for compression (low entropy)
- RCFile
- ORC
    + designed specially for Hive
    + light weight, always on compression
    + predicates push down to storage level
- Parquet
    + general purpose storage format
- Avro + Parquet
    + single interface to all files in cluster


#### 1.1.5 Compression

Important: help reduce disk and network I/O

- Formats
    + Snappy
        * from Google, good trade-off between speed and size
        * not splittable, use with Avro
    + LZO
        * optimized with speed
        * splittable, require additional indexing
    + Gzip
        * optimized with size slow when compress
        * not splittable, use with Avro,
    + bzip2
        * best compression, very slow
        * inherently splittable
- Notes
    + Enable compression of MapReduce Intermediate output
    + Pay attention how data is ordered

### 1.2 HDFS Schema Design

- Why
    + easier to share data between teams
    + allow enforcing access and quota controls
    + code reuse
    + tools assumptions default directories
- Pattern
    + `/user/<name>`
        * Data, JAR, configuration files for specific user
    + `/etl`
        * readable and writable by ETL process
        * `/etl/BI/clickstream/aggregate_user_preference/input` 
        * team, application, data folder, processing stage
    + `/tmp`
        * temp data generated by tool, shared by all users
    + `/data`
        * processed and shared across organization
        * business critical, only automated ETL process allowed to write
        * different group have different read access
    + `/app`
        * Hadoop running scripts
        * JAR, Oozie workflow, Hive HQL, Hive UDF
        * `/app/BI/clickstream/latest/aggregate_preferences/uber-aggregate-preferences.jar`
    + `/metadata`
        * extra metadata except Hive metastore
        * called by ETL jobs, like Avro schema
        * `/metadata/movie-lens/movie/movie.avsc`
- Partitioning
    + HDFS doesn't store index (greatly speed up ingestion), but has to do full table scan when request
    + each partition in a subdirectory
    + `/medication_orders/date=20131101/{order1.csv, order2.csv}`

- Bucketing
    + small file problem: adding new files daily, excessive memory use for NameNode
    + bucket by physician
        * each bucket with size of a few multiple of HDFS block
        * hash physicians into a specified number of buckets
        * even distribution of data, buckets power of 2
    + benefit
        * join two sets by key: when sets bucketed on the join key and one data set is multiple of the other, you can join individually without the entire data sets
        * can load small bucket in memory, and do map-side join (gretly imporove performance than reduce-side join)

- Denormalizing
    + reduce the need of join data sets
    + reduce side join need sending entire table over network
        * bucket help join with less file
        * can also create prejoined/preaggregated data sets


### 1.3 HBase Schema Design

HBase is huge hash table, support operations like

- Operations
    + put, get, delete
    + iterate
    + Increment value
- The value proposition of HBase lies in its scalability and flexibility. 
- HBase works for problems that can be solved in a few get and put requests.

HBase store data in HFile
- each column value get its own row in HFile
    + row fields: row key, timestamp, column name, value
    + example:  1001 | 1395531114 | N | John
    + one character column name: common in hbase, take space
- example: 2 logic columns
    + can get, modify and age out independently
    + also occupy two rows in HFile
- together or separate?
    + how many records can fit in block cache
    + how many records can fit into memstore flush

#### 1.3.1 Row Key

-   Record retrieval
    + can have unlimited columns, but only a row key
        * unlike rdb, where primary key can be a composite
        * you need to combine a row key like `customer_id,order_id,timestamp`
    + get operation is the fastest
        * this mean put lot of info into a single record
        * denormalized design: retrieve everything from a single get
        * distinct from normalized rdb design, where you store customers, contact, orders, order details in separate table
-   Distribution
    + all row key are sorted
    + each region pinned to a server
    + do not use timestamp for row key, that would make request focused on a single region, we need salt the key
-   Block Cache
    + HBase read records in chunks of 64KB (HBase block)
    + when read, put into block cache, as recently fetched records is likely to use again
    + salt meaningful things as the first part of row key, so the cached block would be more likely to be a hit
-   Ability to scan
    + wise selection of row key can be used to co-locate related records
    + scan only a limited regions to obtain the result
-   Size
    + shorter: storage overhead, faster read/write
    + longer: better get/scan
    + can also compress with Snappy, good when row keys are close

#### 1.3.2 Timestamp

+ determine which records are newer when put
+ determine order return when multiple version requested
+ determine whether move out-of-date records

#### 1.3.3 Hop

- number of synchronized get required
- example
    + Person table: Name - Friends - Address
        + get address of friends: 2-hop request
    + Person table: id - name, Friend table: id - ids, Address table: id - address
        + 3-hop, not recommended in HBase

#### 1.3.4 Tables and Regions

- structure
    + node - region server = many regions
    + a table must ahve at least one region

- put performance
    + memstore: cache structure on every HBase region server
        * cache writes, sort before flush
        * the more region, the less memstore, smaller flush, smaller file
        * idea flush size is 100MB per region

- compaction time
    + create table with single default region, and autosplits when increase
    + create splitted table, avoid autosplit

#### 1.3.5 Time-To-Live

- built in feature that age out data based on timestamp
- do not need extra scan if you need retire out 7 year+ data

### 1.4 Managing Metadata

- Hive: first project that started storing managing and leveraging metadata
    + store in a RDB called Hive metastore
- HCatalog: more project want to use same metadata in Hive
    + now part of Hive and allow Pig/MapReduce to integrate
    + expose a rest api to Hive metastore via WebHCat server
    + have 3 modes, usually use remote mode
- example of use
    + Hive/Impala: integrate directly when create/alter table
    + Pig: rely on HCatalog integration with Pig
    + MR, Spark, Cascading: use HCatalog's Java API

-----

## 2. Data Movement
### 2.1 Data Ingestion Considerations

Common Data Source

- Traditional data management systems
    + Relational databases
    + mainframes
- Logs, machine-generated data, other even data
- Files imported from existing enterprise data storage system 

#### 2.1.1 Timeliness of data ingestion and accessibility

- Time lag: from when data is available for ingestion to accessile to Hadoop tools
- will have a large impact on the storage medium and on the method of ingestion.
- Classifications 
    + Macro batch: 15 min, hours, daily
    + Micro batch: every 2-15 min
    + Near-Real-Time Decision Support: 2s - 2min
    + Near-Real-Time Event Processing: 100ms - 2s
    + Real Time: 100ms- 
- Tool to Use
    + 15min+: HDFS, file transfer/Sqoop job
    + 2-15min: HDFS, Flume/Kafka    
    + 500ms-2min: HBase, Solr
    + 500ms-: Storm, Spark Streaming

#### 2.1.2 Incremental updates

- Whether new data will append existing data or modify it
    + append only, like logging user activity
        * Use HDFS, high read and write rates
        * HDFS is optimized for large files
    + modify existing Files
        *  HDFS is read only — you can’t update records in place
            - Need minutes 
            - first write a “delta” file that includes the changes
            - do a compaction job
            - sort data by primary key
            - overwrite the row found twice
        * Use HBase
            - HBase handles compactions in the background 
            - support deltas that take effect upon the completion of an HBase put, which typically occurs in milliseconds
            - also can do random access in milliseconds
            - however have low scan rates of 8-10min


#### 2.1.3 Access Patterns

- How to be used
    + HDFS: scan rate, data transformation, compression, simplicity
    + HBase: random row access, use Solr to search

#### 2.1.4 Source system and data structure

- Read speed of the devices on source systems
    +  Disk I/O bottleneck, like Hadoop 20-100 MBps
    +  on a typical drive three threads is normally required to maximize throughput
-  Orginal file type
    +  Hadoop accept many file format, but not all are optimal
        *  Parquet is more effient at processing and storage than CSV
    + file type work with different tool  
        * Variable length file not good fit for Hive  
-  Compression
    +  require less I/O but may not be splittable(e.g. Gzip)
    +  but can work with splittable container (SequenceFiles, Parquet, Avro)
-  Relational Database
    +  integrate with Oracle, Netezza, Greenplum, Vertica, Teradata, Microsoft, and others
    +  batch process: always use Sqoop
        *  require Data Nodes, not just Edge Nodes connection to RDBMS
        *  if not possible, use a RDBMS file dump
    +  faster: use Flume or Kafka
        *  split data on ingestion, with one pipeline landing data in the RDBMS, and one landing data in HDFS
        *  complex implementation, require application layer code
- Streaming data
    + Twitter feeds, JMS Queue, events firing from server
    + use Flume or Kafka
- Logfiles
    + do not read from disk: will lose data
    + use Flume or Kafka


#### 2.1.5 Transformations

- Transformation: xml/json->delimited data
- Partition: stock trade data, require partition by ticker
- Splitting: need part HDFS and part HBase for different access pattern

Sometimes timeliness do not allow file transfer with MapReduce

- use Flume
- Flume Interceptor
    + allows for in-flight enrichment of event data
    + implemment in Java
    + can transform batch of event, add/remove
- Flume Selector
    + fork in the road
    + decide which to go down 

### 2.2 Data Ingestion Options

#### 2.2.1 File Transfer
```
hadoop fs -put
hadoop fs -get
```

- put commands
    + normally two approaches: double hop and single hop
    + double hop: External - Edge Node Local Drive - HDFS
    + sometimes external fs not available to be mounted from HDFS

- Problem
    + all or nothing batch processing, no failure handling
    + single threaded
    + only from traditional fs to HDFS
    + transformation not supported

#### 2.2.2 Sqoop: Batch Transfer Between Hadoop and Relational Database

Sqoop generate a map only job
- each mapper connects to odb using jdbc driver
- flexible allow adding where clauses


```bash
sqoop import \
--connect jdbc:oracle:thin:@localhost:1521/oracle \
--username scott --password tiger \
--table  HR.employees 
--target-dir /etl/HR/landing/employee \
--input-fields-terminated-by "\t" \
--compression-codec org.apache.hadoop.io.compress.SnappyCodec --compress
```

```bash
sqoop import \
--connect jdbc:oracle:thin:@localhost:1521/oracle \
--username scott  --password tiger \
--query 'SELECT a.*, b.* FROM a JOIN b on (a.id == b.id) WHERE $CONDITIONS' \
--split-by a.id 
--target-dir /user/foo/joinresults
# $CONDITIONS is a literal placeholder used by Sqoop, will be replaced by Sqoop with generated conditions when the command is run.
```

Split-by column
- Sqoop default use 4 mappers, split between min/max of primary key column
- argument: split-by, num-mappers
- if no index or partition key column exist, specify only one mapper

Goldilocks Principle of Sqoop performance tuning
- mapper controll
    + too many: overload the database, denial of service attack at rdb
    + too little: slow ingestion
    + start with low and increase

Ingest multiple table from same RDBMS
- Load sequentially, simple, but mapper left idle
- Load in parallel, complex, use fair scheduler

Update
- when table is small: Refresh the data 
- when table is large
    + Sequence ID
        * Sqoop can track last ID it wrote to HDFS
        * next time only import row with IDs higher
        * for add only data
    + Timestamp
        * Sqoop can track last timestamp it wrote to HDFS
        * next time only import row with time higher
        * for both add and update
    + use `--incremental` flag
        * reuse same directory name
        * new data loaded as additional files
            - easy to find new data
            - lose data consistency if sqoop running
        * recommend update into new directory
            - track `\_SUCCESS` file for finish
    + `sqoop-merge`
        * take merge key, old, new, target directories


```bash
# first create a job:
sqoop job --create movie_id --import  --connect \
jdbc:mysql://localhost/movielens  \
--username training --password training \
--table movie --check-column id --incremental append

# Then, to get an incremental update of the RDBMS data, execute the job:
sqoop job --exec movie_id
```

#### 2.2.3 Flume: Event-Based Data Collection and Processing

Flume Architecture
- Source
    + consume events from external sources and forward to channels
    + sns feed, machine logs, message queue
    + AvroSource, JMSSource, HTTPSource
- Interceptor
    + intercept and modify event in flight
    + format, partition, filter, split, validate, apply metadata
    + great power with greater risk of memory leak or excessive CPU
- Selector
    + provide routing, send down different paths
- Channel
    + store events
    + memory channel: best performance but less reliable
    + file channel: more persistence
- Sink
    + remove from channel and delivers
    + can have multiple HDFS sink (HDFS should never be bottleneck)
- Flume Agent
    + container for all components
    + a JVM process host everything


Flume Patterns

- Fan-in
    + a flume agent in each source
    + send to multiple agents on hadoop edge node (reliability)
- Split
    + primary cluster and backup cluster (DR: disaster recovery)
    + partition into HDFS by timestamp, or HDFS and HBase
- Streaming Analysis
    + send to Storm or Spark Streaming
    + simple: Flume Avro sink point to Spark Flume Stream

File Formats
- Flume event serializer
- recommend to save as Sequence File or Avro
- Avro Serializer
    + use event schema, not prefered for storing data
    + need override EventSerializer interface to create custom ouput
    + common task, often required to make storing schema

#### 2.2.4 Kafka

- distributed publish-subscribe messaging system, each node called broker
- application 
    + topic: main level of data separation
    + producer: publish message
        * can choose whether write be acknowledged 
            - by all replica, by leader, no acknowledgement
    + consumer: subscribe to topics
        *  registered in consumer groups
        *  each read one or more partition of a topic
        *  message -> many subscribed group, only one consuemr in each group
- partitioned log
    + main unit of parallelism
    + each topic -> many partitions (ordered subset)
    + partitions can spread across many brokers
    + message: ordered within a partition, has unique offset
    + unique message: (topic, partition, offset)
- recovery
    + each topic partition replicate into multiple brokers
    + keep track of last message consumed by each consumer
        * it do not track acknowledgement of consuemrs
    + consumer have rewind feature
        * rewind state of queue, reconsumer message
- usage
    + in place of a traditional message queue
    + common in high rate activity stream like website, metric, logging
    + use in stream data processing
- work with Hadoop
    + store message in HDFS
    + stream processing data source


Kafka vs Flume
- Flume
    + complete Hadoop ingest solution
    + good support for HDFS, HBase, Solr
    + configuration based, no much code
    + handle common issues like reliability, file size, metadata
- Kafka
    + reliable, high available, high performance
    + fault-tolerant
    + develop own producers and consumers
- can use both
    + Flume has Kafka source, sink, channel
    + source: kafka consumer
        * configuration: set a topic, the zookeeper server used by kafka, and channel
        * use groupId flume
    + sink: kafka producer
    + channel: combination

---
## Chapter 3. Processing data in Hadoop
### 3.1 Map Reduce

View More at MapReduce

### 3.2 Spark

MapReduce
- Mapper, Reducer connected by DAG Model
- directed acyclic graphs: series of actions connected to each other
- but only being an abstraction added externally to the model. 

Spark 
- creates complex chains of steps from the application’s logic 
- allows developers to express complex algorithms and data processing pipelines within the same job
- advantage
    + cleaner and simpler API
    + generic extensible parallel processing framework
    + store RDD in memory and process without additional I/O
    + allows the framework to optimize the job as a whole
    + multiple language / resource manager / Interactive Shell support

#### 3.2.1 Spark Components

- Driver
    + define RDD objects and relationship
    + RDD: main data structure
- DAG Scheduler
    + like a queue planner
    + input: parallel operations on RDDs
    + output: optimize code and produce efficient resulting DAG
- Task Scheduler
    + cluster manager: YARN, Mesos or Spark native
    + has info about workers, assigned threads and location of data blocks
    + assign specific processing to workers
- Worker
    + receive unit of work
    + without knowledge of entire DAG

#### 3.2.2 Spark Concepts

- Resilient Distributed Datasets
    +  collection of serializable elements
    +  creation 
        *  from Hadoop input format, determine number of partitions
        *  from existing RDD, can shuffle / repartition
    + store
        * store lineage: set of transformation used to create current state, starting from first input format
        * replay lineage when data lost
    +  can reduce I/O and maintain processed data set in memory

- Shared Variable
    + broadcast
        * send to all remote execution nodes
        * similar to Configuration objects in MapReduce
    + accumulator
        * also send to remote
        * can be modified by executors (add only)
        * similar to MapReduce counters

- SparkContext
    + object that represent the connection to Spark Cluster
    + create RDD, broadcast data, initialize accumulators

- transformations
    + transform RDD -> modified RDD 
        * immutable, return a new
        * lazy, don't compute lineage
            - allow cleverly optimization
            - exception handling more difficult
    + example
        * map(): `lines.map(s=>s.length)`
        * filter(): `lines.filter(s=>(s.length>50))` remove false
        * keyBy():`lines.keyBy(s=>s.length)` create (k,v) pair
        * join():`lines.join(more_lines)` join two (k,v) RDD by key
        * groupByKey(): like group pv performed by a single user
        * sort(): return sorted
    + action
        * let RDD perform computation and return result


### 3.3 Abstraction

- ETL model: Pig, Crunch, Cascading
- Query model: Hive, Impala

#### 3.3.1 Pig

Feature
- Pig Latin language with interactive Grunt shell
- support UDF, engine independent

```pig
fooOriginal = LOAD 'foo/foo.txt' USING PigStorage('|')
  AS (fooId:long, fooVal:int, barId:long);

fooFiltered = FILTER fooOriginal BY (fooVal <= 500);

barOriginal = LOAD 'bar/bar.txt' USING PigStorage('|')
 AS (barId:long, barVal:int);

joinedValues = JOIN fooFiltered by barId, barOriginal by barId;

joinedFiltered = FILTER joinedValues BY (fooVal + barVal <= 500);

STORE joinedFiltered INTO 'pig/out' USING PigStorage ('|');
```

- Data container: relations
    + indentifier for dataset  `fooOriginal `, like RDDs
    + relation is a bag, collection of tuples (k, v), may also be bags
- Transformation functions
    + FILTER, JOIN, also lazy, until `saveToTextFile`
- Field definition
    + unlike Spark, fields and data types are called out
    + `AS (barId:long, barVal:int)`
- No java, limited by dialect of Pig Latin
- Describe & Explain: figure out what happened

#### 3.3.2 Crunch (FlumeJava)

- Feature
    + Java language,full access to MR , engine dependent
    + separate business logic from integration logic

- Similar to Pig/Spark
    + Pipeline: MRPipeline or Spark Pipeline (like SparkContext)
    + PCollections & PTables (like relation/RDD)
    + done()  (like action/save)

- not 100% code transferable, depend on Pipeline implementation

#### 3.3.3 Hive 

- use familiar SQL abstraction
- cornerstone of newer SQL implementation like Impala, Presto, SparkSQL
- performance concern: MapReduce as execution engine
    + Hive-on-tez: enabling Tez — a more performant batch engine than MapReduce
    + Hive-on-Spark: similar, use Spark
    + Vectorized query excecution:  processing batch of rows at a time, reducing conditional branches. Need file formats like ORC and Parquet

- Architecture: HiveServer2
    + Thrift Service
        * Beeline CLI
        * JDBC
        * ODBC
        * Thift Clients
    + Multiple Sessions
        * Hive Driver
        * Cimpler
        * Executor
    + Metastore service
        * Hive Metastore Database

```sql
# define column delimitor, store format and location
CREATE EXTERNAL TABLE foo(fooId BIGINT, fooVal INT, fooBarId BIGINT)
ROW FORMAT DELIMITED
 FIELDS TERMINATED BY '|'
STORED AS TEXTFILE
LOCATION 'foo';

CREATE EXTERNAL TABLE bar(barId BIGINT, barVal INT)
ROW FORMAT DELIMITED
 FIELDS TERMINATED BY '|'
STORED AS TEXTFILE
LOCATION 'bar';

# computing statistics in Hive ( Run map only job)
ANALYZE TABLE foo COMPUTE STATISTICS;
ANALYZE TABLE bar COMPUTE STATISTICS;

# Query
SELECT * FROM
foo f JOIN bar b ON (f.fooBarId = b.barId)
WHERE
f.fooVal < 500 AND
f.fooVal + b.barVal < 1000;
```


Hive support various distributed joins, hive select the right join automatically, which may lead to better performance than others

- map join (hash join)
- bucketed join
- sorted bucketed merge join
- regular join

#### 3.3.4 Impala

Feature

- open-source, low-latency SQL engine
- inspired by Google Dremel (scalable, interactive ad-hoc query engine)
- not based on MapReduce
- similar to architecture of traditional MPP (Massive Parallel Processing) data warehouse, like Netezza, Greenplum and Teradata

Architecture

- Impala daemon (_impalad_)
    + run on every node
    + each daemon is capable of acting as 
        * query planner: parse out given SQL query, produce execution plan
        * query coordinator: take plan assign to rest of daemons
        * query execution engine
- Connection: client to daemon via Apache Thift
    + use JDBC, ODBC, impala shell or directly
    + daemon connected will act as planner and coordinator
- Catalog Service
- Statestore

```
CONNECT <impalaDaemon host name or loadbalancer>;
-- Make sure Impala has the latest metadata about which tables exist, etc.
-- from the Hive metastore
INVALIDATE METADATA;

SELECT * FROM
foo f JOIN bar b ON (f.fooBarId = b.barId)
WHERE
f.fooVal < 500 AND
f.fooVal + b.barVal < 1000;
```

Impala vs Hive
- much faster than Hive
- not fault-tolerant like Hive
    + not good for large query which take hours
    + abilility to revocer a node is critical
- have less feature like complex data types (map, struct, array)
    + if nested data type is important
- support custom file formats
    + Hive support 
        * all popular formats: CSV, Parquet, Avro, RC, Sequence
        * custom formats like JSON XML via pluggable SerDe
    + Impala: use hive to read in popular format

### 3.4 Other tools

- RHadoop: collection of projects like rmr
- Apache Mahout: machine learning tasks 
- Oryx: machine learning applications that leverage Lambda Architecture
- Python: a guide to python frameworks for hadoop

---
## Chapter 4. Common Hadoop Processing Pattern

### 4.1 Removing duplicate records by primary key
### 4.2 Windowing Analysis
### 4.3 Time Series Modification

---
## Chapter 6. Orchestration

Workflow orchestration (business process automation)
- the tasks of scheduling, coordinating, and managing workflows
- break workflow into reusable components, use external engine to handle details

### 6.1 Limit of Scripting

you may first want to use scripting language like Bash, Perl or Python
```sh
sqoop job -exec import_data
if beeline -u "jdbc:hive2://host2:10000/db" -f add_partition.hql 2>&1 | grep FAIL
then
    echo "Failed to add partition. Cannot continue."
fi

if beeline -u "jdbc:hive2://host2:10000/db" -f aggregate.hql 2>&1 | grep FAIL
then
    echo "Failed to aggregate. Cannot continue."
fi
sqoop job -exec export_data
```

you can execute every day at 1 a.m, but what if you want to
- handle errors
- notify user workflow status
- track execution time
- apply sophisticated logic between stages
- reuse components among various workflows

### 6.2 Orchestration Frameworks

- Oozie by Yahoo:
- Azkaban by Linkedin, visual and easy solution
- Luigi by Spotify, python package, orchestrate long batch jobs
- Chronos by Airbnb: top of Mesos, replacement for cron

Choose Criterion
- ease of installation
- user interface support
- testing
- logs
- workflow management
- error handling

### 6.3 Oozie

- Terminaology
    + workflow action: a unit of work by orchestration engine
    + workflow: a control dependency DAG of jobs
    + coordinator: data set / schedules that trigger workflow
    + bundle: collection of coordinators

- Main logic components
    + workflow engine
    + scheduler/coordinator
    + REST API
    + Command-line client
    + Bundles
    + Notification
    + SLA monitoring
    + Backend database

---
## Chapter 7. Near real time processing

- Examples
    + Social media feeds (detecting trends)
    + Financial feeds (anomaly detection)
    + Video game usage (unfair advantage exploit)
    + machine data feeds (raising alerts in logs)
- Kafka
    + distributed message bus
    + pro: reliable message delivery in high rate event
    + con: does not provide function to transform, alert, count
    + not a streaming process system, but a key part in streaming architecture
- Flume
    + often thought of as an ingestion mechanism
    + but flume interceptor is good for event-level operation
- Processing structure
    + 50ms: Flume Interceptor, Storm
    + 500ms: Impala, Spark Streaming, Trident
    + 30s: Impala, Spark, Tez
    + 90s: also Map Reduce
```
### 7.1 Storm
### 7.2 Trident
### 7.3 Spark Streaming
### 7.4 Flume Interceptor
```

---
## Case Study 1: Clickstream




---
## Case Study 2: Fraud Detection





---
## Case Study 3: Data warehouse

Data warehouse areas
- Extract-transform-load (ETL) 
    + transforming based on business requirements, 
    + combining data with other data sources,  
    + validating/rejecting data based on some criteria.
- Data archiving
    + Common to move historical data to external archival systems like tape
    + But also inaccessible unless back online
    + Move Enterprise DataWarehouse to Hadoop
- Exploratory analysis
    + ideal sandbox to analyze unstructured data

High Level Data Warehouse
- Source: Operational Source System
- Extract: Data Staging Area
- Load: Data Warehouse
- Apply: Data Analysis / Visualization Tool

High Level Hadoop Data Warehouse
- Operational Source System: to HDFS via Sqoop
- Logs: to HDFS via Flume/Kafka
- HDFS: transformation via Hive/Impala/Spark/HBase
- EDW: connect with Sqoop

###  Defining Use Case
Movielens data: movie rating system as a use case.


### OLTP database

OLTP Schema
- occupation (id, occupation)
- user (id, gender, zipcode, age, occupation_id, last_modified)
- user_rating (id, timestamp, user_id, movie_id, rating)
- movie (id, title, release_date, video_release_date, imdb_url)
- movie_genre (movie_id, genre_id)
- genre (id, name)


### DataWarehouse: Intro and Terminology

fundemental dimensions

- grain
- additivity
- aggregate fact tables
- fact tables
- dimension tables
- calendar dimension
- slowly changing dimensions

Data Modeling and Storage
- Choosing a storage engine
"他边说”不好意思，引号反了“边把论文交给老师”

