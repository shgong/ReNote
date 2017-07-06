# NumericGeneric

using Numeric combine to implicit conversion your example 

```scala
import Numeric._
def f[T](l: List[T])(implicit n: Numeric[T]):T = {
    var s = n.zero
    for (i <- l)
        s = n.plus(s, i)
    s
}

val l1 = List(1.0, 2.0, 3.0)
val l2 = List(1, 2, 3)

println(f(l1))
println(f(l2))

//or
def f2[T](l: List[T])(implicit n: Numeric[T]):T = {
 import n._
 var s = zero
 for (i <- l)
   s += i
 s
}
println(f2(l1))
println(f2(l2))
```


Now another example doing the sum in a more scala way:

```scala
def sum[T](l:List[T])(implicit n: Numeric[T]):T = {
 import n._
 l.foldLeft(zero)(_ + _)
}

println(sum(l1))
println(sum(l2))

//or since 2.8 Seq include already a sum function
def sum[T](l:List[T])(implicit n: Numeric[T]):T = l.sum

println(sum(l1))
println(sum(l2))
```



uses the Numeric trait.
```
import Numeric._
def f[A](l: List[A])(implicit numeric: Numeric[A]) = 
  l reduceLeft ((l,r) => numeric.plus(l, r))
```
Or using context bounds:
```
def f[A : Numeric](l: List[A]) =
   l.reduceLeft((l,r) => implicitly[Numeric[A]].plus(l, r))
```


def add[A](x: A, y: A)(implicit numeric: Numeric[A]): A = numeric.plus(x, y)


