<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<jsp:directive.page import="org.akaza.openclinica.bean.core.SubjectEventStatus"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<jsp:useBean scope="request" id="currEvent" class="org.akaza.openclinica.bean.managestudy.StudyEventBean" />

<%--
<jsp:useBean scope="request" id="eventStatus" class="org.akaza.openclinica.bean.core.SubjectEventStatus" />
--%>

<c:set var="eblRowCount" value="${param.rowCount}" />
<c:set var="count" value="${param.colCount}" />
<c:set var="eventId" value="${param.eventId}" />
<c:set var="eventDefId" value="${param.eventDefId}" />
<c:set var="subjectId" value="${param.subjectId}" />
<c:set var="subjectName" value="${param.subjectName}" />
<c:set var="eventSysStatus" value="${param.eventSysStatus}" />
<c:set var="eventName" value="${param.eventName}" />
<c:set var="eventStatus" value="${param.eventStatus}" />
<c:set var="module" value="${param.module}" />
<c:set var="eventStatusName" value="${param.eventStatusName}"/>

<c:set var="no_startedi18n"><fmt:message key="not_started" bundle="${resword}"/></c:set>
<c:set var="data_entry_completei18n"><fmt:message key="data_entry_complete" bundle="${resterm}"/></c:set>
<c:set var="initial_data_entry_completei18n"><fmt:message key="initial_data_entry_complete" bundle="${resterm}"/></c:set>
<c:set var="double_data_entryi18n"><fmt:message key="double_data_entry" bundle="${resterm}"/></c:set>
<c:set var="lockedi18n"><fmt:message key="locked" bundle="${resterm}"/></c:set>
<c:set var="completedi18n"><fmt:message key="completed" bundle="${resterm}"/></c:set>
<c:set var="availablei18n"><fmt:message key="available" bundle="${resterm}"/></c:set>
<c:set var="removedi18n"><fmt:message key="removed" bundle="${resterm}"/></c:set>
<c:set var="notscheduledi18n"><fmt:message key="not_scheduled" bundle="${resterm}"/></c:set>
<c:set var="signedi18n"><fmt:message key="signed" bundle="${resterm}"/></c:set>

<%-- for setting the div width...--%>
<c:choose>
  <c:when test="${currEvent.repeatingNum >= 3}">
    <c:set var="divWidth" value="540" />
  </c:when>
  <c:when test="${currEvent.repeatingNum ==2}">
    <c:set var="divWidth" value="360" />
  </c:when>
  <c:otherwise>
    <c:set var="divWidth" value="180" />
  </c:otherwise>
</c:choose>
<!-- This Event Status box has been modified to accomodate repeating events in a scrollable box -->

<div id="Lock_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>" style="position: absolute; visibility: hidden; z-index: 3; width: 50px; height: 30px; top: 0px;">
		
    <c:choose>		               
	 <c:when test="${currEvent.repeatingNum>1}">
         <a href="javascript:ExpandEventOccurrences('<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>',<c:out value="${currEvent.repeatingNum}"/>);
	  </c:when>	 
	 <c:otherwise>
	   	<a href="javascript:leftnavExpand('Menu_on_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>'); 
	 </c:otherwise>
	 </c:choose> 	
		     javascript:leftnavExpand('Menu_off_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>');" 
		     
		     onmouseover="layersShowOrHide('visible','Event_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>');
				javascript:setImage('ExpandIcon_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>','images/icon_collapse.gif');" onclick="layersShowOrHide('hidden','Lock_all'); 
				layersShowOrHide('hidden','Event_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>');
				layersShowOrHide('hidden','Lock_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>');
				javascript:setImage('ExpandIcon_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>','images/icon_blank.gif');"><img src="images/spacer.gif" border="0" height="30" width="50"></a>
			  
