<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<jsp:useBean scope="request" id="currRow" class="org.akaza.openclinica.web.bean.FilterRow" />

<tr>
	<td class="table_cell">
		<c:choose>
			<c:when test='${currRow.bean.status.name == "removed"}'>
				<font color='gray'><c:out value="${currRow.bean.name}" /></font>
			</c:when>
			<c:otherwise>
				<c:out value="${currRow.bean.name}" />
			</c:otherwise>
		</c:choose>
	</td>
	<td class="table_cell"><c:out value="${currRow.bean.description}" /></td>
	<td class="table_cell"><c:out value="${currRow.bean.owner.name}" /></td>
	<td class="table_cell"><fmt:formatDate value="${currRow.bean.createdDate}" pattern="${dteFormat}"/></td>
	<td class="table_cell"><c:out value="${currRow.bean.status.name}" /></td>
	
	<%-- ACTIONS --%>
	<td class="table_cell">
	<table border="0" cellpadding="0" cellspacing="0">
      <tr>
		<c:choose>
			<c:when test='${currRow.bean.status.name == "removed"}'>
			<td></td>
				<%-- parts to be added later, look at showUserAccountRow.jsp, tbh --%>
			</c:when>
			<c:otherwise>
			<td>	
				<a href="ApplyFilter?action=details&filterId=<c:out value="${currRow.bean.id}"/>"
			onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
			onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><img 
		    name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6"></a>
		    </td><td>
		    	<a href="ApplyFilter?action=validate&submit=Apply Filter&filterId=<c:out value="${currRow.bean.id}"/>"
			onMouseDown="javascript:setImage('bt_Export1','images/bt_Export_d.gif');"
			onMouseUp="javascript:setImage('bt_Export1','images/bt_Export.gif');"><img 
		    name="bt_Export1" src="images/bt_Export.gif" border="0" alt="<fmt:message key="apply_filter" bundle="${resword}"/>" title="<fmt:message key="apply_filter" bundle="${resword}"/>" align="left" hspace="6"></a>
			</td>
				
			
			</c:otherwise>
		</c:choose>
		</tr>
		</table>	
	</td>
</tr>
