
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

```
## explore env tree
as.environment("package:stats")

## access from certain env
head(globalenv()$cards, 3)
assign("new", "Hello Global", envir = globalenv())
```


---------------------------------------------------------------

# III. Slot Machine

SLOT Machine in Casinos
```
play()
## 0 0 DD
## $0

play()
## 7 7 7
## $80
```


## 7. Programs

```r
get_symbols <- function() {
  wheel <- c("DD", "7", "BBB", "BB", "B", "C", "0")
  sample(wheel, size = 3, replace = TRUE,
  prob = c(0.03, 0.03, 0.06, 0.1, 0.25, 0.01, 0.52))
}

score <- function (symbols) {

  # identify case
  same <- symbols[1] == symbols[2] && symbols[2] == symbols[3]
  bars <- symbols %in% c("B", "BB", "BBB")

  # get prize
  if (same) {
    payouts <- c("DD" = 100, "7" = 80, "BBB" = 40, "BB" = 25, "B" = 10, "C" = 10, "0" = 0)
    prize <- unname(payouts[symbols[1]])
  } else if (all(bars)) {
    prize <- 5
  } else {
    cherries <- sum(symbols == "C")
    prize <- c(0, 2, 5)[cherries + 1]
  }

  # adjust for diamonds
  diamonds <- sum(symbols == "DD")
  prize * 2 ^ diamonds
}

play <- function() {
  symbols <- get_symbols()
  print(symbols)
  score(symbols)
}
```

## 8. S3

- S3 system is a class system built into R
- governs how R handles objects of different classes
- R function will look up object's S3 class and behave differently


```
num<-1000000000
print(num)
## 1000000000

class(num)<-c("POSIXct", "POSIXt")
print(num)
## 2001--0-08 19:46:40 CST
```

### Attributes

- r helper function to set attributes
  - names
  - dim
  - class
- Example: attributes of a deck
- assign new attribute
  - `levels(deck) <- c("level 1", "level 2", "level 3")`
  - or customized attribute
  - `attr(deck, "symbols") <- c("B", "0", "B")`


  ```
  $names
  [1] "face"  "suit"  "value"

  $row.names
   [1]  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17
  [18] 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34
  [35] 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51
  [52] 52

  $class
  [1] "data.frame"
  ```


### Methods

print actually calls a UseMethod("print") function
```
> print

function (x, ...)
UseMethod("print")
<bytecode: 0x087611dc>
<environment: namespace:base>

> print.POSIXct

function (x, tz = "", usetz = TRUE, ...)
{
    max.print <- getOption("max.print", 9999L)
    FORM <- if (missing(tz))
        function(z) format(x, usetz = usetz)
    else function(z) format(x, tz = tz, usetz = usetz)
    if (max.print < length(x)) {
        print(FORM(x[seq_len(max.print)]), ...)
        cat(" [ reached getOption(\"max.print\") -- omitted",
            length(x) - max.print, "entries ]\n")
    }
    else print(if (length(x))
        FORM(x)
    else paste(class(x)[1L], "of length 0"), ...)
    invisible(x)
}
<bytecode: 0x0882c9fc>
<environment: namespace:base>

> print.table
function (x, digits = getOption("digits"), quote = FALSE, na.print = "",
    zero.print = "0", justify = "none", ...)
{
    d <- dim(x)
    if (any(d == 0)) {
        cat("< table of extent", paste(d, collapse = " x "),
            ">\n")
        return(invisible(x))
    }
    xx <- format(unclass(x), digits = digits, justify = justify)
    if (any(ina <- is.na(x)))
        xx[ina] <- na.print
    if (zero.print != "0" && any(i0 <- !ina & x == 0))
        xx[i0] <- zero.print
    if (is.numeric(x) || is.complex(x))
        print(xx, quote = quote, right = TRUE, ...)
    else print(xx, quote = quote, ...)
    invisible(x)
}
<bytecode: 0x08816034>
<environment: namespace:base>
```

- Method Dispatch
  - Every S3 method has two-part name
    - first: the function that method work with
    - second: class
  - `summary.matrix`
- we can assign "slot" class
  - and write function for "slot"

```r
print.slots <- function(x, ...) {
  cat("I'm using the print.slots method")
}
```

### Classes

show method available in a R package
```
methods(class = "factor")

## [1] [.factor [[.factor
## [3] [[<-.factor [<-.factor
## [5] all.equal.factor as.character.factor
## [7] as.data.frame.factor as.Date.factor
## [9] as.list.factor as.logical.factor
## [11] as.POSIXlt.factor as.vector.factor
## [13] droplevels.factor format.factor
## [15] is.na<-.factor length<-.factor
## [17] levels<-.factor Math.factor
## [19] Ops.factor plot.factor*
## [21] print.factor relevel.factor*
## [23] relist.factor* rep.factor
## [25] summary.factor Summary.factor
## [27] xtfrm.factor
```

- Challenges to create class
  - R drops attributes when assign, combine into vector
  - R drops attributes when subset the object
- what to do
  - write c`.slots` method
  - write `[.slots` method
- more problems
  - how would you combine the symbols attributes into vector of attributes
  - how would you change print to handle multiple outputs

> However, you will usually not have to attempt this type of large-scale programming as a data
scientist.


- There are S4 and S5
  - reference classes
  - harder to use, however offer safeguards that S3 does not

## 9. Loops

- Calculate payout rate of your machine
- Calculate expected values

### expand.grid

- write combination in n vectors
```
rolls <- expand.grid(dice, dice)

## Var1 Var2
## 1 1 1
## 2 2 1
## 3 3 1
## ...
## 34 4 6
## 35 5 6
## 36 6 6

rolls$sum = rolls$Var1 + rolls$Var2

##       Var1 Var2 sum
## 1     1    1   2
## 2     2    1   3
## 3     3    1   4
## 4     4    1   5
## 5     5    1   6
```


