<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
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

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: all">
        <td class="sidebar_tab">
        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>
        <b><fmt:message key="instructions" bundle="${resword}"/></b>
        <div class="sidebar_tab_content">
            <fmt:message key="restore_rule_assignment_1" bundle="${restext}"/>&nbsp;<c:out value="${study.name}"/>.&nbsp;<fmt:message key="restore_rule_assignment_2" bundle="${restext}"/>
        </div>
        </td>
    </tr>
    <tr id="sidebar_Instructions_closed" style="display: none">
        <td class="sidebar_tab">
        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>
        <b><fmt:message key="instructions" bundle="${resword}"/></b>
        </td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='ruleSet' class='org.akaza.openclinica.domain.rule.RuleSetBean'/>

<h1><c:choose>
<c:when test="${userBean.sysAdmin && module=='admin'}">
<span class="title_manage">
</c:when>
<c:otherwise>
<span class="title_Manage">
</c:otherwise>
</c:choose><fmt:message key="rule_restore_rule_assignments" bundle="${resword}"/></span></h1>
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr valign="top"><td class="table_header_column"><fmt:message key="rule_expression" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${ruleSet.target.value}"/>
  </td></tr>
  <tr valign="top"><td class="table_header_column_top"><fmt:message key="rule_study_event_definition" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${ruleSet.studyEventDefinitionNameWithOID}"/>
   </td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="CRF_name" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${ruleSet.crfWithVersionNameWithOid}"/>
  </td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="rule_group_label" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${ruleSet.groupLabelWithOid}"/>
  </td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="rule_item_name" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${ruleSet.itemNameWithOid}"/>
  </td></tr>
</table>
</div>
</div></div></div></div></div></div></div></div>

</div>
<br>
<c:choose>
<c:when test="${userBean.sysAdmin && module=='admin'}">
<span class="table_title_Admin">
</c:when>
<c:otherwise>
<span class="table_title_Manage">
</c:otherwise>
</c:choose>
<div style="width: 850px">
 <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr valign="top">
    <td class="table_header_row_left"><fmt:message key="rule_name" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="rule_expression" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="rule_status" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="rule_execute_on" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="rule_action_type" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="rule_action_summary" bundle="${resword}"/></td>
    </tr>
  <c:forEach var ="ruleSetRule" items="${ruleSet.ruleSetRules}">
    <c:choose>
     <c:when test="${ruleSetRule.status.name eq 'available'}">
    <c:set var="className" value="aka_green_highlight"/>
    </c:when>
    <c:otherwise>
     <c:set var="className" value="aka_red_highlight"/>
    </c:otherwise>
    </c:choose>
    <c:set var="count" value="0"/>
    <c:forEach items="${ruleSetRule.allActionsWithEvaluatesToAsKey}" varStatus="status">
      <c:set  var="count" value="${count + fn:length(status.current.value) +1}" />
    </c:forEach>

    <tr valign="top">
    <td rowspan="${count +1}" class="table_cell_left"><c:out value="${ruleSetRule.ruleBean.name}"/></td>
    <td rowspan="${count +1}" class="table_cell"><c:out value="${ruleSetRule.ruleBean.expression.value}"/></td>
    <td rowspan="${count +1}" class="table_cell <c:out value='${className}'/>">${ruleSetRule.status.name}</td>
    <td class="table_cell"></td>
    <td class="table_cell"></td>
    <td class="table_cell">&nbsp;</td>

    <c:forEach items="${ruleSetRule.allActionsWithEvaluatesToAsKey}" varStatus="status">
    <tr valign="top">
        <td rowspan="${fn:length(status.current.value) +1 }" class="table_cell"><c:out value="${status.current.key}"/></td>
        <td class="table_cell">&nbsp;</td>
        <td class="table_cell">&nbsp;</td>
        <td class="table_cell">&nbsp;</td>
    </tr>
        <c:forEach items="${status.current.value}" var="val">
        <tr valign="top">
            <td class="table_cell"><c:out value="${val.actionType}"/></td>
            <td class="table_cell"><c:out value="${val.message}"/></td>
            <td class="table_cell">&nbsp;</td>
        </tr>
        </c:forEach>
    </c:forEach>

    <tr><td class="table_divider" colspan="7">&nbsp;</td></tr>


 </c:forEach>

</table>
</div>
</div></div></div></div></div></div></div></div>

</div>
<form action='RestoreRuleSet?action=submit&ruleSetId=<c:out value="${ruleSet.id}"/>' method="POST">
<input type="submit" name="Submit" value="<fmt:message key="rule_restore_rule_assignments_button" bundle="${resword}"/>" class="button_xlong" onClick='return confirm("<fmt:message key="rule_if_you_restore_this_assignments" bundle="${resword}"/>");'>
<input type="button" name="Cancel" id="cancel" value="<fmt:message key='cancel' bundle='${resword}'/>" class="button_long" onClick="window.location.href='ViewRuleAssignment';"/>
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