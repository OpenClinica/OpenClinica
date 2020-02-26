<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.page_messages" var="resmessages"/>


<jsp:include page="include/managestudy_top_pages.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="../images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

        <b><fmt:message key="instructions" bundle="${restext}"/></b>

        <div class="sidebar_tab_content">

            <fmt:message key="design_implement_sdv" bundle="${restext}"/>

        </div>

    </td>

</tr>
<tr id="sidebar_Instructions_closed" style="display: none">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="../images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

        <b><fmt:message key="instructions" bundle="${restext}"/></b>

    </td>
</tr>
<jsp:include page="include/sideInfo.jsp"/>
<link rel="stylesheet" href="../includes/jmesa/jmesa.css" type="text/css">
<script type="text/JavaScript" language="JavaScript" src="../includes/jmesa/jquery-1.3.2.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="../includes/jmesa/jmesa.js"></script>
<script type="text/JavaScript" language="JavaScript" src="../includes/jmesa/jquery.jmesa.js"></script>
<%-- view all subjects starts here --%>
<script type="text/javascript">

    function onInvokeAction(id,action) {
        setExportToLimit(id, '');
        createHiddenInputFieldsForLimitAndSubmit(id);
    }
    function onInvokeExportAction(id) {
        var parameterString = createParameterStringForLimit(id);
        //location.href = '${pageContext.request.contextPath}/ViewCRF?module=manage&crfId=' + '${crf.id}&' + parameterString;
    }
</script></div>

<h1><span class="title_manage"><fmt:message key="sdv_sdv_for" bundle="${resword}"/> <c:out value="${study.name}"/></span></h1>

<div id="searchFilterSDV">
    <table border="0" cellpadding="0" cellspacing="0">
        <tr>
            <td valign="bottom" id="Tab1'">
                <div id="Tab1NotSelected"><div class="tab_BG"><div class="tab_L"><div class="tab_R">
                    <a class="tabtext" title="View By Event CRF" href='viewAllSubjectSDVtmp?studyId=${studyId}' onclick="javascript:HighlightTab(1);">View By Event CRF</a></div></div></div></div>
                <div id="Tab1Selected" style="display:none"><div class="tab_BG_h"><div class="tab_L_h"><div class="tab_R_h"><span class="tabtext">View By Event CRF</span></div></div></div></div></td>
              
            <td valign="bottom" id="Tab2'">
				<div id="Tab2Selected"><div class="tab_BG"><div class="tab_L"><div class="tab_R">
                    <a class="tabtext" title="View By Study Subject ID" href='viewSubjectAggregate?studyId=${studyId}' onclick="javascript:HighlightTab(2);">View By Study Subject ID</a></div></div></div></div>
                <div id="Tab2NotSelected" style="display:none"><div class="tab_BG_h"><div class="tab_L_h"><div class="tab_R_h"><span class="tabtext">View By Study Subject ID</span></div></div></div></div></td>

        </tr>
    </table>
    <script language="JavaScript">
        HighlightTab(1);
    </script>
    </div>
    <%--
