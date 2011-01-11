<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%--isELIgnored="false" --%>


<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<%--  resolutionStatus is a HashSet of resolutionStatus values --%>
<c:set var="resolutionStatusFromServlet" value="${resolutionStatus}"/>

<c:set var="discNoteType" value="${discNoteType}"/>

<c:set var="eblRowCount" value="${param.eblRowCount}" />
<!-- row number: <c:out value="${eblRowCount}"/> -->

<jsp:useBean scope="request" id="currRow" class=
  "org.akaza.openclinica.web.bean.DisplayStudySubjectRow" />
<c:set var="groups" value="3"/>
<c:forEach var="group" items="${currRow.bean.studyGroups}">
    <c:set var="groups" value="${groups+1}"/>
</c:forEach>
<tr valign="top">
<c:choose>
    <c:when test ="${currRow.sortingColumn >= 1 && currRow.sortingColumn < groups}">
        <td class="table_cell_left"><c:out value="${currRow.bean.studySubject.label}"/>&nbsp;</td>
        <td class="table_cell" id="Groups_0_1_<c:out value="${eblRowCount+1}"/>">
            <c:choose>
                <c:when test="${currRow.bean.studySubject.status.id==1 || currRow.bean.studySubject.status.id==8 }">
                    <c:if test="${currRow.bean.studySubject.status.id==1}"><fmt:message key="active" bundle="${resword}"/></c:if>
                    <c:if test="${currRow.bean.studySubject.status.id==8}"><fmt:message key="signed" bundle="${resword}"/></c:if>
                </c:when>
                <c:otherwise>
                    <fmt:message key="inactive" bundle="${resword}"/>
                </c:otherwise>
            </c:choose>
        </td>
        <%--<td class="table_cell"><c:out value="${currRow.bean.studySubject.oid}"/></td>--%>
        <%-- <td class="table_cell" id="Groups_0_2_<c:out value="${eblRowCount+1}"/>"><c:out value="${currRow.bean.studySubject.gender}"/>&nbsp;</td>--%>

        <%--<c:set var="groupCount" value="3"/>
        <c:forEach var="group" items="${currRow.bean.studyGroups}">
            <td class="table_cell" id="Groups_0_<c:out value="${groupCount}"/>_<c:out value="${eblRowCount+1}"/>"><c:out value="${group.studyGroupName}"/>&nbsp;</td>
            <c:set var="groupCount" value="${groupCount+1}"/>
        </c:forEach>--%>
    </c:when>
    <c:otherwise>
        <td class="table_cell_left"><c:out value="${currRow.bean.studySubject.label}"/>&nbsp;</td>
        <td class="table_cell" id="Groups_0_1_<c:out value="${eblRowCount+1}"/>">
            <c:choose>
                <c:when test="${currRow.bean.studySubject.status.id==1 || currRow.bean.studySubject.status.id==8 }">
                    <c:if test="${currRow.bean.studySubject.status.id==1}"><fmt:message key="active" bundle="${resword}"/></c:if>
                    <c:if test="${currRow.bean.studySubject.status.id==8}"><fmt:message key="signed" bundle="${resword}"/></c:if>
                </c:when>
                <c:otherwise>
                    <fmt:message key="inactive" bundle="${resword}"/>
                </c:otherwise>
            </c:choose>
        </td>
        <%-- <td class="table_cell"><c:out value="${currRow.bean.studySubject.oid}"/></td>
        <td class="table_cell" style="display: none" id="Groups_0_2_<c:out value="${eblRowCount+1}"/>"><c:out value="${currRow.bean.studySubject.gender}"/>&nbsp;</td>

        <c:set var="groupCount" value="3"/>
        <c:forEach var="group" items="${currRow.bean.studyGroups}">
            <td class="table_cell" style="display: none" id="Groups_0_<c:out value="${groupCount}"/>_<c:out value="${eblRowCount+1}"/>"><c:out value="${group.studyGroupName}"/>&nbsp;</td>
            <c:set var="groupCount" value="${groupCount+1}"/>
        </c:forEach>--%>
    </c:otherwise>
</c:choose>

<td class="table_cell">${currRow.bean.siteName}&nbsp;</td>

<c:set var ="prevDefId" value="1"/>
<c:set var="currDefId" value="1"/>
<c:set var ="repeatNum" value="0"/>
<c:set var="count" value="${0}"/>
<%--  Do not include the download link for the subjects that do not have disc notes--%>
<c:set var="hasDiscNotes" value="${false}"/>

