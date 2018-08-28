<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<jsp:useBean scope="request" id="currRow" class="org.akaza.openclinica.web.bean.DisplayStudyEventRow" />
<jsp:useBean scope="request" id="studySub" class="org.akaza.openclinica.bean.managestudy.StudySubjectBean"/>
<script type="text/javascript" language="JavaScript" src="includes/permissionTagAccess.js"></script>

<tr>
     <td class="table_cell_left"><c:out value="${currRow.bean.studyEvent.studyEventDefinition.name}"/>
        <c:if test="${currRow.bean.studyEvent.studyEventDefinition.repeating}">
            (<c:out value="${currRow.bean.studyEvent.sampleOrdinal}"/>)
        </c:if>
     </td>
     <td class="table_cell"><fmt:formatDate value="${currRow.bean.studyEvent.dateStarted}" pattern="${dteFormat}"/>
     </td>
     <td class="table_cell" width="20"><c:out value="${currRow.bean.studyEvent.subjectEventStatus.name}"/></td>
     <td class="table_cell">
       <table border="0" cellpadding="0" cellspacing="0">
		
		<td> 
        <a href="EnterDataForStudyEvent?eventId=<c:out value="${currRow.bean.studyEvent.id}"/>"
		onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
		onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><span
		name="bt_View1" class="icon icon-search" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6"></a>
		</td>
		
		
		
		<td>
		<c:if test="${(studySub.status.name != 'removed' && studySub.status.name != 'auto-removed' && !currRow.bean.studyEvent.status.deleted && currRow.bean.studyEvent.editable) && (study.status.available) && (!userRole.monitor)}">
        <a href="UpdateStudyEvent?event_id=<c:out value="${currRow.bean.studyEvent.id}"/>&ss_id=<c:out value="${studySub.id}"/>"
		onMouseDown="javascript:setImage('bt_Edit1','images/bt_Edit_d.gif');"
		onMouseUp="javascript:setImage('bt_Edit1','images/bt_Edit.gif');"><span
		name="bt_Edit1" class="icon icon-pencil" border="0" alt="<fmt:message key="edit" bundle="${resword}"/>" title="<fmt:message key="edit" bundle="${resword}"/>" align="left" hspace="6"></a>
		</c:if>
		</td>
		
		
		
		<td>
		<c:choose>
         <c:when test="${!currRow.bean.studyEvent.status.deleted}">
           <c:if test="${!userRole.monitor && study.status.available}">
           <a href="RemoveStudyEvent?action=confirm&id=<c:out value="${currRow.bean.studyEvent.id}"/>&studySubId=<c:out value="${studySub.id}"/>"
			onMouseDown="javascript:setImage('bt_Remove1','images/bt_Remove_d.gif');"
			onMouseUp="javascript:setImage('bt_Remove1','images/bt_Remove.gif');"><span
			name="bt_Remove1" class="icon icon-cancel" border="0" alt="<fmt:message key="remove" bundle="${resword}"/>" title="<fmt:message key="remove" bundle="${resword}"/>" align="left" hspace="6"></a>
           </c:if>
         </c:when>
         <c:otherwise>
          <c:if test="${(!userRole.monitor && studySub.status.name != 'removed' && studySub.status.name != 'auto-removed') && (currRow.bean.studyEvent.studyEventDefinition.status.available) && (study.status.available)}">
           <a href="RestoreStudyEvent?action=confirm&id=<c:out value="${currRow.bean.studyEvent.id}"/>&studySubId=<c:out value="${studySub.id}"/>"
			onMouseDown="javascript:setImage('bt_Restor3','images/bt_Restore_d.gif');"
			onMouseUp="javascript:setImage('bt_Restore3','images/bt_Restore.gif');"><span
			name="bt_Restore3" class="icon icon-ccw" border="0" alt="<fmt:message key="restore" bundle="${resword}"/>" title="<fmt:message key="restore" bundle="${resword}"/>" align="left" hspace="6"></a>
		 </c:if>
         </c:otherwise>
        </c:choose>

        <c:if test="${userRole.manageStudy && study.status.available  && currRow.bean.studyEvent.studyEventDefinition.status.available && (currRow.bean.studyEvent.studyEventDefinition.status.available)}">
            
            <td>
                <a class="accessCheck" href="DeleteStudyEvent?action=confirm&id=<c:out value="${currRow.bean.studyEvent.id}"/>&studySubId=<c:out value="${studySub.id}"/>"
                 onMouseDown="javascript:setImage('bt_Remove1','images/bt_Remove_d.gif');"
                 onMouseUp="javascript:setImage('bt_Remove1','images/bt_Remove.gif');"><span
                 name="bt_Delete1" class="icon icon-trash red" border="0" alt="<fmt:message key="delete" bundle="${resword}"/>"
				 title="<fmt:message key="delete" bundle="${resword}"/>" align="left" hspace="6"></a>
            </td></tr>
        </c:if>


		</td>
      
      </table>
     </td>
     <td class="table_cell">

      <c:choose>

	  <c:when test="${empty currRow.bean.uncompletedCRFs && empty currRow.bean.displayEventCRFs}">
		<fmt:message key="no_CRFs" bundle="${resword}"/>
	  </c:when>

	  <c:otherwise>

      <table border="0" cellpadding="0" cellspacing="0">
      <tr valign="top">
			<td class="table_cell" width="180" style="background-color: #ccc;"><center><fmt:message key="name" bundle="${resword}"/></center></td>
			<td class="table_cell" width="180" style="background-color: #ccc;"><center><fmt:message key="version" bundle="${resword}"/></center></td>
			<td class="table_cell" width="180" style="background-color: #ccc;"><center><fmt:message key="status" bundle="${resword}"/></center></td>
			<td class="table_cell" width="180" style="background-color: #ccc;"><center><fmt:message key="update" bundle="${resword}"/></center></td>
			<td class="table_cell" width="180" style="background-color: #ccc;"><center><fmt:message key="actions" bundle="${resword}"/></center></td>
		</tr>

	  <c:forEach var="dedc" items="${currRow.bean.uncompletedCRFs}">

		<c:set var="getQuery" value="eventDefinitionCRFId=${dedc.edc.id}&studyEventId=${currRow.bean.studyEvent.id}&subjectId=${studySub.subjectId}&eventCRFId=${dedc.eventCRF.id}&exitTo=ViewStudySubject?id=${studySub.id}" />
