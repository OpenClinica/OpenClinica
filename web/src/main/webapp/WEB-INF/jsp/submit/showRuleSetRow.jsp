<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<jsp:useBean scope="request" id="currRow" class="org.akaza.openclinica.control.submit.ViewRuleAssignmentRow" />
<c:set var="count" value="${currRow.bean.ruleSetRuleSize}"/>
<c:set var="count" value="${count+1}"/>

<c:choose>
  <c:when test="${currRow.bean.status.name eq 'AVAILABLE' }">
    <c:set var="className" value="aka_green_highlight"/>
  </c:when>
  <c:when test="${currRow.bean.status.name eq 'DELETED'}">
    <c:set var="className" value="aka_red_highlight"/>
  </c:when>
</c:choose>
<tr valign="top" bgcolor="#F5F5F5">
  <td rowspan="<c:out value="${count}"/>" class="table_cell_left"><c:out value="${currRow.bean.studyEventDefinitionName}"/></td>
  <td rowspan="<c:out value="${count}"/>" class="table_cell"><c:out value="${currRow.bean.crfWithVersionName}"/></td>
  <td rowspan="<c:out value="${count}"/>" class="table_cell"><c:out value="${currRow.bean.groupLabel}"/>&nbsp;</td>
  <td rowspan="<c:out value="${count}"/>" class="table_cell"><c:out value="${currRow.bean.itemName}"/>&nbsp;</td>
  <td class="table_cell"></td>
  <td class="table_cell">&nbsp;</td>
    <%--oid space --%>
  <td class="table_cell">&nbsp;</td>
  <%--
  <td class="table_cell"><fmt:formatDate value="${currRow.bean.createdDate}" dateStyle="short"/></td>
  <td class="table_cell"><c:out value="${currRow.bean.owner.name}"/></td>
  <td class="table_cell <c:out value='${className}'/>"><c:out value="${currRow.bean.status.name}"/></td>
  
  <td class="table_cell">&nbsp;</td>--%>
  <td class="table_cell">
    <table border="0" cellpadding="0" cellspacing="0">
      <tr>
        <td><a href="ViewRuleSet?ruleSetId=<c:out value="${currRow.bean.id}"/>"
               onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
               onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><img
          name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6"></a>
        </td>
        <c:choose>
          <c:when test="${currRow.bean.status.name eq 'AVAILABLE' }">
            <c:if test="${userBean.sysAdmin || (userRole.manageStudy)}">
              <td><a href="RunRuleSet?ruleSetId=<c:out value="${currRow.bean.id}"/>"
                   onMouseDown="javascript:setImage('bt_Run1','images/bt_ExexuteRules.gif');"
                   onMouseUp="javascript:setImage('bt_Run1','images/bt_ExexuteRules.gif');"><img
                  name="bt_Edit1" src="images/bt_ExexuteRules.gif" border="0" alt="<fmt:message key="rule_run" bundle="${resword}"/>" title="<fmt:message key="rule_run" bundle="${resword}"/>" align="left" hspace="6"></a>
              </td>
              <c:if test="${ readOnly != 'true' }">
              <td><a href="RemoveRuleSet?action=confirm&ruleSetId=<c:out value="${currRow.bean.id}"/>"
                     onMouseDown="javascript:setImage('bt_Remove1','images/bt_Remove_d.gif');"
                     onMouseUp="javascript:setImage('bt_Remove1','images/bt_Remove.gif');"><img
                name="bt_Remove1" src="images/bt_Remove.gif" border="0" alt="<fmt:message key="remove" bundle="${resword}"/>" title="<fmt:message key="remove" bundle="${resword}"/>" align="left" hspace="6"></a>
              </td>
              </c:if>
            </c:if>
          </c:when>
          <c:otherwise>
            <c:if test="${ readOnly != 'true' }">
            <td><a href="RestoreRuleSet?action=confirm&ruleSetId=<c:out value="${currRow.bean.id}"/>"
                   onMouseDown="javascript:setImage('bt_Restor3','images/bt_Restore_d.gif');"
                   onMouseUp="javascript:setImage('bt_Restore3','images/bt_Restore.gif');"><img
              name="bt_Restore3" src="images/bt_Restore.gif" border="0" alt="<fmt:message key="restore" bundle="${resword}"/>" title="<fmt:message key="restore" bundle="${resword}"/>" align="left" hspace="6"></a>
            </td>
            </c:if>
          </c:otherwise>
        </c:choose>
        <td><a href="ViewRuleSetAudit?ruleSetId=<c:out value="${currRow.bean.id}"/>"
                     onMouseDown="javascript:setImage('bt_Audit1','images/icon_NotStarted.gif');"
                     onMouseUp="javascript:setImage('bt_Audit1','images/icon_NotStarted.gif');"><img
                name="bt_Remove1" src="images/icon_NotStarted.gif" border="0" alt="<fmt:message key="audit_logs" bundle="${resword}"/>" title="<fmt:message key="audit_logs" bundle="${resword}"/>" align="left" hspace="6"></a>
              </td>
      </tr>
    </table>
  </td>
</tr>
<c:forEach var ="ruleSetRule" items="${currRow.bean.ruleSetRules}">
    
  <%-- color-coded statuses...
  <c:choose>
    <c:when test="${version.status.name eq 'available'}">
      <c:set var="className" value="aka_green_highlight"/>
    </c:when>
    <c:when test="${version.status.name eq 'removed'}">
      <c:set var="className" value="aka_red_highlight"/>
    </c:when>
  </c:choose>--%>
  <tr valign="top">
    <td class="table_cell"><c:out value="${ruleSetRule.ruleBean.name}"/></td>
    <td class="table_cell"><c:out value="${ruleSetRule.ruleBean.oid}"/></td>
    <td class="table_cell">
        <c:forEach var ="action" items="${ruleSetRule.actions}">
            <c:out value="${action.actionType}"/>
        </c:forEach>
    </td>
    <td class="table_cell">&nbsp;</td>
  </tr> 
</c:forEach>
<tr><td class="table_divider" colspan="9">&nbsp;</td></tr>