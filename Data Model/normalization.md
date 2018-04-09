# Normalization

## Normalize
- Goal
  - enhance write performance
  - reduce read performance
  - reduce data redundancy and improve data integrity.
- Normalized design
  - store different but related pieces information
  - in separate logical tables
  - multi-join would be slow


## Denormalization

- Goal
  - enhance read performance
  - reduce write performance

- Need rules ensure consistency
  - Constraints
    - specify how redundant copies kept synchronized
  - potential issue
    - update anomaly: change address need to apply to multiple rows of the same employee
    - insertion anomaly: can not insert record with missing column value
    - deletion anomaly: we may delete the last record of an employee's project, effectively delete the employee

- Examples
  - store aggregation of the many elements in one-many relation
    - add attributes to a relation
  - star schema
  - prebuilt summarization

- Denormalization can improve scalability
  - downside: use more storage (columns)
