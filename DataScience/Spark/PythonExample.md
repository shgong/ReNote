# PythonExample

## Wordcount

```python
from pyspark import SparkContext, SparkConf
conf = SparkConf().setAppName("NLP")
sc = SparkContext(conf=conf)

contentRDD = sc.textFile("Content.txt")

nonempty_lines = contentRDD.filter(lambda x: len(x) > 0)  # empty lines
words = nonempty_lines.flatMap(lambda x: x.split(' ')) # split by space
wordcounts = words.map(lambda x: (x, 1)) \
        .reduceByKey(lambda x, y: x+y) \
        .map(lambda x: (x[1], x[0])) \
        .sortByKey(False)

for word in wordcounts.collect():
    print(word)
```

save as sequence file
```
# write
nonempty_lines.map(lambda line: (None, line)).
    saveAsSequenceFile("problem86_1")

# read
seqRDD = sc.sequenceFile("problem86_1")
```


SparkSQL with Hive
```py
from pyspark.sql import HiveContext

# Create sqlContext
sqlContext = HiveContext(sc)

# Query hive
employee2 = sqlContext.sql("select * from employee2")

# Now prints the data
for row in employee2.collect():
    print(row)

# Print specific column
for row in employee2.collect():
    print(row.first_name)
```

SQL with JSON
```py
from pyspark import SQLContext
sqlContext = SQLContext(sc)
employee = sqlContext.jsonFile("employee.json") #Load json file

#Register RDD as a temp table
employee.registerTempTable("Employee Tab")
employeeInfo = sqlContext.sql("select * from EmployeeTab")
for row in employeeInfo.collect():
    print(row)

employeeInfo.toJSON().saveAsTextFile("employeeJson1")
```


Reduce
```
totalRevenue = extractedRevenueColumn.reduce(lambda a, b: a + b)
maximumRevenue = extractedRevenueColumn.reduce(lambda a, b: (a if a>=b else b))
minimumRevenue = extractedRevenueColumn.reduce(lambda a, b: (a if a<=b else b))
```

byKey series
```
file 91
```

totalRevenue Practice
```
file92
```

