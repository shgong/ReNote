


# Chapter I. Foundations of Data Systems


> Computing is pop culture. […] Pop culture holds a disdain for history. Pop culture is all
about identity and feeling like you’re participating. It has nothing to do with cooperation,
the past or the future—it’s living in the present. I think the same is true of most people who
write code for money. They have no idea where [their culture came from].
—__Alan Kay__ in interview with __Dr Dobb’s Journal__ (2012)



## 1. Reliable, Scalable and Maintainable Applications

- data-intensive applications
  - database
  - cache
  - search indexes
  - stream processing
  - batch processing
- compose new, special-purpose data system from components
  - primary database
  - application-managed caching layer (memcache)
  - full-text search server (elastic or solr)
  - message queue
- concerns?

### 1.1 Reliability

Reliability or Fault Tolerant

- Hardware Faults
  - like
    - hard disk crash
    - RAM faulty
    - power grid blakout
  - average hard disk failure 10-50 years
    - in 10000 disk data center
    - should expect a failure per day
- Software Errors
  - may crash every instance given a wrong input (like leap second)

### 1.2 Scalability

### 1.3 Maintainability

## 2. Data Models and Query Languages

## 3. Storage and Retrieval

## 4. Encoding and Evolution
