<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.page_messages" var="resmessages"/>
<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='study' class='org.akaza.openclinica.bean.managestudy.StudyBean'/>
<jsp:useBean scope='session' id='userRole' class='org.akaza.openclinica.bean.login.StudyUserRoleBean'/>

<jsp:include page="include/home-header.jsp"/>
<!-- move the alert message to the sidebar-->
<jsp:include page="include/sideAlert.jsp"/>


<link rel="stylesheet" href="includes/jmesa/jmesa.css" type="text/css">
<link rel="stylesheet" href="includes/css/oc2017_styles.css" type="text/css">
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.jmesa.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jmesa.js"></script>
<%-- <script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jmesa-original.js"></script> --%>
<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery.blockUI.js"></script>
<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery-migrate-1.1.1.js"></script>
<style type="text/css">

    .graph {
        position: relative; /* IE is dumb */
        width: 100px;
        border: 1px solid #3876C1;
        padding: 2px;
    }

    .graph .bar {
        display: block;
        position: relative;
        background: #E8D28C;
        text-align: center;
        color: #333;
        height: 1em;
        line-height: 1em;
    }

    .graph .bar span {
        position: absolute;
        left: 1em;
    }
</style>

<!-- then instructions-->
<div id="box" class="dialog">
    <span id="mbm">
        <br>
        <c:if test="${(!study.status.pending)}">
            <fmt:message key="study_frozen_locked_note" bundle="${restext}"/>
        </c:if>
        
        <c:if test="${(study.status.pending)}">
            <fmt:message key="study_design_note" bundle="${restext}"/>
        </c:if>   
    </span><br>
    <div style="text-align:center; width:100%;">
        <button onclick="hm('box');">OK</button>
    </div>
</div>

<tr id="sidebar_Instructions_open" style="display: all">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray"></span></a>

        <fmt:message key="instructions" bundle="${resword}"/>

        <div class="sidebar_tab_content">
            <fmt:message key="may_change_request_access" bundle="${restext}"/>
        </div>

    </td>

</tr>
<tr id="sidebar_Instructions_closed" style="display: none">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray"></span></a>

        <fmt:message key="instructions" bundle="${resword}"/>

    </td>
</tr>

<jsp:include page="include/sideInfo.jsp"/>


<h1>
    <span class="title_manage" style="line-height:35px;">
        <fmt:message key="welcome_to" bundle="${restext}"/>
            <c:choose>
                <c:when test='${study.parentStudyId > 0}'>
                    <c:out value='${study.parentStudyName}'/>
                </c:when>
                <c:otherwise>
                    <c:out value='${study.name}'/>
                </c:otherwise>
            </c:choose>
        <a href="javascript:openDocWindow('https://docs.openclinica.com/3.1/openclinica-user-guide/overview-openclinica')">
            <span class=""></span></a>
    </span>
</h1>

<c:set var="roleName" value=""/>
<c:if test="${userRole != null && !userRole.invalid}">
    <c:set var="roleName" value="${userRole.role.name}"/>


    <%--
    <c:set var="linkStudy">
    <c:choose>
       <c:when test="${study.parentStudyId>0}">
         <a href="ViewSite?id=<c:out value="${study.id}"/>">
       </c:when>
       <c:otherwise>
         <a href="ViewStudy?id=<c:out value="${study.id}"/>&viewFull=yes">
       </c:otherwise>
    </c:choose>
    <c:out value="${study.name}"/></a></span>
    </c:set>
     --%>
    <c:set var="studyidentifier">
        <span class="alert"><c:out value="${study.identifier}"/></span>
    </c:set>

</c:if>
<span class="table_title_Admin" style="line-height:15px;">
<a style="text-decoration: none;" href="ViewNotes?module=submit&listNotes_f_discrepancyNoteBean.user=<c:out value='${userBean.name}' />"><p style="padding-left:10px;"><fmt:message key="notes_assigned_to_me" bundle="${restext}"/>
                <span name="flag_start" class="fa fa-bubble-white" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>"> 0</p></a><br /><br />
</span>

<c:if test="${userRole.investigator || userRole.researchAssistant || userRole.researchAssistant2}">
    <script type="text/javascript">
        function Redirect() {
           window.location="ListStudySubjects";
        }
        setTimeout('Redirect()', 0)
    </script>
</c:if>

<c:if test="${userRole.coordinator || userRole.director}">


    <script type="text/javascript">
        function onInvokeAction(id, action) {
            if (id.indexOf('studySiteStatistics') == -1) {
                setExportToLimit(id, '');
            }
            if (id.indexOf('subjectEventStatusStatistics') == -1) {
                setExportToLimit(id, '');
            }
            if (id.indexOf('studySubjectStatusStatistics') == -1) {
                setExportToLimit(id, '');
            }
            createHiddenInputFieldsForLimitAndSubmit(id);
        }

    </script>

    <table>
        <tr>
            <td valign="top">
                <form action="${pageContext.request.contextPath}/MainMenu">
                        ${studySiteStatistics}
                </form>
            </td>
            <td valign="top">
                <form action="${pageContext.request.contextPath}/MainMenu">
                        ${studyStatistics}
                </form>
            </td>
        </tr>
    </table>


    <table>
        <tr>
            <td valign="top">
                <form action="${pageContext.request.contextPath}/MainMenu">
                        ${subjectEventStatusStatistics}
                </form>
            </td>

            <td valign="top">
                <form action="${pageContext.request.contextPath}/MainMenu">
                        ${studySubjectStatusStatistics}
                </form>
            </td>
        </tr>
    </table>

</c:if>

<c:if test="${userRole.monitor}">
    <script type="text/javascript">
        function Redirect() {
           window.location="pages/viewAllSubjectSDVtmp?sdv_restore=${restore}&studyId=${study.id}";
        }
        setTimeout('Redirect()', 0)
     </script>  
</c:if>
