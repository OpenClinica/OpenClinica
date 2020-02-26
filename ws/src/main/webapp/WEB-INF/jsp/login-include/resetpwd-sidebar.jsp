<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page import="org.akaza.openclinica.dao.core.SQLInitServlet"%>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/> 
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>

<!-- End Header Table -->
<table border="0" cellpadding=0" cellspacing="0">
    <tr>
        <td class="sidebar" valign="top">

<!-- Sidebar Contents -->

    <br><br>
    <a href="MainMenu">&nbsp;<fmt:message key="home" bundle="${resworkflow}"/></a>
    <br><br>
    <a href="RequestAccount">&nbsp;<fmt:message key="request_an_account" bundle="${resword}"/></a>
    <br><br>
    <a href="RequestPassword">&nbsp;<fmt:message key="forgot_password" bundle="${resword}"/></a>
    


<!-- End Sidebar Contents -->

                <br><img src="images/spacer.gif" width="120" height="1">

                </td>
                <td class="content" valign="top">