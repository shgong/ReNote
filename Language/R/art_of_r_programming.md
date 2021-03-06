# 1. Start

```
data() ## get R internal demo data
Nile
sd(Nile)
hist(Nile)

38 %% 7  ## mod
f <- function(x) return(x+y)  ## in-line function
```

Regression Analysis
```

examsquiz <- data.frame(
V1 = c(2.0,3.3,4.0, 2.3,0.0,3.3),
V2 = c(3.3,2.0,3.7, 2.3,1.0,3.3),
V3 = c(4.0,4.3,4.0, 3.3,3.7,4.0))

## linear model, predict 2 using 1
lma <- lm(examsquiz[,2] ~ examsquiz[,1])
lma <- lm(examsquiz$V2 ~ examsquiz$V1)


lma

## Call:
## lm(formula = examsquiz[, 2] ~ examsquiz[, 1])
##
## Coefficients:
##    (Intercept)  examsquiz[, 1]
##         1.2582          0.5403


summary(lma)

## Call:
## lm(formula = examsquiz[, 2] ~ examsquiz[, 1])
##
## Residuals:
##       1       2       3       4       5       6
##  0.9611 -1.0413  0.2805 -0.2009 -0.2582  0.2587
##
## Coefficients:
##                Estimate Std. Error t value Pr(>|t|)
## (Intercept)      1.2582     0.6636   1.896   0.1308
## examsquiz[, 1]   0.5403     0.2369   2.280   0.0847 .
## ---
## Signif. codes:
## 0 ‘***’ 0.001 ‘**’ 0.01 ‘*’ 0.05 ‘.’ 0.1 ‘ ’ 1
##
## Residual standard error: 0.7518 on 4 degrees of freedom
## Multiple R-squared:  0.5652,	Adjusted R-squared:  0.4566
## F-statistic: 5.201 on 1 and 4 DF,  p-value: 0.08474
```


Getting helps
```
help(seq)
?seq
example(seq)
help.search("multivariate normal")
```


# 2. Vectors

- Vector stored like arrays in C
- array, matrices and lists are actually vectors
  - with a different class attribute
- R auto recycle vector to match longer one

```
x <- c(88,5,12,13)
x <- c(x[1:3],168,x[4]) # insert 168 before the 13

length(x)

i<-2
1:i-1   ## 0 1
1:(i-1) ## 1    : has higher precendence over -


seq(from=1,to=10,by=3)
seq(from=1,to=10,length.out=5)

rep(8,4)     ## 8 8 8 8

x<-1:10
all(x>8)  ## FALSE
any(x>8)  ## TRUE
```

## Create Matrix

```
x <- 1:8
z12 <- function(z) return(c(z,z^2))
matrix(z12(x), ncol=2)

[,1] [,2]
[1,] 1 1
[2,] 2 4
[3,] 3 9
[4,] 4 16
[5,] 5 25
[6,] 6 36
[7,] 7 49
[8,] 8 64

## or use sapply(), simplify apply to get 2-by-8 matrix
z12 <- function(z) return(c(z,z^2))
sapply(1:8,z12)

[,1] [,2] [,3] [,4] [,5] [,6] [,7] [,8]
[1,] 1 2 3 4 5 6 7 8
[2,] 1 4 9 16 25 36 49 64
```

## NA vs NULL

```
x <- c(88,NA,12,168,13)
mean(x)         ## NA
mean(x,na.rm=T) ## 70.25

x <- c(88,NULL,12,168,13)
mean(x)         ## 70.25

## build vector with NULL in loops
z <- NULL
for (i in 1:10) if (i %%2 == 0) z <- c(z,i)
```


## Vector filter
```
k=seq(1,10,3)
k[k*k<18]     ## filter sqrt < 18 elements
k[k>3]<-0     ## replace >3 elements with a 0

## get the location
which(k*k>18)  ##  1 2

## subset
x <- c(6,1:3,NA,12)
x[x > 5]       ## 6 NA 12
subset(x, x>5) ## 6 12

## condition
x <- 1:10
y <- ifelse(x %% 2 == 0,5,12) # %% is the mod operator
[1] 12 5 12 5 12 5 12 5 12 5

```

