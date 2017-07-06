
implicit class IntegerWrapper(var a: Int) {
  def unary_! = a + 1
  def apply(b: Int) = a ^ b
  def map(f: Int => Int ) = f(a)
  def flatMap(f: Int => Int ) = f(a+10)
  def >>:(b: Int): Int = 10*a+b
}

for {
  x <- 3(1)
  y <- !(!x)
  z <- x >>: y
} yield z





