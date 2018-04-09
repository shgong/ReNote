# Normalization

- Normalize
  - enhance write performance
  - reduce read performance
  - reduce data redundancy and improve data integrity.
- Normalized design
  - store different but related pieces information
  - in separate logical tables
  - multi-join would be slow


- Denormalization
  - enhance read performance
  - reduce write performance
- Need rules ensure consistency
  - Constraints
    - specify how redundant copies kept synchronized
  - Examples
    - store aggregation of the many elements in one-many relation
      - add attributes to a relation
    - star schema
    - prebuilt summarization
- Denormalization can improve scalability
  - downside: use more storage (columns)
