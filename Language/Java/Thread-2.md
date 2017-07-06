
# Thread Usecase

## 1. Terminology

- Concurrency: ability to excute computations simultaneously by distribution among CPU cores or machines
- Process: execution environment from OS ( JVM in Java )
- Thread: a light weight process, each with its own counter, stack and local variables, multi thread to decrease CPU idle time
- Java Thread
    + Every program executed within the main thread (at least one)
        * if no thread started, getName() will return "main"
    + Properties
        * id (long): unique within JVM
        * name (String)
        * priority (int)
        * state (java.lang.Thread.State)
        * threadGroup (java.lang.ThreadGroup): interrupt or set priority as a whole
    + State
        * NEW: not yet started
        * RUNNABLE: A thread executing in the Java virtual machine
        * BLOCKED: blocked waiting for a monitor lock
        * WAITING: waiting indefinitely for another thread to perform a particular action
        * TIMED_WAITING: Awaiting for another thread to perform an action for up to some time
        * TERMINATED: A thread that has exited
- Atomic
    + basic operations that make sure concurrency threads see the same value
    + Read/write to reference & primitive variable (except long/double)
    + Read/write for all `volatile` variable

```java
// Access current thread
long id = Thread.currentThread().getId();
String name = Thread.currentThread().getName();

// set priority
setPriority(int)
setPriority( Thread.MAX_PRIORITY ) // values may differ

```



## 2. Creation

Thread cannot be started twice.

### 2.1 Extend java.lang.Thread class

```java
public class MyThread extends Thread {

    public MyThread(String name) {
        super(name);
    }

    @Override
    public void run() {
        System.out.println("Executing thread "+Thread.currentThread().getName());
    }

    public static void main(String[] args) throws InterruptedException {
        MyThread myThread = new MyThread("myThread");
        myThread.start();
    }
}
```

### 2.2 Implement java.lang.Runnable interface
```java
public class MyRunnable implements Runnable {

    public void run() {
        System.out.println("Executing thread "+Thread.currentThread().getName());
    }

    public static void main(String[] args) throws InterruptedException {
        Thread myThread = new Thread(new MyRunnable(), "myRunnable");
        myThread.start();
    }
}
```



## 3. Thread Type

### 3.1 Daemon Thread

- a thread whose execution state is not evaluated when the JVM decides if it should stop or not. 
- The JVM stops when all user threads (not daemon threads) are terminated. 
- daemon threads can be used to implement monitoring functionality 

```java
public class Example {

    private static class MyDaemonThread extends Thread {

        public MyDaemonThread() {
            setDaemon(true); 
            // only call before started
            // otherwise raise IllegalThreadStateException
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new MyDaemonThread();
        thread.start();
    }
}
// The example application above terminates even though the daemon thread is still running in its endless while loop.
```

###  3.2 shutdown hook

- a thread executed when JVM shuts down
```java
Runtime.getRuntime().addShutdownHook(new Thread() {
    @Override
    public void run() {
    }
});
```



## 4 Thread Methods

### 4.1 Sleep()

- sleep milliseconds is not exact time, Not for real-time processing
- Interrupt: wake up sleeping thread
- check if interrupted: `Thread.interrupted()`

```java
public class InterruptExample implements Runnable {

    public void run() {
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName()+"Interrupted");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread myThread = new Thread(new InterruptExample(), "myThread");
        myThread.start();

        System.out.println(Thread.currentThread().getName()+" Sleeping 5s");
        Thread.sleep(5000);

        System.out.println(Thread.currentThread().getName()+" Interrupting");
        myThread.interrupt();
    }
}

```

#### Exception

- InterruptedException should be handled by run()
- if unchecked exception stopped by JVM
    + can catch by registering instance implements `UncaughtExceptionHandler`
    + JVM query ` Thread.getUncaughtExceptionHandler()`
    + and invoke `uncaughtException` method with thread/exception as argument

### 4.2 Join()

- wait for another thread's termination

```java
Thread thread = new Thread(new Runnable() {
    @Override
    public void run() {
    }
});
thread.start();
thread.join();
```


### 4.3 Stop()

Use a volatile reference pointing to the current thread that can be set to null by other thread.
```java
private static class MyStopThread extends Thread {
    private volatile Thread stopIndicator;

    public void start() {
        stopIndicator = new Thread(this);
        stopIndicator.start();
    }

    public void stopThread() {
        stopIndicator = null;
    }

    @Override
    public void run() {
        Thread thisThread = Thread.currentThread();
        while(thisThread == stopIndicator) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }
}
```

