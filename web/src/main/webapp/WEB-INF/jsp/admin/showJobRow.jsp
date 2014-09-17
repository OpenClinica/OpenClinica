<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>

<jsp:useBean scope="request" id="currRow" class="org.akaza.openclinica.web.bean.TriggerRow" />
<c:set var="dtetmeFormat"><fmt:message key="date_time_format_string" bundle="${resformat}"/></c:set>

<tr valign="top" bgcolor="#F5F5F5">
	<td class="table_cell_left"><c:out value="${currRow.bean.fullName}" /></td>
	<td class="table_cell"><fmt:formatDate value="${currRow.bean.previousDate}" pattern="${dtetmeFormat}"/></td>
	<td class="table_cell">
	<c:if test="${currRow.bean.active}">
		<fmt:formatDate value="${currRow.bean.nextDate}" pattern="${dtetmeFormat}"/>
	</c:if>
	</td>
	<td class="table_cell"><c:out value="${currRow.bean.description}" /></td>
	<td class="table_cell"><c:out value="${currRow.bean.periodToRun}" /></td>
	<td class="table_cell"><c:out value="${currRow.bean.datasetName}" /></td>
	<td class="table_cell"><c:out value="${currRow.bean.studyName}" /></td>
	<!-- actions -->
	
	<td class="table_cell">
	 <table border="0" cellpadding="0" cellspacing="0">
	 <tr>
	 	<td><a href="ViewSingleJob?tname=<c:out value="${currRow.bean.fullName}" />&gname=<c:out value="${currRow.bean.groupName}" />"><img name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="2"></a></td>
		<td><a href="UpdateJobExport?tname=<c:out value="${currRow.bean.fullName}" />"><img name="bt_Edit1" src="images/bt_Edit.gif" border="0" alt="<fmt:message key="edit" bundle="${resword}"/>" title="<fmt:message key="edit" bundle="${resword}"/>" align="left" hspace="2"></a></td>
		<td>
		<c:choose>
			<c:when test="${currRow.bean.active}">
				<a href="PauseJob?tname=<c:out value="${currRow.bean.fullName}" />&gname=0" onClick='return confirm("<fmt:message key="confirm_pausing_this_job" bundle="${restext}"/>");'><img border="0" title="<fmt:message key="remove" bundle="${resword}"/>" src="images/bt_Remove.gif" alt="<fmt:message key="remove" bundle="${resword}"/>" align="left" hspace="2"/></a> 
			</c:when>
			<c:otherwise>
				<a href="PauseJob?tname=<c:out value="${currRow.bean.fullName}" />&gname=0" onClick='return confirm("<fmt:message key="confirm_restoring_this_job" bundle="${restext}"/>");'><img border="0" title="<fmt:message key="restore" bundle="${resword}"/>" src="images/bt_Restore.gif" alt="<fmt:message key="restore" bundle="${resword}"/>" align="left" hspace="2"/></a>
			</c:otherwise>
		</c:choose>
		</td>&nbsp;
		<c:if test="${userBean.sysAdmin}">
			<td>
				<a href="PauseJob?tname=<c:out value="${currRow.bean.fullName}" />&del=y&gname=0" onClick='return confirm("<fmt:message key="confirm_deleting_this_job" bundle="${restext}"/>");'><img border="0" title="<fmt:message key="delete" bundle="${resword}"/>" src="images/bt_Delete.gif" alt="<fmt:message key="delete" bundle="${resword}"/>" align="left" hspace="2"/></a>
			</td>
		</c:if>
	 </tr>
		</table>
	</td>
</tr>