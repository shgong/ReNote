glom.md

partition based


scala> divNE.partitions.size
res24: Int = 40


find largest element in each partition
```scala
val max = divNE.glom().map( 
  value => value.max(
    Ordering.by[Moto,Double](_.romi)
    )
  )
```

print max,min in each partition
```scala
divNE.glom().map{array=>
  val k = array.map(_.romi); (k.max,k.min)
}.zipWithIndex().collect().foreach(println)
```




http://blog.madhukaraphatak.com/glom-in-spark/

Using glom for calculating weighted matrix

Glom is highly useful when you want to represent rdd operations as matrix manipulations. In many machine learning algorithms you will be needed to find weighted value of rows , i.e multiplying each row by a given weight vector. Doing this row by row, using map operation will be very costly as you will be not able to use matrix libraries optimization.

But with glom, you can multiply with whole partition at a time so that your computation will speed up significantly.Code listing for same is below
```
// Weighted sum using glom
  import org.jblas.DoubleMatrix
  val rowsList = List[List[Double]](
      List(50.0,40.0,44.0),
      List(88,44.0,44.0),
      List(855,0,55.0,44.0),
      List(855,0,55.0,70.0)
    )
  val weights = List(1.0,0.5,3)
  val rowRDD = sc.makeRDD(rowsList)
  val result = rowRDD.glom().map( value =>{
    val doubleMatrix = new DoubleMatrix( value.map(value => value.toArray))
    val weightMatrix = new DoubleMatrix(1, weights.length,weights.toArray:_*)
    doubleMatrix.mmul( weightMatrix.transpose())

  })
```