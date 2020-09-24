<c:if test="${ !userRole.monitor && studyRelatedTostudySub.status.available && studySub.status.name != 'Removed' && studySub.status.name != 'auto-removed' && currRow.bean.studyEvent.removed !=true && currRow.bean.studyEvent.archived !=true && currRow.bean.studyEvent.locked !=true && dec.eventCRF.removed != true && dec.eventCRF.archived != true && currRow.bean.studyEvent.workflowStatus != 'SKIPPED' && currRow.bean.studyEvent.workflowStatus != 'STOPPED' && dec.eventCRF.formLayout.status.name != 'Removed'}">
    <c:if test="${dec.eventCRF.workflowStatus != 'COMPLETED'}">
      <td>
        <a class="accessCheck" href="EnketoFormServlet?formLayoutId=<c:out value="${dec.eventCRF.formLayout.id}"/>&studyEventId=<c:out value="${currRow.bean.studyEvent.id}"/>&eventCrfId=<c:out value="${dec.eventCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>&mode=<c:out value="edit"/>"onMouseDown="javascript:setImage('bt_EnterData1','images/bt_EnterData_d.gif');"onMouseUp="javascript:setImage('bt_EnterData1','images/bt_EnterData.gif');">
            <span name="bt_EnterData1" class="icon icon-pencil-squared" border="0" alt="<fmt:message key="continue_entering_data" bundle="${resword}"/>" title="<fmt:message key="continue_entering_data" bundle="${resword}"/>" align="left" hspace="6">
        </a>
      </td>
    </c:if>

    <c:if test="${dec.eventCRF.workflowStatus == 'COMPLETED'}">
      <td>
        <a class="accessCheck" href="EnketoFormServlet?formLayoutId=<c:out value="${dec.eventCRF.formLayout.id}"/>&studyEventId=<c:out value="${currRow.bean.studyEvent.id}"/>&eventCrfId=<c:out value="${dec.eventCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>&mode=<c:out value="edit"/>"onMouseDown="javascript:setImage('bt_EnterData1','images/bt_EnterData_d.gif');"onMouseUp="javascript:setImage('bt_EnterData1','images/bt_EnterData.gif');">
            <span name="bt_EnterData1" class="icon icon-pencil-squared" border="0" alt="<fmt:message key="administrative_editing" bundle="${resword}"/>" title="<fmt:message key="administrative_editing" bundle="${resword}"/>" align="left" hspace="6">
        </a>
      </td>
    </c:if>
      <%-- locked status here --%>
  </c:if>
  <td>
      <a class="accessCheck" href="EnketoFormServlet?formLayoutId=<c:out value="${dec.eventCRF.formLayout.id}"/>&studyEventId=<c:out value="${currRow.bean.studyEvent.id}"/>&eventCrfId=<c:out value="${dec.eventCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>&mode=<c:out value="view"/>"
      onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
      onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><span
      name="bt_View1" class="icon icon-search" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6"></a>
  </td>
          <c:if test="${!userRole.monitor && studyRelatedTostudySub.status.available && studySub.status.name != 'Removed' && studySub.status.name != 'auto-removed' && currRow.bean.studyEvent.removed !=true && currRow.bean.studyEvent.archived !=true && dec.eventCRF.removed != true && dec.eventCRF.archived != true && currRow.bean.studyEvent.locked !=true && dec.eventCRF.workflowStatus != 'NOT_STARTED'}">
              <td>
                  <a class="accessCheck" href="RemoveEventCRF?action=confirm&eventCrfId=<c:out value="${dec.eventCRF.id}"/>&studySubId=<c:out value="${studySub.id}"/>&originatingPage=<c:out value="${originatingPage}"/>"
                  onMouseDown="javascript:setImage('bt_Remove1','images/bt_Remove_d.gif');"
                  onMouseUp="javascript:setImage('bt_Remove1','images/bt_Remove.gif');"><span
                  name="bt_Remove1" class="icon icon-cancel" border="0" alt="<fmt:message key="remove" bundle="${resword}"/>" title="<fmt:message key="remove" bundle="${resword}"/>" align="left" hspace="6"></a>
              </td>
          </c:if>
          <c:if test="${!userRole.monitor && studyRelatedTostudySub.status.available && studySub.status.name != 'Removed' && studySub.status.name != 'auto-removed' && currRow.bean.studyEvent.removed !=true && currRow.bean.studyEvent.archived !=true && currRow.bean.studyEvent.locked !=true && dec.eventCRF.removed == true  && dec.eventCRF.archived != true }">
              <td>
                  <a class="accessCheck" href="RestoreEventCRF?action=confirm&eventCrfId=<c:out value="${dec.eventCRF.id}"/>&studySubId=<c:out value="${studySub.id}"/>"
                  onMouseDown="javascript:setImage('bt_Restor3','images/bt_Restore_d.gif');"
                  onMouseUp="javascript:setImage('bt_Restore3','images/bt_Restore.gif');"><span
                  name="bt_Restore3" class="icon icon-ccw" border="0" alt="<fmt:message key="restore" bundle="${resword}"/>" title="<fmt:message key="restore" bundle="${resword}"/>" align="left" hspace="6"></a>
              </td>
          </c:if>

  <c:if test="${!userRole.monitor && studyRelatedTostudySub.status.available && studySub.status.name != 'Removed' && studySub.status.name != 'auto-removed' && currRow.bean.studyEvent.removed !=true && currRow.bean.studyEvent.archived !=true && currRow.bean.studyEvent.locked !=true && currRow.bean.studyEvent.workflowStatus != 'SKIPPED' && currRow.bean.studyEvent.workflowStatus != 'STOPPED' && dec.eventCRF.removed != true && dec.eventCRF.archived != true && dec.eventCRF.workflowStatus != 'NOT_STARTED' && dec.eventCRF.formLayout.status.name != 'Removed'}">
      <td>
          <a class="accessCheck" href="DeleteEventCRF?action=confirm&ssId=<c:out value="${studySub.id}"/>&eventCrfId=<c:out value="${dec.eventCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>"
          onMouseDown="javascript:setImage('bt_Delete1','images/bt_Delete_d.gif');"
          onMouseUp="javascript:setImage('bt_Delete1','images/bt_Delete.gif');"><span
          name="bt_Delete1" class="icon icon-trash" border="0" alt="<fmt:message key="clear_form" bundle="${resword}"/>"
          title="<fmt:message key="clear_form" bundle="${resword}"/>" align="left" hspace="6"></a>
      </td>
  </c:if>
  <c:set var="versionCount" value="0"/>
  <c:forEach var="version" items="${dec.eventDefinitionCRF.versions}">
      <c:set var="versionCount" value="${versionCount+1}"/>
  </c:forEach>
  <c:set var="differentFormVersionAvailable" value="${false}" />
  <c:if test="${versionCount==1}">
      <c:forEach var="version" items="${dec.eventDefinitionCRF.versions}">
          <c:set var="formLayoutId" value="${version.id}" />
          <c:if test="${formLayoutId != dec.eventCRF.formLayout.id}">
              <c:set var="differentFormVersionAvailable" value="${true}" />
          </c:if>
      </c:forEach>
  </c:if>

  <c:if test="${ (userRole.director || userRole.coordinator) && (versionCount>1 || differentFormVersionAvailable) && studyRelatedTostudySub.status.available  && studySub.status.name != 'Removed' && studySub.status.name != 'auto-removed' && currRow.bean.studyEvent.removed !=true && currRow.bean.studyEvent.archived !=true  && currRow.bean.studyEvent.locked !=true && currRow.bean.studyEvent.workflowStatus != 'SKIPPED' && currRow.bean.studyEvent.workflowStatus != 'STOPPED' && dec.eventCRF.removed != true && dec.eventCRF.archived != true }">
      <td>
          <a class="accessCheck"  access_attr='<c:out value="${dec.eventCRF.id}"/>'
          href='pages/managestudy/chooseCRFVersion?crfId=<c:out value="${dec.eventCRF.crf.id}" />&crfName=<c:out value="${dec.eventCRF.crf.name}" />&formLayoutId=<c:out value="${dec.eventCRF.formLayout.id}" />&formLayoutName=<c:out value="${dec.eventCRF.formLayout.name}" />&studySubjectLabel=<c:out value="${studySub.label}"/>&studySubjectId=<c:out value="${studySub.id}"/>&eventCrfId=<c:out value="${dec.eventCRF.id}"/>&eventDefinitionCRFId=<c:out value="${dec.eventDefinitionCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>'
          onMouseDown="javascript:setImage('bt_Reassign','images/bt_Reassign_d.gif');"
          onMouseUp="javascript:setImage('bt_Reassign','images/bt_Reassign.gif');"><span
          name="Reassign" class="icon icon-icon-reassign3" border="0" alt="<fmt:message key="reassign_crf_version" bundle="${resword}"/>" title="<fmt:message key="reassign_crf_version" bundle="${resword}"/>" align="left" hspace="6"></a>
      </td>
  </c:if>
