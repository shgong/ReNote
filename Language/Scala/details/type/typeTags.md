# typeTags.md

```
import reflect.runtime.universe._
def baseClasses[A <: Any : TypeTag] = typeOf[A].baseClasses
def isSubClassOf[A <: Any : TypeTag, B: TypeTag] = typeOf[A].baseClasses.contains(typeOf[B].typeSymbol)

baseClasses[Null]
// res0: List[reflect.runtime.universe.Symbol] = List(class Null, class Object, class Any)

baseClasses[Int]
// res1: List[reflect.runtime.universe.Symbol] = List(class Int, class AnyVal, class Any)

isSubClassOf[Null, Any]
// res2: Boolean = true
```

TypeTags provide a 1:1 translation of Scala types because they represent the types the compiler knows. Therefore they are much more powerful than plain old Manifests:
