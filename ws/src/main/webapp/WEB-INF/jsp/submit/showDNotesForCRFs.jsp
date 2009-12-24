<%@ page contentType="text/html; charset=UTF-8" %>

<%@ page import="org.akaza.openclinica.bean.core.DataEntryStage"%>
<%@ page import="org.akaza.openclinica.bean.core.SubjectEventStatus"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<c:set var="eblRowCount" value="${param.eblRowCount}" />
<!-- row number: <c:out value="${eblRowCount}"/> -->
<c:set var="eventCRFNum" value="${param.eventDefCRFNum}" />
<c:set var="resolutionStatusFromServlet" value="${param.resolutionStatus}" />

<jsp:useBean scope="request" id="currRow" class="org.akaza.openclinica.web.bean.DisplayStudySubjectRow" />
<c:set var="eventCount" value="0"/>
<c:forEach var="event" items="${currRow.bean.studyEvents}">
    <c:set var="eventCount" value="${eventCount+1}"/>
</c:forEach>
<c:set var="groups" value="3"/>
<c:forEach var="group" items="${currRow.bean.studyGroups}">
    <c:set var="groups" value="${groups+1}"/>
</c:forEach>

<c:choose>
<c:when test="${!empty currRow.bean.studyEvents}">
<c:set var="count" value="0"/>

<%-- Make sure we set this to true if any of the DisplayStudyEventBeans have disc notes,
 so that the download disc note icon displays--%>
<c:set var="hasDiscNotes" value="${false}"/>
<c:forEach var="event" items="${currRow.bean.studyEvents}">
    <c:if test="${! hasDiscNotes}">
        <c:forEach var="dnote" items="${event.studyEvent.discBeanList}">
            <c:set var="hasDiscNotes" value="${true}"/>
        </c:forEach>
    </c:if>
</c:forEach>

<c:forEach var="event" items="${currRow.bean.studyEvents}">
<!-- currRow.bean is a DisplayStudySubjectBean; has a List of StudyEventBeans;
-->
<!--event is a StudyEventBean studyEvent; -->
<!--ArrayList allEventCRFs; -->
<!--StudySubjectBean studySubject;-->
<!--havingEventCRF = false;-->
<%-- each studyEvent has a List of disc notes; --%>


<tr valign="top">

<c:if test="${count==0}">
    <td class="table_cell_left" rowspan="<c:out value="${eventCount}"/>">
        <c:out value="${currRow.bean.studySubject.label}"/>&nbsp;
    </td>
    <c:choose>
        <c:when test ="${currRow.sortingColumn >= 1 && currRow.sortingColumn < groups}">
            <td class="table_cell" rowspan="<c:out value="${eventCount}"/>" style="display: all" id="Groups_0_1_<c:out value="${eblRowCount+1}"/>">
                <c:choose>
                    <c:when test="${currRow.bean.studySubject.status.id==1}">
                        <fmt:message key="active" bundle="${resword}"/>
                    </c:when>
                    <c:otherwise>
                        <fmt:message key="inactive" bundle="${resword}"/>
                    </c:otherwise>
                </c:choose>
            </td>

            <td class="table_cell" rowspan="<c:out value="${eventCount}"/>" style="display: all" id="Groups_0_2_<c:out value="${eblRowCount+1}"/>"><c:out value="${currRow.bean.studySubject.gender}"/>&nbsp;</td>

            <c:set var="groupCount" value="3"/>
            <c:forEach var="group" items="${currRow.bean.studyGroups}">
                <td class="table_cell" rowspan="<c:out value="${eventCount}"/>" style="display: all" id="Groups_0_<c:out value="${groupCount}"/>_<c:out value="${eblRowCount+1}"/>"><c:out value="${group.studyGroupName}"/>&nbsp;</td>
                <c:set var="groupCount" value="${groupCount+1}"/>
            </c:forEach>
        </c:when>
        <c:otherwise>
            <td class="table_cell" rowspan="<c:out value="${eventCount}"/>" style="display: none" id="Groups_0_1_<c:out value="${eblRowCount+1}"/>">
                <c:choose>
                    <c:when test="${currRow.bean.studySubject.status.id==1}">
                        <fmt:message key="active" bundle="${resword}"/>
                    </c:when>
                    <c:otherwise>
                        <fmt:message key="inactive" bundle="${resword}"/>
                    </c:otherwise>
                </c:choose>
            </td>

            <td class="table_cell" rowspan="<c:out value="${eventCount}"/>" style="display: none" id="Groups_0_2_<c:out value="${eblRowCount+1}"/>"><c:out value="${currRow.bean.studySubject.gender}"/>&nbsp;</td>

            <c:set var="groupCount" value="3"/>
            <c:forEach var="group" items="${currRow.bean.studyGroups}">
                <td class="table_cell" rowspan="<c:out value="${eventCount}"/>" style="display: none" id="Groups_0_<c:out value="${groupCount}"/>_<c:out value="${eblRowCount+1}"/>"><c:out value="${group.studyGroupName}"/>&nbsp;</td>
                <c:set var="groupCount" value="${groupCount+1}"/>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</c:if>



