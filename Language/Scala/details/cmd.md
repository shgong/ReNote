cmd.md

partition
```scala
val cmd = """--appName Moto --sparkMaster=yarn --nExecutors 100 --port=50070"""
val args = cmd.split(" ")

val (opts, vals) = args.partition {
  _.startsWith("-")
}

opts: Array[String] = Array(--appName, --sparkMaster=yarn, --nExecutors, --port=50070)
vals: Array[String] = Array(Moto, 100)
```



```scala
  def getOpts(args: Array[String]): collection.mutable.Map[String, String] = {
    val (opts, vals) = args.partition { _.startsWith("-") }

    val optsMap = collection.mutable.Map[String, String]()

    opts.map { x =>
      val pair = x.split("=")
      if (pair.length == 2) {
        optsMap += (pair(0).split("-{1,2}")(1) -> pair(1))
      } else {
        SparkleLog.warn(usage)
        System.exit(1)

      }
    }

    return optsMap
  }
```



functional style
```scala

  def getOpts(args: Array[String]): collection.immutable.Map[String, String] =
    try {
      args.toList
        .flatMap(_.split("="))
        .foldLeft[List[List[String]]](List())(
        (list, str) =>
          if (str.startsWith("-")) List(str.replace("-", "")) :: list
          else (str :: list.head) :: list.tail)
        .map(x => x.last -> x.init.last)
        .toMap
    } catch {
      case e: Exception =>
        SparkleLog.warn(usage)
        System.exit(1)
        Map()
    }

}
```


