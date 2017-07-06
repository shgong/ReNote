filter.md

scala> val r = "Y,"
r: String = Y,

scala> r.split(",")
res1: Array[String] = Array(Y)

scala> r.split(",",-1)
res2: Array[String] = Array(Y, "")






scala> val m = ""
m: String = ""

scala> m.split(",")
res3: Array[String] = Array("")

scala> m.split(",", -1)
res4: Array[String] = Array("")