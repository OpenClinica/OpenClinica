<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>
<c:set var="dtetmeFormat"><fmt:message key="date_time_format_string" bundle="${resformat}"/></c:set>

<jsp:useBean scope="request" id="currRow" class="org.akaza.openclinica.web.bean.ArchivedDatasetFileRow" />
<tr>
	<td class="table_cell_left"><c:out value="${currRow.bean.name}" /></td>
	<td class="table_cell"><c:out value="${currRow.bean.runTime}" /></td>
	<td class="table_cell"><c:out value="${currRow.bean.fileSize}" /></td>
	<td class="table_cell"><fmt:formatDate value="${currRow.bean.dateCreated}"/></td>
	<td class="table_cell"><c:out value="${currRow.bean.owner.name}" /></td>
    <td class="table_cell">
        <a target="_new" href="AccessFile?fileId=<c:out value="${currRow.bean.id}"/>">
            <img name="bt_Download1" src="images/bt_Download.gif" border="0" align="left" hspace="6"
                 alt="<fmt:message key="download" bundle="${resword}"/>" title="<fmt:message key="download" bundle="${resword}"/>">
        </a>
        <a href="ExportDataset?action=delete&datasetId=<c:out value="${currRow.bean.datasetId}"/>&adfId=<c:out value="${currRow.bean.id}"/>">
            <img name="bt_Delete1" src="images/bt_Delete.gif" border="0" alt="<fmt:message key="delete" bundle="${resword}"/>"
                 title="<fmt:message key="delete" bundle="${resword}"/>" align="left" hspace="6"
                 onClick='return confirm("<fmt:message key="if_you_delete_this_dataset" bundle="${restext}"/>");'>
        </a>
    </td>
</tr>
