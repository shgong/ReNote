# Chapter III. Derived Data


## 10. Batch Processing


> A system cannot be successful if it is too strongly influenced by a single person. Once the
initial design is complete and fairly robust, the real test begins as people with many different
viewpoints undertake their own experiments.
—__Donald Knuth__

### 10.1 Unix Tools

- example: nginx default access log

```
216.58.210.78 - - [27/Feb/2015:17:55:11 +0000] "GET /css/typography.css HTTP/1.1"
200 3377 "http://martin.kleppmann.com/" "Mozilla/5.0 (Macintosh; Intel Mac OS X
10_9_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115
Safari/537.36"
```
#### Simple log analysis

```
cat /var/log/nginx/access.log |
  awk '{print $7}' |
  sort |
  uniq -c |
  sort -r -n |
  head -n 5
```

- find most popular five pages
  - awk: split each line into fields why whitespace, and output only the seventh such field from each line (url)
  - sort the urls alphabetically
  - uniq -c: aggregate adjacent line with counter
  - sort -r -n: sort by number reversed
  - head -n: first n lines

```
4189 /favicon.ico
3631 /2013/05/24/improving-security-of-ssh-private-keys.html
2124 /2012/12/05/schema-evolution-in-avro-protocol-buffers-thrift.html
1369 /
915 /css/typography.css
```

```ruby
counts = Hash.new(0)

File.open('/var/log/nginx/access.log') do |file|
  file.each do |line|
    url = line.split[6]
    counts[url] += 1
  end
end

top5 = counts.map{|url, count| [count, url] }.sort.reverse[0...5]
top5.each{|count, url| puts "#{count} #{url}" }
```

- sorting versus in-memoery aggregation
  - ruby script keep in-memoery hash table, better for small-mid
  - unix rely on sorting a list of url, better for large table
    - Linux sort utility auto handle large dataset by splilling to disk

- Unix Philosophy
  - make each program do one thing well
  - expect output of every program input to another
  - design and build software, even os, to be tried early
  - use tools in perference to unskilled help to lighten a programming task
- it's like Agile and DevOps today
  - automation, rapid prototyping, incremental iteration, experiment friendly, modularize
  - little has changed in four decades


### 10.2 MapReduce

- HDFS
  - shared-nothing principle
  - daemon process running on each machine
  - central server called NameNode keep track of file blocks
  - conceptually create one fs that use space of all machines
- MapReduce
  - mapper

- reduce-side join & groups
  - sort-merge join
    - partition to partition
  - group by
    - used in count, sum, statistics
  - Handling skew
    - what if large amount of data related to single key
      - called linchpin objects or hot keys
    - solutions
      - skewed join in Pig first run sampling job
        - then use many reducer randomly for one key, and replicate smaller part
      - skewed join in Hive specify hot key in metadata, thus separate files
        - hot key use map-side join

- reduce-side join
  - you don't need to make any assumptions about input data
  - but sorting, copying, merging reducer inputs can be quite expensive
- map-side join make some assupmption about input data, to make join faster
  - with no reducer, no join

- map-side join
  - broadcast hash join
    - large join small
    - if small enough to fit in memory of each mappers
    - used in
      - Pig replicated join
      - Hive MapJoin
      - Cascading
      - Crunch
      - Impala
  - partitioned hash join
    - if inputs to map-side join partitioned in the same way
    - like partitioned based last 3 digit of userID, with same num of partitions
    - known as bucketed map joins in Hive
  - map-side merge joins
    - if both paritioned and sorted in the same way
    - merge join is very fast

- Hadoop vs Distributed DB
  - diversity of storage
    - structure data to particular model
    - byte sequence
  - diversity of processing models
    - efficient on designed type queries, SQL, graphical analyst
    - more general data processing
  - designing for frequent Faults

### 10.3 Beyond MapReduce

- Materialization of intermediate state
  - have to configure first output as second input
  - in many case it is just intermediate file
  - have downside compared to unix
    - MR job can start only when preceding jobs completed
    - mappers are often redundant, read back the same file
    - 3 replication for intermediate data is a overkill

- New execution engines
  - Dataflow engines
    - Spark
    - Tez
    - Flink
  - arrange oeprators as DAG
  - iterative processing
  - high level API and languages

## 11. Stream Processing

### 11.1 Transmitting Event Stream

- in stream processing context
  - a record is an event
    - encoded
    - from producer to consumer
    - grouped into topic or stream


#### Messaging System

- Unix pipes and TCP connect
  - basic model with one sender and one recipient

- 1. What if producers send messages faster?
  - drop messages
  - buffer messages in a queue
    - what happened as queue grows
      - crash
      - write to disk
      - how disk access affect performance
  - apply back pressure (flow control)
    - like TCP have small fixed buffer, will block producer if full
- 2. What if node crash? Message lost?
  - if you can accept lose message, you can get higher throughput and lower latency
  - vary by systems: sensor or security


