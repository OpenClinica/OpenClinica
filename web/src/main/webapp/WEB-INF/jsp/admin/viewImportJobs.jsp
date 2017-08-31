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

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray" border="0" align="right" hspace="10"></a>

		<fmt:message key="instructions" bundle="${resword}"/>

		<div class="sidebar_tab_content">

		</div>

		</td>

	</tr>
	<tr id="sidebar_Instructions_closed" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray" border="0" align="right" hspace="10"></a>

		<fmt:message key="instructions" bundle="${resword}"/>

		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<h1 style="margin: 0em;"><span class="title_manage"><fmt:message key="view_scheduled_inport_jobs" bundle="${resword}"/></span></h1>

<%-- <div class="homebox_bullets"><a href="CreateJobExport">Create a new Export Data Job</a></div> --%>
<div class="homebox_bullets"><a style="text-decoration: none" href="CreateJobImport"><fmt:message key="create_a_new_import_data_job" bundle="${resword}"/></a></div>
<p></p>
<c:set var="dtetmeFormat"><fmt:message key="date_time_format_string" bundle="${resformat}"/></c:set>
<jsp:useBean id="now" class="java.util.Date" />
<P><I>Note that all jobs are set to run on the server time.  The current server time is <fmt:formatDate value="${now}" pattern="${dtetmeFormat}"/>.</I></P>

<c:import url="../include/showTable.jsp">
	<c:param name="rowURL" value="showImportJobRow.jsp" />
</c:import>
<br><br>
<input type="button" onclick="confirmExit('ViewAllJobs');" name="exit" value="<fmt:message key="exit" bundle="${resword}"/>   "class="button_medium"/>   </td>

<jsp:include page="../include/footer.jsp"/>
