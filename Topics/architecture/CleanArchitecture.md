# Clean Architecture

> This is the monstrosity in love, lady, that the will is infinite, and the execution confined; that the desire is boundless, and the act a slave to limit. — William Shakespeare


### Preface

- The architecture rules stays the same
  - it is independent of every other variable
  - very little has changed over 50 years
  - basic building blocks of a computer program have not changed
- And it is those rules—those timeless, changeless, rules—that this book is all about.


## I. Introduction

### 1. What is Design and Architecture

- There is no different between them, none at all.
- The low-level details and the high-level structure are all part of the same whole, no clear dividing line separates them.
- The only way to go fast is to go well

### 2. A tale of two values

- two values
  - behavior
    - the requirements, to make money or save money
  - structure
    - software must be soft, easy to change
    - each new request is harder to fit the last

- President Eisenhower's matrix
  - I have two kinds of problems, the urgent and the important
  - the urgent are not important
  - the important are never urgent


## II. Programming Paradigms

- Structured programming
  - Edsger Wybe Dijkstra, 1968
    - Go To statement considered harmful, an article to CACM, March issue
  - replaced with if/then/else and do/while/until
  - Discipline on direct transfer of control

- Object Oriented programming
  - Ole Johan Dahl & Kristen Nygaard, 1966
    - function call stack frame in ALGOL could move to a heap for use later
  - What is OO
    - combination of data and the function
  - enabled class, local variable, polymorphism and avoid function pointers
  - Discipline on indirect transfer of control
- Functional programming
  - ALonzo Church, 1936, invented l-calculus and later form lisp language
  - Discipline on assignment

- Each paradigm take something away of us

- Each paradigm help major concern of architecture
  - structured, algorithm foundation of modules
  - OOP, separation of components
  - functional, location of and access to data

## III. Design Principles

### SRP: The Single Responsibility Principles
- corollary to Conway's law
  - the best structure for a software system
  - heavily influenced by the social structure of the organization that use it
- each software module has one and only one reason to change
  - or, a module should be responsible for only one actor

- Symptoms
  - accidental duplication: two actor share the same method
  - merge: no easy way to merge changes
- Solution
  - put into multiple related classes
- Scale
  - at level of component, the common closure Principle
  - at level of architecture, the Axis of Change responsible for creation of Boundaries

### OCP: The Open-Closed Principle
- Bertrand Meyer, 1980s
- a software artifact should be open for extension but closed for modification
- solution
  - partition the system into components
  - arrange those components into a dependency hierarchy that protect higher-level components

### LSP: The Liskov Substitution Principle
- Barbara Liskov, 1988
- To build software system from interchangeable parts
  - They must adhere to a contract that allow them subsituted each other
- Read specification carefully to avoid add additional mechanism

### ISP: The Interface Segregation Principle
- avoid depending on things that they don't use
- language level
  - statically typed language force declaration like import, use, include
  - dynamiclaly typed language can infer deps runtime
- architecture level
  - system use framework, framework use database
  - database update may fail the system

### DIP: The dependency Inversion Principle
- high-level policy should not depend on code
  - that implements low-level detail
- Stable abstractions
  - don't refer to volatile concrete classes
  - don't derive from volatile concrete classes
  - don't override concrete functions
  - don't mention the name of anything concrete and volatile
- Factories
  - to handle creation of volatile concrete objects

## IV. Component Principles

- Components are units of deployment
  - Java jar, ruby gem, .Net Dll

### Components Cohesion

- Question: Which class belong in which components?

- REP: The Reuse/Release Equivalence Principle
  - the granule of reuse is the granule of release
  - the classes and modules formed into a component must belong to a cohesive group
  - they should be releasable together, sharing same version number and release tracking, documentation

- CCP: The Common Closure Principles
  - gather into components those classes that change for the same reasons and at the same time
  - separate into different components those classes that change at different times and for different reasons
  - SRP at component levels

- CRP: The Common Reuse Principle
  - don't force users of a component to depend on things they don't need
  - ISP at component level


### Components Coupling
- Question: Relationship between components

- The Acyclilc dependency principle
  - allow no cycles in the component dependency graph
    - everyone keeps on changing code trying to make it work with last changes some one else made
  - Weekly build
    - used to be common in medium-sized projects
    - all developers ignore each other for 4 days, then integrate on Friday
    - but it become increasingly hard to finish integration
  - Eliminating dependency cycles
    - partition environment into releasable components
    - changes don't have immediate affect on other teams
    - inverting the dependency when necessary

- The stable dependencies principle
  - depend in the direction of stability
    - fan-in: incoming dependency, classes that used by others
    - fan-out: outgoing dependency, classes that use outside components
    - I (Instability) = fan-out / (fan-in + fan-out), 0 most stable, 1 most unstable

- the stable abstraction principle
  - a component should be as abstract as it is stable
    - Nc: classes in the component
    - Na: abstract classes and interfaces in the component
    - A (Abstractness) = Na / Nc

- The I/A Graph
  - zone of pain: area of (0,0), highly stable and concrete, hard to extend
  - zone of uselessness: area of (1,1), unstable and abstract, no one use it
  - desirable: A + I = 1  line area

## V. Architecture

## VI. Details
