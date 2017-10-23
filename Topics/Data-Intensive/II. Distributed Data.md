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
### 5.4 Leaderless Replication




## 6. Partitioning
### 6.1 Parition and Replication
### 6.2 Key-value Data
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
