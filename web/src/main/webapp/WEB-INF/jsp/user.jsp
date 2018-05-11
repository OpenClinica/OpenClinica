<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<html>
  <head><title><fmt:message key="user_page" bundle="${resword}"/></title></head>
  <body>
  <fmt:message key="welcome" bundle="${resword}"/>
  <c:forEach var="user" varStatus="status" items="${stringList}">
      ${user}<c:if test="${! status.last}">,</c:if>&nbsp;
  </c:forEach>
</body>
</html>