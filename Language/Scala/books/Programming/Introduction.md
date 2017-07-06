# Programming in Scala

Scala = Scalable Language
- runs on the standard Java platform 
- interoperates seamlessly with all Java libraries.
- a blend of object-oriented and functional programming concepts in a statically typed language
- both convenience and flexibility


### Growing types

```scala
  var capital = Map("US" -> "Washington", "France" -> "Paris")
  capital += ("Japan" -> "Tokyo")
  println(capital("France")) 
```

The feel is that of a modern "scripting" language like Perl, Python, or Ruby. 
But Scala gives you this fine-grained control if you need it, because maps in Scala are not language syntax. They are library abstractions that you can extend and adapt.

You could specify a particular implementation, such as a HashMap or a TreeMap, or you could specify that the map should be thread-safe, by mixing in a SynchronizedMap trait, or you could override any other method of the map you create. 

Scala allows users to grow and adapt the language in the directions they need by defining easy-to-use libraries that feel like native language support.

### Actor from Erlang

Actors are concurrency abstractions that can be implemented on top of threads. They communicate by sending messages to each other. An actor can perform two basic operations, message send and receive. The send operation, denoted by an exclamation point (!), sends a message to an actor. Here's an example in which the actor is named recipient:

    recipient ! msg

A send is asynchronous; that is, the sending actor can proceed immediately, without waiting for the message to be received and processed. Every actor has a mailbox in which incoming messages are queued. An actor handles messages that have arrived in its mailbox via a receive block:

```  scala
  actor { 
    var sum = 0
    loop {
      receive {
        case Data(bytes)       => sum += hash(bytes)
        case GetSum(requester) => requester ! sum
      }
    }
  }
```
A receive block consists of a number of cases that each query the mailbox with a message pattern. The first message in the mailbox that matches any of the cases is selected, and the corresponding action is performed on it. If the mailbox does not contain any messages that match one of the given cases, the actor suspends and waits for further incoming messages.

### Why Scalable
object-oriented, every value is an object and every operation is a method call
(primitive value in java is not java)

Scala is more advanced than most other languages when it comes to composing objects. Traits are like interfaces in Java, but they can also have method implementations and even fields. Objects are constructed by mixin composition, which takes the members of a class and adds the members of a number of traits to them. In this way, different aspects of classes can be encapsulated in different traits. 

This looks a bit like multiple inheritance, but differs when it comes to the details. Unlike a class, a trait can add some new functionality to an unspecified superclass. This makes traits more "pluggable" than classes. In particular, it avoids the classical "diamond inheritance" problems of multiple inheritance, which arise when the same class is inherited via several different paths.
(In java, use interface and composition for multiple inheritance)


Functional: full blown functional language
Functions are first-class values. 
The operations of a program should map input values to output values rather than change data in place, methods should not have any side effects, encourage immutable data structures and referentially transparent methods. 
- immutable, thread-safe, scalable, safe hashtable keys / require that a large object graph be copied

Other popular functional languages are Scheme, SML, Erlang, Haskell, OCaml, and F#.


### Statically Typed
classifies variables and expressions according to the kinds of values they hold and compute

Starting from a system of nested class types much like Java's, it allows you to parameterize types with generics, to combine types using intersections, and to hide details of types using abstract types.

The most common arguments against static types are that they make programs too verbose, prevent programmers from expressing themselves as they wish, and make impossible certain patterns of dynamic modifications of software systems. For instance, Alan Kay, the inventor of the Smalltalk language, once remarked: "I'm not against types, but I don't know of any type systems that aren't a complete pain, so I still like dynamic typing."

- Verifiable properties: booleans are never added to integers; private variables are not accessed from outside their class; functions are applied to the right number of arguments; only strings are ever added to a set of strings.

- Safe refactorings: provides a safety net to changes codebase. like a refactoring that adds an additional parameter to a method , you can do the change, re-compile your system and simply fix all lines that cause a type error. In all cases a static type check will provide enough assurance that the new system works just like the old.

- Documentation. Unlike a normal comment, a type annotation can never be out of date. Furthermore, compilers and integrated development environments can make use of type annotations to provide better context help. 




