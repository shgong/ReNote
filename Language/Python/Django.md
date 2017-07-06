
# Django 
named after: Django Reinhardt

Principle: 
- more productive with less code
- loose coupling
- DRY

Feature:
- Template
- URL mapping
- Form
- Package
- ORM: Object Relationship Matching api for SQL
- Admin: Can generate interface automatically


<br><hr>
## 1. Install

```
Virtualenv -p path/to/python myproject
. bin/activate
```
use python3 path to set up separate environment

```
pip install django
```

<br><hr>
## 2. Starting a Django Project
### Starting
```sh
/opt/local/Library/Frameworks/Python.framework/Versions/2.7/lib/python2.7/site-packages/django/bin
django-admin.py startproject boardgames
```

```sh
# run server
python manage.py runserver
```

### Django App

typical django prject consist of many apps
keep apps small, simple, reusable

App
- a python package
- a more-or-less complete web application
- own models, views, templates, url mappings

Add a new app folder to structure
```sh
python manage.py startapp appname
```

Add appname to INSTALLED_APPS in settings.py

### Adding a page
add response to main/view.py
```py
from django.http import HttpResponse
def home(request):
    return HttpResponse("Hello, world!")
```

add `url(r'','main.views.home')` to boardgames/url.py

### Url Mapping
prefix r: use regexp strings (begining and ending)
`r'^$'`  only match empty string to homepage, other page not found

### Views
Django views is Controllers in MVC framework

a view is callable
- take request
- status code, html, new instance

### templates
set templates html
and replace request function:
```
def home(request):
    return render(request,"main/home.html")
```

Variable in templates
```py
def home(request):
    return render(request,"main/home.html",
        {'message':'Hi~~'}})
```

