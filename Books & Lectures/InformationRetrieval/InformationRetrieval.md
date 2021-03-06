
# Preface

Information retrieval (IR) is finding material (usually documents) of
an unstructured nature (usually text) that satisfies an information need
from within large collections (usually stored on computers).

# 1. Boolean Retrieval

## 1.1 Example IR problem

- Shakespeare's Collected Works
  - find which play contains Brutus & Caesar & !Calpurnia
  - simplest: linear search
  - more requirements
    - flexible match (distance of related words)
    - best match (ranked)
- Build Matrix
  - Index to avoid linearly scanning texts
  - record whether each document contains a word
    - Binary term-document incidence matrix

||Antony and Cleopatra|Julius Caesar|The Tempest|Hamlet|Othello|Macbeth
|---|---|---|---|---|---|---
|Antony|1|1|0|0|0|1
|Brutus|1|1|0|1|0|0
|Caesar|1|1|0|1|1|1
|mercy|1|0|1|1|1|1

  - To answer which play contains Brutus & Caesar & !Calpurnia
    - take the vector, complement if not, do a bitwise AND
    - 110100 AND 110111 AND 101111


- Scale up
  - N=1m, ~1000 words, ~6 bytes(including spaces) => 6GB corpus
  - 500k distinct terms inside
- Effectiveness
  - Precision(how relevant are positives): tp/(tp+fp)
  - Recall(how good to detect positive): fn/(tp+fn)
  - specificity(how good to avoid false positive): tn/(tn+fp)
- We cannot build 500k x 1m matrix to fit memory
- Inverted Index
  - first major concept in IR
  - keep a dictionary of terms, each term have a list of document it occurs, sort by docID

## 1.2. Inverted Index
- Generation
  - docID
  - sorted term dictionary, each has a posting list
  - also keep stats like document frequency, length of posting list
- Result
  - Dictionary: keep in memory
  - Posting list
    - memory: singly linked list / variable length array => skip lists
    - if infrequent update: hybrid scheme of linked list of fixed length array
    - disk: contiguous run of postings without explicit pointers


## 1.3 Processing Boolean Queries

- Simple conjunctive queries
  - find doc that contains both terms
    - posting list intersection
    - posting merge: two traverse on posting, each step move pointer on the smaller docId
  - multiple terms
    - process smaller posting list first to reduce intermediate expression size


## 1.4 Extended Boolean model vs Ranked Retrieval

- ranked retrieval model
  - like vector space model
  - query with free text (search engine)

## Example 1.1 Commericial Boolean Search: Westlaw

- largest commercial legal search service from 1975
- use boolean search (Terms and Connectors) until 2005

```
# Prevent former employee disclose trade secret
Query: "trade secret" /s disclos! /s prevent /s employe!

# host's responsibility for drunk guests
Query: host! /p (responsib! liab!) /p (intoxicat! drunk!) /p guest

/s /p /k, AND, same sentence, paragraph, within k words
! trailing wildcard query
```


# 2. Term vocabulary and posting list

## 2.1 Document delineation and character sequence decoding

### 2.1.1 Char Sequence

- determine the encoding (a classification problem)
  - ASCII
  - Unicode UTF-8
  - Various national / vendor-specific standards
- decode out of binary representations
  - zip compression
  - MS Word file
  - plain text char entities
    - XML &amp
- extract text out of structure
  - pdf, postscript
- char sequence also questioned by some writing systems
  - like Arabic
  - from right to left, with complex mutations

### 2.1.2 Doc Unit

- Determine granularity
- email
  - thread or message
- single document or multiple pages
  - powerpoint or LATEX
- book
  - chapter, book, passages


## 2.2 Determine vocabulary of terms

### 2.2.1 Tokenization

- What correct token to use
  - like `O'neil` and `aren't`
- This issues of tokenization are language-specific

- language identifications
  - languages C++ and C#
  - aircraft names B-52
  - url jblack@gmail.com
  - IP addr 142.32.48.231

- hyphen
  - join vowels `co-education`
  - word grouping `the hold-him-back maneuver`
  - word connecting `New York-San Francisco`
  - alternate writing `lowercase` vs `lower-case`

