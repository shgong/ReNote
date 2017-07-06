StrangeError.md

```
val dataset = full.filter("COLUMN!=VALUE")
COLUMN is ByteType while VALUE is a string

it would okay if
1. run on spark shell
2. use (COLUMN NOT IN (VALUE))
```

# Shell & Submit difference ?

I have walked through the code for this module several times, strangely the exact same code on spark shell will return the correct result, while spark submit will always return 0
I am not sure where this error come from, as shell & submit should be identical except for some variable overwriting things

Perhaps when type mismatch, it will sometimes skip comparing values but return false directly.
I heard some type boxing & unboxing behavior is different at Interpreter, like when you call 
`val s: String = s + s`
You will get s = “nullnull” with interpreter, but throws exception with sbt

Solutions:
1.  make sure we pass the right value type ( 1/0 instead of Y/N in this case), so we don’t need to change the code
2.  replace  A!=B  syntax with  A NOT IN (B), I don’t know why, it just works…

# Turns out to be caching

```scala
 val fullTable = spark.read.parquet("/db/ebi/sparkle/moto/parquet/joined")
    val where = Map("division" ->"WEST DIVISION", "region" -> "CALIFORNIA REGION")
    val exclusion = Map("VOICE_SERVICEABILITY_IND" -> Left("N"))

    val campaignDF = fullTable
      .filterWithWhereMap(where)
      .excludeWithWhereMap(exclusion)

    println("-----------------------------------------------")
    println("[sparkTest] uncached result: " + campaignDF.count().toString)

    fullTable.cache()

    val campaignDF2 = fullTable
      .filterWithWhereMap(where)
      .excludeWithWhereMap(exclusion)

    println("[sparkTest] cached result: " + campaignDF2.count().toString)
    println("-----------------------------------------------")
```

[sparkTest] uncached result: 2536620
[moto] filter: division=="WEST DIVISION" and region=="CALIFORNIA REGION"
[moto] exclusion: VOICE_SERVICEABILITY_IND!="N"
[sparkTest] cached result: 0
-----------------------------------------------

# Behavior with different approach

spark v2.0.2

```scala
val fullTable = spark.read.parquet("/db/ebi/sparkle/moto/parquet/joined")
val campaignDF = fullTable.filter("VOICE_SERVICEABILITY_IND!=\"N\"").count()  
// 28107351
fullTable.cache()
val campaignDF2 = fullTable.filter("VOICE_SERVICEABILITY_IND!=\"N\"").count() 
// 0

// now throw away cache and try again with temp table
fullTable.unpersist()
fullTable.registerTempTable("fullt")
spark.sql("select count(1) from fullt where VOICE_SERVICEABILITY_IND!=\"N\"").show 
// 28107351
fullTable.cache()
spark.sql("select count(1) from fullt where VOICE_SERVICEABILITY_IND!=\"N\"").show 
// 0
```