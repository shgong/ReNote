# Structure and Interpretation of Computer Programs

-----
## 1 Building Abstractions with Procedures

The acts of the mind, wherein it exerts its power over simple ideas, are chiefly these three: 

1. Combining several simple ideas into one compound one
2. Bringing two ideas together, and setting them by one another (to take a view of them at once with all its ideas of relations)
3. Separating them from all other ideas that accompany them in their real existence (abstraction)

> John Locke, An Essay Concerning Human Understanding (1690)

- Computational process, the idea we are about to study
- Lisp, we use for describing processes
    + John McCarthy 1960, AI Group of MIT Research
    + Lisp is for List Processing
    + Recursive Functions of Symbolic Expressions and Their Computation by Machine
- Why using Lisp
    + the language possess unique feature making it execellent medium 
        * for studying important programming constructs & data structures
        * for relating them to linguistic features 
    + most significant feature: Lisp procedures (descriptions of process) can themselves be represented and manipulated as Lisp data
        * blur passive data & active process

### 1.1 The Elements of Programming 

- Every powerful language has three mechanisms to combine ideas
    + primitive expressions
    + means of combination
    + means of abstraction

#### 1.1.1 Expressions
Explore interpreter for Scheme dialect of Lisp

```lisp
( + 137 349)            ;486
(* 5 99)                ;495
(+ 21 35 12 7)          ;75
(+ (* 3 5) (- 10 6))    ;19
```

- Prefix Notation: convention of placing operator to the left
- Advantages 
    + can accommodate procedures take arbitrary number of arguments
    + easy to extend, allow nested combinations 

#### 1.1.2 Naming and the Environment 

```lisp
(define pi 3.14159)
(define radius 10)
(* pi (* radius radius))                ;314.159
(define circumference (* 2 pi radius)) 
circumference                           ;62.8318
```

- `define` is our language's simplest means of abstraction
- interpreter will keep track of name-object pairs in memory, aka environment

#### 1.1.3 Evaluating Combinations 

- in evaluating ocmbinations, interpreter follow a procedures
    + evaluate subexpressions of the combination
    + apply procedure (leftmost operator) to arguments (the other operands)
- stipulations
    + values of numerals are numbers
    + values of built-in operators are machine instruction sequences
    + values of other names are objects associated in environment
        - the role of environment
        - it is meaningless to speak of `(+ x 1)` without specify meaning of `x`
- special forms (exception of general rule)
    + Define: `(define x 3)` does not apply `define` to two arguments

#### 1.1.4 Compound Procedures

```lisp
(define (square x) (* x x))
; (define (<name> <formal parameters>) <body>)

(define (sum-of-squares x y)
  (+ (square x) (square y)))
(sum-of-squares 3 4) ;25
(define (f a)
  (sum-of-squares (+ a 1) (* a 2)))
(f 5) ;136
```

#### 1.1.5 The Substitution Model for Procedure Application 

- To apply a compound procedure to arguments, evaluate the body of the procedure with each formal parameter replaced by the corresponding argument.
- The purpose of the substitution is to help us think about procedure application, not to provide a description of how the interpreter really works.
- The substitution model is only the first of these models -- a way to get started thinking formally about the evaluation process.

```lisp
(f 5)
(sum-of-squares (+ 5 1) (* 5 2))
(+ (square 6) (square 10))
(+ (* 6 6) (* 10 10))
(+ 36 100)
```

##### Applicative order versus Normal order

another evaluation model:  not evaluate the operands until their values were needed
```lisp
(f 5)
(sum-of-squares (+ 5 1) (* 5 2))
(+    (square (+ 5 1))    (square (* 5 2))  )
(+    (* (+ 5 1) (+ 5 1)) (* (* 5 2) (* 5 2)))
(+ (* 6 6) (* 10 10))
(+ 36 100)
```

- normal-order evaluation: full expand & reduce 

- applicative-order evaluation: evaluate arguments then apply
    + lisp actually used
    + avoid multiple evaluation of (+ 5 1)
    + normal order become much more complicated

#### 1.1.6 Conditional Expressions and Predicates

case analysis: `cond`
```lisp
( define (abs x)
    (cond ((< x 0) x)
          ((= x 0) 0)
          ((< x 0) (- x)))

; predicate: first expression in each pair
; if first predicate is false, second is evaluated

(define (abs x)
    (cond ((< x 0) (- x))
    (else x)))
; else for default value

(define (abs x)
    (if (< x 0) (- x) x))
; when there are only two cases
```

- logic compounds
    + (and x y)  
    + (or x y)
    + (not x)

#### 1.1.7 Example: Square Roots by Newton’s Method 

square root
```lisp
(define (sqrt x)
    ( the y (and (>= y 0)
                 (= (square y) x)))


(define (sqrt-iter guess x)
  (if (good-enough? guess x)
      guess
      (sqrt-iter (improve guess x)
                 x)))

(define (improve guess x)
  (average guess (/ x guess)))

(define (average x y)
  (/ (+ x y) 2))

(define (good-enough? guess x)
  (< (abs (- (square guess) x)) 0.001))

(define (sqrt x)
  (sqrt-iter 1.0 x))
```

exercises...

#### 1.1.8 Procedures as Black-Box Abstractions
 
 - decomposition strategy
     + not simply dividing program into parts
     + we can regard other procedure as black box
- local names
    + procedure implementation should not matter to names
    + If the parameters were not local to the bodies of their respective procedures, then the parameter x in square could be confused with the parameter x in good-enough?
- internal definitions and block structure
    + 


