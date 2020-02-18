<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>

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

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: all">
    <td class="sidebar_tab">
        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
            <img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10">
        </a>
        <b><fmt:message key="instructions" bundle="${restext}"/></b>
        <div class="sidebar_tab_content">
            <fmt:message key="import_rule_instructions" bundle="${restext}"/>
        </div>
    </td>
</tr>

<tr id="sidebar_Instructions_closed" style="display: none">
    <td class="sidebar_tab">
        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
            <img src="images/sidebar_expand.gif" border="0" align="right" hspace="10">
        </a>
        <b><fmt:message key="instructions" bundle="${restext}"/></b>
    </td>
</tr>



<jsp:include page="../include/sideInfo.jsp"/>


<jsp:useBean scope='session' id='version' class='org.akaza.openclinica.bean.submit.CRFVersionBean'/>
<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='importedData' class='org.akaza.openclinica.domain.rule.RulesPostImportContainer'/>
<jsp:useBean scope='session' id='crfName' class='java.lang.String'/>

 <c:out value="${crfName}"/>

<h1><span class="title_manage"><fmt:message key="import_rule_data" bundle="${resworkflow}"/></span></h1>
<p><fmt:message key="import_rule_instructions" bundle="${restext}"/></p>

<form action="VerifyImportedRule?action=save" method="POST">


<input type="hidden" name="crfId" value="<c:out value="${version.crfId}"/>">

<fmt:message key="rule_verify_import_valid_rules" bundle="${resword}"/>

<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0">

<tr valign="top">
    <td class="table_header_row"><fmt:message key="rule_verify_import_rule_oid" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="rule_name" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="rule_verify_import_description" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="rule_verify_import_warnings" bundle="${resword}"/></td>
</tr>

<c:set var="rowCount" value="${0}" />
<c:forEach var="ruleBeanWrapper" items="${importedData.validRuleDefs}" >
    <tr valign="top">
        <td class="table_cell_left">
            <c:out value="${ruleBeanWrapper.auditableBean.oid}" />
        </td>
        <td class="table_cell_left">
            <c:out value="${ruleBeanWrapper.auditableBean.name}" />
        </td>
        <td class="table_cell_left">
            <c:out value="${ruleBeanWrapper.auditableBean.description}" />
        </td>
        <td class="table_cell_left">
            <c:forEach var="warning" items="${ruleBeanWrapper.importErrors}" >
            <c:out value="${warning}" />
            </c:forEach>
        </td>
</tr>
</c:forEach>

</table>

</div>
</div></div></div></div></div></div></div></div></div>
<br clear="all">
<fmt:message key="rule_verify_import_duplicate_rules" bundle="${resword}"/>

<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="tablebox_center">

<table border="0" cellpadding="0" cellspacing="0">
<tr valign="top"></tr>

<tr valign="top">
    <td class="table_header_row"><fmt:message key="rule_verify_import_rule_oid" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="rule_name" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="rule_verify_import_description" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="rule_verify_import_warnings" bundle="${resword}"/></td>
</tr>

<c:set var="rowCount" value="${0}" />
<c:forEach var="ruleBeanWrapper" items="${importedData.duplicateRuleDefs}" >
    <tr valign="top">
        <td class="table_cell_left">
            <c:out value="${ruleBeanWrapper.auditableBean.oid}" />
        </td>
        <td class="table_cell_left">
            <c:out value="${ruleBeanWrapper.auditableBean.name}" />
        </td>
        <td class="table_cell_left">
            <c:out value="${ruleBeanWrapper.auditableBean.description}" />
        </td>
        <td class="table_cell_left">
            <c:forEach var="warning" items="${ruleBeanWrapper.importErrors}" >
            <c:out value="${warning}" />
            </c:forEach>
        </td>
    </tr>
</c:forEach>
</table>

</div>
</div></div></div></div></div></div></div></div></div>

<br clear="all">
<fmt:message key="rule_verify_import_invalid_rules" bundle="${resword}"/>
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0">

<tr valign="top">
    <td class="table_header_row"><fmt:message key="rule_verify_import_rule_oid" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="rule_name" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="rule_verify_import_description" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="rule_import_error" bundle="${resword}"/></td>
</tr>

<c:set var="rowCount" value="${0}" />
<c:forEach var="ruleBeanWrapper" items="${importedData.inValidRuleDefs}" >
    <tr valign="top">
        <td class="table_cell_left">
            <c:out value="${ruleBeanWrapper.auditableBean.oid}" />
        </td>
        <td class="table_cell_left">
            <c:out value="${ruleBeanWrapper.auditableBean.name}" />
        </td>
        <td class="table_cell_left">
            <c:out value="${ruleBeanWrapper.auditableBean.description}" />
        </td>
        <td class="table_cell_left">
            <c:forEach var="error" items="${ruleBeanWrapper.importErrors}" >
            <c:out value="${error}" />
            </c:forEach>
        </td>
</tr>
</c:forEach>
</table>

</div>
</div></div></div></div></div></div></div></div></div>
<br clear="all"/>

<fmt:message key="rule_verify_import_valid_rule_assignments" bundle="${resword}"/>
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0">

<tr valign="top">

</tr>
<tr valign="top">
    <td class="table_header_row">Target</td>
    <td class="table_header_row">Rules</td>
</tr>

