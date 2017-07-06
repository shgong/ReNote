# Effect & IO
## 13. External Effects and I/O

### 13.1 Factoring Effects

Start from simple side effects example:

```scala
case class Player(name: String, score: int)
def contest(p1: Player, p2: Player): Unit =
    if (p1.score > p2.score)
        println(s"${p1.name} is the winner!")
    else if (p2.score > p1.score)
        println(s"${p2.name} is the winner!")
    else
        println("It's a draw.")
```

TO

```scala
def winner(p1: Player, p2: Player): Option[Player] =
    if (p1.score > p2.score) Some(p1)
    else if (p1.score < p2.score) Some(p2)
    else None

def winnerMsg(p: Option[Player]): String = p map {
        case Player(name, _) => s"$name is the winner!"
    } getOrElse "It's a draw."

def contest(p1: Player, p2: Player): Unit =
    println(winnerMsg(winner(p1, p2)))
```

- given an impure function f of type A=>B, we can split
    + a pure function of type A=>D, description
    + an impure function of type D=>B, interpreter

### 13.2 A simple I/O

We can even factor out side effect from println, to make contest function more pure
```scala
trait IO {def run: Unit}
def PrintLine(msg: String): IO = 
    new IO {def run = println(msg) }
def contest(p1: Player, p2:Player): IO = 
    PrintLine(winnerMsg(winner(p1,p2)))
```

to make IO more useful
```scala 
trait IO { self =>  // let us refer object as self instead of this
    def run: Unit
    def ++(io: IO): IO = new IO {
        def run = {self.run; io.run} // self refer to outer IO
    }
}

object IO {
    def empty: IO = new IO { def runn = () }
}
```

now IO become a monoid, empty is identity, ++ is the associative

you, as the programmer, get to invent whatever API you wish to represent your computations, including those that interact with the universe external to your program. 

This process of crafting pleasing, useful, and composable descriptions of what you want your programs to do is at its core language design.

You’re crafting a little language, and an associated interpreter, that will allow you to
express various programs. If you don’t like something about this language you’ve created,
change it! You should approach this like any other design task.

#### 13.2.1 Handling input effects

what about readLine that returns a String?

Extend IO to allow input with a type param
```scala
sealed trait IO[A] { self =>
    def run: A
    def map[B](f: A => B): IO[B] =
        new IO[B] { def run = f(self.run) }
    def flatMap[B](f: A => IO[B]): IO[B] =
        new IO[B] { def run = f(self.run).run }
}  // now IO can be used in for comprehension

// with flatMap, IO forms a monad
object IO extends Monad[IO] {
    def unit[A](a: => A): IO[A] = new IO[A] { def run = a }
    def flatMap[A,B](fa: IO[A])(f: A => IO[B]) = fa flatMap f
    def apply[A](a: => A): IO[A] = unit(a)

    def ref[A](a: A): IO[IORef[A]] = IO { new IORef(a) }
    sealed class IORef[A](var value: A) {
      def set(a: A): IO[A] = IO { value = a; a }
      def get: IO[A] = IO { value }
      def modify(f: A => A): IO[A] = get flatMap (a => set(f(a)))
    }
}
// with apply, we can use function application syntax to construct
// IO block in `IO { ... }`
```


We can now write our converter example:
```scala
def ReadLine: IO[String] = IO { readLine }

def PrintLine(msg: String): IO[Unit] = IO { println(msg) }

def converter: IO[Unit] = for {
    _ <- PrintLine("Enter a temperature in degrees Fahrenheit: ")
    d <- ReadLine.map(_.toDouble)
    _ <- PrintLine(fahrenheitToCelsius(d).toString)
} yield ()
```

example usage
```scala
val echo: IO[Unit] = ReadLine.flatMap(PrintLine)
val readInt: IO[Int] = ReadLine.map(_.toInt)
val readInts: IO[Int, Int] = readInt ** readInt
// a**b is the same as map2(a,b)((_,_)) combine to a pair
```

larger example: factorial
```scala
def factorial(n: Int): IO[Int] = for {
    acc <- ref(1)
    _ <- foreachM (1 to n toStream) (i => acc.modify(_ * i).skip)
    result <- acc.get
} yield result

val factorialREPL: IO[Unit] = sequence_(
    IO { println(helpstring) },
    doWhile { IO { readLine } } { line =>
        when (line != "q") { for {
        n <- factorial(line.toInt)
        _ <- IO { println("factorial: " + n) }
    } yield () }
})
```