### Static
static folder to put bootstrap, fonts, libraries, images
(you don't want to include at every app)

`django.contrib.staticfiles` included in INSTALLED_APPS
`STATIC_URL` and `STATICFILES_DIRS` at the bottom

`{% load staticfiles %}` at top of html
`href="{% static 'bootstrap/css/bottstrap.min.css' %}`
when use static in the template

### Model Template View
model: represent data, database table
template(view): generate html, presentation logic only
view(controller): http req/res,use model and call template


<br><hr>
## 3. Models

### example

python manage.py startapp tictactoe
edit models.py
```py
from django.db import models
from django.contrib.auth.models import User

# Create your models here.
class Game(models.Model):
    first_player = models.ForeignKey(User,related_name="games_first_player")
    second_player = models.ForeignKey(User,related_name="games_second_player")
    next_to_move = models.ForeignKey(User,related_name="games_to_move")
    start_time = models.DateTimeField(auto_now_add=True)
    last_active ＝ models.DateTimeField(auto_now=True)


class Move(models.Model):
    x = models.IntegerField()
    y = models.IntegerField()
    comment = models.CharField(mex_length=300)
    game = models.ForeignKey(Game)
```

```sh
python manage.py syncdb
```
auth systems
```
python manage.py sql tictactoe
```

Auto generate
```SQL
BEGIN;
CREATE TABLE "tictactoe_game" (
    "id" integer NOT NULL PRIMARY KEY AUTOINCREMENT,
    "first_player_id" integer NOT NULL REFERENCES "auth_user" ("id"),
    "second_player_id" integer NOT NULL REFERENCES "auth_user" ("id"),
    "next_to_move_id" integer NOT NULL REFERENCES "auth_user" ("id"),
    "start_time" datetime NOT NULL,
    "last_active" datetime NOT NULL
)
;
CREATE TABLE "tictactoe_move" (
    "id" integer NOT NULL PRIMARY KEY AUTOINCREMENT,
    "x" integer NOT NULL,
    "y" integer NOT NULL,
    "comment" varchar(300) NOT NULL,
    "game_id" integer NOT NULL REFERENCES "tictactoe_game" ("id")
)
;

COMMIT;
```

### Model Classes
- each model class maps to a database table
- each attribute represents a database field
- django use field class types for column/html widgets
- django generates a model api

### database commands
`python manage.py sql tictactoe`: print CREATE TABLE SQL 
`python manage.py syncdb`: create database tables for all apps


### admin interface
urls.py: `admin.autodiscover()`
tictactoe/admin.py

display string of objects in the interface:
```py
def __str__(self):
    return "{0} vs {1}".format(self.first_player, self.second_player)
```

after model changes
```sh
python manage.py dbshell
drop table tictactoe_game
python manage.py syncdb
```

can be easily customizable

### Model API
model classes have a manager instance: object
class attribute: Game.objects
- Game.objects.get(pk=1)
- Game.objects.all
- Game.objects.filter(status="A")
- Game.objects.exclude(status="A")

### Handle One to Many Relations
Python objects->SQL
one side: defined by a foreign key field
other side: a xxx_set attribute

set relation from move m to game g
```
m.game=g or g.move_set.add(m)
```

read documents for other relationships.

<br><hr>
## 4. Adding a User Home Page

add template in settings
it only search app folder by default for templates
not global template folder
```
TEMPLATE_DIRS = (os.path.join(BASE_DIR,"templates"),)
```

### Python URL patterns 
```py
urlpatterns += patterns(
    'django.contrib.auth.views',
    # using common prefix for all url under this pattern

    url(r'^login/', 'login',
        {'template_name':'login.html'},
        name="boardgames_login"),

    url(r'^logout/', 'logout',
        {'next_page':'boardgames_home'},
        name="boardgames_logout"),
)

```

- add patterns with +=
- pattern functions has a prefix string as first arguments
- views can receive keywords arguments
- url can be named

### Login and Logout views

django.contrib.auth.views.login
    have to provide template yourself
django.contrib.auth.views.out
    have to give next pages as argument
setting.py
```py
    LOGIN_URL='boardgames_login'
    LOGOUT_URL='boardgames_logout'
    LOGIN_REDIRECT_URL = 'boardgames_home'
```

use {% if user.is_authenticated %}
in templates to know user situation

### Template Language

https://docs.djangoproject.com/en/1.8/ref/templates/builtins/

Variables: {{var}}, render var from context

Tags: {% tag %}
- build-in tags: for, if, url, etc
- write custom tags

if: elif, else, endif
url: generate url by name
for: for item in list, empty, endfor
include: add another template 
        with argument1="" argument2=list


extend tag to extend a base template
use block content to overide

```html
{% extends "base.html" %}

{% block title %}

     Overview for {{user.username}}

{% endblock title%}

{% block content %}

<div class="well col-md-6">

{% include "tictactoe/game_list_snippet.html" with header="Games waiting for your move" games_list=waiting_games %}

{% include "tictactoe/game_list_snippet.html" with header="Other games" games_list=other_games %}

{% include "tictactoe/game_list_snippet.html" with header="Finished games" games_list=finished_games %}

</div>

{% endblock content %}

```

### url mappin

App has its own urls.py

include in project urls.py:
    `url(r'^prefix/', include('myapp.urls'))`
no $ sign as we need following texts

### decorators
restrict non-login user access certain page

from django.contrib.auth.decorators import login_required
@login_required

### template context
data is provided to template by request context
add values as a dictionary
`render(request,'myapp/index.html',{dictionary})`

all data should be present in the context
use view and model to retrieve data/logic



<br><hr>
## 5. Forms
add html form to model

### ModelForm
generate Html from a model class

use meta class to specify model

initial view
- http get
- initialize empty form instance

after submit
- http post
- initialize form from request.POST
- call form.is_valid()

validation errors
- render template with error again

ok:
- form.save
- django.shortcuts.redirect

### templates
{%csrf_token%}
{{form}}
<form> <submit>

### More options
verbose name
- first argument of fields: x=IntegerField("X Coordinates")
- on ForeignKey field use "verbose_name"

help_text

allow mode field blank
- null=True
- text field: blank=True

### Named Groups in URL

named groups in expression
- (?P<name>expr)
- (?P<pk>\d+)

capture values
- def some_view(request, pk)

url tag
- {% url 'tictactoe_accept' pk=inv.id %}


<br><hr>
## 6. Making Moves

### Fat Models, Skinny Views
logic goes in models, keep views/templates simple.
Why? DRY, Testing, Readability
m
example: 
implement get_absolute_url() on a Model
- provide canonical URL for model instance
- use reverse() function

django.core.urlresolvers.reverse
- get url for a specific view
- work like {% url %} template tag
- pass it argments: reverse('view_name', args=[4])

### Templates:Lookup

{{user.name}}, {% for m in game.move_set %}

a dot causes:
- dictionary lookup   dict["key"]
- attribute lookup
- method call          g.move_set.count()
- list-index lookup  list[5]

But you can NOT pass args to methods from template
eg. {% if game.is_users_turn(user) %}
    {% for i in range(10) %}
Write such logic in model/view instead

### template inheritance
{{block.super}}

### Model Meta Class
- add options to your model
- ordering
- latest/earliest
- table name

### ModelForm Validation
add customized validation
validate a single field: use validator
- y=models.IntegerField(validators=[MinValidatorValues(0)])

Not for a specific field
- should overwrite clean methods

<br><hr>
## 7. Odds and Ends
### Generic Views
function-based views
class-based views - function inherited and extended

application demo:
```py
from django.views.generic import ListView

class AllGamesList(ListView):
    model = Game
# very short view code
```

map to url
```py
from .view import AllGamesList

urlpatterns = patterns("",
    url(r'^game/all', AllGamesList.as_view()),
    # default template name: model_list.html
)

```

### Debugging
pip install django-debug-toolbar

add debug-toolbar to site, now your have a sidebar in the page
template, sql, all data, generate time

import pdb
pdb.set_trace()


revsys.com/django/cheatsheet
ccbv.co.uk
djangopackages
djangosnippets
### Further

