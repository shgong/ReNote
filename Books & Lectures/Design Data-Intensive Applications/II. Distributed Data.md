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

#### 6.3.1 Document based

- car sale website
  - key: docID
    - 191 -> {color:red. make:Honda, location:Palo Alto}
  - secondaryL color, make
    - color:red -> [191]

- scatter/gather
  - each partition is completely separate
    - secondary index only cover that partition
  - search query will send to all partitions
    - read query can be expensive
  - used in
    - MongoDB, Riak, Cassandra, ElasticSearch, Solr, VoltDB

#### 6.3.2 Term based

- construct global index
  - this index is also partitioned
  - partition by term help scan/search
- write is slower
  - might effect multiple index partitions
  - in practice, secondary index are often async
- used in
  - DynamoDB, Riak search feature


### 6.4 Rebalancing

- Not to do: Hash mod N
  - if N change, most record will move to another node

- Fixed number of partitions
  - 10 nodes and 1000 partitions
  - when full, only entire partitions are moved
  - used in Riak, ElasticSearch, Couchbase and Voldemort

- Dynamic Partitioning
  - use key range, can not move around
  - split in half when full
  - manually set partition when data is small
  - used in HBase and RethinkDB

- Proportional to nodes
  - when new node join, random choose existing partition to split
  - 256 partitions per node by default
  - used in Cassandra and Ketama

### 6.5 Request Routing

- Which partition should client connect to?
- Service discovery problem
  - 1. client contact any node, if not, forward to another node
    - Cassandra and Riak use gossip protocol among nodes
    - nodes will forward query
  - 2. client contact routing tier, as load balancer
    - Zookeeper
    - LinkedIn's Espresso use Helix, rely on Zookeeper
    - Kafka use Zookeeper
    - MongoDB use own config server
  - 3. client fetch metadata, and contact right node


## 7. Transactions

> Some authors have claimed that general two-phase commit is too expensive to support, because of the performance or availability problems that it brings. We believe it is better to have application programmers deal with performance problems due to overuse of transac‐ tions as bottlenecks arise, rather than always coding around the lack of transactions.
—__James Corbett__ et al., Spanner: Google’s Globally-Distributed Database (2012)

- Transaction
  - a way to group several reads & writes into a logical unit
  - either entire transaction succeed or fails
  - so application can safely retry when fail
    - without worry about partial failure

### 7.1 Concept
#### 7.1.1 ACID

- safety guarantee provided by transaction
  - Atomicity
    - atomic: can not be broken down into smaller part
    - transaction is atomic, if one of writes failed, whole transaction is aborted
  - Consistency
    - certain statements about your data must always be true
    - like in accounting system, credits and debits are balanced
  - Isolation
    - transactions are isolated from each other
  - Durability
    - transaction succeed, the data will not be lost


- Consistency is overloaded
  - replica consistency: eventual consistency, replica become sync
  - CAP theorem: Linearizability
  - ACID: good state

#### Single-Object & Multi-Object

- multi-object transactions are needed to keep sync
  - atomicity + isolation: message and counter should be in sync
  - how to determine which read/write belong to same transaction
    - client: based on TCP connection
      - BEGIN TRANSACTION and COMMIT
- single object write
  - write 20kb json to a db, what if power/network fails, or other client reads?
  - Atomicity: use log for crash recovery
  - Isolation: use a lock on each object
- multi-object transaction
  - difficult to implement across partitions
  - but useful in scenarios
    - rdb, foreign key reference
    - document db that lack join, encourage denormalization sync
    - secondary index update

### 7.2 Weak Isolation Levels

- Concurrency bugs
  - triggered only bad luck with timing
  - might occur very rare
  - difficult to reason about, large application with partitions
- DB try to hide concurrency issue from developers, by transaction isolation
  - many popular RDB use weak isolation, can cause issue

#### Read Commited
- rule
  - no dirty reads: can not see uncommited data
  - no dirty writes: can not overwrite uncommited data
- used
  - Oracle 11g, PostgreSQL, SQL Server 2012, MemSQL etc
- achieve
  - aborts
  - prevent reading incomplete results
  - prevent concurrent writes
- implementation
  - row-level locks: transaction acquire lock on row object
  - also copy old commited value, so don't need read lock (extra await

