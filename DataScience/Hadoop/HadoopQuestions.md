
### 1.MapReduce

- Difference between Hadoop 1.0 and Hadoop 2.0?
- What is combiner? Where do you use it?
- How do you implement a map-side join and reduce-side join? What is the - difference?
- Describe the parameters you can set before a mapreduce job.
- Walk through all the stages in a mapreduce job.
- How do you test your MapReduce job?

### 2.Kafka
- How many partitions do your divide the topic?
- What is consumer group?
- How to improve performance when the consumers are overloaded? Name a few ways

### 3. Flume
- What terminal command do you use to start Flume?
- What Flume source do you use? 
- The Flume channel memory size of your project?

### 4. Hive

- Design considerations using Hive vs HDFS?
- Describe a working scenario you use Hive UDF.
- What is the return type of the UDF?
- ClusterBy vs SortBy vs DistributeBy vs OrderBy?
- What types in java collection do Hive support?
- How to join two very large tables in Hive?
- Difference between dynamic partition and static partition?
- What is bucket and why is it useful?

### 5. Avro
- What is the advantage of Avro compared to SequenceFile and csv?
- What is avro schema? Name two field types?
- you add a new schema column to Avro schema, how to set default value for previous data? 

### 6. HBase
- How many regions in your company's HBase cluster?
- Describe what happens when you fill in data to an empty hbase 
- What is compaction?

### 7. Sqoop
- What database do sqoop support?
- How to ensure each imported file averagely distributed?

### 8. Spark
What is the difference between Hadoop and Spark?
What storage modes can you config in Spark nodes?
Where do node store intermediate file when call groupByKey()
How to join two large tables in Spark without using join()?


### code questions

A

```
Given a document in local file system. We want to generate a bigram count with scala RDD transformation, and save back to local file system. Write all the script (shell, scala) involved in this process.

example
what you see is what you get
=>
what you 2
you see 1
see is 1
is what 1
you get 1
```

B

```
A large transction csv file with fields ( state, clientID, date, purchase )

Write scala code to produce
1. Find the top3 state with the most transactions
2. Find the top3 state with the most clients 
3. Find the top3 state with the most transactions for each date
4. Ignore transaction below $100, find top3 clients based on the total purchase
```

C

```
Two csv tables from a college discussion forum, 
(student,date,post) (student,major)

Write MapReduce jobs to output 
the top 5 active students from each major on each date

example:

Jun 15
Physics - jsmith, bcarson, owinfrey, swatson, vkorn
Math - achen, hwillis ...
......

Jun 16
Biology - ......
```