<c:set var="rowCount" value="${0}" />
<c:forEach var="ruleBeanWrapper" items="${importedData.validRuleSetDefs}" >
    <tr valign="top">
        <td class="table_cell_left">
            <c:out value="${ruleBeanWrapper.auditableBean.target.value}" />
        </td>
        <td class="table_cell_left">
            <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
            <div class="tablebox_center">
            <table border="0" cellpadding="0" cellspacing="0">
            <tr valign="top">
                <td class="table_header_row"><fmt:message key="rule_verify_import_rule_oid" bundle="${resword}"/></td>
                <td class="table_header_row"><fmt:message key="rule_verify_import_action_type" bundle="${resword}"/></td>
                <td class="table_header_row"><fmt:message key="rule_verify_import_action_message" bundle="${resword}"/></td>
            </tr>
            <c:forEach var="ruleSetRule" items="${ruleBeanWrapper.auditableBean.ruleSetRules}" >

            <tr valign="top">
                <td class="table_cell_left">
                    <c:out value="${ruleSetRule.oid}" />
                </td>
                <c:forEach var="action" items="${ruleSetRule.actions}" >
                <td class="table_cell_left">
                        <c:out value="${action.actionType}" />
                </td>
                <td class="table_cell_left">
                        <c:out value="${action.message}" />
                </td>
                </c:forEach>
            </tr>
            </c:forEach>
            </table>
            </div>
            </div></div></div></div></div></div></div></div></div>

        </td>
    </tr>
</c:forEach>
</table>
</div>
</div></div></div></div></div></div></div></div></div>


<br clear="all">
<fmt:message key="rule_verify_import_duplicate_rule_assignments" bundle="${resword}"/>
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0">

<tr valign="top">

</tr>
<tr valign="top">
    <td class="table_header_row">Target</td>
    <td class="table_header_row">Rules</td>
</tr>

<c:set var="rowCount" value="${0}" />
<c:forEach var="ruleBeanWrapper" items="${importedData.duplicateRuleSetDefs}" >
    <tr valign="top">
        <td class="table_cell_left">
            <c:out value="${ruleBeanWrapper.auditableBean.target.value}" />
        </td>
        <td class="table_cell_left">
            <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
            <div class="tablebox_center">
            <table border="0" cellpadding="0" cellspacing="0">
            <tr valign="top">
                <td class="table_header_row"><fmt:message key="rule_verify_import_rule_oid" bundle="${resword}"/></td>
                <td class="table_header_row"><fmt:message key="rule_verify_import_action_type" bundle="${resword}"/></td>
                <td class="table_header_row"><fmt:message key="rule_verify_import_action_message" bundle="${resword}"/></td>
            </tr>
            <c:forEach var="ruleSetRule" items="${ruleBeanWrapper.auditableBean.ruleSetRules}" >

            <tr valign="top">
                <td class="table_cell_left">
                    <c:out value="${ruleSetRule.oid}" />
                </td>
                <c:forEach var="action" items="${ruleSetRule.actions}" >
                <td class="table_cell_left">
                        <c:out value="${action.actionType}" />
                </td>
                <td class="table_cell_left">
                        <c:out value="${action.message}" />
                </td>
                </c:forEach>
            </tr>
            </c:forEach>
            </table>
            </div>
            </div></div></div></div></div></div></div></div></div>

        </td>
    </tr>
</c:forEach>
</table>
</div>
</div></div></div></div></div></div></div></div></div>

<br clear="all">
<fmt:message key="rule_verify_import_invalid_rule_assignments" bundle="${resword}"/>
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0">

<tr valign="top">

</tr>
<tr valign="top">
    <td class="table_header_row"><fmt:message key="rule_verify_import_target" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="rule_verify_import_rules" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="rule_verify_import_errors" bundle="${resword}"/></td>
</tr>

<c:set var="rowCount" value="${0}" />
<c:forEach var="ruleBeanWrapper" items="${importedData.inValidRuleSetDefs}" >
    <tr valign="top">
        <td class="table_cell_left">
            <c:out value="${ruleBeanWrapper.auditableBean.target.value}" />
        </td>
        <td class="table_cell_left">
            <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
            <div class="tablebox_center">
            <table border="0" cellpadding="0" cellspacing="0">
            <tr valign="top">
                <td class="table_header_row"><fmt:message key="rule_verify_import_rule_oid" bundle="${resword}"/></td>
                <td class="table_header_row"><fmt:message key="rule_verify_import_action_type" bundle="${resword}"/></td>
                <td class="table_header_row"><fmt:message key="rule_verify_import_action_message" bundle="${resword}"/></td>
            </tr>
            <c:forEach var="ruleSetRule" items="${ruleBeanWrapper.auditableBean.ruleSetRules}" >

            <tr valign="top">
                <td class="table_cell_left">
                    <c:out value="${ruleSetRule.oid}" />
                </td>
                <c:forEach var="action" items="${ruleSetRule.actions}" >
                <td class="table_cell_left">
                        <c:out value="${action.actionType}" />
                </td>
                <td class="table_cell_left">
                        <c:out value="${action.message}" />
                </td>
                </c:forEach>
            </tr>
            </c:forEach>
            </table>
            </div>
            </div></div></div></div></div></div></div></div></div>

        </td>
        <td class="table_cell_left">
            <c:forEach var="error" items="${ruleBeanWrapper.importErrors}" >
            <c:out value="${error}" />
            </c:forEach>
        </td>
    </tr>
</c:forEach>
</table>
</div>
</div></div></div></div></div></div></div></div></div>

<br clear="all">
<input type="submit" value="Continue" class="button_long">
<input type="button" onclick="goBack()"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/>

</form>

<c:choose>
  <c:when test="${userBean.sysAdmin && module=='admin'}">
  <c:import url="../include/workflow.jsp">
   <c:param name="module" value="admin"/>
  </c:import>
 </c:when>
  <c:otherwise>
   <c:import url="../include/workflow.jsp">
   <c:param name="module" value="manage"/>
  </c:import>
  </c:otherwise>
 </c:choose>

<jsp:include page="../include/footer.jsp"/>