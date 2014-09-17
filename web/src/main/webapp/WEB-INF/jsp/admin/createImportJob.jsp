<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>

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

<c:set var="activeStudyId" value="${0}" />

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='filePath' class='java.lang.String'/>
<jsp:useBean scope='request' id='hours' class='java.lang.String'/>
<jsp:useBean scope='request' id='minutes' class='java.lang.String'/>
<jsp:useBean scope="request" id="studies" class="java.util.ArrayList"/>

<h1><span class="title_manage">Create Scheduled Job: Import Data</span></h1>
<p>
<c:set var="dtetmeFormat"><fmt:message key="date_time_format_string" bundle="${resformat}"/></c:set>
<jsp:useBean id="now" class="java.util.Date" />
<P><I><fmt:message key="note_that_job_is_set" bundle="${resword}"/> <fmt:formatDate value="${now}" pattern="${dtetmeFormat}"/>.</I></P>

<p class="text"><br/><fmt:message key="field_required" bundle="${resword}"/></p>

<form action="CreateJobImport" method="post">

<input type="hidden" name="action" value="confirmall" />

<table>
	<tr>
		<td class="text"><b><fmt:message key="import_job_name" bundle="${resword}"/>:</b><br><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="jobName"/></jsp:include></td>
		<td class="text">
			<input type="text" name="jobName" size="30" value="<c:out value="${jobName}"/>"/> *
		</td>		
	</tr>
	<tr>
		<td class="text"><b><fmt:message key="import_job_desc" bundle="${resword}"/>:</b><br><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="jobDesc"/></jsp:include></td>
		<td class="text"><input type="text" name="jobDesc" size="60" value="<c:out value="${jobDesc}"/>"/> *
		</td> 
	</tr>
	<tr>
		<td class="text">
			<b><fmt:message key="import_job_study" bundle="${resword}"/>:</b><br>
			<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="jobDesc"/></jsp:include>
		</td>
		<td class="text">
						<%-- <select name="studyId">
							<c:forEach var="studyRole" items="${studies}">
								<option value="<c:out value="${studyRole.studyId}"/>"><c:out value="${studyRole.studyName}"/>
							</c:forEach>
						</select> --%>
						<select name="studyId" id="studyId">
						<option value="0">-<fmt:message key="select" bundle="${resword}"/>-</option>

                            <c:forEach var="study" items="${studies}">
								<c:choose>
									<c:when test="${activeStudy == study.id}">
										<c:choose>
										<c:when test="${study.parentStudyId > 0}">
											<option value='<c:out value="${study.id}" />' selected>&nbsp;&nbsp;&nbsp;&nbsp;<c:out value="${study.name}" /></option>
										</c:when>
										<c:otherwise>
											<option value='<c:out value="${study.id}" />' selected><c:out value="${study.name}" /></option>
										</c:otherwise>
										</c:choose>
									</c:when>
									<c:otherwise>
										<c:choose>
										<c:when test="${study.parentStudyId>0}">
											<option value='<c:out value="${study.id}" />'>&nbsp;&nbsp;&nbsp;&nbsp;<c:out value="${study.name}" /></option>
										</c:when>
										<c:otherwise>
											<option value='<c:out value="${study.id}" />'><c:out value="${study.name}" /></option>
										</c:otherwise>
										</c:choose>
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</select> *

						<%-- have to put a default picker here next, tbh--%>
		</td>
		
	</tr>
	<tr>
		<td class="text"><b><fmt:message key="the_default_filepath" bundle="${resword}"/>:</b></td>
		<td class="text"><c:out value="${filePath}"/></td>
	</tr>
	<tr>
		<td class="text"><fmt:message key="you_can_create_a_new_directory" bundle="${resword}"/>:<br>
		<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="filePathDir"/></jsp:include>
		</td>
		<td class="text"><input name="filePathDir" type=text/></td>
	</tr>

	<%-- <tr>
		<td class="text"><b>Start Date/Time:</b></td>
		<td class="text">
			<table border="0" cellpadding="0" cellspacing="0">
			<tr>
				<c:import url="../include/showDateTimeInput.jsp"><c:param name="prefix" value="job"/><c:param name="count" value="1"/></c:import>
				<td>(<fmt:message key="date_time_format" bundle="${resformat}"/>) </td>
			</tr>
			<tr>
				<td colspan="7"><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="start"/></jsp:include></td>
			</tr>
			</table>
		</td>
	</tr>--%>

	<tr>
		<td class="text"><b><fmt:message key="frequency" bundle="${resword}"/>:</b><br><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="hours"/></jsp:include>
		</td>
		<td class="text">
		<fmt:message key="run_every" bundle="${resword}"/>&nbsp;
		<select name="hours">
			<option value="0"
			<c:if test="${hours=='0'}">
			selected
			</c:if>
			>0</option>
			<option value="1"
			<c:if test="${hours=='1'}">
			selected
			</c:if>
			>1</option>
			<option value="2"
			<c:if test="${hours=='2'}">
			selected
			</c:if>
			>2</option>
			<option value="4"
			<c:if test="${hours=='4'}">
			selected
			</c:if>
			>4</option>
			<option value="8"
			<c:if test="${hours=='8'}">
			selected
			</c:if>
			>8</option>
			<option value="24"
			<c:if test="${hours=='24'}">
			selected
			</c:if>
			>24</option>
		</select>
			<fmt:message key="hours_and" bundle="${resword}"/>&nbsp;
		<select name="minutes">
			<option value="0"
			<c:if test="${minutes=='0'}">
			selected
			</c:if>
			>0</option>
			<option value="10"
			<c:if test="${minutes=='10'}">
			selected
			</c:if>
			>10</option>
			<option value="20"
			<c:if test="${minutes=='20'}">
			selected
			</c:if>
			>20</option>
			<option value="30"
			<c:if test="${minutes=='30'}">
			selected
			</c:if>
			>30</option>
			<option value="40"
			<c:if test="${minutes=='40'}">
			selected
			</c:if>
			>40</option>
			<option value="50"
			<c:if test="${minutes=='50'}">
			selected
			</c:if>
			>50</option>
		</select> <fmt:message key="minutes" bundle="${resword}"/>. *
		</td>		
	</tr>

	<tr>
		<td colspan="2" align="left"><fmt:message key="the_freq_you_select_it_to_run" bundle="${resword}"/></td>
	</tr>

	<tr>
		<td class="text">
			<b><fmt:message key="contact_email" bundle="${resword}"/>:</b></td>
		<td class="text">
			<input type="text" name="contactEmail" size="90"/>
			<br><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="contactEmail"/></jsp:include>
		</td>
	</tr>

	<tr>
		<td align="left">
		  <input type="submit" name="btnSubmit" value="<fmt:message key="save" bundle="${resword}"/>" class="button_xlong"/>
		</td>
		</form>
		<form action='ViewImportJob' method="POST">
		<td>
		  <input type="submit" name="btnSubmit" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button_xlong"/>
		</td>
	</tr>
</table>

</form>

<jsp:include page="../include/footer.jsp"/>
