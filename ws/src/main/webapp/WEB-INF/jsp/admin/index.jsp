<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<jsp:include page="../include/admin-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${restext}"/></b>

		<div class="sidebar_tab_content">
        <fmt:message key="director_coordinator_privileges_manage" bundle="${restext}"/><br><br>
        <fmt:message key="side_tables_shows_last_modified" bundle="${restext}"/>

		</div>

		</td>

	</tr>
	<tr id="sidebar_Instructions_closed" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${restext}"/></b>

		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<h1><span class="title_manage"><fmt:message key="business_administrator" bundle="${resword}"/></span></h1>



	<h2><fmt:message key="recent_activity" bundle="${restext}"/></h2>
<c:set var="userCount" value="0"/>
<c:forEach var="user" items="${users}">
  <c:set var="userCount" value="${userCount+1}"/>
</c:forEach>
<c:set var="studyCount" value="0"/>
<c:forEach var="study" items="${studies}">
  <c:set var="studyCount" value="${studyCount+1}"/>
</c:forEach>
<c:set var="subjectCount" value="0"/>
<c:forEach var="subject" items="${subjects}">
  <c:set var="subjectCount" value="${subjectCount+1}"/>
</c:forEach>
<c:set var="crfCount" value="0"/>
<c:forEach var="crf" items="${crfs}">
  <c:set var="crfCount" value="${crfCount+1}"/>