<!-- <td class="table_cell"><c:out value="${count+1}"/></td>-->
<td class="table_cell">

    <c:choose>
        <c:when test="${event.studyEvent.subjectEventStatus.id==1}">

            <img src="images/icon_Scheduled.gif"  border="0" alt="<c:out value="${event.studyEvent.subjectEventStatus.name}"/>" style="position: relative; left: 7px;">

        </c:when>
        <c:when test="${event.studyEvent.subjectEventStatus.id==2}">
            <img src="images/icon_NotStarted.gif" alt="${event.studyEvent.subjectEventStatus.name}" title="<c:out value="${event.studyEvent.subjectEventStatus.name}"/>" border="0" style="position: relative; left: 7px;">


        </c:when>
        <c:when test="${event.studyEvent.subjectEventStatus.id==3}">
            <img src="images/icon_InitialDE.gif" alt="${event.studyEvent.subjectEventStatus.name}" title="<c:out value="${event.studyEvent.subjectEventStatus.name}"/>" border="0" style="position: relative; left: 7px;">


        </c:when>
        <c:when test="${event.studyEvent.subjectEventStatus.id==4}">
            <img src="images/icon_DEcomplete.gif" alt="${event.studyEvent.subjectEventStatus.name}" title="<c:out value="${event.studyEvent.subjectEventStatus.name}"/>" border="0" style="position: relative; left: 7px;">

        </c:when>
        <c:when test="${event.studyEvent.subjectEventStatus.id==5}">
            <img src="images/icon_Stopped.gif" alt="${event.studyEvent.subjectEventStatus.name}" title="<c:out value="${event.studyEvent.subjectEventStatus.name}"/>" border="0" style="position: relative; left: 7px;">


        </c:when>
        <c:when test="${event.studyEvent.subjectEventStatus.id==6}">
            <img src="images/icon_Skipped.gif" alt="${event.studyEvent.subjectEventStatus.name}" title="<c:out value="${event.studyEvent.subjectEventStatus.name}"/>" border="0" style="position: relative; left: 7px;">

        </c:when>
        <c:when test="${event.studyEvent.subjectEventStatus.id==7}">
            <img src="images/icon_Locked.gif" alt="${event.studyEvent.subjectEventStatus.name}" title="<c:out value="${event.studyEvent.subjectEventStatus.name}"/>" border="0" style="position: relative; left: 7px;">

        </c:when>
        <c:when test="${event.studyEvent.subjectEventStatus.id==8}">
            <img src="images/icon_Signed.gif" alt="${event.studyEvent.subjectEventStatus.name}" title="<c:out value="${event.studyEvent.subjectEventStatus.name}"/>" border="0" style="position: relative; left: 7px;">

        </c:when>
    </c:choose>
        <%--  </a><img name="ExpandIcon_<c:out value="${currRow.bean.studySubject.label}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount+count}"/>" src="images/icon_blank.gif" width="15" height="15" style="position: relative; left: 8px;">--%>


</td>
<td class="table_cell">
    <fmt:formatDate value="${event.studyEvent.dateStarted}" pattern="${dteFormat}"/>&nbsp;</td>
    <%--event = StudyEventBean
edc = EventDefinitionCRFBean
foreach event.dnotes
if dnote.crfName == edc.crfName, then include dnote--%>
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

    <%--
    UNCOMPLETED, INITIAL_DATA_ENTRY, INITIAL_DATA_ENTRY_COMPLETE, DOUBLE_DATA_ENTRY, DOUBLE_DATA_ENTRY_COMPLETE, ADMINISTRATIVE_EDITING, LOCKED
    --%>

