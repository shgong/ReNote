# Common Structures in Functional Design

- Purely algebraic structures

## 10. Monoids

- monoid
    + structure defined only by its algebra: simple, ubiquitous and useful
    + come up all the time in everyday programming
        * working with lists
        * concatenate strings
        * accumulating results of a loop
    + useful in two ways
        * facilitate prallel computation by giving us freedom to break down problem
        * composed to assemble complex calculation from simpler prices

### 10.1 Monoid

- consider algebra of different operations
    + identity element: "", 0, true/false
    + associative: ((r+s)+t) or (r+(s+t))
- Monoid laws
    + some type A
    + an associative binary operation,  op(op(x,y),z) == op(x,op(y,z))
    + an identity value: zero:A, op(x,zero)==op(zero,x)==x

```scala
trait Monoid[A] {
    def op(a1:A,a2:A): A
    def zero: A
}
val stringMonoid = new Monoid[String] {
    def op(a1:String, a2:String) = a1 + a2
    def zero = ""
}
val listMonoid[A] = new Monoid[List[A]] {
    def op(a1:List[A], a2:List[A]) = a1 ++ a2
    def zero = Nil
}
val booleanOr: Monoid[Boolean] = new Monoid[Boolean] {
  def op(x: Boolean, y: Boolean) = x || y
  val zero = false
}
```

Monoid Dual
- can compose options in either order, both satisfy monoid law, but not equal
- every monoid has a dual
    + booleanOr & intAddition are equivalent to duals
    + because op is commutative & associative

```scala
def optionMonoid[A]: Monoid[Option[A]] = new Monoid[Option[A]] {
  def op(x: Option[A], y: Option[A]) = x orElse y
  val zero = None
}

// We can get the dual of any monoid just by flipping the `op`.
def dual[A](m: Monoid[A]): Monoid[A] = new Monoid[A] {
  def op(x: A, y: A): A = m.op(y, x)
  val zero = m.zero
}

// Now we can have both monoids on hand:
def firstOptionMonoid[A]: Monoid[Option[A]] = optionMonoid[A]
def lastOptionMonoid[A]: Monoid[Option[A]] = dual(firstOptionMonoid)
```

EndoFunction
- function with same argument and return type
```scala
def endoMonoid[A]: Monoid[A => A] = new Monoid[A => A] {
  def op(f: A => A, g: A => A) = f compose g     // f(g(a))
  // def op(f: A => A, g: A => A) = f andThen g  // g(f(a))
  val zero = (a: A) => a
}
```

Test Monoid
```scala
import fpinscala.testing._
import Prop._

def monoidLaws[A](m: Monoid[A], gen: Gen[A]): Prop =
  // Associativity
  forAll(for {
    x <- gen
    y <- gen
    z <- gen
  } yield (x, y, z))(p =>
    m.op(p._1, m.op(p._2, p._3)) == m.op(m.op(p._1, p._2), p._3)) &&
  // Identity
  forAll(gen)((a: A) =>
    m.op(a, m.zero) == a && m.op(m.zero, a) == a)
```

What is Monoid
- a monoid is actually both things
    + the type Monoid[A]
    + also the instance satisfying the law
- a monoid is a type together with an binary operation (op) over that type, satisfying associativity and having an identity element (zero)

### 10.2 Folding lists with monoids

- Monoid have an intimate connection with Lists
- signature of folds
    + fold left/right with monoid yield the same result

```scala
def foldRight[B](z: B)(f: (A, B) => B): B
def foldLeft[B](z: B)(f: (B, A) => B): B

val words = List("Hic", "Est", "Index")
val s = words.foldRight(stringMonoid.zero)(stringMonoid.op) // HicEstIndex
val t = words.foldLeft(stringMonoid.zero)(stringMonoid.op)  // HicEstIndex
```

- foldMap: if list have element type that doesn't have Monoid instance
```scala
def foldMap[A, B](as: List[A], m: Monoid[B])(f: A => B): B =
  as.foldLeft(m.zero)((b, a) => m.op(b, f(a)))

// The function type `(A, B) => B`, when curried, is `A => (B => B)`.
// And of course, `B => B` is a monoid for any `B` (via function composition).
def foldRight[A, B](as: List[A])(z: B)(f: (A, B) => B): B =
  foldMap(as, endoMonoid[B])(f.curried)(z)

// Folding to the left is the same except we flip the arguments to
// the function `f` to put the `B` on the correct side.
// Then we have to also "flip" the monoid so that it operates from left to right.
def foldLeft[A, B](as: List[A])(z: B)(f: (B, A) => B): B =
  foldMap(as, dual(endoMonoid[B]))(a => b => f(b, a))(z)
```


### 10.3 Associativity & Parallelism

- associativity
    + we can choose how we fold a data structure
    + use balanced fold, enable parellel, more efficient
    + `op(op(a,b),op(c,d))`  -> reduce

```scala
// more effective foldMap
def foldMapV[A, B](as: IndexedSeq[A], m: Monoid[B])(f: A => B): B =
  if (as.length == 0)
    m.zero
  else if (as.length == 1)
    f(as(0))
  else {
    val (l, r) = as.splitAt(as.length / 2)
    m.op(foldMapV(l, m)(f), foldMapV(r, m)(f))
  }


// This ability to 'lift' a monoid any monoid to operate within
// some context (here `Par`) is something we'll discuss more in 
// chapters 11 & 12
def par[A](m: Monoid[A]): Monoid[Par[A]] = new Monoid[Par[A]] {
  def zero = Par.unit(m.zero)  
  def op(a: Par[A], b: Par[A]) = a.map2(b)(m.op)
}

// we perform the mapping and the reducing both in parallel
def parFoldMap[A,B](v: IndexedSeq[A], m: Monoid[B])(f: A => B): Par[B] = 
  Par.parMap(v)(f).flatMap { bs => 
    foldMapV(bs, par(m))(b => Par.async(b)) 
  }


// This implementation detects only ascending order,
// but you can write a monoid that detects both ascending and descending
// order if you like.
def ordered(ints: IndexedSeq[Int]): Boolean = {
  // Our monoid tracks the minimum and maximum element seen so far
  // as well as whether the elements are so far ordered.
  val mon = new Monoid[Option[(Int, Int, Boolean)]] {
    def op(o1: Option[(Int, Int, Boolean)], o2: Option[(Int, Int, Boolean)]) =
      (o1, o2) match {
        // The ranges should not overlap if the sequence is ordered.
        case (Some((x1, y1, p)), Some((x2, y2, q))) =>
          Some((x1 min x2, y1 max y2, p && q && y1 <= x2))
        case (x, None) => x
        case (None, x) => x
      }
    val zero = None
  }
  // The empty sequence is ordered, and each element by itself is ordered.
  foldMapV(ints, mon)(i => Some((i, i, true))).map(_._3).getOrElse(true)
}

```

