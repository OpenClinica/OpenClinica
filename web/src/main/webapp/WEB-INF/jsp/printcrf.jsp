<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>


<c:set var="dtetmeFormat"><fmt:message key="date_time_format_string" bundle="${resformat}"/></c:set>

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

      var app_eventCRFLabel = '<fmt:message key="event_CRF" bundle="${resword}"/>';
      var app_SELabel = '<fmt:message key="SE" bundle="${resword}"/>';
  
      var app_Audit_HistoryLabel = '<fmt:message key="Audit_History" bundle="${resword}"/>';
      var app_audit_eventLabel = '<fmt:message key="audit_event" bundle="${resword}"/>';
	  var app_date_time_of_serverLabel = '<fmt:message key="date_time_of_server" bundle="${resword}"/>';
	  var app_userLabel = '<fmt:message key="user" bundle="${resword}"/>';
	  var app_value_typeLabel = '<fmt:message key="value_type" bundle="${resword}"/>';
	  var app_oldLabel = '<fmt:message key="old" bundle="${resword}"/>';
	  var app_newLabel = '<fmt:message key="new" bundle="${resword}"/>';
	 
	  
	  

	  
	  var app_nav_notes_and_discrepanciesLabel = '<fmt:message key="nav_notes_and_discrepancies" bundle="${resword}"/>';
	  var app_noteLabel = '<fmt:message key="note" bundle="${resword}"/>';
	  var app_assigned_toLabel = '<fmt:message key="assigned_to" bundle="${resword}"/>';
	  var app_IDLabel = '<fmt:message key="ID" bundle="${resword}"/>';
	  var app_typeLabel = '<fmt:message key="type" bundle="${resword}"/>';
	  var app_current_statusLabel = '<fmt:message key="current_status" bundle="${resword}"/>';
	  var app_n_of_notesLabel = '<fmt:message key="n_of_notes" bundle="${resword}"/>';
	  var app_Last_updatedLabel = '<fmt:message key="Last_updated" bundle="${resword}"/>';
	  var app_this_is_a_new_threadLabel = '<fmt:message key="this_is_a_new_thread" bundle="${resword}"/>';
	
	  
	  
	  
	  var app_metadataLabel = '<fmt:message key="metadata" bundle="${resword}"/>';
	  var app_left_item_textLabel = '<fmt:message key="left_item_text" bundle="${resword}"/>';
	  var app_unitsLabel = '<fmt:message key="units" bundle="${resword}"/>';
	  var app_data_typeLabel = '<fmt:message key="data_type" bundle="${resword}"/>';
	  var app_response_options_textLabel = '<fmt:message key="response_options_text" bundle="${resword}"/>';
	  var app_response_valuesLabel = '<fmt:message key="response_values" bundle="${resword}"/>';
	  var app_rule_group_labelLabel = '<fmt:message key="rule_group_label" bundle="${resword}"/>';
								  
	  
	  
	  
	  
	  
	  
  
  
	  
  
  
  var app_formVersionOID = '${formVersionOID}';
  var app_protocolIDLabel = '<fmt:message key="protocol_ID" bundle="${resword}"/>';
  var app_siteNameLabel = '<fmt:message key="site" bundle="${resword}"/>';
  var app_studyNameLabel = '<fmt:message key="study_name" bundle="${resword}"/>';
  var app_studySubjectLabel = '<fmt:message key="study_subject" bundle="${resword}"/>';
  var app_studySubjectIDLabel = '<fmt:message key="study_subject_ID" bundle="${resword}"/>';
  var app_pageNumberLabel = '<fmt:message key="page_x_de_y" bundle="${resword}"/>'.replace(/[{}]/g, '');
  var app_investigatorLabel = '<fmt:message key="investigator" bundle="${resterm}"/>';
  var app_timeAndEventsLabel = '<fmt:message key="time_and_events" bundle="${resword}"/>';
  var app_investigatorNameLabel = '<fmt:message key="investigator_name" bundle="${resword}"/>';
  var app_investigatorSignatureLabel = '<fmt:message key="investigator_signature" bundle="${resword}"/>'; 
  var app_eventDateLabel = '<fmt:message key="event_date" bundle="${resword}"/>'; 
  var app_eventLocationLabel = '<fmt:message key="event_location" bundle="${resword}"/>'; 
  var app_eventNameLabel = '<fmt:message key="event_name" bundle="${resword}"/>'; 
  var app_personIDLabel = '<fmt:message key="person_ID" bundle="${resword}"/>'; 
  var app_interviewerLabel = '<fmt:message key="interviewer" bundle="${resword}"/>'; 
  var app_interviewerName = '<fmt:message key="interviewer_name" bundle="${resword}"/>'; 
  var app_interviewDateLabel = '<fmt:message key="interview_date" bundle="${resword}"/>'; 
  var app_secondaryLabel = '<fmt:message key="secondary_label" bundle="${resword}"/>'; 
  var app_studySubjectDOBLabel = '<fmt:message key="study_subject_dob" bundle="${resword}"/>'; 
  var app_dateLabel = '<fmt:message key="date" bundle="${resword}"/>'; 
  var app_studySubjectBirthYearLabel = '<fmt:message key="study_subject_birth_year" bundle="${resword}"/>'; 
  var app_datePrintedLabel = '<fmt:message key="date_printed" bundle="${resword}"/>'; 
  var app_sectionTitle =  '<fmt:message key="section_title" bundle="${resword}"/>'+":";
  var app_printTime = '<fmt:formatDate value="<%= new java.util.Date() %>" type="both" pattern="${dtetmeFormat}" timeStyle="short"/>';
  var app_sectionSubtitle = '<fmt:message key="subtitle" bundle="${resword}"/>'+":";
  var app_sectionInstructions = '<fmt:message key="instructions" bundle="${resword}"/>'+":";
  var app_sectionPage = '<fmt:message key="page" bundle="${resword}"/>'+":";
  var app_displayAudits='${includeAudits}';
  var app_displayDNs='${includeDNs}';
