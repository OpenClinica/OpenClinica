<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>
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


<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='definition' class='org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean'/>
<jsp:useBean scope='request' id='eventDefinitionCRFs' class='java.util.ArrayList'/>
<jsp:useBean scope='request' id='defSize' type='java.lang.Integer'/>

<h1><span class="title_manage"><fmt:message key="view_event_definition" bundle="${resword}"/> </span></h1>

<div style="width: 600px">
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
<div class="table_title_manage">
  <fmt:message key="CRFs" bundle="${resword}"/>
</div>
<p><fmt:message key="click_the_up_down_arrow_icons" bundle="${restext}"/></p>
    <div style="float:right;padding-right:6px;width:8%">
       <a href="javascript:processPrintCRFRequest('rest/metadata/html/print/<c:out value="${study.oid}"/>/<c:out value="${definition.oid}"/>/*')"
       onMouseDown="javascript:setImage('bt_Print1','images/bt_Print_d.gif');"
       onMouseUp="javascript:setImage('bt_Print1','images/bt_Print.gif');"><img
       name="bt_Print1" src="images/bt_Print.gif" border="0" alt="<fmt:message key="print_all_available_crf" bundle="${resword}"/>" title="<fmt:message key="print_all_available_crf" bundle="${resword}"/>" align="left" hspace="6"></a>
   </div>
    &nbsp;
    &nbsp;
<p>
    
</p>
<div style="width: 1000px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">


<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%"> 
 <tr valign="top"> 
    <td class="table_header_row_left"><fmt:message key="order" bundle="${resword}"/></td>              
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
    <td valign="top" class="table_header_row"><fmt:message key="actions" bundle="${resword}"/></td>
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
    <td class="table_cell_left">
      <c:choose>
        <c:when test="${status.first}">
          <c:choose>
           <c:when test="${defSize>1}">
               <a href="ChangeDefinitionCRFOrdinal?current=<c:out value="${nextCrf.id}"/>&previous=<c:out value="${crf.id}"/>&id=<c:out value="${definition.id}"/>&currentOrdinal=<c:out value="${nextCrf.ordinal}"/>&previousOrdinal=<c:out value="${crf.ordinal}"/>"><img src="images/bt_sort_descending.gif" border="0" alt="move down" title="move down"/></a>
           </c:when>
           <c:otherwise>
             &nbsp;
           </c:otherwise>
          </c:choose>         
        </c:when>
        <c:when test="${status.last}">
            <a href="ChangeDefinitionCRFOrdinal?current=<c:out value="${crf.id}"/>&previous=<c:out value="${prevCrf.id}"/>&id=<c:out value="${definition.id}"/>&currentOrdinal=<c:out value="${crf.ordinal}"/>&previousOrdinal=<c:out value="${prevCrf.ordinal}"/>"><img src="images/bt_sort_ascending.gif" alt="move up" title="move up" border="0"/></a>
        </c:when>
        <c:otherwise>
            <a href="ChangeDefinitionCRFOrdinal?current=<c:out value="${crf.id}"/>&previous=<c:out value="${prevCrf.id}"/>&id=<c:out value="${definition.id}"/>&currentOrdinal=<c:out value="${crf.ordinal}"/>&previousOrdinal=<c:out value="${prevCrf.ordinal}"/>"><img src="images/bt_sort_ascending.gif" alt="move up" title="move up" border="0" /></a>
            <a href="ChangeDefinitionCRFOrdinal?previous=<c:out value="${crf.id}"/>&current=<c:out value="${nextCrf.id}"/>&id=<c:out value="${definition.id}"/>&previousOrdinal=<c:out value="${crf.ordinal}"/>&currentOrdinal=<c:out value="${nextCrf.ordinal}"/>"><img src="images/bt_sort_descending.gif" alt="move down" title="move up" border="0" /></a>
        </c:otherwise>
      </c:choose>
    </td>             
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
      <c:otherwise> No </c:otherwise>
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
              <c:when test="${crf.allowAnonymousSubmission == true}">
               <fmt:message key="yes" bundle="${resword}"/> </c:when>
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
   <td class="table_cell">
     <table border="0" cellpadding="0" cellspacing="0">
	  <tr>       
        <td>
          <!-- <a href="ViewTableOfContent?crfVersionId=<c:out value="${crf.defaultVersionId}"/>&sedId=<c:out value="${definition.id}"/>"
			onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
			onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><img 
			name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view_CRF_version" bundle="${resword}"/>" title="<fmt:message key="view_CRF_version" bundle="${resword}"/>" align="left" hspace="6"></a>-->
		  <!--<a href="ViewSectionDataEntry?module=<c:out value="${module}"/>&crfId=<c:out value="${crf.crfId}"/>&crfVersionId=<c:out value="${crf.defaultVersionId}"/>&tabId=1"
               onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
               onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><img
              name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6"></a>-->
            <a href="ViewCRF?crfId=<c:out value="${crf.crfId}"/>"
                 onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
                 onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><img
                name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6"></a>

        </td>
		<%--
		<c:if test="${crf.status.id==1 && crf.owner.id==userBean.id}">
		<td>
		 <a href="InitUpdateCRF?crfId=<c:out value="${crf.crfId}"/>"
			onMouseDown="javascript:setImage('bt_Edit1','images/bt_Edit_d.gif');"
			onMouseUp="javascript:setImage('bt_Edit1','images/bt_Edit.gif');"><img 
			name="bt_Edit1" src="images/bt_Edit.gif" border="0" alt="<fmt:message key="edit_CRF" bundle="${resword}"/>" title="<fmt:message key="edit_CRF" bundle="${resword}"/>" align="left" hspace="6"></a>
		  
		</td>
		</c:if>		
		--%>
	  </tr>
	 </table> 	
   </td>
   </tr>
   <c:set var="prevCrf" value="${crf}"/>
   <c:set var="count" value="${count+1}"/>
 </c:forEach>
 
</table>
</div>
</div></div></div></div></div></div></div></div>

</div>
</c:if>
<p><a href="ListEventDefinition"><fmt:message key="go_back_to_definition_list" bundle="${resword}"/></a></p>  
 
 <c:import url="../include/workflow.jsp">
   <c:param name="module" value="manage"/> 
 </c:import>
 
   
<jsp:include page="../include/footer.jsp"/>
