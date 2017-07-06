
# CATS

## 1. Semigroup

A semigroup for type A has a single operation, which obeys commutative law:
> ((a combine b) combine c) === (a combine (b combine c)) 

```scala
import cats.kernel.Semigroup

Semigroup[Int].combine(1, 2) // 3
Semigroup[List[Int]].combine(List(1, 2, 3), List(4, 5, 6)) // List(1,2,3,4,5,6)
Semigroup[Option[Int]].combine(Option(1), Option(2)) // Option(3)
Semigroup[Option[Int]].combine(Option(1), None) // Option(1)
Semigroup[Int ⇒ Int].combine({ (x: Int) ⇒ x + 1 }, { (x: Int) ⇒ x * 10 }).apply(6) // 67
```

Merge Map: combine vs ++

```
import cats.implicits._

Map("a" -> Map("b" -> 5)) combine (Map("a" -> Map("b" -> 6))  // Map(a -> Map(b -> 11))
Map("a" -> List(1, 2)) combine Map("a" -> List(3, 4))         // Map(a -> List(1, 2, 3, 4))  

Map("a" -> Map("b" -> 5)) ++ Map("a" -> Map("b" -> 6))        // Map(a -> Map(b -> 6))
Map("a" -> List(1, 2)) ++ Map("a" -> List(3, 4))              // Map(a -> List(3, 4))
```

Inline syntax
```scala
import cats.implicits._

val one: Option[Int] = Option(1)
val two: Option[Int] = Option(2)
val n: Option[Int] = None

one |+| two should be(Option(3))
n |+| two should be(Option(2))
n |+| n should be(None)
```


## 2. Monoid

Monoid extends Semigroup, adding empty method to semigroup combine.

> (combine(x, empty) == combine(empty, x) == x)

```
import cats._
import cats.implicits._

Monoid[String].empty  // ""
Monoid[String].combineAll(List("a", "b", "c"))  // "abc"
Monoid[String].combineAll(List()) // ""

Monoid[Map[String, Int]].combineAll(List(Map("a" → 1, "b" → 2), Map("a" → 3)))
// Map("a"->4,"b"->2)
Monoid[Map[String, Int]].combineAll(List())  // Map()

```

use Foldable's foldMap 
accumulating mapped results using available Monoid

```scala
List(1, 2, 3, 4, 5).foldMap(identity)    // 15
List(1, 2, 3, 4, 5).foldMap(_.toString)  // "12345"
```

Customize Monoid: monoidTuple < already defined in cats
```scala
implicit def monoidTuple[A: Monoid, B: Monoid]: Monoid[(A, B)] =
  new Monoid[(A, B)] {
    def combine(x: (A, B), y: (A, B)): (A, B) = {
      val (xa, xb) = x
      val (ya, yb) = y
      (Monoid[A].combine(xa, ya), Monoid[B].combine(xb, yb))
    }
    def empty: (A, B) = (Monoid[A].empty, Monoid[B].empty)
  }
```

so we can do both in one pass
```
val l = List(1, 2, 3, 4, 5)
l.foldMap(i ⇒ (i, i.toString))
```


## 3. Functor

Functor is a type class involving types with a hole or `F[_]`, e.g. Option, List & Future.

Functor category involves a single operation named `map`

```scala
def map[A,B](fa:F[A])(f: A=>B): F[B]

Option(1).map(_ + 1)
List(1, 2, 3).map(_ + 1)
Vector(1, 2, 3).map(_.toString)
```

### Creating Functor

```scala
import cats._

// easier to create for a type with well behaved map method
implicit val listFunctor: Functor[List] = new Functor[List] {
  def map[A, B](fa: List[A])(f: A => B) = fa map f
}

// for types which don't have a map
implicit def function1Functor[In]: Functor[Function1[In, ?]] =
  new Functor[Function1[In, ?]] {
    def map[A, B](fa: In => A)(f: A => B): Function1[In, B] = fa andThen f
  }

```

about kind-projector compiler plugin:
take two hole types and constraint one of the holds to be the In type, another for return
`({type F[A] = Function1[In, A]})#F` => `Function1[In, ?]`

