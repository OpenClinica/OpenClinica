<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<jsp:include page="../include/admin-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		<div class="sidebar_tab_content">

		</div>

		</td>

	</tr>
	<tr id="sidebar_Instructions_closed" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>


<jsp:useBean scope='request' id='studyToRemove' class='org.akaza.openclinica.bean.managestudy.StudyBean'/>
<jsp:useBean scope='request' id='sitesToRemove' class='java.util.ArrayList'/>
<jsp:useBean scope='request' id='userRolesToRemove' class='java.util.ArrayList'/>
<jsp:useBean scope='request' id='subjectsToRemove' class='java.util.ArrayList'/>
<jsp:useBean scope='request' id='definitionsToRemove' class='java.util.ArrayList'/>

<h1><span class="title_manage"><fmt:message key="confirm_removal_of_study" bundle="${resword}"/></span></h1>

<p><fmt:message key="you_choose_to_remove_the_following_study" bundle="${restext}"/>:</p>
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr valign="top"><td class="table_header_column"><fmt:message key="name" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToRemove.name}"/>
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="brief_summary" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToRemove.summary}"/>
  </td></tr>
 </table>

 </div>
</div></div></div></div></div></div></div></div>

</div>
<br>
 <span class="table_title_Admin"><fmt:message key="sites" bundle="${resword}"/>:</span>
 <div style="width: 600px">
 <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr valign="top">
  <td class="table_header_column_top"><fmt:message key="name" bundle="${resword}"/></td>
  <td class="table_header_column_top"><fmt:message key="status" bundle="${resword}"/></td>
  </tr>
  <c:forEach var="site" items="${sitesToRemove}">
  <tr valign="top">
   <td class="table_cell"><c:out value="${site.name}"/></td>
   <td class="table_cell"><c:out value="${site.status.name}"/></td>
  </tr>
  </c:forEach>
  </table>
</div>
</div></div></div></div></div></div></div></div>

</div>
<br>
 <span class="table_title_Admin"><fmt:message key="user_and_roles" bundle="${resword}"/>:</span>
 <div style="width: 600px">
 <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
 <tr valign="top">
   <td class="table_header_column_top"><fmt:message key="user_name" bundle="${resword}"/></td>
   <td class="table_header_column_top"><fmt:message key="role" bundle="${resword}"/></td>
  </tr>
  <c:forEach var="userRole" items="${userRolesToRemove}">
  <tr valign="top">
   <td class="table_cell">
    <c:out value="${userRole.userName}"/>
   </td>
   <td class="table_cell">
    <c:out value="${userRole.role.name}"/>
   </td>
  </tr>
  </c:forEach>
  </table>
</div>
</div></div></div></div></div></div></div></div>

</div>
  <br>
 <span class="table_title_Admin"><fmt:message key="subjects" bundle="${resword}"/>:</span>
 <div style="width: 600px">
 <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
 <tr valign="top">
   <td class="table_header_column_top"><fmt:message key="study_subject_ID" bundle="${resword}"/></td>
   <td class="table_header_column_top"><fmt:message key="record_ID" bundle="${resword}"/></td>
  </tr>
  <c:forEach var="subject" items="${subjectsToRemove}">
  <tr valign="top">
   <td class="table_cell">
    <c:out value="${subject.label}"/>
   </td>
   <td class="table_cell">
    <c:out value="${subject.id}"/>
   </td>
  </tr>
  </c:forEach>
  </table>
</div>
</div></div></div></div></div></div></div></div>

</div>
<br>
 <span class="table_title_Admin"><fmt:message key="SED" bundle="${resword}"/>:</span>
 <div style="width: 600px">
 <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
 <tr valign="top">
   <td class="table_header_column_top"><fmt:message key="name" bundle="${resword}"/></td>
   <td class="table_header_column_top"><fmt:message key="description" bundle="${resword}"/></td>
  </tr>
  <c:forEach var="definition" items="${definitionsToRemove}">
   <tr>
   <td class="table_cell">
    <c:out value="${definition.name}"/>
   </td>
  <td class="table_cell">
    <c:out value="${definition.description}"/>&nbsp;
   </td>
  </tr>
  </c:forEach>
  </table>
</div>
</div></div></div></div></div></div></div></div>

</div>

  <form action="RemoveStudy?action=submit&id=<c:out value="${studyToRemove.id}"/>" method="POST">
    <input type="submit" name="submit" value="<fmt:message key="remove_study" bundle="${resword}"/>" onClick='return confirm("<fmt:message key="if_you_remove_this_study" bundle="${restext}"/>");' class="button_long">
    <input type="button" value="<fmt:message key="cancel" bundle="${resword}"/>" title="<fmt:message key="cancel" bundle="${resword}"/>" class="button_long" size="50" onclick="history.back();"/>
 </form>

<c:import url="../include/workflow.jsp">
  <c:param name="module" value="admin"/>
</c:import>

<jsp:include page="../include/footer.jsp"/>
