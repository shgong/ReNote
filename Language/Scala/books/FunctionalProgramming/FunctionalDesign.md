# Functional Design and Combinator

## 7. Purely functional parallism

- how to approach the problem of designing a purely funtional library
    + high composable & modular
    + separate describing computaiton from running it

// start with simple parallelizable computation: sum list of int

Summing integers is in practice probably so fast that parallelization imposes more
overhead than it saves. But simple examples like this are exactly the kind that are
most helpful to consider when designing a functional library.

```scala

def sum(ints:Seq[Int]):Int = ints.foldLeft(0)((a,b)=>a+b)

// divide-and-conquer
def sum(ints: IndexedSeq[Int]): Int =
    if (ints.size <= 1)
        ints.headOption getOrElse 0
    else {
        val (l,r) = ints.splitAt(ints.length/2)
        sum(l) + sum(r)
    }
```

### 7.1 Choosing data type & functions

- invent a container type, Par[A]
    + `unit[A](a:=>A):Par[A]`: return computation in a separate thread
    + `get[A](a:Par[A])`: extract result
- the problem with using concurrency primitives directly
    + api transcribed into Scala
    + `trait Runnable { def run: Unit }`
    + `class Thread(r:Runnable){ def start:Unit; def join:Unit}`
    + none of the methods return a meaningful value, always have side effect
- should create as many logical threads for our problem
    + then map these onto actual JVM threads
    + `class ExecutorService { def submit[A](a:Callable[A]):Future[A] }`
    + `trait Future[A]{def get:A}`
    + call to Future.get block calling thread until ExecutorService finished
    + and API provides no means of composing futures


```scala
def sum(ints: IndexedSeq[Int]): Int =
    if (ints.size <= 1)
        ints headOption getOrElse 0
    else {
        val (l,r) = ints.splitAt(ints.length/2)
        val sumL: Par[Int] = Par.unit(sum(l))
        val sumR: Par[Int] = Par.unit(sum(r))
        Par.get(sumL) + Par.get(sumR)
    }
```

- choosing the meaning of unit & get
    + evaluate immediately after unit, or hold til get called?
    + for parallelism, require unit evaluating concurrently immediately
        * if delayed until get is called, computation become sequential
        * `Par.get(Par.unit(sum(l))) + Par.get(Par.unit(sum(r)))` sequential
- combining computations
    + map2: should it take argument lazily?
    + run both side in parallel
    + should be lazy

```scala
def sum(ints: IndexedSeq[Int]): Par[Int] =
    if (ints.size <= 1)
        Par.unit(ints.headOption getOrElse 0)
    else {
        val (l,r) = ints.splitAt(ints.length/2)
        Par.map2(sum(l), sum(r))(_ + _)
        // def map2[A,B,C](a: Par[A], b: Par[B])(f: (A,B) => C): Par[C] 
    }

// if map2 strict in both arguments
// we construct entire left tree before move to right
sum(IndexedSeq(1,2,3,4))

map2(
    sum(IndexedSeq(1,2)),
    sum(IndexedSeq(3,4)))(_ + _)

map2(
    map2(
        sum(IndexedSeq(1)),
        sum(IndexedSeq(2)))(_ + _),
    sum(IndexedSeq(3,4)))(_ + _)

map2(
    map2(unit(1),unit(2))(_ + _),
    map2(
        sum(IndexedSeq(3)),
        sum(IndexedSeq(4)))(_ + _))(_ + _)
```

- Explicit Forking
    + when computation get forked off main thread, programmer can know it explicitly
    + use a fork API: `def fork[A](a:=>Par[A]):Par[A]`
    + now we can make map2 and unit strict
- Evaluation in fork or get? (eager or lazy)
    + if fork evaluate immediately, the implementation must know
        * how to create threads
        * the thread pool must be accessible wherever we call fork
        * lose ability to control parallelism strategy for different parts
    + better let get create threads and submit tasks
        * infer this didn't require knowing how fork & get implement
        * examine consequence of having Par values know about this
    + if fork simply holds on to unevaluated value
        * Par itself don't know how to implement parallelism
        * just description that interpreted later by get
        * meaning of Par changed from a parallel container to a description
        * now get is not get, but a run() instead
    
```scala
def unit[A](a:A): Par[A]
def lazyUnit[A](a:=>A):Par[A] = fork(unit(a))
```

### 7.2 Picking a representation

```scala
// create a computation that immediately results in value a
def unit[A](a: A): Par[A]

// combine two parallel results with a binary function
def map2[A,B,C](a: Par[A], b: Par[B])(f: (A,B) => C): Par[C]

// wrap expression a for concurrent evaluation by run
def fork[A](a: => Par[A]): Par[A]
def lazyUnit[A](a: => A): Par[A] = fork(unit(a))

// evaluate a given Par, spawning parallel computation by fork
def run[A](a: Par[A]): A
```