### Using Functor

__map__
```scala
Functor[List].map(List("qwer", "adsfg"))(_.length) // List(4,5)
Functor[Option].map(Option("Hello"))(_.length)  // Option(5)
Functor[Option].map(None: Option[String])(_.length)  // None
```

__lift__ a function from A=>B to F[A]=>F[B]
```scala
val lenOption: Option[String] => Option[Int] = Functor[Option].lift(_.length)
lenOption(Some("abcd"))
```

__fproduct__ to pair value with result
```scala
val source = List("Cats", "is", "awesome")
val product = Functor[List].fproduct(source)(_.length).toMap
// Map(Cats -> 4, is -> 2, awesome -> 7)
```

__compose__ Given any functor `F[_]` and any functor `G[_]` we can create a new functor `F[G[_]]` by composing them
```scala
val listOpt = Functor[List] compose Functor[Option]
listOpt.map(List(Some(1), None, Some(3)))(_ + 1)
```



## 4. Apply

Apply extends Functor with a `ap`, that transform value in context `F[A]=>F[B]`
Map takes a `A=>B`, while Apply takes a `F[A=>B]`

```scala
import cats._

implicit val optionApply: Apply[Option] = new Apply[Option] {
  def ap[A, B](f: Option[A => B])(fa: Option[A]): Option[B] =
    fa.flatMap(a => f.map(ff => ff(a)))

  def map[A, B](fa: Option[A])(f: A => B): Option[B] = fa map f

  def product[A, B](fa: Option[A], fb: Option[B]): Option[(A, B)] =
    fa.flatMap(a => fb.map(b => (a, b)))
}

implicit val listApply: Apply[List] = new Apply[List] {
  def ap[A, B](f: List[A => B])(fa: List[A]): List[B] =
    fa.flatMap(a => f.map(ff => ff(a)))

  def map[A, B](fa: List[A])(f: A => B): List[B] = fa map f

  def product[A, B](fa: List[A], fb: List[B]): List[(A, B)] =
    fa.zip(fb)
}
```

In addition to Functor, it support

### ap, ap2, ap3

```scala
Apply[Option].ap(Some(intToString))(Some(1)) // Some("1")

val addArity2 = (a: Int, b: Int) ⇒ a + b
Apply[Option].ap2(Some(addArity2))(Some(1), Some(2)) // Some(3)
Apply[Option].ap2(Some(addArity2))(Some(1), None) // None

val addArity3 = (a: Int, b: Int, c: Int) ⇒ a + b + c
Apply[Option].ap3(Some(addArity3))(Some(1), Some(2), Some(3)) // Some(6)
```

### map2 map3 tuple2 tuple3
```scala
Apply[Option].map2(Some(1), Some(2))(addArity2) // Some(3)
Apply[Option].map3(Some(1), Some(2), Some(3))(addArity3) // Some(6)

Apply[Option].tuple2(Some(1), Some(2)) // Some((1,2))
Apply[Option].tuple3(Some(1), Some(2), Some(3)) // Some(1,2,3)
```

### Apply Builder |@|
```scala
import cats.implicits._
val option2 = Option(1) |@| Option(2)
val option3 = option2 |@| Option.empty[Int]

option2 map addArity2 // Some(3)
option3 map addArity3 // None

option2 apWith Some(addArity2)  // Some(3)
option3 apWith Some(addArity3)  // None

option2.tupled Some(1,2)
option3.tupled None
```

## 5. Applicative

Applicative extends Apply by adding single method pure
`def pure[A](x:A):F[A]`

```
import cats._
import cats.implicits._

Applicative[Option].pure(1) // Option(1)
Applicative[List].pure(1) // List(1)
Applicative[List] compose Applicative[Option]).pure(1) // List(Option(1))
```

### Applicative vs Monad
    + generalization of Monad, allowing expression of effectful computations in a pure way
    + preferred if structure of computation is fixed a priori
    + possible to perform certain kinds of static analysis on applicative values

```
Monad[Option].pure(1) // Option(1)
Applicative[Option].pure(1) // Option(1)
```

- Monad will short circuit if you want to collect error messages
    + Applicative will allow past and future run in parallel
