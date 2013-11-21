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
  
  var app_formVersionOID = '${formVersionOID}';
  var app_protocolIDLabel = '<fmt:message key="protocol_ID" bundle="${resword}"/>'
  var app_siteNameLabel = '<fmt:message key="site_name" bundle="${resword}"/>'
  var app_studyNameLabel = '<fmt:message key="study_name" bundle="${resword}"/>'
  var app_studySubjectIDLabel = '<fmt:message key="study_subject_ID" bundle="${resword}"/>'
  var app_pageNumberLabel = '<fmt:message key="page_x_de_y" bundle="${resword}"/>'.replace(/[{}]/g, '');
  var app_investigatorLabel = '<fmt:message key="investigator" bundle="${resterm}"/>';
  var app_timeAndEventsLabel = '<fmt:message key="time_and_events" bundle="${resword}"/>';
  var app_investigatorNameLabel = '<fmt:message key="investigator_name" bundle="${resword}"/>';
  var app_investigatorSignatureLabel = '<fmt:message key="investigator_signature" bundle="${resword}"/>'; 
  var app_eventDateLabel = '<fmt:message key="end_date1" bundle="${resword}"/>'; 
  var app_eventLocationLabel = '<fmt:message key="event_location" bundle="${resword}"/>'; 
  var app_eventNameLabel = '<fmt:message key="event_name" bundle="${resword}"/>'; 
  var app_personIDLabel = '<fmt:message key="person_ID" bundle="${resword}"/>'; 
  var app_interviewerLabel = '<fmt:message key="interviewer" bundle="${resword}"/>'; 
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
var app_crfLabel='<fmt:message key="CRF" bundle="${resword}"/>';
var genderLabel='<fmt:message key="gender" bundle="${resword}"/>';

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