- Java ExecutorService API transcribed to Scala
    + ExecutorService let us submit a Callable value (lazy argument)
    + obtain value from Future with get method
```scala
class ExecutorService {
    def submit[A](a:Callable[A]):Future[A]
}
trait Callable[A]{def call:A}
trait Future[A]{
    def get: A
    def get(timeout:Long, unit:TimeUnit):A
    def cancel(evenIfRunning: Boolean):Boolean
    def isDone:Boolean
    def isCancelled:Boolean
}

type Par[A] = ExecutorService => Future[A]
def run[A](s: ExecutorService)(a: Par[A]): Future[A] = a(s)
```

### 7.3 Refine the API

starting
```scala

object Par {
    def unit[A](a: A): Par[A] = (es: ExecutorService) => UnitFuture(a)
    
    private case class UnitFuture[A](get: A) extends Future[A] {
        def isDone = true
        def get(timeout: Long, units: TimeUnit) = get
        def isCancelled = false
        def cancel(evenIfRunning: Boolean): Boolean = false
    }


    def map2[A,B,C](a: Par[A], b: Par[B])(f: (A,B) => C): Par[C] =
    (es: ExecutorService) => {
        val af = a(es)
        val bf = b(es)
        UnitFuture(f(af.get, bf.get))
    }

    def fork[A](a: => Par[A]): Par[A] =
        es => es.submit(new Callable[A] {
            def call = a(es).get
        })
    }


```

Map2 don't respect timeout currently
```scala
/* This version respects timeouts. See `Map2Future` below. */
def map2[A,B,C](a: Par[A], b: Par[B])(f: (A,B) => C): Par[C] =
  es => {
    val (af, bf) = (a(es), b(es))
    Map2Future(af, bf, f)
  }

/*
Note: this implementation will not prevent repeated evaluation if multiple threads call `get` in parallel. We could prevent this using synchronization, but it isn't needed for our purposes here (also, repeated evaluation of pure values won't affect results).
*/
case class Map2Future[A,B,C](a: Future[A], b: Future[B],
                             f: (A,B) => C) extends Future[C] {
  @volatile var cache: Option[C] = None
  def isDone = cache.isDefined
  def isCancelled = a.isCancelled || b.isCancelled
  def cancel(evenIfRunning: Boolean) =
    a.cancel(evenIfRunning) || b.cancel(evenIfRunning)
  def get = compute(Long.MaxValue)
  def get(timeout: Long, units: TimeUnit): C =
    compute(TimeUnit.NANOSECONDS.convert(timeout, units))

  private def compute(timeoutInNanos: Long): C = cache match {
    case Some(c) => c
    case None =>
      val start = System.nanoTime
      val ar = a.get(timeoutInNanos, TimeUnit.NANOSECONDS)
      val stop = System.nanoTime;val aTime = stop-start
      val br = b.get(timeoutInNanos - aTime, TimeUnit.NANOSECONDS)
      val ret = f(ar, br)
      cache = Some(ret)
      ret
  }
}
```

This API already enables a rich set of operations. Example: using lazyUnit, write a function to convert any function A => B to one that evaluates its
result asynchronously.

```
def asyncF[A,B](f: A => B): A => Par[B] =
  a => lazyUnit(f(a))
```

- list of Par and Par of List
    + sequence function
    + so we can sort inside a Par easily
    + with a sortPar function `map2(parList, unit(()))((a, _) => a.sorted)`
    + or with ParMap
        * `map2(pa, unit(()))((a,_) => f(a))`
        * `map(parList)(_.sorted)`

```scala
def sequence_simple[A](l: List[Par[A]]): Par[List[A]] = 
  l.foldRight[Par[List[A]]](unit(List()))((h,t) => map2(h,t)(_ :: _))

// tail recursive
def sequenceRight[A](as: List[Par[A]]): Par[List[A]] = 
  as match {
    case Nil => unit(Nil)
    case h :: t => map2(h, fork(sequenceRight(t)))(_ :: _)
  }

// divide and conquer, best performance
def sequenceBalanced[A](as: IndexedSeq[Par[A]]): Par[IndexedSeq[A]] = fork {
  if (as.isEmpty) unit(Vector())
  else if (as.length == 1) map(as.head)(a => Vector(a))
  else {
    val (l,r) = as.splitAt(as.length/2)
    map2(sequenceBalanced(l), sequenceBalanced(r))(_ ++ _)
  }
}
def sequence[A](as: List[Par[A]]): Par[List[A]] =
  map(sequenceBalanced(as.toIndexedSeq))(_.toList)

// easy to implement parmap now
def parMap[A,B](ps: List[A])(f: A => B): Par[List[B]] = fork {
    val fbs: List[Par[B]] = ps.map(asyncF(f))
    sequence(fbs)
}
```

### 7.4 the algebra of an API

- following the types
    + write down type signature for an operation we want
    + then follow the types
    + it's not cheating, it's natural style of algebra reasoning
