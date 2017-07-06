# Hibernate Framework

ORM system, high level

Object Relational Mapping
    - binding between class instance



#### Advantages of Hibernate Framework
1.  Open source and Lightweight
2.  Fast performance: two types of cache 
3.  Database Independent query: HQL (Hibernate Query Language)
4.  Automatic table creation: no need to create tables manually.
5.  Simplifies complex join: easy To fetch data form multiple tables
6.  Provides query statistics and database status



## Hibernate Architecture
Hibernate framework uses many objects session factory, session, transaction etc. alongwith existing Java API such as JDBC (Java Database Connectivity), JTA (Java Transaction API) and JNDI (Java Naming Directory Interface).

- SessionFactory
The SessionFactory is a factory of session and client of ConnectionProvider. It holds second level cache (optional) of data. The org.hibernate.SessionFactory interface provides factory method to get the object of Session.

- Session
The session object provides an interface between the application and data stored in the database. It is a short-lived object and wraps the JDBC connection. It is factory of Transaction, Query and Criteria. It holds a first-level cache (mandatory) of data. The org.hibernate.Session interface provides methods to insert, update and delete the object, and factory methods for Transaction, Query and Criteria.

- Transaction
The transaction object specifies the atomic unit of work. It is optional. The org.hibernate.Transaction interface provides methods for transaction management.

- ConnectionProvider
It is a factory of JDBC connections. It abstracts the application from DriverManager or DataSource. It is optional.

- TransactionFactory
It is a factory of Transaction. It is optional.


#### difference between JDBC & hibernate
work for all databases, JDBC consider small syntax difference.
But hibernate is at a higher level, you use java class, you don't talk to database directly. When migrate to another database, you can just change the configuration file: `hibernate.cfg.xml`  `<orm-class-name>hbm.xml`

Support query cache for better performance. Development fast in case of Hibernate because you don’t need to write queries. In the xml file you can see all the relations between tables in case of Hibernate. Easy readability.


## How to create
##### 1. Create persistence class

•   A no-arg constructor:  It is recommended that you have a default constructor at least package visibility so that hibernate can create the instance of the Persistent class by newInstance() method.
•   Provide an identifier property (optional): It is mapped to the primary key column of the database.
•   Declare getter and setter methods (optional): The Hibernate recognizes the method by getter and setter method names by default.
•   Prefer non-final class: Hibernate uses the concept of proxies, that depends on the persistent class. The application programmer will not be able to use proxies for lazy association fetching.
•   Example - Employee.java

```java
public class Employee {    
    int eid;
    String lastName;

    public Employee(int eid, String lastName) {
        super();
        this.eid = eid;
       this.lastName = lastName;
    }
    
    public Employee() {
        
    }

    public int getEid() {return eid;}
    public void setEid(int eid) {this.eid = eid;}
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) {this.lastName = lastName;
    }

}
```


##### 2. Create mapping file for class
•   hibernate-mapping - is the root element in the mapping file.
•   class - It is the sub-element of the hibernate-mapping element. It specifies the Persistent class.
•   id - It is the sub-element of class. It specifies the primary key attribute in the class.
•   Generator- It is the sub-element of id. It is used to generate the primary key. There are many generator classes such as assigned (It is used if id is specified by the user), increment, hilo, sequence, native etc.
•   property - It is the sub-element of class that specifies the property name of the Persistent class.
Mapping file for the Emplyee class - Employee.hbm.xml


```xml
<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE hibernate-mapping PUBLIC
          "-//Hibernate/Hibernate Mapping DTD 3.0//EN"
          "http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd">

<hibernate-mapping>
    <class name="com.hbm.Employee" table="emp123">
        <meta attribute="class-description">
            This class contains the employee detail. 
        </meta>

        <id name="eid">
            <generator class="assigned"></generator>
        </id>

        <property name="firstName" length="20"></property>
        <property name="lastName" length="20"></property>
        <property name="age"></property>
        <property name="salary"></property>

    </class>

</hibernate-mapping>
```


##### 3. Create configuration file
The configuration file contains informations about the database and mapping file. Conventionally, its name should be hibernate.cfg.xml .

```xml
<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE hibernate-configuration PUBLIC
          "-//Hibernate/Hibernate Configuration DTD 3.0//EN"
          "http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd">

<!-- Generated by MyEclipse Hibernate Tools.                   -->
<hibernate-configuration>

    <session-factory>
        <property name="hbm2ddl.auto">update</property>
        <property name="dialect">org.hibernate.dialect.MySQLDialect</property>
        <property name="connection.url">jdbc:mysql://localhost:3306/bigdata</property>
        <property name="connection.username">root</property>
        <property name="connection.password">password</property>
        <property name="connection.driver_class">com.mysql.jdbc.Driver</property>
        <mapping resource="employee.hbm.xml"/>
    </session-factory>

</hibernate-configuration>
```