<c:forEach var="event" items="${currRow.bean.studyEvents}">

<c:set var="currEvent" scope="request" value="${event}" />
<c:set var="discBeanList" scope="request" value="${event.discBeanList}" />


<c:if test="${! (empty discBeanList)}"><c:set var="hasDiscNotes" value="${true}"/></c:if>

<td class="table_cell">
    <%--  <table border="0" cellpadding="0" cellspacing="0">
<tr valign="top">
    <td>--%>
    <%-- ${currRow.bean.studySubject.id} : displaystudysubject
   event: studyevent--%>
<div style="margin-bottom:5px;">
    <c:choose>
        <c:when test="${event.subjectEventStatus.id == 1}">
            <img
              name="icon_Note" src="images/icon_Scheduled.gif" border="0"
              alt="<fmt:message key="not_started" bundle="${resterm}"/>" title="<fmt:message key="not_started" bundle="${resterm}"/>" align="left"/>
        </c:when>
        <c:when test="${event.subjectEventStatus.id == 2}">
            <img
              name="icon_Note" src="images/icon_NotStarted.gif" border="0"
              alt="<fmt:message key="not_scheduled" bundle="${resterm}"/>" title="<fmt:message key="not_scheduled" bundle="${resterm}"/>" align="left"/>
        </c:when>
        <c:when test="${event.subjectEventStatus.id == 3}">
            <img
              name="icon_Note" src="images/icon_InitialDE.gif" border="0"
              alt="<fmt:message key="data_entry_started" bundle="${resterm}"/>" title="<fmt:message key="data_entry_started" bundle="${resterm}"/>" align="left"/>
        </c:when>
        <c:when test="${event.subjectEventStatus.id == 4}">
            <img
              name="icon_Note" src="images/icon_DEcomplete.gif" border="0"
              alt="<fmt:message key="completed" bundle="${resterm}"/>" title="<fmt:message key="completed" bundle="${resterm}"/>" align="left"/>
        </c:when>
        <c:when test="${event.subjectEventStatus.id == 5}">
            <img
              name="icon_Note" src="images/icon_Stopped.gif" border="0"
              alt="<fmt:message key="stopped" bundle="${resterm}"/>" title="<fmt:message key="stopped" bundle="${resterm}"/>" align="left"/>
        </c:when>
        <c:when test="${event.subjectEventStatus.id == 6}">
            <img
              name="icon_Note" src="images/icon_Skipped.gif" border="0"
              alt="<fmt:message key="skipped" bundle="${resterm}"/>" title="<fmt:message key="skipped" bundle="${resterm}"/>" align="left"/>
        </c:when>
        <c:when test="${event.subjectEventStatus.id == 7}">
            <img
              name="icon_Note" src="images/icon_Locked.gif" border="0"
              alt="<fmt:message key="locked" bundle="${resterm}"/>" title="<fmt:message key="locked" bundle="${resterm}"/>" align="left"/>
        </c:when>
         <c:when test="${event.subjectEventStatus.id == 8}">
            <img
              name="icon_Note" src="images/icon_Signed.gif" border="0"
              alt="<fmt:message key="signed" bundle="${resterm}"/>" title="<fmt:message key="signed" bundle="${resterm}"/>" align="left"/>
        </c:when>
    </c:choose>
</div> <br />
    <%-- keep track of the number of disc notes of each status --%>
<c:set var="newDN" value="0"/>
<c:set var="updatedDN" value="0"/>
<c:set var="resolvedDN" value="0"/>
<c:set var="closedDN" value="0"/>
<c:set var="not_applicableDN" value="0"/>


<c:set var="newDisplayed" value="false"/>
<c:set var="updatedDisplayed" value="false"/>
<c:set var="resolvedDisplayed" value="false"/>
<c:set var="closedDisplayed" value="false"/>
<c:set var="not_applicableDisplayed" value="false"/>


    <%--    dn: ${hasDiscNotes}--%>

    <%-- find out how many of each type of disc note (resolution status = 1) should be displayed --%>
<c:forEach var="discNote"  items="${discBeanList}">

    <c:choose>
        <c:when test="${discNote.resolutionStatusId == 1}">
            <c:set var="newDN" value="${newDN + 1}"/>
        </c:when>
        <c:when test="${discNote.resolutionStatusId == 2}">
            <c:set var="updatedDN" value="${updatedDN + 1}"/>
        </c:when>
        <c:when test="${discNote.resolutionStatusId == 3}">
            <c:set var="resolvedDN" value="${resolvedDN + 1}"/>

        </c:when>
        <c:when test="${discNote.resolutionStatusId == 4}">
            <c:set var="closedDN" value="${closedDN + 1}"/>
        </c:when>

        <c:when test="${discNote.resolutionStatusId == 5}">
            <c:set var="not_applicableDN" value="${not_applicableDN + 1}"/>
        </c:when>

    </c:choose>