#### 13.2.2 Benefits & drawbacks of the simple IO type

It clearly separates pure code from impure code
forcing us to be honest about where intereaction with outside world are occuring

- Benefits
    + IO computations are ordinary values
    + can store in list, pass to function, warp patterne and reuse
    + we can craft more interesting interpreter than simple run method
- Problems
    + many IO programs will overflow runtime call stack
    + value of type IO[A] is completely opaque. just a lazy Identity, too general
    + has nothing to do with concurrency & asynchronous oepration
    
### 13.3 Avoid StackOverflow

consider a simple program `val p = IO.forever(PrintLine("Still goiing...")`, when run, it calls itself over and over finally crash with `StackOverflowError`

Problem is definition of flatMap, which create a new IO object
```
def flatMap[B](f: A => IO[B]): IO[B] = 
    new IO[B]{def run = f(self.run).run}
```

#### 13.3.1 Reifying control flow as data constructors

- DO NOT let program control flow through with function calls
    + like making flatMap a method that construct a new IO
- INSTEAD bake the control flow we want into our data type 
    + make it a data constructor of the IO data type, thus form a tail-recursive loop
    + when encounter FlatMap(x,k) constructor, interpret x and then call k on the result

```scala
sealed trait IO[A] {
    def flatMap[B](f: A => IO[B]): IO[B] = 
        FlatMap(this, f)
    def map[B](f: A => B): IO[B] = 
        flatMap(f andThen (Return(_)))
}
case class Return[A](a: A) extends IO[A]
// a pure computation that immediately returns an A without any further steps

case class Suspend[A](resume: () => A) extends IO[A]
// a suspension of computation where resume takes no argument, but has effect

case class FlatMap[A,B](sub: IO[A], k: A => IO[B]) extends IO[B]
// a composition of two steps
```

for this new IO type, forever println becomes
```scala
def printLine(s: String): IO[Unit] = 
    Suspend(() => Return(println(s)))

val p = IO.forever(printLine("Still going..."))

// actually create an infinite nested structure like Stream
FlatMap( Suspend(() => println(s))),
         _ => FlatMap ( ... ) )
         // tail-recursive
```

implement of new run
```scala
@annotation.tailrec def run[A](io: IO[A]): A = io match {
    case Return(a) => a
    case Suspend(r) => r()
    case FlatMap(x, f) => x match {  
    // we could say run(f(run(x))), but then inner run() wouldn't be in tail position
        case Return(a) => run(f(a))
        case Suspend(r) => run(f(r()))
        // run(Suspend(r)), expand to make run at tail
        case FlatMap(y, g) => run(y flatMap (a => g(a) flatMap f))
        // FlatMap(FlatMap(y,g), f)
    }
}
```

- FlatMap(FlatMap(y,g), f)
    + reassociate: (y flatMap g) flatMap f
    + monad associative law: y flatMap (g flatMap f)
    + y flatMap ( a => g(a) flatMap f)

- When program on JVM makes a function call
    + it push a frame onto call stack, to remember where to return
- we made it control explicit in IO data type
- when run interprets an IO program
    + if execute some effect with Suspend(s)
    + or call subroutine with FlatMap(x,f)
        * call x() and then continue by calling f()
        * f() immediately return one of three, transfer control to run() again

IO program is kind of coroutine that executes cooperatively with run, and run function is sometimes called a trampoline, returning control to single loop to eliminate stack.

#### 13.3.2 Trampolining: general solution to stack overflow

StackoverflowError manifests itself in Scala wherever we have composition function
```scala
val f = (x: Int) => x
val g = List.fill(100000)(f).foldLeft(f)(_ compose _)
g(42)

java.lang.StackOverflowError
```

solve this with our IO monad
```scala
val f: Int => IO[Int] = (x: Int) => Return(x)
val g = List.fill(100000)(f).foldLeft(f){
    (a,b) => x => Suspend(() => a(x).flatMap(b))
}
val x = run(g(42))
```

it is not really an IO monad, it is tail-call elimination monad

