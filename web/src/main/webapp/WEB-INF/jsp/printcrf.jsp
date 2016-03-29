<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="oc" uri="http://www.openclinica.com/jsp/tld/oc" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.page_messages" var="respage_messages"/>


<c:set var="dtetmeFormat"><fmt:message key="date_time_format_string" bundle="${resformat}"/></c:set>
<c:set var="search" value="'"/>
<c:set var="replace" value="\\'"/>
<c:set var="test"><fmt:message key="sure_to_sign_subject1" bundle="${resword}"/><fmt:message key="sure_to_sign_subject2" bundle="${resword}"/></c:set>
<c:set var="test1" value="${fn:replace(test,search,replace)}"></c:set>
<c:set var="app_print_CRF_Message_at_Loading"><fmt:message key="print_CRF_Message_at_Loading" bundle="${respage_messages}"/></c:set>

<script>
  var app_contextPath = '${pageContext.request.contextPath}';
  var app_studyOID = '${studyOID}';
  var app_studySubjectOID = '${studySubjectOID}';
  var app_siteOID = '${studyOID}';

  var app_eventOID = '${eventOID}';
  var app_eventOrdinal = 1;
  if ('${eventOID}'.indexOf('[') != -1) {
    app_eventOID = '${eventOID}'.substring(0,'${eventOID}'.indexOf('['));
    app_eventOrdinal = '${eventOID}'.substring('${eventOID}'.indexOf('[')+1, '${eventOID}'.indexOf(']'));
  }

  var app_eventCRFLabel = '<oc:jsEscape key="event_CRF" bundle="${resword}"/>';
      var app_SELabel = '<oc:jsEscape key="SE" bundle="${resword}"/>';
      var app_study_subjectLabel = '<oc:jsEscape key="study_subject" bundle="${resword}"/>';


      var app_Audit_HistoryLabel = '<oc:jsEscape key="Audit_History" bundle="${resword}"/>';
      var app_audit_eventLabel = '<oc:jsEscape key="audit_event" bundle="${resword}"/>';
	  var app_date_time_of_serverLabel = '<oc:jsEscape key="date_time_of_server" bundle="${resword}"/>';
	  var app_userLabel = '<oc:jsEscape key="user" bundle="${resword}"/>';
	  var app_value_typeLabel = '<oc:jsEscape key="value_type" bundle="${resword}"/>';
	  var app_oldLabel = '<oc:jsEscape key="old" bundle="${resword}"/>';
	  var app_newLabel = '<oc:jsEscape key="new" bundle="${resword}"/>';





	  var app_nav_notes_and_discrepanciesLabel = '<oc:jsEscape key="nav_notes_and_discrepancies" bundle="${resword}"/>';
	  var app_noteLabel = '<oc:jsEscape key="note" bundle="${resword}"/>';
	  var app_assigned_toLabel = '<oc:jsEscape key="assigned_to" bundle="${resword}"/>';
	  var app_IDLabel = '<oc:jsEscape key="ID" bundle="${resword}"/>';
	  var app_typeLabel = '<oc:jsEscape key="type" bundle="${resword}"/>';
	  var app_current_statusLabel = '<oc:jsEscape key="current_status" bundle="${resword}"/>';
	  var app_n_of_notesLabel = '<oc:jsEscape key="n_of_notes" bundle="${resword}"/>';
