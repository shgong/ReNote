
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
- store values in 2d array

```
m <- matrix(die, nrow = 2)
m
## [,1] [,2] [,3]
## [1,] 1 3 5
## [2,] 2 4 6

## fill by row
m <- matrix(die, nrow = 2, byrow = TRUE)
m
## [,1] [,2] [,3]
## [1,] 1 2 3
## [2,] 4 5 6
```


- change dimensional value will not change type of object
- but it change class of object
  - `class(dice)` matrix
  - `class(1)` numeric  (return type if don't have class attribute)
  - `class("Hello")` character


Factor
- R way of store categorical information

```
gender <- factor(c("male", "female", "female", "male"))

typeof(gender)
## "integer"

attributes(gender)
## $levels
## [1] "female" "male"
##
## $class
## [1] "factor"
```


List
- group data into one-dimensional set
- unlike atomic vector, group individual values
- list group together R objects, like vectors and other list

```
list1 <- list(100:130, "R", list(TRUE, FALSE))
```


Data Frames
- 2d version of a list
- group vectors into a 2d table
- actually it is a list with class `data.frame`
- default store string as Factors
  - can disable by `stringAsFactors=FALSE`

```
df <- data.frame(face = c("ace", "two", "six"),
                 suit = c("clubs", "clubs", "clubs"),
                 value = c(1, 2, 3))


deck <- data.frame(
face = c("king", "queen", "jack", "ten", "nine", "eight", "seven", "six",
"five", "four", "three", "two", "ace", "king", "queen", "jack", "ten",
"nine", "eight", "seven", "six", "five", "four", "three", "two", "ace",
"king", "queen", "jack", "ten", "nine", "eight", "seven", "six", "five",
"four", "three", "two", "ace", "king", "queen", "jack", "ten", "nine",
"eight", "seven", "six", "five", "four", "three", "two", "ace"),
suit = c("spades", "spades", "spades", "spades", "spades", "spades",
"spades", "spades", "spades", "spades", "spades", "spades", "spades",
"clubs", "clubs", "clubs", "clubs", "clubs", "clubs", "clubs", "clubs",
"clubs", "clubs", "clubs", "clubs", "clubs", "diamonds", "diamonds",
"diamonds", "diamonds", "diamonds", "diamonds", "diamonds", "diamonds",
"diamonds", "diamonds", "diamonds", "diamonds", "diamonds", "hearts",
"hearts", "hearts", "hearts", "hearts", "hearts", "hearts", "hearts",
"hearts", "hearts", "hearts", "hearts", "hearts"),
value = c(13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 13, 12, 11, 10, 9, 8,
7, 6, 5, 4, 3, 2, 1, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 13, 12, 11,
10, 9, 8, 7, 6, 5, 4, 3, 2, 1)
)

write.csv(deck, file = "cards.csv", row.names = FALSE)
```

## 4. R Notation

deck[i,j] will return value of deck on ith row and jth column
deck[1, 1:3] to extract multiple columns
deck[-(2:55), 1:3] negative will remove some rows or columns
deck[1,] extract every column
deck[1, c("face","value")] extract with names

```r
deal <- function(cards) {
  cards[1, ]
}

shuffle <- function(cards) {
  random <- sample(1:52, size = 52)
  cards[random, ]
}

## extract value
mean(deck$value)

## single bracket -> return list
lst["numbers"]
lst[1]

## double bracket -> return elements
lst[["numbers"]]
lst[[1]]
```

## 5. Modifying Value

```r
vec[1]<-1000

## update multiple value
vec[c(1, 3, 5)] <- c(1, 1, 1)

## create value do not yet exist
vec[7] <- 0


cards$value[c(13,26,39,52)]
## 1 1 1 1
## assign higher value for ace
cards$value[c(13,26,39,52)] <- 14
```

Logical filters

```r
1 %in% c(3,4,5)       ## false
c(1,2) %in% c(2,3,4)  ## false true
cards[1:3,]$face == "king"   ## TRUE FALSE FALSE
sum(cards$face == "ace")     ## 4  (interpret as 1 or 0)
cards$value[cards$face == "ace"] <- 14     ## better assign

## boolean operation
queenOfSpades <- cards$face == "queen" & cards$suit == "spades"
## FALSE TRUE FALSE ...
## boolean list as a filter
cards$value[queenOfSpades] <- 100
```

Missing Information: NA
```
## if one value is NA, average will be NA
vec <- c(NA, 1:50)
mean(vec)  ## NA
mean(vec, na.rm=TRUE)  ## 25.5

## compare with NA will be NA
c(1,2,3,NA)==NA     ## NA NA NA NA
is.na(c(1,2,3,NA))  ## FALSE FALSE FALSE TRUE

```






## 6. Environments


---------------------------------------------------------------

# III. Slot Machine
## 7. Programs

## 8. S3

## 9. Loops

## 10. Speed
