<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>

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

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>

<jsp:useBean scope="request" id="triggerBean" class="org.akaza.openclinica.bean.admin.TriggerBean" />

<jsp:useBean scope="request" id="groupName" class="java.lang.String" />

<c:set var="dtetmeFormat"><fmt:message key="date_time_format_string" bundle="${resformat}"/></c:set>
<jsp:useBean id="now" class="java.util.Date" />

<h1><span class="title_manage">View Job: <c:out value="${triggerBean.fullName}" /></span></h1>

<P><I><fmt:message key="note_that_job_is_set" bundle="${resword}"/> <fmt:formatDate value="${now}" pattern="${dtetmeFormat}"/>.</I></P>
<%-- set up table here --%>
<div style="width: 400px">

<!-- These DIVs define shaded box borders -->

	<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

		<div class="tablebox_center">


		<!-- Table Contents -->

<table border="0" cellpadding="0" cellspacing="0" width="100%">
	<tr>
		<td class="table_header_column_top"><fmt:message key="job_name" bundle="${resword}"/>:</td>
		<td class="table_cell_top"><c:out value="${triggerBean.fullName}" />&nbsp;</td>
	</tr>
	<tr>
		<td class="table_header_column"><fmt:message key="last_time_fired" bundle="${resword}"/>:</td>
		<td class="table_cell"><fmt:formatDate value="${triggerBean.previousDate}" pattern="${dtetmeFormat}"/>&nbsp;</td>
	</tr>
	<tr>
		<td class="table_header_column"><fmt:message key="next_time_to_fire" bundle="${resword}"/>:</td>
		<td class="table_cell">
		<c:if test="${triggerBean.active}">
			<fmt:formatDate value="${triggerBean.nextDate}" pattern="${dtetmeFormat}"/>&nbsp;
		</c:if>
		</td>
	</tr>
	<tr>
		<td class="table_header_column"><fmt:message key="description" bundle="${resword}"/>:</td>
		<td class="table_cell"><c:out value="${triggerBean.description}" />&nbsp;</td>
	</tr>
	<c:if test="${groupName=='XsltTriggersExportJobs'}">
	<tr>
		<td class="table_header_column"><fmt:message key="dataset" bundle="${resword}"/>:</td>
		<td class="table_cell"><c:out value="${triggerBean.dataset.name}" />&nbsp;</td>
	</tr>
	<tr>
		<td class="table_header_column"><fmt:message key="period_to_run" bundle="${resword}"/>:</td>
		<td class="table_cell"><c:out value="${triggerBean.periodToRun}" />&nbsp;</td>
	</tr>
	</c:if>
	<tr>
		<td class="table_header_column"><fmt:message key="contact_email" bundle="${resword}"/>:</td>
		<td class="table_cell"><c:out value="${triggerBean.contactEmail}" />&nbsp;</td>
	</tr>
	<c:if test="${groupName=='XsltTriggersExportJobs'}">
	<tr>
		<td class="table_header_column"><fmt:message key="file_formats" bundle="${resword}"/>:</td>
		<td class="table_cell">
            <c:out value="${triggerBean.exportFormat}"/>
		</td>
	</tr>
	</c:if>

	</table>
	</div>

	</div></div></div></div></div></div></div></div>

	</div>

	<c:import url="../include/showTable.jsp"><c:param name="rowURL" value="showAuditEventJobRow.jsp" /></c:import>

<table border="0" cellpadding="0" cellspacing="0">
  <tr>
<c:choose>
<c:when test="${groupName=='XsltTriggersExportJobs'}">
   <td>
   <form action='UpdateJobExport?tname=<c:out value="${triggerBean.fullName}" />' method="POST">
    <input type="submit" name="submit" value="<fmt:message key="edit_this_job" bundle="${resword}"/>" class="button_long">
   </form>
   </td>
   <td>
   <form action='CreateJobExport' method="POST">
    <input type="submit" name="submit" value="<fmt:message key="create_a_new_export_data_job" bundle="${resword}"/>" class="button_long">
   </form>
   </td>
<td>
<input type="button" onclick="confirmExit('ViewJob');" name="exit" value="<fmt:message key="exit" bundle="${resword}"/>   "class="button_medium"/>   </td>
   <td>
   </td>
</c:when>
<c:otherwise>
	<td>
   <form action='UpdateJobImport?tname=<c:out value="${triggerBean.fullName}" />' method="POST">
    <input type="submit" name="submit" value="<fmt:message key="edit_this_job" bundle="${resword}"/>" class="button_long">
   </form>
   </td>
   <td>
   <form action='CreateJobImport' method="POST">
    <input type="submit" name="submit" value="<fmt:message key="create_a_new_import_data_job" bundle="${resword}"/>" class="button_long">
   </form>
   </td>
<td>
<input type="button" onclick="confirmExit('ViewImportJob');" name="exit" value="<fmt:message key="exit" bundle="${resword}"/>   "class="button_medium"/>   </td>
   <td>
   </td>
</c:otherwise>
</c:choose>
   
  </tr>
</table>


<jsp:include page="../include/footer.jsp"/>
