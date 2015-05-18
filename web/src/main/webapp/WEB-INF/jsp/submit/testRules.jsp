<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>




<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword" />
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>


<c:choose>
    <c:when test="${userBean.sysAdmin && module=='admin'}">
        <c:import url="../include/admin-header.jsp" />
    </c:when>
    <c:otherwise>
        <c:import url="../include/managestudy-header.jsp" />
    </c:otherwise>
</c:choose>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp" />

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: all">
    <td class="sidebar_tab"><a
        href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img
        src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

    <b><fmt:message key="instructions" bundle="${resword}" /></b>

    <div class="sidebar_tab_content"></div>
        <fmt:message key="test_rules_instructions_1" bundle="${resword}" />

    </td>

</tr>
<tr id="sidebar_Instructions_closed" style="display: none">
    <td class="sidebar_tab"><a
        href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img
        src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

    <b><fmt:message key="instructions" bundle="${resword}" /></b></td>
</tr>
<jsp:include page="../include/sideInfo.jsp" />


<STYLE TYPE="text/css">
.formValue {
    padding-left: 6px;
    padding-right: 6px;
    padding-top: 4px;
    vertical-align: top;
}
</STYLE>
<script type="text/javascript">
    function displayLink(itemOid)
    {
        var theOid = itemOid.match('[A-Z_0-9]*$');
        openDocWindow('ViewItemDetail?itemOid='+theOid);
    }
    function displayEventLink(itemOid)
    {
        var theOid = itemOid.split(".")[0];
        openDocWindow('ViewEventDefinitionReadOnly?eventOid='+theOid);
    }
    function spliceAndReturn(itemOid)
    {
        if (itemOid.length > 50){
            document.write(itemOid.substr(0,50) + "<br>");
            document.write(itemOid.substr(50,itemOid.length));
        }else{
            document.write(itemOid);
        }
        
    }
</script>

<jsp:useBean scope='session' id='userBean'
    class='org.akaza.openclinica.bean.login.UserAccountBean' />
<jsp:useBean scope='request' id='ruleSet'
    class='org.akaza.openclinica.domain.rule.RuleSetBean' />


<jsp:useBean scope="request" id="presetValues" class="java.util.HashMap" />

<c:set var="target" value="" />
<c:set var="rule" value="" />


<c:forEach var="presetValue" items="${presetValues}">
    <c:if test='${presetValue.key == "target"}'>
        <c:set var="target" value="${presetValue.value}" />
    </c:if>
    <c:if test='${presetValue.key == "rule"}'>
        <c:set var="rule" value="${presetValue.value}" />
    </c:if>
</c:forEach>

<h1> <span class="title_manage">
   <fmt:message key="test_rules_title" bundle="${resword}" /></span></h1>


