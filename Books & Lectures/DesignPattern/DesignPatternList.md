
# Design Pattern List

## 2. Creational Pattern
Creational class patterns defer some part of object creation to subclasses, while Creational object patterns defer it to another object.

### 2.1 Singleton
Ensure a class only has one instance, and provide a global point of access to it.

```java
public final class Foo {

    private static final Foo INSTANCE = new Foo();

    private Foo() {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already instantiated");
        }
    }

    public static Foo getInstance() {
        return INSTANCE;
    }
}
```

after java5
```java
public enum Foo {
   INSTANCE;
}
```

### 2.2 Abstract Factory
Provide an interface for creating families of related or dependent objects without specifying their concrete classes.


### 2.3 Factory Method
Class Pattern
Define an interface for creating an object, but let subclasses decide which class to instantiate. Factory Method lets a class defer instantiation to subclasses.

### 2.4 Prototype
Specify the kinds of objects to create using a prototypical instance, and create new objects by copying this prototype.


## 3. Structural Pattern
The Structural class patterns use inheritance to compose classes, while the Structural object patterns describe ways to assemble objects.

### 3.1 Adapter 
Class Pattern
Convert the interface of a class into another interface clients expect. Adapter lets classes work together that couldn't otherwise because of incompatible interfaces.

### 3.2 Composite
Compose objects into tree structures to represent part-whole hierarchies. Composite lets clients treat individual objects and compositions of objects uniformly.

### 3.3 Decorator
Attach additional responsibilities to an object dynamically. Decorators provide a flexible alternative to subclassing for extending functionality.

## 4. Bahavioral Pattern
The Behavioral class patterns use inheritance to describe algorithms and flow of control, whereas the Behavioral object patterns describe how a group of objects cooperate to perform a task that no single object can carry out alone.

### 4.1 Visitor

### 4.1 Observer
Define a one-to-many dependency between objects so that when one object changes state, all its dependents are notified and updated automatically.

### 4.2 Interpreter
Class Pattern
Given a language, define a represention for its grammar along with an interpreter that uses the representation to interpret sentences in the language.

### 4.3 Strategy
Define a family of algorithms, encapsulate each one, and make them interchangeable. Strategy lets the algorithm vary independently from clients that use it.

### 4.4 Template Method
Class Pattern
Define the skeleton of an algorithm in an operation, deferring some steps to subclasses. Template Method lets subclasses redefine certain steps of an algorithm without changing the algorithm's structure.

### 4.5 Iterator
Provide a way to access the elements of an aggregate object sequentially without exposing its underlying representation.