- however need examine future prior to running past
    + we cannot write
    + `ifA :: Applicative f => f Bool -> f a -> f a -> f a`
    + while we can write
    + `ifM :: Monad m => m Bool -> m a -> m a -> m a`

```haskell
ifM mbool th el = do
  bool <- mbool
  if bool then th else el
```


The difference between a Monad and an Applicative is that in the Monad there's a choice. The key distinction of Monads is the ability to choose between different paths in computation (not just break out early). Depending on a value produced by a previous step in computation, the rest of computation structure can change.

## 6. Monad

Monad extends Applicative with new function `flatten`
which takes nested context `F[F[A]]` and joins the context into `F[A]`

we can use flatten to define flatMap
- flatMap is map followed by flatten
- fatten is flatMap using identity Monad `x=>x`

```
Option(Option(1)).flatten // Option(1)
Option(None).flatten // None
List(List(1), List(2, 3)).flatten // List(1,2,3)
```

```scala
import cats._

implicit def optionMonad(implicit app: Applicative[Option]) =
  new Monad[Option] {
    // Define flatMap using Option's flatten method
    override def flatMap[A, B](fa: Option[A])(f: A => Option[B]): Option[B] =
      app.map(fa)(f).flatten
    // Reuse this definition from Applicative.
    override def pure[A](a: A): Option[A] = app.pure(a)
  }

```

### flatMap

```scala
implicit val listMonad = new Monad[List] {
  def flatMap[A, B](fa: List[A])(f: A => List[B]): List[B] = fa.flatMap(f)
  def pure[A](a: A): List[A] = List(a)
}

Monad[List].flatMap(List(1, 2, 3))(x ⇒ List(x, x)) // List(1,1,2,2,3,3)
```

### IFM

Monad provides the ability to choose later operations in a sequence based on the results of earlier ones. This is embodied in ifM, which lifts an if statement into the monadic context.

```scala
import cats._
import cats.implicits._

Monad[Option].ifM(Option(true))(Option("truthy"), Option("falsy")) // Some("truthy")
Monad[List].ifM(List(true, false, true))(List(1, 2), List(3, 4)) // List(1,2,3,4,1,2)
```

### Composition

Unlike Functors and Applicatives, you cannot derive a monad instance for a generic `M[N[_]]` where both `M[_]` and `N[_]` have an instance of a monad.

However, it is common to want to compose the effects of both `M[_]` and `N[_]`. One way of expressing this is to provide instructions on how to compose any outer monad (F in the following example) with a specific inner monad (Option in the following example).

Monad transformer

```scala
case class OptionT[F[_], A](value: F[Option[A]])

implicit def optionTMonad[F[_]](implicit F: Monad[F]) = {
  new Monad[OptionT[F, ?]] {
    def pure[A](a: A): OptionT[F, A] = OptionT(F.pure(Some(a)))
    def flatMap[A, B](fa: OptionT[F, A])(f: A => OptionT[F, B]): OptionT[F, B] =
      OptionT {
        F.flatMap(fa.value) {
          case None => F.pure(None)
          case Some(a) => f(a).value
        }
      }
    def tailRecM[A, B](a: A)(f: A => OptionT[F, Either[A, B]]): OptionT[F, B] =
      defaultTailRecM(a)(f)
  }
}
```

cats already provide a OptionT

```scala
import cats.implicits._
optionTMonad[List].pure(42) // OptionT(List(Option(42)))
```

## 7. Foldable

- Foldable[F] is implemented in terms of two basic methods
    + foldLeft(fa,b)(f): eagerly folds fa l to r
    + foldRight(fa,b)(f): lazily folds fa r to l
- Fold, also called combineAll, fold with Monoid instance
- Foldmap will first map before fold

```scala
Foldable[List].foldLeft(List(1, 2, 3), 0)(_ + _) // 6
Foldable[List].foldLeft(List("a", "b", "c"), "")(_ + _) // "abc"

val lazyResult = Foldable[List].foldRight(List(1, 2, 3), Now(0))((x, rest) ⇒ Later(x + rest.value))
lazyResult.value // 6


Foldable[List].fold(List("a", "b", "c")) // "abc"
Foldable[List].fold(List(1, 2, 3)) // 6

Foldable[List].foldMap(List("a", "b", "c"))(_.length) // 3
Foldable[List].foldMap(List(1, 2, 3))(_.toString) // "123"
```

