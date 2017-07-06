# Python Advanced

## 1. Object Oriented

### Class Example
```py
class Employee:
   'Common base class for all employees'
   # class variable for all instance
   empCount = 0

   def __init__(self, name, salary): # Constructor
      self.name = name
      self.salary = salary
      Employee.empCount += 1
   
   def displayCount(self):
     print "Total Employee %d" % Employee.empCount

   def displayEmployee(self):
      print "Name : ", self.name,  ", Salary: ", self.salary


emp1 = Employee("Zara", 2000)
emp2.displayEmployee()
print ("Total Employee %d" % Employee.empCount)
```

### Modify Attribute
```
emp1.age = 7000
emp1.name = 'xyz' 
del emp1.salary

hasattr(emp1, 'salary')    
getattr(emp1, 'salary')    
setattr(emp1, 'salary', 7000)
delattr(emp1, 'salary')    
```

### Built-in Attributes

- `__dict__`: Dictionary containing the class's namespace.
- `__doc__`: Class documentation string or none, if undefined.
- `__name__`: Class name.
- `__module__`: Module name in which the class is defined. This attribute is "`__main__`" in interactive mode.
- `__bases__`: A possibly empty tuple containing the base classes, in the order of their occurrence in the base class list.

### Garbage Collection

Python deletes unneeded objects (built-in types or class instances) automatically to free the memory space. The process by which Python periodically reclaims blocks of memory that no longer are in use is termed Garbage Collection.

Triggered when an object's reference count reaches zero. An object's reference count changes as the number of aliases that point to it changes: deleted, reassigned, or reference goes out of scope.

### Class Inheritance
```py
#!/usr/bin/python3
class Parent:        # define parent class
   parentAttr = 100
   def __init__(self):
      print ("Calling parent constructor")

   def parentMethod(self):
      print ('Calling parent method')

   def setAttr(self, attr):
      Parent.parentAttr = attr

   def getAttr(self):
      print ("Parent attribute :", Parent.parentAttr)

class Child(Parent): # define child class
   def __init__(self):
      print ("Calling child constructor")

   def childMethod(self):
      print ('Calling child method')

c = Child()          # instance of child
c.childMethod()      # child calls its method
c.parentMethod()     # calls parent's method
c.setAttr(200)       # again call parent's method
c.getAttr()          # again call parent's method

```

### Overload
```
#!/usr/bin/python3

class Vector:
   def __init__(self, a, b):
      self.a = a
      self.b = b

   def __str__(self):
      return 'Vector (%d, %d)' % (self.a, self.b)
   
   def __add__(self,other):
      return Vector(self.a + other.a, self.b + other.b)

v1 = Vector(2,10)
v2 = Vector(5,-2)
print (v1 + v2)
```


### Data Hiding

variable start with underscore are not directly visible to outside
```
#!/usr/bin/python3

class JustCounter:
   __secretCount = 0
  
   def count(self):
      self.__secretCount += 1
      print (self.__secretCount)

counter = JustCounter()
counter.count()
counter.count()
print (counter.__secretCount)  # AttributeError
```

