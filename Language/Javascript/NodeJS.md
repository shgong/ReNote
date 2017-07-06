
## 1. Intro to Node.js

V8 Javascript Runtime: really fast, mostly C code.

__Could build:__
- Websocket Server
- File Upload Client
- Ad Server
- Real-Time Data Apps

__It is NOT__
- a web framework
- for beginners
- multi-threaded

#### Event Loop & Blocking

Javascript has no concept of threads, its model of concurrency is completely based around events. Has event loop (request, connection, close).

Typical blocking things in web service:
call out, read/write on database, calls to extensions


__Blocking & Non-Blocking Code__

```js
var fs = require('fs');

//Blocking
var contents=fs.readFileSync('/etc/hosts');
console.log(contents);

//Non-Blocking - Callback - much quicker
fs.readFile('/etc/hosts',function(err,contents){
    console.log(contents);
});
```


### Hello World
```js
var http=require('http'); 
http.createServer(function(request,response){ // register request event loop
    response.writeHead(200); //status code in header
    response.write('content'); // response body
    response.end();
}).listen(8080); //listen for connection on this port
console.log('Listening on port 8080...')
```

```sh
node hello.js  # Run Server
curl http://localhost:8080  # Get Response
```


### Read file and Response
```js
var http = require('http');
var fs = require('fs');

http.createServer(function(request, response) {
  response.writeHead(200, {
      'Content-Type': 'text/html' }); // response parameters
    fs.readFile('index.html',function(err,contents){
      response.end(contents); //can replace a write with end
    });
}).listen(8080);
```


## 2.Events
In Node, Many objects emit events: 

+ net.Server->request
+ fs.readStream->data,
+ custom event emitter

```js
var EventEmitter = require('events').EventEmitter;
var logger = new EventEmitter(); // Error, Warn, Info

logger.on('error', function(message){
    console.log('ERR: '+message);
});

logger.emit('error','Spilled Milk');
```

#### create Server syntax
```js
http.createServer(function(request,response){....});
// same as
var server=http.createServer();
server.on('request',function(request,response){...});
server.on('close',function(){...});
```


## 3.Stream
like channels to receive data, can be readable, writable or both
e.g. request: readable stream, response:writable stream

Readable stream can contain EventEmitter, emit events like readable and end.

pipe automatically end readable
```js
//options
reader.pipe(writer, { end: false });
```

```js
http.createServer(function(request,response){
    response.writeHead(200);
    request.on('readable',function(){
        var chunk=null;
        while(null !== (chunk=request.read())){
            response.write(chunk);
        }
    });
    request.on('end',function(){
        response.end();
    });
}).listen(8080);
```

```js
// use pipe for short
http.createServer(function(request,response){
    response.writeHead(200);
    request.pipe(response);
}).listen(8080);
```

```sh
curl -d 'hello' http://localhost:8080 //
```

#### file system streaming
```js
var fs=require('fs');

var file=fs.createReadStream("readme.md");
var newFile=fs.createWriteStream("readme_copy.md");
file.pipe(newFile); //pipe one into another
```

