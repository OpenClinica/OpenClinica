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

<jsp:useBean scope='session' id='newStudy' class='org.akaza.openclinica.bean.managestudy.StudyBean'/>
<jsp:useBean scope="request" id="obserPurposeMap" class ="java.util.HashMap"/>
<jsp:useBean scope="request" id="durationMap" class ="java.util.HashMap"/>
<jsp:useBean scope="request" id="selectionMap" class ="java.util.HashMap"/>
<jsp:useBean scope="request" id="timingMap" class ="java.util.HashMap"/>
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
<span class="title_Admin"><p><b><fmt:message key="section_b_study_status" bundle="${resword}"/>- <fmt:message key="design_details" bundle="${resword}"/> - <fmt:message key="observational" bundle="${resword}"/></b></p></span>
* <fmt:message key="indicates_required_field" bundle="${resword}"/><br>
<input type="hidden" name="action" value="next">
<input type="hidden" name="pageNum" value="3">
<!-- These DIVs define shaded box borders -->
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0">
  <tr valign="bottom"><td class="formlabel"><fmt:message key="purpose" bundle="${resword}"/>:</td><td>
   <c:set var="purpose1" value="${newStudy.purpose}"/>
  <div class="formfieldXL_BG"><select name="purpose" class="formfieldXL">
   <c:forEach var="purpose" items="${obserPurposeMap}">
   <c:set var="purposekey">
   <fmt:message key="${purpose.key}" bundle="${resadmin}"/>
   </c:set>

       <c:choose>
        <c:when test="${purpose1 == purposekey}">
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
   <c:set var="longitudinal">
    <fmt:message key="longitudinal" bundle="${resadmin}"/>
   </c:set>

  <c:choose>
   <c:when test="${newStudy.duration ==longitudinal}">
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
  <div class="formfieldXL_BG">
  <select name="selection" class="formfieldXL">
   <c:forEach var="selection" items="${selectionMap}">
   <c:set var="selectionkey">
    <fmt:message key="${selection.key}" bundle="${resadmin}"/>
   </c:set>

       <c:choose>
        <c:when test="${selection1 == selectionkey}">
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
   <div class="formfieldXL_BG">
   <select name="timing" class="formfieldXL">
    <c:forEach var="timing" items="${timingMap}">
   <c:set var="timingkey">
    <fmt:message key="${timing.key}" bundle="${resadmin}"/>
   </c:set>
       <c:choose>
        <c:when test="${timing1 == timingkey}">
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
<td><input type="button" name="Cancel" id="cancel" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button_medium" onClick="javascript:myCancel();"/></td>
</tr>
</table>
</form>
<br><br>

<!-- EXPANDING WORKFLOW BOX -->

<table border="0" cellpadding="0" cellspacing="0" style="position: relative; left: -14px;">
	<tr>
		<td id="sidebar_Workflow_closed" style="display: none">
		<a href="javascript:leftnavExpand('sidebar_Workflow_closed'); leftnavExpand('sidebar_Workflow_open');"><img src="images/<fmt:message key="image_dir" bundle="${resformat}"/>/tab_Workflow_closed.gif" border="0"></a>
	</td>
	<td id="sidebar_Workflow_open" style="display: all">
	<table border="0" cellpadding="0" cellspacing="0" class="workflowBox">
		<tr>
			<td class="workflowBox_T" valign="top">
			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td class="workflow_tab">
					<a href="javascript:leftnavExpand('sidebar_Workflow_closed'); leftnavExpand('sidebar_Workflow_open');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

					<b><fmt:message key="workflow" bundle="${resword}"/></b>

					</td>
				</tr>
			</table>
			</td>
			<td class="workflowBox_T" align="right" valign="top"><img src="images/workflowBox_TR.gif"></td>
		</tr>
		<tr>
			<td colspan="2" class="workflowbox_B">
			<div class="box_R"><div class="box_B"><div class="box_BR">
				<div class="workflowBox_center">


		<!-- Workflow items -->

				<table border="0" cellpadding="0" cellspacing="0">
					<tr>
						<td>

				<!-- These DIVs define shaded box borders -->
						<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

							<div class="textbox_center" align="center">

							<span class="title_admin">

							<fmt:message key="study_title_description" bundle="${resword}"/><br><br>


							</span>

							</div>
						</div></div></div></div></div></div></div></div>

						</td>
						<td><img src="images/arrow.gif"></td>
						<td>

				<!-- These DIVs define shaded box borders -->
						<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

							<div class="textbox_center" align="center">

							<span class="title_admin">


							<b><fmt:message key="status_and_design" bundle="${resword}"/></b>


							</span>

							</div>
						</div></div></div></div></div></div></div></div>

						</td>
						<td><img src="images/arrow.gif"></td>
						<td>

				<!-- These DIVs define shaded box borders -->
						<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

							<div class="textbox_center" align="center">

							<span class="title_admin">

					        <fmt:message key="conditions_and_eligibility" bundle="${resword}"/><br><br>

							</span>

							</div>
						</div></div></div></div></div></div></div></div>

						</td>
						<td><img src="images/arrow.gif"></td>
						<td>

				<!-- These DIVs define shaded box borders -->
						<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

							<div class="textbox_center" align="center">

							<span class="title_admin">


							 <fmt:message key="specify_facility" bundle="${resword}"/>


							</span>

							</div>
						</div></div></div></div></div></div></div></div>

						</td>
						<td><img src="images/arrow.gif"></td>
						<td>

				<!-- These DIVs define shaded box borders -->
						<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

							<div class="textbox_center" align="center">

							<span class="title_admin">


							 <fmt:message key="other_related_information" bundle="${resword}"/>


							</span>

							</div>
						</div></div></div></div></div></div></div></div>

						</td>
						<td><img src="images/arrow.gif"></td>
						<td>

				       <!-- These DIVs define shaded box borders -->
						<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

							<div class="textbox_center" align="center">

							<span class="title_admin">

					         <fmt:message key="study_parameter_configuration" bundle="${resword}"/>

							</span>

							</div>
						</div></div></div></div></div></div></div></div>

						</td>
						<td><img src="images/arrow.gif"></td>
						<td>

				<!-- These DIVs define shaded box borders -->
						<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

							<div class="textbox_center" align="center">

							<span class="title_admin">


							 <fmt:message key="confirm_and_submit_study" bundle="${resword}"/>


							</span>

							</div>
						</div></div></div></div></div></div></div></div>

						</td>

					</tr>
				</table>


		<!-- end Workflow items -->

				</div>
			</div></div></div>
			</td>
		</tr>
	</table>
	</td>
   </tr>
</table>

<!-- END WORKFLOW BOX -->
<jsp:include page="../include/footer.jsp"/>