- we have been reasoning somewhat informally about our API
    + there's nothing wrong
    + but it can be helpful to step back and formalize it

#### 7.4.1 the law of mapping

- start from map
    + `map(unit(1))(_ + 1) == unit(2)`
    + general case: `map(unit(x))(f) == unit(f(x))`
    + algebra transformation
        * `map(unit(x))(id) == unit(id(x)) == unit(x)`
        * `map(y)(id) == y`
- we can also deduce map(map(y)(g))(f)==map(y)(f compose g)

#### 7.4.2 the law of forking

- fork(x)==x
    + obviously true how fork should work
    + but it place strong contraints on our implementation of fork
    + think through any possible corner cases, counter examples

#### 7.4.3 breaking the law

- ExecutorService & fork implementation
    + when using ExecutorService backed by a thread pool of bounded size
    + it's very easy to run into a deadlock

```scala
val a = lazyUnit(42+1)
val S = Executors.newFixedThreadPool(1) // if pool size only 1
println(Par.equals(S)(a, fork(a)))

def fork[A](a: => Par[A]): Par[A] =
    es => es.submit(new Callable[A] {  // submit a callable first
        def call = a(es).get           // submit another within, deadlock
})
// for more threadpool, fork(fork(fork...)) will block
```

- what to do when couter example?
    + try to fix implementation
        * `def fork[A](fa: => Par[A]): Par[A] = es => fa(es)`
        * no use, still run hugeComputation on main thread
    + refine your law
        * pick a different representation of par

#### 7.4.4 new implementation & Actors

- problem: cannot get value out of future without blocking get method
- basic idea
    + instead of turning Par into java.util.concurrent.Future
    + use own version to register a callback to invoke when result is ready
        * rather than calling get to obtain result
        * our future has an apply(K:function) as callback
        * Using local side effect for a pure API

```scala
sealed trait Future[A] {
    private[parallelism] def apply(k: A => Unit): Unit
}
type Par[+A] = ExecutorService => Future[A]
```

```scala
def run[A](es: ExecutorService)(p: Par[A]): A = {
    val ref = new AtomicReference[A]   // mutable thread-safe reference storage
    val latch = new CountDownLatch(1)  // allow threads to wait until countDown n times
    p(es) { a => ref.set(a); latch.countDown } // set result when receive value
    latch.await // wait for latch, run block the calling thread now
    ref.get
}
// example of Pars
def unit[A](a: A): Par[A] =
    es => new Future[A] {
    def apply(cb: A => Unit): Unit =
        cb(a)   // executorService is not needed
}

def fork[A](a: => Par[A]): Par[A] =
    es => new Future[A] {
        def apply(cb: A => Unit): Unit =
            eval(es)(a(es)(cb))  // callback invoke async 
}
def eval(es: ExecutorService)(r: => Unit): Unit =
    es.submit(new Callable[Unit] { def call = r })
```

// what about map2? how to run both arguemnts parallel?

#### 7.4.5 Actors

- actor
    + a concurrent process that doesn't constantly occupy a thread
        * only occupy when receives a message
        * process only one message at a time
        * queueing other messages to avoid racec/deadlock

Minimum Implementation
```scala
val S = Executors.newFixedThreadPool(4)
val echoer = Actor[String](S) {
    msg => println (s"Got message: '$msg'")
}
```

Implement Map2
// if the other is None, Store
// otherwise call f with both result and pass result to callback
```scala
def map2[A,B,C](p: Par[A], p2: Par[B])(f: (A,B) => C): Par[C] =
    es => new Future[C] {
        def apply(cb: C => Unit): Unit = {
            var ar: Option[A] = None
            var br: Option[B] = None
        
            val combiner = Actor[Either[A,B]](es) {
                case Left(a) => br match {
                    case None => ar = Some(a)
                    case Some(b) => eval(es)(cb(f(a, b)))
                }
                case Right(b) => ar match {
                    case None => br = Some(b)
                    case Some(a) => eval(es)(cb(f(a, b)))
                }
            }
            p(es)(a => combiner ! Left(a))
            p2(es)(b => combiner ! Right(b))
        }
    }
```

Finally law of forking works for fixed size thread pools.

### 7.5 Refining combinators to general form

```scala
def choice[A](cond: Par[Boolean])(t: Par[A], f: Par[A]): Par[A] =
    es =>
        if (run(es)(cond).get) t(es)
        else f(es)
```

- suppose we want a function to choose between two forking computations
    + think about combinator a bit
    + General version `choiceN (n:Par[int])(choices:List[Par[A]])`
    + Why list? What if we have a map of it?
    + What about a primitive chooser?

