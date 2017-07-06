
```scala
implicit class When[A](a: A) {
  def when(f: A => Boolean)(g: A => A) = if (f(a)) g(a) else a
}

class When[A](a: A) {
  def when(f: A => Boolean)(g: A => A) = if (f(a)) g(a) else a
}

scala> "fish".when(_.length<5)(_.toUpperCase)
res2: java.lang.String = FISH

```