</c:forEach>


<c:forEach var="discNote"  items="${discBeanList}">

    <c:choose>
        <c:when test="${discNote.resolutionStatusId == 1}">
            <c:if test="${newDisplayed eq 'false'}"><img
              name="icon_Note" src="images/icon_Note.gif" border="0"
              alt="<fmt:message key="Open" bundle="${resterm}"/>" title="<fmt:message key="Open" bundle="${resterm}"/>" align="left"/>
                <c:out value=" (${newDN})"/><br /></c:if>
            <c:set var="newDisplayed" value="true"/>
        </c:when>
        <c:when test="${discNote.resolutionStatusId == 2}">
            <c:if test="${updatedDisplayed eq 'false'}">
                <img
                  name="icon_flagYellow" src="images/icon_flagYellow.gif" border="0"
                  alt="<fmt:message key="Updated" bundle="${resterm}"/>" title="<fmt:message key="Updated" bundle="${resterm}"/>" align="left"/>
                <c:out value="(${updatedDN})"/><br /></c:if>
            <c:set var="updatedDisplayed" value="true"/>
        </c:when>

        <c:when test="${discNote.resolutionStatusId == 3}">
            <c:if test="${resolvedDisplayed eq 'false'}">

                <img
                  name="icon_flagGreen" src="images/icon_flagGreen.gif" border="0"
                  alt="<fmt:message key="Resolved" bundle="${resterm}"/>" title="<fmt:message key="Resolved" bundle="${resterm}"/>" align="left"/>
                <c:out value="(${resolvedDN})"/><br /></c:if>
            <c:set var="resolvedDisplayed" value="true"/>
        </c:when>

        <c:when test="${discNote.resolutionStatusId == 4}">
            <c:if test="${closedDisplayed eq 'false'}">
                <img
                  name="icon_flagBlack" src="images/icon_flagBlack.gif" border="0"
                  alt="<fmt:message key="Closed" bundle="${resterm}"/>" title="<fmt:message key="Closed" bundle="${resterm}"/>" align="left"/>
                <c:out value="(${closedDN})"/><br /></c:if>
            <c:set var="closedDisplayed" value="true"/>
        </c:when>

        <c:when test="${discNote.resolutionStatusId == 5}">
            <c:if test="${not_applicableDisplayed eq 'false'}">
                <img
                  name="icon_flagWhite" src="images/icon_flagWhite.gif" border="0"
                  alt="<fmt:message key="Not_Applicable" bundle="${resterm}"/>" title="<fmt:message key="Not_Applicable" bundle="${resterm}"/>" align="left"/>
                <c:out value="(${not_applicableDN})"/><br /></c:if>
            <c:set var="not_applicableDisplayed" value="true"/>
        </c:when>

    </c:choose>

</c:forEach>


    <%-- </td>


   <td valign="top">--%>
<c:if test="${event.repeatingNum>1}">
    <!--&nbsp;-->
    <br />
    <strong style="text-align:left">[<c:out value="${event.repeatingNum}"/>]</strong>
</c:if>
<!--</td>

    </tr>
</table>&nbsp;-->
    <%-- For Internet Explorer, which doesn't display some table cell's borders if they don't
  have any content--%>
<c:if test="${! hasDiscNotes}">&nbsp;</c:if>
</td>

<c:set var="count" value="${count+1}"/>
</c:forEach>

<td class="table_cell" style="white-space:nowrap">
    <c:set var="boolResStatus"
           value="${false}"/>
    <%-- If this HashSet has any content, then the boolResStatus value is set to true--%>
    <c:forEach var="resStatus" items="${resolutionStatusFromServlet}">
        <c:set var="boolResStatus"
           value="${true}"/>
    </c:forEach>

    <c:set var="booldiscNoteType"
           value="${discNoteType >= 1 && discNoteType <= 7}"/>
    <%--    <table border="0" cellpadding="0" cellspacing="0">
