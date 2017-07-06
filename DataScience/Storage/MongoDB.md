

# MongoDB

## 1. Terminology
- Database: physical container for collections
- Collection: a group of MongoDB documents, like table
- Document: a set of key-value pairs
- Sharding: store data  records across machine
      * shard store data, high availabiility & consistency
      * config server: store metadata, exact 3
      * query routers: mongos instance, multiple in a cluster to divide request load
- MONGODUMP: create backup
- 
```json
{
   _id: ObjectId(7df78ad8902c)
   title: 'MongoDB Overview', 
   description: 'MongoDB is no sql database',
   by: 'tutorials point',
   url: 'http://www.tutorialspoint.com',
   tags: ['mongodb', 'database', 'NoSQL'],
   likes: 100, 
   comments: [  
      {
         user:'user1',
         message: 'My first comment',
         dateCreated: new Date(2011,1,20,2,15),
         like: 0 
      },
      {
         user:'user2',
         message: 'My second comments',
         dateCreated: new Date(2011,1,25,7,45),
         like: 5
      }
   ]
}
```

## 2. Data Model

- Design schema
    + user requirement
    + combine objects when used together
    + do joins while write, not read
    + optimize for frequent use cases
    + do complex aggregation in the schema
- blog post example: show from one collection only
```json
{
   _id: POST_ID
   title: TITLE_OF_POST, 
   description: POST_DESCRIPTION,
   by: POST_BY,
   url: URL_OF_POST,
   tags: [TAG1, TAG2, TAG3],
   likes: TOTAL_LIKES, 
   comments: [  
      {
         user:'COMMENT_BY',
         message: TEXT,
         dateCreated: DATE_TIME,
         like: LIKES 
      },
      {
         user:'COMMENT_BY',
         message: TEXT,
         dateCreated: DATE_TIME,
         like: LIKES
      }
   ]
}
```


## 3. Operations

```sh
show dbs
use mydb
db.dropDatabase()   #drop

#<-----Insert Document---->
db.mycol.insert({
   _id: ObjectId(7df78ad8902c),
   title: 'MongoDB Overview', 
   description: 'MongoDB is no sql database',
   by: 'tutorials point',
   url: 'http://www.tutorialspoint.com',
   tags: ['mongodb', 'database', 'NoSQL'],
   likes: 100
})

#<-----Update Document----->
db.mycol.save()
# same as insert if not specify _id. 
# otherwise will overwrite/replace old document

db.mycol.update({'title':'MongoDB Overview'},
    {$set:{'title':'New MongoDB Tutorial'}})
db.mycol.update({'title':'MongoDB Overview'},
    {$set:{'title':'New MongoDB Tutorial'}},{multi:true}) # multiple

#<-----Delete Document----->
db.mycol.remove({'title':'MongoDB Overview'})
db.mycol.remove({'title':'MongoDB Overview'}, 1) #remove only one

#<----Query Document---->
db.mycol.find()
db.mycol.find().pretty()  # formatted

# WHERE clause
db.mycol.find({"likes":50}).pretty()
db.mycol.find({"likes":{$lt:50}}).pretty()
db.mycol.find({"likes":{$gte:50}}).pretty()
db.mycol.find({"likes":{$ne:50}}).pretty()

# AND
db.mycol.find({key1:value1,key2:value2}).pretty()
# OR
db.mycol.find({$or:[{key1:value1},{key2:value2}]}).pretty()

#<-----Projection---->
# select only necessary fields
db.mycol.find({},{"title":1,_id:0})  # only show title
            # _id is always displayed when find(), set 0 to hide
#<------Limit,Skip,Sort------->
db.mycol.find({},{"title":1,_id:0}).limit(2)            # record 1-2
db.mycol.find({},{"title":1,_id:0}).limit(2).skip(1)    # record 2-3
db.mycol.find({},{"title":1,_id:0}).sort({"title":-1})  # descending

#<-----Indexing----->
# special data structure that store small portion of data
# in a easy to traverse way
db.mycol.ensureIndex({"title":1,"description":-1})

#<-----Aggregation--->
 db.mycol.aggregate([{
    $group : {_id : "$by_user", num_tutorial : {$sum : 1}}
    }])
# support: $sum, $avg, $min, $max
# $push (allow duplicate), $addToSet (not allow)
```

## 4. Java

- MongoDB JDBC Driver: mongo.jar
```java
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;

import com.mongodb.ServerAddress;
import java.util.Arrays;

public class MongoDBJDBC {

   public static void main( String args[] ) {
    
      try{   
         // To connect to mongodb server
         MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
            
         // Now connect to your databases
         DB db = mongoClient.getDB( "test" );
         System.out.println("Connect to database successfully");
         boolean auth = db.authenticate(myUserName, myPassword);
         System.out.println("Authentication: "+auth);
            
      }catch(Exception e){
         System.err.println( e.getClass().getName() + ": " + e.getMessage() );
      }
   }
}
```

