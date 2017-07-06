language that compile to JS

# Basics

```
// sum function
greet = (a,b) -> a+b 

var greet;
greet = function(a, b) {
  return a + b;
};
```

```
// coffeescript string argument
greet = (name='Stranger') -> "Hello, #{name}"

var greet;
greet = function(name) {
  if (name == null) {
    name = 'Stranger';
  }
  return "Hello, " + name;
};
```

# jQuery
$->$  this->@
Do not want to go on...