```scala
def choiceN[A](n: Par[Int])(choices: List[Par[A]]): Par[A] = 
  es => {
    val ind = run(es)(n).get // Full source files
    run(es)(choices(ind))
  }

def choiceMap[K,V](key: Par[K])(choices: Map[K,Par[V]]): Par[V] =
  es => {
    val k = run(es)(key).get
    run(es)(choices(k))
  }

def chooser[A,B](p: Par[A])(choices: A => Par[B]): Par[B] = 
  es => {
    val k = run(es)(p).get
    run(es)(choices(k))
  }
```


- Whenever you generalize function like this
    + take critical look at your finished function
    + chooser is no longer proper name for this operation
    + instead, it is a bind or flatMap
        * `flatMap[A,B](a:Par[A])(f:A=>Par[B]):Par[B]`

```
def choiceViaFlatMap[A](p: Par[Boolean])(f: Par[A], t: Par[A]): Par[A] =
  flatMap(p)(b => if (b) t else f)

def choiceNViaFlatMap[A](p: Par[Int])(choices: List[Par[A]]): Par[A] =
  flatMap(p)(i => choices(i))
```

- More combinators like join
```scala
def join[A](a: Par[Par[A]]): Par[A] = 
  es => run(es)(run(es)(a).get())

def joinViaFlatMap[A](a: Par[Par[A]]): Par[A] = 
  flatMap(a)(x => x)

def flatMapViaJoin[A,B](p: Par[A])(f: A => Par[B]): Par[B] = 
  join(map(p)(f))

def flatMap[B](f: A => Par[B]): Par[B] = 
  Par.flatMap(p)(f)

def map[B](f: A => B): Par[B] = 
  Par.map(p)(f)

def map2[B,C](p2: Par[B])(f: (A,B) => C): Par[C] =
  Par.map2(p,p2)(f)

def zip[B](p2: Par[B]): Par[(A,B)] = 
  p.map2(p2)((_,_))
```



## 8. Property-based testing

### 8.1 ScalaCheck

```scala
val intList = Gen.listOf(Gen.choose(0,100))
val prop =
    forAll(intList)(ns => ns.reverse.reverse == ns) &&
    forAll(intList)(ns => ns.headOption == ns.reverse.lastOption)
val failingProp = forAll(intList)(ns => ns.reverse == ns)
```

- reverse example
    + intList is a Gen[List[Int]], generator of List[Int]
    + forAll: assert all value produced satisfy predicate
        * guarantee they have correct bahavior of reverse method

### 8.2 Choosing Data Types and Functions

```scala
def listOf[A](a:Gen[A]):Gen[List[A]]
def forAll[A](a:Gen[A])(f:A=>Boolean):Prop

trait Prop {def &&(p:Prop):Prop}
def &&(p: Prop): Prop = new Prop {
  def check = Prop.this.check && p.check
}
```

- requirement
    + return a parametric generator in some type
    + make it polymorphic
+ Prop: property
    + forAll: creating a property
    + &&: composing a property
    + check: running a property

```scala
object Prop {
    type FailedCase = String
    type SuccessCount = Int
}
trait Prop {
    def check: Either[(FailedCase, SuccessCount), SuccessCount]
}
```

- Gen
    + randomly generate values
    + wrap State transition over a random generator

```scala
case class Gen[A](sample: State[RNG,A])

def unit[A](a: => A): Gen[A] =
  Gen(State.unit(a))

def boolean: Gen[Boolean] =
  Gen(State(RNG.boolean))

def choose(start: Int, stopExclusive: Int): Gen[Int] =
  Gen(State(RNG.nonNegativeInt).map(n => start + n % (stopExclusive-start)))

def listOfN[A](n: Int, g: Gen[A]): Gen[List[A]] =
  Gen(State.sequence(List.fill(n)(g.sample)))
```

- understanding what operation are primitive & derived
    + pick some concerete example you like to express
    + see if you can assemble the functionality
    + look for patterns, try factoring out pattern into combinators
- ideas
    + if we can generate single Int in some range, can we generate (Int, Int) pair in some range
    + can we produce Gen[Option[A]] from Gen[A]? or the other way around
    + can we generate strings using existing primitives
- Generators that depend on generated value
    + need flatMap

```scala
def flatMap[B](f: A => Gen[B]): Gen[B] =
  Gen(sample.flatMap(a => f(a).sample))

/* A method alias for the function we wrote earlier. */
def listOfN(size: Int): Gen[List[A]] =
  Gen.listOfN(size, this)

/* A version of `listOfN` that generates the size to use dynamically. */
def listOfN(size: Gen[Int]): Gen[List[A]] =
  size flatMap (n => this.listOfN(n))

def union[A](g1: Gen[A], g2: Gen[A]): Gen[A] = 
  boolean.flatMap(b => if (b) g1 else g2)

def weighted[A](g1: (Gen[A],Double), g2: (Gen[A],Double)): Gen[A] = {
  /* The probability we should pull from `g1`. */
  val g1Threshold = g1._2.abs / (g1._2.abs + g2._2.abs)

  Gen(State(RNG.double).flatMap(d =>
    if (d < g1Threshold) g1._1.sample else g2._1.sample))
}    
```


