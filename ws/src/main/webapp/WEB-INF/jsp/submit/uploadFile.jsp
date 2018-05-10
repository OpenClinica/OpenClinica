<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:useBean scope='session' id='pageMessage' class='java.lang.String'/>
<jsp:useBean scope='session' id='mayProcessUploading' class='java.lang.String'/>
<jsp:useBean scope='request' id='fileItemId' class='java.lang.String'/>
<jsp:useBean scope='request' id='fileName' class='java.lang.String'/>
<jsp:useBean scope='request' id='inputName' class='java.lang.String'/>
<jsp:useBean scope='request' id='attachedFilePath' class='java.lang.String'/>
<jsp:useBean scope='request' id='uploadFileStatus' class='java.lang.String'/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<link rel="stylesheet" href="includes/styles.css" type="text/css">

<html>
<script>
	function processUpload(itemId,fileName,isGroupItem) {
		var a = fileName;
		var b = 'ft' + itemId;
		var c = 'input' + itemId;
		if(isGroupItem == 'true' && itemId.indexOf("input") > 0 ) {
			c = itemId;
		}
		var bb = window.opener.document.getElementById('ft'+itemId);
		bb.setAttribute("value", a);
		window.opener.document.crfForm.elements[b].value = a;
		window.opener.document.crfForm.elements[c].value = a;
	}

	function cleanFile() {
		var f = document.getElementById("file");
		f.value = "";
		window.close();
	}
</script>

<body>
<br><br><br>
<c:forEach var="message" items="${pageMessages}">
 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<c:out value="${message}" escapeXml="false"/>
</c:forEach>
<br><br><br>
<form name="uploadForm" action="UploadFile" method="post" enctype="multipart/form-data">
	<input type="hidden" name="itemId" value="${fileItemId}">
	<input type="hidden" name="inputName" value="${inputName}">
	<c:choose>
	<c:when test="${mayProcessUploading=='true'}">
		<c:choose>
		<c:when test="${uploadFileStatus=='successed'}">
			<c:choose>
			<c:when test="${inputName == null || inputName == ''}">
				<script type="text/javascript">
					processUpload('<c:out value="${fileItemId}"/>','<c:out value="${fileName}"/>','false');
				</script>
			</c:when>
			<c:otherwise>
				<script type="text/javascript">
					processUpload('<c:out value="${inputName}"/>','<c:out value="${fileName}"/>','true');
				</script>
			</c:otherwise>
			</c:choose>
			<br><br>
			<fmt:message key="select_close_window_button" bundle="${restext}"/>
			<br><br><br>
			<P>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="button" name="close" value="<fmt:message key="close_window" bundle="${resword}"/>" onClick="javascript:window.close();" class="button_long"></P>
		</c:when>
		<c:otherwise>
			<fmt:message key="upload_note" bundle="${restext}"/>
			<br><br>
			<fmt:message key="select_cancel_upload_button" bundle="${restext}"/>
			<br><br><br>
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input id="file" type="file" name="browse" size="60">
			<P>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type="submit" name="upload" value="<fmt:message key="upload_file" bundle="${resword}"/>" class="button_long">
			<input type="button" name="cancel" value="<fmt:message key="cancel_upload" bundle="${resword}"/>" onClick="cleanFile()" class="button_long"></P>
			<input type="hidden" name="crfId" value="<c:out value="${version.crfId}"/>">
		</c:otherwise>
		</c:choose>
	</c:when>
	<c:otherwise>
		&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <fmt:message key="uploading_not_process_because_permission" bundle="${restext}"/>
	</c:otherwise>
	</c:choose>
</form>

</body>
</html>