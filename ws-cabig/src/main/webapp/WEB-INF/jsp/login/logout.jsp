<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<link rel="stylesheet" href="includes/styles.css" type="text/css">
<html>
<body onload="javascript:window.location.href='MainMenu'">
<center><h1><fmt:message key="logging_out" bundle="${resword}"/></h1></center>
</body>
</html>