<form name="startForm<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>" action="InitialDataEntry?<c:out value="${getQuery}"/>" method="POST">

			<tr valign="top">

			  <td class="table_cell" width="180"><c:out value="${dedc.edc.crf.name}" /></td>

				<td class="table_cell" width="100">

				<c:choose>
				<c:when test="${dedc.eventCRF.id > 0}">
				<!-- found an event crf id -->
					<input type="hidden" name="formLayoutId" value="<c:out value="${dedc.eventCRF.formLayout.id}"/>">
				</c:when>
				<c:otherwise>
				<!-- did not find an event crf id -->
					<input type="hidden" name="formLayoutId" value="<c:out value="${dedc.edc.defaultVersionId}"/>">
				</c:otherwise>
				</c:choose>

				  <c:set var="versionCount" value="0"/>
				  <c:forEach var="version" items="${dedc.edc.versions}">
                    <c:set var="formLayoutOID" value="${version.oid}"/>
				    <c:set var="versionCount" value="${versionCount+1}"/>
				  </c:forEach>

				 <c:choose>
				 <c:when test="${versionCount<=1}">
				   <c:forEach var="version" items="${dedc.edc.versions}">
				     <c:out value="${version.name}"/>
				   </c:forEach>
				 </c:when>

				 <c:when test="${dedc.eventCRF.id == 0}">

				<select name="versionId<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>" onchange="javascript:changeQuery<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>();">
				 <c:forEach var="version" items="${dedc.edc.versions}">

				 <c:set var="getQuery" value="action=ide_s&eventDefinitionCRFId=${dedc.edc.id}&studyEventId=${currRow.bean.studyEvent.id}&subjectId=${studySub.subjectId}" />

				 <c:choose>
                  <c:when test="${dedc.edc.defaultVersionId==version.id}">
                  <option value="<c:out value="${version.id}"/>" selected>
					<c:out value="${version.name}"/>
                       <c:set var="formLayoutOID" value="${version.oid}"/>
				  </option>
                  </c:when>
                  <c:otherwise>
                    <option value="<c:out value="${version.id}"/>">
						<c:out value="${version.name}"/>
						 <c:set var="formLayoutOID" value="${version.oid}"/>
					</option>
                  </c:otherwise>
                  </c:choose>

                 </c:forEach>
                 </select>

                 <SCRIPT LANGUAGE="JavaScript">

				 function changeQuery<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>() {
                  var qer = document.startForm<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>.versionId<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>.value;
                  document.startForm<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>.formLayoutId.value=qer;
                  document.getElementById('ide1-<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>').href = 
                	  buildUrl(qer,'<c:out value="${currRow.bean.studyEvent.id}"/>','<c:out value="${dedc.eventCRF.status.id}"/>','<c:out value="${originatingPage}"/>' ,'<c:out value="edit"/>');
                  document.getElementById('ide2-<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>').href = 
                  	  buildUrl(qer,'<c:out value="${currRow.bean.studyEvent.id}"/>','<c:out value="${dedc.eventCRF.status.id}"/>','<c:out value="${originatingPage}"/>','<c:out value="view"/>' );
                 }
                 
                  function buildUrl(formLayoutId, studyEventId, eventCRFStatusId, originatingPage,mode){
                 	 return "EnketoFormServlet?formLayoutId="+ formLayoutId + 
                 			 "&studyEventId=" + studyEventId + 
                 			 "&eventCrfId=" + eventCRFStatusId + 
                 			 "&originatingPage=" + originatingPage+
                 			 "&mode=" + mode;
                  }                                  
                 </SCRIPT>
                 

                                   
                 </c:when>

				 <c:otherwise><c:out value="${dedc.eventCRF.formLayout.name}"/>
				             <c:set var="formLayoutOID" value="${dedc.eventCRF.formLayout.oid}"/>
				 
				 </c:otherwise>
				 </c:choose>

				</td>
				<c:choose>

				<c:when test="${dedc.status.name=='locked'}">
					<td class="table_cell" bgcolor="#F5F5F5" align="center" width="20">
						<span class="icon icon-lock" alt="<fmt:message key="locked" bundle="${resword}"/>" title="<fmt:message key="locked" bundle="${resword}"/>">
					</td>
				</c:when>

                <c:when test="${studySubject.status.name != 'removed'&& studySubject.status.name != 'auto-removed'}">
                    <c:choose>
                        <c:when test="${dedc.eventCRF.id>0}">
                            <td class="table_cell" bgcolor="#F5F5F5" align="center"><span class="images/icon_Initialicon icon-icon-dataEntryCompleted orange" alt="<fmt:message key="initial_data_entry" bundle="${resword}"/>" title="<fmt:message key="initial_data_entry" bundle="${resword}"/>"></td>
                        </c:when>
                        <c:otherwise>
                            <td class="table_cell" bgcolor="#F5F5F5" align="center"><span class="icon icon-doc" alt="<fmt:message key="not_started" bundle="${resword}"/>" title="<fmt:message key="not_started" bundle="${resword}"/>"></td>
                        </c:otherwise>
                    </c:choose>
                </c:when>
				<c:otherwise>
					<td class="table_cell" bgcolor="#F5F5F5" align="center" width="20">
						<span class="icon icon-file-excel red" alt="<fmt:message key="invalid" bundle="${resword}"/>" title="<fmt:message key="invalid" bundle="${resword}"/>">
					</td>
				</c:otherwise>
				</c:choose>
				<td class="table_cell" width="80">&nbsp;&nbsp;</td>
				<td class="table_cell" width="140">
				<table cellspacing="0" cellpadding="0" border="0">
				<tr>
				<c:choose>

				 <c:when test="${dedc.status.name=='locked' || (currRow.bean.studyEvent.subjectEventStatus.signed && !userRole.manageStudy)}">
				 	
				 </c:when>

				 <c:when test="${studySub.status.name != 'removed' && studySub.status.name != 'auto-removed'}">
					 <td>
                <c:if test="${study.status.available && !currRow.bean.studyEvent.status.deleted && !currRow.bean.studyEvent.subjectEventStatus.locked && !userRole.monitor}">
                    <c:choose>
                    <c:when test="${dedc.eventCRF.status.id != 0}">
                    <a class="accessCheck" href="EnketoFormServlet?formLayoutId=<c:out value="${dedc.eventCRF.formLayout.id}"/>&studyEventId=<c:out value="${currRow.bean.studyEvent.id}"/>&eventCrfId=<c:out value="${dedc.eventCRF.status.id}"/>&originatingPage=<c:out value="${originatingPage}"/>&mode=<c:out value="edit"/>"
                      onMouseDown="javascript:setImage('bt_EnterData1','images/bt_EnterData_d.gif');"
                      onMouseUp="javascript:setImage('bt_EnterData1','images/bt_EnterData.gif');">
                     <span name="bt_EnterData1" class="icon icon-pencil-squared" border="0" alt="<fmt:message key="enter_data" bundle="${resword}"/>" title="<fmt:message key="enter_data" bundle="${resword}"/>" align="right" hspace="6">
                    </a>
                    </c:when>
                    <c:otherwise>
                    
                    <a id="ide1-<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>" class="accessCheck"
					   href="EnketoFormServlet?formLayoutId=<c:out value="${dedc.edc.defaultVersionId}"/>&studyEventId=<c:out value="${currRow.bean.studyEvent.id}"/>&eventCrfId=<c:out value="${dedc.eventCRF.status.id}"/>&originatingPage=<c:out value="${originatingPage}"/>&mode=<c:out value="edit"/>"
                      onMouseDown="javascript:setImage('bt_EnterData1','images/bt_EnterData_d.gif');"
                      onMouseUp="javascript:setImage('bt_EnterData1','images/bt_EnterData.gif');">
                     <span name="bt_EnterData1" class="icon icon-pencil-squared" border="0" alt="<fmt:message key="enter_data" bundle="${resword}"/>" title="<fmt:message key="enter_data" bundle="${resword}"/>" align="right" hspace="6">
                    </a>
                    </c:otherwise>
                    </c:choose>
                 </c:if>
                 </td>
				</c:when>

				</c:choose>

		         <td>
                    <a id="ide2-<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>" class="accessCheck"
					   href="EnketoFormServlet?formLayoutId=<c:out value="${dedc.edc.defaultVersionId}"/>&studyEventId=<c:out value="${currRow.bean.studyEvent.id}"/>&eventCrfId=<c:out value="${dedc.eventCRF.status.id}"/>&originatingPage=<c:out value="${originatingPage}"/>&mode=<c:out value="view"/>"
                        onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
			            onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');">
                     <span
		         name="bt_View1" class="icon icon-search" border="0" alt="<fmt:message key="view_default" bundle="${resword}"/>" title="<fmt:message key="view_default" bundle="${resword}"/>" align="left" hspace="6">

		         </a>
                 </td>