### for loop
```r
for (value in that) {
  this
}

for (value in c("My", "second", "for", "loop")) {
  print(value)
}

for (i in 1:4) {
  chars[i] <- words[i]
}
```


### while loops

```
plays_till_broke <- function(start_with) {
  cash <- start_with
  n <- 0
  while (cash > 0) {
    cash <- cash - 1 + play()
    n <- n + 1
  }
  n
}
plays_till_broke(100)
## 260
```


## 10. Speed

- fastest r code will use three things
  - logical test
  - subsetting
  - element-wise execution

```
## slow
abs_loop <- function(vec){
  for (i in 1:length(vec)) {
    if (vec[i] < 0) {
      vec[i] <- -vec[i]
    }
  }
vec
}

## fast
abs_sets <- function(vec){
  negs <- vec < 0
  vec[negs] <- vec[negs] * -1
  vec
}

long <- rep(c(-1, 1), 5000000)

## evaluate performance
## Sys.time return current time
## system.time return performance

system.time(abs_loop(long))
## user system elapsed
## 15.982 0.032 16.018

system.time(abs_sets(long))
## user system elapsed
## 0.529 0.063 0.592
```


### Vectorize change_symbol function
```
change_symbols <- function(vec){
  for (i in 1:length(vec)){
    if (vec[i] == "DD") {
      vec[i] <- "joker"
    } else if (vec[i] == "C") {
      vec[i] <- "ace"
    } else if (vec[i] == "7") {
      vec[i] <- "king"
    }else if (vec[i] == "B") {
      vec[i] <- "queen"
    } else if (vec[i] == "BB") {
      vec[i] <- "jack"
    } else if (vec[i] == "BBB") {
      vec[i] <- "ten"
    } else {
      vec[i] <- "nine"
    }
  }
  vec
}

change_vec <- function(vec){
  tb <- c("DD" = "joker", "C" = "ace", "7" = "king", "B" = "queen",
  "BB" = "jack", "BBB" = "ten", "0" = "nine")
  unname(tb[vec])
}
```

### Faster loops in R

- optimize for loop
  - do as much as you can outside for loop
    - code inside for loop will run many times
  - any storage objects in the loop are large enough to contain all the results
    - it is slow when R expand length of object one by one'
    - R will try to find new place in memory, copy vector over

```
system.time(
  output <- rep(NA, 1000000)
  for (i in 1:1000000) {
    output[i] <- i + 1
  }
)
## user system elapsed
## 1.709 0.015 1.724

system.time(
  output <- NA
  for (i in 1:1000000) {
    output[i] <- i + 1
  }
)
## user system elapsed
## 1689.537 560.951 2249.92
```


### Vectorize Slot Machine

```r
# symbols should be a matrix with a column for each slot machine window
score_many <- function(symbols) {

  # Step 1: Assign base prize based on cherries and diamonds ---------

  ## Count the number of cherries and diamonds in each combination
  cherries <- rowSums(symbols == "C")
  diamonds <- rowSums(symbols == "DD")

  ## Wild diamonds count as cherries
  prize <- c(0, 2, 5)[cherries + diamonds + 1]

  ## ...but not if there are zero real cherries
  ### (cherries is coerced to FALSE where cherries == 0)
  prize[!cherries] <- 0

  # Step 2: Change prize for combinations that contain three of a kind
  same <- symbols[, 1] == symbols[, 2] &
  symbols[, 2] == symbols[, 3]
  payoffs <- c("DD" = 100, "7" = 80, "BBB" = 40, "BB" = 25, "B" = 10, "C" = 10, "0" = 0)
  prize[same] <- payoffs[symbols[same, 1]]

  # Step 3: Change prize for combinations that contain all bars ------
  bars <- symbols == "B" | symbols == "BB" | symbols == "BBB"
  all_bars <- bars[, 1] & bars[, 2] & bars[, 3] & !same
  prize[all_bars] <- 5

  # Step 4: Handle wilds ---------------------------------------------
  ## combos with two diamonds
  two_wilds <- diamonds == 2

  ### Identify the nonwild symbol
  one <- two_wilds & symbols[, 1] != symbols[, 2] &
  symbols[, 2] == symbols[, 3]
  two <- two_wilds & symbols[, 1] != symbols[, 2] &
  symbols[, 1] == symbols[, 3]
  three <- two_wilds & symbols[, 1] == symbols[, 2] &
  symbols[, 2] != symbols[, 3]

  ### Treat as three of a kind
  prize[one] <- payoffs[symbols[one, 1]]
  prize[two] <- payoffs[symbols[two, 2]]
  prize[three] <- payoffs[symbols[three, 3]]

  ## combos with one wild
  one_wild <- diamonds == 1

  ### Treat as all bars (if appropriate)
  wild_bars <- one_wild & (rowSums(bars) == 2)
  prize[wild_bars] <- 5

  ### Treat as three of a kind (if appropriate)
  one <- one_wild & symbols[, 1] == symbols[, 2]
  two <- one_wild & symbols[, 2] == symbols[, 3]
  three <- one_wild & symbols[, 3] == symbols[, 1]

  prize[one] <- payoffs[symbols[one, 1]]
  prize[two] <- payoffs[symbols[two, 2]]
  prize[three] <- payoffs[symbols[three, 3]]

  # Step 5: Double prize for every diamond in combo ------------------
  unname(prize * 2^diamonds)
}

system.time(play_many(10000000))
## user system elapsed
## 20.942 1.433 22.367

```

- it is 17 times faster
