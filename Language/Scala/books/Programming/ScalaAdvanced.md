# Scala Advanced

## 1. Class and Objects

default access modifier: public

method parameters are vals, if modify b, won't compile

```scala
class ChecksumAccumulator {
  private var sum = 0  
  def add(b: Byte): Unit = {
    sum += b
  }
  def checksum(): Int = {
    return ~(sum & 0xFF) + 1
  }
}

val acc = new ChecksumAccumulator
```

### Concise style
Leave off the curly braces if a method computes only a single result expression. If the result expression is short, it can even be placed on the same line as the def itself. 
```scala
  class ChecksumAccumulator {
    private var sum = 0
    def add(b: Byte): Unit = sum += b
    def checksum(): Int = ~(sum & 0xFF) + 1
  }
```

Methods with a result type of Unit, such as ChecksumAccumulator's add method, are executed for their side effects. 

A side effect is generally defined as mutating state somewhere external to the method or performing an I/O action, like sum+=b

Can use procedure-like concise style for Unit:

```scala
class ChecksumAccumulator {
  private var sum = 0
  def add(b: Byte) { sum += b }
  def checksum(): Int = ~(sum & 0xFF) + 1
}
```

Unit lost values

```scala
def g() { "this String gets lost" }
g: ()Unit

def h() = { "this String gets returned!" }
h: ()java.lang.String
```


### Singleton Object

```scala
// In file ChecksumAccumulator.scala
import scala.collection.mutable.Map

object ChecksumAccumulator {

  private val cache = Map[String, Int]()

  def calculate(s: String): Int = 
    if (cache.contains(s))
      cache(s) // return value when in map
    else {
      val acc = new ChecksumAccumulator
      for (c <- s)
        acc.add(c.toByte)
      val cs = acc.checksum()
      cache += (s -> cs)
      cs 
    }
}
```

the companion object of a class

one way to think of singleton objects is as the home for any static methods you might have written in Java. You can invoke methods on singleton objects using a similar syntax: the name of the singleton object, a dot, and the name of the method.

One difference between classes and singleton objects is that singleton objects cannot take parameters, whereas classes can. Because you can't instantiate a singleton object with the new keyword, you have no way to pass parameters to it. Each singleton object is implemented as an instance of a synthetic class referenced from a static variable, so they have the same initialization semantics as Java statics. In particular, a singleton object is initialized the first time some code accesses it.

a singleton object that does not share the same name with a companion class is called a standalone object. You can use standalone objects for many purposes, including collecting related utility methods together, or defining an entry point to a Scala application. This use case is shown in the next section.

### Scala Application

```scala
// In file Summer.scala
import ChecksumAccumulator.calculate

object Summer {
  def main(args: Array[String]) {
    for (arg <- args)
      println(arg +": "+ calculate(arg))
  }
}
```

To run a Scala program, you must supply the name of a standalone singleton object with a main method that takes one parameter, an Array[String], and has a result type of Unit. Any standalone object with a main method of the proper signature can be used as the entry point into an application. 

Neither ChecksumAccumulator.scala nor Summer.scala are scripts, because they end in a definition. A script, by contrast, must end in a result expression. 

Compile
```
 scalac ChecksumAccumulator.scala Summer.scala //slow
 fsc ChecksumAccumulator.scala Summer.scala //fast Scala compiler
 scala Summer <args>
```


Application Trait
```scala
    import ChecksumAccumulator.calculate
  
    object FallWinterSpringSummer extends Application {
  
      for (season <- List("fall", "winter", "spring"))
        println(season +": "+ calculate(season))
    }
```


## 2. Types and Oeprations

All basic types can be written with literals, like java

String & Symbol literal is different

### String literals
```scala
  scala> val hello = "hello"    // hello: java.lang.String = hello

  scala> val escapes = "\\\"\'" //escapes: java.lang.String = \"'

  println("""Welcome to Ultamix 3000.
             Type "HELP" for help.""")

// the leading spaces before the second line are included in the string

  println("""|Welcome to Ultamix 3000.
             |Type "HELP" for help.""".stripMargin)
```

### Symbol literals
A symbol literal is written 'ident, where ident can be any alphanumeric identifier. Such literals are mapped to instances of the predefined class scala. Like an identifier in a dynamically typed language.

```
def updateRecordByName(r: Symbol, value: Any) {
           // code goes here
         }
updateRecordByName('favoriteAlbum, "OK Computer")
```


### Operators are methods
```
val sum = 1 + 2    // Scala invokes (1).+(2)
val sumMore = (1).+(2) 

val s = "Hello, world!" 
scala> s indexOf 'o'     // Scala invokes s.indexOf('o')

-2.0                  // Scala invokes (2.0).unary_-
```

use == to compare both primitive and reference types. 
- primitive types
    + Java's == compares value equality, as in Scala. 
- reference types, 
    + Java's == compares reference equality(point to the same object on the JVM's heap) 
    + Scala provides eq and its opposite, ne, however only apply to objects that directly map to Java objects. 

## 3. Functional Objects

### Class Rational
```
  scala> val oneHalf = new Rational(1, 2)                     
  oneHalf: Rational = 1/2
  
  scala> val twoThirds = new Rational(2, 3)                   
  twoThirds: Rational = 2/3
  
  scala> (oneHalf / 7) + (1 - twoThirds)                       
  res0: Rational = 17/42
```


###Construct  

```scala
class Rational(n: Int, d: Int) {
    require(d != 0)   // precondition, like assert

    private val g = gcd(n.abs, d.abs)
    val numer = n / g
    val denom = d / g

    def this(n: Int) = this(n, 1) // auxiliary constructor

    def + (that: Rational): Rational =
      new Rational(
        numer * that.denom + that.numer * denom,
        denom * that.denom
      )
    def - (that: Rational): Rational =
      new Rational(
        numer * that.denom - that.numer * denom,
        denom * that.denom
      )
    def * (that: Rational): Rational =
      new Rational(numer * that.numer, denom * that.denom)
  
    def / (that: Rational): Rational =
      new Rational(numer * that.denom, denom * that.numer)

    def + (i: Int): Rational =
      new Rational(numer + i * denom, denom)
    def - (i: Int): Rational =
      new Rational(numer - i * denom, denom)
    def * (i: Int): Rational =
      new Rational(numer * i, denom)
    def / (i: Int): Rational =
      new Rational(numer, denom * i)
  
    override def toString = numer +"/"+ denom

    private def gcd(a: Int, b: Int): Int = 
      if (b == 0) a else gcd(b, a % b)
```


in interpret
```
implicit def intToRational(x: Int) = new Rational(x)
```

otherwise 1+x 2*x would not work

## 4. Built-in Control Structure


### if expression
Scala's if is an expression śhat results in a value.
```scala
  var filename = "default.txt"
  if (!args.isEmpty)
    filename = args(0)

  val filename =
    if (!args.isEmpty) args(0)
     else "default.txt"

```


### while loop
```scala
    def gcdLoop(x: Long, y: Long): Long = {
      var a = x; var b = y
      while (a != 0) {
        val temp = a
        a = b % a
        b = temp
      }
      b
    }

scala> def greet() { println("hi") }
greet: ()Unit
  
scala> greet() == ()                                  
hi
res0: Boolean = true

```

The while and do-while constructs are called "loops," not expressions, because they don't result in an interesting value. The type of the result is Unit.

It is called the unit value and is written (). The existence of () is how Scala's Unit differs from Java's void. 

Because the while loop results in no value, it is often left out of pure functional languages. Scala includes the while loop nonetheless, because sometimes an imperative solution can be more readable, especially to programmers with a predominantly imperative background. 

```scala
    def gcd(x: Long, y: Long): Long =
      if (y == 0) x else gcd(y, x % y) 
```

### for expression