<form action="TestRule?action=${action}" method="post">
<h3><fmt:message key="test_rules_step_1" bundle="${resword}" /></h3>
<div style="width: 650px"><!-- These DIVs define shaded box borders -->
<div class="box_T">
<div class="box_L">
<div class="box_R">
<div class="box_B">
<div class="box_TL">
<div class="box_TR">
<div class="box_BL">
<div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0" cellspacing="0">

    <c:if test="${itemName != null}">
        <input type="hidden" name="itemName" value="<c:out value="${itemName}"/>" />
        <input type="hidden" name="ruleSetId" value="<c:out value="${ruleSetId}"/>" />
        <tr valign="top">
            <td class="formlabel"><fmt:message key="test_rules_item_name"
                bundle="${resword}" />:</td>
            <td class="formValue">
                <c:out value='${itemName}' />&nbsp;
                    <c:if test="${ruleSetId != null  && ruleSetId != '' }">
                        <a href="ViewRuleSet?ruleSetId=${ruleSetId}"/>(<fmt:message key="test_rules_view_all_rules_for_item" bundle="${resword}" />)</a>
                    </c:if>
            </td>
        </tr>

        <tr valign="top">
            <td>&nbsp;</td>
        </tr>
    </c:if>
    
    <c:if test="${itemDefinition != null}">
        <input type="hidden" name="itemDefinition" value="<c:out value="${itemDefinition}"/>" />
        <tr valign="top">
            <td class="formlabel"><fmt:message key="test_rules_item_definition"
                bundle="${resword}" />:</td>
            <td class="formValue"><c:out value='${itemDefinition}' /></td>
        </tr>

        <tr valign="top">
            <td>&nbsp;</td>
        </tr>
    </c:if>

    <tr valign="top">
        <td class="formlabel"><fmt:message key="test_rules_target"
            bundle="${resword}" />:</td>
        <td><textarea name="target" rows="3" cols="70" wrap="hard"
            style="font-size: 11px;"><c:out value='${target}' /></textarea></td>
    </tr>

    <tr valign="top">
        <td>&nbsp;</td>
    </tr>

    <tr valign="top">
        <td class="formlabel"><fmt:message key="test_rules_expression"
            bundle="${resword}" />:</td>
        <td><textarea name="rule" rows="3" cols="70" wrap="hard"
            style="font-size: 11px;"><c:out value='${rule}' /></textarea></td>
    </tr>

    <tr valign="top">
        <td>&nbsp;</td>
    </tr>
    <!--
  <tr valign="top"><td class="formlabel"><fmt:message key="rule_result" bundle="${resword}"/>:</td>
  <td style="color:#789EC5;"><center>${result} - ${duration} </center></td>
  </tr>
-->
</table>

<table border="0" cellpadding="0" cellspacing="0">
    <tbody>
        <tr valign="top">
            <td width="80px" class="formlabel"><fmt:message
                key="test_rules_actions" bundle="${resword}" /></td>
        </tr>


        <c:forEach items="${sessionScope.testRuleActions}"
            var="testRuleAction" varStatus="status">
            <tr valign="top">
                <td></td>
                <td class="formlabel"><fmt:message
                    key="test_rules_if_expression_evaluates" bundle="${resword}" /></td>
                <td class="formValue"><select name="actions${status.count -1}">
                    <option value="1"
                        <c:if test='${testRuleAction.expressionEvaluatesTo == "true"}'>SELECTED</c:if>>true</option>
                    <option value="0"
                        <c:if test='${testRuleAction.expressionEvaluatesTo == "false"}'>SELECTED</c:if>>false</option>
                </select></td>
            </tr>
            <c:forEach items="${testRuleAction.propertiesForDisplay}" var="mapEntry" varStatus="status">
                <tr valign="top"><td></td>
                    <c:choose>
                    <c:when test="${status.count == 1}">
                        <td class="formlabel"><fmt:message key="test_rules_execute_action" bundle="${resword}"/></td>
                    </c:when>
                    <c:otherwise><td></td></c:otherwise>
                    </c:choose>     
                    <td><i><fmt:message key="${mapEntry.key}" bundle="${resword}"/></i></td>
                    <td>${mapEntry.value}</td>
                </tr>
            </c:forEach>
            
            <c:if test="${testRuleAction.actionType.code!=1 && testRuleAction.actionType.code !=2 && testRuleAction.actionType.code !=7 && fn:length(testRuleAction.properties)>0}">
                <c:set var="props" value=""/>
                <c:forEach items="${testRuleAction.properties}" var="prop" varStatus="status">
                    <c:set var="props"><c:out value="${props}"/> <c:out value="${prop.oid}"/>,</c:set>
                </c:forEach>
                <c:if test="${fn:length(props)>0}">
                    <tr valign="top"><td></td><td></td>
                        <td><i><fmt:message key="dest_prop_colon" bundle="${resword}" /></i>&nbsp;</td>
                        <td><c:out value="${fn:substring(props,0,fn:length(props)-1)}"/></td>
                    </tr>
                </c:if>
               </c:if>

            <tr valign="top">
                <td>&nbsp;</td>
            </tr>
        </c:forEach>
</table>
</div>
</div>
</div>
</div>
</div>
</div>
</div>
</div>
</div>

</div>

