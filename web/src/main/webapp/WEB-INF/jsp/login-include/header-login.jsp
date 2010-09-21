<%@ page contentType="text/html; charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/logic" prefix="logic" %>
<%@ taglib uri="/bean" prefix="bean" %>
<%@ taglib uri="/template" prefix="template" %>


<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

	

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='study' class='org.akaza.openclinica.bean.managestudy.StudyBean' />
<jsp:useBean scope='session' id='userRole' class='org.akaza.openclinica.bean.login.StudyUserRoleBean' />
<jsp:useBean scope='request' id='isAdminServlet' class='java.lang.String' />

<html>

<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
 <meta http-equiv="X-UA-Compatible" content="IE=8" />

<title><fmt:message key="openclinica" bundle="${resword}"/></title>
<link rel="stylesheet" href="includes/styles.css" type="text/css">
<script language="JavaScript" src="includes/global_functions_javascript.js"></script></head>

<body bgcolor="#FFFFFF" topmargin="0" leftmargin="0" marginwidth="0" marginheight="0">

<table border="0" cellpadding="0" cellspacing="0" width="100%" height="100%">
   <tr>
	<td valign="topm">
	<table border="0" cellpadding="0" cellspacing="0" width="100%" background="images/header_BG.gif">
	   <tr>
		<td height="55" valign="bottom"><img src="images/Pheno.gif"></td>
		<td width="90%" background="images/spacer.gif" align="right" class="userinfo">

<!-- User Info goes here -->
<%-- the logic for displaying the study and role is a little complicated:
- if we are using an admin servlet, then the user should think of himself as the study administrator
- otherwise, the user should think of himself as working within a study; the study should be displayed
	- however, if the user doesn't have a role in the current study, the role will appear as "invalid"; if the user is a sys admin, we might as well show System Administrator instead.
--%>
	<c:if test="${userBean != null && userBean.id > 0}">
<fmt:message key="user" bundle="${resword}"/><b><c:out value="${userBean.name}" /></b>
&nbsp;&nbsp;&nbsp;&nbsp;
<c:choose>
	<c:when test='${(isAdminServlet == "admin") || (!userRole.role.active && userBean.sysAdmin)}'>
<b><fmt:message key="system_administrator" bundle="${resword}"/></b>
	</c:when>
	<c:otherwise>
<fmt:message key="study" bundle="${resword}"/>: <b><c:out value="${study.name}" /></b>
&nbsp;&nbsp;&nbsp;&nbsp;
<fmt:message key="role" bundle="${resword}"/>:<b><c:out value="${userRole.role.name}" /></b>
	</c:otherwise>
</c:choose>
<!-- End User Info -->

		<br>
		<table border="0" cellpadding="0" cellspacing="0" background="images/spacer.gif">
		   <tr>
			<td background="images/button_BG.gif" height="16" class="buttontext" align="right">

<!-- LOG OUT BUTTON -->

	<a class="buttontext" href="j_spring_security_logout"><fmt:message key="log_out" bundle="${resword}"/></a>

<!-- end LOG OUT BUTTON -->

			</td>
			<td valign="top"><img src="images/button_R.gif"></td>
		   </tr>
		   <tr>
			<td background="images/button_BG.gif" height="16" class="buttontext" align="right">

<!-- CHANGE PROJECT BUTTON -->

	<a class="buttontext" href="ChangeStudy"><fmt:message key="change_study" bundle="${resworkflow}"/></a>

<!-- end CHANGE PROJECT BUTTON -->

			</td>
			<td valign="top"><img src="images/button_R.gif"></td>
		   </tr>
		</table>
		</td>
</c:if>

		<td rowspan="2" width="5%" background="images/spacer.gif"><img src="images/clientlogo.gif" height="62" vspace="5" hspace="6"></td>
	   </tr>
	   <tr>
		<td colspan="2" height="17" background="images/spacer.gif">
		<table border="0" cellpadding="0" cellspacing="0" height="17" background="images/spacer.gif">
		   <tr>
