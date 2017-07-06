# Programming Animals
------------------------------------------------------------------------------------------------

## Contents
> [1.Introduct](#1-introduction)
> [8.OOP](#8-oop)
> [20. DSL](#20-dsl)

## 1. Introduction

### A taste of concurrency

Define Shape class
```scala
case class Point(x:Double, = 0.0, y: Double = 0.0)
abstract class Shape() {
    def draw(f: String => Unit): Unit = f(s"draw: ${this.toString}")
}
case class Circle(center: Point, radius: Double) extends Shape
case class Rectangle(lowerLeft: Point, height: Double, width: Double) extends Shape
case class Triangle(point1: Point, point2: Point, point3: Point)
```

Shape drawing Actor
```scala
Object Messages {       // define most messages we will send between actors
    object Exit         // no state, act as flag            
    object Finished
    case class Response(message: String)    // send string message
}

import akka.actor.Actor

class ShapesDrawingActor extends Actor {
    import Messages._

    def receive = {                         // message handler
        case s: Shape =>
            s.draw(str => println(s"ShapesDrawingActor: $str"))     // call draw
            sender ! Response(s"ShapesDrawingActor: $s drawn")      // response to sender
        case Exit =>
            println(s"ShapesDrawingActor: exiting...")
            sender ! Finished
        case unexpected => // default. unexpected: Any
            val response = Response(s"Error: Unknown message: $unexcepted")
            println(s"ShapesDrawingActor: $response")
            sender ! response
    }
}


```

Shapes Drawing Driver
```scala
import akka.actor.{Actor, Props, ActorRef, ActorSystem}
import com.typesafe.config.ConfigFactory

// Message used only in this file
private object Start // private initialization

object ShapesDrawingDriver {
    def main(args: Array[String]) {
        val system = ActorSystem("DrawingActorSystem", ConfigFactory.load())
        val drawer = system.actorOf(
                Props(new ShapesDrawingActor), "drawingActor")
        val driver = system.actorOf(
                Props(new ShapesDrawingDriver(drawer)), "drawingService")
        driver ! Start
    }
}

class ShapesDrawingDriver(drawerActor: ActorRef) extends Actor {
    import Messages._

    def receive = {
        case Start =>
            drawerActor ! Circle(Point(0.0,0.0), 1.0)
            drawerActor ! Rectangle(Point(0.0, 0.0), 2, 5)
            drawerActor ! 3.14159
            drawerActor ! Triangle(Point(0.0, 0.0), Point(2.0, 0.0), Point(1.0, 2.0))
            drawerActor ! Exit
        case Finished =>
            println(s"ShapesDrawingDriver: cleaning up...")
            context.system.shutdown()
        case response: Response =>
            println(s"ShapesDrawingDriver: Response = $response")
        case unexpected =>
            println(s"ShapesDrawingDriver: ERROR: Received an unexpected message = $unexpected")
    }
}
```

------------------------------------------------------------------------------------------------
## 2. Type Less, Do More

Scala features
- Semicolon delimiters can be infered
    + use linebreak sign in REPL `= { , . + `
- define immutability with `var val`
- Ranges
    + `1 to 10` inclusive, `1 until 10` exclusive 
    + `10 to 1 by -3` , `'a' to 'g' by 3` step
- Options
    + avoid nullpointer
- PartialFunction
    + can chain together `pf1 orElse pf2 orElse pf3`
    + `val pf1: PartialFunction[Any,String] = {case s:String => "Yes"}`
    + `try {pf(x).toString} catch {case _:MatchError => "Error!"}`
    + `pf.isDefinedAt(x)`

### Method Declaration

Code block syntax
```
def draw(offset:Point)(f: String => Unit)
draw(point){ str => println(str)}
```

Curried Type Inference
```
def m1[A](a: A, f: A => String) = f(a)
def m2[A](a: A)(f: A => String) = f(a)
m1(100, i => s"$i + $i")  // error, missing parameter type
m2(100)(i => s"$i + $i")  // 100 + 100
```

Nesting method definition & Recursion
- let compiler tell if correctly tail recursion with 
```scala
import scala.annotation.tailrec
def factorial(i: Int): Long = {
    @tailrec
    def fact(i:Int, accumulator: Int): Long = 
        if (i<=1) accumulator else fact(i-1, i*accumulator)
    fact(i,1)
}
```

### Type Inference
```scala
val intToStringMap = new HashMap[Integer, String]
def joiner(strings: String*): String =  strings.mkString("-")
def joiner(strings: List[String]): String = joiner(strings :_*)
def double(i: Int) {2*i} // return Unit
def double(i: Int)={2*i} // return Int
```

### Reserved Words
abstract, case, catch, class, def, do, else, extends, false, final, finally, for, forSome, if, implicit, import, lazy, match, new, null, object, override, package, private, protected, requires, return, sealed, super, this, throw, trait, try, true, type, val, var, while, with, yield
```
= assignment
=> function literals
<- for comprehension in generator expression
@ annotations
# type projections
>: parameterized and abstract type declaration to constraint the allowed types
<% parameterized and abstract type view bounds declaration
<: parameterized and abstract type declaration to constraint the allowed types
```

### Sealed Class Hierarchies

- design a class with only two valid subtypes
- prevent user from creating them own subtype

```scala
sealed abstract class Option[+A]
// + means covariant 
```

------------------------------------------------------------------------------------------------
## 3. Rounding Out Basics

### Domain Specific Languages

e.g. ScalaTest

```scala
import org.scalatest.{ FunSpec, ShouldMatchers}
calss NerdFinderSpec extends FunSpec with ShouldMatchers {
    describe ("nerd finder") {
        it ("identify nerds from a List") {
            val actors = List("Rick Moranis", "James Dean", "Woody Allen")
            val finder = new NerdFinder(actors)
            finder.findNerds shouldEqual List("Rick Moranis", "Woody Allen")
        }
    }
}
```

### Call by Name, Call by Value

```scala
import scala.language.reflectiveCalls
import scala.util.control.NonFatal

object manage {
    def apply[R <: {def close():Unit}, T](resource: => R)(f: R=>T) = {
        var res: Option[R] = None
        try {
            res = Some(resource)
            f(res.get)
        } catch {
            case NonFatal(ex) => println(s"Non fatal exception! $ex")
        } finally {
            if (res != None) {
                println(s"Closing resource...")
                res.get.close
            }
        }
    }
}

object TryCatchARM {
    /** Usage: Scala rounding.TryCatch filename1 filename2 ...**/
    def main(args: Array[String]) = {
        args foreach (arg => countLines(arg))
    }

    import scala.io.Source

    def countLines(fileName: String) = {
        println()
        manage(Source.fromFile(fileName)) { source =>
            val size = source.getLInes.size
            println(s"file $fileName has $size lines")
            if(size > 20) throw new RuntimeException("Big file!")
        }
    }
}
```

- lovely little bit of separation of concern
    + small case object naming `manage`
        * when using it as function, immitate built-in operator
    + subclass notion
        * `<:` means R is subclass of structural type with close method ( Closable )
    + by-name parameter (lazy)
        * `resource :=>R` as a function that we call without parameters
        * `resource :() => R` for short
        * usually scala use call-by-value semantic
        * but call by name defer the evaluation until `res = Some(resource)`

lazy
```
object ExpensiveResource {
    lazy val resource: Int = init()
    def init(): Int = {
        //do something expensive
        0
    }
}
```

------------------------------------------------------------------------------------------------
## 4. Pattern Matching

```scala
something match {
    case d: Double          => s"a double: $d"
    case `y`                => "found y!"           // scope variable for value held
    case _: Int | _: Double => "a number"
    case head +: tail       => s"$head +: ${seqToString(tail)}"     // match seq
    case (first, second)    => s"other $first $second"              // match tuple
    case _ if i%2 == 0      => s"case clause Guards" 
    case Person(name,_)     => s"match case class"
    case Where(col, val1, vals @ _*)         => "Complex match with class"
    case p @ Person(_, _, a@Address(_,_,_))  => "Complex match with class"
}

```

### unapply

- how is matching case class work?
    + Scala looks for `Person.unapply` and `Address.unapply`
    + which return Option[TupleN[...]]
    + after 2.11 can replace Option with any class with `isEmpty:Boolean` and `get:T` method

```scala
Object Person {
    def apply(name:String, age:Int, address:Address) = 
        new Person(name, age, address)

    def unapply(p: Person): Option[Tuple3[String, Int, Address]] = 
        Some((p.name, p.age, p.address))
}
```

### regex

```scala
val BookExtractorRE = """Book: title=([^,]+),\s+author=(.+)""".r
val MagaExtractorRE = """Magazine: title=([^,]+),\s+issue=(.+)""".r

val catalog = Seq(
    "Book: title=Programming Scala Second Edition, author=Dean Wampler",
    "Magazine: title=The New Yorker, issue=January 2014",
    "Unknown: text=Who put this here???"
)

for (item <- catalog) {
    item match {
        case BookExtractorRE(title, author) =>
            println(s"$title - $author")
        case MagaExtractorRE(title, issue) =>
            println(s"$title - $issue")
        case entry =>
            println(s"Unrecognized entry")
    }
}

```

### List[String] & List[Double]

Scala runs on JVM, these warnings result from JVM's type erasure, a historical legacy of Java's introduction of generics in Java 5. JVM byte code doesn't retain information about actual type parameters used for instance of generic types like List, in order to avoid breaking older code.

So it can not check at runtime whether it is a List[Double] or List[String], so first `case seq: Seq[Double]` will match for any seq, making `case seq: Seq[String]` unreachable

## 5. Implicits

### 5.1 implicit arguments
implicit: a ommitable argument, will use a type-compatible implicit value from the scope

only last argument list can have implicits
can put many implicits in last ()

```scala
def calcTax(amount: Float)(implicit rate: Float): Float = amount * rate
implicit val currentTaxRate = 0.08F
...
val tax = calcTax(50000F) // 4000.0
```

Using implicitly
```scala
import math.Ordering

case class MyList[A](list: List[A]) {
    def sortBy1[B](f: A=>B)(implicit ord: Ordering[B]): List[A] = 
        list.sortBy(f)(ord)

    def sortBy2[B: Ordering](f: A=>B): List[A] =
        list.sortBy(f)(implicitly[Ordering[B]])
}
```

### 5.2 Scenarios for implicits

#### Execution Contexts
    + Future.apply use an ExecutionContext
    + `apply[T](body:=>T)(implicit executor: ExecutionContext): Future[T]`
    + import scala.concurrent.ExecutionContext.Implicits.global

#### Capabilities
    + user interface menu item
    + `def createMenu(implicit session:Session): Menu`
    + `if (session.loggedin())`

#### Constraining Allowed instances
- scala collection api use this to solve a design problem
    + many methods supported by concrete collection classes are implemented by parent types
    + like map is implemented in generic trait `TraversableLike` & mixed into all collections
    + how to return same collection type we start with?
- Scala API convention: passing `builder` as implicit arg to map
    + builder knows how to construct a new collection of the same type

```scala
trait TraversableLike[+A, +Repr] extends ... {
    def map[B, That](f: A=>B)(
        implicit bf: CanBuildeFrom[Repr, B, That]): That = {...}
}
```

- Repr is the actual collection type used internally to hold the items
- That is the type parameter of target collection we want to create
- create collection types with your own CanBuildFroms, reuse TraversableLike.map

#### Database API like Cassandra

```scala
package database_api {
    case class InvalidColumnName(name:String) extends RuntimeException(s"Invalid $name")
    trait Row {
        def getInt   (colName:String): Int
        def getDouble(colName:String): Double
        def getText  (colName:String): String
    }
}

package javadb {
    import database_api._

    case class JRow(representation: Map[String, Any]) extends Row {
        private def get(colName:String): Any = 
            representation.getOrElse(colName, throw InvalidColumnName(colName))
        def getInt   (colName:String): Int    = get(colName).asInstanceOf[Int]
        def getDouble(colName:String): Double = get(colName).asInstanceOf[Double]
        def getText  (colName:String): String = get(colName).asInstanceOf[String]
     }

     object JRow {
        def apply(pairs: (String, Any)*) = new JRow(Map(pairs :_*))
     }
}
```

- why not using a single get[T] method with one of allowed types
    + would promote more uniform invocation
    + not need case statement to pick up method
- Java makes distinction between primitive & reference
    + cannot use primitive in parameterized methods like get[T]
    + have to use boxed `java.lang.Integer` instead of `int`, with boxing overhead (speed)

```scala
package scaladb {
    object implicits {
        import javadb.JRow
        implicit class SRow(jrow: JRow) {
            def get[T](colName: String)(implicit toT: (JRow, String) => T): T =
                toT(jrow, colName)
        }
        implicit val jrowToInt: (JRow, String) => Int =
            (jrow: JRow, colName: String) => jrow.getInt(colName)
        implicit val jrowToDouble: (JRow, String) => Double =
            (jrow: JRow, colName: String) => jrow.getDouble(colName)
        implicit val jrowToString: (JRow, String) => String =
            (jrow: JRow, colName: String) => jrow.getString(colName)

    }
    object DB {
        import implicits._

        def main(args:Array[String]) = {
            val row = javadb.JRow("one"->1, "two"->2.2, "three"->"THREE!")
            val oneValue1: Int = row.get("one")
            val oneValue2 = row.get[Int]("one")
        }
    }
}
```

#### Implicit Evidence

- just constrain the allowed types and not provide additional processing capability
- need evidence that proposed types satisfy our requirements

- toMap method available for all traversable collections
    + but we can't allow if sequence is not in pairs
- ev is the evidence
    + `<:<` is a type to resemble type parameter constraint
    + `<:<[A, B]` has same infix notion `A <:< B`
    + shows implicit evidence will be synthesized by compiler only if `A <: (T,U)`

```scala
trait TraversableOnce[+A] {
    def toMap[T,U](implicit ev: <:<[A, (T,U)]): immutable.Map[T,U]
    def toMap[T,U](implicit ev: A <:< (T,U) ): immutable.Map[T,U]
}
```

#### Phantom Types

- useful for defining work flows that must proceeed in a particular order
- e.g. payroll deduct pre-tax first, than tax and post-tax

```scala
sealed trait PreTaxDeductions
sealed trait PostTaxDeductions
sealed trait Final
// sealed trait with no data, no classes, only serve as markerss
// only used as type token

case class Employee( name: String, annualSalary:Float, taxRate:Flat, ... )

case class Pay[Step](employee: Employee, netPay: Float)

object Payroll {
    // Biweekly paychecks
    def start(employee: Employee): Pay[PreTaxDeductions] =
        pay[PreTaxDeductions](employee, employee.annualSalary/26.0f)

    def minusInsurancce(pay: Pay[PreTaxDeductions]): Pay[PreTaxDeductions] = {
        val newNet = pay.netPay - pay.employee.insurancePremiuumsPerPayPeriod
        pay copy (netPay = newNet)
    }    

    def minus401k(pay: Pay[PreTaxDeductions]): Pay[PreTaxDeductions] = {
        val newNet = pay.netPay - pay.employee._401kDeductionRate * pay.netPay
        pay copy (netPay = newNet)
    }

    def minusTax(pay: Pay[PreTaxDeductions]): Pay[PostTaxDeductions] = {
        val newNet = pay.netPay - pay.employee.taxRate * pay.netPay
        pay copy (netPay = newNet)
    }

    def minusFinalDeductions(pay: Pay[PostTaxDeductions]): Pay[Final] = {
        val newNet = pay.netPay - pay.employee.postTaxDeductions
        pay copy (netPay = newNet)
    }
}

object Pipeline {
    implicit class toPiped[V](value: V) {
        def |>[R](f: V => R) = f(value)
    }
}
object CalculatePayroll2 {
    def main(args: Array[String]) = {
        import Pipeline._
        import Payroll._
        val e = Employee("Buck Smith", 10000.0F, 0.25F, 200F, 0.10F, 0.05F)
        val pay = start(e)  |>
              minus401k     |>
              minusInsurance|>
              minusTax      |>
              minusFinalDeductions
        val twoWeekGross = e.annualSalary / 26.0F
        val twoWeekNet = pay.netPlay
        ....
    }
}

```

### 5.3 Implicit Conversion

- create implicit wrapper class for extra method
- e.g. the arrow notion for map
    + final keyword prevent subclasses declared
- the compiler
    + sees try to call a -> method on String
    + String has no -> method, look for implicit conversion in scope
    + finds ArrowAssoc
    + resolve 1 part and confirms type matches Map.apply requires (pair)

```scala
implicit final class ArrowAssoc[A](val self:A){
    def ->[B](y:B): Tuple2[A,B] = Tuple2(self,y)
}
// Before Scala 2.10, use implicit method(2 step)
implicit def any2ArrowAssoc[A](x:A): ArrowAssoc[A] = new ArrowAssoc(x)
```

```
// Campanion object is also searched
case class Foo(s:String)
object Foo {
    implicit def fromString(s:String): Foo = Foo(s)
}
class O {
    def m1(foo:Foo) = println(foo)
    def m(s: String) = m1(s)
}
```

#### String Interpolator example
```scala
import scala.util.parsing.json._

object Interpolators {
    // define inside object to limit scope (safety)
    implicit class jsonForStringContext(val sc:StringContext) {
        def json(values: ANy*): JSONObject = {
            val keyRE = """^[\s{,]*(\s+):\s*"""
            val keys = sc.parts map {
                case keyRE(key) => key
                case str => str
            }
            val kvs = keys zip values // zip Seqs
            JSONObject(kvs.toMap)
        }
    }
}

import Interpolators._
val jsonobj = json"{name: Dean Wampler, book: Programming Scala, Second Edition}"
```

#### Expression Problem

- we added a new method to all types without editing source for any of them
- Expression Problem, 
    + a term coined by Philip Walder
    + desire to extend modules without modifying their source code
- OOP achieve this by subtype polymorphism
    + base types declare behavior in an abstraction
    + subtypes implement appropriate variations of behavior
- draw back of subtyping
    + should we have that behavior defined in type hierarchy in the first place?
    + what if behavior only needed in a few contexts, just a burden for most?
        * unused code take up system resource
        * inevitable most defined behaviors will be refined over time
- Single Responsibility Principle
    + design principle: define abstractions and implementing types with just a single behavior
    + Scala trait & mixin helps implement this

### Type Class Pattern
```scala
case class Address(street: String, city:String)
trait ToJSON {
    def toJSON(level: Int = 0): String
    val INDENTATION = " "
    def indentation(level: Int = 0): (String, String) =
        (INDENTATION * level, INDENTATION * (level + 1))
}

implicit class AddressToJSON(address:Adress) extends ToJSON {
    def toJSON(level: Int = 0):String = {
        val (outdent, indent) = indentation(level)
        s"""{
          |${indent}"street": "${address.street}",
          |${indent}"city":   "${address.city}"
          |$outdent}""".stripMargin
    }
}
```

implicit have a narrow purpose, just add extra method, so cannot use `implicit case class`

### Best Practice & Notes

- always specify the return type of an implicit conversion method
- compiler does a few convenient(may troublesome) conversion for you
    + when define + method with wrong type, compiler will try to call toString()
    + compiler will auto-tuple arguments to a method when needed
        * pass two arg to a method that accept tuple (will warning)


------------------------------------------------------------------------------------------------
## 6. Function Programming

```scala
(1 to 10) filter (_ % 2 == 0) map (_ * 2) reduce (_ * _)

var factor = 2
val multiplier = (i: Int) => i * factor
(1 to 10) filter (_ % 2 == 0) map multiplier reduce (_ * _)
```

### TailRecursion

```scala
// NOT @tailrec
def factorial(i: BigInt): BigInt = 
    if(i == 1) i
    else i*factorial(i-1)

def factorial(i: BigInt): BigInt = {
    @tailrec
    def fact(i: BigInt, acc: BigInt): BigInt = 
        if(i==1) acc else fact(i-1, i*acc)

    fact(i, 1)
}
```

### Trampoline object
- do a back and forth recursion call converted to loop
```scala
import scala.util.control.TailCalls._

def isEven(xs: List[Int]): TailRec[Boolean] = 
    if(xs.isEmpty) done(true) else tailcall(isOdd(xs.tail))

def isOdd(xs: List[Int]): TailRec[Boolean] =
    if(xs.isEmpty) done(false) else tailcall(isEven(xs.tail))
```

### Currying and other transformations

```scala
//cat1 is more readable
//cat2 don't need underscore when trated as partially applied function
def cat1(s1: String)(s2: String) = s1 + s2
def cat2(s1: String) = (s2: String) => s1 + s2

def cat3(s1: String, s2: String)
val cat3Curried = (cat3 _).curried
val cat3Uncurried = Function.uncurried(cat3Curried)

def mult(d1:Double,d2:Double,d3:Double) = d1*d2*d3
val d3 = (2.2,3.3,4.4)
mult(d3._1, d3._2, d3._3)
val multTupled = Function.tupled(mult _)
multTupled(d3)
val multUntupled = Function.untupled(multTupled)

// partial function & fucntion with option
val finicky: PartialFunction[String, String] = {
    case "finicky" => "FINICKY"
} 
// MatchError if not match
val finickyOption = finicky.lift
// take am Option, None if not match
```

## 7. For Comprehension in Depth
```scala
object RemoveBlanks {
    def apply(path:String, compressWhiteSpace: Boolean = false): Seq[String] = 
        for {
            line <- scala.io.Source.fromFile(path).getLines.toSeq
            // flatMap Seq to lines
            if line.matches("""^\s*$""") == false
            // filter lines
            line2 = if (compressWhiteSpace) line replaceAll ("\\s+", " ")
                    else line
            // define local variable, compress to single space
        } yield line2

    def main(args:Array[String]) = for {
        path2 <- args
        (compress, path) = if(path2 startsWith "-") (true, apth2.substring(1))
                            else (false, path2)
        // define local variable, file path start with - means enabled
        line <- apply(path, compress)
    }
}
```

the comprehension is using @ syntax
```scala
val z @ (x,y) = (1->2)  // z=(1,2)  x=1  y=2

for {
    (key,value)<-map
    i10 = value + 10
} yield (i10)

for {
    (i, i10) <- for {
        x1 @ (key, value) <- map
    } yield {
        val x2 @ i10 = value + 10
        (x1, x2)
    }
} yield (i10)
```

using options
```scala
val results: Seq[Option[Int]] = Vector(Some(10), None, Some(20))

val results2 = for {
    Some(i) <- resi;
 } yield (2*i)
// Returns: Seq[Int] = Vector(20,40)
// compiler translate to a filter(remove None) and a direct map

def positive(i: Int): Option[Int] = 
    if (i>0) Some(i) else None

for {
    i1 <- positive(5)
    i2 <- positive(10 * i1)
    i3 <- positive(-1 * i2)
    i4 <- positive(5 + i3)
} yield (i1 + i2 + i3 + i4)
// None, i3 return None and following mapping stops
```

using either: show where error is & allow exception
```scala
def positive(i: Int): Either[String, Int] = 
    if (i>0) Right(i) else Left(s"nonpositive $i")

for {
    i1 <- positive(5)
    i2 <- positive(10 * i1).right
    i3 <- positive(-1 * i2).right
    i4 <- positive(5 + i3).right
} yield (i1 + i2 + i3 + i4)
// nonpositive -50

def addInts(s1:String, s2:String): Either[NumberFormatException,Int] =
  try {
    Right(s1.toInt + s2.toInt)
  } catch {
    case nfe: NumberFormatException => Left(nfe)
  }

def addInts(s1:String, s2:String): Either[Throwable,Int] =
  Try(Right(s1.toInt + s2.toInt)) match {
    case Success(v) => v
    case Failure(e) => Left(e)
  }
```

using Try
```scala
def positive(i: Int): Try[Int] = Try{
    assert (i>0, s"nonpositive number $i")
    i
}

for {
    i1 <- positive(5)
    i2 <- positive(10 * i1)
    i3 <- positive(-1 * i2)
    i4 <- positive(5 + i3)
} yield (i1 + i2 + i3 + i4)
// java.lang.AssertionError: assertion failed: nonpositive -50
```

## 8. OOP

### Reference vs Value

- Java syntax models how JVM implements data
    + stored on the stack or CPU registers
        * primitives: short, int, long, float, double, boolean, char, byte
        * keyword void
    + allocated on the heap, refer to heap locations
        * reference types
        * no structural types can live in stack as C or C++
        * created with new keyword
- Scala
    + root type `Any`
    + all references types `AnyRef`, close to java root type `Object`
    + all value types `AnyVal`, close to primitive
        * Short Int Long Float Double Boolean Char Byte Unit
        * actually using JVM primitives

- Scala type class wrappers will turn primitives into reference types
- How to provide wrapper method while keeping the stack performance?

### Value Classes
```
class Dollar(val value: Float) extends AnyVal {
    override def toString = "$.2f".format(value)
}
```

- To be a valid value class
    + value class has one and only one val argument
    + type of argument must not be a value class it self
    + doesn't define secondary constructors
    + defines only methods
    + can't override equals and hashCode
    + define no nested traits, classes, objects
    + cannot be subclassed
    + can only inherit from universal trait
    + must be a top=level type or member of an object
- Universal trait
    + derives from Any
    + defines only methods
    + does no initialization

```scala
trait Digitizer extends Any {
    def digits(s: String): String = s.replaceAll("""\D""", "")
}

trait Formatter extends Any {
    def format(areaCode:String, exchange:String, subnumber: String): String =
        s"($areaCode) $exchange-$subnumer"
}

class USPhoneNumber(Val s: String) extends AnyVal
    with Digitizer with Formatter {
        override def toString = {
            val digs = digits(s)
            val areaCode = digs.substring(0,3)
            val exchange = digs.substring(3,6)
            val subnumber = digs.substring(6,10)
            format(areaCode, exchange, subnumber)
        }
    }

val number = new USPhoneNumber("987-654-3210")
```

### Type Inheritance
```scala
abstract class BulkReader {
    type In
    val source: In
    def read: String
}
class StringBulkReader(val source: String) extends BulkReader {
    type In = String
    def read: String = source
}
class FileBulkReader(val source: java.io.File) extends BulkReader {
    type In = java.io.File
    def read: String = {...}
}
//versus
abstract class BulkReader[In]
class StringBulkReader extends BulkReader[String]
```

My observation so far about abstract type members is that they are primarily a better choice than generic type parameters when you want to let people mix in definitions of those types via traits.

### Constructor

secondary constructor calls primary constructor
```scala
case class Address(street: String, city:String, state: String, zip:String) {
    def this(zip:String) = 
        this("", Address.zipToCity(zip), Address.zipToState(zip), zip)
}

object Address {
    def zipToCity
    def zipToState
}
```

overload method like apply in campanion object to provide convient constructors
```
case class Person(name:String, age:Option[Int]=None, address:Option[Address]=None)
object Person {
    def apply(name:String): Person = new Person(name)
    def apply(name:String, age:Int) = new Person(name,Some(age))
}
```

### Call Parent Class Contructors

Bad OOP Design
```scala
case class Person(
        name:String,
        age: Option[Int] = None,
        address: Option[Address] = None
    ){
        override def toString = 
                s"People($name, $age, $address)"
        }}

class Employee(
        name:String,
        age: Option[Int] = None,
        address: Option[Address] = None
        val title: String = "unknown",
        val manager: Option[Employee] = None) extends Person(name,age,address) {
            override def toString = 
                s"Employee($name, $age, $address, $title, $manager)"
        }

// super can be used to invoke overriden methods, but not constructors
```

- this code smells
    + declaration mix val keywords and no keywords
    + we can't derive one case class from another
    + is Employee and Person equal or not?
- good OO design favors composition over inheritance

```scala
trait PersonState {
    val name:String
    val age: Option[Int]
    val address: Option[Address]
}

case class Person(
    name:String
    age:Option[Int] = None
    address: Option[Address] = None) extends PersonState

trait EmployeeState {
    val title: String
    val manager: Option[Employee]
}

case class Employee(
    name: String
    age: Option[Int] = None
    address: Option[Address] = None
    title: String = "unknown"
    manger: Option[Employee] = None) extends PersonState with EmployeeState

```

### Nested Types
```scala
object Database {
    case class ResultSet()
    case class Connection()
    case calss DatabaseException(message: String, cause: Throwable) extends
        RuntimException(message, cause)

    sealed trait Status
    case object Disconnected extends Status
    case class Connected(connection:Connection) extends Status
    case class QuerySucceed(results: ResultSet) extends Status
    case class QueryFailed(e: DatabaseException) extends Status
}

class Database {
    import Database._
    def connect(server:String): Status
    def disconnect(): Status
    def queyr(): Status
}
```

## 9. Trait

GUI toolkit example
```scala
class ButtonWithCallbacks(val label:String, val callbacks: List[()=>Unit]=Nil) extends Widget {
    def click(): Unit = { 
        updateUI()
        callbacks.foreach(f => f())
    }

    protected def updateUI(): Unit = { ... }
}

object ButtonWithCallbacks {
    def apply(label:String, callback: ()=>Unit) = 
        new ButtonWithCallbacks(label, List(callback))
    def apply(label:String) = 
        new ButtonWithCallbacks(label, Nil)
}
```

OOP version
```
trait Observer[-State] {
    def receiveUpdate(state:State): Unit
}

trait Subject[State] {
    private var observers: List[Observer[State]] = Nil

    def addObserver(observer: Observer[State]): Unit =   // mutable, not thread safe
        obsevers ::= observer 

    def notifyObservers(state:State): Unit = 
        observers foreach (_.receiveUpdate(state))
}

class Button(val label:String) extends Widget {
    def click(): Unit = updateUI()
    def updateUI(): Unit =  ... {}
}

class ObervableButton(name: String) extends Button(name) with Subject[Button]{
    override def click(): Unit = {  // mix in observability
        super.click()
        notifyObservers(this)
    }
}

class ButtonCountObserver extends Observer[Button] {
    var count = 0
    def receiveUpdate(state: Button): Unit = count +=1
}

val button = new ObservableButton("clickme")
val bco1 = new ButtonCountObserver
val bco2 = new ButtonCountObserver
button addObserver bco1
button addObserver bco2
(1 to 5) foreach (_ => button.click())
```

## 10. Scala Object System I

### Parameterized Types
- type variance
    + `+T` for `? extends T`, covariance like list[T]
    + `-T` for `? super T` contravariance like Function2[T]

#### contravariant types
FunctionN trait (0<N<=22), or anonymous function, function literal

syntactic sugar for `i => i + 3`
```scala
val f: Int => Int = new Function1[Int,Int] {
    def apply(i: Int): Int = i + 3
}
```

contravariance is used
```scala
trait Function1[-T, +R] extends AnyRef
trait Function2[-T1, -T2, +R] extends AnyRef
```

understand the variance behavior
```scala
class CSuper                {}
class C      extends CSuper {}
class CSub   extends C      {}

var f: C=>C = (c:C)      => new C
    f:      = (c:CSuper) => new CSub    // contravariant & covariant
    f:      = (c:CSuper) => new C       // contra, can use CSuper for C argument
    f:      = (c:C)      => new CSub    // co, co use CSub for C return
    f:      = (c:CSub)   => new CSuper  // both invalid
```

- C=>C type contract
    + any valid C value can be passed to f
        * if argument can accept CSuper, also accept any C
    + f will never return anything other than C value
        * if return type is CSub, is also C

#### mutable variance
- for mutable types, only invariant is allowed
    + `class Container[+A or -A](var value:A)` will throw error
- scala interprets fields in classes as getter setter like 
```scala
class Container[+A](var a:A) {
    private var _value: A = a
    def value_=(newA: A): Unit = _value = newA
    def value: A = _value
}
```

- if we support covariance, we must also support contravariance for substution
- mutable field with getter & setter
    + appears in covariant position when read
    + apprears in contravariant position when written
- no type parameter support both
- use back to invariant please

### Nothing and Null

- Nothing: subtype of all types
    + Predef `def ??? : Nothing = throw new NotImplementedError`
    + list map (i => ???)
    + def mean_stdDev(data: Seq[Double]): (Double, Double) = ???
- Null: subtype of all reference types
    + `abstract final class Null extends AnyRef`
    + why abstract & final? runtime environment provide one instance
- Nil: special case of empty list
    + `object Nil extends List[Nothing] with Product with Serializable`


### Product, tuple

- Tuple
    + like Function, upper limit for tuple is also 22
- case class with Product 
    + upper limit is 22 in scala 2.10, then removed

```scala
val p: Product = Person("Dean", 29)
p.productArity // Int = 2
p.productElement(0) // Any = Dean
p.productElement(1) // Any = 29
p.prodcutIterator foreach println

val t2 = ("Dean", 29)
t2._1 // String = Dean
t2._2 // Int = 29
```

## 11. Scala Object System II

Linearization of Object Hierarchy


Sequence of function
```scala
class C1            { def m = print("C1 ") }
trait T1 extends C1 { override def m = {print("T1 "); super.m }}
trait T2 extends C1 { override def m = {print("T2 "); super.m }}
trait T3 extends C1 { override def m = {print("T3 "); super.m }}
trait C2 extends T1 with T2 with T3 { override def m = {print("C2 "); super.m }}
val c2 = new C2
c2.m
// C2 T3 T2 T1 C1
```

Sequence of Constructors
```scala
class C1            { println("C1 ") }
trait T1 extends C1 { println("T1 ") }
trait T2 extends C1 { println("T2 ") }
trait T3 extends C1 { println("T3 ") }
trait C2 extends T1 with T2 with T3 { println("C2 ") }
val c2 = new C2
// C1 T1 T2 T3 C2
```

Complex Linearization
```scala
class C1            { def m = print("C1 ") }
trait T1 extends C1 { override def m = {print("T1 "); super.m }}
trait T2 extends C1 { override def m = {print("T2 "); super.m }}
trait T3 extends C1 { override def m = {print("T3 "); super.m }}
class C2A extends T2{ override def m = {print("C2A "); super.m}}
trait C2 extends C2A with T1 with T2 with T3 { override def m = {print("C2 "); super.m }}

def calcLinearization(obj: C1, name:String) = {
    println(s"$name: )"
    obj.m
    print("AnyRef ")
    println("Any")
}
```

C2: C2 T3 T1 C2A T2 C1 AnyRef Any
T3: T3 C1 AnyRef Any
C2A: C2A T2 C1 AnyRef ANy

- linearization order
    + add type of instance: C2
    + add linearization from far right
        * T3: C2 T3 C1
        * T2: C2 T3 C1 T2 C1
        * T1: C2 T3 C1 T2 C1 T1 C1
        * C2A: C2 T3 C1 T2 C1 T1 C1 C2A T2 C1
    + remove duplicates, keep last
        * C2 T3 T1 C2A T2 C1

## 12. Scala Collection Library

### Design Idioms in the Collections Library

collection.mutable.Builder
```scala
trait Builder[-Elem, +To] {
    def +=(elem: Elem): Builder.this.type
    // singleton type, ensure += can only return Builder instance called on
    def clear()
    def result(): To
}

class ListBuilder[T] extends Builder[T, List[T]] {
    private var storage = Vector.empty[T]

    def +=(elem:T) = {
        storage = storage :+ elem
        this
    }
    def clear(): Unit = {storage = Vector.empty[T]}
    def result(): List[T] = storage.toList
}

val lb = new ListBuilder[Int]
(1 to 3) foreach (i=> lb += i)
lb.result
```

CanBuildFrom
```scala
trait TraversableLike[+A, +Repr](f: A=>B)(
    implicit bf: CanBuildFrom[Repr, B, That]): That = {...}
```

- TraversableLike: A trait for builder factories
    + Using implicits
        * knows nothing of subtypes like List, but can construct a new List to return
    + Drawback
        * extra complexity in actual method signature
    + Advantage
        * enable object-oriented reuse of monadic operations
        * modularize and generalize construction
            - instantiate builders for multiple concrete collection with one CanBuildFrom
                + put large Map in Hashtable, O(1) with large constant factor overhead
                + put small Map in array, O(n)
            - convertion when input collection cannot use for output
                + Example: map a Bitset to other type
        * carry context info not known to original collection
            - with distributed computing API, special CanBuildFrom instance
            - optimized collection instance for serialization
            
```scala
val set = collection.Bitset(1,2,3,4,5)
set map (_.toString)
// the implicit CanBuildFrom match different collection
> scala.collection.SortedSet[String] = TreeSet(1,2,3,4,5)

"xyz" map (_.toInt)
> scala.collection.immutable.IndexedSeq[Int] = Vector(120,121,122)
```

- Like Traits
    + added to specify type parameter and promote implementation reuse
- `collection.SeqLike`
    + Parameterized with both A and Seq[A]
        * Seq[A]: constrain the allowed CanBuildFrom instance to add method for for comprehension

```scala
trait Seq[+A] extends Iterable[A] with collection.Seq[A]
    with GenericTraversableTemplate[A, Seq] with SeqLike[A, Seq[A]]
    with Parallelizable[A, ParSeq[A]]
```

### Specialization/Miniboxing for Value Types

- Type versus
    + Scala has uniform treatment of value/reference types, allow List[Int]
        * primitive values contiguous in memory can improve cache hit ratios
    + Java require boxed types, like List<Integer>
        * require extra memory per object & extra time for memory management
- Why does not solve the problem
    + JVM type erasure, doesn't retain info about type of container elements
    + List[Double] elements assumed to be Object
    + so a List[Double] will still use boxed `Double`
- Specialization
    + add @specialized notation to use primitive value
    + use specialized type for all/selected primitives

```scala
class SpecialVector[@specialized T] {...}
class SpecialVector[@specialized(Int, Double, Boolean) T] {...}
```

- Problem with specialization
    + result in a lot of generated code, making library excessively large
    + several design flaws
        * if a field declared of generic type in original container, not converted in specialization
            - rather a duplicate field of primitive is created, leading to bug
        * specialized containers implemented as subclass of original generic container
            - breaks when container is also specialized
- Scala Lib has limited use of this, most in FunctionN, TupleN, ProductN
- alternate: *Miniboxing*
    + convert generic container into trait with two subclasses
        * primitive + reference
        * primitive version
            - as 8-byte value can hold any primitive types
            - a tag added to indicate how to interpret
            - bahaves as tagged union, no need separate instantiation
    + inheritance problem is solved with splitting into two class
    
```scala
trait C[T]
class C_primitive[T] extends C[T]
class C_anyref[T] extends C[T]

trait D[T] extends C[T]
class D_primitive[T] extends C_primitive[T] with D[T]
class D_anyref[T] extends C_anyref[T] with D[T]
```


## 14. Scala's Type System I

- Scala
    + statically typed
    + with arguably most sophisticated type system
        * combine comprehensive ideas from FP & OOP
    + type system tries to enforce constraints at compile time 
        * to eliminate runtime as much as possible

### Parameterized Types

- Variance Annotations
    + List[+A]
- Type Constructors
    + all class are type constructors
    + List construct List[String] and List[Int], which are different types
- Type Parameter Names
    + use single-letter or double-letter `A,B,T1,T2` for every generic container types
    + use descriptive names for types associated with container
        * That & List.+: Signature
         
### Type Bonds

+ Difference with variance
    * type bond: constrains on allowed types for a parameter
    * variance: when instance of subtype or subtype can be subsituted
+ Upper Type Bounds `<:`
    * Predef implicit that convert Java `Array` to `collection.mutable.ArrayOps`
+ Lower Type Bounds `>:`
    * Option implementation that require a supertype

```scala
implicit def refArrayOps[T <: AnyRef](xs: Array[T]): ArrayOps[T] =
    new ArrayOps.ofRef[T](xs)

implicit def longArrayOps(xs: Array[Long]): ArrayOps[Long] = 
    new ArrayOps.ofLong(xs)
```


```scala
sealed abstract class Option[+A] extends Product with Serializable {
    @inline final def getOrElse[B >: A](default:=>B): B = { ... }
}
```

Why necessary?
- Because covariant allows `val op:Option[Parent] = Option[Child](null)`
- combinator return Parent though actually Child inside
- then if you call `val p: Parent = op.getOrElse(new Parent())`
    + without lowe bounds, it will call it without type check
    + op stored Child null, so it expect a Child passed (instead a Parent)

Covariant type A occurs in contravariant position
- attempt to define a parameterized type convariant in parameter
- but also define methods that accept that, instead of new super parameter

### Context Bonds

Using implicitly
```scala
import math.Ordering

case class MyList[A](list: List[A]) {
    // show explicitly
    def sortBy1[B](f: A=>B)(implicit ord: Ordering[B]): List[A] = 
        list.sortBy(f)(ord)
    // hidden as parameterized type
    def sortBy2[B: Ordering](f: A=>B): List[A] =
        list.sortBy(f)(implicitly[Ordering[B]])
}
```


### View Bonds
- a special case of context bonds
```scala
class C[A] {
    def m1[B](...)(implicit view: A=>B): ReturnType = {...}
    def m2[A <% B](...): ReturnType = {...}
}
```

- context bound
    + implicit value for A;B have to be type B[A]
        * need implicit function that converts  `Int => Ordering[Int]`
- view bound
    + need implicit function convert A => B
        * we say B is a view onto A
        * not strict requirement like upper bound `<:`, just convertible `<%`

example Hadoop Java API

```scala
import scala.language.implicitConversions

object Serialization {
    case class Writable(value:Any) {
        def serialized: String = s"-- $value --"
    }

    implicit def fromInt(i: Int) = Writable(i)
    implicit def fromFloat(f: Float) = Writable(f)
    implicit def fromString(s: String) = Writable(s)
}

import Serialization._
object RemoteConnection {
    def write[T <% Writable](t:T) =
        println(t.serialized) // use stdout as remote connections
}
RemoteConnection.write("hello!")
```

### Abstract type

an alternative of parameterized types
```
trait exampleTrait {
    type t1
    type t2 >: t3 <: t1
    type t3 <: Seq[t1]

    val v1: t1
    val v2: t2
    val v3: t3
}
```

- Parameterized types for containers and collections, when type given to constructor
- abstract type parameters
    + useful for type families, where they are linked

```scala
abstract class SubjectObserver {  // Encapsulate s-o relationship in a single type
    type S <: Subject
    type O <: Observer

    trait Subject { self: S =>    // subject trait
        private var observers = List[O]()
        def addObserver(observer: O) = observers ::= observer
        def notifyObservers() = observers.foreach(_.receiveUpdate(self))
    }
    trait Observer{              // observer trait
        def receiveUpdate(subject: S)
    }
}
```


### Self-Type Annotation
- two objectives
    + let you specify additional type expectations for this
    + they can be used to create aliases for this

look at above code use `self: S =>` and `self`
use the observer in a three-tier application, with Persistent, Midtier and UI
```scala
trait Persistence {def startPersistence():Unit}
trait Midtier {def startMidtier(): Unit}
trait UI {def startUI():Unit}

trait Database extends Persistence {
    def startPersistence(): Unit = println("starting Database")
}

trait BizLogic extends Persistence {
    def startMidtier(): Unit = println("starting BizLogic")
}

trait WebUi extends Persistence {
    def startUI(): Unit = println("starting WebUi")
}

trait App { self: Persistence with Midtier with UI =>
    def run() = {
        startPersistence()
        startBizLogic()
        startUI()
    }
}

object MyApp extends App With Database With BizLogic with WebUI
MyApp.run
```

when type annotation added, it specifies the trait or abstract class must be mixed to define a concrete instance. This assumption let trait access members of other trait, even though they are not yet part of the type.

- shadowing this
    + self also works when you have nested classes to get outer scope self

### Structural Types

- a type-safe approach to duck typing, which resolution works in dynamically typed language
- Scala don't support runtime method resolution, except Dynamic Invocation
    + instead, provide a structure typing
- Observer example
    + above Observer implementation require all watcher implement Observer trait
    + true minimum requirement is that they implement the `receiveUpdate` method
    + can define any type with a receiveUpdate method as a new type

```scala
trait Subject {
    import scala.language.reflectiveCalls
    type State
    type Observer = {def receiveUpdate(state:Any):Unit} // any type with a receiveUpdate method
    private var observers: List[Observer] = Nil
    def addObserver(observer:Observer):Unit = 
        observers ::= observer
    def notifyObservers(state:State):Unit = 
        observers foreach (_.receiveUpdate(state))
}
```

### Existential Types

```scala
object Doubler {
    def double(seq: Seq[String]): Seq[Int] = double(seq map (_.toInt))
    def double(seq: Seq[Int]): Seq[Int] =seq map (_*2)
}
```

- you get a compilation error that two methods have the same type after erasure
- use existential type `Seq[_]` instead

```scala
object Doubler {
    def double(seq: Seq[_]): Seq[Int] = seq match {
        case Nil => Nil
        case head +: tail => (toInt(head)*2) +: double(tail)
    }
    private def toInt(x: Any): Int = x match {
        case i:Int => i
        case s:String => s.toInt
        case x => throw new RuntimeException(s"Unexpected $x")
    }
}
```


## 15. Scala's Type System II

### Dependent method types

- path-dependent typing for magnet pattern
    + single processing method takes an magnet object
    + ensures a compatible return type


```scala
case class LocalResponse(statusCode: Int)
case class RemoteResponse(message: String)
sealed trait Computation {
    type Response
    val work: Future[Response]
}
case class LocalComputation(
    work: Future[LocalResponse]) extends Computation {
        type Response = LocalResponse
    }

case class RemoteComputation(
    work: Future[RemoteResponse]) extends Computation {
        type Response = RemoteResponse
    }

object Service {
    def handle(computation: Computation): computation.Response = {
        val duration = Duration(2, SECONDS)
        Await.result(computation.work, duration)
    }
}
Service.handle(LocalComputation(Future(LocalResponse(0))))
```


### Type for Values
```scala
// Tuple
val t1: Tuple3[String, Int, Double]
val t2: (String, Int, Double)

// Function
val f1: Function2[Int, Double, String]
val f2: (Int, Double) => String

// Infix
val x1: Int Either Double
val x2: Either[Int, Double]
```

### Type Lambdas

where we need to use a parameterized type that has two many type parameters for the context

```scala
trait Functor[A, +M[_]] {
    def map2[B](f: A=>B): M[B]
}
object Functor {
    implicit class SeqFunctor[A](seq: Seq[A]) extends Functor[A,Seq] {
        def map2[B](f:A=>B): Seq[B] = seq map f
    }

    implicit class OptionFunctor[A](opt: Option[A]) extends Functor[A,Option] {
        def map2[B](f:A=>B): Option[B] = opt map f
    }

    implicit class MapFunctor[K,V1](mapKV1: Map[K,V1]) 
        extends Functor[V1, ({type λ[α] = Map[K,α]})#λ] {
            def map2[V2](f: V1 => V2): Map[K, V2] = mapKV1 map {
                case (k,v) => (k, f(v))
            }
        }
}
```

Above is actually mapValues.
```scala
Functor[V1,   // functor expect second type param to be a container[T]
    (
        {     // start defining a structure type
            type λ[α] = Map[K,α]  // define map in structure
        }
    )#λ       // type projection out of structure type
]
```

type projection can also be used with concrete classes

### Self-Recursive Types: F-Bounded Polymorphism

classic example is Java's Enum abstract class
```java
public abstract class Enum<E extends Enum<E>> extends Object
implements Comparable<E>, Serializable {}
```

- the benefits of this signature
- compileError to pass object to compareTo
    + isn't one of enumeration values defined for the same type

```java
int compareTo(E obj)
TimeUnit.MILLISECONDS compareTo TimeUnit.SECONDS // Int = -1
Type.HTTP compareTo Type.SOCKS // Int = -1
TimeUnit.SECONDS comparTo Type.HTTP // error: type mismatch
```

In Scala, recursive types also handy for defining methods with same return type as caller
```scala
trait Parent[T <: Parent[T]]{
    def make:T
}

case class Child1(s: String) extends Parent[Child1] {
    def make: Child1 = Child1(s"Child1: make: $s")
}
case class Child2(s: String) extends Parent[Child2] {
    def make: Child2 = Child2(s"Child2: make: $s")
}

val c1 = Child1("c1") // c1:Child1 = Child1(c1)
val c2 = Child2("c2") // c2:Child2 = Child2(c2)
val c11 = c1.make // c1: Child1 = Child1(child1: make: c1)
val c22 = c2.make // c2: Child2 = Child2(child2: make: c2)
val p1: Parent[Child1] = c1  // p1: Parent[Child1] = Child1(c1)
val p2: Parent[Child2] = c2  // p2: Parent[Child2] = Child2(c2)
val p11 = p1.make // p1: Child1 = Child1(child1: make: c1)
val p22 = p2.make // p2: Child2 = Child2(child2: make: c2)
```

read more: Shapeless & Scalaz

## 17. Tools for concurrency aka Akka
Messages
```scala
object Messages {
    sealed trait Request { val key: Long }                      // CRUD trait
    case class Create(key:Long, value: String) extends Request
    case class Read(key:Long) extends Request
    case class Update(key: Long, value: String) extends Requet
    case class Delete(key: Long) extends Request

    case class Response(result: Try[String])    // wrap response in Try

    case class Start(numberOfWorkers: Int = 1)
    case Crash(whichOne: Int)
    case Dump(whichOne: Int)
    case object DumpAll
}
```


config
```scala
// src/main/resources/application.conf
akka {
    loggers = [akka.event.slf4j.Slf4jLogger]
    loglevel = debug

    actor {
        debug {
            unhandled = on
            lifecycle = on
        }
    }

    server {
        number-workers = 5
    }
}
```

AkkaClient
```scala
object AkkaClient {
    import Messages._
    private var system: Option[ActorSystem] = None

    def main(args:Array[String]) = {
        processArgs(args)
        val sys = ActorSystem("AkkaClient")
        system = Some(sys)
        val server = ServerActor.make(sys)
        val numberOfWorkers = sys.settings.config.getInt("server.number-workers")
        server ! Start(numberOfWorkers)
        processInput(server)
    }

    private def processArgs(args: Seq[String]): Unit = args match {
        case Nil =>
        case ("-h"|"--help") +: tail => exit(help,0)
        case head +: tail => exit(s"Unknown input $head!\n" + help, 1)
    }
    
    private def processInput(server: ActorRef): Unit = {
        val blankRE =    """^\s*#?\s*$""".r
        val badCrashRE = """^\s*[Cc][Rr][Aa][Ss][Hh]\s*$""".r
        val crashRE =    """^\s*[Cc][Rr][Aa][Ss][Hh]\s+(\d+)\*$""".r
        val dumpRE =     """^\s*[Dd][Uu][Mm][Pp](\s+\d+)?\s*$""".r
        val charNumberRE="""^\s*(\w)\s+(\d+)\s*$""".r
        val charStringRE="""^\s*(\w)\s+(\d+)\s+(.*)$""".r
    
        def prompt() = print(">> ")
        def missingActorNumber() = 
            println("Crash command requires an actor number.")
        def invalidInput(s:String) = 
            println(s"Unrecognized command: $s")
        def invalidCommand(c:String) = 
            println(s"Expected 'c','r','u' or 'd'. Got $c")
        def invalidNumber(s:String) =
            println(s"Expected a number, got $s")
        def expectedString(): Unit = 
            println(s"Expected astring after command and number")
        def unexpectedString(c:String, n:Int)
            println(s"Extra arguments after command and number '$c $n'")
        def finished(): Nothing = exit("Goodbye!", 0)
    
        val handleLine: PartialFunction[String, Unit] ={
            case blankRE() => 
            case "h"|"help" => println(help)
            case dumpRE(n) =>
                server ! (if (n==null) DumpAll else Dump(n.trim.toInt))
            case badCrashRE() => missingActorNumber()
            case crashRE(n) => server ! Crash(n.toInt)
            case charStringRE(c,n,s) => c match {
                case "c"|"C" => server ! Create(n.toInt, s)
                case "u"|"U" => server ! Update(n.toInt, s)
                case "r"|"R" => unexpectedString(c, n.toInt)
                case "d"|"D" => unexpectedString(c, n.toInt)
                case _ => InvalidCommand(c)
            }
            case charNumberRE(c,n) => c match {
                case "c"|"C" => expectedString
                case "u"|"U" => expectedString
                case "r"|"R" => server ! Read(n.toInt)
                case "d"|"D" => server ! Delete(n.toInt)
                case _ => InvalidCommand(c)           
            }
            case "q"|"quit"|"exit" => finished()
            case string => invalidInput(string)
        }

        while(true){
            prompt()
            Console.in.readLine() match{
                case null => finished()
                case line => handleLine(line)
            }
        }

    }

    private val help = 
    """Usage: AkkaClient [-h | --help]
      |Then, enter one of the following commands, one per line:
      |  h | helps      Print this help message.
      |  c n string     Create record for key n for value string
      |  r n            Read record for key n, error if not found
      |  u n string     Update/create record for key n
      |  d n            Delete record for key n, error if not found
      |  crash n        Crash worker n
      |  dump [n]       Dump state of all workers
      |  ^d | quit      Quit.
      |""".stripMargin
    
    private def exit(message:String, status:Int): Nothing = {
        for (sys<-system) sys.shutdown()
        println(message)
        syst.exit(status)
    }
}
```


ServerActor
```scala
class ServerActor extends Actor with ActorLogging {
    import Messages._
    implicit val timeout = TImeout(1.seconds)

    override val supervisorStrategy: SupervisorStrategy = {
        // override the default supervisor strategy
        val decider: SupervisorStrategy.Decider = {
            case WorkerActor.CrashException => SupervisorStrategy.Restart
            case NonFatal(ex) => SupervisorStrategy.Resume
        }
        // workers independent, use one-for-one
        OneForOneStrategy()(decider orElse super.supervisorStrategy.decider)
    }

    var workers = Vector.empty[ActorRef]  // kkep Actor Reference
    def receive = initial

    // Receive aliases PartialFunction[Any, Unit] 
    // used to start the first state of a state machine
    val initail: Receive = {
        case Start(numberOfWorkers) =>
            workers = ((1 to numberOfWorkers) map makerWorker).toVector
            context become processRequests
            // transition to second state of the machine
    }



    import akka.pattern.ask
    val processRequests: Receive = {
        case c@Crash(n) => workers(n % worker.size) ! c
        // take together all dump response and format at a result message
        // with akka.pattern.ask, we can use ? to send a message like above
        // this returns a Future instead of Unit
        case DumpAll =>
            Future.fold(workers map (_ ? DumpAll))(Vector.empty[Any])(_ :+ _)
                .onComplete(askHandler("State of the workers"))
        case Dump(n) =>
            (workers(n % workers.size) ? DumpAll).map(Vector(_))
                .onComplete(askHandler(s"State of workers $n"))
        case request: Request =>
            val key = request.key.toInt
            val index = key % workers.size
            workers(index) ! request
        case Response(Success(message)) => printResult(message)
        case Response(Failure(ex)) => printResult(s"Error! $ex")
    }

    def askHandler(prefix: String): PartialFunction[Try[Any], Unit] = {
        case Success(suc) => suc match {
            case vect: Vector[_] =>
                printResult(s"$prefix:\m")
                vect foreach {
                    case Response(Success(message)) =>
                        printResult(s"$message")
                    case Response(Failure(ex)) =>
                        printResult(s"ERROR! Success received wrapping $ex")
                }
            case _ => printResult(s"BUG! Expected a vector, got $suc")
        }
        case Failure(ex) => printResult(s"ERROR! $ex")
    }

    protected def printResult(message: String) ={
        println(s"<< $message")
    }
    protected def makeWorker(i: Int) =
        context.actorOf(Props[WorkerActor], s"worker-$i")
}

object ServerActor {
    def make(system:ActorSystem): ACtorRef = 
        system.actorOf(Props[ServerActor], "server")
}
```

WorkerActor
```scala
class WorkerActor extends Actor with ActorLogging {
    import Messages._

    private val datastore = collection.mutable.Map.empty[Long, String]

    def receive = {
        case Create(key,value) =?
            datastore += key->value
            sender ! Response(Success(s"$key -> $value added"))
        case Read(key) =>
            sender ! Response(Try(s"${datastore(key)} found for key = $key"))
        case Update(key, value) =>
            datastore += key -> value
            sender ! Response(Success(s"$key -> $value updated"))
        case Delete(key) =>
            datastore -= key
            sender ! Response(Success(s"$key deleted"))
        case Crash(_) => throw WorkerActor.CrashException
        case DumpAll =>
            sender ! Response(Success(s"${self.path}: datastore = $datastore"))
    }
}

object WorkerActor {
    case object CrashException extends RuntimeException("Crash!")
}
```

## 19. Dynamic Invocation

### A Motivating Exmaple: ActiveRecord in Ruby on Rails

- ActiveRecord
    + original ORM library integrated with Rails
    + offers a DSL for composing queries
        *  chaining method calls on a domain object
        *  invocations are routed to Ruby's catch-all for undefined method `method_missing`
            - normally throws an exception
            - can be overriden to do something else 

```sql
CREATE TABLE states (
    name        TEXT,       -- Name of the State
    capital     TEXT,       -- Name of the capital city
    statehood   INTEGER     -- Year the state was admitted to union
)
```


Ruby domain object `State` is the analo of table states
- It is impossible to define all permutations of `find_by_*` when table is large.
- But the protocol defined pattern is easy to automate
- ActiveRecord defines such ORM
    + implements as an embedded or internal DSL
        * the language is an idiomatic dialect of host language Ruby

```ruby
State.find_by_name("Alaska")
State.find_by_name_and_statehood("Alaska", 1959)
```

### Dynamic Trait in Scala

```scala
class Foo extends Dynamic {}
foo = new Foo()

foo.method("blah")          foo.applyDynamic("method")("blah")
foo.method(x = "blah")      foo.applyDynamic("method")(("x", "blah"))
foo.method(x = 1, 2)        foo.applyDynamic("method")(("x",1), ("", 2))
foo.field                   foo.selectDynamic("field")
foo.field = 10              foo.updateDynamic("varia")
foo.arr(10) = 13            foo.selectDynamic("arr").update(10,13)
foo.arr(10)                 foo.applyDynamic("arr")(10)
```


### CLINQ

- LINQ: Language-integrated Query
    + a query DSL in .NET language
    + enables SQL-like queries to be embedded into .NET program
    + one inspiration for Slick, a Scala functional-relation mapping (FRM) library
- CLINQ: we will implement a subset, cheap LINQ 

```scala
def makeMap(name:String, capital:String, statehood:Int) =
    Map("name" -> name, "capital" -> capital, "state"->statehood)

val states = CLINQ(
        List(
            makeMap("Alaska",       "Juneau",       1959),
            makeMap("California",   "Sacramento",   1850),
            makeMap("Illinois",     "Springfield",  1818),
            makeMap("Virginia",     "Richmond",     1788),
            makeMap("Washington",   "Olympia",      1889)))

state.name
state.name_and_capital
state.capital_and_statehood
state.all
state.all.where("name").NE("Alaska")
state.name_and_statehood.where("statehood").EQ(1990)
```


### Implementation
```scala
import scala.language.dynamics

case class CLINQ[T](records: Seq[Map[String, T]]) extends Dynamic {

    // selectDynamic for projections of fields
    def selectDynamic(name: String): CLINQ[T] =
        // return itself if all/nothing
        if(name == "all" || records.length ==0) this
        else {
            // joined selection
            val fields = name.split("_and_")
            val seed = Seq.empty[Map[String,T]]
            val newRecords = (records foldLeft seed){
                (results, record) =>
                    val projection = record filter {
                        case (key, value) => fields contains key
                    }
                    // Drop records with no projection.
                    if (projection.size>0) results :+ projection
                    else results
            }
            CLINQ(newRecords)
        } 

    def applyDynamic(name:String)(field:String): Where = name match {
        case "where" => new Where(field)
        case _ => throw CLINQ.BadOperation(field, """Expected "Where".""")
    }

    // inner class to do apply
    protected class Where(field: String) extends Dynamic {
        def filter(value:T)(op: (T,T)=>Boolean): CLINQ[T] = {
            val newRecords = records filter {
                _ exists {
                    case (k,v) => field == k && op(value, v)
                }
            }
            CLINQ(newRecords)
        }

        def applyDynamic(op: String)(value: T): CLINQ[T] = op match {
            case "EQ" => filter(value)(_ == _)
            case "NE" => filter(vlaue)(_ != _)
            case _ => throw CLINQ.BadOperation(field, """Expected "EQ" or "NE".""")
        }
    }

    override def toString: String = records mkString "\n"
}


object CLINQ {
    case class BadOperation(name:String, msg:String) extends RuntimeException(
            s"Unrecognized oepration $name. $msg")
}



```

## 20. DSL

- DSL: domain-specific language
    + benefits
        * Encapsulation: hide implementation details and exposes abstraction relevant
        * Productivity: optimize effort required to write/modify code
        * Communication: help dev understand domain, help domain expert verify implementation
    + drawbacks
        * Difficult to create
            - non trivial implementation technique
            - harder to design than traditional API
        * Hard to maintain
            - implementation simplicity is often sacrificed for better user experience

### XML and JSON DSL for Scala

- scala.xml.{Elem, Node}

```scala
import scala.xml._
val xmlAsString = "<sammich>...</sammich>"
val xml1 = XML.loadString(xmlAsString)

val xml2 =
<sammich>
    <bread>wheat</bread>
    <meat>salami</meat>
    <condiments>
        <condiment expired="true">mayo</condiment>
        <condiment expired="false">mustard</condiment>
    </condiments>
</sammich>

for {
    condiment <- (xml2 \\ "condiment")
    if (condiment \ "@expired").text == "true"
} println(s"the ${condiment.text} has expired!")

def isExpired(condiment: Node): String = 
    condiment.attribute("expired") match {
        case Some(Nil)|None => "unknown"
        case Some(nodes) => nodes.head.text
    }

xml2 match {
    case <sammich>{ingredients @ _*}</sammich> => {
        for {
            condiments @ <condiments>{_*}</condiments> <- ingredients
            cond <= condiments \ "condiment"
        } println(s"  condiment: ${cond.text} is expired? ${isExpired(cond)}")
    }
}

```


### Internal DSLs
```scala
import scala.language.postfixOps
// we want to use 20 dollars notion

object Payroll {
    import dsl._

    def main(args: Array[String]) = {
        val biweeklyDeductions = biweekly { deduct =>
            deduct federal_tax          (25.0   percent)
            deduct state_tax            (5.0    percent)
            deduct insurance_premiums   (500.0  dollars)
            deduct retirement_saving    (10.0   percent)
        }  // DSL in action, easy understood by business users

        println(biweeklyDeduction)
        val annualGross = 10000.0
        val gross = biweeklyDeduction.gross(annualGross)
        val net = biweeklyDeduction.net(annualGross)
        print(f"Biweekly pay (annual: $$${annualGross}%.2f): ")
        println(f"Gross: $$${gross}%.2f, Net: $$${net}%.2f")
    }
}
```

how it is implemented
```scala
object dsl {
    def biweekly(f: DeductionsBuilder => Deductions) =
        f(new DeductionsBuilder("Biweekly", 26.0))

    class DeductionsBuilder(
        name:String, 
        divisor:Double=1.0,
        deducts:Vector[Deduction]=Vector.empty) extends Deductions(name,divisor,deducts) {

            def federal_tax(amount: Amount): DeductionsBuilder = {
                deductions = deductions :+ Deduction("federal taxes", amount)
                this
            }
            ...
        }

}
```

- Problem of internal DSL
    + relies heavily on Scala syntax tricks
    + syntax uses arbitrary conventions (curly braces and parentheses)
    + poor error message (scala errors)
    + does not prevent user from doing wrong things

### External DSLs with Parser Combinators

can use together with String interpolator to achieve `sql"SELECT * FORM"` class

```scala
import scala.util.parsing.combinator._

object Payroll {
    import dsl.PayrollParser

    def main(args: Array[String]) = {
        val input = """biweekly {
            federal tax         20.0 percent,
            state tax           3.0 percent,
            insurance premiums  250.0 dollars,
            retirement savings  15.0 percent
        }"""

        val parser = new PayrollParser
        val biweeklyDeductions = parser.parseAll(parser.biweekly, input).get

        println(biweeklyDeductions)
        val annualGross = 10000.0
        val gross = biweeklyDeduction.gross(annualGross)
        val net = biweeklyDeduction.net(annualGross)
        print(f"Biweekly pay (annual: $$${annualGross}%.2f): ")
        println(f"Gross: $$${gross}%.2f, Net: $$${net}%.2f")
    }
}

```


implementation
```scala
object dsl {
    class PayrollParser extends JavaTokenParsers {
        // @return Parser[(Deductions)]
        // top level parser, created by building up small parsers
        def biweekly = "biweekly" -> "{" -> deductions <~ "}" ^^ { ds =>
            Deductions("Biweekly", 26.0, ds)
        }
        // @return Parser[Vector[Deduction]]
        // comma separated list of deductions, repsep parse arbitrary number of expression
        def deductions = repsep(deduction, ",") ^^k {ds => 
            ds.foldLeft(Vector.empty[Deduction])(_:+_)
        }

        // @return Parser[Deduction]
        def deduction = federal_tax | state_tax | insurance | retirement

        // @return Parser[Deduction]
        // construct four deductions
        def federal_tax = parseDeduction("federal", "tax")
        def state_tax = parseDeduction("state", "tax")
        def insurance = parseDeduction("insurance", "premiums")
        def retirement = parseDeduction("retirement", "saving")

        private def parseDeduction(word1: String, word2: String) = 
            word1 ~> word2 ~> ammount ^^ { amount =>
                Deduction(s"${word1} ${word2}", amount)
            }

        // @return Parser[Amount]
        def amount = dollars | percentage
        def dollars = doubleNumber <~ "dollars" ^^ {d => Dollars(d)}
        def percentage = doubleNumber <~ "percent" ^^ {d => Percentage(d)}
        def doubleNumber = floatingPointNumber ^^ (_.toDouble)
    }

}
```

- Tokens
    + arrow-like methods `~>`, `<~` mean dropping token on the side the `~`
- `^^` separate left side (tokens) from right side (grammar rules)
- Grammar Rules
    + take token retained as arguments
        * one: `ds => ...`
        * multiple: `{case t1~t2~t3 => ...}`

## 24. Metaprogramming: Macros and Reflections

### ClassTag

```scala
def useClassTag[T: ClassTag](seq: Seq[T]): String = seq match {
    case Nil => "Nothing"
    case head +: _ => implicitly(seq.head).getClass.toString
    // if nonEmpty, get implicit ClassTag instance of head
    // not work for mixed type sequence (need bound)
}
def check(seq: Seq[_]): String = 
    s"Seq: ${useClassTag(seq)}"

Seq(Seq(5.5,5.6,5.7), Seq("a","b")), Seq(1,"two",3.14), Nil) foreach {
    case seq:Seq[_] => println("%20s: %s".format(seq,check(seq)))
    case x          => println("%20s: $s".format(x,"unknown!"))
}

// List(5.5,5.6,5.7)    Seq: class java.lang.Double
// List(a,b)            Seq: class java.lang.String
// List(1, two,3.14)    Seq: class java.lang.Integer
// List()               Seq: Nothing
```

- Class tag is not accurate for mixed types
- can't resurrect type information from byte code

Another Usage: construct Java Arrays of correct AnyRef subtype

```scala
import scala.reflect.ClassTag
def mkArray[T: ClassTag](elems: T*) = Array[T](elems: _*)
mkArray(1,2,3)
mkArray("one","two","three")
mkArray(1,"two",3.14)
// warning: a type was inferred to be `Any`
```


### Advanced Runtime Reflection API: TypeTag

```scala
import scala.reflect.runtime.universe._
def toType2[T](t:T)(implicit tag: TypeTag[T]): Type = tag.tpe
// use an implicit arg for TypeTag[T], then ask for its type

def toType[T: TypeTag](t: T): Type = typeOf[T]
// type bound shortcut
// typeOf = implicitly[TypeTag[T]].tpe
```

Examples
```scala
toType(1)  
// reflect.runtime.unverse.Type = Int

toType(true)  
// reflect.runtime.unverse.Type = Boolean

toType(Seq(1, true, 3.14))  
// reflect.runtime.unverse.Type = Seq[AnyVal]

toType((i:Int)=>i.toString)
// reflect.runtime.unverse.Type = Int => java.lang.String
```

compare types / variance
```scala
toType(1) =:= toType(1)
toType(1) <:< typeOf[AnyVal]
toType(1) <:< toType(1)
typeOf[Seq[Int]] <:< typeOf[Seq[Any]]

class CSuper                {}
class C      extends CSuper {}
class CSub   extends C      {}

typeOf[C=>C] =:= typeOf[C=>C]
typeOf[C=>C] <:< typeOf[C=>C]
typeOf[CSuper=>CSub] <:< typeOf[C=>C]
```

More information about Type API

```scala
val ts = toType(Seq(1, true, 3.14))  // Seq[AnyVal]

ts.typeSymbol
// reflect.runtime.universe.Symbol = trait Seq
ts.erasure
// reflect.runtime.universe.Type = Seq[Any]
ts.typeArgs
// List[reflect.runtime.universe.Type] = List(AnyVal)
ts.baseClasses
// List[reflect.runtime.universe.Symbol] =
//     List(trait Seq, trait SeqLike, trait GenSeq, trait GenSeqLike, ..)
ts.companion
// reflect.runtime.universe.Type = scala.collection.Seq.type
ts.decls
// reflect.runtime.universe.MemberScope = SynchronizedOps(
//      method $init$, method companion, method seq)
ts.members
// reflect.runtime.universe.MemberScope =Scopes(
//      method seq, method companion, method $init$, method toString ...)
```

### Macros

- used to implement clever solutions to difficult design problems
    + require understanding compiler internals
        * like abstract syntax tree (AST) representation by compiler

- Scala Meta project
    + implementing a new macro system to avoid this coupling
    + not yet available
    + quasiquotes will remain relatively unchanged

```scala
import reflect.runtime.universe._           // universe feature required
import reflect.runtime.currentMirror       
import tools.reflect.ToolBox                // convenient ToolBox
val toolbox = currentMirror.mkToolBox()
```

#### Quasiquotes

Example
```scala
val C = q"case class C(s:String)"
C: reflect.runtime.universe.ClassDef = 
case class C extends scala.Produt with scala.Serializable {
    <caseaccessor><paramaccessor> val s: String = _;
    def <init>(s: String) = {
        super.<init>();
        ()
    }
}

showCode(C)
res0: String = case class C(s: String)

showRaw(C)
res1: String = ClassDef(Modifiers(CASE), TypeName("C"), List(), ...)
```

q is for general quasiquotes
tq is for constructing trees for types
```scala
val q = q"List[String]"
val tq = tq"List[String]"

showRaw(q)
res1: String = TypeApply(Ident(TermName("List")), 
                         List(Ident(TypeName("String"))))

showRaw(tq)
res2: String = AppliedTypeTree(Ident(TypeName("List")),
                         List(Ident(TypeName("String"))))

q equalsStructure tq // Boolean = false
```

- TypeApply
    + type specification appears in term
    + `foo[T]` in `def foo[T](t:T) = ...`  
- AppliedTypeTree
    + type declarations
    + `T` in `val t:T`


#### Unquoting
Expand other q into a q using string interpolation

```scala
Seq(tq"Int", tq"String") map { param =>
    q"case class C(s: $param)"
} foreach { q =>
    println(showCode(q))
}
```

Lifting normal values to quasiquotes
```scala
val list = Seq(1,2,3,4)
val format = "%d, %d, %d, %d"
val printq = q"println($format, ..$list)"
```

`..$list` spread syntax expands the list into comma-separated values
`...$list` for sequence of sequence


Unlifting quasiquotes to normal values with pattern match
```scala
val q"${i: Int} + ${d: Double}" = q"1 + 3.14"
i: Int = 1
d: Double = 3.14
```

#### Example: Enforcing Invariants

Macros are limited form of compiler plug-in, invoked in an intermediate phase of compilation process. 
Macro must be compile separately and ahead of time from code using them.

Macro: invariant
```scala
import reflect.runtime.universe._
import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

// require predicate for invariant to be true
// before each expression is evaluated
object invariant {
    case class InvariantFailure(msg: String) extends RuntimeException(msg)

    def apply[T](predicate: => Boolean)(block: =>T): T = macro impl
    // used to wrap expression to enforce invariant
    // Macro always start with public method invoked in client code

    // pass two atgs, a predicate & block of code to test
    // each is corresponding abstract syntax tree generated from expr
    def impl(c: Context)(predicate: c.Tree)(block: c.Tree) = {
        import c.universe._
        val predStr = showCode(predicate)
        // pattern match block into serials of statements
        val q"...$stmts" = block
        // modify each one to capture return value, check predicate
        val invariantStmts = stmts.flatMap {stmt =>
            val msg = s"FAILURE! $predStr == false, for state: " + showCode(stmt)
            val tif = q"throw new metaprogramming.invariant.InvariantFailure($msg)"
            val predq2 = q"if(false == $predicate) $tif"
            List(q"{val tmp = $stmt; $predq2; tmp};")
        }
        val tif = q"throw new metaprogramming.invariant.InvariantFailure($msg)"
        val predq = q"if(false == $predicate) $tif"
        // rejoin and return the modified AST
        q"$predq; ..$invariantStmts"
    }
}

```


Test with ScalaTest
```scala
import reflect.runtime.universe._
import org.scalatest.FunSpec

class InvariantSpec extends FunSpec {
    case class Variable(var i:Int, var s:String)

    describe ("invariant.apply") {
        def succeed() = {
            val v = Variable(0, "Hello!") 
            val i1 = invariant(v.s == "Hello!"){
                v.i += 1
                v.i += 1
                v.i
            }
            assert(i1 === 2)
        }

        it ("should not fail if the invariant holds") { succeed() }
        it ("should return the value returned by expressions") { succeed() }
        it ("should fail if invariant is broken") {
            intercept[invariant.InvariantFailure] {
                val v = Variable(0, "Hello!")
                invariant(v.s == "Hello!") {
                    v.i += 1
                    v.s = "Goodbye!"
                    v.i += 1
                }
            }
        }
    }
}
```

Output:
```
[info] - should fail if the invariant is broken  *** FAILED ***
[info] metaprogramming.invariant$InvariantFailure:
    FAILURE! v.s.==("Hello!") == false, for statement: v.`s_=`("Goodbye!")
```

In this way, we show a readable message for the predicate failed trigger

