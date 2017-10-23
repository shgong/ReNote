


# Chapter I. Foundations of Data Systems


> Computing is pop culture. […] Pop culture holds a disdain for history. Pop culture is all
about identity and feeling like you’re participating. It has nothing to do with cooperation,
the past or the future—it’s living in the present. I think the same is true of most people who
write code for money. They have no idea where [their culture came from].
—__Alan Kay__ in interview with __Dr Dobb’s Journal__ (2012)



## 1. Reliable, Scalable and Maintainable Applications

- data-intensive applications
  - database
  - cache
  - search indexes
  - stream processing
  - batch processing
- compose new, special-purpose data system from components
  - primary database
  - application-managed caching layer (memcache)
  - full-text search server (elastic or solr)
  - message queue
- concerns?

### 1.1 Reliability

Reliability or Fault Tolerant

- Hardware Faults
  - like
    - hard disk crash
    - RAM faulty
    - power grid blakout
  - average hard disk failure 10-50 years
    - in 10000 disk data center
    - should expect a failure per day
- Software Errors
  - may crash every instance given a wrong input (like leap second)
  - processs used up shared resource (CPU, memory, disk, network bandwidth)
  - cascade failure
- Human Errors
  - configuration error by operators
  - non-production sandbox
  - test thoroughly at all levels, unit/integration/manual
  - detailed and clear monitoring
  - quick and easy recovery


### 1.2 Scalability

#### Describing Load

Use twitter for example, when someone post a tweet

- insert into global collection

  ```sql
  SELECT tweets.*, users.* FROM tweets
  JOIN users ON tweets.sender_id = users.id
  JOIN follows ON follows.followee_id = users.id
  WHERE follows.follower_id = current_user
  ```
- or, maintain cache for each user's timeline
  - look up all followers and insert into their timeline caches

- twitter first used approach 1, then switched to approach 2
  - easy to show home timeline
  - posting tweet now requires a lot of extra work
    - someone with 30m followers, one tweet lead to 30m writes
    - doing this in 5s is a big challenge
- finally use a hybrid move
  - most users use approach 2
  - tweets from celebrities are fetched separated and merged when read

#### Describing Performance

- In batch processing system like Hadoop, we care about `throughput`
  - the number of records per second
- In online systems, we care about `response time`
  - latency may vary
  - background process, loss of network packet & TCP retransmission, GC pause, page fault
  - use response time percentiles or median

#### Coping with Load

- Scaling
  - Scale up / Vertical: moving to a more powerful machine
  - Scale out / Horizontal: distribute among more smaller machine
  - Elastic: automatically add computing resource
- there is no `magic scaling sauce`
  - 100k 1kB request per second
  - 3 2GB request per min
  - same throughput with very different architecture

### 1.3 Maintainability

- Three design principle
  - Operability
    - make routine task easy, with monitors, bug-tracking, good practice and tools
  - Simplicity
    - use abstraction to removing accidental complexity
      - high-level programming languages hide machine code
      - SQL hide on-disk and in-memory data structures
    - finding good abstraction is hard
  - Evolvability
    - Agile working patterns


## 2. Data Models and Query Languages

> The limits of my language mean the limits of my world.
> — Ludwig Wittgenstein, Tractatus Logico-Philosophicus (1922)
> 我的语言的界限意味着我的世界的界限 - 维特根斯坦 《逻辑哲学论》

### 2.1 Relational Model vs Document Model

- Birth of NoSQL
  - need for greater scalability
  - free and open source over commercial databases
  - frustration with restrictions of relational schema

