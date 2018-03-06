# https://stackoverflow.com/questions/37132559/add-jars-to-a-spark-job-spark-submit

# The ambiguous and/or omitted details

Following ambiguity, unclear, and/or omitted details should be clarified for each option:

- How ClassPath is affected
  - Driver
  - Executor (for tasks running)
  - Both
  - not at all
- Separation character: comma, colon, semicolon
- If provided files are automatically distributed
  - for the tasks (to each executor)
  - for the remote Driver (if ran in cluster mode)
- type of URI accepted: local file, hdfs, http, etc
- If copied into a common location, where that location is (hdfs, local?)


# The options to which it affects :

- --jars
- SparkContext.addJar(...) method
- SparkContext.addFile(...) method
- --conf spark.driver.extraClassPath=... or --driver-class-path ...
- --conf spark.driver.extraLibraryPath=..., or --driver-library-path ...
- --conf spark.executor.extraClassPath=...
- --conf spark.executor.extraLibraryPath=...
- not to forget, the last parameter of the spark-submit is also a .jar file.

I am aware where I can find the main spark documentation, and specifically about how to submit, the options available, and also the JavaDoc. However that left for me still quite some holes, although it answered partially too.

I hope that it is not all that complex, and that someone can give me a clear and concise answer.

If I were to guess from documentation, it seems that --jars, and the SparkContext addJar and addFile methods are the ones that will automatically distribute files, while the other options merely modify the ClassPath.

Would it be safe to assume that for simplicity, I can add additional application jar files using the 3 main options at the same time:
```
spark-submit --jar additional1.jar,additional2.jar \
  --driver-library-path additional1.jar:additional2.jar \
  --conf spark.executor.extraLibraryPath=additional1.jar:additional2.jar \
  --class MyClass main-application.jar
```



-----------------------------------------


# ClassPath:

ClassPath is affected depending on what you provide. There are a couple of ways to set something on the classpath:

- `spark.driver.extraClassPath` or it's alias `--driver-class-path` to set extra classpaths on the node running the driver.
- `spark.executor.extraClassPath` to set extra class path on the Worker nodes.

If you want a certain JAR to be effected on both the Master and the Worker, you have to specify these separately in BOTH flags.

# Separation character:

Following the same rules as the JVM:
```
Linux: A colon :
e.g: --conf "spark.driver.extraClassPath=/opt/prog/hadoop-aws-2.7.1.jar:/opt/prog/aws-java-sdk-1.10.50.jar"
Windows: A semicolon ;
e.g: --conf "spark.driver.extraClassPath=/opt/prog/hadoop-aws-2.7.1.jar;/opt/prog/aws-java-sdk-1.10.50.jar"
```

# File distribution:

This depends on the mode which you're running your job under:

1. Client mode - Spark fires up a Netty HTTP server which distributes the files on start up for each of the worker nodes. You can see that when you start your Spark job:
```
16/05/08 17:29:12 INFO HttpFileServer: HTTP File server directory is /tmp/spark-48911afa-db63-4ffc-a298-015e8b96bc55/httpd-84ae312b-5863-4f4c-a1ea-537bfca2bc2b
16/05/08 17:29:12 INFO HttpServer: Starting HTTP Server
16/05/08 17:29:12 INFO Utils: Successfully started service 'HTTP file server' on port 58922.
16/05/08 17:29:12 INFO SparkContext: Added JAR /opt/foo.jar at http://***:58922/jars/com.mycode.jar with timestamp 1462728552732
16/05/08 17:29:12 INFO SparkContext: Added JAR /opt/aws-java-sdk-1.10.50.jar at http://***:58922/jars/aws-java-sdk-1.10.50.jar with timestamp 1462728552767
```

2. Cluster mode - In cluster mode spark selected a leader Worker node to execute the Driver process on. This means the job isn't running directly from the Master node. Here, Spark will not set an HTTP server. You have to manually make your JARS available to all the worker node via HDFS/S3/Other sources which are available to all nodes.


# Accepted URI's for files

In "Submitting Applications", the Spark documentation does a good job of explaining the accepted prefixes for files:

