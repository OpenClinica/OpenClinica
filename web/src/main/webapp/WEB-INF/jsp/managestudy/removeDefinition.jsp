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
<tr id="sidebar_Instructions_open" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		<div class="sidebar_tab_content">		
        <fmt:message key="confirm_removal_of_this_SED_from_study"  bundle="${resword}"/> <c:out value="${study.name}"/>. <fmt:message key="the_SED_and_all_subject_data_associated_remove"  bundle="${resword}"/>
		</div>

		</td>
	
	</tr>
	<tr id="sidebar_Instructions_closed" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='definitionToRemove' class='org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean'/>
<jsp:useBean scope='request' id='eventDefinitionCRFs' class='java.util.ArrayList'/>
<jsp:useBean scope='request' id='events' class='java.util.ArrayList'/>

<h1><span class="title_manage"><fmt:message key="confirm_removal_of_event_definition"  bundle="${resword}"/> </span></h1>

<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">   
  <tr valign="top"><td class="table_header_column"><fmt:message key="name" bundle="${resword}"/>:</td><td class="table_cell">  
  <c:out value="${definitionToRemove.name}"/>
   </td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="description" bundle="${resword}"/>:</td><td class="table_cell">  
  <c:out value="${definitionToRemove.description}"/>
  </td></tr>
 
 <tr valign="top"><td class="table_header_column"><fmt:message key="repeating" bundle="${resword}"/>:</td><td class="table_cell">
  <c:choose>
   <c:when test="${definitionToRemove.repeating == true}"> <fmt:message key="yes" bundle="${resword}"/> </c:when>
   <c:otherwise> <fmt:message key="no" bundle="${resword}"/> </c:otherwise>
  </c:choose>
  </td></tr>
  
  <tr valign="top"><td class="table_header_column"><fmt:message key="type" bundle="${resword}"/>:</td><td class="table_cell">
    <c:out value="${definitionToRemove.type}"/>
   </td></tr>
  
  <tr valign="top"><td class="table_header_column"><fmt:message key="category" bundle="${resword}"/>:</td><td class="table_cell">  
  <c:out value="${definitionToRemove.category}"/>&nbsp;
  </td></tr>
 </table> 
 </div>
