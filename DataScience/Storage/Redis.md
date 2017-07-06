# Redis

[View Redis Applications](#/DataScience/Database/RedisApplication.md)

## 1. General Comparison

Relational Database: data normalization
remove data redundancy and minimize data dependency

### The SQL way

- `id-name-subject1-subject2`
    + Have to check two columns when search a subject
    + Have to add column when more than 2
- `id-name` & `sid-subject` & `id-sid`
    + Allows to manipulate the data easily
    + Allows insertions and deletions without worrying about artifacts
    + Need a lot of join (expensive) to search

```sql
SELECT a.Name 
from STUDENTS a 
JOIN STU_SUB_MAP b ON a.student_id = b.student_id 
JOIN SUBJECTS c ON c.Subject_id = b.Subject_id 
where c.subject_name='Math'
```

### The NoSQL Way

- high performance
- no concept of normalization
- sacrifice data consistency/reliability
- Need a lot of work on data modeling

Redis stored as key/value pair, no easy normalization steps like SQL,
- Key: string
- Value: string, list, hash, set ...

```
# create set
SADD student:JOHN  Math Biology
SADD student:SMITH Math Physics
SADD student:SCOTT Biology

SADD subject:Math JOHN SMITH
SADD subject:Biology JOHN SCOTT
SADD subject:Physics SMITH

SMEMEBERS SMITH
# output: Math, Physics

SINTER Math Biology
# output: JOHN
# need update two tables when update or delete
```


## 2. Transactions and Locks

### SQL Transactions

Transactions are used to maintain atomicity, consistency, isolation, and durability (ACID)

eg Banking
- If Fails, should roll back to initial and credit back.
- Achieved using COMMIT on success and ROLLBACK on failure

Would become slow in distributed database
> In order to maintain consistency across multiple databases, statements are prepared and executed in all databases and only after getting a success message from all servers is the transaction completed. As the lock is maintained until all the servers commit, the application is slowed down in the process.

```sql
BEGIN TRANSACTION;
DELETE FROM tbl_students WHERE user_id='3';
DELETE FROM tbl_student_subjects WHERE user_id='3';
COMMIT;
```

### Data consistency in Redis

if design in the way of RDB

```
HMSET Stud:1 Name John ID 1 
HMSET Stud:2 Name Smith ID 2

HMSET Sub:1 Name Math Room_No 201
HMSET Sub:2 Name Physics Room_No 2106
HMSET Sub:3 Name Biology Room_No 5105

SADD Map_Stud_1 Sub_1 Sub_3
SADD Map_Stud_2 Sub_1 Sub_2
```

Problem 1:
Either loop through all set to do subject search,
Or create another set with subject as keys.

Problem 2:
When `DEL Stud_:1` the reference in Map set is not delete

```
HMSET Stud:1 Name John ID 1 Subject "Sub:1,Sub:3"
HMSET Stud:2 Name Smith ID 2 Subject "Sub:1,Sub:2"
```

can refractor to this, but update process is more difficult


### Transactions in Redis
Use commands like MULTI, EXEC, DISCARD, and WATCH

Delete Transaction
```
MULTI
DEL Stud:1
DEL Map_Stud_1
EXEC
```

No roll back in Redis, as it only fail due to programming error

The `WATCH` command: provide a check-and-set (CAS) behavior, make sure the transaction is executed only when other clients modify none of the watched keys. 

For example, let's say we want to remove the top player from a real-time leaderboard. We have to make sure the top player does not change while we are calculating the leader. If the leaderboard gets updated before we remove the item from the sorted set, the EXEC command is never executed.

```
WATCH leaderboard element = ZREVRANGEBYSCORE leaderboard +inf -inf WITHSCORES
MULTI
ZREM leaderboard element
EXEC
```

## 3. Data Types in Redis

Link: [Official commands documents](http://redis.io/commands)

##### Strings
- most basic data type, binary safe, up to 512 MB
- can hold anything such as a binary stream of images, data in JSON format, generated HTML cached for faster delivery

##### Lists
- string in the order of insertion. 
- Implemented using a basic linked list, lookup operation is O(N)
- Pro: adding an element in the head or tail of the list takes the same time. 
- Con: accessing middle element is too long. Better work only with borders
- Ideal for modeling queues or stacks for event logging

##### Sets
- Collection of unique unsorted string. No duplication. 
- Efficiently add, delete, or verify the existence at O(1)
- support for peek and pop  (using the SRANDMEMBER and SPOP commands). 
- Also good for random lookups and to maintain indexes.
- Store up to 2^32-1 elements.
- Support complex operations under the fold such as UNION and INTERSECTION between different sets.

##### Sorted sets
- Improved sorted sets based on associative scores, from low to high
- an additional operation to the query based on score ranges. 
- most advanced and complex data type in Redis
- can add, delete, or update elements in a sorted set quickly.  O(log(N))
- example of building a priority queue:

```
ZADD p_queue 100 "task1"
ZADD p_queue 400 "task2"
ZADD p_queue 20 "task3"
ZRANGE p_queue 0 -1     # "task3"-"task1"-"task2"
zrevrange p_queue 0 -1  # reversed: 2-1-3
```

Due to the ordered behavior and optimization, sorted sets are best suited to maintain leaderboards, timestamp data ranges, or to implement auto-completion with Redis. They make good priority queues and can be used to implement weighted random selections.

##### Hashes
- Represent map between string fields and string values
- Good to represent linked data structures using references.
- Complex data structures: a hash with a reference to lists and sets.
- Ideal to store some data related to an object without encoding (eg. JSON XML) , objects with fields and values, and linked data structures such as sets through reference.

```
HMSET id:1765 name "Hari Seldon" profession "Mathematician" 
HSET id:1765 profession "PyschoHistorian"
HGETALL id:1765
```

HSET can add a value to a single hash field,
HMSET can set multiple hash fields with values in the same command.

##### HyperLogLog
- probabilistic data structure used to count unique items. 
- For example, we want to know the number of unique search terms searched in our site or the number of unique products that are viewed by the visitors; we need to store the terms or IDs in a list and add to the list after checking whether the item already exists. 
- To achieve this, the amount of memory required is proportional to the number of items in the list we are counting. HyperLogLog is an approximation algorithm that sacrifices the precision for memory. 
- In the case of Redis implementation, the standard error is less than 1 percent and the memory used is far less.

PFADD is the command used to add the items,
PFCOUNT is used to retrieve the approximate count of unique items
```
(integer) 1
PFADD terms hello hi howdy
PFCOUNT terms
(integer) 3
```


##### Expiration
- set expiry information in seconds or milliseconds (SETEX in old version)
- use PERSIST to cancel the timeout
```
SET mykey myvalue EX 5
GET mykey
```

