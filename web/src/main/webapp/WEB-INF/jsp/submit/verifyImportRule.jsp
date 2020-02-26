<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %> 

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

<script type="text/javascript">
   function proceed(){
            var confirm1 = confirm('<fmt:message key="rule_verify_import_rule_message" bundle="${resword}"/>');
            if(confirm1){
                document.forms["verifyImportedRule"].submit();
            }
        }
</script>

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
            <fmt:message key="verify_import_rule_instructions" bundle="${restext}"/>
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

<h1><span class="title_manage"><fmt:message key="import_rule_data" bundle="${resworkflow}"/> ${study.name}</span></h1>

<form action="VerifyImportedRule?action=save" name="verifyImportedRule" method="POST">
<input type="hidden" name="crfId" value="<c:out value="${version.crfId}"/>">

<div style="width: 500px">

<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="textbox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">

    <tr valign="top">
        <td class="table_header_row" colspan="2"><fmt:message key="summary_statistics" bundle="${resword}"/>:</td>
    </tr>
    <tr valign="top">
        <td class="table_cell_left"><fmt:message key="rule_verify_import_valid_rules_num" bundle="${resword}"/> <c:out value="${fn:length(importedData.validRuleDefs)}" /></td>
        <td class="table_cell_left"><fmt:message key="rule_verify_import_valid_rule_assign_num" bundle="${resword}"/> <c:out value="${fn:length(importedData.validRuleSetDefs)}" /></td>
    </tr>
    <tr valign="top">
        <td class="table_cell_left"><fmt:message key="rule_verify_import_dup_valid_rules_num" bundle="${resword}"/><c:out value="${fn:length(importedData.duplicateRuleDefs)}" /></td>
        <td class="table_cell_left"><fmt:message key="rule_verify_import_dup_valid_rule_assign_num" bundle="${resword}"/> <c:out value="${fn:length(importedData.duplicateRuleSetDefs)}" /></td>
    </tr>
    <tr valign="top">
        <td class="table_cell_left">
            <fmt:message key="rule_verify_import_invalid_rules_num" bundle="${resword}"/>
            <c:set var="inValidRuleDefs" value="${fn:length(importedData.inValidRuleDefs)}"/>
            <c:if test="${inValidRuleDefs > 0 }">
                <b><span style="color:red"><c:out value="${inValidRuleDefs}" /></span></b>
            </c:if>
            <c:if test="${inValidRuleDefs == 0 }">
                <c:out value="${inValidRuleDefs}" />
            </c:if>
        </td>
        <td class="table_cell_left"><fmt:message key="rule_verify_import_invalid_rule_assign_num" bundle="${resword}"/>
            <c:set var="inValidRuleSetDefs" value="${fn:length(importedData.inValidRuleSetDefs)}"/>
            <c:if test="${inValidRuleSetDefs > 0 }">
                <b><span style="color:red"><c:out value="${inValidRuleSetDefs}" /></span></b>
            </c:if>
            <c:if test="${inValidRuleSetDefs == 0 }">
                <c:out value="${inValidRuleSetDefs}" />
            </c:if>
        </td>
    </tr>
</table>
</div>
</div></div></div></div></div></div></div></div>
</div>
<br/>

<p>
<c:choose>
<c:when test="${fn:length(importedData.inValidRuleDefs) > 0 || fn:length(importedData.inValidRuleSetDefs) > 0 }"></c:when>
<c:otherwise>
    <input type="button" onClick="proceed()" value="<fmt:message key="continue" bundle="${resword}"/>" class="button_long"> 
</c:otherwise>
</c:choose>
<input type="button" onclick="goBack()"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/>

</p>

<h3><fmt:message key="rule_verify_import_valid_rules" bundle="${resword}"/></h3>
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
<h3 style="margin-bottom: 0px;"><fmt:message key="rule_verify_import_duplicate_rules" bundle="${resword}"/></h3>
<div style="margin-bottom: 14px;"><fmt:message key="rule_verify_import_duplicate_rules2" bundle="${resword}"/></div>
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
<h3><fmt:message key="rule_verify_import_invalid_rules" bundle="${resword}"/></h3>
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
            <span style="color:red"><b><c:out value="${error}" /></b></span>
            </c:forEach>
        </td>
</tr>
</c:forEach>
</table>

</div>
</div></div></div></div></div></div></div></div></div>
<br clear="all"/>