- Refine Prop data type 
    + when know more about generator representations
    + make a new data type for result
        * Option[(FailedCase, SuccessCount)]

```scala
sealed trait Result {
    def isFalsified:Boolean
}
case object Passed extends Result (
    def isFalsified = false
)
case class Falsified(failure: FailedCase, 
        successes:SuccessCount) extends Result (
    def isFalsified = true
)

case class Prop(run: (TestCases,RNG) => Result)

def forAll[A](as: Gen[A])(f: A => Boolean): Prop = Prop {
    (n,rng) => randomStream(as)(rng).zip(Stream.from(0)).take(n).map {
        case (a, i) => try {
            if (f(a)) Passed else Falsified(a.toString, i)
        } catch { case e: Exception => Falsified(buildMsg(a, e), i) }
    }.find(_.isFalsified).getOrElse(Passed)
}

def randomStream[A](g: Gen[A])(rng: RNG): Stream[A] =
    Stream.unfold(rng)(rng => Some(g.sample.run(rng)))

def buildMsg[A](s: A, e: Exception): String =
    s"test case: $s\n" +
    s"generated an exception: ${e.getMessage}\n" +
    s"stack trace:\n ${e.getStackTrace.mkString("\n")}"




def &&(p: Prop) = Prop {
  (max,n,rng) => run(max,n,rng) match {
    case Passed => p.run(max, n, rng)
    case x => x
  }
}

def ||(p: Prop) = Prop {
  (max,n,rng) => run(max,n,rng) match {
    // In case of failure, run the other prop.
    case Falsified(msg, _) => p.tag(msg).run(max,n,rng)
    case x => x
  }
}

/* in the event of failure, we simply prepend
 * the given message on a newline in front of the existing message.
 */
def tag(msg: String) = Prop {
  (max,n,rng) => run(max,n,rng) match {
    case Falsified(e, c) => Falsified(msg + "\n" + e, c)
    case x => x
  }
}
```

### 8.3 Test case Minimization

- two approach to find smallest/simplest failing test case
    + shrinking: decrease size util no longer fails
    + sized generation: generate test cases in order of increasing size

```scala
case class SGen[+A](g: Int => Gen[A]) {
  def apply(n: Int): Gen[A] = g(n)

  //helper functions for converting Gen to SGen
  def unsized: SGen[A] = SGen(_ => this)

  def map[B](f: A => B): SGen[B] =
    SGen { g(_) map f }

  def flatMap[B](f: A => SGen[B]): SGen[B] = {
    val g2: Int => Gen[B] = n => {
      g(n) flatMap { f(_).g(n) }
    }
    SGen(g2)
  }

  def **[B](s2: SGen[B]): SGen[(A,B)] =
    SGen(n => apply(n) *

  def listOf[A](g: Gen[A]): SGen[List[A]] = 
    SGen(n => g.listOfN(n))
```

- SGen: Sized Geneartion
    + what about forall?
    + `def forAll[A](g: SGen[A])(f: A => Boolean): Prop`
    + not impossible to implement, prop doesn't receive size
    + use a maximum size


```scala
type MaxSize = Int
case class Prop(run: (MaxSize,TestCases,RNG) => Result)

def forAll[A](g: SGen[A])(f: A => Boolean): Prop =
    forAll(g(_))(f)

def forAll[A](g: Int => Gen[A])(f: A => Boolean): Prop = Prop {
    (max,n,rng) =>
        val casesPerSize = (n + (max - 1)) / max
        val props: Stream[Prop] =
            Stream.from(0).take((n min max) + 1).map(i => forAll(g(i))(f))
        val prop: Prop =
            props.map(p => Prop { (max, _, rng) =>
                p.run(max, casesPerSize, rng)
            }).toList.reduce(_ && _)
        prop.run(max,n,rng)
}
```

### 8.4 Reusability

```scala
val smallInt = Gen.choose(-10,10)
val maxProp = forAll(listOf(smallInt)) { ns =>
    val max = ns.max
    !ns.exists(_ > max)
}

// run helper function on Prop
def run(p: Prop,
    maxSize: Int = 100,
    testCases: Int = 100,
    rng: RNG = RNG.Simple(System.currentTimeMillis)): Unit =
p.run(maxSize, testCases, rng) match {
    case Falsified(msg, n) =>
        println(s"! Falsified after $n passed tests:\n $msg")
    case Passed =>
        println(s"+ OK, passed $testCases tests.")
}
```