<c:forEach var="eventDefinitionCRF" items="${eventDefCRFs}">
<%-- event is a DisplayStudyEventBean;  find out how many of each type of disc note (resolution status = 1) should be displayed --%>
<c:forEach var="discNoteBean" items="${event.studyEvent.discBeanList}">
    <c:forEach var="displayEventCRFBean" items="${event.displayEventCRFs}">
        <c:if test="${displayEventCRFBean.eventCRF.crf.name eq eventDefinitionCRF.crf.name &&
                        displayEventCRFBean.eventCRF.id eq discNoteBean.eventCRFId}">

            <c:choose>
                <c:when test="${discNoteBean.resolutionStatusId == 1}">
                    <c:set var="newDN" value="${newDN + 1}"/>
                </c:when>
                <c:when test="${discNoteBean.resolutionStatusId == 2}">
                    <c:set var="updatedDN" value="${updatedDN + 1}"/>
                </c:when>
                <c:when test="${discNoteBean.resolutionStatusId == 3}">
                    <c:set var="resolvedDN" value="${resolvedDN + 1}"/>

                </c:when>
                <c:when test="${discNoteBean.resolutionStatusId == 4}">
                    <c:set var="closedDN" value="${closedDN + 1}"/>
                </c:when>

                <c:when test="${discNoteBean.resolutionStatusId == 5}">
                    <c:set var="not_applicableDN" value="${not_applicableDN + 1}"/>
                </c:when>

            </c:choose>

        </c:if>
    </c:forEach>
