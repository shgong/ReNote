### What is the difference between a var, a val and def?
### What is the difference between a trait and an abstract class?
### What is the difference between an object and a class?
### What is a case class?
### What is the difference between a Java future and a Scala future?
### What is the difference between unapply and apply, when would you use them?
### What is a companion object?
### What is the difference between the following terms and types in Scala: Nil, Null, None, Nothing?
### What is Unit?
### What is the difference between a call-by-value and call-by-name parameter?
### How does Scala's Stream trait levarages call-by-name?
### Define uses for the Option monad and good practices it provides.
### How does yield work?
### Explain the implicit parameter precedence.
### What operations is a for comprehension syntactic sugar for?
### Streams:
### What consideration you need to have when you use Scala's Streams?
### What technique does the Scala's Streams use internally?

- What's the difference between val and var
    + val: declare an immutable variable
    + var: declare a  mutable variable

- What is Object
    + a singleton class
- What is a companion object
    + a object that share the same name with a class
    + they can see private methods from each other

- What is the advantage of case class
    + can use Pattern match (have unapply keyword)
    + Easy to instantiate (don't need new keyword)
    + Serialized (extends Serializable)
    + Have built-in useful methods: toString,  hashCode, equals

- Is function parameter call by value or call by name in Scala by default?
    + Call by value 
- How to declare a call by name parameter
    + Add a right arrow in its type signature 
    + like `a:Int` (call by value) and `a:=>Int` (call by name) 

- What is None, Null, Nil and Nothing
    + None: Option's subclass, used for avoid NullPointerException / error handling
    + Null: a trait(Null) or a instance(null) for a unset reference
    + Nil: an empty List
    + Nothing: subtype of every other type

- What method should a class have if you want to use it in a for comprehension
    + flatMap, map, foreach, filter (withFilter)