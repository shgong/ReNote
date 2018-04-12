# Summary

- Write a series of SQL queries using a MySQL database.
- We use MySQL for convenience and knowing standard Ansi SQL is sufficient for the interview.
- The purpose is to ensure you are able to think about problems using a set-based rather than procedural approach.
- We will evaluate you on understanding of fundamental SQL concepts like different types of:
   - Joins (inner, left/right outer, full outer, cross)
     - cross join: each row join with each row, produce x*y rows
   - Nested and Correlated subqueries
     - correlated, use values from the outer query
     - can be inefficient (once per row, better with aggregated data)
     - join is better
   - Aggregations (COUNT, SUM, AVERAGE and etc)
   - ‘where’ vs ‘having’
     - where (for rows) -> group by -> having (for group aggregation)
   - Handling NULL values
     - IFNULL(Units, 0)
     - COALESCE(Units, 0)
   - Case statements (IF, THEN, ELSE, WHEN and etc)
   - Filters
   - Groupings
   - Rankings
   - Recursive queries
- You’ll also need to verify the results and take care of exceptions and NULL handling

```
CASE
    WHEN Quantity > 30 THEN "The quantity is greater than 30"
    WHEN Quantity = 30 THEN "The quantity is 30"
    ELSE "The quantity is something else"
END

IF(500<1000, "YES", "NO")
```


- Window Function
  - show aggregation by group

```sql
   select emp_name, dealer_id, sales, avg(sales) over (partition by dealer_id) as avgsales from q1_sales;
   +-----------------+------------+--------+-----------+
   |    emp_name     | dealer_id  | sales  | avgsales  |
   +-----------------+------------+--------+-----------+
   | Ferris Brown    | 1          | 19745  | 14357     |
   | Noel Meyer      | 1          | 19745  | 14357     |
   | Raphael Hull    | 1          | 8227   | 14357     |
   | Jack Salazar    | 1          | 9710   | 14357     |
   | Beverly Lang    | 2          | 16233  | 13925     |
   | Kameko French   | 2          | 16233  | 13925     |
   | Haviva Montoya  | 2          | 9308   | 13925     |
   | Ursa George     | 3          | 15427  | 12368     |
   | Abel Kim        | 3          | 12369  | 12368     |
   | May Stout       | 3          | 9308   | 12368     |
   +-----------------+------------+--------+-----------+

 select dealer_id, sales, emp_name,row_number() over (partition by dealer_id order by sales) as `row`,avg(sales) over (partition by dealer_id) as avgsales from q1_sales;
   +------------+--------+-----------------+------+---------------+
   | dealer_id  | sales  |    emp_name     | row  |      avgsales |
   +------------+--------+-----------------+------+---------------+
   | 1          | 8227   | Raphael Hull    | 1    | 14356         |
   | 1          | 9710   | Jack Salazar    | 2    | 14356         |
   | 1          | 19745  | Ferris Brown    | 3    | 14356         |
   | 1          | 19745  | Noel Meyer      | 4    | 14356         |
   | 2          | 9308   | Haviva Montoya  | 1    | 13924         |
   | 2          | 16233  | Beverly Lang    | 2    | 13924         |
   | 2          | 16233  | Kameko French   | 3    | 13924         |
   | 3          | 9308   | May Stout       | 1    | 12368         |
   | 3          | 12369  | Abel Kim        | 2    | 12368         |
   | 3          | 15427  | Ursa George     | 3    | 12368         |
   +------------+--------+-----------------+------+---------------+


-- Running Total, from start time
-- if no order by, will have global sum, not sliding sum
-- if order by start_time, same start_time will be calculated together
--                         n tile function also depend on order
SELECT duration_seconds,
       SUM(duration_seconds) OVER (ORDER BY start_time) AS running_total
  FROM tutorial.dc_bikeshare_q1_2012


SELECT start_terminal,
       duration_seconds,
       SUM(duration_seconds) OVER
         (PARTITION BY start_terminal ORDER BY start_time)
         AS running_total
  FROM tutorial.dc_bikeshare_q1_2012
 WHERE start_time < '2012-01-08'

```