- Object-Relational Mismatch
  - common criticism for SQL: an awkward translation layer is required
    - ORM frameworks like ActiveRecord and Hibernate
    - a linkedin profile may need six tables
      - user table
      - regions table
      - industry table
      - positions table
      - education table
      - contact_info table
    - or one json
      ```json
      {
        "user_id": 251,
        "first_name": "Bill",
        "last_name": "Gates",
        "summary": "Co-chair of the Bill & Melinda Gates... Active blogger.",
        "region_id": "us:91",
        "industry_id": 131,
        "photo_url": "/p/7/000/253/05b/308dd6e.jpg",
        "positions": [
          {"job_title": "Co-chair", "organization": "Bill & Melinda Gates Foundation"},
          {"job_title": "Co-founder, Chairman", "organization": "Microsoft"}
        ],
        "education": [
          {"school_name": "Harvard University", "start": 1973, "end": 1975},
          {"school_name": "Lakeside School, Seattle", "start": null, "end": null}
        ],
        "contact_info": {
          "blog": "http://thegatesnotes.com",
          "twitter": "http://twitter.com/BillGates"
        }
        }
      ```
    - though there are also problems with JSON as a data encoding format

- Many-to-One and Many-to-Many
  - why region_id and industry_id use id?
    - drop down input to avoid ambiguity and spelling error
    - better localization support and search
  - such normalization is many-to-one, don't fit nicely into document model
  - reference are handled specially, e.g. organizations, schools and other users

- Document Model
  - lead to simpler application code
  - limitation
    - cannot refer directly to a nested item within document
      - second item in list of positions for user 251
    - poor support for join
    - not good for many-to-many relationship
  - better schema flexibility
    - schema-on-read is like dynamic type checking in PL
  - storage locality
    - have performance advantage, not split among tables
    - exceptions
      - Google Spanner offer same locality in a relational model
      - allow schema to declare that a table's rows should be nested within a parent table
      - Oracle allows multi-table index cluster table
      - with column family concept in Bigtable data model (Cassandra/HBase)

- Converge of document and relational DB
  - many RDB support XML around mid-2000s
  - PostgreSQL 9.3, MySQL 5.7, IBM DB2 10.5 support JSON web API
  - RethinkDB support relational-like joins
  - some MongoDB Drivers automatically resolve database references

### 2.2 Query Languages

#### SQL
Imperative
```js
function getSharks() {
  var sharks = []
  for (var i=0; i<animals.length; i++){
    if(animals[i].family == "Sharks") sharks.push(animals[i])
  }
  return sharks
}
```

Declarative
```sql
/* sharks = animal.filter(family="Sharks") */
SELECT * FROM animals WHERE family = "Sharks"
```

#### Declarative Queries on the web

Declarative
```css
li.selected > p {
  background-color: blue;
}
```

Imperative
```js
var liElements = document.getElementsByTagName("li");
for (var i = 0; i < liElements.length; i++) {
  if (liElements[i].className === "selected") {
    var children = liElements[i].childNodes;
    for (var j = 0; j < children.length; j++) {
      var child = children[j];
      if (child.nodeType === Node.ELEMENT_NODE && child.tagName === "P") {
        child.setAttribute("style", "background-color: blue");
      }
    }
  }
}
```

#### MapReduce Query

- something in between
  - logic of query is expressed with snippets of code

```sql
SELECT date_trunc('month', observation_timestamp) AS observation_month, sum(num_animals) AS total_animals
FROM observations
WHERE family = 'Sharks' GROUP BY observation_month;
```

MongoDB MapReduce feature
```js
db.observations.mapReduce(
  function map() {
    var year = this.observationTimestamp.getFullYear();
    var month = this.observationTimestamp.getMonth() + 1;
    emit(year + "-" + month, this.numAnimals);
  },
  function reduce(key, values) {
    return Array.sum(values);
  },
  {
    query: { family: "Sharks" }, // starting filter, declarative
    out: "monthlySharkReport"    // which collection to write final output
  }
);
```

MongoDB 2.2 aggregation pipeline
more like SQL now with different style
```js
db.observations.aggregate([
        { $match: { family: "Sharks" } },
        { $group: {
            _id: {
                year:  { $year:  "$observationTimestamp" },
                month: { $month: "$observationTimestamp" }
            },
            totalAnimals: { $sum: "$numAnimals" }
        }}
]);
```


### 2.3 Graph-Like Model

- structures
  - document model: one-to-many or no relationship
  - relational model: simple many-to-many
  - graph model: complex many-to-many
