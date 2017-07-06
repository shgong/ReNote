# Intro to Functional

## 1. What is FP

- we construct our programs using only pure function
    + functions that have no side effect
        * side effect: do other thing than return a result
            - modify variable/data structure/field
            - throw exception
            - print to console/ file io
    + FP is restriction on how we write, not what we write

Side effect example
```scala
class Cafe {
    def buyCoffee(cc:CreditCard): Coffee = {
        val cup = new Coffeee()
        cc.charge(cup.price) // side effect, actually charge credit card
        // this make code difficult to test as involve external interaction
    }
    cup
}
// add a payment object
class Cafe{
    def buyCoffee(cc:CreditCard, p:Payments):Coffee = {
        val cup = new Coffee()
        p.charge(cc, cup.price)
        cup
    }
}
// Payment can be a interface, easy to do mock implementation
// but still difficult to reuse buyCoffee
class Cafe{
    def buyCoffee(cc:CreditCard):(Cofee,Charge) = {
        val cup = new Coffee()
        (cup, Charge(cc,cup.price))
    }

    def buyCoffees(cc:CreditCard, n:Int):(List[Coffee],Charge) = {
        val purchases: List[(Coffee,Charge)]: List.fill(n)(buyCoffee(cc))
        val (coffees,charges) = purchases.unzip
            (coffees,charges.reduce(c1,c2)=>c1.combine(c2))
    }
}
// separate creation & processing of charge
case class Charge(cc:CreditCard, amount:Double){
    def combine(other:Charge):Charge =
        if (cc==other.cc)
            Charge(cc,amount+other.amount)
        else
            throw new Exception("Can't combine charges to different cards")
    }
```

- Referential transparency, purity, and substitution model
- Referential transparency
    + forced invariant that everything function does is represented by the value that it returns
    + enable simple & natural mode of reasoning, like solve algebraic equation
- Substitution model
    + not mentally track all the state change
    + only require local reasoning
    + funcitonal programming is more modular

```scala
val x = "Helllo, World"
val r1= x.reverse
val r2= x.reverse

val x = new StringBuilder("Hello, ")
val r1 = x.append("World").toString
val r2 = x.append("World").toString
// Not the SAME, StringBuilder.append() is not pure function
```

## 2. Scala Language

- Writing Loop Functionally
    + inner function for recursion
    + just like normal local integers or strings
- tail recursion
    + scala detect self recursion and compile to same bytecode as while loop
    + as long as recursive call in tail position


```scala
def factorial(n: Int): Int = {
    @annotation.tailrec
    def go(n: Int, acc: Int): Int =
        if (n <= 0) acc
        else go(n-1, n*acc)
    go(n, 1)
}
```

- High Order Functions
    + passing function to functions

```scala
def formatResult(name: String, n: Int, f: Int => Int) = {
    val msg = "The %s of %d is %d."
    msg.format(name, n, f(n))
}
```

- Polymorphic functions
    + abstracting over type
    + generic function: abstracting over the type

Monomorphic func to find String in an array
```scala
def findFirst(ss: Array[String], key: String): Int = {
    @annotation.tailrec
    def loop(n: Int): Int =
        if (n >= ss.length) -1
        else if (ss(n) == key) n
        else loop(n + 1)
    loop(0)
}
```

Polymorphic
```scala
def findFirst[A](as: Array[A], p: A => Boolean): Int = {
    @annotation.tailrec
    def loop(n: Int): Int =
        if (n >= as.length) -1
        else if (p(as(n))) n
        else loop(n + 1)
    loop(0)
}
```

- Partially Applied Function
```scala
def partial1[A,B,C](a: A, f: (A,B) => C): B => C =
    (b: B) => f(a, b)

def curry[A,B,C](f: (A, B) => C): A => (B => C) =
  a => b => f(a, b)

def uncurry[A,B,C](f: A => B => C): (A, B) => C =
  (a, b) => f(a)(b)

def compose[A,B,C](f: B => C, g: A => B): A => C =
  a => f(g(a))
```


## 3. Functional Data Structure

### 3.1  data structure
- functional
    + oprated on only pure functions
    + by definition immutable
    + empty list, List() or Nil is as eternal and immutable as constant value
- List Data Type in Scala
    + List("a","b")
    + Cons("a",Cons("b",Nil))

