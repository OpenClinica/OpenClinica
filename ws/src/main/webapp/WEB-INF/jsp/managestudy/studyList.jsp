<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="core.org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="core.org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="core.org.akaza.openclinica.i18n.notes" var="restext"/>

<jsp:include page="../include/admin-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		<div class="sidebar_tab_content">
		<fmt:message key="studies_are_indicated_in_bold" bundle="${restext}"/>
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

<jsp:useBean scope='session' id='userBean' class='core.org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='table' class='core.org.akaza.openclinica.web.bean.EntityBeanTable'/>

<h1><span class="title_manage"><fmt:message key="administer_studies" bundle="${resword}"/> <a href="javascript:openDocWindow('help/6_1_administerStudies_Help.html')"><img src="images/bt_Help_Manage.gif" border="0" alt="<fmt:message key="help" bundle="${resword}"/>" title="<fmt:message key="help" bundle="${resword}"/>"></a></span></h1>

<div class="homebox_bullets"><a href="CreateStudy"><fmt:message key="create_a_new_study" bundle="${resword}"/></a></div>
<p>
<fmt:message key="studies_are_indicated_in_bold" bundle="${restext}"/>
</p>

<c:import url="../include/showTable.jsp"><c:param name="rowURL" value="showStudyRow.jsp" /></c:import>
<br><br>

<jsp:include page="../include/footer.jsp"/>
