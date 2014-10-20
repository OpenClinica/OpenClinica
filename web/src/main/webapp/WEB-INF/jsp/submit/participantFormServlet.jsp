<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
  <body>
    <p>Enketo URL = <a href="<c:out value="${formURL}"/>"><c:out value="${formURL}"/></a></p>
  </body>
</html>
