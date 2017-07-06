
# Sqoop

## 1 What's Sqoop

Enables __Directly Connection__ betweeen Hadoop and database
( Replacing earlier solution: InputFormat + DBInputFormat + DBWritable )
Including: Teradata, Mysql, Oracle, Green plum, Netteza, Micro Strategy

Procedure: Gather Metadata → Submit Map-only Job → Sqoop Job save to HDFS


## 2 Why Sqoop

### 2.1 Features of Sqoop

Characteristics

- Support all RDBMS
- Manage Parallel extraction of data
- Automatic boilerplate code gneeration
- Support for Hive/HBase import

### 2.2 Performance Benchmarks in Clusters of Sqoop



## 3  Importing and Exporting

### 3.1 Importing from RDBMS

##### MySQL example
```bash
sqoop import \
--connect jdbc:mysql://<jdbc-uri>
--table <mysql_tablename>
--username <username_mysql> --password <pw>
--target-dir <HDFS Path>
--m <number_of_mappers_to_run>
```

Change to `export-dir`, `sqoop export` when Exporting

##### Optional Arguments:
```sh
--connection-manager <classname> # Specify Manager Class
--driver <classname> # Specify JDBC driver class
--hadoop-home <dir> # Override $HADOOP_HOME
--query 'SELECT * FROM Table WHERE a="query"'

# where clause
--where \`category_id\`=22 
--where \`category_id\`>22
--where "\`category_id\` between 1 and 22"
--columns=category_name,category_id 

--fields-terminated-by="|"
--null-string='N' 
```


##### Primary Key

|Primary key|Mapper Numbers| Processing |
|---|---|---|
|Specifed|Multiple|  mostly happen in mapper |
|Not |One|  mostly happen in reducer|


##### Import Path & Formats

```
# Warehouse Directory
--warehouse-dir <directory to store all tables>

# Path for Each Table
--target-dir <directory instead of given table name>

# Specify Delimiters
--fields-terminated-by '\t'
--lines-terminated-by '\n'

# Append
--append \
```

##### Export formats
```
sqoop export --connect jdbe:mysql://quickstart:3306/retail_db \
--username retail_dba \
--password cloudera \
--table departments \
--export-dir new_data \
--batch \
--m 1\
--update-key department_id \
--update-mode allowinsert
--update-mode updateonly
```

### 3.2 Hive


```
sqoop import
--connect <jdbc-dir>
--username <un> --password <pw>
--table <table>
--hive-import
-m 1
```

Import

```
sqoop import-all-tables \
-m 3 \
--connect jdbc:mysql://quickstart:3306/retail_db \
--username=retail_dba \
--password=cloudera \
--hive-import \
--hive-overwrite \
--create-hive-table \   # when non-existing
--compress \
--compression-codec org.apache.hadoop.io.compress.SnappyCodec \
--outdir java_output

 --compression-codec=snappy 
 --as-parquetfile 
```

Incremental Append
            
    --append \
    --check-column "department_id" \
    --incremental append \
    --last-value 7

When you create table without delimiter fields

    --fields-terminated-by '\001'  // default field is AA \001

### 3.3 HBase

```
sqoop import
--connect <jdbc-dir>
--username <un> --password <pw>
--table <table>
--hbase-table <target_table>
--column-family <column_family_name>
--hbase-row-key <row_key_column>
--hbase-create-table
```



### 3.4 others

List Table
```
sqoop list-tables --connect jdbc:mysql://quickstart:3306/retail_db --username retail_dba --password cloudera
```

 Eval command, just run a count query on one of the table.
```
sqoop eval \
--connect jdbc:mysql://quickstart:3306/retail_db \
--username retail_dba \
--password cloudera \
--query "select count(1) from order_items"
```

import all table as avro
```
sqoop import-all-tables \
--connect jdbc:mysql://quickstart:3306/retail_db \
--username=retail_dba \
--password=cloudera \
--as-avrodatafile \
--warehouse-dir=/user/hive/warehouse/retail_stage.db \
-m 1
```



split averagely to each mapper
```
    sqoop import \
    --connect jdbc:mysql://quickstart:3306/retail_db \
    --username=retail_dba \
    --password=cloudera \
    --query="select * from orders join order_items on orders.order_id = order_items.order_item_order_id where \$CONDITIONS" \
    --target-dir /user/cloudera/order_join \
    --split-by order_id \
    --num-mappers 2
```


Make sure intermediate data folder do not exist
```
Please check following directory must not exist else it will give error,

    hdfs dfs -ls /user/cloudera/departments

If directory already exists, make sure it is not useful and than delete the same.

This is the staging directory where Sqoop store the intermediate data before pushing in hive table

    hadoop fs -rm -R departments
```

Output format
```
    --enclosed-by \' 
    --escaped-by \\ 
    --fields-terminated-by='~' 
    --lines-terminated-by :
```

escape by: when field contain escape characters like: 

'9999'~'\'Data Science\'':
```
\b (backspace)
\n (newline)
\r (carriage return)
\t (tab)
\" (double-quote)
\' (single-quote)
\\ (backslash)
```


Null Fields
```
#import
    --null-string "" \
    --null-non-string -999 \
#export
    -input-null-string "" \
    -input-null-non-string -999
```

## Sqoop Job
```
Below is the command to create Sqoop Job (Please note that --import space is mandatory)

    sqoop job -create sqoop_job \
    -- import \
    --connect "jdbc:mysql://quickstart:3306/retail_db" \
    --username=retail_dba \
    -password=cloudera \
    -table categories \
    -target-dir categories_target_job \
    -fields-terminated-by '|' \
    -lines-terminated-by '\n'

# List all the Sqoop Jobs

    sqoop job --list
    sqoop job --show sqoop_job

# Execute the sqoopjob

    sqoop job --exec sqoop_job

# Check the output of import job

    hdfs dfs -ls categories_target_job
```



## More questions

- Sqoop need JDBC driver to connect to different DB vendor, and also JDBC connector
- Subset of rows from table
    + where clause
    + filtering query on database to temp table
- PW
    + password-file option can be used inside script
    + -p read from input, prevent automation
- default compress: gz
- avoid import table one by one: import-all-tables
- updating tables: --incremental with append/lastmodified
- slice data to multiple tasks: split-by
- all-or-nothing load: staging-table option, to final table only if successful
- update rows: --update-key
- export subset columns: --column
