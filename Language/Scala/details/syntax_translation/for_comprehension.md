
# Excerpt from Programming in Scala

Generally, a for expression is of the form:
```
for ( seq ) yield expr

for {
    p <- persons // a generator
    n = p.name // a definition
    if (n startsWith "To") // a filter
} yield n
```


First, assume you have a simple for expression:
```
for (x <- expr1) yield expr2
// translated to:
expr1.map(x => expr2)
```

Now, consider for expressions that combine a leading generator with some other elements. A for expression of the form:
```
for (x <- expr1 if expr2) yield expr3
// translated to:
expr1 withFilter (x => expr2) map (x => expr3)
```

The next case handles for expressions that start with two generators, as in:
```
for (x <- expr1; y <- expr2) yield expr3
// The for expression above is translated to an application of flatMap:
expr1.flatMap(x => for (y <- expr2) yield expr3)
```


# Example
```scala
for (x <- 8) yield x
for (x <- 8; y <- x-1) yield y+6


> Error:(3, 12) value map is not a member of Int
> Error:(4, 12) value flatMap is not a member of Int


implicit class IntegerWrapper(x: Int) {
  def map(f: Int => Int ) = f(x)
  def flatMap(f: Int => Int ) = f(x)
}

for (x <- 8) yield x
for (x <- 8; y <- x-1) yield y+6

> res0: Int = 8
> res1: Int = 13
```
