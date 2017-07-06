# SyntaxSugars.md


## equivalents

`a b` is equivalent to `a.b`

`a b c` is equivalent to `a.b(c)`, except when b ends in `:`  
In that case, a b c is equivalent to c.b(a)

`a(b)` is equivalent to `a.apply(b)` 
This is why the following definitions for an anonymous functions are identical: 

```scala
val square1 = (x: Int) => x * x 
val square2 = new Function1[Int,Int] { 
    def apply(x: Int) = x * x 
}
```

When calling square1(y), you are actually calling `square1.apply(y)` which square1 must have as specified by the Function1 trait (or Function2, etc...)

`a(b) = c` is equivalent to `a.update(b,c)` 
Likewise, `a(b,c) = d` is equivalent to `a.update(b,c,d)` and so on.

`a.b = c` is equivalent to `a.b_=(c)` 
When you create a val/var x in a Class/Object, Scala creates the methods x and x_= for you. 
You can define these yourself, but if you define `y_=` you must define `y` or it will not compile

```scala
scala> val b = new Object{ def set_=(a: Int) = println(a) }
b: java.lang.Object{def set_=(Int): Unit} = $anon$1@17e4cec

scala> b.set = 5
<console>:6: error: value set is not a member of java.lang.Object{def set_=(Int): Unit}
       b.set = 5
         ^

scala> val c = new Object{ def set = 0 ; def set_=(a:Int) = println(a) }
c: java.lang.Object{def set: Int; def set_=(Int): Unit} = $anon$1@95a253

scala> c.set = 5
5
```

`-a` corresponds to `a.unary_-` Likewise for `+a,~a,!a`

`a <operator>= b`, where `<operator>` is some set of special characters, is equivalent to `a = a <operator> b` only if a doesn't have the `<operator>=` method

```scala
class test(val x:Int) {
    def %%(y: Int) = new test(x*y)
}

var a = new test(10)
a.x // 10
a %%= 5 //Equivalent to a = a %% 5
a.x // 50
```


## right associate

Why does this work:

    def includeIn_:[P <: Piece](piece: P): Unit = ...

but this doesn't:

    def includeIn:[P <: Piece](piece: P): Unit = ...

There is a general rule that mixing alpha/non-alpha characters be separated by underscore. This is a different issue to associativity.


And the reason why there needs to be an underscore between the alphanumeric and the "operator" part in a method name is because you wouldn't know whether

```scala
unary!
//means
unary.!
//or
this.unary!
```

Thus, foo! as a method name is illegal, it needs to be called `foo_!`.


## tuples and symbols get a slightly special syntax.

```scala
val sym1 = 'x
val sym2 = Symbol("x")

val tuple1 = ("Hello",1)
val tuple2 = Tuple2[String,Int]("Hello",1)
```


## type F[A,B] can be used as A F B.

```scala
type ->[A,B] = (A,B)
def foo(f: String -> String)

val map: String Map Int = Map("age"->100)
```

## Using => type in a method definition makes the compiler wrap expressions inside the method call in a function thunk.

```scala
def until(cond: => Boolean)(body: => Unit) = while(!cond) body

var a = 0
until (a > 5) {a += 1}
```
