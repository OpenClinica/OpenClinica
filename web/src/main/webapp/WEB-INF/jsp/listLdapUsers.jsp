<%@page import="java.net.URLEncoder"%>
<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.page_messages" var="resmessages"/>

<link rel="stylesheet" href="../includes/styles.css" type="text/css">
<style>
<!--
body {
    background-color: #FFFFFF;
}
-->
</style>
<html>
<head>
</head>
<body>
<script type="text/JavaScript" language="JavaScript" src="../includes/jmesa/jquery-1.3.2.min.js"></script>
<h1><span class="title_manage">
LDAP Users
</span></h1>

<span>
<form action="" method="get">
Filter: <input type="text" name="filter" value="<c:out value="${param.filter}"/>"/> <input type="submit"/>
</form>
</span>

<c:if test="${not empty param.filter }">
<table cellspacing="10" cellpadding="0" border="0">
<tr>
<td>
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="tablebox_center">
<table cellspacing="0" cellpadding="0" border="0">
    <thead>
        <tr>
            <th class="table_header_row_left">Username&nbsp;</th>
            <th class="table_header_row">Full name&nbsp;</th>
            <th class="table_header_row">Actions&nbsp;</th>
        </tr>
    </thead>
    <tbody>
    <c:forEach items="${memberList}" var="m">
        <tr>
            <td class="table_cell_left"><c:out value="${m.username}"/></td>
            <td class="table_cell"><c:out value="${m.firstName} ${m.lastName}"/></td>
            <td class="table_cell">
            <a href="<c:url value="/pages/selectLdapUser"><c:param name="dn" value="${m.distinguishedName}"/></c:url>" target="_parent"><img src="../images/create_new.gif" border="0"/></a></td>
        </tr>
    </c:forEach>
    <c:if test="${empty memberList}">
        <tr>
            <td colspan="3">No results found</td>
        </tr>
    </c:if>
    </tbody>
</table>
</div>
</div></div></div></div></div></div></div></div>
</td>
</tr>
</table>
</c:if>


<a href="<c:url value="/pages/selectLdapUser"/>" target="_parent">CLOSE</a>
</body>
</html>
