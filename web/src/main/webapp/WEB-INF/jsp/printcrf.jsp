<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<script>
  var app_contextPath = '${pageContext.request.contextPath}'; 
  var app_studyOID = (window.location.pathname.split('/'))[4];
  var app_formOID = (window.location.pathname.split('/'))[5];
</script>

<html>
 <head>
  <meta charset="UTF-8">
  <title>OpenClinica - Print CRF Test</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/normalize.css" type="text/css"/>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css" type="text/css"/>
  <script src="${pageContext.request.contextPath}/js/lib/head.min.js"></script>
  <script src="${pageContext.request.contextPath}/js/load_scripts.js"></script>
 </head>
 
 <body>
  <div id="main_screen">
   <div id="form_wrapper" class="centered"> </div>
  </div>
 </body>
</html>