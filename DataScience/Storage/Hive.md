


# Hadoop Hive

Hive Timeline: started at FB, collected by nightly cron job into Oracle DB
ETL via hand-coded python, grew from 10GB to 1TB/day

### 1. Why Hive
- Data warehousing package built on top of hadoop
- used for data analysis
- targeted towards users comfortable with sql
- similar to sql, called hiveQL
- for managing and querying structured data 
- abstracts complexity of hadoop
- no need learn java and hadoop apis

For short:
Defines SQL-Like Query, 
Data Warehousing Infrastructure, 
Allow plug-in customized mappers and reducers, 
Many tools to easy data ETL

### 2. Usage

For data mining, log processing, business intelligence...
Not For transaction, interactive, real-time queries (high latency)

### 3. Hive Architecture
Commnand Line Interface
Web Interface
Thrift Server (jdbc, odbc)
Metastore
Libraries
HiveQL
Driver (Compiler, Optimizer, Executer) - connect to HADOOP



##### Metastore
Central repo for hive meta data

Three configurations
- Embedded metastore: Derby database backed by local disk
- Local metastore: A standalone database, mostly MySQL
- Remote metastore: Any Server

Table schemas are stored here


##### HiveQL

hive's SQL dialect, no full feature provided

| Feature | SQL | HiveQL |
|-----|-----|-----|
|Updates|Insert Update Delete| Insert Overwrite Table|
|Indexes|Supported|Not Supported|
|Functions|Hundreds of built in functions|Dozens of built in functions|
|Views|Updatable| Read-only|
|Multitable Insert| Not Supported| Supported|

##### Data Types and Table Types

Primitive Data Types

- Signed Integer - Tinyint, Smallint, Int, Bigint
- Floating Point - Float, Double
- Boolean
- String
- VARCHAR 1 to 65355
- CHAR    255


Complex Data Types

- Array
- Map
- Struct

Hive Table Types

- Managed Tables
    When a managed table is dropped then the table including its data and metadata is deleted
- External Tables
    When an external table is dropped hive will leave the data untouched and delete only the metadata.


```
hive> Create table managed_table(dummy String);
Load data inpath '/user/txt' into table managed_table;

hive>Create EXTERNAL table ext_table(dummyString) location '/user/tom/ext_table
hive>load data inpath '/user/text' into table ext_table;
```


```sql
CREATE TABLE <table name>
    (<column name> <data type>, ...)
    ROW FORMAT DELIMITED FIELDS TERMINATED BY '<character>';
Terminated By ' <character>';

ALTER TABLE <table name> ADD COLUMN (<column name> <data type>);

DROP TABLE <table name>;

DESCRIBE <table name>

SHOW TABLES

LOAD DATA INPATH <file path> INTO TABLE <table name>

SELECT * from <table name>
```

subquery
```sql
# Hive supports subqueries only in the FROM clause.
# The columns in the subquery select list are available in the outer query just like columns of a table
SELECT col FROM (
SELECT col1+col2 AS col
FROM table1 ) table2
```


### 4. Hive Data Model
Tables, Partitions, Buckets



##### 4.1 Partitions

- hive organize tables into Partitions
- a way of dividing table into coarse grained parts based on value of partition column
- helps to do query faster on slices of data
- a table may be partitioned in multiple dimensions
- partitiones are defined at table creation using keyword PARTITIONED BY cllause, which takes a list of column d

Partitioning data is often used for distributing load horizontally, this has performance benefit, and helps in organizing data in a logical fashion. Example like if we are dealing with large employee table and often run queries with WHERE clauses that restrict the results to a particular country or department . For a faster query response Hive table can be PARTITIONED BY (country STRING, DEPT STRING), Partitioning tables changes how Hive structures the data storage and Hive will now create subdirectories reflecting the partitioning structure like . .../employees/country=ABC/DEPT=XYZ. If query limits for employee from country ABC t will only scan the contents of one directory ABC. This can dramatically improve query performance, but only if the partitioning scheme reflects common filtering. Partitioning feature is very useful in Hive, however, a design that creates too many partitions may optimize some queries, but be detrimental for other important queries. Other drawback is having too many partitions is the large number of Hadoop files and directories that are created unnecessarily and overhead to NameNode since it must keep all metadata for the file system in memory.


##### 4.2 Buckets

- Bucketing
    + to populate bucketed table, set `hive.enforce.bucketing` property to `true`
    + each bucket is a file, arranged in alphabet order
    + a job will produce as many buckets(output file) as reduce tasks
    + Hive does bucketing by hashing value and reducing modulo the number of buckets

- example
    + suppose a table 
        * top-level partition: date
        * second-level partition: employee_id 
        * problem: too many small partitions. 
    + if we bucket the employee table
        * the value of this column will be hashed by a user-defined number into buckets. 
        * Records with the same employee_id will always be stored in the same bucket. 
        * `CLUSTERED BY (employee_id) INTO XX BUCKETS` 
    + Advantage
        * impose extra structure on table
        * The number of buckets is fixed so it does not fluctuate with data. 
        * If two tables are bucketed by employee_id, Hive can create a logically correct sampling for testing
        * Make certain queries more efficient: like two table join by a bucketing column, can do map-side join more easily

