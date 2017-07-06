```
Map(1 -> "one", 2 -> "two") map { case (a, b)  => a -> b.length }

Map((1,"one"), 2 -> "two") map ((_: Int) -> (_: String).length).tupled

Map(1 -> "one", 2 -> "two") map ((p: (Int, String))  => p._1 -> p._2.length)
```