- can add trampolining to any function A => B
- using TailRec can be slower than directly function calls, but gain predictable stack usage

```scala
sealed trait TailRec[A] {
    def flatMap[B](f: A => TailRec[B]): TailRec[B] = FlatMap(this, f)
    def map[B](f: A => B): TailRec[B] = flatMap(f andThen (Return(_)))
}
case class Return[A](a: A) extends TailRec[A]
case class Suspend[A](resume: () => A) extends TailRec[A]
case class FlatMap[A,B](sub: TailRec[A], k: A => TailRec[B]) extends TailRec[B]
```

### 13.4 more nuanced IO type

- other two prbolems with monad still remain
    + inexplicit about what kinds of effect may occur
    + has no concurrency mechanism

- What if we used `Par` type for suspension instead of `()=>A` ?

```scala
sealed trait Async[A] {
    def flatMap[B](f: A => Async[B]): Async[B] = FlatMap(this, f)
    def map[B](f: A => B): Async[B] = flatMap(f andThen (Return(_)))
}
case class Return[A](a: A) extends Async[A]
case class Suspend[A](resume: Par[A]) extends Async[A]
case class FlatMap[A,B](sub: Async[A], k: A => Async[B]) extends Async[B]

@annotation.tailrec
def step[A](async: Async[A]): Async[A] = async match {
    case FlatMap(FlatMap(x,f), g) => step(x flatMap (a => f(a) flatMap g))
    case FlatMap(Return(x), f) => step(f(x))
    case _ => async
}

def run[A](async: Async[A]): Par[A] = step(async) match {
    case Return(a) => Par.unit(a)
    case Suspend(r) => Par.flatMap(r)(a => run(a))
    case FlatMap(x, f) => x match {
        case Suspend(r) => Par.flatMap(r)(a => run(f(a)))
        case _ => sys.error("Impossible; `step` eliminates these cases")
    }
}
```

use arbitratry constructor
```scala
sealed trait Free[F[_],A]
case class Return[F[_],A](a: A) extends Free[F,A]
case class Suspend[F[_],A](s: F[A]) extends Free[F,A]
case class FlatMap[F[_],A,B](s: Free[F,A], f: A => Free[F,B]) extends Free[F,B]

type TailRec[A] = Free[Function0,A]
type Async[A] = Free[Par,A]
```

#### 13.4.1 Reasonably priced monads

Free is a monad
- use type lambda trick to partially apply free type constructor ( Free[F[__], A])

```scala
def freeMonad[F[_]]: Monad[({type f[a] = Free[F,a]})#f] =
    new Monad[({type f[a] = Free[F,a]})#f] {
      def unit[A](a: => A) = Return(a)
      def flatMap[A,B](fa: Free[F, A])(f: A => Free[F, B]) = fa flatMap f
    }

 @annotation.tailrec
  def runTrampoline[A](a: Free[Function0,A]): A = (a) match {
    case Return(a) => a
    case Suspend(r) => r()
    case FlatMap(x,f) => x match {
      case Return(a) => runTrampoline { f(a) }
      case Suspend(r) => runTrampoline { f(r()) }
      case FlatMap(a0,g) => runTrampoline { a0 flatMap { a0 => g(a0) flatMap f } }
    }
  }

// a `Free` interpreter which works for any `Monad`
def run[F[_],A](a: Free[F,A])(implicit F: Monad[F]): F[A] = step(a) match {
    case Return(a) => F.unit(a)
    case Suspend(r) => r
    case FlatMap(Suspend(r), f) => F.flatMap(r)(a => run(f(a)))
    case _ => sys.error("Impossible, since `step` eliminates these cases")
}
```

- What is the meaning of Free[F,A]? 
- Essentially, it’s a recursive structure that contains a value of type A wrapped in zero or more layers of F. 
- It’s a monad because flatMap lets us take the A and from it generate more layers of F. Before getting at the result, an interpreter of the structure must be able to process all of those F layers. We can view the structure and its interpreter as coroutines that are interacting, and the type F defines the protocol of this interaction. By choosing our F carefully, we can precisely control what kinds of interactions are allowed.

#### 13.4.2 Monad that supports only console io

- use a more restricteive F: console interaction instead of just Function0 `()=>A`

