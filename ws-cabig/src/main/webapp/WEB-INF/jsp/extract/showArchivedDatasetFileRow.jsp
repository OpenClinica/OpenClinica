<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>
<c:set var="dtetmeFormat"><fmt:message key="date_time_format_string" bundle="${resformat}"/></c:set>

<jsp:useBean scope="request" id="currRow" class="org.akaza.openclinica.web.bean.ArchivedDatasetFileRow" />

<tr>
	<td class="table_cell_left">
		<a target="_new" href="AccessFile?fileId=<c:out value="${currRow.bean.id}"/>">
			<c:out value="${currRow.bean.name}" />
		</a>
	</td>
	<td class="table_cell"><c:out value="${currRow.bean.runTime}" /></td>
	<td class="table_cell"><c:out value="${currRow.bean.fileSize}" /></td>
	<td class="table_cell"><fmt:formatDate value="${currRow.bean.dateCreated}"/></td>
	<td class="table_cell"><c:out value="${currRow.bean.owner.name}" /></td>
</tr>
