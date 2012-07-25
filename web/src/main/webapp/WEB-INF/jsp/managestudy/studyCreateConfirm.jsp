<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
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

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		<div class="sidebar_tab_content">
        <fmt:message key="enter_the_study_and_protocol" bundle="${resword}"/>
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

<jsp:useBean scope='session' id='newStudy' class='org.akaza.openclinica.bean.managestudy.StudyBean'/>
<jsp:useBean scope='request' id='interventions' class='java.util.ArrayList'/>
<h1><span class="title_manage">
<fmt:message key="confirm_study" bundle="${resword}"/>
</span></h1>

<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr bgcolor="#F5F5F5"><td class="table_header_column" colspan="2"><fmt:message key="section_a_study_description" bundle="${resword}"/></td></tr>
  <tr valign="top"><td class="table_header_column"><b><fmt:message key="brief_title" bundle="${resword}"/></b>:</td><td class="table_cell">
  <c:out value="${newStudy.name}"/>
  </td></tr>

  <tr valign="top"><td class="table_header_column"><b><fmt:message key="official_title" bundle="${resword}"/></b>:</td><td class="table_cell">
  <c:out value="${newStudy.officialTitle}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><b><fmt:message key="unique_protocol_ID" bundle="${resword}"/></b>:</td><td class="table_cell">
  <c:out value="${newStudy.identifier}"/>
  </td></tr>

  <tr valign="top"><td class="table_header_column"><b><fmt:message key="secondary_IDs" bundle="${resword}"/></b>:</td><td class="table_cell">
   <c:out value="${newStudy.secondaryIdentifier}"/>&nbsp;
   </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="principal_investigator" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.principalInvestigator}"/>
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="brief_summary" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.summary}"/>
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="protocol_detailed_description" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.protocolDescription}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="sponsor" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.sponsor}"/>
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="collaborators" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.collaborators}"/>&nbsp;
  </td></tr>

  <tr bgcolor="#F5F5F5"><td class="table_header_column" colspan="2"><fmt:message key="section_b_study_status" bundle="${resword}"/></td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="study_phase" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.phase}"/>
  </td></tr>

 <tr valign="top"><td class="table_header_column"><fmt:message key="protocol_type" bundle="${resword}"/>:</td><td class="table_cell">
 <c:out value="${newStudy.protocolType}"/>
 </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="protocol_verification" bundle="${resword}"/>:</td><td class="table_cell">
  <fmt:formatDate value="${newStudy.protocolDateVerification}" pattern="${dteFormat}"/>
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="start_date" bundle="${resword}"/>:</td><td class="table_cell">
   <fmt:formatDate value="${newStudy.datePlannedStart}" pattern="${dteFormat}"/>
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="estimated_completion_date" bundle="${resword}"/>:</td><td class="table_cell">
   <fmt:formatDate value="${newStudy.datePlannedEnd}" pattern="${dteFormat}"/>&nbsp;
  </td></tr>

  <c:choose>
   <c:when test="${newStudy.parentStudyId == 0}">
      <c:set var="key" value="study_system_status"/>
   </c:when>
   <c:otherwise>
       <c:set var="key" value="site_system_status"/>
   </c:otherwise>
  </c:choose>

  <tr valign="top"><td class="table_header_column"><fmt:message key="${key}" bundle="${resword}"/>:</td><td class="table_cell">
   <%-- <c:out value="${newStudy.status.name}"/> --%> <c:out value="Available"/>
   </td></tr>


  <tr valign="top"><td class="table_header_column"><fmt:message key="purpose" bundle="${resword}"/>:</td><td class="table_cell">
   <c:out value="${newStudy.purpose}"/>
  </td></tr>

  <c:choose>
  <c:when test="${newStudy.protocolTypeKey=='interventional'}">

  <tr valign="top"><td class="table_header_column"><fmt:message key="allocation" bundle="${resword}"/>:</td><td class="table_cell">
   <c:out value="${newStudy.allocation}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="masking" bundle="${resword}"/>:</td><td class="table_cell">
    <c:out value="${newStudy.masking}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="control" bundle="${resword}"/>:</td><td class="table_cell">
    <c:out value="${newStudy.control}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="assignment" bundle="${resword}"/>:</td><td class="table_cell">
   <c:out value="${newStudy.assignment}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="endpoint" bundle="${resword}"/>:</td><td class="table_cell">
   <c:out value="${newStudy.endpoint}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="interventions" bundle="${resword}"/>:</td><td class="table_cell">
   <c:forEach var ="intervention" items="${interventions}">
     <fmt:message key="type" bundle="${resword}"/>:<c:out value="${intervention.type}"/> &nbsp;
     <fmt:message key="name" bundle="${resword}"/>: <c:out value="${intervention.name}"/> <br>
   </c:forEach>
  </td></tr>

  </c:when>
  <c:otherwise>
  <tr valign="top"><td class="table_header_column"><fmt:message key="duration" bundle="${resword}"/>:</td><td class="table_cell">
   <c:out value="${newStudy.duration}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="selection" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.selection}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="timing" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.timing}"/>&nbsp;
  </td></tr>

  </c:otherwise>
  </c:choose>

  <tr bgcolor="#F5F5F5"><td class="table_header_column" colspan="2"><fmt:message key="section_c_conditions_and_eligibility" bundle="${resword}"/></td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="conditions" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.conditions}"/>&nbsp;
 </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="keywords" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.keywords}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="eligibility" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.eligibility}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="gender" bundle="${resword}"/>:</td><td class="table_cell">
    <c:out value="${newStudy.gender}"/> &nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="minimum_age" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.ageMin}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="maximum_age" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.ageMax}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="healthy_volunteers_accepted" bundle="${resword}"/>:</td><td class="table_cell">
  <c:choose>
    <c:when test="${newStudy.healthyVolunteerAccepted == true}">
     <fmt:message key="yes" bundle="${resword}"/>
    </c:when>
    <c:otherwise>
     <fmt:message key="no" bundle="${resword}"/>
    </c:otherwise>
   </c:choose>
 </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="expected_total_enrollment" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.expectedTotalEnrollment}"/>&nbsp;
  </td></tr>

  <tr bgcolor="#F5F5F5"><td class="table_header_column" colspan="2"><fmt:message key="section_d_facility_information" bundle="${resword}"/></td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_name" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.facilityName}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_city" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.facilityCity}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_state_province" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.facilityState}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_ZIP" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.facilityZip}"/>&nbsp;
 </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_country" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.facilityCountry}"/>&nbsp;
  </td></tr>

  <!--<tr valign="top"><td class="table_header_column"><fmt:message key="facility_recruitment_status" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.facilityRecruitmentStatus}"/>&nbsp;
 </td></tr>  -->

  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_contact_name" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.facilityContactName}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_contact_degree" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.facilityContactDegree}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_contact_phone" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.facilityContactPhone}"/>&nbsp;
 </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_contact_email" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.facilityContactEmail}"/>&nbsp;
  </td></tr>



   <tr bgcolor="#F5F5F5"><td class="table_header_column" colspan="2"><fmt:message key="section_e_related_information" bundle="${resword}"/></td></tr>


  <tr valign="top"><td class="table_header_column"><fmt:message key="MEDLINE_identifier_references" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.medlineIdentifier}"/>
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="results_reference" bundle="${resword}"/></td><td class="table_cell">
  <c:choose>
    <c:when test="${newStudy.resultsReference == true}">
     <fmt:message key="yes" bundle="${resword}"/>
    </c:when>
    <c:otherwise>
     <fmt:message key="no" bundle="${resword}"/>
    </c:otherwise>
   </c:choose>
 </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="URL_reference" bundle="${resword}"/></td><td class="table_cell">
  <c:out value="${newStudy.url}"/>&nbsp;
 </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="URL_description" bundle="${resword}"/></td><td class="table_cell">
  <c:out value="${newStudy.urlDescription}"/>&nbsp;
  </td></tr>


  <tr bgcolor="#F5F5F5"><td class="table_header_column" colspan="2"><fmt:message key="section_f_study_parameter_configuration" bundle="${resword}"/></td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="collect_subject_date_of_birth" bundle="${resword}"/>:</td>
   <td class="table_cell">
   <c:choose>
   <c:when test="${newStudy.studyParameterConfig.collectDob == '1'}">
   <fmt:message key="yes" bundle="${resword}"/>
   </c:when>
   <c:when test="${newStudy.studyParameterConfig.collectDob == '2'}">
    <fmt:message key="only_year_of_birth" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
    <fmt:message key="not_used" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>

  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="allow_discrepancy_management" bundle="${resword}"/>:</td>
  <td class="table_cell">
   <c:choose>
   <c:when test="${newStudy.studyParameterConfig.discrepancyManagement == 'true'}">
    <fmt:message key="yes" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
    <fmt:message key="no" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="gender_required" bundle="${resword}"/>:</td>
  <td class="table_cell">
   <c:choose>
   <c:when test="${newStudy.studyParameterConfig.genderRequired == 'false'}">
    <fmt:message key="no" bundle="${resword}"/>
   </c:when>
   <c:when test="${newStudy.studyParameterConfig.genderRequired == 'true'}">
    <fmt:message key="yes" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
    <fmt:message key="not_used" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="subject_person_ID_required" bundle="${resword}"/>:</td>
  <td class="table_cell">
   <c:choose>
   <c:when test="${newStudy.studyParameterConfig.subjectPersonIdRequired == 'required'}">
    <fmt:message key="required" bundle="${resword}"/>
   </c:when>
    <c:when test="${newStudy.studyParameterConfig.subjectPersonIdRequired == 'optional'}">
     <fmt:message key="optional" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
    <fmt:message key="not_used" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

   <tr valign="top"><td class="table_header_column"><fmt:message key="how_to_generate_the_study_subject_ID" bundle="${resword}"/>:</td>
   <td class="table_cell">
   <c:choose>
   <c:when test="${newStudy.studyParameterConfig.subjectIdGeneration == 'manual'}">
    <fmt:message key="manual_entry" bundle="${resword}"/>
   </c:when>
    <c:when test="${newStudy.studyParameterConfig.subjectIdGeneration == 'auto editable'}">
    <fmt:message key="auto_generated_and_editable" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
    <fmt:message key="auto_generated_and_non_editable" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

  <!--
   <tr valign="top"><td class="table_header_column">Generate Study Subject ID Automatically With Prefix/Suffix?:</td>
   <td class="table_cell">
   <c:choose>
   <c:when test="${newStudy.studyParameterConfig.subjectIdPrefixSuffix == 'true'}">
    Yes
   </c:when>
   <c:otherwise>
     No
   </c:otherwise>
  </c:choose>
  </td>
  </tr>
 -->

 <tr valign="top"><td class="table_header_column"><fmt:message key="show_person_id_on_crf_header" bundle="${resword}"/>:</td>
   <td class="table_cell">
   <c:choose>
   <c:when test="${newStudy.studyParameterConfig.personIdShownOnCRF == 'true'}">
    <fmt:message key="yes" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
     <fmt:message key="no" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

   <tr valign="top"><td class="table_header_column"><fmt:message key="when_entering_data" bundle="${resword}"/></td>
   <td class="table_cell">
   <c:choose>
   <c:when test="${newStudy.studyParameterConfig.interviewerNameRequired== 'true'}">
   <fmt:message key="yes" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
    <fmt:message key="no" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="interviewer_name_default_as_blank" bundle="${resword}"/></td>
  <td class="table_cell">
   <c:choose>
   <c:when test="${newStudy.studyParameterConfig.interviewerNameDefault== 'blank'}">
    <fmt:message key="blank" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
   <fmt:message key="pre_populated_from_active_user" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="interviewer_name_editable" bundle="${resword}"/></td>
  <td class="table_cell">
   <c:choose>
   <c:when test="${newStudy.studyParameterConfig.interviewerNameEditable== 'true'}">
   <fmt:message key="yes" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
   <fmt:message key="no" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="interviewer_date_required" bundle="${resword}"/></td>
  <td class="table_cell">
   <c:choose>
   <c:when test="${newStudy.studyParameterConfig.interviewDateRequired== 'true'}">
   <fmt:message key="yes" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
    <fmt:message key="no" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="interviewer_date_default_as_blank" bundle="${resword}"/></td>
  <td class="table_cell">
   <c:choose>
   <c:when test="${newStudy.studyParameterConfig.interviewDateDefault== 'blank'}">
    <fmt:message key="blank" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
    <fmt:message key="pre_populated_from_SE" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="interviewer_date_editable" bundle="${resword}"/></td>
  <td class="table_cell">
   <c:choose>
   <c:when test="${newStudy.studyParameterConfig.interviewDateEditable== 'true'}">
   <fmt:message key="yes" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
   <fmt:message key="no" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="secondary_label_viewable" bundle="${resword}"/></td>
  <td class="table_cell">
   <c:choose>
   <c:when test="${newStudy.studyParameterConfig.secondaryLabelViewable== 'true'}">
   <fmt:message key="yes" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
   <fmt:message key="no" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

</table>
</div>
</div></div></div></div></div></div></div></div>

</div>
<br>
<table border="0" cellpadding="0" cellspacing="0">
 <tr>
  <td>
  <form action="CreateStudy" method="post">
   <input type="hidden" name="action" value="submit">
   <input type="submit" name="Submit" value="<fmt:message key="submit_study" bundle="${resword}"/>" class="button_long">
   </form>
  </td>
  <td>
    <form action="CreateStudy" method="POST">
         <input type="hidden" name="action" value="cancel">
         <input id="cancel" type="button" name="Cancel" value="<fmt:message key="cancel_creation" bundle="${resword}"/>" class="button_long" onclick="myCancel('<fmt:message key="sure_to_cancel" bundle="${resword}"/>');">
    </form>
  </td>
 </tr>
</table>

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


							 <b><fmt:message key="confirm_and_submit_study" bundle="${resword}"/></b>


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
