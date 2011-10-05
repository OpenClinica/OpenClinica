<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>

<jsp:include page="../include/managestudy_top_pages_new.jsp"/>
	
	

<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="../../images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		<div class="sidebar_tab_content">

		</div>

		</td>

	</tr>
	<tr id="sidebar_Instructions_closed" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="../../images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		</td>
  </tr><jsp:include page="../include/sideInfo.jsp"/>
  
  
  
<h1><span class="title_manage">
<fmt:message key="confirm_CRF_version" bundle="${resword}"/>
<!--
 <a href="javascript:openDocWindow('https://docs.openclinica.com/3.1/?')"><img src="images/bt_Help_Manage.gif" border="0" alt="<fmt:message key="help" bundle="${resword}"/>" title="<fmt:message key="help" bundle="${resword}"/>"></a>
 -->
</span></h1>



<table cellpadding="2" cellspacing="2" border="0" >

<!-- subject lable here -->
<tr><td>
<fmt:message key="study_subject_ID" bundle="${resword}"/>: &nbsp;</td>
<td><c:out value="${studySubjectLabel}"/></td></tr>

<!-- CRF name  here -->
<tr><td>
<fmt:message key="choose_CRF_version_crf_name" bundle="${resword}"/></td>
<td><c:out value="${crfName}"/></td></tr>
<tr><td>


</td></tr>
</table>
<br><br>
<!-- crf table here -->

<table cellpadding="2" cellspacing="2" border="1"  >
<tr><td>
<table cellpadding="2" cellspacing="2" border="0"  >
<tr>
<td class="table_header_row" style="color: #789EC5;" colspan="3"><fmt:message key="confirm_CRF_version_current_version" bundle="${resword}"/>: '
<c:out value="${crfVersionName}"/>'</td></tr>
<tr>
<td class="table_header_row"><fmt:message key="confirm_CRF_version_field_name" bundle="${resword}"/></td>
<td class="table_header_row"><fmt:message key="confirm_CRF_version_field_oid" bundle="${resword}"/></td>
<td class="table_header_row"><fmt:message key="confirm_CRF_version_field_value" bundle="${resword}"/></td>
</tr>
<c:forEach items="${rows}" var="row" varStatus="rowLoop" begin="0" step="1">

<tr>
<td  class="table_cell_right" > ${row[0]}&nbsp;</td>
<td  class="table_cell"  > ${row[1]}&nbsp;</td>
<td  class="table_cell"  > ${row[2]}&nbsp;</td>
</tr>
</c:forEach>
</table></td>
<td>
<table cellpadding="2" cellspacing="2" border="0"  >
<tr>
<td class="table_header_row" style="color: #789EC5;"  colspan="3"><fmt:message key="confirm_CRF_version_new_version" bundle="${resword}"/>: '
<c:out value="${selectedVersionName}"/>'</td>
</tr>
<tr>
<td class="table_header_row"><fmt:message key="confirm_CRF_version_field_name" bundle="${resword}"/></td>
<td class="table_header_row"><fmt:message key="confirm_CRF_version_field_oid" bundle="${resword}"/></td>
<td class="table_header_row"><fmt:message key="confirm_CRF_version_field_value" bundle="${resword}"/></td>
</tr>

<c:forEach items="${rows}" var="row" varStatus="rowLoop" begin="0" step="1">

<tr>
<td  class="table_cell"  > ${row[3]}&nbsp;</td>
<td  class="table_cell"  > ${row[4]}&nbsp;</td>
<td  class="table_cell"  > ${row[5]}&nbsp;</td>
</tr>
</c:forEach>
</table>
</td></tr></table>

<table border="0" colspan="2"><tr><td>
<form method="POST" action="${pageContext.request.contextPath}/pages/managestudy/changeCRFVersion" >
<input type="hidden" name="newCRFVersionId" value="${selectedVersionId}">

<input type="hidden" name="studySubjectId" value="${studySubjectId}">
<input type="hidden" name="eventDefinitionCRFId" value="${eventDefinitionCRFId}">
<input type="hidden" name="studySubjectLabel" value="${studySubjectLabel}">
<input type="hidden" name="crfversionId" value="${crfversionId}">
<input type="hidden" name="crfId" value="${crfId}">
<input type="hidden" name="crfName" value="${crfName}">
<input type="hidden" name="crfVersionName" value="${crfVersionName}">
<input type="hidden" name="eventCRFId" value="${eventCRFId}">


<input type="submit" name="Submit" value="<fmt:message key="submit" bundle="${resword}"/>" class="button_long">
</form></td><td>

<form id="fr_cancel_button" method="get">
<input type="hidden" name="id" value="<c:out value="${studySubjectId}"/>" />
<input type="button" name="Cancel" id="cancel" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button_long" onClick="confirmCancelAction('ViewStudySubject?id=<c:out value="${studySubjectId}"/>', '${pageContext.request.contextPath}');" >


</form>
</td></tr></table>


<jsp:include page="../include/footer.jsp">
 <jsp:param name="isSpringControllerFooter" value="1" />
    </jsp:include>