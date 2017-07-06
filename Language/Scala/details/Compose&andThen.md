val add2 = (b:Int) => b+2
val multi2 = (c:Int) => 2*c

val cp = add2 andThen multi2
cp(20) //44

val rc = add2 compose multi2
rc(20) //42

