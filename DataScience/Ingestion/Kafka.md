# Kafka

## 1. Kafka Basics

- publish-subscribe messaging system from LinkedIn
- messages 
    + organized by topics
    + persist for 7 days
    + each topic divided into partitions, replicated & distributed
    + assigned an offset
- Kafka does not know about the status of each consumer
- consumer have to maintain an offset per partition

## 2. Kafka Program

### 2.1 Environment

```
# maintained by zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# start server
bin/kafka-server-start.sh config/server.properties

# start topic
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic mytopic

# update to two topic
bin/kafka-topics.sh --zookeeper localhost:2181 --alter --partitions 2 --topic mytopic

```

### 2.2 Producer

Maven 

```xml
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <version>0.9.0.1</version>
</dependency>
```


Kafka Producer

```java
package com.ipponusa;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import java.util.Properties;

public class SimpleStringProducer {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        // SETTING
        // host & port to kafka server
        // serializer of key/value of message

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        for (int i = 0; i < 100; i++) {
            ProducerRecord<String, String> record = new ProducerRecord<>("mytopic", "value-" + i);
            producer.send(record);
        }

        producer.close();
    }
}
```

Kafka Consumer
```java
package com.ipponusa;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Properties;

public class SimpleStringConsumer {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "mygroup");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        // group.id  tell kafka which consumer group belong

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList("mytopic"));

        boolean running = true;
        while (running) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                System.out.println(record.value());
            }
        }
        // use poll to query for messages
        // return a list of Consumer Records object

        consumer.close();
    }
}
```

## 3. Kafka & Spark

### 3.1 Spark Streaming
- DStream( Discretized Streams )
    + event/message are accumulated over short period
    + micro-batches
    + DStream->RDD1->RDD2->RDD3->RDD4

```java
package com.ipponusa;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

public class SparkStringConsumer {

    public static void main(String[] args) {

        SparkConf conf = new SparkConf()
                .setAppName("kafka-sandbox")
                .setMaster("local[*]");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaStreamingContext ssc = new JavaStreamingContext(sc, new Duration(2000));

        // TODO: processing pipeline

        ssc.start();
        ssc.awaitTermination();
    }
}

```

### 3.2 Spark Streaming with Kafka

Maven

```xml
<dependency>
    <groupId>org.apache.spark</groupId>
    <artifactId>spark-streaming-kafka_2.10</artifactId>
    <version>1.6.0</version>
</dependency>
```

Two ways to link kafka

- Receiver-based Approach
- Direct Approach (newer, more efficient, easier)

Set up Kafka Connector
```java
Map<String, String> kafkaParams = new HashMap<>();
kafkaParams.put("metadata.broker.list", "localhost:9092");
Set<String> topics = Collections.singleton("mytopic");

JavaPairInputDStream<String, String> directKafkaStream = KafkaUtils.createDirectStream(ssc,
        String.class, String.class, StringDecoder.class, StringDecoder.class, kafkaParams, topics);

```

Processing
```java
directKafkaStream.foreachRDD(rdd -> {
    System.out.println("--- New RDD with " + rdd.partitions().size()
            + " partitions and " + rdd.count() + " records");
    rdd.foreach(record -> System.out.println(record._2));
});
```

- messages are not in the same order as they were published.
- Spark Streaming’s direct approach makes a one-to-one mapping between partitions in a Kafka topic and partitions in a Spark RDD
- each RDD partition is processed in parallel by separate threads.

## 4. Avro

### 4.1 Avro: Data Serialization system

- JSON schema: decribe fields and types
- two cases
    + serializing: schema written to the file
    + RPC System like Kafka: both system know scheme prior
- advantage
    + do not need to generate data classes

Maven
```xml
<dependency>
    <groupId>org.apache.avro</groupId>
    <artifactId>avro</artifactId>
    <version>1.8.0</version>
</dependency>
```

Schema
```json
{
    "fields": [
        { "name": "str1", "type": "string" },
        { "name": "str2", "type": "string" },
        { "name": "int1", "type": "int" }
    ],
    "name": "myrecord",
    "type": "record"
}
```

## 4.2 Serde: twitter bijection
```xml
<dependency>
    <groupId>com.twitter</groupId>
    <artifactId>bijection-avro_2.10</artifactId>
    <version>0.9.2</version>
</dependency>
```

Process with Serde
```java
Injection<GenericRecord, byte[]> recordInjection = GenericAvroCodecs.toBinary(schema);

# Ser
GenericData.Record record = new GenericData.Record(schema);
avroRecord.put("str1", "My first string");
avroRecord.put("str2", "My second string");
avroRecord.put("int1", 42);
byte[] bytes = recordInjection.apply(record);

# De
GenericRecord record = recordInjection.invert(bytes).get();
String str1 = (String) record.get("str1");
String str2 = (String) record.get("str2");
int int1 = (int) record.get("int1");
```

## 4.3 Producer