### 10.5 Foldable data structures

When we’re writing code that needs to process data contained in one of these structures, we often don’t care about the shape of the structure (whether it’s a tree or a list), or whether it’s lazy or not, or provides efficient random access, and so forth.

```
ints.foldRight(0)(_ + _)
```

Trait Foldable
```scala
trait Foldable[F[_]] {
  def foldRight[A,B](as: F[A])(z: B)(f: (A,B) => B): B 
  def foldLeft[A,B](as: F[A])(z: B)(f: (B,A) => B): B
  def foldMap[A,B](as: F[A])(f: A => B)(mb: Monoid[B]): B 
  def concatenate[A](as: F[A])(m: Monoid[A]): A =
              foldLeft(as)(m.zero)(m.op)
}
```

Tree Foldable
```scala
sealed trait Tree[+A]
case class Leaf[A](value: A) extends Tree[A]
case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]

object TreeFoldable extends Foldable[Tree] {
  override def foldMap[A, B](as: Tree[A])(f: A => B)(mb: Monoid[B]): B = as match {
    case Leaf(a) => f(a)
    case Branch(l, r) => mb.op(foldMap(l)(f)(mb), foldMap(r)(f)(mb))
  }
  override def foldLeft[A, B](as: Tree[A])(z: B)(f: (B, A) => B) = as match {
    case Leaf(a) => f(z, a)
    case Branch(l, r) => foldLeft(r)(foldLeft(l)(z)(f))(f)
  }
  override def foldRight[A, B](as: Tree[A])(z: B)(f: (A, B) => B) = as match {
    case Leaf(a) => f(a, z)
    case Branch(l, r) => foldRight(l)(foldRight(r)(z)(f))(f)
  }
}
```

Option Foldable
```scala
object OptionFoldable extends Foldable[Option] {
  override def foldMap[A, B](as: Option[A])(f: A => B)(mb: Monoid[B]): B =
    as match {
      case None => mb.zero
      case Some(a) => f(a)
    }
  override def foldLeft[A, B](as: Option[A])(z: B)(f: (B, A) => B) = as match {
    case None => z
    case Some(a) => f(z, a)
  }
  override def foldRight[A, B](as: Option[A])(z: B)(f: (A, B) => B) = as match {
    case None => z
    case Some(a) => f(a, z)
  }
}
```


Foldable can convert to list
```scala
def toList[A](as: F[A]): List[A] =
  foldRight(as)(List[A]())(_ :: _)
```

### 10.6 Composing Monoids

real power of monoids is the fact that they composes

```scala
def productMonoid[A,B](A: Monoid[A], B: Monoid[B]): Monoid[(A, B)] =
  new Monoid[(A, B)] {
    def op(x: (A, B), y: (A, B)) =
      (A.op(x._1, y._1), B.op(x._2, y._2))
    val zero = (A.zero, B.zero)
  }
```

##### 10.6.1 Assembling more complex monoids

Some data structures form monoids as long as types of elements they contain also form monoids

Example: Merging Key-value Maps
```scala
def mapMergeMonoid[K,V](V: Monoid[V]): Monoid[Map[K,V]] =
    new Monoid[Map[K,V]]{
        def zero = Map[K,V]()
        def op（a: Map[K,V], b: Map[K,V]) =
            (a.keySet ++ b.keySet).foldLeft(zero){ (acc,k) =>
                acc.updated(k, V.op(a.getOrElse(k,V.zero), 
                                    b.getOrElse(k,V.zero)))
            }
    }

val M: Monoid[Map[String, Map[String, Int]]] =
    mapMergeMonoid(mapMergeMonoid(intAddition))

// This allows us to combine nested expressions using the monoid, with no additional programming:

scala> val m1 = Map("o1" -> Map("i1" -> 1, "i2" -> 2))
m1: Map[String,Map[String,Int]] = Map(o1 -> Map(i1 -> 1, i2 -> 2))

scala> val m2 = Map("o1" -> Map("i2" -> 3))
m2: Map[String,Map[String,Int]] = Map(o1 -> Map(i2 -> 3))
 
scala> val m3 = M.op(m1, m2)
m3: Map[String,Map[String,Int]] = Map(o1 -> Map(i1 -> 1, i2 -> 5))
```

Function Monoid
```scala
def functionMonoid[A,B](B: Monoid[B]): Monoid[A => B] =
  new Monoid[A => B] {
    def op(f: A => B, g: A => B) = a => B.op(f(a), g(a))
    val zero: A => B = a => B.zero
  }
```

##### 10.6.2 Using composed monoids to fuse traversals

The fact that multiple monoids can be composed into one means that we can perform multiple calculations simultaneously when folding data structure

take the length & sum of a list at the same time
```scala
val m = productMonoid(intAddition, intAddition)
val p = listFoldable.foldMap(List(1,2,3,4))(a=> (1,a))(m)
(4,10)
```

It is tedious to assemble monoids by hand using productMonoid & foldMap
But we can create a combinator lib to compose monoid and define computation


## 11. Monads

### 11.1 Functors: generalizing the map functions

we implemneted a `map` func for each data type to lift a function taking one argument into the context of some data type

```scala
def map[A,B](ga: Gen[A])(f: A => B): Gen[B]
def map[A,B](pa: Parser[A])(f: A => B): Parser[B] 
def map[A,B](oa: Option[A])(f: A => B): Option[A]

trait Functor[F[_]] {
    def map[A,B](fa: F[A])(f: A => B): F[B]
}

val listFunctor = new Functor[List] {
    def map[A,B](as: List[A])(f: A => B): List[B] = as map f
}
```

- a type constructor like List, is a functor
- Functor[List] instance constitutes proof that List is a functor
- what can we do with this abstraction

example: generic unzip function for any functor
```scala
trait Functor[F[_]] { 
    ...
    def eiw54ibu53[A,B](fab: F[(A, B)]): (F[A], F[B]) = 
        (map(fab)(_._1), map(fab)(_._2))  // or know as unzip

    def codistribute[A,B](e: Either[F[A], F[B]]): F[Either[A, B]] = 
        e match {
            case Left(fa) => map(fa)(Left(_))
            case Right(fb) => map(fb)(Right(_)) 
        }
}
```

