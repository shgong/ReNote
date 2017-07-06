
# Sqoop

MySQL to HDFS
```
sqoop import 
--connect jdbc:mysql://quickstart:3306/retail_db 
--table=categories 
--username=retail_dba --password=cloudera 
--target-dir /user/cloudera/import
--fields-terminated-by="\t"
--lines-terminated-by="\n"
-m 1
```

MySQL to Hive
```
sqoop import
--connect jdbc:mysql://quickstart:3306/retail_db
--table=categories
--username=cloudera --password=cloudera
--hive-import
--hive-overwrite 
--create-hive-table 
-m 1
```

Export from hive
```
sqoop export
--connect jdbc:mysql://quickstart:3306/retail_db
--table=categories
--username=cloudera --password=cloudera
--export-dir /user/hive/warehouse/tablename
--input-fields-terminated-by '\001'
-m 1
```

# Hive
Create
```sql
# create
CREATE EXTERNAL TABLE FamilyHead
(name string, business_places ARRAY<string>, sex_age STRUCT<sex:string,age:int>, fatherName_NuOfChild MAP<string,int>)
ROW FORMAT DELIMITED
FIELDS TERMINATED BY '|'
 
# load data into
LOAD DATA LOCAL INPATH '/home/training/Consumer_Complaints.csv' 
OVERWRITE INTO TABLE <table name>

# create from select
CREATE TABLE target AS
SELECT col1, col2 FROM source;

# output to local
INSERT OVERWRITE LOCAL DIRECTORY '/tmp/pv_gender_sum'
SELECT col1, col2 FROM source;

# multi
FROM records2
INSERT OVERWRITE TABLE stations_by_year
SELECT year, COUNT(DISTINCT station) GROUP BY year
INSERT OVERWRITE TABLE records_by_year
SELECT year, COUNT(1) WHERE temperature != 9999 AND (quality = 0 OR quality = 1 OR quality = 4 OR quality = 5 OR quality = 9) GROUP BY year;

```

Dynamic Partition
```sql
-- static
LOAD DATA LOCAL INPATH '${env:HOME}/staticinput.txt'
INTO TABLE partitioned_user
PARTITION (country = 'US', state = 'CA');

-- non strict dynamic partition
create table table3 (type STRING, category STRING, attackon STRING) PARTITIONED BY (region STRING) ROW FORMAT  DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;

Insert overwrite table table3 PARTITION (region)
select type,category,region,attackon FROM table2 WHERE category="Attack";

-- mixed key dynamic partition
create table table3 (type STRING,  attackon STRING) PARTITIONED BY (category STRING, region STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE;

Insert overwrite table table3 PARTITION (category="Attack", region)
select type,attackon,region FROM table2;
```


```
set hive.exec.dynamic.partition.mode=nonstrict;
insert into table orders_partition1 partition (order_month)
select order_id, order_date, order_customer_id, order_status, order_month from orders1;
```


```sql
    CREATE TABLE ccaspark_USER_COUNTRY(
    firstname VARCHAR(64),
    lastname VARCHAR(64),
    address STRING,
    City VARCHAR(64),
    pincode STRING,
    home VARCHAR(64),
    office STRING,
    email STRING,
    website STRING
    )
    PARTITIONED BY (country VARCHAR(64), state VARCHAR(64))
    ROW FORMAT DELIMITED
    FIELDS TERMINATED BY ','
    Stored as textfile;

    LOAD DATA INPATH '/user/cloudera/problem82/data.txt'
    INTO TABLE ccaspark_USER_COUNTRY
    PARTITION (country = 'US', state = 'CA');
```


```
    CREATE TABLE ccaspark_USER_COUNTRY_SEQ(
    firstname VARCHAR(G4),
    lastname VARCHAR(64),
    address STRING,
    city VARCHAR(64),
    pincode STRING,
    home VARCHAR(64);
    office STRING,
    email STRING,
    website STRING
    )
    PARTITIONED BY (country VARCHAR(64), state VARCHAR(64))
    STORED AS SEQUENCEFILE;


    set hive.exec.dynamic.partition.mode=nonstrict;
    INSERT INTO TABLE ccaspark_USER_COUNTRY_SEQ  PARTITION (country, state)
    SELECT firstname,lastname,address,city,pincode,home,office,email,website,country,state FROM ccaspark_USER_COUNTRY;
```

# Avro
```
java -jar /usr/lib/avro/avro-tools.jar

java -jar /usr/lib/avro/avro-tools.jar getschema hdfs://quickstart.cloudera:8020/user/cloudera/module_avro/EMPLOYEE/part-m-00000.avro > Employee.avsc

CREATE TABLE orders_partition1 (
order_id int,
order_date bigint,
order_customer id int,
order_status string
)
PARTITIONED BY (order_month string)
STORED AS AVRO
LOCATION 'hdfs:///user/hive/warehouse/retail_stage1.db/orders_partition'
TBLPROPERTIES ('avro.schema.literal'='{
"type": "record",
"name" : "orders partition1",
"doc" : "Hive partitioned table schema ",
"fields" : [ {
"name": "order_id",
"type" : [ "int", "null" ]
},{
"name" : "order_date',
"type" : [ "long", "null" ]
},{
"name" : "order_customer_id",
"type" : [ "int", "null" ]
},{
"name" : "order_status",
"type" : [ "string", "null" ]
}],
"tableName" : "orders_partition1"
}');


CREATE TABLE orders_partition4 (
order_id int,
order_date bigint,
order_customer id int,
order_status string,
order_value int,
order_description string
)
STORED AS AVRO
LOCATION 'hdfs:///user/hive/warehouse/retail_stage.db/orders'
TBLPROPERTIES ('avro.schema.url'='hdfs://quickstart.cloudera/user/cloudera/retail_stage1/sqoop_import_orders2.avsc');
```