```scala

sealed trait List[+A]
// +A means A is covariant, or positive parameter
case object Nil extends List[Nothing]
// Nil extends Nothing, Nothing is subtype of anything
case class Cons[+A](head: A, tail: List[A]) extends List[A]
// non-empty list constructor, tail is another list, maybe Nil or Cons

// pattern matching, like a fancy switch statement
object List {
  def sum(ints: List[Int]): Int = ints match {
    case Nil => 0
    case Cons(x,xs) => x + sum(xs)
  }
  def product(ds: List[Double]): Double = ds match {
    case Nil => 1.0
    case Cons(0.0, _) => 0.0
    case Cons(x,xs) => x * product(xs)
  }

  // Variadic function, accept zero or more
  // recursive composition of A
  def apply[A](as: A*): List[A] =
    if (as.isEmpty) Nil
    else Cons(as.head, apply(as.tail: _*))
    // For datatypes, common to have variadic apply in companion object
    // to conveniently construct instance
}
```

### 3.3 Data Sharing in Functional Data Structures
implement more list functions

```scala
def tail[A](l: List[A]): List[A] = 
  l match {
    case Nil => sys.error("tail of empty list")
    case Cons(_,t) => t
  }

def setHead[A](l: List[A], h: A): List[A] = l match {
  case Nil => sys.error("setHead on empty list")
  case Cons(_,t) => Cons(h,t)
}

def drop[A](l: List[A], n: Int): List[A] = 
  if (n <= 0) l
  else l match {
    case Nil => Nil
    case Cons(_,t) => drop(t, n-1) 
  }

def dropWhile[A](l: List[A], f: A => Boolean): List[A] = 
  l match {
    case Cons(h,t) if f(h) => dropWhile(t, f) 
    case _ => l
  }

def append[A](a1: List[A], a2: List[A]): List[A] =
  a1 match {
      case Nil => a2
      case Cons(h,t) => Cons(h, append(t, a2))
}

```

### 3.4 Recursion over lists and generalizing to high order function

- product & sum function for Double & Int
    + similar definition, only different type & operator
- Fold
    + foldRight(list, z)(f)
    + replace the constructor of list, Nil & Cons with z & f
        * `Cons(1, Cons(2, Nil)`
        * `f   (1,    f(2, z  )`
    + `(List(a, b, c) :\ z) (op)` 
    + more efficient than FoldLeft 

```scala
def foldRight[A,B](as: List[A], z: B)(f: (A, B) => B): B = as match {
        case Nil => z
        case Cons(x, xs) => f(x, foldRight(xs, z)(f))
    }

def sum2(ns: List[Int]) = foldRight(ns, 0)((x,y) => x + y)
def product2(ns: List[Double]) = foldRight(ns, 1.0)(_ * _)
def length[A](l: List[A]): Int = foldRight(l, 0)((_,acc) => acc + 1)

```

- foldRight is not tail recursive
    + result in StackOverflowError for large list
    + another general list-recursion foldLeft
- foldLeft
    + z, (x,xs) => f(z,x), xs
    + recursive

```scala
@annotation.tailrec
def foldLeft[A,B](l: List[A], z: B)(f: (B, A) => B): B = l match { 
  case Nil => z
  case Cons(h,t) => foldLeft(t, f(z,h))(f)
}

def sum3(l: List[Int]) = foldLeft(l, 0)(_ + _)
def product3(l: List[Double]) = foldLeft(l, 1.0)(_ * _)
def length2[A](l: List[A]): Int = foldLeft(l, 0)((acc,h) => acc + 1)


def reverse[A](l: List[A]): List[A] = 
    foldLeft(l, List[A]())((acc,h) => Cons(h,acc))

// implement foldRight with foldLeft, make it available for tailrecursion
def foldRightViaFoldLeft[A,B](l: List[A], z: B)(f: (A,B) => B): B = 
  foldLeft(reverse(l), z)((b,a) => f(a,b))

def appendViaFoldRight[A](l: List[A], r: List[A]): List[A] = 
  foldRight(l, r)(Cons(_,_))
```

### More function for working with list 

```scala
def add1(l: List[Int]): List[Int] = 
  foldRight(l, Nil:List[Int])((h,t) => Cons(h+1,t))

def doubleToString(l: List[Double]): List[String] = 
  foldRight(l, Nil:List[String])((h,t) => Cons(h.toString,t))

def map[A,B](l: List[A])(f: A => B): List[B] = 
  foldRight(l, Nil:List[B])((h,t) => Cons(f(h),t)) 
  // or via FoldLeft
def map_1[A,B](l: List[A])(f: A => B): List[B] = 
  foldRightViaFoldLeft(l, Nil:List[B])((h,t) => Cons(f(h),t))

def filter[A](l: List[A])(f: A => Boolean): List[A] = 
  foldRight(l, Nil:List[A])((h,t) => if (f(h)) Cons(h,t) else t)

def zipWith[A,B,C](a: List[A], b: List[B])(f: (A,B) => C): List[C] = (a,b) match {
  case (Nil, _) => Nil
  case (_, Nil) => Nil
  case (Cons(h1,t1), Cons(h2,t2)) => Cons(f(h1,h2), zipWith(t1,t2)(f))
}
```

