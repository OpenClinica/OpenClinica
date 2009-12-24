<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
  <head><title>User page</title></head>
  <body>
  Welcome
  <c:forEach var="user" varStatus="status" items="${stringList}">
      ${user}<c:if test="${! status.last}">,</c:if>&nbsp;
  </c:forEach>
</body>
</html>