- different languages
  - French
    - use apostrophe for article before vowel word `l'ensemble`
    - or its full form `un emsemble`
    - use hyphen with postposed clitic pronouns in imperatives and questions `donne-moi, give me`
  - German
    - compound nouns without spaces
      - `Computerlinguistik` Computational linguistics
      - `Lebensversicherungsgesellschaftsangestellter` Life insurance company employee
    - better with a compound-splitter module
  - East asian languages CJKThai
    - text written without any spaces between words
    - need word segmentation
    - ambiguity, like `和尚` means `monk` or `and/still`

### 2.2.2 Dropping common terms: stop words

- stop list, like `of`, `the`, `we`
- may ignore some phrases full of stop words
  - Vannevar Bush's article `As we may think`
  - Shakespeare `To be or not to be`
- IR systems evolves from 200-300 stop words => 7-12 => no stop word over time

### 2.2.3 Normalization
- two main approach
  - mapping rules
    - implicitly create equivalent classes
    - `USA` and `U.S.A`
    - you may not add characters like `antidiscriminatory` => `anti-discriminatory`
  - maintain relations between unnormalized tokens
    - maintain query expansion list, more flexible
    - automobile => car
    - windows, Windows => window, Windows => operation system
    - USA => U.S.A.

- common practices and problems
  - accents and diacritics
    - normalize tokens to remove diacritics
    - english: like `cliché` and `cliche`
    - spanish: `peña` is ‘a cliff’, while `pena` is ‘sorrow`
  - capitalization/case-folding
    - reducing all letters to lowercase
    - person names: Bush, Black
    - companies: General Motors, The Associated Press
  - date format
  - British and American spelling
    - color, colour
  - French article `the` depend on gender and vowel
    - le, la, l', les
  - German, vowels with umlaut can be rendered together
    - `Schütze` and `Schuetze`
  -Japanese mix Chinese characters, two syllabaries(hiragana/katakana) and western chars
    - a word can be written in katakana for emphasis
    - same word can write as hiragana or Chinese characters

### 2.2.4 stemming and lemmatization

- problem
  - different forms of a word for grammatical reasons
    - organize, organizes, organizes, organizing
  - derivationally related words
    - democratic, democratization
- stemming: chops off the ends of words, remove derivational affixes
- lemmatization: return base or dictionary form, like saw => see

#### Porter Stemmer

- Porter's algorithm (Porter, 1980)
- Rule Group
  - SSES => SS
  - IES => I
  - SS => SS
  - S =>
  - (m>1) EMENT =>
- [website](http://www.tartarus.org/˜martin/PorterStemmer/)

- problem
  - forms of a word is important in some cases
  - operational Research
  - operating System
  - operative dentistry

  ## 2.3 Faster posting list intersection via skip pointers

-  augment with skip pointers at indexing time
  - jump based on next docId
  - where to place skip is a tradeoff, usually sqrt(P)

## 2.4 Positional popsting and phrase queries

- Stanford University should be treated as a phrase
  - may also get `Stanford went to university` if separated

### 2.4.1 Biword indexes

- use pair of words when indexing
  - stanford university palo alto
  - 3 searches: stanford university, university palo, palo alto
  - remove prepositions like `of`, `the`

### 2.4.2 Positional indexes

```
to, 993427:
  1, 6:[7, 18, 33, 72, 86, 231]
  2, 5:[1, 17, 74, 222, 255]
  4, 5:[8, 16, 190, 429, 433]
  5, 2:[363, 367]
  7, 3:[13, 23, 191]

