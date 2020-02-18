<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="org.akaza.openclinica.i18n.util.ResourceBundleProvider" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>

<script language="JavaScript">
        function confirmCancel(pageName){
            var confirm1 = confirm('<fmt:message key="sure_to_cancel" bundle="${resword}"/>');
            if(confirm1){
                window.location = pageName;
            }
        }
        function confirmExit(pageName){
            var confirm1 = confirm('<fmt:message key="sure_to_exit" bundle="${resword}"/>');
            if(confirm1){
                window.location = pageName;
            }
        }
        function goBack(){
            var confirm1 = confirm('<fmt:message key="sure_to_cancel" bundle="${resword}"/>');
            if(confirm1){
                return history.go(-1);
            }
        }
        function lockedCRFAlert(userName){
            alert('<fmt:message key="CRF_unavailable" bundle="${resword}"/>'+'\n'
                    +'          '+userName+' '+'<fmt:message key="Currently_entering_data" bundle="${resword}"/>'+'\n'
                    +'<fmt:message key="Leave_the_CRF" bundle="${resword}"/>');
            return false;
        }
        function confirmCancelAction( pageName, contextPath){
            var confirm1 = confirm('<fmt:message key="sure_to_cancel" bundle="${resword}"/>');
            if(confirm1){
            	 var tform = document.forms["fr_cancel_button"];
            	tform.action=contextPath+"/"+pageName;
            	tform.submit();

            }
        }
        function confirmExitAction( pageName, contextPath){
            var confirm1 = confirm('<fmt:message key="sure_to_exit" bundle="${resword}"/>');
            if(confirm1){
            	 var tform = document.forms["fr_cancel_button"];
            	tform.action=contextPath+"/"+pageName;
            	tform.submit();

            }
        }
</script>


<jsp:useBean scope='session' id='tableFacadeRestore' class='java.lang.String' />
<c:set var="restore" value="true"/>
<c:if test="${tableFacadeRestore=='false'}"><c:set var="restore" value="false"/></c:if>
<c:set var="profilePage" value="${param.profilePage}"/>
<!--  If Controller Spring based append ../ to urls -->
<c:set var="urlPrefix" value=""/>
<c:set var="requestFromSpringController" value="${param.isSpringController}" />
<c:if test="${requestFromSpringController == 'true' }">
      <c:set var="urlPrefix" value="${pageContext.request.contextPath}/"/>
</c:if>

