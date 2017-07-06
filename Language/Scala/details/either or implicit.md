
```
  object IntOrString {
    implicit def fromInt(i: Int): IntOrString = new IntOrString(None, Some(i))
    implicit def fromString(s: String): IntOrString = new IntOrString(Some(s), None)
  }
  case class IntOrString(str: Option[String], int: Option[Int])
  implicit def IntOrStringToInt(v: IntOrString): Int = v.int.get
  implicit def IntOrStringToStr(v: IntOrString): String = v.str.get

  def myFunc(input:String): IntOrString = {
    if(input.isEmpty) {
      1
    }  else {
      "test"
    }
  }

  val i: Int = myFunc("")
  val s: String = myFunc("123")
  //exception
  val ex: Int = myFunc("123")

```


Polymorphic return types in Scala using implicit parameters

```
trait Factory[T] {
  def create: T
}

object Factory {
  implicit def stringFactory: Factory[String] = new Factory[String] {
    def create = "foo"
  }

  implicit def intFactory: Factory[Int] = new Factory[Int] {
    def create = 1
  }
}

object Main {
  def create[T](implicit factory: Factory[T]): T = factory.create
  // or using a context bound
  // def create[T : Factory]: T = implicitly[Factory[T]].create

  def main(args: Array[String]): Unit = {
    val s = create[String]
    val i = create[Int]

    println(s) // "foo"
    println(i) // 1
  }
}
```