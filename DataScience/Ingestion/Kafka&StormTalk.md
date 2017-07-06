# Verizon Kafka & Storm Integration

## 1. Problem

### 1.1  Why need kafka & storm

- Original Solution
    + different application in servers
    + put servers to RDBMS
    + run oozie sqoop job
- Problem
    + run every night, not real time
    + report: only have yesterdays'

### 1.2 Verizon Data Size

 - 20 million transaction per day
 - you need tool like this when so much data
 - have a lot of restrictions
     + have India test team
     + not allow to share production data
     
## 2. Technology

### 2.1 Kafka

- Kafka cluster
    + have multiple brokers (servers)
    + have topics in brokers
    + producer / consumer
- the new application
    + Producer
        * write a producer and put to servers
        * call function from producer jar to create data real time
        * Write directly from application to topics
        * Use multithreading
            - actually my colleague does that part
            - so I just do the thinking
    + Topic
        * have multiple partitions
        * can set retention (life) of partitions
    + Speed: 20 Million record /s 
    + Consumer
        * call directly from topics
        * can intergrate with storm
            - other available integrate: Trident, Spark

### 2.2 Storm Structure

- Storm is similar to Hadoop
    + Storm Topology: similar to Hadoop MapReduce
    + Language: Java, Scala, Python
- Storm nodes
    + Nimbus: like Namenode
    + Supervisor: Slave nodes
- Topology
    + Structure
        * Sprout: send stream of data
        * Bolt: process data, like twitter feeds
        * Bolt
        * Bolt
- Bolt Example
    + can do a lot of actions, basically java code
        + write to tables
        + process live feed, json->json
        + link to other application

### 2.3 Integration

- Storm Connection to Kafka
    + a Kafka consumer receive JSON data
    + this consumer is the Sprout of Storm
        * it is not using storm api
        * it is a kafka java code replacement
    + producer send streams to Bolt
        * HBase Bolt: write to HBase
            - Rowkey: use a combination of three key
            - `mobile number, date, transaction-id` as rowkey
        * Hive Bolt: write to Hive Tables
            - Why use Hive with HBase?
            - Same data, same tables
            - HBase is faster, but Hive support joins
    + write in Java
        * can use StreamAnalytics UI to config
        * also handle the integration
            - other solution: StormKafkaConnector
            - storm.kafka.BrokerHost interface
- Verizon have 30 applications
    + now implement all them into Kafka & Storm
    + once hive table ready, will use Hive with Tableau to create report
    + use TableauJS to config in front end
    + create frames to generate dashboards UI
    + see the performance real time


## 3. Trouble Shooting

### 3.1 Data Format Error

 + in Verizon, such error will be solved in the application side
 + so do not need to clean data in the cluster
 + have a troubleshooting framework 
     * can check logs, most cases solved
     * Splunk team use logs for their reports
     * if anything go wrong, Splunk team will give alerts

### 3.2 Version Problem

+ some Server use Java 1.6, met problems
+ Storm use Java 1.7
+ we realize this production have Kerberos secruity
+ Kerbero 4 is not compatible with Java 1.6
+ still trying to solve
    * try to download particular jar to replace missing functions

### 3.3 Configuration issue

+ Work with 4 different team
+ HBase/Hive, StreamAnalysis, Tableau, Kafka/Producer/Sprout
+ Error
    * escape character disrupt json format
    * Tableau collection wrong

### 3.4 Missing data issue

+ cannot stop stream, will lose more data
+ use the traditional oozie-sqoop job
+ directly recover from rdbms
