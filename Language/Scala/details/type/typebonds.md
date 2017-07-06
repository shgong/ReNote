# typebonds.md

## Upper Type bonds

```scala
trait Similar {
  def isSimilar(x: Any): Boolean
}

case class MyInt(x: Int) extends Similar {
  def isSimilar(m: Any): Boolean =
    m.isInstanceOf[MyInt] &&
    m.asInstanceOf[MyInt].x == x
}

object UpperBoundTest extends App {
  def findSimilar[T <: Similar](e: T, xs: List[T]): Boolean =
    if (xs.isEmpty) false
    else if (e.isSimilar(xs.head)) true
    else findSimilar[T](e, xs.tail)
  val list: List[MyInt] = List(MyInt(1), MyInt(2), MyInt(3))
  println(findSimilar[MyInt](MyInt(4), list))
  println(findSimilar[MyInt](MyInt(2), list))
}
```

without upper type bound annotation it would be impossible to call `isSimilar` in method `findSimilar`  ( smaller than :)

## Lower Type bonds

```scala
case class ListNode[T](h: T, t: ListNode[T]) {
  def head: T = h
  def tail: ListNode[T] = t
  def prepend(elem: T): ListNode[T] =
    ListNode(elem, this)
}
```

Currently ListNode[String] is not subtype of ListNode[Any]
- we can't do `case class ListNode[+T](h: T, t: ListNode[T]) { ... }`
- covariance annotation is only possible if type variable only used in covariant positions
- T also appears as parameter in method prepend, rule broken 
- make method prepend accept a supertype

```scala
case class ListNode[+T](h: T, t: ListNode[T]) {
  def head: T = h
  def tail: ListNode[T] = t
  def prepend[U >: T](elem: U): ListNode[U] =
    ListNode(elem, this)
}


object LowerBoundTest extends App {
  val empty: ListNode[Null] = ListNode(null, null)
  val strList: ListNode[String] = empty.prepend("hello")
                                       .prepend("world")
  val anyList: ListNode[Any] = strList.prepend(12345)
}
```
