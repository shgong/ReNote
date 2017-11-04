
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
- alpha-beta prune

## 17. Encapsulation

- module
  - set of methods that work together to perform some task
  - It is Encapsulated
    - if implementation is hidden
    - accessed only through documented interface
- Why Encapsulation
  - implementation is independent from functionality
  - prevent doug from module internal state change
  - ADT guarantee invariant presearch
  - Teamwork
  - Documentation & maintainbility
- document
  - list all modules
  - for each module, specify its interface
    - behavior comment, parameter and return value
  - unusually or errorous input/circumstance

## 18. Encapsulated list

- list
  - isEmpty
  - insertFront
  - Front
- listNode
  - item
  - next
  - remove
  - insertAfter

## 19. Asymptotic Analysis
## 20. Algorithm Analysis

- Big O Analysis (at least this good)
- Big Omega Analysis (at least this bad)

- Give a set of p points, find their closest to each other
  - Calculate distance between every pair, return minimum
  - p(p-1)/2 pair
  - Time ~ O(p^2) Ω(p^2)
- Matchmaking for w women and m man
  - running time T(w,m) ~ O(wm)
- Array contains n music albums, sort by title, get k album starting with "Best of"
  - bsearch O(logn + k), iterate k from lowerbound
  - no simple expression, k can be 0 or n

## 21. HashTable

- dictionary
- hash table
  - hash huge set of n possible keys into N buckets
  - with a compression function
    - mod function
  - chaining: each bucket reference a linked list of entries
- Load factor of hashTable n/N
  - if n>>N: O(n)
  - otherwise: O(1)
- choices of hash function
  - int
    - mod 4: easily skew
    - (a * hashcode + b) mod p mod N
  - words
    - first 3 letters
    - sum of ascii
    - suppose prime modulus to 127
- store minimax results in hashtable to reduce generation

## 22. Stack and Queue

- Easily implemented with linked list
  - sample parenthesis matching
    - scan through string
    - push lefty onto stack
    - when righty, pop counterpart off stack

## 23. Trees and Traversals

- Tree
- Tree Traversals
  - preorder: node-left-right
    - recursive
  - postorder: left-right-node
    - recursive
  - inorder: left-node-right
    - recursive
  - level order: root-1 level-2 level...
    - dequeue, visit, enqueue children

## 24. Priority Queues

- Priority Queue
  - dictionary that store entries
  - a total order is defined on the keys
  - operations
    - identify or remove entry whose key is lowest
    - any key may be inserted at any time

- Binary Heap: a complete binary tree
  - every row is full except bottom row
  - no children has key less than parent
- example
  - stored in array directly, leave first empty (help index)
  - 2, 5 3, 9 6 7 4
  - mapping of node to indices: level numbering
    - node i's chilren are 2i and 2i+1
    - i's parent is floor(i/2)
- entry insert
  - if not full, put in first free spot from left
  - then bubble up the tree until heap-order property satisfied
    - keep swap with its parent
- remove min
  - fill hole with last entry in the tree
  - still has the property that left, right children are binary heap
  - just need to check top of each to find the min key
    - after swap, need check the branch recursive until it is right

- running times
  - binary heap: min O(1), insert O(logn), remove O(logn)
  - sorted list: min O(1), insert O(n),    remove O(1)
  - unsort list: min O(n), insert O(1),    remove O(n)
- why logn: tree levels


## 25. Binary Search Tree

- Ordered dictionary
  - dictionary in which keys have total order, like heap
- Binary search tree implementation
  - every key in left subtree <= x's key
  - every key in right subtree >= x's key
  - inorder traversal will be sorted order
- skew O(n), balanced O(logn)

## 26. Balanced Search Trees

- BST
  - 2,3,4 Tree
  - SPlay Tree
  - AVL Tree
  - Red-black Tree
  - B Tree

### 2-3-4 B-Tree

- B Tree
  - multiple element per node
  - less rebalance operation needed
  - but waste some space as node is not full
- Property
  - every node has 2,3 or 4 children, except leaves
  - every node stores 1,2 or 3 keys
    - 3 keys means 4 children
    - subtree are placed according to greater/smaller keys
- Insert
  - walk down tree in search of k
  - if finds k, proceeds to k's "left" child and continue
  - when reach leaf, insert it
  - if leaf exceed 3 item, how to restructure?
    - break up by kicking middle key up
      - recursive kick up
      - root node (20 - 40 - 50) will kick into a new level (40)