## Equal
```
x <- 1:2
y <- c(1,2)

x==y
[1] TRUE TRUE

identical(x,y)
[1] FALSE

typeof(x)
[1] "integer"

typeof(y)
[1] "double"
```

# 3. Matrices and Arrays

Generate covariance matrix
```
makecov <- function(rho,n) {
  m <- matrix(nrow=n,ncol=n)
  m <- ifelse(row(m) == col(m),1,rho)
  return(m)
}
```

## apply family

- `apply(m, dimcode, f, fargs)`
  - m: matrix
  - dimcode: 1 if applies to rows, 2 for columns

```
> z
[,1] [,2]
[1,] 1 4
[2,] 2 5
[3,] 3 6

> apply(z,2,mean)
2 5
```

## change size

- rbind (row)
- cbind (column)

```
> one
[1] 1 1 1 1

> z
[,1] [,2] [,3]
[1,] 1 1 1
[2,] 2 1 0
[3,] 3 0 1
[4,] 4 0 0

> cbind(one,z)
[1,]1 1 1 1
[2,]1 2 1 0
[3,]1 3 0 1
[4,]1 4 0 0
```



# 4. Lists

- list combines objects from different types.
- list is a vector
  - normal vector are atomic vectors
- list is like a struct

```
j <- list(name="Joe", salary=55000, union=T)
j$name
j[[1]]
j[["name"]]

class(j[1])   # list
class(j[[1]]) # character
```


## Unlist
```
> z <- list(a=5,b=12,c=13)
> y <- unlist(z)

> class(y)
[1] "numeric"

> y
a b c
5 12 13
```


## lapply and sapply
```
> lapply(list(1:3,25:29),median)

[[1]]
[1] 2
[[2]]
[1] 27

## simplify to a vector
> sapply(list(1:3,25:29),median)
[1] 2 27
```


# 5. Dataframes
- dataframe is like matrix
  - but each column may have a different mode
  - list to vectors what dataframes to matrix
- technically, df is a list

```
> kids <- c("Jack","Jill")
> ages <- c(12,10)
> d <- data.frame(kids,ages,stringsAsFactors=FALSE)

## list style
> d[[1]]
> d$kids
[1] "Jack" "Jill"

## matrix style
> d[,1]
[1] "Jack" "Jill"

## apply: find max grade for each student
> apply(examsquiz,1,max)
[1] 4.0 3.7 4.0 3.3 3.3 4.0 3.7 3.3 4.0 4.0 4.0 3.3 4.0 4.0 3.7 4.0 3.3 3.7 4.0
```


## merge
can merge if share one or more columns with names in common
```
> d1
kids states
1 Jack CA
2 Jill MA
3 Jillian MA
4 John HI

> d2
ages kids
1 10 Jill
2 7 Lillian
3 12 Jack

> d <- merge(d1,d2)
> d
kids states ages
1 Jack CA 12
2 Jill MA 10

## if not the same key
merge(d1,d3,by.x="kids",by.y="pals")
```


# 6. Factors and Tables
## Factors and Levels

- string, but stored as number in categories instead
- can not insert illegal level

