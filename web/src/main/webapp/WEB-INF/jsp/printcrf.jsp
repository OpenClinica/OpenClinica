<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<script>
  var app_contextPath = '${pageContext.request.contextPath}'; 
  var app_studyOID = '${studyOID}';
  var app_siteOID = '${studyOID}';
  var app_eventOID = '${eventOID}';
  var app_formOID = '${formOID}';
  var app_printTime = '<%= new java.util.Date() %>';
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