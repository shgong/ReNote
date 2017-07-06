# Scala Basics

### Scala Interactive Interpreter

```scala
1+2

res0*3

println("Hello, world!")

val msg = "Hello, world!"
val msg2: java.lang.String = "Hello again, world!" // specify
val msg3: String = "Hello yet again, world!" // equivalent notation
println(msg)

msg = "Goodbye cruel world!!" // cannot reassign val


var greeting = "Hello, world!"
greeting = "Leave me alone, world!"   // can assign var


def max(x: Int, y: Int): Int = {
        if (x > y) x
        else y
    }
max(10,15) // functions

def greet() = println("Hello, world!") // no parameter

```


### Basic Logics

// while loop
```scala
var i = 0
while (i < args.length) {
  println(args(i))
  i += 1
}
// must put in (),  if i < 10 NOT ALLOWED
```

// foreach
```scala
args.foreach(arg => println(arg))
args.foreach((arg: String) => println(arg)) //same
```

// for expresion
```scala
  for (arg <- args)
    println(arg)
```

bash
`scala test.scala Scala is fun`


### Array & Parameterizing
Use value & type to parameterizing object
Array use () instead of []

```scala
// with value
val big = new java.math.BigInteger("12345")

// with type (string)
val greetStrings = new Array[String](3)
// or more explicit
val greetStrings: Array[String] = new Array[String](3)

greetStrings(0) = "Hello"
greetStrings(1) = ", "
greetStrings(2) = "world!\n"

for (i <- 0 to 2)
    print(greetStrings(i))

// more concise
val numNames = Array("zero", "one", "two")
```


Why use () to access array

- compiler transform to a update method
- greet(0)="Hello" → greet.update(0,"Hello")

```scala
greetStrings.update(0, "Hello")
greetStrings.update(1, ", ")
greetStrings.update(2, "world!\n")

for (i <- 0.to(2)) print(greetStrings.apply(i))

// more concise
val num = Array.apply("zero", "one", "two")
```

- Achieves a conceptual simplicity by treating everything, from arrays to expressions, as objects with methods. 
- Don't have to remember special cases, such as primitive & wrapper in java
- Don't incur a significant performance cost. Scala compiler uses Java arrays, primitive types, and native arithmetic wherever possible 



### Tuples
Like lists, tuples are immutable
Unlike lists, tuples can contain different types of elements
Tuple is 1-based as tradition in Haskell and ML
Actual type is flexible, like `Tuple2[Int, String]`

```scala
    val pair = (99, "Luftballons")
    println(pair._1)
    println(pair._2)
```


### Set
Also provides mutable and immutable alternatives, but in a different way.

Scala has base trait for set, with 2 subtrait, mutable & immutable


##### Default: Immutable
```scala
    var jetSet = Set("Boeing", "Airbus")
    jetSet += "Lear"
    println(jetSet.contains("Cessna"))
```
immutable, += method will return a new set, so cannot use val


##### Mutable
```scala
    import scala.collection.mutable.Set

    val movieSet = Set("Hitch", "Poltergeist")
    movieSet += "Shrek"
    println(movieSet) 
```
mutable, movieSet itself do not change, so can use val

##### Implementation

Set is a trait (abstract), scala choose a concrete implement based on number of elements 

Number of elements  | Implementation
----|----
0                   | scala.collection.immutable.EmptySet
1                   | scala.collection.immutable.Set1
2                   | scala.collection.immutable.Set2
3                   | scala.collection.immutable.Set3
4                   | scala.collection.immutable.Set4
5 or more           | scala.collection.immutable.HashSet


You can also choose a concrete realization yourself using:

```scala
import scala.collection.immutable.HashSet

val hashSet = HashSet("Tomatoes", "Chilies")
println(hashSet + "Coriander")
```


### Map

##### default: immutable map

```scala
val romanNumeral = Map(
  1 -> "I", 2 -> "II", 3 -> "III", 4 -> "IV", 5 -> "V"
)
println(romanNumeral(4))
```

##### mutable map

```scala
import scala.collection.mutable.Map

val treasureMap = Map[Int, String]()
treasureMap += (1 -> "Go to island.")
treasureMap += (2 -> "Find big X on ground.")
treasureMap += (3 -> "Dig.")
println(treasureMap(2))
```

### Recognize Functional Style

One telltale sign is that if code contains any vars, it is probably in an imperative style. If the code contains no vars at all—i.e., it contains only vals—it is probably in a functional style. One way to move towards a functional style, therefore, is to try to program without vars.

```scala
def printArgs(args: Array[String]): Unit = {
  var i = 0
  while (i < args.length) {
    println(args(i))
    i += 1
  }
}

def printArgs(args: Array[String]): Unit = {
  for (arg <- args)
    println(arg)
}

def printArgs(args: Array[String]): Unit = {
  args.foreach(println)
}

def formatArgs(args: Array[String]) = args.mkString("\n")
  val res = formatArgs(Array("zero", "one", "two"))
  assert(res == "zero\none\ntwo")
```

### Readlines from file

```scala
  import scala.io.Source
  
    if (args.length > 0) {
  
      for (line <- Source.fromFile(args(0)).getLines)
        print(line.length +" "+ line + "\n")
    }
    else
      Console.err.println("Please enter filename")

```

line width formatting

```scala
    import scala.io.Source

    def widthOfLength(s: String) = s.length.toString.length

    if (args.length > 0) {

      val lines = Source.fromFile(args(0)).getLines.toList

      val longestLine = lines.reduceLeft(
        (a, b) => if (a.length > b.length) a else b
      )

      // "reduceLeft" applies the function to the first two elements
      // then result and the next, and so on all the way through the list.
      // return the result of the last application of the function

      val maxWidth = widthOfLength(longestLine)

      for (line <- lines) {
        val numSpaces = maxWidth - widthOfLength(line)
        val padding = " " * numSpaces
        println(padding + line.length +" | "+ line)
        // println: print + \n
      }
    }
    else
      Console.err.println("Please enter filename")


```