</c:forEach>
<td class="table_cell"><%-- cell containing notes --%>
<div style="margin-bottom:5px;">
        <%--Display the NotStarted icon if this eventDefinitionCRF is not associated with
       any displayEventCRFBeans--%>
    <c:set var="hasCRFBean" value="${false}"/>
    <c:forEach var="displayEventCRFBean" items="${event.displayEventCRFs}">
        <c:if test="${displayEventCRFBean.eventCRF.crf.name eq eventDefinitionCRF.crf.name}">
            <c:set var="hasCRFBean" value="${true}"/>
        </c:if>
    </c:forEach>
    <c:if test="${! hasCRFBean}">
        <img
          src="images/icon_NotStarted.gif" border="0"
          alt="<fmt:message key="not_started" bundle="${resterm}"/>" title="<fmt:message key="not_started" bundle="${resterm}"/>" align="left"/>
    </c:if>

    <c:forEach var="displayEventCRFBean" items="${event.displayEventCRFs}">
        <c:if test="${displayEventCRFBean.eventCRF.crf.name eq eventDefinitionCRF.crf.name}">
            <c:choose>
                <%--<c:when test="${displayEventCRFBean.eventCRF.status.name eq 'removed'}">
                            <img
                              name="icon_Note" src="images/icon_Invalid.gif" border="0"
                              alt="<fmt:message key="removed" bundle="${resterm}"/>" title="<fmt:message key="removed" bundle="${resterm}"/>" align="left"/>
                        </c:when>--%>
                <c:when test="${! (displayEventCRFBean.eventCRF.stage.name eq '') }">
                    <%--event CRF dataentrystage--%>
                    <c:choose>
                        <c:when test="${displayEventCRFBean.eventCRF.stage.id == 0}">
                            <img
                              name="icon_Note" src="images/icon_Invalid.gif" border="0"
                              alt="<fmt:message key="invalid" bundle="${resterm}"/>" title="<fmt:message key="invalid" bundle="${resterm}"/>" align="left"/>
                        </c:when>
                        <c:when test="${displayEventCRFBean.eventCRF.stage.id == 1}">
                            <img
                              name="icon_Note" src="images/icon_NotStarted.gif" border="0"
                              alt="<fmt:message key="not_started" bundle="${resterm}"/>" title="<fmt:message key="not_started" bundle="${resterm}"/>" align="left"/>
                        </c:when>
                        <c:when test="${displayEventCRFBean.eventCRF.stage.id == 2}">
                            <img
                              name="icon_Note" src="images/icon_InitialDE.gif" border="0"
                              alt="<fmt:message key="initial_data_entry" bundle="${resterm}"/>" title="<fmt:message key="initial_data_entry" bundle="${resterm}"/>" align="left"/>
                        </c:when>
                        <c:when test="${displayEventCRFBean.eventCRF.stage.id == 3}">
                            <img
                              name="icon_Note" src="images/icon_InitialDEcomplete.gif" border="0"
                              alt="<fmt:message key="initial_data_entry_complete" bundle="${resterm}"/>" title="<fmt:message key="initial_data_entry_complete" bundle="${resterm}"/>" align="left"/>
                        </c:when>
                        <c:when test="${displayEventCRFBean.eventCRF.stage.id == 4}">
                            <img
                              name="icon_Note" src="images/icon_DDE.gif" border="0"
                              alt="<fmt:message key="double_data_entry" bundle="${resterm}"/>" title="<fmt:message key="double_data_entry" bundle="${resterm}"/>" align="left"/>
                        </c:when>
                        <c:when test="${displayEventCRFBean.eventCRF.stage.id == 5}">
                            <img
                              name="icon_Note" src="images/icon_DEcomplete.gif" border="0"
                              alt="<fmt:message key="data_entry_complete" bundle="${resterm}"/>" title="<fmt:message key="data_entry_complete" bundle="${resterm}"/>" align="left"/>
                        </c:when>
                        <c:when test="${displayEventCRFBean.eventCRF.stage.id == 6}">
                            <img
                              name="icon_Note" src="images/icon_AdminEdit.gif" border="0"
                              alt="<fmt:message key="administrative_editing" bundle="${resterm}"/>" title="<fmt:message key="administrative_editing" bundle="${resterm}"/>" align="left"/>
                        </c:when>
                        <c:when test="${displayEventCRFBean.eventCRF.stage.id == 7}">
                            <img
                              name="icon_Note" src="images/icon_Locked.gif" border="0"
                              alt="<fmt:message key="locked" bundle="${resterm}"/>" title="<fmt:message key="locked" bundle="${resterm}"/>" align="left"/>
                        </c:when>
                        <c:otherwise>
                            <img
                              name="icon_Note" src="images/icon_NotStarted.gif" border="0"
                              alt="<fmt:message key="not_started" bundle="${resterm}"/>" title="<fmt:message key="not_started" bundle="${resterm}"/>" align="left"/>
                        </c:otherwise>
                    </c:choose>
                </c:when>
                <c:otherwise>
                    <img
                      name="icon_Note" src="images/icon_Invalid.gif" border="0"
                      alt="<fmt:message key="not_started" bundle="${resterm}"/>" title="<fmt:message key="not_started" bundle="${resterm}"/>" align="left"/>
                </c:otherwise>
            </c:choose>

        </c:if>

    </c:forEach>
</div> <br />

