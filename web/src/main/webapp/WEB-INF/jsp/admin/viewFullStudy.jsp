<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<c:choose>
	<c:when test="${userRole.role.id > 3}">
		<jsp:include page="../include/home-header.jsp"/>
	</c:when>
	<c:otherwise>
		<jsp:include page="../include/admin-header.jsp"/>
	</c:otherwise>
</c:choose>
<%--<jsp:include page="../include/admin-header.jsp"/>--%>


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
<jsp:useBean scope='request' id='studyToView' class='org.akaza.openclinica.bean.managestudy.StudyBean'/>
<jsp:useBean scope='request' id='sitesToView' class='java.util.ArrayList'/>
<jsp:useBean scope='request' id='userRolesToView' class='java.util.ArrayList'/>
<jsp:useBean scope='request' id='subjectsToView' class='java.util.ArrayList'/>
<jsp:useBean scope='request' id='definitionsToView' class='java.util.ArrayList'/>



<script language="JavaScript">

         function leftnavExpand(strLeftNavRowElementName){
	       var objLeftNavRowElement;

           objLeftNavRowElement = MM_findObj(strLeftNavRowElementName);
           if (objLeftNavRowElement != null) {
             if (objLeftNavRowElement.style) { objLeftNavRowElement = objLeftNavRowElement.style; }
	           objLeftNavRowElement.display = (objLeftNavRowElement.display == "none" ) ? "" : "none";
               objExCl = MM_findObj("excl_"+strLeftNavRowElementName);
               if(objLeftNavRowElement.display == "none"){
                   objExCl.src = "images/bt_Expand.gif";
               }else{
                   objExCl.src = "images/bt_Collapse.gif";
               }
	         }
           }


 </script>
<h1><span class="title_manage"><c:out value="${studyToView.name}"/></span></h1>


<strong><fmt:message key="download_study_meta" bundle="${restext}"/>
<a href="javascript:openDocWindow('DownloadStudyMetadata?studyId=<c:out value="${studyToView.id}"/>');">
<fmt:message key="here" bundle="${restext}"/></a>.
<fmt:message key="opening_finished_may_save" bundle="${restext}"/>
 </strong>
<fmt:message key="get_subject_oid_from_matrix_show_more" bundle="${restext}"/>
<br><br>
<a href="javascript:leftnavExpand('overview');">
    <img id="excl_overview" src="images/bt_Collapse.gif" border="0">
    <span class="table_title_Admin">
    <fmt:message key="overview" bundle="${resword}"/></span></a>
<div id="overview" style="">
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr valign="top"><td class="table_header_column"><fmt:message key="name" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.name}"/>
  </td></tr>
  <tr valign="top"><td class="table_header_column"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#PrimaryId" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#PrimaryId'); return false;"><fmt:message key="unique_protocol_ID" bundle="${resword}"/></a>:</td><td class="table_cell">
  <c:out value="${studyToView.identifier}"/>
  </td></tr>
   <tr valign="top"><td class="table_header_column"><fmt:message key="OID" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.oid}"/>
  </td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="principal_investigator" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.principalInvestigator}"/>
  </td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="brief_summary" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.summary}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="owner" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.owner.name}"/>
  </td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="date_created" bundle="${resword}"/>:</td><td class="table_cell">
  <fmt:formatDate value="${studyToView.createdDate}" pattern="${dteFormat}"/>
  </td></tr>
 </table>

 </div>
</div></div></div></div></div></div></div></div>

</div>
</div>
<br>

<a href="javascript:leftnavExpand('sectiona');">
    <img id="excl_sectiona" src="images/bt_Expand.gif" border="0">
    <span class="table_title_Admin"><fmt:message key="view_study_details" bundle="${resword}"/>: [<fmt:message key="section" bundle="${resword}"/> A: <fmt:message key="study_description" bundle="${resword}"/>]</span></a>
