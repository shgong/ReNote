```scala

import org.apache.spark.sql.DataFrame

val df = spark.sql("select * from bgong001c.moto_stage32 limit 1000")


implicit class WorkerChecker(df:  org.apache.spark.sql.DataFrame) {
  def check(c:Any) =
    df.rdd.mapPartitions { iter =>
      Iterator(c)
    }
  }

usage: df.check(blabla).collect

df.check{import java.io.File; new File("/home/ebisasicep/").listFiles}



df.rdd.mapPartitions { iter => val k=new File("/").listFiles; Iterator(k)}.collect().map(_.toList).foreach(println)
```
