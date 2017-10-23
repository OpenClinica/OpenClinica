<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.admin" var="resadmin"/>
<jsp:include page="../include/admin-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray" border="0" align="right" hspace="10"></span></a>

		<fmt:message key="instructions" bundle="${resword}"/>

		<div class="sidebar_tab_content">
        <fmt:message key="enter_the_study_and_protocol" bundle="${resword}"/>
		</div>

		</td>

	</tr>
	<tr id="sidebar_Instructions_closed" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray" border="0" align="right" hspace="10"></span></a>

		<fmt:message key="instructions" bundle="${resword}"/>

		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope='session' id='newStudy' class='org.akaza.openclinica.bean.managestudy.StudyBean'/>
<script type="text/JavaScript" language="JavaScript">
  <!--
<%-- function myCancel() {

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

  }--%>
   //-->
</script>
<h1><span class="title_manage">
<fmt:message key="create_a_new_study_continue" bundle="${resword}"/>
</span></h1>

<form action="CreateStudy" method="post">
<span class="title_Admin"><p><b><fmt:message key="section_b_study_status" bundle="${resword}"/>- <fmt:message key="design_details" bundle="${resword}"/> - <fmt:message key="observational" bundle="${resadmin}"/></b></p></span>
* <fmt:message key="indicates_required_field" bundle="${resword}"/><br>
<input type="hidden" name="action" value="next">
<input type="hidden" name="pageNum" value="3">
<div style="width: 600px">
<!-- These DIVs define shaded box borders -->
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0" cellspacing="0">
  <tr valign="bottom"><td class="formlabel"><fmt:message key="purpose" bundle="${resword}"/>:</td><td>
   <c:set var="purpose1" value="${newStudy.purpose}"/>
  <div class="formfieldXL_BG"><select name="purpose" class="formfieldXL">
   <c:forEach var="purpose" items="${obserPurposeMap}">
       <c:choose>
        <c:when test="${purpose1 == purpose.key}">
         <option value="<c:out value="${purpose.key}"/>" selected><c:out value="${purpose.value}"/>
        </c:when>
        <c:otherwise>
         <option value="<c:out value="${purpose.key}"/>"><c:out value="${purpose.value}"/>
        </c:otherwise>
       </c:choose>
    </c:forEach>
  </select></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="purpose"/></jsp:include></td><td class="formlabel">*</td></tr>

  <tr valign="bottom"><td class="formlabel"><fmt:message key="duration" bundle="${resword}"/>:</td><td>
  <c:choose>
   <c:when test="${newStudy.duration =='longitudinal'}">
    <input type="radio" checked name="duration" value="longitudinal"><fmt:message key="longitudinal" bundle="${resword}"/>:
    <input type="radio" name="duration" value="cross-sectional"><fmt:message key="cross_sectional" bundle="${resword}"/>:
   </c:when>
   <c:otherwise>
    <input type="radio" name="duration" value="longitudinal"><fmt:message key="longitudinal" bundle="${resword}"/>:
    <input type="radio" checked name="duration" value="cross-sectional"><fmt:message key="cross_sectional" bundle="${resword}"/>:
   </c:otherwise>
  </c:choose>
  </td></tr>

  <tr valign="bottom"><td class="formlabel"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#PrimaryId" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#PrimaryId'); return false;">
  <fmt:message key="selection" bundle="${resword}"/>:</td><td>
  <c:set var="selection1" value="${newStudy.selection}"/>
  <div class="formfieldXL_BG"><select name="selection" class="formfieldXL">
   <c:forEach var="selection" items="${selectionMap}">
       <c:choose>
        <c:when test="${selection1 == selection.key}">
         <option value="<c:out value="${selection.key}"/>" selected><c:out value="${selection.value}"/>
        </c:when>
        <c:otherwise>
         <option value="<c:out value="${selection.key}"/>"><c:out value="${selection.value}"/>
        </c:otherwise>
       </c:choose>
    </c:forEach>
  </select></div>
  </td></tr>

  <tr valign="bottom"><td class="formlabel"><fmt:message key="timing" bundle="${resword}"/>:</td><td>
   <c:set var="timing1" value="${newStudy.timing}"/>
   <div class="formfieldXL_BG"><select name="timing" class="formfieldXL">
    <c:forEach var="timing" items="${timingMap}">
       <c:choose>
        <c:when test="${timing1 == timing.key}">
         <option value="<c:out value="${timing.key}"/>" selected><c:out value="${timing.value}"/>
        </c:when>
        <c:otherwise>
         <option value="<c:out value="${timing.key}"/>"><c:out value="${timing.value}"/>
        </c:otherwise>
       </c:choose>
     </c:forEach>
   </select></div>
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
<td><input type="button" name="Cancel" id="cancel" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button_medium" onClick="javascript:myCancel('<fmt:message key="sure_to_cancel" bundle="${resword}"/>');"/></td>
</tr>
</table>
</form>
<br><br>

<jsp:include page="../include/footer.jsp"/>
