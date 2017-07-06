

# HDFS

Hadoop Distributed Filesystem, designed for storing very large files (terabytes, petabytes) with streaming data access patterns, running on clusters of commodity hardware. Built around idea that most efficient data processing is write-once, read-many-times.

HDFS block is 64MB, very large compared to normal disk block ~512B
It reduce the time to search start of the block

HDFS Cluster have namenode and many datanodes. The namenode manages tree and metadata, with two files: namespace image and edit log.

Secondary namenode periodically merge the namespace image with edit log to prevent log from being too large. Also keep a copy in case namenode failing.

### the command line interface

config (single datanode case)
```
fs.default.name="hdfs://localhost/"
dfs.replication=1 // do not replicate by default three
```


file utility testing
```
writeFile(p+"/test/_header","a|b|c")
writeFile(p+"/test/model_id","1|2|3")
deleteIfExists(p+"/test.txt")
merge(p+"/test",p+"/test.txt")
```



Terminal
```bash

hdfs fs
hdfs fs -help get

hadoop fs -copyFromLocal input/quangle.txt  /user/tom/quangle.txt
hadoop fs -copyFromLocal input/quangle.txt  quangle.txt  # default /user/tom

hadoop fs -mkdir books
hadoop fs -ls
hadoop fs -ls file:///
```



```sh
# create a directory in hdfs
hadoop dfs -mkdir input

#copy a local ﬁle in hdfs
hadoop dfs -copyFromLocal /tmp/example.txt input

#delete a directory in hdfs
hadoop dfs -rmr input
rm -rf <path> #removes in local file system.

# Merge files return to local
hdfs dfs -getmerge -nl Employee MergedEmployee.txt

# Change Permission
hdfs dfs -chmod 664 Employee/MergedEmployee.txt

# Forced
hadoop fs -copyFromLocal -f



```

execute MR application
```sh
hadoop jar <path-jar> <jar-MainClass> <jar-parameters>
hadoop dfs -mkdir input
hadoop dfs -mkdir output
hadoop dfs -copyFromLocal /tmp/parole.txt input
hadoop jar /tmp/word.jar WordCount input/parole.txt output/result

```



##### jps
Check all running daemons in Hadoop using the command jps
```
$:~ jps

30773 JobTracker
30861 TaskTracker
30707 SecondaryNameNode
30613 DataNode
30524 NameNode
30887 Jps
```

the output shows the local VM identifier (lvmid) and a short form of the class name or JAR file that the VM was started from on the local machine.



### Sequence File
A SequenceFile contains a binary encoding of an arbitrary number key-value pairs. Each key must be the same type. Each value must be same type.




### File Write Example
1. Client creates the file with create()
2. DistributedFileSystem RPC call Namenode to create a new file in the filesystem’s namespace, with no blocks associated with it. Namenode check authority and existance, then makes a record or throw an IOException.
3. DistributedFileSystem returns a FSDataOutputStream for client
4. Client writes data, DFSOutputStream splits it into packets, write to data queue. DataStreamer ask the namenode to allocate new blocks by picking a list of suitable datanodes to store the replicas. The list of datanodes forms a pipeline (replicas)
5. DataStreamer streams the packets to 1st datanode in the pipeline. 1st stores and forwards to 2nd .. until the last
6. DFSOutputStream also maintains an acknowledge queue with packets waiting to be acknowledged by datanodes. A packet is removed when acknowledged by all datanodes in the pipeline
7. client calls close() on the stream, this action flushes all the remaining packets to the datanode pipeline and waits for acknowledgments, before signal namenode that file is complete. the ack is for each block, can read written block even whole file is not finished.

## chmod
3 numbers: user, group, others
read write execute: 111, 7
no write: 101, 5
no execute: 110, 6

## additional
```sh
# See how much space this directory occupies in HDFS.
hadoop fs -du -s -h hadoop/retail

# Copy a directory from one node in the cluster to another
hadoop fs -distcp hdfs://namenodeA/apache_hadoop hdfs://namenodeB/hadoop

# Use ‘-setrep’ command to change replication factor of a file
hadoop fs -setrep -w 2 apache_hadoop/sample.txt

# Move a directory from one location to other
hadoop fs -mv hadoop apache_hadoop

# Default file permissions are 666 in HDFS
# Use ‘-chmod’ command to change permissions of a file
hadoop fs -ls hadoop/purchases.txt
sudo -u hdfs hadoop fs -chmod 600 hadoop/purchases.txt

# Default names of owner and group are training,training
# Use ‘-chown’ to change owner name and group name simultaneously
hadoop fs -ls hadoop/purchases.txt
sudo -u hdfs hadoop fs -chown root:root hadoop/purchases.txt

# Default name of group is training
# Use ‘-chgrp’ command to change group name
hadoop fs -ls hadoop/purchases.txt
sudo -u hdfs hadoop fs -chgrp training hadoop/purchases.txt
```
