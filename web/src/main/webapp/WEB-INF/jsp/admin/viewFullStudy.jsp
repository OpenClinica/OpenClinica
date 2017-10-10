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

<h1><span class="title_manage"><c:out value="${studyToView.name}"/></span></h1></br>

<a style="text-decoration:none" href="javascript:openDocWindow('DownloadStudyMetadata?studyId=<c:out value="${studyToView.id}"/>');"><fmt:message key="download_study_meta" bundle="${restext}"/></a>.
<fmt:message key="get_subject_oid_from_matrix_show_more" bundle="${restext}"/>
<a style="text-decoration:none" href="ListStudySubjects">Subject Matrix</a>.

<br><br>
<a href="javascript:leftnavExpand('overview');" style="text-decoration:none;">
    <img id="excl_overview" src="images/bt_Collapse.gif" border="0">
    <span>
    <fmt:message key="overview" bundle="${resword}"/></span></a>
<div id="overview" style="">
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr valign="top"><td class="table_header_column"><fmt:message key="name" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.name}"/>
  </td></tr>
  <tr valign="top"><td class="table_header_column"><a style="text-decoration:none" href="http://prsinfo.clinicaltrials.gov/definitions.html#PrimaryId" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#PrimaryId'); return false;"><fmt:message key="unique_protocol_ID" bundle="${resword}"/></a>:</td><td class="table_cell">
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

<a href="javascript:leftnavExpand('sectiona');" style="text-decoration:none;">
    <img id="excl_sectiona" src="images/bt_Expand.gif" border="0">
    <span><fmt:message key="view_study_details" bundle="${resword}"/>: [<fmt:message key="section" bundle="${resword}"/> A: <fmt:message key="study_description" bundle="${resword}"/>]</span></a>
<div id="sectiona" style="display:none ">
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr valign="top"><td class="table_header_column"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#BriefTitle" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#BriefTitle'); return false;"><fmt:message key="brief_title" bundle="${resword}"/></a>:</td><td class="table_cell">
  <c:out value="${studyToView.name}"/>
  </td></tr>

  <tr valign="top"><td class="table_header_column"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#PrimaryId" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#PrimaryId'); return false;"><fmt:message key="unique_protocol_ID" bundle="${resword}"/></a>:</td><td class="table_cell">
  <c:out value="${studyToView.identifier}"/>
  </td></tr>

  <tr valign="top"><td class="table_header_column"><fmt:message key="principal_investigator" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.principalInvestigator}"/>
  </td></tr>

  <tr valign="top"><td class="table_header_column"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#BriefSummary" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#BriefSummary'); return false;"><fmt:message key="brief_summary" bundle="${resword}"/></a>:</td><td class="table_cell">
  <c:out value="${studyToView.summary}"/>
  </td></tr>
  </table>
</div>
</div></div></div></div></div></div></div></div>
</div>
</div>
<br>


<a href="javascript:leftnavExpand('sectionb');" style="text-decoration:none;">
    <img id="excl_sectionb" src="images/bt_Expand.gif" border="0">
    <span><fmt:message key="view_study_details" bundle="${resword}"/>: [<fmt:message key="section" bundle="${resword}"/> B: <fmt:message key="study_status_and_design" bundle="${resword}"/>]</span></a>
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

  <c:choose>
  <c:when test="${studyToView.protocolTypeKey=='interventional'}">
  </c:when>
  <c:otherwise>
  </c:otherwise>
  </c:choose>
</table>
</div>
</div></div></div></div></div></div></div></div>
</div>
</div>
<br>

<a href="javascript:leftnavExpand('sectionc');" style="text-decoration:none;">
    <img id="excl_sectionc" src="images/bt_Expand.gif" border="0"> <span><fmt:message key="view_study_details" bundle="${resword}"/>: [<fmt:message key="section" bundle="${resword}"/> C: <fmt:message key="conditions_and_eligibility" bundle="${resword}"/>]</span></a>
<div id="sectionc" style="display:none ">
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">

  <tr valign="top"><td class="table_header_column"><fmt:message key="expected_total_enrollment" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${studyToView.expectedTotalEnrollment}"/>&nbsp;
  </td></tr>
  </table>
</div>
</div></div></div></div></div></div></div></div>
</div>
</div>
<br>

<a href="javascript:leftnavExpand('sectiond');" style="text-decoration:none;">
    <img id="excl_sectiond" src="images/bt_Expand.gif" border="0"> <span><fmt:message key="view_study_details" bundle="${resword}"/>: [<fmt:message key="section" bundle="${resword}"/> D: <fmt:message key="facility_information" bundle="${resword}"/>]</span></a>
<div id="sectiond" style="display:none ">
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">

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

<a href="javascript:leftnavExpand('sectionf');" style="text-decoration:none;">
    <img id="excl_sectionf" src="images/bt_Expand.gif" border="0"> <span><fmt:message key="view_study_details" bundle="${resword}"/>: [<fmt:message key="section" bundle="${resword}"/> E: <fmt:message key="study_parameter_configuration" bundle="${resword}"/>]</span></a>
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
            or ((studyToView.studyParameterConfig.subjectPersonIdRequired == 'always'))}">    <fmt:message key="required" bundle="${resword}"/>
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
</table>

</div>
</div></div></div></div></div></div></div></div>

</div>
</div>
<br>
 <a href="javascript:leftnavExpand('sites');" style="text-decoration:none;">
     <img id="excl_sites" src="images/bt_Collapse.gif" border="0"> <span><fmt:message key="sites" bundle="${resword}"/>: (<c:out value="${siteNum}"/> <fmt:message key="sites" bundle="${resword}"/>)</span></a>
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
      onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><span
      name="bt_View1" class="icon icon-search" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6"></a>
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

 <a href="javascript:leftnavExpand('definitions');" style="text-decoration:none;">
     <img id="excl_definitions" src="images/bt_Collapse.gif" border="0"> <span><fmt:message key="event_definitions" bundle="${resword}"/>: (<c:out value="${defNum}"/> <fmt:message key="definitions" bundle="${resword}"/>)</span></a>
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
      onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><center><span
        name="bt_View1" class="icon icon-search" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6"></a>
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
<a href="javascript:leftnavExpand('users');" style="text-decoration:none;">
    <img id="excl_users" src="images/bt_Expand.gif" border="0">  <span><fmt:message key="users" bundle="${resword}"/>: (<c:out value="${userNum}"/> <fmt:message key="users" bundle="${resword}"/>)</span></a>
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

      onMouseDown="javascript:setImage('bt_View1','icon icon-search');"
      onMouseUp="javascript:setImage('bt_View1','icon icon-search');"><center><span
      name="bt_View1" class="icon icon-search" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6"></a>


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
