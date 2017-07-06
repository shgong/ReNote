SamConversion.md

# LAMBDA SYNTAX FOR SAM TYPES
The Scala 2.12 type checker accepts a function literal as a valid expression for any Single Abstract Method (SAM) type, in addition to the FunctionN types from standard library. This improves the experience of using libraries written for Java 8 from Scala code. Here is a REPL example using java.lang.Runnable:

```scala
scala> val r: Runnable = () => println("Run!")
r: Runnable = $$Lambda$1073/754978432@7cf283e1
scala> r.run()
Run!
```

Note that only lambda expressions are converted to SAM type instances, not arbitrary expressions of FunctionN type:

```scala
scala> val f = () => println("Faster!")
scala> val fasterRunnable: Runnable = f
<console>:12: error: type mismatch;
 found   : () => Unit
 required: Runnable
```


# requirements for SAM conversion


With the use of default methods, Scala’s built-in FunctionN traits are compiled to SAM interfaces. This allows creating Scala functions from Java using Java’s own lambda syntax:
```scala
public class A {
  scala.Function1<String, String> f = s -> s.trim();
}
```


# SAM conversion precedes implicits
The SAM conversion built into the type system takes priority over implicit conversion of function types to SAM types. This can change the semantics of existing code relying on implicit conversion to SAM types:

```scala
trait MySam { def i(): Int }
implicit def convert(fun: () => Int): MySam = new MySam { def i() = 1 }
val sam1: MySam = () => 2 // Uses SAM conversion, not the implicit
sam1.i()                  // Returns 2
```

To retain the old behavior, your choices are:

- compile under -Xsource:2.11
- use an explicit call to the conversion method
- disqualify the type from being a SAM (e.g. by adding a second abstract method).

Note that SAM conversion only applies to lambda expressions, not to arbitrary expressions with Scala FunctionN types:
```scala
val fun = () => 2     // Type Function0[Int]
val sam2: MySam = fun // Uses implicit conversion
sam2.i()              // Returns 1
```


SAM conversion in overloading resolution

In order to improve source compatibility, overloading resolution has been adapted to prefer methods with Function-typed arguments over methods with parameters of SAM types. The following example is identical in Scala 2.11 and 2.12:
```scala

scala> object T {
     |   def m(f: () => Unit) = 0
     |   def m(r: Runnable) = 1
     | }
scala> val f = () => ()
scala> T.m(f)
res0: Int = 0

```


While the adjustment to overloading resolution improves compatibility overall, code does exist that compiles in 2.11 but is ambiguous in 2.12, for example:

```scala
scala> object T {
     |   def m(f: () => Unit, o: Object) = 0
     |   def m(r: Runnable, s: String) = 1
     | }
defined object T
scala> T.m(() => (), "")
<console>:13: error: ambiguous reference to overloaded definition
```
