# Learn Data Modeling

http://learndatamodeling.com/blog/category/data-modeling/


## Relational Data Modeling

- Source table
  - State
  - County
  - City
  - First name
  - Last name
  - Full name
  - Manager name
  - Branches name
  - Timestamp

- Discussion
  - City and county
  - county and state
  - state and USA
  - employee in branches
  - employee with no manager

- Employee Branch cross reference XREF
  - Employee
    - State Code FK
      - State Name, time
    - Country Code FK
      - County Name, time
    - City Code FK
      - City Name, time
    - Manager Id FK
    - First Name
    - Last name
    - Full name
    - Timestamp
  - Branches
    - Branch Name, time

## Time Dimension

- Year Lookup
- Quarter Lookup
- Month Lookup
- Week Lookup
- DateAdd(year, 1, '2017-08-21')
- where date>date_from and date <= DateAdd(year, 1, date_from)

## Organization Dimension

- Corporate Lookup
  - AAPL
  - GOOG
- Region
  - SE: Southeast
  - MW: MidWest
- Branch Lookup
  - ILCH: Illiois-Chicago
  - FLTM: Florida-Tampa
- Employee Lookup
  - E102111
  - E101332
- Organization Dimension