```
x<-c(5,12,13,12)
xf<-factor(x)

>xf
[1] 5 12 13 12
Levels: 5 12 13

> str(xf)
Factor w/ 3 levels "5","12","13": 1 2 3 2

> unclass(xf)
[1] 1 2 3 2

attr(,"levels")
[1] "5" "12" "13"

> xf[2] <- 28
Warning message:
In `[<-.factor`(`*tmp*`, 2, value = 28) :
invalid factor level, NAs generated
```

## Common Functions

### tapply: group by, and apply function

```
> ages <- c(25,26,55,37,21,42)
> affils <- c("R","D","D","R","U","D")

> tapply(ages,affils,mean)
D R U
41 31 21
```

Used in dataframe
```
> d <- data.frame(list(gender=c("M","M","F","M","F","F"), age=c(47,59,21,32,33,24),income=c(55000,88000,32450,76500,123000,45650)))

gender age income
1 M 47 55000
2 M 59 88000
3 F 21 32450
4 M 32 76500
5 F 33 123000
6 F 24 45650

> d$over25 <- ifelse(d$age > 25,1,0)

gender age income over25
1 M 47 55000 1
2 M 59 88000 1
3 F 21 32450 0
4 M 32 76500 1
5 F 33 123000 1
6 F 24 45650 0

> tapply(d$income,list(d$gender,d$over25),mean)
0 1
F 39050 123000.00
M NA 73166.67
```

### split

- just forming groups, don't apply function
```
> split(d$income,list(d$gender,d$over25))

$F.0
[1] 32450 45650
$M.0
numeric(0)
$F.1
[1] 123000
$M.1
[1] 55000 88000 76500
```


### by

- tapply forms groups according to levels of a factor
  - first input must be a vector
- what if function is multivariate
  - need pass a dataframe

```
> aba <- read.csv("abalone.data",header=TRUE)
> by(aba,aba$Gender,function(m) lm(m[,2]~m[,3]))

Call:
lm(formula = m[, 2] ~ m[, 3])
Coefficients:
(Intercept) m[, 3]
0.04288 1.17918
------------------------------------------------------------
aba$Gender: I
Call:
lm(formula = m[, 2] ~ m[, 3])
Coefficients:
(Intercept) m[, 3]
0.02997 1.21833
------------------------------------------------------------
aba$Gender: M
Call:
lm(formula = m[, 2] ~ m[, 3])
Coefficients:
(Intercept) m[, 3]
0.03653 1.19480
```

## Working with Tables
read ct.dat as table
```
"Vote for X" "Voted For X Last Time"
"Yes" "Yes"
"Yes" "No"
"No" "No"
"Not Sure" "Yes"
"No" "No"
```

read in
```
> ct <- read.table("ct.dat",header=T)
Vote.for.X Voted.for.X.Last.Time
1 Yes Yes
2 Yes No
3 No No
4 Not Sure Yes
5 No No

# compute the contingency table

> cttab <- table(ct)
> cttab
Voted.for.X.Last.Time
Vote.for.X No Yes
No 2 0
Not Sure 0 1
Yes 1 1
```

## Aggregate
```
aggregate(aba[,-1],list(aba$Gender),median)
Group.1 Length Diameter Height WholeWt ShuckedWt ViscWt ShellWt Rings
1 F 0.590 0.465 0.160 1.03850 0.44050 0.2240 0.295 10
2 I 0.435 0.335 0.110 0.38400 0.16975 0.0805 0.113 8
3 M 0.580 0.455 0.155 0.97575 0.42175 0.2100 0.276 10
```

# 7. Programming Structure

- get: takes an string argument, return object of that name
- return: call, or last expression
- function
  - r function are first-class object
  - "{" is a function
    - `()` is function(x) x, also set to visible
    - `{}` is function(x) result of last expression
- some R built-in functions are written in C
  - thus not viewable
  - `sum`

- no pointers, immutable, almost no side effect
  - you can not write function that change arguments
  - python `[13,5,12].sort()`
  - r `sort([13,5,12])`, the argument do not change


- gather into a list to change
```
oddsevens<-function(v){
  odds <- which(v %% 2 == 1)
  evens <- which(v %% 2 == 1)
  list(o=odds,e=evens)
}

f <- function(lxxyy) {
  ...
  lxxyy$x <- ...
  lxxyy$y <- ...
  return(lxxyy)
}

# set x and y
lxy$x <- ...
lxy$y <- ...
lxy <- f(lxy)

# use new x and y
... <- lxy$x
... <- lxy$y

```

- superassign
  - `<<-` or `assign()` function
  - `<<-` used to write to top-level variable
  - or `assign("u",2*u,pos=.GlobalEnv)`
- provide a easier way to set

```
f <- function() {
  ...
  x <<- ...
  y <<- ...
}

# set x and y
x <- ...
y <- ...
f() # x and y are changed in here

# use new x and y
... <- x
... <- y
```

## Closure

a counter with internal state

```
function () {
  ctr <- 0
  f <- function() {
    ctr <<- ctr + 1
    cat("this count currently has value",ctr,"\n")
  }
  return(f)
}
```

## Create binary operation
```
"%a2b%" <- function(a,b) return(a+2*b)

3 %a2b% 5
13
```

# 8. Doing Math and Simulations

- review later

# 9. OOP

- S3 class:a list, a class name attribute, add dispatch capability
- s4 class:s3 class add safety

## S3 Classes

R is polymorphic, same function lead to different operation for different classes

A class instance is created by forming a list, with the components of the list being the member variables of the class. (like Perl's OOP system)

```
methods(print)
getAnywhere()

j <- list(name="Joe", salary=55000, union=T)
class(j) <- "employee"
attributes(j) # let's check

## $names
## [1] "name" "salary" "union"
## $class
## [1] "employee"

print.employee <- function(wrkr) {
  cat(wrkr$name,"\n")
  cat("salary",wrkr$salary,"\n")
  cat("union member",wrkr$union,"\n")
}
```

Inheritance
```
k <- list(name="Kate", salary= 68000, union=F, hrsthismonth= 2)
class(k) <- c("hrlyemployee","employee")

## print will still work on k
```


## S4 Classes

S3 willl not complain if union missing, misspelled, assigned to irrelevant object by mistake.

```
## define a class
setClass("employee",
  representation(name="character",salary="numeric",union="logical")
)

joe <- new("employee",name="Joe",salary=55000,union=T)

joe@salary
slot(joe, "salary")

setMethod("show", "employee",
  function(object) {
    inorout <- ifelse(object@union,"is","is not")
    cat(object@name,"has a salary of",object@salary,
    "and",inorout, "in the union", "\n")
  }
)

show(joe)
joe
```


## S3 vs S4
- John Chambers: S4 is needed to write clear and reliable software
- Google R Style: Avoid S4 objects and methods when possible


Helper functions
```
class(), mode()
names(), attributes()
unclass(), str()
edit(), exists()
```


# 10. I/O

## Keyboard and Monitor
```
scan()  ## read in a vector
scan("integer.txt")
scan("char.txt", what="", sep="\n")

scan()  ## read block from keyboard
readline() ## read line from keyboard

print("abc")  ## only print one expression and output is numbered
cat("abc\n")    ## custoimzed output
```

## Files
```
name age
John 25
Mary 28
Jim 19

z <- read.table("z.tab",header=TRUE)

1 0 1
1 1 1
1 1 0
1 1 0
0 0 1

x <- matrix(scan("x"),nrow=5,byrow=TRUE)

John 25
Mary 28
Jim 19

> z1 <- readLines("z1")
> z1
[1] "John 25" "Mary 28" "Jim 19"

write.table(xc,"xcnew",row.names=FALSE,col.names=FALSE)
cat("de\n",file="u",append=TRUE)
```

# 11. String Manipulation

- functions
  - grep(pattern, vector): return element id that contains pattern
  - nchar(x): get length (not work with factor mode)
  - paste(...): concat several strings
  - sprintf(...): assemble string with format
    - sprintf("the square of %d is %d", i, i^2)
  - substr(x, start, stop): substring
  - strsplit(x, split="-"): split x by split
  - regexpr(pattern, text): find first pattern in text


# 12. Graphics

```
# point dot
plot(c(1,2,3), c(1,2,4))

# draw lm
x <- c(1,2,3)
y <- c(1,3,8)
plot(x,y)
lmout <- lm(y ~ x)
abline(lmout)

# add line to existing graph
lines(c(1.5,2.5),c(3,3))

# dot connect graph
plot(x,y,type="l")

# don't erase graph before another
hist(x)

X11()        # linux
macintosh()  # mac
windows      # win

hist(y)


# more functions
points(testscores$Exam1,testscores$Exam3,pch="+")
example(legend)
text(2.5,4,"abc")
```

3D plot

```
> library(lattice)
> a <- 1:10
> b <- 1:15
> eg <- expand.grid(x=a,y=b)
> eg$z <- eg$x^2 + eg$x * eg$y
> wireframe(z ~ x+y, eg)
```

# 13. Debugging

- R debugger
- breakpoint

# 14. Speed & memory

- optimize R code through vectorization, use byte-code comilation
- write key, CPU-intensive part in compiled language like C/C++
- write code in form of parallel R

## The Dreaded For loop

```
x <- runif(1000000)
y <- runif(1000000)

> system.time(z <- x + y)
user system elapsed
0.052 0.016 0.068

> system.time(for (i in 1:length(x)) z[i] <- x[i] + y[i])
user system elapsed
8.088 0.044 8.175
```

- why loop version is slow
  - for() is a function
  - colon ":" is a function
  - vector subscription represent a function call
    - call `[` for two reads
    - call `[<-` for write
  - need setting up stack frame to do function calls
  - not exist in C

- another: allocation cost
  - array is dynamic data structure
  - when full, will find a larger place in mermory, and copy paste, which is slow

```
# forms matrix of powers of vector x throguh degree dg
powers1 <- function(x,dg) {
  pw <- matrix(x,nrow=length(x))
  prod <- x # current product
  for (i in 2:dg) {
    prod <- prod * x
    pw <- cbind(pw,prod)
  }
  return(pw)
}

# cbind() is used to build up the output matrix, column by column
# allocate the full matrix at the beginning, even though it will be empty

# forms matrix of powers of the vector x, through degree dg
powers2 <- function(x,dg) {
  pw <- matrix(nrow=length(x),ncol=dg)
  prod <- x # current product
  pw[,1] <- prod
  for (i in 2:dg) {
    prod <- prod * x
    pw[,i] <- prod
  }
  return(pw)
}
```

## Functional Programming and Memory Issues

Vector Assignment
```
z[3]<-8

# is actually a call of replacement function

z<- "[<-"(z,3,value=8)

# an internal copy of z is made, and reassigned
# entire vector is recomputed
```


Copy-on-Change Issue
- `y<-z`
- initially y share same memory with z
- if either change, a copy is made in different area

```
> z <- runif(10)
> tracemem(z)
[1] "<0x88c3258>"
> y <- z
> z[3] <- 8
> z[3]<-8
tracemem[0x163f28b0 -> 0x163e8370]
```

Use Rprof() to find slow spots in your code


## Byte Code Compilation
```
> library(compiler)
> f <- function() for (i in 1:length(x)) z[i] <<- x[i] + y[i]
> cf <- cmpfun(f)

> system.time(for (i in 1:length(x)) z[i] <- x[i] + y[i])
user system elapsed
8.088 0.044 8.175

> system.time(cf())
user system elapsed
0.845 0.003 0.848
```

# 15. Interfacing R to Other Languages
## call C/C++ extension

```c
#include <R.h> // required
// arguments:
// m: a square matrix
// n: number of rows/columns of m
// k: the subdiagonal index--0 for main diagonal, 1 for first
// subdiagonal, 2 for the second, etc.
// result: space for the requested subdiagonal, returned here

void subdiag(double *m, int *n, int *k, double *result)
{
  int nval = *n, kval = *k;
  int stride = nval + 1;
  for (int i = 0, j = kval; i < nval-kval; ++i, j+= stride)
    result[i] = m[j];
}
```

compile file
```
% R CMD SHLIB sd.c
gcc -std=gnu99 -I/usr/share/R/include -fpic -g -O2 -c sd.c -o sd.o
gcc -std=gnu99 -shared -o sd.so sd.o -L/usr/lib/R/lib -lR
```

use in R
```
> dyn.load("sd.so")
> m <- rbind(1:5, 6:10, 11:15, 16:20, 21:25)
> k <- 2
> .C("subdiag", as.double(m), as.integer(dim(m)[1]), as.integer(k),
result=double(dim(m)[1]-k))

[[1]]
[1] 1 6 11 16 21 2 7 12 17 22 3 8 13 18 23 4 9 14 19 24 5 10 15 20 25

[[2]]
[1] 5

[[3]]
[1] 2

$result
[1] 11 17 23
```


## use R from Python

```
sudo apt-get install python-rpy
from rpy import *
```

refer to http://rpy.sourceforge.net/rpy/doc/rpy.pdf