```scala
val filesHere = (new java.io.File(".")).listFiles  
for (file <- filesHere)
    println(file)

  
for (file <- filesHere 
  if file.isFile;
  if file.getName.endsWith(".scala")  // filtering
) println(file)

for (i <- 1 to 4)   // 1 until 4
    println("Iteration "+ i)

```

 yield output

```scala
def scalaFiles =
  for {
    file <- filesHere
    if file.getName.endsWith(".scala")
  } yield file
```
convert to array of files


### try expression

throw is an expression that has a result type Nothing.


```scala
import java.io.FileReader
import java.io.FileNotFoundException
import java.io.IOException

try {
  val f = new FileReader("input.txt")
  // Use and close file
} catch {
  case ex: FileNotFoundException => // Handle missing file
  case ex: IOException => // Handle other I/O error
}
```

```scala
import java.net.URL
import java.net.MalformedURLException

def urlFor(path: String) =
  try {
    new URL(path)
  } catch {
    case e: MalformedURLException =>
      new URL("http://www.scala-lang.org")
  }
```


  def f(): Int = try { return 1 } finally { return 2 }  // 2
  def g(): Int = try { 1 } finally { 2 }                // 1


finally clauses is a way to ensure some side effect happens, such as closing an open file. If a finally clause includes an explicit return statement, or throws an exception, that return value or exception will "overrule" any previous one that originated in the try block or one of its catch clauses. 


### match expression
```scala
    val firstArg = if (!args.sEmpty) args(0) else ""
  
    val friend =
      firstArg match {
        case "salt" => "pepper"
        case "chips" => "salsa"
        case "eggs" => "bacon"
        case _ => "huh?"
      }           
```

The most significant difference from Java's switch, however, may be that match expressions result in a value.

### Variable Scope

Variable declarations in Scala programs have a scope that defines where you can use the name. The most common example of scoping is that curly braces generally introduce a new scope, so anything defined inside curly braces leaves scope after the final closing brace

brace, bracket, angle bracket, curly brace

## 5. Functions and CLosures

### 5.1 method

```scala
import scala.io.Source

object LongLines {
  def processFile(filename: String, width: Int) {
    val source = Source.fromFile(filename)
    for (line <- source.getLines) 
      processLine(filename, width, line)
  }

  private def processLine(filename: String, width: Int, line: String) {
    if (line.length > width)
      println(filename +": "+ line.trim)
  }
}
```

