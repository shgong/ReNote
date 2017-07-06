# PythonTrivial

Container’s `__str__` uses contained objects `__repr__`



Customize Iterable

```py
class FibIterable:
    """this class is a generates a well known sequence of numbers """
    def __init__(self,iLast=1,iSecondLast=0,iMax=50):
        self.iLast = iLast 
        self.iSecondLast = iSecondLast
        self.iMax = iMax  #cutoff
    def __iter__(self):
        return self    # because the object is both the iterable and the itorator
    def next(self):
        iNext = self.iLast + self.iSecondLast
        if iNext > self.iMax:
            raise StopIteration()
        self.iSecondLast = self.iLast
        self.iLast = iNext
        return iNext

o = FibIterable()
for i in o:
    print(i)

print FibIterable.__doc__
```
