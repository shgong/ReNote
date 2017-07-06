zipWithIndex

Much worse than traversing twice, it creates an intermediary array of pairs. You can use view. When you do collection.view, you can think of subsequent calls as acting lazily, during the iteration. If you want to get back a proper fully realized collection, you call force at the end. Here that would be useless and costly. So change your code to

for((x,i) <- xs.view.zipWithIndex) println("String #" + i + " is " + x)

Nice idea, only one traversal, but it also creates n pairs, even if it does not create a new collection proper. 

# collection view
 view allows to compute only needed elements.

## find first matched
Find the first element that satisfies condition X in a Seq
```
val str = "1903 January"
val formats = List("MMM yyyy", "yyyy MMM", "MM yyyy", "MM, yyyy")
  .map(new SimpleDateFormat(_))
formats.flatMap(f => {try {
  Some(f.parse(str))
}catch {
  case e: Throwable => None
}}).head
```

```
formats.view.map{format => Try(format.parse(str)).toOption}.filter(_.isDefined).head
```
It uses new scala 2.10 feature Try. view allows to compute only needed elements.