# ImplicitTypeClass.md

## GOF Adapter pattern

adapter case class to interface of a LabelMaker

```scala
case class Address(no: Int, street: String, city: String, state: String, zip:String)

trait LabelMaker[T] {
    def toLabel(value: T): String
}

// adapter class
case class AddressLabelMaker extends LabelMaker[Address] {
    def toLabel(address: Address) = {
        import address._
        s"""$no $street, $city, $state - $zip"""
    }
}

AddressLabelMaker().toLabel(Address(100, "Monroe Street", "Denver", "CO", "80231"))
```

- this leads to an identity crisis of the Address instance itself
- Object adapters don't compose, and class variant of adapter pattern is worse

## Type Class

In Scala we have first class module support through object syntax

```scala
object LabelMaker {
  implicit object AddressLabelMaker extends LabelMaker[Address] {
    def toLabel(address: Address): String = {
      import address._
      s"""$no $street, $city, $state - $zip"""
  }
}

def printLabel[T](t: T)(implicit lm: LabelMaker[T]) = lm.toLabel(t)

// or using the Scala 2.8 context bound syntax, we can make the implicit argument unnamed ..
def printLabel[T: LabelMaker](t: T) = implicitly[LabelMaker[T]].toLabel(t)

// now we can call
printLabel(Address(100, "Monroe Street", "Denver", "CO", "80231"))

```


## Compare to Haskell implmentation
```haskell
class LabelMaker a where
    toLabel :: a -> String

instance LabelMaker Address where
    toLabel (Address h s c st z) = show(h) ++ " " ++ s ++ "," ++ c ++ "," ++ st ++ "-" ++ z


type HouseNo = Int
type Street = String
type City = String
type State = String
type Zip = String

data Address = Address {
      houseNo        :: HouseNo
    , street         :: Street
    , city           :: City
    , state          :: State
    , zip            :: Zip
    }

printLabel :: (LabelMaker a) => a -> String
printLabel a = toLabel(a)
```

- Haskell is much less verbose than scala
- verbose vs concise
    + name instance explicitly as AddressLabelMaker in Scala, while unnamed in Haskell
        * Haskell compiler looks into dictionary on global namespace for qualifying instance
        * Scala search performed in local scope of the method
    + In Scala, you can inject another instance into scope

```scala
object SpecialLabelMaker {
  implicit object AddressLabelMaker extends LabelMaker[Address] {
    def toLabel(address: Address): String = {
      import address._
      "[%d %s, %s, %s - %s]".format(no, street, city, state, zip)
    }
  }
}

// you can choose bring which instance in scope with 
import SpecialLabelMaker._
printLabel(Address(100, "Monroe Street", "Denver", "CO", "80231"))
// you don't get this feature in Haskell
```