```java
package com.ipponusa.avro;

import com.twitter.bijection.Injection;
import com.twitter.bijection.avro.GenericAvroCodecs;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class SimpleAvroProducer {

    public static final String USER_SCHEMA = "{"
            + "\"type\":\"record\","
            + "\"name\":\"myrecord\","
            + "\"fields\":["
            + "  { \"name\":\"str1\", \"type\":\"string\" },"
            + "  { \"name\":\"str2\", \"type\":\"string\" },"
            + "  { \"name\":\"int1\", \"type\":\"int\" }"
            + "]}";

    public static void main(String[] args) throws InterruptedException {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");

        Schema.Parser parser = new Schema.Parser();
        Schema schema = parser.parse(USER_SCHEMA);
        Injection<GenericRecord, byte[]> recordInjection = GenericAvroCodecs.toBinary(schema);

        KafkaProducer<String, byte[]> producer = new KafkaProducer<>(props);

        for (int i = 0; i < 1000; i++) {
            GenericData.Record avroRecord = new GenericData.Record(schema);
            avroRecord.put("str1", "Str 1-" + i);
            avroRecord.put("str2", "Str 2-" + i);
            avroRecord.put("int1", i);

            byte[] bytes = recordInjection.apply(avroRecord);

            ProducerRecord<String, byte[]> record = new ProducerRecord<>("mytopic", bytes);
            producer.send(record);

            Thread.sleep(250);

        }

        producer.close();
    }
}
```


## 4.4 Consumer
```java
package com.ipponusa.avro;

import com.twitter.bijection.Injection;
import com.twitter.bijection.avro.GenericAvroCodecs;
import kafka.serializer.DefaultDecoder;
import kafka.serializer.StringDecoder;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SparkAvroConsumer {

    public static void main(String[] args) {
        SparkConf conf = new SparkConf()
                .setAppName("kafka-sandbox")
                .setMaster("local[*]");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaStreamingContext ssc = new JavaStreamingContext(sc, new Duration(2000));

        Set<String> topics = Collections.singleton("mytopic");
        Map<String, String> kafkaParams = new HashMap<>();
        kafkaParams.put("metadata.broker.list", "localhost:9092");

        JavaPairInputDStream<String, byte[]> directKafkaStream = KafkaUtils.createDirectStream(ssc,
                String.class, byte[].class, StringDecoder.class, DefaultDecoder.class, kafkaParams, topics);

        directKafkaStream.foreachRDD(rdd -> {
            rdd.foreach(avroRecord -> {
                Schema.Parser parser = new Schema.Parser();
                Schema schema = parser.parse(SimpleAvroProducer.USER_SCHEMA);
                Injection<GenericRecord, byte[]> recordInjection = GenericAvroCodecs.toBinary(schema);
                GenericRecord record = recordInjection.invert(avroRecord._2).get();

                System.out.println("str1= " + record.get("str1")
                        + ", str2= " + record.get("str2")
                        + ", int1=" + record.get("int1"));
            });
        });

        ssc.start();
        ssc.awaitTermination();
    }
}
```


## Consumer Group

Consumer Group: High Level Consumer
- Why
    + sometimes logic don't care about message offset
    + high level Consumer abstract most details of consuming events
    + store last offset read from specific partition from zookeeper
        * based on the name of consumer group
        * When a new process is started with the same Consumer Group name, Kafka will add that processes' threads to the set of threads available to consume the Topic and trigger a 're-balance'. 
        * During this re-balance Kafka will assign available partitions to available threads, possibly moving a partition to another process.
- a high level consumer
    + must be multi-threaded application
    + rules
        * if partition < thread, useless thread
        * if thread < partition, multiple partition per thread
        * no guarantee about message order
        * adding more processes/threads will cause Kafka to re-balance, possibly changing the assignment of a Partition to a Thread.

```java
package com.test.groups;
 
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
 
public class ConsumerTest implements Runnable {
    private KafkaStream m_stream;
    private int m_threadNumber;
 
    public ConsumerTest(KafkaStream a_stream, int a_threadNumber) {
        m_threadNumber = a_threadNumber;
        m_stream = a_stream;
    }
 
    public void run() {
        ConsumerIterator<byte[], byte[]> it = m_stream.iterator();
        while (it.hasNext()) // Read until you stop it
            System.out.println("Thread " + m_threadNumber + ": " + new String(it.next().message()));
        System.out.println("Shutting down Thread: " + m_threadNumber);
    }
}

```

Configure
```java
private static ConsumerConfig createConsumerConfig(String a_zookeeper, String a_groupId) {
        Properties props = new Properties();
        props.put("zookeeper.connect", a_zookeeper);
        props.put("group.id", a_groupId);
        props.put("zookeeper.session.timeout.ms", "400");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.offset.reset", "smallest");
        return new ConsumerConfig(props);
    }
```

run with
```sh
xxxx.jar server01.myco.com1:2181 group3 myTopic  4
```