```scala
  object FindLongLines {
    def main(args: Array[String]) {
      val width = args(0).toInt
      for (arg <- args.drop(1))
        LongLines.processFile(arg, width)
    } 
  } 
```
find the lines in LongLines.scala that are over 45 characters in length (there's just one):


### 5.2 local function

Java : private functions

Scala: you can define functions inside other functions. Just like local variables, such local functions are visible only in their enclosing block,
we do not need to pass filename/width args in this case.

```scala
def processFile(filename: String, width: Int) {

  def processLine(line: String) {
    if (line.length > width)
      print(filename +": "+ line)
  }    

  val source = Source.fromFile(filename)
  for (line <- source.getLines) 
    processLine(line)
}
```

### 5.3 First-class functions
define functions as unnamed literals (lambda)
function as variable, can be reassigned.

```
scala> var increase = (x: Int) => x + 1
increase: (Int) => Int = <function>

scala> increase(10)
res0: Int = 11
```

Function literals
```scala
val someNumbers = List(-11, -10, -5, 0, 5, 10)
someNumbers.filter((x: Int) => x > 0)
someNumbers.filter(x => x > 0)
someNumbers.foreach((x: Int) => println(x))
```

### 5.4 Placeholder
```scala
someNumbers.filter(_ > 0)

scala> val f = _ + _
<console>:4: error: missing parameter type for expanded 
function ((x$1, x$2) => x$1.$plus(x$2))
       val f = _ + _

scala> val f = (_: Int) + (_: Int)
f: (Int, Int) => Int = <function>

scala> f(5, 10)
res11: Int = 15
```

You can think of the underscore as a "blank" in the expression that needs to be "filled in." This blank will be filled in with an argument to the function each time the function is invoked.

Partially applied functions
```scala
someNumbers.foreach(println _)

val a = sum _
a: (Int, Int, Int) => Int = <function>

someNumbers.foreach(println)  // if required at end
```


### 5.5 Closure
```scala
val someNumbers = List(-11, -10, -5, 0, 5, 10)
var sum = 0
someNumbers.foreach(sum +=  _)

def makeIncreaser(more: Int) = (x: Int) => x + more
scala> val inc1 = makeIncreaser(1)
inc1: (Int) => Int = <function>

scala> val inc9999 = makeIncreaser(9999)
inc9999: (Int) => Int = <function>

scala> inc1(10)
res24: Int = 11

scala> inc9999(10)
res25: Int = 10009
```

### 5.6 Repeated Params

function that called with zero to many params


```scala
  scala> def echo(args: String*) = 
           for (arg <- args) println(arg)
  echo: (String*)Unit

  echo("What's", "up", "doc?")

  val arr = Array("What's", "up", "doc?")
  echo(arr: _*)
```

### 5.7 Tail Recursion

searching problems

```scala
  def approximate(guess: Double): Double = 
    if (isGoodEnough(guess)) guess
    else approximate(improve(guess))

  def approximateLoop(initialGuess: Double): Double = {
    var guess = initialGuess
    while (!isGoodEnough(guess))
      guess = improve(guess)
    guess
  }
```
recursion or imperative?
the execution time are almost the same.



A tail-recursive function will not build a new stack frame for each call; all calls will execute in a single frame. This may surprise a programmer inspecting a stack trace of a program that failed. 

In the case of approximate above, the Scala compiler is able to apply an important optimization. 

```scala
def boom(x: Int): Int = 
  if (x == 0) throw new Exception("boom!")
  else boom(x - 1) + 1

  scala>  boom(3)
  java.lang.Exception: boom!
        at .boom(<console>:5)
        at .boom(<console>:6)
        at .boom(<console>:6)
        at .boom(<console>:6)
        at .<init>(<console>:6)
  ...


def bang(x: Int): Int = 
  if (x == 0) throw new Exception("bang!")
  else bang(x - 1)

scala> bang(5)
java.lang.Exception: bang!
      at .bang(<console>:5)
      at .<init>(<console>:6)
  ...

```

This time, you see only a single stack frame for bang. You might think that bang crashed before it called itself, but this is not the case. 


## 6 Control Abstraction

### Reducing code duplication

One benefit of higher-order functions is they enable you to create control abstractions that allow you to reduce code duplication. F

For example, file browser
- First, you add a facility to search for files whose names end in a particular string. This would enable your users to find,by extension
- Later on, though, you decide to let people search based on any part of the file name.
- Eventually, you give in to the requests of a few power users who want to search based on regular expressions. 

```scala
  object FileMatcher {
    private def filesHere = (new java.io.File(".")).listFiles
  
    def filesEnding(query: String) =
      for (file <- filesHere; if file.getName.endsWith(query))
        yield file

    def filesContaining(query: String) =
      for (file <- filesHere; if file.getName.contains(query))
        yield file

    def filesRegex(query: String) =
      for (file <- filesHere; if file.getName.matches(query))
        yield file
}
```


Function values provide an answer. While you cannot pass around a method name as a value, you can get the same effect by passing around a function value that calls the method for you. In this case, you could add a matcher parameter to the method whose sole purpose is to check a file name against a query:

```scala
object FileMatcher {
    private def filesHere = (new java.io.File(".")).listFiles
  
    private def filesMatching(matcher: String => Boolean) =
      for (file <- filesHere; if matcher(file.getName))
        yield file
  
    def filesEnding(query: String) =
      filesMatching(_.endsWith(query))
  
    def filesContaining(query: String) =
      filesMatching(_.contains(query))
  
    def filesRegex(query: String) =
      filesMatching(_.matches(query))
}

```

Using closures to reduce code duplication, _ to fit the context

```scala
def containsOdd(nums: List[Int]) = nums.exists(_ % 2 == 1)
def containsNeg(nums: List[Int]) = nums.exists(_ < 0)
```


### Currying
A curried function is applied to multiple argument lists, instead of just one.
```scala
    def plainOldSum(x: Int, y: Int) = x + y
    // plainOldSum: (Int,Int)Int

    def curriedSum(x: Int)(y: Int) = x + y

    //You can use the placeholder notation to use curriedSum in a partially applied function expression

    val onePlus = curriedSum(1)_
    onePlus(2) // res:Int=3
```


### New Control Structure

Any time you find a control pattern repeated in multiple parts of your code, you should think about implementing it as a new control structure. 

```scala
  scala> def twice(op: Double => Double, x: Double) = op(op(x))
  twice: ((Double) => Double,Double)Double
  
  scala> twice(_ + 1, 5)
  res9: Double = 7.0
```


PrintWriter method

withPrintWriter, not user code, that assures the file is closed at the end. So it's impossible to forget to close the file. This technique is called the loan pattern, because a control-abstraction function, such as withPrintWriter, opens a resource and "loans" it to a function. 

```scala
def withPrintWriter(file: File, op: PrintWriter => Unit) {
  val writer = new PrintWriter(file)
  try {
    op(writer)
  } finally {
    writer.close()
  }
}

withPrintWriter(
  new File("date.txt"),
  writer => writer.println(new java.util.Date)
)
```

One way in which you can make the client code look a bit more like a built-in control structure is to use curly braces instead of parentheses to surround the argument list. In any method invocation in Scala in which you're passing in exactly one argument, you can opt to use curly braces to surround the argument instead of parentheses.

```
  scala> println("Hello, world!")
  Hello, world!

  scala> println { "Hello, world!" }
  Hello, world!
```


Rewrite to new version
```scala
def withPrintWriter(file: File)(op: PrintWriter => Unit) {
  val writer = new PrintWriter(file)
  try {
    op(writer)
  } finally {
    writer.close()
  }
}

withPrintWriter(new File("date.txt")) {
  writer => writer.println(new java.util.Date)
} 

```

### By-name Params

The withPrintWriter method differs from built-in control structures of the language, in that the code between the curly braces takes an argument. 

myAssert function: take a function value as input and consult a flag to decide what to do.

```scala
  var assertionsEnabled = true
  
  def myAssert(predicate: () => Boolean) =
    if (assertionsEnabled && !predicate())
      throw new AssertionError

  myAssert(() => 5 > 3)
```

leave out empty parameter 

```scala
    def byNameAssert(predicate: => Boolean) =
      if (assertionsEnabled && !predicate)
        throw new AssertionError

   byNameAssert(5 > 3)
```


why not use a single boolean value?

Because the type of boolAssert's parameter is Boolean, the expression inside the parentheses in boolAssert(5 > 3) is evaluated before the call to boolAssert. 

By contrast, because the type of byNameAssert's predicate parameter is => Boolean, the expression inside the parentheses in byNameAssert(5 > 3) is not evaluated before the call to byNameAssert. 


## 7. Composition and Inheritance

### Element
```scala
  abstract class Element {
    def contents: Array[String]
    def height: Int = contents.length
    def width: Int = if (height == 0) 0 else contents(0).length
  }

  abstract class Element {
    def contents: Array[String]
    val height = contents.length
    val width = 
      if (height == 0) 0 else contents(0).length
  }
```
// parameterless method, use whenever there are no parameters and the method accesses mutable state only by reading fields of the containing object 

or use fields:
slightly faster than method invocations, 
require extra memory space in each Element object.

### ArrayElement
```
    class ArrayElement(conts: Array[String]) extends Element {
      def contents: Array[String] = conts
    }

class WontCompile {
    private var f = 0 // Won't compile, because a field 
    def f = 1         // and method have the same name
}

  class LineElement(s: String) extends ArrayElement(Array(s)) {
    override def width = s.length
    override def height = 1
  }
```

- Java's four namespace: fields, methods, types, and packages.
- Scala's two namespaces are:
   + values (fields, methods, packages, and singleton objects)
   + types (class and trait names)
- Why 
   + override a parameterless method with a val, 
   + something you can't do with Java

### Finish

```scala
import Element.elem

abstract class Element {
  def contents:  Array[String]

  def width: Int = contents(0).length
  def height: Int = contents.length

  def above(that: Element): Element = {
    val this1 = this widen that.width
    val that1 = that widen this.width
    elem(this1.contents ++ that1.contents)
  }

  def beside(that: Element): Element = {
    val this1 = this heighten that.height
    val that1 = that heighten this.height
    elem(
      for ((line1, line2) <- this1.contents zip that1.contents) 
      yield line1 + line2)
  }

  def widen(w: Int): Element = 
    if (w <= width) this
    else {
      val left = elem(' ', (w - width) / 2, height) 
      var right = elem(' ', w - width - left.width, height)
      left beside this beside right
    }

  def heighten(h: Int): Element = 
    if (h <= height) this
    else {
      val top = elem(' ', width, (h - height) / 2)
      var bot = elem(' ', width, h - height - top.height)
      top above this above bot
    }

  override def toString = contents mkString "\n"
}

```

## 8. Scala Hierarchy

### root class Any
```
class Any(){
  final def ==(that: Any): Boolean
  final def !=(that: Any): Boolean
  def equals(that: Any): Boolean
  def hashCode: Int
  def toString: String
}
```

Because every class inherits from Any, every object in a Scala program can be compared using ==, !=, or equals; hashed using hashCode; and formatted using toString. 

== is always the same as equals and != is always the negation of equals.

### primitives

Scala stores integers in the same way as Java: as 32-bit words. This is important for efficiency on the JVM and also for interoperability with Java libraries. Standard operations like addition or multiplication are implemented as primitive operations. 

However, Scala uses the "backup" class java.lang.Integer whenever an integer needs to be seen as a (Java) object. This happens for instance when invoking the toString method on an integer number or when assigning an integer to a variable of type Any. Integers of type Int are converted transparently to "boxed integers" of type java.lang.Integer whenever necessary.

All this sounds a lot like auto-boxing in Java 5 and it is indeed quite similar. There's one crucial difference, though, in that boxing in Scala is much less visible than boxing in Java

```java
  // This is Java
  boolean isEqual(int x, int y) {
    return x == y;
  }
  System.out.println(isEqual(421, 421)); //ture

//change the argument types of isEqual to java.lang.Integer/Object:
  boolean isEqual(Integer x, Integer y) {
    return x == y;
  }
  System.out.println(isEqual(421, 421)); // false
// The programmer should have used equals in this case, but easy to forget.
```

Scala do not have such problem, but there are situations where you need reference equality instead of user-defined equality. For example, in some situations where efficiency is paramount, you would like to hash cons with some classes and compare their instances with reference equality(ne, eq)

### bottom types

Any->AnyValue->(Unit,Int, Boolean, Char, Float, Double)->Nothing
Any->AnyRef-> String -> Null -> Nothing
Any->AnyRef-> ScalaObject -> Iterable -> Seq -> List -> Null -> Nothing

scala.Null: Class Null is the type of the null reference; it is a 
- subclass of every reference class
- not compatible with value types.
- cannot assign a null value to an integer variable:

scala.Nothing
-  subtype of every other type
-  no values of this type 
-  signals abnormal termination, use with exception

# 9. Traits
trait: a fundamental unit of code reuse

A trait definition looks just like a class definition except that it uses the keyword trait.

If does not declare a superclass, use the default superclass of AnyRef

Trait scan be mixed in to a class using either the extends or with keyword

```scala
    trait Philosophical {
      def philosophize() {
        println("I consume memory, therefore I am!")
      }
    }

    class Frog extends Philosophical {
      override def toString = "green"
    }


    class Animal
    trait HasLegs
  
    class Frog extends Animal with Philosophical with HasLegs {
      override def toString = "green"
    }

```


### Thin versus rich interfaces

 commonly faced trade-off in object-oriented design. 

- rich interface has many methods, which make it convenient for the caller
- thin interface has fewer methods, which is easier on the implementers. 

Java's interfaces are more often thin than rich

Scala traits can contain concrete methods, they make rich interfaces far more convenient.You only need to implement the method once, in the trait itself, instead of needing to reimplement it for every class that mixes in the trait. 

### Example
```scala
trait Rectangular {
  def topLeft: Point
  def bottomRight: Point

  def left = topLeft.x
  def right = bottomRight.x
  def width = right - left
  // and many more geometric methods...
}

class Rectangle(val topLeft: Point, val bottomRight: Point)
  extends Rectangular {
// other methods...
}


// type parameter
trait Ordered[T] {
  def compare(that: T): Int

  def <(that: T): Boolean = (this compare that) < 0
  def >(that: T): Boolean = (this compare that) > 0
  def <=(that: T): Boolean = (this compare that) <= 0
  def >=(that: T): Boolean = (this compare that) >= 0
  }
```


### IntQueue

```scala
    abstract class IntQueue {
      def get(): Int
      def put(x: Int)
    }

    import scala.collection.mutable.ArrayBuffer
    class BasicIntQueue extends IntQueue {
      private val buf = new ArrayBuffer[Int]
      def get() = buf.remove(0)
      def put(x: Int) { buf += x }
    }

    trait Doubling extends IntQueue {
      abstract override def put(x: Int) { super.put(2 * x) }
    }

    class MyQueue extends BasicIntQueue with Doubling
    val queue = new MyQyeye
    // defines no new code, simply mix with trait, SAME AS:
    val queue = new BasicIntQueue with Doubling
```

Order of filtering

```scala
trait Incrementing extends IntQueue {
   abstract override def put(x: Int) { super.put(x + 1) }
}
trait Filtering extends IntQueue {
  abstract override def put(x: Int) {
    if (x >= 0) super.put(x)
  }
}

val queue = (new BasicIntQueue
             with Incrementing with Filtering)
queue.put(-1); queue.put(0); queue.put(1)
queue.get() //Int = 1
queue.get() //Int = 2
//traits further to the right take effect first
// if with Filtering with Incrementing 
// will output 0,1,2
```


Sealed Trait
A sealed trait can only be extended inside the same source file.

### trait vs multiple inheritance

 the interpretation of super. 

 With multiple inheritance, the method called by a super call can be determined right where the call appears. 

 With traits, the method called is determined by a linearization of the classes and traits that are mixed into a class, which enables the stacking of modifications.

 ```scala
  class Animal 
  trait Furry extends Animal
  trait HasLegs extends Animal
  trait FourLegged extends HasLegs
  class Cat extends Animal with Furry with FourLegged

 ```

linearization:
Furry, Animal, AnyRef, Any
FourLegged, HasLegs, Animal, AnyRef, Any
Cat, FourLegged, HasLegs, Furry, Animal, AnyRef, Any


### trait vs class
- Not Reused: Concrete Class
- Might Reused in multiple, unrelated classes: Trait
- Inherit from Java code: Abstract class
- Distributed in compiled form: Abstract class
- Efficiency is very important: Any Class
- If you still do not know: Trait


## 9. Packages and Imports


### Packages
Java Syntax: package whole file

    package bobsrockets.navigation
    class Navigator

C# namespace
```scala
    package bobsrockets {
      package navigation {
        class Navigator
      }
      package launch {
        class Booster {
          // No need to say bobsrockets.navigation.Navigator
          val nav = new navigation.Navigator
        }
      }
    }
```
 Inside the Booster class, it's not necessary to reference Navigator as bobsrockets.navigation.Navigator. it can be referred to as simply as navigation.Navigator.

 Also packages in an inner scope hide packages of the same name that are defined in an outer scope

 Accessing hidden package names:
```scala
// In file launch.scala
package launch {
  class Booster3
}

// In file bobsrockets.scala
package bobsrockets {
  package navigation {
    package launch {
      class Booster1
    }
    class MissionControl {
      val booster1 = new launch.Booster1
      val booster2 = new bobsrockets.launch.Booster2
      val booster3 = new _root_.launch.Booster3
    }
  }
  package launch {
    class Booster2
  }
}
```


### Imports

```scala
package bobsdelights

abstract class Fruit(
  val name: String,
  val color: String
)

object Fruits {
  object Apple extends Fruit("apple", "red")
  object Orange extends Fruit("orange", "orange")
  object Pear extends Fruit("pear", "yellowish")
  val menu = List(Apple, Orange, Pear)
}
```



```scala
  // easy access to Fruit
  import bobsdelights.Fruit
  
  // easy access to all members of bobsdelights
  import bobsdelights._
  
  // easy access to all members of Fruits
  import bobsdelights.Fruits._
```
1. correspond to Java's single type import, 
2. correspond to Java's on-demand import. written with a trailing underscore (_) instead of an asterisk (*) (after all, * is a valid identifier in Scala!)
3. correspond to Java's import of static class fields.

e.g.
```scala
    def showFruit(fruit: Fruit) {
      import fruit._
      println(name +"s are "+ color)
    }

    import java.util.regex  
    class AStarB {
      // Accesses java.util.regex.Pattern
      val pat = regex.Pattern.compile("a*b")
    }
```

Imports in Scala can also rename or hide members

```scala
import Fruits.{Apple => McIntosh, Orange}
import java.sql.{Date => SQLDate}
import Fruits.{Apple => McIntosh, _}
import Fruits.{Pear => _, _}
```


## 10. Assertion and Unit Testing

### Assertion & Ensuring
```scala
def above(that: Element): Element = { 
  val this1 = this widen that.width 
  val that1 = that widen this.width 
  assert(this1.width == that1.width)
  elem(this1.contents ++ that1.contents) 
}

private def widen(w: Int): Element =
  if (w <= width) 
    this 
  else { 
    val left = elem(' ', (w - width) / 2, height) 
    var right = elem(' ', w - width - left.width, height) 
    left beside this beside right 
} ensuring (w <= _.width)
```

Assertions (and ensuring checks) can be enabled and disabled using the JVM's -ea and -da command-line flags. When enabled, each assertion serves as a little test that uses the actual data encountered as the software runs.


### Scala Test
create classes that extend org.scalatest.Suite and define test methods in those classes. A Suite represents a suite of tests. Test methods start with "test".

```scala
import org.scalatest.Suite
import Element.elem

class ElementSuite extends Suite {

  def testUniformElement() {
    val ele = elem('x', 2, 3)
    assert(ele.width == 2)
  }
}

scala> (new ElementSuite).execute()
```

Functional Suite: define test as function values rather than method
```scala
import org.scalatest.FunSuite
import Element.elem

class ElementSuite extends FunSuite {
  test("elem result should have passed width") {
    val ele = elem('x', 2, 3)
    assert(ele.width == 2)
  }
}
```

expect(): emphasize this distinction
```
  expect(2) {
    ele.width
  }
```

intercept : check that a method throws an expected exception
```
  intercept[IllegalArgumentException] {
   elem('x', -2, 3)
  }
```

specifications: In the behavior-driven development (BDD) testing style, the emphasis is on writing human-readable specifications of the expected behavior of code, and accompanying tests that verify the code has the specified behavior. 

```scala
import org.scalatest.Spec
class ElementSpec extends Spec {

  describe("A UniformElement") {

    it("should have a width equal to the passed value") {
      val ele = elem('x', 2, 3)
      assert(ele.width === 2)
    }

    it("should have a height equal to the passed value") {
      val ele = elem('x', 2, 3)
      assert(ele.height === 3)
    }

    it("should throw an IAE if passed a negative width") {
      intercept[IllegalArgumentException] {
        elem('x', -2, 3)
      }
    }
  }
}
```

## 11. Case Classes and Pattern Matching

### Case Classes

```scala
    abstract class Expr
    case class Var(name: String) extends Expr
    case class Number(num: Double) extends Expr
    case class UnOp(operator: String, arg: Expr) extends Expr
    case class BinOp(operator: String, 
        left: Expr, right: Expr) extends Expr
```
The hierarchy includes an abstract base class Expr with four subclasses, one for each kind of expression being considered.[1] The bodies of all five classes are empty. 

As mentioned previously, in Scala you can leave out the braces around an empty class body if you wish, so class C is the same as class C {}.

Using the case modifier makes the Scala compiler add some syntactic conveniences to your class.

- a factory method with the name of the class
  This means you can write `v=Var("x")` to construct a Var object instead of the slightly longer new Var("x"), good for nesting: `val op = BinOp("+", Number(1), v)`

- all arguments implicitly get a val prefix
  maintained as fields: `v.name=='x'`

- compiler adds "natural" implementations of methods toString, hashCode, and equals to your class, so elements of case classes are always compared structurally: `op.right == Var("x")`



### Patter matching
```
    def simplifyTop(expr: Expr): Expr = expr match {
      case UnOp("-", UnOp("-", e))  => e   // Double negation
      case BinOp("+", e, Number(0)) => e   // Adding zero
      case BinOp("*", e, Number(1)) => e   // Multiplying by one
      case _ => expr
    }
```

### Kinds of Patterns

- wildcard _: matches any object, or ignore part of object you do not care about
- constant pattern: matches only itself
- variable pattern: match any object to act on object
- constructor pattern: support deep match
- sequence pattern: `case List(0,_*)`
- tuple pattern: `case (a,b,c)`
- typed pattern: `case m:Map[_,_]`


- expr.isInstanceOf[String]


```
    expr match {
      case 0 => "zero"
      case somethingElse => "not zero: "+ somethingElse
    }
```

Variable pattern start with lowercase
Should use Pi or `pi` for constant variable in this case.
```scala
val pi = Math.Pi
E match {
           case pi => "strange math? Pi = "+ pi
           case _ => "OK"  
         }
<console>:9: error: unreachable code
           case _ => "OK"  
                     ^
```


##### Type erasure for map
Scala uses the erasure model of generics, just like Java does. This means that no information about type arguments is maintained at runtime. Consequently, there is no way to determine at runtime whether a given Map object has been created with two Int arguments, rather than with arguments of different types.

```scala
  scala> def isIntIntMap(x: Any) = x match {
           case m: Map[Int, Int] => true
           case _ => false
         }
  warning: there were unchecked warnings; re-run with 
     -unchecked for details
  isIntIntMap: (Any)Boolean

  scala> isIntIntMap(Map("abc" -> "abc"))
  res18: Boolean = true
```

you can only inspect array in this way, as it is specailly handled in scala

##### Variable binding as a whole
```
    expr match {
      case UnOp("abs", e @ UnOp("abs", _)) => e
      case _ =>
    }
```


### Pattern Guards

```scala
  scala> def simplifyAdd(e: Expr) = e match {
           case BinOp("+", x, x) => BinOp("*", x, Number(2))
           case _ => e
         }
  <console>:10: error: x is already defined as value x
           case BinOp("+", x, x) => BinOp("*", x, Number(2))
                              ^

  scala> def simplifyAdd(e: Expr) = e match {
           case BinOp("+", x, y) if x == y => BinOp("*", x, Number(2))
           case _ => e
         }
  simplifyAdd: (Expr)Expr


  // match only positive integers
  case n: Int if 0 < n => ...  
  
  // match only strings starting with the letter `a'
  case s: String if s(0) == 'a' => ... 
```

This fails, because Scala restricts patterns to be linear: a pattern variable may only appear once in a pattern. However, you can re-formulate the match with a pattern guard (if expression)



###  Sealed classes
pattern match should cover all of the possible cases, but what if there is not a sensible default behavior?

Use sealed class
```scala
    sealed abstract class Expr
    case class Var(name: String) extends Expr
    case class Number(num: Double) extends Expr
    case class UnOp(operator: String, arg: Expr) extends Expr
    case class BinOp(operator: String, 
        left: Expr, right: Expr) extends Expr

  def describe(e: Expr): String = (e: @unchecked) match {
    case Number(_) => "a number"
    case Var(_)    => "a variable"
  }
```

### The Option Type
Scala has a standard type named Option for optional values. Such a value can be of two forms. It can be of the form Some(x) where x is the actual value. Or it can be the None object, which represents a missing value.

Optional values are produced by some of the standard operations on Scala's collections. For instance, the get method of Scala's Map produces Some(value) if a value corresponding to a given key has been found, or None if the given key is not defined in the Map. Here's an example:

```scala
val capitals = Map("France" -> "Paris", "Japan" -> "Tokyo")
scala> capitals get "France"
res21: Option[java.lang.String] = Some(Paris)

scala> capitals get "North Pole"
res22: Option[java.lang.String] = None


//Usage
def show(x: Option[String]) = x match {
   case Some(s) => s
   case None => "?"
}

scala> show(capitals get "Japan")
res23: String = Tokyo

scala> show(capitals get "France")
res24: String = Paris

scala> show(capitals get "North Pole")
res25: String = ?

```


### Patterns Everywhere


1. Patterns in variable definitions

val (number, string) = myTuple

2. Case sequences as partial functions

```scala

  val withDefault: Option[Int] => Int = {
    case Some(x) => x
    case None => 0
  }
// useful in actor library
  react {
    case (name: String, actor: Actor) => {
      actor ! getip(name)
      act()
    }
    case msg => {
      println("Unhandled message: "+ msg)
      act()
    }
  }
```



Partial function 

```scala
val second: List[Int] => Int = {
    case x :: y :: _ => y
}
// Do not compile when List=()

val second: PartialFunction[List[Int],Int] = {
  case x :: y :: _ => y
}

// has method `isDefinedAt()`
second.isDefinedAt(List()) // False

new PartialFunction[List[Int], Int] {
  def apply(xs: List[Int]) = xs match {
    case x :: y :: _ => y 
  }
  def isDefinedAt(xs: List[Int]) = xs match {
    case x :: y :: _ => true
    case _ => false
  }
}
```

// Patterns in for expressions
```scala
for ((country, city) <- capitals)
             println("The capital of "+ country +" is "+ city)

val results = List(Some("apple"), None,Some("orange"))
scala> for (Some(fruit) <- results) println(fruit)
    apple
    orange
```

## 12. Working with list

```scala
  val fruit: List[String] = List("apples", "oranges", "pears")
  val nums: List[Int] = List(1, 2, 3, 4)
  val diag3: List[List[Int]] =
    List(
      List(1, 0, 0),
      List(0, 1, 0),
      List(0, 0, 1)
    )
  val empty: List[Nothing] = List()

```

- lists are immutable,  act like java string pool
- lists have a recursive structure (i.e. linked list) whereas arrays are flat.
- the elements of a list all have the same type
- The list type in Scala is covariant
    + if S is a subtype of T, then List[S] is a subtype of List[T].
-  empty list has type List[Nothing], also type of List[String]

### definition

```scala
  val fruit = "apples" :: ("oranges" :: ("pears" :: Nil))
  val nums  = 1 :: (2 :: (3 :: (4 :: Nil)))
  val diag3 = (1 :: (0 :: (0 :: Nil))) ::
              (0 :: (1 :: (0 :: Nil))) ::
              (0 :: (0 :: (1 :: Nil))) :: Nil
  val empty = Nil
```

Same as Haskell List

```scala
// list concatenation
val oneTwo = List(1, 2)
val threeFour = List(3, 4)
val oneTwoThreeFour = oneTwo ::: threeFour
println(""+ oneTwo +" and "+ threeFour +" were not mutated.")
println("Thus, "+ oneTwoThreeFour +" is a new list.")

// cons
val twoThree = List(2, 3)
val oneTwoThree = 1 :: twoThree
println(oneTwoThree)

// same as
val oneTwoThree = 1 :: 2 :: 3 :: Nil
```

Why not append? Time complexity grows linealy, while :: constantly

### list operations

```scala
val thrill = "Will" :: "fill" ::"until" :: Nil

thrill(2)   // returns "until"

thrill.length   // 3
thrill.isEmpty  // false
thrill.exists(s => s == "until")    // returns true
thrill.count(s => s.length == 4)    // Counts string with length 4 (returns 2)

thrill.take(2)      // list with left 2 elements 
thrill.drop(2)      // list without left 2 elements  "until"
thrill.dropRight(2) // list without right 2 elements "Will"

thrill.filter(s => s.length == 4)   // returns List("Will", "fill")
thrill.remove(s => s.length == 4)   // returns List("until")
thrill.reverse  // returns List("until", "fill", "Will")

thrill.forall(s =>s.endsWith("l"))  //whether all end with "l" (true)
thrill.foreach(s => print(s))  // prints "Willfilluntil"
thrill.foreach(print)   // Same as the previous
thrill.mkString(", ")   // returns "Will, fill, until"

thrill.head // first element
thrill.tail // all but first
thrill.last // last element
thrill.init // all but last

thrill.map(s => s + "y")   // returns List("Willy", "filly", "untily")

thrill.sort((s, t) => s.charAt(0).toLowerCase < t.charAt(0).toLowerCase)    
//sort in alphabetical order  (returns List("fill", "until", "Will"))
```

list concatenation
List(1, 2, 3) ::: List(4)


### Divide and Conquer principle

append the second list after the first
```scala
  def append[T](xs: List[T], ys: List[T]): List[T] =
    xs match { 
      case List() => // ??
      case x :: xs1 => // ??
    }

  def append[T](xs: List[T], ys: List[T]): List[T] =
    xs match { 
      case List() => ys
      case x :: xs1 => x :: append(xs1, ys)
    }

```

##### Make
```
  scala> List.make(5, 'a')
  res54: List[Char] = List(a, a, a, a, a)
  
  scala> List.make(3, "hello")
  res55: List[java.lang.String] = List(hello, hello, hello)

```
##### Apply
```
  scala> abcde apply 2 // rare in Scala
  res11: Char = c

  scala> abcde(2)      // shortened version
  res12: Char = c

  scala> abcde.indices
  res13: List[Int] = List(0, 1, 2, 3, 4)
```

in fact, apply is simply defined by a combination of drop and head:
`xs apply n`    equals    `(xs drop n).head`

##### SplitAt
```
  scala> abcde splitAt 2
  res10: (List[Char], List[Char]) = (List(a, b),List(c, d, e))
```

##### Zip
```
scala> abcde.indices zip abcde
res14: List[(Int, Char)] = List((0,a), (1,b), (2,c), (3,d), (4,e))

scala> val zipped = abcde zip List(1, 2, 3)
zipped: List[(Char, Int)] = List((a,1), (b,2), (c,3))


scala> val zipped = "abcde".toList zip List(1, 2, 3)
zipped: List[(Char, Int)] = List((a,1), (b,2), (c,3))

scala> List.unzip(zipped)
res56: (List[Char], List[Int]) = (List(a, b, c),
    List(1, 2, 3))


scala> abcde.zipWithIndex
res15: List[(Char, Int)] = List((a,0), (b,1), (c,2), (d,3), (e,4))
```

##### toString & mkString


  mkString (pre, sep, post)
  //  pre + xs(0) + sep + ...+ sep + xs(xs.length - 1) + post

  xs mkString sep
  // xs mkString ("", sep, "")

  xs.mkString
  // xs mkString ""


```
  scala> abcde.toString
  res16: String = List(a, b, c, d, e)

  scala> abcde mkString ("[", ",", "]")
  res17: String = [a,b,c,d,e]
  
  scala> abcde mkString ""
  res18: String = abcde
  
  scala> abcde.mkString
  res19: String = abcde
  
  scala> abcde mkString ("List(", ", ", ")")
  res20: String = List(a, b, c, d, e)
```


Also have addString for StringBuilder
```
  scala> val buf = new StringBuilder
  buf: StringBuilder = 
  
  scala> abcde addString (buf, "(", ";", ")")
  res21: StringBuilder = (a;b;c;d;e)
```


Conversion
```
  scala> val arr = abcde.toArray
  arr: Array[Char] = Array(a, b, c, d, e)
  
  scala> arr.toString
  res22: String = Array(a, b, c, d, e)
  
  scala> arr.toList
  res23: List[Char] = List(a, b, c, d, e)

// copyto

  scala> val arr2 = new Array[Int](10)
  arr2: Array[Int] = Array(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
  
  scala> List(1, 2, 3) copyToArray (arr2, 3)
  
  scala> arr2.toString
  res25: String = Array(0, 0, 0, 1, 2, 3, 0, 0, 0, 0)  

// iterator

  scala> val it = abcde.elements
  it: Iterator[Char] = non-empty iterator
  
  scala> it.next
  res26: Char = a
  
  scala> it.next
  res27: Char = b

```

### Merge Sort
```scala
def msort[T](less: (T, T) => Boolean)
    (xs: List[T]): List[T] = {

  def merge(xs: List[T], ys: List[T]): List[T] =
    (xs, ys) match {
      case (Nil, _) => ys
      case (_, Nil) => xs
      case (x :: xs1, y :: ys1) =>
        if (less(x, y)) x :: merge(xs1, ys)
        else y :: merge(xs, ys1)
    }

  val n = xs.length / 2
  if (n == 0) xs
  else {
    val (ys, zs) = xs splitAt n
    merge(msort(less)(ys), msort(less)(zs))
  }
}
```

### High order function

##### Mapping over list

map and flatmap

```scala
List(1, 2, 3) map (_ + 1) // List(2, 3, 4)
  
val words = List("the", "quick", "brown", "fox")
words map (_.length)  // List(3, 5, 5, 3)
words map (_.toList.reverse.mkString) // List(eht, kciuq, nworb, xof)

words map (_.toList)
// List(List(t, h, e), List(q, u, i, c, k), List(b, r, o, w, n), List(f, o, x))
  
words flatMap (_.toList)
// List(t, h, e, q, u, i, c, k, b, r, o, w, n, f, o, x)
// automatic concatenate

List.range(1, 5) flatMap (
           i => List.range(1, i) map (j => (i, j))
         )
// (1,2,3,4) -> (1)(1,2)(1,2,3)
// corespond i->(2)(3,3)(4,4,4)
// output List((2,1), (3,1), (3,2), (4,1), (4,2), (4,3))

//SAME AS
 for (i <- List.range(1, 5); j <- List.range(1, i)) yield (i, j) 
```

foreach

```
 List(1, 2, 3, 4, 5) foreach (sum += _)
```

##### filtering list

filter, partition, find, takeWhile, dropWhile, and span
```scala
List(1, 2, 3, 4, 5) filter (_ % 2 == 0) //List(2, 4)
words filter (_.length == 3) //List(the, fox)

List(1, 2, 3, 4, 5) partition (_ % 2 == 0) // (List(2, 4),List(1, 3, 5))
List(1, 2, 3, 4, 5) find (_ <= 0) //None

List(1, 2, 3, -4, 5) takeWhile (_ > 0) //List(1, 2, 3)
words dropWhile (_ startsWith "t") // List(quick, brown, fox)

//xs span p    equals    (xs takeWhile p, xs dropWhile p)
List(1, 2, 3, -4, 5) span (_ > 0) // (List(1, 2, 3),List(-4, 5))
```

##### Prediction
```
def hasZeroRow(m: List[List[Int]]) = 
           m exists (row => row forall (_ == 0))
```

##### FoldingList /: and :\

sum(List(a, b, c)) 

product(List(a, b, c)) 

A fold left operation `(z /: xs) (op)` involves 3 objects: 
a start value z, a list xs, and a binary operation op. 

`(z /: List(a, b, c)) (op)`    equals    `op(op(op(z, a), b), c)`
`(List(a, b, c) :\ z) (op)`   equals    `op(a, op(b, op(c, z)))`


```scala
scala>  ("" /: words) (_ +" "+ _)
res46: java.lang.String =  the quick brown fox

scala> (words.head /: words.tail)  (_ +" "+ _)
res47: java.lang.String = the quick brown fox



def flattenLeft[T](xss: List[List[T]]) =
    (List[T]() /: xss) (_ ::: _)

def flattenRight[T](xss: List[List[T]]) =
    (xss :\ List[T]()) (_ ::: _)
```

- the [T] in empty list could not be ommited.
  + neccesary make the type inferencer work
- time complexity of List concatenation xs ::: ys
    +  proportional to its first argument xs
    + FoldRight is more effective

```
 def reverseLeft[T](xs: List[T]) =
    (List[T]() /: xs) {(ys, y) => y :: ys}
```

##### sort
sort means the order for result
```
scala> List(1, -3, 4, 2, 6) sort (_ < _)
res48: List[Int] = List(-3, 1, 2, 4, 6)

scala> words sort (_.length > _.length)
res49: List[java.lang.String] = List(quick, brown, fox, the)
```


###  Understanding Scala's type inference algorithm
```
  scala> msort((x: Char, y: Char) => x > y)(abcde)
  res64: List[Char] = List(e, d, c, b, a)
//with:
  scala> abcde sort (_ > _)
  res65: List[Char] = List(e, d, c, b, a)
```

the short form cannot be used with msort.

Type inference in Scala is flow based. 

In a method application m(args), the inferencer first checks whether the method m has a known type. If it has, that type is used to infer the expected type of the arguments. 

In abcde.sort(_ > _), the type of abcde is List[Char], hence sort is known to be a method that takes an argument of type (Char, Char) => Boolean and produces a result of type List[Char].

The type of msort is a curried, polymorphic method type that takes an argument of type (T, T) => Boolean to a function from List[T] to List[T] where T is some as-yet unknown type. The msort method needs to be instantiated with a type parameter before it can be applied to its arguments.The type inferencer changes its strategy in this case,  first type checks method arguments to determine the proper instance type of the method. 

Alternate solution
1. pass an explicit type parameter
```
  scala> msort[Char](_ > _)(abcde)
  res66: List[Char] = List(e, d, c, b, a)
```
2. swap msort method
```
  def msortSwapped[T](xs: List[T])(less:
      (T, T) => Boolean): List[T] = {
  
    // same implementation as msort,
    // but with arguments swapped
    // inferencer first inspect List[T] now
  }
```


##13. Collection

### 13.1 Iterable

Supertrait of both mutable and immutable version of seqs, sets and maps

Iterable: represents collection objects that can produce an Iterator via a method named elements:`def elements: Iterator[A]`. Iterable provides dozens of useful concrete methods, like  map, flatMap, filter, exists, and find.

Iterator: extends AnyRef, represent mechanism used to perform a iteration. Can be used just once, cannot reuse it. `def hasNext:Boolean` and `def next: A`

### 13.2 Sequence

#### 13.2.1 Lists

- immutable linked-list 
- fast addition and removal for head, not for arbitrary index
- not for the end, need build a backward list

```
scala> val colors = List("red", "blue", "green")
colors: List[java.lang.String] = List(red, blue, green)

scala> colors.head
res0: java.lang.String = red

scala> colors.tail
res1: List[java.lang.String] = List(blue, green)
```

#### 13.2.2 Arrays

- efficiently access an element at an arbitrary position
-  accessed in Scala by placing an index in parentheses, not square brackets as in Java

```
scala> val fiveInts = new Array[Int](5)
fiveInts: Array[Int] = Array(0, 0, 0, 0, 0)

scala> val fiveToOne = Array(5, 4, 3, 2, 1)
fiveToOne: Array[Int] = Array(5, 4, 3, 2, 1)

scala> fiveInts(0) = fiveToOne(4)

scala> fiveInts
res1: Array[Int] = Array(1, 0, 0, 0, 0)
```

#### 13.2.3 List Buffers
- a mutable object (contained in package scala.collection.mutable), which can help you build lists more efficiently when you need to append
-  provides constant time append and prepend operations. You append elements with the += operator, and prepend them with the +: operator.

```
  scala> import scala.collection.mutable.ListBuffer
  import scala.collection.mutable.ListBuffer
  
  scala> val buf = new ListBuffer[Int]             
  buf: scala.collection.mutable.ListBuffer[Int] = ListBuffer()
  
  scala> buf += 1                                  
  
  scala> buf += 2                                  
  
  scala> buf     
  res11: scala.collection.mutable.ListBuffer[Int]
    = ListBuffer(1, 2)
  
  scala> 3 +: buf                                  
  res12: scala.collection.mutable.Buffer[Int]
    = ListBuffer(3, 1, 2)
  
  scala> buf.toList
  res13: List[Int] = List(3, 1, 2)
```


#### 13.2.4 Array Buffer
- additionally add and remove elements from the beginning and end of the sequence
- The new addition and removal operations are constant time on average, but occasionally require linear time due to the implementation needing to allocate a new array to hold the buffer's contents.
```
scala> import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ArrayBuffer

scala> val buf = new ArrayBuffer[Int]()
buf: scala.collection.mutable.ArrayBuffer[Int] = 
  ArrayBuffer()

scala> buf += 12
scala> buf += 15

scala> buf.length
res17: Int = 2

scala> buf(0)
res18: Int = 12
```

#### 13.2.5 Queue
Immutable Queue
```
scala> import scala.collection.immutable.Queue
scala> val empty = new Queue[Int]           
scala> val has1 = empty.enqueue(1)
scala> val has123 = has1.enqueue(List(2, 3))
  has123: scala.collection.immutable.Queue[Int] = Queue(1,2,3)

scala> val (element, has23) = has123.dequeue
element: Int = 1
has23: scala.collection.immutable.Queue[Int] = Queue(2,3)
```

Mutable Queue
```
  scala> import scala.collection.mutable.Queue                
  scala> val queue = new Queue[String]

  scala> queue += "a"
  scala> queue ++= List("b", "c")
  scala> queue
  res21: scala.collection.mutable.Queue[String] = Queue(a, b, c)
  
  scala> queue.dequeue
  res22: String = a
  
  scala> queue
  res23: scala.collection.mutable.Queue[String] = Queue(b, c)

```

#### 13.2.6 Stack

```
  scala> import scala.collection.mutable.Stack
  scala> val stack = new Stack[Int]           

  scala> stack.push(1)
  scala> stack.push(2)
  res3: scala.collection.mutable.Stack[Int] = Stack(1, 2)
  
  scala> stack.top
  res8: Int = 2
  res9: scala.collection.mutable.Stack[Int] = Stack(1, 2)
  
  scala> stack.pop    
  res10: Int = 2
  res11: scala.collection.mutable.Stack[Int] = Stack(1)
```

### 13.3 Set & Map
By default when you write "Set" or "Map" you get an immutable object

It is defined in predef object, which is implicitly imported into every Scala source file
```
    object Predef {
      type Set[T] = scala.collection.immutable.Set[T]
      type Map[K, V] = scala.collection.immutable.Map[K, V]
      val Set = scala.collection.immutable.Set
      val Map = scala.collection.immutable.Map
      // ...
    }
```

#### 13.3.1 Set

```
scala> val text = "See Spot run. Run, Spot. Run!"
scala> val wordsArray = text.split("[ !,.]+")    
wordsArray: Array[java.lang.String] =
   Array(See, Spot, run, Run, Spot, Run)

scala>  val words = mutable.Set.empty[String]
words: scala.collection.mutable.Set[String] = Set()

scala> for (word <- wordsArray)
         words += word.toLowerCase

scala> words
res25: scala.collection.mutable.Set[String] =
  Set(spot, run, see)
```


Set Operations
```
val nums = Set(1, 2, 3)
nums +5
nums - 3  
nums ++ List(5, 6)
nums -- List(1, 2)
nums ** Set(1, 3, 5, 7)  // the intersection of two sets
nums.size 
nums.contains(3)  // Checks for inclusion (returns true)

import scala.collection.mutable
val words = mutable.Set.empty[String]
words += "the" 
words -= "the" 
words ++= List("do", "re", "mi")
words --= List("do", "re") 
words.clear  // Removes all elements (words.toString returns Set())
```

#### 13.3.2 Map 
```
scala> val map = mutable.Map.empty[String, Int]
  scala> map("hello") = 1
  
  scala> map("there") = 2
  
  scala> map
  res28: scala.collection.mutable.Map[String,Int] =
    Map(hello -> 1, there -> 2)
```

WordCount
```
def countWords(text: String) = {
         val counts = mutable.Map.empty[String, Int]
         for (rawWord <- text.split("[ ,!.]+")) {
           val word = rawWord.toLowerCase
           val oldCount = 
             if (counts.contains(word)) counts(word)
             else 0
           counts += (word -> (oldCount + 1))
         }
         counts
       }
countWords: (String)scala.collection.mutable.Map[String,Int]

scala> countWords("See Spot run! Run, Spot. Run!")
res30: scala.collection.mutable.Map[String,Int] =
  Map(see -> 1, run -> 3, spot -> 2)
```

Oprations
```
val nums = Map("i" -> 1, "ii" -> 2) 
nums + ("vi" -> 6)  
nums - "ii" 
nums ++ List("iii" -> 3, "v" -> 5)  
nums -- List("i", "ii") 
nums.size
nums.contains("ii") 
nums("ii")  
nums.keys  // returns an Iterator over keys
nums.keySet // returns the keys as a set 
nums.values // returns an Iterator over values
nums.isEmpty  

import scala.collection.mutable 
val words = mutable.Map.empty[String, Int] 
words += ("one" -> 1) Adds a map entry from "one" to 1 
words -= "one"
words ++= List("one" -> 1, "two" -> 2, "three" -> 3)
words --= List("one", "two")
```

Sorted sets and maps
- traits SortedSet and SortedMap
- only have immutable variants
- implemented by classes TreeSet and TreeMap, which use a red-black tree to keep elements (in the case of TreeSet) or keys (in the case of TreeMap) in order
- The order is determined by the Ordered trait, which the element type of the set, or key type of the map, must either mix in or be implicitly convertable to. 
```scala
scala> import scala.collection.immutable.TreeSet
scala> val ts = TreeSet(9, 3, 1, 8, 0, 2, 7, 4, 6, 5)
ts: scala.collection.immutable.SortedSet[Int] =
  Set(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)

scala> val cs = TreeSet('f', 'u', 'n')
cs: scala.collection.immutable.SortedSet[Char] = Set(f, n, u)


scala> import scala.collection.immutable.TreeMap
scala> var tm = TreeMap(3 -> 'x', 1 -> 'x', 4 -> 'x')
tm: scala.collection.immutable.SortedMap[Int,Char] =
  Map(1 -> x, 3 -> x, 4 -> x)

scala> tm += (2 -> 'x')
scala> tm
res38: scala.collection.immutable.SortedMap[Int,Char] =
    Map(1 -> x, 2 -> x, 3 -> x, 4 -> x)
```

Synchronized sets and maps
- if you needed a thread-safe map, you could mix the SynchronizedMap trait into whatever particular map implementation you desired

```scala
   import scala.collection.mutable.{Map, SynchronizedMap, HashMap}
  
    object MapMaker {
        def makeMap: Map[String, String] = {
            new HashMap[String, String] with SynchronizedMap[String, String] {
              override def default(key: String) = "Why do you want to know?"
          }
      }
    }
```

the Scala compiler will generate a synthetic subclass of HashMap that mixes in SynchronizedMap, and create (and return) an instance of it. 

This synthetic class will also override a method named default, when asked the value for a particular key, but it doesn't have a mapping for that key, will return NoSuchElementException or overrided default value. 

### 13.4 Mutable vs Immutable

Friendly to reason
- When in doubt, it is better to start with an immutable collection and change it later if you need to, because immutable collections can be easier to reason about than mutable ones.
- If you find some code that uses mutable collections becoming complicated and hard to reason about, consider whether it would help to change some of the collections to immutable alternatives.

Storage
- immutable collections can usually be stored more compactly than mutable ones if the number of elements stored in the collection is small.
- Scala collection stores immutable map/set with up to four entry in a single object, which takes 16~40 bytes
- mutable (80 + 16 x entry) vs immutable  1 pointer + (4~10) x entry

Immutable Syntax Sugar
- do not support a true += method, but can update using var
- Scala will try interpreting += as a = a + b
- so no additional change except import is needed when migration

```
var people = Set("Nancy", "Jane")
people += "Bob"
people ++= List("Tom", "Harry")
```

### 13.5 Initialization

- pass initial elements to factory method
    + Scala infer the element type automatically
    + if use special type, need say explicitly
- initialize with another collection 
    + tree set: cannot pass to factory methods, need create a empty set and add   with ++ operator
    + list: invoke toList Method
    + array: invoke toArray Method
- mutable <=> immutable
    + similar to treeset


```
List(1, 2, 3)
Set('a', 'b', 'c')

import scala.collection.mutable
mutable.Map("hi" -> 2, "there" -> 5)
Array(1.0, 2.0, 3.0)

val stuff = mutable.Set[Any](42)
stuff += "abracadabra"

// treeset
val colors = List("blue", "yellow", "red", "green")
val treeSet = TreeSet[String]() ++ colors

// array&list
treeSet.toList
treeSet.toArray   // with alphabetical order (from treeset)

//set
val mutaSet = mutable.Set.empty ++ treeSet
val immutaSet = Set.empty ++ mutaSet

// map
val muta = mutable.Map("i" -> 1, "ii" -> 2)
val immu = Map.empty ++ muta
```


### 13.6 Tuple

- combines a fixed number of items together so that they can be passed around as a whole
- a tuple can hold objects with different types

A common application of tuples is returning multiple values from a method. For example, here is a method that finds the longest word in a collection and also returns its index:

```scala
 def longestWord(words: Array[String]) = {
    var word = words(0)
    var idx = 0
    for (i <- 1 until words.length)
      if (words(i).length > word.length) {
        word = words(i)
        idx = i
      }
    (word, idx)
  }

  scala> val longest = longestWord("The quick brown fox".split(" "))
  longest: (String, Int) = (quick,1)

```