#### 11.1.1 Functor laws

- map(x)(a=>a) == x
    + map function preserve the structure of x
    + restrict implementation from doing strange things like
        * throwing exception
        * removing first element of a list
        * converting Some to None

### 11.2 Monads: Generalizing flatMap & Unit functions

map2 for Gen, Parser & Option
```scala
def map2[A,B,C](
        fa: Gen[A], fb: Gen[B])(f: (A,B) => C): Gen[C] =
  fa flatMap (a => fb map (b => f(a,b)))

def map2[A,B,C](
        fa: Parser[A], fb: Parser[B])(f: (A,B) => C): Parser[C] =
  fa flatMap (a => fb map (b => f(a,b)))

def map2[A,B,C](
        fa: Option[A], fb: Option[B])(f: (A,B) => C): Option[C] =
  fa flatMap (a => fb map (b => f(a,b)))

```

#### 11.2.1 Monad trait

- map can be implemented using flatMap & Unit
- all monad are functor
- monad come from category theory

```scala
trait Monad[F[_]] extends Functor[F] {
    def unit[A](a: => A): F[A]
    def flatMap[A,B](ma: F[A])(f: A => F[B]): F[B]

    def map[A,B](ma: F[A])(f: A=>B): F[B] =
        flatMap(ma)( a => unit(f(a)))
    def map2[A,B,C](ma: F[A], mb: F[B])(f: (A,B) => C): F[C] =
        flatMap(ma)(a => map(mb)(b => f(a,b)))
}

object Monad {
  val genMonad = new Monad[Gen] {
    def unit[A](a: =>A): Gen[A] = Gen.unit(a)
    def flatMap[A,B](ma: Gen[A])(f: A => Gen[B]): Gen[B] = 
      ma flatMap f
  }
}

```


Monad for more classes
```scala
val parMonad = new Monad[Par] {
  def unit[A](a: => A) = Par.unit(a)
  def flatMap[A,B](ma: Par[A])(f: A => Par[B]) = Par.flatMap(ma)(f)
}

def parserMonad[P[+_]](p: Parsers[P]) = new Monad[P] {
  def unit[A](a: => A) = p.succeed(a)
  def flatMap[A,B](ma: P[A])(f: A => P[B]) = p.flatMap(ma)(f)
}

val optionMonad = new Monad[Option] {
  def unit[A](a: => A) = Some(a)
  def flatMap[A,B](ma: Option[A])(f: A => Option[B]) = ma flatMap f
}

val streamMonad = new Monad[Stream] {
  def unit[A](a: => A) = Stream(a)
  def flatMap[A,B](ma: Stream[A])(f: A => Stream[B]) = ma flatMap f
}

val listMonad = new Monad[List] {
  def unit[A](a: => A) = List(a)
  def flatMap[A,B](ma: List[A])(f: A => List[B]) = ma flatMap f
}
```

### 11.3 Monadic combinators

sequence & traverse
```scala
def sequence[A](lma: List[F[A]]): F[List[A]] =
  lma.foldRight(unit(List[A]()))((ma, mla) => map2(ma, mla)(_ :: _))

def traverse[A,B](la: List[A])(f: A => F[B]): F[List[B]] =
  la.foldRight(unit(List[B]()))((a, mlb) => map2(f(a), mlb)(_ :: _))

/** 
 * 'Balanced' sequencing, which should behave like `sequence`,
 * but it can use less stack for some data types. We'll see later
 * in this chapter how the monad _laws_ let us conclude both 
 * definitions 'mean' the same thing.
 */
def bsequence[A](ms: Seq[F[A]]): F[IndexedSeq[A]] = {
  if (ms.isEmpty) point(Vector())
  else if (ms.size == 1) ms.head.map(Vector(_))
  else {
    val (l,r) = ms.toIndexedSeq.splitAt(ms.length / 2)
    map2(bsequence(l), bsequence(r))(_ ++ _)
  }
}
```

replicateM
```scala
// Recursive version:
def _replicateM[A](n: Int, ma: F[A]): F[List[A]] =
  if (n <= 0) unit(List[A]()) else map2(ma, replicateM(n - 1, ma))(_ :: _)

// Using `sequence` and the `List.fill` function of the standard library:
def replicateM[A](n: Int, ma: F[A]): F[List[A]] =
  sequence(List.fill(n)(ma))
```

For `List`, generate a list of lists. a length `n` list foreach element selected 

For `Option`, generate  a list of length `n`  either `Some` or `None` based on whether the input is `Some` or `None`

The general meaning of `replicateM` is described well by the implementation `sequence(List.fill(n)(ma))`. It repeats the `ma` monadic value `n` times and gathers the results in a single value, where the monad `F` determines how values are actually combined.


FilterM
```scala
/*
For `Par`, `filterM` filters a list, applying the functions in parallel; 
for `Option`, it filters a list, but allows the filtering function to fail and abort the filter
computation; 
for `Gen`, it produces a generator for subsets of the input list, where the function `f` picks a 
'weight' for each element (in the form of a `Gen[Boolean]`)
*/
def filterM[A](ms: List[A])(f: A => F[Boolean]): F[List[A]] =
  ms match {
    case Nil => unit(Nil)
    case h :: t => flatMap(f(h))(b =>
      if (!b) filterM(t)(f)
      else map(filterM(t)(f))(h :: _))
  }
```

### 11.4 Monad laws

#### 11.4.1 Associative law

- If want to combine 3 monadic values into 1,
    + which 2 should we combine first? 
    + Should it matter?

```scala
case class Order(item: Item, quantity: Int)
case class Item(name: String, price: Double)

val genOrder: Gen[Order] = for {
      name <- Gen.stringN(3)
      price <- Gen.uniform.map(_ * 10)
      quantity <- Gen.choose(1,100)
  } yield Order(Item(name,price), quantity)

// Is it the same as ???
val genItem: Gen[Item] = for {
    name <- Gen.stringN(3)
    price <- Gen.uniform.map(_ * 10)
  } yield Item(name, price)

val genOrder: Gen[Order] = for {
    item <- genItem
    quantity <- Gen.choose(1,100)
  } yield Order(item, quantity)

```

