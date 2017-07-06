
# Spark Advanced Analytics

## 1. Analyzing Big Data

Goal

- build model to detect credit card fraud from transaction data
- intelligently reocmmendation
- estimate financial risk through simulation of portfolio
- easily manipulate data from human genomes to detect genetic association

Evolution

- open source framework
    + R
    + PyData
    + Octave
- works for small data set, but difficult to distribute
    + partitioned across many nodes: wide data dependency algorithm will suffer from network transfer rate
    + as machine number increase, probability of failure increase
    + require programming paradigm in highly parallel manner
- HPC cluster-computing framework
    + problem: relative low level of abstraction
    + example: large DNA sequence
        * manually split up and submit
        * resubmit manually when failed
        * stream to single node when sort
        * resort to low level MPI with C/distributed

Challenge of Data Science

- majority work lies in preprocessing data
    + data is messy
        * cleansing, munging, fusing, mushing
    + a typical data pipeline require far more time
        * in feature engineering & selection
        * than choosing ML algorithm
- iteration
    + modeling & analysis require multiple passes over same data
    + popular optimization like stochastic gradient descent & expectation maximization involve repeated scan to reach convergence
    + iterations when choose feature, pick algorithm, run tests, find hyperparameters
- further analysis
    + task is not over when a list of regression weights is trained
    + model become part of production service which need rebuild periodically or even in real time
    + analytic in lab vs analytic in factory

## 2. Data Analysis with Scala and Spark

### 2.1 Preprocess

Data Type
```
"id_1","id_2","cmp_fname_c1","cmp_fname_c2","cmp_lname_c1","cmp_lname_c2",
"cmp_sex","cmp_bd","cmp_bm","cmp_by","cmp_plz","is_match" 37291,53113,0.833333333333333,?,1,?,1,1,1,1,0,TRUE 39086,47614,1,?,1,?,1,1,1,1,1,TRUE 70031,70237,1,?,1,?,1,1,1,1,1,TRUE 84795,97439,1,?,1,?,1,1,1,1,1,TRUE
```

Remove Header
```scala
def isHeader(line: String) = line.contains("id_1")

def isHeader(line: String): Boolean = { 
    line.contains("id_1")
}

// 3 ways to filter
head.filterNot(isHeader) // from array class
head.filter(x=>!isHeader(x)) // anonymous function
head.filter(!isHeader(_)) // underscore representation
```

Parse and assembly classes
```scala
val pieces = line.split(',')
val id1 = pieces(0).toInt
val id2 = pieces(1).toInt
val matched = pieces(11).toBoolean

val rawscores = pieces.slice(2, 11)
rawscores.map(s => s.toDouble)
// not work, there is question mark entry

// define toDouble(string)
def toDouble(s: String) = {
if ("?".equals(s)) Double.NaN else s.toDouble
}
val scores = rawscores.map(toDouble)
```

Form Tuple
```scala
def parse(line: String) = {
    val pieces = line.split(',')
    val id1 = pieces(0).toInt
    val id2 = pieces(1).toInt
    val scores = pieces.slice(2, 11).map(toDouble) 
    val matched = pieces(11).toBoolean
    (id1, id2, scores, matched)
    }
val tup = parse(line)

tup._1                  // get 1st element
tup.productElement(0)   // get 1st element
tup.productArity        // get size
```

Address elements of record by position instead of meaningful name can make it difficult to understand. Use Caseclass instead.

```scala
case class MatchData(id1: Int, id2: Int, 
    scores: Array[Double], matched: Boolean)

def parse(line: String) = { 
    val pieces = line.split(',') 
    val id1 = pieces(0).toInt
    val id2 = pieces(1).toInt
    val scores = pieces.slice(2, 11).map(toDouble) 
    val matched = pieces(11).toBoolean 
    MatchData(id1, id2, scores, matched)
}
val md = parse(line)

md.matched
md.id1

val mds = head.filterNot(isHeader).map(parse)
```

### 2.2 Histogram

Aggregate by compound key

```scala
val grouped = mds.groupBy(md => md.matched)
grouped.mapValues(x => x.size).foreach(println)
```

Create Histogram

- count how many of MatchData in parsed have value of true or false
- RDD[T] define an action called CountByValue 

```scala
// case calss to single true false value
val matchCounts = parsed.map(md => md.matched).countByValue()
val matchCountsSeq = matchCounts.toSeq

matchCountsSeq.sortBy(_._1).foreach(println) // label
//(false,5728201)
//(true,20931)

matchCountsSeq.sortBy(_._2).reverse.foreach(println)  // reverse number
//(false,5728201)
//(true,20931)
```

### 2.3 Summary Statistics

- countByValue not work for continuous fields
- RDD[Double] stats
    + the missing NaN value tripping up Spark summary statistics
    + should filter them manually

```scala
parsed.map(md => md.scores(0)).stats()
StatCounter = (count: 5749132, mean: NaN, stdev: NaN, max: NaN, min: NaN)

import java.lang.Double.isNaN
parsed.map(md => md.scores(0)).filter(!isNaN(_)).stats()
StatCounter = (count: 5748125, mean: 0.7129, stdev: 0.3887, max: 1.0, min: 0.0)
```

using Scala’s Range construct to create a loop that would iterate through each index value and compute the statistics for the column

```scala
val stats = (0 until 9).map(i => {
parsed.map(md => md.scores(i)).filter(!isNaN(_)).stats()
})
stats(1)
StatCounter = (count: 103698, mean: 0.9000, stdev: 0.2713, max: 1.0, min: 0.0)

stats(8)
StatCounter = (count: 5736289, mean: 0.0055, stdev: 0.0741, max: 1.0, min: 0.0)
```

### 2.4 Reusable Summary

- create scala class & API 
- write a analogue of Spark's Stat Count
- NAStatCount
    + two member variable
        * stats: immutable StatCount instance
        * missing: mutable Long variable
        * Serialization: use inside Spark RDD, will fail if cannot serialize
    + add()
        * bring a new Double value into statistics
        * reading it as missing, or add to StatCount if not
    + merge()
        * incorporate statistics from another NAStatCounter
        * us StatCounter merge method


StatsWithMissing.scala
```scala
import org.apache.spark.util.StatCounter

class NAStatCounter extends Serializable { 
    val stats: StatCounter = new StatCounter() 
    var missing: Long = 0

    def add(x: Double): NAStatCounter = { 
        if (java.lang.Double.isNaN(x)) {
            missing += 1 
        }else{
          stats.merge(x)
        }
        this
    }

    def merge(other: NAStatCounter): NAStatCounter = { 
        stats.merge(other.stats)
        missing += other.missing
        this
    }

    override def toString = {
        "stats: " + stats.toString + " NaN: " + missing
    } 
}

// comliment object
object NAStatCounter extends Serializable {
    def apply(x: Double) = new NAStatCounter().add(x)
}

// apply method: special syntactic sugar
val nastats = NAStatCounter.apply(17.29) 
val nastats = NAStatCounter(17.29)
```


encapsulate to scala file
```scala
import org.apache.spark.rdd.RDD
    
def statsWithMissing(rdd: RDD[Array[Double]]): Array[NAStatCounter] = {
  // mapPartitions with iterator
  val nastats = rdd.mapPartitions((iter: Iterator[Array[Double]]) => {
    // Create counter with first iterator
    val nas: Array[NAStatCounter] = iter.next().map(d => NAStatCounter(d))
    iter.foreach(arr => {
      // zip: add 9 fields one by one
      nas.zip(arr).foreach { case (n, d) => n.add(d) }
    })
    // return a new iterator
    Iterator(nas)
  })

  // zip: merge 9 fields one by one
  nastats.reduce((n1, n2) => {
    n1.zip(n2).map { case (a, b) => a.merge(b) }
  }) 
}
```

