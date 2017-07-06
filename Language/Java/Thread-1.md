

# Multithreading

Multithreading is a process of executing multiple threads simultaneously. Its main advantage is:

- Threads share the same address space.
- Thread is lightweight.
- Cost of communication between process is low.

## 1 Mode

Preemptive scheduling: the highest priority task executes until it enters the waiting or dead states or a higher priority task comes into existence. 

Time slicing: a task executes for a predefined slice of time and then reenters the pool of ready tasks. The scheduler then determines which task should execute next, based on priority and other factors.

## 2 Java Thread

- An instance of class java.lang.Thread
    like any other object in Java, it contains variables and methods which lives and dies on the heap.

- A Thread of Execution
    individual process in your program, start running from first main()

    + User Thread:
        JVM exit only when all User Thread completed, doesn’t care about Daemon Thread.
    + Daemon Thread:
        low priority thread, provide background support





### 2.1 Create Thread
Class need to extend the Thread class / implements Runnable interface
Override the run() method.

```java
class MyThread extends Thread{
    public void run(){
        System.out.println("Important job running in MyThread");
    }
}
MyThread t = new MyThread();

// another way
//Single runnable instance can be passed to multiple Thread object.

class MyRunnable implements Runnable {
    public void run() {
     System.out.println("Important job running in MyRunnable");
    } 
}
MyRunnable r = new MyRunnable();

// Thread(Runnable threadObj, String threadName);
Thread t = new Thread(r,"MyThread");
```

### 2.2 Thread States: 

- New: New Thread(), remain until start()
- Runnable: become runnable after start()
- Running: after run()
- Waiting/blocked/sleeping
- Dead: after complete task

### 2.3 Thread Priorities

Priority

- helps the operating system determine the order 
- range between MIN_PRIORITY (1) and MAX_PRIORITY (10). 
    + By default,  NORM_PRIORITY (5).
    + high priority threads are allocated more processor time before lower-priority threads


## 3 Methods

```java
//method from thread class
public static void sleep(long millis) throws InterruptedException
public static void yield()  // put back high priority thread to back
public final void join() throws InterruptedException // start when another ends
public final void setPriority(int newPriority)

//method from object class
public final void wait() throws InterruptedException
public final void notify()
public final void notifyAll()
```


##### notify()
The notify() is used to unblock one waiting thread whereas notifyAll() method is used to unblock all the threads in waiting state.

##### wait() vs sleep()
wait() release the lock, sleep() do not

##### join()
causes the currently running threads to stop executing until the thread it joins with completes its task, or run for some milliseconds.

```java
class TestJoinMethod1 extends Thread{  
 public void run(){  
  for(int i=1;i<=5;i++){  
   try{  
    Thread.sleep(500);  
   }catch(Exception e){System.out.println(e);}  
  System.out.println(i);  
  }  
 }  
public static void main(String args[]){  
 TestJoinMethod1 t1=new TestJoinMethod1();  
 TestJoinMethod1 t2=new TestJoinMethod1();  
 TestJoinMethod1 t3=new TestJoinMethod1();  
 t1.start();  
 try{  
  t1.join();  
 }catch(Exception e){System.out.println(e);}  
  
 t2.start();  
 t3.start();  
 }  
}  
// OUTPUT: 123451122334455

// t1.join(1500);  wait first 3 cycles
// OUTPUT: 123141252334455
```


##### Synchronization
Synchronized block is used to lock an object for any shared resource.

if multiple threads try to write within a same file then they may corrupt the data because one of the threads can overrite data or while one thread is opening the same file at the same time another thread might be closing the same file.

So there is a need to synchronize the action of multiple threads and make sure that only one thread can access the resource at a given point in time. This is implemented using a concept called monitors. Each object in Java is associated with a monitor, which a thread can lock or unlock. Only one thread at a time may hold a lock on a monitor.

if loop is inside
```java
synchronized (this / object reference expression) {   
  //code block   
}  
```

#### Shutdown hook 
basically a thread, invoked implicitely before JVM shuts down. So we can use it perform clean up resource.


### Deadlock
a situation when two threads are waiting on each other to release a resource. Each thread waiting for a resource which is held by the other waiting thread.

A Java multithreaded program may suffer from the deadlock condition because the synchronized keyword causes the executing thread to block while waiting for the lock

### Example
```java
class RunnableDemo implements Runnable {
   private Thread t;
   private String threadName;
   
   RunnableDemo( String name){
       threadName = name;
       System.out.println("Creating " +  threadName );
   }

   public void run() {
      System.out.println("Running " +  threadName );
      try {
         for(int i = 4; i > 0; i--) {
            System.out.println("Thread: " + threadName + ", " + i);
            // Let the thread sleep for a while.
            Thread.sleep(50);
         }
     } catch (InterruptedException e) {
         System.out.println("Thread " +  threadName + " interrupted.");
     }
     System.out.println("Thread " +  threadName + " exiting.");
   }
   
   public void start ()
   {
      System.out.println("Starting " +  threadName );
      if (t == null)
      {
         t = new Thread (this, threadName);
         t.start ();
      }
   }

}

public class TestThread {
   public static void main(String args[]) {
      RunnableDemo R1 = new RunnableDemo( "Thread-1");
      R1.start();
      
      RunnableDemo R2 = new RunnableDemo( "Thread-2");
      R2.start();
   }   
}
```

runnable can only implement run()

thread class allow many method(), sleep() notify()
parameters, unisecond, nanosecond


##  future class

A Future represents the result of an asynchronous computation. Methods are provided to check if the computation is complete, to wait for its completion, and to retrieve the result of the computation. The result can only be retrieved using method get when the computation has completed, blocking if necessary until it is ready. Cancellation is performed by the cancel method. Additional methods are provided to determine if the task completed normally or was cancelled. Once a computation has completed, the computation cannot be cancelled. If you would like to use a Future for the sake of cancellability but not provide a usable result, you can declare types of the form Future and return null as a result of the underlying task.

```js
interface ArchiveSearcher { String search(String target); }
class App {
  ExecutorService executor = ...
  ArchiveSearcher searcher = ...
  void showSearch(final String target)
      throws InterruptedException {
    Future<String> future = executor.submit(new Callable<String>() {
        public String call() {
            return searcher.search(target);
        }});
    displayOtherThings(); // do other things while searching
    try {
      displayText(future.get()); // use future
    } catch (ExecutionException ex) { cleanup(); return; }
  }
}
```


## z. practical cases


#### 1. wait for multiple thread: 

If you control the creation of the Threads: use ExecutorCompletionService

Using a `ExecutorCompletionService.poll/take`, you are receiving the Futures as they finish, in completion order (more or less). Using `ExecutorService.invokeAll`, you either block until are all completed, or you specify a timeout after which the incomplete are cancelled.
```java
final ExecutorService pool = Executors.newFixedThreadPool(2);
final List<? extends Callable<String>> callables = Arrays.asList(
    new SleepingCallable("quick", 500),
    new SleepingCallable("slow", 5000));
try {
  for (final Future<String> future : pool.invokeAll(callables)) {
    System.out.println(future.get());
  }
} catch (ExecutionException | InterruptedException ex) { }
pool.shutdown();
```

[src](http://stackoverflow.com/questions/11872520/executorcompletionservice-why-do-need-one-if-we-have-invokeall)

join them one by one
```
for (Thread thread : threads) {
  thread.join();
}
```

#### static synchronized method

At run time every class has an instance of a Class object. That is the object that is locked on by static synchronized methods. (Any synchronized method or block has to lock on some object.)


