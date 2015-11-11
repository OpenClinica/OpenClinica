<%@ page contentType="text/html; charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.page_messages" var="resmessages"/>

<jsp:useBean scope='request' id='pageMessages' class='java.util.ArrayList'/>
<%--<jsp:useBean scope='request' id='message' class='java.lang.String'/>--%>

<c:if test="${!empty pageMessages}">
<div class="alert">    
<c:forEach var="message" items="${pageMessages}">
 <c:out value="${message}" escapeXml="false"/> 
 <br><br>
</c:forEach>
</div>
</c:if>


<c:if test="${empty pageMessages && param.alertmessage!=null  }">
<div class="alert"> 
 <c:out value="${param.alertmessage}" escapeXml="false"/> 
 <br><br>
</div>
</c:if>

<c:if test="${param.message == 'authentication_failed'}">
<div class="alert">
    <fmt:message key="no_have_correct_privilege_current_study" bundle="${resmessages}"/>
    <fmt:message key="change_study_contact_sysadmin" bundle="${resmessages}"/>
 <br><br>
</div>
</c:if>