<div id="sectiona" style="display:none ">
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr valign="top"><td class="table_header_column"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#BriefTitle" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#BriefTitle'); return false;"><fmt:message key="brief_title" bundle="${resword}"/></a>:</td><td class="table_cell">
  <c:out value="${studyToView.name}"/>
  </td></tr>

  <tr valign="top"><td class="table_header_column"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#OfficialTitle" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#OfficialTitle'); return false;"><fmt:message key="official_title" bundle="${resword}"/></a>:</td><td class="table_cell">
  <c:out value="${studyToView.officialTitle}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#PrimaryId" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#PrimaryId'); return false;"><fmt:message key="unique_protocol_ID" bundle="${resword}"/></a>:</td><td class="table_cell">
  <c:out value="${studyToView.identifier}"/>
  </td></tr>

  <tr valign="top"><td class="table_header_column"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#SecondaryIds" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#SecondaryIds'); return false;"><fmt:message key="secondary_IDs" bundle="${resword}"/></a>:</td><td class="table_cell">
   <c:out value="${studyToView.secondaryIdentifier}"/>&nbsp;
   </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="principal_investigator" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.principalInvestigator}"/>
  </td></tr>

  <tr valign="top"><td class="table_header_column"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#BriefSummary" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#BriefSummary'); return false;"><fmt:message key="brief_summary" bundle="${resword}"/></a>:</td><td class="table_cell">
  <c:out value="${studyToView.summary}"/>
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="protocol_detailed_description" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.protocolDescription}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#LeadSponsor" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#LeadSponsor'); return false;"><fmt:message key="sponsor" bundle="${resword}"/></a>:</td><td class="table_cell">
  <c:out value="${studyToView.sponsor}"/>
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="collaborators" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.collaborators}"/>&nbsp;
  </td></tr>
  </table>
</div>
</div></div></div></div></div></div></div></div>
</div>
</div>
<br>


<a href="javascript:leftnavExpand('sectionb');">
    <img id="excl_sectionb" src="images/bt_Expand.gif" border="0">
    <span class="table_title_Admin"><fmt:message key="view_study_details" bundle="${resword}"/>: [<fmt:message key="section" bundle="${resword}"/> B: <fmt:message key="study_status_and_design" bundle="${resword}"/>]</span></a>
<div id="sectionb" style="display:none ">
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
 <tr valign="top"><td class="table_header_column"><fmt:message key="study_phase" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.phase}"/>
  </td></tr>

 <tr valign="top"><td class="table_header_column"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#StudyType" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#StudyType'); return false;"><fmt:message key="protocol_type" bundle="${resword}"/></a>:</td><td class="table_cell">
 <c:out value="${studyToView.protocolType}"/>
 </td></tr>

  <tr valign="top"><td class="table_header_column"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#VerificationDate" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#VerificationDate'); return false;"><fmt:message key="protocol_verification" bundle="${resword}"/></a>:</td><td class="table_cell">
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

  <c:choose>
    <c:when test="${studyToView.status.locked}">
        <strong><c:out value="${studyToView.status.name}"/></strong>
    </c:when>
    <c:otherwise>
        <c:out value="${studyToView.status.name}"/>
    </c:otherwise>
  </c:choose>
   </td></tr>


  <tr valign="top"><td class="table_header_column"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#IntPurpose" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#IntPurpose'); return false;"><fmt:message key="purpose" bundle="${resword}"/></a>:</td><td class="table_cell">
   <c:out value="${studyToView.purpose}"/>
  </td></tr>

  <c:choose>
  <c:when test="${studyToView.protocolTypeKey=='interventional'}">

  <tr valign="top"><td class="table_header_column">
  	<a href="http://prsinfo.clinicaltrials.gov/definitions.html#IntAllocation" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#IntAllocation'); return false;"><fmt:message key="allocation" bundle="${resword}"/></a>
	:</td><td class="table_cell">
   <c:out value="${studyToView.allocation}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column">
  	<a href="http://prsinfo.clinicaltrials.gov/definitions.html#IntMasking" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#IntMasking'); return false;"><fmt:message key="masking" bundle="${resword}"/></a>:</td><td class="table_cell">
    <c:out value="${studyToView.masking}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="control" bundle="${resword}"/>:</td><td class="table_cell">
    <c:out value="${studyToView.control}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column">
  <%--<fmt:message key="assignment" bundle="${resword}"/>--%>
	<a href="http://prsinfo.clinicaltrials.gov/definitions.html#IntDesign" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#IntDesign'); return false;">
           <fmt:message key="intervention_model" bundle="${resword}"/></a>:</td><td class="table_cell">
   <c:out value="${studyToView.assignment}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><%--<fmt:message key="endpoint" bundle="${resword}"/>--%>
	<a href="http://prsinfo.clinicaltrials.gov/definitions.html#IntEndpoints" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#IntEndpoints'); return false;">
          <fmt:message key="study_classification" bundle="${resword}"/></a>:</td><td class="table_cell">
   <c:out value="${studyToView.endpoint}"/>&nbsp;
  </td></tr>

  <tr valign="top">
    <td class="table_header_column">
      <fmt:message key="interventions" bundle="${resword}"/>:
    </td>
    <td class="table_cell">
      <c:out value="${studyToView.interventions}"/>&nbsp;
    </td>
  </tr>

  </c:when>
  <c:otherwise>
  <tr valign="top"><td class="table_header_column"><fmt:message key="duration" bundle="${resword}"/>:</td><td class="table_cell">
   <c:out value="${studyToView.duration}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#EligibilitySamplingMethod" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#EligibilitySamplingMethod'); return false;">
          <fmt:message key="selection" bundle="${resword}"/></a>:</td><td class="table_cell">
  <c:out value="${studyToView.selection}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="timing" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.timing}"/>&nbsp;
  </td></tr>

  </c:otherwise>
  </c:choose>