```scala
sealed trait Console[A] {
    def toPar: Par[A]
    def toThunk: () => A
}

case object ReadLine extends Console[Option[String]] {
    def toPar = Par.lazyUnit(run)
    def toThunk = () => run
    def run: Option[String] = try Some(readLine()) catch { case e: Exception => None }
}

case class PrintLine(line: String) extends Console[Unit] {
    def toPar = Par.lazyUnit(println(line))
    def toThunk = () => println(line)
}


object Console {
    type ConsoleIO[A] = Free[Console, A]

    def readLn: ConsoleIO[Option[String]] = Suspend(ReadLine)
    def printLn(line: String): ConsoleIO[Unit] = Suspend(PrintLine(line))
}

// example program
val f1: Free[Console, Option[String]] = for {
    _ <- printLn("I can only interact with the console.")
    ln <- readLn
} yield ln
```

But how to run a ConsoleIO? signature run require a Monad[Console], and it is impossible to implement flatMap for Console. Need translation type

```scala
trait Translate[F[_], G[_]] { def apply[A](f: F[A]): G[A] }
type ~>[F[_], G[_]] = Translate[F,G]

val consoleToFunction0 =
    new (Console ~> Function0) { def apply[A](a: Console[A]) = a.toThunk }
val consoleToPar =
    new (Console ~> Par) { def apply[A](a: Console[A]) = a.toPar }

def runFree[F[_],G[_],A](free: Free[F,A])(t: F ~> G)(implicit G: Monad[G]): G[A] =
    step(free) match {
        case Return(a) => G.unit(a)
        case Suspend(r) => t(r)
        case FlatMap(Suspend(r),f) => G.flatMap(t(r))(a => runFree(f(a))(t))
        case _ => sys.error("Impossible; `step` eliminates these cases")
}
```

Now we can implement runConsoleFunction0 & runConsolePar, relying on having Monad[Function0] and Monad[Par]:

```scala
def runConsoleFunction0[A](a: Free[Console,A]): () => A =
    runFree[Console,Function0,A](a)(consoleToFunction0)
def runConsolePar[A](a: Free[Console,A]): Par[A] =
    runFree[Console,Par,A](a)(consoleToPar)

implicit val function0Monad = new Monad[Function0] {
    def unit[A](a: => A) = () => a
    def flatMap[A,B](a: Function0[A])(f: A => Function0[B]) =
        () => f(a())()
}
implicit val parMonad = new Monad[Par] {
    def unit[A](a: => A) = Par.unit(a)
    def flatMap[A,B](a: Par[A])(f: A => Par[B]) =
        Par.fork { Par.flatMap(a)(f) }
}
```

#### 13.4.3 Pure interpreters

use ReaderMonad to translate our Console requests to a String=>A

```scala
case class ConsoleReader[A](run: String => A) {
    def map[B](f: A => B): ConsoleReader[B] =
        ConsoleReader(r => f(run(r)))
    def flatMap[B](f: A => ConsoleReader[B]): ConsoleReader[B] =
        ConsoleReader(r => f(run(r)).run(r))
}
object ConsoleReader {
    implicit val monad = new Monad[ConsoleReader] {
        def unit[A](a: => A) = ConsoleReader(_ => a)
        def flatMap[A,B](ra: ConsoleReader[A])(f: A => ConsoleReader[B]) =
            ra flatMap f
    }
}

sealed trait Console[A] {
    ...
    def toReader: ConsoleReader[A]
}

val consoleToReader = new (Console ~> ConsoleReader) {
    def apply[A](a: Console[A]) = a.toReader
}

@annotation.tailrec
def runConsoleReader[A](io: ConsoleIO[A]): ConsoleReader
```

### 13.5 Non-blocking & asynchronous I/O
last problem: non-blocking or async I/O

```scala
trait Source {
    def readBytes(
    numBytes: Int,
    callback: Either[Throwable, Array[Byte]] => Unit): Unit
}

trait Future[+A] {
    private def apply(k: A => Unit): Unit
}

type Par[+A] = ExecutorService => Future[A]

def async[A](run: (A => Unit) => Unit): Par[A] = es => new Future {
    def apply(k: A => Unit) = run(k)
}

def nonblockingRead(source: Source, numBytes: Int):
    Par[Either[Throwable,Array[Byte]]] =
    async { (cb: Either[Throwable,Array[Byte]] => Unit) =>
    source.readBytes(numBytes, cb)
}
def readPar(source: Source, numBytes: Int): Free[Par,Either[Throwable,Array[Byte]]] =
    Suspend(nonblockingRead(source, numBytes))
```


