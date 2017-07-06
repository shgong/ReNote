```
mysql -u root -p

CREATE USER 'shgong'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON * . * TO 'shgong'@'localhost';
FLUSH PRIVILEGES;


```


# cmd start

 /usr/bin/mysql -u root -p password

SHOW DATABASES;
USE ----;
show tables;
 
# Basic

INSERT: add data
UPDATE: change data
SELECT...FROM: read data
DELETE: remove data
WHERE: filter data, AND, OR   = > < <>
ORDER BY: sort data, DESC, ASC
HAVING: restrictions

## Database

```sql
CREATE DATABASE ________;
DROP DATABASE ________;

CREATE TABLE ______
(
    column_name1 datatype,
    column_name2 datatype,
    column_name3 datatype
);

CREATE TABLE users
(
    usersname varchar(20),
    password varchar(20)
);


CREATE TABLE movies
(
    id          int,
    title       varchar(50),
    genre       varchar(100),
    duration    int
);

DROP TABLE movies;

ALTER TABLE movies ADD COLUMN ratings int;

ALTER TABLE movies DROP COLUMN ratings;

```




## Table

```sql
SELECT id, title, genre, duration FROM movies;
SELECT * FROM movies;
SELECT * FROM movies WHERE genre='Adventure' ; 

INSERT INTO movies (title, genre, duration) 
VALUES('Peter Pan', 'Animation', 134);

INSERT INTO concessions (id, item, price)
VALUES (8, 'Pizza', 2.00);

UPDATE movies SET title = 'The Iron Mask' WHERE id = 4;

DELETE FROM movies WHERE genre='Comedy'; 
```



# Advanced

## Aggregate Functions

count(), sum(), avg(), max(), min()

```sql
SELECT count(*) FROM Movies;

SELECT max(salary),min(salary) FROM Actors;

# sum for each genre
SELECT genre, sum(cost) 
FROM Movies 
GROUP BY genre
HAVING COUNT(*)>2;

```


## Identifying Constraints

### effect
Prevent NULL values
Ensure column values are UNIQUE
Provide additional VALIDATIONS
CHECK value in range
// will prompt error when lack value / duplicate value

### primary key
Most Table should have a primary key, only once
both UNIQUE and NOT NULL, e.g. id key

### foreign key
a column that reference the primary key column of another table
query relationship data, need constraint

Orphan records:the child record when parent record deleted,
SQL will prompt error when delete referenced data


```sql
# basic way of creating
CREATE TABLE Promotions
(
    id int,
    name varchar(50) NOT NULL UNIQUE,
);

# use name for constraint, easy for alter later
CREATE TABLE Promotions
(
    id int PRIMARY KEY,
    name varchar(50) NOT NULL,
    CONSTRAINT unique_name UNIQUE (name)
);

# specify each pair is unique, but not instance
   CONSTRAINT unique_pair UNIQUE (name,category)

# forign key
CREATE TABLE Promotions
(
    id int PRIMARY KEY,
    movie_id int REFERENCES movies(id),
    #OR  movie_id int REFERENCE movies,(key name same)
    name varchar(50),
    duration int CHECK (duration > 0)
);

# another syntax
CREATE TABLE Actors (
  id int PRIMARY KEY,
  name varchar(50) NOT NULL UNIQUE,
  country_id int,
  FOREIGN KEY(country_id) REFERENCES Countries(id)
);
```


## Normalization

How to select multiple genre,  "Adventure,Fantasy"?
It is not selected when WHERE genre="Adventure"

### Normal Form Rule
must not contain repeating groups of data in 1 column
must not contain unnecessary repeating of information

### join table
a genre table (genre_id, genre_name)
a movies table (id, title, duration)

join table: Movies_Genres
movie_id, genre_id, 
```sql
# add genre to table:
INSERT INTO Movies_Genres(movie_id, genre_id);


# How to find the genre of Peter Pan?
SELECT id FROM Movies WHERE title="Peter Pan";  # return 2
SELECT genre_id FROM Movies_Genres WHERE movie_id=2; # return 2,3
SELECT name FROM Genres WHERE id IN (2,3);
```

## JOIN

### Inner joins

Movie (id, title, genre)
Review(id, review, movie_id)
```sql
SELECT Movies.title, Reviews.review
FROM Movies
INNER JOIN Reviews
ON Movies.id=Reviews.movie_id

# Peter Pan Example
SELECT Movies.title,Genres.name
FROM Movies
INNER JOIN Movies_Genres
ON Movies.id = Movies_Genres.movie_id
INNER JOIN Genres
ON Movie_Genres.genre_id = Genres.id
WHERE Movies.title='Peter Pan'

```


### Aliases
Change titles with alias
`SELECT Movies.title AS films, Reviews.review AS "Weekly Reviews"`

Change Table with alias
`SELECT m.title FROM Movies m`


```sql
# Peter Pan Example
SELECT m.title,g.name FROM Movies m
INNER JOIN Movies_Genres mg ON m.id = mg.movie_id
INNER JOIN Genres g ON mg.genre_id = g.id
WHERE m.title='Peter Pan'
```


### Outer Joins
Left outer join: Take all rows in the left, expand all of them

same Right outer join

```sql
# list multiple times when having multiple review
# still listed with no review

SELECT m.title, r.review FROM Movies m
LEFT OUTER JOIN Reviews r ON m.id=r.movie_id
ORDER BY r.id;
```

### Subqueries

Feed inner query to outer query

Syntax: 
WHERE field IN subquery
WHERE field NOT IN subquery

```sql
# Get all non-cash promotion movie sales

SELECT SUM(sales)
FROM Movies
WHERE id IN
(SELECT movie_id
FROM Promotions
WHERE category="Non-cash");

```

Mostly can achieve same result from inner join
Subquery - easy to read / innerjoin - performant


Sometimes couldn't, like when you want to find films with a duration larger than the total average duration

```sql
# ERROR: aggregate funtion not allowed in where
SELECT * FROM Movies WHERE duration > AVG(duration);

# Right solution
SELECT * FROM Movies WHERE duration > (SELECT AVG(duration) FROM Movies) ;

```


