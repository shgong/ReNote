# Quasiquotes

## 1. Introduction

Quasiquotes are a neat notation lets you manipulate Scala syntax tree.

```scala
scala> val tree = q"i am { a quasiquote }"
tree: universe.Tree = i.am(a.quasiquote)

// support pattern match
scala> tree match { case q"i am { a quasiquote }" => "it worked!" }
"it worked"

// match equivalent structure
scala> q"foo + bar" equalsStructure q"foo.+(bar)"
true

// unquoting
scala> val q"i am $what" = q"i am {a quasiquote}"
what:univerese.Tree  = a.quasiquote
```

### 1.1 Interpolators
```scala
scala> val x = q"""
         val x: List[Int] = List(1, 2) match {
           case List(a, b) => List(a + b)
         }
       """
x: universe.ValDef =
val x: List[Int] = List(1, 2) match {
  case List((a @ _), (b @ _)) => List(a.$plus(b))
}
```

Scala is a language with rich syntax that differs greatly depending on the syntactical context:

- List(1,2), List(a+b) are expressions, covered by `q`
- List[Int] is a type, covered by `tq`
- List(a,b) is a pattern, covered by `pq`
- also
    + `cq` for case clauses
    + `fq` for loop enumerator


### 1.2 Splicing
Unquote a variable number of elements
```scala
scala> val ab = List(q"a", q"b")
scala> val fab = q"f(..$ab)"
fab: universe.Tree = f(a,b)
```

dots are called splicing rank
- `..$` expects Iterable[Tree]
- `...$` expects Iterable[Iterable[Tree]]

```scala
scala> val c = q"c"
scala> val fabcab = q"f(..$ab, $c, ..$ab)"
fabcab: universe.Tree = f(a,b,c,a,b)

scala> val argss = List(ab, List(c))
scala> val fargss = q"f(...$argss)"
fargss: universe.Tree = f(a,b)(c)
```

can also used to tear tree apart
```scala
scala> val q"f(...$argss)" = q"f(a,b)(c)"
argss: List[List[universe.Tree]] = List(List(a,b), List(c))
```


## 2. Lifting

Lifting: extensible way to unquote custom data types in quasiquotes
Support unquoting of literal values & reflection primitives as trees

```scala
scala> val two =  1+ 1
two: Int = 2

scala> val four = q"$two + $two"
four: universe.Treee = 2.$plus(2)

// it works because Int is Liftable
trait Liftable[T] {
    def apply(value:T): Tree
}
```

you can unquote T in quasiquotes 
- whenever implicit value of Liftable[T] available

combine lifting & unquote splicing
```scala
scala> val ints = List(1,2,3)
scala> val f123 = q"f(..$ints)"
f123: universe.Tree = f(1,2,3)

scala> val intss = List(List(1,2,3), List(4,5), List(6))
scala> val f123456 = q"f(...$intss)"
f123456: universe.Tree = f(1,2,3)(4,5)(6)
```

### Bring your own

```scala
package points
import scala.universe._
case class Point(x: Int, y: Int)
object Point {
  implicit val lift = Liftable[Point] { p =>
    q"_root_.points.Point(${p.x}, ${p.y})"
  }
}
```

This way whenever a value of Point type is unquoted in runtime quasiquote it will be automatically transformed into a case class constructor call. 

## 3. Unlifting
```scala
trait Unliftable[T] {
  def unapply(tree: Tree): Option[T]
}
```

Due to the fact that tree might not be a represention of our data type, the return type of unapply is Option[T] rather than just T. Such signature also makes it easy to use Unliftable instances as extractors.

```scala
scala> val q"${left: Int} + ${right: Int}" = q"2 + 2"
left: Int = 2
right: Int = 2
scala> left + right
res4: Int = 4
```

unlifting will not be performed where Name, TermName, Modified extracted by default
```
scala> val q"foo.${bar: Int}" = q"foo.bar"
<console>:29: error: pattern type is incompatible with expected type;
 found   : Int
 required: universe.NameApi
       val q"foo.${bar: Int}" = q"foo.bar"
                        ^
```

