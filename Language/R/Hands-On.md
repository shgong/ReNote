
# I. Weighted Dice

## 1. Basics

```r
a <- 1      ## assign value to object
A <- 2      ## case sensitive
dice <- 1:6
rm(a)       ## clear memory

dice * dice ## use element-wise computation
            ## 1 4 9 16 25 36

dice + 1:2  ## vector recycling
            ## 2 4 4 6 6 8

dice %*% dice   ## inner product
dice %o% dice   ## matrix multiply
```

Functions
```
round(3.14159)
factorial(3)
mean(1:6)
args(round)          ## see what arguments to pass

sample(1:6, size=2)  ## roll dice
                     ## second roll will never be same as first roll
sample(1:6, size=2, replace=TRUE)
                     ## sample with replacement, can be same value
```

Function Constructor
```
roll <- function(dice) {
  dice <- sample(dice, 2, TRUE)
  sum(dice)
}

roll(1:6)
```
--

## 2. Packages

```r
install.packages("ggplot2")
library("ggplot2")

## create vector
x <- c(-1, -0.8, -0.6, -0.4, -0.2, 0, 0.2, 0.4, 0.6, 0.8, 1)
y <- x^3
y
qplot(x,y)

x <- c(1,2,2,2,3)
qplot(x, binwidth=1)


## replicate a command many times

replicate(3, 1 + 1)
## 2 2 2

replicate(10, roll())
## 3 7 5 3 6 2 3 8 11 7

rolls <- replicate(10000, roll(1:6))
qplot(rolls, binwidth = 1)
```

---------------------------------------------------------------
# II. Playing Cards
## 3. R Object

 Atomic Vectors

```r
dice <- c(1,2,3,4,5,6)
is.vector(dice)          ## TRUE
is.vector(1)             ## single value is length 1 vector

typeof(c(1,2,3,4,5,6))   ## double  default
typeof(c(1L,-1L,2L))     ## integer
typeof(c("Hello","W"))   ## character
typeof("Hello")          ## character

sqrt(2)^2-2              ##  4.440892e-16
```

Complex and Raw
```
comp <- c(1+1i, 1+2i)
typeof(comp)            ## complex

raw(3)              ## 00 00 00
typeof(raw(3))      ## raw
```


Attributes
- you can attach information to atomic vector
- or any R objects

```
dice <- c(1,2,3,4,5,6)
names(dice) <- c("one", "two", "three", "four", "five", "six")
dice + 1
##   one   two three  four  five   six
##    2     3     4     5     6     7
```



Dim
- transform atomic vector into n-d array
- set dim to numeric vector of length n
- r will reorganize elements into n dimensions

```
dim(dice) <- c(2, 3)
dice
## [,1] [,2] [,3]
## [1,] 1 3 5
## [2,] 2 4 6


dim(die) <- c(1, 2, 3)
die
## , , 1
##
## [,1] [,2]
## [1,] 1 2
##
## , , 2
##
## [,1] [,2]
## [1,] 3 4
##
## , , 3
##
## [,1] [,2]
## [1,] 5 6
```


Matrices


## 4. R Notation
## 5. Modifying Value
## 6. Environments


---------------------------------------------------------------

# III. Slot Machine
## 7. Programs

## 8. S3

## 9. Loops

## 10. Speed