- Remove
  - find key k
  - if in leaf, remove it
  - if internal node, replace with next higher key
    - that key will be in leaf node,
  - what if leaf has no items, how to restructure?
    - try steal from a sibling, rotate
    - if no adjacent has >1 keys, try steal key from parent, fusion with sibling
    - if parent is root, merge with sibling, tree depth decrease

## 27. Graph

- Graph representations
  - adjacency matrix: time-efficient for complete graph
  - adjacency list: sparse, time & space-efficient
- Graph Taaversal
  - DFS: pre-order traversal
  - BFS: level-order traversal
    - each vertex has boolean visited field
- find shortest path: BFS, distance

## 28. Weighted Graph

- adjacency matrix: array of ints, doubles
- adjacency list: each listnode includes a weight

- Famous problem: Minimum spanning tree
  - collect all nodes with least total distance, optimization
- Kruskol's algorithm
  - Spanning tree, or a forest if G is not connected
  - start with empty tree T
  - sort edges by weight, low to high
  - iterate through edges, for each edge (u,w)
    - add (u,w) to T, if u & w are not connected
- why it is safe: edge is sorted


## 29. Sorting I

### Insertion sort

- always ensure list is sorted
- algorithm
  - start with empty list S
  - for each item in set
    - insert x into S in sorted order
- complexity
  - S is linked list, O(n) to find position
  - S is array, O(logn) to find, but O(n) to shift orders
    - it is a in-place sort, use very little memory
  - S is balanced search tree
    - O(logn) guaranteed
    - but this is not usual insertion sort

### selection sort

- worse, always O(n^2)
- algorithm
  - start with empty list S
  - loop n times
    - find smallest item
    - remove and add to S
- complexity
  - S is array or linked list, both O(n^2)
    - also in-place, swapping

### heap sort

- selection sort using heap
- algorithm
  - toss all items onto heap
  - bottom up heap
    - from bottom, swap to make it a heap
  - loop n times
    - remove min
    - append to S
- complexity
  - removeMin is O(logn)
  - in-place: maintain heap backward in an array
    - last item is the root
    - remove min, after bottom up, first space is empty
    - keep doing, it is sorted
  - excellent for arrays
  - clumsy for linked lists

### merge sort

- you can merge two sorted list into one in linear time
- algorithm
  - while (Q1 Q2 not empty)
    - check both front item
    - move small one into Q
  - concat remaining non-empty queue
- complexity
  - excellent for linked lists
  - clumsy for arrays, not in-place, need twice much memory
  - recursive divide and conquer
    - split log(n) times, merge O(n)

## 30. Sort II: quicksort

- recursive divide and conquer
- mergesort is simple divide, complex merge
  - quicksort is complex divide, simple merge
- fastest comparison-based sort
  - worst case O(n^2)
  - virtually always O(nlogn) in practice
- algorithm
  - pick a special pivot item
  - partition with pivot item, I1, I2, V
    - when linked list, use three part splits
      - slow to go through same item
    - when array, equal item can go to one partition
      - because array sort, no place to put extra items
  - simply concat S1, V, S2
- complexity
  - need choose pivot well
    - if already sorted, use first item
      - each time skew split O(n^2)
  - how to choose
    - randomly select an item
    - on average 1/4, 3/4 split

- implemented with array
  - sort item a[lo] to a[hi]
  - choose pivot v, swap with last item a[hi]
  - let i=lo-1, j=hi
    - keep all item left of i <= pivot
    - keep all item right of j > pivot
  - loop until i>=j
    - advance i until > pivot
    - decrease j until <= pivot
    - swap

## 31. Disjoint Sets

- No item is in more than one set
- Every item in exactly one set
- Support two operations
  - union: merge 2 sets into one
  - find: takes an item and tell what set it is in

### List-based disjoint sets

- Each set reference list of items in the set
- Each item reference set that contains it
  - find: O(1) time
  - union: slow, O(n)

### Tree-based disjoint sets

- Each set is maintained as a tree, general tree
  - data structure is a forest
  - every item is initially root of its own tree
  - tree identity of each set recorded at the root
- Union: O(1) time
  - make root of one set child of another root
  - record size of tree, put smaller tree under larger tree
- Find: slower
  - proportional to item's depth

### Union Find Set