### 2.5 Simple Variable Selection & Scoring

- with statsWithMissing function, we can analyze difference of score distribution, for matches & nonmatches
- a good feature
    + have significantly different values for the two type
    + occur often enough in data, so we can rely on to be regularly available

```scala
val statsm = statsWithMissing(parsed.filter(_.matched).map(_.scores)) 
val statsn = statsWithMissing(parsed.filter(!_.matched).map(_.scores))

// stats for each pair
// total missing & mean difference
statsm.zip(statsn).map { case(m, n) =>
    (m.missing + n.missing, m.stats.mean - n.stats.mean)
}.foreach(println)

// ((1007, 0.2854...), 0)
// ((5645434,0.09104268062279874), 1)
// ((0,0.6838772482597568), 2)
// ((5746668,0.8064147192926266), 3)
// ((0,0.03240818525033484), 4)
// ((795,0.7754423117834044), 5)
// ((795,0.5109496938298719), 6)
// ((795,0.7762059675300523), 7)
// ((12843,0.9563812499852178), 8)
```

- 1,3 missed too much
- 0,4 not significant
- 5,7 exellent
- 2,6,8 beneficial

Try to build a model with 2,5,6,7,8

```scala
// replace missing data with 0
// sum feature
def naz(d: Double) = if (Double.NaN.equals(d)) 0.0 else d 

case class Scored(md: MatchData, score: Double)
val ct = parsed.map(md => {
    val score = Array(2, 5, 6, 7, 8).map(i => naz(md.scores(i))).sum
    Scored(md, score) 
})

// test
ct.filter(s => s.score >= 4.0).map(s => s.md.matched).countByValue()
Map(false -> 637, true -> 20871)
// filter out most nonmatch while keep 90% match

ct.filter(s => s.score >= 2.0).map(s => s.md.matched).countByValue()
Map(false -> 596414, true -> 20931)
// filter out much nonmatch while keep all match
```

## 3. Recommend Music and Audioscrobbler Data Set

AudioScrobbler
- first music recommendation system for last.fm
- open API for scrobbling, or record listeners' plays of songs
- old recommend engines use rating data, like 3.5 stars, but that's rare
- but plays record is way more important

Data Set
- `user_artist_data.txt`: 24.2m play records
- `artist_data.txt`: artist name could be misspelled or nonstandard
- `artist_alias.txt`: map artist ID to canonical

### 3.1 Alternating Least Squares Recommender Algorithm

Background

- learn without access to user/artist attributes
- collaborative filtering: two users may both like same song as they play many other same songs
- the data set looks large, but it is sparse

The Algorithm
- matrix factorization model
- treat user & product as if it were a large but very sparse matrix A
    + entry row i and column j: user i play artist j
    + most entries are zero
- factor A as product of smaller X, Y with same row but fewer column
    + only approximate as k is small
    + also called matrix completion algorithm, as A may be sparse, but XY is dense
    + user-feature matrix x feature-artist matrix => complete
- Problem
    + XY should be as close to A as possible
    + can't be solved directly for both the best X and Y at the same time
    + simple to solve X if Y is known
    + but neither is know beforehand
