


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





## 3. Storage and Retrieval

### 3.1 Data Structures

### 3.2 Transaction processing or analytics

### 3.3 Column-Oriented Storage


## 4. Encoding and Evolution

### 4.1 Formats for Encoding Data

### 4.2 Modes of Dataflow
