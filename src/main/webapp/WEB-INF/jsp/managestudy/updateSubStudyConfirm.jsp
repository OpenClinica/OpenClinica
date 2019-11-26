<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<jsp:include page="../include/managestudy-header.jsp"/>


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

<jsp:useBean scope='session' id='newStudy' class='core.org.akaza.openclinica.domain.datamap.Study'/>
<jsp:useBean scope="session" id="parentName" class="java.lang.String"/>
<h1><span class="title_manage">
<fmt:message key="confirm_site_details" bundle="${resword}"/>
</span></h1>

<form action="UpdateSubStudy" method="post">
<input type="hidden" name="action" value="submit">
 <div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr valign="top"><td class="table_header_column"><fmt:message key="parent_name" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${parentName}"/>
  </td></tr>
  
  <tr valign="top"><td class="table_header_column"><fmt:message key="site_name" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.name}"/>
  </td></tr>
  
  <tr valign="top"><td class="table_header_column"><b><fmt:message key="unique_protocol_ID" bundle="${resword}"/></b>:</td><td class="table_cell">
  <c:out value="${newStudy.uniqueIdentifier}"/>
  </td></tr>
  
  <tr valign="top"><td class="table_header_column"><b><fmt:message key="secondary_IDs" bundle="${resword}"/></b>:</td><td class="table_cell">
   <c:out value="${newStudy.secondaryIdentifier}"/>
   </td></tr>  
   
  <tr valign="top"><td class="table_header_column"><fmt:message key="principal_investigator" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.principalInvestigator}"/>
  </td></tr> 
  
  <tr valign="top"><td class="table_header_column"><fmt:message key="brief_summary" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.summary}"/>
  </td></tr>
  
  <tr valign="top"><td class="table_header_column"><fmt:message key="protocol_verification" bundle="${resword}"/>:</td><td class="table_cell">
  <fmt:formatDate value="${newStudy.protocolDateVerification}" pattern="${dteFormat}"/>
  </td></tr> 
   
  <tr valign="top"><td class="table_header_column"><fmt:message key="start_date" bundle="${resword}"/>:</td><td class="table_cell">
  <fmt:formatDate value="${newStudy.datePlannedStart}" pattern="${dteFormat}"/>
  </td></tr> 
  
  <tr valign="top"><td class="table_header_column"><fmt:message key="estimated_completion_date" bundle="${resword}"/>:</td><td class="table_cell">
  <fmt:formatDate value="${newStudy.datePlannedEnd}" pattern="${dteFormat}"/>
  </td></tr> 
  
  <tr valign="top"><td class="table_header_column"><fmt:message key="expected_total_enrollment" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.expectedTotalEnrollment}"/>
  </td></tr> 
  
  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_name" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.facilityName}"/>
  </td></tr> 
  
  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_city" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.facilityCity}"/>
  </td></tr> 
  
  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_state_province" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.facilityState}"/>
  </td></tr> 
  
  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_ZIP" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.facilityZip}"/>
 </td></tr>
  
  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_country" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.facilityCountry}"/>
  </td></tr> 
  
  <!--<tr valign="top"><td class="table_header_column"><fmt:message key="facility_recruitment_status" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.facilityRecruitmentStatus}"/>
 </td></tr>  -->
  
  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_contact_name" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.facilityContactName}"/>
  </td></tr>   
  
  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_contact_degree" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.facilityContactDegree}"/>
  </td></tr> 
  
  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_contact_phone" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.facilityContactPhone}"/>
 </td></tr>       
  
  <tr valign="top"><td class="table_header_column"><fmt:message key="facility_contact_email" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${newStudy.facilityContactEmail}"/>
  </td></tr>  
  
  <c:choose>
   <c:when test="${newStudy.study == null || newStudy.study.studyId == 0}">
      <c:set var="key" value="study_system_status"/>
   </c:when>
   <c:otherwise>
       <c:set var="key" value="site_system_status"/>
   </c:otherwise>
  </c:choose>

  <tr valign="top"><td class="table_header_column"><fmt:message key="${key}" bundle="${resword}"/>:</td><td class="table_cell">
   <%-- <c:out value="${newStudy.status.description}"/> --%> <c:out value="Available"/>
   </td></tr> 
   
   <c:forEach var="config" items="${newStudy.studyParameterValues}">
   <c:choose>
   <c:when test="${config.studyParameter.handle=='collectDOB'}">
     <tr valign="top"><td class="table_header_column"><fmt:message key="collect_subject_date_of_birth" bundle="${resword}"/>:</td><td class="table_cell">
       <c:choose>
         <c:when test="${config.value == '1'}">
          <fmt:message key="yes" bundle="${resword}"/>          
         </c:when>
         <c:when test="${config.value == '2'}">
           
          <fmt:message key="only_year_of_birth" bundle="${resword}"/>
          
         </c:when>
         <c:otherwise>
          <fmt:message key="not_used" bundle="${resword}"/>
         </c:otherwise>
      </c:choose>  
      </td></tr>
   
   </c:when>
    
   <c:when test="${config.studyParameter.handle=='discrepancyManagement'}">
		  <tr valign="top"><td class="table_header_column"><fmt:message key="allow_discrepancy_management" bundle="${resword}"/>:</td><td class="table_cell">
		   <c:choose>
		   <c:when test="${config.value == 'false'}">
		    <fmt:message key="no" bundle="${resword}"/>	
		   </c:when>
		   <c:otherwise>
		   <fmt:message key="yes" bundle="${resword}"/>		 
		   </c:otherwise>
		  </c:choose>
		  </td>
		  </tr>
	</c:when>
	
	<c:when test="${config.studyParameter.handle =='genderRequired'}">
		  <tr valign="top"><td class="table_header_column"><fmt:message key="gender_required" bundle="${resword}"/>:</td><td class="table_cell">
		   <c:choose>
		   <c:when test="${config.value == false}">
		   <fmt:message key="no" bundle="${resword}"/>
		   </c:when>
		   <c:otherwise>
		   <fmt:message key="yes" bundle="${resword}"/>		  
		   </c:otherwise>
		  </c:choose>
		  </td>
		  </tr>
	</c:when>	  
    <c:when test="${config.studyParameter.handle=='subjectPersonIdRequired'}">
		  <tr valign="top"><td class="table_header_column"><fmt:message key="subject_person_ID_required" bundle="${resword}"/>:</td><td class="table_cell">
		   <c:choose>
		   <c:when test="${config.value == 'required'}">
		    <fmt:message key="required" bundle="${resword}"/>		   
		   </c:when>
		    <c:when test="${newStudy.subjectPersonIdRequired == 'optional'}">
		     <fmt:message key="optional" bundle="${resword}"/>		    
		   </c:when>
		   <c:otherwise>
		     <fmt:message key="not_used" bundle="${resword}"/>
		   </c:otherwise>
		  </c:choose>
		  </td>
		  </tr>
	</c:when>
	<c:when test="${config.studyParameter.handle=='subjectIdGeneration'}">
		   <tr valign="top"><td class="table_header_column"><fmt:message key="how_to_generate_the_subject" bundle="${resword}"/>:</td><td class="table_cell">
		   <c:choose>
		   <c:when test="${config.value == 'manual'}">
		     <fmt:message key="manual_entry" bundle="${resword}"/>		  
		   </c:when>
		    <c:when test="${newStudy.subjectPersonIdRequired == 'auto editable'}">
		      <fmt:message key="auto_generated_and_editable" bundle="${resword}"/>		   
		   </c:when>
		   <c:otherwise>
		    <fmt:message key="auto_generated_and_non_editable" bundle="${resword}"/>
		   </c:otherwise>
		  </c:choose>
		  </td>
		  </tr>
	</c:when>
	<c:when test="${config.studyParameter.handle=='subjectIdPrefixSuffix'}">
		   <tr valign="top"><td class="table_header_column"><fmt:message key="generate_subject_ID_automatically" bundle="${resword}"/>:</td><td class="table_cell">
		   <c:choose>
		   <c:when test="${config.value == 'true'}">
		    <fmt:message key="yes" bundle="${resword}"/>		    
		   </c:when>    
		   <c:otherwise>
		    <fmt:message key="no" bundle="${resword}"/>		   
		   </c:otherwise>
		  </c:choose>
		  </td>
		  </tr>
	</c:when>
	<c:when test="${config.studyParameter.handle=='interviewerNameRequired'}">
		   <tr valign="top"><td class="table_header_column"><fmt:message key="when_entering_data_entry_interviewer" bundle="${resword}"/></td><td class="table_cell">
		   <c:choose>
		   <c:when test="${config.value == 'true'}">
		   <fmt:message key="yes" bundle="${resword}"/>
		   
		   </c:when>    
		   <c:otherwise>
		   <fmt:message key="no" bundle="${resword}"/>   
		   </c:otherwise>
		  </c:choose>
		  </td>
		  </tr>
	</c:when>
	<c:when test="${config.studyParameter.handle=='interviewerNameDefault'}">
		  <tr valign="top"><td class="table_header_column"><fmt:message key="interviewer_name_default_as_blank" bundle="${resword}"/></td><td class="table_cell">
		   <c:choose>
		   <c:when test="${config.value == 'blank'}">
		    <fmt:message key="blank" bundle="${resword}"/>
		    
		   </c:when>    
		   <c:otherwise>
		   <fmt:message key="pre_populated_from_active_user" bundle="${resword}"/>   
		   </c:otherwise>
		  </c:choose>
		  </td>
		  </tr>
	</c:when>
	<c:when test="${config.studyParameter.handle=='interviewerNameEditable'}">
		  <tr valign="top"><td class="table_header_column"><fmt:message key="interviewer_name_editable" bundle="${resword}"/></td><td class="table_cell">
		   <c:choose>
		   <c:when test="${config.value == 'true'}">
		    <fmt:message key="yes" bundle="${resword}"/>		    
		   </c:when>    
		   <c:otherwise>
		   <fmt:message key="no" bundle="${resword}"/>   
		   </c:otherwise>
		  </c:choose>
		  </td>
		  </tr>
	</c:when>
	<c:when test="${config.studyParameter.handle=='interviewDateRequired'}">
		  <tr valign="top"><td class="table_header_column"><fmt:message key="interviewer_date_required" bundle="${resword}"/></td><td class="table_cell">
		   <c:choose>
		   <c:when test="${config.value == 'true'}">
		    <fmt:message key="yes" bundle="${resword}"/>		    
		   </c:when>    
		   <c:otherwise>
		    <fmt:message key="no" bundle="${resword}"/>   
		   </c:otherwise>
		  </c:choose>
		  </td>
		  </tr>
    </c:when>		  
	<c:when test="${config.studyParameter.handle =='interviewDateDefault'}">
		  <tr valign="top"><td class="table_header_column"><fmt:message key="interviewer_date_default_as_blank" bundle="${resword}"/></td><td class="table_cell">
		   <c:choose>
		   <c:when test="${config.value == 'blank'}">
		   <fmt:message key="blank" bundle="${resword}"/>
		   
		   </c:when>    
		   <c:otherwise>
		    
		   <fmt:message key="pre_populated_from_SE" bundle="${resword}"/>   
		   </c:otherwise>
		  </c:choose>
		  </td>
		  </tr>
	 </c:when>
	 <c:otherwise>	  
		  <tr valign="top"><td class="table_header_column"><fmt:message key="interviewer_date_editable" bundle="${resword}"/></td><td class="table_cell">
		   <c:choose>
		   <c:when test="${config.value== 'true'}">
		    <fmt:message key="yes" bundle="${resword}"/>		   
		   </c:when>    
		   <c:otherwise>
		    <fmt:message key="no" bundle="${resword}"/>   
		   </c:otherwise>
		  </c:choose>
		  </td>
		  </tr>
     </c:otherwise>
   </c:choose>
   
  </c:forEach>


</table>
</div>
  </div></div></div></div></div></div></div></div>

  </div> 
            
 <input type="submit" name="Submit" value="<fmt:message key="submit_site" bundle="${resword}"/>" class="button_long">
</form>
<br><br>
<jsp:include page="../include/footer.jsp"/>
