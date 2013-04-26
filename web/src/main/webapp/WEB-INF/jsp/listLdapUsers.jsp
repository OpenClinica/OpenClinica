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
<link rel="stylesheet" href="../includes/jmesa/jmesa.css" type="text/css">
<script type="text/JavaScript" language="JavaScript" src="../includes/jmesa/jquery.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="../includes/jmesa/jquery.jmesa.js"></script>
<script type="text/JavaScript" language="JavaScript" src="../includes/jmesa/jmesa.js"></script>
  <script type="text/javascript" language="JavaScript" src="../includes/jmesa/jquery-migrate-1.1.1.js"></script>

<table cellpadding="10">
<tr>
<td>

<h1><span class="title_manage">
<fmt:message key="listLdapUsers.header.title" bundle="${resword}"/>
</span></h1>

<span>
<form action="" method="get">
<span class="aka_font_general"><fmt:message key="listLdapUsers.filter.label" bundle="${resword}"/></span>
<input type="text" name="filter" value="<c:out value="${param.filter}"/>"/> 
<input type="submit" class="button_search" value="<fmt:message key="listLdapUsers.filter.submit" bundle="${resword}"/>"/>
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
            <td class="table_header_row_left">
            <fmt:message key="listLdapUsers.list.table.header.username.label" bundle="${resword}"/>&nbsp;</td>
            <td class="table_header_row">
            <fmt:message key="listLdapUsers.list.table.header.firstName.label" bundle="${resword}"/>&nbsp;</td>
            <td class="table_header_row">
            <fmt:message key="listLdapUsers.list.table.header.lastName.label" bundle="${resword}"/>&nbsp;</td>
            <td class="table_header_row">
            <fmt:message key="listLdapUsers.list.table.header.email.label" bundle="${resword}"/>&nbsp;</td>
            <td class="table_header_row">
            <fmt:message key="listLdapUsers.list.table.header.actions.label" bundle="${resword}"/>&nbsp;</td>
        </tr>
    </thead>
    <tbody>
    <c:forEach items="${memberList}" var="m">
        <tr>
            <td class="table_cell_left"><c:out value="${m.username}"/>&nbsp;</td>
            <td class="table_cell"><c:out value="${m.firstName}"/>&nbsp;</td>
            <td class="table_cell"><c:out value="${m.lastName}"/>&nbsp;</td>
            <td class="table_cell"><c:out value="${m.email}"/>&nbsp;</td>
            <td class="table_cell">
            <a href="<c:url value="/pages/selectLdapUser"><c:param name="dn" value="${m.distinguishedName}"/></c:url>" target="_parent">
            <img src="../images/create_new.gif" 
            alt="<fmt:message key="listLdapUsers.list.table.selectUser.tooltip" bundle="${resword}"/>" 
            title="<fmt:message key="listLdapUsers.list.table.selectUser.tooltip" bundle="${resword}"/>" border="0"/></a></td>
        </tr>
    </c:forEach>
    <c:if test="${empty memberList}">
        <tr>
            <td colspan="3"><fmt:message key="listLdapUsers.list.table.emptyResults.label" bundle="${resword}"/></td>
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

<form action="<c:url value="/pages/selectLdapUser"/>" method="get" target="_parent">
<input type="submit" class="button_medium" value="<fmt:message key="listLdapUsers.close.label" bundle="${resword}"/>">
</form>

</td>
</tr>
</table>

</body>
</html>