</table>
</div>
</div></div></div></div></div></div></div></div>
</div>
</div>
<br>

<a href="javascript:leftnavExpand('sectionc');">
    <img id="excl_sectionc" src="images/bt_Expand.gif" border="0"> <span class="table_title_Admin"><fmt:message key="view_study_details" bundle="${resword}"/>: [<fmt:message key="section" bundle="${resword}"/> C: <fmt:message key="conditions_and_eligibility" bundle="${resword}"/>]</span></a>
<div id="sectionc" style="display:none ">
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tr valign="top"><td class="table_header_column"><fmt:message key="conditions" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.conditions}"/>&nbsp;
 </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="keywords" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.keywords}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="eligibility" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.eligibility}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="gender" bundle="${resword}"/>:</td><td class="table_cell">
    <c:out value="${studyToView.gender}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="minimun_age" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.ageMin}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="maximun_age" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.ageMax}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="healthy_volunteers_accepted" bundle="${resword}"/>:</td><td class="table_cell">
  <c:choose>
    <c:when test="${studyToView.healthyVolunteerAccepted == true}">
  <fmt:message key="yes" bundle="${resword}"/>
    </c:when>
    <c:otherwise>
   <fmt:message key="no" bundle="${resword}"/>
    </c:otherwise>
   </c:choose>
 </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="expected_total_enrollment" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.expectedTotalEnrollment}"/>&nbsp;
  </td></tr>
  </table>
</div>
</div></div></div></div></div></div></div></div>
</div>
</div>
<br>

<a href="javascript:leftnavExpand('sectiond');">
    <img id="excl_sectiond" src="images/bt_Expand.gif" border="0"> <span class="table_title_Admin"><fmt:message key="view_study_details" bundle="${resword}"/>: [<fmt:message key="section" bundle="${resword}"/> D: <fmt:message key="facility_information" bundle="${resword}"/>]</span></a>
<div id="sectiond" style="display:none ">
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">

  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_name" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.facilityName}"/>&nbsp;
  </td></tr>

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

  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_contact_name" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.facilityContactName}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_contact_degree" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.facilityContactDegree}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_contact_phone" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.facilityContactPhone}"/>&nbsp;
 </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_contact_email" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.facilityContactEmail}"/>&nbsp;
  </td></tr>
  </table>
  </div>
</div></div></div></div></div></div></div></div>
</div>
</div>
<br>


 <a href="javascript:leftnavExpand('sectione');">
     <img id="excl_sectione" src="images/bt_Expand.gif" border="0"> <span class="table_title_Admin"><fmt:message key="view_study_details" bundle="${resword}"/>: [<fmt:message key="section" bundle="${resword}"/> E: <fmt:message key="related_infomation" bundle="${resword}"/>]</span></a>
