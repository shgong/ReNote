# Chapter II. Distributed Data

- What if multiple machine are involved in storage and retrival of data
  - Scalability
  - Fault tolerance/High Availability
  - Latency
- Common way data distributed across nodes
  - Replication
  - Partitioning

## 5. Replication

- Why copy
  - geographically close to reduce latency
  - allow continue working when failure
  - scale out server to increase read throughput
- trade offs
  - sync or async
  - how to handle failed replica

### 5.1 Leaders and Followers

- leader-based
  - read from any
  - write to leader
    - client send to leader
    - leader write new data
    - leader send to all followers the change stream
  - usage
    - built-in of MySQL, PostgreSQL, SQL Server HA
    - MongoDB, RethinkDB, Espresso
    - Kafka, RabbitMQ HA

- Sync Replication
  - leader wait until follower confirmed ok
  - PRO: confirmed follower has copy, can recover if leader fail
  - CON: must block write if any sync follower crash
- Semi-Sync
  - one of follower is sync
  - switch to another when slow
- No Sync
  - a write is not guaranteed to be durable
  - but still widely used, if have many followers in various location

- New followers
  - take a snapshot, with log sequence / binlog coordinates
  - use backlog to catch up
- Node Outage
  - Follower failure: catch-up recovery
  - Leader failure: failover
    - one of follower promoted to be new leader
    - if async, may recieve conflict writes, overwrite lagged primary keys
    - split brain, two nodes believe it is leader
    - how to decide if leader crashed

- Replication Logs
  - Statement-based replication
    - logs write request as statement, like INSERT, UPDATE, DELETE
    - CON
      - non-deterministic function like NOW(), RAND() may fail
      - autoincrementing column demand strict transaction order
    - used in MySQL before 5.1
  - WAL shipping
    - in case of SSTable and B-Tree, WAL is append-only sequence
    - used in PostgreSQL and Oracle
    - CON
      - describe dat on very low level, byte changes in disk blocks
      - won't work when have version difference
  - Logical (row-based) replication
    - describe write to tables at granularity of a row
    - insert values, delete keys, update news
    - used in MySQL binlog

### 5.2 Replication Lag

- Leader-based replication require all write to one node
  - sync replication will make it unavailable
  - read from async follower may not be constistent

- read-after-write consistency
  - show your own writes
  - read from replica, but may not arrive follower in time
  - how to do
    - read modified from leader, require special knowledge
    - check last update time, and redirect to leader
    - client can remember most recent write
      - what about user's another device?

- moving backward in time
  - make query twice, first to a lag, then a greater lag
  - solution: monotonic reads
    - user read from the same replica

- causal dependency between dialogue
  - answer may come earlier than question
  - solution: consistent prefix reads
    - common in partitioned databases
    - write causally related writes to same partition

### 5.3 Multi-Leader Replication

- multiple data centers
  - within each leader-follower structure
  - PRO
    - avoid every write go across data center, reduce latency
    - tolerance of outage and network issue
  - Big downside
    - resolve conflict across data center

- Clients with offline replication
  - Calendar app with local database, lilke multiple data center
  - rich history of broken calendar sync

- Collaborative editting
  - not a db replicate problem, but similar
  - either lock doc, or handle conflicts

#### Handle write conflicts

- Sync vs async
  - single-leader can block second writer, or let user retry
  - you may make conflict detection sync, but lose main advantage of multi-leader

- Conflict Avoidance
  - makes sure all writes for some record go to same leader
  - route to the same data center (unless data center crash)

- Converge towards consistent state
  - determine which is the final write
  - approaches
    - give write unique ID, like timestamp, pick highest
    - give replica a unique ID, always replicate from higher replica
    - record conflict and prompt the user

