<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>

<html>
<head>
<link rel="stylesheet" href="includes/styles.css" type="text/css">
<script language="JavaScript" src="includes/global_functions_javascript.js"></script>
<style type="text/css">

.popup_BG { background-image: url(images/main_BG.gif);
  background-repeat: repeat-x;
  background-position: top;
  background-color: #FFFFFF;
  }

</style>

</head>
<body class="popup_BG">

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='definition' class='org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean'/>
<jsp:useBean scope='request' id='eventDefinitionCRFs' class='java.util.ArrayList'/>
<jsp:useBean scope='request' id='defSize' type='java.lang.Integer'/>

<h1><span class="title_manage" style="margin-left: 20px;"><fmt:message key="view_event_definition" bundle="${resword}"/> </span></h1>

<div style="width: 600px; margin-left: 20px;">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">   
  <tr valign="top"><td class="table_header_column"><fmt:message key="name" bundle="${resword}"/>:</td><td class="table_cell">  
  <c:out value="${definition.name}"/>
   </td></tr>
    <tr valign="top"><td class="table_header_column"><fmt:message key="oid" bundle="${resword}"/>:</td><td class="table_cell">  
  <c:out value="${definition.oid}"/>
   </td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="description" bundle="${resword}"/>:</td><td class="table_cell">  
  <c:out value="${definition.description}"/>&nbsp;
  </td></tr>
 
 <tr valign="top"><td class="table_header_column"><fmt:message key="repeating" bundle="${resword}"/>:</td><td class="table_cell">
  <c:choose>
   <c:when test="${definition.repeating == true}"> <fmt:message key="yes" bundle="${resword}"/> </c:when>
   <c:otherwise> <fmt:message key="no" bundle="${resword}"/> </c:otherwise>
  </c:choose>
  </td></tr>
  
  <tr valign="top"><td class="table_header_column"><fmt:message key="type" bundle="${resword}"/>:</td><td class="table_cell">
    <c:out value="${definition.type}"/>
   </td></tr>
  
  <tr valign="top"><td class="table_header_column"><fmt:message key="category" bundle="${resword}"/>:</td><td class="table_cell">  
  <c:out value="${definition.category}"/>&nbsp;
  </td></tr>
  </table>
  </div>
</div></div></div></div></div></div></div></div>

</div>
<br>
<c:if test="${!empty eventDefinitionCRFs}">
<div class="table_title_manage" style="margin-left: 20px;">
  <fmt:message key="CRFs" bundle="${resword}"/>
</div>
<%--<p><fmt:message key="click_the_up_down_arrow_icons" bundle="${restext}"/></p>--%>
<div style="width: 900px;margin-left: 20px;">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">


