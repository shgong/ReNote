# Core I

### 0.Overview

Features: Object-oriented, platform independent(byte code), secure, multithreaded,  distributed, dynamic(runtime information), high performance (JIT), Easy(no Pointer)

```sh
java -version   # check java is successfully installed
javac target.java    # compile java to byte code (.class), in the same location
javac -d . target.java   # in corresponding package location (package com.Tuple)
java com.Tuple.target      # run file, no need .class extension
```

Basic Syntax
- Case Sensitive
- UppercaseClassName, lowercaseMethodName()
- `public static void main(String args[])`
- Identifiers begin with $_Aa
- Comment with `/* */` or `//`


### 1.Environment - Java Virtual Machine
- JVM = Runtime environment for compiled bytecode. Platform dependent.
- JRE = Implementation of JVM with Required Limbrary.
- JDK = JRE + Develop Tools

JIT compiler: a JVM implementations. Turns Java bytecode into machine instructions. Compiles similar functionality byte code at the same time. Reduce compilation time.

Classloader: a subsystem of JVM that is used to load classes and interfaces.

##### Memory areas are allocated by JVM
- Class(Method) Area
- Heap
- Stack
- Program Counter Register
- Native Method Stack


### 2 Class & Method
Java Class is a template for creating objects which defines its state and behavior. A class contains field and method to define the state and behavior of its object.

```java
package com.Tuple;

public class HelloWorld {
    int instanceField;          // Instance Variable
    static int staticField;   // Class or Static Variable

    public HelloWorld() {  // Constructor (Creator)
        System.out.println("Constructor");
    }

    void method() {        // Method (Behavior)
        final String k = "Method";// Local Variable
        System.out.println(k);
    }
}

System.out.println(HelloWorld.staticField);  // can access by ClassName， no need to create object
HelloWorld obj = new HelloWorld(); // Reference Variable
obj.method();
System.out.println(obj.instanceField);
System.out.println(obj.staticField);
```

main method is static to avoid extra memory allocation
if no static signature, it will compile, but cannot find when run



##### Modifier

Access Modifier
- Class: public for all classes, default for same package
- Method/Variable: protected for sub classes inherited, private for itself

Non Access Modifier
- final: class cannot extend, method cannot override, variable cannot change
- abstract: method that only have a signature, also make it class abstract
- synchronized: method access by one thread at a time
- native: method with platform dependent code

```
            | Class | Package | Subclass | World
————————————+———————+—————————+——————————+———————
public      |   +   |    +    |    +     |   +
————————————+———————+—————————+——————————+———————
protected   |   +   |    +    |    +     |
————————————+———————+—————————+——————————+———————
no modifier |   +   |    +    |          |
————————————+———————+—————————+——————————+———————
private     |   +   |         |          |

+ : accessible
  : not accessible
```


##### Create Objects

```java
public class ObjectCreationExample {
    public static void main(String[] args) {
        Tuple obj = new Tuple();    // Using NEW KEYWORD
        Class cls = Class.forName("Tuple");
        Tuple obj = (Tuple) cls.newInstance();  // Using NEW INSTANCE, typical jdbc
    }
}

class Tuple{
    String Owner;
}
```



### 3 Data Types

##### Primitive

- byte: 8bit, -128~127
- short: 16bit
- long: 64bit
- float: single-precision 32-bit IEEE 754
- double: double-precision 64-bit IEEE 754
- boolean
- char: single 16bit unicode, \u0000 to \uffff

Reference
- created using constructor of class

Literal
- source code representation of fixed value
- number system for literals:
- `int decimal = 100;int octal = 0144;int hexa =  0x64;`


##### Number Class
it is a wrapper class of Byte Double Float Integer Short Long
use object instead of primitives
```java
Integer x = 5; // boxes int to an Integer object
x =  x + 10;   // unboxes the Integer to a int

//Method
x.doubleValue() // type conversion, 15.0
x.compareTo(target)
int x =Integer.parseInt("9");

```

##### Character Class
when need objects, use this instead of char
```
Character ch = new Character('a');
isLetter('9'), isDigit('6'), isWhitespace('')
isUpperCase('C'),isLowerCase('c'),toUpperCase(‘e),toString('s')
```


#####  String Class

String is Immutable, once created it can not be modified. To overcome that problem String Buffer and String builder can be used.

Why immutable? Because java uses the concept of string literal to make it more memory efficient. one reference variable changes the value of the object, it will be affected to all the reference variables. That is why string objects are immutable in java.

Part of java.lang package.

- String: immutable
- notable methods:
    1.length()
    2.toString()
    3.trim
- String Buffer: thread safe
- String Builder: mutable, not thread safe, faster in most class
- notable methods:
    - append()
    - Insert()
    - Length()

String Intern: string equal(method) with same content, but == only when exactly point to same object, == save space and faster


String creation

```java
String str=new String("newstring");
// create a new object, and give it a literal

String str2="newstring";
// search all literals on string pool to see whether match content
// if so, return reference, otherwise create new and intern it in string pool

char[] helloArray = { 'h', 'e', 'l', 'l', 'o', '.'};
String helloString = new String(helloArray);
```

String literal: the reference in String Pool, to String Objects in Heap
String Object: directly referring the String Object in Heap

