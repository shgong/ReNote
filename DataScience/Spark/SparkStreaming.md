# SparkStreaming

## 1. Simple Example

```scala
// scala imports
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.Duration
import org.apache.spark.streaming.Seconds

// Create a StreamingContext with a 1-second batch size from a SparkConf
val ssc = new StreamingContext(conf, Seconds(1))
// Create a DStream after connecting to port 7777 on the local machine
val lines = ssc.socketTextStream("localhost", 7777) 
// Filter our DStream for lines with "error"
val errorLines = lines.filter(_.contains("error")) 
errorLines.print()

// Start our streaming context and wait for it to "finish"
ssc.start()
// Wait for the job to finish 
ssc.awaitTermination()

```


```bash
spark-submit --class com.StreamingLogInput  $ASSEMBLY_JAR local[4]
nc localhost 7777 # Lets you type input lines to send to the server
```

## 2. Stateless transforation
- map, reduceByKey, join

```scala
// ApacheAccessLog: a utility class for parsing entries from Apache logs
val accessLogDStream = logData.map(
        line => ApacheAccessLog.parseFromLogLine(line)) 

val ipDStream = accessLogsDStream.map(entry => (entry.getIpAddress(), 1))
val ipCountsDStream = ipDStream.reduceByKey((x, y) => x + y)

val ipBytesDStream = accessLogsDStream.map(entry => (entry.getIpAddress(), entry.getContentSize()))
val ipBytesSumDStream = ipBytesDStream.reduceByKey((x, y) => x + y)

val ipBytesRequestCountDStream = ipCountsDStream.join(ipBytesSumDStream)
```

transform()
- advanced operator if above are insufficient
- provide any arbitrary RDD function
- get called on each batch data

if you had a function, extractOutliers(), that acted on an RDD of log lines to produce an RDD of outliers (perhaps after running some statistics on the messages), you could reuse it within a transform()

```scala
val outlierDStream = accessLogsDStream.transform { rdd => extractOutliers(rdd)
}
```


## 3. Stateful Transformations

- track data across time
- main types
    + windowed operation
    + updateStateByKey()
- require checkpointing enabled
    + pass a directory with `ssc.checkpoint("hdfs://...")`

### 3.1 Windowed

- Parameters: (Window duration, batch interval)
- reduce function
    + reduceByWindow()
    + reduceByKeyAndWindow()
- incrementally
    + compute reduction consider only data going out and going in
    + this require inverse of reduce function

```scala
val accessLogsWindow = accessLogsDStream.window(Seconds(30), Seconds(10)) val windowCounts = accessLogsWindow.count()

// incremental
val ipDStream = accessLogsDStream.map(logEntry => (logEntry.getIpAddress(), 1)) 
val ipCountDStream = ipDStream.reduceByKeyAndWindow(
{(x, y) => x + y}, // Adding elements 
{(x, y) => x - y}, // Removing elements 
Seconds(30), // Window duration
Seconds(10)) // Slide duration

// windowed count
val ipDStream = accessLogsDStream.map{entry => entry.getIpAddress()}
val ipAddressRequestCount = ipDStream.countByValueAndWindow(Seconds(30), Seconds(10)) 
val requestCount = accessLogsDStream.countByWindow(Seconds(30), Seconds(10))
```


### 3.2 UpdateStateByKey

- maintain state across the batches
- provide a state variable for DStreams of k/v pairs
- updateStateByKey()
    + provide function: update(events, oldState), return new state
    + example: keep a running count of the number of log messages with each HTTP response code

```scala
// value size to state
def updateRunningSum(values: Seq[Long], state: Option[Long]) = { 
    Some(state.getOrElse(0L) + values.size)
}

val responseCodeDStream = accessLogsDStream.map(log => (log.getResponseCode(), 1L))
val responseCodeCountDStream = responseCodeDStream.updateStateByKey(updateRunningSum _)

```


## 4 Output Operation
```scala
// save to text file
ipAddressRequestCount.saveAsTextFiles("outputDir", "txt")

// save to Sequence
val writableIpAddressRequestCount = ipAddressRequestCount.map { (ip, count) => (new Text(ip), new LongWritable(count)) }

writableIpAddressRequestCount.saveAsHadoopFiles[ SequenceFileOutputFormat[Text, LongWritable]]("outputDir", "txt")
```

## 5 Input Sources

- Core Sources
    + Stream of files
    + Sequence File
- Additional Sources
    + Kafka
    + Flume
        * push-based receiver: Avro sink
        * pull-based receiver: pull from intermediate sink

Core
```scala
// Text File
val logData = ssc.textFileStream(logDirectory)

// Sequence File
ssc.fileStream[LongWritable, IntWritable, 
    SequenceFileInputFormat[LongWritable, IntWritable]](inputDirectory).map { 
    case (x, y) => (x.get(), y.get())
}
```

Apache Kafka
```scala
import org.apache.spark.streaming.kafka._
...
// Create a map of topics to number of receiver threads to use
val topics = List(("pandas", 1), ("logs", 1)).toMap
val topicLines = KafkaUtils.createStream(ssc, zkQuorum, group, topics) StreamingLogInput.processLines(topicLines.map(_._2))
```

Flume
```
// push
a1.sinks = avroSink
a1.sinks.avroSink.type = avro
a1.sinks.avroSink.channel = memoryChannel
a1.sinks.avroSink.hostname = receiver-hostname
a1.sinks.avroSink.port = port-used-for-avro-sink-not-spark-port

val events = FlumeUtils.createStream(ssc, receiverHostname, receiverPort)

// pull
a1.sinks = spark
a1.sinks.spark.type = org.apache.spark.streaming.flume.sink.SparkSink
a1.sinks.spark.hostname = receiver-hostname
a1.sinks.spark.port = port-used-for-sync-not-spark-port
a1.sinks.spark.channel = memoryChannel

val events = FlumeUtils.createPollingStream(ssc, receiverHostname, receiverPort)
```
