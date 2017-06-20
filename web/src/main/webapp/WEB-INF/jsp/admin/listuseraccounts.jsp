<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<jsp:include page="../include/admin-header.jsp"/>


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
<jsp:useBean scope='request' id='table' class='org.akaza.openclinica.web.bean.EntityBeanTable'/>
<jsp:useBean scope='request' id='message' class='java.lang.String'/>

<h1><span class="title_manage"><fmt:message key="administer_users" bundle="${resword}"/> <a href="javascript:openDocWindow('https://docs.openclinica.com/3.1/openclinica-user-guide/overview-user-types-and-roles')"><span class="icon icon-question-circle gray"></span></a></span></h1><br/>

<div class="homebox_bullets"><a href="CreateUserAccount"><fmt:message key="create_a_new_user" bundle="${resword}"/></a></div><br/>
<div class="homebox_bullets"><a href="AuditUserActivity?restore=true"><fmt:message key="audit_user_activity" bundle="${resword}"/></a></div><br/>
<div class="homebox_bullets"><a href="Configure"><fmt:message key="lock_out_configuration" bundle="${resword}"/></a></div><br/>
<div class="homebox_bullets"><a href="ConfigurePasswordRequirements"><fmt:message key="configure_password_requirements" bundle="${resword}"/></a></div>
<p></p>

<c:import url="../include/showTable.jsp">
	<c:param name="rowURL" value="showUserAccountRow.jsp" />
</c:import>

<jsp:include page="../include/footer.jsp"/>
