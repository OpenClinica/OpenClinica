<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:useBean scope='request' id='pageMessages' class='java.util.ArrayList'/>

<c:if test="${!empty pageMessages}">
<div class="alert">    
<c:forEach var="message" items="${pageMessages}">
 <c:out value="${message}" escapeXml="false"/> 
 <br><br>
</c:forEach>
</div>
</c:if>
