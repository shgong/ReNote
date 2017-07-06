# Learn You a Haskell for Great Good!

## 1. Tutorial
setting up ghci terminal interface

## 2. Starting Out

```hs
50 * 100 - 4999    // 1
5 /= 5             // False
succ 8             // 9
max 100 101        // 101
succ 9 * 10        // 100
succ (9*10)        // 91
```

Function application (calling a function by putting a space after it and then typing out the parameters) has the highest precedence of all.

### Functions
baby.hs
```
doubleMe x = x + x
doubleUs x y = doubleMe x + doubleMe y
doubleSmallNumber x = if x > 100
                        then x
                        else x*2
doubleSmallNumber' x = (if x > 100 then x else x*2) + 1
```

- `'` is a valid char in function name
- function name can't begin with uppercase letters 

Compile
```
:l baby
doubleMe 8.3
doubleSmallNumber 121
```


### List
```
let lostNumbers = [4,8,15,16,23,42]  

[1,2,3] ++ [4,5]    -- syntactic sugar for 1:2:3:[]
"hello"             -- same as ['h','e','l','l','o']
5:[1,2,3,4,5]
lostNumbers !! 4    -- 23
head [5,4,3,2,1]    -- 5
tail [5,4,3,2,1]    -- [4,3,2,1]
last [5,4,3,2,1]    -- 1
init [5,4,3,2,1]    -- [5,4,3,2]
length[5,4,3,2,1]   -- 5
null[1,2,3]         -- False
reverse[1,2,3]      -- [3,2,1]
take 3 [5,4,3,2,1]  -- [5,4,3]
drop 3 [5,4,3,2,1]  -- [2,1]
maximum [1,2,3]      -- 3
minimium [1,2,3]     -- 1
sum [1,2,3]          -- 6
product [6,2,1,2]    -- 24
4 `elem` [3,4,5]     -- True       `elem` for infix function
elem 4 [3,4,5]       -- True
```

### Texas Ranges
```
[1..5]              -- [1,2,3,4,5]
['a'..'e']          -- 'edcba'
[5,4..1]            -- [5,4,3,2,1]  when increment is not 1
[3,6..14]           -- [3,6,9,12]
[0.1, 0.3 .. 1]     -- [0.1,0.3,0.5,0.7,0.899999,1.099999] No FLOAT
take 24 [13,26..]   -- [13,26..24*13]  haskell is lazy
take 10 (cycle [1,2,3])   -- [1,2,3,1,2,3,1,2,3,1]
take 10 (repeat 5)        -- [5,5,5,5,5,5,5,5,5,5]  
```

### list comprehension
```
[x*2 | x <- [1..10], x*2>=12]          --[12,14,16,18,20]  
[ x | x <- [50..100], mod x 7 == 3]    --[52,59,66,73,80,87,94]   
[ x*y | x <- [2,5,10], y <- [8,10,11]] --[16,20,22,40,50,55,80,100,110]

let nouns = ["hobo","frog","pope"]  
let adjectives = ["lazy","grouchy","scheming"]  
[adjective ++ " " ++ noun | adjective <- adjectives, noun <- nouns]  
-- ["lazy hobo","lazy frog","lazy pope","grouchy hobo","grouchy frog",  
"grouchy pope","scheming hobo","scheming frog","scheming pope"]  

length' xs = sum [1 | _ <- xs]    -- _ means don't care what we draw
removeNonUppercase st = [ c | c <- st, c `elem` ['A'..'Z']]   

[ [ x | x <- xs, even x ] | xs <- xxs]   -- can nest
```

### Tuples
```
fst (8,11)          -- 8
snd ("Wow", False)  -- False
zip [1,2,3][5,5,5]  -- [(1,5),(2,5),(3,5)]  pairs up

let rightTriangles = [ (a,b,c) | c <- [1..10], b <- [1..c], a <- [1..b], a^2 + b^2 == c^2]

```

## 3. Types and Type Classes

```
:t 'a'              -- 'a' :: Char
:t (True, 4==5)     -- (True, 'a') :: (Bool, Char)  

addThree :: Int -> Int -> Int -> Int  
addThree x y z = x + y + z              --  type declaration
```

### Types

- Int: bounded to 32 bit 
- Integer: not bounded, less efficient
- Float
- Double
- Bool
- Char

