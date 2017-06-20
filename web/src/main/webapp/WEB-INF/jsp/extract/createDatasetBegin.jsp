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

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray"></span></a>

		<fmt:message key="instructions" bundle="${resword}"/>

		<div class="sidebar_tab_content">

		</div>

		</td>

	</tr>
	<tr id="sidebar_Instructions_closed" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray"></span></a>

		<fmt:message key="instructions" bundle="${resword}"/>

		</td>
  </tr>

<jsp:include page="../include/sideInfo.jsp"/>


<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>


<h1><span class="title_manage"><c:out value="${study.name}"/>: <fmt:message key="create_dataset" bundle="${resword}"/> <a href="javascript:openDocWindow('https://docs.openclinica.com/3.1/openclinica-user-guide')"><span class="icon icon-question-circle gray"></span></a></span></h1>

<p><fmt:message key="steps_to_extract_or_filter_dataset" bundle="${restext}"/>
<ol>
<li><fmt:message key="select_events_CRF_items_apply_dataset" bundle="${restext}"/>
<li><fmt:message key="specify_study_longitudinal_scope" bundle="${restext}"/>
<li><fmt:message key="specify_filtering_choosing_filters" bundle="${restext}"/>
<li><fmt:message key="specify_metadata_for_the_dataset" bundle="${restext}"/>
<li><fmt:message key="save_and_export_desired_format" bundle="${restext}"/>
</ol></p>
<form action="CreateDataset" method="post">
<input type="hidden" name="action" value="begin"/>
<input type="submit" name="Submit" value="<fmt:message key="proceed_to_create_a_dataset" bundle="${restext}"/>" class="button_xlong"/>
<input type="button" onclick="confirmCancel('ViewDatasets');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/>
</form>

<c:import url="../include/workflow.jsp">
   <c:param name="module" value="extract"/>
</c:import>
<jsp:include page="../include/footer.jsp"/>