- graph algorithm
  - car navigation
  - page rank

#### property graph using relational schema
```sql
CREATE TABLE vertices (
  vertex_id  integer PRIMARY KEY,
  properties json
);

CREATE TABLE edges (
  edge_id     integer PRIMARY KEY,
  tail_vertex integer REFERENCES vertices (vertex_id),
  head_vertex integer REFERENCES vertices (vertex_id),
  label       text,
  properties  json
);

CREATE INDEX edges_tails ON edges (tail_vertex);
CREATE INDEX edges_heads ON edges (head_vertex);
```

#### Cypher Query Language

- declarative query language by Neo4j

```cypher
CREATE
  (NAmerica:Location {name:'North America', type:'continent'}),
  (USA:Location      {name:'United States', type:'country'  }),
  (Idaho:Location    {name:'Idaho',         type:'state'    }),
  (Lucy:Person       {name:'Lucy' }),
  (Idaho) -[:WITHIN]->  (USA)  -[:WITHIN]-> (NAmerica),
  (Lucy)  -[:BORN_IN]-> (Idaho)

MATCH
  (person) -[:BORN_IN]->  () -[:WITHIN*0..]-> (us:Location {name:'United States'}),
  (person) -[:LIVES_IN]-> () -[:WITHIN*0..]-> (eu:Location {name:'Europe'})
RETURN person.name
```

#### SPARQL query language

- for triple store using RDF data model
```sparql
PREFIX : <urn:example:>
SELECT ?personName WHERE {
  ?person :name ?personName.
  ?person :bornIn  / :within* / :name "United States".
  ?person :livesIn / :within* / :name "Europe".
}

(person) -[:BORN_IN]-> () -[:WITHIN*0..]-> (location)   # Cypher
?person :bornIn / :within* ?location.                   # SPARQL
(usa {name:'United States'})   # Cypher
?usa :name "United States".    # SPARQL
```

## 3. Storage and Retrieval

### 3.1 Data Structures

#### World's simplest database

```sh
#!/bin/bash
db_set () {
  echo "$1,$2" >> database
}
db_get () {
  grep "^$1," database | sed -e "s/^$1,//" | tail -n 1
}
```

- bash key-value store with comma separated text file
  - efficient db_set
  - terrible db_get: need index
- trade off
  - well-chosen index speed up read
  - every index slow down write
- most db let user choose index, using knowledge of typical query patterns


#### Hash Indexes

- key-value or dictionary data type, usually implemented as hash map
  - Used by Bitcask
- simplistic hashmap: in-memory hashmap to byte offset
  - require: all keys fit in RAM

- Data file Segment
  - break log into segments, close when it read a certain limit
  - compaction
    - throw away duplicate, keep most recent
    - can run several compaction together
  - every query
    - search most recent segment's index

- Real implementation
  - format: not CSV, binary with length encoder and raw string is more efficient (no escaping)
  - deletion: append a deletion record (called tombstone)
  - crash: read file to rebuild hashmap, or storing snapshot
  - partially written records: checksum to detect corruption
  - concurrency: only one writer

- Pro
  - append-only is fastest at write
  - immutable support concurrency and crash recovery
  - compaction to merge segments  
- Con
  - Hash table must fit in memory
  - No Scans / Range queries


#### SSTables and LSM-Trees

- What if Sort-by-key
  - seems break our ability to use sequential writes？
  - only within blocks

- SSTable, or Sorted String Table
  - merge is simple and efficient
    - read files side by side, merge sort
  - don't need detailed index
    - only some index, then scan/binary-search is enough
    - can group records into block, compress before writing to disk
- How to get data sorted
  - maintain a sorted structure
    - on disk: B Tree
    - in memory: AVL Tree or Red Black Tree
- example
  - Write: add to in-memory balanced tree, memtable
  - Exceeds size limit: write to disk as SSTable file
  - Read: memtable, then most recent segment file
  - From time to time, run merge / compaction in the background
  - Recovery: keep recent write log, remove when become SSTable