<tr>
    <td>--%>
    <c:choose>
        <c:when test="${! boolResStatus}">
            <a href="ViewNotes?viewForOne=y&id=<c:out value="${currRow.bean.studySubject.id}"/>&module=${module}"
               onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
               onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><img
              hspace="2" style="float:left" name="bt_View1" src="images/bt_View.gif" width="24 " height="15" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left"></a>

            <!--align="left" -->
            <c:if test="${hasDiscNotes}"><a href=
              "javascript:openDocWindow('ChooseDownloadFormat?subjectId=<c:out value="${currRow.bean.studySubject.id}"/>&discNoteType=${discNoteType}&module=${module}')"><img
              hspace="2" width="24 " height="15" name="bt_View1" src="images/bt_Download.gif" border="0" alt="<fmt:message key="download_discrepancy_notes" bundle="${resword}"/>" title="<fmt:message key="download_discrepancy_notes" bundle="${resword}"/>"></a>


            </c:if>
        </c:when>
        <c:otherwise>
            <a href="ViewNotes?viewForOne=y&id=<c:out value="${currRow.bean.studySubject.id}"/>&resolutionStatus=${param.resolutionStatus}&discNoteType=${discNoteType}&module=${module}"
               onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
               onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><img
              hspace="4" style="float:left" width="24 " height="15" name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left"></a>

            <c:if test="${hasDiscNotes}"><a href=
              "javascript:openDocWindow('ChooseDownloadFormat?subjectId=<c:out value="${currRow.bean.studySubject.id}"/>&discNoteType=${discNoteType}&resolutionStatus=${param.resolutionStatus}')"><img
              hspace="4" width="24 " height="15" name="bt_View1" src="images/bt_Download.gif" border="0" alt="<fmt:message key="download_discrepancy_notes" bundle="${resword}"/>" title="<fmt:message key="download_discrepancy_notes" bundle="${resword}"/>"></a>

                <%--<a href=
    "javascript:openDocWindow('ChooseDownloadFormat?module=manage')"><img
    style="float:left" width="24 " height="15" name="bt_View1" src="images/bt_Download.gif" border="0" alt="<fmt:message key="download_all_discrepancy_notes" bundle="${resword}"/>" title="<fmt:message key="download_all_discrepancy_notes" bundle="${resword}"/>"></a>--%>
            </c:if>
        </c:otherwise>
    </c:choose>     <%--  </td>
            <c:choose>
                <c:when test="${!currRow.bean.studySubject.status.deleted}">
                    <td><a href="RemoveStudySubject?action=confirm&id=<c:out value="${currRow.bean.studySubject.id}"/>&subjectId=<c:out value="${currRow.bean.studySubject.subjectId}"/>&studyId=<c:out value="${currRow.bean.studySubject.studyId}"/>"  align="right"
                           onMouseDown="javascript:setImage('bt_Remove1','images/bt_Remove_d.gif');"
                           onMouseUp="javascript:setImage('bt_Remove1','images/bt_Remove.gif');"><img
                      name="bt_Remove1" src="images/bt_Remove.gif" border="0" alt="<fmt:message key="remove" bundle="${resword}"/>" title="<fmt:message key="remove" bundle="${resword}"/>" align="left" hspace="6"></a>
                    </td>
                    <td><a href="ReassignStudySubject?id=<c:out value="${currRow.bean.studySubject.id}"/>"
                           onMouseDown="javascript:setImage('bt_Reassign1','images/bt_Reassign_d.gif');"
                           onMouseUp="javascript:setImage('bt_Reassign1','images/bt_Reassign.gif');"><img
                      name="bt_Reassign1" src="images/bt_Reassign.gif" border="0" alt="<fmt:message key="reassign" bundle="${resword}"/>" title="<fmt:message key="reassign" bundle="${resword}"/>" align="left" hspace="6"></a>
                    </td>

                </c:when>
                <c:otherwise>
                    <td>
                        <a href="RestoreStudySubject?action=confirm&id=<c:out value="${currRow.bean.studySubject.id}"/>&subjectId=<c:out value="${currRow.bean.studySubject.subjectId}"/>&studyId=<c:out value="${currRow.bean.studySubject.studyId}"/>"
                           onMouseDown="javascript:setImage('bt_Restor3','images/bt_Restore_d.gif');"
                           onMouseUp="javascript:setImage('bt_Restore3','images/bt_Restore.gif');"><img
                          name="bt_Restore3" src="images/bt_Restore.gif" border="0" alt="<fmt:message key="restore" bundle="${resword}"/>" title="<fmt:message key="restore" bundle="${resword}"/>" align="left" hspace="6"></a>
                    </td>
                </c:otherwise>
            </c:choose>
        </tr>
    </table>--%>
</td>
</tr>

