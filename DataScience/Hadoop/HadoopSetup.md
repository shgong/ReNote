# Hadoop Setup

## 1 Hadoop

### 1.1 Prerequisite

- Java Installed: `java -version`
- Remote login Enabled: `ssh localhost`
- Find {java_home}: `/usr/libexec/java_home`
- MySQL installed

### 1.2 Install Hadoop

Download a Hadoop Distribution

Extract it at {distribution directory}

Edit `etc/hadoop/hadoop-env.sh`
```sh
export JAVA_HOME={your java home directory}
export HADOOP_PREFIX={hadoop distribution directory}
```

Add to `$HOME/.bash_profile`
```sh
export HADOOP_PREFIX={hadoop distribution directory} # /usr/local/hadoop
export PATH=$PATH:$HADOOP_PREFIX/bin
export PATH=$PATH:$HADOOP_PREFIX/sbin
```

Restart Terminal

### 1.3 Configuration

3.1 etc/hadoop/core-site.xml:
```xml
<configuration>
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://localhost:9000</value>
    </property>
</configuration>
```

3.2 etc/hadoop/hdfs-site.xml:
```xml
<configuration>
    <property>
        <name>dfs.replication</name>
        <value>1</value>
    </property>
</configuration>
```

3.3 etc/hadoop/mapred-site.xml:
```xml
<configuration>
    <property>
        <name>mapreduce.framework.name</name>
        <value>yarn</value>
    </property>
</configuration>
```

3.4 etc/hadoop/yarn-site.xml:
```xml
<configuration>
    <property>
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
    </property>
</configuration>
```


### 1.4 Running

```sh
hdfs namenode -format # format filesystem
start-dfs.sh # start namenode
# browse the web interface for the NameNode at - http://localhost:50070/

hdfs dfs -mkdir /user
hdfs dfs -mkdir /user/{username} #make sure you add correct username here

stop-yarn.sh
stop-dfs.sh
```



## 2. Hive

Download from [Hive Release] (https://hive.apache.org/downloads.html)

```
# unpack the tarball
tar -xzvf hive-x.y.z.tar.gz

# if installed hadoop with homebrew, move it to Cellar
mv apache-hive–x.y.z-bin /usr/local/Cellar/hive–x.y.z
cd /usr/local/Cellar/hive–x.y.z

# add HIVE_Home to PATHs
export HIVE_HOME=/usr/local/Cellar/hive–x.y.z

# set up HIVE folders in HDFS
hadoop fs -mkdir       /tmp
hadoop fs -mkdir       /user/hive/warehouse
hadoop fs -chmod g+w   /tmp
hadoop fs -chmod g+w   /user/hive/warehouse
```

Download the mysql connector
```
$ curl -L 'http://www.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.22.tar.gz/from/http://mysql.he.net/' | tar xz

$ sudo cp mysql-connector-java-5.1.15/mysql-connector-java-5.1.22-bin.jar /usr/local/Cellar/hive/hive.version.no/libexec/lib/
```


Create Mysql metastore
```mysql
CREATE DATABASE metastore;
USE metastore;
CREATE USER 'hiveuser'@'localhost' IDENTIFIED BY 'password';
GRANT SELECT,INSERT,UPDATE,DELETE,ALTER,CREATE ON metastore.* TO 'hiveuser'@'localhost';
```


Create hive-site.xml
```
$ cd /HIVE_HOME/conf
$ cp hive-default.xml.template hive-site.xml
```

Add SQL
```
<property>
  <name>javax.jdo.option.ConnectionURL</name>
  <value>jdbc:mysql://localhost/metastore</value>
</property>
<property>
  <name>javax.jdo.option.ConnectionDriverName</name>
  <value>com.mysql.jdbc.Driver</value>
</property>
<property>
  <name>javax.jdo.option.ConnectionUserName</name>
  <value>hiveuser</value>
</property>
<property>
  <name>javax.jdo.option.ConnectionPassword</name>
  <value>password</value>
</property>
<property>
  <name>datanucleus.fixedDatastore</name>
  <value>false</value>
</property>
```

If not initialized
```
rm -Rf $HIVE_HOME/metastore_db
$HIVE_HOME/bin/schematool -initSchema -dbType derby
```

Run Hive
$HIVE_HOME/bin/hive







