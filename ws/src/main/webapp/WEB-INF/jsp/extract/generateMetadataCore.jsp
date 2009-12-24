<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/> 

<jsp:useBean scope="request" id="dataset" class="org.akaza.openclinica.bean.extract.DatasetBean"/>
<jsp:useBean scope="request" id="file" class="org.akaza.openclinica.bean.extract.ArchivedDatasetFileBean"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML>
<HEAD>
<link rel="stylesheet" href="includes/styles.css" type="text/css">
<style type="text/css"> 
    #waitpage { position: absolute; } 
    #mainpage { position: absolute; visibility: hidden; } 
</style> 
<TITLE></TITLE>

<script language="JavaScript">
<!--
function init() {
	if (document.layers) {
		document.waitpage.visibility = 'hide';
		document.mainpage.visibility = 'show';
	} else {
		if (document.all) {
			document.all.waitpage.style.visibility = 'hidden';
			document.all.mainpage.style.visibility = 'visible';
		}
	}
}

//-->

</script>
</HEAD>

<BODY onLoad="init();" class="popup_BG">
<div id="waitpage"> 

<h1><span class="title_manage"> <fmt:message key="page_loading_please_wait" bundle="${restext}"/> </span></h1>

</div> 

<!-- put the rest of your page contents here -->
<!-- work on putting csv, etc into web page tomorrow? -->
<div id="mainpage">
	<%--<jsp:forward page="generateMetadataFile.jsp"/>--%>
<script language="JavaScript">
<!--
	location.href="ShowFile?datasetId=<c:out value="${dataset.id}"/>&fileId=<c:out value="${file.id}"/>";
// -->
</script>

</div>

</body>
</html>
