# REST

## Restful Architecture

### REpresentational State Transfer

- It is NOT an system architecture
- It is a set of contraint in system design
- Can create system with specific roles for data, components, hyperlinks, communication protocols, and data consumers.

REST contraints

- It must be a client-server system
- It has to be stateless
    + there should be no need for the service to keep users' sessions
    + in other words, each request should be independent of others
- It has to support a caching system
    + the network infrastructure should support cache at different levels
- It has to be uniformly accessible
    + each resource must have a unique address and a valid point of access
- It has to be layered
    + it must support scalability
- It should provide code on demand
    + although this is an optional constraint, applications can be extendable at runtime by allowing the downloading of code on demand, for example, Java Applets

RESTful system can be implemented in any networking architecture available.


What it's meant for the Web to be RESTful

-  the static web is RESTful
-  traditional dynamic web applications haven't always been RESTful
    +  most dynamic applications are not stateless
    +  servers require tracking users through container sessions or client-side cookie schemes.

### Concepts

- Resource: anything that is addressable over the Web
- Representation: a temporal state of the actual data located in some storage device at the time of a request. such as an image, a text file, or an XML stream or a JSON stream.
- URI (Uniform Resource Identifier): a hyperlink to a resource, and it's the only means for clients and servers to exchange representations.
- Uniform interfaces: POST, GET, PUT, and DELETE

### Web services

What kind of problem do web services solve?

- Software companies opening their APIs to the public using
    + offer search, hosting, marketing services
- Within large enterprises to support operations
    + connect previously disjointed departments
    + such as marketing and manufacturing.

Service-Oriented Architecture

-  creating an SOA culture requires a large investment in IT and a strategic shift in operations.
