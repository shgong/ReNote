# Spark SQL

## 1. SchemaRDD

capabilities
- laod data from varied sources: JSON, Hive, Parquet
- SQL, inside Spark program and through connectors
- rich intergration between SQL and regular Python/Java/Scala code

SchemaRDD
- an RDD of Row objects, each presenting a record
- look like regular RDD, but store more efficiently with schema

## 2. Start

Initializing Spark SQL
```scala
import org.apache.spark.sql.SQLContext
// this is used to implicitly convert an RDD to a DataFrame.
import sqlContext.implicits._
```

```py
from pyspark.sql import SQLContext, Row
```


Creation
```
val df = sqlContext.read.json("examples/src/main/resources/people.json")
df.show()
```

Operation
```python
from pyspark.sql import SQLContext
sqlContext = SQLContext(sc)

# Create the DataFrame
df = sqlContext.read.json("examples/src/main/resources/people.json")

df.show()
df.printSchema()

# Select only the "name" column
df.select("name").show()
# Select everybody, but increment the age by 1
df.select(df['name'], df['age'] + 1).show()
# Select people older than 21
df.filter(df['age'] > 21).show()
# Count people by age
df.groupBy("age").count().show()
```

More Example
```scala
// Data can easily be extracted from existing sources,
// such as Apache Hive.
val trainingDataTable = sql("""
  SELECT e.action
         u.age,
         u.latitude,
         u.logitude
  FROM Users u
  JOIN Events e
  ON u.userId = e.userId""")
 
// Since `sql` returns an RDD, the results of the above
// query can be easily used in MLlib
val trainingData = trainingDataTable.map { row =>
  val features = Array[Double](row(1), row(2), row(3))
  LabeledPoint(row(0), features)
}
 
val model =
  new LogisticRegressionWithSGD().run(trainingData)
```



```scala
val allCandidates = sql("""
  SELECT userId,
         age,
         latitude,
         logitude
  FROM Users
  WHERE subscribed = FALSE""")
 
// Results of ML algorithms can be used as tables
// in subsequent SQL statements.
case class Score(userId: Int, score: Double)
val scores = allCandidates.map { row =>
  val features = Array[Double](row(1), row(2), row(3))
  Score(row(0), model.predict(features))
}
scores.registerAsTable("Scores")
 
val topCandidates = sql("""
  SELECT u.name, u.email
  FROM Scores s
    JOIN Users u ON s.userId = u.userId
  ORDER BY score DESC
  LIMIT 100""")
// Send emails to top candidates to promote the service.
```