They are the same if we assume associative law
```scala
x.flatMap(f).flatMap(g) == x.flatMap(a=>f(a).flatMap(g))
```

#### 11.4.2 Prove the law

For Option
```scala
x.flatMap(f).flatMap(g)       == x.flatMap(a=>f(a).flatMap(g))
Some(v).flatMap(f).flatMap(g) == Some(v).flatMap(a=>f(a).flatMap(g))
f(v).flatMap(g)               == (a => f(a).flatMap(g)(v))
f(v).flatMap(g)               == f(v).flatMap(g)
```

##### Kleisli composition

Why is this law an associative law?

For monoids, it is clear `op(op(x,y), z) == op(x, op(y,z))`

Use Kleisli composition to make it clear
```scala
def unit[A](a: => A): F[A]
def flatMap[A,B](ma: F[A])(f: A => F[B]): F[B]

def compose[A,B,C](f: A => F[B], g: B => F[C]): A => F[C] =
  a => flatMap(f(a))(g)

def flatMap[A,B](ma: F[A])(f: A => F[B]): F[B] =
  compose((_:Unit) => ma, f)(())
```

another minimal set of combinator: compose and unit


##### two formulation are equivalent


Let's rewrite the following in terms of `flatMap`:

    compose(compose(f, g), h) == compose(f, compose(g, h))
    a => flatMap(compose(f, g)(a))(h) == a => flatMap(f(a))(compose(g, h))
    a => flatMap((b => flatMap(f(b))(g))(a))(h) == a => flatMap(f(a))(b => flatMap(g(b))(h))

So far we have just expanded the definition of `compose`. Equals substituted for equals.
Let's simplify the left side a little:

    a => flatMap(flatMap(f(a))(g))(h) == a => flatMap(f(a))(b => flatMap(g(b))(h))

Let's simplify again by eliminating the `a` argument and substituting a hypothetical value `x` for `f(a)`:

    flatMap(flatMap(x)(g))(h) == flatMap(x)(b => flatMap(g(b))(h))
    flatMap(flatMap(x)(f))(g) == flatMap(x)(a => flatMap(f(a))(g))

Q.E.D.

##### more example

```scala
trait Monad[F[_]] {
  def unit[A](a: => A): F[A]
  def flatMap[A,B](ma: F[A])(f: A => F[B]): F[B]
  def map[A,B](ma: F[A])(f: A=>B): F[B] =
    flatMap(ma)( a => unit(f(a)))
  def compose[A,B,C](f: A => F[B], g: B => F[C]): A => F[C] =
    a => flatMap(f(a))(g)
  def flatMap2[A,B](ma: F[A])(f: A => F[B]): F[B] =
    compose((_:Unit) => ma, f)(())
}

//------------------------------------------------------------------------------

val listMonad = new Monad[List] {
  def unit[A](a: => A) = List(a)
  def flatMap[A,B](ma: List[A])(f: A => List[B]) = ma flatMap f
}

listMonad.unit(1)
//res0: List[Int] = List(1)

listMonad.flatMap(List(1,2,3))(x=>List(x,x))
//res1: List[Int] = List(1, 1, 2, 2, 3, 3)

listMonad.flatMap(List(1,2,3))(x=>listMonad.unit(x+2))
listMonad.map(List(1,2,3))(x => x+2)
//res2: List[Int] = List(3, 4, 5)

val k = listMonad.compose[Int,Int,Int](x=>List(x, x+5), x=>List(x*2))
listMonad.flatMap(List(1,2,3))(k)
//res4: List[Int] = List(2, 12, 4, 14, 6, 16)

listMonad.flatMap2(List(1,2,3))(x=>listMonad.unit(x*10))
listMonad.compose[Unit,Int,Int]((_:Unit)=>List(1,2,3), x=>listMonad.unit(x*10))(())
//res5: List[Int] = List(10, 20, 30)

//------------------------------------------------------------------------------

val optionMonad = new Monad[Option] {
  def unit[A](a: => A) = Some(a)
  def flatMap[A,B](ma: Option[A])(f: A => Option[B]) = ma flatMap f
}

optionMonad.unit(1)
//res7: Some[Int] = Some(1)


optionMonad.flatMap(Some(1))(x=>Some("str" + x.toString))
// res8: Option[String] = Some(str1)

optionMonad.flatMap(Some(1))(x=>optionMonad.unit(x))
optionMonad.map(Some(1))(x => "str" + x.toString)
//res10: Option[String] = Some(str1)

val k3 = optionMonad.compose[Int,Int,String](x=>Some(x*2), x=>Some(x.toString+"s"))
optionMonad.flatMap(Some(5))(k3)
//res11: Option[String] = Some(10s)

optionMonad.flatMap2(Some(7))(x=>optionMonad.unit("["+x.toString+"]"))
optionMonad.compose[Unit,Int,String]((_:Unit)=>Some(7), x=>optionMonad.unit("["+x.toString+"]"))(())
//res12: Option[String] = Some([7])
```

#### 11.4.3 identity laws

- left identity: compose(f, unit) == f, or flatMap(x)(unit) == x
- right identity: compose(unit, f) == f, or flatMap(unit(y))(f) == f(y)

##### third minimal set of monadic combinator

map, unit and join

```scala
def join[A](mma: F[F[A]]): F[A] = flatMap(mma)(ma => ma)
// flatten List(List(a)) => List(a)

def flatMap[A,B](ma: F[A])(f: A => F[B]) =
  join(map(ma)(f))

def compose[A,B,C](f: A => F[B], g: B => F[C]): A => F[C] =
  a => join(map(f(a))(g))
```

##### What associative law means for Parser

We can state the associative law in terms of `join`:

join(join(x)) == join(map(x)(join))

For `Par`, the `join` combinator means something like "make the outer thread wait for the inner one to finish." What this law is saying is that if you have threads starting threads three levels deep, then joining the inner threads and then the outer ones is the same as joining the outer threads and then the inner ones.

For `Parser`, the `join` combinator is running the outer parser to produce a `Parser`, then running the inner `Parser` _on the remaining input_. The associative law is saying, roughly, that only the _order_ of nesting matters, since that's what affects the order in which the parsers are run.

##### What identity law means for Gen & List

Recall the identity laws:

left identity:  flatMap(unit(x))(f) == f(x)
right identity: flatMap(x)(unit)    == x

The left identity law for `Gen`:
The law states that if you take the values generated by `unit(x)` (which are always `x`) and apply `f` to those values, that's exactly the same as the generator returned by `f(x)`.

