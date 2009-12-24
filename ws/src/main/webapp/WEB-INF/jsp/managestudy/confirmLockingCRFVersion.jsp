<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<jsp:include page="../include/admin-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		<div class="sidebar_tab_content">

        <fmt:message key="confirm_archive_of_this_version"  bundle="${resword}"/>.
        <fmt:message key="no_new_event_will_be_created_using_this_version"  bundle="${resword}"/>

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

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='crfVersionToLock' class='org.akaza.openclinica.bean.submit.CRFVersionBean'/>
<script type="text/JavaScript" language="JavaScript">
  <!--
 function myCancel() {

    cancelButton=document.getElementById('cancel');
    if ( cancelButton != null) {
      if(confirm('<fmt:message key="sure_to_cancel" bundle="${resword}"/>')) {
        window.location.href="ListCRF?module=<c:out value='${module}'/>";
       return true;
      } else {
        return false;
       }
     }
     return true;

  }
   //-->
</script>
<h1><span class="title_manage"><fmt:message key="confirm_archiving_crf_version"  bundle="${resword}"/> </span></h1>

<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr valign="top"><td class="table_header_column_top"><fmt:message key="CRF_name" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${crf.name}"/>
   </td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="CRF_version" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${crfVersionToLock.name}"/>
  </td></tr>

 <tr valign="top"><td class="table_header_column"><fmt:message key="description" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${crfVersionToLock.description}"/>
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="revision_notes" bundle="${resword}"/>:</td><td class="table_cell">
    <c:out value="${crfVersionToLock.revisionNotes}"/>
   </td></tr>

  </table>
 </div>
</div></div></div></div></div></div></div></div>
 </div>
<br>

<c:choose>
<c:when test="${!empty eventSubjectsUsingVersion}">
<fmt:message key="subjects_events_using_this_version"  bundle="${resword}"/>:
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tr valign="top" bgcolor="#EBEBEB">
   <td class="table_header_row_left"><fmt:message key="study_subject_label" bundle="${resword}"/></td>

   <td class="table_header_row"><fmt:message key="study_name" bundle="${resword}"/></td>

   <td class="table_header_row"><fmt:message key="SE" bundle="${resword}"/></td>

  </tr>
<c:forEach var="studySubjectEvent" items="${eventSubjectsUsingVersion}">
<tr>
<td class="table_cell_left"><c:out value="${studySubjectEvent.studySubjectName}"/></td>
<td class="table_cell"><c:out value="${studySubjectEvent.studyName}"/></td>
<td class="table_cell"><c:out value="${studySubjectEvent.eventName}"/></td>
</tr>
</c:forEach>
</table>
 </div>
</div></div></div></div></div></div></div></div>
 </div>

</c:when>
<c:otherwise>
 <fmt:message key="currently_no_subject_using_this_version"  bundle="${resword}"/>
</c:otherwise>
</c:choose>
</br>
<table border="0" cellpadding="0" cellspacing="0">
<tr>
<td>
<form action='LockCRFVersion?action=confirm&module=admin&id=<c:out value="${crfVersionToLock.id}"/>' method="POST">
<input type="submit" name="submit" value="<fmt:message key="Archive_CRF_Version" bundle="${resword}"/>" class="button_xlong">
</form>
</td>
<td><input type="button" name="Cancel" id="cancel" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button_medium" onClick="javascript:myCancel();"/>
</td>
</tr>
</table>

<c:choose>
 <c:when test="${userBean.sysAdmin && module=='admin'}">
    <c:import url="../include/workflow.jsp">
    <c:param name="module" value="admin"/>
    </c:import>
  </c:when>
  <c:otherwise>
    <c:import url="../include/workflow.jsp">
    <c:param name="module" value="manage"/>
     </c:import>
  </c:otherwise>
  </c:choose>


<jsp:include page="../include/footer.jsp"/>
