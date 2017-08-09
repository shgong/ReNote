
// composition
def f1(y:Int):Int = y + 1
def f2(y:Int):Int = 10*y
f1(10)
f2(10)

val f3 = f1 _ compose f2
val f4 = f1 _ andThen f2
f3(10)
f4(10)

val f5 = Function.chain(Seq(f1 _,f2 _,f1 _))
val f6 = Function.chain(Seq(f2 _,f1 _,f2 _))
f5(10)
f6(10)


// curry and tuple
def x(a:Int, b:String) = b + a

val p = (x _).curried
p(1)("asd")

val q = (x _).tupled
q((1,"asd"))
