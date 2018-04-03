
```sql
--- second highest salary
SELECT MAX(Salary) AS SecondHighestSalary FROM Employee WHERE Salary!=(SELECT MAX(Salary) FROM Employee)

--- nth highest Salary
CREATE FUNCTION getNthHighestSalary(N INT) RETURNS INT
BEGIN
  DECLARE M INT;
  SET M=N-1;
  RETURN (
      # Write your MySQL query statement below.
      SELECT DISTINCT Salary FROM Employee ORDER BY Salary DESC LIMIT M,1
  );
END

--- score rank
SELECT
  Score,  (SELECT count(distinct Score) FROM Scores WHERE Score >= s.Score) Rank
FROM Scores s
ORDER BY Score desc

--- consecutive numbers
Select DISTINCT l1.Num AS ConsecutiveNums from Logs l1, Logs l2, Logs l3
  where l1.Id=l2.Id-1 and l2.Id=l3.Id-1
and l1.Num=l2.Num and l2.Num=l3.Num


--- employee earn more than manager
SELECT a.Name AS Employee FROM Employee a, Employee b WHERE a.ManagerId=b.Id AND a.Salary>b.Salary

--- duplicate emails
select DISTINCT a.Email From Person a, Person b WHERE a.Email=b.Email AND a.Id!=b.Id

--- customer never orders
Select Name AS Customers FROM Customers WHERE Id NOT IN (Select DISTINCT CustomerId from Orders)

--- high salary by department
select a.Name as Department, b.Name as Employee, b.Salary
from Department a, Employee b
  where a.Id = b.DepartmentId and NOT EXISTS
  (select 1 from Employee where DepartmentId=a.Id and Salary>b.Salary)

--- top3 salary by Department
select a.Name as Department, b.Name as Employee, b.Salary
from Department a, Employee b
  where a.Id = b.DepartmentId and
  (select count(distinct salary) from Employee where DepartmentId=a.Id and Salary>b.Salary)<3

--- rising temperature
SELECT b.Id FROM Weather a, Weather b WHERE a.Temperature<b.Temperature AND b.Date = a.Date + Interval 1 Day

--- delete duplicate email
DELETE p from Person p, Person q where p.Id>q.Id AND q.Email=p.Email

---  find the cancellation rate of requests made by unbanned clients between Oct 1, 2013 and Oct 3, 2013

SELECT Request_at as Day,
       ROUND(COUNT(IF(Status != 'completed', TRUE, NULL)) / COUNT(*), 2) AS 'Cancellation Rate'
FROM Trips
WHERE (Request_at BETWEEN '2013-10-01' AND '2013-10-03')
      AND Client_id NOT IN (SELECT Users_Id FROM Users WHERE Banned = 'Yes')
GROUP BY Request_at;

--- list out all classes which have more than or equal to 5 students.

SELECT class FROM (
    SELECT class, count(DISTINCT student) as num
    FROM courses
    GROUP BY class
) r WHERE num>4

--- swap sex
UPDATE salary
SET sex = IF(sex='m', 'f', 'm')

```
