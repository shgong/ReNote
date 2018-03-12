
# Design Pattern Overview

### 1 Preview

- Designing object-oriented software is hard
    + Designing reusable object-oriented software is even harder
- Procedure
    + find pertinent objects
    + factor them into classes at the right granularity
    + define class interfaces and inheritance hierarchies
    + establish key relationships among them. 
- Requirement
    + be specific to the problem at hand 
    + be general to address future problems and requirements
    + avoid redesign, at least minimize it. 

> A design pattern names, abstracts, and identifies the key aspects of a common design structure that make it useful for creating a reusable object-oriented design.

###  2 MVC Example

- MVC establish a subscribe/notify protocol between Model/View, and encapsulates the response mechanism in a Controller object
    + Model: application object
    + View: screen presentation
    + Controller: how user interface reacts to user input. 
- Main Patterns Used
    + Observer: change to model can affect any number of views
    + Composite: group nested views and treat the group like individual object
    + Strategy: controller represent algorithm of response behavior
- Other Pattern
    + Factory Method: specify the default controller class for a view 
    + Decorator: add scrolling to a view

###  3 How Design Patterns Solve Design Problems

- How do we decide what should be an object?
- Facade: represent complete subsystems as objects
- Flyweight: support huge numbers of objects at the finest granularities


#### 3.1 Finding Appropriate Objects

- the object's internal state is said to be encapsulated
    + can only be accessed via request/message, 
    + can only be changed with methods/operations 
    + the representation is invisible from outside.
- Decomposing system into objects is difficult, too many factors often in conflicting ways:
    + encapsulation, 
    + granularity, 
    + dependency, 
    + flexibility, 
    + performance, 
    + evolution, 
    + reusability, and on and on. 
- Design patterns help you identify less-obvious abstractions and the objects that can capture them, like objects that represent a process or algorithm don't occur in nature
    + Strategy pattern describes how to implement interchangeable families of algorithms. 
    + State pattern represents each state of an entity as an object. 

#### 3.2 Specifying Object Interfaces

- Concept
    + signature: defines name, parameters, and return value of operation
    + interface: set of all signature in a object
    + type: denote a particular interface, has subtype/supertype
    + dynamic binding: the run-time association of a request to an object and one of its operations
    + polymorphism:  substitute objects that have identical interfaces for each other at run-time, lets a client object make few assumptions about other objects beyond supporting a particular interface, simplifies the definitions of clients, decouples objects from each other, and lets them vary their relationships to each other at run-time.
- Design Patterns help you define interfaces by identifying their key elements and the kinds of data that get sent across an interface, specify relationships between interfaces
    + Memento: The pattern stipulates that Memento objects must define two interfaces: a restricted one that lets clients hold and copy mementos, and a privileged one that only the original object can use to store and retrieve state in the memento

#### 3.3 Specifying Object Implementations

- Concept
    + class: define an object's implementation 
    + abstract class: define common interface
    + inheritance: define class by extending
    + mixin class: provide optional interface
- Class versus Interface Inheritance
    + Type: only refers to its interface—the set of requests to which it can respond
    + Class: internal state and the implementation of its operations
    + Class inheritance:  defines an object's implementation in terms of another object's implementation (code and representation sharing) 
    + Interface inheritance (subtyping): describes when an object can be used in place of another.
    + Many languages don't make the distinction explicit. In languages like C++ and Eiffel, inheritance means both interface and implementation inheritance.
- Many of the design patterns depend on this distinction. 
    + Chain of Responsibility: objects must have a common type, but usually they don't share a common implementation. 
    + Composite: Component defines a common interface, but Composite often defines a common implementation. 
    + Command, Observer, State and Strategy: often implemented with abstract classes that are pure interfaces.

#### 3.4 Programming to an Interface, not an Implementation

- Inheritance's ability to define families of objects with identical interfaces is also important.Because polymorphism depends on it.
- two benefits to manipulating objects solely in terms of the interface defined by abstract classes:
    + Clients remain unaware of the specific types of objects they use, as long as the objects adhere to the interface that clients expect.
    + Clients remain unaware of the classes that implement these objects. Clients only know about the abstract class(es) defining the interface.
- Creational patterns ensure that your system is written in terms of interfaces, not implementations.

> Programming to an Interface, not an Implementation

#### 3.5 Putting Reuse Mechanisms to Work

- Inheritance vs Composition
    + Class inheritance
        * define the implementation of one class in terms of another's
        * white-box: internals of parent classes are often visible to subclasses.
        * advantage: 
            - statically defined at compile time, starightforward to use
            - easier to modify the implementation, override
        * disadvantage: 
            - cannot change parent implementation at run time
            - inheritance exposes a subclass to details of its parent's implementation, inheritance breaks encapsulation
            - rewritten parent class limits flexibility and reusablity
    + Object composition
        * obtain new functionality by assembling or composing objects
        * requires the objects being composed have well-defined interfaces
        * black-box: no internal details of objects are visible.
        * difference:
            - defined dynamically at runtime, require carefully designed interface
            - objects are accessed solely through their interfaces, we don't break encapsulation
            - fewer implementation dependencies
            - keep each class encapsulated and focused on one task
        * tradeoff
            - Your classes and class hierarchies will remain small and will be less likely to grow into unmanageable monsters 
            - a design based on object composition will have more objects

> Favor object composition over class inheritance.

- Delegation
    + two objects are involved in handling a request: a receiving object delegates operations to its delegate. the receiver passes itself to the delegate to let the delegated operation refer to the receiver.
    + example
        * Inheritance: make Window a subclass of Rectangle
        * Delegation: Window class keep a Rectangle instance variable and delegating Rectangle-specific behavior to it
    + advantage
        * making composition as powerful for reuse as inheritance
        * easy to compose behaviors at run-time and to change the way they're composed
    + disadvantage
        * Dynamic, highly parameterized software is harder to understand
        * run-time inefficiencies
    + Design Pattern
        * State, Strategy and Visitor: depend on delegation
        * Mediator, Chain of Responsibility and Bridge: also use it
- Inheritance vs Parameterized Types
    + Parameterized types, also known as generics and templates
    + define a type without specifying all the other types it uses
    + difference
        * composition: change the behavior at run-time, requires indirection andless efficient.
        *  Inheritance: provide default implementations and lets subclasses override them, but cannot change at run-time
        *  Parameterized types: can change the types that a class can use, but  but cannot change at run-time

#### 3.6 Designing for Change

Design patterns help you avoid this by ensuring that a system can change in specific ways. Each design pattern lets some aspect of system structure vary independently of other aspects, thereby making a system more robust to a particular kind of change.

Typical Problems

- Creating an object by specifying a class explicitly: create object directly can complicate future changes
- Dependence on specific operations: not easy to change how request get satisfied
- Dependence on hardware/software platform: limit external os and api
- Dependence on object implementation: hiding implementation information from clients keeps changes from cascading
- Algorithmic dependencies: algorithms that are likely to change should be isolated.
- Tight coupling: Classes that are tightly coupled are hard to reuse in isolation
- extending functionality by subclassing: use Object composition and delegation 
- Inability to alter classes conveniently: use modification design patterns like adapter and decorator



