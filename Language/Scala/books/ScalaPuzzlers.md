
# Scala Puzzlers

## 1. Hi There


```scala
scala> List(1, 2).map { i => println("Hi"); i + 1 }
Hi
Hi
res23: List[Int] = List(2, 3)

scala> List(1, 2).map { println("Hi"); _ + 1 }
Hi
res25: List[Int] = List(2, 3)
```


Inside anoymous function

- often passed with arguments, common to surround by {}
- actually delimit a block expression
    + first: one function
        * `val func1 = (i:Int)=>{println("Hi");i+1}`
    + second: two expressions
        * `val func2 = {println("Hi"); (_:Int)+1 }`
        * Hi is print when evaluation, not in actual function
- lesson
    + scope of anonymous function with placeholder syntax
    + stretch only to the expression with _

```scala
val regularFunc =
{ a: Any => println("foo"); println(a); "baz" }
regularFunc: Any => String = <function1>

val confinedFunc =
{ println("foo"); { a: Any => println(a) }; "baz" }
confinedFunc: String = baz
```


## 2. UPSTAIRS downstairs

- scala offer convenient ways to initialize multiple variables
- UPPERCASE is tricky when multiple value assignment
    + multiple-variable assignments are based on pattern matching
    + within a pattern match, variables starting with an uppercase letter take on a special meaning: they are stable identifier
    + stable identifier is never assigned value during pattern match
- lowercase val with backticks can also be treated as stable identifier

```scala
var MONTH = 12; var DAY = 24            // fine
var (HOUR, MINUTE, SECOND) = (12, 0, 0) // compile error
var (hour, minute, second) = (12, 0, 0) // right
var (MONTH, day) = (12, 15)             // day = 15
var (MONTH, day) = (13, 15)             // MatchError
```


Pattern Match Example
```scala
final val TheAnswer = 42
def checkGuess(guess: Int) = guess match {
    case TheAnswer => "Your guess is correct"
    case _ => "Try again"
}

final val theAnswer = 42
def checkGuess(guess: Int) = guess match {
    case `theAnswer` => "Your guess is correct"
    case _ => "Try again"
}

```

## 3. Location, Location, Location

- Scala allow accept parameter and assign to class members in one go.
- `class MyClass(val member1, val member2, ...) {...}`


```scala
trait A {
    val audience: String
    println("Hello " + audience)
}

class BMember(a: String = "World") extends A {  // Hello null
    val audience = a
    println("I repeat: Hello " + audience)      // Hello Readers
}

class BConstructor(val audience: String = "World") extends A { // Hello Readers
    println("I repeat: Hello " + audience)      // Hello Readers
}
new BMember("Readers")
new BConstructor("Readers")
```

- When will assignment of "Readers" to audience become visible?
    + both class decalaration are of the form 
    + `class c(param1) extends superclass { statements }`
- step by step
    + "Readers" is evaluated
        * BMember: just evaluated
        * BConstructor: evaluated and assigned straight away
    + superclass constructor A
    + statement sequence in the body of BMembers & BConstructors
- Usually prefer Bconstructor pattern
    + need  early definition if used with Bmember pattern
    + it happen before superclass constructor

```scala
class BEarlyDef(a: String = "World") extends {val audience = a} with A {
    println("I repeat: Hello " + audience)
}
```


Full Example about execution orders
```scala
trait A {
    val audience: String
    println("Hello " + audience)
}
trait AfterA {
    val introduction: String
    println(introduction)
}
class BEvery(val audience: String) extends {
    val introduction =
        { println("Evaluating early def"); "Are you there?" }
} with A with AfterA {
    println("I repeat: Hello " + audience)
}

scala> new BEvery({ println("Evaluating param"); "Readers" })

Evaluating param
Evaluating early def
Hello Readers
Are you there?
I repeat: Hello Readers
res3: BEvery = BEvery@6bcc2569
```


## 4. Now You See Me, Now You Don't

```scala
trait A {
    val foo: Int
    val bar = 10
    println("In A: foo: " + foo + ", bar: " + bar)
}
class B extends A {
    val foo: Int = 25
    println("In B: foo: " + foo + ", bar: " + bar)
}
class C extends B {
    override val bar = 99
    println("In C: foo: " + foo + ", bar: " + bar)
}
new C()

In A: foo: 0, bar: 0
In B: foo: 25, bar: 0
In C: foo: 25, bar: 99
```

