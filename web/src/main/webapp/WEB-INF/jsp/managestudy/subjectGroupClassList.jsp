<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>

<jsp:include page="../include/managestudy-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray"></span></a>

		<fmt:message key="instructions" bundle="${resword}"/>

		<div class="sidebar_tab_content">

		<fmt:message key="List_of_all_subject_group_classes_with_their_subject"  bundle="${resword}"/> <fmt:message key="select_any_group_class_for_details_on_it_records"  bundle="${resword}"/>
		<br><br>
        <fmt:message key="the_list_of_subject_group_classes_in_the_current_study"  bundle="${resword}"/>

		</div>

		</td>

	</tr>
	<tr id="sidebar_Instructions_closed" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray"></span></a>

		<fmt:message key="instructions" bundle="${resword}"/>

		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='table' class='org.akaza.openclinica.web.bean.EntityBeanTable'/>

<h1><span class="title_manage">
<fmt:message key="manage_all_groups_in_study" bundle="${restext}"/>
    <c:out value="${study.name}"/>
    <a href="javascript:openDocWindow('https://docs.openclinica.com/3.1/openclinica-user-guide/monitor-and-manage-data/manage-all-groups')">
        <span class="icon icon-question-circle gray"></span></a>
</span></h1>

<%-- 
<c:if test="${(isParentStudy == true) && (!study.status.locked)}" >
    <div class="homebox_bullets"><a href="CreateSubjectGroupClass"><fmt:message key="create_a_subject_group_class" bundle="${resword}"/></a></div>
</c:if>
--%>

<p></p>
<c:import url="../include/showTable.jsp"><c:param name="rowURL" value="showSubjectGroupClassRow.jsp" /></c:import>
<br>
<div class="homebox_bullets"><a href="pages/studymodule"><fmt:message key="go_back_build_study_page" bundle="${resword}"/></a></div>

<jsp:include page="../include/footer.jsp"/>