### Consumer Groups and Topic Subscriptions

- the concept of consumer groups
    + allow a pool of processes to divide up the work of consuming and processing records. 
    + either be running on the same machine 
    + distributed over many machines 
- Each Kafka consumer 
    + can  configure a consumer group that it belongs to
    + can dynamically set the list of topics it wants to subscribe to through subscribe(List, ConsumerRebalanceListener)
    + or subscribe to all topics matching certain pattern through subscribe(Pattern, ConsumerRebalanceListener). 
- Kafka will deliver each message in the subscribed topics to one process in each consumer group. 
    + achieved by balancing the partitions in the topic over the consumer processes in each group. 
    + if there is a topic with four partitions, and a consumer group with two processes, each process would consume from two partitions. 
    + This group membership is maintained dynamically: 
        * if process fails the partitions -> reassigned to other processes in the same group
        * if a new process joins -> partitions will be moved from existing consumers to this new process.
    + So if two processes subscribe to a topic both specifying different groups they will each get all the records in that topic; if they both specify the same group they will each get about half the records.

- Conceptually you can think of a consumer group as being a single logical subscriber that happens to be made up of multiple processes. 

- traditional messaging system 
    + all processes would be part of a single consumer group and hence record delivery would be balanced over the group like with a queue. 
    + Unlike though, you can have multiple such groups. each process would have its own consumer group, so each process would subscribe to all the records published to the topic.
- Group Reassignment 
    + happens automatically, consumers can be notified through ConsumerRebalanceListener, which allows them to finish necessary application-level logic such as state cleanup, manual offset commits (note that offsets are always committed for a given consumer group), etc. 
    + It is also possible for the consumer to manually specify the partitions that are assigned to it through assign(List), which disables this dynamic partition assignment.




```java
package com.test.groups;
 
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
 
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
 
public class ConsumerGroupExample {
    // Consumer, Topic, Executor
    private final ConsumerConnector consumer;
    private final String topic;
    private  ExecutorService executor;
 
    // Construct by ConsumerConfig
    public ConsumerGroupExample(String a_zookeeper, String a_groupId, String a_topic) {
        consumer = kafka.consumer.Consumer.createJavaConsumerConnector(
                createConsumerConfig(a_zookeeper, a_groupId));
        this.topic = a_topic;
    }
 

    // Shutdown
    public void shutdown() {
        if (consumer != null) consumer.shutdown();
        if (executor != null) executor.shutdown();
        try {
            if (!executor.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                System.out.println("Timed out waiting for consumer threads to shut down, exiting uncleanly");
            }
        } catch (InterruptedException e) {
            System.out.println("Interrupted during shutdown, exiting uncleanly");
        }
   }
 

    public void run(int a_numThreads) {
        // Map: Topic->Count
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, new Integer(a_numThreads));
        // Map: Topic -> List of consumers (Kafka Stream)
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        // the consumer list for this topic
        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);
 
        // now launch all the threads
        executor = Executors.newFixedThreadPool(a_numThreads);
 
        // now create object to consume the messages
        int threadNumber = 0;
        for (final KafkaStream stream : streams) {
            executor.submit(new ConsumerTest(stream, threadNumber));
            threadNumber++;
        }
    }
 
    // Config Setup
    private static ConsumerConfig createConsumerConfig(String a_zookeeper, String a_groupId) {
        Properties props = new Properties();
        props.put("zookeeper.connect", a_zookeeper);
        props.put("group.id", a_groupId);
        props.put("zookeeper.session.timeout.ms", "400");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
 
        return new ConsumerConfig(props);
    }
 
    public static void main(String[] args) {
        String zooKeeper = args[0];
        String groupId = args[1];
        String topic = args[2];
        int threads = Integer.parseInt(args[3]);
 
        ConsumerGroupExample example = new ConsumerGroupExample(zooKeeper, groupId, topic);
        example.run(threads);
 
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ie) {
 
        }
        example.shutdown();
    }
}
```




### Scala Kafka

```scala
/**
 * Illustrates a basic Kafka stream
 */
package com.oreilly.learningsparkexamples.scala

import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._

object KafkaInput {
  def main(args: Array[String]) {
    val Array(zkQuorum, group, topic, numThreads) = args
    val conf = new SparkConf().setAppName("KafkaInput")
    // Create a StreamingContext with a 1 second batch size
    val ssc = new StreamingContext(conf, Seconds(1))
    // Create a map of topics to number of receiver threads to use
    val topics = List((topic, 1)).toMap
    val topicLines = KafkaUtils.createStream(ssc, zkQuorum, group, topics)
    val lines = StreamingLogInput.processLines(topicLines.map(_._2))
    lines.print()
    // start our streaming context and wait for it to "finish"
    ssc.start()
    // Wait for 10 seconds then exit. To run forever call without a timeout
    ssc.awaitTermination(10000)
    ssc.stop()
  }
}
```
