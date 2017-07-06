# DatasetExp.md


|Type| Syntax | Analysis
|----------|--------- | -----
|SQL | Runtime | Runtime
|Dataframe | Compile Time | Runtime
|Dataset| Compile Time | Compile Time



    val table = spark.read.parquet("/db/ebi/sparkle/moto/store/campaign-10")
    table.limit(1000).cache()
    table.groupBy('segment).count.show
   



    allDF.withColumn("target",
        when(col("demcons_group_code")==="A","African American").
        when(col("demcons_group_code")==="B","East Asian").
        when(col("demcons_group_code")==="N","East Asian").
        when(col("demcons_group_code")==="G","European").
        when(col("demcons_group_code")==="L","European").
        when(col("demcons_group_code")==="E","European").
        when(col("demcons_group_code")==="O","Hispanic").
        when(col("demcons_group_code")==="I","Middle Eastern").
        when(col("demcons_group_code")==="C","South Asian").
        when(col("demcons_group_code")==="K","Western European").
        otherwise("Other")
    )

or udf

```
def t = { println(java.time.LocalDateTime.now) }

t;table.withColumn("sum", 'tbd_mailed + 'tbd_knocked).show();t;

table.map( _.getAs[Int]("tbd_knocked")).show

table.map { row =>
    val tbd_knocked = row.getAs[Int]("tbd_knocked")
    val tbd_called = row.getAs[Int]("tbd_called")
    org.apache.spark.sql.Row(tbd_knocked + tbd_called, tbd_knocked - 1)
}



import org.apache.spark.sql.types._
val schema = StructType(
  StructField("tbd1", IntegerType, nullable = false) ::
  StructField("tbd2", IntegerType, nullable = false) :: Nil
  )

import org.apache.spark.sql.catalyst.encoders.RowEncoder
implicit val encoder = RowEncoder(schema)
```