The right identity law for `Gen`:
The law states that if you apply `unit` to the values inside the generator `x`, that does not in any way differ from `x` itself.

The left identity law for `List`:
The law says that wrapping a list in a singleton `List` and then flattening the result is the same as doing nothing.

The right identity law for `List`:
The law says that if you take every value in a list, wrap each one in a singleton `List`, and then flatten the result, you get the list you started with.


### 11.5 Just what is a Monad

Monad, like Monoid, is a more abstract, purely algebraic interface

Monad does not generalize one type, it support vastly different types

** Monad is an implementation of one of the minimal sets of monadic combinators, satisfying the laws of associativity and identity **

- It is the only correct definition
- Monad is precisely defined by its operation & laws, no more, no less
- It is a self-contained definition
- look at more monads and connect them to a wider context

#### 11.5.1 The identity monad

simplest interesting specimen, the identity monad

```scala
case class Id[A](value: A) {
  def map[B](f: A => B): Id[B] = Id(f(value))
  def flatMap[B](f: A => Id[B]): Id[B] = f(value)
}

object Id {
  val idMonad = new Monad[Id] {
    def unit[A](a: => A) = Id(a)
    def flatMap[A,B](ida: Id[A])(f: A => Id[B]): Id[B] = ida flatMap f
  }
}
```

what is the meaning of the identity monad?

```scala
scala> Id("Hello, ") flatMap (a => Id("monad!") flatMap (b => Id(a + b)))
scala> for {a <- Id("Hello, ") b <- Id("monad!")} yield a + b
res0: Id[java.lang.String] = Id(Hello, monad!)
// simply varaible substitution
```

Monads provide a context for introducing and binding variables, 
and performing variable substitution

#### 11.5.2 The State Monad and partial type application

```scala
case class State[S, A](run: S => (A, S)) {
  def map[B](f: A => B): State[S, B] =
    State(s => {
      val (a, s1) = run(s)
      (f(a), s1)
    })

  def flatMap[B](f: A => State[S, B]): State[S, B] =
    State(s => {
      val (a, s1) = run(s)
      f(a).run(s1)
    })
}
```

State definitely fits the profile for being a monad, except type constructor takes two arguments

We would partially apply State to where the S type argument is fixed to be some concrete type

```scala
type IntState[A] = State[Int, A]
object IntStateMonad extends Monad[IntState] {
  def unit[A](a: => A): IntState[A] = State(s => (a, s))
  def flatMap[A,B](st: IntState[A])(f: A => IntState[B]): IntState[B] =
    st flatMap f
}
```

IntState is exactly the thing we can build a monad for.

Scala don't allow `State[Int, _]` to create anonymous constructor, but we can use lambda syntax at type level    
- declaring an anonymous type within parentheses.
- This anonymous type has member: the type alias IntState 
- Outside the parentheses we’re then accessing its IntState member with the # syntax. 
- Just like we can use a dot (.) to access a member of an object at the value level, we can use the # symbol to access a type member (type projection)

```scala
object IntStateMonad extends
  Monad[({type IntState[A] = State[Int, A]})#IntState] {
  ...
}
```

- type lambda
  + a type constructor declared inline like this
  + allow us to partially apply `State` and declare `StateMonad` trait

```scala
def stateMonad[S] = new Monad[({type f[x] = State[S,x]})#f] {
  def unit[A](a: => A): State[S,A] = State(s => (a, s))
  def flatMap[A,B](st: State[S,A])(f: A => State[S,B]): State[S,B] =
    st flatMap f
}
```

#### 11.5.3 Difference between monads

- primitive operations on State
  + getState[S]: State[S:S]
  + setState[s :=> S]: State[S, Unit]

What laws mutually hold for getState, setState, unit and flatMap?

```scala
// Getting and setting the same state does nothing:
getState.flatMap(setState) == unit(())

// written as for-comprehension:
for {
  x <- getState
  _ <- setState(x)
} yield ()

// Setting the state to `s` and getting it back out yields `s`.
setState(s).flatMap(_ => getState) == unit(s)

// alternatively:
for {
  _ <- setState(s)
  x <- getState
} yield x
```

What does this tell us about the meaning of the State monad?

```scala
val F = stateMonad[Int]
def zipWithIndex[A](as: List[A]): List[(Int,A)] =
  as.foldLeft(F.unit(List[(Int, A)]()))((acc,a) => for {
    xs <- acc
    n <- getState
    _ <- setState(n + 1)
} yield (n, a) :: xs).run(0)._1.reverse
```

this function numbers all elements in a list using a state action, keep a state (int)

- We are obviously getting variable binding like in Id monad
- But there are more going on, flatMap making sure current setState available to getState
- We can see a chain of flatMap calls, or for comprehension
- like imperative program with statements that assign to variable
  + Id, nothing occur except wrapping and unwrapping
  + State, current state passed from one statement to another
  + Option, state may return None and terminate program
  + List, statement may return many result & run many times

#### 11.5.4 Reader Monad

```scala
case class Reader[R, A](run: R => A)

object Reader {
  def readerMonad[R] = new Monad[({type f[x] = Reader[R,x]})#f] {
    def unit[A](a: => A): Reader[R,A] = 
      Reader(_ => a)

    def flatMap[A,B](st: Reader[R,A])(f: A => Reader[R,B]): Reader[R,B] =
      Reader(r => f(st.run(r)).run(r))
  }

  // A primitive operation for it would be simply to ask for the `R` argument:
  def ask[R]: Reader[R, R] = Reader(r => r)
}
```

The action of Reader's `flatMap` is to pass the `r` argument along to both the
outer Reader and also to the result of `f`, the inner Reader. Similar to how
`State` passes along a state, except that in `Reader` the "state" is read-only.

The meaning of `sequence` here is that if you have a list of functions, you can
turn it into a function that takes one argument and passes it to all the functions
in the list, returning a list of the results.

The meaning of `join` is simply to pass the same value as both arguments to a
binary function.

The meaning of `replicateM` is to apply the same function a number of times to
the same argument, returning a list of the results. Note that if this function
is _pure_, (which it should be), this can be exploited by only applying the
function once and replicating the result instead of calling the function many times.
This means the Reader monad can override replicateM to provide a very efficient
implementation.

## 12. Applicative & Traversable functors

- applicative functors
  + less powerful than monads
  + more general & more common

