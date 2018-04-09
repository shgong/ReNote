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


## Product Dimension

- Category Lookup
  - 1: Apparel
  - 2: Shoe
- Sub-Category
  - 11: Shirt
  - 12: Trouser
  - 13: Casual
  - 14: Formal
- Product
  - 1001: Van Heusen
  - 1002: Arrow
  - 1003: Nike
  - 1004: Adidas
- Product Feature
  - 10001 Van-M
  - 10002 Van-L
  - 10003 Van-XL


## Development Cycle

- Conceptual Data modeling
  - Draw diagram
  - Bank
    - Investment
      - Saving
      - Checking
      - Certificate Deposit
    - Credit Card
      - General Card
      - Premiere Card
      - VIP Card
    - Loan
      - Mortgage
      - Personal

- Logical Data Modeling
  - employee
    - state lookup
    - country Lookup
    - city lookup
  - employer
  - employer employee xref

- Physical Data Modeling
  - consider
    - database performance
    - indexing strategy
    - physical storage
  - employee
    - id, Number(10,0), NN, PK
    - country_cd: Varchar2(5), NN, FK
    - st_cd: Varchar2(5), NN, FK
    - city_cd: Varchar2(5), NN, FK
    - Manager_id, Number, N, FK
    - Name, Varchar2(15), NN

## Type of Fact tables