combine with splitting
```scala
scala> val q"f(..${ints: List[Int]})" = q"f(1, 2, 3)"
ints: List[Int] = List(1, 2, 3)

scala> val q"f(...${intss: List[List[Int]]})" = q"f(1, 2, 3)(4, 5)(6)"
intss: List[List[Int]] = List(List(1, 2, 3), List(4, 5), List(6))
```

### Bring your own
```scala
package Points
import scala.universe._
case class Point(x: Int, y: Int)
object Point {
  implicit val unliftPoint = Unliftable[points.Point] {
    case q"_root_.points.Point(${x: Int}, ${y: Int})" => Point(x, y)
  }
}
```

## 4. Hygiene
A code generator is called hygienic if it ensures absence of name clashes between regular and generated code, preventing accidental capture of identifiers. As numerous experience reports show, hygiene is of great importance to code generation, because name binding problems are often very non-obvious and lack of hygiene might manifest itself in subtle ways.

In Scala we don’t have automatic hygiene yet - both of our codegen facilities (compile-time codegen with macros and runtime codegen with toolboxes) require programmers to handle hygiene manually. 

### Referential transparency

Issue1: when import `collection.mutable.Map`, `Map` is not resolved

Issue2: 
```scala
object MyMacro {
  def wrapper(x: Int) = { println(s"wrapped x = $x"); x }
  def apply(x: Int): Int = macro impl
  def impl(c: Context)(x: c.Tree) = {
    import c.universe._
    q"wrapper($x)"
  }
}
// ---- Test.scala ----
package example
object Test extends App {
  def wrapper(x: Int) = x
  MyMacro(2)
}
```

MyMacro will be resolved as `wrapper(2)`, thus call Test.wrapper()
won't print as expected

To solve this
- inside `impl`
- full qualify all references
    + `q"_root_.example.MyMacro.wrapper($x)"`
- unquote symbols
    + ` val myMacro = symbolOf[MyMacro.type].asClass.module`
    + ` val wrapper = myMacro.info.member(TermName("wrapper"))`
    + ` q"$wrapper($x)"`


### Hygiene in the narrow sense

```scala
scala> val originalTree = q"val x = 1; x"
originalTree: universe.Tree = ...

scala> toolbox.eval(originalTree)
res1: Any = 1

scala> val q"$originalDefn; $originalRef" = originalTree
originalDefn: universe.Tree = val x = 1
originalRef: universe.Tree = x

scala> val generatedTree = 
    q"$originalDefn; { val x = 2; println(x); $originalRef }"
generatedTree: universe.Tree = ...

scala> toolbox.eval(generatedTree)
2
res2: Any = 2
```

## 5. Use cases

### 5.1 AST manipulation in macros and compiler plugins

```scala
// macro that prints expression code before executing it
object debug {
    def apply[T](x: => T): T = macro impl
    def impl(c: Context)(x: c.Tree) = { import c.universe._
        val q"..$stats" = x    // cTree unsplicing
        val loggedStats = stats.flatMap { stat =>
            val msg = "executing " + showCode(stat)
            List(q"println($msg)", stat)
        }
        q"..$loggedStats"      // cTree splicing
    }
}

// usage
object Test extends App {
    def faulty:Int = throw new Exception
    debug {
        val x = 1
        val y = x + faulty
        x + y
    }
}

// output
executing val x: Int = 1
executing val y: Int = x.+(Test.this.faulty)
java.lang.Exception
```


### 5.2 Just In Time Compilation

ToolBox api, generate compile and run Scala code at runtime

```scala
scala> val code = q"""println("compiled and run at runtime!")"""
scala> val compiledCode = toolbox.compile(code)
scala> val result = compiledCode()
compiled and run at runtime!
result: Any = ()
```

### 5.3 Offline code generation

Thanks to new showCode pretty printer one can implement offline code generator that does AST manipulation with the help of quasiquotes and then serializes into actual source right before writing them to disk:

```scala
object OfflineCodeGen extends App {
  def generateCode() =
    q"package mypackage { class MyClass }"

  def saveToFile(path: String, code: Tree) = {
    val writer = new java.io.PrintWriter(path)
    try writer.write(showCode(code))
    finally writer.close()
  }
  
  saveToFile("myfile.scala", generateCode())
}
```