Try using the library to construct tests and see if we notice any deficiencies
```scala
// Define listOf1 for generating nonempty lists
def listOf1[A](g: Gen[A]): SGen[List[A]] =
  SGen(n => g.listOfN(n max 1))
    
val maxProp1 = forAll(listOf1(smallInt)) { l => 
  val max = l.max
  !l.exists(_ > max) // No value greater than `max` should exist in `l`
}

//Write a property to verify the behavior of List.sorted
val sortedProp = forAll(listOf(smallInt)) { ns =>
  val nss = ns.sorted
  // We specify that every sorted list is either empty, has one element,
  // or has no two consecutive elements `(a,b)` such that `a` is greater than `b`.
  (nss.isEmpty || nss.tail.isEmpty || !nss.zip(nss.tail).exists {
    case (a,b) => a > b
  })
    // Also, the sorted list should have all the elements of the input list,
    && !ns.exists(!nss.contains(_))
    // and it should have no elements not in the input list.
    && !nss.exists(!ns.contains(_))
}
```

## 9. Parser Combinators

### 9.1 Designing an algebra first

- previous approach
    + invent & refine functions
    + tweaking data type reppresentation
    + laws was afterthoughts

```scala
trait Parsers[ParseError, Parser[+_]]{self=>
    def run[A](p:Parser[A])(input:String):Either[ParseError, A]

    def char(c:Char):Parser[Char]
    def string(s:String):Parser[String]

    def or[A](s1:Parser[A], s2:Parser[A]):Parser[A]
    implicit def string(s: String): Parser[String]
    implicit def operators[A](p: Parser[A]) = ParserOps[A](p)
    implicit def asStringParser[A](a: A)(implicit f: A => Parser[String]):
        sParserOps[String] = ParserOps(f(a))

    case class ParserOps[A](p: Parser[A]) {
        def |[B>:A](p2: Parser[B]): Parser[B] = self.or(p,p2) 
        def or[B>:A](p2: ss=> Parser[B]): Parser[B] = self.or(p,p2)
    }
}

run(or(string("abra"),string("cadabra")))("abra") == Right("abra")
```

- ScalaTest
    + `Parser[+_]` argument is Scala syntax for type parameter that is itself a type constructors
    + start from char matcher
    + add combinator like or & and
    + define syntax sugar shortcut for or 
- implicit conversion
    + string(s:String) -> Parser[String]
    + operators(Parser) -> ParserOps
    + asStringParser(a:A)(f:A=>String) -> ParserOps(f(a))
- auto promote a String to Parser, and getinfix operator for any type
- more combinators
    + repetition: listOfN

### 9.2 A possible algebra

combinator design
```
def many[A](p: Parser[A]): Parser[List[A]]
def map[A,B](a: Parser[A])(f: A=>B): Parser[B]

val numA: Parser[Int] = char('a').many.map(_.size)

def char(c: Char): Parser[Char] = string(c.toString) map (_.charAt(0))
def succeed[A](a: A): Parser[A] = string("") map (_ => a)
```

define testing laws in a object
```scala
trait Parsers[ParseError,Parser[_+]]
    ...
    object Laws {
        def equal[A](p1:Parser[A], p2: Parser[A])(in: Gen[String]): Prop = 
            forall(in)(s=> run(p1)(s) == run(p2)(s))
        def mapLaw[A](p: Parser[A])(in: Gen[String]): Prop =
            euqal(p, p.map(a=>a))(in) 
    }
```

####  Slicing & Nonempty repetition

- slice
    + a parser to see what portion of input string it examines
    + `def slice[A](p: Parser[A]): Parser[String]`
    + e.g.  counter: char('a').many.slice.map(_.size)
        * ignore the list from many, return things matched
        * _.size took constant time, rather than the list length

```scala
def product[A,B](p: Parser[A], p2: Parser[B]): Parser[(A,B)]
def map2[A,B,C](p: Parser[A], p2: Parser[B])(f: (A,B) => C): Parser[C] = 
    map(product(p, p2))(f.tupled)
def many1[A](p: Parser[A]): Parser[List[A]] = map2(p, many(p))(_ :: _)

def many[A](p: Parser[A]): Parser[List[A]] =
  map2(p, many(p))(_ :: _) or succeed(List())

def listOfN[A](n: Int, p: Parser[A]): Parser[List[A]] =
  if (n <= 0) succeed(List())
  else map2(p, listOfN(n-1, p))(_ :: _)  
```

Because a call to map2 always evaluates its second argument, our many function will
never terminate! That’s no good. This indicates that we need to make product and
map2 non-strict in their second argument:
```
def product[A,B](p: Parser[A], p2: => Parser[B]): Parser[(A,B)]
def map2[A,B,C](p: Parser[A], p2: => Parser[B])(
                f: (A,B) => C): Parser[C] =
    product(p, p2) map (f.tupled)

def wrap[A](p: => Parser[A]): Parser[A]

def many[A](p: Parser[A]): Parser[List[A]] =
    map2(p, wrap(many(p)))(_ :: _) or succeed(List())

```


### 9.3 Handling context sensitivity

