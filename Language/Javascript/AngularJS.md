# AngularJS

responsive, fast, easy to test
Modern Api Driven Application
Client js framework add interactivity to html

<hr>

## Module & Controller

- Directive

```html
<body ng-controller="StoreController">
<body ng-app=“store”>  // directive - run this module when page loads
```


- Expression: insert dynamic value into html

```html
{{ 4+6 }} {{“hello” + “you"}}
```


#### html
```html
    <html ng-app="store">  // app name
    <body ng-controller="StoreController as store"> // controller name， as alias
        <div ng-repeat="product in store.products">
            <h1> {{product.name}} </h1>
            <h2> ${{product.price}}</h2>
            <button ng-show="product.canPurchase"> Add to Cart </button>    // use to show/hide
        </div>
    </body>
```
#### js

```javascript
( function(){   //wrap js in a closure is a good habit
    var app=angular.module(’store’,[]);         // Module(name,dependency)
    app.controller('StoreController',function(){   //Controller
        this.product=gems;              //make gem object a property of the controller
    });
cheatsheet
    var gems=[{                         // Array of products
        name:"Dodecahedron",
        price:2.95,
        canPurchase:false;
    },{
        name:"Pentagondron",
        price:5.95,
        canPurchase:false;
    }

    ]
})();

```

<br><hr>

### filters :  {{data | filter:options}}

| data | filter | Result | Notes |
|------|--------|---------|-----------------|
| ‘1388123412323’ | date:'MM/dd/yyyy @ h:mma' |  12/27/2013@2:50am | |
| ‘abcd’ | uppercase | ABCD | |
| ‘My Descriptions’ | limitTo:8 |  My Descr   |  useful when display only n products |
| ‘product in store.products’ | orderBy:'-price' |  Sorted  |  - means descending |

#### image

src not working as browser try to load before angularJS works

use `<img ng-src={{product.image[0].full}}>` instead

<br><hr>

### bootstrap tab

#### html
```html
    <section ng-init="tab=1">                                       //initialize
        <ul class="nav nav-pills">
            <li ng-class="{active: tab===1 }">                      // use value to set class
                <a href ng-click="tab = 1"> Description </a></li>   // assign value
            <li ng-class="{active: tab===2 }">
                <a href ng-click="tab = 2"> Specification </a></li>
            <li ng-class="{active: tab===3 }">
                <a href ng-click="tab = 3"> Review </a></li>
        </ul>
        {{tab}}                                     // define a two way data binding
    </section>

    <div class="panel" ng-show="tab===1">           // tab respond display
        <h4> Description </h4>
        <p>{{product.description}}</p>
    </div>
```

wrap logic in html page looks dirty


#### html
```html
    <section ng-controller="PanelController as panel">
        <ul class="nav nav-pills">
            <li ng-class="{'active':panel.isSelected(1) }">
                <a href ng-click="panel.SelectTab(1)"> Description </a></li>
        </ul>
    </section>
```
#### js
```js
    app.controller('PanelController', function(){
        this.tab=1;
        this.SelectTab=function(setTab){
            this.tab=setTab;
        }
        this.IsSelected=function(checkTab){
            return this.tab===checkTab;
        }
    });
```


## Forms， Models & Validations

#### html
```html
    <form name="reviewForm">

        <blockquote>                // live preview
        <b>Stars: {{review.stars}}</b>
        {{review.body}}
        <cite> by: {{review.author}} </cite>
        </blockquote>

        <select ng-mode="review.stars">     // bind properties with forms
            <option value="1"> 1 star <option>
            ...
        </select>
        <textarea ng-model="review.body"></textarea>
        <label> by: <label>
        <input ng-model="review.author" type="email" />
        <input type="submit" value="Submit" />
    </form>
```
better use ReviewController to organize

### Submission / Validation

