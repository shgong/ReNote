
starting with version 2.10, Scala introduced implicit Classes to handle precisely this issue.

This will perform an implicit conversion on a given type to a wrapped class, which can contain your own methods and values.

In your specific case, you'd use something like this:

```
implicit class RichInt(x: Int) {
  def isAFactorOf(y: Int) = x % y == 0
}
```

```
2.isAFactorOf(10)
// or, without dot-syntax
2 isAFactorOf 10
```

Note that when compiled, this will end up boxing our raw value into a RichInt(2). You can get around this by declaring your RichInt as a subclass of AnyVal:
```
implicit class RichInt(val x: Int) extends AnyVal { ... }
```
This won't cause boxing, but it's more restrictive than a typical implicit class. It can only contain methods, not values or state.

