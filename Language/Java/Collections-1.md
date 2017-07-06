

# Collections
Collection is a data structure in which Objects are stored.

- Objects can be Added, Deleted and can traversed in Collection.

- There are 4 type of basic Collection
    + List : Ordered, Duplicates are allowed, Indexed
        * ArrayList : Fast Iteration & Fast Random Access.
        * Vector: Synchronized Method, thread safe
        * LinkedList : Good for implementing Stack and Queue.
    + Sets : No Duplicates. May or may not Ordered. 
        * HashSet : Fast Access, No Order.
        * LinkedHashSet : Iterates by insertion order.
        * TreeSet : Iterates in sorted order.
    + Maps : No Duplicate Keys
        * HashMap: No Order. Not Synchronized. Allow null key.
        * HashTable: Synchronized, thread safe.
        * TreeMap: maintains ascending order.
    + Queue
        * FIFO
        * Priority

### Java Object Class

equals(), toString(), hashcode(), clone()
wait(), notify(), notifyAll()

What is Significance of hashcode()?
Difference between equals and hashcode? 

How to implement deepcopy()?
Clonable interface

### Decide What to Use

- Ordering: Some sort of ordering in the elements. For example, sorted order, insertion order or no specific ordering
- Duplicates – May or may not want to allow duplicate elements in a collection.
- Thread Safe – Ensure the safety of the elements in a collections in case there are multiple threads accessing it.
- Key-Value pair – Store in key-value pairs.
- Blocking operations – Wait for the collection to become non-empty when retrieving an element.
- Random Access – Instant retrieval of an element.
- Upper Bounds – To limit the maximum number of elements a collection can hold.

### utility
```java
Arrays.sort (Object [], Comparator)
Arrays.sort(stringArray, Collections.reverseOrder());

```

##### Comparable and Comparator
||Comparable | Comparator
|-|-----|-----|
|Provide|  only one sort of sequence.|  multiple sort of sequences.
|Method Name | compareTo() | compare().
|Package|java.lang package. |   java.util package.
|If we implement interface| actual class is modified. |Actual class is not modified.

##### Properties file
change the value in properties file, you don't need to recompile the java class. So, it makes the application easy to manage.

#####  hashCode() 
returns a hash code value (an integer number).
d returns the same integer number, if two keys are same.
But, it is possible that two hash code numbers can have different or same keys.

##### Generic
Generic class, we don't need typecasting. It is typesafe and checked at compile time.

##### Iterator

Iterator traverses the elements in forward direction only. Can be used in List, Set and Queue.

ListIterator traverses the elements in backward and forward directions both.
Can be used in List only.

Enumeration can traverse only legacy elements, not non-legacy elements, not fail-fast, but faster than iterators.

### List

ArrayList: not sync, increase size by 50% array size, not efficient for manipulation because a lot of shifting is required, better to store and fetch data.

LinkedList: Uses doubly linked list. Efficient for manipulation.



```java
// Built-in Arrays: Fixed length
int[] arr  = new int[10];
int[][] arr  = new int[10][]; //   Multi Dimensional Array  

// ArrayList
ArrayList<String> obj = new ArrayList<String>();
obj.add("hello");
obj.add(2, "bye");
obj.remove("Chaitanya");
obj.remove(3); // remove index
obj.remove((Integer)3)//if remove integer object
obj.set(2, "Tom");
int pos = obj.indexOf("Tom"); // find in arraylist
String str= obj.get(2); // find with index
int numberofitems = obj.size();
obj.contains("Steve"); 
obj.clear(); // remove all
```

### Set
HashSet contains only values whereas HashMap contains entry(key,value). HashSet can be iterated but HashMap need to convert into Set to be iterated.


### HashTable

```java
import java.util.HashMap;   import java.util.Iterator;
import java.util.Map;       import java.util.Set;
 
public class HashMapExample {

public static void main(String[] args) {
 
    Map<Tuple, Integer> m1 = new HashMap<Tuple, Integer>();
     
    Tuple t1 = new Tuple(1, 2);     m1.put(t1, 1);
    Tuple t2 = new Tuple(1, 3);     m1.put(t2, 2);
    Tuple t3 = new Tuple(2, 1);     m1.put(t3, 3);

    System.out.println("Addition Done");
     
    /*
    - Below you can find 3 different ways to iterate a Map. Uncomment
    - different section and see the different in Output. Pay attention to
    - when Hashcode and Equals is called
    */
     
    Set s = m1.entrySet();
    for (Iterator i = s.iterator(); i.hasNext();) {
    Map.Entry me = (Map.Entry) i.next();
    System.out.println(me.getKey() + ":" + me.getValue());
    }
   
    for (Map.Entry<Tuple, Integer> entry : m1.entrySet()) {
    System.out.println(entry.getKey() + ":" + entry.getValue());
    }
    
    for (Object key : m1.keySet()) {
    System.out.println(key.toString() + ":" + m1.get(key));
    }
     
}
}
 
class Tuple {
    Tuple(int i, int j) {
        this.i = i;
        this.j = j;
    }
     
    int i, j;
     
    @Override
    public int hashCode() {
        int k = i + j;
        return k;
    }
     
    @Override
    public boolean equals(Object obj) {   
        if (i == ((Tuple) obj).i && j == ((Tuple) obj).j)
        return true;
        else
        return false;
    }
     
    @Override
    public String toString() {
        return String.valueOf(i).concat(String.valueOf(j));
    }
     
}

```