- Every class has primary constructor
    + formed from all statement in class definition, including fields
    + Scala does not intrinsically differentiate class fields from constructor local values
- Init & Override behavior
    + Superclasses are fully initialized before subclass
    + Members are initialized in order they are declared
    + When val is overridden, it can still only be initialized once
    + overriden val will have default initial value during super class construction, the value in overriden trait is not assigned
- Default values
    + 0 for Byte, Short, Int
    + 0L, 0.0f, 0.0d for Long, Float, Double
    + '\0' for Char
    + false for Boolean
    + () for Unit
    + null for Other Types
- How to initialize bar to 99 from the Early stage (trait A)
    + use `def bar:Int=10` in A
        * drawback: evaluated upon each and every invocation
    + use `lazy val bar = 10` in A
        * used to defer expensive initialization to last possible moment
        * incur slight performance cost due to synchroniztion
        * cause stackoverflow or deadlock when create cyclic reference
    + early initialize in c
        * `class C extends {override val bar = 99} with B {...}`
        * make sure it run before superclass
        * more complex

## 5. The Missing List

```scala
def sumSizes(collections: Iterable[Iterable[_]]): Int =
    collections.map(_.size).sum
sumSizes(List(Set(1, 2), List(3, 4)))
sumSizes(Set(List(1, 2), Set(3, 4)))

List(Set(1, 2), List(3, 4)).map(_.size)
res0: List[Int] = List(2, 2)

Set(Set(1, 2), List(3, 4)).map(_.size)
res0: scala.collection.immutable.Set[Int] = Set(2)
```

- Scala Collection Library
    + will return an Iterable with same type as input type
- solution
    + convert to a known type like toSeq
    + or use fold to eliminate one iteration
    
```scala
def sumSizes(collections: Iterable[Iterable[_]]): Int =
    collections.toSeq.map(_.size).sum

def sumSizes(collections: Iterable[Iterable[_]]): Int =
    collections.foldLeft(0) {
        (sumOfSizes, collection) => sumOfSizes + collection.size
    }
```

## 6. Arg Arrgh! 


```scala
// n application of f to arg: f(f(f(f(arg))))
val result = (1 to n).foldLeft(arg) { (acc, _) => f(acc) }

def applyNMulti[T](n: Int)(arg: T, f: T => T) =
    (1 to n).foldLeft(arg) { (acc, _) => f(acc) }
def applyNCurried[T](n: Int)(arg: T)(f: T => T) =
    (1 to n).foldLeft(arg) { (acc, _) => f(acc) }

def nextInt(n: Int) = n * n + 1
def nextNumber[N](n: N)(implicit numericOps: Numeric[N]) =
    numericOps.plus(numericOps.times(n, n), numericOps.one)

println(applyNMulti(3)(2, nextInt))  // 677
println(applyNCurried(3)(2)(nextInt))  // 677
println(applyNMulti(3)(2.0, nextNumber)) 
// error: could not find implicit value for parameter numericOps: Numeric[N]
println(applyNCurried(3)(2.0)(nextNumber)) // 677.0
```


One liner generator
- fold: natural choice for recursive computations
- Multi
    + In the multi case, why look for Numeric[N] instead of Numeric[Double]
    + compiler try to satisfy type for each parameter individually
    + can not use information provided by different parameter
- 2.0 provided Double information
    + but can not be used when search for numericOps
- Curried Case
    + partially applied function
    + `applyNCurried(3)(2.0) _`
    + `res9: (Double => Double) => Double = <function1>`
- To Fix
    + specify the type
        * `applyNMulti(3)(2.0, nextNumber[Double])`
        * `applyNMulti[Double](3)(2.0, nextNumber)`

## 7. Caught Up in Closures 
```scala
import collection.mutable.Buffer
val accessors1 = Buffer.empty[() => Int]
val accessors2 = Buffer.empty[() => Int]
val data = Seq(100, 110, 120)
var j = 0

for (i <0 until data.length) {
    accessors1 += (() => data(i))
    accessors2 += (() => data(j))
    j += 1
}
accessors1.foreach(a1 => println(a1())) // 100 110 120
accessors2.foreach(a2 => println(a2())) // IndexOutOfBoundsException
```

- how scala enabled function body to access variable
    + To access these free variables when the function is invoked in a
