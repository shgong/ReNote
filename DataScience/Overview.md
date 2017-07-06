# Overview

## 1.Core 

#### 1.1 Frameworks

* [Apache Hadoop](http://hadoop.apache.org/) - framework for distributed processing. Integrates MapReduce (parallel processing), YARN (job scheduling) and HDFS (distributed file system).

#### 1.2 Distributed Programming

* [Amazon Lambda](http://aws.amazon.com/lambda/) - a compute service that runs your code in response to events and automatically manages the compute resources for you.
* [Apache Crunch](http://crunch.apache.org/) - a simple Java API for tasks like joining and data aggregation that are tedious to implement on plain MapReduce.
* [Apache Flink](http://flink.incubator.apache.org/) - high-performance runtime, and automatic program optimization.
* [Apache MapReduce](http://wiki.apache.org/hadoop/MapReduce/) - programming model for processing large data sets with a parallel, distributed algorithm on a cluster.
* [Apache Pig](https://pig.apache.org/) - high level language to express data analysis programs for Hadoop.
* [Apache S4](http://incubator.apache.org/s4/) - framework for stream processing, implementation of S4.
* [Apache Spark](http://spark.incubator.apache.org/) - framework for in-memory cluster computing.
* [Apache Spark Streaming](http://spark.incubator.apache.org/docs/0.7.3/streaming-programming-guide.html) - framework for stream processing, part of Spark.
* [Apache Storm](http://storm-project.net/) - framework for stream processing by Twitter also on YARN.
* [Blaze](http://blaze.pydata.org/en/latest/) - Python users high-level access to efficient computation on inconveniently large data.
* [Cascalog](http://cascalog.org/) - data processing and querying library.
* [Concurrent Cascading](http://www.cascading.org/) - framework for data management/analytics on Hadoop.
* [DistributedR](http://www.vertica.com/distributedr/) - scalable high-performance platform for the R language.
* [Google Dataflow](http://googledevelopers.blogspot.it/2014/06/cloud-platform-at-google-io-new-big.html) - create data pipelines to help themæingest, transform and analyze data.
* [Google MapReduce](http://research.google.com/archive/mapreduce.html) - map reduce framework.
* [HParser](http://www.informatica.com/us/products/big-data/hparser/) - data parsing transformation environment optimized for Hadoop.
* [IBM Streams](http://www.ibm.com/software/products/en/infosphere-streams) - advanced analytic platform that allows user-developed applications to quickly ingest, analyze and correlate information as it arrives from thousands of real-time sources.
* [Nextflow](http://www.nextflow.io) - Dataflow oriented toolkit for parallel and distributed computational pipelines.
* [Oryx](http://oryx.io/) - is a realization of the lambda architecture built on Apache Spark and Apache Kafka, but with specialization for real-time large scale machine learning.
* [Pubnub](http://www.pubnub.com/) - Data stream network.
* [Pydoop](http://pydoop.sourceforge.net/docs/) - Python MapReduce and HDFS API for Hadoop.
* [spark-dataflow](https://github.com/cloudera/spark-dataflow) - allows users to execute dataflow pipelines with Spark.

#### 1.3 Integrated Development Environments

* [R-Studio](https://github.com/rstudio/rstudio) - IDE for R.

## 2.Databases

#### 2.1 Distributed Filesystem

* [Amazon Elastic File System](https://aws.amazon.com/efs/) - file storage service for Amazon Elastic Compute Cloud (Amazon EC2) instances.
* [Amazon Simple Storage Service](http://aws.amazon.com/s3/) - secure, durable, highly-scalable object storage.
* [Apache HDFS](http://hadoop.apache.org/) - a way to store large files across multiple machines. 

#### 2.2 Key-Map (Wide Column) Data Model

* [**Apache Cassandra**](http://cassandra.apache.org/) - column-oriented distribuited datastore, inspired by BigTable.
* [Apache HBase](http://hbase.apache.org/) - column-oriented distribuited datastore, inspired by BigTable.
* [Facebook HydraBase](https://code.facebook.com/posts/321111638043166/hydrabase-the-evolution-of-hbase-facebook/) - evolution of HBase made by Facebook.
* [Google BigTable](http://static.googleusercontent.com/external_content/untrusted_dlcp/research.google.com/en//archive/bigtable-osdi06.pdf) - column-oriented distributed datastore.

#### 2.3 Document Data Model

* [Amazon SimpleDB](http://aws.amazon.com/simpledb/) - a highly available and flexible non-relational data store that offloads the work of database administration.
* [Microsoft DocumentDB](http://azure.microsoft.com/en-us/services/documentdb/) - fully-managed, highly-scalable, NoSQL document database service.
* [MongoDB](http://www.mongodb.org/) - Document-oriented database system.
* [RethinkDB](http://www.rethinkdb.com/) - document database that supports queries like table joins and group by.

#### 2.4 Key-value Data Model

* [Amazon DynamoDB](http://aws.amazon.com/dynamodb/) - distributed key/value store, implementation of Dynamo paper.
* [Oracle NoSQL Database](http://www.oracle.com/technetwork/database/database-technologies/nosqldb/overview/index.html) - distributed key-value database by Oracle Corporation.
* [Redis](http://redis.io) - in memory key value datastore.
* [Redis Cluster](http://redis.io/topics/cluster-spec) - distributed implementation of Redis.

#### 2.5 Graph Data Model

* [Apache Giraph](http://giraph.apache.org/) - implementation of Pregel, based on Hadoop.
* [Apache Spark Bagel](http://spark.incubator.apache.org/docs/0.7.3/bagel-programming-guide.html) - implementation of Pregel, part of Spark.
* [GraphX](https://amplab.cs.berkeley.edu/publication/graphx-grades/) - resilient Distributed Graph System on Spark.
* [Neo4j](http://www.neo4j.org/) - graph database writting entirely in Java.
* [Titan](http://thinkaurelius.github.io/titan/) - distributed graph database, built over Cassandra.

#### 2.6 SQL-like

* [Actian SQL for Hadoop](http://www.actian.com/products/analytics-platform/) - high performance interactive SQL access to all Hadoop data.
* [AMPLAB Shark](https://github.com/amplab/shark/) - data warehouse system for Spark.
* [Apache HCatalog](http://hive.apache.org/docs/hcat_r0.5.0/) - table and storage management layer for Hadoop.
* [Apache Hive](http://hive.apache.org/) - SQL-like data warehouse system for Hadoop.
* [Cloudera Impala](http://www.cloudera.com/content/cloudera/en/products-and-services/cdh/impala.html) - framework for interactive analysis, Inspired by Dremel.
* [SparkSQL](http://databricks.com/blog/2014/03/26/Spark-SQL-manipulating-structured-data-using-Spark.html) - Manipulating Structured Data Using Spark.


#### 2.7 Embedded Databases

* [Actian PSQL](http://www.actian.com/products/operational-databases/) - ACID-compliant DBMS developed by Pervasive Software, optimized for embedding in applications.
* [Google Firebase](https://www.firebase.com/) - a powerful API to store and sync data in realtime.
* [LMDB](http://symas.com/mdb/) - ultra-fast, ultra-compact key-value embedded data store developed by Symas.

#### 2.8 Data Warehouse

* [Google Mesa](http://static.googleusercontent.com/media/research.google.com/en/us/pubs/archive/42851.pdf) - highly scalable analytic data warehousing system.
* [IBM dashDB](https://cloudant.com/dashdb/) - Data Warehousing and Analysis Needs, all in the Cloud.
* [Microsoft Azure SQL Data Warehouse](http://techcrunch.com/2015/04/29/microsoft-introduces-azure-sql-data-warehouse/) - businesses access to an elastic petabyte-scale, data warehouse-as-a-service offering that can scale according to their needs.


## 3.Services


#### 3.1 Data Ingestion

* [Apache Chukwa](http://incubator.apache.org/chukwa/) - data collection system.
* [Apache Flume](http://flume.apache.org/) - service to manage large amount of log data.
* [Apache Samza](http://samza.incubator.apache.org/) - stream processing framework, based on Kafla and YARN.
* [Apache Sqoop](http://sqoop.apache.org/) - tool to transfer data between Hadoop and a structured datastore.
* [Cloudera Morphlines](https://github.com/cloudera/cdk/tree/master/cdk-morphlines) - framework that help ETL to Solr, HBase and HDFS.
* [Fluentd](http://fluentd.org/) - tool to collect events and logs.
* [Logstash](http://logstash.net) - a tool for managing events and logs.

#### 3.2 Message-oriented middleware

* [Amazon Simple Queue Service](http://aws.amazon.com/sqs/) - fast, reliable, scalable, fully managed queue service.
* [RabbitMQ](http://www.rabbitmq.com/) - Robust messaging for applications.
* [RQ](http://python-rq.org/) - simple Python library for queueing jobs and processing them in the background with workers.

#### 3.3 Benchmarking

* [Apache Hadoop Benchmarking](https://issues.apache.org/jira/browse/MAPREDUCE-3561) - micro-benchmarks for testing Hadoop performances.


#### 3.4 System Deployment

* [Apache YARN](http://hortonworks.com/hadoop/yarn/) - Cluster manager.
* [Cloudera Director](http://www.cloudera.com/content/cloudera/en/products-and-services/director.html) - a comprehensive data management platform with the flexibility and power to evolve with your business.
* [Cloudera HUE](http://gethue.com/) - web application for interacting with Hadoop.
* [Jumbune](http://www.jumbune.org/) - Jumbune is an open-source product built for analyzing Hadoop cluster and MapReduce jobs..
* [Scaling Data](http://www.scalingdata.com/big-data) - tracing data center problems to root cause, predict capacity issues, identify emerging failures and highlight latent threats.

* [Apache Avro](http://avro.apache.org/) - data serialization system.

* [Apache Zookeeper](http://zookeeper.apache.org/) - centralized service for process management.


#### 3.5 Container Manager

* [Amazon EC2 Container Service](https://aws.amazon.com/ecs/) - a highly scalable, high performance container management service that supports Docker containers.
* [Docker](https://www.docker.com/) - an open platform for developers and sysadmins to build, ship, and run distributed applications.
* [Fig](http://www.fig.sh/) - fast, isolated development environments using Docker.




## 4.Analysis

#### 4.1 Machine Learning

* [Amazon Machine Learning](https://aws.amazon.com/machine-learning/) - visualization tools and wizards that guide you through the process of creating machine learning (ML) models without having to learn complex ML algorithms and technology.
* [Apache Mahout](http://mahout.apache.org/) - machine learning library for Hadoop.
* [brain](https://github.com/harthur/brain) - Neural networks in JavaScript.
* [Concurrent Pattern](http://www.cascading.org/pattern/) - machine learning library for Cascading.
* [H2O](http://0xdata.github.io/h2o/) - statistical, machine learning and math runtime for Hadoop.
* [IBM Watson](http://www.ibm.com/smarterplanet/us/en/ibmwatson/) - cognitive computing system.
* [KeystoneML](https://github.com/amplab/keystone) - Simplifying robust end-to-end machine learning on Apache Spark.
* [PredictionIO](http://prediction.io/) - machine learning server buit on Hadoop, Mahout and Cascading.
* [scikit-learn](https://github.com/scikit-learn/scikit-learn) - scikit-learn: machine learning in Python.
* [Spark MLlib](http://spark.apache.org/docs/0.9.0/mllib-guide.html) - a Spark implementation of some common machine learning (ML) functionality.
* [Theano](http://deeplearning.net/software/theano/) - Python package for deep learning that can utilize NVIDIA's CUDA toolkit to run on the GPU.
* [Viv](http://viv.ai/) - global platform that enables developers to plug into and create an intelligent, conversational interface to anything.
* [Wolfram Alpha](http://www.wolframalpha.com/) - computational knowledge engine.


#### 4.2 Business Intelligence

* [Pentaho](http://www.pentaho.com/) - business intelligence platform.
* [Qlik](http://www.qlik.com/) - business intelligence and analytics platform.
* [Tableau](https://www.tableausoftware.com/) - business intelligence platform.


#### 4.3 Data Analysis

* [Apache Zeppelin](http://zeppelin.incubator.apache.org/) - a web-based notebook that enables interactive data analytics.
* [Datameer](http://www.datameer.com/product/index.html) - data analytics application for Hadoop combines self-service data integration, analytics and visualization.
* [Microsoft Cortana Analytics](http://www.microsoft.com/en-us/server-cloud/cortana-analytics-suite/overview.aspx) - a fully managed big data and advanced analytics  suite that enables you to transform your data into  intelligent action..
* [Periscope](https://www.periscope.io/) - plugs directly into your databases and lets you run, save, and share analyses over billions of data rows in seconds.


#### 4.4 Data Visualization

* [Chart.js](http://www.chartjs.org/) - open source HTML5 Charts visualizations.
* [Cubism](https://github.com/square/cubism) - JavaScript library for time series visualization.
* [Cytoscape](http://cytoscape.github.io/) - JavaScript library for visualizing complex networks.
* [D3](http://d3js.org/) - javaScript library for manipulating documents.
* [Google Charts](https://developers.google.com/chart/) - simple charting API.
* [Graphite](http://graphite.wikidot.com/) - scalable Realtime Graphing.
* [IPython](http://ipython.org/) - provides a rich architecture for interactive computing.
* [Matplotlib](https://github.com/matplotlib/matplotlib) - plotting with Python.
* [NVD3](http://nvd3.org/) - chart components for d3.js.
* [Plot.ly](http://plot.ly) - Easy-to-use web service that allows for rapid creation of complex charts, from heatmaps to histograms. Upload data to create and style charts with Plotly's online spreadsheet. Fork others' plots..
* [Sigma.js](https://github.com/jacomyal/sigma.js) - JavaScript library dedicated to graph drawing.


Reference
1. [Data Science Ecosystem Table](#/DataScience/Reference/EcosystemTable.md)
2. [Database Ranking](http://db-engines.com/en/ranking)
3. [Wikipedia - BigData](https://en.wikipedia.org/wiki/Big_data)