- Usage
  - SSTable and Memtable, Introduced in Google's Bigtable paper
  - Used in LevelDB, rocksDB, Cassandra and HBase
  - original algorithm by Patrick O'Neil, LSM-tree, log-structured merge-tree
  - used by Lucene (Elastic Search and Solr) to store term dictionary


- Performance Optimization
  - slow when look up key does not exist
    - use bloom filter
      - a memory-efficient data structure to approximating contents of a set
  - different compaction
    - levelDB and RocksDB use leveled compaction
      - key range split up into smaller SSTables
      - old data moved into separate levels
    - HBase use size-tiered compaction
      - new and smaller SSTables are successively merged into older and larger one
    - Cassandra support both

#### B Trees

- most wildly used indexing structure
  - SSTable keep large block and write sequentially
  - B Tree keep 4KB block/page and read/write one page at a time
    - each page has address, like pointer in disk (instead of memeory)
    - look up key from root, each child for a certain range of keys
    - growing by spliting a page
    - with branching factor

- basic operation
  - overwrite a page on disk with new data
  - need move hard disk head
  - split operation require rewriting multiple pages
    - dangerous, what if crash in the process
- WAL, write-ahead log
  - append-only log before any B-Tree modification

- Optimization
  - LMDB use copy-on-write scheme instead of WAL log
    - Snapshot isolation, better for concurrency control
  - abbreviate keys to save space
  - try lay out the tree in sequential order on disk as possible
  - add jumping pointer to trees (like sibling)


#### B-Tree vs LSM-Tree

- B-Tree
  - read fast
  - each key exist exact one place, good for transactions
  - write data twice: WAL & tree page
  - leave disk space unused due to fragmentation
- LSM-Tree
  - write fast
  - compressed better
  - compaction process can be expensive


- R-Tree
  - above trees have a single key
    - cannot query multiple columns of a table simultaneously
  - you can translate into single number, then use regular B-Tree
  - more commonly, specialized spatial index R-Tree is used
  - example: latitude & longtitude, postGIS

### 3.2 Transaction processing or analytics

- Transaction
  - Transaction don't necessarily need ACID
  - Just allow clients to make low-latency read/write

- OLTP: Online Transaction Processing
  - read: small records by key
  - write: random-access, low-latency
  - usage: customer, web-app
  - data: latest state, GB
- OLAP: Online analytics Processing
  - read: aggregate large number of records
  - write: ETL, bulk import
  - usage: analytics
  - data: history, TB-PB

- Data Warehousing
  - Different sources
    - Sales DB from E-Commerce site (customer)
    - Inventory DB from Stock app (warehouse worker)
    - Geo DB from vehicle route planner (driver)
  - ETL into warehouse
    - Extract
    - Transform
    - Load
  - Vendors
    - Teradata
    - Vertica
    - SAP HANA
    - ParAccel: Amazon Redshift
    - Hadoop: Hive, Spark, Impala, Presto (Google Dremel)

### 3.3 Column-Oriented Storage

- A typical data warehouse query only access 4-5 of 1000 columns
- Column-Oriented storage will be efficient
  - e.g. Parquet, based on Google Dremel

- Column Compression
  - e.g. 69, 69, 69, 69, 74, 31, 31, 31, 69
  - bitmap for possible values
    - 31: 0 0 0 0 0 1 1 1 0
    - 69: 1 1 1 1 0 0 0 0 1
    - 74: 0 0 0 0 1 0 0 0 0
  - zero-one encoding
    - 31: 5,3,1
    - 69: 0,4,4,1
    - 74: 4,1
  - query: when value in (31,74)
    - use bitmap can fetch column super fast with bitset merge

- Doesn't really matter column orders
  - however, sort rows is tricky if stored in column
  - usually sorted by most frequent query
  - that column is also hugely compressed
- Vertica C-Store
  - Different query benefit from different sort-order
  - store same data sorted in different ways


## 4. Encoding and Evolution

- data model upgrade compatibility
  - backward compatible
  - forward compatible

### 4.1 Formats for Encoding Data



### 4.2 Modes of Dataflow