<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%"> 
 <tr valign="top"> 
    <td class="table_header_row"><fmt:message key="name" bundle="${resword}"/></td>   
    <td valign="top" class="table_header_row"><fmt:message key="required" bundle="${resword}"/></td>     
    <td valign="top" class="table_header_row"><fmt:message key="double_data_entry" bundle="${resword}"/></td>         
    <td valign="top" class="table_header_row"><fmt:message key="password_required" bundle="${resword}"/></td>
    <!-- <td valign="top" class="table_header_row"><fmt:message key="enforce_decision_conditions" bundle="${restext}"/></td>-->
    <td valign="top" class="table_header_row"><fmt:message key="default_version" bundle="${resword}"/></td>
     <td valign="top" class="table_header_row"><fmt:message key="hidden_crf" bundle="${resword}"/></td>     
    <c:choose>
    <c:when test="${participateFormStatus == 'enabled'}">
     <td valign="top" class="table_header_row"><fmt:message key="participant_form" bundle="${resword}"/></td>     
     <td valign="top" class="table_header_row"><fmt:message key="allow_anonymous_submission" bundle="${resword}"/></td>     
     <td valign="top" class="table_header_row"><fmt:message key="submission_url" bundle="${resword}"/></td>     
     <td valign="top" class="table_header_row"><fmt:message key="offline" bundle="${resword}"/></td>     
    </c:when>  
   </c:choose>

     <td valign="top" class="table_header_row"><fmt:message key="null_values" bundle="${resword}"/></td>    
     <td valign="top" class="table_header_row"><fmt:message key="sdv_option" bundle="${resword}"/></td>
    <td valign="top" class="table_header_row"><fmt:message key="status" bundle="${resword}"/></td>
  </tr>             
 
  <c:set var="prevCrf" value=""/>
  <c:set var="count" value="0"/>
  <c:set var="last" value="${defSize-1}"/>
 <c:forEach var ="crf" items="${eventDefinitionCRFs}" varStatus="status">
  <c:choose>
    <c:when test="${count == last}">
      <c:set var="nextCrf" value="${eventDefinitionCRFs[count]}"/>
    </c:when>  
    <c:otherwise> 
     <c:set var="nextCrf" value="${eventDefinitionCRFs[count+1]}"/>
    </c:otherwise>
  </c:choose>
   <tr valign="top">
    <td class="table_cell"><c:out value="${crf.crfName}"/></td> 
    <td class="table_cell">
    <c:choose>
    <c:when test="${crf.requiredCRF == true}"> <fmt:message key="yes" bundle="${resword}"/> </c:when>
     <c:otherwise> <fmt:message key="no" bundle="${resword}"/> </c:otherwise>
    </c:choose>
   </td>
     
    <td class="table_cell">
     <c:choose>
      <c:when test="${crf.doubleEntry == true}"> <fmt:message key="yes" bundle="${resword}"/> </c:when>
      <c:otherwise> <fmt:message key="no" bundle="${resword}"/> </c:otherwise>
     </c:choose>
    </td>         

    <td class="table_cell">
     <c:choose>
      <c:when test="${crf.electronicSignature == true}"> <fmt:message key="yes" bundle="${resword}"/> </c:when>
      <c:otherwise> <fmt:message key="no" bundle="${resword}"/> </c:otherwise>
     </c:choose>
    </td>

    <%--<td class="table_cell">
     <c:choose>
      <c:when test="${crf.decisionCondition == true}"> <fmt:message key="yes" bundle="${resword}"/> </c:when>
      <c:otherwise> No </c:otherwise>
     </c:choose>
   </td>--%>
  
   <td class="table_cell">   
    <c:out value="${crf.defaultVersionName}"/>     
   </td>

    <td class="table_cell">
     <c:choose>
      <c:when test="${crf.hideCrf == true}"> <fmt:message key="yes" bundle="${resword}"/> </c:when>
      <c:otherwise> <fmt:message key="no" bundle="${resword}"/> </c:otherwise>
     </c:choose>
    </td>

    <c:choose>
      <c:when test="${participateFormStatus == 'enabled'}">
        <td class="table_cell">
          <c:choose>
            <c:when test="${crf.participantForm == true}"> <fmt:message key="yes" bundle="${resword}"/> </c:when>
            <c:otherwise> <fmt:message key="no" bundle="${resword}"/> </c:otherwise>
          </c:choose>
        </td>
        <td class="table_cell">
          <c:choose>
            <c:when test="${crf.participantForm == true}">
              <c:choose>
                <c:when test="${crf.allowAnonymousSubmission == true}"> <fmt:message key="yes" bundle="${resword}"/> </c:when>
                <c:otherwise> <fmt:message key="no" bundle="${resword}"/> </c:otherwise>
              </c:choose>
            </c:when>
          </c:choose>
        </td>
        <td class="table_cell">
          <c:choose>
            <c:when test="${crf.participantForm == true && crf.allowAnonymousSubmission == true}">
              <c:choose>
                <c:when test="${crf.submissionUrl != ''}">
                  <c:out value="${participantUrl}${crf.submissionUrl}"/>
                </c:when>
             </c:choose>
            </c:when>
          </c:choose>
        </td>
        
             <td class="table_cell">
        <c:choose>
          <c:when test="${crf.participantForm == true && crf.allowAnonymousSubmission == true}">
            <c:choose>
              <c:when test="${crf.offline == true}"> 
                <fmt:message key="yes" bundle="${resword}"/> 
              </c:when>
              <c:otherwise> <fmt:message key="no" bundle="${resword}"/> </c:otherwise>
            </c:choose>
          </c:when>
        </c:choose>
      </td>
        
        
      </c:when>
    </c:choose>

   <td class="table_cell"> 
    <c:out value="${crf.nullValues}"/> &nbsp;    
  </td>          
  <td class="table_cell"><fmt:message key="${crf.sourceDataVerification.description}" bundle="${resterm}"/></td> 
   <td class="table_cell"><c:out value="${crf.status.name}"/></td> 
   
   </tr>
   <c:set var="prevCrf" value="${crf}"/>
   <c:set var="count" value="${count+1}"/>
 </c:forEach>
 
</table>
</div>
</div></div></div></div></div></div></div></div>

</div>
</c:if>
 
 