## 5. Node.js

CREATE database

```js
// Retrieve
var MongoClient = require('mongodb').MongoClient;

// Connect to the db
MongoClient.connect("mongodb://localhost:27017/exampleDb", function(err, db) {
  if(err) { return console.dir(err); }

  db.collection('test', function(err, collection) {});
  // not actually create, until insert the first document
  db.collection('test', {strict:true}, function(err, collection) {});
  // check if exist, issue an error if do not
  db.createCollection('test', function(err, collection) {});
  // create collection, ignore if exist
  db.createCollection('test', {w:1}, function(err, collection) {});
  // create collection, issue an error if exist
});
```

CRUD
```js
// Retrieve
var MongoClient = require('mongodb').MongoClient;

// Connect to the db
MongoClient.connect("mongodb://localhost:27017/exampleDb", function(err, db) {
  if(err) { return console.dir(err); }

  var collection = db.collection('test');
  var doc1 = {'hello':'doc1'};
  var doc2 = {'hello':'doc2'};
  var lotsOfDocs = [{'hello':'doc3'}, {'hello':'doc4'}];

  collection.insert(doc1);
  // live analytics, loose data won't matter
  collection.insert(doc2, {w:1}, function(err, result) {});
  // get error if failed
  collection.insert(lotsOfDocs, {w:1}, function(err, result) {});

  // set or push
  collection.update({mykey:1}, {$set:{fieldtoupdate:2}}, {w:1}, function(err, result) {});
  collection.update({mykey:2}, {$push:{docs:{doc2:1}}}, {w:1}, function(err, result) {});

  // remove
  collection.remove({mykey:1});
  collection.remove({mykey:2}, {w:1}, function(err, result) {});
  collection.remove();

  // retrieve
    collection.find().toArray(function(err, items) {});
    // fetch all doc, but toArray cause a lot memory usage
    var stream = collection.find({mykey:{$ne:2}}).stream();
    stream.on("data", function(item) {});
    stream.on("end", function() {});
    //  deserialized as data events, then emit, finally end event
    collection.findOne({mykey:1}, function(err, item) {});
    // just retrieve one
});
```


## 6. Connect with Spark

```py
# set up parameters for reading from MongoDB via Hadoop input format
config = {"mongo.input.uri": "mongodb://localhost:27017/marketdata.minbars"}
inputFormatClassName = "com.mongodb.hadoop.MongoInputFormat"

# these values worked but others might as well
keyClassName = "org.apache.hadoop.io.Text"
valueClassName = "org.apache.hadoop.io.MapWritable"
  
# read the 1-minute bars from MongoDB into Spark RDD format
minBarRawRDD = sc.newAPIHadoopRDD(inputFormatClassName, keyClassName, valueClassName, None, None, config)
 
# configuration for output to MongoDB
config["mongo.output.uri"] = "mongodb://localhost:27017/marketdata.fiveminutebars"
outputFormatClassName = "com.mongodb.hadoop.MongoOutputFormat"
  
# takes the verbose raw structure (with extra metadata) and strips down to just the pricing data
minBarRDD = minBarRawRDD.values()
  
import calendar, time, math
  
dateFormatString = '%Y-%m-%d %H:%M'
  
# sort by time and then group into each bar in 5 minutes
groupedBars = minBarRDD.sortBy(lambda doc: str(doc["Timestamp"])).groupBy(lambda doc: (doc["Symbol"], math.floor(calendar.timegm(time.strptime(doc["Timestamp"], dateFormatString)) / (5*60))))

# define function for looking at each group and pulling out OHLC
# assume each grouping is a tuple of (symbol, seconds since epoch) and a resultIterable of 1-minute OHLC records in the group
  
# write function to take a (tuple, group); iterate through group; and manually pull OHLC
def ohlc(grouping):
    low = sys.maxint
    high = -sys.maxint
    i = 0
    groupKey = grouping[0]
    group = grouping[1]
    for doc in group:
        #take time and open from first bar
        if i == 0:
            openTime = doc["Timestamp"]
            openPrice = doc["Open"]
 
        #assign min and max from the bar if appropriate
        if doc["Low"] < low:
            low = doc["Low"]
 
        if doc["High"] > high:
            high = doc["High"]
 
        i = i + 1
        # take close of last bar
        if i == len(group):
            close = doc["Close"]
        outputDoc = {"Symbol": groupKey[0], 
        "Timestamp": openTime,
        "Open": openPrice,
            "High": high,
            "Low": low,
        "Close": close}
 
 
    return (None, outputDoc)
 
 resultRDD = groupedBars.map(ohlc)
  
resultRDD.saveAsNewAPIHadoopFile("file:///placeholder", outputFormatClassName, None, None, None, None, config)
```
