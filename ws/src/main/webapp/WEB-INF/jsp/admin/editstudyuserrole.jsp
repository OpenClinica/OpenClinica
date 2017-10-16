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
<jsp:useBean scope='request' id='studyUserRole' class='org.akaza.openclinica.bean.login.StudyUserRoleBean'/>
<jsp:useBean scope='request' id='userName' class='java.lang.String'/>
<jsp:useBean scope='request' id='chosenRoleId' type='java.lang.Integer' />
<jsp:useBean scope="request" id="roles" class="java.util.LinkedHashMap"/>

<h1><span class="title_manage">
<fmt:message key="modify_role_for" bundle="${restext}">
	<fmt:param value="${userName}"/>
	<fmt:param value="${studyUserRole.studyName}"/>
</fmt:message>
</span></h1>

<form action="EditStudyUserRole" method="post">
<jsp:include page="../include/showSubmitted.jsp" />
<input type="hidden" name="studyId" value="<c:out value="${studyUserRole.studyId}" />" />
<input type="hidden" name="userName" value="<c:out value="${userName}" />" />

<div style="width: 400px">

<!-- These DIVs define shaded box borders -->

	<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

		<div class="tablebox_center">


		<!-- Table Contents -->

<table border="0" cellpadding="0" cellspacing="0" width="100%">
	<tr>
		<td class="table_header_column_top"><fmt:message key="username2" bundle="${resword}"/>:</td>
		<td class="table_cell_top"><b><c:out value="${userName}" /></b></td>
	</tr>
	<tr>
		<td class="table_header_column"><fmt:message key="study" bundle="${resword}"/>:</td>
		<td class="table_cell"><b><c:out value="${studyUserRole.studyName}" /></b></td>
	</tr>
	<tr>
		<td class="table_header_column"><fmt:message key="role" bundle="${resword}"/>:</td>
		<td valign="top">
			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td valign="top"><div class="formfieldM_BG">
						<select name="role" class="formfieldM">
							<option value="0">-<fmt:message key="select" bundle="${resword}"/>-</option>
							<c:forEach var="role" items="${roles}">
								<c:choose>
									<c:when test="${chosenRoleId == role.key}" >
										<option value="<c:out value="${role.key}"/>" selected><c:out value="${role.value}" /></option>
									</c:when>
									<c:otherwise>
										<option value="<c:out value="${role.key}"/>"><c:out value="${role.value}" /></option>
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</select>
					</div></td>
					<td>&nbsp;</td>
				</tr>
				<tr>
					<td colspan="2"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="role"/></jsp:include></td>
				</tr>
			</table>
		</td>
	</tr>

	</table>
	</div>

	</div></div></div></div></div></div></div></div>

	</div>

<input type="submit" name="Submit" value="<fmt:message key="submit" bundle="${resword}"/>" class="button_medium">
<input type="button" onclick="confirmCancel('ListUserAccounts');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/>

</form>

<c:import url="../include/workflow.jsp">
 <c:param name="module" value="admin"/>
</c:import>
<jsp:include page="../include/footer.jsp"/>
