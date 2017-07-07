# Simple Build Tool

## Getting Started

```sh
mkdir -p src/{main,test}/{java,resources,scala}
mkdir lib project target
```

```sbt
name := "MyProject"
version := "1.0"
scalaVersion= "2.10.0"
libraryDependencies += "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"
Add
```

```
|-- build.sbt
|-- lib
|-- project
|-- src
| |-- main
| | |-- java
| | |-- resources
| | |-- scala
| |-- test
| |-- java
| |-- resources
| |-- scala
|-- target
```

src/main/scala/Hello.scala
```scala
object Hello extends App {
 val p = Person("Alvin Alexander")
 println("Hello from " + p.name)
}
case class Person(var name: String)
```

src/test/scala/HelloTests.scala
```scala
import org.scalatest.FunSuite
class HelloTests extends FunSuite {
 test("the name is set correctly in constructor") {
 val p = Person("Barney Rubble")
 assert(p.name == "Barney Rubble")
 }
 test("a Person's name can be changed") {
 val p = Person("Chad Johnson")
 p.name = "Ochocinco"
 assert(p.name == "Ochocinco")
 }
}
```

```sh
sbt run
sbt test
sbt compile
sbt package
```

## Managing Dependencies

```sbt
libraryDependencies += "org.foobar" %% "foobar" % "1.8"
libraryDependencies += "org.scalatest" %% "scalatest" % "latest.milestone" % 
"test"
libraryDependencies += "org.foobar" %% "foobar" % "latest.integration"
logLevel := Level.Debug
```

## sbt-assembly

project/plugins.sbt
```sbt
resolvers += Resolver.url("artifactory",
url("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))↵
(Resolver.ivyStylePatterns)
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.8.4")
```

build.sbt
```sbt
import AssemblyKeys._
// sbt-assembly
assemblySettings
```


shell
```
sbt assembly
sbt assemblyDependenciesJarsBlaBla
```

## Publishing Your Library

build.sbt
```
publishTo := Some(Resolver.file("file", new File("/Users/al/tmp")))
```

shell
```
sbt publish
sbt publish-local
```