```
When using spark-submit, the application jar along with any jars included with the --jars option will be automatically transferred to the cluster. Spark uses the following URL scheme to allow different strategies for disseminating jars:

1. file: - Absolute paths and file:/ URIs are served by the driver’s HTTP file server, and every executor pulls the file from the driver HTTP server.
2. hdfs:, http:, https:, ftp: - these pull down files and JARs from the URI as expected
3. local: - a URI starting with local:/ is expected to exist as a local file on each worker node. This means that no network IO will be incurred, and works well for large files/JARs that are pushed to each worker, or shared via NFS, GlusterFS, etc.

Note that JARs and files are copied to the working directory for each SparkContext on the executor nodes.
```

As noted, JARs are copied to the working directory for each Worker node. Where exactly is that? It is usually under /var/run/spark/work, you'll see them like this:

```
drwxr-xr-x    3 spark spark   4096 May 15 06:16 app-20160515061614-0027
drwxr-xr-x    3 spark spark   4096 May 15 07:04 app-20160515070442-0028
drwxr-xr-x    3 spark spark   4096 May 15 07:18 app-20160515071819-0029
drwxr-xr-x    3 spark spark   4096 May 15 07:38 app-20160515073852-0030
drwxr-xr-x    3 spark spark   4096 May 15 08:13 app-20160515081350-0031
drwxr-xr-x    3 spark spark   4096 May 18 17:20 app-20160518172020-0032
drwxr-xr-x    3 spark spark   4096 May 18 17:20 app-20160518172045-0033
```

And when you look inside, you'll see all the JARs you deployed along:

```
[*@*]$ cd /var/run/spark/work/app-20160508173423-0014/1/
[*@*]$ ll
total 89988
-rwxr-xr-x 1 spark spark   801117 May  8 17:34 awscala_2.10-0.5.5.jar
-rwxr-xr-x 1 spark spark 29558264 May  8 17:34 aws-java-sdk-1.10.50.jar
-rwxr-xr-x 1 spark spark 59466931 May  8 17:34 com.mycode.code.jar
-rwxr-xr-x 1 spark spark  2308517 May  8 17:34 guava-19.0.jar
-rw-r--r-- 1 spark spark      457 May  8 17:34 stderr
-rw-r--r-- 1 spark spark        0 May  8 17:34 stdout
```


# Affected options:

The most important thing to understand is priority. If you pass any property via code, it will take precedence over any option you specify via spark-submit. This is mentioned in the Spark documentation:
```
Any values specified as flags or in the properties file will be passed on to the application and merged with those specified through SparkConf. Properties set directly on the SparkConf take highest precedence, then flags passed to spark-submit or spark-shell, then options in the spark-defaults.conf file
So make sure you set those values in the proper places, so you won't be surprised when one takes priority over the other.
```
Lets analyze each option in question:

1. --jars vs SparkContext.addJar: These are identical, only one is set through spark submit and one via code. Choose the one which suites you better. One important thing to note is that using either of these options does not add the JAR to your driver/executor classpath, you'll need to explicitly add them using the extraClassPath config on both.
SparkContext.addJar vs SparkContext.addFile: Use the former when you have a dependency that needs to be used with your code. Use the latter when you simply want to pass an arbitrary file around to your worker nodes, which isn't a run-time dependency in your code.
2. --conf spark.driver.extraClassPath=... or --driver-class-path: These are aliases, doesn't matter which one you choose
3. --conf spark.driver.extraLibraryPath=..., or --driver-library-path ... Same as above, aliases.
4. --conf spark.executor.extraClassPath=...: Use this when you have a dependency which can't be included in an uber JAR (for example, because there are compile time conflicts between library versions) and which you need to load at runtime.
5. --conf spark.executor.extraLibraryPath=... This is passed as the java.library.path option for the JVM. Use this when you need a library path visible to the JVM.

Would it be safe to assume that for simplicity, I can add additional application jar files using the 3 main options at the same time:

You can safely assume this only for Client mode, not Cluster mode. As I've previously said. Also, the example you gave has some redundant arguments. For example, passing JARs to --driver-library-path is useless, you need to pass them to extraClassPath if you want them to be on your classpath. Ultimately, what you want to do when you deploy external JARs on both the driver and the worker is:

```
spark-submit --jars additional1.jar,additional2.jar \
  --driver-class-path additional1.jar:additional2.jar \
  --conf spark.executor.extraClassPath=additional1.jar:additional2.jar \
  --class MyClass main-application.jar
```