### FoldK
- FoldK detect inside MonoidK to combine before fold
- A MonoidK[F] can produce a Monoid[F[A]] for any type A.
    + which inherits from SemigroupK

```
Foldable[List].foldK(List(List(), List(1, 2), List(3, 4, 5)))  // List(1,2,3,4,5)
Foldable[List].foldK(List(None, Option("two"), Option("three"))) // Some(two)

MonoidK[List].combineK(List(), List(1,2))  // List(1,2)
MonoidK[Option].combineK(Option("two"),Option("three")) // Some(two)
```

### Find, Exist & Forall

```
Foldable[List].find(List(1, 2, 3))(_ > 2) // Some(3)
Foldable[List].find(List(1, 2, 3))(_ > 5) // None

Foldable[List].exists(List(1, 2, 3))(_ > 2) // true
Foldable[List].exists(List(1, 2, 3))(_ > 5) // false

Foldable[List].forall(List(1, 2, 3))(_ <= 3) // true
Foldable[List].forall(List(1, 2, 3))(_ < 3) // false
```

### Filter_ 
F[A] to List[A]

```scala
Foldable[Option].filter_(Option(42))(_ != 42)  // Nil
```

### Traverse_
traverse foldable mapping A to G[B], and combine using Applicative[G]
primarily useful when `G[_]` represents an action or effect, thus B can be discarded
```scala
import cats.implicits._
import cats.data.Xor

def parseInt(s: String): Option[Int] =
  Xor.catchOnly[NumberFormatException](s.toInt).toOption

Foldable[List].traverse_(List("1", "2", "3"))(parseInt) // Some(Unit)
Foldable[List].traverse_(List("a", "b", "c"))(parseInt) // None
```


### Compose
`Foldable[F[_]]` and `Foldable[G[_]]` to obtain `Foldable[F[G[_]]]` 

```
val FoldableListOption = Foldable[List].compose[Option]
FoldableListOption.fold(List(Option(1), Option(2), Option(3), Option(4)))  // 10
FoldableListOption.fold(List(Option("1"), Option("2"), None, Option("3"))) // "123"
```


## 8. Traverse
In functional programming it is common to encode effects as data types
- Option for missing values
- Xor & Validated for possible errors
- Future for asynchronous computations

```scala
import cats.data.Xor
import scala.concurrent.Future

def parseInt(s: String): Option[Int] = ???

trait SecurityError
trait Credentials

def validateLogin(cred: Credentials): Xor[SecurityError, Unit] = ???

trait Profile
trait User

def userInfo(user: User): Future[Profile] = ???
```

what if when we get profiles for a list of users

```scala
def profilesFor(users: List[User]): List[Future[Profile]] = users.map(userInfo)
```

### The type class

```
trait Traverse[F[_]] {
    def traverse[G[_]: Applicative, A, B](fa: F[A])(f: A=>G[B]): G[F[B]]
}
```

F[_] is some sort of context which may contain a value (or several), e.g. List

### Choose your effect
traverseU is for all intents and purposes the same as traverse, but with some type-level trickery to allow it to infer the `Applicative[Xor[A, ?]]` and `Applicative[Validated[A, ?]]` instances - scalac has issues inferring the instances for data types that do not trivially satisfy the `F[_]`shape required by Applicative.

```scala
// Xor removed from cats since Either fill the gap in Scala 2.12
import scala.util.{Either => Xor}
import cats.Semigroup
import cats.data.{NonEmptyList, OneAnd, Validated, ValidatedNel, Either}
import cats.implicits._

def parseIntXor(s: String): Either[NumberFormatException, Int] =
  Xor.catchOnly[NumberFormatException](s.toInt)

def parseIntValidated(s: String): ValidatedNel[NumberFormatException, Int] =
  Validated.catchOnly[NumberFormatException](s.toInt).toValidatedNel

List("1", "2", "3").traverseU(parseIntXor) // Xor.Right(List(1,2,3))
List("1", "abc", "3").traverseU(parseIntXor).isLeft // true
```