visit [gulp.js](http://gulpjs.com/) for more details
1854 gulp streaming plugins

#### Upload File

```js
http.createServer(function(request,response){
    var newFile=fs.createWriteStream("write.md");
    request.pipe(newFile);
    request.on('end',function(){
        response.end('uploaded!\n');
    });
}).listen(8080);
```

```sh
curl --upload-file read.md http://localhost:8080
```

#### Upload Progress
Awesome streaming due to nodes nature: all non-blocking.
Reason why create node.js is to deal with File Uploading
There are all the blocking server issues and progress display

```js

http.createServer(function(request,response){
    var newFile=fs.createWriteStream("write.md");
    var fileBytes = request.headers['content-length'];
    var uploadedBytes=0;

    request.on('readable',function(){
        var chunk=null;
        while(null !== (chunk=request.read())){
            uploadedBytes+=chunk.length;
            var progress=(uploadedBytes/fileBytes)*100;
            response.write("progress:"+parseInt(progress,10)+"%\n");
        }
    });  // readable - keep track of reading progress

    request.pipe(newFile);

    request.on('end',function(){
        response.end('uploaded!');
    });
}).listen(8080);
console.log('Listening on port 8080...')

```


```sh
curl --upload-file read.md http://localhost:8080 
```



## 4.Modules
modules like fs and http

### Export Syntax

custom_hello.js
```js
//only one method public
var hello=function(){
    console.log("hello!");
}
module.exports=hello;
```

custom_goodbye.js
```js
//multiple method public
var goodbye=function(){
   console.log("bye!");
}
exports.goodbye=goodbye
```

app.js
```js
var hello=require('./custom_hello');
hello();

var gb=require('./custom_goodbye');
gb.goodbye();
//or
require('./custom_goodbye').goodbye();
```

### http request module

make_request.js
```js
var http=require('http');

var makeRequest=function(message){
    var options = {
        host:'localhost',port:8080,path:'/',method:'POST'
    }
    var request=http.request(options,function(response){
        response.on('data',function(data){
            console.log(data);
        });
    });
    request.write(message);
    request.end();
}
module.exports=makeRequest;
```


### path
require('./make_request')   search in same directory

require('make_request') 
if current path is home/eric/my_app/app.js
search in node_moduls directory
- home/eric/my_app/node_modules/make_request.js
- home/eric/node_modules/make_request.js
- home/node_modules/make_request.js
- node_modules/make_request.js

### npmjs.org
find modules and packages for node, all kinds of open source repos
```sh
# search
npm search request
# local
npm install request
# global
npm install request -g 
```

global npm module can be used via commandline
but cannot be required in the app.js, still need local install

### defining dependencies
my_app/package.json
```json
{
    "name":"My App",
    "version":"1",
    "dependencies":{
        "connect":"1.8.7",
        "something":"~1"  //fetch latest between 1-2
        "something":"~1.8"  //fetch latest between 1.8-1.9
    }
}
```

so when you use `npm install`, depencies would be checked/fetched



## 5.Express
sinatra inpired web development framework for node.js

sinatra is a ruby framework
- easy route URLs to callbacks
- Middleware (from Connect)
- Environment based configuaration
- Redirection helpers
- File Upload

```sh
# --save: install and add to dependency file
npm install --save express
```

#### basic

```js
var express=require('express');
var app=express();
// Endpoint=root, __dirname=currentdirectory
app.get('/',function(request,response){
    response.sendFile(__dirname+"/index.html");
});
app.listen(8080);
```

#### express routes
e.g. get latest 10 tweets

```js
var request=require('request');
var url=require('url');

// : means dynamic
app.get('/tweets/:username',function(req,response){
    var username=req.params.username;
    // request options
    options={
        protocol:"http",
        host:"api.twitter.com",
        pathname:"/1/statuses/user_timeline.json",
        query:{screen_name:username, count:10}
    }
    var twitterUrl=url.format(options);
    request(twitterUrl).pipe(response);
});
// this don't work now, you need authenticate

```

#### put json data in browser

use ejs library

```
app.get('/tweets/:username',function(req,response){
    ...
    request(url,function(err,res,body){
        var tweets=JSON.parse(body);
        response.locals={tweets:tweets,name:req.params.username};
        response.render('tweets.ejs');
    })
```

tweets.ejs // define template render
// ruby like syntax: = to get value from expression
```html
<h1> Tweets for @ <%= name %></h1>
<ul>
<% tweets.forEach(function(tweet){%>
    <li><%=tweet.text%></li>
<% }); %>
</ul>
```


## 6.socket.io

build a real time app: Chattr
use websockets, socketio realtime connection
```
npm install --save socket.io
```

```js
var express=require('express');
var app=express();
var server=require('http').createServer(app);
// now io and server share the same server
var io=require('socket.io')(server);

io.on('connection',function(client){
    console.log('Client connected...');
    //new member join
    client.on('join',function(name){
        client.nickname=name;
    });

    //receive message
    client.on('messages',function(data){
        //get name first
        var nickname=client.nickname;
        //broadcast function to every other client
        client.broadcast.emit("messages",nickname + ":" + data);
        // emit to itself
        client.emit("messages",nickname + ":" + data); // can have multiple arguments after
    });
});

app.get('/',function(req,res){
    res.sendFile(__dirname+'/index.html');
});

server.listen(8080);
```

```html
<script src="/socket.io/socket.io.js"></script>
<script>
    var socket=io.connect('http://localhost:8080');

    socket.on('connect',function(data){
        $('#status').html('connected to chattr');
        nickname=prompt("What is your nickname?");
        server.emit('join',nickname);
    });

    //listen to the client.emit function
    socket.on('messages',function(data){
        //add to text in chat div 
        insertMessage(data);
    });
    // send message to server
    $('#chat_from').submit(function(e){
        var message=$('#chat_input').val();
        socket.emit('messages',message);
    });
</script>
```


## 7.Persisting data
persisting data, see previous messages

```js
// store at most 10 previous messages
var messages=[]; //disappear when server restart
var storeMessage=function(name,data){
    message.push({name:name,data:data});
    if(messages.length>10){
        message.shift(); //remove first one
    }
};
io.sockets.on('connection',function(client){

    client.on('join',function(name){
        client.nickname=name;
        messages.forEach(function(message){
            client.emit("messages",message.name+": "+message.data);
        });
    });

    client.on("messages",function(message){
        client.get("nickname",function(error,name){
            client.broadcast.emit("messages",name+":"+message);
            client.emit("messages",name+":"+message);
            storeMessage(name,message);
        });    
    });

});

```

could not persist data

use redis instead

#### Redis

Strings: SET, GET,APPEND, DESCR, INCR
Hashes: HSET, HGET, HDEL, HGETALL
Lists: LPUSH,LREM,LTRIM,RPOP,LINSERT
Sets:SADD,SREM,SMOVE,SMEMBERS
Sorted Sets: ZADD,ZREM,ZSCORE,ZRANK

```
npm install redis --save
```

```js
var redis=require('redis');
var client=redis.createClient();

//string
client.set("message1","hello,yes this is dog");
client.get("message1",function(err,reply){
    console.log(reply);
});

//list
var message="sss"
client.lpush("messages",message,function(err,reply){
    //keep first two and remove rest
    client.ltrim("messages",0,1);
    console.log(reply);
});

//0 -1 return all
client.lrange("messages",0,-1,function(err,messages){
    console.log(messages);
});
```

#### rewrite message=[] in redis

```js
var redisClient=redis.createClient();
var storeMesssage=function(name,data){
    //obj2str to store in redis
    var message= JSON.stringify({name:name,data:data});
    redisClient.lpush("messages",message,function(err,reply){
        client.ltrim("messages",0,9);
    });
};

io.sockets.on('connection',function(client){
    ...
    client.on('join',function(name){
        client.nickname=name;

        redisClient.lrange("messages",0,-1,function(err,res){
            messages=messages.reverse();
            messages.forEach(function(message)){
                client.emit("messages",message.name+": "+message.data);
            };
    });
    ...
});
```


#### members list
use set data structure

```js
client.on('join',function(name){
    client.broadcast.emit("add chatter",name);
    redisClient.smembers("names",function(err,names){
        names.forEach(function(name){
            client.emit('add chatter',name);
        });
    });
    redisClient.sadd("chatters",name);
});

client.on('disconnect',function(name){
    client.get("nickname",function(err,name){
        client.broadcast.emit("remove chatter",name);
        redisClient.srem("chatters",name);
    });
});

```

```js
socket.on('add chatter',function(name){
    var chatter=$('<li>'+name+'</li>').data('name',name);
        $("#chatters").append(chatter);
});
socket.on('remove chatter',function(name){
    $("#chatters li[data-name='"+name+"']").remove();
});

```