- what we have so far
    + string(s): recognize and return single String
    + slice(p): return portion of input inspected by p, if successful
    + suceed(a): always suceed with a
    + map(p)(f): apply f to result of p, if success
    + product(p1,p2): sequence two parsers, return pair of their results
    + or(p1,p2): choose between two parsers, p2 if p1 fails
- can express any context free grammar, including JSON


- what cannot express?
    + context sensitive parsers
    + e.g. 1a 2aa 4aaaa
- expressive limitation, solved by a new primitive: flatMap
- new primitive: regex, which promote regex object to parser
    + in Scala, a string s can be promoted to a Regex object
    + `"[a-zA-Z_][a-zA-Z0-9_]*".r`

```scala
def flatMap[A,B](p:Parser[A])(f: A=>Parser[B]): Parser[B]

implicit def regex(r:Regex):Parser[String] 

// Generate context sensitive parser
for {
  digit <- "[0-9]+".r
  val n = digit.toInt // should catch exceptions thrown by toInt and convert to parse failure
  _ <- listOfN(n, char('a'))
} yield n

def product[A,B](p: Parser[A], p2: => Parser[B]): Parser[(A,B)] = 
  flatMap(p)(a => map(p2)(b => (a,b)))

def map2[A,B,C](p: Parser[A], p2: => Parser[B])(f: (A,B) => C): Parser[C] = 
  for { a <- p; b <- p2 } yield f(a,b)

def map[A,B](p: Parser[A])(f: A => B): Parser[B] = p.flatMap(a => succeed(f(a)))
```

### 9.4 Writing a JSON parser

at the design phase of a library, easier without any particular implementation

```scala
def jsonParser[Err, Parser[+_]](P: Parsers[Err,Parser]): Parser[JSON] = {
    import P._ // combinators
    val spaces = char(' ').many.slice
    ...
}
```

#### 9.4.1 JSON format

- object: comma-separated sequence of k-v pair, wrapped in curly braces {}
    + key: string
    + value
        * another object
        * array: []
        * literal: "MSFT", true, null, 30.66

```scala
trait JSON
object JSON {
    case object JNull extends JSON
    case class JNumber(get: Double) extends JSON
    case class JString(get: String) extends JSON
    case class JBool(get: Boolean) extends JSON
    case class JArray(get: IndexedSeq[JSON]) extends JSON
    case class JObject(get: Map[String, JSON]) extends JSON
}
```

#### 9.4.2 a JSON parser

```scala
package fpinscala.parsing

import language.higherKinds
import language.implicitConversions

trait JSON

object JSON {
  case object JNull extends JSON
  case class JNumber(get: Double) extends JSON
  case class JString(get: String) extends JSON
  case class JBool(get: Boolean) extends JSON
  case class JArray(get: IndexedSeq[JSON]) extends JSON
  case class JObject(get: Map[String, JSON]) extends JSON

  def jsonParser[Parser[+_]](P: Parsers[Parser]): Parser[JSON] = {
    // we'll hide the string implicit conversion and promote strings to tokens instead
    // this is a bit nicer than having to write token everywhere
    import P.{string => _, _}
    implicit def tok(s: String) = token(P.string(s))

    def array = surround("[","]")(
      value sep "," map (vs => JArray(vs.toIndexedSeq))) scope "array"
    def obj = surround("{","}")(
      keyval sep "," map (kvs => JObject(kvs.toMap))) scope "object"
    def keyval = escapedQuoted ** (":" *> value)
    def lit = scope("literal") {
      "null".as(JNull) |
      double.map(JNumber(_)) |
      escapedQuoted.map(JString(_)) |
      "true".as(JBool(true)) |
      "false".as(JBool(false))
    }
    def value: Parser[JSON] = lit | obj | array
    root(whitespace *> (obj | array))
  }
}

/**
 * JSON parsing example.
 */
object JSONExample extends App {
  val jsonTxt = """
{
  "Company name" : "Microsoft Corporation",
  "Ticker"  : "MSFT",
  "Active"  : true,
  "Price"   : 30.66,
  "Shares outstanding" : 8.38e9,
  "Related companies" : [ "HPQ", "IBM", "YHOO", "DELL", "GOOG" ]
}
"""

  val P = fpinscala.parsing.Reference
  import fpinscala.parsing.ReferenceTypes.Parser

  def printResult[E](e: Either[E,JSON]) =
    e.fold(println, println)

  val json: Parser[JSON] = JSON.jsonParser(P)
  printResult { P.run(json)(jsonTxt) }
}
```


### 9.5 Error reporting
Even without knowing what an implementation of Parsers will look like, we can reason abstractly about what information is being specified by a set of combinators.

Error handling, Error nesting


### 9.6 Implementing algebra


