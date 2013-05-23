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
  var app_siteOID = '${studyOID}';
  var app_eventOID = '${eventOID}';
  var app_formVersionOID = '${formVersionOID}';
  var app_protocolIDLabel = '<fmt:message key="protocol_ID" bundle="${resword}"/>'
  var app_siteNameLabel = '<fmt:message key="site_name" bundle="${resword}"/>'
  var app_studyNameLabel = '<fmt:message key="study_name" bundle="${resword}"/>'
  var app_studySubjectIDLabel = '<fmt:message key="study_subject_ID" bundle="${resword}"/>'
  var app_pageNumberLabel = '<fmt:message key="page_x_de_y" bundle="${resword}"/>'.replace(/[{}]/g, '');
  var app_investigatorLabel = '<fmt:message key="investigator" bundle="${resterm}"/>';
  var app_timeAndEventsLabel = '<fmt:message key="time_and_events" bundle="${resterm}"/>';
  var app_investigatorNameLabel = '<fmt:message key="investigator_name" bundle="${resterm}"/>';
  var app_investigatorSignatureLabel = '<fmt:message key="investigator_signature" bundle="${resterm}"/>';
  var app_sectionTitle =  '<fmt:message key="section_title" bundle="${resword}"/>'+":";
  var app_printTime = '<fmt:formatDate value="<%= new java.util.Date() %>" type="both" pattern="${dtetmeFormat}" timeStyle="short"/>';
  var app_sectionSubtitle = '<fmt:message key="subtitle" bundle="${resword}"/>'+":";
  var app_sectionInstructions = '<fmt:message key="instructions" bundle="${resword}"/>'+":";
  var app_sectionPage = '<fmt:message key="page" bundle="${resword}"/>'+":";
  
</script>

<html>
 <head>
  <meta charset="UTF-8">
  <title>OpenClinica - Printable Forms</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/normalize.css" type="text/css"/>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css" type="text/css"/>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/print.css" type="text/css"/>
  <script src="${pageContext.request.contextPath}/js/lib/head.min.js"></script>
  <script src="${pageContext.request.contextPath}/js/load_scripts.js"></script>
 </head>
 
 <body>
   <img id="loading" src="${pageContext.request.contextPath}/images/loading_wh.gif" class="spinner"/> 
 </body>
</html>