- Difference between List & Standard Lib List
    + Cons is called `::`, associate to the right
    + `1::2::Nil` equal to `1::(2::Nil)`
    + avoid nested parentheses when pattern matching
- other useful methods
    + def take(n: Int): List[A]
    + def takeWhile(f: A => Boolean): List[A]
    + def forall(f: A => Boolean): Boolean
    + def exists(f: A => Boolean): Boolean

### 3.5 Trees

```scala
sealed trait Tree[+A]
case class Leaf[A](value: A) extends Tree[A]
case class Branch[A](left: Tree[A], right: Tree[A]) extends Tree[A]
```

Tree functions

```scala
def size[A](t: Tree[A]): Int = t match {
  case Leaf(_) => 1
  case Branch(l,r) => 1 + size(l) + size(r)
}

def maximum(t: Tree[Int]): Int = t match {
  case Leaf(n) => n
  case Branch(l,r) => maximum(l) max maximum(r)
}

def depth[A](t: Tree[A]): Int = t match {
  case Leaf(_) => 0
  case Branch(l,r) => 1 + (depth(l) max depth(r))
}

def map[A,B](t: Tree[A])(f: A => B): Tree[B] = t match {
  case Leaf(a) => Leaf(f(a))
  case Branch(l,r) => Branch(map(l)(f), map(r)(f))
}


// can also implement via fold
def fold[A,B](t: Tree[A])(f: A => B)(g: (B,B) => B): B = t match {
  case Leaf(a) => f(a)
  case Branch(l,r) => g(fold(l)(f)(g), fold(r)(f)(g))
}

def sizeViaFold[A](t: Tree[A]): Int = 
  fold(t)(a => 1)(1 + _ + _)

def maximumViaFold(t: Tree[Int]): Int = 
  fold(t)(a => a)(_ max _)

def depthViaFold[A](t: Tree[A]): Int = 
  fold(t)(a => 0)((d1,d2) => 1 + (d1 max d2))

def mapViaFold[A,B](t: Tree[A])(f: A => B): Tree[B] = 
  fold(t)(a => Leaf(f(a)): Tree[B])(Branch(_,_))
```

## 4. Error without Exception

- If exceptions aren't used in functional code, what to use instead?
    + Option & Either

### 4.1 Good & Bad Aspects of Exception

- Exception
    + break referential transparency
    + RT expression could be substitute with the value it refer to
        * following example: if substitute y, error won't happen
    + meaning of RT expression does not depend on context
        * throw expression is context dependent, take different meaning depending on the try block
- Why Not Exception
    + Exception move us away from simple reasoning model
    + Exception is not type safe
        * type of Int=>Int tell us nothing about exception may occur
        * compiler could not check until runtime
- Still want the good part of Exception
    + Consolidate and centralize error-handling logic
    + without distribute logic throughout codebase

```scala
def failingFn(i: Int): Int = {
    val y: Int = throw new Exception("fail!")
    try {
        val x = 42 + 5
        x + y
    }
    catch { case e: Exception => 43 }
}
```

### 4.2 Possible Alternative
```scala
def mean(xs: Seq[Double]): Double =
    if (xs.isEmpty)
        throw new ArithmeticException("mean of empty list!")
    else xs.sum / xs.length
```

Alternatives
- Return bogus value of type Double
    + Example: return Double.NaN or null or -999999.99
    + Why not
        * silently propagate error, caller may forget to check
        * need explicit if to check whether result is real
        * not applicable for polymorphic code
            - null for non-primitive type, not work for Double & Int
            - comparison generic need cannot decide type when empty
- Supply an argument
    + Example:  return a default value when empty
    + Why not
        * require immediate caller have error-handle knowledge
        * we may need freedom to handle error when different case

### 4.3 The Option data type

```scala
sealed trait Option[+A]
case class Some[+A](get: A) extends Option[A]
case object None extends Option[Nothing]

def mean(xs: Seq[Double]): Option[Double] =
    if (xs.isEmpty) None
    else Some(xs.sum / xs.length)


```

