# Basic select

```sql
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


# Aggregation
```sql
select floor(avg(population)) from city
select ceiling(avg(population)) from city
select max(population) - min(population) from city

--- repalce number values
SELECT CEIL(AVG(Salary)-AVG(REPLACE(Salary,'0',''))) FROM EMPLOYEES;

--- max income and max counts
select max(months*salary), count(*) from employee where months*salary = (select max(months*salary) from Employee)
select (salary * months) as earnings, count(*) from employee group by earnings order by earnings desc limit 1;

--- round
select round(sum(lat_n),2), round(sum(long_w),2) from station

--- other column of max
select ROUND(LONG_W,4) from STATION WHERE LAT_N = (SELECT MAX(LAT_N) FROM STATION WHERE LAT_N<137.2345);

--- query distances
select round(max(lat_n) - min(lat_n) + max(long_w) - min(long_w), 4) from station
select round(sqrt(pow(max(lat_n) - min(lat_n),2) + pow(max(long_w) - min(long_w),2)), 4) from station

--- median
select round(lat_n,4) from station c
where
(select count(*) from station where lat_n>=c.lat_n) = (select count(*) from station where lat_n<=c.lat_n)

```

# Basic Join
```sql
--- city and country
select sum(city.population) from city
inner join country on city.countrycode = country.code
where country.continent = 'Asia'

--- group stats
select country.continent, floor(avg(city.population)) from city
inner join country on city.countrycode = country.code
group by country.continent

--- not equal join
select IF(grade<8, NULL, name), grade, marks from
students inner join grades
on students.marks>=grades.min_mark and students.marks<=grades.max_mark
order by grade desc, name, marks asc

-- top competitors
select x.hacker_id, x.name from (
  select a.hacker_id, a.name, count(*) as c
  from Hackers a
  inner join Submissions b on a.hacker_id = b.hacker_id
  inner join Challenges c on c.challenge_id = b.challenge_id
  inner join Difficulty d on c.difficulty_level = d.difficulty_level
  where b.score = d.score  -- candidate that get full socre
  group by a.hacker_id, a.name
  order by count(*) desc, a.hacker_id
) x where x.c>1

select h.hacker_id, h.name as c from Hackers h
inner join Submissions s on h.hacker_id = s.hacker_id
inner join Challenges c on c.challenge_id = s.challenge_id
inner join Difficulty d on c.difficulty_level = d.difficulty_level
where s.score = d.score
group by h.hacker_id, h.name
having count(s.hacker_id)>1    -- having after group, before order by
order by count(s.hacker_id) desc, s.hacker_id


-- determining the minimum number of gold galleons needed to buy each non-evil wand of high power and age. Write a query to print the id, age, coins_needed, and power of the wands that Ron's interested in, sorted in order of descending power. If more than one wand has same power, sort the result in order of descending age.

select w.id, p.age, w.coins_needed, w.power from Wands as w join Wands_Property as p on (w.code = p.code)
where p.is_evil = 0
and
w.coins_needed = (
    select min(coins_needed) from Wands as w1 join Wands_Property as p1 on (w1.code = p1.code)
    where w1.power = w.power and p1.age = p.age
) order by w.power desc, p.age desc

-- print the hacker_id, name, and the total number of challenges created by each student.
-- print all student with max submission, and other student with unique submission

select H.hacker_id, H.name, count(*) as total
from Hackers H, Challenges C where H.hacker_id = C.hacker_id group by H.hacker_id, H.name
having total =
    (select count(*)
     from challenges
     group by hacker_id
     order by count(*) desc limit 1
     )
or total in
    (select total
     from (
        select count(*) as total
        from Hackers H, Challenges C
        where H.hacker_id = C.hacker_id
        group by H.hacker_id, H.name
      ) counts
     group by total
     having count(*) = 1)
order by total desc, H.hacker_id asc;

-- sum score for each Submissions
select x.hacker_id, x.name, sum(x.hiscore) as total from
(
    select h.hacker_id, h.name, max(score) as hiscore from
    Hackers h
    inner join Submissions s on h.hacker_id = s.hacker_id
    group by h.hacker_id, h.name, s.challenge_id
) x
group by x.hacker_id, x.name
having total != 0
order by total desc, x.hacker_id
```

# Advanced Join



# Alternatives
