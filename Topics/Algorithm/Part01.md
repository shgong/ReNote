
# 1. Introduction
# 2. Objects & Methods

- Java string point to the same

```java
String s1 = new String();
String s2 = new String();
s1 = "Yow!";
s2 = s1;              // same object
s2 = "Yow!";          // different object
s2 = new String(s1);  // different object
```

- 3 String Constructors
  - new String()
  - "whatever"
    - Constructor always have same name as class
    - except for this magic quote
  - new String(s1)
    - makes a copy of the object s1 references
- String objects are immutable

```java
s2 = s1.toUppercase();
String s3 = s2.concat("!");
s3 = s2 + '!';
String s4 = "*".concat(s2).concat('*');
s4 = "*" + s2 + "*";
```

- Java I/O Classes
  - System.out
  - System.in
  - BufferedReader objects have `readLine` method => chars into text
    - construct with InputStreamReader => into characters, typically 2 byte Unicode
      - construct with System.in() => raw data

```java
import java.io.*;
import java.net.*;

class SimpleIO {
  public static void main(String[] arg) throw Exception {
    InputStream ins = System.in;
    ins = new URL("http://www.whitehouse.gov/").openStream();
    BufferedRead key = new BufferedReader(new InputStreamReader(ins));
    System.out.println(key.readLine());
  }
}
```

## 3. Defining classes

- Java Classes
  - Fields
    - variables stored in objects
    - used to store data
    - instance variables
  - Static Fields
    - shared by whole class
  - Static Method
    - method shared by whole class
    - no this inside

```java
public Human {
  public String name;
  public int age;
  public static int numberOfHumans = 0;

  public void introduce(){
    System.out.println("I'm " + name + ", " + age + " years old.")
  }

  public Human(String givenName){
    this.name = givenName;
    numberOfHumans++;
  }

  public Human(){
    numberOfHumans++;
  }
}
```

## 4. Types and Conditions
- primitive types
  - byte: 8 bit, -128 to 127
  - short: 16 bit, -32768 to 32767
  - int: 32 bit, -2147483648 to 2147483647
  - long: 64 bit, default choice
  - double: 64 bit
  - float: 32 bit
  - boolean: 1 bit
  - char: a character
- object Types: a reference

- `java.lang` library
  - Math.abs(x)
  - Integer.parseInt("1984")
  - Double.parseDouble("3.14")

```java
int i = 43;
long l = 43L;
l = i; // okay
i = l; // compile ERROR
i = (int) l // okay
```

Boolean test
```java
3 == 5 // false
false == (3==0) //true
4!=5-1 // false
4.5f == 4.5 //true, equal to double 4.5
4.4f == 4.4 //false, 4.4f will be rounded off
```

## 5. Iteration and Array I
```java
public static boolean isPrime(int n){
  int divisor = 2;
  while (divisor < n) {
    if(n % divisor == 0) {return false;}
  }
  return true;
}

public static void printPrimes(int n){
  int i;
  for (i=2;i<=n;i++){
    if(isPrime(i)){
      System.out.println("" + i);
    }
  }
}

public static void betterPrimes(int n){
  boolean[] prime = new boolean[n+1];
  for (int i=2;i<=n;i++){prime[i]=true}
  for (int divisor=2; divisor*divisor<=n; divisor++){
    if(prime[divisor]){
      for (int i=2*divisor; i<=n; i=i+divisor) {
        prime[i] = false;
      }
    }
  }
}
```

## 6. Iteration and Array II

- Automatic Array Construction

```java
int [][] table = new int[x][y];
// x arrays of y ints
int [][] c = {{7,3,2}, {x}, {8,5,0,0}, {y+z,3}};
// only initialize, cannot use in assignment
int[] a, b, c; // all arrays
int a[], b, c[][]; // c-style declaration
int[] a, b[]; // mix style, a is array, b is 2d array
```

- continue
  - only applied to loops
  - don't exit loops

