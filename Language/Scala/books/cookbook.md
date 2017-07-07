
# Scala Cookbook

## Regex

Finding Patterns in String

```scala
val nunmPattern = "[0-9]+".r
val address = "123 Main Street Suite 101"
val match1 = numPattern.findFirstIn(address)  // Some(123)
val match2 = numPattern.findAllIn(address) // List(123,101)

// replaceAll, replaceFirst
```

Extract Pattern
```scala
val pattern = "([0-9]+) ([A-Za-z]+)".r
val pattern(count, fruit) = "100 Bananas"

// scala.util.matching.Regex = ([0-9]+) ([A-Za-z]+)
// count: String = 100
// fruit: String = Bananas
```


## File & Process

```scala

val lines = Source.fromFile("/Users/blabla").getLines

val s = Source.fromString("foo\nbar\n")
printLines(s)
```

LoanPattern
```scala
object Control {
    def using[A <: { def close(): Unit }, B](resource: A)(f: A => B): B =
        try {
            f(resource)
        } finally {
            resource.close()
        }
}

// usage
object TestUsing extends App {
    import Control._
    using(io.Source.fromFile("example.txt")) { source => {
        for (line <- source.getLines) {
            println(line)
        }
    }}
}
```

## Write File

```scala
// PrintWriter
import java.io._
val pw = new PrintWriter(new File("hello.txt" ))
pw.write("Hello, world")
pw.close

// FileWriter
val file = new File(canonicalFilename)
val bw = new BufferedWriter(new FileWriter(file))
bw.write(text)
bw.close()
```

For instance, while both classes extend from Writer, and both can be used
for writing plain text to files, FileWriter throws IOExceptions, whereas PrintWriter
does not throw exceptions, and instead sets Boolean flags that can be checked.
There are a few other differences between the classes

%LET USER_ID=__WFA_HDFS_USER;


## Executing External Commands

To execute external commands, use the methods of the scala.sys.process package.

There are three primary ways to execute external commands:
• Use the ! method to execute the command and get its exit status.
• Use the !! method to execute the command and get its output.
• Use the lines method to execute the command in the background and get its result
as a Stream.

This recipe demonstrates the ! method, and the next recipe demonstrates the !!method.
The lines method is shown in the Discussion of this recipe.

To execute a command and get its exit status, import the necessary members and run
the desired command with the ! method


```scala

import sys.process._
"ls -al".!
val exitCode = "ls -al".!

import java.io.File
("ls -al" #> new File("files.txt")).!
("ps aux" #> new File("processes.txt")).!

// pipe them together
("ps aux" #| "grep http" #> new File("http-processes.out")).!


```

## Web

MongoDB => Casbah