<!-- Main Navigation -->
     <div class="oc_nav">
        <div id="StudyInfo">
            <c:choose>
                <c:when test='${study.parentStudyId > 0}'>
                    <b><a href="${urlPrefix}ViewStudy?id=${study.parentStudyId}&viewFull=yes" title="<c:out value='${study.parentStudyName}'/>" alt="<c:out value='${study.parentStudyName}'/>" ><c:out value="${study.abbreviatedParentStudyName}" /></a>
                    :&nbsp;<a href="${urlPrefix}ViewSite?id=${study.id}" title="<c:out value='${study.name}'/>" alt="<c:out value='${study.name}'/>"><c:out value="${study.abbreviatedName}" /></a></b>
                </c:when>
                <c:otherwise>
                    <b><a href="${urlPrefix}ViewStudy?id=${study.id}&viewFull=yes" title="<c:out value='${study.name}'/>" alt="<c:out value='${study.name}'/>"><c:out value="${study.abbreviatedName}" /></a></b>
                </c:otherwise>
            </c:choose>
            (<c:out value="${study.abbreviatedIdentifier}" />)&nbsp;&nbsp;|&nbsp;&nbsp;
            <a href="${urlPrefix}ChangeStudy"><fmt:message key="change_study_site" bundle="${resworkflow}"/></a>
        </div>
        <div id="UserInfo">
            <a href="${urlPrefix}UpdateProfile"><b><c:out value="${userBean.name}" /></b> (<c:out value="${userRole.role.description}" />)&nbsp;
				<c:out value="<%=ResourceBundleProvider.getLocale().toString()%>"/>
            </a>&nbsp;|&nbsp;
            <a href="${urlPrefix}j_spring_security_logout"><fmt:message key="log_out" bundle="${resword}"/></a>
        </div>
        <br/><br style="line-height: 4px;"/>
        <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

            <div class="navbox_center">
                <!-- Top Navigation Row -->
                <table border="0" cellpadding="0" cellspacing="0" width="100%">
                    <tr>
                        <td>
                            <div id="bt_Home" class="nav_bt"><div><div><div>
                            <table border="0" cellpadding="0" cellspacing="0" width="100%">
                                <tr>
                                    <td>
                                        <ul>
                                        <c:if test="${userRole.coordinator || userRole.director}">
                                            <li><a href="${urlPrefix}MainMenu"><fmt:message key="nav_home" bundle="${resword}"/></a>&nbsp;&nbsp;|&nbsp;&nbsp;</li>
                                            <li><a href="${urlPrefix}ListStudySubjects"><fmt:message key="nav_subject_matrix" bundle="${resword}"/></a>&nbsp;&nbsp;|&nbsp;&nbsp;</li>
                                            <li><a href="${urlPrefix}ViewNotes?module=submit"><fmt:message key="nav_notes_and_discrepancies" bundle="${resword}"/></a>&nbsp;&nbsp;|&nbsp;&nbsp;</li>
                                            <li><a href="${urlPrefix}StudyAuditLog"><fmt:message key="nav_study_audit_log" bundle="${resword}"/></a>&nbsp;&nbsp;|&nbsp;&nbsp;</li>
                                        </c:if>
                                        <c:if test="${userRole.researchAssistant ||userRole.researchAssistant2}">
                                            <li><a href="${urlPrefix}MainMenu"><fmt:message key="nav_home" bundle="${resword}"/></a>&nbsp;&nbsp;|&nbsp;&nbsp;</li>
                                            <li><a href="${urlPrefix}ListStudySubjects"><fmt:message key="nav_subject_matrix" bundle="${resword}"/></a>&nbsp;&nbsp;|&nbsp;&nbsp;</li>
                                            <c:if test="${study.status.available}">
                                                <li><a href="${urlPrefix}AddNewSubject"><fmt:message key="nav_add_subject" bundle="${resword}"/></a>&nbsp;&nbsp;|&nbsp;&nbsp;</li>
                                            </c:if>
                                            <li><a href="${urlPrefix}ViewNotes?module=submit"><fmt:message key="nav_notes_and_discrepancies" bundle="${resword}"/></a>&nbsp;&nbsp;|&nbsp;&nbsp;</li>
                                        </c:if>
                                        <c:if test="${userRole.investigator}">
                                            <li><a href="${urlPrefix}MainMenu"><fmt:message key="nav_home" bundle="${resword}"/></a>&nbsp;&nbsp;|&nbsp;&nbsp;</li>
                                            <li><a href="${urlPrefix}ListStudySubjects"><fmt:message key="nav_subject_matrix" bundle="${resword}"/></a>&nbsp;&nbsp;|&nbsp;&nbsp;</li>
                                            <c:if test="${study.status.available}">
                                                <li><a href="${urlPrefix}AddNewSubject"><fmt:message key="nav_add_subject" bundle="${resword}"/></a>&nbsp;&nbsp;|&nbsp;&nbsp;</li>
                                            </c:if>
                                            <li><a href="${urlPrefix}ViewNotes?module=submit"><fmt:message key="nav_notes_and_discrepancies" bundle="${resword}"/></a>&nbsp;&nbsp;|&nbsp;&nbsp;</li>
                                        </c:if>
                                        <c:if test="${userRole.monitor }">
                                            <li><a href="${urlPrefix}MainMenu"><fmt:message key="nav_home" bundle="${resword}"/></a>&nbsp;&nbsp;|&nbsp;&nbsp;</li>
                                            <li><a href="${urlPrefix}ListStudySubjects"><fmt:message key="nav_subject_matrix" bundle="${resword}"/></a>&nbsp;&nbsp;|&nbsp;&nbsp;</li>
                                            <li><a href="${urlPrefix}pages/viewAllSubjectSDVtmp?sdv_restore=${restore}&studyId=${study.id}"><fmt:message key="nav_sdv" bundle="${resword}"/></a>&nbsp;&nbsp;|&nbsp;&nbsp;</li>
                                            <li><a href="${urlPrefix}ViewNotes?module=submit"><fmt:message key="nav_notes_and_discrepancies" bundle="${resword}"/></a>&nbsp;&nbsp;|&nbsp;&nbsp;</li>
                                        </c:if>
                                        <li id="nav_Tasks" style="position: relative; z-index: 3;">
                                            <a href="#" onmouseover="setNav('nav_Tasks');" id="nav_Tasks_link"><fmt:message key="nav_tasks" bundle="${resword}"/>
                                                <img src="${urlPrefix}images/bt_Tasks_pulldown.gif" alt="Tasks" border="0"/></a>
                                        </li>
                                        </ul>
                                    </td>
                                    <td align="right" style="font-weight: normal;">

                                        <form METHOD="GET" action="${urlPrefix}ListStudySubjects" onSubmit=" if (document.forms[0]['findSubjects_f_studySubject.label'].value == '<fmt:message key="study_subject_ID" bundle="${resword}"/>') { document.forms[0]['findSubjects_f_studySubject.label'].value=''}">
                                            <c:if test="${not empty sessionScope.supportURL}">
                                            	<a href="javascript:openDocWindow('<c:out value="${sessionScope.supportURL}" />')"><fmt:message key="openclinica_feedback" bundle="${resword}"/></a>&nbsp;&nbsp;
                                            </c:if>
                                            <input type="text" name="findSubjects_f_studySubject.label" onblur="if (this.value == '') this.value = '<fmt:message key="study_subject_ID" bundle="${resword}"/>'" onfocus="if (this.value == '<fmt:message key="study_subject_ID" bundle="${resword}"/>') this.value = ''" value="<fmt:message key="study_subject_ID" bundle="${resword}"/>" class="navSearch"/>
                                            <input type="hidden" name="navBar" value="yes"/>
                                            <input type="submit" value="<fmt:message key="go" bundle="${resword}"/>"  class="navSearchButton"/>
                                        </form>

                                    </td>
                                </tr>
                            </table>
                            </div></div></div></div>
                        </td>
                    </tr>
                                    </table>
            </div>
            <!-- End shaded box border DIVs -->
        </div></div></div></div></div></div></div></div></div>


            </td>
        </tr>
    </table>
    <!-- NAVIGATION DROP-DOWN -->


