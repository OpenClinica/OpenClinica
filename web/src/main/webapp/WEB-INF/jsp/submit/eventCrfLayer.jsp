<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>

<c:set var="eblRowCount" value="${param.rowCount}" />
<c:set var="count" value="${param.colCount}" />
<c:set var="eventId" value="${param.eventId}" />
<c:set var="eventCrfId" value="${param.crfId}" />
<c:set var="subjectId" value="${param.subjectId}" />
<c:set var="studySubjectject_subjectId" value="${param.studySubjectject_subjectId}" />
<c:set var="studySubjectjectId" value="${param.studySubjectjectId}"/>
<c:set var="studySubjectjectStatusId" value="${param.studySubjectjectStatusId}" />
<c:set var="crfVersionId" value="${param.crfVersionId}" />
<c:set var="edcId" value="${param.edcId}" />
<c:set var="crfStatus" value="${param.crfStatus}" />
<c:set var="crfName" value="${param.crfName}" />
<c:set var="subjectName" value="${param.subjectName}" />
<c:set var="module" value="${param.module}" />

<c:set var="no_startedi18n"><fmt:message key="not_started" bundle="${resword}"/></c:set>
<c:set var="data_entry_completei18n"><fmt:message key="data_entry_complete" bundle="${resterm}"/></c:set>
<c:set var="initial_data_entry_completei18n"><fmt:message key="initial_data_entry_complete" bundle="${resterm}"/></c:set>
<c:set var="double_data_entryi18n"><fmt:message key="double_data_entry" bundle="${resterm}"/></c:set>
<c:set var="lockedi18n"><fmt:message key="locked" bundle="${resterm}"/></c:set>


<c:if test="${studySubjectjectStatusId == 5 || studySubjectjectStatusId == 7}">
	<c:set var="crfStatus" value="invalid"/>
