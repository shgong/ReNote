
def add1(x: Int, y: Int) = x + y
val add1a = add1 _
val add1b = add1(1, _:Int)
val add1c = add1a.curried
// val add1d = add1c(23)_   // NOT work,
// underscore must follow method or a variable(call-by-name parameter)
add1a(1,2)
add1b(4)
add1c(1)(2)


def add2(x: Int)(y: Int) = x + y
val add2a = add2 _
val add2b = add2(1)_
val add2c = Function.uncurried(add2a)

add2a(1)(2)
add2b(4)
add2c(1,2)

def add3(x:Int, y:Int, z:Int) = x + y + z
val add3a = add3 _
val add3b = add3(4,_:Int,_:Int)
val add3c = add3a.curried
val add3d = add3b.curried

add3a(1,2,3)
add3b(4,4)
add3c(1)(6)(12)
add3d(4)(4)

def add4(x: Int*) = x.toList.sum
val add4a = add4 _
val add4b = add4(4, _:Int, _:Int)
// val add4c = add4(4, _:Int*) // NOT WORK
// star sign only in function define

// val add4c = add4b.curreid // NOT WORK
// value curried not member of (Int,Int)=>Int


add4a(List(1,2,3))
add4b(4,1)

val tupledFunction = Function.tupled(add4b)
tupledFunction((4,1))




// type inferenece in curried version
def m1[A](a: A, f: A => String) = f(a)
def m2[A](a: A)(f: A => String) = f(a)
m1(100, i => s"$i + $i")  // error, missing parameter type
m2(100)(i => s"$i + $i")  // 100 + 100