<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>

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

<jsp:useBean scope='session' id='newStudy' class='core.org.akaza.openclinica.domain.datamap.Study'/>
<script type="text/JavaScript" language="JavaScript">
  <!--
 function myCancel() {

    cancelButton=document.getElementById('cancel');
    if ( cancelButton != null) {
      if(confirm('<fmt:message key="sure_to_cancel" bundle="${resword}"/>')) {
        window.location.href="ListStudy";
       return true;
      } else {
        return false;
       }
     }
     return true;

  }
   //-->
</script>
<h1><span class="title_manage">
<fmt:message key="update_study_details_continue" bundle="${resword}"/>
</span></h1>

<form action="UpdateStudy" method="post">
<span class="title_Admin"><p><b><fmt:message key="section_e_related_information" bundle="${resword}"/></b></p></span>
* <fmt:message key="indicates_required_field" bundle="${resword}"/><br>
<input type="hidden" name="action" value="next">
<input type="hidden" name="pageNum" value="6">
<!-- These DIVs define shaded box borders -->
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0">

  <tr valign="top"><td class="formlabel"><fmt:message key="MEDLINE_identifier" bundle="${resword}"/>:</td><td>
  <div class="formfieldXL_BG"><input type="text" name="medlineIdentifier" value="<c:out value="${newStudy.medlineIdentifier}"/>" class="formfieldXL"></div>
    <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="medlineIdentifier"/></jsp:include>
  </td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="results_reference" bundle="${resword}"/>:</td><td>
   <div class="formfieldS_BG"><select name="resultsReference" class="formfieldS">
    <c:choose>
     <c:when test="${newStudy.resultsReference == true}">
      <option value="1" selected><fmt:message key="yes" bundle="${resword}"/></option>
      <option value="0"><fmt:message key="no" bundle="${resword}"/></option>
     </c:when>
     <c:otherwise>
      <option value="1"><fmt:message key="yes" bundle="${resword}"/></option>
      <option value="0" selected><fmt:message key="no" bundle="${resword}"/></option>
     </c:otherwise>
    </c:choose>
   </select></div>
  </td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="URL_reference" bundle="${resword}"/></td><td>
  <div class="formfieldXL_BG"><input type="text" name="url" value="<c:out value="${newStudy.url}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="url"/></jsp:include>
  </td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="URL_description" bundle="${resword}"/></td><td>
  <div class="formfieldXL_BG"><input type="text" name="urlDescription" value="<c:out value="${newStudy.urlDescription}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="urlDescription"/></jsp:include>
  </td></tr>

</table>
</div>
</div></div></div></div></div></div></div></div>

</div>
<table border="0" cellpadding="0" cellspacing="0">
<tr>
<td>
 <input type="submit" name="Submit" value="<fmt:message key="continue" bundle="${resword}"/>" class="button_medium">
</td>
<td><input type="button" name="Cancel" id="cancel" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button_medium" onClick="javascript:myCancel();"/></td>
</tr>
</table>
</form>
<br><br>
<jsp:include page="../include/footer.jsp"/>
