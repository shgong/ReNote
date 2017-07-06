# Ordering

- scala.math.ordering
    + ordering.by
    + ordering.on
    
```scala
import scala.util.Sorting
val pairs = Array(("a", 5, 2), ("c", 3, 1), ("b", 1, 3))

// sort by 2nd element
Sorting.quickSort(pairs)(Ordering.
    by[(String, Int, Int), Int](_._2)

// sort by the 3rd element, then 1st
Sorting.quickSort(pairs)(Ordering[(Int, String)].
    on[(String, Int, Int)]((_._3, _._1))

```