- Direct messaging from producer to consumers
  - UDP multicast used in financial industry -> low latency
  - Brokerless messaging like ZeroMQ and nanomsg, use TCP or IP multicast
  - StatsD and Brubeck use UDP for metrics
  - webhooks use HTTP or RPC, with consumer exposing services
- Message brokers
  - alternative: send via message queue, streaming database
  - with broker, durability issue goes to broker instead
    - keep in memory, or write to hard disk in case crashed
  - queueing also make consumers async
- Broker vs Database
  - quick deletion
  - small buffer, degrade if full
  - topic subscription
  - don't support query, notify when data change
- Multiple Consumer: two main patterns
  - load balancing
    - each message deliver to one of consumers
    - when message is expensive to process
    - add consumer to parallel
  - fan-out
    - each message deliver to all consumers
    - broadcasting message
    - when different job consume same file
- Acknowledgement and redelivery
  - how to know if message is lost
  - Acknowledgement: client explicitly tell broker when finish processing a message
  - redeliver may break the message order


#### Paritioned logs

- log-based message broker
  - if add new consumer to messaging system, only start receiving message later
  - if add new client to db, can read data long in the past
  - is hybrid option possible?

- structure
  - producer append message to log
  - consumer read log sequentially, `tail -f` when finished
  - add partition for higher throughput
    - within each partition, broker assigns offset to every message
    - though no order guarantee across partitions
- used in
  - Apache Kafka, Amazon Kinesis Streams, Twitter DistributedLog


- Compared to traditional messaging
  - trivially support fan-out
  - load balancing
    - assign entire partition to nodes in the consumer group
    - each consumer process assigned log partition
  - downside
    - consumer can not exceed log partition number
    - single message delay hold up processing of whole partition
  - use traditional messaging when
    - message takes time to process
    - ordering is not so important


- consumer offsets
  - consumer know processed or not from the offset
    - don't track Acknowledgement
    - periodically record offset is fine
  - similar to log sequence number in single-leader database replication
    - the number allow disconnected follower to resume replication writes


- disk space usage
  - log will eventually run out of disk space
    - delete old segment from time to time
    - circular buffer or ring buffer
  - if a consumer is too slow, may miss some messages
    - 6TB hard disk, 150MB/s throughput -> 11h lagging is enough for manual fix

- replaying old messages
  - as long as log is available
  - can reset consumer id to repeat

### 11.2 Database and Stream

#### Keep Systems in Sync

- data warehouse synchronization between db, cache, search index
  - usually performed by ETL process
  - if periodly full dump are too slow, can use dual writes
    - application code explicitly write to each Systems
    - but with serious problem
      - race condition between clients
      - one of writes may fail, thus inconsistent
- the situation would be better if there really was only one leader
  - can we make search index a follower of database?

#### Change Data Capture

- most DB replication log is internal implementation, not public API
  - difficult to replicate to other systems
  - recently, CDC make changes available as a stream
    - also makes one DB the leader, rest follower
  - used in
    - Linkedin's Databus
    - Facebook's Wormhole
    - Yahoo!'s Sherpa
    - Bottled Water for PostgreSQL
    - Maxwell and Debezium for MySQL
    - Mongoriver for MongoDB
    - GoldenGate for Oracle

- Log compaction
  - LSM-Tree will periodically looks for log records with same key, keep recent
    - update with special null value (tombstone) means key delete
  - the same for log-based message broekrs
  - if CDC set up every change has primary key, every update replace previous value
    - can just keep most recent write
  - supported by Kafka

#### Event Sourcing

- also store all changes to application state as log of events
  - at different level of abstraction than CDC
    - CDC: mutable db, extract log at db low level
    - ES: use immutable events written  to event log, happened at application level
      - easier to understand, evolve and debugging
- derive current state
  - event log by itself is not useful
  - need to transform to application state, replay (like game replays)
  - can not use compaction for events
- also store snapshot for faster recovery

#### States, Streams and Immutability

- derive multiple view from same event log
  - different read-oriented representations
    - Druid, Pistachio, Kafka Connect
  - explicit translation step: event log to db
    - help evolve your application over time
    - new feature introduced

- concurrent control
  - consumer of event log are async
  - ueser may write record, and not found in read
    - Reading your own writes (also a replication problem)
  - 1. update read view synchronouosly with appending event to the log
    - require transaction, make it atomic
  - 2. event help concurrent problem
    - wrap write to name, age, info into an atomic event

### 11.3 Processing Streams

- once you have stream, you can
  - take data in events and write it to storage systems
  - push events to users, like notification, dashboard, emails
  - process 1-n input stream and produce 1-n stream

#### Uses of stream processing


#### Reasoning about time


#### Stream joins



#### Fault tolerance



## 12. The Future of Data Systems
### 12.1 Integration
### 12.2 Unbundling Databases
### 12.3 Correctness
### 12.4 Do the Right thing
