# JavaServerPages

### Overview
JSP is still very much in use, and worth learning.
But Look for a good web application framework to give structure to your application, always resist the temptation to write business logic in your JSPs just because you can.


insert java into html pages using JSP tags `<% tags %>`

add dynamic content to html, more convenient way to write servlets
similar to servlet life cycle, with extra step to compile jsp into servlet:
`jspInit()`->`jspService()`(Request, Response)->`jspDestroy()`

Usage:
- retrieving information from a database
- registering user preferences
- accessing JavaBeans components
- passing control between pages 
- sharing information between requests, pages etc.

### Syntax

<% code fragment %>

```html
// Declaration
<%! declaration; [ declaration; ]+ ... %>
<%! Circle a = new Circle(2.0); %> 

// Expression
<%= expression %>
Today's date: <%= (new java.util.Date()).toLocaleString()%>

// Comment
<%-- This is JSP comment --%>

// Directives
<%@ directive attribute="value" %>

// Decision-Making Statements

<% if (day == 1 | day == 7) { %>
      <p> Today is weekend</p>
<% } else { %>
      <p> Today is not weekend</p>
<% } %>

<%for ( fontSize = 1; fontSize <= 3; fontSize++){ %>
   <font color="green" size="<%= fontSize %>">JSP Tutorial</font><br />
<%}%>

// Output
<% out.println("It\'s Friday."); %>

```


```
<%! int day = 3; %> 
<html> 
<head><title>SWITCH...CASE Example</title></head> 
<body>
<% 
switch(day) {
```


### Directives
JSP directives provide directions and instructions to the container, telling it how to handle certain aspects of JSP processing.

three types of directive tag:

1. page
Page-dependent attributes/scripting language/error page/buffering requirements.
<%@ page attribute="value" %>

2. include
Includes a file during the translation phase.
<%@ include file="relative url" >

3. taglib
Declares a tag library, containing custom actions, used in the page
<%@ taglib uri="uri" prefix="prefixOfTag" >


### actions

<jsp:action_name attribute="value" />

##### include
```html
//include another jsp
<jsp:include page="date.jsp" flush="true" />
```

##### use Beans
first searches for an existing object utilizing the id and scope variables. If an object is not found, it then tries to create the specified object.
<jsp:useBean id="name" class="package.class" />

Once a bean class is loaded, you can use jsp:setProperty and jsp:getProperty actions to modify and retrieve bean properties.

```html

<jsp:useBean id="test" class="action.TestBean" />
 
<jsp:setProperty name="test" 
                    property="message" 
                    value="Hello JSP..." />
 
<p>Got message....</p>
 
<jsp:getProperty name="test" property="message" />
 
```

##### forward
terminates the action of the current page and forwards the request to another resource such as a static page, another JSP page, or a Java Servlet.

<jsp:forward page="date.jsp" />

### Implicit Objects
- request: HttpServletRequest
- response: HttpServletResponse
- out: PrintWriter 
- session: HttpSession 
- application: ServletContext 
- config:  ServletConfig 
- pageContext: JspWriters
- page: this
- Exception: Exception




### JSP login example
JSP
```html
<form action="login.jsp" method="post">
    <input type="text" name="uname" />
    <input type="password" name="pword" />
    <input type="submit" value="OK" />
</form>
```

login.jsp
```
<jsp:useBean id="obj" class="com.demo.LoginService" />
<body>
<%
    String uname = request.getParameter("uname");
    String pword = request.getParameter("pword");
    
    boolean flag = obj.login(uname, pword);
    
    if(flag)
        response.sendRedirect("success.jsp");
    else
        response.sendRedirect("error.jsp"); 
%>
</body>
```


### jsp sessions

visit count
```html
<body>
<%
    Integer count = (Integer) session.getAttribute("countVisits");
    if(count == null)
        count = new Integer(1);
    else
        count++;
    session.setAttribute("countVisits", count); 
%>
    <h2>You have visited here <%= count %> Times...</h2>
</body>

```


shopping cart example
```java
<%@ page import="java.util.ArrayList" %> 

<body>
<%
    String newItems[] = request.getParameterValues("flashdrives");
    ArrayList<String> sessionItems = (ArrayList<String>) session.getAttribute("flashCart");
    
    if(sessionItems == null) {
        sessionItems = new ArrayList<String>();
    }
    for(int i=0;i<newItems.length;i++) {
        sessionItems.add(newItems[i]);
    }
    session.setAttribute("flashCart",sessionItems);
%>
    
    You have the following items in the cart:<ul>
        <% for(String str : sessionItems) {%>
            <li><%=str %></li>
        <% } %>
    </ul><a href="products.html">Back to products</a>
</body>
```

```html
<form method="post" action="cart.jsp">
<select multiple size="6" name="flashdrives">
    <option>4 GB Kingston
    <option>8 GB Kingston
</select>
<input type="submit" value="ADD" />
</form>
```



### JSP taglib
```
<%@ taglib uri="WEB-INF/e.tld" prefix="e" %>
<e:employee firstName="a" salary="3000"/>
```

```tld
<taglib>

  <tlib-version>1.0</tlib-version>
  <jsp-version>1.2</jsp-version>
  <short-name>simple</short-name>
  <uri>http://tomcat.apache.org/example-taglib</uri>

<tag>
<name>employee</name>
<tag-class>com.marlabs.tags.EmployeeHandler</tag-class>
<attribute>
   <name>salary</name>
   <required>false</required>
</attribute>
<attribute>
   <name>firstName</name>
   <required>true</required>
</attribute>
</tag>

</taglib>
```



extend tagsupport class
```java

public class EmployeeHandler extends TagSupport {
    
    Connection con;
    PreparedStatement ps;
    ResultSet rs;
    
    private String firstName;
    private int salary;
    
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary= salary;
    }

    public EmployeeHandler() {
        try{
            Class.forName("oracle.jdbc.driver.OracleDriver");// 
            con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE","username","pw");
        } catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public int doStartTag() throws JspException {
        JspWriter out=pageContext.getOut();
        try{
            if(firstName != null && salary != 0) {
                ps = con.prepareStatement("select * from employee where fname like ? and salary > ?");
                ps.setInt(2, salary);
            }
            else {
                ps = con.prepareStatement("select * from employee where fname like ?");
            }
            ps.setString(1, "%" + firstName + "%");
            rs = ps.executeQuery();
            out.println("<table border='1'>");
            if(!rs.next()) {
                out.println("<tr>");
                out.println("<td>No data for this criteria</td>");
                out.println("</tr>");
            }
            else {
                do {
                    out.println("<tr>");
                    out.println("<td>ID</td>");
                    out.println("<td>" + rs.getString(1) + "</td>");
                    out.println("<td>First Name</td>");
                    out.println("<td>" + rs.getString(2) + "</td>");
                    out.println("<td>Last Name</td>");
                    out.println("<td>" + rs.getString(3) + "</td>");
                    out.println("<td>Salary</td>");
                    out.println("<td>" + rs.getString(5) + "</td>");
                    out.println("</tr>");
                } while(rs.next());
            }
            out.println("</table>");
            rs.close();
        }catch(Exception e){System.out.println(e);}
        return SKIP_BODY;
    }

}

```
