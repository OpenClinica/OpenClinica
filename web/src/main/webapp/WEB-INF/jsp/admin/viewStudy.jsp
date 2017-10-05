<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>


<c:choose>
 <c:when test="${isAdminServlet == 'admin' && userBean.sysAdmin}">
   <c:import url="../include/admin-header.jsp"/>
 </c:when>
 <c:otherwise>
  <c:choose>
   <c:when test="${userRole.manageStudy}">
    <c:import url="../include/managestudy-header.jsp"/>
   </c:when>
   <c:otherwise>
    <c:import url="../include/home-header.jsp"/>
   </c:otherwise>
  </c:choose>
 </c:otherwise>
</c:choose>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
    <td class="sidebar_tab">

    <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray"></span></a>

    <fmt:message key="instructions" bundle="${resword}"/>

    <div class="sidebar_tab_content">

    </div>

    </td>

  </tr>
  <tr id="sidebar_Instructions_closed" style="display: all">
    <td class="sidebar_tab">

    <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray"></span></a>

    <fmt:message key="instructions" bundle="${resword}"/>

    </td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope='request' id='studyToView' class='org.akaza.openclinica.bean.managestudy.StudyBean'/>
<h1><div class="title_manage"><fmt:message key="view_study_metadata" bundle="${resword}"/>
</div></h1></br>

<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr bgcolor="#F5F5F5"><td class="table_header_column" colspan="2"><b><fmt:message key="section" bundle="${resword}"/> A: <fmt:message key="study_description" bundle="${resword}"/></b></td></tr>
  
  <tr valign="top"><td class="table_header_column"><fmt:message key="brief_title" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.name}"/>
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="unique_protocol_ID" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.identifier}"/>
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="principal_investigator" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.principalInvestigator}"/>
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="brief_summary" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.summary}"/>
  </td></tr>

  <tr bgcolor="#F5F5F5"><td class="table_header_column" colspan="2"><b><fmt:message key="section" bundle="${resword}"/> B: <fmt:message key="study_status_and_design" bundle="${resword}"/></b></td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="study_phase" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.phase}"/>
  </td></tr>

 <tr valign="top"><td class="table_header_column"><fmt:message key="protocol_type" bundle="${resword}"/>:</td><td class="table_cell">
 <c:out value="${studyToView.protocolType}"/>
 </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="protocol_verification" bundle="${resword}"/>:</td><td class="table_cell">
  <fmt:formatDate value="${studyToView.protocolDateVerification}" pattern="${dteFormat}"/>
  </td></tr>

  

  <tr valign="top"><td class="table_header_column"><fmt:message key="start_date" bundle="${resword}"/>:</td><td class="table_cell">
   <fmt:formatDate value="${studyToView.datePlannedStart}" pattern="${dteFormat}"/>
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="estimated_completion_date" bundle="${resword}"/>:</td><td class="table_cell">
   <fmt:formatDate value="${studyToView.datePlannedEnd}" pattern="${dteFormat}"/>&nbsp;
  </td></tr>

  <c:choose>
   <c:when test="${studyToView.parentStudyId == 0}">
      <c:set var="key" value="study_system_status"/>
   </c:when>
   <c:otherwise>
       <c:set var="key" value="site_system_status"/>
   </c:otherwise>
  </c:choose>

  <tr valign="top"><td class="table_header_column"><fmt:message key="${key}" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.status.name}"/>
   </td></tr>

  <c:choose>
  <c:when test="${studyToView.protocolTypeKey=='interventional'}">


  <tr valign="top"><td class="table_header_column"><fmt:message key="assignment" bundle="${resword}"/>:</td><td class="table_cell">
   <c:out value="${studyToView.assignment}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="endpoint" bundle="${resword}"/>:</td><td class="table_cell">
   <c:out value="${studyToView.endpoint}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="interventions" bundle="${resword}"/>:</td><td class="table_cell">
    <c:out value="${studyToView.interventions}"/>&nbsp;


  </td></tr>

  </c:when>
  </c:choose>

  <tr bgcolor="#F5F5F5"><td class="table_header_column" colspan="2"><b><fmt:message key="section" bundle="${resword}"/> C: <fmt:message key="conditions_and_eligibility" bundle="${resword}"/></b></td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="expected_total_enrollment" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.expectedTotalEnrollment}"/>&nbsp;
  </td></tr>

  <tr bgcolor="#F5F5F5"><td class="table_header_column" colspan="2"><b><fmt:message key="section" bundle="${resword}"/> D: <fmt:message key="facility_information" bundle="${resword}"/></b></td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_city" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.facilityCity}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_state_province" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.facilityState}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_ZIP" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.facilityZip}"/>&nbsp;
 </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_country" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.facilityCountry}"/>&nbsp;
  </td></tr>

  <!--<tr valign="top"><td class="table_header_column"><fmt:message key="facility_recruitment_status" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.facilityRecruitmentStatus}"/>&nbsp;
 </td></tr> -->

  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_contact_name" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.facilityContactName}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_contact_phone" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.facilityContactPhone}"/>&nbsp;
 </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_contact_email" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.facilityContactEmail}"/>&nbsp;
  </td></tr>

   <tr bgcolor="#F5F5F5"><td class="table_header_column" colspan="2" ><b><fmt:message key="section" bundle="${resword}"/> E: <fmt:message key="related_infomation" bundle="${resword}"/></b></td></tr>

  <tr bgcolor="#F5F5F5"><td class="table_header_column" colspan="2"><b><fmt:message key="section" bundle="${resword}"/> F: <fmt:message key="study_parameter_configuration" bundle="${resword}"/></b></td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="collect_subject" bundle="${resword}"/></td>
   <td class="table_cell">

   <c:choose>
   <c:when test="${(studyToView.studyParameterConfig.collectDob == '1')}">
  <fmt:message key="yes" bundle="${resword}"/>
   </c:when>
   <c:when test="${studyToView.studyParameterConfig.collectDob == '2'}">
       <fmt:message key="only_year_of_birth" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
    <fmt:message key="not_used" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>

  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="gender_required" bundle="${resword}"/></td>
  <td class="table_cell">
   <c:choose>
   <c:when test="${studyToView.studyParameterConfig.genderRequired == 'false'}">
   <fmt:message key="no" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
  <fmt:message key="yes" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="subject_person_ID_required" bundle="${resword}"/></td>
  <td class="table_cell">
   <c:choose>
   <c:when test="${(studyToView.studyParameterConfig.subjectPersonIdRequired == 'required')
        or ((studyToView.studyParameterConfig.subjectPersonIdRequired == 'always'))}">
    <fmt:message key="required" bundle="${resword}"/>
   </c:when>
    <c:when test="${studyToView.studyParameterConfig.subjectPersonIdRequired == 'optional'}">
    <fmt:message key="optional" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
       <c:out value="${studyToView.studyParameterConfig.subjectPersonIdRequired}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

  <!--
   <tr valign="top"><td class="table_header_column">Generate Study Subject ID Automatically With Prefix/Suffix?:</td>
   <td class="table_cell">
   <c:choose>
   <c:when test="${studyToView.studyParameterConfig.subjectIdPrefixSuffix == 'true'}">
    Yes
   </c:when>
   <c:otherwise>
     No
   </c:otherwise>
  </c:choose>
  </td>
  </tr>
 -->

</table>

</div>
</div></div></div></div></div></div></div></div>

</div>

<jsp:include page="../include/footer.jsp"/>
