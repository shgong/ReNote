ReflectionCampanion.md

```
def companion[T](name : String)(implicit man: Manifest[T]) : T = 
    Class.forName(name + "$").getField("MODULE$").get(man.erasure).asInstanceOf[T]

val result = companion[SomeTrait]("SomeObject").someMethod
```


```
scala> def currentMethodName() : String = Thread.currentThread.getStackTrace()(2).getMethodName
currentMethodName: ()String

scala> def getMeASammy() = { println(currentMethodName()) }
getMeASammy: ()Unit

scala> getMeASammy()
getMeASammy
```





You need to get an instance mirror for your module, on which you can reflect the method.

def dynamicInvocation( y: Integer) = {
  val m = ru.runtimeMirror(getClass.getClassLoader)
  val module = m.staticModule("thePackage.object" + y)
  val im = m.reflectModule(module)
  val method = im.symbol.info.decl(ru.TermName("theMethod")).asMethod

  val objMirror = m.reflect(im.instance)
  objMirror.reflectMethod(method)("test")
}




name := "FPinScala"

version := "1.0"

scalaVersion := "2.11.8"

// https://mvnrepository.com/artifact/org.scala-lang/scala-reflect
libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.12.0-M5"



# playing with scala reflection

scala is an object-functional programming language, designed to concisely express solutions in an elegant, type-safe and lightweigt manner. scala compiles to java bytecode, so its code runs glueless on the JVM and java libraries can be freely called from scala and vice-versa.

i started using scala about 2 months ago and i’m still impressed so far. simple functionality can be written in a very clean and elegant way and thanks to popular frameworks like play! or lift, the language comes with ‘batteries included’ for web development.

in scala 2.10 the new reflection API was introduced. this API provides the program the ability to inspect and possibly even modify itself at runtime. 

so, let’s see what we can do with it. first of all the API has to be included:

```
import scala.reflect.runtime.universe._
```

objects and classes can now be instantiated at runtime using an appropriate runtime mirror. by obtaining a mirror from the current classloader, all types and classes are available. invoking the instance of classes and objects differs a little bit, since objects in scala are treated like singleton objects.

```
def getObjectInstance(clsName: String): ModuleMirror = {
    val mirror = runtimeMirror(getClass.getClassLoader)
    val module = mirror.staticModule(clsName)
    mirror.reflectModule(module).instance      
}

def getClassInstance(clsName: String): Any = {
    val mirror = runtimeMirror(getClass.getClassLoader)
    val cls = mirror.classSymbol(Class.forName(clsName))
    val module = cls.companionSymbol.asModule
    mirror.reflectModule(module).instance
}
```

so, assuming you already have an object like this:
```
package models.entities

object User {
  def findByUsername(username: String) = {
    // database or whatever magic is done here ;)
  }
}
```

you can now invoke a method of this object at runtime:
```
def invokeMethod(param: String) = {
    val im =mirror.reflect(getObjectInstance("models.entities.User"))
    val method = im.symbol.typeSignature.member(newTermName("findByUsername")).asMethod
    im.reflectMethod(method)(param)
}
```



    def mirrorMethod(name:String) = {
      val mirror = runtimeMirror(getClass.getClassLoader)
      val im =mirror.reflect(
        mirror.reflectModule(
          mirror.staticModule("looptime.LoopTimer")
        ).instance
      )
      val method = im.symbol.typeSignature.member(newTermName("findByUsername")).asMethod
      im.reflectMethod(method)(name)
    }
    