<div id="sectione" style="display:none ">
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">

  <tr valign="top"><td class="table_header_column"><fmt:message key="medline_identifier_references" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.medlineIdentifier}"/>&nbsp;
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="results_reference" bundle="${resword}"/></td><td class="table_cell">
  <c:choose>
    <c:when test="${studyToView.resultsReference == true}">
  <fmt:message key="yes" bundle="${resword}"/>
    </c:when>
    <c:otherwise>
   <fmt:message key="no" bundle="${resword}"/>
    </c:otherwise>
   </c:choose>
 </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="URL_reference" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.url}"/>&nbsp;
 </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="URL_description" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.urlDescription}"/>&nbsp;
  </td></tr>

</table>

</div>
</div></div></div></div></div></div></div></div>

</div>
</div>
<br>
<a href="javascript:leftnavExpand('sectionf');">
    <img id="excl_sectionf" src="images/bt_Expand.gif" border="0"> <span class="table_title_Admin"><fmt:message key="view_study_details" bundle="${resword}"/>: [<fmt:message key="section" bundle="${resword}"/> F: <fmt:message key="study_parameter_configuration" bundle="${resword}"/>]</span></a>
<div id="sectionf" style="display:none ">
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">

  <tr valign="top"><td class="table_header_column"><fmt:message key="collect_subject" bundle="${resword}"/></td>
   <td class="table_cell">
   <c:choose>
   <c:when test="${studyToView.studyParameterConfig.collectDob == '1'}">
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

  <tr valign="top"><td class="table_header_column"><fmt:message key="allow_discrepancy_management" bundle="${resword}"/></td>
  <td class="table_cell">
   <c:choose>
   <c:when test="${studyToView.studyParameterConfig.discrepancyManagement == 'true'}">
  <fmt:message key="yes" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
   <fmt:message key="no" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

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
   <c:when test="${studyToView.studyParameterConfig.subjectPersonIdRequired == 'required'}">
    <fmt:message key="required" bundle="${resword}"/>
   </c:when>
    <c:when test="${studyToView.studyParameterConfig.subjectPersonIdRequired == 'optional'}">
    <fmt:message key="optional" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
     <fmt:message key="not_used" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

   <tr valign="top"><td class="table_header_column"><fmt:message key="how_generete_study_subject_ID" bundle="${restext}"/></td>
   <td class="table_cell">
   <c:choose>
   <c:when test="${studyToView.studyParameterConfig.subjectIdGeneration == 'manual'}">
    <fmt:message key="manual_entry" bundle="${resword}"/>
   </c:when>
    <c:when test="${studyToView.studyParameterConfig.subjectIdGeneration == 'auto editable'}">
    <fmt:message key="auto_generated_and_editable" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
     <fmt:message key="auto_generated_and_non_editable" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

  <!--
   <tr valign="top"><td class="table_header_column"><fmt:message key="generate_study_subject_ID_automatically" bundle="${resword}"/></td>
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

  <tr valign="top"><td class="table_header_column"><fmt:message key="show_person_id_on_crf_header" bundle="${resword}"/></td>
   <td class="table_cell">
   <c:choose>
   <c:when test="${studyToView.studyParameterConfig.personIdShownOnCRF == 'true'}">
    <fmt:message key="yes" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
    <fmt:message key="no" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>
   <tr valign="top">
       <td class="table_header_column"><fmt:message key="when_entering_data_entry_interviewer" bundle="${resword}"/></td>
       <td class="table_cell">
            <c:choose>
		   <c:when test="${   studyToView.studyParameterConfig.interviewerNameRequired == 'yes' }">
		   <fmt:message key="yes" bundle="${resword}"/>

		   </c:when>
		   <c:when test="${studyToView.studyParameterConfig.interviewerNameRequired == 'no' }">
		  			<fmt:message key="no" bundle="${resword}"/>
		  </c:when>
		   <c:otherwise>
		   <fmt:message key="not_used" bundle="${resword}"/>
		     </c:otherwise>
		  </c:choose>
      </td>
   </tr>


  <tr valign="top"><td class="table_header_column"><fmt:message key="interviewer_name_default_as_blank" bundle="${restext}"/></td>

  <td class="table_cell">
   <c:choose>
   <c:when test="${studyToView.studyParameterConfig.interviewerNameDefault== 'blank'}">
   <fmt:message key="blank" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
   <fmt:message key="pre_populated_from_study_event" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>


  <tr valign="top"><td class="table_header_column"><fmt:message key="interviewer_name_editable" bundle="${restext}"/></td>
  <td class="table_cell">
   <c:choose>
   <c:when test="${studyToView.studyParameterConfig.interviewerNameEditable== 'true'}">
  <fmt:message key="yes" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
   <fmt:message key="no" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="forced_reason_for_change" bundle="${resword}"/></td>
  <td class="table_cell">
   <c:choose>
   <c:when test="${studyToView.studyParameterConfig.adminForcedReasonForChange == 'true'}">
  <fmt:message key="yes" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
   <fmt:message key="no" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

  <tr valign="top">
      <td class="table_header_column"><fmt:message key="interview_date_required" bundle="${restext}"/></td>
      <td class="table_cell">
           <c:choose>
		   <c:when test="${  studyToView.studyParameterConfig.interviewDateRequired == 'yes'}">
		   <fmt:message key="yes" bundle="${resword}"/>

		   </c:when>
		  <c:when test="${studyToView.studyParameterConfig.interviewDateRequired == 'no'  }">
		  			<fmt:message key="no" bundle="${resword}"/>
		  </c:when>
		   <c:otherwise>
		   <fmt:message key="not_used" bundle="${resword}"/>
		     </c:otherwise>
		  </c:choose>
      </td>
  </tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="interview_date_default_as_blank" bundle="${restext}"/></td>
  <td class="table_cell">
   <c:choose>
   <c:when test="${studyToView.studyParameterConfig.interviewDateDefault== 'blank'}">
   <fmt:message key="blank" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
   <fmt:message key="pre_populated_from_study_event" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="interview_date_editable" bundle="${restext}"/></td>
  <td class="table_cell">
   <c:choose>
   <c:when test="${studyToView.studyParameterConfig.interviewDateEditable== 'true'}">
  <fmt:message key="yes" bundle="${resword}"/>
   </c:when>
   <c:otherwise>
   <fmt:message key="no" bundle="${resword}"/>
   </c:otherwise>
  </c:choose>
  </td>
  </tr>
    <tr valign="top">
        <td class="table_header_column"><fmt:message key="event_location_required" bundle="${resword}"/></td>
        <td class="table_cell">
            <fmt:message key="${studyToView.studyParameterConfig.eventLocationRequired}" bundle="${resword}"/>
       </td>
    </tr>
    
    
    <c:if test="${portalURL!= '' && portalURL!= null}">   
    <tr valign="top">
        <td class="table_header_column"><fmt:message key="participant_portal" bundle="${resword}"/></td>
        <td class="table_cell">
            <fmt:message key="${studyToView.studyParameterConfig.participantPortal}" bundle="${resword}"/>
       </td>
      </tr>
   </c:if>
    

    <c:if test="${moduleManager!= '' && moduleManager!= null}">
    <tr valign="top">
        <td class="table_header_column"><fmt:message key="randomization" bundle="${resword}"/></td>
        <td class="table_cell">
            <fmt:message key="${studyToView.studyParameterConfig.randomization}" bundle="${resword}"/>
       </td>
      </tr>
   </c:if>
    