- typesafe
    + reflect possibility that result may not always be defined
    + mean is a total function

```scala
trait Option[+A] {
    // apply f if not none
    def map[B](f: A => B): Option[B]
    def flatMap[B](f: A => Option[B]): Option[B]

    // B:>A means B must be a supertype of A
    // default:=>B: Don't evaluate ob unless needed
    def getOrElse[B >: A](default: => B): B
    def orElse[B >: A](ob: => Option[B]): Option[B]

    // convert to None if doesn't satisfy
    def filter(f: A => Boolean): Option[A]
}
```

Use Scenarios for Option functions

```scala
case class Employee(name:String, department:String)

def lookupByName(name:String):Option[Employee] = ...
val joeDepartment:Option[String] = lookupByName("Joe").map(_.department)
val department:String = joeDepartment
    .filter(_ != "Accounting").getOrElse("Default Dept.")
// when doing a chain of lookup, if first lookup returns None will abort rests

// implement variance
// multiple stage, but immediately return None if mean(xs) failed
def variance(xs: Seq[Double]): Option[Double] = 
  mean(xs) flatMap (m => mean(xs.map(x => math.pow(x - m, 2))))
```

Option composition, lifting and wrapping APIs

```scala
// lift ordinary functions to option
def lift[A,B](f:A=>B): Option[A] => Option[B] = _ map f

def insuranceRateQuote(age: Int, numberOfSpeedingTickets: Int): Double
// may fail with non-integer strings



def parseInsuranceRateQuote(age: String, numberOfSpeedingTickets: String): Option[Double] = {
    val optAge: Option[Int] = Try(age.toInt)
    val optTickets: Option[Int] = Try(numberOfSpeedingTickets.toInt)
    map2(optAge, optTickes)(insuranceRateQuote)
}
// with an Option based Exception API
// lazy: non-strict argument that evaluate at runtime
def Try[A](a: => A): Option[A] =
    try Some(a)
    catch { case e: Exception => None }

// also need to lift insuranceQuote
// flatMap A=>Option[B]: aa=>b(Option) map .. 
// map A=>B: bb=>f(aa,bb)
def map2[A,B,C](a: Option[A], b: Option[B])(f: (A, B) => C): Option[C] =
  a flatMap (aa => b map (bb => f(aa, bb)))


// more utilities
def sequence[A](a: List[Option[A]]): Option[List[A]] =
  a match {
    case Nil => Some(Nil)
    case h :: t => h flatMap (hh => sequence(t) map (hh :: _))
  }

def traverse[A, B](a: List[A])(f: A => Option[B]): Option[List[B]] =
  a match {
    case Nil => Some(Nil)
    case h::t => map2(f(h), traverse(t)(f))(_ :: _)
  }

// for comprehension: bind previous to flatmap and final to map
def map2[A,B,C](a: Option[A], b: Option[B])(f: (A, B) => C):
    Option[C] =
    for { aa <- a; bb <- b} yield f(aa, bb)
```

### 4.4 The Either data type

- Option only give None when exception
    + sometimes we want to learn more
    + replace None with another case => either
    + Right => Right, Left => Failure

```scala
sealed trait Either[+E, +A] {
    def map[B](f: A => B): Either[E, B]
    def flatMap[EE >: E, B](f: A => Either[EE, B]): Either[EE, B]
    def orElse[EE >: E,B >: A](b: => Either[EE, B]): Either[EE, B]
    def map2[EE >: E, B, C](b: Either[EE, B])(f: (A, B) => C):
    Either[EE, C]
}
case class Left[+E](value: E) extends Either[E, Nothing]
case class Right[+A](value: A) extends Either[Nothing, A]

def mean(xs: IndexedSeq[Double]): Either[String, Double] =
    if (xs.isEmpty)
        Left("mean of empty list!")
    else
        Right(xs.sum / xs.length)

def Try[A](a: => A): Either[Exception, A] =
    try Right(a)
    catch { case e: Exception => Left(e) }
```

Use Either for data validation
```scala
case class Person(name: Name, age: Age)
sealed class Name(val value: String)
sealed class Age(val value: Int)

def mkName(name: String): Either[String, Name] =
    if (name == "" || name == null) Left("Name is empty.")
    else Right(new Name(name))

def mkAge(age: Int): Either[String, Age] =
    if (age < 0) Left("Age is out of range.")
    else Right(new Age(age))

def mkPerson(name: String, age: Int): Either[String, Person] =
    mkName(name).map2(mkAge(age))(Person(_, _))
```

## 5. Strictness and Laziness