do NOT call stop() method: this causes the thread to unlock all monitors it has acquired. If released locks protect an inconsistent state object, this state gets visible to all other threads. 

### 4.4 notify(), wait(), notifyall()

- used to implement producer/consumer scenario
- wait() can be woken up, but sleep() cannot
- when queue is empty, worker thread can free the processor by acquiring lock on queue by calling `wait()`
- when producer put new task into queue and call `notify()` or `notifyall()` to wake up waiting threads

### 4.5 yield()

gives the scheduler a hint that the current thread is willing to free the processor.


## 5. Synchronized

- Synchronization
    + used when need exclusive access to resource, like static value/file
    + built around intrinsic lock / monitor
        *  every object has an intrinsic lock
        *  a thread own the intrinsic lock during that time
    + check by `Thread.holdsLock(Object)`

- Synchronized Method
    +  A synchronized method acquires the intrinsic lock for that method’s object and releases it when the method returns/exception.
   + constructor method can not be synchronized

- Synchronized Statement
    + must specify the object that provides the lock
    + can sync two changes, but not other objects' method
    + also useful for improving concurrency with fine-grained sync
        * two independent fields to sync
        * create two object solely to provide locks
    + Reentrant Synchronization
        + thread can aquire a lock it already owns more than once
        + allow sync code invoke a method also contains sync code
        
```java
public void addName(String name) {
    synchronized(this) {
        lastName = name;
        nameCount++;
    }
    nameList.add(name);
}
```

```java
public class MsLunch {
    private long c1 = 0;
    private long c2 = 0;
    private Object lock1 = new Object();
    private Object lock2 = new Object();

    public void inc1() {
        synchronized(lock1) {
            c1++;
        }
    }

    public void inc2() {
        synchronized(lock2) {
            c2++;
        }
    }
}
```

## 6. Thread Example

### 6.1 Double Checked Singleton 

```java
// Broken under Java 1.4 and earlier semantics for volatile
class Foo {
    private volatile Helper helper = null;
    // must be volatile, or may return before fully initialized
    public Helper getHelper() {
        Helper result = helper;
        if (result == null) {
            synchronized(this) {
                result = helper;
                if (result == null) {
                    helper = result = new Helper();
                }
            }
        }
        return result;
    }
}

// If help holder can be static
// Correct lazy initialization in Java
class Foo {
    private static class HelperHolder {
       public static final Helper helper = new Helper();
    }

    public static Helper getHelper() {
        return HelperHolder.helper;
    }
}
```

## 7. Locking Situations

### 7.1 Deadlock

- situation: two threads waiting for each other, when locked one resource, thread tries to acquire another lock on other resource
- prevention
    + keep synchronized block short
    + don't call method might get blocked inside sync block
    + don't invoke other object method while holding a lock
- detection
    + monitored and modelled locks as a directed graphy
    + can search for deadlock threads

### 7.2 Livelock

-  two or more threads block each other by responding to an action that is caused by another thread.
-  example: both threads concurrently try to acquire the first, one succeed, the other succeed in acquire the second. Then they release all locks and try again from start.


### 7.3 Thread Starvation

- low priority thread performs a long enduring computation, which make some resource unavailable for long time.

### 7.4 Fair lock

- Take waiting time of threads into account when chooseing next thread
- example implementation: `java.util.concurrent.locks.ReentrantLock`

### 7.5 Lock contention

- 2+ threads compete in acquisition of a lock
- scheduler has to decide 
    + wait sleeping and perform a context switch
    + sometimes busy-waiting is more efficient
- how to reduce
    + scope of lock is reduced
    + number of times a lock is acquired is reduced (lock splitting)
    + use hardware optimistic locking instead of synchronization
    + avoid synchronization where possible
    + avoid object pooling

```java
UUID randomUUID = UUID.randomUUID();
Integer value = Integer.valueOf(42);
String key = randomUUID.toString();
// move above local code out of synchronization
synchronized (map) { 
    map.put(key, value);
}
```

### 7.5 Lock Splitting

- when one lock is used to synchronize access to different aspects of the same application.
- example
    + uses the keyword synchronized in each method signature
    + each method invocation may cause lock contention
    + split the lock on the object instance into a few smaller locks for each type of statistical data within each method

### 7.6 Lock striping

