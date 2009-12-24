<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<jsp:include page="../include/extract-header.jsp"/>


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

<jsp:include page="../include/createDatasetSideInfo.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='newDataset' class='org.akaza.openclinica.bean.extract.DatasetBean'/>

<c:choose>
<c:when test="${newDataset.id>0}">
<h1><span class="title_manage"><fmt:message key="edit_dataset" bundle="${resword}"/> - <fmt:message key="confirm_dataset_properties" bundle="${resword}"/>
: <c:out value='${newDataset.name}'/></span></h1>
</c:when>
<c:otherwise>
<h1><span class="title_manage"><fmt:message key="create_dataset" bundle="${resword}"/>: <fmt:message key="confirm_dataset_properties" bundle="${resword}"/></span></h1>
</c:otherwise>
</c:choose>

<%--
<jsp:include page="createDatasetBoxes.jsp" flush="true">
<jsp:param name="saveAndExport" value="1"/>
</jsp:include>
--%>
<p><fmt:message key="confirm_dataset_properties" bundle="${restext}"/>

<form action="CreateDataset" method="post">
<input type="hidden" name="action" value="confirmall" />

<table>
	<tr>
		<td class="text"><fmt:message key="name" bundle="${resword}"/></td>
		<td class="text"><b><c:out value="${newDataset.name}" /></b>
	</tr>
	<tr>
		<td class="text" valign="top"><fmt:message key="description" bundle="${resword}"/></td>
		<td class="text" valign="top"><b><c:out value="${newDataset.description}" /></b></td>
	</tr>
	<%--<tr>
		<td class="text">Events Sample From:</td>
		<td class="text"><b>
		   <c:choose>
		   <c:when test="${defaultStart==newDataset.dateStart}">
		    Not specified
		   </c:when>
		   <c:otherwise>
		   <fmt:formatDate value="${newDataset.dateStart}" dateStyle="short"/>
		   </c:otherwise>
		   </c:choose>
		</b>
	</tr>
	<tr>
		<td class="text">Events Sample To:</td>
		<td class="text"><b>
		<c:choose>
		   <c:when test="${defaultEnd==newDataset.dateEnd}">
		   Not specified
		   </c:when>
		   <c:otherwise>
		   <fmt:formatDate value="${newDataset.dateEnd}" dateStyle="short"/>

		   </c:otherwise>
		   </c:choose>
		</b>
	</tr>
	<tr>
		<td class="text"><fmt:message key="status" bundle="${resword}"/></td>
		<td class="text"><b><c:out value="${newDataset.status.name}" /></b>
	</tr>--%>
	<tr>
		<td colspan="2" align="left">
		  <input type="submit" name="btnSubmit" value="<fmt:message key="confirm_and_save" bundle="${resword}"/>" class="button_xlong"/>
		</td>
		<td>
		  <input type="submit" name="btnSubmit" value="<fmt:message key="back" bundle="${resword}"/>" class="button_xlong"/>
		</td>
	</tr>
</table>

</form>

<c:import url="../include/workflow.jsp">
   <c:param name="module" value="extract"/>
</c:import>
<jsp:include page="../include/footer.jsp"/>