<c:if test="${userBean != null && userBean.id > 0}">		   
		   
<!-- Nav with HOME tab selected -->

		<!--<td valign="top"><img src="images/tab_L_h.gif"></td>-->
	<!--	<td background="images/tab_h_BG.gif" class="tab">-->
	<!--	<a class="tab">Clinical Research Repository</a></td>-->
	<!--	<td valign="top"><img src="images/tab_R_h.gif"></td>-->
		<!--<td valign="top"><img src="images/tab_mid_h_R.gif"></td>-->


	<!-- HOME tab -->

		<td valign="top"><img src="images/tab_L_h.gif"></td>
		<td background="images/tab_h_BG.gif" class="tab">
		<a class="tabselected" href="MainMenu"><fmt:message key="home" bundle="${resworkflow}"/></a>
		</td>

	<!-- end HOME tab -->
	<td valign="top"><img src="images/tab_mid_h_R.gif"></td>

	<!-- SUBMIT DATA SETS tab -->
	
     <c:if test="${userRole != null }">
      <c:set var="roleName" value="${userRole.role.name}"/>
      <c:if test="${userRole.submitData}">
		<td background="images/tab_BG.gif" class="tab">
		<a class="tab" href="SubmitData"><fmt:message key="submit_data_sets" bundle="${resworkflow}"/></a>
		</td>

	<!-- end SUBMIT DATA SETS tab -->

		<td valign="top"><img src="images/tab_mid.gif"></td>
	
	<!-- EXTRACT DATA SETS tab -->

		<td background="images/tab_BG.gif" class="tab">
		<a class="tab" href="ExtractDatasetsMain"><fmt:message key="extract_datasets" bundle="${resworkflow}"/></a>
		</td>

	<!-- end EXTRACT DATA SETS tab -->

		<td valign="top"><img src="images/tab_mid.gif"></td>
     </c:if>
   </c:if>
	<!-- MANAGE STUDY tab -->
	 <c:if test="${userRole != null }">
      <c:set var="roleName" value="${userRole.role.name}"/>
      <c:if test="${userRole.manageStudy}">
		<td background="images/tab_BG.gif" class="tab">
		<a class="tab" href="ManageStudy"><fmt:message key="manage_study" bundle="${resworkflow}"/></a>
		</td>

	<!-- end MANAGE STUDY tab -->

		<td valign="top"><img src="images/tab_mid.gif"></td>
	 </c:if>
	</c:if>
	<!-- ADMINISTER SYSTEM tab -->
	<c:if test="${userBean.sysAdmin}">
		<td background="images/tab_BG.gif" class="tab">
		<a class="tab" href="AdminSystem"><fmt:message key="administer_system" bundle="${resworkflow}"/></a>
		</td>

	<!-- end ADMINISTER SYSTEM tab -->

		<td valign="top"><img src="images/tab_R.gif"></td>
	</c:if>
	
<!-- end Nav with HOME tab selected -->
</c:if>
		   </tr>
		</table>
		</td>
	   <tr>
	   <tr>
		<td colspan="3" bgcolor="#AFB5C5" height="16" class="nav1">

<!-- First Level Nav goes here -->



<!-- End First Level Nav -->

		</td>
	   </tr>
	   <tr>
		<td colspan="3" bgcolor="#E6E6E6" height="16" class="nav2">

<!-- Second Level Nav goes here -->



<!-- End Second Level Nav -->

		</td>
	   </tr>
	   <tr>
		<td colspan="3" height="1" bgcolor="#"><img src="images/spacer.gif" width="1" height="1"></td>
	   </tr>
	</table>
	<table border="0" cellpadding="20" cellspacing="0" width="100%">
	   <tr>
		<td valign="top">

<!-- MAIN CONTENT AREA -->