<div id="nav_hide" style="position: absolute; left: 0px; top: 0px; visibility: hidden; z-index: 2; width: 100%; height: 400px;">

<a href="#" onmouseover="hideSubnavs();"><img src="${urlPrefix}images/spacer.gif" alt="" width="1000" height="400" border="0"/></a>
</div>


    </div>
    <img src="${urlPrefix}images/spacer.gif" width="596" height="1"><br>
<!-- End Main Navigation -->
<div id="subnav_Tasks" class="dropdown">
    <div class="dropdown_BG">
        <c:if test="${userRole.monitor }">
        <div class="taskGroup"><fmt:message key="nav_monitor_and_manage_data" bundle="${resword}"/></div>
        <div class="taskLeftColumn">
            <div class="taskLink"><a href="${urlPrefix}ListStudySubjects"><fmt:message key="nav_subject_matrix" bundle="${resword}"/></a></div>
            <div class="taskLink"><a href="${urlPrefix}ViewStudyEvents"><fmt:message key="nav_view_events" bundle="${resword}"/></a></div>
            <div class="taskLink"><a href="${urlPrefix}pages/viewAllSubjectSDVtmp?sdv_restore=${restore}&studyId=${study.id}"><fmt:message key="nav_source_data_verification" bundle="${resword}"/></a></div>
        </div>
        <div class="taskRightColumn">
            <div class="taskLink"><a href="${urlPrefix}ViewNotes?module=submit"><fmt:message key="nav_notes_and_discrepancies" bundle="${resword}"/></a></div>
            <div class="taskLink"><a href="${urlPrefix}StudyAuditLog"><fmt:message key="nav_study_audit_log" bundle="${resword}"/></a></div>
        </div>
        <br clear="all">
        <div class="taskGroup"><fmt:message key="nav_extract_data" bundle="${resword}"/></div>
        <div class="taskLeftColumn">
            <div class="taskLink"><a href="${urlPrefix}ViewDatasets"><fmt:message key="nav_view_datasets" bundle="${resword}"/></a></div>
        </div>
        <div class="taskRightColumn">
            <div class="taskLink"><a href="${urlPrefix}CreateDataset"><fmt:message key="nav_create_dataset" bundle="${resword}"/></a></div>
        </div>
        <br clear="all">
        </c:if>
        <c:if test="${userRole.researchAssistant ||userRole.researchAssistant2  }">
        <div class="taskGroup"><fmt:message key="nav_submit_data" bundle="${resword}"/></div>
        <div class="taskLeftColumn">
            <div class="taskLink"><a href="${urlPrefix}ListStudySubjects"><fmt:message key="nav_subject_matrix" bundle="${resword}"/></a></div>
            <c:if test="${study.status.available}">
                <div class="taskLink"><a href="${urlPrefix}AddNewSubject"><fmt:message key="nav_add_subject" bundle="${resword}"/></a></div>
            </c:if>
            <div class="taskLink"><a href="${urlPrefix}ViewNotes?module=submit"><fmt:message key="nav_notes_and_discrepancies" bundle="${resword}"/></a></div>
        </div>
        <div class="taskRightColumn">
            <c:if test="${!study.status.frozen && !study.status.locked}">
                <div class="taskLink"><a href="${urlPrefix}CreateNewStudyEvent"><fmt:message key="nav_schedule_event" bundle="${resword}"/></a></div>
            </c:if>
            <div class="taskLink"><a href="${urlPrefix}ViewStudyEvents"><fmt:message key="nav_view_events" bundle="${resword}"/></a></div>
            <div class="taskLink"><a href="${urlPrefix}ImportCRFData"><fmt:message key="nav_import_data" bundle="${resword}"/></a></div>
        </div>
        <br clear="all">
        </c:if>
        <c:if test="${userRole.investigator}">
        <div class="taskGroup"><fmt:message key="nav_submit_data" bundle="${resword}"/></div>
        <div class="taskLeftColumn">
            <div class="taskLink"><a href="${urlPrefix}ListStudySubjects"><fmt:message key="nav_subject_matrix" bundle="${resword}"/></a></div>
            <c:if test="${study.status.available}">
                <div class="taskLink"><a href="${urlPrefix}AddNewSubject"><fmt:message key="nav_add_subject" bundle="${resword}"/></a></div>
            </c:if>
            <div class="taskLink"><a href="${urlPrefix}ViewNotes?module=submit"><fmt:message key="nav_notes_and_discrepancies" bundle="${resword}"/></a></div>
        </div>
        <div class="taskRightColumn">
            <c:if test="${!study.status.frozen && !study.status.locked}">
                <div class="taskLink"><a href="${urlPrefix}CreateNewStudyEvent"><fmt:message key="nav_schedule_event" bundle="${resword}"/></a></div>
            </c:if>
            <div class="taskLink"><a href="${urlPrefix}ViewStudyEvents"><fmt:message key="nav_view_events" bundle="${resword}"/></a></div>
            <div class="taskLink"><a href="${urlPrefix}ImportCRFData"><fmt:message key="nav_import_data" bundle="${resword}"/></a></div>
        </div>
        <br clear="all">
        <div class="taskGroup"><fmt:message key="nav_extract_data" bundle="${resword}"/></div>
        <div class="taskLeftColumn">
            <div class="taskLink"><a href="${urlPrefix}ViewDatasets"><fmt:message key="nav_view_datasets" bundle="${resword}"/></a></div>
        </div>
        <div class="taskRightColumn">
            <div class="taskLink"><a href="${urlPrefix}CreateDataset"><fmt:message key="nav_create_dataset" bundle="${resword}"/></a></div>
        </div>
        <br clear="all">
        </c:if>
        <c:if test="${userRole.coordinator || userRole.director}">
        <div class="taskGroup"><fmt:message key="nav_submit_data" bundle="${resword}"/></div>
        <div class="taskLeftColumn">
            <div class="taskLink"><a href="${urlPrefix}ListStudySubjects"><fmt:message key="nav_subject_matrix" bundle="${resword}"/></a></div>
            <c:if test="${study.status.available}">
                <div class="taskLink"><a href="${urlPrefix}AddNewSubject"><fmt:message key="nav_add_subject" bundle="${resword}"/></a></div>
            </c:if>
            <div class="taskLink"><a href="${urlPrefix}ViewNotes?module=submit"><fmt:message key="nav_notes_and_discrepancies" bundle="${resword}"/></a></div>
        </div>
        <div class="taskRightColumn">
            <c:if test="${!study.status.frozen && !study.status.locked}">
                <div class="taskLink"><a href="${urlPrefix}CreateNewStudyEvent"><fmt:message key="nav_schedule_event" bundle="${resword}"/></a></div>
            </c:if>
            <div class="taskLink"><a href="${urlPrefix}ViewStudyEvents"><fmt:message key="nav_view_events" bundle="${resword}"/></a></div>
            <div class="taskLink"><a href="${urlPrefix}ImportCRFData"><fmt:message key="nav_import_data" bundle="${resword}"/></a></div>
        </div>
        <br clear="all">
        <div class="taskGroup"><fmt:message key="nav_monitor_and_manage_data" bundle="${resword}"/></div>
        <div class="taskLeftColumn">
            <div class="taskLink"><a href="${urlPrefix}pages/viewAllSubjectSDVtmp?sdv_restore=${restore}&studyId=${study.id}"><fmt:message key="nav_source_data_verification" bundle="${resword}"/></a></div>
            <div class="taskLink"><a href="${urlPrefix}StudyAuditLog"><fmt:message key="nav_study_audit_log" bundle="${resword}"/></a></div>
            <c:choose>
                <c:when test="${study.parentStudyId > 0 && (userRole.coordinator || userRole.director) }">
                </c:when>
                <c:otherwise>
                    <div class="taskLink"><a href="${urlPrefix}ViewRuleAssignment?read=true"><fmt:message key="nav_rules" bundle="${resword}"/></a></div>
                </c:otherwise>
            </c:choose>
        </div>
        <div class="taskRightColumn">
        <c:choose>
            <c:when test="${study.parentStudyId > 0 && (userRole.coordinator || userRole.director) }">
            </c:when>
            <c:otherwise>
                <div class="taskLink"><a href="${urlPrefix}ListSubjectGroupClass?read=true"><fmt:message key="nav_groups" bundle="${resword}"/></a></div>
                <div class="taskLink"><a href="${urlPrefix}ListCRF?module=manage"><fmt:message key="nav_crfs" bundle="${resword}"/></a></div>
            </c:otherwise>
        </c:choose>
        </div>
        <br clear="all">
        <div class="taskGroup"><fmt:message key="nav_extract_data" bundle="${resword}"/></div>
        <div class="taskLeftColumn">
            <div class="taskLink"><a href="${urlPrefix}ViewDatasets"><fmt:message key="nav_view_datasets" bundle="${resword}"/></a></div>
        </div>
        <div class="taskRightColumn">
            <div class="taskLink"><a href="${urlPrefix}CreateDataset"><fmt:message key="nav_create_dataset" bundle="${resword}"/></a></div>
        </div>
        <br clear="all">
        <div class="taskGroup"><fmt:message key="nav_study_setup" bundle="${resword}"/></div>
        <div class="taskLeftColumn">
            <div class="taskLink"><a href="${urlPrefix}ViewStudy?id=${study.id}&viewFull=yes"><fmt:message key="nav_view_study" bundle="${resword}"/></a></div>
            <c:choose>
                <c:when test="${study.parentStudyId > 0 && (userRole.coordinator || userRole.director) }">
                </c:when>
                <c:otherwise>
                    <div class="taskLink"><a href="${urlPrefix}pages/studymodule"><fmt:message key="nav_build_study" bundle="${resword}"/></a></div>
                    <!-- <div class="taskLink"><a href="${urlPrefix}ListEventDefinition?read=true"><fmt:message key="nav_event_definitions" bundle="${resword}"/></a></div>  -->
                </c:otherwise>
            </c:choose>
        </div>
        <div class="taskRightColumn">
            <div class="taskLink"><a href="${urlPrefix}ListStudyUser"><fmt:message key="nav_users" bundle="${resword}"/></a></div>
            <%--
            <c:choose>
                <c:when test="${study.parentStudyId > 0 && (userRole.coordinator || userRole.director) }">
                </c:when>
                <c:otherwise>
                    <div class="taskLink"><a href="${urlPrefix}ListSite?read=true"><fmt:message key="nav_sites" bundle="${resword}"/></a></div>
                </c:otherwise>
            </c:choose>
             --%>
        </div>
        <br clear="all">
        </c:if>
        <c:if test="${userBean.sysAdmin || userBean.techAdmin}">
        <div class="taskGroup"><fmt:message key="nav_administration" bundle="${resword}"/></div>
        <div class="taskLeftColumn">
            <div class="taskLink"><a href="${urlPrefix}ListStudy"><fmt:message key="nav_studies" bundle="${resword}"/></a></div>
            <div class="taskLink"><a href="${urlPrefix}ListUserAccounts"><fmt:message key="nav_users" bundle="${resword}"/></a></div>
            <div class="taskLink"><a href="${urlPrefix}ListCRF?module=admin"><fmt:message key="nav_crfs" bundle="${resword}"/></a></div>
        </div>
        <div class="taskRightColumn">
            <div class="taskLink"><a href="${urlPrefix}ViewAllJobs"><fmt:message key="nav_jobs" bundle="${resword}"/></a></div>
            <div class="taskLink"><a href="${urlPrefix}ListSubject"><fmt:message key="nav_subjects" bundle="${resword}"/></a></div>
        </div>
        <br clear="all">
        </c:if>
        <div class="taskGroup"><fmt:message key="nav_other" bundle="${resword}"/></div>
        <div class="taskLeftColumn">
            <div class="taskLink"><a href="${urlPrefix}UpdateProfile"><fmt:message key="nav_update_profile" bundle="${resword}"/></a></div>
        </div>
        <div class="taskRightColumn">
            <div class="taskLink"><a href="${urlPrefix}j_spring_security_logout"><fmt:message key="nav_log_out" bundle="${resword}"/></a></div>
        </div>
        <br clear="all">
        <div></div>
    </div>
</div>