<!-- These DIVs define shaded box borders -->
<div id="startBox" class="box_T"><div class="box_L"><div class="box_R"><div class="box_B">
<div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="textbox_center">
<form method="GET" action="viewAllSubjectSDVform" name="sdvFilterForm">
    --%><%--<input type="hidden" name="srch" value="y" />--%><%--
                    <input type="hidden" name="studyId" value="${studyId}"/>
                    <table border="0" cellpadding="0" cellspacing="0">
                        <tr valign="top"><td><b><fmt:message key="filter_events_by" bundle="${resword}"/>:</b></td></tr>
                        <tr valign="top">
                            <td><fmt:message key="study_subject" bundle="${resword}"/>:</td>
                            <td><div class="formfieldL_BG" style="margin-right:10px">
                                <input type="text" name="study_subject_id" class="formfieldS" />
                            </div>
                            </td>
                            <td><fmt:message key="event_crf" bundle="${resword}"/>:</td>
                            <td>
                                <div class="formfieldM_BG" style="margin-right:10px">
                                    --%><%--<c:set var="status1" value="${statusId}"/>--%><%--
                                    <select name="eventCRFName" class="formfieldM">
                                        <option value="0">--<fmt:message key="all" bundle="${resword}"/>--</option>
                                        --%><%-- probably need to use study event name here --%><%--
                                        <c:forEach var="eventCRFNam" items="${eventCRFNames}">
                                            <option value="${eventCRFNam}">${eventCRFNam}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </td>
                            <td><fmt:message key="study_event_definition" bundle="${resword}"/>:</td>
                            <td>
                                <div class="formfieldM_BG">
                                    --%><%--<c:set var="status1" value="${statusId}"/>--%><%--
                                    <select name="studyEventDefinition" class="formfieldM">
                                        <option value="0">--<fmt:message key="all" bundle="${resword}"/>--</option>
                                        --%><%-- probably need to use study event name here --%><%--
                                        <c:forEach var="studyEventDefinition" items="${studyEventDefinitions}">
                                        <option value="<c:out value="${studyEventDefinition.id}"/>"><c:out value="${studyEventDefinition.name}"/>
                                            </c:forEach>
                                    </select>
                                </div>
                            </td>
                        </tr>
                        <tr valign="top">
                            <td><fmt:message key="study_event_status" bundle="${resword}"/>:</td>
                            <td>
                                <div class="formfieldM_BG" style="margin-right:10px">
                                    --%><%--<c:set var="status1" value="${statusId}"/>--%><%--
                                    <select name="studyEventStatus" class="formfieldM">
                                        <option value="-1">--<fmt:message key="all" bundle="${resword}"/>--</option>
                                        <c:forEach var="studyEventStatus" items="${studyEventStatuses}">
                                        <option value="<c:out value="${studyEventStatus.id}"/>"><c:out value="${studyEventStatus.name}"/>
                                            </c:forEach>
                                    </select>
                                </div>
                            </td>
                            <td><fmt:message key="event_crf_status" bundle="${resword}"/>:</td>
                            <td>
                                <div class="formfieldM_BG" style="margin-right:10px">
                                    --%><%--<c:set var="status1" value="${statusId}"/>--%><%--
                                    <select name="eventCRFStatus" class="formfieldM">
                                        <option value="-1">--<fmt:message key="all" bundle="${resword}"/>--</option>
                                        <c:forEach var="eventCRFStatus" items="${eventCRFDStatuses}">
                                        <option value="<c:out value="${eventCRFStatus.id}"/>"><c:out value="${eventCRFStatus.name}"/>
                                            </c:forEach>
                                    </select>
                                </div>
                            </td>
                            <td>
                                <fmt:message key="event_crf_sdv_status" bundle="${resword}"/>:
                            </td>
                            <td>
                                <div class="formfieldM_BG">
                                    --%><%--<c:set var="status1" value="${statusId}"/>--%><%--
                                    <select name="eventcrfSDVStatus" class="formfieldM">
                                        <option value="N/A"><fmt:message key="N/A" bundle="${resword}"/></option>
                                        <option value="None"><fmt:message key="none" bundle="${resword}"/></option>
                                        <option value="Complete"><fmt:message key="complete" bundle="${resword}"/></option>
                                    </select>
                                </div>
                            </td>
                        </tr>
                        <tr valign="top">
                            <td>
                                <fmt:message key="event_crf_sdv_require" bundle="${resword}"/>:
                            </td>
                            <td>
                                <div class="formfieldM_BG" style="margin-right:10px">
                                    --%><%--<c:set var="status1" value="${statusId}"/>--%><%--
                                    <select name="sdvRequirement" class="formfieldM">
                                        <option value="0">--<fmt:message key="all" bundle="${resword}"/>--</option>
                                        <c:forEach var="sdvRequirement" items="${sdvRequirements}">
                                        <option value="<c:out value="${sdvRequirement.code}"/>"><c:out value="${sdvRequirement.description}"/>
                                            </c:forEach>
                                    </select>
                                </div>
                            </td>
                            <td>
                                <fmt:message key="event_crf_date_updated" bundle="${resword}"/>:
                            </td>
                            <td>
                                <div class="formfieldS_BG" style="width:157px">
                                    <input type="text" name="startUpdatedDate" value="${startUpdatedDate}" class="formfieldS" id="startUpdatedDateField"><A HREF="#"><img src="../images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" id="startDateTrigger"/>
                                    <script type="text/javascript">
                                        Calendar.setup({inputField  : "startUpdatedDateField", ifFormat    : "<fmt:message key="date_format_calender" bundle="${resformat}"/>", button      : "startDateTrigger" });
                                    </script>
                                </a>
                                </div>
                            </td>
                            <td><div><b><fmt:message key="To" bundle="${resword}"/></b></div></td>
                            <td><div class="formfieldS_BG" style="width:157px">
                                <input type="text" name="endDate" value="${endDate}" class="formfieldS" id="endDateField">
                                <A HREF="#">
                                    <img src="../images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" id="startDateTriggerB"/>
                                    <script type="text/javascript">
                                        Calendar.setup({inputField  : "endDateField", ifFormat    : "<fmt:message key="date_format_calender" bundle="${resformat}"/>", button      : "startDateTriggerB" });
                                    </script>
                                </a>
                            </div>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="6" align="right"><input type="submit" name="submit" value="<fmt:message key="apply_filter" bundle="${resword}"/>" class="button_medium"></td>
                        </tr>
                    </table>
                </form>
            </div>
        </div></div></div></div></div></div></div></div>--%>

<script type="text/javascript">
    function prompt(formObj,crfId){
        var bool = confirm(
                "<fmt:message key="uncheck_sdv" bundle="${resmessages}"/>");
        if(bool){
            formObj.action='${pageContext.request.contextPath}/pages/handleSDVRemove';
            formObj.crfId.value=crfId;
            formObj.submit();
        }
    }
</script>
<div id="subjectSDV">
    <form name='sdvForm' action="${pageContext.request.contextPath}/pages/viewAllSubjectSDVtmp">
        <input type="hidden" name="studyId" value="${param.studyId}">
        <input type="hidden" name=imagePathPrefix value="../">
        <%--This value will be set by an onclick handler associated with an SDV button --%>
        <input type="hidden" name="crfId" value="0">
        <%-- the destination JSP page after removal or adding SDV for an eventCRF --%>
        <input type="hidden" name="redirection" value="viewAllSubjectSDVtmp">
        <%--<input type="hidden" name="decorator" value="mydecorator">--%>
        ${sdvTableAttribute}
        <br />
        <input type="submit" name="sdvAllFormSubmit" class="button_medium" value="<fmt:message key="sdv_all_checked" bundle="${resword}"/>" onclick="this.form.method='POST';this.form.action='${pageContext.request.contextPath}/pages/handleSDVPost';this.form.submit();"/>
        <!--  <input type="submit" name="sdvAllFormCancel" class="button_medium" value="Cancel" onclick="this.form.action='${pageContext.request.contextPath}/pages/viewAllSubjectSDVtmp';this.form.submit();"/> -->
    </form>
   <%-- <script type="text/javascript">hideCols('sdv',[2,3,6,7,11,12,13])</script> --%>

</div>
<%-- view all subjects ends here --%>



<jsp:include page="include/footer.jsp"/>