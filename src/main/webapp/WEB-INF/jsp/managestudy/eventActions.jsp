<td>
    <a href="EnterDataForStudyEvent?eventId=<c:out value="${currRow.bean.studyEvent.id}"/>"
    onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
    onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><span
    name="bt_View1" class="icon icon-search" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6"></a>
</td>
<c:if test="${ !userRole.monitor && studyRelatedTostudySub.status.available && studySub.status.name != 'Removed' && studySub.status.name != 'auto-removed' && currRow.bean.studyEvent.removed != true  && currRow.bean.studyEvent.archived != true && currRow.bean.studyEvent.locked != true }">
    <td>
        <a href="UpdateStudyEvent?event_id=<c:out value="${currRow.bean.studyEvent.id}"/>&ss_id=<c:out value="${studySub.id}"/>"
        onMouseDown="javascript:setImage('bt_Edit1','images/bt_Edit_d.gif');"
        onMouseUp="javascript:setImage('bt_Edit1','images/bt_Edit.gif');"><span
        name="bt_Edit1" class="icon icon-pencil" border="0" alt="<fmt:message key="edit" bundle="${resword}"/>" title="<fmt:message key="edit" bundle="${resword}"/>" align="left" hspace="6"></a>
    </td>
</c:if>
<%--
OC-13202
AC1: If an event is both Locked and Removed, users with locking access should be able to Unlock the event. (i.e., users with unlock permission would be able to View or Unlock in this case)
AC2: If an event is Removed and not locked, user will not be able to Lock the event.
--%>
<c:if test="${userRole.manageStudy && study.status.available && studySub.status.name != 'Removed' && studySub.status.name != 'auto-removed' && currRow.bean.studyEvent.archived != true}">
    <c:choose>
        <c:when test="${currRow.bean.studyEvent.removed == true && currRow.bean.studyEvent.locked == true}">
            <td>
                <a href="UpdateStudyEvent?action=submit&newStatus=UnLocked&statusId=<c:out value="${currRow.bean.studyEvent.workflowStatus}"/>&event_id=<c:out value="${currRow.bean.studyEvent.id}"/>&ss_id=<c:out value="${studySub.id}"/>" onMouseDown="javascript:setImage('bt_Unlock','icon icon-lock-open');" onMouseUp="javascript:setImage('bt_Unlock','icon icon-lock-open');"><span name="bt_Unlock" class="icon icon-lock-open" border="0" alt="<fmt:message key="unlock" bundle="${resword}"/>" title="<fmt:message key="unlock" bundle="${resword}"/>" align="left" hspace="6"></a>
            </td>
        </c:when>
        <c:otherwise>
            <c:if test="${currRow.bean.studyEvent.removed != true}">
                <c:choose>
                    <c:when test="${currRow.bean.studyEvent.locked != true}">
                        <td>
                            <a href="UpdateStudyEvent?action=submit&newStatus=Locked&statusId=<c:out value="${currRow.bean.studyEvent.workflowStatus}"/>&event_id=<c:out value="${currRow.bean.studyEvent.id}"/>&ss_id=<c:out value="${studySub.id}"/>" onMouseDown="javascript:setImage('bt_Lock1','icon icon-lock');" onMouseUp="javascript:setImage('bt_Lock1','icon icon-lock');"><span name="bt_lock1" class="icon icon-lock" border="0" alt="<fmt:message key="lock" bundle="${resword}"/>" title="<fmt:message key="lock" bundle="${resword}"/>" align="left" hspace="6"></a>
                        </td>
                    </c:when>
                    <c:otherwise>
                        <td>
                            <a href="UpdateStudyEvent?action=submit&newStatus=UnLocked&statusId=<c:out value="${currRow.bean.studyEvent.workflowStatus}"/>&event_id=<c:out value="${currRow.bean.studyEvent.id}"/>&ss_id=<c:out value="${studySub.id}"/>" onMouseDown="javascript:setImage('bt_Unlock','icon icon-lock-open');" onMouseUp="javascript:setImage('bt_Unlock','icon icon-lock-open');"><span name="bt_Unlock" class="icon icon-lock-open" border="0" alt="<fmt:message key="unlock" bundle="${resword}"/>" title="<fmt:message key="unlock" bundle="${resword}"/>" align="left" hspace="6"></a>
                        </td>
                    </c:otherwise>
                </c:choose>
            </c:if>
        </c:otherwise>
    </c:choose>
</c:if>
<c:if test="${!userRole.monitor && studyRelatedTostudySub.status.available && studySub.status.name != 'Removed' && studySub.status.name != 'auto-removed' && currRow.bean.studyEvent.removed != true && currRow.bean.studyEvent.archived != true && currRow.bean.studyEvent.locked != true}">
    <td>
        <a href="RemoveStudyEvent?action=confirm&id=<c:out value="${currRow.bean.studyEvent.id}"/>&studySubId=<c:out value="${studySub.id}"/>"
        onMouseDown="javascript:setImage('bt_Remove1','images/bt_Remove_d.gif');"
        onMouseUp="javascript:setImage('bt_Remove1','images/bt_Remove.gif');"><span
        name="bt_Remove1" class="icon icon-cancel" border="0" alt="<fmt:message key="remove" bundle="${resword}"/>" title="<fmt:message key="remove" bundle="${resword}"/>" align="left" hspace="6"></a>
    </td>
</c:if>
<c:if test="${!userRole.monitor && studyRelatedTostudySub.status.available && studySub.status.name != 'Removed' && studySub.status.name != 'auto-removed' &&  currRow.bean.studyEvent.removed == true && currRow.bean.studyEvent.archived != true && currRow.bean.studyEvent.locked != true}">
    <td>
        <a href="RestoreStudyEvent?action=confirm&id=<c:out value="${currRow.bean.studyEvent.id}"/>&studySubId=<c:out value="${studySub.id}"/>"
        onMouseDown="javascript:setImage('bt_Restor3','images/bt_Restore_d.gif');"
        onMouseUp="javascript:setImage('bt_Restore3','images/bt_Restore.gif');"><span
        name="bt_Restore3" class="icon icon-ccw" border="0" alt="<fmt:message key="restore" bundle="${resword}"/>" title="<fmt:message key="restore" bundle="${resword}"/>" align="left" hspace="6"></a>
    </td>
</c:if>
<c:if test="${userRole.isInvestigator() && currRow.bean.studyEvent.removed != true && currRow.bean.studyEvent.archived != true && (currRow.bean.studyEvent.workflowStatus == 'COMPLETED' || currRow.bean.studyEvent.workflowStatus == 'STOPPED' || currRow.bean.studyEvent.workflowStatus == 'SKIPPED') && currRow.bean.isSignAble() && studySub.status.name != 'Removed' && studySub.status.name != 'auto-removed'}">
    <td>
        <a onmouseup="javascript:setImage('bt_View1','icon icon-icon-sign');" onmousedown="javascript:setImage('bt_View1','icon icon-icon-sign');" href="UpdateStudyEvent?action=confirm&statusId=signed&ss_id=<c:out value="${studySub.id}"/>&event_id=<c:out value="${currRow.bean.studyEvent.id}"/>&first_sign=true"><span hspace="2" border="0" title="Sign" alt="Sign" class="icon icon-icon-sign" name="bt_sign"></span></a>
    </td>
</c:if>