different scope, Scala “closes over” them to create a closure.
    + each time i is stored somewhere its value is copied - therefore it prints the expected result:
    + As j changes inside the loop, all the three closures "see" the very same variable j and not a copy of it. after closure the value of j is already 3.
- For accessor2, inserted is actually a `runtime.IntRef`
    + it become 3 when reference later
- Solution
    + avoid a var
    + or freeze it by assigning to temporary val
        *  `val currentJ = j; accessors2 += (() => data(currentJ))`
        *  before calling `j+=1`

## 8. Map Comprehension
```scala
val xs = Seq(Seq("a", "b", "c"), Seq("g", "h"), Seq("i", "j", "k"))
val ys = for (Seq(x, y, z) <- xs) yield x + y + z 
val zs = xs map { case Seq(x, y, z) => x + y + z }
```

- result
    + ys evaluate to List(abc, ijk)
    + zs have a matchError
- for compresion
    + ` for (i<-0 to 1) yield i+1`
        * desugared to: `0 to 1 map {i=>i+1}`
    + Generate pattern
        * left-hand side of generator is not simple variable but pattern
        * actual mechanic

```scala
for (pattern <- expr) yield fun
// actually becomes
expr withFilter {
    case pattern => true
    case _ => false
} map { case pattern => fun }
```

## 9. Init You, Init Me 
```scala
object XY {
    object X {
        val value: Int = Y.value + 1
    }
    object Y {
        val value: Int = X.value + 1
    }
}
println(if (math.random > 0.5) XY.X.value else XY.Y.value)
```

- When either object X or Y is accessed, then its field value is initialized (objects are not initialized until they are accessed). 
- If object X is initialized first
    + triggers the initialization of object Y.
        * the field X.value is accessed. 
        * The VM takes notice that the initialization of object X is already running and returns the current value of X.value which is zero (the default value for Int fields) 
    + As a consequence Y.value is set to 0 + 1 = 1
    + X.value to 1 + 1 = 2. 
- if object Y is initialized first, then Y.value is initialized with 2 and X.value with 1.
- In this problem, which ever object is accessed first, its field value is initialized to 2, the result of the conditional statement is always 2.

## 10. A Case of Equality

Scala’s case classes are an easy way to represent entities, with factory methods, extractors, and several convenience methods implemented “for free”:

```scala
class Country(val isoCode: String, val name: String)
val homeOfScala = new Country("CH", "Switzerland")
scala> println(homeOfScala equals new Country("CH", "Switzerland"))
false
scala> println(homeOfScala.toString)
$line348.$read$$iw$$iw$Country@39eb8ede

case class CountryCC(isoCode: String, name: String)
val homeOfScalaCC = CountryCC("CH", "Switzerland") // factory method
scala> println(homeOfScalaCC equals CountryCC("CH", "Switzerland"))
true
scala> println(homeOfScalaCC.toString)
CountryCC(CH,Switzerland)
```

What would happen if mix with trait

Scala String hashCode
`"a".##=97  "b".##=98 "ab".##=3105=31*97+98`

```scala
trait SpyOnEquals {
  override def equals(x: Any) = { println("DEBUG: In equals"); super.equals(x) }
}

case class CC()
case class CCSpy() extends SpyOnEquals

val cc1 = new CC() with SpyOnEquals
val cc2 = new CC() with SpyOnEquals
val ccspy1 = CCSpy()
val ccspy2 = CCSpy()

println(cc1 == cc2)             // DEBUG: In equals, true
println(cc1.## == cc2.##)       // true
println(ccspy1 == ccspy2)       // DEBUG: In equals, false
println(ccspy1.## == ccspy2.##) // true
```

- A case class implicitly overrides the methods equals, hashCode and toString of class scala.AnyRef 
    + only if the case class itself does not provide a definition for one of these methods 
    + only if a concrete definition is not given in some base class of the case class (except AnyRef).
- the base trait SpyOnEquals of CCSpy provides an equals method
    + so the case class does not provide its own definition 
    + the comparison of two different instances returns false.
    + hashCode not implemented in SpyOnEquals, so case class equal
- case class CC, no definition of both
    + Mixing in SpyOnEquals when creating instances of the case classes does not affect this.

