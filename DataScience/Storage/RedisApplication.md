# Redis - Applications

[View Redis Basics](#/DataScience/Database/Redis.md)

## 1 Caching Server

### Caching

Caching is for improving the response time and in turn increase user engagement. It is easiest way to scale an application, computing once and delivering from temporary storage. With less than 10 milliseconds access time for a record during typical benchmarking and complex data structure support, Redis is one of the best data stores when it comes to caching.

### Simple SET and GET cache servers

the web page to cache takes the URI as a parameter and generates the page. Then, we store the page contents as a value to a unique key. In this case, instead of storing URI as the key, we can use the hash of URI. 

Refer to the following PHP code sample in which we consider $redis to be the Predis object:
```PHP
$redis = new Predis\Client([
  'scheme' => 'tcp',
  'host'   => '127.0.0.1',
  'port'   => 6379,
]); 

$uri = $_SERVER['REQUEST_URI'];
if ($redis->exists(md5($uri)) {
  $contents = $redis->get(md5($uri));
  echo $contents;
  exit();
}
/* Operations to generate the page and assign the output to a variable $contents */

$redis->set(md5($uri), $contents);
$redis->expire(md5($uri), 6*60*60) //6 Hours validity
```

However, in this case, we will have 100K key-value pairs if we want to cache 100K pages. If we want to store the data efficiently, we need to take advantage of special encoding of small aggregate data types in Redis.

### Memory optimization technique

Redis can reduce the usage of RAM with a trade-off occurring with the CPU processing time, if the size of the data structure is small. 

The amount of optimization can be controlled easily by altering `hash-max-ziplist-entries 256` and `hash-max-ziplist-value 512` in the Redis configuration file.

> Read more at [Redis Document](http://redis.io/topics/memory-optimization.)

Once the number of entries or values exceeds the configured number, Redis converts the data back to normal encoding, which is transparent to the user and fast for smaller datasets.

In order to keep the size below the configured values, use the hashes data type now. Each value in a hash will be the cached data. 

Assume we have 10,000 users on our website and each user has a dedicated page in which they can view their wish list and personalized data with a constraint being that the data does not change for two hours.

> In case we need to continue using the URL as key, we can convert the URL into an integer variable using MD5 or any hashing functions with less collision in order to shard the data using the same logic.

In order to use special encoding, we need to store these 10,000 user pages into 20 hashes each with 512 entries for each user (`hash-max-ziplist-value 512`).

```
$userId = $_GET['userid'];
$key = "USER:" . floor($userId / 512);
$field = $userId % 512;

if ($redis->hexists($key, $field) {
  $contents = $redis->hget($key, $field);
  echo $contents;
  exit();
}
/* Operations to generate the page and assign the output to a variable $contents */

$redis->hset($key, $field, $contents);
```

### Scaling

assume we have around a million users and we want to cache the data across four different Redis instances, each consisting of 262144 users. So, we will go ahead and update the `redis.conf` file with updated configuration values: `hash-max-ziplist-entries 512`, `hash-max-ziplist-value 512`.

Since we want to use multiple servers, we need to maintain our own array of Redis instances and use similar functions to find the Redis instance to store the key name and field name.

In the case of PHP, Predis supports consistent hashing implicitly. You can simply achieve the same behavior by defining multiple instances. Predis will take the responsibility of choosing the Redis instance for you based on the hashing function implemented. All the major languages have a library, which supports client-side sharding. 
```
$redis = new Predis\Client([
    'tcp://127.0.0.1:6379?alias=first-node',
    'tcp://127.0.0.1:6381?alias=second-node',
    'tcp://10.232.55.50:6379?alias=third-node',
    'tcp://10.232.55.51:6379?alias=fourth-node',
]);
```


## 2 E-Commerce Inventory System
### Inventory Management

### Product Catalog

## 3 Autosuggest
### Autosuggest systems

### The faceted search

## 4 Real Time Analysis
### Session management and analysis 


## 5 Gaming

### Leaderboards

### Notification Center



## 6 Commenting System
### A nonthreaded comment system

### A threaded comment system

## 7 Advertising Network
### Ad Inventory
### Frequency Capping
### Keyword Targeting

## 8 Social Network
### Build Social Network