###js
```js
    app.controller("ReviewController",function(){
        this.review={};
        this.addReview = function(product) {
            product.reviews.push(this.review);
            this.review={};
        };
    });
```
###html
```html
    <form name="reviewForm" ng-controller="ReviewController as reviewCtrl" ng-submit="reviewForm.$valid && reviewCtrl.addReview(product)" novalidate>      // turn down default validation, check valid

      <!--  Live Preview -->
      <blockquote >
        <strong>{{reviewCtrl.review.stars}} Stars</strong>
        {{reviewCtrl.review.body}}
        <cite class="clearfix">—{{reviewCtrl.review.author}}</cite>
      </blockquote>

      <!--  Review Form -->
      <h4>Submit a Review</h4>
      <fieldset class="form-group">
        <select ng-model="reviewCtrl.review.stars" class="form-control" ng-options="stars for stars in [5,4,3,2,1]" title="Stars" required>
          <option value="">Rate the Product</option>
        </select>
      </fieldset>
      <fieldset class="form-group">
        <textarea ng-model="reviewCtrl.review.body" class="form-control" placeholder="Write a short review of the product..." title="Review" required></textarea>
      </fieldset>
      <fieldset class="form-group">
        <input ng-model="reviewCtrl.review.author" type="email" class="form-control" placeholder="jimmyDean@example.org" title="Email" required/>
      </fieldset>
      <fieldset class="form-group">
        <input type="submit" class="btn btn-primary pull-right" value="Submit Review" />
      </fieldset>
    </form>
```

#### css

```css
    .ng-pristine.ng-invalid{.before-type}   
    .ng-invalid.ng-dirty{.invalid}    
    .ng-valid.ng-dirty{.valid}   
```


### Custom Directives

#### ng-include: reuse html templates
```html
    <h3 ng-include="'product-title.html'"></h3>

    <product-title></product-title>  //element
    <h3 product-title></h3>         // attribute
```
#### configuration object
```js
    app.directive('productTitle',function(){
        return{
            restrict:'E',  // type of directive: E for element directive
                                              // A for attribute directive
            templateUrl:'product-title.html',
            controller:function(){

            },
            controllerAs: 'panels'       // define alias, also act as controller

        };
    })
```

### dependencies

pull out product related directives to product.js, define a new module called 'store-products', and add it in the store dependency.

### HTTP method
array of product data, pull from api
```js
    $http({method:'Get',url:'/products.json'});
    $http.get('/products.json',{apiKey:'myApiKey'});
```
which return a promise with success() and error();

use by controller : dependency injection
```js
    app.controller('StoreController',['$http', function($http){
        var store=this;   // this won't work in the http scope
        store.product=[];
        $http.get('/product.json').success(function(data){
            store.product=data;
        });
    }]);
```
also could post put delete


## Routes

maintain good directories: js folder for controllers, filters, services and directives. html templates all in its own folder with a main index file. Need to define route to load templates.

include ngRoute in main app.js
```js
    angular.module("NoteWrangler",['ngRoute'])
```
work on routes.js
```js
    // don't set app module to a variable like before
    var app=angular.module();
    app.directive();

    //better re-declare module
    angular.module('NoteWrangler').config(function($routeProvider){});

    // usage
    .when(path,route)   //matched definition
    .otherwise(params)  //otherwise


    //example
    angular.module('NoteWrangler')
    .config(function($routeProvider){
        $routeProvider.when('/notes', {
            templateUrl:'/templates/pages/notes/index.html'
            controller:"NotesIndexController",
            controllerAS:"indexCtrl"
        })
        .when('/notes/:id', {               //parameter
            templateUrl:'/templates/pages/notes/show.html'
            controller:"NotesIndexController",
            controllerAs:"indexCtrl"
        })
        .when('/users', {
            templateUrl:'/templates/pages/users/index.html'
        })
        .otherwise({redirectTo:'/'});
    });
    // use redirectTo / template wisely


    // controller
    angular.module('NoteWrangler')
    .controller('NotesIndexController',fucntion($http, $routeParams){
        var controller=this;
        $http({method:'GET',url:'/notes'+$routeParams.id})
        .success(function(data){
            controller.notes=data;
        });
    });

    angular.module('NoteWrangler')
    .controller('NotesCreateController',fucntion($http){
        var controller=this;
        this.saveNote=function(note){
            $http({method:'POST',url:'/notes'，data:note})
        });
    });
```

