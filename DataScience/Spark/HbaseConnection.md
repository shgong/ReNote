# Reading Hive-HBase tables through spark-shell, spark-submit, pyspark, or spark-sql
IOP's Spark Thrift Server starts automatically upon installation. However, it is not configured to work with HBase tables. You need to add the necessary HBase jars and configurations. You can use the Spark Thrift Server through the JDBC connection. For example, the beeline client is one way to use the JDBC connection. Follow these steps:

These shells honor the configuration property spark.driver.extraClassPath and spark.executor.extraClassPath in SPARK_CONF_DIR/spark-defaults.conf. Follow these steps:
Add the HBase jar files into these properties in the Ambari UI. The value of these two properties is:

```
/usr/iop/current/hbase-client/lib/hbase-annotations.jar
     :/usr/iop/current/hbase-client/lib/hbase-spark.jar
     :/usr/iop/current/hbase-client/lib/hbase-common.jar
     :/usr/iop/current/hbase-client/lib/hbase-client.jar
     :/usr/iop/current/hbase-client/lib/hbase-server.jar
     :/usr/iop/current/hbase-client/lib/hbase-protocol.jar
     :/usr/iop/current/hbase-client/lib/guava-12.0.1.jar
     :/usr/iop/current/hbase-client/lib/htrace-core-3.1.0-incubating.jar
     :/usr/iop/current/hbase-client/lib/zookeeper.jar
     :/usr/iop/current/hbase-client/lib/protobuf-java-2.5.0.jar
     :/usr/iop/current/hbase-client/lib/hbase-hadoop2-compat.jar
     :/usr/iop/current/hbase-client/lib/hbase-hadoop-compat.jar
     :/usr/iop/current/hbase-client/lib/metrics-core-2.2.0.jar
     :/usr/iop/current/hive-client/lib/hive-hbase-handler.jar
     :/usr/iop/current/hbase-client/conf
```


Save the configuration and restart Spark2 Service. Then, when you run the shells or spark-submit, for example, enter the following code:
```
$ spark-shell --master yarn --deploy-mode client
scala> spark.sql("select * from hbasedb.t1").show
```