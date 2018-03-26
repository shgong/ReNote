# Basic select

```sql
select * from CITY where COUNTRYCODE='USA' and POPULATION>100000
select name from city where countrycode='USA' and population>120000
select * from city
select * from city where id=1661
select * from city where COUNTRYCODE='JPN'
select name from city where countrycode='JPN'


select city, state from station

-- distinct even id city
select distinct city from station where id % 2 = 0

-- count minus distinct
select count(*) - count(distinct(city)) from station

-- longest and shortest city name
select CITY, length(CITY) from station order by length(CITY), city limit 1;
select CITY, length(CITY) from station order by length(CITY) desc, city limit 1;

-- start with vowel
SELECT distinct CITY FROM STATION WHERE CITY LIKE 'a%' or CITY LIKE 'e%' or CITY LIKE 'i%' or CITY LIKE 'o%' or CITY LIKE 'u%';
SELECT distinct CITY FROM STATION WHERE city REGEXP "^[aeiou]";

-- end with vowel
SELECT distinct CITY FROM STATION WHERE city REGEXP "[aeiou]$";

-- both
SELECT distinct CITY FROM STATION WHERE city REGEXP "^[aeiou].*[aeiou]$";

-- do not start with vowels
SELECT distinct CITY FROM STATION WHERE city NOT REGEXP "^[aeiou]";
SELECT DISTINCT CITY FROM STATION WHERE CITY REGEXP '^[^aeiou]';

-- do not end with vowels
SELECT DISTINCT CITY FROM STATION WHERE CITY REGEXP '[^aeiou]$';

-- either
SELECT DISTINCT CITY FROM STATION WHERE CITY REGEXP '[^aeiou]$' or CITY REGEXP '^[^aeiou]';

-- neither
SELECT DISTINCT CITY FROM STATION WHERE CITY REGEXP '^[^aeiou].*[^aeiou]$'

-- sort by the last three characters of each name
select name from students where marks>75 order by Right(name,3), id
```


# Advance Select

```Sql
-- triangle Types
select IF(A+B<=C or B+C<=A or A+C<=B, 'Not A Triangle',
          IF(A=B and B=C, 'Equilateral',
            IF(A=B or B=C or C=A, 'Isosceles', 'Scalene')))  
from TRIANGLES


--- custom string
select concat(name, '(', left(occupation, 1), ')') from occupations order by name;
select concat('There are a total of ', count(*), ' ', LOWER(occupation), 's.') from occupations group by occupation order by count(*), occupation;


--- binary tree node
select n, if(p is null, 'Root',
            if((n in (select distinct p from bst b)) , 'Inner', 'Leaf')) from bst order by n

---  print the company_code, founder name, total number of lead managers, total number of senior managers, total number of managers, and total number of employees
select company_code, founder,
(select count(distinct lead_manager_code) from Lead_Manager where company_code=c.company_code),
(select count(distinct senior_manager_code) from Senior_Manager where company_code=c.company_code),
(select count(distinct manager_code) from Manager where company_code=c.company_code),
(select count(distinct employee_code) from Employee where company_code=c.company_code)
from Company c
order by company_code;
```



# Join