- How Scala evaluate a chain of operations?
    + non-strictness, or laziness
    + fundamental technique for improving efficiency & modularity of FP
- non-strictness
    + means function may choose not to evaluate one or more of arguments
    + Boolean function && and || in many languages are non-strict
        * && only evaluate the second when first is true
        * || don't evaluate the rest when first is true
    + can accept some of arguments unevaluated
+ thunk expression: ()=>A, zero argument and return an A
    * passing a function of no arguments for non-strict paramter
    * `onTrue: => A` for short, lazy unevaluated arguments
    * but won't cache result, evaluate each time it appear
    * can pass to a lazy val
        - `lazy val j = i` for `i:=>Int`
        - lazy delay evaluation until first referenced
        - also cache the result to prevent repeated evaluation

```scala
def if2[A](cond: Boolean, onTrue: () => A, onFalse: () => A): A =
    if (cond) onTrue() else onFalse()
    
if2(a < 22,
    () => println("a"),
    () => println("b")
)

scala> if2(false, sys.error("fail"), 3)
res2: Int = 3
```


### 5.1 Stream: A lazy list

- Stream
    + identical to list
    + except Cons take explicit thunks
- can evaluate only portion actually demanded

```scala
sealed trait Stream[+A]
case object Empty extends Stream[Nothing]
case class Cons[+A](h: () => A, t: () => Stream[A]) extends Stream[A]

object Stream {
    def cons[A](hd: => A, tl: => Stream[A]): Stream[A] = {
        lazy val head = hd
        lazy val tail = tl
        Cons(() => head, () => tail)
    }
    def empty[A]: Stream[A] = Empty
    def apply[A](as: A*): Stream[A] =
        if (as.isEmpty) empty else cons(as.head, apply(as.tail: _*))
}

// extract head, use h() instread
def headOption: Option[A] = this match {
    case Empty => None
    case Cons(h, t) => Some(h())
}

val x = Cons(() => expensive(x), tl)
val h1 = x.headOption
val h2 = x.headOption // will compute twice
```

- Memorizing streaming & Avoid Recomputation
- smart constructors 
    + typically lowercase the first letter of the corresponding data constructor
    + ensures that our thunk will only do its work once, when forced for the first time
- example
    + cons smart constructor memorize hd & tl
    + empty smart constructor return Empty
        * annotates Empty as Stream[A], for better type inference
    + used in apply
    + cons have lazy args, apply won't evaluate beforehand


Helper Functions
```scala
// Convert Stream to List
def toListRecursive: List[A] = this match {
  case Cons(h,t) => h() :: t().toListRecursive
  case _ => List()
}
// tail recursive for large streams
def toList: List[A] = {
  @annotation.tailrec
  def go(s: Stream[A], acc: List[A]): List[A] = s match {
    case Cons(h,t) => go(t(), h() :: acc)
    case _ => acc
  }
  go(this, List()).reverse
}

// takeN
def take(n: Int): Stream[A] = this match {
  case Cons(h, t) if n > 1 => cons(h(), t().take(n - 1))
  case Cons(h, _) if n == 1 => cons(h(), empty)
  case _ => empty
}

// takeWhile
def takeWhile(f: A => Boolean): Stream[A] = this match { 
  case Cons(h,t) if f(h()) => cons(h(), t() takeWhile f)
  case _ => empty 
}
```

### 5.3 Separating program description from evaluation

- major theme in FP is separation of concerns   
    + Option to capture error and another to handle it
    +  Stream to build computation logic but don't run until really need

```scala
// exists
def exists(p: A => Boolean): Boolean = this match {
    case Cons(h, t) => p(h()) || t().exists(p) // lazy
    case _ => false
}

// exists with foldright
def foldRight[B](z: => B)(f: (A, => B) => B): B =
    this match {
        case Cons(h,t) => f(h(), t().foldRight(z)(f))
        case _ => z
    }

def exists(p: A => Boolean): Boolean = foldRight(false)((a, b) => p(a) || b)
def forAll(f: A => Boolean): Boolean = foldRight(true)((a,b) => f(a) && b)
def takeWhile_1(f: A => Boolean): Stream[A] = foldRight(empty[A])((h,t) => 
    if (f(h)) cons(h,t) else empty)
def map[B](f: A => B): Stream[B] = foldRight(empty[B])((h,t) => cons(f(h), t)) 
def filter(f: A => Boolean): Stream[A] = foldRight(empty[A])((h,t) => 
    if (f(h)) cons(h, t) else t) 
def append[B>:A](s: => Stream[B]): Stream[B] =  foldRight(s)((h,t) => cons(h,t))
def flatMap[B](f: A => Stream[B]): Stream[B] =  foldRight(empty[B])((h,t) => f(h) append t)
```