</c:forEach>

	<table border="0" cellpadding="0" cellspacing="0">
		<tr>
			<td valign="top" width="330" style="padding-right: 20px">
    <div class="table_title_Admin"><fmt:message key="subjects" bundle="${resword}"/>
	<c:choose>
	  <c:when test="${allSubjectNumber>0}">
	   (<c:out value="${subjectCount}"/> <fmt:message key="of" bundle="${restext}"/> <c:out value="${allSubjectNumber}"/> <fmt:message key="shown" bundle="${restext}"/>)
	  </c:when>
	  <c:otherwise>
	   <fmt:message key="currently_no_subjects" bundle="${restext}"/>
	  </c:otherwise>
	</c:choose></div>

	<!-- These DIVs define shaded box borders -->
		<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

			<div class="tablebox_center">
				<table border="0" cellpadding="0" cellspacing="0" width="100%">
					<tr valign="top">
						<td class="table_header_row_left"><fmt:message key="person_ID" bundle="${resword}"/></td>
						<td class="table_header_row"><fmt:message key="date_updated" bundle="${resword}"/></td>
						<td class="table_header_row"><fmt:message key="status" bundle="${resword}"/></td>
					</tr>
					<c:forEach var="subject" items="${subjects}">
					<tr valign="top">
						<td class="table_cell_left"><c:out value="${subject.uniqueIdentifier}"/>&nbsp;</td>
						<td class="table_cell">
						 <c:choose>
						  <c:when test="${subject.updatedDate != null}">
						   <fmt:formatDate value="${subject.updatedDate}" pattern="${dteFormat}"/>
						  </c:when>
						  <c:otherwise>
						    <fmt:formatDate value="${subject.createdDate}" pattern="${dteFormat}"/>
						  </c:otherwise>
						 </c:choose>
						</td>
						<td class="table_cell"><c:out value="${subject.status.name}"/></td>
					</tr>
					</c:forEach>
					<tr valign="top">
					 <td class="table_cell" align="right" colspan="3"><c:if test="${subjectCount>0}"><a href="ListSubject"><fmt:message key="show_all" bundle="${resword}"/></a></c:if> </td>
					</tr>
				</table>

			</div>

		</div></div></div></div></div></div></div></div>

			</td>
			<td valign="top" width="330" style="padding-right: 20px">
              <div class="table_title_Admin"><fmt:message key="users" bundle="${resword}"/>
	<c:choose>
	  <c:when test="${allUserNumber>0}">
	   (<c:out value="${userCount}"/> <fmt:message key="of" bundle="${restext}"/> <c:out value="${allUserNumber}"/> <fmt:message key="shown" bundle="${restext}"/>)
	  </c:when>
	  <c:otherwise>
	   <fmt:message key="currently_no_users" bundle="${restext}"/>
	  </c:otherwise>
	</c:choose></div>

	<!-- These DIVs define shaded box borders -->
		<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

			<div class="tablebox_center">
			<table border="0" cellpadding="0" cellspacing="0" width="100%">
					<tr valign="top">
						<td class="table_header_row_left"><fmt:message key="user_name" bundle="${resword}"/></td>
						<td class="table_header_row"><fmt:message key="date_updated" bundle="${resword}"/></td>
						<td class="table_header_row"><fmt:message key="status" bundle="${resword}"/></td>
					</tr>
					<c:forEach var="user" items="${users}">
					 <tr valign="top">
						<td class="table_cell_left"><c:out value="${user.name}"/></td>
						<td class="table_cell">
						<c:choose>
						 <c:when test="${user.updatedDate != null}">
						  <fmt:formatDate value="${user.updatedDate}" pattern="${dteFormat}"/>
						 </c:when>
						 <c:otherwise>
						    <fmt:formatDate value="${user.createdDate}" pattern="${dteFormat}"/>
						 </c:otherwise>
						</c:choose>
						</td>
						<td class="table_cell"><c:out value="${user.status.name}"/></td>
					 </tr>
					</c:forEach>
					<tr valign="top">
					 <td class="table_cell" align="right" colspan="3"><c:if test="${userCount>0}"><a href="ListUserAccounts"><fmt:message key="show_all" bundle="${resword}"/></a> |</c:if> <a href="CreateUserAccount"><fmt:message key="add_new" bundle="${resword}"/></a> </td>
					</tr>
				</table>


			</div>

		</div></div></div></div></div></div></div></div>


	</td>
		</tr>
		</table><br>
	<table border="0" cellpadding="0" cellspacing="0">
		<tr>
			<td valign="top" width="330" style="padding-right: 20px">
   <div class="table_title_Admin">
	<fmt:message key="studies" bundle="${resword}"/>
	<c:choose>
	  <c:when test="${allStudyNumber > 0}">
	   (<c:out value="${studyCount}"/> of <c:out value="${allStudyNumber}"/> <fmt:message key="shown" bundle="${restext}"/>)
	  </c:when>
	  <c:otherwise>
	   (<fmt:message key="currently_no_studies" bundle="${resword}"/>)
	  </c:otherwise>
	</c:choose></div>

	<!-- These DIVs define shaded box borders -->
		<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

			<div class="tablebox_center">
				<table border="0" cellpadding="0" cellspacing="0" width="100%">
					<tr valign="top">
						<td class="table_header_row_left"><fmt:message key="name" bundle="${resword}"/></td>
						<td class="table_header_row"><fmt:message key="date_updated" bundle="${resword}"/></td>
						<td class="table_header_row"><fmt:message key="status" bundle="${resword}"/></td>
					</tr>
					<c:forEach var="study" items="${studies}">
					<tr valign="top">
						<td class="table_cell_left"><c:out value="${study.name}"/></td>
						<td class="table_cell">
						<c:choose>
						 <c:when test="${study.updatedDate != null}">
						  <fmt:formatDate value="${study.updatedDate}" pattern="${dteFormat}"/>
						 </c:when>
						 <c:otherwise>
						  <fmt:formatDate value="${study.createdDate}" pattern="${dteFormat}"/>
						 </c:otherwise>
						</c:choose>
						</td>
						<td class="table_cell"><c:out value="${study.status.name}"/></td>
					</tr>
					</c:forEach>
					<tr valign="top">
					 <td class="table_cell" align="right" colspan="3"><c:if test="${studyCount>0}"><a href="ListStudy"><fmt:message key="show_all" bundle="${resword}"/></a> | </c:if><a href="CreateStudy"><fmt:message key="add_new" bundle="${resword}"/></a> </td>
					</tr>
				</table>




			</div>

		</div></div></div></div></div></div></div></div>

			</td>
			<td valign="top" width="330" style="padding-right: 20px">

	<div class="table_title_Admin"><fmt:message key="CRFs" bundle="${resword}"/>
	<c:choose>
	  <c:when test="${allCrfNumber>0}">
	   (<c:out value="${crfCount}"/> <fmt:message key="of" bundle="${restext}"/> <c:out value="${allCrfNumber}"/> <fmt:message key="shown" bundle="${restext}"/>)
	  </c:when>
	  <c:otherwise>
	   <fmt:message key="currently_no_crfs" bundle="${restext}"/>
	  </c:otherwise>
	</c:choose></div>

	<!-- These DIVs define shaded box borders -->
		<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

			<div class="tablebox_center">
				<table border="0" cellpadding="0" cellspacing="0" width="100%">
					<tr valign="top">
						<td class="table_header_row_left"><fmt:message key="name" bundle="${resword}"/></td>
						<td class="table_header_row"><fmt:message key="date_updated" bundle="${resword}"/></td>
						<td class="table_header_row"><fmt:message key="status" bundle="${resword}"/></td>
					</tr>
					<c:forEach var="crf" items="${crfs}">
					<tr valign="top">
						<td class="table_cell_left"><c:out value="${crf.name}"/></td>
						<td class="table_cell">
						 <c:choose>
						  <c:when test="${crf.updatedDate != null}">
						   <fmt:formatDate value="${crf.updatedDate}" pattern="${dteFormat}"/>
						  </c:when>
						  <c:otherwise>
						    <fmt:formatDate value="${crf.createdDate}" pattern="${dteFormat}"/>
						  </c:otherwise>
						 </c:choose>
						</td>
						<td class="table_cell"><c:out value="${crf.status.name}"/></td>
					</tr>
					</c:forEach>
					<tr valign="top">
					 <td class="table_cell" align="right" colspan="3"><c:if test="${crfCount>0}"><a href="ListCRF"><fmt:message key="show_all" bundle="${resword}"/></a> | </c:if><a href="CreateCRFVersion?module=admin"><fmt:message key="add_new" bundle="${resword}"/></a> </td>
					</tr>
				</table>

			</div>

		</div></div></div></div></div></div></div></div>


	</td>
		</tr>
	</table>


<c:import url="../include/workflow.jsp">
 <c:param name="module" value="admin"/>
</c:import>


<jsp:include page="../include/footer.jsp"/>
