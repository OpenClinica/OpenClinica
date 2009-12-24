<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<script language="JavaScript">
<!--

function selectAll() {
    if (document.cl.all.checked) {
	  for (var i=0; i <document.cl.elements.length; i++) {		
		if (document.cl.elements[i].name.indexOf('groupSelected') != -1) {
			document.cl.elements[i].checked = true;
		}
	  }
	} else {
	  for (var i=0; i <document.cl.elements.length; i++) {		
		if (document.cl.elements[i].name.indexOf('groupSelected') != -1) {
			document.cl.elements[i].checked = false;
		}
	  }
	}
}
function notSelectAll() {
	if (!this.checked){
		document.cl.all.checked = false;
    }

}
//-->
</script>

<jsp:include page="../include/extract-header.jsp"/>


<jsp:include page="../include/sidebar.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope="request" id="eventlist" class="java.util.HashMap"/>

<h1><span class="title_manage"><fmt:message key="create_dataset" bundle="${resword}"/>: <fmt:message key="select_group_attributes" bundle="${resword}"/></span></h1>

<P><jsp:include page="../showInfo.jsp"/></P>

<jsp:include page="createDatasetBoxes.jsp" flush="true">
<jsp:param name="selectStudyEvents" value="1"/>
</jsp:include>

<P><jsp:include page="../showMessage.jsp"/></P>

<fmt:message key="instructions_extract_select_CRF_and_group" bundle="${resword}"/>

<form action="CreateDataset" method="post" name="cl">
<input type="hidden" name="action" value="beginsubmit"/>
<input type="hidden" name="crfId" value="0">
<input type="hidden" name="groupAttr" value="1">

   <%-- <p>
    <c:choose>
     <c:when test="${newDataset.showSubjectGroupInformation}">
       <input type="checkbox" checked name="group_information" value="yes">  
     </c:when>
     <c:otherwise>
       <input type="checkbox" name="group_information" value="yes">
     </c:otherwise>
    </c:choose>
    Show All Group Information for Subjects
   </p> --%>
   
   <p><input type="checkbox" name="all" value="1" 
	onClick="javascript:selectAll();"><fmt:message key="select_all_groups" bundle="${resword}"/></p>
	

   <%-- put in a table with metadata, tbh --%>
<div style="width: 100%">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="textbox_center" align="center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tr>
<td>&nbsp;</td>
<td class="table_header_column_top"><fmt:message key="subject_group_class" bundle="${resword}"/></td>
<td class="table_header_column_top"><fmt:message key="subject_group_type" bundle="${resword}"/></td>
<td class="table_header_column_top"><fmt:message key="subject_groups" bundle="${resword}"/></td>
<td class="table_header_column_top"><fmt:message key="status" bundle="${resword}"/></td>
<td class="table_header_column_top"><fmt:message key="subject_assignment" bundle="${resword}"/></td>
</tr>
   <c:forEach var='sgclass' items='${allSelectedGroups}'>
   
   <tr>
   
   <td class="table_cell">
   <c:choose>
   	<c:when test="${sgclass.selected}">
   		<input type="checkbox" checked name="groupSelected<c:out value='${sgclass.id}'/>" value="yes">
   	</c:when>
   	<c:otherwise>
   		<input type="checkbox" name="groupSelected<c:out value='${sgclass.id}'/>" value="yes">
   	</c:otherwise>
   	</c:choose>
	</td>
	
	<td class="table_cell"><c:out value="${sgclass.name}"/></td>
	
	<td class="table_cell"><c:out value="${sgclass.groupClassTypeName}"/></td>
	
	<td class="table_cell">
	<c:forEach var='group' items='${sgclass.studyGroups}' varStatus='status'>
	  <c:choose>
		<c:when test="${status.last}">
			<c:out value="${group.name}"/>&nbsp;
		</c:when>
		<c:otherwise>
			<c:out value="${group.name}"/>,&nbsp;
		</c:otherwise>
	  </c:choose>
	</c:forEach>&nbsp;</td>
	
	<td class="table_cell"><c:out value="${sgclass.status.name}"/></td>
	
	<td class="table_cell"><c:out value="${sgclass.subjectAssignment}"/></td>
	
	</tr>
   
   </c:forEach>
   </table>
</div>

</div></div></div></div></div></div></div></div>
</div>
   
 
<table border="0" cellpadding="0" cellspacing="0" >
  <tr>
   <td><input type="submit" name="save" value="<fmt:message key="save_and_add_more_items" bundle="${resword}"/>" class="button_xlong"/></td>
   <td><input type="submit" name="saveContinue" value="<fmt:message key="save_and_define_scope" bundle="${resword}"/>" class="button_xlong"/></td>
   <td><input type="button" onclick="confirmCancel('ViewDatasets');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/></td>   
  </tr>
</table>
</form>

<jsp:include page="../include/footer.jsp"/>