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
<jsp:useBean scope ="request" id="studyTypes" class="java.util.ArrayList"/>
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
<span class="title_Admin"><p><b><fmt:message key="section_d_facility_information" bundle="${resword}"/></b></p></span>
<P>* <fmt:message key="indicates_required_field" bundle="${resword}"/></P>
<form action="UpdateStudy" method="post">
<input type="hidden" name="action" value="next">
<input type="hidden" name="pageNum" value="5">
<!-- These DIVs define shaded box borders -->
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="5">
<tr valign="top"><td class="formlabel"><fmt:message key="facility_name" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
  <input type="text" name="facName" value="<c:out value="${newStudy.facilityName}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="facName"/></jsp:include>
  </td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="facility_city" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
  <input type="text" name="facCity" value="<c:out value="${newStudy.facilityCity}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="facCity"/></jsp:include>
  </td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="facility_state_province" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
  <input type="text" name="facState" value="<c:out value="${newStudy.facilityState}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="facState"/></jsp:include>
  </td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="postal_code" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
  <input type="text" name="facZip" value="<c:out value="${newStudy.facilityZip}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="facZip"/></jsp:include>
  </td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="facility_country" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
  <input type="text" name="facCountry" value="<c:out value="${newStudy.facilityCountry}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="facCountry"/></jsp:include>
  </td></tr>

  <!--<tr valign="top"><td class="formlabel"><fmt:message key="facility_recruitment_status" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
  <c:set var="facStatus" value="${newStudy.facilityRecruitmentStatus}"/>
  <select class="formfieldXL" name="facRecStatus">
   <option value="">-<fmt:message key="select" bundle="${resword}"/>-</option>
   <c:forEach var="recStatus" items="${facRecruitStatusMap}">
       <c:choose>
        <c:when test="${facStatus == recStatus.key}">
         <option value="<c:out value="${recStatus.key}"/>" selected><c:out value="${recStatus.value}"/>
        </c:when>
        <c:otherwise>
         <option value="<c:out value="${recStatus.key}"/>"><c:out value="${recStatus.value}"/>
        </c:otherwise>
       </c:choose>
    </c:forEach>
  </select> </div>
  </td></tr>
  -->
  <tr valign="top"><td class="formlabel"><fmt:message key="facility_contact_name" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
  <input type="text" name="facConName" value="<c:out value="${newStudy.facilityContactName}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="facConName"/></jsp:include>
  </td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="facility_contact_degree" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
  <input type="text" name="facConDegree" value="<c:out value="${newStudy.facilityContactDegree}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="facConDegree"/></jsp:include>
  </td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="facility_contact_phone" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
  <input type="text" name="facConPhone" value="<c:out value="${newStudy.facilityContactPhone}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="facConPhone"/></jsp:include>
  </td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="facility_contact_email" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
  <input type="text" name="facConEmail" value="<c:out value="${newStudy.facilityContactEmail}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="facConEmail"/></jsp:include></td></tr>


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

					       <fmt:message key="conditions_and_eligibility" bundle="${resword}"/>

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


							 <b><fmt:message key="specify_facility" bundle="${resword}"/></b>


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
