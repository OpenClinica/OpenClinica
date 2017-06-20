<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>

<c:choose>
<c:when test="${userBean.sysAdmin && module=='admin'}">
 <c:import url="../include/admin-header.jsp"/>
</c:when>
<c:otherwise>
 <c:import url="../include/managestudy-header.jsp"/>
</c:otherwise>
</c:choose>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray"></span></a>

		<fmt:message key="instructions" bundle="${restext}"/>

		<div class="sidebar_tab_content">
        <fmt:message key="CRF_library_shows_all_CRFs" bundle="${restext}"/>

		</div>

		</td>

	</tr>
	<tr id="sidebar_Instructions_closed" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray"></span></a>

		<fmt:message key="instructions" bundle="${restext}"/>

		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='userRole' class='org.akaza.openclinica.bean.login.StudyUserRoleBean' />
<jsp:useBean scope='request' id='table' class='org.akaza.openclinica.web.bean.EntityBeanTable'/>
<c:choose>
<c:when test="${userBean.sysAdmin && module=='admin'}">
	<h1><span class="title_manage"><fmt:message key="administer_CRFs2" bundle="${resworkflow}"/>
        <a href="javascript:openDocWindow('https://docs.openclinica.com/3.1/administer-crfs-crf-template#content-title-2991')">
            <span class="icon icon-question-circle gray"></span></a>
</span></h1>
</c:when>
<c:otherwise>
	<h1><span class="title_manage"><fmt:message key="manage_CRFs2" bundle="${resworkflow}"/>
        <a href="javascript:openDocWindow('https://docs.openclinica.com/3.1/openclinica-user-guide/monitor-and-manage-data/manage-crf')">
		<span class="icon icon-question-circle gray"></span></a>
</span></h1>
</c:otherwise>
</c:choose>

<!-- <p><fmt:message key="can_download_blank_CRF_excel" bundle="${restext}"/> <a href="DownloadVersionSpreadSheet?template=1"><b><fmt:message key="here" bundle="${resword}"/></b></a>.</p> -->
<%--
<p><fmt:message key="also_download_set_example_CRFs" bundle="${restext}"/> <a href="http://www.openclinica.org/entities/entity_details.php?eid=151" target="_blank"><fmt:message key="here" bundle="${resword}"/></a>.</p>
--%>
<br/>
<c:import url="../include/showTable.jsp"><c:param name="rowURL" value="showCRFRow.jsp" /></c:import>


<jsp:include page="../include/footer.jsp"/>