Bucketing also aids in doing efficient map-side joins etc.


##### 4.3 Example
| |Name|HDFS Directory
|--|--|--
|Table|pvs|/wh/pvs|
|Partition| ds=2009,ctry=US | /wh/pvs/ds=2009/ctry=US
|Bucket| into 32 buckets file for hash 0 | /wh/pvs/ds=2009/ctry=US/part-00000

```
CREATE TABLE logs(ts BINGINT, line STRING)
    PARTITIONED BY (dt STRING, country STRING);

LOAD DATALOCAL INPATH 'input/hive/partitions/file1' INTO TABLE logs
    PARTITION (dt='2012-01-01',country='GB');
```

### HiveQL

##### Join

- Hive supports only equality joins, outer joins, and left semi joins.
- Hive does not support join conditions that are not equality conditions as it is very difficult to express such conditions as a map/reduce job.
- More than two tables can be joined in Hive

```
SELECT table1.*, table2.*
>FROM table1 JOIN table2 ON (table1.col1 = table2.col1) ;
```

##### Multi Table Insert

```sql
FROM records2
INSERT OVERWRITE TABLE stations_by_year
SELECT year, COUNT(DISTINCT station) GROUP BY year
INSERT OVERWRITE TABLE records_by_year
SELECT year, COUNT(1) GROUP BY year
INSERT OVERWRITE TABLE good_records_by_year
SELECT year, COUNT(1) WHERE temperature != 9999 AND (quality = 0 OR quality = 1 OR quality = 4 OR quality = 5 OR quality = 9) GROUP BY year;
```

##### Table with Select
```
Create Table as Select:
CREATE TABLE target
AS
SELECT col1,col2
FROM source;
```

##### Importing Data to Local file

```
INSERT OVERWRITE LOCAL DIRECTORY '/tmp/pv_gender_sum'
SELECT pv_gender_sum.*
FROM pv_gender_sum;
```


##### Sort by VS Order by

standard `ORDER BY` clause produce a result totally sorted, but also set reducer to one, making it very inefficient

Most case a globally sorted result is not required: Use nonstandard `SORT BY`instead, produce a sorted file per reducer.

When want to control which reducer a particular row goes to, use `DISTRIBUTED BY` clause, eg.
```
FROM records2 
SELECT year,temperature
DISTRIBUTED BY year
SORT BY year ASC, temperature DESC
```

- ORDER BY x: guarantees global ordering, but does this by pushing all data through just one reducer. This is basically unacceptable for large datasets. You end up one sorted file as output.
- SORT BY x: orders data at each of N reducers, but each reducer can receive overlapping ranges of data. You end up with N or more sorted files with overlapping ranges.
- DISTRIBUTE BY x: ensures each of N reducers gets non-overlapping ranges of x, but doesn't sort the output of each reducer. You end up with N or unsorted files with non-overlapping ranges.
- CLUSTER BY x: ensures each of N reducers gets non-overlapping ranges, then sorts by those ranges at the reducers. This gives you global ordering, and is the same as doing (DISTRIBUTE BY x and SORT BY x). You end up with N or more sorted files with non-overlapping ranges.



### User Defined Function(UDF)
- three types
    + UDF: Operate single row and create single row output
    + UD Aggregate F: Operate multiple row and create single row
    + UD Table Generating F: Operate single row and produce multiple rows
- properties
    + UDF must be subclass of `org.apache.hadoop.hive.ql.exec.UDF`
    + UDF must implement at least one evaluate() method
- Usage
    add JAR `/path/to/hive-examples.jar;`
    Create temporary function strip as 'com.hive.Strip'; 

### Collection Item

Support 3 kind of collection item: array, struct and map 

`Name|Location1,Location2...Location5|Sex,Age|FatherName:Number of Child`

```sql
CREATE TABLE FamilyHead
(
name string,
business_places ARRAY<string>,
sex_age STRUCT<sex:string,age:int>,
fatherName_NuOfChild MAP<string,int>
)
ROW FORMAT DELIMITED
FIELDS TERMINATED BY '|'
COLLECTION ITEMS TERMINATED BY ','
MAP KEYS TERMINATED BY ':';
```


### DDL
```sql
CREATE DATABASE IF NOT EXISTS Family
COMMENT 'This database will be used for collecting various family data and their daily habits'
LOCATION '/hdfs/family'
WITH DBPROPERTIES ('Database creator'='Vedic','Database_Created_On'='2016-01-01');
```




## Case Impala
By default Impala does not know, its metadata has been updated. Hence first make Impala aware about metadata change and update the same.

    show tables;
    invalidate metadata;