We need proof that `NonEmptyList[A]` is a Semigroup for there to be an Applicative instance for ValidatedNel.
```scala
implicit def nelSemigroup[A]: Semigroup[NonEmptyList[A]] =
  OneAnd.oneAndSemigroupK[List].algebra[A]
```

with this implicit evidence, we can use `ValidateNel` as an applicative
```scala
import cats.Semigroup
import cats.data.{NonEmptyList,OneAnd, ValidateNel}

List("1","2","3").traverseU(parseIntValidated).isValid // true
```

Xor: once hit first bad parse, won't attempt to parse any others down the line
Validated: continue parse others and accumulate errors

### Playing with Reader

`Reader[E,A]` is a type alias for `Kleisli[Id, E, A]`, a wrapper around `E=>A`

Reader can be used as applicative  if we fix E to be some environment/configuration.

```scala
import cats.data.Reader

trait Context
trait Topic
trait Result

type Job[A] = Reader[Context, A]

def processTopic(topic: Topic): Job[Result] = ???
```

We can imagine we have a data pipeline that processes a bunch of data, each piece of data being categorized by a topic. Given a specific topic, we produce a Job that processes that topic. (Note that since a Job is just a Reader/Kleisli, one could write many small Jobs and compose them together into one Job that is used/returned by processTopic.)

Corresponding to our bunches of data are bunches of topics, a List[Topic] if you will. Since Reader has an Applicative instance, we can traverse over this list with processTopic.
```scala
def processTopics(topics: List[Topic]) =
  topics.traverse(processTopic)
```

An example of context can be found in Spark project

- information to run a Spark job resides in `SparkContext`
- can define spark jobs `type Job[A] = Reader[SparkContext, A]` 
- run several Spark jobs via `traverse`
- get a `Job[List[Result]]` or `SparkContext => List[Result]` back

### Sequencing

a shorthand of applicative traverse

```scala
import cats.implicits._
List(Option(1), Option(2), Option(3)).traverse(identity) // Some(List(1,2,3))
List(Option(1), None, Option(3)).traverse(identity) // None
```

```scala
List(Option(1), Option(2), Option(3)).sequence
List(Option(1), None, Option(3)).sequence
```

### TRAVERSING FOR EFFECT

Sometimes our effectful functions return a Unit value in cases where there is no interesting value to return (e.g. writing to some sort of store).

```
trait Data
def writeToStore(data: Data): Future[Unit] = ???
```

Traverse using this will end up with `Future[List[Unit]]` which is funny
So when traverse for effect, use foldable's `traverse_` and `sequence_`

```scala
import cats.implicits._

List(Option(1), Option(2), Option(3)).sequence_  // Some(Unit)
List(Option(1), None, Option(3)).sequence_ // None
```

## 9. Identity
 the ambient monad that encodes the effect of having no effect. It is ambient in the sense that plain pure values are values of Id.

```scala
type Id[A] = A
val x: Id[Int] = 1
val y: Int = x
```

thus we can treat our Id type constructor as Monad & as Comonad
```scala
import cats.Functor
val one: Int = 1
Functor[Id].map(one)(_ + 1)    // functor map is function application
Applicative[Id].pure(42)       // pure is just identity function
Monad[Id].map(one)(_ + 1)    
Monad[Id].flatMap(one)(_ + 1)  // monad flatMap is also just function application
Comonad[Id].coflatMap(fortytwo)(_ + 1) // 43
```


## 10. XOR

- Why old `Either` doesn't work
    + it lacks flatMap and map methods
    + you need do a RightProjection
- But for Xor OR the new Either
    + it is right-biased when built-in

```scala
val e1: Either[String, Int] = Right(5)
e1.right.map(_ + 1)

val e2: Either[String, Int] = Left("hello")
e2.right.map(_ + 1)

e1.map(_ + 1)
e2.map(_ + 1)
```

Right biased Either is possible to define a monad instance for it, as we can fix left type parameter.

