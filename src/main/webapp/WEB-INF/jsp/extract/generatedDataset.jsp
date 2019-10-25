<%--
<%@ page contentType="text/html; charset=UTF-8" %
--%>
<%@page contentType="text/plain"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:useBean scope="request" id="generate" class="java.lang.String"/>
<c:out value="${generate}"/>
