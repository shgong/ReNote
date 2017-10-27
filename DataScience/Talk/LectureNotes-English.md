# Data Lecture Notes

8:00pm EST, 2016.04.28

----

## 1. Projects Overview

| Company | Goldman Sachs | Comcast |
| --- | --- | ---|
| Team Size | 30，Global team | 10，Architecture/ETl |
| Cluster Size| main computing cluster 220node, several mid sized 12-16node | QA cluster 12node
| Data Flow | 30+GB/day, require no trnasfer error | 500+GB/day，TBs before compression，not as high requirement
| Data Type | transaction data | ipv6 data flow like complaints


- Main
    + Streaming ETL
    + Data Migration
    + Java Code
        * framework
        * consumer
        * report
        * mapreduce
    + Task Management: Agile
- Notice
    + Autentication，use Kerberos to communicate, key-cache
    + Consumer Design: code reuse, structure
    + HBase: Interval setting, HTable Interface, Buffer setting


## 2. Cluster

- Structure
    + Admin
        * 200 node administrator
        * a whole admin team
    + Separation
        * Computing and Storage, HDFS and HBase in different clusters
        * Read data from HDFS and save to HBase
        * three authentication may make you crazy for some time

- UI interface
    + Cloudera Hue/Impala or Hortonworks Ambari
    + only admin team have access to it.

- MapReduce Job
    + around 6-12 hours, need contact admin team if need ~20 hours
    + Transaction data are streaming in on weekdays, usually do the job on Friday evening
    + you can submit the job to boss, but need review before running

- Cooperation
    + take a lot of time communicate who/when to use cluster
    + some company have mission control team manage this


- Queue name
    + Default: root.default - very low，your MapReduce job stopped when anyone join
    + use the queue name of your team, will use your own resource

- Spark Resource Manager
    + Goldman Sachs: YARN
    + Comcast: Spark Standalone

- Companies are rich
    + Goldman Sachs
        * HDFS and YARN have independent node
        * 12-node cluster only run HBase
    + Comcast have Nifi cluster


## 3. Technologies

### 3.1 Streaming

- Data format
    + JSON
        * rarely used
        * Human readable, High Data Transfer Cost
    + Avro/Protocol Buffer
        * lower transfer cost
        * Schema + Binary => JSON
    + Example: json format changed last year
        * use conversion api in java
        * better solution: update all old format with MapReduce
- Comparison
    + Kafka: Universal, Large dataflow，write Kafka Consumer in Java
    + RabbitMQ: Medium dataflow, data is more secure than kafka
    + Flume: Most universal，like can detect folder log
    + Storm: loop structure, high Python requirement，extremely large dataflow
    + Nifi: Simple and UI only solution



### 3.2 MapReduce

- think twice about logic
- ensure Map output and Reduce input are serializable
    + inherit Writable
    + data cannot be serialized error
- code looks nasty when complexity grows


### 3.3 HBase

- Feature
    + useful
    + immutable(add only)
    + very difficult to design a good rowkey
- Preparation
    + Don't worry about zookeeper structure
    + practice writing queries
    + rowkey and column family
    + Performance

### 3.3 Spark

- Spark
    + Large memory consumption, memory not large enough for 10+TB data
    + have hard disk mode, but no one use that (mapreduce)
    + less stable than MR when using Kerberos

- Use Case
    + common Jobs that MapReduce can do
    + ETL/data cleaning/report
    + will do spark streaming and memSQL
    + very few Machine Learning things

- Data Cleaning
    + column missing
    + formating: NYSE  NYEXCHANGE



### 3.4 Scheduler
- send jar file to MC team
- big company have their own Scheduler，not using Oozie
    + Goldman Sachs: SLANG
    + BoA: PHP
    + Comcast: Bash?

### 3.5 Others

- Machine Learning
    + ALS: Alternative Least Square
- Hive/Pig
    + Hive: sometimes
    + Pig: far less efficient than spark
    + UDF: never used
- customized format: rarely used
    + example: write a HBase Custom Coprocessor
    + MC team do not understand how it works
    + not permit to run, a waste of time
