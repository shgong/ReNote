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