- This implementations are incremental. 
    + They do not fully generate answer 
    + until some other computation look at elements of the resulting Stream
- find terminate as soon as target find
- they also work for infinite streams

### 5.4 Infinite Streams

```scala
// infinite stream that refer itself
val ones:Stram[Int] = Stream.cons(1,ones);
ones.map(_ + 1).exists(_ % 2 == 0) 
ones.forAll(_ != 1)
ones.forAll(_ == 1) // inspect forever

// constant stream // only create one object
def constant[A](a: A): Stream[A] = {
  lazy val tail: Stream[A] = Cons(() => a, () => tail) 
  tail
}
// increasing stream from n
def from(n: Int): Stream[Int] = cons(n, from(n+1))
// fibonacci
val fibs = {
  def go(f0: Int, f1: Int): Stream[Int] = 
    cons(f0, go(f1, f0+f1))
  go(0, 1)
}
```


### 5.5 Unfold
- unfold
  +  example of corecursive function, or guarded recursion
  +  produce data, production or cotermination

```scala
// unfold
// start from S, return A to keep in Stream and next S
def unfold[A, S](z: S)(f: S => Option[(A, S)]): Stream[A] =
  f(z) match {
    case Some((h,s)) => cons(h, unfold(s)(f))
    case None => empty
  }

//example
val b = unfold[String,Int](10) { x:Int => {
  if (x>0) Some((x.toString, x - 1)) else None}
} // 10,9,8,7,6,5,4,3,2,1 // don't necessary to end (infinite)

val fibsViaUnfold = unfold((0,1)) { case (f0,f1) => Some((f0,(f1,f0+f1))) }
def fromViaUnfold(n: Int) = unfold(n)(n => Some((n,n+1)))
def constantViaUnfold[A](a: A) = unfold(a)(_ => Some((a,a)))
val onesViaUnfold = unfold(1)(_ => Some((1,1)))

def mapViaUnfold[B](f: A => B): Stream[B] =
  unfold(this) {
    case Cons(h,t) => Some((f(h()), t()))
    case _ => None
  }

def takeViaUnfold(n: Int): Stream[A] =
  unfold((this,n)) {
    case (Cons(h,t), 1) => Some((h(), (empty, 0)))
    case (Cons(h,t), n) if n > 1 => Some((h(), (t(), n-1)))
    case _ => None
  }


def zipWith[B,C](s2: Stream[B])(f: (A,B) => C): Stream[C] =
  unfold((this, s2)) {
    case (Cons(h1,t1), Cons(h2,t2)) =>
      Some((f(h1(), h2()), (t1(), t2())))
    case _ => None
  }

// zipAll: continue traverse as long as have more elements
// use option to indicate exhaustion
def zipWithAll[B, C](s2: Stream[B])(f: (Option[A], Option[B]) => C): Stream[C] =
  Stream.unfold((this, s2)) {
    case (Empty, Empty) => None
    case (Cons(h, t), Empty) => Some(f(Some(h()), Option.empty[B]) -> (t(), empty[B]))
    case (Empty, Cons(h, t)) => Some(f(Option.empty[A], Some(h())) -> (empty[A] -> t()))
    case (Cons(h1, t1), Cons(h2, t2)) => Some(f(Some(h1()), Some(h2())) -> (t1() -> t2()))
  }

// if same type
def zip[B](s2: Stream[B]): Stream[(A,B)] = zipWith(s2)((_,_))
def zipAll[B](s2: Stream[B]): Stream[(Option[A],Option[B])] = zipWithAll(s2)((_,_))
```

### 5.6 Stream Usage

haveSubsequence Example
```scala
def startsWith[A](s: Stream[A]): Boolean = 
  zipAll(s).takeWhile(!_._2.isEmpty) forAll {
    case (h,h2) => h == h2
  }

// For a given Stream, tails returns the Stream of suffixes of the input sequence, starting with the original Stream. For example, given Stream(1,2,3), it would return Stream(Stream(1,2,3), Stream(2,3), Stream(3), Stream()).
def tails: Stream[Stream[A]] =
  unfold(this) {
    case Empty => None
    case s => Some((s, s drop 1))
  } append Stream(empty)

def hasSubsequence[A](s: Stream[A]): Boolean =
  tails exists (_ startsWith s)

// same number of steps as a more monolithic nested loop implementation
// using laziness, same as logic for breaking out of loop early
```