```
:t head   -- head :: [a] -> a 
:t fst    -- fst :: (a, b) -> a   
:t (==)  (==) :: (Eq a) => a -> a -> Bool  
```


left of => is class constraints, means a should be member of Eq typeclass

If a function is comprised only of special characters, it's considered an infix function by default. If we want to examine its type, pass it to another function or call it as a prefix function, we have to surround it in parentheses.


### TypeClasses

Eq: implement function  == and /=
Ord:  >, <, >= , <= and compare (GT, LT or EQ)
Show: can be presented as strings
Read: can takes a string and returns the type
Enum: can enumerate, succ, pred
Bounded: have an upper/lower bound
Num: act like numbers, implemen operation like *, Show, Eq
Integral: Int & Integer
Floating: Float & Double

```
"Abrakadabra" `compare` "Zebra"   -- LT  
show 5.334               --"5.334"  
read "[1,2,3,4]" ++ [3]  -- [1,2,3,4,3]  
read "[1,2,3,4]" :: [Int]  -- [1,2,3,4]  
read "5" :: Float          -- 5.0  
[LT .. GT]                 --[LT,EQ,GT]  
maxBound :: Char           -- '\1114111'  
minBound :: Bool           -- False  
```


## 4. Syntax in Functions

### Pattern Matching

define separate function bodies for different patterns. This leads to really neat code that's simple and readable.

```
sayMe :: (Integral a) => a -> String  
sayMe 1 = "One!"  
sayMe 2 = "Two!"  
sayMe 3 = "Three!"  
sayMe 4 = "Four!"  
sayMe 5 = "Five!"  
sayMe x = "Not between 1 and 5"  

-- recursion
factorial :: (Integral a) => a -> a  
factorial 0 = 1  
factorial n = n * factorial (n - 1)

-- pattern match inside tuple
addVectors :: (Num a) => (a, a) -> (a, a) -> (a, a)  
addVectors (x1, y1) (x2, y2) = (x1 + x2, y1 + y2)  

-- replacement
first :: (a, b, c) -> a  
first (x, _, _) = x  

-- implement a head function
head' :: [a] -> a  
head' [] = error "Can't call head on an empty list, dummy!"  
head' (x:_) = x  

-- implement length
length' :: (Num b) => [a] -> b  
length' [] = 0  
length' (_:xs) = 1 + length' xs  

-- implement sum
sum' :: (Num a) => [a] -> a  
sum' [] = 0  
sum' (x:xs) = x + sum' xs  

-- pattern shorthand, easy to get whole list via name before @
capital :: String -> String  
capital "" = "Empty string, whoops!"  
capital all@(x:xs) = "The first letter of " ++ all ++ " is " ++ [x]  

```


### Guards Guards

```
bmiTell :: (RealFloat a) => a -> a -> String  
bmiTell weight height  
    | weight / height ^ 2 <= 18.5 = "You're underweight, you emo, you!"  
    | weight / height ^ 2 <= 25.0 = "You're supposedly normal."  
    | weight / height ^ 2 <= 30.0 = "You're fat! Lose some weight, fatty!"  
    | otherwise                 = "You're a whale, congratulations!"  

max' :: (Ord a) => a -> a -> a  
max' a b | a > b = a | otherwise = b  

```

### Where
Don't repeat yourself
```hs
bmiTell :: (RealFloat a) => a -> a -> String  
bmiTell weight height  
    | bmi <= skinny = "You're underweight, you emo, you!"  
    | bmi <= normal = "You're supposedly normal. Pffft, I bet you're ugly!"  
    | bmi <= fat    = "You're fat! Lose some weight, fatty!"  
    | otherwise     = "You're a whale, congratulations!"  
    where bmi = weight / height ^ 2  
           (skinny, normal, fat) = (18.5, 25.0, 30.0)  
```

can define functions too
```
calcBmis :: (RealFloat a) => [(a, a)] -> [a]  
calcBmis xs = [bmi w h | (w, h) <- xs]  
    where bmi weight height = weight / height ^ 2  
```

### Let it be

```
cylinder :: (RealFloat a) => a -> a -> a  
cylinder r h = 
    let sideArea = 2 * pi * r * h  
        topArea = pi * r ^2  
    in  sideArea + 2 * topArea  

let square x = x * x in (square 5, square 3, square 2)   --(25,9,4)  

calcBmis :: (RealFloat a) => [(a, a)] -> [a]  
calcBmis xs = [bmi | (w, h) <- xs, let bmi = w / h ^ 2, bmi >= 25.0]  
```

