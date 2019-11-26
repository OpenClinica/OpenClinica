<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<table border="0" cellpadding="0" cellspacing="0" <%--style="position: relative; top: -21px;"--%>>
 
  <tr>
	<td valign="top">
	

<table border="0" cellpadding="0" cellspacing="0" width="160">

<!-- Side alert, onlu show the content after user logs in -->

 <c:if test="${userBean != null && userBean.id>0}">	 
  <c:choose>
  
    <c:when test="${!empty pageMessages || param.message == 'authentication_failed'|| param.alertmessage !=null}">

	<tr id="sidebar_Alerts_open" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Alerts_open'); leftnavExpand('sidebar_Alerts_closed');"><span class="icon icon-caret-down gray" border="0" align="right" hspace="10"></span></a>

		<fmt:message key="alerts_messages" bundle="${resword}"/>

		<div class="sidebar_tab_content">

			<i>	
			<c:choose>
			<c:when test="${userBean!= null && userBean.id>0}">             
            <jsp:include page="../include/showSideMessage.jsp" />
            </c:when>
            <c:otherwise>             
            <fmt:message key="have_logged_out_application" bundle="${resword}"/><a href="MainMenu"><fmt:message key="login_page" bundle="${resword}"/></a> <fmt:message key="in_order_to_re_enter_openclinica" bundle="${resword}"/>         
		
            </c:otherwise>
            </c:choose>
            </i>

			<br>

			<!--<a href="#">View JsonLog</a>-->

		</div>

		</td>
	</tr>
	<tr id="sidebar_Alerts_closed" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Alerts_open'); leftnavExpand('sidebar_Alerts_closed');"><span class="icon icon-caret-right gray" border="0" align="right" hspace="10"></span></a>

		<fmt:message key="alerts_messages" bundle="${resword}"/>

		</td>
	</tr>
    </c:when>
    <c:otherwise>
       <tr id="sidebar_Alerts_open" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Alerts_open'); leftnavExpand('sidebar_Alerts_closed');"><span class="icon icon-caret-down gray"></span></a>

		<fmt:message key="alerts_messages" bundle="${resword}"/>

		<div class="sidebar_tab_content">

			<i>	
			<c:choose>
			<c:when test="${userBean!= null && userBean.id>0}">             
            <jsp:include page="../include/showSideMessage.jsp" />
            </c:when>
            <c:otherwise>             
            <fmt:message key="have_logged_out_application" bundle="${resword}"/><a href="MainMenu"><fmt:message key="login_page" bundle="${resword}"/></a> <fmt:message key="in_order_to_re_enter_openclinica" bundle="${resword}"/>          
		
            </c:otherwise>
            </c:choose>
            </i>

		</div>

		</td>
	</tr>
	<tr id="sidebar_Alerts_closed" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Alerts_open'); leftnavExpand('sidebar_Alerts_closed');"><span class="icon icon-caret-right gray"></span></a>

		<fmt:message key="alerts_messages" bundle="${resword}"/>

		</td>
	</tr>
    </c:otherwise>	
  </c:choose>	
</c:if>
	