<c:forEach var="discNoteBean" items="${event.studyEvent.discBeanList}">
    <c:forEach var="displayEventCRFBean" items="${event.displayEventCRFs}">
        <c:if test="${displayEventCRFBean.eventCRF.crf.name eq eventDefinitionCRF.crf.name &&
                        displayEventCRFBean.eventCRF.id eq discNoteBean.eventCRFId}">

            <c:choose>
                <c:when test="${discNoteBean.resolutionStatusId == 1}">
                    <c:if test="${newDisplayed eq 'false'}"><img
                      name="icon_Note" src="images/icon_Note.gif" border="0"
                      alt="<fmt:message key="Open" bundle="${resterm}"/>" title="<fmt:message key="Open" bundle="${resterm}"/>" align="left"/>
                        <c:out value=" (${newDN})"/><br /></c:if>
                    <c:set var="newDisplayed" value="true"/>
                </c:when>

                <c:when test="${discNoteBean.resolutionStatusId == 2}">
                    <c:if test="${updatedDisplayed eq 'false'}">
                        <img
                          name="icon_flagYellow" src="images/icon_flagYellow.gif" border="0"
                          alt="<fmt:message key="Updated" bundle="${resterm}"/>" title="<fmt:message key="Updated" bundle="${resterm}"/>" align="left"/>
                        <c:out value="(${updatedDN})"/><br /></c:if>
                    <c:set var="updatedDisplayed" value="true"/>
                </c:when>

                <c:when test="${discNoteBean.resolutionStatusId == 3}">
                    <c:if test="${resolvedDisplayed eq 'false'}">

                        <img
                          name="icon_flagGreen" src="images/icon_flagGreen.gif" border="0"
                          alt="<fmt:message key="Resolved" bundle="${resterm}"/>" title="<fmt:message key="Resolved" bundle="${resterm}"/>" align="left"/>
                        <c:out value="(${resolvedDN})"/><br /></c:if>
                    <c:set var="resolvedDisplayed" value="true"/>
                </c:when>

                <c:when test="${discNoteBean.resolutionStatusId == 4}">
                    <c:if test="${closedDisplayed eq 'false'}">
                        <img
                          name="icon_flagBlack" src="images/icon_flagBlack.gif" border="0"
                          alt="<fmt:message key="Closed" bundle="${resterm}"/>" title="<fmt:message key="Closed" bundle="${resterm}"/>" align="left"/>
                        <c:out value="(${closedDN})"/><br /></c:if>
                    <c:set var="closedDisplayed" value="true"/>
                </c:when>

                <c:when test="${discNoteBean.resolutionStatusId == 5}">
                    <c:if test="${not_applicableDisplayed eq 'false'}">
                        <img
                          name="icon_flagWhite" src="images/icon_flagWhite.gif" border="0"
                          alt="<fmt:message key="Not_Applicable" bundle="${resterm}"/>" title="<fmt:message key="Not_Applicable" bundle="${resterm}"/>" align="left"/>
                        <c:out value="(${not_applicableDN})"/><br /></c:if>
                    <c:set var="not_applicableDisplayed" value="true"/>
                </c:when>

            </c:choose>

        </c:if>
    </c:forEach>
</c:forEach>

    <%-- For Internet Explorer, which doesn't display some table cell's borders if the cells don't
  have any content--%>
<c:if test="${! hasDiscNotes}">&nbsp;</c:if>
</td>
</c:forEach>

<c:if test="${count==0}">
    <td class="table_cell" rowspan="<c:out value="${eventCount}"/>">

            <%--ACTIONS... --%>
        <c:set var="boolResStatus"
               value="${false}"/>
            <%-- If this HashSet has any content, then the boolResStatus value is set to true--%>
        <c:forEach var="resStatus" items="${resolutionStatusFromServlet}">
            <c:set var="boolResStatus"
                   value="${true}"/>
        </c:forEach>
            <%--    <table border="0" cellpadding="0" cellspacing="0">
        <tr>
            <td>--%>
        <c:choose>
            <c:when test="${! boolResStatus}">
                <a href="ViewNotes?viewForOne=y&id=<c:out value="${currRow.bean.studySubject.id}"/>&module=${module}"
                   onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
                   onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><img
                  hspace="2" style="float:left" name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left"></a>

                <c:if test="${hasDiscNotes}"><a href=
                  "javascript:openDocWindow('ChooseDownloadFormat?discNoteType=${discNoteType}&module=${module}&subjectId=${currRow.bean.studySubject.id}')"><img
                  hspace="2" name="bt_View1" src="images/bt_Download.gif" border="0" alt="<fmt:message key="download_discrepancy_notes" bundle="${resword}"/>" title="<fmt:message key="download_discrepancy_notes" bundle="${resword}"/>"></a>

                    <%--<a href=
                      "javascript:openDocWindow('ChooseDownloadFormat?module=manage')"><img
                      style="float:left" width="24 " height="15" name="bt_View1" src="images/bt_Download.gif" border="0" alt="<fmt:message key="download_all_discrepancy_notes" bundle="${resword}"/>" title="<fmt:message key="download_all_discrepancy_notes" bundle="${resword}"/>"></a>--%>
                </c:if>
            </c:when>
            <c:otherwise>
                <a href="ViewNotes?viewForOne=y&module=${module}&id=<c:out value="${currRow.bean.studySubject.id}"/>&resolutionStatus=<c:out value="${resolutionStatusFromServlet}"/>"
                   onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
                   onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><img
                  hspace="2" style="float:left" name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left"></a>

                <c:if test="${hasDiscNotes}"><a href=
                  "javascript:openDocWindow('ChooseDownloadFormat?subjectId=${currRow.bean.studySubject.id}&discNoteType=${discNoteType}&module=${module}&resolutionStatus=${param.resolutionStatus}')"><img
                  hspace="2" name="bt_View1" src="images/bt_Download.gif" border="0" alt="<fmt:message key="download_discrepancy_notes" bundle="${resword}"/>" title="<fmt:message key="download_discrepancy_notes" bundle="${resword}"/>"></a>

                    <%--  <a href=
        "javascript:openDocWindow('ChooseDownloadFormat?module=manage')"><img
        style="float:left" width="24 " height="15" name="bt_View1" src="images/bt_Download.gif" border="0" alt="<fmt:message key="download_all_discrepancy_notes" bundle="${resword}"/>" title="<fmt:message key="download_all_discrepancy_notes" bundle="${resword}"/>"></a>--%>
                </c:if>

            </c:otherwise>
        </c:choose>
    </td>
