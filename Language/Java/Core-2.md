# Core II

### Concept

- Static
    + used to manage memory to be more efficient
    + common property of all object
    + non-static variable b cannot be referenced from a static context.
    + static method can be access without create instance
- Static block
    + initialize data members
    + highest priority when execution
    + order: static block > non-static block > superclass constructor > subclass constructor = main method.
+ Private
    - sub class cannot access private variable in the super class.
    - Whereas, the inner class can do this from the outer class directly.
+ Protected
    * protected can be accessed within the same package and also outside the package using inheritance (i.e., in subclasses).
+ final
    * final class cannot be inherited
    * final method cannot be overriden
    * final object cannot be modified once assigned like String
    * final variable is constant
+ type casting
    * automatic when widening
        - int i=100; long l=i; float f=l;
    * explicit when narrowing
        - double d=100.04;long l=(long) d; int i = (int) l;

### Package

- java.lang
    + String, StringBuilder, StringBuffer, Math, Object, Throwable
- java.util
    + Arrays, Collections, Date, Scanner, Arraylist, LinkedList, HashSet, TreeSet, HashTable, HashMap, TreeMap

#### java.util.Arrays

- fixed-size sequential of same type
- non-primitive, reference type
- has only one field: length, not length() not a method

```java
int[] myArray5 = new int[5];
int[] myArray5 = { 1, 2, 3, 4, 5 };

for (int i = 0; i < myArray5.length; i++) {
    System.out.print(myArray5[i] + ", ");
}
for (int i : myArray5) {
    System.out.print(myArray5[i] + ", ");
}

myArray5.toString();
myArray5.length;

Integer.valueOf()
Integer.parseInt()

Arrrays.sort()
Arrays.toString()
System.arraycopy()
```

#### java.lang.String
```java
/* literal method */
String s1 = "welcome";
String s2 = "welcome";
System.out.println(s1.equals(s2)); // T
System.out.println(s1 == s2);      // T

/* constructor method */
String s3 = new String("welcome");
String s4 = new String("welcome");
System.out.println(s3.equals(s4)); // T, compare value
System.out.println(s3 == s4);      // F, compare address

// == and equals() are not overridden in StringBuilder
StringBuilder sb1 = new StringBuilder("marlabs");
StringBuilder sb2 = new StringBuilder("marlabs");
System.out.println(sb1.equals(sb2)); // F
System.out.println(sb1 == sb2);      // F
```

- String is immutable
    + high performance: hashcode is frequently used like in hashmap
    + security: widely used as parameter, immutable thread safe
    + methods: toCharArray(), charAt(), length(), substring()
- mutable
    + StringBuffer: threadsafe but slow
    + StringBuilder: fast but not safe
    + add method like append() insert() reverse()


#### Data Type Cast
```java
public class DataTypeCast {
    public static void main(String[] args) {
        short a = 32767;
        short b1 = 0;
        short b2 = 1;

        short c1 = (short) (a + b1); // 32767
        short c2 = (short) (a + b2); // -32768
        int c3 = a + b2;             // 32768
    }
}
```

#### Binary Type Test
```java
System.out.println(1 + 2 + "Marlabs" + 3 + 4);    // 3Marlabs34
System.out.println(1 + 2 + "Marlabs" + (3 + 4));  // 3Marlabs7
```


### Collection

#### equals and hashcode

```java
package com.marlabs;

import java.util.HashMap;

public class Apple {
    private String color;

    public Apple(String color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Apple))
            return false;
        // Cast to the appropriate type.
         Apple target = (Apple) obj;
        return color.equals(target.color);
        // return true;
    }

    // if the hashCode method is not overridden, the final output will be null
    @Override
    public int hashCode() {
        // return this.color.hashCode();

        // the overridden method is not unique
        return this.color.length();
    }

    public static void main(String[] args) {

        Apple a1 = new Apple("green");
        Apple a2 = new Apple("red");

        // hashMap stores apple color and its quantity
        HashMap<Apple, Integer> hm = new HashMap<>();
        hm.put(a1, 10);
        hm.put(a2, 20);
        // hm.put(a1, 30);

        // the "green" apple here can be different if hashCode() is not overridden
        System.out.println(hm.get(new Apple("green")));

        // since no new object is created, hashCode() cannot be overridden
        // System.out.println(hm.get(a2));
    }

}

```

Design Hashcode

- Create a int result and assign a non-zero value.
- For every field f tested in the equals() method, calculate a hash code c by:
    + boolean: calculate (f ? 0 : 1);
    + byte, char, short or int: calculate (int)f;
    + long: calculate (int)(f ^ (f >>> 32));
    + float: calculate Float.floatToIntBits(f);
    + double: calculate Double.doubleToLongBits(f) and handle the return value like every long value;
    + object: Use the result of the hashCode() method or 0 if f == null;
    + array: see every field as separate element and calculate the hash value in a recursive fashion and combine the values as described next.
- Combine the hash value c with result:
    + result = 37 * result + c
- Return result


### Example from Android
```java

  // Use @Override to avoid accidental overloading.
   @Override
   public boolean equals(Object o) {
     // Return true if the objects are identical.
     // (This is just an optimization, not required for correctness.)
     if (this == o) {
       return true;
     }

     // Return false if the other object has the wrong type.
     // This type may be an interface depending on the interface's specification.
     if (!(o instanceof MyType)) {
       return false;
     }

     // Cast to the appropriate type.
     // This will succeed because of the instanceof, and lets us access private fields.
     MyType lhs = (MyType) o;

     // Check each field. Primitive fields, reference fields, and nullable reference
     // fields are all treated differently.
     return primitiveField == lhs.primitiveField &&
             referenceField.equals(lhs.referenceField) &&
             (nullableField == null ? lhs.nullableField == null
                                    : nullableField.equals(lhs.nullableField));
   }

 @Override
 public int hashCode() {
     // Start with a non-zero constant.
     int result = 17;

     // Include a hash for each field.
     result = 31 * result + (booleanField ? 1 : 0);

     result = 31 * result + byteField;
     result = 31 * result + charField;
     result = 31 * result + shortField;
     result = 31 * result + intField;

     result = 31 * result + (int) (longField ^ (longField >>> 32));

     result = 31 * result + Float.floatToIntBits(floatField);

     long doubleFieldBits = Double.doubleToLongBits(doubleField);
     result = 31 * result + (int) (doubleFieldBits ^ (doubleFieldBits >>> 32));

     result = 31 * result + Arrays.hashCode(arrayField);

     result = 31 * result + referenceField.hashCode();
     result = 31 * result +
         (nullableReferenceField == null ? 0
                                         : nullableReferenceField.hashCode());

     return result;
   }


```


### HashMap
```java
HashMap<Integer, String> hm = new HashMap<>();

//add elements to hashmap
hm.put(10, "apple");
hm.put(2, "banana");
hm.put(3, "orange");
hm.put(20, null);
hm.put(null, null);//one null key, multiple null value
System.out.println(hm);

for(Map.Entry<Integer, String> me : hm.entrySet()) {
    System.out.println("Key: " + me.getKey() + " -> Value: " + me.getValue());
}

//use iterator to print key-value pair from a hashmap
Set set = hm.entrySet();
Iterator iterator = set.iterator();
while(iterator.hasNext()) {
    Map.Entry<Integer, String> entry = (Map.Entry) iterator.next();
    //insertion order is not preserved
    System.out.println(entry.getKey() + " : " + entry.getValue());
}
```


## Exception

- throws
    + one or more exception
    + followed by exception class name
    + in method decalaration/signature
- throw
    + one exception
    + followed by specific exception constructor
    + inside method body