<c:if test="${fn:length(testValues) > 0}">
    <h3><fmt:message key="test_rules_step_2" bundle="${resword}" /></h3>
    <div style="width: 650px"><!-- These DIVs define shaded box borders -->
    <div class="box_T">
    <div class="box_L">
    <div class="box_R">
    <div class="box_B">
    <div class="box_TL">
    <div class="box_TR">
    <div class="box_BL">
    <div class="box_BR">

    <div class="textbox_center">
    <table border="0" cellpadding="0" cellspacing="0" style="width: 570px;">



        <c:forEach items="${sessionScope.testValues}" var="mapEntry">
            <c:set var="tooltipKey" value="${mapEntry.key}-tooltip" />
            <c:set var="dibKey" value="${mapEntry.key}-dib" />
            <c:choose>
               <c:when test= "${dibKey}">
                 <c:set var="dibItemDataType" value ='<%= ((org.akaza.openclinica.bean.submit.DisplayItemBean)request.getAttribute((String)pageContext.getAttribute("dibKey"))).getItem().getItemDataTypeId() %>' />
               </c:when>
            <c:otherwise>
            <c:set var="dibItemDataType" value="${studyEventProperty}"/>
            </c:otherwise>
            </c:choose>
 
      
 <c:choose>
  <c:when test="${fn:endsWith(mapEntry.key,'.STATUS')}">
            <tr valign="top">
                <td class="formlabel">
                 <script>spliceAndReturn('${mapEntry.key}')</script>:</td>
                <td style="color: #789EC5;"><input name="${mapEntry.key}" value="${mapEntry.value}" id="${mapEntry.key}"/>
  </c:when>

  <c:when test="${fn:endsWith(mapEntry.key,'.STARTDATE')}">
            <c:set var="dibItemDataType" value="9"/>
            <tr valign="top">
                <td class="formlabel">
                <a href="javascript: displayEventLink('${mapEntry.key}')"
                    onmouseover="Tip('<%= request.getAttribute((String)pageContext.getAttribute("tooltipKey") ) %>')" 
                    onmouseout="UnTip()" >
                <script>spliceAndReturn('${mapEntry.key}')</script></a>:</td>
                <td style="color: #789EC5;"><input name="${mapEntry.key}" value="${mapEntry.value}" id="${mapEntry.key}"/>
  </c:when>
 
  <c:otherwise>
            <tr valign="top">
                <td class="formlabel">
 
                  <a href="javascript: displayLink('${mapEntry.key}')"
                    onmouseover="Tip('<%= request.getAttribute((String)pageContext.getAttribute("tooltipKey") ) %>')" 
                    onmouseout="UnTip()" >
                    <script>spliceAndReturn('${mapEntry.key}')</script></a>:</td>
                <td style="color: #789EC5;"><input name="${mapEntry.key}" value="${mapEntry.value}" id="${mapEntry.key}"/>
  </c:otherwise>
</c:choose>
 
 
  
                <c:if test="${dibItemDataType == 9 }">
                    <A HREF="#">
                      <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" id="${mapEntry.key}trigger" />
                        <script type="text/javascript">
                        Calendar.setup({inputField  : "${mapEntry.key}", ifFormat    : "<fmt:message key="date_format_calender" bundle="${resformat}"/>", button      : "${mapEntry.key}trigger" });
                        </script>
                    </a>
                </td>     
                
                </c:if>
            </tr>
            <tr>
               <td></td>
               <td colspan="2">&nbsp;<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="${mapEntry.key}" /></jsp:include></td>
           </tr>
        </c:forEach>



    </table>
    </div>
    </div>
    </div>
    </div>
    </div>
    </div>
    </div>
    </div>
    </div>

    </div>
