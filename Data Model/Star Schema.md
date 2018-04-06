# Star Schema

## Structure

- Fact + Dimension
  - fact: 5 id and some measure
  - dimension: 1 id and some attributes

- facts: measurable, quantitative data about a business
  - low level
  - example
    - transaction table record sales events
    - snapshot tables record given point with month end
    - accumulation tables record stats for last month
- dimensions: descriptive attributes related to fact data
  - relatively small records
  - example
    - time dimension: lowest level of time granularity
    - geography: location data, country, state, city
    - product
    - employee: sales people assigned
    - range dimension: range of time, dollar values or other to simpify reporting

## Pro & Con

- pro: denormalized
  - simpler queries
  - simpler reporting logic
  - query performance
  - fast aggregation
- con: data integrity
  - one-off insert and updates can result in data anomalies
  - loaded in highly controlled fashion
    - like batch processing, daily
  - more purpose built, not allow more complex logic
  - don't naturally support many-to-many

## example

```
Fact Sales (Date_Id, Store_Id, Product_Id, Unit)
Dim Date (Date_Id Date Day Month Quarter Year)
Dim Product (Product_Id, EAN_code, Product_Name, Brand, Category)
Dim Store (Store_Id, Store_Number, Store_State, Country)
```

```SQL
SELECT P.Brand, S.Country AS Countries, SUM(F.Units_Sold)
FROM Fact_Sales F
INNER JOIN Dim_Date D    ON (F.Date_Id = D.Id)
INNER JOIN Dim_Store S   ON (F.Store_Id = S.Id)
INNER JOIN Dim_Product P ON (F.Product_Id = P.Id)
WHERE D.Year = 1997 AND  P.Product_Category = 'tv'
GROUP BY	P.Brand,	S.Country
```


## Extension

- Multiple Fact Tables
  - Sales: product, client, shop, date
  - Delivery: product, supplier, date
- Dimensional
  - Product: product, brand
  - Time: date
  - Supplier: supplier
  - Client: client, client_group
  - Client_Group: client_group
  - Brand: brand, supplier
  - Shop: shop, city
  - City: city, region
  - Region: region
