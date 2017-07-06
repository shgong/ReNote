

# 1.Overview

# 2.HDFS

Hadoop Book Chapter 3

HDFS Structure
https://hadoop.apache.org/docs/r1.2.1/hdfs_design.html

HDFS User Guide
https://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-hdfs/HdfsUserGuide.html

# 3. MapReduce

second namenode



# Hadoop2.0 / Yarn

Advantage

- horizontal scalability (Many NameNode)
- Highly Available
- Processing Control with Resource Manager, NodeManager and App Master
- Address resource pressure on the JobTracker
- Ability to run framework other than MapReduce, such as MPI 

Problem MR1.0
- As cluster size grow and reach 4000 nodes
- cascading failures: serious deterioration of overall cluster
multi-tennancy:MRv1 dedicate nodes to hadoop and cannot be repurposed


YARN: Yet Another Resource Negotiator

- NameNode (manager)
    + Active Namenode
    + Secondary Namenode
    + Standby Namenode

- Resource Manager(manager)
    + Application management
    + Scheduling: like Fair Schedule
    + Security
- NodeManager(worker)
    + process instantiates user code
    + executes map and reduce tasks
    + provides per-application resource like memory cpu
- Container
    + resource in a node for a job
    + as a unit of processing
- ApplicationMaster
    + Run as Container 0
    + negotiations map and reduce Containers from the Scheduler
    + tracking their status and monitoring progress
    + manage individual jobs
- Job History Server
    + Job history preserved in HDFS
- Client & Admin Utilities
    + CLI
    + REST
    + JMX


Capacity Schedule: 
- fair schedule
    Fair scheduling is a method of assigning resources to jobs such that all jobs get, on average, an equal share of resources over time. 
- FIFO Schedule
    Tasks are scheduled on the order of their job submission 
    all tasks from job earlier are guaranteed to finish before all tasks from 
job later, regardless of the priority








