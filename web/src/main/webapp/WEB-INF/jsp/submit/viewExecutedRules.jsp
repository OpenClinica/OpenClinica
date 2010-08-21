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
<tr id="sidebar_Instructions_open" style="display: all">
        <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

        <b><fmt:message key="instructions" bundle="${resword}"/></b>

        <div class="sidebar_tab_content">
            <fmt:message key="rule_execute_rule_bottom_message" bundle="${resword}"/>
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

<script language="JavaScript" type="text/JavaScript">
    function showOrHideSubjects(count,showLink,hideLink){
        if ($("tr"+count).style.display == ''){
        $("tr"+count).style.display='none';
        $("a"+count).innerHTML= showLink ;
        }else{
        $("tr"+count).style.display='';
        $("a"+count).innerHTML= hideLink ;
        }
    }

</script>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='ruleSet' class='org.akaza.openclinica.domain.rule.RuleSetBean'/>
<jsp:useBean scope='request' id='ruleSetResult' class='java.util.ArrayList'/>

<h1><c:choose>
<c:when test="${userBean.sysAdmin && module=='admin'}">
<span class="title_manage">
</c:when>
<c:otherwise>
<span class="title_Manage">
</c:otherwise>
</c:choose><fmt:message key="rule_execute_rule_title" bundle="${resword}"/></span></h1>
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr valign="top"><td class="table_header_column"><fmt:message key="view_executed_rules_target_oid" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${ruleSet.target.value}"/>
  </td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="rule_study_event_definition" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${ruleSet.studyEventDefinitionNameWithOID}"/>
   </td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="CRF_name" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${ruleSet.crfWithVersionNameWithOid}"/>
  </td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="rule_group_label" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${ruleSet.groupLabelWithOid}"/>
  </td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="rule_item_name" bundle="${resword}"/>:</td><td class="table_cell">
  <a href="javascript: openDocWindow('ViewItemDetail?itemId=${ruleSet.itemId}')"><c:out value="${ruleSet.itemNameWithOid}"/></a>
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
<c:if test="${fn:length(ruleSetResult) > 0}">
<div style="width: 850px">
 <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr valign="top">
    <td class="table_header_row_left"><fmt:message key="rule_name" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="view_executed_rules_oid" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="rule_expression" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="rule_execute_on" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="rule_action_type" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="rule_action_summary" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="action" bundle="${resword}"/></td>
    </tr>
  <c:forEach var ="result" items="${ruleSetResult}" varStatus="status">
    <tr valign="top">
    <td  class="table_cell_left"><c:out value="${result.ruleName}"/></td>
    <td  class="table_cell"><c:out value="${result.ruleOid}"/></td>
    <td  class="table_cell"><c:out value="${result.expression}"/></td>
    <td class="table_cell"><c:out value="${result.executeOn}"/></td>
    <td class="table_cell"><c:out value="${result.actionType}"/></td>
    <td class="table_cell"><c:out value="${result.actionSummary}"/></td>
    <td class="table_cell"><span id="a${status.count}" style="color: BLUE; text-decoration: underline;" onClick="showOrHideSubjects(${status.count},'<fmt:message key="rule_show_subjects" bundle="${resword}" />','<fmt:message key="rule_hide_subjects" bundle="${resword}" />')"><u><fmt:message key="rule_show_subjects" bundle="${resword}" /></u></span></td>
</tr>
    <tr id="tr${status.count}" style="display: none;">
        <td class="table_cell" colspan="7">
        <span style="color:#D4A718;font-weight:bold;font-size:12px;"><fmt:message key="rule_subjects" bundle="${resword}" /></span><br/>
        <c:forEach var ="subject" items="${result.subjects}">
            <c:out value="${subject}"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        </c:forEach></td>
    </tr>
    </c:forEach>

</table>
</div>
</div></div></div></div></div></div></div></div>

</div>

</c:if>
<table border="0" cellpadding="0" cellspacing="0">
<tr>
<c:if test="${fn:length(ruleSetResult) > 0}">
<td>
<input type="button" name="Submit" id="submit" value="<fmt:message key="submit" bundle="${resword}"/>" class="button_long" onClick="window.location.href='RunRuleSet?ruleSetId=${ruleSet.id}&dryRun=no';"/></td>
</td>
</c:if>
<td>
<input type="button" name="Cancel" id="cancel" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button_long" onClick="window.location.href='ViewRuleAssignment';"/></td>
</tr></table>

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