</c:if>
<div id="Lock_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;">
		
 <c:choose>
  <c:when test="${crfStatus ==data_entry_completei18n ||crfStatus =='administrative editing'}">
	<!-- administrative editing -->								
   <a href="javascript:leftnavExpand('Menu_on_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>'); javascript:leftnavExpand('Menu_off_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');" onMouseOver="layersShowOrHide('visible','Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
		javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_Complete_collapse.gif');" 
	    onClick="layersShowOrHide('hidden','Lock_all'); 
		layersShowOrHide('hidden','Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
		layersShowOrHide('hidden','Lock_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
		javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_Complete.gif');"><img src="images/spacer.gif" width="144" height="30" border="0"></a>

		
  </c:when>
  
  <c:when test="${crfStatus ==initial_data_entry_completei18n}">
	<!-- initial data entry completed -->							
   <a href="javascript:leftnavExpand('Menu_on_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>'); javascript:leftnavExpand('Menu_off_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');" onMouseOver="layersShowOrHide('visible','Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
		javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_InitialDEcomplete_collapse.gif');" 
	    onClick="layersShowOrHide('hidden','Lock_all'); 
		layersShowOrHide('hidden','Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
		layersShowOrHide('hidden','Lock_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
		javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_InitialDEcomplete.gif');"><img src="images/spacer.gif" width="144" height="30" border="0"></a>

		
  </c:when>
  
  <c:when test="${crfStatus ==double_data_entryi18n}">
	<!-- double data entry -->							
   <a href="javascript:leftnavExpand('Menu_on_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>'); javascript:leftnavExpand('Menu_off_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');" onMouseOver="layersShowOrHide('visible','Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
		javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_DDE_collapse.gif');" 
	    onClick="layersShowOrHide('hidden','Lock_all'); 
		layersShowOrHide('hidden','Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
		layersShowOrHide('hidden','Lock_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
		javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_DDE.gif');"><img src="images/spacer.gif" width="144" height="30" border="0"></a>

		
  </c:when>
   
  <c:when test="${crfStatus ==lockedi18n }">
	<!-- locked, we show nothing -->						       
  </c:when>
  
  <c:when test="${crfStatus ==no_startedi18n }">	
  <!-- we have not started -->
   <a href="javascript:leftnavExpand('Menu_on_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>'); javascript:leftnavExpand('Menu_off_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');" onMouseOver="layersShowOrHide('visible','Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
		javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_Scheduled.gif');" 
	    onClick="layersShowOrHide('hidden','Lock_all'); 
		layersShowOrHide('hidden','Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
		layersShowOrHide('hidden','Lock_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
		javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_Scheduled.gif');"><img src="images/spacer.gif" width="144" height="30" border="0"></a>
  
  </c:when>
  <c:when test="${crfStatus =='invalid' }">
  <!-- this is invalid -->	
   <a href="javascript:leftnavExpand('Menu_on_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>'); javascript:leftnavExpand('Menu_off_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');" onMouseOver="layersShowOrHide('visible','Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
		javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_Invalid_collapse.gif');" 
	    onClick="layersShowOrHide('hidden','Lock_all'); 
		layersShowOrHide('hidden','Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
		layersShowOrHide('hidden','Lock_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
		javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_Invalid.gif');"><img src="images/spacer.gif" width="144" height="30" border="0"></a>
  
  </c:when>
  <c:otherwise>
  <!-- all other cases, check to see here -->
    <a href="javascript:leftnavExpand('Menu_on_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>'); javascript:leftnavExpand('Menu_off_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');" onMouseOver="layersShowOrHide('visible','Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
		javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_Started.gif');" 
	    onClick="layersShowOrHide('hidden','Lock_all'); 
		layersShowOrHide('hidden','Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
		layersShowOrHide('hidden','Lock_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
		javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_Started.gif');"><img src="images/spacer.gif" width="144" height="30" border="0"></a>
  
  </c:otherwise>
  </c:choose>
</div>
		
<div id="Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>" style="position: absolute; visibility: hidden; z-index: 3; width: 180px; top: 0px;">
		
			<!-- These DIVs define shaded box borders -->
			<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
		
			<div class="tablebox_center">
			<div class="ViewSubjectsPopup" style="color:#5b5b5b">
		
					  <table border="0" cellpadding="0" cellspacing="0" width="100%">		
						<tr valign="top">
						<td class="table_header_row_left">
						<fmt:message key="subject" bundle="${resword}"/>: <c:out value="${subjectName}"/>
		
						<br>
		
						<fmt:message key="CRF" bundle="${resword}"/>: <c:out value="${crfName}"/>

		
						<br>
						<c:choose>
		                  <c:when test="${crfStatus== 'invalid'}">
						   <fmt:message key="status" bundle="${resword}"/>: <fmt:message key="removed" bundle="${resword}"/>
						  </c:when>
						  <c:otherwise>
						     <fmt:message key="status" bundle="${resword}"/>: <c:out value="${crfStatus}"/>
						  </c:otherwise>
		                </c:choose>
						</td>
						
						 <c:choose>
                         <c:when test="${crfStatus ==data_entry_completei18n ||
                         crfStatus =='administrative editing'}">
						    <td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>'); javascript:leftnavExpand('Menu_off_1_6_1');" 
										onClick="layersShowOrHide('hidden','Lock_all'); 
											layersShowOrHide('hidden','Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
											layersShowOrHide('hidden','Lock_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
											javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_Complete.gif');">X</a></td>

														
							
					     </c:when>
					     <c:when test="${crfStatus ==initial_data_entry_completei18n}">
						    <td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>'); javascript:leftnavExpand('Menu_off_1_6_1');" 
										onClick="layersShowOrHide('hidden','Lock_all'); 
											layersShowOrHide('hidden','Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
											layersShowOrHide('hidden','Lock_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
											javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_InitialDEcomplete.gif');">X</a></td>

														
							
					     </c:when>
					     <c:when test="${crfStatus ==double_data_entryi18n}">
						    <td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>'); javascript:leftnavExpand('Menu_off_1_6_1');" 
										onClick="layersShowOrHide('hidden','Lock_all'); 
											layersShowOrHide('hidden','Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
											layersShowOrHide('hidden','Lock_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
											javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_DDE.gif');">X</a></td>

														
							
					     </c:when>
		                 <c:when test="${crfStatus ==lockedi18n }">
							       
		                 </c:when>
		                 <c:when test="${crfStatus ==no_startedi18n }">	
		                   <td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>'); javascript:leftnavExpand('Menu_off_1_6_1');" 
										onClick="layersShowOrHide('hidden','Lock_all'); 
											layersShowOrHide('hidden','Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
											layersShowOrHide('hidden','Lock_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
											javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_Scheduled.gif');">X</a></td>
		                   
		                 </c:when>
		                 <c:when test="${crfStatus =='invalid' }">	
		                   <td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>'); javascript:leftnavExpand('Menu_off_1_6_1');" 
										onClick="layersShowOrHide('hidden','Lock_all'); 
											layersShowOrHide('hidden','Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
											layersShowOrHide('hidden','Lock_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
											javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_Invalid.gif');">X</a></td>
		                   
		                 </c:when>
		                 <c:otherwise>
		                  <td class="table_header_row_left" align="right"><a href="javascript:leftnavExpand('Menu_on_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>'); javascript:leftnavExpand('Menu_off_1_6_1');" 
										onClick="layersShowOrHide('hidden','Lock_all'); 
											layersShowOrHide('hidden','Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
											layersShowOrHide('hidden','Lock_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
											javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_Started.gif');">X</a></td>
		                  
		   			
		                 </c:otherwise>
		  
		             </c:choose>			
						</tr>	
						<tr id="Menu_off_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>" style="display: all">
										<td class="table_cell_left" colspan="2">
										<c:choose>
										 <c:when test="${crfStatus==no_startedi18n}">	
										  <c:choose>
										    <c:when test="${eventId>0}">
										      <i><fmt:message key="click_to_enter_data" bundle="${restext}"/><br><fmt:message key="to_use_another_version_click" bundle="${restext}"/></i>
										 
										    </c:when>
										    <c:otherwise>
										     <i> <fmt:message key="in_order_to_enter_data_create_event" bundle="${restext}"/></i>
										    </c:otherwise>
										  </c:choose>									  
										  
										 </c:when>
										 <c:when test="${eventId<=0 && crfStatus==no_startedi18n}">										  
										  <i><fmt:message key="click_to_enter_data" bundle="${restext}"/><br><fmt:message key="to_use_another_version_click" bundle="${restext}"/></i>
										 </c:when>
										 <c:otherwise>
										   <i><fmt:message key="click_for_more_options" bundle="${restext}"/></i>
										 </c:otherwise>
										</c:choose> 
										
										</td>
						</tr>
						<tr id="Menu_on_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>" style="display: none">
							<td colspan="2">
							<table border="0" cellpadding="0" cellspacing="0" width="100%">		
							   <c:choose>
							    <c:when test="${crfStatus ==data_entry_completei18n ||  crfStatus =='administrative editing'}">
						    	      <tr valign="top"><td class="table_cell_left"><a href="ViewEventCRFContent?id=<c:out value="${subjectId}"/>&ecId=<c:out value="${eventCrfId}"/>&eventId=<c:out value="${eventId}"/>"><img src="images/bt_View.gif" border="0" align="left"></a>&nbsp;&nbsp;
							          <a href="ViewSectionDataEntry?eventDefinitionCRFId=<c:out value="${edcId}"/>&ecId=<c:out value="${eventCrfId}"/>&tabId=1"><fmt:message key="view" bundle="${resword}"/></a></td></tr>
								      <tr valign="top"><td class="table_cell_left">
    <a href="javascript:openPrintCRFWindow('rest/clinicaldata/html/print/<c:out value="${study.oid}"/>/<c:out value="${studySubject.oid}"/>/<c:out value="${currRow.bean.studyEvent.studyEventDefinition.oid}"/>/<c:out value="${dec.eventCRF.crfVersion.oid}"/>')"
								      
								      <img src="images/bt_Print.gif" border="0" align="left"></a>&nbsp;&nbsp;
    <a href="javascript:openPrintCRFWindow('rest/clinicaldata/html/print/<c:out value="${study.oid}"/>/<c:out value="${studySubject.oid}"/>/<c:out value="${currRow.bean.studyEvent.studyEventDefinition.oid}"/>/<c:out value="${dec.eventCRF.crfVersion.oid}"/>')"
								      
								      <fmt:message key="print" bundle="${resword}"/></a></td></tr>
                                    <!-- New Statement for locked or frozen study -->
                                      <c:if test="${study.status.available}">
    								    <c:if test="${userRole.director || userBean.sysAdmin}">
                                           <tr valign="top"><td class="table_cell_left"><a href="AdministrativeEditing?eventCRFId=<c:out value="${eventCrfId}"/>">
		        						   <img src="images/bt_Edit.gif" border="0" align="left"></a>&nbsp;&nbsp; <a href="AdministrativeEditing?eventCRFId=<c:out value="${eventCrfId}"/>"><fmt:message key="edit" bundle="${resword}"/></a></td></tr>
				        				   <!--
						        		   <tr valign="top"><td class="table_cell_left"><a href="">
        								   <img src="images/bt_Lock.gif" border="0" align="left"></a>&nbsp;&nbsp; <a href="">Lock</a></td></tr>
		        						   -->
				        				   <tr valign="top"><td class="table_cell_left"><a href="RemoveEventCRF?action=confirm&id=<c:out value="${eventCrfId}"/>&studySubjectId=<c:out value="${subjectId}"/>">
						        		   <img src="images/bt_Remove.gif" border="0" align="left"></a>&nbsp;&nbsp; <a href="RemoveEventCRF?action=confirm&id=<c:out value="${eventCrfId}"/>&studySubjectId=<c:out value="${subjectId}"/>"><fmt:message key="remove" bundle="${resword}"/></a></td></tr>
	    							    </c:if>
                                        <c:if test="${userBean.sysAdmin && (study.status.available)}">
			        					   <tr valign="top"><td class="table_cell_left">
                                               <a href="DeleteEventCRF?action=confirm&ssId=<c:out value="${subjectId}"/>&ecId=<c:out value="${eventCrfId}"/>">
                                                   <img src="images/bt_Delete.gif" border="0" align="left"></a>&nbsp;&nbsp;
                                               <a href="DeleteEventCRF?action=confirm&ssId=<c:out value="${subjectId}"/>&ecId=<c:out value="${eventCrfId}"/>">
                                               <fmt:message key="delete" bundle="${resword}"/></a></td></tr>
                                        </c:if>
                                    </c:if>
							    </c:when>
							    <c:when test="${crfStatus ==lockedi18n}">
							        <tr valign="top"><td class="table_cell_left"><a href="ViewEventCRFContent?id=<c:out value="${subjectId}"/>&ecId=<c:out value="${eventCrfId}"/>&eventId=<c:out value="${eventId}"/>"><img src="images/bt_View.gif" border="0" align="left"></a>&nbsp;&nbsp; 
							         <a href="ViewSectionDataEntry?eventDefinitionCRFId=<c:out value="${edcId}"/>&ecId=<c:out value="${eventCrfId}"/>&tabId=1"><fmt:message key="view" bundle="${resword}"/></a></td></tr>
								  
									<tr valign="top"><td class="table_cell_left"><a href="ViewEventCRFContent?id=<c:out value="${subjectId}"/>&ecId=<c:out value="${eventCrfId}"/>&eventId=<c:out value="${eventId}"/>"><img src="images/bt_Print.gif" border="0" align="left"></a>&nbsp;&nbsp; <a href="ViewEventCRFContent?id=<c:out value="${subjectId}"/>&ecId=<c:out value="${eventCrfId}"/>&eventId=<c:out value="${eventId}"/>"><fmt:message key="print" bundle="${resword}"/></a></td></tr>
							        <c:if test="${(userRole.director || userBean.sysAdmin) && (study.status.available)}">
							          <tr valign="top"><td class="table_cell_left"><a href="RemoveEventCRF?action=confirm&id=<c:out value="${eventCrfId}"/>&studySubjectId=<c:out value="${subjectId}"/>"><img src="images/bt_Remove.gif" border="0" align="left"></a>&nbsp;&nbsp; <a href="RemoveEventCRF?action=confirm&id=<c:out value="${eventCrfId}"/>&studySubjectId=<c:out value="${subjectId}"/>"><fmt:message key="remove" bundle="${resword}"/></a></td></tr>
						 	        </c:if>
						 	    </c:when>
						 	    <c:when test="${crfStatus ==no_startedi18n }">
						 	            <c:if test="${eventId>0 && !userRole.monitor && study.status.available}">
										      <tr valign="top"><td class="table_cell_left"><a href="InitialDataEntry?<c:out value="eventDefinitionCRFId=${edcId}&studyEventId=${eventId}&subjectId=${studySubjectject_subjectId}&eventCRFId=${eventCrfId}&crfVersionId=${crfVersionId}"/>"><img src="images/bt_Edit.gif" border="0" align="left"></a>&nbsp;&nbsp; <a href="InitialDataEntry?<c:out value="eventDefinitionCRFId=${edcId}&studyEventId=${eventId}&subjectId=${studySubjectject_subjectId}&eventCRFId=${eventCrfId}&crfVersionId=${crfVersionId}"/>"><fmt:message key="enter_data" bundle="${resword}"/></a></td></tr>
						 	    		</c:if>
						 	           <tr valign="top"><td class="table_cell_left"><a href="ViewSectionDataEntry?eventDefinitionCRFId=<c:out value="${edcId}"/>&crfVersionId=<c:out value="${crfVersionId}"/>&tabId=1"><img src="images/bt_View.gif" border="0" align="left"></a>&nbsp;&nbsp;
							           <a href="ViewSectionDataEntry?eventDefinitionCRFId=<c:out value="${edcId}"/>&crfVersionId=<c:out value="${crfVersionId}"/>&tabId=1"><fmt:message key="view" bundle="${resword}"/></a></td></tr>
								       <tr valign="top"><td class="table_cell_left">
    <a href="javascript:openPrintCRFWindow('rest/clinicaldata/html/print/<c:out value="${study.oid}"/>/<c:out value="${studySubject.oid}"/>/<c:out value="${currRow.bean.studyEvent.studyEventDefinition.oid}"/>/<c:out value="${dec.eventCRF.crfVersion.oid}"/>')"
								       <img src="images/bt_Print.gif" border="0" align="left"></a>&nbsp;&nbsp; 
    <a href="javascript:openPrintCRFWindow('rest/clinicaldata/html/print/<c:out value="${study.oid}"/>/<c:out value="${studySubject.oid}"/>/<c:out value="${currRow.bean.studyEvent.studyEventDefinition.oid}"/>/<c:out value="${dec.eventCRF.crfVersion.oid}"/>')"
								       <fmt:message key="print" bundle="${resword}"/></a></td></tr>
						 	    </c:when>
						 	    <c:when test="${crfStatus =='invalid' }">
						 	           <tr valign="top"><td class="table_cell_left"><a href="ViewSectionDataEntry?eventDefinitionCRFId=<c:out value="${edcId}"/>&ecId=<c:out value="${eventCrfId}"/>&tabId=1"><img src="images/bt_View.gif" border="0" align="left"></a>&nbsp;&nbsp;
							           <a href="ViewSectionDataEntry?eventDefinitionCRFId=<c:out value="${edcId}"/>&ecId=<c:out value="${eventCrfId}"/>&tabId=1"><fmt:message key="view" bundle="${resword}"/></a></td></tr>
								       <tr valign="top"><td class="table_cell_left">
    <a href="javascript:openPrintCRFWindow('rest/clinicaldata/html/print/<c:out value="${study.oid}"/>/<c:out value="${studySubject.oid}"/>/<c:out value="${currRow.bean.studyEvent.studyEventDefinition.oid}"/>/<c:out value="${dec.eventCRF.crfVersion.oid}"/>')"
								       
								       
								       <img src="images/bt_Print.gif" border="0" align="left"></a>&nbsp;&nbsp;
    <a href="javascript:openPrintCRFWindow('rest/clinicaldata/html/print/<c:out value="${study.oid}"/>/<c:out value="${studySubject.oid}"/>/<c:out value="${currRow.bean.studyEvent.studyEventDefinition.oid}"/>/<c:out value="${dec.eventCRF.crfVersion.oid}"/>')"
								       
								       
								       <fmt:message key="print" bundle="${resword}"/></a></td></tr>
								    <c:if test="${studySubjectjectStatusId != 5 && studySubjectjectStatusId != 7}">
									    <c:if test="${userRole.director || userBean.sysAdmin}">
										<tr valign="top"><td class="table_cell_left"><a href="RestoreEventCRF?action=confirm&id=<c:out value="${eventCrfId}"/>&studySubjectId=<c:out value="${studySubjectjectId}"/>">
										<img src="images/bt_Restore.gif" border="0" align="left"></a>&nbsp;&nbsp; <a href="RestoreEventCRF?action=confirm&id=<c:out value="${eventCrfId}"/>&studySubjectId=<c:out value="${studySubjectjectId}"/>"><fmt:message key="restore" bundle="${resword}"/></a></td></tr>
									   </c:if>
								   </c:if>
						 	    </c:when>
							    <c:otherwise>
							      <c:if test="${(!userRole.monitor) && (study.status.available)}">
							      <c:choose>
							        <c:when test="${crfStatus==initial_data_entry_completei18n || crfStatus==double_data_entryi18n}">
    									<tr valign="top"><td class="table_cell_left"><a href="DoubleDataEntry?eventCRFId=<c:out value="${eventCrfId}" />">
	    								    <img src="images/bt_Edit.gif" border="0" align="left"></a>&nbsp;&nbsp;
                                            <a href="DoubleDataEntry?eventCRFId=<c:out value="${eventCrfId}" />">
                                            <fmt:message key="enter_data" bundle="${resword}"/></a></td></tr>
				    				</c:when>
					    			<c:otherwise>
						    			 <tr valign="top"><td class="table_cell_left">
                                             <a href="InitialDataEntry?eventCRFId=<c:out value="${eventCrfId}" />">
								    	     <img src="images/bt_Edit.gif" border="0" align="left"></a>&nbsp;&nbsp;
                                             <a href="InitialDataEntry?eventCRFId=<c:out value="${eventCrfId}" />">
                                             <fmt:message key="enter_data" bundle="${resword}"/></a></td>
                                        </tr>
									</c:otherwise>
									
								 </c:choose>	
								 </c:if>
									<%--<tr valign="top"><td class="table_cell_left"><a href="MarkEventCRFComplete?eventCRFId=<c:out value="${eventCrfId}" />"><img src="images/icon_DEcomplete.gif" border="0" align="left"></a>&nbsp;&nbsp; <a href="MarkEventCRFComplete?eventCRFId=<c:out value="${eventCrfId}" />" onClick="javascript:return confirm('Click OK to mark the <c:out value="${crfName}"/> completed for Subject <c:out value="${subjectName}"/>.');">Mark Complete</a></td></tr>--%>
									
									<tr valign="top"><td class="table_cell_left"><a href="ViewSectionDataEntry?eventDefinitionCRFId=<c:out value="${edcId}"/>&ecId=<c:out value="${eventCrfId}"/>&tabId=1"><img src="images/bt_View.gif" border="0" align="left"></a>&nbsp;&nbsp;
							         <a href="ViewSectionDataEntry?eventDefinitionCRFId=<c:out value="${edcId}"/>&ecId=<c:out value="${eventCrfId}"/>&tabId=1"><fmt:message key="view" bundle="${resword}"/></a></td></tr>
								  
									<tr valign="top"><td class="table_cell_left">
    <a href="javascript:openPrintCRFWindow('rest/clinicaldata/html/print/<c:out value="${study.oid}"/>/<c:out value="${studySubject.oid}"/>/<c:out value="${currRow.bean.studyEvent.studyEventDefinition.oid}"/>/<c:out value="${dec.eventCRF.crfVersion.oid}"/>')"
									
									<img src="images/bt_Print.gif" border="0" align="left"></a>&nbsp;&nbsp; 
    <a href="javascript:openPrintCRFWindow('rest/clinicaldata/html/print/<c:out value="${study.oid}"/>/<c:out value="${studySubject.oid}"/>/<c:out value="${currRow.bean.studyEvent.studyEventDefinition.oid}"/>/<c:out value="${dec.eventCRF.crfVersion.oid}"/>')"
									
									<fmt:message key="print" bundle="${resword}"/></a></td></tr>
								  
								   <c:if test="${(userRole.director || userBean.sysAdmin) && (study.status.available)}">
									<tr valign="top"><td class="table_cell_left"><a href="RemoveEventCRF?action=confirm&id=<c:out value="${eventCrfId}"/>&studySubjectId=<c:out value="${subjectId}"/>">
									<img src="images/bt_Remove.gif" border="0" align="left"></a>&nbsp;&nbsp; <a href="RemoveEventCRF?action=confirm&id=<c:out value="${eventCrfId}"/>&studySubjectId=<c:out value="${subjectId}"/>"><fmt:message key="remove" bundle="${resword}"/></a></td></tr>
								   </c:if>
								   
								   <c:if test="${userBean.sysAdmin && (study.status.available)}">
								    <tr valign="top"><td class="table_cell_left"><a href="DeleteEventCRF?action=confirm&ssId=<c:out value="${subjectId}"/>&ecId=<c:out value="${eventCrfId}"/>">
								    <img src="images/bt_Delete.gif" border="0" align="left"></a>&nbsp;&nbsp; 
								    <a href="DeleteEventCRF?action=confirm&ssId=<c:out value="${subjectId}"/>&ecId=<c:out value="${eventCrfId}"/>"><fmt:message key="delete" bundle="${resword}"/></a></td></tr>
                                  </c:if>
						 	    
								</c:otherwise>
						 	   </c:choose>
						 	</table>
							</td>
						</tr>
					  </table>
		
			</div>
			</div>
		
			</div></div></div></div></div></div></div></div>
		  </div>
         
         <c:choose>
         <c:when test="${crfStatus ==data_entry_completei18n ||  crfStatus =='administrative editing'}">
          <a href="javascript:leftnavExpand('Menu_on_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>'); javascript:leftnavExpand('Menu_off_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');" 
							onmouseover="moveObject('Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>', event);
							javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_Complete_expand.gif');"
							onmouseout="layersShowOrHide('hidden','Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
							javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_Complete.gif');"
							onClick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>', event);">
		  
		  </c:when>
		  <c:when test="${crfStatus ==initial_data_entry_completei18n}">
          <a href="javascript:leftnavExpand('Menu_on_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>'); javascript:leftnavExpand('Menu_off_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');" 
							onmouseover="moveObject('Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>', event);
							javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_InitialDEcomplete_expand.gif');"
							onmouseout="layersShowOrHide('hidden','Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
							javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_InitialDEcomplete.gif');"
							onClick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>', event);">
		  
		  </c:when>
		  <c:when test="${crfStatus ==double_data_entryi18n}">
          <a href="javascript:leftnavExpand('Menu_on_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>'); javascript:leftnavExpand('Menu_off_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');" 
							onmouseover="moveObject('Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>', event);
							javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_DDE_expand.gif');"
							onmouseout="layersShowOrHide('hidden','Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
							javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_DDE.gif');"
							onClick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>', event);">
		  
		  </c:when>
		  <c:when test="${crfStatus ==lockedi18n }">
							       
		 </c:when>
		 <c:when test="${crfStatus ==no_startedi18n }">
			<a href="javascript:leftnavExpand('Menu_on_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>'); javascript:leftnavExpand('Menu_off_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');" 
							onmouseover="moveObject('Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>', event);
							javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_Scheduled.gif');"
							onmouseout="layersShowOrHide('hidden','Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
							javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_Scheduled.gif');"
							onClick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>', event);">
		  				    
		 </c:when>
		 <c:when test="${crfStatus =='invalid' }">
			<a href="javascript:leftnavExpand('Menu_on_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>'); javascript:leftnavExpand('Menu_off_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');" 
							onmouseover="moveObject('Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>', event);
							javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_Invalid_expand.gif');"
							onmouseout="layersShowOrHide('hidden','Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
							javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_Invalid.gif');"
							onClick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>', event);">
		  				    
		 </c:when>
		 <c:otherwise>
		   <a href="javascript:leftnavExpand('Menu_on_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>'); javascript:leftnavExpand('Menu_off_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');" 
							onmouseover="moveObject('Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>', event);
							javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_Started.gif');"
							onmouseout="layersShowOrHide('hidden','Event_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>');
							javascript:setImage('CRFicon_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>','images/CRF_status_icon_Started.gif');"
							onClick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_<c:out value="${eblRowCount}"/>_<c:out value="${count}"/>_<c:out value="${subjectName}"/>', event);">
		  			
		 </c:otherwise>
		  
		</c:choose>					

<c:set var="crfStatus" value="${param.crfStatus}" />