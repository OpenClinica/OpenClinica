<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<jsp:include page="../include/admin-header.jsp"/>


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

<jsp:useBean scope="request" id="localBirthDate" class="java.lang.String" />

<body class="aka_bodywidth" onload="<c:if test='${popUpURL != ""}'>openDNoteWindow('<c:out value="${popUpURL}" />');</c:if>">

<h1><span class="title_manage"><fmt:message key="update_subject_details" bundle="${resword}"/></span></h1>
<P><fmt:message key="field_required" bundle="${resword}"/></P>
<form action="UpdateSubject" method="post">
<input type="hidden" name="action" value="confirm">
<input type="hidden" name="id" value="<c:out value="${id}"/>">
<input type="hidden" name="studySubId" value="<c:out value="${studySubId}"/>">
<jsp:useBean scope="request" id="subjectToUpdate" class="org.akaza.openclinica.bean.submit.SubjectBean" />

<!-- These DIVs define shaded box borders -->
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="2" cellspacing="2">
<c:if test="${study.studyParameterConfig.subjectPersonIdRequired=='required' || study.studyParameterConfig.subjectPersonIdRequired=='optional'}">
	<tr valign="top">
	  	<td class="formlabel"><fmt:message key="person_ID" bundle="${resword}"/>:</td>
		<td colspan="2">
		  <div class="formfieldXL_BG"><input type="text" name="uniqueIdentifier" value="<c:out value="${subjectToUpdate.uniqueIdentifier}"/>" class="formfieldXL"></div>
		  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="uniqueIdentifier"/></jsp:include>
		</td>
	</tr>
</c:if>

	<tr valign="top">
		<td class="formlabel"><fmt:message key="gender" bundle="${resword}"/>:</td>
		<td colspan="2">
		  <input type="radio" name="gender" <c:if test="${subjectToUpdate.gender == 109}">checked</c:if> value="m"><fmt:message key="male" bundle="${resword}"/>
          <input type="radio" name="gender" <c:if test="${subjectToUpdate.gender == 102}">checked</c:if> value="f"><fmt:message key="female" bundle="${resword}"/>
          <input type="radio" name="gender"  <c:if test="${!(subjectToUpdate.gender == 109 || subjectToUpdate.gender == 102)}">checked</c:if> value=" "><fmt:message key="not_specified" bundle="${resword}"/>
         
       
        <c:if test="${study.studyParameterConfig.discrepancyManagement=='true'}">
		<a href="#" onClick="openDNoteWindow('CreateDiscrepancyNote?subjectId=${studySubId}&name=subject&id=<c:out value="${subjectToUpdate.id}"/>&field=gender&column=gender','spanAlert-gender'); return false;">
		<img name="flag_gender" src="images/<c:out value="${genderDNFlag}"/>.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"></a>
		</c:if>
		 <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="gender"/></jsp:include>
		</td>
	</tr>
	<%-- DOB collected in one form or another --%>
	<c:if test="${study.studyParameterConfig.collectDob=='1' || study.studyParameterConfig.collectDob=='2' }">
	<tr valign="top">
		<td class="formlabel" >
		<c:if test="${study.studyParameterConfig.collectDob=='1'}"><fmt:message key="date_of_birth" bundle="${resword}"/></c:if>
		<c:if test="${study.studyParameterConfig.collectDob=='2'}"><fmt:message key="year_of_birth" bundle="${resword}"/></c:if>
		:</td>
	  	<td>
		  <div class="formfieldXL_BG">
		<%--  <c:if test="${study.studyParameterConfig.collectDob=='1'}">
		  <input type="text" name="localBirthDate" size="15" value="<fmt:formatDate value="${subjectToUpdate.dateOfBirth}"  pattern="${dteFormat}"/>" class="formfieldXL">
		  </c:if>
		  <c:if test="${study.studyParameterConfig.collectDob=='2'}">
		  --%>
	  		<input type="text" name="localBirthDate" size="15" value="<c:out value="${localBirthDate}"/>" class="formfieldXL">
	  	<%--  </c:if>--%>
		  </div>
		  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="localBirthDate"/></jsp:include>
	  	</td><td class="formlabel" style="padding-left: 0px;padding-right: 0px;">(
	  	<c:if test="${study.studyParameterConfig.collectDob=='1'}"><fmt:message key="date_format" bundle="${resformat}"/></c:if>
	  	<c:if test="${study.studyParameterConfig.collectDob=='2'}"><fmt:message key="date_format_year" bundle="${resformat}"/></c:if>
	  	) *
	  
	  	<c:if test="${study.studyParameterConfig.discrepancyManagement=='true'}">
		 <a href="#" onClick="openDNoteWindow('CreateDiscrepancyNote?subjectId=${studySubId}&name=subject&id=<c:out value="${subjectToUpdate.id}"/>&field=dateOfBirth&column=date_of_birth','spanAlert-dateOfBirth'); return false;">
		 <img name="flag_dateOfBirth" src="images/<c:out value="${birthDNFlag}"/>.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"></a>
		</c:if>
	  	</td>
	</c:if>
	
	
	
	
	
	
	</table>
</div>
</div></div></div></div></div></div></div></div>

</div>
 <input type="submit" name="Submit" value="<fmt:message key="confirm" bundle="${resword}"/>" class="button_medium">
 <input type="button" onclick="confirmCancel('ListSubject');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/>
</form>

</body>

<jsp:include page="../include/footer.jsp"/>
