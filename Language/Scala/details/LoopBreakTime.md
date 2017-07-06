```scala
package looptimer
import scala.util.control.Breaks._

/**
  * Created by bgong001c on 8/10/2016.
  */
class Timer {
  var start = System.currentTimeMillis()
  var step = start
  def get: Long = {
    val end = System.currentTimeMillis()
    val time = end - step
    step = end
    time
  }
  def stop: Long = System.currentTimeMillis() - start
}

object AllDone extends Exception { }





object LoopTimer {

  def whileTag(n: Array[Int], sum: Int): Unit = {
    var done = false
    var i = 0
    var s = sum
    while (i <= n.length && !done) {
      s -= n(i)
      i+=1
      if (s < 0)done = true;
    }
  }

  def takeIterator(n: Array[Int], sum: Int): Unit = {
    var s = sum
    n.iterator.takeWhile(_ => s > 0).foreach { x => s -= x }
  }


  def functionReturn(n: Array[Int], sum: Int): Unit = {
    var s=sum
    n.foreach{x=>s-=x;if(s<0) return}
  }

  def functionReturn2(n: Array[Int], sum: Int): Unit = {
    var s=sum
    0 to n.length foreach{i=>s-=n(i);if(s<0) return}
  }

  def scalaBreak(n: Array[Int], sum: Int): Unit = {
    var s = sum
    breakable {
      for (i <- n) {
        s-=i
        if (s<0) break
      }
    }
  }

  def scalaBreak2(n: Array[Int], sum: Int): Unit = {
    var s = sum
    breakable {
      for (i <- 0 to n.length) {
        s-=n(i)
        if (s<0) break
      }
    }
  }


  def exception(n: Array[Int], sum: Int): Unit = {
    var s = sum
    try {
      for (i <- n) { s -= i; if (s<0) throw AllDone }
    } catch {
      case AllDone =>
    }
  }

  def exception2(n: Array[Int], sum: Int): Unit = {
    var s = sum
    try {
      for (i <- 0 to n.length) { s -= n(i); if (s<0) throw AllDone }
    } catch {
      case AllDone =>
    }
  }

  def tailRecursion(n: Array[Int], sum: Int): Unit = {
    var s = sum
    def addTo(i: Iterator[Int], max: Int) {
      s -= i.next();
      if (s>0 && i.hasNext) addTo(i,max)
    }
    addTo(n.iterator,sum)
  }

  def tailRecursion2(n: Array[Int], sum: Int): Unit = {
    var s = sum
    val max = n.length
    def addTo(i: Int) {
      s -= n(i)
      if (s>0 && i<max) addTo(i+1)
    }
    addTo(0)
  }

  def test() = {
    // which is faster? to stream takewhile or break?
    val timer = new Timer()
    val n = Array.range(0, 10000000)
    val sum = 100000000
    val loops = 10000

    timer.get

    1 to loops foreach { _ => whileTag(n,sum) }
    println("whileTag: %s ms".format(timer.get))

    1 to loops foreach { _ => takeIterator(n,sum) }
    println("takeIterator: %s ms".format(timer.get))

    1 to loops foreach { _ => functionReturn(n,sum) }
    println("funReturn: %s ms".format(timer.get))

    1 to loops foreach { _ => functionReturn2(n,sum) }
    println("funReturn2: %s ms".format(timer.get))

    1 to loops foreach { _ => exception(n,sum) }
    println("exception: %s ms".format(timer.get))

    1 to loops foreach { _ => exception2(n,sum) }
    println("exception2: %s ms".format(timer.get))

    1 to loops foreach { _ => scalaBreak(n,sum) }
    println("scalaBreak: %s ms".format(timer.get))

    1 to loops foreach { _ => scalaBreak2(n,sum) }
    println("scalaBreak2: %s ms".format(timer.get))

    1 to loops foreach { _ => tailRecursion(n,sum) }
    println("tailRecursion: %s ms".format(timer.get))

    1 to loops foreach { _ => tailRecursion2(n,sum) }
    println("tailRecursion2: %s ms".format(timer.get))
  }

  def main(args: Array[String]): Unit = {
    test()
  }

}
```



takeStream: 13567 ms
funReturn: 776 ms
exception: 1141 ms
breaks: 1543 ms
whileTag: 74 ms
tailRecursion: 724 ms

```
Ironically the Scala break in scala.util.control.Breaks is an exception:

def break(): Nothing = { throw breakException }
```