#### Snapshot Isolation
- Serializable (Oracle) or Repeatable Read(MySQL, PostgreSQL)
- problem with read commited
  - read skew
    - multiple object transaction, snapshot of both object may not be in sync
    - like transfer transaction, there is a time when total money become less
  - usually reload is fine
  - but when backup, or run analytical queries, may cause issue
- used
  - PostgreSQL, mySQL with InnoDB, Oracle, SQL Server
- Implementation
  - reader never block writer, writer never block reader
  - keep commit history of a object, data tagged with transaction ID
  - define visible rules to present consistent snapshot

#### Preventing lost updates
- above two: what read-only transaction can see
- for concurrent writing transactions
  - second write may not include first modification
    - increase counter
    - append element
- atomic write operation
  - many database provide such update operation
  - concurrency-safe code
    - `update counters SET value = value + 1 WHERE key = 'foo'`
  - implementation
    - take exclusive lock on object when it is reads
    - force all atomic operation run on same thread
  - ORM framework easily perform read-modify-write cycle, instead of atomic DB operation
- explicit locking
  - add an explicit lock to forbid other play move pawn
  - `SELECT * FROM figures WHERE name='robot' AND game_id=222 FOR UPDATE`
  - for update will lock on all rows returned by the query

- CAS Compare-and-set
  - provided in databases that don't have transaction
  - provide read result as write condition
  - `UPDATE wiki_pages SET content = 'new content'`
  - ` WHERE id = 1234 AND content = 'old content';`

- Replication
  - lock and CAS assume there is single up-to-date copy
  - replicated database allow concurrent writes create multiple version
  - unfortunately, LWW is default in many replicated DB

#### Write Skew and Phantoms

- race conditions
  - dirty writes
  - lost updates
  - write skew
    - two transaction updating two different objects
      - two doctor cancel on-call
      - due to snapshot isolation, total count return 2 for both
    - multiplayer game
    - meeting room registration
    - claiming unique username
    - prevent credit double-spending

- Phantoms
  - a write in one transaction change result of another transaction

