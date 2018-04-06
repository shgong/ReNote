
# Data Model

## 1. Flat Model
- a single, 2d array of elements

```
id    name    team
1     Amy     Blues
2     Bob     Reds
3     Chuck   Blues
4     Richard Blues
5     Ethel   Reds
6     Fred    Blues
7     Gilly   Blues
8     Hank    Reds
9     Hank    Blues
```

## 2. Hierarchy Model
- tree structured
  - windows registry
  - two table
    - parent (one-to-one)
    - children (one-to-many)
  - special case
    - reportsTo as foreign key that point to self
```
[employee]
EmpNo	First Name	Last Name	Dept. Num
100	Mahwish	Faki	10-L
101	Hamadh	Hashim	10-L
102	Nirun	Ar	20-B
103	Chaaya	Sandakelum	20-B

[computer]
Serial Num	Type	User EmpNo
3009734-4	Computer	100
3-23-283742	Monitor	100
2-22-723423	Monitor	100
232342	Printer	100
```

## 3. Network Model

- allows each record to have multiple parent and child records
  - forming a generalized graph structure
- example
  - order-line
    - order
      - customer
    - product
    - shipment

## 4. Relational Model

- based on first-order predicate logic
  - as collection of predicate over finite set of variables
- newer than network / hierarchy

- SQL vs Relational model
  - SQL can have duplicate row
  - order of column is defined
  - NULL

```
Customer (Customer ID, Tax ID, Name, Address, City, State, Zip, Phone, Email,Sex)
Order (Order No, Customer ID, Invoice No, Date Placed, Date Promised, Terms, Status)
Order Line (Order No, Order Line No, Product Code, Qty)
Invoice (Invoice No, Customer ID, Order No, Date, Status)
Invoice Line (Invoice No, Invoice Line No, Product Code, Qty Shipped)
Product (Product Code, Product Description)
```

## 5. Star schema

- centralized fact table
- connect to multiple dimension tables