- Array, number from zero
  - record parent of each item
  - if has no parent, record size of its tree, using negative number
  - initially every one is -1
- Union
  - if a[root2] < a[root1]
    - a[root2] += a[root1]
    - a[root1] = root2
  - vice versa
- Find
  - if a[x] < 0
    - return x
  - else
    - a[x] = find(a[x])
    - return a[x]

  - performance
    - Union: O(1)
    - Find: O(logn) at worst case, average close to constant


## 32. Sorting III

### Selection

- Find kth smallest key in the list
  - or find index k-1 if list is sorted
  - application: find the median of a set, k=(n-1)/2
- Quick Select, modifies quicksort
  - start w/ unsorted list I
  - choose pivot v from I
  - partition into I1, Iv, I2
  - only look into one of them when selection
- complexity
  - O(n) average


### lower bound on comparison-based sorting

- How to prove any other algorithm is slower than nlogn?
- n numbers
  - permutation of orders: n!
  - n! >= (n/2)^(n/2)
  - log(n!) >= n/2 * log(n/2)
- Comparison-based sorting
  - all decisions based on comparing keys
  - sorting algorithm must generate a different sequence of t/f answers
  - d question generates 2^d sequence of answers
  - 2^d >= n!
  - d >= nlog(n)
- faster algorithms make q-way decision for large q


### linear-time sorting

- Bucket sort
  - when keys are in small ranges, like 0 ~ q-1
- Array of q queues
  - put into queues, concat the queue
  - O(q) to intialize and concat buckets
  - O(n) to put items in buckets
  - O(q+n)
- Stable
  - items with equal keys come out in the same order went in
  - stable
    - insertion
    - selection
    - merge sort
    - list based qsort
  - not stable
    - array based qsort
    - heap sort without secondary key


## 33. Sorting V

### Counting sort

- if items are keys, no associated values
  - no queues
  - count copies of each key
- with associated values
  - no need to store a queue
  - just count
  - after the count
    - do a scanLeft
    - counts[i] tells first index to put items with key
  - revisit the original queue, copy to designated location

- Bucket sort and counting sort
  - both take O(q+n) time
    - counting sort for array
    - bucket sort for linked list
  - if q (number of possible keys) <= n



### Radix sort

- What is q >> n?
  - like sort 1000 items in range 0, 9999999999

- Provide q = 10 buckets
  - make sure all number have the same digit (prepend 0)
  - put into buckets using first digit
    - within bucket is not sorted
  - sort queue recursively on second digit ...
    - but smaller subsets will be sorted inefficiently

- Clever idea
  - keep numbers in one pile throughout sort
  - sort last digit first, then next-to-last
  - this works because bucket and counting sorts are stable

- In practice
  - faster if we sort 2 digits at a time
  - use q=256 radix, sort one byte at a time
  - each pass inspect log2(q) bits of each key
- running time
  - `O((n+q)*(b/log2(q)) )`
  - time: choose q=n, round down to power of two
  - space: choose q=sqrt(n), round down to power of two


## 34. Splay tree

- Balance Binary Search Tree
- All operations take O(logn) on average
  - but some operation can be very slow, like O(n) at worst case
    - it rebalance itself whenever has slow operation (like UFS)
  - If satisfy conditions
    - any sequence of k ops, starting from empty tree, within n items
    - will take O(klogn) at worst-case time
- Much easier to program than 2-3-4 trees
- Fast access to frequent items O(1)
- Most used data structure in industry


- Tree Rotations
  - many algorithm keep balance by rotation
  - AVL, Red Black, Splay

- Example of Rotations
  - rotate right: Y[X[A,B], C] => X[A, Y[B,C]]
  - rotate left: X[A, Y[B,C]] => Y[X[A,B], C]

- Operation
  - Find(k)
    - begin like ordinary BST
    - walk down tree to entry k, or dead end
    - let X be node where search ended, whether it contains k or not
    - Splay X up through a sequence of rotations, so X becomes root
  - First(k)
    - find entry with min/max key, splay it to root
  - Insert(k,v)
    - insert new entry into the tree like BST
    - Splay new node to the root
  - Remove(k)
    - let X be node removed from tree
    - Splay X's parent to root
    - if X is already root, move greater node to root
      - now X become child, can be removed
      - then Splay X's parent to root
    - if k is not in tree
      - splay node last visited to the root