<h3><fmt:message key="rule_verify_import_valid_rule_assignments" bundle="${resword}"/></h3>
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
                <!-- <td class="table_header_row"><fmt:message key="rule_verify_import_action_type" bundle="${resword}"/></td> -->
                <td class="table_header_row"><fmt:message key="rule_verify_import_action_message" bundle="${resword}"/></td>
            </tr>
            <c:forEach var="ruleSetRule" items="${ruleBeanWrapper.auditableBean.ruleSetRules}" >
            <tr valign="top">

                <c:if test="${ruleSetRule.status.code != 5 }">
                <c:forEach var="action" items="${ruleSetRule.actions}" >
                <tr>
                <td class="table_cell_left">
                    <c:choose>
                        <c:when test="${ruleSetRule.ruleBean.oid != '' && ruleSetRule.ruleBean.oid != null}">
                            <c:out value="${ruleSetRule.ruleBean.oid}" />
                        </c:when>
                        <c:otherwise>
                            <c:out value="${ruleSetRule.originalOid}" />
                        </c:otherwise>
                    </c:choose>
                </td>
                <td class="table_cell_left">
                    <table>
                    <c:forEach items="${action.propertiesForDisplay}" var="mapEntry" varStatus="status">
                            <tr valign="top">
                                <td class="formlabel"><i><fmt:message key="${mapEntry.key}" bundle="${resword}" /></i></td>
                                <td class="formValue">${mapEntry.value}</td>
                            </tr>
                    </c:forEach>
                    </table>
                </td>
                <!-- 
                <td class="table_cell_left">
                        <c:out value="${action.actionType}" />
                </td>
                <td class="table_cell_left">
                        <c:out value="${action.summary}" />
                </td>
                 -->
                <td class="table_cell_left">
                        <c:if test="${ruleSetRule.status.code == 5 }"><b><fmt:message key="rule_verify_import_info1" bundle="${resword}"/></b></c:if>
                        <c:if test="${ruleSetRule.id ==  null }"><b><fmt:message key="rule_verify_import_info3" bundle="${resword}"/></b></c:if>
                        <c:if test="${ruleSetRule.ruleSetRuleBeanImportStatus == 'TO_BE_REMOVED'}"><b><fmt:message key="rule_verify_import_info2" bundle="${resword}"/></b></c:if>
                        <c:if test="${ruleSetRule.ruleSetRuleBeanImportStatus == 'EXACT_DOUBLE' }"><b><fmt:message key="rule_verify_import_info4" bundle="${resword}"/></b></c:if>
                </td>
                </tr>
				</c:forEach>
                </c:if>
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
<h3 style="margin-bottom: 0px;"><fmt:message key="rule_verify_import_duplicate_rule_assignments" bundle="${resword}"/></h3>
<div style="margin-bottom: 14px;"><fmt:message key="rule_verify_import_duplicate_rule_assignments2" bundle="${resword}"/></div>
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
                <!--<td class="table_header_row"><fmt:message key="rule_verify_import_action_type" bundle="${resword}"/></td> -->
                <td class="table_header_row"><fmt:message key="rule_verify_import_action_message" bundle="${resword}"/></td>
                <td class="table_header_row"><fmt:message key="rule_verify_import_info" bundle="${resword}"/></td>
            </tr>
            <c:forEach var="ruleSetRule" items="${ruleBeanWrapper.auditableBean.ruleSetRules}" >
            <tr valign="top">
                
                <c:if test="${ruleSetRule.status.code != 5 && ruleSetRule.ruleSetRuleBeanImportStatus != null }">
                <c:forEach var="action" items="${ruleSetRule.actions}" >
                <tr>
                <td class="table_cell_left">
                    ${ruleSetRule.ruleBean.oid}
                    <c:choose>
                        <c:when test="${ruleSetRule.ruleBean.oid != '' && ruleSetRule.ruleBean.oid != null }">
                            <c:out value="${ruleSetRule.ruleBean.oid}" />
                        </c:when>
                        <c:otherwise>
                            <c:out value="${ruleSetRule.originalOid}" />
                        </c:otherwise>
                    </c:choose>
                </td>
                <td class="table_cell_left">
                    <table>
                    <c:forEach items="${action.propertiesForDisplay}" var="mapEntry" varStatus="status">
                            <tr valign="top">
                                <td class="formlabel"><i><fmt:message key="${mapEntry.key}" bundle="${resword}" /></i></td>
                                <td class="formValue">${mapEntry.value}</td>
                            </tr>
                    </c:forEach>
                    </table>
                </td>
                <!--
                <td class="table_cell_left">
                        <c:out value="${action.actionType}" />
                </td>
                <td class="table_cell_left">
                        <c:out value="${action.summary}" />
                </td>
                -->
                <td class="table_cell_left">
                        <c:if test="${ruleSetRule.status.code == 5 }"><b><fmt:message key="rule_verify_import_info1" bundle="${resword}"/></b></c:if>
                        <c:if test="${ruleSetRule.status.code == 1 && ruleSetRule.ruleSetRuleBeanImportStatus == null }"><b><fmt:message key="rule_verify_import_info5" bundle="${resword}"/></b></c:if>
                        <c:if test="${ruleSetRule.id ==  null }"><b><fmt:message key="rule_verify_import_info3" bundle="${resword}"/></b></c:if>
                        <c:if test="${ruleSetRule.ruleSetRuleBeanImportStatus == 'TO_BE_REMOVED'}"><b><fmt:message key="rule_verify_import_info2" bundle="${resword}"/></b></c:if>
                        <c:if test="${ruleSetRule.ruleSetRuleBeanImportStatus == 'EXACT_DOUBLE' }"><b><fmt:message key="rule_verify_import_info4" bundle="${resword}"/></b></c:if>
                </td>
                </tr>
                </c:forEach>
                </c:if>
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
<h3><fmt:message key="rule_verify_import_invalid_rule_assignments" bundle="${resword}"/></h3>
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
                <!--<td class="table_header_row"><fmt:message key="rule_verify_import_action_type" bundle="${resword}"/></td> -->
                <td class="table_header_row"><fmt:message key="rule_verify_import_action_message" bundle="${resword}"/></td>
                <td class="table_header_row"><fmt:message key="rule_verify_import_info" bundle="${resword}"/></td>
            </tr>
            <c:forEach var="ruleSetRule" items="${ruleBeanWrapper.auditableBean.ruleSetRules}" >
            <tr valign="top">
                
                <c:if test="${ruleSetRule.status.code != 5 && ruleSetRule.ruleSetRuleBeanImportStatus != null }">
                <c:forEach var="action" items="${ruleSetRule.actions}" >
                <tr>
                <td class="table_cell_left">
                    <c:choose>
                        <c:when test="${ruleSetRule.ruleBean.oid != '' && ruleSetRule.ruleBean.oid != null}">
                            <c:out value="${ruleSetRule.ruleBean.oid}" />
                        </c:when>
                        <c:otherwise>
                            <c:out value="${ruleSetRule.originalOid}" />
                        </c:otherwise>
                    </c:choose>
                </td>
                <td class="table_cell_left">
                    <table>
                    <c:forEach items="${action.propertiesForDisplay}" var="mapEntry" varStatus="status">
                            <tr valign="top">
                                <td class="formlabel"><i><fmt:message key="${mapEntry.key}" bundle="${resword}" /></i></td>
                                <td class="formValue">${mapEntry.value}</td>
                            </tr>
                    </c:forEach>
                    </table>
                </td>
                <!--
                <td class="table_cell_left">
                        <c:out value="${action.actionType}" />
                </td>
                <td class="table_cell_left">
                        <c:out value="${action.summary}" />
                </td>
                -->
                <td class="table_cell_left">
                        <c:if test="${ruleSetRule.status.code == 5 }"><b><fmt:message key="rule_verify_import_info1" bundle="${resword}"/></b></c:if>
                        <c:if test="${ruleSetRule.status.code == 1 && ruleSetRule.ruleSetRuleBeanImportStatus == null }"><b><fmt:message key="rule_verify_import_info5" bundle="${resword}"/></b></c:if>
                        <c:if test="${ruleSetRule.id ==  null }"><b><fmt:message key="rule_verify_import_info3" bundle="${resword}"/></b></c:if>
                        <c:if test="${ruleSetRule.ruleSetRuleBeanImportStatus == 'TO_BE_REMOVED'}"><b><fmt:message key="rule_verify_import_info2" bundle="${resword}"/></b></c:if>
                        <c:if test="${ruleSetRule.ruleSetRuleBeanImportStatus == 'EXACT_DOUBLE' }"><b><fmt:message key="rule_verify_import_info4" bundle="${resword}"/></b></c:if>
                </td>
                </tr>
                </c:forEach>
                </c:if>
            </tr>
            </c:forEach>
            </table>
            </div>
            </div></div></div></div></div></div></div></div></div>

        </td>
        <td class="table_cell_left">
            <c:forEach var="error" items="${ruleBeanWrapper.importErrors}" >
                <span style="color:red"><b><c:out value="${error}" /></b></span>
            </c:forEach>
        </td>
    </tr>
</c:forEach>
</table>
</div>
</div></div></div></div></div></div></div></div></div>

<br clear="all">
<c:choose>
<c:when test="${fn:length(importedData.inValidRuleDefs) > 0 || fn:length(importedData.inValidRuleSetDefs) > 0 }"></c:when>
<c:otherwise>
    <input type="button" onClick="proceed()" value="<fmt:message key="continue" bundle="${resword}"/>" class="button_long"> 
</c:otherwise>
</c:choose>
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