An object is eligible for garbage collection when it is no longer referenced from an active part of the application. String literals always have a reference to them from the String Literal Pool. That means that they always have a reference to them and are, therefore, not eligible for garbage collection.

In general, you should use the string literal notation when possible. It is easier to read and it gives the compiler a chance to optimize your code.


String Manipulation
```java
a.length(); string1.concat(string2), string1+string2

String fs;
fs = String.format("The value of the float variable is " +
                   "%f, while the value of the integer " +
                   "variable is %d, and the string " +
                   "is %s", floatVar, intVar, stringVar);
System.out.println(fs);

char get=s.charAt(int index); int find = s.indexOf( 'o' );
int hc = s.hashCode()

for (String retval: s.split("-")){
   System.out.println(retval);
}
```


##### Array

0-based

```java
dataType[] arrayRefVar;
arrayRefVar = new dataType[arraySize];

for (int i = 0; i < myList.length; i++) {
    System.out.println(myList[i] + " ");
}

for (double element: myList) {
   System.out.println(element);
}
```

Array Class
```
public static int binarySearch(Object[] a, Object key)
public static boolean equals(long[] a, long[] a2)
public static void fill(int[] a, int val)
public static void sort(Object[] a)
```

### 3 Streaming
java.io package

##### byte stream
```java
import java.io.*;

public class CopyFile {
   public static void main(String args[]) throws IOException
   {
      FileInputStream in = null;  FileOutputStream out = null;
      try {
         in = new FileInputStream("input.txt");
         out = new FileOutputStream("output.txt");
         int c;
         while ((c = in.read()) != -1) out.write(c);
      }finally {
         if (in != null)  in.close();
         if (out != null) out.close();
      }
   }
}
```

##### standard stream
```java
import java.io.*;
public class fileStreamTest{
   public static void main(String args[]){
   try{
      byte bWrite [] = {11,21,3,40,5};
      OutputStream os = new FileOutputStream("test.txt");
      for(int x=0; x < bWrite.length ; x++){
         os.write( bWrite[x] ); // writes the bytes
      }
      os.close();

      InputStream is = new FileInputStream("test.txt");
      int size = is.available();
      for(int i=0; i< size; i++){
         System.out.print((char)is.read() + "  ");
      }
      is.close();
   }catch(IOException e){
      System.out.print("Exception");
   }
   }
}

```

##### directory of files
```java
import java.io.File;

public class ReadDir {
   public static void main(String[] args) {

      File file = null;
      String[] paths;

      try{
        File d = new File("/tmp/user/java/bin");
        d.mkdirs();

        file = new File("/tmp");
        paths = file.list();
        for(String path:paths) System.out.println(path);

      }catch(Exception e){
         // if any error occurs
         e.printStackTrace();
      }
   }
}

```

### 4 Exceptions

Throwable
- Error: environment error, cannot be handle, e.g. JVM running out of memory
- Exception
    + Checked: must be caught at compile time, like arithmetic
    + Unchecked: Runtime exceptions, like array out of bounds

```java
try {
throw new IOException();
} catch (IOException e) {
// Handle only IO Exception
// This block will get executed only in case of IOException
}catch (Exception e) {
// Handle only all other type of exception
// This block will get executed in case of all exception except IOException
}finally{
System.out.println("This block will get executed no matter exception occur or not");
}
```

Exception Propagation
catch block > method where this method was called from > previous method in method stack > reach to bottom and handled by JVM.

### 5. Nested Classes

- Static Nested Class
- Inner Class
    + Inner Class
    + Method Local Inner Class
    + Anonymous Inner Class

##### Inner class
just need to write a class within a class. can be private and only access the class through a method. can also be used to retrieve private members
```
class Outer_Demo {
   //private variable of the outer class
   private int num= 175;
   //inner class
   public class Inner_Demo{
      public int getNum(){
         System.out.println("This is the getnum method of the inner class");
         return num;
      }
   }
}

public class My_class2{
   public static void main(String args[]){
      //Instantiating the outer class
      Outer_Demo outer=new Outer_Demo();
      //Instantiating the inner class
      Outer_Demo.Inner_Demo inner=outer.new Inner_Demo();
      System.out.println(inner.getNum());
   }
}
```

##### Method-local Inner Class
the scope of the inner class is restricted within the method.

##### Anonymous Inner Class
declare and instantiate them at the same time. Generally they are used whenever you need to override the method of a class or an interface.

```java
abstract class AnonymousInner{
   public abstract void mymethod();
}

public class Outer_class {
   public static void main(String args[]){
   AnonymousInner inner= new AnonymousInner(){
      public void mymethod(){
         System.out.println("This is an example of anonymous inner class");
      }
   };
   inner.mymethod();
 }
}

```

##### Static
```
public class Outer{
   static class Nested_Demo{
      public void my_method(){
         System.out.println("This is my nested class");
      }
   }

   public static void main(String args[]){
      Outer.Nested_Demo nested=new Outer.Nested_Demo();
      nested.my_method();
   }

}
```


### Enumeraion
restrict variable to few values

```java
//create
public enum Workday {
MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY
}

private Workday workday;
if(workday == Workday.FRIDAY)
    System.out.println("Hurray! Tomorrow is weekend!");

// enum
for(Workday w : Workday.values()) {
    System.out.println(w.name());
}

```
