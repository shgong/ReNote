# Flume

Apache Flume is a service for streaming logs into Hadoop. Apache Flume is a 
distributed, reliable, and available service for efficiently collecting, aggregating, and moving large amounts of streaming data into the Hadoop Distributed File System (HDFS). It has a simple and flexible architecture based on streaming data flows; and is robust and fault tolerant with tunable reliability mechanisms for failover and recovery. 


## 1. Flume Configuration

### 1.1 Define source, sink, channel and agent

```
    agent1.sources = source1
    agent1.sinks = sink1
    agent1.channels = channel1
```

two channels
```
    agent.sources = tailsrc
    agent.channels = mem1 mem2
    agentsinks = std1 std2
```

### 1.2 Source

exec
```
    agent1.sources.source1.type = exec
    agent1.sources.source1.command = tail -F /opt/gen_logs/logs/access.log
```

```
    agent.sources.tailsrc.type = exec
    agent.sources.tailsrc.command = tail -F /home/cloudera/flumetest/in.txt
    agent.sources.tailsrc.batchSize = 1
```

netcat
```
agent1.sources.source1.type = netcat
agent1.sources.source1.bind = 127.0.0.1
agent1.sources.source1.port = 44444
```


### 1.3 Interceptors

exclude matches from regex match
```
agent1.sources.source1.interceptors=i1
agent1.sources.source1.interceptors.i1.type=regex_filter
agent1.sources.source1.interceptors.i1.regex=female
agent1.sources.source1.interceptors.i1.excludeEvents=true
```

```
    agent.sources.tailsrc.interceptors = i1
    agent.sources.tailsrc.interceptors.i1.type = regexextractor
    agent.sources.tailsrc.interceptors.il.regex = ^(\\d)
    agent.sources.tailsrc.interceptors.il.serializers = t1
    agent.sources.tailsrc.interceptors.i1.serializers.t1.name = type
```
  

### Selector

```
    agent.sources.tailsrc.selector.type = multiplexing
    agent.sources.tailsrc.selector.header = type
    agent.sources.tailsrc.selector.mapping.1 = mem1
    agent.sources.tailsrc.selector.mapping.2 = mem2
```

### 1.4 Sink
```
    agent1.sinks.sink1.channel = memory-channel
    agent1.sinks.sink1.type = hdfs
    agent1.sinks.sink1.hdfs.path =flume1
    agent1.sinks.sink1.hdfs.fileType = DataStream
```

```
    agent1.sinks.sink1.hdfs.path = /user/hive/warehouse/flumemaleemployee
    hdfs-agent.sinks.hdfs-write.hdfs.writeFormat=Text
```


### 1.3 Channel
```
    agent1.channels.channel1.type = memory
    agent1.channels.channel1.capacity = 1000
    agent1.channels.channel1.transactionCapacity = 100
```


### 1.5 Binding
```
    agent1.sources.source1.channels = channel1
    agent1.sinks.sink1.channel = channel1
```



## 2. Flume Service

```
flume-ng agent --conf /home/cloudera/flumeconf --conf-file /home/cloudera/flumeconf/flume1.conf --Dflume.root.logger=DEBUG,INFO,console --name agent1

flume-ng agent --conf /home/cloudera/flumeconf --conf-file /home/cloudera/flumeconf/flume1.conf --name agent
```


## 3. Questions

- 100% end-to-end reliability
- consolidation: collect data from different source agent to one
- agent runs independently, scale horizontally
- interceptor: filter event
- selector: separate to channels, multiplexing channel selectors separate based on event's header information

