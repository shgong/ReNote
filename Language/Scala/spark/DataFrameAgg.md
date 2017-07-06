# DataFrameAgg.md

to dynamically build the aggregation query, we can build a query from list, like this

```scala 
    def getSum(columnName: List[String]): Row =
      ds.agg(
        columnName.map {
          case "count" => ("*", "count")
          case x => (x, "sum")
        } (collection.breakOut): Map[String, String]
      )
      .na.fill(0)
      .collect()
      .head

// The collection.breakOut is basically a more efficient toMap (which need one more traverse)

someDataSet.getSum("count","salary","purchase_possibility")
```


It works because agg function accept different argument types, including

- (String, String)*
- Column*
- Map[String, String]

However the problem with Map is that, the toMap or breakOut won't preserve the element order

```scala
scala> val l = List("tbd_mailed","tbd_knocked","tbd_called")
l: List[String] = List(tbd_mailed, tbd_knocked, tbd_called)


scala> val l2 = "count"::"adj_pml"::l
l2: List[String] = List(count, adj_pml, tbd_mailed, tbd_knocked, tbd_called)


scala> val m = l2.map {
     |           case "count" => ("*", "count")
     |           case x => (x, "sum")
     |         }
m: List[(String, String)] = List((*,count), (adj_pml,sum), (tbd_mailed,sum), (tbd_knocked,sum), (tbd_called,sum))


scala> val m2 = m.toMap
m2: scala.collection.immutable.Map[String,String] = Map(tbd_mailed -> sum, * -> count, adj_pml -> sum, tbd_knocked -> sum, tbd_called -> sum)
```


even pass it directly will still break the order
```scala
val m= List(
  ("*","count"),
  ("adj_pml","sum"),
  ("tbd_mailed","sum"),
  ("tbd_knocked","sum"),
  ("tbd_called","sum")
)

val t = Map(m:_*)

// or 

val t = Map(("*","count"),
  ("adj_pml","sum"),
  ("tbd_mailed","sum"),
  ("tbd_knocked","sum"),
  ("tbd_called","sum"))

t: scala.collection.immutable.Map[String,String] = Map(tbd_mailed -> sum, * -> count, adj_pml -> sum, tbd_knocked -> sum, tbd_called -> sum)
```

but agg don't support Map with orders, like listMap / sortedMap


### Solution
Use agg(x:_*) directly with agg instead, agg support expressions, only use agg with Map would cause this problem

// rewrite getSum with seq argument conversion as toMap causes ordering issue







Why?

4 elements -> ok
5 elements -> order wrong

How scala collection implement toMap
```scala
val t = List(
  ("1","0"),
  ("2","0"),
  ("3","0"),
  ("4","0"),
  ("5","0")
)

val b = immutable.Map.newBuilder[String,String]
for (x <- t) {
  b += x
  println(b.result())
}

b: scala.collection.mutable.Builder[(String, String),scala.collection.immutable.Map[String,String]] = scala.collection.mutable.MapBuilder@13b4f9a3
Map(1 -> 0)
Map(1 -> 0, 2 -> 0)
Map(1 -> 0, 2 -> 0, 3 -> 0)
Map(1 -> 0, 2 -> 0, 3 -> 0, 4 -> 0)
Map(4 -> 0, 5 -> 0, 1 -> 0, 2 -> 0, 3 -> 0)
```

// builder just use a Builder pattern to construct map incrementally
// use map directly yield the same result
```
var b = immutable.Map[String,String]()
for (x <- t) {
  b += x
  println(b)
}

Map(1 -> 0)
Map(1 -> 0, 2 -> 0)
Map(1 -> 0, 2 -> 0, 3 -> 0)
Map(1 -> 0, 2 -> 0, 3 -> 0, 4 -> 0)
Map(4 -> 0, 5 -> 0, 1 -> 0, 2 -> 0, 3 -> 0)
```


// look into Map object
// direct definition of classes

```
class Map1[A, +B](key1: A, value1: B) extends AbstractMap[A, B] with Map[A, B] with Serializable
class Map2[A, +B](key1: A, value1: B, key2: A, value2: B) extends AbstractMap[A, B] with Map[A, B] with Serializable
class Map3[A, +B](key1: A, value1: B, key2: A, value2: B, key3: A, value3: B) extends AbstractMap[A, B] with Map[A, B] with Serializable
class Map4[A, +B](key1: A, value1: B, key2: A, value2: B, key3: A, value3: B, key4: A, value4: B) extends AbstractMap[A, B] with Map[A, B] with Serializable
```

// toString is achieved from MapLike using IterableLike
```
var b = immutable.Map[String,String]()
for (x <- t) {
  b += x
  println(b.toIterator.next())
}

(1,0)
(1,0)
(1,0)
(1,0)
(4,0)
```

// What if increase Map elements
```
Map(4 -> 0, 5 -> 0, 1 -> 0, 2 -> 0, 3 -> 0)
Map(4 -> 0, 5 -> 0, 6 -> 0, 1 -> 0, 2 -> 0, 3 -> 0)
Map(4 -> 0, 5 -> 0, 6 -> 0, 1 -> 0, 2 -> 0, 7 -> 0, 3 -> 0)
```

// when more than 4 elements, it falls to MapLike Implementation
// Where keys are implemented with keySet: immutable.Set[A] instead

```
val t = List( "1", "2", "3", "4", "5", "6", "7")

var b = immutable.Set[String]()
for (x <- t) {
  b += x 
  println(b)
}



```
