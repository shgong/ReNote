
Apache Hadoop is an open source, scalable and reliable solution framework that stores and allows distributed processing of large data sets.

### Hadoop vs Traditional Systems

|   From | RDBMS - EDW - MassivePP | - NoSQL to Hadoop   |
|------------|----------------------|----------------------|
|DataTypes   |Structured              |Multi & Unstructured |
|Processing  |Limited                 |Coupled with Data
|Schema      |Schema on Write       |Schema on Read
|Speed       |Read Fast               |Write Fast
|Cost        |Software License        |Supports
|Resources   |Known Entity            |Growing, Complexity, Wide
|Best Fit Use|Interactive OLAP Analysis | Data Discovery
|            |Complex ACID Transactions | Processing Unstructured Data
|            |Operational Data Store    | Massive Storage/Processing


RDBMS use traditional B-tree, good for updating small records, but not majority of database. Better use MapReduce to sort/merge/rebuild.

Structured data that conform to a particular predefined schema is the realm of RDBMS. Unstructured data like text or image, good for MapReduce to interpret data at processing time.

MapReduce is linearly scalable. RDB mostly not.

However the difference start to blur. RDB incorporate idea from MR, MR build high level query language like Hive and Pig.