### 12.1 Generalizing monads

```scala
def sequence[A](lfa: List[F[A]]): F[List[A]] = 
  traverse(lfa)(fa=>fa)

def traverse[A,B](as: List[A])(f: A=>F[B]): F[List[B]] =
  as.foldRight(unit(List[B]()))( (a,mbs) => map2(f(a), mbs)(_ :: _) )
```

- traverse combinator does not call flatMap directly, instead only map2 & unit
- for many data type, map2 can be implemented directly without flatMap

```py
fmap :: (a -> b) -> f a -> f b
<*> :: f (a -> b) -> f a -> f b
(>>=) :: (a -> m b) -> m a -> m b

def map[B](fa: F[A])(f: A => B): F[B]
def map2[A,B,C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C]
def flatMap[A,B](ma: M[A])(f: A => M[B]): M[B]
```

### 12.2 the applicative trait

```scala
trait Applicative[F[_]] extends Functor[F] {
  // primitive combinators
  def map2[A,B,C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C]
  def unit[A](a: => A): F[A]

  // derived combinators
  def map[B](fa: F[A])(f: A => B): F[B] =
    map2(fa, unit(()))((a, _) => f(a))

  def traverse[A,B](as: List[A])(f: A => F[B]): F[List[B]]
    as.foldRight(unit(List[B]()))((a, fbs) => map2(f(a), fbs)(_ :: _))

  def sequence[A](fas: List[F[A]]): F[List[A]] =
    traverse(fas)(fa => fa)

  def replicateM[A](n: Int, fa: F[A]): F[List[A]] =
    sequence(List.fill(n)(fa))

  def product[A,B](fa: F[A], fb: F[B]): F[(A,B)] =
    map2(fa, fb)((_,_))
}
```

alternate primitives: unit & apply

`map2` is implemented by first currying `f` so we get a function of type `A => B => C`. This is a function that takes `A` and returns another function of type `B => C`. So if we map `f.curried` over an `F[A]`, we get `F[B => C]`. Passing that to `apply` along with the `F[B]` will give us the desired `F[C]`.

```scala
trait Applicative[F[_]] extends Functor[F] {
  def map2[A,B,C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C] = 
    apply(map(fa)(f.curried), fb)

  def apply[A,B](fab: F[A => B])(fa: F[A]): F[B] =
    map2(fab, fa)(_(_))

  def unit[A](a: => A): F[A]
  
  def map[A,B](fa: F[A])(f: A => B): F[B] =
    apply(unit(f))(fa)
}
```

apply method is useful for implementing map3 & map4
```scala

def map3[A,B,C,D](fa: F[A],
                  fb: F[B],
                  fc: F[C])(f: (A, B, C) => D): F[D] =
  apply(
    apply(
      apply(
        unit(f.curried)
      )(fa)
    )(fb)
  )(fc)

def map4[A,B,C,D,E](fa: F[A],
                    fb: F[B],
                    fc: F[C],
                    fd: F[D])(f: (A, B, C, D) => E): F[E] =
  apply(
    apply(
      apply(
        apply(
          unit(f.curried)
        )(fa)
      )(fb)
    )(fc)
  )(fd)
```

Monad can be a subtype of Applicative, given one of flatMap, join or map
```scala
trait Monad[F[_]] extends Applicative[F] {
  def flatMap[A,B](fa: F[A])(f: A => F[B]): F[B] = join(map(fa)(f))
  def join[A](ffa: F[F[A]]): F[A] = flatMap(ffa)(fa => fa)
  def compose[A,B,C](f: A => F[B], g: B => F[C]): A => F[C] = 
    a => flatMap(f(a))(g)
  def map[B](fa: F[A])(f: A => B): F[B] =
    flatMap(fa)((a: A) => unit(f(a)))
  def map2[A,B,C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C] =
    flatMap(fa)(a => map(fb)(b => f(a,b)))
}
```

### 12.3 difference between monads & applicative functors

- look at join (`F[F[A]] -> A`) which removes a layer of f
  + unit, add an F layer
  + map2, apply a function within F

#### 12.3.1 Option applicative vs Option Monad
```scala
val F: Applicative[Option]
val o: Option[String] = F.map2(dept.get("Alice"), salaries.get("Alice"))(
    (dept, salary) => s"Alice in $dept makes $salary per year"
  )

// what if we want result of one look up affect next lookup?
val o: Option[String] = 
  idsByName.get("Bob").flatMap { id =>
      F.map2(dept.get(id), salaries.get(id))(
        (dept, salary) => s"Bob in $dept makes $salary per year"
  }
```

- with Applicative, the structure of our computation is fixed
- with Monad, the results of previous computations may influence what computations to run next.

#### 12.3.2 Par applicative vs Par Monad

```
1/1/2010, 25
2/1/2010, 28
3/1/2010, 42
4/1/2010, 53
```

If we know ahead we have date & temperature cols:
```scala
case class Row(date: Date, temperature: Double)
val d: Parser[Date]
val temp: Parser[Double]
val row : Parser[Row] = F.map2(d, temp)(Row(_,_))
val rows: Parser[List[Row]] = row.sep("\n")
```

If we don't know? Map first line a parser of parser
```
# Temperature, Date
25, 1/1/2010
28, 2/1/2010
42, 3/1/2010
53, 4/1/2010
```


```scala
case class Row(date: Date, temperature: Double)
val F: Monad[Parser] = ...
val d: Parser[Date] = ...
val temp: Parser[Double] = ...
val header: Parser[Parser[Row]] = ...
val rows: Parser[List[Row]] = F.flatMap (header) { row => row.sep("\n") }
```

use the first parser to parse the rest dynamically

#### conclusion

- Applicative computations have fixed structure and simply sequence effects,
whereas monadic computations may choose structure dynamically, based on
the result of previous effects.
- Applicative constructs context-free computations, while Monad allows for context
sensitivity.
- Monad makes effects first class; they may be generated at “interpretation” time,
rather than chosen ahead of time by the program. We saw this in our Parser
example, where we generated our Parser[Row]as part of the act of parsing, and
used this Parser[Row] for subsequent parsing.

### 12.4 advantages of applicative functors

- No need to implement traverse on data type support map2 than flatMap
- Weaker, thus gives interpreter of applicative effect more flexibility
- Applicative functors compose, whereas monads in genenral do not