The word to has a document frequency
993,477, and occurs 6 times in document 1 at positions 7, 18, 33, etc.
```

- for each word pair, start from the word with least frequency and search for candidates
  - like `employment /3 place` connection

### 2.4.3 Combination Schemes

- use Biword Index for some phrases and positional for some else
- next word index
  - Williams et al. (2004)
  - add records of terms that follow it in a document.
  - completed in one quarter of the time, while 26% more space than use of a positional index alone.


## 3. Dictionaries and tolerant retrieval

### 3.1 Search Structure for dictionaries

- Two broad classes of solutions
  - Hashing
    - cannot find minor variants (hashed into different buckets)
    - cannot search with prefix
  - Search Trees
    - basic: binary search tree
    - dictionary: B tree
    - cannot process character language without unique ordering

- B Tree
  - every internal node has number of children in interval [a,b]
  - collapsing multiple level of bst into one
  - when dictionary is disk-resident, use disk block to determine [a,b]


### 3.2 Wildcard query

- Trailing wildcard query `mon*`
  - use B tree directly
- Leading wildcard query `*mon`
  - consider reverse B-tree, written backwards
- Infix wildcard `se*mon`
  - use B-tree and reverse B-tree together
  - find the intersection


#### 3.2.1 General wildcard queries

- Permuterm indexes
  - add symbol $ as end term in our char set
  - construct various permuterm of each term
    - e.g. hello
      - hello$
      - o$hell
      - lo$hel
      - llo$he
      - ello$h
  - when query `m*n`
    - rotate to make * appear at the end
    - become `n$m*`, thus fit in search tree

- problem
  - for each english word, take 10x space

#### 3.2.2 k-gram indexes

- dictionary contains all k-grams that occur in any term
- `re*ve`
  - run boolean query `ve$` and `$re`
  - does not match exactly
- post filtering
  - checked individually against original query
  - simple string match

### 3.3 Spelling Errors

- principle
  - choose nearest one
  - choose most common one
- use edit distance
  - insert
  - delete
  - replace
- fill matrix m with 0, length on axis
  - m(i,j) hold edit distance of first i of s1, first j of s2
  - m(i,j) = min of
    - m(i-1,j) + 1
    - m(i, j-1) + 1
    - m(i-1,j-1) + if s1[i]==s2[j] then 0 else 1

- k-gram index
- content senstive spelling correction

### 3.4 Phonetical Correction

- Soundex
  - retain first letter
    - A E I O U H W Y => 0
    - B F P V => 1
    - C G J K Q S X Z => 2
    - D T => 3
    - L => 4
    - M N => 5
    - R => 6
  - repeatedly remove pairs
  - remove zero, pad with trailing zero, return first four position

- Example
  - Hermann = H655

## 4. Index Construction

### 4.1 Hardware basics

- memory access: 5x10-9s/b
- disk access: 2x10-8s/b
- seek time: 5x10-3s
  - when disk io, disk head move to where data located
- move 10MB from disk to memory
  - if saved together: 0.2s
  - if in 100 parts，add 100x seek time = 0.5s
  - handled by system bus, not processor
    - processor is available during disk io
    - can use efficient decompress algorithm
- OS read write entire block
  - block size of 8, 16, 32, 64 kb are common


### 4.2 blocked sort-based indexing

- main approach
  - pass through collection assembling all term-docId pairs
  - sort pairs with term
  - organize docId into posting list
  - large list need secondary storage
- need external sorting
  - must minimize random disk seeks during sorting
- Blocked sort-based indexing, or BSBI
  - single-machine algorithm designed for static collections
  - indexing time dominated by parsing and merge final block

```
n=0
while (all documents not processed)
  do n = n + 1
     block = parseNextBlock()
     BSBI-Invert(block)
     WriteBlockToDisk(block, fn)
  MergeBlocks(f1, .. fn) => f
```

### 4.3 single-pass in-memory indexing

- more scalable, use terms instead of term ID
- start a new dictionary for next block


SPIMI Invert
```
while (free memoery available)
for each token
  get or create posting list in dictionary
  double if posting list full
  add docID in it
