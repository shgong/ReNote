# Java Collection Notes

# 1. Terminology

- Collection
    + A collection (container) is an object that groups multiple elements into a single unit.
    + Benefits
        * Improve program quality & speed
        * Increase chances of reusability
        * Decrease programming effort

- Collection Interface
    + root interface in collection hierarchy
        * or iterable interface, as collection interface extends it
    + Collections Class, both in java.util package

- Synchronized Classes
    + Stack, Properties, Vector and Hashtable
    + convert to synchronizedcollection: `Collections.synchronizedCollection(Collection collectionObj) `

- ReadOnly Collections
    + Collections.unmodifiableMap(Map m)
    + Collections.unmodifiableList(List l)
    + Collections.unmodifiableSet(Set s)


# 2. Interface Hierarchy

List interface: ArrayList, Vector, LinkedList
Set interface: HashSet, TreeSet

Map do not extend Collection interface: require both key value, not compatible

# 3. Versus

- Set vs List
    + Set is unordered, contain only unique elements
- Map vs Set
    + Map has unique keys and values
- Iterator vs Enumeration
    + Iterator has remove() method
    + Enumeration behaves like read-only interface 
- Queue vs Stack
    + FIFO, LIFO
- Array vs ArrayList
    + Array is static in size, can contain primitives
    + ArrayList is dynamic, find next memory period when about to full
- ArrayList vs Vector
    + Vector is synchronized, old class, thread safe
    + when needed, Vector increase capacity 100%, ArrayList 50%
- LinkedList vs ArrayList
    + doubly linked list implementation vs resizable array 
    + ArrayList O(1) for get(), Linkedlist O(1) for Insert/Delete
    + linkedlist has reverse traverse method `descendingIterator() `
- HashSet vs TreeSet
    + HashSet maintain elements in random order, not sorted
    + HashSet can store null object
- HashMap vs Hashtable/ConcurrentHashMap
    + HashMap allow null key and null values
    + HashMap is not synchronized
- Iterator vs ListIterator
    + forward direction, dual direction


||Comparable | Comparator
|-|-----|-----|
|Provide|  only one sort of sequence.|  multiple sort of sequences.
|Method Name | compareTo() | compare().
|Package|java.lang package. |   java.util package.
|If we implement interface| actual class is modified. |Actual class is not modified.


# 4. Interfaces

- Collections Class
    + reverse(): built in reverse method
- Arrays
    + asList(): convert string[] to list
- Queue
    + remove(): remove head object, throw NoSuchElementException when empty
    + poll(): same, return null when empty
    + peek(): return but not remove, also null when empty
- HashMap
    + Key: needs to implement equals() and HashCode() 
        * equals ＝> same hashcode
        * hash collision ＝> linked list
    + Implementation
        * call hashCode() on key object, use return Value to find a bucket
        * bucket is the linked list effectively . Its not a LinkedList as in a java.util.LinkedList - It's a separate (simpler) implementation just for the map
        * traverse through linked list , comparing keys in each entries using keys.equals() until it return true
    + remove()
        * Map.Entry object 
            + static nested class stores in (hash,key,value,bucketindex)
            + need hashvalue & bucketindex to access Entry object
        * call RemoveEntryForKey(key) method
            - find in bucket linked list
            - remove a list node when found
- HashTable 
    + legacy class in JDK 1.1, similar to Synchronized Map
    + lock whole table, compared to ConcurrentHashMap
    + performance: Hashtable  <  Collections.SynchronizedMap  <  ConcurrentHashMap  <  HashMap
- HashSet
    + internally use HashMap to maintain uniqueness
    + HashMap put function()
        * return null , if key is unique and added to the map
        * old Value of the key , if key is duplicate
    + HashSet add() return `map.put(e,PRESENT)==null`
    + HashSet clone() use shallow copy, only reference is cloned
    + why not concurrenthashset()?
        * Collections.newSetFromMap(new HashMap<Object,Boolean>())
- BlockingQueue
    + jdk 1.5
    + support operations that wait for the queue to become non-empty when retrieving an element , and wait for space to become available in the queue when storing an element
    + designed for the producer-consumer problems.
    + BlockingQueue implementations are thread-safe 
- TreeMap
    + use Red-Black tree to sort element
        * color of every node is either red or black
        * root = black
        * red node can not have red neighbor
        * all path from root to null has same black nodes
    + performance
        * log(n) cost for get put remove()
        * vs HashMap: constant time (but keys are not sorted)


### Others

Suppose there is an Employee class. We add Employee class objects to the ArrayList. Mention the steps need to be taken , if I want to sort the objects in ArrayList using the employeeId attribute present  in Employee class. 

a. Implement the Comparable interface for the Employee class and now to compare the objects by employeeId we will override the emp1.compareTo(emp2)
b. We will now call Collections class sort method and pass the list as argument , that is , Collections.sort(empList)  
