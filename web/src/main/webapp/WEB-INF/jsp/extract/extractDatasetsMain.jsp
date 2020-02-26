<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>

<jsp:include page="../include/extract-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		<div class="sidebar_tab_content">

		<fmt:message key="extract_dataset_side_info1" bundle="${restext}"/>
		<!-- You may create filters to limit your solutions and
 apply them to your dataset.-->
		<fmt:message key="extract_dataset_side_info2" bundle="${restext}"/>
<br><br>
<fmt:message key="extract_dataset_side_info3" bundle="${restext}"/>

		</div>

		</td>

	</tr>
	<tr id="sidebar_Instructions_closed" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		</td>
  </tr>

<jsp:include page="../include/sideInfo.jsp"/>



<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope="request" id="datasets" class="java.util.ArrayList"/>

<h1><span class="title_manage"><c:out value="${study.name}" />: <fmt:message key="extract_datasets" bundle="${resworkflow}"/></span></h1>

<OL>
<LI><a href="ViewDatasets"><fmt:message key="view_datasets" bundle="${resworkflow}"/></a>
<LI><a href="CreateDataset"><fmt:message key="create_dataset" bundle="${resword}"/></a>
<!--<LI><a href="CreateFiltersOne">View Filters</a>-->
<!--<LI><a href="CreateFiltersOne?action=begin&submit=Create+New+Filter">Create Filter</a>-->
</OL>


<c:import url="../include/showTable.jsp">
<c:param name="rowURL" value="showDatasetRow.jsp" />
</c:import>

<c:import url="../include/workflow.jsp">
   <c:param name="module" value="extract"/>
</c:import>

<jsp:include page="../include/footer.jsp"/>