</table>

</div>
</div></div></div></div></div></div></div></div>

</div>
</div>
<br>
 <a href="javascript:leftnavExpand('sites');">
     <img id="excl_sites" src="images/bt_Collapse.gif" border="0"> <span class="table_title_Admin"><fmt:message key="sites" bundle="${resword}"/>: (<c:out value="${siteNum}"/> <fmt:message key="sites" bundle="${resword}"/>)</span></a>
<div id="sites" style="display: ">
 <div style="width: 600px">
 <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr valign="top">
  <td class="table_header_row_left"><fmt:message key="name" bundle="${resword}"/></td>
  <td class="table_header_row"><fmt:message key="OID" bundle="${resword}"/></td>
  <td class="table_header_row"><fmt:message key="principal_investigator" bundle="${resword}"/></td>
  <td class="table_header_row"><fmt:message key="status" bundle="${resword}"/></td>
  <td class="table_header_row">&nbsp;</td>
  <td></td>
  </tr>
  <c:forEach var="site" items="${sitesToView}">
  <tr valign="top">
   <td class="table_cell_left"><c:out value="${site.name}"/></td>
   <td class="table_cell"><c:out value="${site.oid}"/></td>
   <td class="table_cell">
    <c:out value="${site.principalInvestigator}"/>
   </td>
   <td class="table_cell"><c:out value="${site.status.name}"/></td>
   <td class="table_cell">
     <c:if test="${userBean.techAdmin || userBean.sysAdmin || userRole.manageStudy}">
     <a href="ViewSite?id=<c:out value="${site.id}"/>"
			onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
			onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><img
			name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6"></a>
     </c:if>
   </td>
  </tr>
  </c:forEach>
  </table>