html
```html
    // Main HTML: insert routes here
    <div ng-view></div>

    // Notes HTML
    <a class="cards-notes" ng-repeat="note in indexCtrl.notes"
                                ng-href="#/notes/{{note.id}}">
        <div ng-if="icon">
            <i class="icon icon-card {{note.icon}}"></i>
        </div>

        <h2> {{note.title}}</h2>
    </a>

    // show HTML
    <div class="card">
        <h1>{{note.title}}</h1>
        ....
    </div>
```



## Directive with scope

### $scope
```js
    .controller('name',['$scope',function($scope){
    }]);

    controller:function($scope){
        $scope.header="Title";
    },

equivalent to declare controller Alias in the html

    this.header - {{card.header}}
    $scope.header - {{header}}
```

### scope
//default, or {} if isolated
```js
    angular.module("NoteWrangler")
    .directive("nwcard", function(){
        var num=1;
        return{
            restrict:"E",
            templateUrl:"example/nw-card.html",
            scope: {
                header:"@"  // @ passing in a string
                            // =  two way binding / also can remove {{}}
            },
            controller: function($scope){
                $scope.header="Title"+ num++ ;
                // actually it is set on parent
            };
        }
    });
```
In html
```html
    <nw-card>  </nw-card>
    <nw-card>  </nw-card>
    <nw-card>  </nw-card>
    <nw-card>  </nw-card>
```
this way will only show 4, Title4, each time number update,
directive will inherit from parent scope by default

Add a line will do the trick: `scope:{}`

but we don't have access to {{note.header}} when isolated,
pass it in the html instead:  `<nw-card header={{note.title}}""</nw-card>`


### link

when toggle hidden item in each nw-card, using jquery will search the whole DOM, bad practice.

use link function which runs after directive compiled and linked up.

```js
    return {
        link: funciton(){
            $("div.card").on("click",function(){
                $("div.card p").toggleClass("hidden");
            });
        }
    } // still search entire dom  (div.card)


    return {

        scope:{
            header:'=',
            body:'=',
        }
        link: funciton(scope, element, attrs){
            element.on("click",function(){
                element("div.card p").toggleClass("hidden");
                scope.body=markdown.toHTML(scope.body);
                // will only show html tags,
                // use ngbindhtml and $sce to solve this

                scope.body=$sce.trustAsHtml( markdown.toHTML(scope.body));
            });
        }
    }
    // element refers to the outermost element of template
    // attr of directive like header in nw-card
```
ngbindhtml
```html
    <a ng-href="#/notes/{{id}}" >{{body}}</a>  // not translate
    <a ng-href="#/notes/{{id}}" ng-bind-html="body"></a>
```


## Service

use service to fetch data and reuse http request

service recipes:
- value: share a value
- factory: share method and object
- service: share instances / rarely used
- provider: share factory with config
- constant: share config value / rarely used




#### Factory: sharing function and objects
```js
    angular.module("")
    .factory("Note",function NoteFactory(){
        return{
            all:function(){
                return $http({method:'GET',url:"/notes"});
            },
            create:function(note){
                return $http({method:'POST',url:"/notes",data:note});
            }
        },
    });

//use

    .controller('Ctrl',["Note",function($scope,Note){
        Note.all().success(function(){...});
    }]);
```
#### example

gravatar image serviceL email - hash - url - template. use a service to do this.
```js
    angular.module("NoteWrangler")
    .factory("Gravatar", function GravatarFactory(){
        var avatarSize=80;
        var avatarUrl="http://www.gravatar.com/avatar";
        return{
            generate: function(email){
                return avatarUrl+ryptoJS.MD5(email)+"?size="+avatarSize.toString();
            }
        }
    });
```
can simplify when only one function

    `return function(email){...}`