sort dictionary
write block to disk
```

- Difference
  - don't collect termId-docId
  - each posting list is dynamic growing
  - single pass: easy to compression


### 4.4 distributed indexing

- term-partition index / MapReduce
  - master node
  - 16MB or 64MB chunk size
  - recasting into key-value pairs
  - map phase: parser
  - reduce phase: inverter => create posting list

### 4.5 dynamic indexing

- frequent changed documents, like some user-generated website, want search index updated quickly
- maintain two index
  - main index
  - auxiliary index that stores new documents
    - keep in memory
    - merge to main when too large
  - deletions kept as invalidation bit vector
- search through both index and then merge
  - merge is the main workload

### 4.6 Other system

- Ranked retrieval system: posting sorted by weight, insert by order
- Security: return list should be filtered by clearance level
  - ACL: Access Control List


## 5. Index Compression

- Why Compression
  - lower disk space
  - increase use of caching, fit more frequent items in memory
  - faster transfer of data, disk=>memory

### 5.1 Statistical Properties

- Rule of 30
  - 30 most common words account for 30% of tokens
- Lossless and lossy
  - lossy: case folding, stemming, stop words eliminatione
- Heap's Law
  - estimat number of items
  - M = k T^b
  - M = 44 * 100,000^0.5
- Zipf's law
  - modeling distribution of items
  - collection frequency of ith most common item is proportional to 1/i

### 5.2 Dictionary compression

- why, dictionary is so small compred to posting list
  - to fit in main memory, reduce disk seek
  - have fast start up time, share resource with other applications

- Dictionary as a string
  - use fixed-width entries for terms is wasteful
    - avg length of english words is 8 words
  - store all terms as a long string,
    - put pointer index for each term id
  - compress Reuters-RCV from 11.2mb to 7.6mb

- Blocked storage
  - grouping terms in the string into blocks of size k
  - store length of term as additional beginning byte
    - eliminate k-1 term pointers, but need k bytes for sizes
    - if 4 term per block, save 3*3-4=6 byte compared to dictionary string

- trade off
  - after binary search to the leave
  - need go through the k terms to find required term

### 5.3 Posting file compression

- 800,000 documents
  - docID: log(2, 800,000) ~ 20 bit ~ 2.5 byte
  - store gaps instead
    - gap for frequent items are mostly 1
    - gap for rare item need 20 bit

#### 5.3.1 Variable byte codes

- use integral number of bytes to encode a gap
- 1 bit continuation + 7 bit payload
  - gap = 5
    - 10000101
    - 1 means code ends with this byte
  - gap = 214577
    - 00001101 00001100 10110001

#### 5.3.2 miu codes

- bit level encoding
  - unary code n is n 1s foolowed by a 0
- split representation of a gap G into (offset length, offset)
  - offset is G in binary with leading 1 removed, prepend with unary length
    - size 2xlog(2,G)
  - gap = 13
    - binary 1101, offset 101
    - length 3, use unary 3: 1110
    - encoded: 1110101
- why
  - prefix free
  - parameter free


## 6. Scoring, term weighting and vector space model

### 6.1 Parametric and zone indexes

- Weighted zone scoring
  - query q and document d, assign a score [0,1]
  - or ranked boolean retrieval
- three weights
  - g1=0.2 author
  - g2=0.3 title
  - g3=0.5 body
- learning weights: machine learned relevance
  - find optimal weight g

### 6.2 Term frequency and weighting

- higher term frequency should yield higher score
- bag of words model
  - order is ignored, only keep occurence
- Inverse document frequency
  - scale down tf weight by a factor of collection frequency
  - idf = log(N/df)
    - N is total number of documents
    - df is number of documents that contain term
- tf-idf weighting
  - tfidf(t,d) = tf(t,d) x idf(t)
  - at this point, we may view each document as vector
  - score(query, d) = Sum(tfidf) for each term t

### 6.3 vector space model

- dot products
  - similarity of documents
    - sim(d1,d2)=V(d1) dot V(d2) / |V(d1)| |V(d2)|
- queries as vector
  - v(q) dot v(d)
    - score = V(q) dot V(d) / |V(q)||V(d)|


### 6.4 Variant of td-idf

-  Term Frequency
  - natural: tf
  - logarithm: 1 + log(tf)
  - augmented: 0.5 + 0.5*tf/max(tf)
  - boolean: 1 if tf>0 else 0
  - log ave: 1+log(tf) / 1+log(average(tf))
- document frequency
  - no: 1
  - idf: log(N/df)
  - prob idf: max{0, log((N-df)/df)}
- normalization
  - no: 1
  - cosine: 1/sqrt(sum(wi^2))
  - pivoted unique: 1/u
  - byte size: CharLength^(-a)

## 7. Computing scores in a complete search system
### 7.1 Efficient scoring and ranking

FastCosineScore(q)
```
for each d:
  initialize Length(d)
for each t:
  calculate w(t,q), fetch posting list for t
  for each pair(d, tf):
    add wf(t,d) to Score(d)
for each d:
  Score(d) = Score(d)/Length(d)
return top K score(d)
```

- Inexact top K, lower cost of computing
  - find a set A of documents that are contenders
  - K < |A| << N
- Index elimination
  - only consider documents contains term idf exceeds threshold
  - only consider documents contain all of terms
- champion list
  - precompute top d for each term


### 7.2 Components of an information retrieval system

- pull together all elements to outline a complete system

#### 7.2.1 Tiered indexes

- using inexact top-K retrieval may yield fewer than K documents
- use tiered indexes
  - a generalization of champion list
  - tier 1 index only tf>20
  - tier 2 index tf>10

#### 7.2.2 Query-term proximity

- search term position proximity should affect score
- hand coding and machine learning
