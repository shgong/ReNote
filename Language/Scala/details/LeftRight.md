LeftRight

```scala
val array = value match {
  case Left(str) => Array(str)
  case Right(arr) => arr
}
```


```scala
val array = value
  .whenOrElse(_.isLeft)(
    x => Array(x.left.get),
    _.right.get
  )
```