- different locks for different aspects of the application, lock striping uses multiple locks to guard different parts of the same data structure
- example
    + class `ConcurrentHashMap` from JDK’s `java.util.concurrent` package
    + the Map implementation use internally different buckets to store values
        * bucketed by value's key
        * use different locks to guard different buckets
        * increase performance when work on different buckets


### 7.7 ReadWriteLock

- threads do not have to lock when reading
- lock pair: 
    + one for read-only: multiple threads
    + one for writing: once released show to all read operations

## 8 Problem Situations

### 9.1 Race condition

- a bug due to missing thread synchronization
- example: the incrementation of an integer variable by two concurrent threads.  After this concurrent incrementation the amount of the integer variable is not increased by two but only by one.

### 9.2 Busy waiting

- wait for an event by performing some active computations that let the thread/process occupy the processor, although it could be removed
- example: spend the waiting time within a loop

```java
Thread thread = new Thread(new Runnable() {
    @Override
    public void run() {
        long millisToStop = System.currentTimeMillis() + 5000;
        long currentTimeMillis = System.currentTimeMillis();
        while (millisToStop > currentTimeMillis) {
            currentTimeMillis = System.currentTimeMillis();
        }
    }
});
```

- solution: call ` java.lang.Thread.sleep(long)` instead

### 9.3 Communication between threads

- make sure they are not manipulated by two threads
- can design immutable object
    + final class, all fields are final and private
    + no setter methods, no directly exposing getter methods

### 9.4 ThreadLocal
- As memory is shared between different threads, ThreadLocal provides a way to store and retrieve values for each thread separately.
- can be used to transport information throughout the application without the need to pass this from method to method, eg login/security information


### 9.5 theoretical maximum speed
given n Processors, Parallel Portion P:
time comsupmtion = (1-P) + P/n

### 9.6 CAS (Compare & Swap)

- Compare and Swap
    + an atomic instruction to achive synchronization
    + compare: content of a memory location to given value, if same, modifiy contents of that memory location to the new value
- Use
    + The SDK classes in the package `java.util.concurrent.atomic` like AtomicInteger or AtomicBoolean

### 9.7 why performance improvements for single-threaded applications can cause performance degradation for multi-threaded applications.

- List implementation: holds the number of elements as a separate variable 
- single-threaded applications: size() does not have to iterate over all elements, can return the current number of elements directly. 
- multi-threaded application: the additional counter has to be guarded by a lock as multiple concurrent threads may insert elements into the list. Cost performance when more updates than invocations of the size() operation.

### 9.8 object pooling

- avoid construction of new objects by pooling them
    + improve performance of single-threaded application
    + interchanged cost for object creation
- multi-threading
    + synchronized access to the pool
    + lock contention may outweight construction/gc cost

## 9.9 Collection 

HashMap vs HashTable

- HashTable: all synchronized, thread safe, less efficient
- HashMap: Not thread safe, more efficient

Synchronized implementation
- synchronizedCollection(Collection), 
- synchronizedList(List)  
- synchronizedMap(Map)

## 9.10 Semaphore

- A semaphore is a data structure that maintains a set of permits that have to be acquired by competing threads
- Used to control how many threads access a critical section or resource simultaneously
- acquire() and release() method


## 10. Executor
- Executor interface
    + only one method: `execute(Runnable)`
    + implementation of this interface will execute given instance some time in the future. 
- ExecutorService
    + add additional methods
        * shutdown, await
        * submit instance of Callable
    + submit() when queue full: throws RejectedExecutionException
- ScheduledExecutorService
    + add method for execution in a given point
- construct 5 thread pool
    + static method newFixedThreadPool(int nThreads) allows the creation of a thread pool with a fixed number of threads (the implementation of MyCallable is omitted)

```java
    ExecutorService executorService = Executors.newFixedThreadPool(5);
    Future<Integer>[] futures = new Future[5];
    for (int i = 0; i < futures.length; i++) {
        futures[i] = execupublic static void main(String[] args) throws InterruptedException, ExecutionException {
torService.submit(new MyCallable());
    }
    for (int i = 0; i < futures.length; i++) {
        Integer retVal = futures[i].get();
        System.out.println(retVal);
    }
    executorService.shutdown();
}
```

#### Callable vs Runnable

Runnable defines the method run() without any return value whereas the interface Callable allows the method call() to return a value and to throw an exception.

#### Future

- java.util.concurrent.Future
    + represent results of async computation that is not immediately available
    + thus provide methods to check if finished.
- two get() function



