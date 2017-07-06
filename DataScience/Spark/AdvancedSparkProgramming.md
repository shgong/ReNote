# Advanced Spark Programming

## 1.Accumulators

- simple syntax to aggregate value from work nodes
- example: count events that occur for debugging purpose

### Accumulator empty line count

```py
file = sc.textFile(inputFile)

# Create Accumulator[Int] initialized to 0 
blankLines = sc.accumulator(0)

def extractCallSigns(line):
    global blankLines # Make the global variable accessible 
    if (line == ""):
        blankLines += 1 
    return line.split(" ")

callSigns = file.flatMap(extractCallSigns)
callSigns.saveAsTextFile(outputDir + "/callsigns") 
print "Blank lines: %d" % blankLines.value
```

or

```scala
val blankLines = sc.accumulator(0) 
val callSigns = file.flatMap(line => { 
    if (line == "") {
        blankLines += 1 
    }
    line.split(" ")
})
callSigns.saveAsTextFile("output.txt")
println("Blank lines: " + blankLines.value)
```

- accumulator works as follows
    + create in the driver by calling `sc.accumulator(initial)`， return type is org.apache.spark.Accumulator[T], T type infered from init value
    + worker code in closures can add to accumulator with += method (cannot read, write-only for workers)
    + driver can call value property  on accumulator
- use case
    + when there are multiple value to keep track of
    + when same value need to increase at multiple place


### Accumulator error count

```py
# Create Accumulators for validating call signs
validSignCount = sc.accumulator(0)
invalidSignCount = sc.accumulator(0)

def validateSign(sign):
    global validSignCount, invalidSignCount
    if re.match(r"\A\d?[a-zA-Z]{1,2}\d{1,4}[a-zA-Z]{1,3}\Z", sign):
        validSignCount += 1
        return True 
    else:
        invalidSignCount += 1 
        return False

# Count the number of times we contacted each call sign
validSigns = callSigns.filter(validateSign)
contactCount = validSigns.map(lambda sign: (sign, 1)).reduceByKey(lambda (x, y): x + y)

# Force evaluation so the counters are populated
contactCount.count()
if invalidSignCount.value < 0.1 * validSignCount.value:
    contactCount.saveAsTextFile(outputDir + "/contactCount") 
else:
    print "Too many errors: %d in %d" % (invalidSignCount.value, validSignCount.value) 
```

###　Fault tolerance

- Spark
    + rerun partition on another node when crashes
    + launch speculative copy on another node if much slower
    + may rerun to rebuild cached value that falls out of memory
- Interact with accumulator
    + for actions: spark apply update only once. use action like `foreach()`when want a reliable absolute value `rdd.foreach(count+=x)`
    + for RDD transformation: guarantee does not exist


## 2.Broadcast Variables

- broadcast: send a large read-only value to all worker nodes
- spark: auto send all variable referenced in closures to worker nodes
    + default task lauching mechanism optimized for small task size
    + might use same variable in multiple parallel operation, but Spark send it separately for each operation.

Country lookup Example

```py
# Look up the locations of the call signs on the RDD contactCounts. 
# We load a list of call sign prefixes to country code to support this lookup

signPrefixes = loadCallSignTable()
def processSignCount(sign_count, signPrefixes):
    country = lookupCountry(sign_count[0], signPrefixes) 
    count = sign_count[1]
    return (country, count)

countryContactCounts = contactCounts.map(processSignCount).reduceByKey((lambda x, y: x+ y))
```

if we had a larger table, the signPrefixes could be megabytes, making it expensive to send from master with each task. It would also resend when used again.

```py
signPrefixes = sc.broadcast(loadCallSignTable())

def processSignCount(sign_count, signPrefixes):
    country = lookupCountry(sign_count[0], signPrefixes.value) 
    count = sign_count[1]
    return (country, count)

countryContactCounts = contactCounts.map(processSignCount).reduceByKey((lambda x, y: x+ y))
```

can choose a data serialization format when broadcasting large values
- use Kryo serialization library

## 3.Work on Per-Partition Basis

- Avoid redoing setup work for each data item
    - open database connection
    - create random number generator
- use per-partition version of map & foreach
    + run only once for each partition RDD
    + like share connection pool

#### Process Example

```py

def processCallSigns(signs):
    """Lookup call signs using a connection pool"""
    # Create a connection pool
    http = urllib3.PoolManager()
    # the URL associated with each call sign record
    urls = map(lambda x: "http://73s.com/qsos/%s.json" % x, signs) # create the requests (non-blocking)
    requests = map(lambda x: (x, http.request('GET', x)), urls)
    result = map(lambda x: (x[0], json.loads(x[1].data)), requests) # remove any empty results and return
    return filter(lambda x: x[1] is not None, result)

def fetchCallSigns(input):
    """Fetch call signs"""
    return input.mapPartitions(lambda callSigns : processCallSigns(callSigns))
    
contactsContactList = fetchCallSigns(validSigns)

```