### 13.6 general-purpose IO type

```scala
abstract class App {
    import java.util.concurrent._
    
    def unsafePerformIO[A](a: IO[A])(pool: ExecutorService): A =
        Par.run(pool)(run(a)(parMonad))

    def main(args: Array[String]): Unit = {
        val pool = Executors.fixedThreadPool(8)
        unsafePerformIO(pureMain(args))(pool)
    }

    def pureMain(args: IndexedSeq[String]): IO[Unit]
}
```

### 13.7 why IO type is insufficient for streaming

For large files, it requires loading the contents entirely into memoery to work on it,
we have to write a monolithic loop to do it in trunk. 

There is nothing wrong, but programming with a composable abstraction like List is much nicer
than programming directly with the primitive I/O operations

We will work on Streaming I/O in chapter 15


# ------------------------------------------------------------------------------
## 14. Local effects and mutable states

- We will develop a more mature concept of referential transparency
    + Effects can occur locally inside an expression
    + We can guarantee no other part of larger program can observe effects occuring
    + expressions can be referentially transparent

### 14.1 Purely functional mutable state

- purely functional programming not use mutable state?
    + Referential Transparency: expression e is RT if for all programs p, all occurences of e in p can be replaced by the result of evaluating e without affect meaning of p
    + Definition: a function is pure if f(x) is RT for all RT x
    + It does not disallow mutation of _local_ state

```scala
def quicksort(xs: List[Int]): List[Int] = if (xs.isEmpty) xs else {
    val arr = xs.toArray

    // swap two elements
    def swap(x: Int, y: Int) = {
        val tmp = arr(x)
        arr(x) = arr(y)
        arr(y) = tmp
    }

    // partitions a portion into elements lt/gt pivots
    def partition(n: Int, r: Int, pivot: Int) = {
        val pivotVal = arr(pivot)
        swap(pivot, r)
        var j = n
        for (i <- n until r) if (arr(i) < pivotVal) {
            swap(i, j)
            j += 1
        }
        swap(j, r)
        j
    }

    // sort portion of array in place
    def qs(n: Int, r: Int): Unit = if (n < r) {
        val pi = partition(n, r, n + (n - r) / 2)
        qs(n, pi - 1)
        qs(pi + 1, r)
    }

    qs(0, arr.length - 1)
    arr.toList
}
```

- quicksort is pure
    + impossible for any caller to know 
        * individual subexpression inside body of quick aren't RT
        * local methods (swap, partition, qs) aren't pure
- any function can use side-effecting components internally, with pure interface

### 14.2 Data type to enforce scoping of side effect
#### 14.2.1 A little language for scoped mutation

- we already have State[S,A] monad, `S=>(A,S)`
    + it takes input state and produce result and output state
    + but mutating state in place, not really passing it from one action to next
    + we will pass token marked with S, call function to mutate data with also S
- static guarantees
    + if we hold a reference to mutable object, nothing can observe us mutating it
    + a mutable object can never be observed outside of the creation scope
- ST
    + new local effect monad
    + State thread/transition/token/tag
    + Similar to State monad, except run is protected

```scala
sealed trait ST[S,A] { self =>
    protected def run(s: S): (A,S)
    def map[B](f: A => B): ST[S,B] = new ST[S,B] {
        def run(s: S) = {
            val (a, s1) = self.run(s)
            (f(a), s1)
        }
    }
    def flatMap[B](f: A => ST[S,B]): ST[S,B] = new ST[S,B] {
        def run(s: S) = {
            val (a, s1) = self.run(s)
            f(a).run(s1)
        }
    }
}

object ST {
    def apply[S,A](a: => A) = {
        lazy val memo = a      // cache in case run is called multiple times
        new ST[S,A] { def run(s: S) = (memo, s) }
    }
}
```


so how do we run an ST action, giving it an initial state?