- Splay
  - Case 1. Zig zag
    - X is left child of right child, or right child of left child
    - rotate X up twice to grandfather
      - G[P[A, X[B,C]] ,D]
      - G[X[P[A,B], C], D]
      - X[P[A,B], G[C,D]]
  - Case 2. Zig Zig
    - left child of left child, or right child of right child
    - rotate grand parent first, then rotate parent
      - G[P[X[A,B], C], D]
      - P[X[A,B], G[C,D]]
      - X[A, P[B,G[C,D]]]
  - Case 3. Zig
    - when child of root, usually last operation
    - simple rotate
      - P[X[A,B], C]
      - X[A, P[B,C]]

- Why zig-zig rotate grandfather first
  - if insert in order
  - you may build a very unbalanced tree
    - 9-8-7-6-5-4-3-2-1-0
  - find(1)
    - when you do zig-zig
    - rotate two level from left to the right
    - you can get almost half depth of the tree
    - depth d => at most d/2 + 3
  - if use zig-zag
    - you get 1-2-3-4-5-6-7-8-9
    - not rebalanced at all


## 35. Amortized Analysis

- Average time
  - Disjoint set, Hash table, Splay tree

## 36. Randomized Analysis

- Make decision
  - roll of dice

## 37. Expression Parsing

- application of stacks
  - infix: 3+4*7
  - prefix: + 3 * 4 7
  - postfix: 3 4 7 * +

### Postfix
- Evaluated by a stack of numbers
- read expression, left to right
  - number: push onto stack
  - operator: pop top 2 number off stack

###  Infix
- first convert to postfix
- Precedence rules
  - first ^ then * / then + -
- Association rule
  - left: + - * /
  - right: ^
    - 2^3^5 = 2^(3^5)
    - because (2^3)^5 = 2^(3*5)

- read expression, left to right
  - number: print out
  - operator: put on stack until an operator with lower or equal precedence appears (for exponential, strictly lower), where upon we pop it and print it


## 38. Garbage Collection

- JVM use hidden data structure to manage memory

- Object that you can not use become garbage
  - Object is live, if reachable from root
    - referenced by root
    - referenced by another live object
  - each object has visited tag

- Memory Address
  - array of bytes with address
  - local variable -> name a memory location

### Mark and Sweep GC
- Java has DS to keep track of free & allocated memory
- 2 phase
- Mark: DFS from every root, mark all live object
- Sweep: each object not marked has its memory reclaimed
  - compacting GC move objects during sweep phase
  - fragmentation: tendency of free memory broken into small pieces

### Copying GC
- Faster than mark & sweep GC, only have one phase (pro)
- memory divided into 2 spaces, old space and new space, only half is available (con)
- DFS to find live objects, if in old space, immediately moves to new space,  next time, new & old swap

### Generational Garbage GC
- Research found
  - most objects have short lifetimes
  - a few live very long
- Generational GC have 2 or more generation with different sizes
  - old generations
    - Mark & Sweep GC
    - Infrequently, so use compact one
  - young generation
    - most object die quickly, so use fast one
    - Eden
      - all object born here
      - most die here, some move to survivor space
      - Eden is cleared every run, so super fast
    - Survivor space
      - if survived a lot, get tenured
      - moved to old generation

- Problems
  - Older generation don't run as frequent
    - What if old generation reference young generation?
    - how do you know it is referenced, without DFS old generation?
  - Keep track of all reference from old to young generation
    - add them to the roots of young generation's copying GC


## 39. Augmenting data structure

- you want something like BST, with
  - Ordered dictionary
  - find in O(1) time
- Solution: use two data structure
  - Splay tree + Hash table

- Database of employee records
  - Prius owners: list of employee
  - Pot smokers: list of employee
  - Cry babies: list of employee
  - how to fire all tree huggers in O(n) time?
    - keep employee back reference to each tree

- Splay tree with node information
  - record size and height of each subtree, for each node
  - when rotate, calculate size, height with some formula
- Query: how many keys between x and y
  - in O(nlogn) amortized time
  - search for X, Y
    - don't go to subtree, just look up the number
    - simply adds up ranged subtrees


- Course Evaluation
  - Good to have specific advice
  - Once got advice: never again wear brown jacket with jeans
    - most useful advice ever, implement immediately
  - President on the radio this morning
    - announcing a new English improvement plan
    - all those important American words difficult to spell become easier to spell than ever
    - as of today, abysmal is spelled as G-R-E-A-T