var removed_crfVersionMessage='<fmt:message key="removed_crf_version" bundle="${resword}"/>'+":";

var app_subjectAgeAtEvent='<fmt:message key="subject_age_at_event" bundle="${resword}"/>';
var app_eventStatus='<fmt:message key="event_status" bundle="${resword}"/>';
//var app_groupName='<fmt:message key="group_name" bundle="${resword}"/>';
//var app_groupClassName='<fmt:message key="group_class_name" bundle="${resword}"/>';
var app_subjectStatus='<fmt:message key="subject_status" bundle="${resword}"/>';
var app_studyLabel='<fmt:message key="study" bundle="${resword}"/>';
// var app_studyEventLabel='<fmt:message key="SE" bundle="${resword}"/>';
//var app_subjectLabel='<fmt:message key="subject" bundle="${resword}"/>';
var app_groupLabel='<fmt:message key="group" bundle="${resword}"/>';
var app_crfLabel='<fmt:message key="CRF_status" bundle="${resword}"/>';
var genderLabel='<fmt:message key="gender" bundle="${resword}"/>';
var app_eventStartDateLabel = '<fmt:message key="start_date" bundle="${resword}"/>';
var app_eventEndDateLabel = '<fmt:message key="end_date" bundle="${resword}"/>';
var app_meaning_of_signatureLabel ='<fmt:message key="meaning_of_signature" bundle="${resword}"/>';
var app_meaning_of_signature ='<fmt:message key="sure_to_sign_subject1" bundle="${resword}"/>'+'<fmt:message key="sure_to_sign_subject2" bundle="${resword}"/>';
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
 
 <body>
   <img id="loading" src="${pageContext.request.contextPath}/images/loading_wh.gif" class="spinner"/> 
 </body>
</html>
