<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:useBean scope='request' id='pageMessages' class='java.util.ArrayList'/>

<c:if test="${!empty pageMessages}">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="alertbox_center">    
<c:forEach var="message" items="${pageMessages}">

 <c:out value="${message}" escapeXml="false"/> 
</c:forEach>
<br><br></div>

</div></div></div></div></div></div></div></div>
</c:if>
