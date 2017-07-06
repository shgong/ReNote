val df = List((1,2,3),(1,4,7),(1,8,10),(2,10,1)).toDF("a","b","c")
df.show
val cols = List("a","b","c")
val w = Window.partitionBy("a").orderBy(desc("c"))


df.select(cols.map(x=> first(col(x)).over(w).alias(x)):_*).dropDuplicates

df.withColumn("rn", row_number.over(w)).where(col("rn") === 1).drop("rn")