let bindings are local in scope, cannot use across guards

### case
```
head' :: [a] -> a  
head' xs = case xs of [] -> error "No head for empty lists!"  
                      (x:_) -> x  

describeList :: [a] -> String  
describeList xs = "The list is " ++ case xs of [] -> "empty."  
                                               [x] -> "a singleton list."   
                                               xs -> "a longer list."  

describeList :: [a] -> String  
describeList xs = "The list is " ++ what xs  
    where what [] = "empty."  
          what [x] = "a singleton list."  
          what xs = "a longer list."  
```

## 5.Recursion

```
maximum' :: (Ord a) => [a] -> a  
maximum' [] = error "maximum of empty list"  
maximum' [x] = x  
maximum' (x:xs) = max x (maximum' xs) 

replicate' :: (Num i, Ord i) => i -> a -> [a]  
replicate' n x  
    | n <= 0    = []  
    | otherwise = x:replicate' (n-1) x  


take' :: (Num i, Ord i) => i -> [a] -> [a]  
take' n _  
    | n <= 0   = []  
take' _ []     = []  
take' n (x:xs) = x : take' (n-1) xs  

reverse' :: [a] -> [a]  
reverse' [] = []  
reverse' (x:xs) = reverse' xs ++ [x]  

repeat' :: a -> [a]  
repeat' x = x:repeat' x  

zip' :: [a] -> [b] -> [(a,b)]  
zip' _ [] = []  
zip' [] _ = []  
zip' (x:xs) (y:ys) = (x,y):zip' xs ys  

elem' :: (Eq a) => a -> [a] -> Bool  
elem' a [] = False  
elem' a (x:xs)  
    | a == x    = True  
    | otherwise = a `elem'` xs   
```


### Quicksort
```
quicksort :: (Ord a) => [a] -> [a]  
quicksort [] = []  
quicksort (x:xs) =   
    let smallerSorted = quicksort [a | a <- xs, a <= x]  
        biggerSorted = quicksort [a | a <- xs, a > x]  
    in  smallerSorted ++ [x] ++ biggerSorted  

```


## 6. High Order Functions

### Curried Function
Every function in Haskell officially only takes one parameter. So how is it possible that we defined and used several functions that take more than one parameter so far? 

max 4 5 first creates a function that takes a parameter and returns either 4 or that parameter, depending on which is bigger. Then, 5 is applied to that function and that function produces our desired result. 

```
compareWithHundred :: (Num a, Ord a) => a -> Ordering  
compareWithHundred x = compare 100 x  
compareWithHundred = compare 100  

divideByTen :: (Floating a) => a -> a  
divideByTen = (/10)  
(/10) 250    -- 25

```

### Some higher-orderism is in order

```
applyTwice :: (a -> a) -> a -> a  
applyTwice f x = f (f x)  
```


```
zipWith' :: (a -> b -> c) -> [a] -> [b] -> [c]  
zipWith' _ [] _ = []  
zipWith' _ _ [] = []  
zipWith' f (x:xs) (y:ys) = f x y : zipWith' f xs ys  
```

zip with a function: The first parameter is a function that takes two things and produces a third thing. They don't have to be of the same type, but they can. The second and third parameter are lists. The result is also a list. 

```
zipWith' (zipWith' (*)) [[1,2,3],[3,5,6],[2,3,4]] [[3,2,2],[3,4,5],[5,4,3]]  
-- [[3,4,6],[9,20,30],[10,12,12]]  
```

flip
```
flip' :: (a -> b -> c) -> (b -> a -> c)  
flip' f = g  
    where g x y = f y x  

flip' :: (a -> b -> c) -> b -> a -> c  
flip' f y x = f x y  
```

### Maps and filters

map takes a function and a list and applies that function to every element in the list, producing a new list. Let's see what its type signature is and how it's defined.

filter is a function that takes a predicate (a predicate is a function that tells whether something is true or not, so in our case, a function that returns a boolean value) and a list and then returns the list of elements that satisfy the predicate. The type signature and implementation go like this:


```
map :: (a -> b) -> [a] -> [b]  
map _ [] = []  
map f (x:xs) = f x : map f xs  


filter :: (a -> Bool) -> [a] -> [a]  
filter _ [] = []  
filter p (x:xs)   
    | p x       = x : filter p xs  
    | otherwise = filter p xs  