and call to use

    `Gravatar("email@email.com")`


#### Provider

config factory parameters.

```js
//change return to $get
    return function(email){...}

    this.$get = funtion($http){
        return function(email){...}
    }
    // $http can be removed from head

//can set configurable items

    this.setSize=function(size){avatarSize=size;}

//config in app.js

    angular.module("NoteWrangler", ["ngRoute"])
    .config(function (GravatarProvider){ // pass function name here
        GravatarProvider.setSize(100);
    });
```
#### ng-resource module

inject ngResource to main app.js
```js
//simplify factory:

    angular.module("NoteWrangler")
    .factory("Note",['$resource',function NoteFactory($resource)){
        return $resource("/notes/:id",{},{});
    }]);

//reform controller

    Note.find($routeParams.id).success(funtion(data){
        $scope.note=data;
    });
    // TO
    $scope.note=Note.get({id:$routeParams.id});

    Note.all().success(function(data)){
        $scope.notes=data;
    }
    //TO
    $scope.notes=Note.query();

    Note.create(note)
    //TO
    $scope.note=new Note();
    Note.$save(note);

// customize

    return $resource("/notes/:id",{},{
        tweetIt: {
          method: 'PUT'
        }
    });
```


## Reusable directives

sort note by categories:`<nw-category-select>`

```js
    directive("nwCategorySelect",function(Category){
        return{
            replace:true,
            restrict:'E',
            templateUrl:"...",

            controller: function($scope){

                this.getActiveCategory=function(){
                    return $scope.activeCategory;
                }

                this.setActiveCategory=function(category){
                    $scope.activeCategory=category.name;
                } // should share with child items

            },

            link: function(scope,element,attrs){
                scope.categories=Category.query();
            }
        };
    });


    directive("nwCategoryItem",function(){
        return{
            replace:true,
            restrict:'E',
            templateUrl:"...",

            scope:{
                category:'='
            },

            require: "^nwCategorySelect",  //^ means parent
            link: function(scope,element,attrs,nwCategorySelectCtrl){
                scope.makeActive=function(){
                    nwCategorySelectCtrl.setActiveCategory(scope.category); //bind to ng-click
                }

                scope.categoryActive=function(){
                    return nwCategorySelectCtrl.getActiveCategory()===scope.category.name;
                }
            };

            // 4th params of link
        };
    });

    // controller, function-this,  variable-$scope, return this?
```
#### filters to sort

the filter filter, select subset of items
```js
    {{ ["stings","stings"] | filter:'a' }} - select all words containing a
    {{ noteArray | filter: {category: {name:'Question'}} }}
```
to customize
```html
    <a ng-repeat="note in notes | filter: {category: {name:activeCategory}}" ng-href="" class="">

    <nw-category-select active-category="category"></nw-category-select>
    // 2 way bind
```

```js
    angular.module()
    .directive('nwCategorySelect',fucntion(Category)){
        return{
            ...
            scope:{
                activeCateory:'='
            }
        }
    }
```

Search
```html
    <input ng-model="searchTerm.title"/>
    note in notes| filter:searchTerm
```
Search wildcard
```html
    ng-model="search.$"
```
Chaining filters
```js
    note in notes| filter:activeCategory | filter:search
```

### Third Party Plugin

e.g. bootstrap tooltip.js

title attribute directive to override html
```js
.directive("title", function($timeout){
    return function(scope,element){
    // shorter way of return link: only one function

        $timeout(function(){
            // otherwise only show "{{header}}" in tooltip
            element.tooltip({container:"body"});
        });

        scope.$on('$destroy', function(){
            element.tolltip('destroy');
        });
    };
}
```

## when to use controller, link

use controller when you want to share function with other directives

other time use link
