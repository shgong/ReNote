# 1.Frist Steps

```
npm install express@4.9.x
```

Header
```js
var express=require('express');
var app=express();
```

#### Hello World
```js
app.get('/',function(req,res){
    //can use node API
    res.write("Hello world");res.end();
    //or express API
    res.send('Hello world')
});
app.listen(3000,function(){
    console.log('listening on port 3000');
});
```

#### responding with JSON
send function converts object/array to json
also string to html,
but better use EJS/Jade for server side rendering

```js
app.get('/blocks',function(req,res){
    var blocks=['a','b','c'];
    response.send(blocks);
    //or json to read better
    response.json(blocks);
});

//redirect
app.get('/parts',function(req,res){
    response.redirect(301,'/blocks');
});

```

```sh
# -i to print response headers
curl -i http://localhost:3000/blocks
```


# 2.Middleware
#### example

serve file with sendFile:
```js
response.sendFile(__dirname+'/public/index.html');
```

or we can add middleware to application stack
define default path to search index.html
```js
app.use(express.static('public'));
```

#### middleware

middleware is functions executed sequentially that access req and res.
In each middleware you can do validation,authentication, data parsing and so on.

there is next() function jump to next middleware
the flow stops once response is sent back to client,
call next() after that will cause error

#### example: static
only default middleware comes with express
only handle get&head function
otherwise pass processing to next function.
```js
exports=module.exports=function serveStatic(root,options){
    ...
    return function serveStatic(req,res,next){
        if(req.method!=='GET' && req.method!=='HEAD'){
            return next();
        }
        ...
        stream.pipe(res);
    }
}
```


#### AJAX calls
place all files under public folder

client.js
```js
$(function(){
    $.get('/blocks',appendToList);
    function appendToList(blocks){
        var list=[];
        for (var i in blocks){
            list.push($('<li>',{text:blocks[i]}));
        }
        $('.block-list').append(list);
    }
});
```

app.js
```js
app.use(express.static('public'));

app.get('/blocks',function(req,res){
    var blocks=[];
    res.json(blocks);
});
```


#### build middleware

use data to track time

logger.js
```js
module.exports=function(request,response,next){
    var start = +new Date();
    var stream = process.stdout;
    var url=request.url;
    var method=request.method;

    response.on('finish',function(){
        var duration=+new Date() - start;
        var message=method + 'to' + url + '\ntook' + duration + 'ms\n';
        stream.write(message);
    });

    next();
};
```

insert in app.js before static middleware
```
var logger=require("./logger");
app.use(logger);
```

#### further
read expressjs/morgan to learn how to write middleware

# 3.User Params

#### query string parameter
use query string parameter to restrict return numbers
`/blocks?limit=1`

add in app.get scope
```js
    if(request.query.limit>=0){
        //js slice to get part of the array
        response.json(blocks.slice(0,request.query.limit));
    }else{
        response.json(blocks);
    }
```

#### dynamic route parameter
create blocks/fixed without building static path
Dynamic Routes.

```js
var blocks={
    'Fixed':'Fastened securely in position',
    'Movable':'Capable of being moved',
    'Rotating':'Moving in a circle around its center'
};
app.get('/blocks/:name',function(req,res){
    var description=blocks[request.params.name];
    if (!description) {
        response.status(404)
        .json('No description found for'+request.params.name);
    } else {
        response.json(description); //also send 200 success
    }
});
```

#### normalization
curretn not work for lowercase
```
var name=request.params.name;
var block=name[0].toUpperCase()+name.slice(1).toLowerCase();
```

#### app.param
reuse parsing code

Define
```js
app.param('name',function(req,res,next){
    var name=request.params.name;
    var block=name[0].toUpperCase()+name.slice(1).toLowerCase();
    request.blockName=block;
    next();
});
```

Usage
```js
    var description=blocks[request.blockName];
```

However now webpage is listing block descriptions instead of names
cause we change blocks from array to object

```
response.json(blocks)
//change to
response.json(Object.keys(blocks));
```


# 4. Body Parser

npm install body-parser

#### Post
201 created status code
create form and POST method to /blocks 

client.js
```js
$(funtion(){
    ...
    $('form').on('submit',function(event){
        event.preventDefault();
        var form=$(this);
        var blockData=form.serialize();
        $.ajax({
            type:'POST', url:'/blocks',data:blockData
        }).done(function(blockName){
            appendToList([blockName]);
            form.trigger('reset');
        });
    });
    ...
    function appendToList(blocks){
        var list=[];
        var content,block;
        for (var i in blocks){
            block=blocks[i];
            content='<a href="/blocks/'+block+'">'+block+'</a>';
            list.push($('<li>',{html:content}));
        }
    }
});
```

app.js
```js
var bodyParser=require('body-parser');
// force use native querystring node library
var parseUrlencoded=bodyParser.urlencoded({extended:false});
var blocks={};

//middle word run first, then function
//use multiple route handlers for re-using middleware
app.post('/bocks',parseUrlencoded,function(req,res){
    var newBlock=request.body;
    blocks[newBlock.name]=newBlock.description;
    response.status(201).json("Success");
});
```


# 5. Routes Instance
all routes seem to be handling requests to similar paths

```js
//chaining functions
app.route('/blocks')
    .get(function(req,res){})
    .post(parseUrlencoded,function(req,res){});
app.route('/blocks/:name')
    .get(function(req,res){})
    .delete(function(req,res){});
```

#### Route Files

extract routes to modules
```js
var blocks=require('./routes/blocks')
//set block route under blocks path
app.use('/blocks',blocks)
```

routes/blocks.js
```js
var express=require('express');
var router=express.Router();
var bodyParser=require('body-parser');
var parseUrlencoded=bodyParser.urlencoded({extended:false});
var blocks={};

// relative to path mounted
router.route('/')
.get(function(req,res){})
.post(parseUrlencoded, function(req,res){})

router.route('/:name')
//for all routes under given path, an alternative for app.param
.all(function(req,res,next){
    var name=req.params.name;
    var block=name[0].toUpperCase()+name.slice(1).toLowerCase();
    request.blockName=block;
    next();
})
.get(function(req,res){})
.delete(function(req,res){})


module.exports=router;
```