</c:if>
<c:set var="count" value="${count+1}"/>
</tr>

</c:forEach>

</c:when>
<%-- not study events for this displaysubjectbean --%>
<c:otherwise>
<tr valign="top">
<c:choose>
    <c:when test ="${currRow.sortingColumn >= 1 && currRow.sortingColumn < groups}">
        <%-- added width tag here, fix for complaint for narrow columns, tbh --%>
        <td class="table_cell_left" width="64"><c:out value="${currRow.bean.studySubject.label}"/>&nbsp;</td>
        <td class="table_cell" style="display: all" id="Groups_0_1_<c:out value="${eblRowCount+1}"/>">
            <c:choose>
                <c:when test="${currRow.bean.studySubject.status.id==1}">
                    <fmt:message key="active" bundle="${resword}"/>
                </c:when>
                <c:otherwise>
                    <fmt:message key="inactive" bundle="${resword}"/>
                </c:otherwise>
            </c:choose>
        </td>
        <%-- <td class="table_cell" style="display: all" id="Groups_0_2_<c:out value="${eblRowCount+1}"/>">
            <c:out value="${currRow.bean.studySubject.gender}"/>&nbsp;
        </td>--%>
        <%-- <c:set var="groupCount" value="3"/>
        <c:forEach var="group" items="${currRow.bean.studyGroups}">
            <td class="table_cell" style="display: all" id="Groups_0_<c:out value="${groupCount}"/>_<c:out value="${eblRowCount+1}"/>"><c:out value="${group.studyGroupName}"/>&nbsp;</td>
            <c:set var="groupCount" value="${groupCount+1}"/>
        </c:forEach>--%>
    </c:when>
    <c:otherwise>
        <%-- added width tag here, fix for complaint for narrow columns, tbh --%>
        <td class="table_cell_left" width="64"><c:out value="${currRow.bean.studySubject.label}"/>&nbsp;</td>
        <td class="table_cell" style="display: none" id="Groups_0_1_<c:out value="${eblRowCount+1}"/>">
            <c:choose>
                <c:when test="${currRow.bean.studySubject.status.id==1}">
                    <fmt:message key="active" bundle="${resword}"/>
                </c:when>
                <c:otherwise>
                    <fmt:message key="inactive" bundle="${resword}"/>
                </c:otherwise>
            </c:choose>
        </td>
        <%--  <td class="table_cell" style="display: none" id="Groups_0_2_<c:out value="${eblRowCount+1}"/>">
            <c:out value="${currRow.bean.studySubject.gender}"/>&nbsp;
        </td>

        <c:set var="groupCount" value="3"/>
        <c:forEach var="group" items="${currRow.bean.studyGroups}">
            <td class="table_cell" style="display: none" id="Groups_0_<c:out value="${groupCount}"/>_<c:out value="${eblRowCount+1}"/>"><c:out value="${group.studyGroupName}"/>&nbsp;</td>
            <c:set var="groupCount" value="${groupCount+1}"/>
        </c:forEach>--%>

    </c:otherwise>
</c:choose>
<!-- <td class="table_cell">&nbsp;</td>-->