infinite stream example
```scala
val streamApplicative = new Applicative[Stream] {
  def unit[A](a: => A): Stream[A] =
      Stream.continually(a)
  def map2[A,B,C](a: Stream[A], b: Stream[B])(f: (A,B) => C): Stream[C] =
      a zip b map f.tupled
}
```

call sequence to transposes the matrix: 1xn -> nx1

validation: an either variant that accumulates error
```scala
def eitherMonad[E]: Monad[({type f[x] = Either[E, x]})#f] =
  new Monad[({type f[x] = Either[E, x]})#f] {
    def unit[A](a: => A): Either[E, A] = Right(a)
    def flatMap[A,B](eea: Either[E, A])(f: A => Either[E, B]) = eea match {
      case Right(a) => f(a)
      case Left(e) => Left(e)
    }
  }
```

Consider a sequence of flatMap validation call for lazy execution
```scala
validName(field1) flatMap (f1 =>
  validBirthdate(field2) flatMap (f2 =>
    validPhone(field3) map (f3 => WebForm(f1, f2, f3))
//or
map3(
  validName(field1),
  validBirthdate(field2),
  validPhone(field3))(
    WebForm(_,_,_))
```

What if we want to collect more errors?
Invent a new data type Validation

```scala
def validationApplicative[E]: Applicative[({type f[x] = Validation[E,x]})#f] =
  new Applicative[({type f[x] = Validation[E,x]})#f] {
    def unit[A](a: => A) = Success(a)

    override def map2[A,B,C](fa: Validation[E,A], fb: Validation[E,B])(f: (A, B) => C) =
      (fa, fb) match {
        case (Success(a), Success(b)) => Success(f(a, b))
        case (Failure(h1, t1), Failure(h2, t2)) =>
          Failure(h1, t1 ++ Vector(h2) ++ t2)
        case (e@Failure(_, _), _) => e
        case (_, e@Failure(_, _)) => e
      }
  }
```

use this for web form validation
```scala
def validName(name: String): Validation[String, String] =
  if (name != "") Success(name)
  else Failure("Name cannot be empty")

def validBirthdate(birthdate: String): Validation[String, Date] =
  try {
    import java.text._
    Success((new SimpleDateFormat("yyyy-MM-dd")).parse(birthdate))
  } catch {
    Failure("Birthdate must be in the form yyyy-MM-dd")
  }

def validPhone(phoneNumber: String): Validation[String, String] =
  if (phoneNumber.matches("[0-9]{10}")) Success(phoneNumber)
  else Failure("Phone number must be 10 digits")

def validWebForm(name: String, birthdate: String, phone: String): Validation[String, WebForm] =
  map3(
    validName(name),
    validBirthdate(birthdate),
    validPhone(phone))(
      WebForm(_,_,_))
```


### 12.5 applicative laws

- Identity
    + map(v)(id) == v
    + map(map(v)(g))(f) == map(v)(f compose g)
- Associative
    + op(a, op(b, c)) == op(op(a, b), c)
    + compose(f, op(g, h)) == compose(compose(f, g), h)
    + product(product(fa,fb),fc) == map(product(fa, product(fb,fc)))(assoc)
- Naturality
    + map2(a,b)(productF(f,g)) == product(map(a)(f), map(b)(g))

### 12.6 traversable functors

As traverse and sequence didn't depend directly on flatMap, we can spot another abstraction by generalizing traverse & sequence once again.

```scala
def traverse[F[_],A,B](as: List[A])(f: A => F[B]): F[List[B]]
def sequence[F[_],A](fas: List[F[A]]): F[List[A]]
```

Sequence over a Map
```scala
def sequenceMap[K,V](ofa: Map[K,F[V]]): F[Map[K,V]] =
    (ofa foldLeft unit(Map.empty[K,V])) { case (acc, (k, fv)) =>
      map2(acc, fv)((m, v) => m + (k -> v))
    }
```

Traverse Interface
- sequence swaps application order
```scala
trait Traverse[F[_]] {
  def traverse[G[_]:Applicative,A,B](fa: F[A])(f: A => G[B]): G[F[B]] =
    sequence(map(fa)(f))
  def sequence[G[_]:Applicative,A](fga: F[G[A]]): G[F[A]] =
    traverse(fga)(ga => ga)
}
```

A few instance of traverse
```scala
val listTraverse = new Traverse[List] {
  override def traverse[G[_],A,B](as: List[A])(f: A => G[B])(implicit G: Applicative[G]): G[List[B]] =
    as.foldRight(G.unit(List[B]()))((a, fbs) => G.map2(f(a), fbs)(_ :: _))
}

val optionTraverse = new Traverse[Option] {
  override def traverse[G[_],A,B](oa: Option[A])(f: A => G[B])(implicit G: Applicative[G]): G[Option[B]] =
    oa match {
      case Some(a) => G.map(f(a))(Some(_))
      case None    => G.unit(None)
    }
}

val treeTraverse = new Traverse[Tree] {
  override def traverse[G[_],A,B](ta: Tree[A])(f: A => G[B])(implicit G: Applicative[G]): G[Tree[B]] =
    G.map2(
      f(ta.head), 
      listTraverse.traverse(ta.tail)(a => traverse(a)(f))
    )(Tree(_, _))
}

```

A traverse is similar to fold in that both take some data structure and apply a function to data within in order to produce a result

Traverse preserves the original structure (reverse F,G)
foldMap discards the structure and replaces it with operation of monoid 

### 12.7 use of traverse

```scala
trait Traverse[F[_]] extends Functor[F] {
  def traverse[G[_],A,B](fa: F[A])(f: A => G[B])(implicit G: Applicative[G]): G[F[B]] =
      sequence(map(fa)(f))
  
  def sequence[G[_],A](fga: F[G[A]])(implicit G: Applicative[G]): G[F[A]] =
      traverse(fga)(ga => ga)
  
  // implement map from traverse
  // use simplest Applicative Id:
  type Id[A] = A
  val idMonad = new Monad[Id] {
    def unit[A](a: => A) = a
    override def flatMap[A,B](a: A)(f: A => B): B = f(a)
  }

  def map[A,B](fa: F[A])(f: A => B): F[B] = 
      traverse[Id, A, B](fa)(f)(idMonad)
}
```

#### 12.7.1 From Monoids to Applicative functors

```scala
def traverse[G[_]:Applicative,A,B](fa: F[A])(f: A => G[B]): G[F[B]]
// if G is a type constructor ConstInt, take any type to Int
type ConstInt[A] = Int
// use it to Init traverse
def traverse[A,B](fa: F[A]))(f: A => Int): Int
// look like foldMap from Foldable, but still need a way to combine Int values
// which is a Monoid[Int]
```