</div></div></div></div></div></div></div></div>
</div> 
 <br>
 <span class="table_title_manage"><fmt:message key="CRFs" bundle="${resword}"/>:</span> 
 <div style="width: 800px">
 <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">  
  <c:forEach var ="crf" items="${eventDefinitionCRFs}">   
   <tr valign="top" bgcolor="#F5F5F5">             
    <td class="table_header_column" colspan="7"><c:out value="${crf.crfName}"/></td> 
    <td class="table_header_column" colspan="1"><c:out value="${crf.status.name}"/></td>      
  </tr>  
   <tr valign="top">   
     
    <td class="table_cell"><fmt:message key="required" bundle="${resword}"/>:
    <c:choose>
    <c:when test="${crf.requiredCRF == true}"> <fmt:message key="yes" bundle="${resword}"/> </c:when>
     <c:otherwise> <fmt:message key="no" bundle="${resword}"/> </c:otherwise>
    </c:choose>
   </td>
     
    <td class="table_cell"><fmt:message key="double_data_entry" bundle="${resword}"/>:
     <c:choose>
      <c:when test="${crf.doubleEntry == true}"> <fmt:message key="yes" bundle="${resword}"/> </c:when>
      <c:otherwise> <fmt:message key="no" bundle="${resword}"/> </c:otherwise>
     </c:choose>
    </td>         
         
    <td class="table_cell"><fmt:message key="enforce_decision_conditions" bundle="${resword}"/>:
     <c:choose>
      <c:when test="${crf.decisionCondition == true}"> <fmt:message key="yes" bundle="${resword}"/> </c:when>
      <c:otherwise> <fmt:message key="no" bundle="${resword}"/> </c:otherwise>
     </c:choose>
   </td>
  
   <td class="table_cell"><fmt:message key="default_version" bundle="${resword}"/>:    
    <c:out value="${crf.defaultVersionName}"/>     
  
  
    <c:choose>
    <c:when test="${participateFormStatus == 'enabled'}">
    <td class="table_cell"><fmt:message key="participant_form" bundle="${resword}"/>:
     <c:choose>
      <c:when test="${crf.participantForm == true}"> <fmt:message key="yes" bundle="${resword}"/> </c:when>
      <c:otherwise> <fmt:message key="no" bundle="${resword}"/> </c:otherwise>
     </c:choose>
   </td>
    <td class="table_cell"><fmt:message key="allow_anonymous_submission" bundle="${resword}"/>:
     <c:choose>
      <c:when test="${crf.allowAnonymousSubmission == true}"> <fmt:message key="yes" bundle="${resword}"/> </c:when>
      <c:otherwise> <fmt:message key="no" bundle="${resword}"/> </c:otherwise>
     </c:choose>
   </td>
   <td class="table_cell"><fmt:message key="submission_url" bundle="${resword}"/>:    
     <c:choose>    
       <c:when test="${crf.submissionUrl != ''}">    
      <c:out value="${participantUrl}${crf.submissionUrl}"/></c:when>   
      <c:otherwise><c:out value="${crf.submissionUrl}"/> </c:otherwise>   
     </c:choose>
   </td>    


    <td class="table_cell"><fmt:message key="offline" bundle="${resword}"/>:
     <c:choose>
      <c:when test="${crf.offline == true}"> <fmt:message key="yes" bundle="${resword}"/> </c:when>
      <c:otherwise> <fmt:message key="no" bundle="${resword}"/> </c:otherwise>
     </c:choose>
   </td>

   </c:when>  
 </c:choose>
  
  
   <td class="table_cell"><fmt:message key="null_values" bundle="${resword}"/>:    
    <c:out value="${crf.nullValues}"/>     
  </td>
  </tr>             
  <tr><td class="table_divider" colspan="8">&nbsp;</td></tr>
 </c:forEach>
 
 </table>

 </div>
</div></div></div></div></div></div></div></div>
</div> 
<br>
 <span class="table_title_manage"><fmt:message key="SE" bundle="${resword}"/>:</span> 
 <div style="width: 600px">
 <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%"> 
<tr valign="top">
  <td class="table_header_column_top"><fmt:message key="study_subject_ID" bundle="${resword}"/></td>   
   <td class="table_header_column_top"><fmt:message key="start_date" bundle="${resword}"/></td>
   <td class="table_header_column_top"><fmt:message key="end_date" bundle="${resword}"/></td>
  </tr>  
  <c:choose> 
  <c:when test="${empty events}">
	  <tr valign="top">
	   <td class="table_cell">
		<fmt:message key="no_study_events_found" bundle="${resword}"/>
	   </td>
	   <td class="table_cell">
    
   </td>
   <td class="table_cell">
    
   </td>
  </tr>  
  </c:when>
  <c:otherwise>
  <c:forEach var="event" items="${events}">
  <tr valign="top">
   <td class="table_cell">
    <c:out value="${event.studySubjectId}"/>
   </td>
   <td class="table_cell">
    <fmt:formatDate value="${event.dateStarted}" pattern="${dteFormat}"/>&nbsp;
   </td>
   <td class="table_cell">
    <fmt:formatDate value="${event.dateEnded}" pattern="${dteFormat}"/>&nbsp;
   </td>
  </tr>  
  </c:forEach>
  </c:otherwise>
  </c:choose>  
 </table>
 
 </div>
</div></div></div></div></div></div></div></div>
</div> 
<br>
<form action='RemoveEventDefinition?action=submit&id=<c:out value="${definitionToRemove.id}"/>' method="POST">
<input type="submit" name="Submit" value="<fmt:message key="remove_event_definition" bundle="${resword}"/>" class="button_xlong" onClick='return confirm("<fmt:message key="if_you_remove_this_definition" bundle="${resword}"/>");'>
<input type="button" onclick="confirmCancel('ListEventDefinition');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/>    
</form>

<c:import url="../include/workflow.jsp">
  <c:param name="module" value="manage"/>
</c:import>
<jsp:include page="../include/footer.jsp"/>