<!-- study.status != locked &&  study.status != frozen, Event CRF - not 'locked' or 'skipped', user='Study Director' or 'Data Manager' or 'admin' -->
<c:if test="${dedc.eventCRF.id>0 && 
 (userRole.director || userRole.coordinator) && study.status.available 
&& !(currRow.bean.studyEvent.subjectEventStatus.locked || currRow.bean.studyEvent.subjectEventStatus.skipped)}">
   <td>

<a class="accessCheck"
   href='pages/managestudy/chooseCRFVersion?crfId=<c:out value="${dedc.eventCRF.crf.id}" />&crfName=<c:out value="${dedc.eventCRF.crf.name}" />&formLayoutId=<c:out value="${dedc.eventCRF.formLayout.id}" />&formLayoutName=<c:out value="${dedc.eventCRF.formLayout.name}" />&studySubjectLabel=<c:out value="${studySub.label}"/>&studySubjectId=<c:out value="${studySub.id}"/>&eventCrfId=<c:out value="${dedc.eventCRF.id}"/>&eventDefinitionCRFId=<c:out value="${dedc.edc.id}"/>'
   onMouseDown="javascript:setImage('bt_Reassign','images/bt_Reassign_d.gif');"
   onMouseUp="javascript:setImage('bt_Reassign','images/bt_Reassign.gif');"><span
   name="Reassign" class="icon icon-icon-reassign3" border="0" alt="<fmt:message key="reassign_crf_version" bundle="${resword}"/>" title="<fmt:message key="reassign_crf_version" bundle="${resword}"/>" align="left" hspace="6"></a>
    </td>
   </c:if>
				</tr></table>
				</td>
			</tr>
			</form>


	</c:forEach>
	<c:forEach var="dec" items="${currRow.bean.displayEventCRFs}">
	<tr>
		<td class="table_cell" width="180"><c:out value="${dec.eventCRF.crf.name}" /></td>
		<td class="table_cell" width="100"><c:out value="${dec.eventCRF.formLayout.name}" /></td>
		<td class="table_cell" bgcolor="#F5F5F5" align="center" width="20">
		<c:choose>
		 <c:when test="${dec.stage.initialDE}">
		  <span class=" icon icon-pencil-squared orange" alt="<fmt:message key="initial_data_entry" bundle="${resword}"/>" title="<fmt:message key="data_entry_started" bundle="${resword}"/>">
		 </c:when>
		 <c:when test="${dec.stage.initialDE_Complete}">
		  <span class="icon icon-pencil-squared orange" alt="<fmt:message key="initial_data_entry_complete" bundle="${resword}"/>" title="<fmt:message key="data_entry_started" bundle="${resword}"/>">
		 </c:when>
		 <c:when test="${dec.stage.doubleDE}">
		    <span class="icon icon-checkbox-checked green" alt="<fmt:message key="double_data_entry" bundle="${resword}"/>" title="<fmt:message key="double_data_entry" bundle="${resword}"/>">
		 </c:when>
		 <c:when test="${dec.stage.doubleDE_Complete}">
		   <span class="icon icon-checkbox-checked green" alt="<fmt:message key="data_entry_complete" bundle="${resword}"/>" title="<fmt:message key="data_entry_complete" bundle="${resword}"/>">
		 </c:when>
		 <c:when test="${dec.stage.admin_Editing}">
		    <span class="icon icon-pencil" alt="<fmt:message key="administrative_editing" bundle="${resword}"/>" title="<fmt:message key="administrative_editing" bundle="${resword}"/>">
		 </c:when>
		 <c:when test="${dec.stage.locked || dec.eventCRF.status.locked || dec.locked}">
		   <span class="icon icon-lock" alt="<fmt:message key="locked" bundle="${resword}"/>" title="<fmt:message key="locked" bundle="${resword}"/>">
		 </c:when>
		 <c:otherwise>
		    <span class="icon icon-file-excel red" alt="<fmt:message key="invalid" bundle="${resword}"/>" title="<fmt:message key="invalid" bundle="${resword}"/>">
		 </c:otherwise>
		</c:choose>

		</td>
		<td class="table_cell" width="80">
		<c:if test="${dec.eventCRF.updatedDate != null}">
		 <fmt:formatDate value="${dec.eventCRF.updatedDate}" pattern="${dteFormat}"/><br>
		</c:if>
		<c:choose>
		  <c:when test="${dec.eventCRF.updater.name == null}">
		    (<c:out value="${dec.eventCRF.owner.name}"/>)
		  </c:when>
		  <c:otherwise>
		   (<c:out value="${dec.eventCRF.updater.name}"/>)
		  </c:otherwise>
		 </c:choose>
		</td>

		<td class="table_cell" width="140">
		 <table border="0" cellpadding="0" cellspacing="0">
	     <tr valign="top">
	     <td>
			<c:if test="${!dec.eventCRF.status.deleted && !dec.eventCRF.status.locked && study.status.available && !currRow.bean.studyEvent.status.deleted && !userRole.monitor}">
				<c:if test="${dec.continueInitialDataEntryPermitted}">
				   <a class="accessCheck" href="EnketoFormServlet?formLayoutId=<c:out value="${dec.eventCRF.formLayout.id}"/>&studyEventId=<c:out value="${currRow.bean.studyEvent.id}"/>&eventCrfId=<c:out value="${dec.eventCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>&mode=<c:out value="edit"/>"
					onMouseDown="javascript:setImage('bt_EnterData1','images/bt_EnterData_d.gif');"
					onMouseUp="javascript:setImage('bt_EnterData1','images/bt_EnterData.gif');">
					   <span name="bt_EnterData1" class="icon icon-pencil-squared" border="0" alt="<fmt:message key="continue_entering_data" bundle="${resword}"/>" title="<fmt:message key="continue_entering_data" bundle="${resword}"/>" align="left" hspace="6">
					</a>
				</c:if>
				<c:if test="${dec.startDoubleDataEntryPermitted}">
					<a class="accessCheck" href="#"
					onMouseDown="javascript:setImage('bt_EnterData1','images/bt_EnterData_d.gif');"
					onMouseUp="javascript:setImage('bt_EnterData1','images/bt_EnterData.gif');"
					onclick="checkCRFLocked('<c:out value="${dec.eventCRF.id}"/>', 'DoubleDataEntry?eventCRFId=<c:out value="${dec.eventCRF.id}"/>&exitTo=ViewStudySubject?id=${studySub.id}');">
						<spam name="bt_EnterData1" class="icon icon-pencil-squared" border="0" alt="<fmt:message key="begin_double_data_entry" bundle="${resword}"/>" title="<fmt:message key="begin_double_data_entry" bundle="${resword}"/>" align="left" hspace="6"></a>
				</c:if>
				<c:if test="${dec.continueDoubleDataEntryPermitted}">
					<a class="accessCheck" href="#"
					onMouseDown="javascript:setImage('bt_EnterData1','images/bt_EnterData_d.gif');"
					onMouseUp="javascript:setImage('bt_EnterData1','images/bt_EnterData.gif');"
					onclick="checkCRFLocked('<c:out value="${dec.eventCRF.id}"/>', 'DoubleDataEntry?eventCRFId=<c:out value="${dec.eventCRF.id}"/>&exitTo=ViewStudySubject?id=${studySub.id}');">
					<span name="bt_EnterData1" class="icon icon-pencil-squared" border="0" alt="<fmt:message key="continue_entering_data" bundle="${resword}"/>" title="<fmt:message key="continue_entering_data" bundle="${resword}"/>" align="left" hspace="6"></a>
				</c:if>
				<c:if test="${dec.performAdministrativeEditingPermitted}">

				   <a class="accessCheck" href="EnketoFormServlet?formLayoutId=<c:out value="${dec.eventCRF.formLayout.id}"/>&studyEventId=<c:out value="${currRow.bean.studyEvent.id}"/>&eventCrfId=<c:out value="${dec.eventCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>&mode=<c:out value="edit"/>"
					onMouseDown="javascript:setImage('bt_EnterData1','images/bt_EnterData_d.gif');"
					onMouseUp="javascript:setImage('bt_EnterData1','images/bt_EnterData.gif');">
					<span name="bt_EnterData1" class="icon icon-pencil-squared" border="0" alt="<fmt:message key="administrative_editing" bundle="${resword}"/>" title="<fmt:message key="administrative_editing" bundle="${resword}"/>" align="left" hspace="6">
					</a>
				</c:if>
			<%-- locked status here --%>
				<c:if test="${dec.locked || dec.eventCRF.status.locked || dec.stage.locked || currRow.bean.studyEvent.subjectEventStatus.locked}"></c:if>
    		</c:if>
		</td>
	     <td>
	    
           <a class="accessCheck" href="EnketoFormServlet?formLayoutId=<c:out value="${dec.eventCRF.formLayout.id}"/>&studyEventId=<c:out value="${currRow.bean.studyEvent.id}"/>&eventCrfId=<c:out value="${dec.eventCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>&mode=<c:out value="view"/>"
			onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
			onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><span
		    name="bt_View1" class="icon icon-search" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6"></a>

		 </td>
		<c:choose>
		<c:when test="${!dec.eventCRF.status.deleted}">
			<c:if test="${!userRole.monitor && study.status.available}">
		  <td><a class="accessCheck" href="RemoveEventCRF?action=confirm&eventCrfId=<c:out value="${dec.eventCRF.id}"/>&studySubId=<c:out value="${studySub.id}"/>&originatingPage=<c:out value="${originatingPage}"/>"
			onMouseDown="javascript:setImage('bt_Remove1','images/bt_Remove_d.gif');"
			onMouseUp="javascript:setImage('bt_Remove1','images/bt_Remove.gif');"><span
			name="bt_Remove1" class="icon icon-cancel" border="0" alt="<fmt:message key="remove" bundle="${resword}"/>" title="<fmt:message key="remove" bundle="${resword}"/>" align="left" hspace="6"></a>
		 </td>
		 </c:if>
		</c:when>
		<c:otherwise>
			<c:if test="${(!userRole.monitor && currRow.bean.studyEvent.status.name!='auto-removed' && dec.eventCRF.status.name != 'auto-removed') && (study.status.available) && (studySub.status.available)}">
		  <td><a class="accessCheck" href="RestoreEventCRF?action=confirm&eventCrfId=<c:out value="${dec.eventCRF.id}"/>&studySubId=<c:out value="${studySub.id}"/>"
			onMouseDown="javascript:setImage('bt_Restor3','images/bt_Restore_d.gif');"
			onMouseUp="javascript:setImage('bt_Restore3','images/bt_Restore.gif');"><span
			name="bt_Restore3" class="icon icon-ccw" border="0" alt="<fmt:message key="restore" bundle="${resword}"/>" title="<fmt:message key="restore" bundle="${resword}"/>" align="left" hspace="6"></a>
		  </td>
		 </c:if>
		</c:otherwise>
		</c:choose>
		<c:if test="${(study.status.available) && (!userRole.monitor) && (!currRow.bean.studyEvent.subjectEventStatus.locked) && (dec.eventCRF.status.name != 'completed') && !(dec.stage.invalid)}">
		<td>
		 <a class="accessCheck" href="DeleteEventCRF?action=confirm&ssId=<c:out value="${studySub.id}"/>&eventCrfId=<c:out value="${dec.eventCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>"
			onMouseDown="javascript:setImage('bt_Delete1','images/bt_Delete_d.gif');"
			onMouseUp="javascript:setImage('bt_Delete1','images/bt_Delete.gif');"><span
		    name="bt_Delete1" class="icon icon-trash red" border="0" alt="<fmt:message key="delete" bundle="${resword}"/>"
			title="<fmt:message key="delete" bundle="${resword}"/>" align="left" hspace="6"></a>
		 </td>
		 </c:if>

		    <c:if test="${ (userRole.director || userRole.coordinator) &&
 (study.status.available ) && !(currRow.bean.studyEvent.subjectEventStatus.locked || currRow.bean.studyEvent.subjectEventStatus.skipped) && !(dec.stage.invalid)}">
   <td>
    <a class="accessCheck"  access_attr='<c:out value="${dec.eventCRF.id}"/>'
	   href='pages/managestudy/chooseCRFVersion?crfId=<c:out value="${dec.eventCRF.crf.id}" />&crfName=<c:out value="${dec.eventCRF.crf.name}" />&formLayoutId=<c:out value="${dec.eventCRF.formLayout.id}" />&formLayoutName=<c:out value="${dec.eventCRF.formLayout.name}" />&studySubjectLabel=<c:out value="${studySub.label}"/>&studySubjectId=<c:out value="${studySub.id}"/>&eventCrfId=<c:out value="${dec.eventCRF.id}"/>&eventDefinitionCRFId=<c:out value="${dec.eventDefinitionCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>'
   onMouseDown="javascript:setImage('bt_Reassign','images/bt_Reassign_d.gif');"
   onMouseUp="javascript:setImage('bt_Reassign','images/bt_Reassign.gif');"><span
      name="Reassign" class="icon icon-icon-reassign3" border="0" alt="<fmt:message key="reassign_crf_version" bundle="${resword}"/>" title="<fmt:message key="reassign_crf_version" bundle="${resword}"/>" align="left" hspace="6"></a>
    </td>
   </c:if>
		</tr>
		</table>
	   </td>
	 </tr>
	</c:forEach>
    </table>
	</c:otherwise>
    </c:choose>
        </td>


    </tr>