</div>
		
	<div id="Event_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>" style="position: absolute; visibility: hidden; z-index: 3;width: <c:out value="${divWidth}"/>px; top: 0px; float: left;">
		<!-- These DIVs define shaded box borders -->
		<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL">
    <div class="box_TR"><div class="box_BL"><div class="box_BR">
		
		<div class="tablebox_center">
		<div class="ViewSubjectsPopup" style="color: rgb(91, 91, 91);">
			<table border="0" cellpadding="0" cellspacing="0">
			<tr valign="top">								               
			<c:choose>		               
		        <c:when test="${currEvent.repeatingNum>1}">
		           <!-- start of the repeating events-->
		              <td class="table_header_row_left" colspan="2">
		        		<fmt:message key="subject" bundle="${resword}"/>: <c:out value="${subjectName}"/>
						<br>
						<fmt:message key="event" bundle="${resword}"/>: <c:out value="${currEvent.studyEventDefinition.name}"/>	
						
		               </td>
		               
		               <!--</td>-->
						<td class="table_header_row_left" align="right" colspan="3">
						<a href="javascript:ExpandEventOccurrences('<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>',<c:out value="${currEvent.repeatingNum}"/>);		              
						javascript:leftnavExpand('Menu_off_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>');"						
							onClick="layersShowOrHide('hidden','Lock_all'); 
								layersShowOrHide('hidden','Event_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>');
								layersShowOrHide('hidden','Lock_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>');
								javascript:setImage('ExpandIcon_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>','images/icon_blank.gif');">X</a>
						 <br>
						<c:if test="${(eventSysStatus != 'removed' && eventSysStatus != 'auto-removed') && (study.status.available)}">						  
						<span style="font-weight: normal;">

						<a href="CreateNewStudyEvent?studySubjectId=<c:out value="${subjectId}"/>&studyEventDefinition=<c:out value="${eventDefId}"/>"><fmt:message key="add_another_occurrence" bundle="${resword}"/></a>
						</c:if>
						&nbsp &nbsp &nbsp 
        <!-- skip forward/back links: variables represent Table ID for this event, number of occurrences of repeating event, and number to skip to -->
                        <c:set var="rc" value="${1}"/>

						<c:forEach begin="1" end="${currEvent.repeatingNum}">
						 <a href="javascript:StatusBoxSkip('<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>',<c:out value="${currEvent.repeatingNum}"/>,<c:out value="${rc}"/>);"><c:out value="${rc}"/></a><c:if test="${rc < currEvent.repeatingNum}">|</c:if>
			             <c:set var="rc" value="${rc+1}"/>
		                </c:forEach>						
						</span>
						
						 </td>
					   </tr>
					   
					<tr>
					<td id="Scroll_off_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>_back" class="statusbox_scroll_L_dis" width="20">
					<img src="images/arrow_status_back_dis.gif" border="0">
					</td>

					<td id="Scroll_on_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>_back" width="20" class="statusbox_scroll_L" style="display: none;">  
		            
		           	
		          <!-- scroll back arrow: variables represent Table ID for this event, and number of occurrences of repeating event -->
		              
				    <div id="bt_Scroll_Event_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>_back" style="display: none;">
				    <a href="javascript:StatusBoxBack('<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>',<c:out value="${currEvent.repeatingNum}"/>);">
				    <img src="images/arrow_status_back.gif" border="0"></a></div>
					<div id="bt_Scroll_Event_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>_back_dis">
					<img src="images/arrow_status_back_dis.gif" border="0"></div>
					</td>
					
					<td id="Event_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>_1" valign="top" width="180">
					  <table border="0" cellpadding="0" cellspacing="0" width="100%">
						
						<tr valign="top">
						<td class="table_header_row" colspan="2">
						<b>
						<fmt:message key="occurrence_x_of" bundle="${resword}"><fmt:param>#1</fmt:param></fmt:message> <c:out value="${currEvent.repeatingNum}"/>
						<br>
						<fmt:formatDate value="${currEvent.dateStarted}" pattern="${dteFormat}"/>
						<br>
						<fmt:message key="status" bundle="${resword}"/>: <c:out value="${currEvent.subjectEventStatus.name}"/>
						</b>
						</td>
						</tr>		                
		                
		           <!-- the options for this event, will be hidden unless user clicks -->
						<tr id="Menu_on_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>_1" style="display: none">
							<td colspan="2">
								<table border="0" cellpadding="0" cellspacing="0" width="100%">							    		
							      
								 <c:choose>
								  <c:when test="${eventSysStatus == 'available' || eventSysStatus == availablei18n || eventSysStatus == 'signed' || eventSysStatus == signedi18n }">	
								   <c:choose>							   
								    <c:when test="${currEvent.subjectEventStatus.name =='completed' || currEvent.subjectEventStatus.name ==completedi18n}">
								      <tr valign="top"><td class="table_cell">
								      <a href="EnterDataForStudyEvent?eventId=<c:out value="${currEvent.id}"/>"><img src="images/bt_View.gif" border="0" align="left"></a>&nbsp;&nbsp; <a href="EnterDataForStudyEvent?eventId=<c:out value="${currEvent.id}"/>"><fmt:message key="view" bundle="${resword}"/></a></td></tr>
								    
								      <c:if test="${(userRole.role.name=='director' || userBean.sysAdmin) && (study.status.available)}">
									  <tr valign="top">
                                          <td class="table_cell">

                                                   <a href="UpdateStudyEvent?event_id=<c:out value="${currEvent.id}"/>&ss_id=<c:out value="${subjectId}"/>">
                                                       <img src="images/bt_Edit.gif" border="0" align="left"></a>
									                    &nbsp;&nbsp; 
                                                   <a href="UpdateStudyEvent?event_id=<c:out value="${currEvent.id}"/>&ss_id=<c:out value="${subjectId}"/>">
                                                        <fmt:message key="edit" bundle="${resword}"/></a>
                                          </td>
                                      </tr>
									  <tr valign="top"><td class="table_cell_left"><a href="RemoveStudyEvent?action=confirm&id=<c:out value="${currEvent.id}"/>&studySubId=<c:out value="${subjectId}"/>"><img src="images/bt_Remove.gif" border="0" align="left"></a>&nbsp;&nbsp;
									  <a href="RemoveStudyEvent?action=confirm&id=<c:out value="${currEvent.id}"/>&studySubId=<c:out value="${subjectId}"/>"><fmt:message key="remove" bundle="${resword}"/></a></td></tr>
							 	     </c:if>
								    </c:when>
								    
								    <c:when test="${currEvent.subjectEventStatus.name =='locked' ||
									currEvent.subjectEventStatus.name == lockedi18n}">
								      
								      <c:if test="${userRole.role.name=='director' || userBean.sysAdmin}">
								    	<tr valign="top"><td class="table_cell">
								      	<a href="EnterDataForStudyEvent?eventId=<c:out value="${currEvent.id}"/>"><img src="images/bt_View.gif" border="0" align="left"></a>&nbsp;&nbsp; <a href="EnterDataForStudyEvent?eventId=<c:out value="${currEvent.id}"/>"><fmt:message key="view" bundle="${resword}"/></a></td></tr>
								          <c:if test="${study.status.available}">
								            <tr valign="top"><td class="table_cell">
								                <a href="RemoveStudyEvent?action=confirm&id=<c:out value="${currEvent.id}"/>&studySubId=<c:out value="${subjectId}"/>">
								                <img src="images/bt_Remove.gif" border="0" align="left"></a>&nbsp;&nbsp; <a href="RemoveStudyEvent?action=confirm&id=<c:out value="${currEvent.id}"/>&studySubId=<c:out value="${subjectId}"/>"><fmt:message key="remove" bundle="${resword}"/></a></td></tr>
							 	           </c:if>
							 	      </c:if>
							 	    
							 	    </c:when>
								    
								    <c:otherwise>		
								      <tr valign="top"><td class="table_cell">
								      <a href="EnterDataForStudyEvent?eventId=<c:out value="${currEvent.id}"/>"><img src="images/bt_View.gif" border="0" align="left"></a>&nbsp;&nbsp; <a href="EnterDataForStudyEvent?eventId=<c:out value="${currEvent.id}"/>"><fmt:message key="view" bundle="${resword}"/>/<fmt:message key="enter_data" bundle="${resword}"/></a></td></tr>
								    					      
								     <c:if test="${(userRole.role.name=='director' || userBean.sysAdmin) && (study.status.available)}">
									  <tr valign="top"><td class="table_cell"><a href="UpdateStudyEvent?event_id=<c:out value="${currEvent.id}"/>&ss_id=<c:out value="${subjectId}"/>"><img src="images/bt_Edit.gif" border="0" align="left"></a>
									  &nbsp;&nbsp; <a href="UpdateStudyEvent?event_id=<c:out value="${currEvent.id}"/>&ss_id=<c:out value="${subjectId}"/>&module=<c:out value="${module}"/>"><fmt:message key="edit" bundle="${resword}"/></a></td></tr>
									
									  <tr valign="top"><td class="table_cell"><a href="RemoveStudyEvent?action=confirm&id=<c:out value="${currEvent.id}"/>&studySubId=<c:out value="${subjectId}"/>"><img src="images/bt_Remove.gif" border="0" align="left"></a>&nbsp;&nbsp; 
									  <a href="RemoveStudyEvent?action=confirm&id=<c:out value="${currEvent.id}"/>&studySubId=<c:out value="${subjectId}"/>"><fmt:message key="remove" bundle="${resword}"/></a></td></tr>
							 	     </c:if>
							 	    </c:otherwise>
							 	   </c:choose>
							 	   
							 	  </c:when>
							 	  
							 	  <c:when test="${currEvent.subjectEventStatus.name == 'removed' ||
								  currEvent.subjectEventStatus.name == removedi18n}">
							 	  
								   <tr valign="top">
								   <td class="table_cell">
								      <a href="EnterDataForStudyEvent?eventId=<c:out value="${currEvent.id}"/>"><img src="images/bt_View.gif" border="0" align="left"></a>&nbsp;&nbsp; <a href="EnterDataForStudyEvent?eventId=<c:out value="${currEvent.id}"/>"><fmt:message key="view" bundle="${resword}"/></a>
								      </td>
									  </tr>

								   <c:if test="${userRole.role.name=='director' || userBean.sysAdmin}">
							 	   	<c:if test="${eventSysStatus != 'removed' && eventSysStatus != 'auto-removed' && eventSysStatus != removedi18n}">
								   		<tr valign="top"><td class="table_cell"><a href="RestoreStudyEvent?action=confirm&id=<c:out value="${currEvent.id}"/>&studySubId=<c:out value="${subjectId}"/>"><img src="images/bt_Restore.gif" border="0" align="left"></a>&nbsp;&nbsp; 
									  <a href="RestoreStudyEvent?action=confirm&id=<c:out value="${currEvent.id}"/>&studySubId=<c:out value="${subjectId}"/>"><fmt:message key="restore" bundle="${resword}"/></a>
									  </td></tr>
								   	</c:if>
								   </c:if>	  
							 	  
								  </c:when>
							 	  
							 	   <c:when test="${eventSysStatus == 'removed' || eventSysStatus == 'auto-removed' || eventSysStatus == removedi18n}">
							 	  
								   <tr valign="top">
								   <td class="table_cell">
								      <a href="EnterDataForStudyEvent?eventId=<c:out value="${currEvent.id}"/>"><img src="images/bt_View.gif" border="0" align="left"></a>&nbsp;&nbsp; <a href="EnterDataForStudyEvent?eventId=<c:out value="${currEvent.id}"/>"><fmt:message key="view" bundle="${resword}"/></a>
								      </td>
									  </tr>
								  </c:when>
							 	  
							 	  <c:otherwise>
							 	  <!-- for future use-->
							 	  </c:otherwise>
							 	  </c:choose> 							 	  
								</table>
							</td>
						</tr>
					</table>
					</td>
	             <!-- start to get the other events in currEvent.repeatEvents array-->                
		              <c:set var="reNum" value="2"/>
		              <c:forEach var="reEvent" items="${currEvent.repeatEvents}">
		               <td id="Event_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>_<c:out value="${reNum}"/>" valign="top" width="180" <c:if test="${reNum > 3}"> style="display: none;" </c:if>>
					  <table border="0" cellpadding="0" cellspacing="0" width="100%">
						<tr valign="top">
						<td class="table_header_row" colspan="2">
						<b>
						<fmt:message key="occurrence_x_of" bundle="${resword}"><fmt:param>#<c:out value="${reNum}"/></fmt:param></fmt:message> <c:out value="${currEvent.repeatingNum}"/>
						<br>
						<fmt:formatDate value="${reEvent.dateStarted}" pattern="${dteFormat}"/>
						<br>	
						<fmt:message key="status" bundle="${resword}"/>: <c:out value="${reEvent.subjectEventStatus.name}"/>
						</b>
						</td>
						</tr>
						
		              <!-- the options for this event, will be hidden unless user clicks -->  
		               		                  
		                 <tr id="Menu_on_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>_<c:out value="${reNum}"/>" style="display: none">
							<td colspan="2">
							<table border="0" cellpadding="0" cellspacing="0" width="100%">						    		
							
							 <c:choose>
							  <c:when test="${eventSysStatus == 'available' || eventSysStatus == availablei18n || eventSysStatus == 'signed' || eventSysStatus == signedi18n }">	
							   <c:choose>							   
							    <c:when test="${reEvent.subjectEventStatus.name =='completed' || reEvent.subjectEventStatus.name ==completedi18n}">
							      <tr valign="top"><td class="table_cell">
							      <a href="EnterDataForStudyEvent?eventId=<c:out value="${reEvent.id}"/>"><img src="images/bt_View.gif" border="0" align="left"></a>&nbsp;&nbsp; <a href="EnterDataForStudyEvent?eventId=<c:out value="${reEvent.id}"/>"><fmt:message key="view" bundle="${resword}"/></a></td></tr>
							    
							      <c:if test="${(userRole.role.name=='director' || userBean.sysAdmin) && (study.status.available)}">
								  <tr valign="top"><td class="table_cell_left"><a href="UpdateStudyEvent?event_id=<c:out value="${reEvent.id}"/>&ss_id=<c:out value="${subjectId}"/>">
                                      <img src="images/bt_Edit.gif" border="0" align="left"></a>
								  &nbsp;&nbsp;
                                      <a href="UpdateStudyEvent?event_id=<c:out value="${reEvent.id}"/>&ss_id=<c:out value="${subjectId}"/>"><fmt:message key="edit" bundle="${resword}"/></a></td></tr>

								  <tr valign="top"><td class="table_cell"><a href="RemoveStudyEvent?action=confirm&id=<c:out value="${reEvent.id}"/>&studySubId=<c:out value="${subjectId}"/>"><img src="images/bt_Remove.gif" border="0" align="left"></a>&nbsp;&nbsp; 
								  <a href="RemoveStudyEvent?action=confirm&id=<c:out value="${reEvent.id}"/>&studySubId=<c:out value="${subjectId}"/>"><fmt:message key="remove" bundle="${resword}"/></a></td></tr>
						 	     </c:if>
							    </c:when>
							    <c:when test="${reEvent.subjectEventStatus.name =='locked' || reEvent.subjectEventStatus.name ==lockedi18n }">
							      <c:if test="${userRole.role.name=='director' || userBean.sysAdmin}">
							        <tr valign="top"><td class="table_cell">
								    <a href="EnterDataForStudyEvent?eventId=<c:out value="${currEvent.id}"/>"><img src="images/bt_View.gif" border="0" align="left"></a>&nbsp;&nbsp; <a href="EnterDataForStudyEvent?eventId=<c:out value="${currEvent.id}"/>"><fmt:message key="view" bundle="${resword}"/></a></td></tr>
								        <c:if test="${study.status.available}">
							            <tr valign="top"><td class="table_cell"><a href="RemoveStudyEvent?action=confirm&id=<c:out value="${reEvent.id}"/>&studySubId=<c:out value="${subjectId}"/>">
							            <img src="images/bt_Remove.gif" border="0" align="left"></a>&nbsp;&nbsp; <a href="RemoveStudyEvent?action=confirm&id=<c:out value="${reEvent.id}"/>&studySubId=<c:out value="${subjectId}"/>"><fmt:message key="remove" bundle="${resword}"/></a></td></tr>
                                        </c:if>
                                   </c:if>
						 	    </c:when>
							    <c:otherwise>		
							      <tr valign="top"><td class="table_cell">
							      <a href="EnterDataForStudyEvent?eventId=<c:out value="${reEvent.id}"/>"><img src="images/bt_View.gif" border="0" align="left"></a>&nbsp;&nbsp; <a href="EnterDataForStudyEvent?eventId=<c:out value="${reEvent.id}"/>"><fmt:message key="view" bundle="${resword}"/>/<fmt:message key="enter_data" bundle="${resword}"/></a></td></tr>
							    					      
							     <c:if test="${(userRole.role.name=='director' || userBean.sysAdmin) && (study.status.available)}">
								  <tr valign="top"><td class="table_cell"><a href="UpdateStudyEvent?event_id=<c:out value="${reEvent.id}"/>&ss_id=<c:out value="${subjectId}"/>"><img src="images/bt_Edit.gif" border="0" align="left"></a>
								  &nbsp;&nbsp; <a href="UpdateStudyEvent?event_id=<c:out value="${reEvent.id}"/>&ss_id=<c:out value="${subjectId}"/>&module=<c:out value="${module}"/>"><fmt:message key="edit" bundle="${resword}"/></a></td></tr>
								
								  <tr valign="top"><td class="table_cell"><a href="RemoveStudyEvent?action=confirm&id=<c:out value="${reEvent.id}"/>&studySubId=<c:out value="${subjectId}"/>"><img src="images/bt_Remove.gif" border="0" align="left"></a>&nbsp;&nbsp; 
								  <a href="RemoveStudyEvent?action=confirm&id=<c:out value="${reEvent.id}"/>&studySubId=<c:out value="${subjectId}"/>"><fmt:message key="remove" bundle="${resword}"/></a></td></tr>
						 	     </c:if>
						 	    </c:otherwise>
						 	   </c:choose>
						 	   
						 	  </c:when>
						 	  
						 	  <c:when test="${reEvent.subjectEventStatus.name == 'removed' || reEvent.subjectEventStatus.name == removedi18n}">
						 	   <tr valign="top"><td class="table_cell_left">
							      <a href="EnterDataForStudyEvent?eventId=<c:out value="${reEvent.id}"/>"><img src="images/bt_View.gif" border="0" align="left"></a>&nbsp;&nbsp; <a href="EnterDataForStudyEvent?eventId=<c:out value="${reEvent.id}"/>"><fmt:message key="view" bundle="${resword}"/></a>
							      </td></tr>
							   <c:if test="${userRole.role.name=='director' || userBean.sysAdmin}">
							   <c:if test="${eventSysStatus != 'removed' && eventSysStatus != 'auto-removed' && eventSysStatus != removedi18n}">
						 	   <tr valign="top"><td class="table_cell_left"><a href="RestoreStudyEvent?action=confirm&id=<c:out value="${reEvent.id}"/>&studySubId=<c:out value="${subjectId}"/>"><img src="images/bt_Restore.gif" border="0" align="left"></a>&nbsp;&nbsp; 
								  <a href="RestoreStudyEvent?action=confirm&id=<c:out value="${reEvent.id}"/>&studySubId=<c:out value="${subjectId}"/>"><fmt:message key="restore" bundle="${resword}"/></a>
								  </td></tr>
							   </c:if>
							   </c:if>	  
						 	  </c:when>
						 	  
						 	   <c:when test="${eventSysStatus == 'removed'|| eventSysStatus == 'auto-removed' || eventSysStatus == removedi18n}">
								   <tr valign="top">
								   <td class="table_cell">
								      <a href="EnterDataForStudyEvent?eventId=<c:out value="${currEvent.id}"/>"><img src="images/bt_View.gif" border="0" align="left"></a>&nbsp;&nbsp; <a href="EnterDataForStudyEvent?eventId=<c:out value="${currEvent.id}"/>"><fmt:message key="view" bundle="${resword}"/></a>
								      </td>
									  </tr>
								  </c:when>
						 	  
						 	  <c:otherwise>
						 	  <!-- for future use-->
						 	  </c:otherwise>
						 	  </c:choose> 
						 	</table>
							</td>
						</tr>
					 </table>
					</td>
		                  <c:set var="reNum" value="${reNum+1}"/>
		                </c:forEach>
		            
					<td id="Scroll_off_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>_next" width="20"  class="statusbox_scroll_R_dis">
					<img src="images/arrow_status_next_dis.gif" border="0">
					</td>

					<td id="Scroll_on_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>_next" width="20"  class="statusbox_scroll_R" style="display: none;">

            <!-- scroll next arrow: variables represent Table ID for this event, and number of occurrences of repeating event -->

					<div id="bt_Scroll_Event_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>_next"><a href="javascript:StatusBoxNext('<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>',<c:out value="${currEvent.repeatingNum}"/>);"><img src="images/arrow_status_next.gif" border="0"></a></div>
					
					<div id="bt_Scroll_Event_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>_next_dis" style="display: none;"><img src="images/arrow_status_next_dis.gif" border="0"></div>
					</td>
					</tr>

					<tr id="Menu_off_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>" style="">
					
					<td class="table_cell_left" colspan="<c:out value="${currEvent.repeatingNum}"/>"><i><fmt:message key="click_for_more_options" bundle="${restext}"/></i>
					</td>
					</tr>
					
              <!--  end of repeating events      -->
		               </c:when>               
		               
		               <c:otherwise>
		               <!-- start of non-repeating events-->
		               <td class="table_header_row_left">
						<fmt:message key="subject" bundle="${resword}"/>: <c:out value="${subjectName}"/>		
						<br>		
						<fmt:message key="event" bundle="${resword}"/>: <c:out value="${eventName}"/>
	
						<br>
						<c:choose>

						<c:when test="${currEvent.studyEventDefinition.repeating == 'false'}">
						<fmt:message key="status" bundle="${resword}"/>: <c:out value="${eventStatus}"/>
						</td>
						<td class="table_header_row_left" align="right">

            <a href="javascript:leftnavExpand('Menu_on_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>'); javascript:leftnavExpand('Menu_off_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>');"
							onClick="layersShowOrHide('hidden','Lock_all'); 
								layersShowOrHide('hidden','Event_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>');
								layersShowOrHide('hidden','Lock_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>');
								javascript:setImage('ExpandIcon_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>','images/icon_blank.gif');">X</a></td>
						</c:when>
						<c:otherwise>
						
							</td>
							<td class="table_header_row_left" align="right">

            <a href="javascript:leftnavExpand('Menu_on_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>'); javascript:leftnavExpand('Menu_off_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>');"
							onClick="layersShowOrHide('hidden','Lock_all'); 
								layersShowOrHide('hidden','Event_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>');
								layersShowOrHide('hidden','Lock_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>');
								javascript:setImage('ExpandIcon_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>','images/icon_blank.gif');">X</a></td>
						    <tr valign="top">
							<td class="table_header_row" colspan="2">
							<b>
							<fmt:message key="occurrence_x_of" bundle="${resword}"><fmt:param>#<c:out value="1"/></fmt:param></fmt:message> <c:out value="${currEvent.repeatingNum}"/>
							<br>
							<fmt:formatDate value="${currEvent.dateStarted}" pattern="${dteFormat}"/>
							<br>	
							<fmt:message key="status" bundle="${resword}"/>: <c:out value="${currEvent.subjectEventStatus.name}"/> 
							</b>
							</td>
							</tr>
							<c:if test="${
								eventStatus !='not scheduled' && 
								eventStatus !=notscheduledi18n && 
								eventSysStatus != 'removed' && 
								eventSysStatus != removedi18n &&
								eventSysStatus != 'auto-removed' &&
								study.status.available}">
							<tr><td class="table_cell_left">	
							
							<a href="CreateNewStudyEvent?studySubjectId=<c:out value="${subjectId}"/>&studyEventDefinition=<c:out value="${eventDefId}"/>"><fmt:message key="add_another_occurrence" bundle="${resword}"/></a>
							
							</td></tr>
							</c:if>
						</c:otherwise>
						</c:choose>
						
						</tr>
						<tr id="Menu_off_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>" style="display: all">
							<td class="table_cell_left" colspan="2"><i><fmt:message key="click_for_more_options" bundle="${restext}"/></i></td>
						</tr>
						<tr id="Menu_on_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>" style="display: none">
							<td colspan="2">
							<table border="0" cellpadding="0" cellspacing="0" width="100%">	
							 <c:choose>
							  <c:when test="${eventSysStatus == 'available' || eventSysStatus == availablei18n || eventSysStatus == 'signed' || eventSysStatus == signedi18n }">	
							  
							  <%-- below lines are for debugging only --%>
							  <%--1:<c:out value="${eventStatus}"/><br/>
							  2:<c:out value="${eventStatusName}"/><br/>
							  3:<c:out value="${currEvent.subjectEventStatus.name}"/><br/>
							  4:<c:out value="${eventSysStatus}"/><br/>--%>
							  <%-- above lines are for debugging only --%>
							  
							   <c:choose>
							   
							    <%--<c:when test="${eventStatus.notScheduled}">--%>
							    <c:when test="${eventStatus =='not scheduled' || eventStatus == notscheduledi18n}">
							       <c:if test="${!userRole.monitor && (study.status.available)}">
    							      <tr valign="top">
                                          <td class="table_cell_left">
	    			    			      <a href="CreateNewStudyEvent?studySubjectId=<c:out value="${subjectId}"/>&studyEventDefinition=<c:out value="${eventDefId}"/>">
    		    					      <img src="images/bt_Schedule.gif" border="0" align="left"></a>&nbsp;&nbsp;
	    		    				      <a href="CreateNewStudyEvent?studySubjectId=<c:out value="${subjectId}"/>&studyEventDefinition=<c:out value="${eventDefId}"/>">
		        	    			      <fmt:message key="schedule" bundle="${resword}"/></a>
				    		    	      </td>
                                      </tr>
							      </c:if>
							   </c:when>
							   
							    <c:when test="${eventStatus =='completed' || eventStatus==completedi18n}">
							    <tr valign="top"><td class="table_cell_left">
							      <a href="EnterDataForStudyEvent?eventId=<c:out value="${eventId}"/>"><img src="images/bt_View.gif" border="0" align="left"></a>&nbsp;&nbsp; <a href="EnterDataForStudyEvent?eventId=<c:out value="${eventId}"/>"><fmt:message key="view" bundle="${resword}"/></a></td></tr>
							    
							      <c:if test="${(userRole.director || userBean.sysAdmin) && (study.status.available)}">
								  <tr valign="top"><td class="table_cell_left"><a href="UpdateStudyEvent?event_id=<c:out value="${eventId}"/>&ss_id=<c:out value="${subjectId}"/>"><img src="images/bt_Edit.gif" border="0" align="left"></a>
								  &nbsp;&nbsp; <a href="UpdateStudyEvent?event_id=<c:out value="${eventId}"/>&ss_id=<c:out value="${subjectId}"/>"><fmt:message key="edit" bundle="${resword}"/></a></td></tr>
								  
								  <tr valign="top"><td class="table_cell_left"><a href="RemoveStudyEvent?action=confirm&id=<c:out value="${eventId}"/>&studySubId=<c:out value="${subjectId}"/>"><img src="images/bt_Remove.gif" border="0" align="left"></a>&nbsp;&nbsp; 
								  <a href="RemoveStudyEvent?action=confirm&id=<c:out value="${eventId}"/>&studySubId=<c:out value="${subjectId}"/>"><fmt:message key="remove" bundle="${resword}"/></a></td></tr>
						 	     </c:if>
						 	    </c:when>
							   
							    <c:when test="${eventStatus =='locked' || eventStatus == lockedi18n}">
							      <c:if test="${userRole.director || userBean.sysAdmin}">
							      
							        <tr valign="top"><td class="table_cell">
								    <a href="EnterDataForStudyEvent?eventId=<c:out value="${currEvent.id}"/>"><img src="images/bt_View.gif" border="0" align="left"></a>&nbsp;&nbsp; <a href="EnterDataForStudyEvent?eventId=<c:out value="${currEvent.id}"/>"><fmt:message key="view" bundle="${resword}"/></a></td></tr>
								    <c:if test="${study.status.available}">  
							          <tr valign="top"><td class="table_cell_left"><a href="RemoveStudyEvent?action=confirm&id=<c:out value="${eventId}"/>&studySubId=<c:out value="${subjectId}"/>">
							          <img src="images/bt_Remove.gif" border="0" align="left"></a>&nbsp;&nbsp; <a href="RemoveStudyEvent?action=confirm&id=<c:out value="${eventId}"/>&studySubId=<c:out value="${subjectId}"/>"><fmt:message key="remove" bundle="${resword}"/></a></td></tr>
						 	        </c:if>
						 	      </c:if>
						 	    </c:when>	   
							    <c:otherwise>
							     <tr valign="top"><td class="table_cell_left">
							      <a href="EnterDataForStudyEvent?eventId=<c:out value="${eventId}"/>"><img src="images/bt_View.gif" border="0" align="left"></a>&nbsp;&nbsp; <a href="EnterDataForStudyEvent?eventId=<c:out value="${eventId}"/>"><fmt:message key="view" bundle="${resword}"/>/<fmt:message key="enter_data" bundle="${resword}"/></a></td></tr>
							    					      
							     <c:if test="${(userRole.role.name=='director' || userBean.sysAdmin) && (study.status.available)}">
								  <tr valign="top"><td class="table_cell_left"><a href="UpdateStudyEvent?event_id=<c:out value="${eventId}"/>&ss_id=<c:out value="${subjectId}"/>"><img src="images/bt_Edit.gif" border="0" align="left"></a>
								  &nbsp;&nbsp; <a href="UpdateStudyEvent?event_id=<c:out value="${eventId}"/>&ss_id=<c:out value="${subjectId}"/>"><fmt:message key="edit" bundle="${resword}"/></a></td></tr>
								  
								  <tr valign="top"><td class="table_cell_left"><a href="RemoveStudyEvent?action=confirm&id=<c:out value="${eventId}"/>&studySubId=<c:out value="${subjectId}"/>"><img src="images/bt_Remove.gif" border="0" align="left"></a>&nbsp;&nbsp; 
								  <a href="RemoveStudyEvent?action=confirm&id=<c:out value="${eventId}"/>&studySubId=<c:out value="${subjectId}"/>"><fmt:message key="remove" bundle="${resword}"/></a></td></tr>
						 	     </c:if>
						 	    </c:otherwise>
						 	   
						 	   </c:choose>
						 	   
						 	  </c:when>
						 	  
						 	  <%-- <c:when test="${eventSysStatus == 'removed'}"> --%>
						 	  <c:when test="${eventSysStatus == 'removed' || eventSysStatus == 'auto-removed' || eventSysStatus == removedi18n}">
						 	   <tr valign="top"><td class="table_cell_left">
							      <a href="EnterDataForStudyEvent?eventId=<c:out value="${eventId}"/>"><img src="images/bt_View.gif" border="0" align="left"></a>&nbsp;&nbsp; <a href="EnterDataForStudyEvent?eventId=<c:out value="${eventId}"/>"><fmt:message key="view" bundle="${resword}"/></a>
							      </td></tr>
							  <%--
							   <c:if test="${userRole.role.name=='director' || userBean.sysAdmin}">
						 	   		<tr valign="top"><td class="table_cell_left"><a href="RestoreStudyEvent?action=confirm&id=<c:out value="${eventId}"/>&studySubId=<c:out value="${subjectId}"/>"><img src="images/bt_Restore.gif" border="0" align="left"></a>&nbsp;&nbsp; 
								  <a href="RestoreStudyEvent?action=confirm&id=<c:out value="${eventId}"/>&studySubId=<c:out value="${subjectId}"/>"><fmt:message key="restore" bundle="${resword}"/></a>
								  </td></tr>
							   </c:if>	 --%> 
						 	  </c:when>
						 	       
						 	  <c:otherwise>
						 	  <!-- for future use-->
						 	  </c:otherwise>
						 	  </c:choose> 
						 	</table>
							</td>
						</tr>
						
						<!-- end of non-repeating events-->
						</c:otherwise>						
						</c:choose>
						
						</table>
		
			</div>
			</div>
		
			</div></div></div></div></div></div></div></div>
		  </div>
         
      <c:choose>		               
		<c:when test="${currEvent.repeatingNum>1}">  
		  <a href="javascript:ExpandEventOccurrences('<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>',<c:out value="${currEvent.repeatingNum}"/>);  
	  </c:when>	 
	 <c:otherwise>
	   <a href="javascript:leftnavExpand('Menu_on_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>'); 
	 </c:otherwise>
	 </c:choose>        
         <%--if(! detectIEWindows(navigator.userAgent)){
          javascript:leftnavExpand('Menu_off_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>');"
							onmouseover="moveObject('Event_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>', event);
							javascript:setImage('ExpandIcon_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>','images/icon_expand.gif');"
							onmouseout="layersShowOrHide('hidden','Event_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>');
							javascript:setImage('ExpandIcon_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>','images/icon_blank.gif');"
							onClick="layersShowOrHide('visible','Lock_all'); LockObject('Lock_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>', event);">--%>


              javascript:leftnavExpand('Menu_off_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>');"
              onmouseover=
			  "moveObject('Event_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>', event); setImage('ExpandIcon_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>','images/icon_expand.gif');"
				onmouseout=
				"layersShowOrHide('hidden','Event_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>'); setImage('ExpandIcon_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>','images/icon_blank.gif');"
				onClick=
				"layersShowOrHide('visible','Lock_all'); LockObject('Lock_<c:out value="${subjectName}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>', event);">
