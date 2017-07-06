# Object Oriented
- Polymorphism
    + ability of an object to take many forms
    + when a superclass reference is used to refer to a subclass object.
        * `SuperPoly obj3 = new Poly();`
    + compile time polymorphism
        * Method overloading: one class with different signature method
    + run time polymorphism
        * Method Overriding: among different class, subclass override super class methods
- Inheritance
    + one class acquires the properties of another class / abstract class / interface
    + extends class, implements interface
- Encapsulation
    + wrapping data and method as single unit
    + declare data as private and provide public getter and setter
- Abstraction
    + hiding implement detail from user
    + achieved by interface and abstract class

##### Inheritance
Inheritance is a mechanism in which one object acquires all the properties and behaviour of another object of another class. It represents IS-A relationship. It is used for Code Resusability and Method Overriding.

- this : refer to current object
- object class : super class of all class
- super: keyword that refers to the immediate parent class object
- composition: hold reference of the other class within some other class

Private members of the superclass are not inherited by the subclass and can only be indirectly accessed.
Since constructors and initializer blocks are not members of a class, they are not inherited by a subclass.


##### Runtime Polymorphism

Runtime polymorphism or dynamic method dispatch is a process in which a call to an overridden method is resolved at runtime rather than at compile-time.

In this process, an overridden method is called through the reference variable of a super class. The determination of the method to be called is based on the object being referred to by the reference variable.

##### abstraction

Abstraction is a process of hiding the implementation details and showing only functionality to the user.

Abstraction hides the implementation details whereas encapsulation wraps code and data into a single unit.

##### Interface

Creating an Interface means defining a Contract. This Contract states what a class can do without forcing how it should do.
```java
public interface MyInterface
{
int i=0;
public void Height(int height);
public abstract void setHeight();
}
```

Methods: implicitly public and abstract, cannot be static,final, strictfp or native.
Variables: Constant. Implicitly public static final.
Extend: Can and can only extend other interface

Marker interface: An interface that have no data member and method. For example Serializable, Cloneable etc.

Difference: abstract class can have real methods, like constructor, method body, instance variable.
can implement over one interfaces




##### Package
A package is a group of similar type of classes interfaces and sub-packages. It provides access protection and removes naming collision.

java.lang package is by default loaded internally by the JVM.

### Garbage Collection

Garbage collection is a process of reclaiming the runtime unused objects.It is performed for memory management.

gc() is a daemon thread.gc() method is defined in System class that is used to send request to JVM to perform garbage collection.

finalize() method is invoked just before the object is garbage collected.It is used to perform cleanup processing.

```
protected void finalize( )
{
   // finalization code here
}
```

could use it as a backstop for an object holding an external resource (socket, file, etc). Implement a close() method and document that it needs to be called.Implement finalize() to do the close() processing if you detect it hasn't been done.

It provides extra safety in an exceptional/buggy situation. Not every caller is going to do the correct try {} finally {} stuff every time. Unfortunate, but true in most environments.

##### How to store SSN Password?

Use mutable character array, not String 
Immutable object store in memory, even use gc() cannot guarantee JVM clean it.
Might be accessed by hackers into the memory.

##### T extends comparable T
```
public static <T extends Comparable<? super T>> void sort(List<T> list) 
```

This declaration says, that argument to sort() method must be of a type List<T>, 
where T could be any type that implements `Comparable<? super T>` (sort requires compareTo method defined in Comparable to compare elements of list) 
`Comparable<? super T>` means that type ? passed to Comparable could be T or any supertype of T. 
