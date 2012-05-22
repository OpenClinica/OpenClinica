<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<jsp:include page="../include/admin-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<%-- BWP>> for formatting dates --%>
<c:set var="dateFormatPattern" value="${requestScope['dateFormatPattern']}" />
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

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
<jsp:include page="../include/sideInfo.jsp"/>


<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='user' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='message' class='java.lang.String'/>

<h1><span class="title_manage">
<fmt:message key="view_user_account" bundle="${resword}"/>
</span></h1>


<%--<p><a href="EditUserAccount?userId=<c:out value="${user.id}" />">Edit this user account</a>
&nbsp;<br>
<a href="AuditLogUser?userLogId=<c:out value="${user.id}" />">View Audit Logs for this user</a>--%>

<div style="width: 400px">

<!-- These DIVs define shaded box borders -->

	<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

		<div class="tablebox_center">


		<!-- Table Contents -->

<table border="0" cellpadding="0" cellspacing="0" width="100%">
	<tr>
		<td class="table_header_column_top"><fmt:message key="first_name" bundle="${resword}"/>:</td>
		<td class="table_cell_top"><c:out value="${user.firstName}" />&nbsp;</td>
	</tr>
	<tr>
		<td class="table_header_column"><fmt:message key="last_name" bundle="${resword}"/>:</td>
		<td class="table_cell"><c:out value="${user.lastName}" />&nbsp;</td>
	</tr>
	<tr>
		<td class="table_header_column"><fmt:message key="email" bundle="${resword}"/>:</td>
		<td class="table_cell"><c:out value="${user.email}" />&nbsp;</td>
	</tr>
	<tr>
		<td class="table_header_column"><fmt:message key="phone" bundle="${resword}"/>:</td>
		<td class="table_cell"><c:out value="${user.phone}" />&nbsp;</td>
	</tr>
	<tr>
		<td class="table_header_column"><fmt:message key="institutional_affiliation" bundle="${resword}"/>:</td>
		<td class="table_cell"><c:out value="${user.institutionalAffiliation}" />&nbsp;</td>
	</tr>

	<tr>
		<td class="table_header_column"><fmt:message key="business_administrator" bundle="${resword}"/>:</td>
		<c:choose>
			<c:when test="${user.sysAdmin}">
				<td class="table_cell"><fmt:message key="yes" bundle="${resword}"/></td>
			</c:when>
			<c:otherwise>
				<td class="table_cell"><fmt:message key="no" bundle="${resword}"/></td>
			</c:otherwise>
		</c:choose>
	</tr>
	<tr>
		<td class="table_header_column"><fmt:message key="technical_administrator" bundle="${resword}"/>:</td>
		<c:choose>
			<c:when test="${user.techAdmin}">
				<td class="table_cell"><fmt:message key="yes" bundle="${resword}"/></td>
			</c:when>
			<c:otherwise>
				<td class="table_cell"><fmt:message key="no" bundle="${resword}"/></td>
			</c:otherwise>
		</c:choose>
	</tr>
	<tr>
		<td class="table_header_column"><fmt:message key="status" bundle="${resword}"/>:</td>
		<td class="table_cell"><c:out value="${user.status.name}" />&nbsp;</td>
	</tr>
	<tr>
    <td class="table_header_column"><fmt:message key="date_created" bundle="${resword}"/>:</td>
		<td class="table_cell"><fmt:formatDate value="${user.createdDate}" type="date" pattern="${dteFormat}"/>&nbsp;</td>
	</tr>
  <tr>
		<td class="table_header_column"><fmt:message key="owner" bundle="${resword}"/>:</td>
		<td class="table_cell"><c:out value="${user.owner.name}" />&nbsp;</td>
	</tr>
	<tr>
		<td class="table_header_column"><fmt:message key="date_updated" bundle="${resword}"/>:</td>
		<td class="table_cell"><fmt:formatDate value="${user.updatedDate}" type="date" pattern="${dteFormat}"/>&nbsp;</td>
	</tr>
	<tr>
		<td class="table_header_column"><fmt:message key="updated_by" bundle="${resword}"/>:</td>
		<td class="table_cell"><c:out value="${user.updater.name}" />&nbsp;</td>
	</tr>
	<tr>
        <td class="table_header_column"><fmt:message key="authorized_run_web_services" bundle="${resword}"/>:</td>
        <c:choose>
            <c:when test="${user.runWebservices}">
                <td class="table_cell"><fmt:message key="yes" bundle="${resword}"/></td>
            </c:when>
            <c:otherwise>
                <td class="table_cell"><fmt:message key="no" bundle="${resword}"/></td>
            </c:otherwise>
        </c:choose>
    </tr>
<!-- TODO:
for each study user is in, show:
	Role
	Studies created/owned
	CRFs created/owned (including versions)
	Study Events created/owned
	Subjects created/owned
	Queries created/owned
	Datasets downloaded
	Link to reload page including full audit record for User.

-->

	<tr>
		<td class="table_header_column"><fmt:message key="roles" bundle="${resword}"/>:</td>
		<td class="table_cell">
			<c:forEach var="studyUserRole" items="${user.roles}">
				<c:out value="${studyUserRole.studyName}" /> - <c:out value="${studyUserRole.role.description}" /><br/>
			</c:forEach>
		</td>
	</tr>
	</table>
	</div>

	</div></div></div></div></div></div></div></div>

	</div>

<table border="0" cellpadding="0" cellspacing="0">
  <tr>
   <td>
   <form action='EditUserAccount?userId=<c:out value="${user.id}" />' method="POST">
    <input type="submit" name="submit" value="<fmt:message key="edit_this_user_account" bundle="${resword}"/>" class="button_long">
   </form>
   </td>
   <td>
   <form action='CreateUserAccount' method="POST">
    <input type="submit" name="submit" value="<fmt:message key="create_a_new_user" bundle="${resword}"/>" class="button_long">
   </form>
   </td>
   <td>
   <form action='ListUserAccounts' method="POST">
    <input type="submit" name="submit" value="<fmt:message key="exit" bundle="${resword}"/>" class="button">
   </form>
   </td>
   <td>
   </td>
  </tr>
</table>
 <c:import url="../include/workflow.jsp">
  <c:param name="module" value="admin"/>
 </c:import>
<jsp:include page="../include/footer.jsp"/>