```scala
import cats.Monad

implicit def xorMonad[Err]: Monad[Xor[Err, ?]] =
  new Monad[Xor[Err, ?]] {
    def flatMap[A, B](fa: Xor[Err, A])(f: A => Xor[Err, B]): Xor[Err, B] =
      fa.flatMap(f)

    def pure[A](x: A): Xor[Err, A] = Xor.right(x)
  }


val right: String Either Int = Either.right(5)
right.flatMap(x ⇒ Either.right(x + 1))  // Right(6)

val left: String Either Int = Either.left("Something went wrong")
left.flatMap(x ⇒ Either.right(x + 1))   // Left(Something went wrong)
```

### Replace exceptions

old code
```scala
object ExceptionStyle {
  def parse(s: String): Int =
    if (s.matches("-?[0-9]+")) s.toInt
    else throw new NumberFormatException(s"${s} is not a valid integer.")

  def reciprocal(i: Int): Double =
    if (i == 0) throw new IllegalArgumentException("Cannot take reciprocal of 0.")
    else 1.0 / i

  def stringify(d: Double): String = d.toString
}
```

Either Style

```
object XorStyle {
  def parse(s: String): Either[NumberFormatException, Int] =
    if (s.matches("-?[0-9]+")) Either.right(s.toInt)
    else Either.left(new NumberFormatException(s"${s} is not a valid integer."))

  def reciprocal(i: Int): Either[IllegalArgumentException, Double] =
    if (i == 0) Either.left(new IllegalArgumentException("Cannot take reciprocal of 0."))
    else Either.right(1.0 / i)

  def stringify(d: Double): String = d.toString

  def magic(s: String): Either[Exception, String] =
    parse(s).flatMap(reciprocal).map(stringify)
}
```


```
import XorStyle._

magic("0").isRight //false
magic("1").isRight //true
magic("Not a number").isRight //false
```

Better: enumerate explicitly with ADT instead of using exceptions

```scala
object EitherStyleWithAdts {
  sealed abstract class Error
  final case class NotANumber(string: String) extends Error
  final case object NoZeroReciprocal extends Error

  def parse(s: String): Either[Error, Int] =
    if (s.matches("-?[0-9]+")) Either.right(s.toInt)
    else Either.left(NotANumber(s))

  def reciprocal(i: Int): Either[Error, Double] =
    if (i == 0) Either.left(NoZeroReciprocal)
    else Either.right(1.0 / i)

  def stringify(d: Double): String = d.toString

  def magic(s: String): Either[Error, String] =
    parse(s).flatMap(reciprocal).map(stringify)
}

import EitherStyleWithAdts._

val result = magic("2") match {
  case Either.Left(NotANumber(_)) ⇒ "Not a number!"
  case Either.Left(NoZeroReciprocal) ⇒ "Can't take reciprocal of 0!"
  case Either.Right(result) ⇒ s"Got reciprocal: ${result}"
}

```

### SMALL & LARGE

Once you start using Either for error handling, you may quickly run into an issue where you need to call into two separate modules which give back separate kinds of errors.

```scala
sealed abstract class DatabaseError
trait DatabaseValue

object Database {
  def databaseThings(): Either[DatabaseError, DatabaseValue] = ???
}

sealed abstract class ServiceError
trait ServiceValue

object Service {
  def serviceThings(v: DatabaseValue): Either[ServiceError, ServiceValue] = ???
}
```

`def doApp = Database.databaseThings().flatMap(Service.serviceThings)` 
does not work. only gives us `Either[Object, ServiceValue]`

Solution1: Application-wide errors
```scala
sealed abstract class AppError
final case object DatabaseError1 extends AppError
final case object DatabaseError2 extends AppError
final case object ServiceError1 extends AppError
final case object ServiceError2 extends AppError

trait DatabaseValue

object Database {
  def databaseThings(): Either[AppError, DatabaseValue] = ???
}

object Service {
  def serviceThings(v: DatabaseValue): Either[AppError, ServiceValue] = ???
}

def doApp = Database.databaseThings().flatMap(Service.serviceThings)
```

Solution2: ADT all the way down