## 6. Purely functional state

### 6.1 Generating random number using side effects

- standard lib: scala.util.Random
  + `val rng = new scala.util.Random`
  + rng.nextDouble, rng.nextInt
  + rng.nextInt(10) - between 0 to 9 
- Problem of side effect
  + assume a dice roller generate 0-5 with offByOne Error
  + test need to reproduce same seed & state
  + make the state update explicit

A example implementation with linear congruential generator

```scala
case class SimpleRNG(seed: Long) extends RNG {
  def nextInt: (Int, RNG) = {
    val newSeed = (seed * 0x5DEECE66DL + 0xBL) & 0xFFFFFFFFFFFFL
    val nextRNG = SimpleRNG(newSeed)
    val n = (newSeed >>> 16).toInt
    (n, nextRNG)
  }
}

val rng = SimpleRNG(42)
val (n1, rng2) = rng.nextInt
val (n2, rng3) = rng2.nextInt
```

### 6.2 Making Stateful API pure

- Make the caller responsible for passing computed next state
  + when reused, always generate the same value

```scala
def randomPair(rng: RNG): (Int,Int) = {
  val (i1,rng2) = rng.nextInt
  val (i2,rng3) = rng2.nextInt // must use result when called twice
  ((i1,i2), rng3)              // must pass the state object
}

// Positive & Double
def nonNegativeInt(rng: RNG): (Int, RNG) = {
  val (i, r) = rng.nextInt
  (if (i < 0) -(i + 1) else i, r)
}

def double(rng: RNG): (Double, RNG) = {
  val (i, r) = nonNegativeInt(rng)
  (i / (Int.MaxValue.toDouble + 1), r)
}

// Multiple values
def double3(rng: RNG): ((Double, Double, Double), RNG) = {
  val (d1, r1) = double(rng)
  val (d2, r2) = double(r1)
  val (d3, r3) = double(r2)
  ((d1, d2, d3), r3)
}

// Terrible Repetitive
// A simple recursive solution
def ints(count: Int)(rng: RNG): (List[Int], RNG) =
  if (count <= 0) 
    (List(), rng)
  else {
    val (x, r1)  = rng.nextInt
    val (xs, r2) = ints(count - 1)(r1)
    (x :: xs, r2)
  }

// A tail-recursive solution
def ints2(count: Int)(rng: RNG): (List[Int], RNG) = {
  def go(count: Int, r: RNG, xs: List[Int]): (List[Int], RNG) =
    if (count <= 0)
      (xs, r)
    else {
      val (x, r2) = r.nextInt
      go(count - 1, r2, x :: xs)
    }
  go(count, rng,List())
```

### 6.4 A better API for state actions

- use Combinators to pass the state

```scala
// to make type conversion easy, RNG state action data type
type Rand[+A] = RNG => (A, RNG)

val int:Rand[Int] = _.nextInt

// Basic Generators
def unit[A](a:A):Rand[A] = rng => (a,rng)

def map[A,B](s: Rand[A])(f: A => B): Rand[B] =
  rng => {
    val (a, rng2) = s(rng)
    (f(a), rng2)
  }

// reimplement in a more elegant way
def nonNegativeEven: Rand[Int] =
  map(nonNegativeInt)(i => i - i % 2)

val _double: Rand[Double] =
  map(nonNegativeInt)(_ / (Int.MaxValue.toDouble + 1))
```

- need new combinator to combine two RNG actions together

```scala
def map2[A,B,C](ra: Rand[A], rb: Rand[B])(f: (A, B) => C): Rand[C] =
  rng => {
    val (a, r1) = ra(rng)
    val (b, r2) = rb(r1)
    (f(a, b), r2)
  }

def both[A,B](ra: Rand[A], rb: Rand[B]): Rand[(A,B)] =
  map2(ra, rb)((_, _))

// combine a whole list of RNG
def sequence[A](fs: List[Rand[A]]): Rand[List[A]] =
  fs.foldRight(unit(List[A]()))((f, acc) => map2(f, acc)(_ :: _))
```