## 11. If at First You Don’t Succeed... 
```scala
var x = 0
lazy val y = 1 / x
try {
    println(y)
} catch {
    case _: Exception => x = 1
    println(y)
}
```

- Lazy will be recomputed on call if there was an exception at the moment of first access, until some definite value is acquired. 
- So you can use this useful pattern in many situations, for example to handle missing files.

## 12. To Map, or Not to Map 
```scala
case class RomanNumeral(symbol: String, value: Int)
implicit object RomanOrdering extends Ordering[RomanNumeral] {
  def compare(a: RomanNumeral, b: RomanNumeral) = a.value compare b.value
}

import collection.immutable.SortedSet
val numerals = SortedSet(RomanNumeral("M", 1000), RomanNumeral("C", 100), RomanNumeral("X", 10), RomanNumeral("I", 1), RomanNumeral("D", 500), RomanNumeral("L", 50),  RomanNumeral("V", 5))

println("Roman numeral symbols for 1 5 10 50 100 500 1000:")

for (num <- numerals; sym = num.symbol) { print(s"${sym} ") }
I V X L C D M

numerals map { _.symbol } foreach { sym => print(s"${sym} ") }
C D I L M V X
```

Iterating over the sorted set of numerals will return them in that order.

Mapping the numerals to their symbols will result in a sorted **set** of Strings which will be ordered lexicographically. Iterating over them will thus return them in that order.

Solution
- numerals.toSeq before mapping, 
- or print directly instead of map


## 13. Self: See Self 

```
val s1: String = s1
val s2: String = s2 + s2
println(s1.length)  // NullPointer Exception
println(s2.length)  // 8
```

The Scala way to accept recursive definitions for all situations is more consistent than Java
- Value s1 is initialized to null, therefore the expression s1.length ends in a NullPointerException at run-time.
- The initialization of the value s2 actually gets translated to byte code equvalent to `s2 = (new StringBuilder()).append(s2).append(s2).toString()`
- returned "nullnull"

## 14. Return to Me!

```
def sumItUp: Int = {
    def one(x: Int): Int = { return x; 1 }
    val two = (x: Int) => { return x; 2 }
    1 + one(2) + two(3)
}
println(sumItUp)
```

- Scala does not complain about unreachable code
    +  use the compiler option -Ywarn-dead-code
- A return expression return e must occur inside the body of some enclosing named method or function
- For the first return x, the enclosing named method is method one
- for the second return x, the enclosing named method is method value
- When the function two(3) is called as part of the expression 1 + one(2) + two(3), then the result 3 is returned as the result of method value.

## 15. Count Me Now, Count Me Later 
## 16. One Bound, Two to Go 

## 17. Implicitly Surprising

Scala partially applied function help you move from specific to more general & reusable functionality

```scala
def add(x:int)(y:Int) = x+y
def add2 = add(2) _
```

how do this work with implicit?
```scala
implicit val z1 = 2
def addTo(n: Int) = {
   def add(x: Int)(y: Int)(implicit z: Int) = x + y + z
   add(n) _
}

implicit val z2 = 3
val addTo1 = addTo(1)

add(1)(2)(3)    // 6
addTo1(2)       // 5
addTo1(2)(3)    // Error
```

- What happended to method add's implicit parameter?
    + type signature of addTo1: `Int=> Int = <function1>`
        * It is function of one, not two parameters
        * implicit z is not parameter of eta-expanded function
    + Why choose z1 not z2
        * implicit decided when addTo is invoked
        * when z1 z2 both in scope, fail to compile

## 18. Information Overload 

Overloaded methods
```scala
def foo(n: Int, a: Any) {
    println(s"n: ${n}, a: ${a}") }

scala> foo(1, 2)
n: 1, a: 2

object A {
    def foo(n: Int, a: Any) {
        println(s"n: ${n}, a: ${a}") }
    def foo(a: Any, n: Int) {
        println(s"a: ${a}, n: ${n}") }
}
scala> A.foo(1, 2) 
Exception: AmbiguousReference
```

Prevent Ambiguous calls
```scala
object Oh {
    def overloadA(u: Unit) = "I accept a Unit"
    def overloadA(u: Unit, n: Nothing) =
        "I accept a Unit and Nothing"
    def overloadB(n: Unit) = "I accept a Unit"
    def overloadB(n: Nothing) = "I accept Nothing"
}
println(Oh overloadA 99)  // I accept a Unit
println(Oh overloadB 99)  // fail to compile
```