- Monoid[Int] + Functor[A] = Int => Applicative
- Given a constant functor, we can turn any Monoid into an Applicative

Monoid Lifter
```scala
type Const[M, B] = M

implicit def monoidApplicative[M](M: Monoid[M]) =
  new Applicative[({ type f[x] = Const[M, x] })#f] {
    def unit[A](a: => A): M = M.zero
    def map2[A,B,C](m1: M, m2: M)(f: (A,B) => C): M = M.op(m1,m2)
  }

```

now Traverse can extend Foldable and we can give a default implementation of foldMap in terms of traverse.

```scala
trait Traverse[F[_]] extends Functor[F] with Foldable[F] {
  ...
  def foldMap[A,M](as: F[A])(f: A => M)(mb: Monoid[M]): M =
    traverse[({type f[x] = Const[M,x]})#f,A,Nothing](
      // Scala can’t infer the partially applied Const type alias here, 
      // so we have to provide an annotation.
      as)(f)(monoidApplicative(mb))
}
```

Note that Traverse now extends both Foldable and Functor! 
Importantly, Foldable itself can’t extend Functor. 
It's because `foldRight`, `foldLeft`, and `foldMap` don't give us any way of constructing a value of the foldable type.

Even though it’s possible to write map in terms of a fold for
most foldable data structures like List, it’s not possible in general.

example of Foldable that is not a Functor
```scala
    case class Iteration[A](a: A, f: A => A, n: Int) {
      def foldMap[B](g: A => B)(M: Monoid[B]): B = {
        def iterate(n: Int, b: B, c: A): B =
          if (n <= 0) b else iterate(n-1, g(c), f(a))
        iterate(n, M.zero, a)
      }
    }
```

#### 12.7.2 Traversal with State

Using a State action to traverse a collection, we can implement complex taverse with internal state
```scala
def traverseS[S,A,B](fa: F[A])(f: A => State[S, B]): State[S, F[B]] =
  traverse[({type f[x] = State[S,x]})#f,A,B](fa)(f)(Monad.stateMonad)

def zipWithIndex[A](ta: F[A]): F[(A,Int)] =
  traverseS(ta)((a: A) => (for {
    i <- get[Int]
    _ <- set(i + 1)
  } yield (a, i))).run(0)._1

def toList[A](fa: F[A]): List[A] =
  traverseS(fa)((a: A) => (for {
    as <- get[List[A]]
    _ <- set(a :: as)
  } yield ()).run(Nil)._2.reverse
```

all the traverseS have mapAccumulate pattern, we can factor it out
```scala
def mapAccum[S,A,B](fa: F[A], s: S)(f: (A, S) => (B, S)): (F[B], S) =
  traverseS(fa)((a: A) => (for {
    s1 <- get[S]
    (b, s2) = f(a, s1)
    _ <- set(s2)
  } yield b)).run(s)

override def toList[A](fa: F[A]): List[A] =
  mapAccum(fa, List[A]())((a, s) => ((), a :: s))._2.reverse

def zipWithIndex[A](fa: F[A]): F[(A, Int)] =
  mapAccum(fa, 0)((a, s) => ((a, s), s + 1))._1

def reverse[A](fa: F[A]): F[A] =
  mapAccum(fa, toList(fa).reverse)((_, as) => (as.head, as.tail))._1

override def foldLeft[A,B](fa: F[A])(z: B)(f: (B, A) => B): B =
  mapAccum(fa, z)((a, b) => ((), f(b, a)))._2
```

#### 12.7.3 Combining Traversable structures

Zip

```scala
def zip[A,B](fa: F[A], fb: F[B]): F[(A, B)] =
  (mapAccum(fa, toList(fb)) {
    case (a, Nil) => sys.error("zip: Incompatible shapes.")
    case (a, b :: bs) => ((a, b), bs)
  })._1

def zipL[A,B](fa: F[A], fb: F[B]): F[(A, Option[B])] =
  (mapAccum(fa, toList(fb)) {
    case (a, Nil) => ((a, None), Nil)
    case (a, b :: bs) => ((a, Some(b)), bs)
  })._1

def zipR[A,B](fa: F[A], fb: F[B]): F[(Option[A], B)] =
  (mapAccum(fb, toList(fa)) {
    case (b, Nil) => ((None, b), Nil)
    case (b, a :: as) => ((Some(a), b), as)
  })._1
```

#### 12.7.4 Traversal Fusion
```scala
def fuse[G[_],H[_],A,B](fa: F[A])(f: A => G[B], g: A => H[B])
                       (implicit G: Applicative[G], H: Applicative[H]): (G[F[B]], H[F[B]]) =
  traverse[({type f[x] = (G[x], H[x])})#f, A, B](fa)(a => (f(a), g(a)))(G product H)
```

#### 12.7.5 Nested Traversal
If we have a nested structure like `Map[K,Option[List[V]]]`, then we can traverse the map, the option, and the list at the same time and easily get to the V value inside, because Map, Option, and List are all traversable.
```scala
def compose[G[_]](implicit G: Traverse[G]): Traverse[({type f[x] = F[G[x]]})#f] =
  new Traverse[({type f[x] = F[G[x]]})#f] {
    override def traverse[M[_]:Applicative,A,B](fa: F[G[A]])(f: A => M[B]) =
      self.traverse(fa)((ga: G[A]) => G.traverse(ga)(f))
  }
```

#### 12.7.6 Monad Composition
the composition of two monads where one of them is traversable
```scala
def composeM[G[_],H[_]](implicit G: Monad[G], H: Monad[H], T: Traverse[H]):
  Monad[({type f[x] = G[H[x]]})#f] = new Monad[({type f[x] = G[H[x]]})#f] {
    def unit[A](a: => A): G[H[A]] = G.unit(H.unit(a))
    override def flatMap[A,B](mna: G[H[A]])(f: A => G[H[B]]): G[H[B]] =
      G.flatMap(mna)(na => G.map(T.traverse(na)(f))(H.join))
  }
```

Expressivity and power sometimes come at the price of compositionality and modularity.
The issue of composing monads is often addressed with a custom-written version
of each monad that’s specifically constructed for composition. This kind of thing is
called a monad transformer.

There’s no generic composition strategy that works for every monad.