```scala
package fpinscala
package parsing

import ReferenceTypes._
import scala.util.matching.Regex

object ReferenceTypes {

  /** A parser is a kind of state action that can fail. */
  type Parser[+A] = ParseState => Result[A]

  /** `ParseState` wraps a `Location` and provides some extra
    * convenience functions. The sliceable parsers defined
    * in `Sliceable.scala` add an `isSliced` `Boolean` flag
    * to `ParseState`.
    */
  case class ParseState(loc: Location) {
    def advanceBy(numChars: Int): ParseState =
      copy(loc = loc.copy(offset = loc.offset + numChars))
    def input: String = loc.input.substring(loc.offset)
    def slice(n: Int) = loc.input.substring(loc.offset, loc.offset + n)
  }

  /* Likewise, we define a few helper functions on `Result`. */
  sealed trait Result[+A] {
    def extract: Either[ParseError,A] = this match {
      case Failure(e,_) => Left(e)
      case Success(a,_) => Right(a)
    }
    /* Used by `attempt`. */
    def uncommit: Result[A] = this match {
      case Failure(e,true) => Failure(e,false)
      case _ => this
    }
    /* Used by `flatMap` */
    def addCommit(isCommitted: Boolean): Result[A] = this match {
      case Failure(e,c) => Failure(e, c || isCommitted)
      case _ => this
    }
    /* Used by `scope`, `label`. */
    def mapError(f: ParseError => ParseError): Result[A] = this match {
      case Failure(e,c) => Failure(f(e),c)
      case _ => this
    }
    def advanceSuccess(n: Int): Result[A] = this match {
      case Success(a,m) => Success(a,n+m)
      case _ => this
    }
  }
  case class Success[+A](get: A, length: Int) extends Result[A]
  case class Failure(get: ParseError, isCommitted: Boolean) extends Result[Nothing]

  /** Returns -1 if s1.startsWith(s2), otherwise returns the
    * first index where the two strings differed. If s2 is
    * longer than s1, returns s1.length. */
  def firstNonmatchingIndex(s1: String, s2: String, offset: Int): Int = {
    var i = 0
    while (i < s1.length && i < s2.length) {
      if (s1.charAt(i+offset) != s2.charAt(i)) return i
      i += 1
    }
    if (s1.length-offset >= s2.length) -1
    else s1.length-offset
  }
}

object Reference extends Parsers[Parser] {

  def run[A](p: Parser[A])(s: String): Either[ParseError,A] = {
    val s0 = ParseState(Location(s))
    p(s0).extract
  }

  // consume no characters and succeed with the given value
  def succeed[A](a: A): Parser[A] = s => Success(a, 0)

  def or[A](p: Parser[A], p2: => Parser[A]): Parser[A] =
    s => p(s) match {
      case Failure(e,false) => p2(s)
      case r => r // committed failure or success skips running `p2`
    }

  def flatMap[A,B](f: Parser[A])(g: A => Parser[B]): Parser[B] =
    s => f(s) match {
      case Success(a,n) => g(a)(s.advanceBy(n))
                           .addCommit(n != 0)
                           .advanceSuccess(n)
      case f@Failure(_,_) => f
    }

  def string(w: String): Parser[String] = {
    val msg = "'" + w + "'"
    s => {
      val i = firstNonmatchingIndex(s.loc.input, w, s.loc.offset)
      if (i == -1) // they matched
        Success(w, w.length)
      else
        Failure(s.loc.advanceBy(i).toError(msg), i != 0)
    }
  }

  /* note, regex matching is 'all-or-nothing':
   * failures are uncommitted */
  def regex(r: Regex): Parser[String] = {
    val msg = "regex " + r
    s => r.findPrefixOf(s.input) match {
      case None => Failure(s.loc.toError(msg), false)
      case Some(m) => Success(m,m.length)
    }
  }

  def scope[A](msg: String)(p: Parser[A]): Parser[A] =
    s => p(s).mapError(_.push(s.loc,msg))

  def label[A](msg: String)(p: Parser[A]): Parser[A] =
    s => p(s).mapError(_.label(msg))

  def fail[A](msg: String): Parser[A] =
    s => Failure(s.loc.toError(msg), true)

  def attempt[A](p: Parser[A]): Parser[A] =
    s => p(s).uncommit

  def slice[A](p: Parser[A]): Parser[String] =
    s => p(s) match {
      case Success(_,n) => Success(s.slice(n),n)
      case f@Failure(_,_) => f
    }

  /* We provide an overridden version of `many` that accumulates
   * the list of results using a monolithic loop. This avoids
   * stack overflow errors for most grammars.
   */
  override def many[A](p: Parser[A]): Parser[List[A]] =
    s => {
      var nConsumed: Int = 0
      val buf = new collection.mutable.ListBuffer[A]
      def go(p: Parser[A], offset: Int): Result[List[A]] = {
        p(s.advanceBy(offset)) match {
          case Success(a,n) => buf += a; go(p, offset+n)
          case f@Failure(e,true) => f
          case Failure(e,_) => Success(buf.toList,offset)
        }
      }
      go(p, 0)
    }
}
```










