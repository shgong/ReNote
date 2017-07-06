# Prefix,Postfix,Infix&Unary

```scala
// prefix, postfix and infix

def add(a: Int, b: Int) = a + b
val addv = (a: Int, b: Int) => a + b
def double(a: Int) = a * 2
val doublev = (a: Int) => a * 2

implicit class IntegerWrapper(var a: Int) {
  def add(b: Int) = a + b
  def double = a * 2
  def unary_! = a * 3
  def $$_:(b: Double): Int = a + b.toInt
  def --:(b: Double): Int = a + b.toInt
  def &(b: Int) = a * b + 1
  def apply(b: Int) = a * b
  def update(b: Boolean, c: Int): Int = {
    if (b) a += c;  a
  }
}

// prefix operator
add(5, 6)
addv(5, 6)
double(5)

// infix operator
5.add(6)
5 add 6 add 7

// inverse infix oeprator
Array(2) :+ 1
1 +: Array(2)

// right associated naming
10.--:(100.0)
100.0 --: 10
100.0 $$_: 10

// postfix operator
import language.postfixOps
5 double

// unary operator & syntax sugars
val r1 = !5
val r2 = 5 (10)
val r3 = 17 (8)
val r4 = 17 (true) = 25
val r5 = 17 (false) = 6

// implicit conversion timing
var g = 17
g(true) = 25
g(false) = 6
g.a
g & 2
g.a
g &= 2
g.a

var g2 = IntegerWrapper(17)
g2(true) = 25
g2(false) = 6
g2.a
g2 & 2
g2.a
g2 &= 2
g2.a

```


### BTW

IntelliJ will pump out warning `Name Boolean Parameter`

it recommend `function(isTre=true)` instead of `function(true)` 

IDEA's Scala plugin has always been lacking quality compared to other products of Jetbrains. The company develops a direct competitor language to Scala named Kotlin, so I think it's simply an issue of their priorities. In a couple of years of posting dozens of bugs related to Scala plugin on their tracker I've grown accustomed not to trust anything that this plugin says or does.

Your case is an example of how it tries to be too smart. You've given a good enough reason to report this on their bugtracker, and, of course, not to follow that ridiculous advice.


## Explaination about syntax sugars

The unary_ prefix for unary prefix operators is a bit misleading: it's more about the prefix part than the unary part. You need some way to distinguish
```scala
!foo // unary prefix !
foo! // unary postfix !
```

Remember: Scala doesn't actually have operators. There are two ways to call a method, either with a . or with whitespace:
```
foo.bar(1, "two")
foo bar(1, "two")
```

And when you have a single argument, you can leave off the parentheses:
```
foo plus(1)
foo plus 1
```

Lastly, (almost) any character is legal in an identifier:
```
foo plus 1
foo + 1
```

Now it looks like Scala has a binary infix + operator, but it actually doesn't. It's just a normal method called with normal method calling syntax.

**What I said above isn't fully true, however.**  If Scala didn't have support for operators and it all was just normal method calling, then `2 + 3 * 4` would evaluate to 20 (like it does in Smalltalk, Self and Newspeak for example) instead of 14. 

So, there is a little bit of support for operators in Scala (two little bits, actually). When a method is called with whitespace (so-called "operator syntax") instead of the ., and that method starts with an operator character, then Scala will respect operator precedence. 

And the other little bit of operator support is that there are some operators that you would like to have, but that cannot be easily expressed as a method call. It works fine for binary infix operators and unary postfix operators:

```scala
foo op bar // same as:
foo.op(bar)

foo op     // same as:
foo.op
```

But not for prefix or "around-fix" operators:

```scala
!foo
foo(bar)
```

So, there are a couple of special syntactic sugar translation rules:

```scala
!foo
foo.unary_!
// same for +, - and ~

foo(bar)
foo.apply(bar)

foo(bar) = 1
foo.update(bar, 1)

foo += 1
foo.+=(1) // but if this doesn't compile, then the compiler will also try
foo = foo.+(1)
```

And the reason why there needs to be an underscore between the alphanumeric and the "operator" part in a method name is because you wouldn't know whether

```scala
unary!
//means
unary.!
//or
this.unary!
```

Thus, foo! as a method name is illegal, it needs to be called `foo_!`.