<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<jsp:include page="../include/managestudy-header.jsp"/>

<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<!-- then instructions-->
<tr id="sidebar_Instructions_open">
	<td class="sidebar_tab">
		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
			<span class="icon icon-caret-down gray"></span>
		</a>
		<fmt:message key="instructions" bundle="${restext}"/>
		<div class="sidebar_tab_content">
		    <fmt:message key="study_have_sites_data_collected" bundle="${restext}"/>
		    <fmt:message key="view_details_site_or_create" bundle="${restext}"/>
		</div>
	</td>
</tr>
<tr id="sidebar_Instructions_closed" style="display: none">
	<td class="sidebar_tab">
		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
			<span class="icon icon-caret-down gray"></span>
		</a>
		<fmt:message key="instructions" bundle="${restext}"/>
	</td>
</tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope='request' id='table' class='org.akaza.openclinica.web.bean.EntityBeanTable'/>

<h1><span class="title_manage">
<fmt:message key="manage_all_sites_in_study" bundle="${restext}"/> <c:out value="${study.name}"/></span></h1>
<%-- 
   <c:if test="${!study.status.locked}">
    <div class="homebox_bullets"><a href="CreateSubStudy"><fmt:message key="create_new_site" bundle="${resworkflow}"/></a></div>
   </c:if>
   --%>
<c:import url="../include/showTable.jsp"><c:param name="rowURL" value="showSiteRow.jsp" /></c:import>
<br><br>
<jsp:include page="../include/footer.jsp"/>
