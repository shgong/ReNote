# Abstract Type Members versus Generic Type Parameters in Scala

A common question asked by people learning Scala is how to decide between abstract type members and generic type parameters when you need to model an abstract type. 

type parameters in Scala are similar to type parameters in Java, except instead of Java's angle brackets, Scala uses square brackets:
```scala
// Type parameter version
trait Collection[T] {
  // ...
}
```

Abstract type members in Scala have no equivalent in Java. 
In Scala, a class or trait can also have type members, and just as methods can be abstract in Java, methods fields, and types can all be abstract in Scala.

```scala
// Type member version
trait Collection {
  type T
  // ...
}
```

In both cases, the abstract type can be made concrete in a subtype. 

```scala
// Type parameter version
trait StringCollection extends Collection[String] {
  // ...
}

// Type member version
trait StringCollection extends Collection {
  type T = String
  // ...
}
```

So when would you choose one over the other? I asked Martin Odersky this question in an interview. 

He first explained that one reason for having both abstract type members and generic type parameters is orthogonality:

There have always been two notions of abstraction: parameterization and abstract members. In Java you also have both, but it depends on what you are abstracting over. In Java you have abstract methods, but you can't pass a method as a parameter. You don't have abstract fields, but you can pass a value as a parameter. And similarly you don't have abstract type members, but you can specify a type as a parameter. So in Java you also have all three of these, but there's a distinction about what abstraction principle you can use for what kinds of things. And you could argue that this distinction is fairly arbitrary.

What we did in Scala was try to be more complete and orthogonal. We decided to have the same construction principles for all three sorts of members. So you can have abstract fields as well as value parameters. You can pass methods (or "functions") as parameters, or you can abstract over them. You can specify types as parameters, or you can abstract over them. And what we get conceptually is that we can model one in terms of the other. At least in principle, we can express every sort of parameterization as a form of object-oriented abstraction. So in a sense you could say Scala is a more orthogonal and complete language.

He also described a difference between abstract type members and generic type parameters that can show up in practice:

But in practice, when you [use type parameterization] with many different things, it leads to an explosion of parameters, and usually, what's more, in bounds of parameters. At the 1998 ECOOP, Kim Bruce, Phil Wadler, and I had a paper where we showed that as you increase the number of things you don't know, the typical program will grow quadratically. So there are very good reasons not to do parameters, but to have these abstract members, because they don't give you this quadratic blow up.

When he gave this answer, I wasn't sure I understood exactly what he was talking about, but I think I can now offer some more insight into this difference. I have tended to use generic type parameters by default, perhaps because I come from a C++ and Java background and are more familiar with type parameterization than type members. However, I have encountered one design problem that I ended up solving with abstract type members, not generic type parameters.

The problem was that I wanted to provide traits in ScalaTest that allow users to write tests into which they can pass fixture objects. This would give people the option of writing tests in a functional style instead of the traditional imperative style of JUnit's setUp and tearDown methods. To provide this option, I needed to allow users to indicate the type of the fixture object by either specifying a concrete type parameter or member. In other words, I'd either provide this trait in the ScalaTest API:

```scala
// Type parameter version
trait FixtureSuite[F] {
  // ...
}

// Type member version
trait FixtureSuite {
  type F
  // ...
}
```

In either case, F would be the type of the fixture parameter to pass into the tests, which suite subclasses would make concrete. 

```scala
// Type parameter version
class MySuite extends FixtureSuite[StringBuilder] {
  // ...
}

// Type member version
class MySuite extends FixtureSuite {
  type F = StringBuilder
  // ...
}
```

So far there's not much difference. However, one other use case I had is that I wanted to allow people to create traits that provide a concrete definition for the fixture type and could be mixed into suite classes. This would allow users to encode commonly used fixtures into helper traits that could be mixed into any of their suite classes that need them. This is where a difference showed up. Here's how you'd write that trait using the generic type parameter approach:

```scala
// Type parameter version
trait StringBuilderFixture { this: FixtureSuite[StringBuilder] =>
  // ...
}
```

The "this: FixtureSuite[StringBuilder]" syntax is a self type, which indicates that trait StringBuilderFixture can only be mixed into a FixtureSuite[StringBuilder]. By contrast, here's what this trait would look like when taking the type member approach:

```scala
// Type member version
trait StringBuilderFixture { this: FixtureSuite =>
  type F = StringBuilder
  // ...
}
```

So far there's still only cosmetic differences, but the pain point of the type member approach is about to arise. As soon as a user tries to mix the StringBuilderFixture into a suite class, you'll see a usability difference. 

In the type parameter approach, the user must repeat the type parameter even though it is defined in the trait, But in the abstract type member approach, no such repetition is necessary


```scala
// Type parameter version
class MySuite extends FixtureSuite[StringBuilder] with StringBuilderFixture {
  // ...
}

// Type member version
class MySuite extends FixtureSuite with StringBuilderFixture {
  // ...
}
```

Because of this difference, I chose to model the fixture parameter type as an abstract type member, not a generic type parameter, in ScalaTest. I named the type member FixtureParam, to make it more obvious to readers of the code what the type is used for. (This feature is in ScalaTest 1.0. For the details, check out the scaladoc documentation of trait FixtureSuite.) In a future release of ScalaTest, I plan to add new traits that allow multiple fixture objects to be passed into tests. For example, if you want to pass three different fixture objects into tests, you'll be able to do so, but you'll need to specify three types, one for each parameter. Thus had I taken the type parameter approach, your suite classes could have ended up looking like this, 
Whereas with the type member approach it will look like this:


```scala
// Type parameter version
class MySuite extends FixtureSuite3[StringBuilder, ListBuffer, Stack] with MyHandyFixture {
  // ...
}

// Type member version
class MySuite extends FixtureSuite3 with MyHandyFixture {
  // ...
}
```

One other minor difference between abstract type members and generic type parameters is that when a generic type parameter is specified, readers of the code do not see the name of the type parameter. Thus were someone to see this line of code:

```
// Type parameter version
class MySuite extends FixtureSuite[StringBuilder] with StringBuilderFixture {
  // ...
}
```

They wouldn't know what the name of the type parameter specified as StringBuilder was without looking it up. Whereas the name of the type parameter is right there in the code in the abstract type member approach:

```
// Type member version
class MySuite extends FixtureSuite with StringBuilderFixture {
  type FixtureParam = StringBuilder
  // ...
}
```

In the latter case, readers of the code could see that StringBuilder is the "fixture parameter" type. They still would need to figure out what "fixture parameter" meant, but they could at least get the name of the type without looking in the documentation. I think there will be design situations where the explicit type member name will be better, and situations where it is worse. I think having the FixtureParam name explicit in ScalaTest FixtureSuites helps the readability of the code. But I wouldn't want to have to do that for collections. I like saying new ListBuffer[String], and am glad I don't have to say new ListBuffer { type E = String }.

My observation so far about abstract type members is that they are primarily a better choice than generic type parameters when you want to let people mix in definitions of those types via traits. You may also want to consider using them when you think the explicit mention of the type member name when it is being defined will help code readability.

What do you think of this take on abstract type members in Scala? Do you have any other insights about the abstract-type-member-versus-generic-type-parameter question to share?