- solution
  - DB config constraints (most DB don't have)
  - explicitly lock the rows
    - `SELECT * FROM doctors WHERE on_call = true AND shift_id = 1234 FOR UPDATE;`

- what if there is no object to which can attach lock?
  - materializing conflicts (last resort)
  - a serializable isolation is more preferable


### 7.3 Serializability

- strongest isolation level
  - guarantees parallel execution have serially effect correctly
  - why not everyone use it?

#### Actual Serial Execution

- remove concurrency entirely, one transaction at a time, single thread
- why the old way
  - RAM price drop
  - OLTP and OLAP separation
- used
  - VoltDB/H-Store, Redis, Datomic
- serial execution requirements
  - every transaction must be small and fast
  - limitation that active dataset fit in memory
  - write throughput must be low

#### Two-Phase Locking (2PL)

- much stronger lock requirement than `no dirty writes`
  - if A read, B want write, B wait until A commits or aborts
  - if A write, B want read, B wait until A commits or aborts
- contrary to snapshot isolation (read never block write)

- implementation
  - have a lock on each object in the database
  - read acquires the lock in shared mode
  - write acquires the lock in exclusive mode
  - read=>write will upgrade the lock
  - lock continue hold until end of transaction (commit/abort)
- deadlock may happen easily
- bad performance


- predicate lock
  - if one transaction search for existing booking for a room within a window
  - another transaction is not allowed to update another booking for the room/range
- `SELECT * FROM bookings WHERE room=123 AND end_time>12 AND start_time <13`
  - the lock belong to all objects that match certain conditions
  - A read, acquire a shared-mode predicate lock on conditions of the query
    - if B has lock on matching condition query, A must wait
  - A write, first check if value match any existing predicate lock
    - if B held the lock, A must wait


- index-range lock
  - predicate lock do not perform well
    - check too much lock is time consuming
  - simplify predicate
    - predicate lock for room 123, noon and 1pm
    - lock all room between noon and 1pm
    - or lock room 123
  - may lock bigger range of objects, but low overhead

#### Serializable Snapshot Isolation

- concurrency control
  - serializability
    - that don't perform well - 2PL
    - that don't scale well - serial
  - weak isolation with good performance
    - that prone to race conditions
      - lost update
      - write skew & phantoms
- SSI
  - used in PostgreSQL 9.1+ and FoundationDB
  - when a transaction want to commits, database check if anything bad happen
    - if so, abort and retry
    - otherwise, allow to commit
  - perform badly when high contention, lots of abortion
    - but with enough space capacity, will out perform 2PL & serial
  - implementation
    - on top of snapshot isolation
    - add an algorithm for detecting serialization conflicts among writes

- How to know if query result might have changed?
  - detect stale MVCC object version (uncommitted write before read)
    - track when A ignore B's write due to MVCC visibility rule
    - when commit A, check whether any ignored writes have been commited
      - why wait for commit? A might be read-only transaction
  - detect writes affect prior reads
    - A B both search for index entry, record the fact they read
    - when A write, look in indexes for any other transaction read affected data
      - similar to acquiring a write lock on key range

## 8. The Trouble with Distributed Systems

- new and exiciting ways for things to go wrong
- thoroughly pessimistic and depressing

### 8.1 Faults and Partial Failures

- Cloud computing vs Supercomputing
  - supercomputer with thousands CPU, weather forecasting or molecular dynamics
  - cloud computing with multi-tenant datacenters, connected with IP network
    - commodity computer, lower cost, higher failure rates

### 8.2 Unreliable Networks

- internet and most internal networks in datacenters (Ethernet)
  - are asynchronous packet network
  - network gives no guarantees to requests
  - only information is timeout
- Faults in practice
  - mid-sized datacenter found 12 network faults per month
  - public cloud like EC2 are notorious for frequent transient network glitch
  - well-managed private data center can be stabler
- Detecting Faults
  - Load balancer need to know when assign task
  - single-leader replication, need promote follower
- How
  - TCP connection is refused
  - node process crashed or killed, a script on OS can inform other nodes (HBase)
  - network interface, query at hardware level to see power down
  - or, time out
- Time out
  - detect faster, but may be just a slowdown
  - when already with high load but just slow down, declare dead may deteorior the situation
  - transfer load to other nodes cause cascading failure (every nodes declare each other dead)

- Network congestion & queueing
  - situation
    - multiple nodes send packet to same direction
    - when packet reach destination but all CPU core busy
    - TCP flow control, node limit its rate of sending
    - TCP consider packet lost and retransmitted
  - TCP vs UDP
    - UDP don't flow control, don't retransmit lost package
    - useful when data is not valuable
    - like video conference and VoIP

### 8.3 Unreliable Clocks

- problems
  - duration
    - has this request time out?
    - what is 99th percentile response time
    - how many queries per second
    - how long user stay
  - points
    - when was article published
    - when to send reminder email
    - when cache entry expire
    - timestamp of error message
- time is tricky
  - network communication take time
  - network may delay
  - machine clock: quartz crystal oscillator are not perfect

- Clock kind
  - time-of-day
    - seconds since epoch (midnight UTC on Jan 1, 1970)
    - Java: System.currentTimeMillis()
    - Linux: Clock_gettime(CLOCK_REALTIME)
    - need synchronization with NTP server
      - google assume a clock drift of 200 ppm
      - 6ms/30s, or 17s/day
      - leap second: let NTP server lie by split gradually among the day
  - monotonic
    - measure duration, or time interval
    - Java: System.nanoTime()
    - Linux: clock_gettime(CLOCK_MONOTONIC)

- Synchronized clocks Problems
  - timestamps for ordering event
    - when two machine has time differnce of ms
    - A write timestamp replicated to B, overwrite a later B update event
    - use logical clock for ordering events
      - safer, don't use real time, only relative order
  - clock read interval
    - ms digits is meaningless if precision on 100ms
    - Google Spanner TrueTime API will report [earliest, latest]

- Process Pauses
  - only leader allowed to accept writes, how to know it is still leader?
  - lease option
    - leader obtain a lease from other nodes, like a lock with timeout
    - leader must renew lease before expire
    - `if (lease.expiryTimeMillis - System.currentTimeMillis() < 10000)`
  - risk
    - time window
    - clock drift
    - thread pause 15s due to
      - GC
      - VM suspend
      - OS paging
      - Unix Ctrl-Z SIGSTOP, then SIGCONT

### 8.4 Knowledge Truth and Lies


## 9. Consistency and Consensus

### 9.1 Consistency Guarantee

- most replicated databases provide at least eventual consistency, or convergence
  - weak guarantee, don't know when will converge
  - also hard for application developers
- stronger guarantees come with worse performance or less fault-tolerant

### 9.2 Linearizability

- idea of linearizability
  - also atomic consistency, strong consistency, immediate consistency, external consistency
  - make the system appear as if only one copy of data, thus all operation are atomic

- examples
  - 3 clients concurrently read write the same key x
    - read before write is 0, after write is 1
    - read overlap write can be 0 or 1
  - add constraint
    - at some point, value of x atomically flip to 1
    - after any read 1, all subsequent reads must be 1

- why linearizability useful
  - locking and leader election
    - leader mechanic must be linearizable, all nodes agree which node owns the lock
    - coordination service like ZooKeeper
  - constraints and uniqueness guarantees
    - username, positive balance, double book seats
    - it is sometimes acceptable to overbook flight
  - cross channel time dependencies

- Implementation
  - really use single copy of data - lol
  - use single-leader replication
    - ensure read from leader or sync follower
    - may have split brain, delusional leader issue
  - consensus algorithms
    - similiar to single-leader, without split brain and stale replica
    - used in ZooKeeper
    - will explain later
  - leaderless replication
    - strict quorum seems to be linearizable
      - with delay, possible to have race condition
    - not linearizable

- Cost of Linearizability
  - cannot use multi-leader replication
  - not efficient when network issue

- CPU: RAM on modern multi-core CPU is not linearizable
  - one core write to memory address, another core may not read the same
    - unless memeory barrier/fence is used
  - every CPU core has own memeory cache and store buffer
    - so there are several copies of data

#### CAP theorem

- Consistence, Availability, Partition
- trade-off
  - if require linearizability
    - some replica disconnected from other replicas
    - they become unavailable
    - CP (not Available)
  - if not require linearizability
    - when disconnected, replica can be available
    - AP (not consistent)
  - or CA (not distributed, no replica)

### 9.3 Ordering Guarantees

#### Ordering and Causality
- examples
  - conversation, answer and question
  - happend before relationship of concurrent writes A,B
  - read skew, snapshot violate causal relationship
  - write skew, go off call
- a causal order is not a total order
  - linearization system have total order
  - causal order can be uncomparable, partial order
    - better performed linearization
    - git version history is like graph of causal dependency
- detect happend-before relationship
  - like in leaderless datastore, detect writes to same key to prevent lost updates
  - causality goes further: track depencies across entire db, than single key
    - thus need version vectors
    - previous version record, like git history approach

#### Sequence Number Ordering

- explicit tracking all data would mean a large overhead
  - better ways
    - use sequence numbers or timestamps
    - time can be from logical clock, as incremental counters
  - single-leader replication log define a total order with log counter
- when multi-leader, leaderless, partitioned
  - naive solution
    - each node generate own set of sequence number, with uniqueID to differentiate
    - attach timestamp from physical clock (may not sequential, need high resolution)
  - problem
    - number generated not consistent with causality
  - Lamport timestamps (1978)
    - each node has unique identifier
    - each node keeps counter of number of operations processed
    - Lamport timestamp (counter, nodeID)
      - compare counter, then nodeID
    - if a node receive request/response with maximum counter value greater than its own value
      - it immediately increase to that value
    - lampaort is total ordering though, it is more compact than version vectors
- order is not sufficient
  - in username problem, both can not decide uniqueless
  - total order only emerges after you collected all operation
  - they don't know causality independently

#### Total order broadcast

- single-leader replication is most powerful at ordering
  - the challenge is throughput greater than leader can handle
- total order broadcast
  - a protocol for exchanging messages between nodes
  - requires
    - reliable delivery: no message lost
    - totally ordered delivery: to every node in same order
  - consensus service like Zookeeper and etcd implement that
- usage
  - database replication: state machine replication
  - serializable transaction: every node process message in same order
  - implement lock service that provides fencing tokens
- order is fixed at the time messages delivered, can not insert
- it is like a way of creating log


- Implementation
  - in linearizable system, there is total order of operation
    - not quite same as total order broadcast, but have close links between the two
  - total order broadcast is async, no guarantee when a message will be delivered
    - linearizable is recency guarantee: read is guaranteed to see latest value written
    - with total order broadcast, you can build linearizable storage on top of it
  - imagine for every username, have a linearizable register with atomic cas operation
    - null, then set to accountID, with linearizable guarantee, username unique is guaranteed
- broadcast as append-only log
  - append message to log with your username
  - read log and wait for message deliver back
  - check messages, if first is your own message, succeed, else abort
    - when concurrent write, refer to same log can ensure linearazble write

- algorithm
  - for every message you want to send through total order broadcast
    - increment-and-get linearizable integer
    - attach value from register as sequence number to th message
    - send the message to all nodes
  - thus even message 6 arrived earlier, node will still wait for message 5
    - unlike lamport, they won't wait, only merge afterwards

### 9.4 Distributed Transactions and Consensus

- most import and fundamental problem
  - get several nodes to agree on something
  - like
    - leader election
    - atomic commit
  - to avoid split history

- FLP result
  - Fisher, Lynch and Paterson
  - no algorithm that always reach consensus if node may crash
  - proved in async system model, that don't use timeout & clocks
  - so it is actually solvable with timeout and crash detection

#### Atomic Commit and 2-Phase Commit

- Atomicity is important, for
  - Multi-object transaction
  - database with secondary index
    - separate data structure that need update consistently
- Single Node
  - transaction commitment crucially depends on the order
  - single device makes the commit atomic
- Multi-Node
  - what to do?
  - Use 2-Phase Commit


- 2PC
  - databases read and write on multiple participants
  - when ready to commit, get unique transaction ID from coordinator
  - Phase 1: prepare
    - send prepare request to each of nodes
  - Phase 2: commit
    - if all participants reply yes
      - commit point
      - pariticipants surrenders the right to abort
      - coordinator write deicision to transaction log on disk
    - otherwise coordinator (transaction manager) sends abort signal


- Coordinator fail
  - participiant always wait for coordinator after prepare->yes
  - if coordinator fails at this point, participants will be uncertain
  - can only wait for coordinator recover
  - BLOCKING

- Nonblocking 3PC
  - require a perfect failure detector
    - tell if node crashed
  - network delay is not reliable failure detector
  - so could only use 2PC


#### Fault-Tolerant Consensus

- nodes propose values, consensus algorithm decides one
- properties
  - uniform agreement: no two nodes decide differently
  - integrity: no node decide twice
  - validity: if decide v, v was proposed by some node
  - termination: if does not crash eventually, decides some value
- you can have a dictator like 2PC, but what if coordinator failed?
- consensus don't wait for any node
  - though there is a limit like quorum


- Consensus Algorithm
  - VSR: Viewstamped Replication
  - Paxos
  - Raft
  - Zab
- Most algorithm decide on a sequence of values
  - like total order broadcast algorithm
  - require messages to be delivered exactly once, in the same order, to all nodes
  - it is like multiple rounds consensus
- Implementation
  - VSR, Raft, Zab use total order broadcast directly
    - more efficient than doing repeated rounds of one-value-at-a-time consensus
  - Paxos
    - multi-paxos optimization


- Why we don't worry about consensus in the single-leader replication
  - if leader chosen manually, it is dictatorship
  - if automatic leader election, closer to total order broadcast
  - but in order to elect a leader, we need consensus, which is a broadcast like single-leader replication, which requires a leader
  - WTF?

- Epoch numbering and quorums
  - all consensus protocols internally use a leader in some form
  - don't guarantee leader is unique
  - instead, make weak guarantee: protocol defines an epoch number
    - Paxos: ballot number
    - VSR: view number
    - Raft: term number
  - guarantee within each epoch, leader is unique
  - each election gives increased epoch number
    - before leader decide anything
      - first check there isn't some other leader with higher epoch, make a conflicting decision
    - how to know itself is leader?
      - Truth defined by Majority
      - collect votes from a quorum of nodes
    - for every decision a leader wants to make
      - send proposed value to other nodes
      - wait for a quorum of nodes respond
      - if node is not aware of leader with higher epoch, vote yes

- 2 Round Voting
  - first vote for leader
  - second vote for leader's proposals
  - quorum for two votes must overlap

- looks similar to 2PC
  - but coordinator for 2PC is not elected
  - coordinator require yes from every participants, not majority

- Limitation of Consensus
  - performance: vote on proposals is a sync replication
  - strict-majority: need a minimum of 2n+1 nodes to tolerate n failure
  - rely on time out to detect failure: varied delay will cause frequent leader election, harm performance
  - edge case: if one particular network link is unreliable, Raft leadership will bounce between two nodes forever


#### Membership and Coordination Services

- ZooKeeper
  - hold small data that fit in memoery
  - replicated across all nodes using total order broadcast
  - useful features
    - linearizable atomic operations
    - total order of oepration
    - failure detection
    - change notification