</div>
</div></div></div></div></div></div></div></div>

</div>
</div>
<br>

 <a href="javascript:leftnavExpand('definitions');">
     <img id="excl_definitions" src="images/bt_Collapse.gif" border="0"> <span class="table_title_Admin"><fmt:message key="event_definitions" bundle="${resword}"/>: (<c:out value="${defNum}"/> <fmt:message key="definitions" bundle="${resword}"/>)</span></a>
 <div id="definitions" style="display: ">
 <div style="width: 600px">
 <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
 <tr valign="top">
   <td class="table_header_row_left"><fmt:message key="name" bundle="${resword}"/></td>
   <td class="table_header_row"><fmt:message key="OID" bundle="${resword}"/></td>
   <td class="table_header_row"><fmt:message key="description" bundle="${resword}"/></td>
   <td class="table_header_row"><fmt:message key="of_CRFs" bundle="${resword}"/></td>
   <td class="table_header_row">&nbsp;</td>
   <td></td>
  </tr>
  <c:forEach var="definition" items="${definitionsToView}">
   <tr>
   <td class="table_cell_left">
    <c:out value="${definition.name}"/>
   </td>
   <td class="table_cell"><c:out value="${definition.oid}"/></td>
  <td class="table_cell">
    <c:out value="${definition.description}"/>&nbsp;
   </td>
  <td class="table_cell">
   <c:out value="${definition.crfNum}"/>&nbsp;
   </td>
   <td class="table_cell">
	   <a href="ViewEventDefinitionReadOnly?id=<c:out value="${definition.id}"/>"
			onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
			onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><img
		    name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6"></a>
	</td>
  </tr>
  </c:forEach>
  </table>
</div>
</div></div></div></div></div></div></div></div>
</div>
</div>
<br>

<br>
<a href="javascript:leftnavExpand('users');">
    <img id="excl_users" src="images/bt_Expand.gif" border="0">  <span class="table_title_Admin"><fmt:message key="users" bundle="${resword}"/>: (<c:out value="${userNum}"/> <fmt:message key="users" bundle="${resword}"/>)</span></a>
<div id="users" style="display:none ">

 <div style="width: 600px">
 <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
 <tr valign="top">
   <td class="table_header_row_left"><fmt:message key="user_name" bundle="${resword}"/></td>
   <td class="table_header_row"><fmt:message key="first_name" bundle="${resword}"/></td>
   <td class="table_header_row"><fmt:message key="last_name" bundle="${resword}"/></td>
   <td class="table_header_row"><fmt:message key="role" bundle="${resword}"/></td>
   <td class="table_header_row"><fmt:message key="study_name" bundle="${resword}"/></td>
   <td class="table_header_row"><fmt:message key="status" bundle="${resword}"/></td>
   <td class="table_header_row">&nbsp;</td>
  </tr>
  <c:forEach var="user" items="${userRolesToView}">
  <tr valign="top">
   <td class="table_cell_left">
    <c:out value="${user.userName}"/>
   </td>
    <td class="table_cell"><c:out value="${user.firstName}"/></td>
      <td class="table_cell"><c:out value="${user.lastName}"/></td>
      <td class="table_cell"><c:out value="${user.role.description}"/></td>
      <td class="table_cell"><c:out value="${user.studyName}"/></td>
      <td class="table_cell"><c:out value="${user.status.name}"/></td>
      <td class="table_cell">
        <c:if test="${userBean.techAdmin || userBean.sysAdmin || userRole.manageStudy}">
         <a href="ViewStudyUser?name=<c:out value="${user.userName}"/>&studyId=<c:out value="${user.studyId}"/>"
			onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
			onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><img
			name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6"></a>
		</c:if>
      </td>
  </tr>
  </c:forEach>
  </table>
</div>
</div></div></div></div></div></div></div></div>
</div>
</div>


<jsp:include page="../include/footer.jsp"/>
