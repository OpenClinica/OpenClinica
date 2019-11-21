<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>


<jsp:include page="../include/submit-header.jsp"/>
<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<style>
    .icon > span {
        font-family: 'Open Sans', arial, helvetica, sans-serif;
    }
</style>

<script type="text/javascript">
    function onInvokeAction(id,action) {
        if(id.indexOf('findSubjects') == -1)  {
        setExportToLimit(id, '');
        }
        createHiddenInputFieldsForLimitAndSubmit(id);
    }
    function onInvokeExportAction(id) {
        var parameterString = createParameterStringForLimit(id);
        location.href = '${pageContext.request.contextPath}/ListStudySubjects?'+ parameterString;
    }
    function URLSearchParams(name){
        var results = new RegExp('[\?&]' + name + '=([^&#]*)').exec(window.location.href);
        if (results == null){
           return null;
        }
        else {
           return decodeURI(results[1]) || 0;
        }
    }

    jQuery(document).ready(function() {
        jQuery('#addSubject').click(function() {
            jQuery('#sidebar_Alerts_open .sidebar_tab_content').html('<i></i>');
            jQuery('#spanAlert-label').hide();
            jQuery.blockUI({ message: jQuery('#addSubjectForm'), css:{left: "300px", top:"10px" } });
        });

        jQuery('input#cancel').click(function() {
            jQuery.unblockUI();
            return false;
        });

        if (URLSearchParams('addNewSubject')) {
            jQuery('#addSubject').click();
        }

        sessionStorage.setItem("pageContextPath", "<c:out value='${pageContext.request.contextPath}' />");
        sessionStorage.setItem("studyOid", "<c:out value='${study.oid}' />");
        sessionStorage.setItem("studyName", "<c:out value='${study.name}' />");
        sessionStorage.setItem("studyParentId", "<c:out value='${study.parentStudyId}' />");
        sessionStorage.setItem("siteSubStringMark", "<c:out value='${siteSubStringMark}' />");
    });

    window.onload = function() {
        document.getElementById("btn").focus();
            <c:if test="${showOverlay}">
                jQuery.blockUI({ message: jQuery('#addSubjectForm'), css:{left: "300px", top:"10px" } });
            </c:if>
    };


</script>

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray"></span></a>

        <fmt:message key="instructions" bundle="${resword}"/>

        <div class="sidebar_tab_content">

        </div>

    </td>

</tr>
<tr id="sidebar_Instructions_closed" style="display: all">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray"></span></a>

        <fmt:message key="instructions" bundle="${resword}"/>

    </td>
</tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope='session' id='userBean' class='core.org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='crf' class='core.org.akaza.openclinica.bean.admin.CRFBean'/>

<c:if test="${participantIDVerification == 'true'}">
    <c:import url="../submit/verifyID.jsp"></c:import>
</c:if>

<h1>
    <span class="title_manage">
        <fmt:message key="view_subjects_in" bundle="${restext}"/> <c:out value="${study.name}"/>
    </span>
</h1>
<br/>

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
        <button id="btn" onclick="hm('box');">OK</button>
    </div>
</div>

<div id="findSubjectsDiv">
    <form  action="${pageContext.request.contextPath}/ListStudySubjects">
        <input type="hidden" name="module" value="admin">
        ${findSubjectsHtml}
    </form>
</div>

<c:if test="${userRole.monitor || userRole.coordinator || userRole.director || userRole.investigator || userRole.researchAssistant || userRole.researchAssistant2}">
    <div id="addSubjectForm" style="display:none;">
          <c:import url="../submit/addNewSubjectExpressNew.jsp">
          </c:import>
    </div>
</c:if>

<br>
<jsp:include page="../include/footer.jsp"/>