```scala
sealed abstract class DatabaseError
trait DatabaseValue

object Database {
  def databaseThings(): Either[DatabaseError, DatabaseValue] = ???
}

sealed abstract class ServiceError
trait ServiceValue

object Service {
  def serviceThings(v: DatabaseValue): Either[ServiceError, ServiceValue] = ???
}

sealed abstract class AppError
object AppError {
  final case class Database(error: DatabaseError) extends AppError
  final case class Service(error: ServiceError) extends AppError
}

// AND use a leftMap to aggregate left side error
def doApp: Either[AppError, ServiceValue] =
  Database.databaseThings().leftMap(AppError.Database).
    flatMap(dv => Service.serviceThings(dv).leftMap(AppError.Service))

def awesome =
  doApp match {
    case Either.Left(AppError.Database(_)) => "something in the database went wrong"
    case Either.Left(AppError.Service(_)) => "something in the service went wrong"
    case Either.Right(_) => "everything is alright!"
  }
```

Another shortcut to handle exception

```scala
val xor: Xor[NumberFormatException, Int] =
  try {
    Xor.right("abc".toInt)
  } catch {
    case nfe: NumberFormatException => Xor.left(nfe)
  }

val xor: Xor[NumberFormatException, Int] =
  Xor.catchOnly[NumberFormatException]("abc".toInt)

Xor.catchOnly[NumberFormatException]("abc".toInt).isRight // false
Xor.catchNonFatal(1 / 0).isLeft  // true
```


in `cats.syntax.EitherOps`:
Either.left("asda").leftMap(_.reverse)


## 11. Validate

PARALLEL VALIDATION: Our goal is to report any and all errors across independent bits of data

For instance, when we ask for several pieces of configuration, each configuration field can be validated separately from one another. 

```scala
case class ConnectionParams(url: String, port: Int)

for {
  url <- config[String]("url")
  port <- config[Int]("port")
} yield ConnectionParams(url, port)
```

### Example

```scala
trait Read[A] {
  def read(s: String): Option[A]
}

object Read {
  def apply[A](implicit A: Read[A]): Read[A] = A

  implicit val stringRead: Read[String] =
    new Read[String] { def read(s: String): Option[String] = Some(s) }

  implicit val intRead: Read[Int] =
    new Read[Int] {
      def read(s: String): Option[Int] =
        if (s.matches("-?[0-9]+")) Some(s.toInt)
        else None
    }
}
```

Errors
```
sealed abstract class ConfigError
final case class MissingConfig(field: String) extends ConfigError
final case class ParseError(field: String) extends ConfigError
```

DataType
```scala
sealed abstract class Validated[+E, +A]

object Validated {
  final case class Valid[+A](a: A) extends Validated[Nothing, A]
  final case class Invalid[+E](e: E) extends Validated[E, Nothing]
}
```

Parser
```scala
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

case class Config(map: Map[String, String]) {
  def parse[A: Read](key: String): Validated[ConfigError, A] =
    map.get(key) match {
      case None => Invalid(MissingConfig(key))
      case Some(value) =>
        Read[A].read(value) match {
          case None => Invalid(ParseError(key))
          case Some(a) => Valid(a)
        }
    }
}
```

Use semigroup as abstraction over binary aggregate error
```scala
import cats.Semigroup

def parallelValidate[E: Semigroup, A, B, C](v1: Validated[E, A], v2: Validated[E, B])(f: (A, B) => C): Validated[E, C] =
  (v1, v2) match {
    case (Valid(a), Valid(b)) => Valid(f(a, b))
    case (Valid(_), i @ Invalid(_)) => i
    case (i @ Invalid(_), Valid(_)) => i
    case (Invalid(e1), Invalid(e2)) => Invalid(Semigroup[E].combine(e1, e2))
  }
```

Time to parse
```scala
import cats.SemigroupK
import cats.data.NonEmptyList
import cats.implicits._

implicit val nelSemigroup: Semigroup[NonEmptyList[ConfigError]] =
  SemigroupK[NonEmptyList].algebra[ConfigError]

implicit val readString: Read[String] = Read.stringRead
implicit val readInt: Read[Int] = Read.intRead

val config = Config(Map(("url", "127.0.0.1"), ("port", "1337")))

val valid = parallelValidate(
  config.parse[String]("url").toValidatedNel,
  config.parse[Int]("port").toValidatedNel
)(ConnectionParams.apply)

```


