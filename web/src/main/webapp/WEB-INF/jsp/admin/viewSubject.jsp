<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

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

<jsp:useBean scope='request' id='subject' class='org.akaza.openclinica.bean.submit.SubjectBean'/>
<jsp:useBean scope='request' id='studySubs' class='java.util.ArrayList'/>
<h1><span class="title_manage">
<fmt:message key="view_subject_details" bundle="${resword}"/>
</span></h1>
<table border="0" cellpadding="0" cellspacing="0">
	<tr>
		<td>
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0">
 <tr valign="top" ><td class="table_header_column_top"><fmt:message key="person_ID" bundle="${resword}"/>:</td><td class="table_cell_top">
  <c:out value="${subject.uniqueIdentifier}"/>
   &nbsp;</td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="gender" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${subject.gender}"/>
  </td>
  <tr valign="top"><td class="table_header_column"><fmt:message key="date_of_birth" bundle="${resword}"/>:</td><td class="table_cell">
  <fmt:formatDate value="${subject.dateOfBirth}" pattern="${dteFormat}"/>
  </td>
  <tr valign="top"><td class="table_header_column"><fmt:message key="date_created" bundle="${resword}"/>:</td><td class="table_cell">
  <fmt:formatDate value="${subject.createdDate}" pattern="${dteFormat}"/>
  </td>
  </tr>
</table>
</div>
</div></div></div></div></div></div></div></div>
		</td>
	</tr>
</table>
<br/><br/>
<div class="table_title_Admin"> <fmt:message key="associated_study_subjects" bundle="${resword}"/></div>
<table border="0" cellpadding="0" cellspacing="0">
	<tr>
		<td>
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center" align="center">
<table border="0" cellpadding="0" cellspacing="0">
 <tr valign="top">
    <td class="table_header_row_left"><fmt:message key="study_subject_ID" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="secondary_ID" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="study_ID" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="enrollment_date" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="date_created" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="created_by" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="status" bundle="${resword}"/></td>
     <td class="table_header_row"><fmt:message key="action" bundle="${resword}"/></td>
    </tr>
  <c:forEach var="studySub" items="${studySubs}">
    <tr valign="top">
    <td class="table_cell_left"><c:out value="${studySub.label}"/></td>
    <td class="table_cell"><c:out value="${studySub.secondaryLabel}"/>&nbsp;</td>
    <td class="table_cell"><c:out value="${studySub.studyId}"/></td>
    <td class="table_cell"><fmt:formatDate value="${studySub.enrollmentDate}" pattern="${dteFormat}"/></td>
    <td class="table_cell"><fmt:formatDate value="${studySub.createdDate}" pattern="${dteFormat}"/></td>
    <td class="table_cell"><c:out value="${studySub.owner.name}"/></td>
    <td class="table_cell"><c:out value="${studySub.status.name}"/></td>
    <td class="table_cell">
     <a href="ViewStudySubject?id=<c:out value="${studySub.id}"/>&from=listSubject&module=admin"
	  onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
	  onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><img
	  name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6"></a>

    </td>
    </tr>
 </c:forEach>
</table>

</div>

</div></div></div></div></div></div></div></div>

		</td>
	</tr>
</table>
<br><p><a href="ListSubject"><fmt:message key="go_back_to_subject_list" bundle="${resword}"/></a></p>
 <c:import url="../include/workflow.jsp">
  <c:param name="module" value="admin"/>
 </c:import>
<jsp:include page="../include/footer.jsp"/>