```


```
quicksort :: (Ord a) => [a] -> [a]    
quicksort [] = []    
quicksort (x:xs) =     
    let smallerSorted = quicksort (filter (<=x) xs)  
        biggerSorted = quicksort (filter (>x) xs)   
    in  smallerSorted ++ [x] ++ biggerSorted  
```


the sum of all odd squares that are smaller than 10,000
```
sum (takeWhile (<10000) (filter odd (map (^2) [1..])))

sum (takeWhile (<10000) [n^2 | n <- [1..], odd (n^2)])    
```

Haskell's property of laziness is what makes this possible. We can map over and filter an infinite list, because it won't actually map and filter it right away, it'll delay those actions. 

####  Collatz sequences

```
chain :: (Integral a) => a -> [a]  
chain 1 = [1]  
chain n  
    | even n =  n:chain (n `div` 2)  
    | odd n  =  n:chain (n*3 + 1)  

numLongChains :: Int -> Int
numLongChains x = length (filter isLong (map chain [1..x]))
    where isLong xs = length xs > 15
```


#### Lambda
To make a lambda, we write a \ (because it kind of looks like the greek letter lambda if you squint hard enough) 

```
numLongChains :: Int  
numLongChains = length (filter (\xs -> length xs > 15) (map chain [1..100]))  

zipWith (\a b -> (a * 30 + 3) / b) [5,4,3,2,1] [1,2,3,4,5]  
-- [153.0,61.5,31.0,15.75,6.6]  

flip' :: (a -> b -> c) -> b -> a -> c  
flip' f = \x y -> f y x  
```

### folds and horses

A fold takes a binary function, a starting value (I like to call it the accumulator) and a list to fold up. The binary function itself takes two parameters. The binary function is called with the accumulator and the first (or last) element and produces a new accumulator. 
```
sum' :: (Num a) => [a] -> a  
sum' xs = foldl (\acc x -> acc + x) 0 xs  
sum' = foldl (+) 0  

elem' :: (Eq a) => a -> [a] -> Bool  
elem' y ys = foldl (\acc x -> if x == y then True else acc) False ys  

map' :: (a -> b) -> [a] -> [b]  
map' f xs = foldr (\x acc -> f x : acc) [] xs  
map' f xs = foldl (\acc x -> acc ++ [f x]) [] xs
-- ++ is much expensive than :
```


```
maximum' :: (Ord a) => [a] -> a  
maximum' = foldr1 (\x acc -> if x > acc then x else acc)  
  
reverse' :: [a] -> [a]  
reverse' = foldl (\acc x -> x : acc) []  
  
product' :: (Num a) => [a] -> a  
product' = foldr1 (*)  
  
filter' :: (a -> Bool) -> [a] -> [a]  
filter' p = foldr (\x acc -> if p x then x : acc else acc) []  
```


### Function application with $

```hs
($) :: (a -> b) -> a -> b  
f $ x = f x  
```

What is this useless operator?

Whereas normal function application (putting a space between two things) has a really high precedence, the $ function has the lowest precedence.

Function application with a space is left-associative (so f a b c is the same as ((f a) b) c)), function application with $ is right-associative.

```
 sum (map sqrt [1..130])
 sum $ map sqrt [1..130] -saving ourselves precious keystrokes!

 sqrt (3 + 4 + 9) 
 sqrt $ 3 + 4 + 9 

sum (filter (> 10) (map (*2) [2..10]))
 sum $ filter (> 10) $ map (*2) [2..10]
```


### Function Composition with .

```
(.) :: (b -> c) -> (a -> b) -> a -> c  
f . g = \x -> f (g x)  
```


```
map (negate . abs) [5,-3,-6,7,-3,2,-19,24]   -- [-5,-3,-6,-7,-3,-2,-19,-24]  
map (negate . sum . tail) [[1..5],[3..6],[1..7]]  -- [-14,-15,-27]  

fn x = ceiling (negate (tan (cos (max 50 x))))  
fn = ceiling . negate . tan . cos . max 50  

oddSquareSum :: Integer  
oddSquareSum = sum . takeWhile (<10000) . filter odd . map (^2) $ [1..]  

oddSquareSum :: Integer  
oddSquareSum =   
    let oddSquares = filter odd $ map (^2) [1..]  
        belowLimit = takeWhile (<10000) oddSquares  
    in  sum belowLimit  
```