<td class="table_cell">
        <%--<c:import url="../submit/eventLayer.jsp">
                        <c:param name="colCount" value="1"/>
                        <c:param name="rowCount" value="${eblRowCount}"/>
                        <c:param name="eventId" value="${event.studyEvent.id}"/>
                        <c:param name="eventStatus"><fmt:message key="not_scheduled" bundle="${resterm}"/></c:param>
                        <c:param name="eventName" value="${studyEventDef.name}"/>
                        <c:param name="subjectId" value="${currRow.bean.studySubject.id}"/>
                        <c:param name="subjectName" value="${currRow.bean.studySubject.label}"/>
                        <c:param name="eventSysStatus" value="${currRow.bean.studySubject.status.name}"/>
                        <c:param name="eventStatusName" value="not_scheduled"/>
        --%>
        <%--
        <c:param name="eventStatusName" value="${event.studyEvent.subjectEventStatus.name}"/>
        --%>
        <%-- <c:param name="eventDefId" value="${event.studyEventDefinition.id}"/>
            <c:param name="module" value="${module}"/>
        </c:import>--%>
    <img src="images/icon_NotStarted.gif" title="<fmt:message key="not_scheduled" bundle="${resterm}"/>" border="0" style="position: relative; left: 7px;">
        <%--
                    </a><img name="ExpandIcon_<c:out value="${currRow.bean.studySubject.label}"/>_<c:out value="${count}"/>_<c:out value="${eblRowCount}"/>" src="images/icon_blank.gif" width="15" height="15" style="position: relative; left: 8px;">
        --%>

</td>

<td class="table_cell">&nbsp;</td>

    <%--<c:forEach begin="1" end="${eventCRFNum}">--%>

<c:set var="edcNum" value="0"/>
<c:forEach var="edc" items="${eventDefCRFs}">
    <td class="table_cell">
        <c:choose>
            <c:when test="${currRow.bean.studySubject.status.id == 5 || currRow.bean.studySubject.status.id == 7}">
                <img
                  name="icon_Note" src="images/icon_Invalid.gif" border="0"
                  alt="<fmt:message key="not_started" bundle="${resterm}"/>" title="<fmt:message key="removed" bundle="${resterm}"/>" align="left"/>
            </c:when>
            <c:otherwise>
                <img
                  name="icon_Note" src="images/icon_NotStarted.gif" border="0"
                  alt="<fmt:message key="not_started" bundle="${resterm}"/>" title="<fmt:message key="not_started" bundle="${resterm}"/>" align="left"/>
            </c:otherwise>
        </c:choose>

            <%-- no discrepancy notes here, because there haven't been any events --%>
    </td>
    <c:set var="edcNum" value="${edcNum+1}"/>

</c:forEach>
<td class="table_cell">
        <%-- no content here, because there hasn't been any events --%>
        <%--ACTIONS--%>
        <%-- <c:set var="boolResStatus"
  value="${resolutionStatusFromServlet >= 1 && resolutionStatusFromServlet <= 4}"/>--%>
        <%--    <table border="0" cellpadding="0" cellspacing="0">
    <tr>
        <td>--%>
        <%-- <c:choose>
           <c:when test="${! boolResStatus}">
                   <a href="ViewNotes?viewForOne=y&id=<c:out value="${currRow.bean.studySubject.id}"/>&module=manage"
                      onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
                      onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><img
                     style="float:left" name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="4"></a>

                <a href=
                  "javascript:openDocWindow('ChooseDownloadFormat?subjectId=<c:out value="${currRow.bean.studySubject.id}"/>')"><img
                     style="float:left" name="bt_View1" src="images/bt_Download.gif" border="0" alt="<fmt:message key="download_discrepancy_notes" bundle="${resword}"/>" title="<fmt:message key="download_discrepancy_notes" bundle="${resword}"/>"></a>
         </c:when>
           <c:otherwise>
              <a href="ViewNotes?viewForOne=y&id=<c:out value="${currRow.bean.studySubject.id}"/>&resolutionStatus=<c:out value="${resolutionStatusFromServlet}"/>&module=manage"
                      onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
                      onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><img
                     style="float:left" name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="4"></a>

                 <a href=
                  "javascript:openDocWindow('ChooseDownloadFormat?subjectId=<c:out value="${currRow.bean.studySubject.id}"/>&resolutionStatus=<c:out value="${resolutionStatusFromServlet}"/>')"><img
                     style="float:left" name="bt_View1" src="images/bt_Download.gif" border="0" alt="<fmt:message key="download_discrepancy_notes" bundle="${resword}"/>" title="<fmt:message key="download_discrepancy_notes" bundle="${resword}"/>"></a>
           </c:otherwise>
        </c:choose>--%>

</td>
</tr>

</c:otherwise>
</c:choose>