support various SQL dialects:
org.hibernate.dialect.OracleDialect, org.hibernate.dialect.MySQLDialect, org.hibernate.dialect.SQLServerDialect, org.hibernate.dialect.PostgreSQLDialect



##### 4. create class that retreives or stores persistence object
e.g. storing the employee object

```java

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class Test {
    public static void main(String[] args) {
        Configuration cfg=new Configuration();
        cfg.configure("hibernate.cfg.xml");//populates the data of the configuration file
        
        //creating seession factory and session
        SessionFactory factory=cfg.buildSessionFactory();
        Session session=factory.openSession();     
        System.out.println("Connected!");
        
        //creating transaction object
        Transaction t=session.beginTransaction();

        Employee e1 = new Employee();
        e1.setEid(1);
        e1.setFirstName("James");
        session.save(e1);
        
        Employee e2 = new Employee(2, "Robert");
        session.save(e2);
        System.out.println("Saved!");
        
        t.commit();
        session.close();
        
    }
}
```

##### 5. load jar files
use maven for dependencies

##### 6. run first hibernate application without IDE



## Collection Mapping
We can map collection elements of Persistent class in Hibernate. You need to declare the type of collection in  Persistent class from one of the following types.

There are many sub-elements of `<class>` elements to map the collection. They are: `<list>`, `<bag>`, `<set>` and `<map>`.

There are three sub-elements used in the list:

•   `<key>` element is used to define the foreign key in this table based on the Question class identifier.
•   `<index>` element is used to identify the type. List and Map are indexed collection.
•   `<element>` is used to define the element of the collection.

This is the  mapping of collection if collection stores string objects. But if collection stores entity reference (another class objects), we need to define `<one-to-many>` or `<many-to-many>` element.

##### Key

The key element is used to define the foreign key in the joined table based on the original identity. The foreign key element is nullable by default. So for non-nullable foreign key, we need to specify not-null attribute such as:

The attributes of the key element are column, on-delete, property-ref, not-null, update and unique.
```
<key
column="columnname"
on-delete="noaction|cascade"
not-null="true|false"
property-ref="propertyName"
update="true|false"
unique="true|false"
/>
```


## Hibernate Caching
- First-level cache: Session cache, mandatory, all requests must pass. When issued multiple updates to an object, Hibernate will delay the update as long as possible to reduce the number of update SQL statements issued. 

- Second-level cache: Optional cache, consult first-level cache before locating objects in the second-level cache. Mainly responsible for caching objects across sessions. Could use any third-party cache.

- Query-level cache: Optional feature, requires two additional physical cache regions that hold the cached query results and the timestamps when a table was last updated. This is only useful for queries that are run frequently with the same parameters.

Not all classes benefit from caching, so it's important to be able to disable the second-level cache

##### Concurrency strategies

A concurrency strategy is a mediator which responsible for storing items of data in the cache and retrieving them from the cache. If you are going to enable a second-level cache, you will have to decide, for each persistent class and collection, which cache concurrency strategy to use.

•   Read-only: A concurrency strategy suitable for data which never changes. Use it for reference data only.
•   Nonstrict-read-write: This strategy makes no guarantee of consistency between the cache and the database. Use this strategy if data hardly ever changes and a small likelihood of stale data is not of critical concern.
•   Read-write: Again use this strategy for read-mostly data where it is critical to prevent stale data in concurrent transactions,in the rare case of an update.
•   Transactional: Use this strategy for read-mostly data where it is critical to prevent stale data in concurrent transactions,in the rare case of an update.

If we are going to use second-level caching for our Employee class, let us add the mapping element required to tell Hibernate to cache Employee instances using read-write strategy.

add a line in `Employee.xml`: `<cache usage="read-write"/>`

##### Pick a Cache provider:

Hibernate forces you to choose a single cache provider for the whole application.(Concurrency strategies supported: 1111)

|| EHCache | OSCache | SwarmCache |JBossCache |
|-------------------------|---------|-----------------------------------|------------------------|--------------------------------------------------|
|Cache in Memory or disk |X|X| | |
| Clustered Caching |X| |X|X|
| Hibernate query cache|X|X| |X|
| Other Support |  | a rich set of expiration policies | clustered invalidation | replication, invalidation, asynchronous, locking |
||
| Read-Only| X  | X  | X | X |
| Unstrict-Read-Write | X | X | X | |
| Read-Write | X | X | | |     
| Transactional | | | | X |


Add a line in `hibernate.cfg.xml `
```xml
   <property name="hibernate.cache.provider_class">
      org.hibernate.cache.EhCacheProvider
   </property>
```

Set up the configuration file of ehcache.xml
```xml
<diskStore path="java.io.tmpdir"/>
<defaultCache
maxElementsInMemory="1000"
eternal="false"
timeToIdleSeconds="120"
timeToLiveSeconds="120"
overflowToDisk="true"
/>

<cache name="Employee"
maxElementsInMemory="500"
eternal="true"
timeToIdleSeconds="0"
timeToLiveSeconds="0"
overflowToDisk="false"
/>
```