- ALS
    + popularized around the Netflix Prize
        * check: [Collaborative Filtering for Implicit Feedback Dataset](https://github.com/tcdoan/S683/blob/master/docs/HuKorenVolinsky-ICDM08.pdf)
        * and: [Large-scale Parallel Collaborative Filtering for
the Netflix Prize](http://machinelearning202.pbworks.com/w/file/fetch/60922097/netflix_aaim08%28submitted%29.pdf)
    + Y => X & X=> Y
        * Y is not known, but can be initialized to a matrix full of random vectors
        * simple linear algebra => best X
        * this process can be done row by row separately
    + Equality cannot be ahieved exactly 
        * minimize |A_i Y (Y^T Y)^-1 - X_i| with least square
        * not actual calculate reverse, use QR decomposition usually
    + ALS can take advantage of sparsity, rely on simple linear algebra, data-parallel nature, very fast on large scale

### 3.2 Preparing Data

One small limit of MLlib's ALS implementation is that it requires numeric IDs for users and items, nonnegative 32-bit integers (<Integer.MAX_VALUE=2147483647)

```scala
val rawUserArtistData = sc.textFile("hdfs:///user/ds/user_artist_data.txt")
// 400MB on HDFS, usually 3-6 partitions
// better to break to smaller parts for ALS, much more computation intensive

rawUserArtistData.map(_.split(' ')(0).toDouble).stats() 
rawUserArtistData.map(_.split(' ')(1).toDouble).stats()
// maximum IDs are 2443548, 10794401, smaller than MAX_VALUE

val artistByID = rawArtistData.map { line =>
      val (id, name) = line.span(_ != '\t') 
      // span() splits the line by its first tab
      (id.toInt, name.trim)
    }
// map return exactly one value => use flatMap()
// Use None/Some subclass

val artistByID = rawArtistData.flatMap { line => 
    val (id, name) = line.span(_ != '\t')
    if (name.isEmpty) {
        None
    }else{ 
        try {
            Some((id.toInt, name.trim)) 
        } catch {
            case e: NumberFormatException => None 
        }
    } 
}
```

Also for ArtistAlias Table
```scala
val rawArtistAlias = sc.textFile("hdfs:///user/ds/artist_alias.txt") 
val artistAlias = rawArtistAlias.flatMap { line =>
    val tokens = line.split('\t') 
    if (tokens(0).isEmpty) {
        None
    }else{
        Some((tokens(0).toInt, tokens(1).toInt))
    }
}.collectAsMap()

// example: map ID 6803336 to 1000010
artistByID.lookup(6803336).head  // Aerosmith(unplugged)
artistByID.lookup(1000010).head  // Aerosmith
```

### 3.3 Building a First Model

two small extra transformation
- all artist ID convert to final canonical ID
- data convert to Rating object (user-product-value)

```scala
import org.apache.spark.mllib.recommendation._

// broadcast, hold one copy for each executor, save lot of traffic/memory
val bArtistAlias = sc.broadcast(artistAlias)
val trainData = rawUserArtistData.map { line =>
    val Array(userID, artistID, count) = line.split(' ').map(_.toInt) 
    val finalArtistID =
        bArtistAlias.value.getOrElse(artistID, artistID)  // getOrElse
    Rating(userID, finalArtistID, count)
}.cache()
// cache(): suggest this RDD temporarily stored in memory after computed
// as ALS is iterative, need repeat 10 times or more
```

Open Storage tab in Spark UI for cached RDD memory usage.

Finally build a model
```scala
val model = ALS.trainImplicit(trainData, 10, 5, 0.01, 1.0
// see the feature vector
// make Array readable by mkString
model.userFeatures.mapValues(_.mkString(",")).first()
```

### 3.4 Spot Checking Recommendation

example: choose user 2093760, extract artist information

```scala
// pick lines from a user
val rawArtistsForUser = rawUserArtistData.map(_.split(' ')). 
    filter { case Array(user,_,_) => user.toInt == 2093760 }

// find existing products
val existingProducts =
    rawArtistsForUser.map { case Array(_,artist,_) => artist.toInt }. 
    collect().toSet

// filter artists appeared in existing product
artistByID.filter { case (id, name) =>  
    existingProducts.contains(id)
}.values.collect().foreach(println)

// David Gray
// Blackalicious 
// Jurassic 5 
// The Saw Doctors 
// Xzibit

// Recommend
val recommendations = model.recommendProducts(2093760, 5) 
val recommendedProductIDs = recommendations.map(_.product).toSet
artistByID.filter { case (id, name) => 
    recommendedProductIDs.contains(id)
}.values.collect().foreach(println)

// Green Day
// Linkin Park
// Metallica
// My Chemical Romance
// System of a Down

// General popular artist, don't appear personized
```

### 3.5 Evaluating Recommendation Quality

put play data into training / testing samples, rank all items and compute score using the held-out artists

- Receiver Operating Characteristic (ROC) Curve
    + metric in the preceding paragraph equals the area under ROC Curve
- AUC, Area under the Curve
    + viewed as probability that randomly chosen good recommendation ranks above randomly chosen bad recommendation
    + used in evaluation of classifiers
    + implemented in MLlib `BinaryClassificationMetrics` class
    + compute AUC per user and average the result => meanAUC
- other metrics
    + precision, recall, mean average precision (MAP)
    + MAP is also frequently used, focus narrowly on quality of top recommendations

Computing AUC

- accept the CV set as positive or good artist for each user & a prediction function (user-artist pair to Rating)
- split data 90% training & 10% cross validation

```scala
import org.apache.spark.rdd._

def areaUnderCurve(
    positiveData: RDD[Rating],
    bAllItemIDs: Broadcast[Array[Int]],
    predictFunction: (RDD[(Int,Int)] => RDD[Rating])) = {
    ... 
}
val allData = buildRatings(rawUserArtistData, bArtistAlias)

val Array(trainData, cvData) = allData.randomSplit(Array(0.9, 0.1)) trainData.cache()
cvData.cache()

val allItemIDs = allData.map(_.product).distinct().collect() 
val bAllItemIDs = sc.broadcast(allItemIDs)

val model = ALS.trainImplicit(trainData, 10, 5, 0.01, 1.0) 
val auc = areaUnderCurve(cvData, bAllItemIDs, model.predict)
```

- result is 96%, reasonably high
- evaluation can repeat will a different 90% as training set (kFold k=10)

Compare to recommending most listened artist to all

```scala
// partial-applied function in scala
def predictMostListened(
    sc: SparkContext,
    train: RDD[Rating])(allData: RDD[(Int,Int)]) = {
        val bListenCount = sc.broadcast( 
            train.map(r => (r.product, r.rating)).
                reduceByKey(_ + _).collectAsMap() 
        )

        // return a transformation of the parameter allData
        allData.map { case (user, product) => Rating(
          user,
          product,
          bListenCount.value.getOrElse(product, 0.0)
        ) 
    }
}
val auc = areaUnderCurve(
      cvData, bAllItemIDs, predictMostListened(sc, trainData))
```

- result is 93%
- already fairly effectly according to this metric

### 3.6 Hyperparameter Selection

parameters used to build `MatrixFactorizaitonModel` were not learned by algorithm, must be chosen by caller

- rank=10
    + number of latent factors
    + column k in the user-feature, product-feature metric
- iterations=5
    + take more time
    + produce better factorization
- lambda=0.01
    + standard overfitting parameter
    + high value resist overfitting but hurt accuracy
- alpha=0.1
    + relative weight of observed vs unobserved user-product interactions

try combinations for best model
```scala
val evaluations =
    for(rank <-Array(10,50);
        lambda <- Array(1.0, 0.0001);
        alpha <- Array(1.0, 40.0))  // triply nested for loop
    yield {
        val model = ALS.trainImplicit(trainData, rank, 10, lambda, alpha) 
        val auc = areaUnderCurve(cvData, bAllItemIDs, model.predict) 
        ((rank, lambda, alpha), auc)
} 
evaluations.sortBy(_._2).reverse.foreach(println)
// sort by AUC, descending and print
```

output
```
((50,1.0,40.0),0.9776687571356233)
((50,1.0E-4,40.0),0.9767551668703566)
((10,1.0E-4,40.0),0.9761931539712336)
((10,1.0,40.0),0.976154587705189)
((10,1.0,1.0),0.9683921981896727)
((50,1.0,1.0),0.9670901331816745)
((10,1.0E-4,1.0),0.9637196892517722)
((50,1.0E-4,1.0),0.9543377999707536)
```

### 3.7 Final Recommendation

At the moment, however, Spark MLlib’s ALS implementation does not support a method to recommend to all users. It is possible to recommend to one user at a time, although each will launch a short-lived distributed job that takes a few seconds. 

```scala
val someUsers = allData.map(_.user).distinct().take(100) 

val someRecommendations =
    someUsers.map(userID => model.recommendProducts(userID, 5)) // local map

someRecommendations.map(
    recs => recs.head.user + " -> " + recs.map(_.product).mkString(", ") 
).foreach(println)
```

interesting stuff: can also recommend user to artist, just reverse order

## 4. Predicting Forest Cover with Decision Trees

the line links two values, and implies that the value of one suggests a lot about the value of the other 

- Feature
    + or dimension, predictor, variable
    + high/low temperature, humidity, wheather, cold forecasts
    + feature vector (13.1,19.0,0.73,cloudy,1)
- Training Samples
    + prediction feature: tomorrow temperature
    + example: (12.5,15.5,0.10,clear,0)=>17.2
    + regression for numeric, classification for type
- Decision Trees and Forests
    + naturally handle both categorical and numeric features
    + built parallel easily, robust to outliers in the data
        * good for extreme conditions like irregular weather
    + comparatively intuitive to understand & reason about
        * series yes/no decision

### 4.1 Preparing the data

- Covtype data set
    + type of forest covering parcels of land in Colorado, USA
        * feature describe each parcel of land
            - elevation
            - slope
            - distance to water
            - shade
            - soil type
        * known forest type covering the land (54 types)
    + `covtype.data.gz`: simple csv format
    + `covtype.info`: accompanying info file
- Spark MLlib abstraction
    + LabeledPoint: abstraction for feature vector, consists of a Vector of features and a target value 
        * all with Double values
        * can be used with category if appropriate encoded
    + Encoding
        * one-hot or 1-of-n encoding: categorical feature that takes on N distinct values become N features taking 0 and 1
        * simply assign single numeric value, but ordering may have problem
        * for performance reason or to match format expected by regression libraries, data sets often contain data encoded with category values
 
### 4.2 A First Decision Tree

Prepare data

```scala
import org.apache.spark.mllib.linalg._
import org.apache.spark.mllib.regression._

val data = rawData.map { line =>
    val values = line.split(',').map(_.toDouble)
    val featureVector = Vectors.dense(values.init) // dense vector
    // init returns all but last value
    val label = values.last - 1
    // decision tree label start at 0
    LabeledPoint(label, featureVector)
}
```

the evaluation metric is different here: the precision

```scala
val Array(trainData, cvData, testData) = 
    data.randomSplit(Array(0.8, 0.1, 0.1))
trainData.cache()
cvData.cache()
testData.cache()
```

TreeModel
```scala
import org.apache.spark.mllib.evaluation._
import org.apache.spark.mllib.tree._
import org.apache.spark.mllib.tree.model._
import org.apache.spark.rdd._

def getMetrics(model: DecisionTreeModel, data: RDD[LabeledPoint]): MulticlassMetrics = {
    val predictionsAndLabels = data.map(example => 
        (model.predict(example.features), example.label)
    )
    new MulticlassMetrics(predictionsAndLabels) 
}

val model = DecisionTree.trainClassifier( 
    trainData, 7, Map[Int,Int](), "gini", 4, 100)

val metrics = getMetrics(model, cvData)
```

- use `trainClassifier`: target value within each LabeledPoint should be treated as a distinct category number, not a numeric feature (trainRegressor)
- specify the target value: 7
- depth = 4, maximum bin count = 100
- MulticlassMetric: compute standard metrics that in differernt measure the quality of predictions from a classifier, take a map function
- BinaryClassificationMetrics: similiar with two values targets


Result
```
metrics.confusionMatrix
...
    14019.0  6630.0   15.0    0.0    0.0  1.0   391.0
    5413.0   22399.0  438.0   16.0   0.0  3.0   50.0
    0.0      457.0    2999.0  73.0   0.0  12.0  0.0
    0.0      1.0      163.0   117.0  0.0  0.0   0.0
    0.0      872.0    40.0    0.0    0.0  0.0   0.0
    0.0      500.0    1138.0  36.0   0.0  48.0  0.0
    1091.0   41.0     0.0     0.0    0.0  0.0   891.0

metrics.precision
...
0.7030630195577938
```

7 category values, each row for actual correct value & each column to predicted value. like category5 is never predicted at all

- precision often accompnay by recall
    + 20 positive sample of 50
    + classifier marks 10 positive, 4 of real positive
    + precision = 4/10 = 0.4, recall = 4/20 = 0.2

```scala
(0 until 7).map(
    cat => (metrics.precision(cat), metrics.recall(cat))
).foreach(println)

    ...
    (0.6805931840866961,0.6809492105763744)
    (0.7297560975609756,0.7892237892589596)
    (0.6376224968044312,0.8473952434881087)
    (0.5384615384615384,0.3917910447761194)
    (0.0,0.0)
    (0.7083333333333334,0.0293778801843318)
    (0.6956168831168831,0.42828585707146427)

```

compare to baseline of random guess by probability
```scala
import org.apache.spark.rdd._

def classProbabilities(data: RDD[LabeledPoint]): Array[Double] = { 
    val countsByCategory = data.map(_.label).countByValue()
    val counts = countsByCategory.toArray.sortBy(_._1).map(_._2) 
    counts.map(_.toDouble / counts.sum)
}

val trainPriorProbabilities = classProbabilities(trainData) 
val cvPriorProbabilities = classProbabilities(cvData) 

// guess the same result
trainPriorProbabilities.zip(cvPriorProbabilities).map {
    case (trainProb, cvProb) => trainProb * cvProb 
}.sum

...
    0.37737764750734776
```

### 4.3 Decision Tree Hyperparameters

- Depth: limit of decision levels, prevent overfitting
- Decision tree will try potential decision rules at each level
    + numeric: weight>100 or weight>500
    + category: feature in (value1, value2..) bins
- impurity: good rule divide value into pure subset
    + Gini impurity
        * accuracy of random-guess classifier
        * probability that random classification of random example is incorrect
        * if subset has N class, each portion p_i
        * I(p) = 1 - Sum(p_i^2)
        * default in Spark implementation
    + Entropy
        * how much uncertainty the collection of target values conatains
        * I(E) = - Sum(p_i log(p_i))

```scala
val evaluations =
    for (impurity <- Array("gini", "entropy");
         depth <- Array(1, 20);
         bins <- Array(10, 300)) 
    yield {
        val model = DecisionTree.trainClassifier(
            trainData, 7, Map[Int,Int](), impurity, depth, bins)
        val predictionsAndLabels = cvData.map(example => 
            (model.predict(example.features), example.label)
        )
        val accuracy =
            new MulticlassMetrics(predictionsAndLabels).precision
          ((impurity, depth, bins), accuracy)
        }

evaluations.sortBy(_._2).reverse.foreach(println)
...
((entropy,20,300),0.9125545571245186)
((gini,20,300),0.9042533162173727)
((gini,20,10),0.8854428754813863)
((entropy,20,10),0.8848951647411211)
((gini,1,300),0.6358065896448438)
((gini,1,10),0.6355669661959777)
((entropy,1,300),0.4861446298673513)
((entropy,1,10),0.4861446298673513)
```

### 4.4 Category feature revisited

- the argument Map[Int, Int]
    + specify the number of distinct values to expect for each categorical features
    + empty Map indicate all feature is numeric
- 40-value category vs 40 numeric feature
    + create decision based on group of categories in one decision
    + 40 features increase memory usage and slow things down
    + undoing one-hot encoding

```scala
val data = rawData.map { line =>
    val values = line.split(',').map(_.toDouble)
    val wilderness = values.slice(10, 14).indexOf(1.0).toDouble  // 4->1
    val soil = values.slice(14, 54).indexOf(1.0).toDouble  // 40->1
    val featureVector =
        Vectors.dense(values.slice(0, 10) :+ wilderness :+ soil) 
    val label = values.last - 1
    LabeledPoint(label, featureVector)
}
```

Training again
```scala
val evaluations =
for (impurity <- Array("gini", "entropy");
     depth <- Array(10, 20, 30);
     bins <- Array(40, 300)) 
yield {
    val model = DecisionTree.trainClassifier( 
        // specify Map of category features
        trainData, 7, Map(10 -> 4, 11 -> 40), 
        impurity, depth, bins)
    val trainAccuracy = getMetrics(model, trainData).precision 
    val cvAccuracy = getMetrics(model, cvData).precision ((impurity, depth, bins), (trainAccuracy, cvAccuracy))
}

((entropy,30,300),(0.9996922984231909,0.9438383977425239))
((entropy,30,40),(0.9994469978654548,0.938934581368939))
((gini,30,300),(0.9998622874061833,0.937127912178671))
((gini,30,40),(0.9995180059216415,0.9329467634811934))
((entropy,20,40),(0.9725865867933623,0.9280773598540899))
((gini,20,300),(0.9702347139020864,0.9249630062975326))
((entropy,20,300),(0.9643948392205467,0.9231391307340239))
((gini,20,40),(0.9679344832334917,0.9223820503114354))
((gini,10,300),(0.7953203539213661,0.7946763481193434))
((gini,10,40),(0.7880624698753701,0.7860215423792973))
((entropy,10,40),(0.78206336500723,0.7814790598437661))
((entropy,10,300),(0.7821903188046547,0.7802746137169208))
```

nearly perfect ~ 0.99969

### 4.5 Random Decision Forests

- decision tree
    + does not consider every possible decision rule at every level
    + for categorical feature over N values, there are 2^N-2 rule subset
    + instead decision tree use several heuristics, randomness, trade a bit of accuracy for a lot of speed, won't build same tree
- wisdom of crowds: the statistic guessing game
- collective average prediction should fall close to true answer

```scala
val forest = RandomForest.trainClassifier( 
    trainData, 7, Map(10 -> 4, 11 -> 40), 20, "auto", "entropy", 30, 300)
```

- two new parameters
    + number of trees to build: 20
    + strategy for choosing feature evaluation: auto
- make individual tree decision independent & forest less prone to overfitting
- each tree will not even see all training data
- final prediction
    + weighted average of trees' prediction
    + 2% better, or 33% reduction on error rate

Prediction
```scala
val input = "2709,125,28,67,23,3224,253,207,61,6094,0,29" 
val vector = Vectors.dense(input.split(',').map(_.toDouble)) 
forest.predict(vector)
```

More implementation: naive bayes, SVM, logistic regression

## 5. Anomaly Detection with K-means Clustering

## 6. Understand Wikipedia with Semantic analysis

- Latent Semantic Analysis (LSA)
    + distill corpus into a set of relevant concepts
    + concept captures a thread of variation in the data
        * a level of affinity for each document
        * a level of affinity for each term
        * a importance score relecting how useful the concept is
    + discover lower dimension representation using SVD
- Singular vlaue decomposition (SVD)
    + powerful version of ALS factorization
    + start with term-document matrix
        * document - column
        * term - row
        * element - term importance
    + SVD factorize into three matrices
        * document, term, importance
        * also popular in face recognition, image compression

### 6.1 Term-Document Matrix

LSA require transform text corpus into term-document matrix

- row: term
- column: document
- weighting schemes: TF-IDF
    + term frequency times inverse document frequency
    + intuitions
        * 1 more often in document
        * 2 not occur equally in global sense
    + assumptions
        * bag of words, ignore ordering
        * not distinguish multiple meaning words

```scala
def termDocWeight(termFrequencyInDoc: Int, totalTermsInDoc: Int, termFreqInCorpus: Int, totalDocs: Int): Double = {
    val tf = termFrequencyInDoc.toDouble / totalTermsInDoc 
    val docFreq = totalDocs.toDouble / termFreqInCorpus 
    val idf = math.log(docFreq)
    tf*idf
}
```


### 6.2 Preparing the Data

dump all wikipedia articles
```
$ curl -s -L http://dumps.wikimedia.org/enwiki/latest/\ 
$ enwiki-latest-pages-articles-multistream.xml.bz2 \
$   | bzip2 -cd \
$   | hadoop fs -put - /user/ds/wikidump.xml
```

example xml
```xml
<page>
    <title>Anarchism</title>
    <ns>0</ns>
    <id>12</id>
    <revision>
      <id>584215651</id>
      <parentid>584213644</parentid>
      <timestamp>2013-12-02T15:14:01Z</timestamp>
      <contributor>
        <username>AnomieBOT</username>
        <id>7611264</id>
      </contributor>
      <comment>Rescuing orphaned refs (&quot;autogenerated1&quot; from rev
      584155010; &quot;bbc&quot; from rev 584155010)</comment>
      <text xml:space="preserve">{{Redirect|Anarchist|the fictional character|
      Anarchist (comics)}}
{{Redirect|Anarchists}}
{{pp-move-indef}}
{{Anarchism sidebar}}
'''Anarchism''' is a [[political philosophy]] that advocates [[stateless society|
stateless societies]] often defined as [[self-governance|self-governed]] voluntary
institutions,&lt;ref&gt;&quot;ANARCHISM, a social philosophy that rejects
authoritarian government and maintains that voluntary institutions are best suited
to express man's natural social tendencies.&quot; George Woodcock.
&quot;Anarchism&quot; at The Encyclopedia of Philosophy&lt;/ref&gt;&lt;ref&gt;
&quot;In a society developed on these lines, the voluntary associations which
already now begin to cover all the fields of human activity would take a still
greater extension so as to substitute ...
```

Use XML input format from apache mahout project
```scala
import com.cloudera.datascience.common.XmlInputFormat
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io._

val path = "hdfs:///user/ds/wikidump.xml"
@transient val conf = new Configuration() 
conf.set(XmlInputFormat.START_TAG_KEY, "<page>")
conf.set(XmlInputFormat.END_TAG_KEY, "</page>")
val kvs = sc.newAPIHadoopFile(path, classOf[XmlInputFormat],
    classOf[LongWritable], classOf[Text], conf) 
val rawXmls = kvs.map(p => p._2.toString)
```

Turn wiki xml into plain text with Cloud9 project
```scala
import edu.umd.cloud9.collection.wikipedia.language._
import edu.umd.cloud9.collection.wikipedia._

def wikiXmlToPlainText(xml: String): Option[(String, String)] = { 
    val page = new EnglishWikipediaPage() WikipediaPage.readPage(page, xml)
    if (page.isEmpty) None
    else Some((page.getTitle, page.getContent)) 
}
val plainText = rawXmls.flatMap(wikiXmlToPlainText)
```

### 6.3 Lemmatization

- turn plain text to bag of terms
- Stanford Core NLP project provides an excellent lemmatizer with a Java API that Scala can take advantage of. 

```scala
import edu.stanford.nlp.pipeline._
import edu.stanford.nlp.ling.CoreAnnotations._

def createNLPPipeline(): StanfordCoreNLP = {
    val props = new Properties()
    props.put("annotators", "tokenize, ssplit, pos, lemma") 
    new StanfordCoreNLP(props)
}

def isOnlyLetters(str: String): Boolean = { 
    str.forall(c => Character.isLetter(c))
}

def plainTextToLemmas(text: String, stopWords: Set[String], pipeline: StanfordCoreNLP): Seq[String] = {
    val doc = new Annotation(text) 
    pipeline.annotate(doc)
    val lemmas = new ArrayBuffer[String]()
    val sentences = doc.get(classOf[SentencesAnnotation]) 
    for (sentence <- sentences;
         token <- sentence.get(classOf[TokensAnnotation])) { 
            val lemma = token.get(classOf[LemmaAnnotation])
            // weed out garbage
            if (lemma.length > 2 && !stopWords.contains(lemma)
                && isOnlyLetters(lemma)) {
                    lemmas += lemma.toLowerCase
            } 
         }
    lemmas
}

val stopWords = sc.broadcast(
    scala.io.Source.fromFile("stopwords.txt").getLines().toSet).value

// initiate once per partition, instead of once per document
val lemmatized: RDD[Seq[String]] = plainText.mapPartitions(it => { 
    val pipeline = createNLPPipeline()
    it.map { case(title, contents) =>
        plainTextToLemmas(contents, stopWords, pipeline)
    }
})
```

### 6.4 Computing TF-IDF

```scala
import scala.collection.mutable.HashMap

val docTermFreqs = lemmatized.map(terms => {
    val termFreqs = terms.foldLeft(new HashMap[String, Int]()) {
        (map, term) => {
            map += term -> (map.getOrElse(term, 0) + 1) 
            map
        } 
    }
    termFreqs
})
docTermFreqs.cache() // will use at least twice
```

Use Hashmap to count
```scala
val zero = new HashMap[String, Int]()

def merge(dfs: HashMap[String, Int], tfs: HashMap[String, Int])
    : HashMap[String, Int] = { 
    tfs.keySet.foreach { term =>
        dfs += term -> (dfs.getOrElse(term, 0) + 1)
    }
    dfs
}

def comb(dfs1: HashMap[String, Int], dfs2: HashMap[String, Int])
    : HashMap[String, Int] = { 
    for ((term, count) <- dfs2) {
        dfs1 += term -> (dfs1.getOrElse(term, 0) + count)
      }
    dfs1
}
docTermFreqs.aggregate(zero)(merge, comb)
```

won't fit in to driver memory

call `docTermFreqs.flatMap(_.keySet).distinct().count()` will return 9014592 terms, too much, should keep top few terms

```scala
val docFreqs = docTermFreqs.flatMap(_.keySet).map((_, 1)). 
    reduceByKey(_ + _)
val numTerms = 50000
val ordering = Ordering.by[(String, Int), Int](_._2) 
val topDocFreqs = docFreqs.top(numTerms)(ordering)

// inverse term (global)
val idfs = docFreqs.map{
case (term, count) => (term, math.log(numDocs.toDouble / count))
}.toMap

val termIds = idfs.keys.zipWithIndex.toMap
val bTermIds = sc.broadcast(termIds).value

// tie all together by creating a TF-IDF vector
// sparse vector
import scala.collection.JavaConversions._
import org.apache.spark.mllib.linalg.Vectors

val vecs = docTermFreqs.map(termFreqs => { 
    val docTotalTerms = termFreqs.values().sum 
    val termScores = termFreqs.filter {
        case (term, freq) => bTermIds.containsKey(term) 
    }.map{
        case (term, freq) => (bTermIds(term), 
            bIdfs(term) * termFreqs(term) / docTotalTerms)
    }.toSeq
    Vectors.sparse(bTermIds.size, termScores) 
})
// size, list of term-score pair
```

### 6.5 Single Value Decomposition

- M = U S VT
    + U mxk, document space to concept
    + S kxk, concept space, diagonal matrix hold single value
    + V kxn, term space to concept
- wrap RDD of row vectors in a RowMatrix and call computeSVD

```scala
import org.apache.spark.mllib.linalg.distributed.RowMatrix

// cache, need multiple pass over data
termDocMatrix.cache()
val mat = new RowMatrix(termDocMatrix) valk=1000
val svd = mat.computeSVD(k, computeU=true)
```


### 6.6 finding important concepts

```scala
def topDocsInTopConcepts(
    svd: SingularValueDecomposition[RowMatrix, Matrix], 
    numConcepts: Int, numDocs: Int, docIds: Map[Long, String])
: Seq[Seq[(String, Double)]] = {
    val u = svd.U
    val topDocs = new ArrayBuffer[Seq[(String, Double)]]() 
    for (i <- 0 until numConcepts) {
        val docWeights = u.rows.map(_.toArray(i)).zipWithUniqueId()
        topDocs += docWeights.top(numDocs).map{ 
            case (score, id) => (docIds(id), score)
        } 
    }
    topDocs
}
```

same with topTerms

inspect first concepts
```scala
val topConceptTerms = topTermsInTopConcepts(svd, 4, 10, termIds) val topConceptDocs = topDocsInTopConcepts(svd, 4, 10, docIds) 

for ((terms, docs) <- topConceptTerms.zip(topConceptDocs)) {
    println("Concept terms: " + terms.map(_._1).mkString(", ")) 
    println("Concept docs: " + docs.map(_._1).mkString(", ")) 
    println()
}
```

output like
```
Concept terms: summary, licensing, fur, logo, album, cover, rationale,
      gif, use, fair
Concept docs: File:Gladys-in-grammarland-cover-1897.png,
      File:Gladys-in-grammarland-cover-2010.png, File:1942ukrpoljudeakt4.jpg,
      File:Σακελλαρίδης.jpg, File:Baghdad-texas.jpg, File:Realistic.jpeg,
      File:DuplicateBoy.jpg, File:Garbo-the-spy.jpg, File:Joysagar.jpg,
      File:RizalHighSchoollogo.jpg

Concept terms: disambiguation, william, james, john, iran, australis,
      township, charles, robert, river
Concept docs: G. australis (disambiguation), F. australis (disambiguation),
      U. australis (disambiguation), L. maritima (disambiguation),
      G. maritima (disambiguation), F. japonica (disambiguation),
      P. japonica (disambiguation), Velo (disambiguation),
      Silencio (disambiguation), TVT (disambiguation)
```

Try to filter image, ambiguation pages
```scala
def wikiXmlToPlainText(xml: String): Option[(String, String)] = {
    if (page.isEmpty || !page.isArticle || page.isRedirect || 
        page.getTitle.contains("(disambiguation)")) {
    }else{
        Some((page.getTitle, page.getContent))
    } 
}
```

Result is more reasonable.

### 6.6 Querying & Scoring with Low-Dimensional Representation

- Questions
    + term relevance
        * cosine similarity between column vectors
        * measure the angle between vectors
    + document relevance: likewise
    + document & set of terms: element at intersection
- however, all score come from shallow knowledge
    + rely on simple frequency count
    + LSA provide deeper understanding of concepts

### 6.7 Term-Term Relevance
```scala
import breeze.linalg.{DenseVector => BDenseVector} 
import breeze.linalg.{DenseMatrix => BDenseMatrix}

def topTermsForTerm(
        normalizedVS: BDenseMatrix[Double], 
        termId: Int): Seq[(Double, Int)] = {
    val rowVec = new BDenseVector[Double]( 
        row(normalizedVS, termId).toArray)

    val termScores = (normalizedVS * rowVec).toArray.zipWithIndex 
    
    termScores.sortBy(-_._1).take(10)
}

val VS = multiplyByDiagonalMatrix(svd.V, svd.s) 
val normalizedVS = rowsNormalized(VS)

def printRelevantTerms(term: String) {
    val id = idTerms(term) 
    printIdWeights(topTermsForTerm(normalizedVS, id, termIds)
}

printRelevantTerms("algorithm")

(algorithm,1.000000000000002), (heuristic,0.8773199836391916),
(compute,0.8561015487853708), (constraint,0.8370707630657652),
(optimization,0.8331940333186296), (complexity,0.823738607119692),
(algorithmic,0.8227315888559854), (iterative,0.822364922633442),
(recursive,0.8176921180556759), (minimization,0.8160188481409465)

 printRelevantTerms("radiohead")

(radiohead,0.9999999999999993), (lyrically,0.8837403315233519),
(catchy,0.8780717902060333), (riff,0.861326571452104),
(lyricsthe,0.8460798060853993), (lyric,0.8434937575368959),
(upbeat,0.8410212279939793), (song,0.8280655506697948),
(musically,0.8239497926624353), (anthemic,0.8207874883055177)
```


### 6.8 Term-Document Relevance
```scala

def topDocsForTerm(US: RowMatrix, V: Matrix, termId: Int) 
        : Seq[(Double, Long)] = {
    val rowArr = row(V, termId).toArray
    val rowVec = Matrices.dense(termRowArr.length, 1, termRowArr)
    val docScores = US.multiply(termRowVec)
    val allDocWeights = docScores.rows.map(_.toArray(0)). 
        zipWithUniqueId()
    allDocWeights.top(10)
}

def printRelevantDocs(term: String) {
    val id = idTerms(term) 
    printIdWeights(topDocsForTerm(normalizedUS, svd.V, id, docIds)
}

printRelevantDocs("graph")

    (K-factor (graph theory),0.07074443599385992),
    (Mesh Graph,0.05843133228896666), (Mesh graph,0.05843133228896666),
    (Grid Graph,0.05762071784234877), (Grid graph,0.05762071784234877),
    (Graph factor,0.056799669054782564), (Graph (economics),0.05603848473056094),
    (Skin graph,0.05512936759365371), (Edgeless graph,0.05507918292342141),
    (Traversable graph,0.05507918292342141)
```

## 7. Concurrent Networks with GraphX


### 7.1 Preparing the data
The MEDLINE Citation Index: A network analysis
```sh
mkdir medline_data
cd medline_data
wget ftp://ftp.nlm.nih.gov/nlmdata/sample/medline/*.gz
gunzip *.gz
ls -ltr
```

Format
```xml
<MedlineCitation Owner="PIP" Status="MEDLINE">
<PMID Version="1">12255379</PMID>
<DateCreated>
  <Year>1980</Year>
  <Month>01</Month>
  <Day>03</Day>
</DateCreated>
...
<MeshHeadingList>
...
  <MeshHeading>
    <DescriptorName MajorTopicYN="N">Intelligence</DescriptorName>
  </MeshHeading>
  <MeshHeading>
    <DescriptorName MajorTopicYN="Y">Maternal-Fetal Exchange</DescriptorName>
  </MeshHeading>
...
</MeshHeadingList>
...
</MedlineCitation>
```

```scala
import com.cloudera.datascience.common.XmlInputFormat 
import org.apache.spark.SparkContext
import org.apache.hadoop.io.{Text, LongWritable} 
import org.apache.hadoop.conf.Configuration

def loadMedline(sc: SparkContext, path: String) = { 
    @transient val conf = new Configuration() 
    conf.set(XmlInputFormat.START_TAG_KEY, "<MedlineCitation ") 
    conf.set(XmlInputFormat.END_TAG_KEY, "</MedlineCitation>") 
    val in = sc.newAPIHadoopFile(path, classOf[XmlInputFormat],
        classOf[LongWritable], classOf[Text], conf) 
    in.map(line => line._2.toString)
}
val medline_raw = loadMedline(sc, "medline")
```

### 7.2 Parsing XML with Scala XML lib

Since 1.2, Scala treat XML as a first-class data type, this means following is valid.
```
import scala.xml._
val cit = <Tag>data</Tag>
```

- unparse first citation record
    + scala.xml.Elem class: present an individual node in XML
    + contain building functions
        * `elem.label`
        * `elem.attributes`
        * `elem \ "MeshHeadingList" `  | retrieve child by name
        * `elem \\ "MeshHeading"` | nondirect child
```scala
val raw_xml = medline_raw.take(1)(0) 
val elem = XML.loadString(raw_xml)

def majorTopics(elem: Elem): Seq[String] = {
    val dn = elem \\ "DescriptorName"
    val mt = dn.filter(n => (n \ "@MajorTopicYN").text == "Y") 
    mt.map(n => n.text)
}
majorTopics(elem)
```

### 7.3 Analyzing MeSH Major Topic & Co-occurence

Sum Statistics
```scala
val topics: RDD[String] = medline.flatMap(mesh => mesh) 
val topicCounts = topics.countByValue() 
topicCounts.size
// 13,034 

val tcSeq = topicCounts.toSeq 
tcSeq.sortBy(_._2).reverse.take(10).foreach(println) 
//(Research,5591)
//(Child,2235)
//(Infant,1388)
//(Toxicology,1251)
//(Pharmacology,1242)
//(Rats,1067)
//(Adolescent,1025)
//(Surgical Procedures, Operative,1011)
//(Pregnancy,996)
//(Pathology,967)

// frequency distribution
val valueDist = topicCounts.groupBy(_._2).mapValues(_.size) 
valueDist.toSeq.sorted.take(10).foreach(println)
// (1,2599)
// (2,1398)
// (3,935)
// (4,761)
// (5,592)
// (6,461)
// (7,413)
// (8,394)
// (9,345)
// (10,297)
```

- Interest in co-occuring MeSH topics 
- generate all 2 subset with scala built-in combinations method
- 13,034 topics could generate 84,936,061 unordered co-occurrence
- actually only 259,920 pairs

```scala
val list = List(1, 2, 3)
val combs = list.combinations(2) 

// make sure list sorted in the same way
val combs = list.reverse.combinations(2) 
combs.foreach(println)

val topicPairs = medline.flatMap(t => t.sorted.combinations(2)) 
val cooccurs = topicPairs.map(p => (p, 1)).reduceByKey(_+_) 
cooccurs.cache()
cooccurs.count()

val ord = Ordering.by[(Seq[String], Int), Int](_._2) 
cooccurs.top(10)(ord).foreach(println)
...
(List(Child, Infant),1097)
(List(Rats, Research),995) 
(List(Pharmacology, Research),895) 
(List(Rabbits, Research),581) 
(List(Adolescent, Child),544) 
(List(Mice, Research),505)
(List(Dogs, Research),469) 
(List(Research, Toxicology),438) 
(List(Biography as Topic, History),435) 
(List(Metabolism, Research),414)
```

### 7.4 Network with GraphX

- GraphX is based on two specialized RDD
    + VertexRDD[VD]
        * RDD[(VertexId:Long,VD:Any)]
    + EdgeRDD[ED]
        * RDD[Edge[ED:Any]]
        * Edge is a case class that contains 
            - two VertexId values
            - edge attribute ED
- first: build identifier for topics
    + build-in `hashCode` method will have too much collision
    + `Hashing` from Google's Guava Library
- Generate Vertices
- Generate Edges
    + make sure left side VertexId(src) is smaller than right side VertexID(dst)
    + 

```scala
import com.google.common.hash.Hashing
def hashId(str: String) = { 
    Hashing.md5().hashString(str).asLong()
}
// did not deduplicate, graphX will do that
val vertices = topics.map(topic => (hashId(topic), topic)) 

// val uniqueHashes = vertices.map(_._1).countByValue()
// val uniqueTopics = vertices.map(_._2).countByValue() 
// uniqueHashes.size == uniqueTopics.size

import org.apache.spark.graphx._

val edges = cooccurs.map(p => { 
    val (topics, cnt) = p
    val ids = topics.map(hashId).sorted
    Edge(ids(0), ids(1), cnt)
})

val topicGraph = Graph(vertices, edges) 
topicGraph.cache()

vertices.count()
// 280823
topicGraph.vertices.count()
// 13034
```

### 7.5 Understand Network Structures

- Whenever investigating a new graph
    + first know about summary statistics
- Connected Components
    + travel from any vertex to any other
    + divide into subgraphs if not connected

```scala
val connectedComponentGraph: Graph[VertexId, Int] = 
    topicGraph.connectedComponents()
// return another Graph, vertex attribute replaced by vertexID Group

def sortedConnectedComponents( 
        connectedComponents: Graph[VertexId, _])
        : Seq[(VertexId, Long)] = {
    val componentCounts = connectedComponents.vertices.map(_._2).
        countByValue 
    componentCounts.toSeq.sortBy(_._2).reverse
}

val componentCounts = sortedConnectedComponents( connectedComponentGraph)

componentCounts.size
//1039

componentCounts.take(5).foreach(println)
// (-9222594773437155629,11915)
// (-6468702387578666337,4)
// (-7038642868304457401,3)
// (-7926343550108072887,3)
// (-5914927920861094734,3)
```

- largest component includes over 90% (11915/13034)
- understand why small component not connected
    + join VertexRDD with concept Graph
    + HIV-1 related topics but not co-occur in HIV-1 papers

```scala

val nameCID = topicGraph.vertices. 
    innerJoin(connectedComponentGraph.vertices) {
        (topicId, name, componentId) => (name, componentId) 
}
// return Vertex[(name, componentId)] = RDD[topicId, (name, componentId)]
val c1 = nameCID.filter(x => x._2._2 == topComponentCounts(1)._2) 
// choose the top2 group
c1.collect().foreach(x => println(x._2._1))
...
Reverse Transcriptase 
Inhibitors
Zidovudine Anti-HIV 
Agents Nevirapine
```

- How connectedComponents worked?
    + a series of iteration computation to identify each vertex belong to
    + During each phase
        * each vertex broadcast its own ID to each neighbor
        * then the smallest VertexID it has seen
    + until no ID changes 

### 7.6 Degree Distribution

```scala
val degrees: VertexRDD[Int] = topicGraph.degrees.cache()
// Degree Map RDD[Vertex, Degree:Int]
degrees.map(_._2).stats()
// (count: 12065, mean: 43.09, stdev: 97.63, max: 3753.0, min: 1.0)
```

- From Degree Map stats
    + count < Vertices.count(): some vertice have no edge
    + It is because

```scala
// Some citation in MEDLINE only have single major topic
sing = Medline.filter(x => x.size == 1)`
sing.count // 48611
singTopic = sing.flatMap(topic=>topic).distinct()
singTopic.count() // 8084
// Some topic never appear as pairs
topic2 = topicPairs.flatMap(p=>p)
singTpoic.substract(topic2).count() // 969
```

- special vertices
    + small mean : average connection is low
    + high max: 1+ popular topic connect with 1/3 nodes
    + should be same as vertice stats

```scala
def topNamesAndDegrees(degrees: VertexRDD[Int],
        topicGraph: Graph[String, Int]): Array[(String, Int)] = {
    val namesAndDegrees = degrees.innerJoin(topicGraph.vertices) {
        (topicId, degree, name) => (name, degree)
    }
    val ord = Ordering.by[(String, Int), Int](_._2) 
    namesAndDegrees.map(_._2).top(10)(ord)
}

topNamesAndDegrees(degrees, topicGraph).foreach(println)

(Research,3753)
(Child,2364)
(Toxicology,2019) 
(Pharmacology,1891) 
(Adolescent,1884) 
(Pathology,1781) 
(Rats,1573) 
(Infant,1568) 
(Geriatrics,1546) 
(Pregnancy,1431)
```

### 7.7 filtering out noisy edges

- Need to distinguish
    + the pair with meaningful semantic relationship
    + the pair happen to both appear frequently
- New weighting schema: Pearson's chi-squared test
    + 2x2 contingency table for A and B
    + count of presence/absence
        * YY YN NY NN YA NA YB NB
        * YN = document that have A but not B
        * T=total documents
    + chi^2 = T (YY*NN-YN*NY)^2 / (YA*NA*YB*NB)
- Build with Triplet
    +  EdgeTriplet class extends the Edge class by adding the srcAttr and dstAttr members which contain the source and destination properties 
    +  


```scala
val T = medline.count()
val topicCountsRDD = topics.map(x=>(hashId(x),1)).reduceByKey(_+_)
// create a new graph with existing edges
val topicCountGraph = Graph(topicCountsRdd, topicGraph.edges)

def chiSq(YY: Int, YB: Int, YA: Int, T: Long): Double = { 
    val NB=T-YB
    val NA=T-YA
    val YN=YA-YY
    val NY=YB-YY
    val NN=T-NY-YN-YY 
    val inner=(YY*NN-YN*NY)-T/2.0 
    T*math.pow(inner,2)/(YA*NA*YB*NB)
}

// apply to graph use mapTriplets Operator
// return new Graph, edge attribute is chi-square of each pair
val chiSquaredGraph = topicCountGraph.mapTriplets(triplet => { 
    chiSq(triplet.attr, triplet.srcAttr, triplet.dstAttr, T)
})
chiSquaredGraph.edges.map(x => x.attr).stats() 
// (count: 259920, mean: 546.97, stdev: 3428.85, max: 222305.79, min: 0.0)

// use as a meaning filter criterion
val interesting = chiSquaredGraph.subgraph( 
    triplet => triplet.attr > 19.5)
interesting.edges.count //    170664
```

### 7.8 Analyzing the Filtered Graph

```scala
val interestingDegrees = interesting.degrees.cache() interestingDegrees.map(_._2).stats()

(count: 12062, mean: 28.30,stdev: 44.84, max: 1603.0, min: 1.0)

topNamesAndDegrees(interestingDegrees, topicGraph).foreach(println) ...

// New top 10 vs Old top 10
(Research,1603)         (Research,3753)
(Pharmacology,873)      (Child,2364)
(Toxicology,814)        (Toxicology,2019) 
(Rats,716)              (Pharmacology,1891) 
(Pathology,704)         (Adolescent,1884) 
(Child,617)             (Pathology,1781) 
(Metabolism,587)        (Rats,1573) 
(Rabbits,560)           (Infant,1568) 
(Mice,526)              (Geriatrics,1546) 
(Adolescent,510)        (Pregnancy,1431)
```

### 7.9 Small-World Networks

- From 1998 Duncan Watts and Steven Strogatz
    + two small world properties
        * most nodes have small degree, blong to a dense cluster of other nodes
        * possible to reach any node in network from any other relatively quickly
    + metric that could be used to rank graph
- Clique
    + graph is complete, if every vertex connected to every other vertex
    + there may many complete subset, called cliques
    + large cliques => graph has locally dense structure
    + find cliques is NP-complete problem
- Understand local density without finding cliques
    + triangle count
        * 3-vertex complete graph
        * count = number of triangle that contain V
    + local clustering coefficient
        * actual triangle / possible triangle from k neighbors
        * C = 2t/k/(k-1)

```scala
// return a Graph VertexRDD contains tiangles
val triCountGraph = graph.triangleCount() 
triCountGraph.vertices.map(x => x._2).stats()
(count: 13034, mean: 163.05, stdev: 616.56, max: 38602.0, min: 0.0)

// possible triangle count
val maxTrisGraph = graph.degrees.mapValues(d => d * (d - 1) / 2.0)

// join and calculate LCC
val clusterCoefGraph = triCountGraph.vertices. 
    innerJoin(maxTrisGraph) { (vertexId, triCount, maxTris) => {
        if (maxTris == 0) 0 else triCount / maxTris }
}

// average LCC
clusterCoefGraph.map(_._2).sum() / graph.vertices.count()
// 0.2784
```

### 7.10 Average Path Length with Pregel

- similar to iterative process finding connected components
    + each vertex maintain (vertice, distance) map
    + iterative update
- based on Google Pregel paper
    + Pregel: a system for large-scale graph processing
        * use Bulk-Synchronous-Parallel(BSP) model
    + a number of open source replicas
        * Apache Giraph & Apache Hama, but too specialized
        * GraphX is more general, provide pregel operator
- how to use pregel
    + figure out what state to keep track of at each vertex
        * what data structure to use
        * in this problem, Map[VertexId, Int]
    + function(currentState, edges) => messages
    + function(message) => merge => update vertex

```scala
def mergeMaps(m1: Map[VertexId, Int], m2: Map[VertexId, Int]) 
        : Map[VertexId, Int] = {
    def minThatExists(k: VertexId): Int = {
        math.min(
            m1.getOrElse(k, Int.MaxValue), 
            m2.getOrElse(k, Int.MaxValue))
    }

    (m1.keySet ++ m2.keySet).map { 
        k => (k, minThatExists(k))
    }.toMap 
}

def update(id: VertexId,
           state: Map[VertexId, Int],
           msg: Map[VertexId, Int]) = { 
    mergeMaps(state, msg)
}

// construct message
// add incoming a by 1 and check same as existing
// if not, emote new map
def checkIncrement(
        a: Map[VertexId, Int], 
        b: Map[VertexId, Int], 
        bid: VertexId) = {
    val aplus=a.map{case(v,d)=>v->(d+1)} 
    if (b != mergeMaps(aplus, b)) {
        Iterator((bid, aplus)) 
    }else{
        Iterator.empty
    }
}

// for each edge, check update iterator from both part
def iterate(e: EdgeTriplet[Map[VertexId, Int], _]) = { 
    checkIncrement(e.srcAttr, e.dstAttr, e.dstId) ++ 
    checkIncrement(e.dstAttr, e.srcAttr, e.srcId)
}

// run pregel with update, iterate, mergeMaps
val start = Map[VertexId, Int]()
val res = mapGraph.pregel(start)(update, iterate, mergeMaps)

// extract path from (id, map(id, distance))
val paths = res.vertices.flatMap { case (id, m) => 
    m.map { case (k, v) =>
        if(id < k){ 
            (id, k, v)
        } else { 
            (k, id, v)
        } 
    }
}.distinct()

paths.cache()


// summary and print histogram

paths.map(_._3).filter(_ > 0).stats()
// (count: 2701516, mean: 3.57, stdev: 0.84, max: 8.0, min: 1.0)

val hist = paths.map(_._3).countByValue() 
hist.toSeq.sorted.foreach(println)

// (0,248)
// (1,5653)
// (2,213584)
// (3,1091273)
// (4,1061114)
// (5,298679)
// (6,29655)
// (7,1520)
// (8,38)
```


## 8. Geospatial Data on NYC Taxi

## 9. Estimating Financial Risk through Monte Carlo Simulation

## 10. Analyzing Genomics Data and the BDG Project