</c:if> <c:if test="${ruleEvaluatesTo != null}">
    <h3><fmt:message key="test_rules_step_3" bundle="${resword}" /></h3>
    <div style="width: 650px"><!-- These DIVs define shaded box borders -->
    <div class="box_T">
    <div class="box_L">
    <div class="box_R">
    <div class="box_B">
    <div class="box_TL">
    <div class="box_TR">
    <div class="box_BL">
    <div class="box_BR">

    <div class="textbox_center">
    <table border="0" cellpadding="0" cellspacing="0">

        <c:choose>
            <c:when test="${targetFail == 'on' || ruleValidation == 'rule_invalid' }">
                <tr valign="top">
                    <td class="formlabel"><fmt:message
                        key="test_rules_rule_validation" bundle="${resword}" /></td>
                    <td class="formValue"><fmt:message
                        key="test_rules_${ruleValidation}" bundle="${resword}" /> <c:if
                        test="${ruleValidationFailMessage != null }"> - ${ruleValidationFailMessage}</c:if></td>
                </tr>

                <tr valign="top">
                    <td>&nbsp;</td>
                </tr>
            </table>    
            </c:when>
            <c:when test="${validate == null}">

                <c:set var="actionFired" value="N" />
                <c:forEach items="${sessionScope.testRuleActions}"
                    var="testRuleAction" varStatus="status">
                    <c:if
                        test="${testRuleAction.expressionEvaluatesTo == ruleEvaluatesTo && ruleEvaluatesTo != '' }">
                        <c:set var="actionFired" value="Y" />
                    </c:if>
                    <c:if test="${ruleEvaluatesTo == null }">
                        <c:set var="actionFired" value="" />
                    </c:if>
                </c:forEach>

                <tr valign="top">
                    <td class="formlabel"><fmt:message
                        key="test_rules_rule_validation" bundle="${resword}" /></td>
                    <td class="formValue"><fmt:message
                        key="test_rules_${ruleValidation}" bundle="${resword}" /> <c:if
                        test="${ruleValidationFailMessage != null }"> - ${ruleValidationFailMessage}</c:if></td>
                </tr>

                <tr valign="top">
                    <td>&nbsp;</td>
                </tr>

                <tr valign="top">
                    <td class="formlabel"><fmt:message
                        key="test_rules_expression_evaluates_to" bundle="${resword}" /></td>
                    <td class="formValue">${ruleEvaluatesTo}</td>
                </tr>

                <tr valign="top">
                    <td>&nbsp;</td>
                </tr>

                <tr valign="top">
                    <td class="formlabel"><fmt:message
                        key="test_rules_actions_fired" bundle="${resword}" /></td>
                    <td class="formValue"><c:out value="${actionFired}" /></td>
                </tr>

                <tr valign="top">
                    <td>&nbsp;</td>
                </tr>

                <tr valign="top">
                    <td class="formlabel"><fmt:message key="test_rules_ran_in"
                        bundle="${resword}" /></td>
                    <td class="formValue"><c:out value="${duration}" /></td>
                </tr>


                <tr valign="top">
                    <td>&nbsp;</td>
                </tr>

                <c:if test="${actionFired == 'Y'}">
                    <tr valign="top">
                        <td class="formlabel"><fmt:message
                            key="test_rules_action_summary" bundle="${resword}" /></td>
                        <td class="formValue"><fmt:message key="test_rules_will_execute" bundle="${resword}" /></td>
                    </tr>
                </c:if>
                <c:if test="${actionFired == 'N'}">
                    <tr valign="top">
                        <td class="formlabel"><fmt:message
                            key="test_rules_action_summary" bundle="${resword}" /></td>
                        <td class="formValue"><fmt:message key="test_rules_will_not_execute" bundle="${resword}" /></td>
                    </tr>
                </c:if>

    </table>
    <table border="0" cellpadding="0" cellspacing="0">

        <c:forEach items="${sessionScope.testRuleActions}"
            var="testRuleAction" varStatus="status">
            <c:if
                test="${testRuleAction.expressionEvaluatesTo == ruleEvaluatesTo && ruleEvaluatesTo !='' && action != 'validate' }">
                <c:forEach items="${testRuleAction.propertiesForDisplay}" var="mapEntry">
                    <tr valign="top">
                        <td class="formlabel">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
                        <td style="color: #789EC5;"></td>
                        <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
                        <td><i><fmt:message
                            key="${mapEntry.key}" bundle="${resword}" /></i></td>
                        <td>${mapEntry.value}</td>
                    </tr>
                </c:forEach>
                
                <c:if test="${testRuleAction.actionType.code!=1 && testRuleAction.actionType.code !=2 && testRuleAction.actionType.code !=7 && fn:length(testRuleAction.properties)>0}">
                <c:set var="props" value=""/>
                <c:forEach items="${testRuleAction.properties}" var="prop" varStatus="status">
                    <c:set var="props"><c:out value="${props}"/> <c:out value="${prop.oid}"/>,</c:set>
                </c:forEach>
                <c:if test="${fn:length(props)>0}">
                    <tr valign="top">
                        <td class="formlabel">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
                        <td style="color: #789EC5;"></td>
                        <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
                        <td><i><fmt:message key="dest_prop_colon" bundle="${resword}" /></i>&nbsp;</td>
                        <td><c:out value="${fn:substring(props,0,fn:length(props)-1)}"/></td>
                    </tr>
                </c:if>
               </c:if>
                
                <tr valign="top">
                    <td>&nbsp;</td>
                </tr>
            </c:if>
        </c:forEach>

    </table>

    </c:when> 
    <c:otherwise>

        <tr valign="top">
            <td class="formlabel"><fmt:message
                key="test_rules_rule_validation" bundle="${resword}" /></td>
            <td class="formValue"><fmt:message
                key="test_rules_${ruleValidation}" bundle="${resword}" /> <c:if
                test="${ruleValidationFailMessage != null }"> - ${ruleValidationFailMessage}</c:if></td>
        </tr>

        <tr valign="top">
            <td>&nbsp;</td>
        </tr>

        <tr valign="top">
            <td class="formlabel"><fmt:message
                key="test_rules_expression_evaluates_to" bundle="${resword}" /></td>
            <td class="formValue">${ruleEvaluatesTo}</td>
        </tr>

        <tr valign="top">
            <td>&nbsp;</td>
        </tr>

        <tr valign="top">
            <td class="formlabel"><fmt:message
                key="test_rules_actions_fired" bundle="${resword}" /></td>
            <td class="formValue"><fmt:message key="test_rules_validate_message" bundle="${resword}" /></td>
        </tr>

        <tr valign="top">
            <td>&nbsp;</td>
        </tr>

        <tr valign="top">
            <td class="formlabel"><fmt:message key="test_rules_ran_in"
                bundle="${resword}" /></td>
            <td class="formValue">${duration}</td>
        </tr>


        <tr valign="top">
            <td>&nbsp;</td>
        </tr>

        <c:if test="${actionFired == 'Y'}">
            <tr valign="top">
                <td class="formlabel"><fmt:message
                    key="test_rules_action_summary" bundle="${resword}" /></td>
                <td class="formValue"></td>
            </tr>
        </c:if>

        <tr valign="top">
            <td>&nbsp;</td>
        </tr>

    </table>
    </c:otherwise> 
    </c:choose></div>
    </div>
    </div>
    </div>
    </div>
    </div>
    </div>
    </div>
    </div>

    </div>
</c:if>



<table border="0" cellpadding="0" cellspacing="0">
    <tr>
        <td><input type="submit" name="Submit"
            value="<fmt:message key="test_rules_validate_test" bundle="${resword}"/>"
            class="button_long"></td>
    </tr>

</table>
</form>
<br>
<p><a href="ViewRuleAssignment?restore=true"/><fmt:message key="test_rules_back_to_rule_assignments" bundle="${resword}"/></a></p>
<br>

<c:choose>
    <c:when test="${userBean.sysAdmin && module=='admin'}">
        <c:import url="../include/workflow.jsp">
            <c:param name="module" value="admin" />
        </c:import>
    </c:when>
    <c:otherwise>
        <c:import url="../include/workflow.jsp">
            <c:param name="module" value="manage" />
        </c:import>
    </c:otherwise>
</c:choose>
<jsp:include page="../include/footer.jsp" />
