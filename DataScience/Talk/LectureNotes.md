# Data Lecture Notes

8:00pm EST, 2016.04.28

----

## 1. Projects Overview

| Company | Goldman Sachs | Comcast |
| --- | --- | ---|
| Team Size | 30人，Global team，纽约只有一个人 | 10人，有 Architecture 有 ETL，各自分工 |
| Cluster Size| 主计算集群 220node 中等集群 12-16node，小集群 4node | QA集群12node
| Data Flow | 30+GB/day，要求所有数据不能错不能丢 | 500+GB/day，压缩前数TB，但不像高盛要求那么高
| Data Type | 期货交易数据 | ipv6数据流，如投诉信息（用户，时间，录音字节流）


- 主要工作内容
    + Streaming ETL
    + Data Migration
    + 写 Java Code 为主
        * framework: 企业自带的，和你自己设计的一套
        * consumer
        * report
        * mapreduce
    + Task Management: Agile 瞎写瞎计划瞎干
- 工作上的一些难点
    + Autentication，集群通信Kerberos, 需要自己管理key-cache
    + 设计consumer针对好几个流，要有代码复用性，层次结构要写好
    + 往 HBase 存注意 Interval，多久刷一次HTable Interface， Buffer设置多大
    + 注意 Java 程序挂了会不会丢数据


## 2. Cluster

- Structure
    + Admin
        * 200 node的administrator
        * 有一整个team，但是他们真的很弱，就好像网管一样：重启试试
    + Separation
        * 生产环境经常有 HDFS HBase 全分开的集群
        * 还有计算集群，YARN 会在计算集群，compute 和 storage 分开
        * 计算机群从HDFS读数据算出来存到HBase，同時三個集群认证，回让你疯掉一段时间

- UI interface
    + Cloudera Hue/Impala 或者 Hortonworks Ambari
    + 一般要 admin 团队才能看，你是看不见的

- Job
    + 一般跑work 6-12小时, 如果跑到20小时，要和admin团队沟通好
    + 平时周一到周五一直有 message 系统更新交易数据，等周五才能跑 MapReduce
    + 平时跑 QA cluster， 加production的数据可以也往里加一份
    + 生产环境你只能提交，老大才能运行

- Cooperation
    + team之间沟通成本，会耽误很多时间
    + 上到你大哥，大哥的大哥，下到QA，support team
    + 有的公司有 mission control MC team 看你的job问题或者控制权限


- Queue name
    + 默认是 root.default 优先级非常低，有其他人进来你的 MapReduce job就停了
    + 加了queue name，会在你的team底下运行，不会被抢资源

- Spark Resource Manager
    + Goldman Sachs: YARN
    + Comcast: Spark Standalone

- 有钱任性
    + Goldman Sachs
        * HDFS和YARN都独立开节点
        * 12个节点的cluster只跑一个HBase
        * 反正都在公司内部交流，nj一批机器，华盛顿一批机器，互相读其实也没多少延迟
    + Comcast也有独立的nifi cluster
    + 有钱任性


## 3. Technologies

### 3.1 Streaming

- 公司一般都是大量实时生成数据
- 数据格式
    + JSON
        * 常见但是很少用
        * Human readable, High Data Transfer Cost
    + Avro/Protocol Buffer
        * 网络花费小一点
        * 分两部分 Schema + Binary 组合成 JSON
    + Example: 一年前用的json format，现在的api无法解析，两种办法
        * 先判断新旧版本，再用java转换api，效率巨低
        * 用mapreduce整体把所有数据更新一遍，再读就方便了
- 工具对比
    + Kafka: 通用，吞吐量巨大，要用 Java 写 Kafka Consumer
    + RabbitMQ: 少一些，但也有2万条/s，Financial Team 数据安全性要求高会用到
    + Flume: 泛用性好，比如监测某个文件夹的log，一般Kafka就够
    + Storm: 环形结构，Python 要求高，超大量数据，一般用 Spark Streaming 就够
    + Nifi: Comcast, 简单，用UI没什么代码，很好用



### 3.2 MapReduce

- 逻辑搞清楚
- map output 和 Reduce input 一定要保证是 serializable
    + 一定是继承 writable 不能用 int， spark 同理
    + data cannot be serialized error
- 还有就是代码看上去很恶心


### 3.3 HBase

- Feature
    + 特别实用
    + 不能修改，只能增加新的条目
    + rowkey设计巨难
    + 什么都能连 rest spark java
- Data Integration Example
    + 设计一个rowkey (用户信息，买的东西)
    + 存在不同column family，可以一起取
- 准备
    + 不用在意 zookeeper 结构
    + 熟悉写 query
    + rowkey 和 column family
    + Performance
- HBase region too busy
    + 原来是有几个默认的维护compaction时间，其他都在周六
    + 还有就是周末有人在更新数据，周末你也不能做migration

### 3.3 Spark

- Spark
    + 需要内存大，20T就hold不住了 (一个集群联合memory 3-6TB)
    + 其实有硬盘模式，但通常不会去调整
    + 配合Kerberos时稳定性交差，放生产cluster上会有问题

- 主要的工作
    + Hadoop Job 里所有没有用到common java api的任务
    + ETL/data cleaning/report
    + 以后会做spark streaming 和 mem SQL
    + 极少做分析，不会去了解business logic

- Data Cleaning
    + column missing: 电话号码没存
    + 不同格式不吻合，要统一 NYSE  NYEXCHANGE
    + 很简单，spark一共就三行



### 3.4 Scheduler
- 通常写好了jar包，有专门的 MC team 负责怎么运行，出错了怎么办
- 一般公司都有自己的 Scheduler，并没有用 Oozie
    + Goldman Sachs: SLANG (SecDB)
    + BoA: PHP
    + Comcast: Manual?

### 3.5 其他

- 测试工具: MRUnit JUnit
- Machine Learning
    + ALS: Alternative Least Square  交替最小二乘法
    + 提前算矩阵，每次拿数据对比一下，隔一段时间再更新
- Hive/Pig
    + Hive: 给不太会编程的人拿数据
    + Pig: 效率太差了，spark强太多了，不会写代码凑合用
    + UDF: 基本没用
- 自定义format: 很少用到
    + HBase 写一个 Custom Coprocessor，提交后，如果看不懂，不会允许运行的
- SQL: 只有Spark一起用的一个memSQL，和 Spark 一样内存溢出也不好使了
- Multithreading: 从来用不到
