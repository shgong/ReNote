
// Try Dataframe Column

Try(z.toInt).transform(i=>Try("_c"+i), _=>Try(z)).get

Try(z.toInt).toOption.map("_c"+_).getOrElse(z)

// fold left

val ff = Array("segment", "division", "region")
(1 /: ff)(_+_.length)
ff.foldLeft(1)(_+_.length)



//----------------------


val gg = List((1,2,"asd"),(3,4,"asdasd"),(5,6,"zoo"))

// 1: underscore
import Function.{tupled => $}
gg map $(_+_+_.length)

// 2: function literal
// func_literal == underscore notation
val func_literal = (a:Int,b:Int,c:String) =>  a+b+c.length
gg map $(func_literal)
gg map func_literal.tupled

// 3: function
// underscore can be ommited only at the end
def func(a:Int,b:Int,c:String):Int = a+b+c.length
val func_literal_same = func _
gg map $(func)
gg map (func _).tupled


// 4: case class
// convert to tuple using unapply
case class CC(a:Int, b:Int, c:String)
val cc = List(CC(1,2,"asd"),CC(3,4,"asdasd"),CC(5,6,"zoo"))

cc map {case CC(a,b,c)=> a+b+c.length}
cc map (CC.unapply _).andThen(_.get).andThen($(func_literal))
cc map (CC.unapply _).andThen(_.get).andThen($(_+_+_.length))
cc map (CC.unapply(_).get) map $(_+_+_.length)
