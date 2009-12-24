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

<jsp:useBean scope='session' id='newStudy' class='org.akaza.openclinica.bean.managestudy.StudyBean'/>
<jsp:useBean scope ="request" id="studyPhaseMap" class="java.util.HashMap"/>
<jsp:useBean scope="request" id="statuses" class="java.util.ArrayList"/>
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
<h1><span class="title_manage"><fmt:message key="update_study_details_continue" bundle="${resword}"/></span></h1>

<span class="title_Admin"><p><b><fmt:message key="section_c_conditions_and_eligibility" bundle="${resword}"/></b></p></span>
<P>* <fmt:message key="indicates_required_field" bundle="${resword}"/></P>
<form action="UpdateStudy" method="post">
<input type="hidden" name="action" value="next">
<input type="hidden" name="pageNum" value="4">
<!-- These DIVs define shaded box borders -->
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0">

  <tr valign="top"><td class="formlabel"><fmt:message key="conditions" bundle="${resword}"/>:</td><td>
  <div class="formfieldXL_BG"><input type="text" name="conditions" value="<c:out value="${newStudy.conditions}"/>" class="formfieldXL">
  </div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="conditions"/></jsp:include>
  </td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="keywords" bundle="${resword}"/>:<br>(<fmt:message key="separate_by_commas" bundle="${resword}"/>)</td><td>
   <div class="formtextareaXL4_BG"><textarea name="keywords" rows="4" cols="50"  class="formtextareaXL4"><c:out value="${newStudy.keywords}"/></textarea>
   </div>
    <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="keywords"/></jsp:include>
  </td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="eligibility_criteria" bundle="${resword}"/>:</td><td>
   <div class="formtextareaXL4_BG"><textarea name="eligibility" rows="4" cols="50" class="formtextareaXL4"><c:out value="${newStudy.eligibility}"/></textarea>
   </div>
   <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="eligibility"/></jsp:include>
  </td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="gender" bundle="${resword}"/>:</td><td>
  <div class="formfieldXL_BG"><select name="gender" class="formfieldXL">
  <c:set var="female">
  	<fmt:message key="female" bundle="${resword}"/>
  </c:set>
  <c:set var="male">
  	<fmt:message key="male" bundle="${resword}"/>
  </c:set>

  <c:choose>
   <c:when test="${newStudy.gender == female}">
    <option value="both"><fmt:message key="both" bundle="${resword}"/></option>
    <option value="male"><fmt:message key="male" bundle="${resword}"/></option>
    <option value="female" selected><fmt:message key="female" bundle="${resword}"/></option>
   </c:when>
   <c:when test="${newStudy.gender == male}">
    <option value="both"><fmt:message key="both" bundle="${resword}"/></option>
    <option value="male" selected><fmt:message key="male" bundle="${resword}"/></option>
    <option value="female"><fmt:message key="female" bundle="${resword}"/></option>
   </c:when>
   <c:otherwise>
    <option value="both" selected><c:out value="${newStudy.gender}"/></option>
    <option value="male"><fmt:message key="male" bundle="${resword}"/></option>
    <option value="female"><fmt:message key="female" bundle="${resword}"/></option>
   </c:otherwise>

  </c:choose>
  </select></div>
  </td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="minimum_age" bundle="${resword}"/>:</td><td>
  <div class="formfieldXL_BG"><input type="text" name="ageMin" value="<c:out value="${newStudy.ageMin}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="ageMin"/></jsp:include></td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="maximum_age" bundle="${resword}"/>:</td><td>
  <div class="formfieldXL_BG"><input type="text" name="ageMax" value="<c:out value="${newStudy.ageMax}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="ageMax"/></jsp:include></td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="healthy_volunteers_accepted" bundle="${resword}"/>:</td><td>
   <div class="formfieldXL_BG"><select name="healthyVolunteerAccepted" class="formfieldXL">
    <c:choose>
     <c:when test="${newStudy.healthyVolunteerAccepted == true}">
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


  <tr valign="top"><td class="formlabel"><fmt:message key="expected_total_enrollment" bundle="${resword}"/>:</td><td>
  <div class="formfieldXL_BG"><input type="text" name="expectedTotalEnrollment" value="<c:out value="${newStudy.expectedTotalEnrollment}"/>" class="formfieldXL"">
   </div><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="expectedTotalEnrollment"/></jsp:include>
  </td><td class="formlabel">*</td></tr>

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


							<fmt:message key="status_and_design" bundle="${resword}"/>


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

					        <b><fmt:message key="conditions_and_eligibility" bundle="${resword}"/></b>

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