#### 14.2.2 An algebra of mutable references

- a little language for mutable reference
    + allocate a new mutable cell
    + write to a mutable cell
    + read from a mutable cell
- data structure
    + a wrapper around a protected var

```scala
sealed trait STRef[S, A] {
    protected var cell: A
    def read: ST[S,A] = ST(cell)
    def write(a:A): ST[S,Unit] = new ST[S, Unit] {
        def run(s:S) = {
            cell = a
            ((), s)
        }
    }
}

objected STRef {
    def apply[S,A](a:A): ST[S, STRef[S,A]] = ST(new STRef[S,A] {
       var cell = a 
    })
}
```

- methods on STRef to read and write are pure, since just return ST actions
    + we never actually use value of type S
    + S serves as only an authorization token, no other purpose
- STRef trait is sealed
    + can only contruct by calling apply method on STRef Campanion object 
    + it return `ST[S, STRef[S,A]` action
        * that constructs STRef when run and given the token of type S

Example usage
```scala
for {
    r1 <- STRef[Nothing,Int](1)
    r2 <- STRef[Nothing,Int](1)
    x <- r1.read
    y <- r2.read
    _ <- r1.write(y+1)
    _ <- r2.write(x+1)
    a <- r1.read
    b <- r2.read
} yield (a,b)
```

We can't yet run because run is still protected.

#### 14.2.3 Running mutable state actions

- the plot with the ST monad
    + use ST to build up a computation
        * when run, allocates some local mutable state
        * proceed to mutate it to accomplish some task
        * then discard the mutable state
    + all mutable state is private and locally scoped, thus RT
- but how to guarantee we can never extract STRef out of an ST action?
    + ST[S, STRef[S, Int]]: not safe to run 
    + ST[S, Int]: safe, literally just an Int
- we should disallow any ST[S,T] if T involves type S

RunnableST: ST actions that are safe to run
```scala
trait RunnableST[A] {
    def apply[S]: ST[S,A]
}

val p = new RunnableST[(Int, Int)] {
    def apply[S] = for {
        r1 <- STRef(1)
        r2 <- STRef(2)
        x <- r1.read
        y <- r2.read
        _ <- r1.write(y+1)
        _ <- r2.write(x+1)
        a <- r1.read
        b <- r2.read
    } yield (a,b)
}
```

Since the RunnableST action is polymorphic in S, it’s guaranteed to not make use of the value that gets passed in. 
So it’s actually completely safe to pass (), the value of type Unit!

Now we can add runST to ST campanion object
```scala
object ST {
    def apply[S,A](a: => A) = {
        lazy val memo = a
        new ST[S,A] {
        def run(s: S) = (memo, s)
    }
}
def runST[A](st: RunnableST[A]): A =
    st.apply[Unit].run(())._1
}

val r = ST.runST(p)  // r = (3,2)
```

#### 14.2.4 Mutable arrays

An array class for ST monad
```scala
sealed abstract class STArray[S,A](implicit manifest: Manifest[A]) {
    // scala requires an implicit Manifest for constructing arrays
    protected def value: Array[A]
    def size: ST[S,Int] = ST(value.size)

    // write value at given index of array
    def write(i: Int, a: A): ST[S,Unit] = new ST[S,Unit] {
        def run(s: S) = {
            value(i) = a
            ((), s)
        }
    }

    // read value at given index of array
    def read(i: Int): ST[S,A] = ST(value(i))

    // turn array into an immutable list
    def freeze: ST[S,List[A]] = ST(value.toList)
}

object STArray {
    def apply[S,A:Manifest](sz: Int, v: A): ST[S, STArray[S,A]] =
        new STArray[S,A] { lazy val value = Array.fill(sz)(v) } // size & initial value
}
```

- Scala stdlib provide manifests for most types
- we always return an STArray packaged in ST action

```scala
// fill the array from a Map
// eg xs.fill(Map(0->"a", 2->"b"))
def fill(xs: Map[Int,A]): ST[S,Unit] =
  xs.foldRight(ST[S,Unit](())) {
    case ((k, v), st) => st flatMap (_ => write(k, v))
  }

// read from list
def fromList[S,A:Manifest](xs: List[A]): ST[S, STArray[S,A]] =
    ST(new STArray[S,A] { lazy val value = xs.toArray })
```

