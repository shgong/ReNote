# toMap

converting a LinkedHashMap to a Map preserve the order in which elements are stored?

The generic Map interface makes no such guarantee of ordering. Nor can it, as this would then rule out HashMap as a possible implementation.

I believe collection.immutable.ListMap preserves insertion order, you could also use a LinkedHashMap via the Map interface, which would then prevent access to any mutator methods. This is easy enough to do by explicitly specifying the type:
```
val m: scala.collection.Map[Int,Int] = collection.mutable.LinkedHashMap(1->2, 2->3)
```

or (using type ascription):

```
val m = collection.mutable.LinkedHashMap(1->2, 2->3) : Map[Int,Int]
```

// Gives Map(a -> b, c -> d), but lose order
```
List("a" -> "b", "c" -> "d").toMap
```

// NOT TESTED
```
x.groupBy(_._1).mapValues(_.map(_._2).head)
```







// If you want a map that returns its elements in sorted order by keys, use a SortedMap:
```scala
scala> import scala.collection.SortedMap
import scala.collection.SortedMap

scala> val grades = SortedMap("Kim" -> 90,
     | "Al" -> 85,
     | "Melissa" -> 95,
     | "Emily" -> 91,
     | "Hannah" -> 92
     | )
grades: scala.collection.SortedMap[String,Int] = Map(Al -> 85, Emily -> 91, Hannah -> 92, Kim -> 90, Melissa -> 95)
```


If you want a map that remembers the insertion order of its elements, use a LinkedHashMap or ListMap. Scala only has a mutable LinkedHashMap, and it returns its elements in the order you inserted them:
```scala
scala> import scala.collection.mutable.LinkedHashMap
import scala.collection.mutable.LinkedHashMap

scala> var states = LinkedHashMap("IL" -> "Illinois")
states: scala.collection.mutable.LinkedHashMap[String,String] = Map(IL -> Illinois)

scala> states += ("KY" -> "Kentucky")
res0: scala.collection.mutable.LinkedHashMap[String,String] = Map(IL -> Illinois, KY -> Kentucky)

scala> states += ("TX" -> "Texas")
res1: scala.collection.mutable.LinkedHashMap[String,String] = Map(IL -> Illinois, KY -> Kentucky, TX -> Texas)
```


Scala has both mutable and immutable ListMap classes. They return elements in the opposite order in which you inserted them, as though each insert was at the head of the map (like a List):

```scala
scala> import scala.collection.mutable.ListMap
import scala.collection.mutable.ListMap

scala> var states = ListMap("IL" -> "Illinois")
states: scala.collection.mutable.ListMap[String,String] = Map(IL -> Illinois)

scala> states += ("KY" -> "Kentucky")
res0: scala.collection.mutable.ListMap[String,String] = Map(KY -> Kentucky, IL -> Illinois)

scala> states += ("TX" -> "Texas")
res1: scala.collection.mutable.ListMap[String,String] = Map(TX -> Texas, KY -> Kentucky, IL -> Illinois)
```



Traversals
```scala
c map (t => t.getP -> t) toMap
but be aware that this needs 2 traversals. To get away with one use

c.map(t => t.getP -> t)(collection.breakOut): Map[P, T]
breakOut instructs .map to immediately create a collection of the expected type.
```