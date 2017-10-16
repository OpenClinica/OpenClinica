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
</c:choose><fmt:message key="test_rule_title" bundle="${resword}"/></span></h1>

<div style="width: 600px">
<fmt:message key="test_rules_instructions_1" bundle="${resword}"/>
</div>
<br>
<form action="TestRule?action=submit" method="post">
<div style="width: 600px">
<!-- These DIVs define shaded box borders -->
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0" cellspacing="0">

  <tr valign="top">
  <td class="formlabel"><fmt:message key="test_rule_target" bundle="${resword}"/>:</td>
  <td><textarea  name="target" rows="3" cols="70" wrap="hard" style="font-size:11px;"><c:out value="${target}"/></textarea></td>
  </tr>

  <tr valign="top"><td>&nbsp;</td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="test_rule" bundle="${resword}"/>:</td>
  <td><textarea  name="rule" rows="3" cols="70" wrap="hard" style="font-size:11px;"><c:out value="${rule}"/></textarea></td>
  </tr>

  <tr valign="top"><td>&nbsp;</td></tr>

  <tr valign="top"><td class="formlabel"><fmt:message key="rule_result" bundle="${resword}"/>:</td>
  <td style="color:#789EC5;"><center>${result}</center></td>
  </tr>


</table>
</div>
</div></div></div></div></div></div></div></div>

</div>

<table border="0" cellpadding="0" cellspacing="0">
<tr>
<td><input type="submit" name="Submit" value="<fmt:message key="submit" bundle="${resword}"/>" class="button_long"></td>
<td><input type="button" name="Exit" value="<fmt:message key="exit" bundle="${resword}"/>" class="button_long" onClick="window.location.href='ViewRuleAssignment';"></td>
</tr>

</table>
</form>
<br><br>

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