#### 14.2.5 Purely functional in-place quicksort

```scala
// An action that does nothing
def noop[S] = ST[S,Unit](())

def partition[S](a: STArray[S,Int], l: Int, r: Int, pivot: Int): ST[S,Int] = for {
  vp <- a.read(pivot)
  _ <- a.swap(pivot, r)
  j <- STRef(l)
  _ <- (l until r).foldLeft(noop[S])((s, i) => for {
    _ <- s
    vi <- a.read(i)
    _  <- if (vi < vp) (for {
      vj <- j.read
      _  <- a.swap(i, vj)
      _  <- j.write(vj + 1)
    } yield ()) else noop[S]
  } yield ())
  x <- j.read
  _ <- a.swap(x, r)
} yield x

def qs[S](a: STArray[S,Int], l: Int, r: Int): ST[S, Unit] = if (l < r) for {
  pi <- partition(a, l, r, l + (r - l) / 2)
  _ <- qs(a, l, pi - 1)
  _ <- qs(a, pi + 1, r)
} yield () else noop[S]

def quicksort(xs: List[Int]): List[Int] =
    if (xs.isEmpty) xs else ST.runST(new RunnableST[List[Int]] {
        def apply[S] = for {
            arr <- STArray.fromList(xs)
            size <- arr.size
            _ <- qs(arr, 0, size - 1)
            sorted <- arr.freeze
        } yield sorted
})

```


### 14.3 Purity is contextual

there are other effects that may be non-observable, depending on who’s looking

```scala
scala> case class Foo(s: String)
scala> val b = Foo("hello") == Foo("hello")
b: Boolean = true
scala> val c = Foo("hello") eq Foo("hello")
c: Boolean = false
```

For most programs, this makes no difference, because most programs don’t check for reference equality. It’s only the eq method that allows our programs to observe this side effect occurring. We could therefore say that it’s not a side effect at all in the context of the vast majority of programs.

# ------------------------------------------------------------------------------
## 15. Stream processing and incremental I/O

#### 15.1 Problem with imperative I/O

example: write a program that checks whether the number of lines in a file is greater than 40,000.
```scala
def linesGt40k(filename: String): IO[Boolean] = IO {
    val src = io.Source.fromFile(filename)
    try {
        var count = 0
        val lines: Iterator[String] = src.getLines
        while (count <= 40000 && lines.hasNext) {
            lines.next
            count += 1
        }
        count > 40000
    }
    finally src.close
}

unsafePerformIO(linesGt40k("lines.txt"))
```

- use low level primitives: while, Iterator and var
- good things
    + incremental: not laoding entire file
    + terminate early
- bad things
    + remember to close file when done -> resource leak
    + entangles high-level algorithm with low-level concerns about iteration & file access

compare with Stream analysis
```scala
lines.filter(!_.trim.isEmpty).zipWithIndex.exists(_._2 > 40000)

lines.filter(!_.trim.isEmpty).
      take(40000).
      map(_.head).
      indexOfSlice("abracadabra".toList)
```

- we could cheat by writing lazy I/O

```scala
def lines(filename: String): IO[Stream[String]] = IO {
    val src = io.Source.fromFile(filename)
    src.getLines.toStream append { src.close; Stream.empty }
}
```

- We are cheating because 
    + Stream[String] inside IO monad isn't actually pure value
    + execute side effects of reading from file
    + close only if examine entire stream
- problematic
    + not resource-safe, cannot really terminate early
    + nothing stop us from traversing Stream again when file closed
    + side effect: two process traversing at the same time can result in unpredictable behavior

#### 15.2 Simple stream transducers

- Stream transducer
    + specifies a transformation from one stream to another
    + stream refer to a lazily genearted sequence
        * lines from file
        * HTTP requests
        * mouse click positions

Process data type
```scala
sealed trait Process[I,O]

case class Emit[I,O](
    head: O,
    tail: Process[I,O] = Halt[I,O]())  // can omit second arg when end
  extends Process[I,O]

case class Await[I,O](
    recv: Option[I] => Process[I,O])
  extends Process[I,O]

case class Halt[I,O]() extends Process[I,O]
```



#### 15.3 An extensible process type























