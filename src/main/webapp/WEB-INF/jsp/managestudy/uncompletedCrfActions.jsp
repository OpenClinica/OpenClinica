<c:if test="${!userRole.monitor && studyRelatedTostudySub.status.available && studySub.status.name != 'Removed' && studySub.status.name != 'auto-removed' && currRow.bean.studyEvent.removed !=true && currRow.bean.studyEvent.archived !=true && currRow.bean.studyEvent.locked !=true && currRow.bean.studyEvent.workflowStatus != 'SKIPPED' && currRow.bean.studyEvent.workflowStatus != 'STOPPED'}">
    <c:choose>
        <c:when test="${dedc.eventCRF.id != 0}">
            <td>
                <a class="accessCheck" href="EnketoFormServlet?formLayoutId=<c:out value="${dedc.eventCRF.formLayout.id}"/>&studyEventId=<c:out value="${currRow.bean.studyEvent.id}"/>&eventCrfId=<c:out value="${dedc.eventCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>&mode=<c:out value="edit"/>"
                  onMouseDown="javascript:setImage('bt_EnterData1','images/bt_EnterData_d.gif');"
                  onMouseUp="javascript:setImage('bt_EnterData1','images/bt_EnterData.gif');">
                 <span name="bt_EnterData1" class="icon icon-pencil-squared" border="0" alt="<fmt:message key="edit" bundle="${resword}"/>" title="<fmt:message key="edit" bundle="${resword}"/>" align="right" hspace="6">
                </a>
            </td>
        </c:when>
        <c:otherwise>
            <td>
                <a id="ide1-<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>" class="accessCheck"
                  href="EnketoFormServlet?formLayoutId=<c:out value="${dedc.edc.defaultVersionId}"/>&studyEventId=<c:out value="${currRow.bean.studyEvent.id}"/>&eventCrfId=<c:out value="${dedc.eventCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>&mode=<c:out value="edit"/>"
                  onMouseDown="javascript:setImage('bt_EnterData1','images/bt_EnterData_d.gif');"
                  onMouseUp="javascript:setImage('bt_EnterData1','images/bt_EnterData.gif');">
                 <span name="bt_EnterData1" class="icon icon-pencil-squared" border="0" alt="<fmt:message key="edit" bundle="${resword}"/>" title="<fmt:message key="edit" bundle="${resword}"/>" align="right" hspace="6">
                </a>
            </td>
        </c:otherwise>
    </c:choose>
</c:if>
<td>
  <a id="ide2-<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>" class="accessCheck"
      href="EnketoFormServlet?formLayoutId=<c:out value="${dedc.edc.defaultVersionId}"/>&studyEventId=<c:out value="${currRow.bean.studyEvent.id}"/>&eventCrfId=<c:out value="${dedc.eventCRF.id}"/>&originatingPage=<c:out value="${originatingPage}"/>&mode=<c:out value="view"/>"
      onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
      onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');">
      <span name="bt_View1" class="icon icon-search" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6">
  </a>
</td>