//	  var app_Last_updatedLabel = '<oc:jsEscape key="last_updated_date" bundle="${resword}"/>';

	  var app_CreatedByLabel = '<oc:jsEscape key="created_by" bundle="${resword}"/>';
	  var app_CreatedDateLabel = '<oc:jsEscape key="created_date" bundle="${resword}"/>';

	  var app_this_is_a_new_threadLabel = '<oc:jsEscape key="this_is_a_new_thread" bundle="${resword}"/>';




	  var app_metadataLabel = '<oc:jsEscape key="metadata" bundle="${resword}"/>';
	  var app_left_item_textLabel = '<oc:jsEscape key="left_item_text" bundle="${resword}"/>';
	  var app_unitsLabel = '<oc:jsEscape key="units" bundle="${resword}"/>';
	  var app_data_typeLabel = '<oc:jsEscape key="data_type" bundle="${resword}"/>';
	  var app_response_options_textLabel = '<oc:jsEscape key="response_options_text" bundle="${resword}"/>';
	  var app_response_valuesLabel = '<oc:jsEscape key="response_values" bundle="${resword}"/>';
	  var app_rule_group_labelLabel = '<oc:jsEscape key="rule_group_label" bundle="${resword}"/>';


     //study subject
	  var app_date_of_birthLabel = '<oc:jsEscape key="date_of_birth" bundle="${resword}"/>';
	  var app_genderLabel = '<oc:jsEscape key="gender" bundle="${resword}"/>';
	  var app_enrollMMDDYYYYLabel = '<oc:jsEscape key="enrollMMDDYYYY" bundle="${resword}"/>';
	  var app_subject_statusLabel = '<oc:jsEscape key="subject_status" bundle="${resword}"/>';
	  var app_subject_unique_IDLabel = '<oc:jsEscape key="subject_unique_ID" bundle="${resword}"/>';
	  var app_secondary_IDLabel = '<oc:jsEscape key="secondary_ID" bundle="${resword}"/>';
	  var app_groupLabel = '<oc:jsEscape key="group" bundle="${resword}"/>';
	  var app_detailsLabel = '<oc:jsEscape key="details" bundle="${resword}"/>';

	  var app_enrollment_dateLabel = '<oc:jsEscape key="enrollment_date" bundle="${resword}"/>';








  var app_formVersionOID = '${formVersionOID}';
  var app_protocolIDLabel = '<oc:jsEscape key="protocol_ID" bundle="${resword}"/>';
  var app_siteNameLabel = '<oc:jsEscape key="site" bundle="${resword}"/>';
  var app_studyNameLabel = '<oc:jsEscape key="study_name" bundle="${resword}"/>';
  var app_studySubjectLabel = '<oc:jsEscape key="study_subject" bundle="${resword}"/>';
  var app_studySubjectIDLabel = '<oc:jsEscape key="study_subject_ID" bundle="${resword}"/>';
  var app_pageNumberLabel = '<oc:jsEscape key="page_x_de_y" bundle="${resword}"/>'.replace(/[{}]/g, '');
  var app_investigatorLabel = '<oc:jsEscape key="investigator" bundle="${resterm}"/>';
  var app_timeAndEventsLabel = '<oc:jsEscape key="time_and_events" bundle="${resword}"/>';
  var app_investigatorNameLabel = '<oc:jsEscape key="investigator_name" bundle="${resword}"/>';
  var app_investigatorSignatureLabel = '<oc:jsEscape key="investigator_signature" bundle="${resword}"/>';
  var app_eventDateLabel = '<oc:jsEscape key="event_date" bundle="${resword}"/>';
  var app_eventLocationLabel = '<oc:jsEscape key="event_location" bundle="${resword}"/>';
  var app_eventNameLabel = '<oc:jsEscape key="event_name" bundle="${resword}"/>';
  var app_personIDLabel = '<oc:jsEscape key="person_ID" bundle="${resword}"/>';
  //var app_interviewerLabel = '<oc:jsEscape key="interviewer" bundle="${resword}"/>';
  var app_interviewerLabel = '<oc:jsEscape key="interviewer_name" bundle="${resword}"/>';
  var app_interviewDateLabel = '<oc:jsEscape key="interview_date" bundle="${resword}"/>';
  var app_secondaryLabel = '<oc:jsEscape key="secondary_label" bundle="${resword}"/>';
  var app_studySubjectDOBLabel = '<oc:jsEscape key="study_subject_dob" bundle="${resword}"/>';
  var app_dateLabel = '<oc:jsEscape key="date" bundle="${resword}"/>';
  var app_studySubjectBirthYearLabel = '<oc:jsEscape key="study_subject_birth_year" bundle="${resword}"/>';
  var app_datePrintedLabel = '<oc:jsEscape key="date_printed" bundle="${resword}"/>';
  var app_sectionTitle =  '<oc:jsEscape key="section_title" bundle="${resword}"/>'+":";
  var app_printTime = '<fmt:formatDate value="<%= new java.util.Date() %>" type="both" pattern="${dtetmeFormat}" timeStyle="short"/>';
  var app_sectionSubtitle = '<oc:jsEscape key="subtitle" bundle="${resword}"/>'+":";
  var app_sectionInstructions = '<oc:jsEscape key="instructions" bundle="${resword}"/>'+":";
  var app_sectionPage = '<oc:jsEscape key="page" bundle="${resword}"/>'+":";
  var app_displayAudits='${includeAudits}';
  var app_displayDNs='${includeDNs}';
