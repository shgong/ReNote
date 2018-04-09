# Data Modeling in Big Data

- Relational
- normalization is a good practice
  - it frees your data of integrity issues on alteration tasks (inserts, updates, deletes)
  - it avoids bias towards any query model.

- Big data context
  - schema concept is no longer applied
  - raw data is analyzed from a different point of view
  - does not worth time to put back together normalized data

- Denormalization is good practice
  - a solution to avoid large scale joins
  - but not complete denormalization
  - e.g. one user may change millions columns

- Integrity Issue
  - data only live in big data database
    - require sync and integration of altered data
  - has original source as relational, ETL arrive in Big data
    - restore logical structure of data through ETL
    - risk of excessive denormalization