### 1.2 Procedures and the Processes They Generate 
#### 1.2.1 Linear Recursion and Iteration
#### 1.2.2 Tree Recursion
#### 1.2.3 Orders of Growth
#### 1.2.4 Exponentiation
#### 1.2.5 Greatest Common Divisors 
#### 1.2.6 Example: Testing for Primality
 
### 1.3 Formulating Abstractions with Higher-Order Procedures
#### 1.3.1 Procedures as Arguments
#### 1.3.2 Constructing Procedures Using Lambda
#### 1.3.3 Procedures as General Methods
#### 1.3.4 Procedures as Returned Values

 
-----
## 2 Building Abstractions with Data
 
### 2.1 Introduction to Data Abstraction
#### 2.1.1 Example: Arithmetic Operations for Rational Numbers 
#### 2.1.2 Abstraction Barriers
#### 2.1.3 What Is Meant by Data?
#### 2.1.4 Extended Exercise: Interval Arithmetic
 
### 2.2 Hierarchical Data and the Closure Property 
#### 2.2.1 Representing Sequences
#### 2.2.2 Hierarchical Structures
#### 2.2.3 Sequences as Conventional Interfaces 
#### 2.2.4 Example: A Picture Language
 
### 2.3 Symbolic Data 
#### 2.3.1 Quotation
#### 2.3.2 Example: Symbolic Differentiation 
#### 2.3.3 Example: Representing Sets
#### 2.3.4 Example: Huffman Encoding Trees
 
### 2.4 Multiple Representations for Abstract Data 
#### 2.4.1 Representations for Complex Numbers 
#### 2.4.2 Tagged data
#### 2.4.3 Data-Directed Programming and Additivity
 
### 2.5 Systems with Generic Operations
#### 2.5.1 Generic Arithmetic Operations 
#### 2.5.2 Combining Data of Different Types 
#### 2.5.3 Example: Symbolic Algebra

-----
## 3 Modularity, Objects, and State
 
### 3.1 Assignment and Local State
#### 3.1.1 Local State Variables
#### 3.1.2 The Benefits of Introducing Assignment 
#### 3.1.3 The Costs of Introducing Assignment

### 3.2 The Environment Model of Evaluation 
#### 3.2.1 The Rules for Evaluation
#### 3.2.2 Applying Simple Procedures
#### 3.2.3 Frames as the Repository of Local State 
#### 3.2.4 Internal Definitions

### 3.3 Modeling with Mutable Data
#### 3.3.1 Mutable List Structure
#### 3.3.2 Representing Queues
#### 3.3.3 Representing Tables
#### 3.3.4 A Simulator for Digital Circuits 
#### 3.3.5 Propagation of Constraints

### 3.4 Concurrency: Time Is of the Essence
#### 3.4.1 The Nature of Time in Concurrent Systems 
#### 3.4.2 Mechanisms for Controlling Concurrency

### 3.5 Streams
#### 3.5.1 Streams Are Delayed Lists
#### 3.5.2 Infinite Streams
#### 3.5.3 Exploiting the Stream Paradigm
#### 3.5.4 Streams and Delayed Evaluation
#### 3.5.5 Modularity of Functional Programs and Modularity of Objects


-----
## 4 Metalinguistic Abstraction
### 4.1 The Metacircular Evaluator
#### 4.1.1 The Core of the Evaluator
#### 4.1.2 Representing Expressions
#### 4.1.3 Evaluator Data Structures
#### 4.1.4 Running the Evaluator as a Program 
#### 4.1.5 Data as Programs
#### 4.1.6 Internal Definitions
#### 4.1.7 Separating Syntactic Analysis from Execution 

### 4.2 Variations on a Scheme -- Lazy Evaluation
#### 4.2.1 Normal Order and Applicative Order 
#### 4.2.2 An Interpreter with Lazy Evaluation 
#### 4.2.3 Streams as Lazy Lists

### 4.3 Variations on a Scheme -- Nondeterministic Computing 
#### 4.3.1 Amb and Search
#### 4.3.2 Examples of Nondeterministic Programs
#### 4.3.3 ImplementingtheAmbEvaluator

### 4.4 Logic Programming
#### 4.4.1 Deductive Information Retrieval
#### 4.4.2 How the Query System Works
#### 4.4.3 Is Logic Programming Mathematical Logic? 
####4.4.4 Implementing the Query System

----- 
## 5 Computing with Register Machines

### 5.1 Designing Register Machines
#### 5.1.1 A Language for Describing Register Machines 
#### 5.1.2 Abstraction in Machine Design
#### 5.1.3 Subroutines
#### 5.1.4 Using a Stack to Implement Recursion
#### 5.1.5 Instruction Summary

### 5.2 A Register-Machine Simulator
#### 5.2.1 The Machine Model
#### 5.2.2 The Assembler
#### 5.2.3 Generating Execution Procedures for Instructions 
#### 5.2.4 Monitoring Machine Performance

### 5.3 Storage Allocation and Garbage Collection 
#### 5.3.1 Memory as Vectors
#### 5.3.2 Maintaining the Illusion of Infinite Memory

### 5.4 The Explicit-Control Evaluator
#### 5.4.1 The Core of the Explicit-Control Evaluator 
#### 5.4.2 Sequence Evaluation and Tail Recursion 
#### 5.4.3 Conditionals, Assignments, and Definitions 
#### 5.4.4 Running the Evaluator

### 5.5 Compilation
#### 5.5.1 Structure of the Compiler
#### 5.5.2 Compiling Expressions
#### 5.5.3 Compiling Combinations
#### 5.5.4 Combining Instruction Sequences
#### 5.5.5 An Example of Compiled Code
#### 5.5.6 Lexical Addressing
#### 5.5.7 Interfacing Compiled Code to the Evaluator


-----

## NOTES








