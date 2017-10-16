<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>


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
<tr id="sidebar_Instructions_open" style="display: none">
        <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

        <b><fmt:message key="instructions" bundle="${resword}"/></b>

        <div class="sidebar_tab_content">

        </div>

        </td>

    </tr>
    <tr id="sidebar_Instructions_closed" style="display: all">
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
</c:choose><fmt:message key="rule_manage_rule_title" bundle="${resword}"/></span></h1>
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
<div style="width: 900px">
 <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr valign="top">
    <td class="table_header_row_left"><fmt:message key="rule_name" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="rule_oid" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="rule_expression" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="rule_status" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="rule_execute_on" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="rule_action_type" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="rule_action_summary" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="action" bundle="${resword}"/></td>
    </tr>
  <c:forEach var ="ruleSetRule" items="${ruleSet.ruleSetRules}">
    <c:choose>
     <c:when test="${ruleSetRule.status.name eq 'AVAILABLE'}">
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
    <td rowspan="${count +1}" class="table_cell"><c:out value="${ruleSetRule.ruleBean.oid}"/></td>
    <td rowspan="${count +1}" class="table_cell"><c:out value="${ruleSetRule.ruleBean.expression.value}"/></td>
    <td rowspan="${count +1}" class="table_cell <c:out value='${className}'/>">${ruleSetRule.status.description}</td>
    <td class="table_cell"></td>
    <td class="table_cell"></td>
    <td class="table_cell">&nbsp;</td>
    <td class="table_cell">
      <table border="0" cellpadding="0" cellspacing="0">
      <tr>
      <c:choose>
      <c:when test="${ ruleSet.status.name eq 'AVAILABLE'  }">
      <c:if test="${ ruleSetRule.status.name eq 'AVAILABLE'  }">
      <td><a href="RunRuleSet?ruleSetId=<c:out value="${ruleSet.id}"/>&ruleId=<c:out value="${ruleSetRule.ruleBean.id}"/>"
      onMouseDown="javascript:setImage('bt_View1','images/bt_ExexuteRules.gif');"
      onMouseUp="javascript:setImage('bt_View1','images/bt_ExexuteRules.gif');"><img
      name="bt_View1" src="images/bt_ExexuteRules.gif" border="0" alt="<fmt:message key="rule_run" bundle="${resword}"/>" title="<fmt:message key="rule_run" bundle="${resword}"/>" align="left" hspace="6"></a>
      </td>
      <td><a href="UpdateRuleSetRule?action=remove&ruleSetRuleId=<c:out value="${ruleSetRule.id}"/>&ruleSetId=<c:out value="${ruleSet.id}"/>"
      onMouseDown="javascript:setImage('bt_Remove1','images/bt_Remove_d.gif');"
      onMouseUp="javascript:setImage('bt_Remove1','images/bt_Remove.gif');"
      onClick='return confirm("<fmt:message key="rule_if_you_remove_this" bundle="${resword}"/>");'><img
      name="bt_Remove1" src="images/bt_Remove.gif" border="0" alt="<fmt:message key="remove" bundle="${resword}"/>" title="<fmt:message key="remove" bundle="${resword}"/>" align="left" hspace="6"></a>
      </td>
      </c:if>
      <c:if test="${ ruleSetRule.status.name eq 'DELETED' }">
      <td>
      <a href="UpdateRuleSetRule?action=restore&ruleSetRuleId=<c:out value="${ruleSetRule.id}"/>&ruleSetId=<c:out value="${ruleSet.id}"/>"
      onMouseDown="javascript:setImage('bt_Restor3','images/bt_Restore_d.gif');"
      onMouseUp="javascript:setImage('bt_Restore3','images/bt_Restore.gif');"
      onClick='return confirm("<fmt:message key="rule_if_you_restore_this" bundle="${resword}"/>");'><img
      name="bt_Restore3" src="images/bt_Restore.gif" border="0" alt="<fmt:message key="restore" bundle="${resword}"/>" title="<fmt:message key="restore" bundle="${resword}"/>" align="left" hspace="6"></a>
      </td>
      </c:if>
      </c:when>
      <c:otherwise>
      </c:otherwise>
      </c:choose>
      </tr></table></td>

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


<p><a href="ViewRuleAssignment"/><fmt:message key="rule_go_back_to_Assignment_list" bundle="${resword}"/></a></p>

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