var removed_crfVersionMessage='<oc:jsEscape key="removed_crf_version" bundle="${resword}"/>'+":";

var app_subjectAgeAtEvent='<oc:jsEscape key="subject_age_at_event" bundle="${resword}"/>';
var app_eventStatus='<oc:jsEscape key="event_status" bundle="${resword}"/>';
//var app_groupName='<oc:jsEscape key="group_name" bundle="${resword}"/>';
//var app_groupClassName='<oc:jsEscape key="group_class_name" bundle="${resword}"/>';
var app_subjectStatus='<oc:jsEscape key="subject_status" bundle="${resword}"/>';
var app_studyLabel='<oc:jsEscape key="study" bundle="${resword}"/>';
 var app_studyEventLabel='<oc:jsEscape key="events" bundle="${resword}"/>';
//var app_subjectLabel='<oc:jsEscape key="subject" bundle="${resword}"/>';
var app_groupLabel='<oc:jsEscape key="group" bundle="${resword}"/>';
var app_crfLabel='<oc:jsEscape key="CRF_status" bundle="${resword}"/>';
var genderLabel='<oc:jsEscape key="gender" bundle="${resword}"/>';
var app_eventStartDateLabel = '<oc:jsEscape key="start_date" bundle="${resword}"/>';
var app_eventEndDateLabel = '<oc:jsEscape key="end_date" bundle="${resword}"/>';
var app_meaning_of_signatureLabel ='<oc:jsEscape key="meaning_of_signature" bundle="${resword}"/>';
//var app_meaning_of_signature ='<oc:jsEscape key="sure_to_sign_subject1" bundle="${resword}"/>'+'<oc:jsEscape key="sure_to_sign_subject2" bundle="${resword}"/>';
var app_meaning_of_signature ='${fn:replace(test,search,replace)}';

var app_start_date_timeLabel = '<oc:jsEscape key="start_date_time" bundle="${resword}"/>';
var app_end_date_timeLabel = '<oc:jsEscape key="end_date_time" bundle="${resword}"/>';


var app_formSigned= '<oc:jsEscape key="signed" bundle="${resterm}"/>';
var app_electronicSignatureLabel='<oc:jsEscape key="electronic_signature" bundle="${resword}"/>';
var app_statusLabel='<oc:jsEscape key="status" bundle="${resword}"/>';
var app_the_eCRF_that_are_part_of_this_event_were_signed_by ='<oc:jsEscape key="the_eCRF_that_are_part_of_this_event_were_signed_by" bundle="${resword}"/>';
var app_under_the_following_attestation ='<oc:jsEscape key="under_the_following_attestation" bundle="${resword}"/>';
var app_server_time='<oc:jsEscape key="server_time" bundle="${resword}"/>';
var app_on='<oc:jsEscape key="on" bundle="${resword}"/>';
var app_tableOfContentsLabel='<oc:jsEscape key="table_of_contents" bundle="${resword}"/>';
var app_case_report_form='<oc:jsEscape key="case_report_form" bundle="${resword}"/>';

var app_error_print_CRF_Message_at_Loading='<oc:jsEscape key="error_print_CRF_Message_at_Loading" bundle="${respage_messages}"/>';


</script>

<html>
 <head>
  <meta charset="UTF-8">
  <title>OpenClinica - Printable Forms</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/normalize.css" type="text/css"/>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css" type="text/css"/>
  <script src="${pageContext.request.contextPath}/js/lib/head.min.js"></script>
  <script src="${pageContext.request.contextPath}/js/load_scripts.js"></script>
 </head>

 <body style="margin-left: 2cm; margin-right: 2cm">
     <div id="loading_msg"> ${app_print_CRF_Message_at_Loading}</div>
    <img id="loading" src="${pageContext.request.contextPath}/images/loading_wh.gif" class="spinner"/>
 </body>
</html>
