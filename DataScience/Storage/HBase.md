# HBase

## 1. Summary

HBase is not a column-oriented database in the typical RDBMS sense, but utilizes an on-disk column storage format.

- columnar databases excel at providing real-time analytical access to data
- HBase excels at providing key-based access to a specific cell of data, or a sequential range of cells


Feature

- Column-Oriented, Horizontally scalable
- random real-time CRUD operation
- automatic fail-over
- distributed
- for large data, 100s of millions rows

Use Case
- lots data
- large ammount of clients/requests
- great for single random selects / range scans
- great for variable shcema (rows drastically differ) 
- when most columns are null

## 2. Structure

- SortedMap<RowKey, List>
    + List: SortedMap<Column, List>
         * List: Value, Timestamp
- Cell may exist in multiple version

Data Model
- row
    + referenced by rowkey
    + row sorted lexicogrpahically by key
    + not number order: 1 10 15 2 3
- column
    + grouped in column families
    + example `user:first_name`
    + families stored together, should be static
- timestamp
    + multiple version kept for each cell
    + read latest first
    + can set time out, aging

HBae Region
- a range of keys
- start->stop: [ K3cod, odiekd )
- when data comes
    + at first only 1 region
    + split when exceed maximum by 256MB, at the middle key
    + common setting
    + 10-1000 region, 1-2GB per region

Storage
- Hfiles/StoreFiles, default block size is 64KB
- basically a key-value map
- when added, write in commit log called WAL (WriteAheadLog)
    + solely for recovery purpose, no retrieval
- when in-memory data exceeds maximum value - flush
    + remove from WAL when persist to HFile
- immutable
    + cannot delete value
    + use a delete marker
- compaction
    + flush create more and more HFiles
    + compaction merge together
    + minor compaction: rewrite smaller files to larger
    + major compaction: rewrite all files within column family to new one
        * scan all k/v, can drop deleted entries

## 3. Client API
```java
// PUT
Put(byte[] row)
Put(byte[] row, RowLock rowLock)
Put(byte[] row, long ts)
Put(byte[] row, long ts, RowLock rowLock)

// toBytes Helper class
static byte[] toBytes(boolean b)
static byte[] toBytes(long val)
static byte[] toBytes(float f)
static byte[] toBytes(int val)

// Add data
boolean has(byte[] family, byte[] qualifier)
boolean has(byte[] family, byte[] qualifier, long ts)
boolean has(byte[] family, byte[] qualifier, byte[] value)
boolean has(byte[] family, byte[] qualifier, long ts, byte[] value)

```

Example
```java
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

public class PutExample {

  public static void main(String[] args) throws IOException {
    Configuration conf = HBaseConfiguration.create(); 

    HTable table = new HTable(conf, "testtable"); 

    Put put = new Put(Bytes.toBytes("row1")); 

    put.add(Bytes.toBytes("colfam1"), Bytes.toBytes("qual1"),
      Bytes.toBytes("val1")); 
    put.add(Bytes.toBytes("colfam1"), Bytes.toBytes("qual2"),
      Bytes.toBytes("val2")); 

    table.put(put); 
  }
}
```


Load from HBase to Spark
```
newAPIHadoopRDD
```

## 4. Topics

- rowkey
    + `mobile number, date, transaction-id` as rowkey
    + short and wide: small column number
    + tall and thin: large column number
    + should we try to minimize the row name and column name sizes 
- command
    + get, put, delete, scan, increment
    + do not support join
- connect
    + zookeeper: config, distirbution, sync, communication
- diable table
    + can modify setting
    + can not accessed through scan
    + `is_disabled "name"`
    + `disable_all "p.*"`
- Filters
    + Column Value Filter
    + Column Value Comparator
    + KeyValue Metadata filter
    + Rowkey filter
- Disadvantage
    + authentication
    + index limited to one column
    + HMaster Node: single point of failure
- two ways of access data
    + rowkey, table scan for a range of value
    + mapreduce batch process



Commandline shell
```sh
list
scan 'testtable'
create 'test','cf1'
put 'test','row1','cf1','val1'
put 'test','row1','cf1','val2'
scan 'test'  //only latest
scan 'test', {Versions>=3}
```

```sh
#alter version
alter 'tablename', {NAME => 'ColFamily', VERSIONS => 4}

#delete column family 
alter 'tablename', {NAME => 'colFamily', METHOD => 'delete'}

# add new column family
Hbase > disable ‘tablename’
Hbase > alter ‘tablename’ {NAME => ‘oldcolfamily’,NAME=>’newcolfamily’}
Habse > enable ‘tablename’

# scan records
scan 'tablename', {LIMIT=>10,STARTROW=>"start_row",STOPROW=>"stop_row"}

# run major compaction
major_compact 'tablename'

```

### 5. CAP & ACID

- CAP theorem: basic requirement
    + impossible for a distributed computer system to simultaneously provide all three of the following guarantees:
    + Consistency: all nodes see the same data at the same time
    + Availability: every request receives a response about whether it succeeded or failed
    + Partition tolerance: the system continues to operate despite arbitrary partitioning due to network failures
- Examples
    + CA: not distributed
        * SQL Server
        * MySQL
        * Postgres
    + AP: keep answering but possibly bad data, 
        * Cassandra
        * Amazon Dynamo
        * CouchDB
    + CP: stop responding when not available
        * HBase
        * Redis
        * MongoDB
- HBase vs Cassandra
    + HBase: 
        * range based row scans 
        * Atomic Compare and Set
    + Cassandra:
        * single row read performance
        * friendly SQL like language
        * No HMaster single point of failure, scalable
- ACID: rule choose to follow
    + a set of properties that apply to data transactions
    + Atomicity: success or none (staging in sqoop)
    + Consistency: commit if pass all rules
    + Isolation: tranaction do not affect each other
    + Durability: safe against error crash once commit
    + may have performance issue if obey all rules