```java
test:
if (x==0) {
  loop:
  while(i<9){
    stuff: {
      switch(z[i]) {
        case 0: break;            // goto statement1
        case 1: break stuff;      // goto statement 2
        case 2: break loop;       // goto statement 4
        case 3: break test;       // goto statement 5
        case 4: continue;         // goto location 3
        default: continue loop;   // goto location 3
      }
      statement1();
    }
    statement2();
    i++;
    // location 3
  }
  statement4()
}
statement5()
```

## 7. Linked List I

```scala
case class ListNode(item:Int, next:Option[ListNode])
// recursive data type
val l3 = ListNode(6, None)
val l2 = ListNode(1, l3)
val l1 = ListNode(7, l2)
```

## 8. Linked List II

```java
class DListNode {
  Object item;
  DListNode next;
  DListNode prev;
}

class DList {
  private DListNode head;
  private DListNode tail;

  public void removeTail(){
      tail.prev.next = null;
      tail = tail.prev;
  }
}
```

## 9. Stack Frames

- Java Stack and Heap
  - Heap stores all objects
  - Stack stores all local variables, and make recursion available

- When method is called, Java create a stack frame (aka activation records)
  - recursive call: stack of stack frames
  - call StackTrace of methods
- When method finished, its stack frame is erased
  - Thread.dumpstock()
    - will print a list of all stack frames now

```java
class IntBox {
  static void doNothing(int x){
    x=2;
  } // int parameter, won't change

  public int i;

  public void set3(IntBox ib){
    ib.i = 3;
  } // object reference, only copy reference

  static void badSet4(IntBox ib){
    ib = new IntBox(4);
    ib.i = 4;
  }
}
```

- `a=1`
  - call `IntBox.doNothing(1)`
  - does not change a value, in a different stack frame
- `b = new IntBox()`
  - call `IntBox.set3(b)`
    - b's value become 3
  - call `IntBox.badSet4(b)`
    - ib refer to another IntBox
    - so we fail to set value

## 10. Testing

- 4 degrees of equality
  - reference equality, ==
  - shallow structural equality
    - fields are ==
  - deep structural equality
    - fields are equals()
  - logical equality
    - fraction class 1/3 == 2/6
    - set element orders
```
s1 = "String"
s2 = "String"
s1 == s1 // false
s1.equals(s2) // true
```

two linked list can be deep eq but not shallow.

## 11. Inheritance

- subclass can modify superclass in 3 ways
  - declare new fields
  - declare new Methods
  - override old methods with new ones

- Java execute a tailList constructor
  - first execute SList() constructor

- Dynamic method lookup
  - every tailList is a SList
    - `SList s = new TailList();` always ok
  - static type: type of a variable
  - dynamic type: class of the object the variable refers
  - when invoke overridden method, Java calls dynamic type method


## 12. Abstract Classes

- Subtleties of Inheritance
  - if method in TailList called eatList()
    - `SList s = new TailList();`
    - `s.eatList();` => compile error
    - Not every SList has an eatList() method
    - Java can't use dynamic method lookup on S
  - assign back and forth
    - `SList s;`
    - `TailList t = new TailList();`
    - `s = t`  fine
    - `t = s`  compile-time error
      - Java compile one line at a line, don't know what s point too;
    - `t = (TailList) s` fine
    - `t = (TailList) new SList()`  run-time error
  - instanceOf tells you whether object is of specific class


## 13. Packages

- collection of classes, interfaces and subpackages
- 3 benefits
  - packages can contain hidden classes not visible outside
  - classes can have hidden fields and methods
  - different packages can have classes with same name

## 14. Exceptions

- Prevent the error by catching the Exceptions
  - 1. Surviving errors
  - 2. Escaping a sinking ship
    - different from return?
      - don't have to return anything
      - can fly many methods down the stack

- Checked and unchecked exceptions
  - Throwable
    - Exception
      - ParserException
      - RuntimeException
        - NullPointer
        - ArrayIndexOutOfBound
    - Error
      - run out of memory or stack space
  - Runtime & Error does not need throws declaration
  - Other exception need declare to check

## 15. More Java

## 16. Game Tree

- Min-in-max algorithm
  - if side=computer find max
  - if side=human find min

## 17. Encapsulation

- module: set of methods that work together to perform some task