- Scala Overloading Resolution
    + if identifier(overloadA) refer to multiple members of class
    + Scala applies a two stage algorithmj
        * compiler compares shapes of member decalarations with invocation to see which choice looks right (number of parameters)
            - avoid potentially expensive type resolution
            - auto choose first, but how int 99 match Unit?
            - value conversion: `{99;()}`
        * shape based is not sufficient, find precisely one option
            - evaluate `val arg=99` resolved to an Int
            - value conversion not work, as it only applies if expected type of value is known (compiler don't know which method to call at this case)

## 19. What’s in a Name?

Named and default arguments
```scala
class SimpleAdder {
    def add(x: Int = 1, y: Int = 2): Int = x + y
}

class AdderWithBonus extends SimpleAdder {
    override def add(y: Int = 3, x: Int = 4): Int =
    super.add(x, y) + 10
}

val adder: SimpleAdder = new AdderWithBonus
adder add (y = 0)  // 13
adder add 0        // 14
```

- How scala handle named & default names 
    + compiler transform invocation with such arguments into regular call
    + define variable foreach parameter required
    + assign value of all providing arguments
    + invoke special default method for other parameters
- `adder add 0`
    + val x$1=0
    + val x$2 = adder.add$default$2
    + add(0,4)+10
- `adder(y=0)`
    + need to rearrange variables
    + method definition of type of value invoked
    + adder's type is simpleAdder (compiler type)
    + not AdderWithBonus (runtime type)
    + 0 is put based on argument position of SimpleAdder

## 20. Irregular Expressions 

- scala.util.matching.Regex class
    + findAllIn method returns a MatchIterator for all occurence
    + "l".r.findAllIn("I love Scala")

```scala
def traceIt[T <: Iterator[_]](it: T) = {
    println(s"TRACE: using iterator '${it}'")
    it
}
val msg = "I love Scala"
println("First match index: " + traceIt("a".r.findAllIn(msg)).start)
println("First match index: " + "a".r.findAllIn(msg).start)
```

- result
    + TRACE: using iterator 'nonempty iterator'
    + First match index: 9
    + the second throws a runtime exception.
- generating the string representation of a MatchIterator has side effect
    + backed by `java.util.regex.Matcher`
    + The explicit state of a matcher is initially undefined;
    + attempting to query any part of it before a successful match will cause an IllegalStateException to be thrown.
- before call start or end, you first need to initialize that matcher
    + toString implementation calls hasNext, which attempts to find a matcher and initialized the matcher

## 21. I Can Has Padding? 

Scala provides incremental stringbuilder that hides java.lang.StringBuilder

```scala
implicit class Padder(val sb: StringBuilder) extends AnyVal {
    def pad2(width: Int) = {
        1 to (width-sb.length) foreach { sb += '*' }
        sb
    }
}

// length == 14
val greeting = new StringBuilder("Hello, kitteh!")
println(greeting pad2 20) // Hello, kitteh!*

// length == 9
val farewell = new StringBuilder("U go now.")
println(farewell pad2 20) // StringIndexOutOfBoundsException
```

- implicit value class
    + transparently enriches StringBuilder with pad2 method
    + a pad2 20, a.pad2(20), a pad2(20)
- `1 to width-sb.length` is a Range, the foreach accept function as argument 
- `+=` is essentially an StringBuilder.append alias
    + `StringBuilder.append` return Stringbuilder it self
    + Stringbuilder as a function:
        * StringBuilder.apply(index:Int):Char
        * Equivalent to charAt, like sb(0)='H'
    + only append once, then get value one by one
        * when 20-length>length, arrayOutOfBounds

Correct Padding
```scala
def pad2(width: Int) = {
    1 to width-sb.length foreach { _ => sb += '*' }
    sb
}

// which translate to
for(_<-1 to width-sb.length) {sb+='*'}
// or use existing padTo
new StringBuilder("Hello, kitteh!").padTo(20).mkString

```

## 22. Cast Away 

- although all values in Scala are objects
- the basic types like Byte,Int are compiled to Java primitive when possible
- however no similiar translation exists between Java & Scala collections
    + use JavaConversions and JavaConverters object
    + JavaConverters is usually prefered: `javaMap.asScala`

```scala
import collection.JavaConverters._
def javaMap: java.util.Map[String, java.lang.Integer] = {
    val map =
        new java.util.HashMap[String, java.lang.Integer]()
    map.put("key", null)
    map
}

val scalaMap = javaMap.asScala
val scalaTypesMap =
    scalaMap.asInstanceOf[scala.collection.Map[String, Int]]

println(scalaTypesMap("key") == null) //true
println(scalaTypesMap("key") == 0)    //true
```

- How is it possible to equal null & 0 at the same time?
    + java.lang.Integer & Scala.Int are not quite the same
    + In Scala, Value of type Int should be never null, as extend AnyVal
    + Scala Collections cannot store Java primitive types directly
        * Collections are generic, generic classes are subject to type erasure
        * all Scala type including Int, are AnyVal
        * need to be boxed into AnyRef when stored in collection
- For the first ==
    + compared to AnyRef wrapper type, which is null
- For the second ==
    + compiler always try to carry out operations on primitive types when possible for performance reason, unbox the ref
    + `unboxToInt(scalaTypesMap.apply("key"))==0`
    + while unboxToInt is implemented as
        * `return i == null ? 0: ((java.lang.Integer)i).intValue();`
- If use scalaType instead of scalaTypesMap
    + retains original java key and value types
    + compiler box the literal, instead of unbox map value
    + `scala.Boolean.box(scalaMap.apply("key")).==(scala.Int.box(0))`

## 23. Adaptive Reasoning 

- Scala support passing argument
    + By-value: evaluate before passed to method, 
        * default
    + By-name: evaluate only when referenced inside methods
        * prefixed with => symbol
        * useful when evaluation is expensive
        * but is evaluated each time when referenced

```scala
def mod(a: => Double) = if (a >= 0) a else -a
mod({ println("evaluating"); 5.2})
// by-name: the {} is not evaluated until used

evaluating
evaluating
res0: Double = 5.2
```

- Scala allow you to omit parentheses when passing a block
    + look and feel of a built-in control structure
    + `List(1, 2, 3) foreach { e => println(math.abs(e)) }`

```scala
class Printer(prompter: => Unit) {
    def print(message: String, prompted: Boolean = false) {
        if (prompted) prompter
        println(message)
    }
}

def prompt() { print("puzzler$ ") }

new Printer { prompt } print (message = "Puzzled yet?")
new Printer { prompt } print (message = "Puzzled yet?", prompted = true)
```

- Output
    + puzzler$ Puzzled yet?
    + puzzler$ Puzzled yet?
- curly braces can be used in place of parenthese
    + ONLY in case of method arguments
    + constructor arguments always need parenthese
    + thus
        * new Printer(prompt) create new instance of Printer with prompt as structor argument
        * new Printer {prompt} initiate an anoymous subclass with a no-arg, primary constructor, it invoked as part of constructor
- But Printer has a class parameter prompter
    + if prompt is not passed, how does code even compile?
    + feature1: defaultly use a empty arguemnt list
    + feature2: argument adaption, compiler try to fix by add Unit ()
    + `new Printer(()) {prompt} print .....`

## 24. Double Trouble 

```scala
def printSorted(a: Array[Double]) {
    util.Sorting.stableSort(a)
    println(a.mkString(" "))
}
printSorted(Array(7.89, Double.NaN, 1.23, 4.56))
// 1.23 4.56 7.89 NaN

printSorted(Array(7.89, 1.23, Double.NaN, 4.56))
// 1.23 7.89 NaN  4.56
```

- Double.NaN
    + adhere to IEEE754 
    + == <= >= > < with NaN always result in false
    + != with NaN always result in true
- Ordering in Double is inconsistent
    + compare(1.0,Double.NaN) is negative
    + lt(1.0,Double.NaN) is also negative
- StableSort
    + first divide down the middle and recursive sort subarray
    + merge: take elem from first sub as long as lt second sub
    + divide to 7.89,1.23  Double.NaN,4.56
        * Ordering.lt(4.56,Double.NaN) is false
    + 1.23,7.89  Double.NaN,4.56
        * take 1.23, lt(Double.NaN,1.23)<0, lt(4.56,1.23)<0
        * 1.23 put in array
        * take 7.89, lt(Double.NaN,7.89)<0
        * put 7.89 in, as second array already sorted
- Solution
    + pass an explicit comparison function
    + like java.lang.Double.compareTo, Double.NaN greater than all other value
    + `def ltWithNaN(x:Double,y:Double)=(x compare y)<0`
    + util.Sorting.stableSort(a, ltWithNaN _)

## 25. Type Extortion 

Best way to extract Option is via getOrElse method

```
val zippedLists = (List(1,3,5),List(2,4,6)).zipped
val (x,y) = zippedLists.find(_._1>10).getOrElse(10)
println(s"Found $x")
```

- Throw a MatchError runtime exception
    + calling getOrElse on Option[A] does not necessarily return type A
    + `final def getOrElse[B>:A](default;=>B):B`
    + can return wider type than original
- Other methods with widening behavior
    - Option.orElse
    - Try.recover
    - Future.recover
- `zippedLists.find(_._1>10).getOrElse(10)` return `res0: Any = 10`
    + but Any cannot be assigned to val of Tuple2, why?
    + tuple interpret as pattern definition
    + `val a$ = 10:Any match {case(b,c)=>(b,c)}; val x=a$._1`
    + MatchError

Many method allow for fallback value can return wider type like (Any,Any), make sure you specify the expected return type

```scala
def howToPronounce(numAndName: Option[(Int, String)]) = {
    val (num, name) = numAndName.getOrElse(("eight", 8))
    println(s"The word for $num is '$name'")
}
scala> howToPronounce(Some((7, "seven")))
The word for 7 is 'seven'
scala> howToPronounce(None)
The word for eight is '8'

```

## 26. Accepts Any Args 

```scala
def prependIfLong(candidate: Any, elems: Any*): Seq[Any] = {
    if (candidate.toString.length > 1)
        candidate +: elems
    else
        elems
}
println(prependIfLong("I", "love", "Scala")(0)) 
// love

def prependIfLongRefac(candidate: Any)(elems: Any*):Seq[Any] = {
    if (candidate.toString.length > 1)
        candidate +: elems
    else
        elems
}
// invoked unchanged
println(prependIfLongRefac("I", "love", "Scala")(0)) 
// ArrayBuffer((I,love,Scala),0)
```

- refractor parameter list to curried parameters
    + the unchanged invocation still compiles successfully
- after refractoring, it expect single argment
    + could not find three argument version
    + package into a tuple
    + and second argument append after that

## 27. A Case of Strings 

```scala
def objFromJava: Object = "string"
def stringFromJava: String = null
def printLengthIfString(a: AnyRef): Unit = a match {
    case str: String =>
        println(s"String of length ${str.length}")
    case _ => println("Not a string")
}
printLengthIfString(objFromJava)    // String of length 6
printLengthIfString(stringFromJava) // Not a string
```

- Scala's Interoperability with java
    + unfortunately java method often return null
        + unclear whether uninitialized, non-existent or empty values
    + Scala Null is a subtype of all reference types
        + but not instance of any type other than Null
        + also Null does not exist in Java runtime
        + `n.isInstanceOf[Null]` Error
- pattern match on types
    + implemented via the isInstanceOf method on RUNTIME types
    + object matched but null not matched
    - if want to match null
        + HANDLE EXPLICITLY
        + `a match {case null => println("Go null!")}`
- Better use Options
    + `Option(objFromJava)   Option[Object]=Some(string)`
    + `Option(stringFromJava)  Option[String]=None`
    + transform NullPointerExceptions into compiler errors


## 28. Pick a Value, AnyValue! 

abstract & generic types

```scala
trait Recipe {
    type T <: AnyVal
    def sugarAmount: T
    def howMuchSugar() {
        println(s"Add ${sugarAmount} tablespoons of sugar")
    }
}

val approximateCake = new Recipe {
    type T = Int
    val sugarAmount = 5
}

scala> approximateCake.howMuchSugar()
Add 5 tablespoons of sugar

val gourmetCake = new Recipe {
    type T = Double
    val sugarAmount = 5.13124
}

scala> gourmetCake.howMuchSugar()
Add 5.13124 tablespoons of sugar
```


```scala
trait NutritionalInfo {
    type T<: AnyVal
    var value : T = _
}
val containSugar = new NutritionalInfo {type T = Boolean}
println(containSugar.value) //null
println(!containSugar.value)//true
```


- Any Value initialized to null
    + default value depends on type T as follows
    + 0,0L,0.0f,0.0d if numeric
    + false if Boolean
    + () if Unit
    + null for all other types
- when handle !case
    + calling `unary_!` , with `UnboxToBoolean` method
    + which return false when handling null

## 29. Implicit Kryptonite


## 30. Quite the Outspoken Type

## 31. A View to a Shill
```
val ints = Map(1 -> List(1, 2, 3, 4, 5))
val bits = ints map { case (k, v) => (k, v.toIterator) }
val nits = ints mapValues (_.toIterator)

"%d%d".format(bits(1).next, bits(1).next)  // 12
"%d%d".format(nits(1).next, nits(1).next)  // 11
```

- mapValues returns a map view which maps every key of this map to f(key)
- the resulting map wraps the original map without copying any elements
- each retrieval from wrapped map
    + result in a new evaluation from mapping function
    + return a new iterator


## 32. Set the Record Straight

```scala
val numbers = List("1", "2").toSet() + "3"
println(numbers) // false3
```

- toSet method is parameterless
    + adding parenthese means calling apply method
    + but no arguments, interpret as ()
- `(List("1","2").toSet[Any] apply ()) + "3"`
    + Unit is not string, but have common supertype Any
    + not found (), return false


## 33. The Devil Is in the Defaults 

In many languages, assigning default values to entries in a map involves tedious boilerplate code
```
import collection.mutable
val accBalances = mutable.Map[String, Int]()

def getBalance(accHolder:String):Int={
    if(!(accBalance isDefinedAt accHolder)){
        accBalances += (accHolder -> accHolder.length)
    }
    accBalances(accHolder)
}

scala>println(getBalance("Alice"))
```

In scala you can eliminate this clutter
```
import collection.immutable
val accBalances = immutable.Map[String,Int]() withDefualt{
    newCustomer => newCustomer.length
}
// or
val accBalances = immutable.Map[String,Int]() withDefualtValue 10
```

Example

```scala
import collection.mutable
import collection.mutable.Buffer

val accBalances: mutable.Map[String, Int] =
    mutable.Map() withDefaultValue 100

def transaction(accHolder: String, amount: Int,
        accounts: mutable.Map[String, Int]) {
    accounts += accHolder -> (accounts(accHolder) + amount)
}

val accBalancesWithHist: mutable.Map[String, Buffer[Int]] =
    mutable.Map() withDefaultValue Buffer(100)

def transactionWithHist(accHolder: String, amount: Int,
        accounts: mutable.Map[String, Buffer[Int]]) {
    val newAmount = accounts(accHolder).head + amount
    accounts += accHolder ->(newAmount +=: accounts(accHolder))
}

transaction("Alice", 100,accBalances)
transactionWithHist("Dave", 100, accBalancesWithHist)
println(accBalances("Alice")) // 0
println(accBalances("Bob"))   // 100
println(accBalancesWithHist("Carol").head) // 0
println(accBalancesWithHist("Dave").head)  // 0
```

- Why
    + `accBalancesWithHist("Carol") eq accBalancesWithHist("Dave")`
    + true
    + all share the same account
- the default value do not act as factory, just same value
    + should always use withDefault
    + `withDefault {_=>Buffer(100)}`

## 34. The Main Thing

Scala put initialization statements directly in a class, trait, object body instead of define primary constructor

For cmd program, simply extends App trait, the body of object become content of the main method, and is executed when program is run

```
object HelloWorld extends App {
    println("Hello World!")
}
```

TBC

## 35. A Listful of Dollars
```scala
type Dollar = Int
final val Dollar: Dollar = 1
val a: List[Dollar] = List(1, 2, 3)

println(a map { a: Int => Dollar })  // List(1,1,1)
println(a.map(a: Int => Dollar))     // IndexOutOfBoundException
```

- Usually curly braces & parentheses are interchangable
    + a.map(...)
    + a map {...}
- Actually parsed differently
    + Curly braces delimit a Block, ResultExpr, `a:Int=>Dollar`
        * ResultExpr may have form arg:type=>expr
        * parsed as (a:Int)=>Dollar
    + Parentheses parsed as Expr
        * require type of any argument enclosed in parenthese
        * parsed as a:(Int=>Dollar)
        * it is possible as List[Dollar] also a function from Int=>Dollar
        * 1,2,3 => a(1) a(2) a(3)
        * a(3) have out of bounds error