- Custom conflict resolution logic
  - on write
    - DB calls conflict handler
    - Bucardo allow you write snippets of Perl to resolve
  - on read
    - multiple versions of data returned
    - CouchDB works this way
  - resolving algorithm
    - Conflict-free replicated datatype CRDT
      - concurrently edible data types, used by Riak 2.0
    - Mergeable persistent data structure
      - track history explicitly, used in Git
    - Operational transformation
      - edit ordered list of items like text document
      - used by Google Doc and etherpad

#### Multi-Leader Replication topology

- relationship between Leaders
  - all-to-all
    - write may arrive in wrong order
  - circular: forward into a circle, MySQL default
    - may fail on one node
  - star: one root node
    - may fail on root node

### 5.4 Leaderless Replication

- Amazon Dynamo DB
  - Riak, Casssandra, Voldemort

- failover does not exist
  - write
    - client send write to all three replica
    - if receive two ok, ignore the fact that one replica missed
  - read
    - sent to several nodes in parallel
    - use the version number determine which is newer

- how to catch up
  - read repair
    - when read, find stale value from wrong replica
    - works well when frequent read
  - anti-entropy process
    - background process that looks for differentce

- Quorum
  - n replica
  - every write must be confirmed by w node
  - every read must query r nodes
  - w+r > n
- DynamoDB
  - n is odd number
  - w = r = (n+1)/2
- Limitation of Quorum Consistency
  - likely to be edge case where stale value returned
  - can tolerate eventual consistence

- Sloppy Quorum and Hinted Handoff
  - what if network is interrupted
    - return errors to all requests that can not reach w/r home nodes?
    - accept errors anyway, write to reachable nodes (neighbors)
  - latter is sloppy quorum
  - when networking resolves, hand off to home nodes

#### Detect concurrent writes

- What if several clients write to same key
  - A,B write to key X in 3 node data store
    - node 1 receive A
    - node 2 receive A then B
    - node 3 receive B then A
- approaches
  - last write wins
    - force an arbitrary order
    - attach timestamp to each write
    - safe way to use: ensure key is written once
  - happens-before relationship
    - server maintain version number for every key
      - or version vector, for multiple replica
    - when read, return all values that haven't been overwritten
    - when write, must include version number from previous read
      - client had to merge values
    - server can overwrite all below that version number


## 6. Partitioning

- Partitioned DB
  - Teradata 1980s
  - NoSQL
  - Hadoop-based warehouse

### 6.1 Parition and Replication

- a typical node
  - partition 1 lead
  - partition 2 follower
  - partition 3 follower

### 6.2 Key-value Data

- which record on which node?
  - skewed: some partition has more data, less effective

- Parition by key range
  - PRO
    - manually or auto choose partition boundry
    - keep keys sorted within each partition, help range scan
  - CON
    - easily skewed, or worse, hotspot
    - if key is timestamp, all records go to same partition

- Partition by key hash
  - PRO: good hash function unify skewed data
    - Cassandra and MongoDB use MD5
    - Voldemort use Fowler-Noll-Vo function
  - CON
    - range query send to all nodes in MongoDB
    - Riak, Couchbase, Voldemort don't support range query

- Cassandra
  - can have compound primary key
    - first part hashed to determine partition
    - rest part as index for sorting inside SSTables
  - support range query on rest part
  - enables an elegant one-to-many relationship

- Other Skewed Occasion
  - celebrity on social network
  - if a key is hot, add random number to end of key
    - but have to read from lots of nodes

### 6.3 Secondary Indexes
### 6.4 Rebalancing
### 6.5 Request Routing

## 7. Transactions
### 7.1 Concept
### 7.2 Weak Isolation Levels
### 7.3 Serializability

## 8. The Trouble with Distributed Systems
### 8.1 Faults and Partial Failures
### 8.2 Unreliable Networks
### 8.3 Unreliable Clocks
### 8.4 Knowledge Truth and Lies

## 9. Consistency and Consensus
### 9.1 Consistency Guarantee
### 9.2 Linearizability
### 9.3 Ordering Guarantees
### 9.4 Distributed Transactions and Consensus
