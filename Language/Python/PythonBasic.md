# Python

## 1. Basic

Feature

- Interpreted: Processed at runtime
- Interactive: have Interpreter
- Object-Oriented: support encapsulation
- Indentation: indicate scope with indentation

How Python Run

- Depending on how you use it, the Python interpreter may take the form of an executable program, or a set of libraries linked into another program.
- Depending on which flavor of Python you run, the interpreter itself may be implemented as a C program, a set of Java classes, or other.
- Whatever form it takes, the Python code you write must always be run by this interpreter.

Multi-line Statement

- inside [] {} () brackets
- inside """ quotes
- otherwise use line continuation char `\`

Variable
```py
a = b = c = 1    # multiple assignment
a, b = 1, 2      # multiple variable
del a,b          # delete reference
```

Type Conversion
```
str(x)   # object->string representation
repr(x)  # object->expression string
set(s)

frozenset(s) # set->frozenset
chr(x)    # int->char
unichr(x) # char->int
```

### Basic Operators
```
+ - * /       # Arithmetic
21 % 10 = 1   # Modulus
10 ** 2 = 100 # Exponent
-11 // 3= 4   # Floor Division

== != > < >= <=  # Comparison
= += -= **= //=  # Assignment

a << 1, a >> 2   # Binary Shift
a&b, a|b, a^b, ~a  # AND OR XOR Unary  (60,13) =>12 61 49 -61
    
in, not in       # Membership Operator
is, is not       # Identity Operator    
```

### Iteration
```py
list=[1,2,3,4]
it = iter(list) # this builds an iterator object

while True:
   try:
      print (next(it))
   except StopIteration:
      sys.exit()

# OR 
for i in it:
    print i
```

### Generator
```py
import sys
def fibonacci(n): #generator function
    a, b, counter = 0, 1, 0
    while True:
        if (counter > n): 
            return
        yield a
        a, b = b, a + b
        counter += 1
f = fibonacci(5) #f is iterator object

while True:
   try:
      print (next(f), end=" ")
   except StopIteration:
      sys.exit()
```

## 2. Types

### 2.1 Number

Number Type Conversion
```
int(x [,base])  # -> int
float(x)        # -> float
complex(r [,i]) # -> create complex number
```

Math Function
```
abs(x), ceil(x), floor(x)  # -> int
fabs(x)                    # -> float, math.fabs in 2.x
cmp(x,y)                   # (x>y)-(x<y)
exp(x), log(x), log10(x)
pow(x,y), sqrt(x)
min(x1,x2...), max(x1,x2...)
```

Random Number
```
choice(seq)     # random item from a list, tuple, or string.
shuffle(lst)    # random shuffe list
random()        # random float r in [0,1)
uniform(x,y)    # random float r in [x,y)
seed(x)         # call before random function, returns None
```

### 2.2 Strings

Python do not support char type.

String Operator
```
+       # Concatenation
*       # Repetition
[]      # Slice
[:]     # Range Slice, [a,b)
r'' R'' # Raw String, suppresses actual meaning of Escape characters.
```

Triple Quotes
```
para_str = """this is a long string that is made up of
several lines and non-printable characters such as
TAB ( \t ) and they will show up that way when displayed.
NEWLINEs within the string, whether explicitly given like
this within the brackets [ \n ], or just a NEWLINE within
the variable assignment will also show up.
"""
```

String Formatting

```py
print ("My name is %s and weight is %d kg!" % ('Zara', 21)) 

%[flags][width][.precision]type 

%s          # string conversion via str() prior to formatting
%i, %d      # signed decimal integer
%e, %E      # exponential notation
%f          # float point
%g, %G      # the shorter of %f and %e/%E

%8.2f       # total 8 characters, 2 for decimal
%0d         # zero padded
%-d         # left adjusted
% d         # leave a blank space if no minus sign 
%+d         # plus sign if no sign


formats = ["%5d","%- 5d","%+5d","%05d"]
value = 42.5
for i in formats:
    print ("%-5s:"+i) % (i, value)
```

output
```
%5d  :   42
%- 5d: 42  
%+5d :  +42
%05d :00042
```

String Methods
```
# Encode
decode(encoding='UTF-8',errors='strict')
encode(encoding='UTF-8',errors='strict')

# Search
count(str, beg= 0,end=len(string))
find(str, beg=0 end=len(string))            # return index or -1
rfind(str, beg=0,end=len(string))
replace(old, new [, max])     
expandtabs(tabsize=8) 

# Check
startswith(str, beg=0,end=len(string))
endswith(suffix, beg=0, end=len(string))    # return true/false
isalpha(),isdigit(),isalnum(),isnumeric(), isspace()  

# Letter-case
isupper(), islower(), istitle()              # is title-cased
upper(), lower(), title()
capitalize()               # Capitalizes first letter of string

# Formatting
center(width, fillchar)             # Centered string padded with fillchar
lstrip(),rstrip(), strip()
ljust(width[, fillchar]), rjust(width[, fillchar])

# Split & Join
" ".join(seq)
split(str="", num=string.count(str))

```

### 2.3 Lists

```
list1 = ['physics', 'chemistry', 1997, 2000];
list[2] = 2001
del list[2]

cmp(list1, list2)
len(list)
max(list), min(list)
list(seq)   # tuple -> list
```

List methods
```
list.append(obj)    #Appends object obj to list
list.count(obj)     #Returns count of how many times obj occurs in list
list.extend(seq)    #Appends the contents of seq to list
list.index(obj)     #Returns the lowest index in list that obj appears
list.insert(index, obj) #Inserts object obj into list at offset index
list.pop(obj=list[-1])  #Removes and returns last object or obj from list
list.remove(obj)    #Removes object obj from list
list.reverse()      #Reverses objects of list in place
```


Sort detail

```py
list.sort() 

def cmpfitness(item1, item2):
    if fitness(item1) < fitness(item2):
        return -1
    elif fitness(item1) > fitness(item2):
        return 1
    else:
        return 0

list.sort(cmpfitness)

sorted(list, key=lambda a:fitness(a))
sorted(numbers, key=lambda a:(a-4)**2)
```

### 2.4 Tuples

A tuple is a sequence of immutable Python objects. Cannot be changed.
Tuples use parentheses, whereas lists use square brackets.

```py
tup1 = ('physics', 'chemistry', 1997, 2000)
tup2 = (50,) # Have to include comma even if only one value
tup3 = tup1 + (2011,)
del tup1
tup1 = tup3
```

only have built in functions
```
cmp(list1, list2)
len(list)
max(list), min(list)
tuple(seq)   # list -> tuple
```

### 2.5 Dictionary

An empty dictionary without any items is written with just two curly braces, like this: {}.

```
dict = {'Name': 'Zara', 'Age': 7, 'Class': 'First'}
dict['Age'] = 8; 
del dict['Name'] # remove entry with key 'Name'
dict.clear()     # remove all entries in dict
del dict         # delete entire dictionary
```

there are no restriction for values, but for key

- No duplicate key is allowed
- Keys must be immutable


Built-in Dictionary Functions & Methods
```
cmp(dict1,dict2)
len(dict)
str(dict)
```

Dictionary Methods
```
dict.copy()                 # shallow copy

dict.get(key, default=None)
dict.setdefault(key, default=None)  # set when not get

dict.has_key(key)           # True False
dict.keys()                 # Returns list of dictionary dict's keys
dict.items()                # Returns a list of  (key, value) tuple pairs
dict.values()               # Returns list of dictionary dict's values


dict.fromkeys(seq, v=None)  # add (k from seq, v) into dict
dict.update(dict2)          # Adds dict2's key-values pairs to dict
dict(d)      # (k,v) tuple->dict
```


## 3 Function
```py
def functionname( parameters ):
   "function_docstring"
   function_suite
   return [expression]
```

### Pass by reference vs value

All parameters (arguments) in the Python language are passed by reference. It means if you change what a parameter refers to within a function, the change also reflects back in the calling function 
- change item of reference array: change outside array
- change reference to a new array: overwrite reference, do not change

```py
def printinfo( arg1=5, arg2="default", *vartuple ):
   "This is __docstring__"
   print (arg1)
   print (arg2)
   for var in vartuple:
      print (var)
   return

# Now you can call printinfo function
printinfo( 10, "pass", "any length" )
printinfo( arg2 = "pass")
```

### Lambda
```py
lambda [arg1 [,arg2,.....argn]]:expression
sum = lambda arg1, arg2: arg1 + arg2
s = lambda a: a[0]+s(a[1:]) if len(a)>0 else 0
fibo = lambda n: fibo(n-1)+fibo(n-2) if n>1 else 1
```

## 4 Module

``` py
import modname
from modname import name1,name2
from modname import *

set PYTHONPATH=/usr/local/lib/python

import math
print dir(math)  #string list of the names defined by a module.
```

### globals() and locals() Functions 

- If locals() called within a function, return all the names can access locally from that function.
- If globals() called within a function, return all the names can access globally from that function.
- The return type of both these functions is dictionary.

```py
a = 1
def func(b):
    """ scope example """
    c = 1
    print locals().keys()
    print globals().keys()
func("test")
```

output
```
['c', 'b']
['a', '__builtins__', '__file__', '__package__', 'func', '__name__', '__doc__']
```

### Package Structure

have several py file under a folder `folder`

Also create a `__init__.py` 
```py
from file1 import func1
from file2 import func2
from file3 import func3
```

Outside the folder
```
import folder
folder.func1()
folder.func2()
folder.func3()
```


### Exceptions

Assertion
```py
assert Expression[, Arguments]

def KelvinToFahrenheit(Temperature):
    assert (Temperature >= 0),"Colder than absolute zero!"
    return ((Temperature-273)*1.8)+32
```

Exception Handling
```py
try:
   fh = open("testfile", "w")
   fh.write("This is my test file for exception handling!!")
except IOError:
   print ("Error: can\'t find file or read data")
else:
   print ("Written content in the file successfully")
   fh.close()
```

```py
try:
   fh = open("testfile", "w")
   try:
      fh.write("This is my test file for exception handling!!")
   finally:
      print ("Going to close the file")
      fh.close()
except IOError:
   print ("Error: can\'t find file or read data")
```

Exception argument
```py
# Define a function here.
def temp_convert(var):
   try:
      return int(var)
   except ValueError, as Argument:
      print ("The argument does not contain numbers\n", Argument)

# Call above function here.
temp_convert("xyz")
```