```scala
val contactsContactLists = validSigns.distinct().mapPartitions{ 
    signs =>
    val mapper = createMapper() 
    val client = new HttpClient() 
    client.start()
    signs.map {sign => createExchangeForSign(sign)
    }.map{ case(sign, exchange) =>(sign, readExchangeCallLog(mapper, exchange))
    }.filter(x => x._2 != null) // Remove empty CallLogs 
}


def createExchangeForSign(client: HttpClient, sign: String): (String, ContentExchange) = {
    val exchange = new ContentExchange()
    exchange.setURL(s"http://new73s.herokuapp.com/qsos/${sign}.json")
    client.send(exchange)
    (sign, exchange)
}


def readExchangeCallLog(mapper: ObjectMapper, exchange: ContentExchange): Array[CallLog] = {
    exchange.waitForDone()
    val responseJson = exchange.getResponseContent()
    val qsos = mapper.readValue(responseJson, classOf[Array[CallLog]])
    qsos
  }

def loadCallSignTable() = {
  scala.io.Source.fromFile("./files/callsign_tbl_sorted").getLines()
    .filter(_ != "").toArray
}
```

#### Function

When operating on a per-partition basis, Spark gives our function an Iterator of the elements in that partition. To return values, we return an Iterable. 

- mapPartitions(): Iterator[T]=>Iterator[U]
- mapPartitionsWithIndex(): (Int,Iterator[T])=>Iterator[U]
- foreachPartition(): Iterator[T]=>Unit

#### Average

```py
def combineCtrs(c1, c2):
    return (c1[0] + c2[0], c1[1] + c2[1])
def basicAvg(nums):
    nums.map(lambda num: (num, 1)).reduce(combineCtrs)
```

#### Average with mapPartitions

```py
def partitionCtr(nums):
    """Compute sumCounter for partition""" 
    sumCount = [0, 0]
    for num in nums:
        sumCount[0] += num
        sumCount[1] += 1 
    return [sumCount]

def fastAvg(nums):
    sumCount = nums.mapPartitions(partitionCtr).reduce(combineCtrs) 
    return sumCount[0] / float(sumCount[1])
```

make an object for aggregating result of different type
- convert to tuple RDD for each element
- convert to tuple once per partition => faster

## 4.Piping to External Program

- If none of scala, java or python does what you need.
- Call pipe() method on RDDs, write parts of jobs in any language with Unix standard streams

./src/R/finddistance.R
```
#!/usr/bin/env Rscript
library("Imap")
f <- file("stdin")
open(f)
while(length(line <- readLines(f,n=1)) > 0) {
  # process line
  contents <- Map(as.numeric, strsplit(line, ","))
  mydist <- gdist(contents[[1]][1], contents[[1]][2],
                  contents[[1]][3], contents[[1]][4],
                  units="m", a=6378137.0, b=6356752.3142, verbose = FALSE) 
  write(mydist, stdout())
}
```

Driver Program
```py
# Compute the distance of each call using an external R program
distScript = "./src/R/finddistance.R" 
distScriptName = "finddistance.R" 
sc.addFile(distScript)

def hasDistInfo(call):
    """Verify that a call has the fields required to compute the distance"""
    requiredFields = ["mylat", "mylong", "contactlat", "contactlong"]
    return all(map(lambda f: call[f], requiredFields)) 

def formatCall(call):
    """Format a call so that it can be parsed by our R program"""
    return "{0},{1},{2},{3}".format( call["mylat"], call["mylong"], call["contactlat"], call["contactlong"])

pipeInputs = contactsContactList.values().flatMap(
    lambda calls: map(formatCall, filter(hasDistInfo, calls)))

distances = pipeInputs.pipe(SparkFiles.get(distScriptName)) 
print distances.collect()s
```


```scala
val distScript = "./src/R/finddistance.R"
val distScriptName = "finddistance.R"
sc.addFile(distScript)

val distances = contactsContactLists.values.flatMap(x => x.map(y =>
s"$y.contactlay,$y.contactlong,$y.mylat,$y.mylong")).pipe(Seq( SparkFiles.get(distScriptName)))
println(distances.collect().toList)
```

## 5.Numeric RDD Operations 

- statistics
    + count()
    + mean()
    + sum()
    + max()
    + min()
    + variance()
    + sampleVariance()
    + stdev()
    + sampleStdev()

```scala
val distanceDouble = distance.map(string => string.toDouble)
val stats = distanceDoubles.stats()
val stddev = stats.stdev
val mean = stats.mean
val reasonableDistances = distanceDoubles.filter(x => math.abs(x-mean) < 3 * stddev) println(reasonableDistance.collect().toList)
```