- nesting state actions
```scala
def nonNegativeLessThan(n: Int): Rand[Int] = { rng =>
  val (i, rng2) = nonNegativeInt(rng)
  // mod, can be skewed if MaxValue have residual for n
  val mod = i % n
  if (i + (n-1) - mod >= 0) // if not exceed MaxValue
    (mod, rng2)
  else nonNegativeLessThan(n)(rng)  // retry
}

// nested structure, need flatMap
def flatMap[A,B](f: Rand[A])(g: A => Rand[B]): Rand[B] =
  rng => {
    val (a, r1) = f(rng)
    g(a)(r1) // We pass the new state along
  }

def nonNegativeLessThan(n: Int): Rand[Int] = {
  flatMap(nonNegativeInt) { i =>
    val mod = i % n
    if (i + (n-1) - mod >= 0) unit(mod) else nonNegativeLessThan(n)
  }
}

// flatMap is more powerful than maps
def _map[A,B](s: Rand[A])(f: A => B): Rand[B] =
  flatMap(s)(a => unit(f(a)))

def _map2[A,B,C](ra: Rand[A], rb: Rand[B])(f: (A, B) => C): Rand[C] =
  flatMap(ra)(a => map(rb)(b => f(a, b)))
```

> def rollDie: Rand[Int] = map(nonNegativeLessThan(6))(_ + 1)


### 6.5 State Actioin Data Type

```scala
type State[S,+A] = S => (A,S)
type Rand[A] = State[RNG, A]

// reimplement in State

case class State[S, +A](run: S => (A, S)) {
  def map[B](f: A => B): State[S, B] =
    flatMap(a => unit(f(a)))
  def map2[B,C](sb: State[S, B])(f: (A, B) => C): State[S, C] =
    flatMap(a => sb.map(b => f(a, b)))
  def flatMap[B](f: A => State[S, B]): State[S, B] = State(s => {
    val (a, s1) = run(s)
    f(a).run(s1)
  })
}

object State {

  def unit[S, A](a: A): State[S, A] =
    State(s => (a, s))

  // The idiomatic solution is expressed via foldRight
  def sequenceViaFoldRight[S,A](sas: List[State[S, A]]): State[S, List[A]] =
    sas.foldRight(unit[S, List[A]](List()))((f, acc) => f.map2(acc)(_ :: _))

  // This implementation uses a loop internally and is the same recursion
  // pattern as a left fold. It is quite common with left folds to build
  // up a list in reverse order, then reverse it at the end.
  // (We could also use a collection.mutable.ListBuffer internally.)
  def sequence[S, A](sas: List[State[S, A]]): State[S, List[A]] = {
    def go(s: S, actions: List[State[S,A]], acc: List[A]): (List[A],S) =
      actions match {
        case Nil => (acc.reverse,s)
        case h :: t => h.run(s) match { case (a,s2) => go(s2, t, a :: acc) }
      }
    State((s: S) => go(s,sas,List()))
  }

  // We can also write the loop using a left fold. This is tail recursive like the
  // previous solution, but it reverses the list _before_ folding it instead of after.
  // You might think that this is slower than the `foldRight` solution since it
  // walks over the list twice, but it's actually faster! The `foldRight` solution
  // technically has to also walk the list twice, since it has to unravel the call
  // stack, not being tail recursive. And the call stack will be as tall as the list
  // is long.
  def sequenceViaFoldLeft[S,A](l: List[State[S, A]]): State[S, List[A]] =
    l.reverse.foldLeft(unit[S, List[A]](List()))((acc, f) => f.map2(acc)( _ :: _ ))

  def modify[S](f: S => S): State[S, Unit] = for {
    s <- get // Gets the current state and assigns it to `s`.
    _ <- set(f(s)) // Sets the new state to `f` applied to `s`.
  } yield ()

  def get[S]: State[S, S] = State(s => (s, s))

  def set[S](s: S): State[S, Unit] = State(_ => ((), s))
}

```


- Finite State Automaton
  + a simple candy dispenser
  + has two types of input: coin or knob
  + in one of two states: locked or unlocked
  + tracks: how many candies left, how many coins contains

```scala
sealed trait Input
case object Coin extends Input
case object Turn extends Input

case class Machine(locked: Boolean, candies: Int, coins: Int)

object Candy {
  def update = (i: Input) => (s: Machine) =>
    (i, s) match {
      case (_, Machine(_, 0, _)) => s
      case (Coin, Machine(false, _, _)) => s
      case (Turn, Machine(true, _, _)) => s
      case (Coin, Machine(true, candy, coin)) =>
        Machine(false, candy, coin + 1)
      case (Turn, Machine(false, candy, coin)) =>
        Machine(true, candy - 1, coin)
    }

  def simulateMachine(inputs: List[Input]): State[Machine, (Int, Int)] = for {
    _ <- sequence(inputs map (modify[Machine] _ compose update))
    s <- get
  } yield (s.coins, s.candies)
}
```
