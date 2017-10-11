
## Receiver functions

I think this is my favourite Kotlin feature overall. The only thing I know that’s similar are Ruby’s blocks. Basically, Kotlin lets you declare higher-order functions that are functions on a particular type. That probably sounds confusing, so here’s an example:

```
fun table(init: Table.() -> Unit): Table {
  val table = Table()
  table.init()
  return table
}

fun Table.row(init: Row.() -> Unit) {
  val row = Row()
  row.init()
  this.add(row)
}

fun Row.cell(text: String) {
  this.add(Cell(text))
}
```

Note the signature of Table.row? init is of type Row.() -> Unit. That means: a function that can be called on a Row object, and returns Unit (i.e. nothing). Within that function, this will refer to the Row.

With the code above, we have defined our own little DSL (domain-specific language)! Observe:

```
val myTable = table {
  row {
    cell("top left")
    cell("top right")
  }
  row {
    cell("bottom left")
    cell("bottom right")
  }
}
```

That works thanks to another small but important syntax rule in Kotlin: if the last argument to a function is itself a function, you can put it outside the parentheses. The Kotlin website has a more elaborate example, though I’m not sure about their shameless abuse of + operator overloading.

And note that this is entirely type-safe: you can’t call cell outside the context of a Row, for example.