### Improvements

#### 1. parallelValidate => Apply

look at ValidatedApplicative signature
```
def parallelValidate[E: Semigroup, A, B, C](v1: Validated[E, A], v2: Validated[E, B])(f: (A, B) => C): Validated[E, C] 

def map2[F[_], A, B, C](fa: F[A], fb: F[B])(f: (A,B) => C): F[C]
```

we can define an Applicative instance
```scala
import cats.Applicative

implicit def validatedApplicative[E: Semigroup]: Applicative[Validated[E, ?]] =
  new Applicative[Validated[E, ?]] {
    def ap[A, B](f: Validated[E, A => B])(fa: Validated[E, A]): Validated[E, B] =
      (fa, f) match {
        case (Valid(a), Valid(fab)) => Valid(fab(a))
        case (i @ Invalid(_), Valid(_)) => i
        case (Valid(_), i @ Invalid(_)) => i
        case (Invalid(e1), Invalid(e2)) => Invalid(Semigroup[E].combine(e1, e2))
      }

    def pure[A](x: A): Validated[E, A] = Validated.valid(x)
    def map[A, B](fa: Validated[E, A])(f: A => B): Validated[E, B] = fa.map(f)
    def product[A, B](fa: Validated[E, A], fb: Validated[E, B]): Validated[E, (A, B)] =
      ap(fa.map(a => (b: B) => (a, b)))(fb)
  }
```

Now we get access to all goodness of Applicative, which includes map{2-22}, Cartesian `|@|`

Thus we can easily extened to multiple Configurations
```s
import cats.Apply
import cats.data.ValidatedNel

implicit val nelSemigroup: Semigroup[NonEmptyList[ConfigError]] =
  SemigroupK[NonEmptyList].algebra[ConfigError]

val config = Config(Map(("name", "cat"), ("age", "not a number"), ("houseNumber", "1234"), ("lane", "feline street")))

case class Address(houseNumber: Int, street: String)
case class Person(name: String, age: Int, address: Address)

val personFromConfig: ValidatedNel[ConfigError, Person] =
  Apply[ValidatedNel[ConfigError, ?]].map4(
    config.parse[String]("name").toValidatedNel,
    config.parse[Int]("age").toValidatedNel,
    config.parse[Int]("house_number").toValidatedNel,
    config.parse[String]("street").toValidatedNel
  ) {
    case (name, age, houseNumber, street) => Person(name, age, Address(houseNumber, street))
  }
```

#### 2. shall we make it a monad?

```scala
import cats.Monad

implicit def validatedMonad[E]: Monad[Validated[E, ?]] =
  new Monad[Validated[E, ?]] {
    def flatMap[A, B](fa: Validated[E, A])(f: A => Validated[E, B]): Validated[E, B] =
      fa match {
        case Valid(a) => f(a)
        case i @ Invalid(_) => i
      }

    def pure[A](x: A): Validated[E, A] = Valid(x)
  }
```

Note: it is auto applicative as well
- `def map[A, B](fa: F[A])(f: A => B): F[B] = flatMap(fa)(f.andThen(pure))`
- `def ap[A, B](fa: F[A])(f: F[A => B]): F[B] = flatMap(fa)(a => map(f)(fab => fab(a)))`
    + however this `ap` will short circuit
    + overwrite ap makes monad not consistent

#### 3. a few more methods

andThen ( similiar to flatMap, pass when success)
```scala
val houseNumber = config.parse[Int]("house_number").andThen { n =>
  if (n >= 0) Validated.valid(n)
  else Validated.invalid(ParseError("house_number"))
}
```

withXor (turn Xor temperarily)
```scala
import cats.data.Xor

def positive(field: String, i: Int): ConfigError Xor Int = {
  if (i >= 0) Xor.right(i)
  else Xor.left(ParseError(field))
}
```


