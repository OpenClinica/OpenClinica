<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>

<c:choose>
<c:when test="${userBean.sysAdmin && module=='admin'}">
 <c:import url="../include/admin-header.jsp"/>
</c:when>
<c:otherwise>
 <c:import url="../include/managestudy-header.jsp"/>
</c:otherwise>
</c:choose>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<link type="text/css" href="includes/jmesa/jmesa.css"  rel="stylesheet">    

<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.jmesa.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jmesa.js"></script>
<%-- <script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jmesa-original.js"></script> --%>
<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery.blockUI.js"></script>
<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery-ui-1.8.2.custom.min.js"></script>

<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery-migrate-1.1.1.js"></script> 


<script type="text/javascript">
    function onInvokeAction(id,action) {
        if(id.indexOf('ruleAssignments') == -1)  {
        setExportToLimit(id, '');
        }
        createHiddenInputFieldsForLimitAndSubmit(id);
    }
    function onInvokeExportAction(id) {
        var parameterString = createParameterStringForLimit(id);
        location.href = '${pageContext.request.contextPath}/ViewRuleAssignment?'+ parameterString;
    }
</script>


<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: all">
        <td class="sidebar_tab">
        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray"></span></a>
        <fmt:message key="instructions" bundle="${restext}"/>
        <div class="sidebar_tab_content">
        <fmt:message key="manage_execute_rule_assignments" bundle="${restext}"/>
        </div>
        </td>
    </tr>
    <tr id="sidebar_Instructions_closed" style="display: none">
        <td class="sidebar_tab">
        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray"></span></a>
        <fmt:message key="instructions" bundle="${restext}"/>
        </td>
  </tr>
<jsp:include page="../include/viewRuleAssignmentSideInfo.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='userRole' class='org.akaza.openclinica.bean.login.StudyUserRoleBean' />
<jsp:useBean scope='session' id='study' class='org.akaza.openclinica.bean.managestudy.StudyBean'/>
<jsp:useBean scope='request' id='table' class='org.akaza.openclinica.web.domain.EntityBeanTable'/>
<c:choose>
<c:when test="${userBean.sysAdmin && module=='admin'}">
 <h1><span class="title_manage"><fmt:message key="rule_manage_rule_assignment" bundle="${resworkflow}"/> <c:out value="${study.name}" /> <a href="javascript:openDocWindow('https://docs.openclinica.com/3.1/rules')"><span class="icon icon-question-circle gray"></span></a>
</c:when>
<c:otherwise>
 <h1><span class="title_manage"><fmt:message key="rule_manage_rule_assignment" bundle="${resworkflow}"/> <c:out value="${study.name}" /> <a href="javascript:openDocWindow('https://docs.openclinica.com/3.1/rules')"><span class="icon icon-question-circle gray"></span></a>
</c:otherwise>
</c:choose>
</span></h1><br/>



<div id="ruleAssignmentsDiv">
    <form  action="${pageContext.request.contextPath}/ViewRuleAssignment">
        <input type="hidden" name="module" value="admin">
        ${ruleAssignmentsHtml}
    </form>
</div>

<script>$j("img[title*='PDF']").attr('title', '<fmt:message key="view_rules_download_xml" bundle="${resword}"/>' );</script>


<br>
<jsp:include page="../include/footer.jsp"/>