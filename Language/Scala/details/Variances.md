# Variances.md

```scala

class Stack[+T]{
    def push[S >: T](elem: S): Stack[S] = new Stack[S] {
        override def top: S = elem
        override def pop: Stack[S] = Stack.this
        override def toString() = elem.toString() + " " + Stack.this.toString()
    }
    
    def top: T = sys.error("no element on stack")
    def pop: Stack[T] = sys.error("no element on stack")
    override def toString() = ""
}

object VarianceTest extends App {
    var s: Stack[Any] = new Stack().push("hello");
    s = s.push(new Object())
    s = s.push(7)
    println(s)
}


```

`+T` type for covariant positions, Stack[T] is subtype of Stack[S], if T is a subtype of S

`-T` for contravariant positions, means the opposite

Now stacks are covariant, our solution allows that 

- e.g. push a string on an integer stack, result will be a stack of type Stack[Any]; 
- only if the result is used in a context where we expect an integer stack, we actually detect the error. 
- Otherwise we just get a stack with a more general element type.

## contravariant functor
```scala
case class Money(amount: Int)
val contramapFn: Money => Int = (money) => money.amount
implicit val moneyOrd: Ordering[Money] = Ordering.by(contramapFn)

scala> import scala.math.Ordered._
import scala.math.Ordered._

scala> Money(13) < Money(20)
res0: Boolean = true

scala> Money(23) < Money(20)
res1: Boolean = false
```

implementing by
```
def by[T, S](f: T => S)(implicit ord: Ordering[S]): Ordering[T] =
  new Ordering[T] {
    def compare(x:T, y:T) = ord.compare(f(x), f(y))
  }
```

accept a `Money=>Int` to do `Ordering[Int]=>Ordering[Money]`





## Code Example

```scala
// co variant
trait Level
class Root extends Level {override def toString = "root"}
class Branch extends Root {override def toString = "branch"}
class Leaf extends Branch {override def toString = "leaf"}

case class ListNode[+T](h: T, t: ListNode[T]) {
  def head: T = h
  def tail: ListNode[T] = t
  def prepend[U >: T](elem: U): ListNode[U] =
    ListNode(elem, this)
}

val empty: ListNode[Null] = ListNode(null, null)
val l2: ListNode[Leaf] = empty.prepend(new Leaf())
val l1: ListNode[Branch] = l2.prepend(new Branch())
val l0: ListNode[Root] = l1.prepend(new Root())


// contra variant
case class ChildType[-T](i: Int) {
  def check[U <: T](u: U): Boolean = true
}

val n = new ChildType[Root](10)
n.check(new Leaf())

val j: ChildType[Branch] = n
j.check(new Leaf())
```

Contravariant is limited, a class that accept method for a narrower type,
we won't store values of that child type and keep the hierarchy, so we can just design logic


### Order 

To make it simple, let's just consider Seq[Int] and List[Int]. It should be clear that List[Int] is a subtype of Seq[Int], ie, List[Int] <: Seq[Int].

```scala
def smaller(a: List[Int], b: List[Int])(implicit ord: Order[List[Int]]) =
  if (ord.order(a,b) == LT) a else b

//Now I'm going to write an implicit Order for Seq[Int]:
implicit val seqOrder = new Order[Seq[Int]] { 
  def order(a: Seq[Int], b: Seq[Int]) = 
    if (a.size < b.size) LT
    else if (b.size < a.size) GT
    else EQ
}

// With these definitions, I can now do something like this:
scala> smaller(List(1), List(1, 2, 3))
res0: List[Int] = List(1)
```
This means that Order[Seq[Int]] <: Order[List[Int]]. Given that Seq[Int] >: List[Int], this is only possible because of contra-variance.


### Equal

```scala
def equalLists(a: List[Int], b: List[Int])(implicit eq: Equal[List[Int]]) = eq.equal(a, b)
```

Because Order extends Equal, I can use it with the same implicit above:
```
scala> equalLists(List(4, 5, 6), List(1, 2, 3)) // we are comparing lengths!
res3: Boolean = true
```

The logic here is the same one. Anything that can tell whether two Seq[Int] are the same can, obviously, also tell whether two List[Int] are the same. From that, it follows that `Equal[Seq[Int]] <: Equal[List[Int]]`, which is true because Equal is contra-variant.
