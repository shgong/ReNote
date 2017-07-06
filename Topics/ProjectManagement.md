
# Project Management

## 1. Classic Project Structure

- The Systems Development Life Cycle (SDLC)
- Five Broad Stage Structure: Inception / Design / Developent / QA / Production


### 1.1 INCEPTION

#### 1.1.1 Vision

A single page document by `CEO` / `CIO`

-  I want to improve the efficiency of manufacture
-  I want to understand where my customer living
-  I want to cut budget for 20 percent

#### 1.1.2 Business Requirement Document (BRD)

Detailed version of vision by `Business Analyst`

This is a document used by non-technical business readers,
they understand the document and signed it.

- What is the executives talking about
- How to target the customer
- Want to get customer data
- Need a data warehouse

Examine and Update BRD every 6 months, month or even week

- Requirement changes, update as early as possible 

#### 1.1.3 Function Specification Document (FSD)

A more detailed document for both business and technical by `Business Analyst`

- need to know specific use cases
- need to have marketing tools
- need to use particular software
- all these data need to be aggregated

A Estimated Budget Plan and A High level Project Plan by `Project Manager`

- It will cost 1 million dollars
- It will take 8 months to get the things done

### 1.2 DESIGN 

#### 1.2.1 Solution Overview Document (SOD)

High level technical document, by `Solution Architect` / `Data Architect` / `Info System Architect` / `Security Architect`

- Data Model and Logic
- Network, Web Server, Layout
- Need a Cassandra / Redis Database
- Can do ETL for some use case
- Logic Design and Physical Design

#### 1.2.2 Non Functional Requirement (NFR)

- Functional requirements are usually in the form of "system shall do somerequirement", while non-functional requirements are in the form of "system shall be some requirement"
- Describe system attributes and ilities
- attributes: time for report, number of users, how much data to store
- ilities: security, reliability, maintainability, scalability, and usability

### 1.3 DEVELOPMENT & QA

- Now Project Manager back to stage. 

- 3 Main Teams / Managers

#### 1.3.1 Development Team

- Structure
    + Head: Delivery Manager
    + Team Lead
    + each Team Lead with developer group

- Preparation
    + figure out the resource requirements
        * Need a ETL Team
        * Need 5 Java Dev, 2 Hadoop Dev
    + Deliver a Development Plan

- Development
    + Doing Unit Test before QA stage
    + then give to release manager

#### 1.3.2 QA Team

- Structure
    + Head: QA Manager
    + SDET, QA Developer

- Preparation 
    + Look at Non Functional Requirement
        * If need to test something, what framework is needed
        * deliver to Environment Manager
    + Look at Funtion Specification Docuemnt
        * Figure out test cases
    + Figure out the resource requirements
        * Need Security Test
        * Hadoop Test
        * ETL Test
    + Deliver a QA Plan and UAT Plan

- Test Scripts
    + Pass/Fail Result
    + Feedback to Delivery Manager or Team Lead

#### 1.3.3 Environment Team

- Structure
    + Head: Environment Manager
    + Unix Lead: Source Control, Unix System
    + Release manager: Take development to QA process
- Preparation 
    + understand what environment do we need , similar role to Info System Architect, look at SOD and discuss with other managers
    + figure out the resource requirements
        * QA Servers
        * Development Cluster
        * Performance Test

#### 1.3.4 User Acceptance Testing (UAT)

Test with real users to see whether satisfy BRD requirement

- last step before production
- Problem, useless requirement, feature request
    + all of them delays production, cost money
- when finished, release manager deliver to production

### 1.4 Production

start production


## 2. Agile

- Iterations
    + Start with a small split
    + Fast interation tools, eg. Hive
    + see the feedback and report
    + Increment by Increment
- After a certain stage
    + know what business user looking for
    + find some requirement that do not change frequently
    + move to stable complex tools, eg. HDFS & MR System

- Advantage of Agile
    + Good when requirement changes frequently
    + UAT possibly happens after 6 months after BRD
    + People want to see things quickly, and feedback is valuable

- Problems of Agile
    + Agile is totally dependent on the people making decision.
        * understand the landscape, the vision
        * contact users
        * split tasks
    + If you do not have a clear BRD over that, Agile might not be good.
    + Agile have very little time to do global decisions
        * It is hard to make system with good design choices

## 3. Others

### 3.1 How to deploy your local program to server

- Developer: only push code to source control
- Release manager will bring it to server

### 3.2 Performance Issues

- Solution architect is responsible for that
- write performance test in development stage
- use application performance monitor tools
    + collect information from JVM
    + heap size, process

### 3.3 Hadoop Administrator

A typical environment manager job

- Set up cluster
- How many cluster/node do you need

Maybe start with 5 cluster, and add to 8


