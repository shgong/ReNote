
The compiler knows more information about types than the JVM runtime can easily represent. A Manifest is a way for the compiler to send an inter-dimensional message to the code at runtime about the type information that was lost.

> This is similar to how the Kleptonians have left encoded messages in fossil records and the "junk" DNA of humans. Due to limitations of lightspeed and gravitational resonance fields, they are unable to communicate directly. But, if you know how to tune into their signal, you can benefit in ways you cannot imagine, from deciding what to eat for lunch or which lotto number to play.

It isn't clear if a Manifest would benefit the errors you are seeing without knowing more detail.

## static type of a collection

One common use of Manifests is to have your code behave differently based on the static type of a collection. For example, what if you wanted to treat a List[String] differently from other types of a List:

```scala
 def foo[T](x: List[T])(implicit m: Manifest[T]) = {
    if (m <:< manifest[String])
      println("Hey, this list is full of strings")
    else
      println("Non-stringy list")
  }

  foo(List("one", "two")) // Hey, this list is full of strings
  foo(List(1, 2)) // Non-stringy list
  foo(List("one", 2)) // Non-stringy list
```

A reflection-based solution to this would probably involve inspecting each element of the list.

## using context bounds

A context bound seems most suited to using type-classes in scala, and is well explained here by Debasish Ghosh: http://debasishg.blogspot.com/2010/06/scala-implicits-type-classes-here-i.html

Context bounds can also just make the method signatures more readable. For example, the above function could be re-written using context bounds like so:

```scala
  def foo[T: Manifest](x: List[T]) = {
    if (manifest[T] <:< manifest[String])
      println("Hey, this list is full of strings")
    else
      println("Non-stringy list")
  }
```
