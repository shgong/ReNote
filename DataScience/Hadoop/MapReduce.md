
# MapReduce

Feature: a brute-force approach, entire dataset is processed for each query. MapReduce is a batch query processor, can run ad hoc query against whole dataset to make good decisions.

### Steps

1. Input Data
2. Split into map tasks
3. Each mapper output key-value pairs
4. Shuffle and Sort
5. Reduce
6. Output


##### Mapper
- read the data in key/value pairs
- map(in_key,in_value) -> (inter_key,inter_value) list
- mapper may use or ignore key
    + key is byte offset into the file start
    + value is contents of line
    + typically key is considered irrelevant

Mapper output (intermediate data) is written to the Local file system (NOT HDFS) of each mapper slave node. Once data transferred to Reducer, We won’t be able to access these temporary files.
(No need to 3 copy, just start a new mapper if losing)

##### Reducer

Shuffle and Sort

- After map phase, all inter values are combined together
- same key guaranteed to the same Reducer
- keys and value lists passed to reducer in sorted key order

Shuffle is mini reducer for each block
Combiner also extend Reducer interface

When is the earliest point at which the reduce method of a given Reducer can be called?  Not until all mappers have finished processing all records.  

##### Why Map Reduce
In data center, Map Task and HDFS Block, could be in same node, in same rack, or different rack, in the worst case.If when not in the same node, we always take processing to the data.

Task Tracker don't move from one to another



```python
def mapper(line):
foreach word in line.split():
    output(word,1)

def reducer(key, values[]):
    output(key,sum(values))


```

# Advanced Topics of Map Reduce

### Combiner
- Mini-reducer in map phase
    + Passed workload further to the Reducers
- Perform a Local Reduce
    + before distribute the mapper result

eg. local word count

### Partitioner
Partitioner: determine which reducer responsible for a particular key

### Distributed Cache
cache file needed by applications
files copied only once per job and should not be modified when running
DistCache can be used to distribute different file

### Map & Reduce Side Join
For task with two tables: 
e.g. Customer: CustID Name Phone
   Tranaction: CustID TransID Amount Location

map side: duplicate small table to each mapper with DistCache
reduce side: Shuffle all lines together to reducer

### Counter
mechanism used for collecting statistical info
useful for diagnose problem in job processing
similar to putting log message in codes


### Input Format
Data passeed to mapper is specified by inputFormat, determine how to split input data into input splits for each mapper

InputFormat is a factory for RecordReader objects to extract 
 (key, value) records from the input source.

- File
- Text (Default)
- Key Value Text: default separator is tab
- Sequence File: binary
- Sequence File As Text: map binary to string
- N line Input Format: treat n lines as a separate inputsplit
- Multi File: abstract class, need supply a getRecordReader() implementation

### Output Format
Reducer->Record Writer->Output File

- File
- Text (Default)
- Sequence File
- Sequence File As Binary

##### InputSplit

- a chunk of the input that is processed by a single map 
– Input data sources (e.g., input files) are divided into fragments 
– Fragments encapsulated in instances of the InputSplit interface 

##### RecordReader
Record Reader is an iterator over records, to generate key-value pairs.

- Ensure each (k,v) pair is processed
- Ensure no (k,v) pair is processed more than once
- Handle (k,v) pair which split across InputSplits

Map task passes split to getRecordReader() on inputFormat to obain a RecordReader, and then pass RecordReader to map function.





### Sequence File
Hadoop is not restricted to plain text, with unstructured data, use binary data type, SequenceFile, a flat file of binary key/value pairs

- Output of Maps stored using Sequence
    - Provider
    - Sorter 

Three different formats


e.g.
images files, find duplication


### Record Compression
record length, key length, key, value -> compressed value


