
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.page_messages" var="pagemessage"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<jsp:include page="include/managestudy_top_pages.jsp"/>
<script type="text/JavaScript" language="JavaScript" src="../includes/jmesa/jquery.min.js"></script>
<script type="text/javascript" language="JavaScript" src="../includes/jmesa/jquery.blockUI.js"></script>



<!-- move the alert message to the sidebar-->
<jsp:include page="include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="${pageContext.request.contextPath}/images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

        <b><fmt:message key="instructions" bundle="${restext}"/></b>

        <div class="sidebar_tab_content">

            <fmt:message key="study_module_instruction" bundle="${restext}"/>

        </div>

    </td>

</tr>
<tr id="sidebar_Instructions_closed" style="display: none">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="${pageContext.request.contextPath}/images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

        <b><fmt:message key="instructions" bundle="${restext}"/></b>

    </td>
</tr>
<jsp:include page="include/sideInfo.jsp"/>

<style type="text/css">
    .contenttable{
            border-left: 1px solid #ccc;
            border-top: 1px solid #ccc;

    }
    .contenttable tbody tr td, .contenttable thead td {
            border-right: 1px solid #ccc;
            border-bottom: 1px solid #ccc;
               border-left: 0px solid #ccc;
            border-top: 0px solid #ccc;

            vertical-align:top;
            text-align: left;
            padding: 4px;
        }
    .contenttable.hiddencontent td {
        border-left: 0px;
        border-top: 0px;
        border-bottom: 0px;
    }
</style>

<script type="text/javascript">
    function prompt(elementName){
        var bool = confirm(
                "<fmt:message key="study_module_uncheck" bundle="${pagemessage}"/>");
        if(bool){
            document.getElementById(elementName).value=2;
            document.forms[1].action='${pageContext.request.contextPath}/pages/studymodule';
            document.forms[1].method="POST";
            document.forms[1].submit();
        }
    }

</script>

 <c:if test="${moduleManager!= '' && moduleManager!= null}">

      <script type="text/javascript">
        jQuery(document).ready(function() {
            jQuery('#requestRandomizationAccess').click(function() {
                jQuery.blockUI({ message: jQuery('#requestRandomizationForm'), css:{left: "300px", top:"10px" } });
            });

            jQuery('#cancelRandomizationAccessRequest').click(function() {
                jQuery.unblockUI();
                $('#randomizationWarnings').empty();
            });
            // If there are warnings, we failed in a previous submission and should display the warnings on the popup window.
            var warnings = "${regMessages}";
            if (warnings.length > 0) {
            	jQuery.blockUI({ message: jQuery('#requestRandomizationForm'), css:{left: "300px", top:"10px" } });
            }
        });

        // Hide the popup window if the escape key is pressed
        jQuery(document).keyup(function(keyPressed) {
            if(keyPressed.keyCode === 27) {
                $('#randomizationWarnings').empty();
                jQuery.unblockUI();
            }
        });
    </script>
</c:if>


<c:if test="${portalURL!= '' && portalURL!= null}">
    <script type="text/javascript">
        jQuery(document).ready(function() {
            jQuery('#requestParticipateAccess').click(function() {
                jQuery.blockUI({ message: jQuery('#requestParticipateForm'), css:{left: "300px", top:"10px" } });
            });

            jQuery('#cancelParticipateAccessRequest').click(function() {
                jQuery.unblockUI();
                $('#participateWarnings').empty();
            });
            // If there are warnings, we failed in a previous submission and should display the warnings on the popup window.
            var warnings = "${regMessages}";
            if (warnings.length > 0) {
            	jQuery.blockUI({ message: jQuery('#requestParticipateForm'), css:{left: "300px", top:"10px" } });
            }
        });

        // Hide the popup window if the escape key is pressed
        jQuery(document).keyup(function(keyPressed) {
            if(keyPressed.keyCode === 27) {
                $('#participateWarnings').empty();
                jQuery.unblockUI();
            }
        });
    </script>
</c:if>


  <form action="studymodule" method="post">
  <div>
      <h1><span class="title_manage"><c:out value="${currentStudy.name}"/></span></h1>
  </div>
  <div style="border: 1px solid #ccc; width:70%; padding-left:5px">
      <p>
          <fmt:message key="study_module_description_1" bundle="${pagemessage}">
              <fmt:param value="<img src='../images/create_new.gif'/>"/>
              <fmt:param value="<img src='../images/create_new.gif'/>"/>
              <fmt:param value="<img src='../images/bt_Edit.gif'/>"/>
              <fmt:param value="<img src='../images/bt_Details.gif'/>"/>
          </fmt:message>
      </p>
      <%--<p>--%>
          <%--<fmt:message key="study_module_description_2" bundle="${pagemessage}"/>--%>
      <%--</p>--%>
      <p>
          <fmt:message key="study_module_description_3" bundle="${pagemessage}"/>
      </p>
      <p>
          <fmt:message key="study_module_description_4" bundle="${pagemessage}"/>
      </p>
  </div>
  &nbsp;&nbsp;&nbsp;
  <div style="border: 1px solid #ccc; width:70%; padding:5px 0px 5px 5px;">
      <fmt:message key="set_study_status" bundle="${resword}"/> &nbsp; <select name="studyStatus">
          <c:forEach var="status" items="${statusMap}">
           <c:choose>
            <c:when test="${currentStudy.status.id == status.id}">
             <option value="<c:out value="${status.id}"/>" selected="selected">
                 <c:if test="${status.id == 4}">
                     <fmt:message key="design" bundle="${resword}"/>
                 </c:if>
                 <c:if test="${status.id == 1}">
                     <fmt:message key="available" bundle="${resword}"/>
                 </c:if>
                 <c:if test="${status.id == 6}">
                     <fmt:message key="locked" bundle="${resword}"/>
                 </c:if>
                 <c:if test="${status.id == 9}">
                     <fmt:message key="frozen" bundle="${resword}"/>
                 </c:if>
            </c:when>
            <c:otherwise>
             <option value="<c:out value="${status.id}"/>">
                 <c:if test="${status.id == 4}">
                     <fmt:message key="design" bundle="${resword}"/>
                 </c:if>
                 <c:if test="${status.id == 1}">
                     <fmt:message key="available" bundle="${resword}"/>
                 </c:if>
                 <c:if test="${status.id == 6}">
                     <fmt:message key="locked" bundle="${resword}"/>
                 </c:if>
                 <c:if test="${status.id == 9}">
                     <fmt:message key="frozen" bundle="${resword}"/>
                 </c:if>
            </c:otherwise>
           </c:choose>
        </c:forEach>
      </select>
      &nbsp;
      <input type="submit" name="saveStudyStatus" value="Save Status" class="button_medium">
  </div>
  &nbsp;&nbsp;&nbsp;
  <table width="78%" class="contenttable" cellspacing="0" cellpadding="2">
      <thead>
        <td width="20">&nbsp;</td>
        <td width="200"><b><fmt:message key="task" bundle="${resword}"/></b></td>
        <td width="120"><b><fmt:message key="status" bundle="${resword}"/></b></td>
        <td width="70"><b><fmt:message key="count" bundle="${resword}"/></b></td>
        <td width="85"><b><fmt:message key="mark_complete" bundle="${resword}"/></b></td>
        <td ><b><fmt:message key="actions" bundle="${resword}"/></b></td>
      </thead>
      <tbody>
        <tr>
            <td>1</td>
            <c:url var="studyUrl" value="/CreateStudy"/>
            <td><fmt:message key="create_study" bundle="${resword}"/></td>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.study == 3}">
                        <fmt:message key="completed" bundle="${resword}"/>
                    </c:when>
                    <c:otherwise>
                        <fmt:message key="in_progress" bundle="${resword}"/>
                    </c:otherwise>
                </c:choose>
            </td>
            <td><fmt:message key="NA" bundle="${resword}"/></td>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.study == 3}">
                        <a href='javascript:void(0)' onclick="prompt('study')"><img src="../images/icon_DEcomplete.gif" border="0"/></a>
                        <input type="hidden" id="study" name="study" value=""/>
                    </c:when>
                    <c:otherwise>
                        <input type="checkbox" id="study" name="study" value="3"/>
                    </c:otherwise>
                </c:choose>
            </td>
            <c:url var="updateStudy" value="/UpdateStudyNew?id=${studyId}"/>
            <c:url var="viewStudy" value="/ViewStudy?id=${studyId}&viewFull=yes"/>
            <td>
                <a href="${viewStudy}"><img src="../images/bt_Details.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>"/></a>
                <a href="${updateStudy}"><img src="../images/bt_Edit.gif" border="0" alt="<fmt:message key="edit" bundle="${resword}"/>" title="<fmt:message key="edit" bundle="${resword}"/>"/></a>
            </td>
        </tr>
        <tr>
            <td>2</td>
            <c:url var="crfUrl" value="/CreateCRFVersion"/>
            <td><fmt:message key="create_CRF" bundle="${resword}"/></td>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.crf == 1}">
                        <fmt:message key="not_started" bundle="${resword}"/>
                    </c:when>
                    <c:when test="${studyModuleStatus.crf == 2}">
                        <fmt:message key="in_progress" bundle="${resword}"/>
                    </c:when>
                    <c:otherwise>
                        <fmt:message key="completed" bundle="${resword}"/>
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <c:out value="${crfCount}"/>
            </td>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.crf == 3}">
                        <a href='javascript:void(0)' onclick="prompt('crf')"><img src="../images/icon_DEcomplete.gif" border="0"/></a>
                        <input type="hidden" id="crf" name="crf" value=""/>
                    </c:when>
                    <c:otherwise>
                        <input type="checkbox" name="crf" value="3"/>
                    </c:otherwise>
                </c:choose>

            </td>
            <c:url var="crfListUrl" value="/ListCRF"/>
            <c:url var="crfCreateUrl" value="/CreateCRFVersion"/>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.crf == 1}">
                        <a href="${crfCreateUrl}"><img src="../images/create_new.gif" border="0" alt="<fmt:message key="add2" bundle="${resword}"/>" title="<fmt:message key="add2" bundle="${resword}"/>"/></a>
                    </c:when>
                    <c:when test="${studyModuleStatus.crf == 2}">
                        <a href="${crfListUrl}"><img src="../images/bt_Details.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>"/></a>
                        <a href="${crfCreateUrl}"><img src="../images/create_new.gif" border="0" alt="<fmt:message key="add2" bundle="${resword}"/>" title="<fmt:message key="add2" bundle="${resword}"/>"/></a>
                        <%-- <a href="${crfListUrl}"><img src="../images/bt_Edit.gif" border="0"/></a> --%>

                    </c:when>
                    <c:otherwise>
                        <a href="${crfListUrl}"><img src="../images/bt_Details.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>"/></a>
                        <a href="${crfCreateUrl}"><img src="../images/create_new.gif" border="0" alt="<fmt:message key="add2" bundle="${resword}"/>" title="<fmt:message key="add2" bundle="${resword}"/>"/></a>
                        <%-- <a href="${crfListUrl}"><img src="../images/bt_Edit.gif" border="0"/></a> --%>

                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
        <tr>
            <td>
                3
            </td>
            <td><fmt:message key="create_events_definitions" bundle="${resword}"/></td>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.eventDefinition == 1}">
                        <fmt:message key="not_started" bundle="${resword}"/>
                    </c:when>
                    <c:when test="${studyModuleStatus.eventDefinition == 2}">
                        <fmt:message key="in_progress" bundle="${resword}"/>
                    </c:when>
                    <c:otherwise>
                        <fmt:message key="completed" bundle="${resword}"/>
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <c:out value="${eventDefinitionCount}"/>
            </td>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.eventDefinition == 3}">
                        <a href='javascript:void(0)' onclick="prompt('eventDefinition')"><img src="../images/icon_DEcomplete.gif" border="0"/></a>
                        <input type="hidden" id="eventDefinition" name="eventDefinition" value=""/>
                    </c:when>
                    <c:otherwise>
                        <input type="checkbox" name="eventDefinition" value="3"/>
                    </c:otherwise>
                </c:choose>

            </td>
            <c:url var="eventUrl" value="/DefineStudyEvent"/>
            <c:url var="edListUrl" value="/ListEventDefinition"/>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.eventDefinition == 1}">
                        <a href="${eventUrl}"><img src="../images/create_new.gif" border="0" alt="<fmt:message key="add2" bundle="${resword}"/>" title="<fmt:message key="add2" bundle="${resword}"/>"/></a>
                    </c:when>
                    <c:when test="${studyModuleStatus.eventDefinition == 2}">
                        <a href="${edListUrl}"><img src="../images/bt_Details.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>"/></a>
                        <a href="${eventUrl}"><img src="../images/create_new.gif" border="0" alt="<fmt:message key="add2" bundle="${resword}"/>" title="<fmt:message key="add2" bundle="${resword}"/>"/></a>
                        <%-- <a href="${edListUrl}"><img src="../images/bt_Edit.gif" border="0"/></a> --%>

                    </c:when>
                    <c:otherwise>
                        <a href="${edListUrl}"><img src="../images/bt_Details.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>"/></a>
                        <a href="${eventUrl}"><img src="../images/create_new.gif" border="0" alt="<fmt:message key="add2" bundle="${resword}"/>" title="<fmt:message key="add2" bundle="${resword}"/>"/></a>
                        <%-- <a href="${edListUrl}"><img src="../images/bt_Edit.gif" border="0"/></a> --%>

                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
        <tr>
            <td>
                4
            </td>
            <td><fmt:message key="create_subject_group_classes" bundle="${resword}"/></td>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.subjectGroup == 1}">
                        <fmt:message key="not_started" bundle="${resword}"/>
                    </c:when>
                    <c:when test="${studyModuleStatus.subjectGroup == 2}">
                        <fmt:message key="in_progress" bundle="${resword}"/>
                    </c:when>
                    <c:otherwise>
                        <fmt:message key="completed" bundle="${resword}"/>
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <c:out value="${subjectGroupCount}"/>
            </td>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.subjectGroup == 3}">
                        <a href='javascript:void(0)' onclick="prompt('subjectGroup')"><img src="../images/icon_DEcomplete.gif" border="0"/></a>
                        <input type="hidden" id="subjectGroup" name="subjectGroup" value=""/>
                    </c:when>
                    <c:otherwise>
                        <input type="checkbox" name="subjectGroup" value="3"/>
                    </c:otherwise>
                </c:choose>

            </td>
            <c:url var="createSubGroupUrl" value="/CreateSubjectGroupClass"/>
            <c:url var="listSubGroupUrl" value="/ListSubjectGroupClass"/>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.subjectGroup == 1}">
                        <a href="${createSubGroupUrl}"><img src="../images/create_new.gif" border="0" alt="<fmt:message key="add2" bundle="${resword}"/>" title="<fmt:message key="add2" bundle="${resword}"/>"/></a>
                    </c:when>
                    <c:when test="${studyModuleStatus.subjectGroup == 2}">
                        <a href="${listSubGroupUrl}"><img src="../images/bt_Details.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>"/></a>
                        <a href="${createSubGroupUrl}"><img src="../images/create_new.gif" border="0" alt="<fmt:message key="add2" bundle="${resword}"/>" title="<fmt:message key="add2" bundle="${resword}"/>"/></a>
                        <%-- <a href="${listSubGroupUrl}"><img src="../images/bt_Edit.gif" border="0"/></a> --%>

                    </c:when>
                    <c:otherwise>
                        <a href="${listSubGroupUrl}"><img src="../images/bt_Details.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>"/></a>
                        <a href="${createSubGroupUrl}"><img src="../images/create_new.gif" border="0" alt="<fmt:message key="add2" bundle="${resword}"/>" title="<fmt:message key="add2" bundle="${resword}"/>"/></a>
                        <%-- <a href="${listSubGroupUrl}"><img src="../images/bt_Edit.gif" border="0"/></a> --%>

                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
        <tr>
            <td>5</td>
            <td><fmt:message key="create_rules" bundle="${resword}"/></td>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.rule == 1}">
                        <fmt:message key="not_started" bundle="${resword}"/>
                    </c:when>
                    <c:when test="${studyModuleStatus.rule == 2}">
                        <fmt:message key="in_progress" bundle="${resword}"/>
                    </c:when>
                    <c:otherwise>
                        <fmt:message key="completed" bundle="${resword}"/>
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <c:out value="${ruleCount}"/>
            </td>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.rule == 3}">
                        <a href='javascript:void(0)' onclick="prompt('rule')"><img src="../images/icon_DEcomplete.gif" border="0"/></a>
                        <input type="hidden" id="rule" name="rule" value=""/>
                    </c:when>
                    <c:otherwise>
                        <input type="checkbox" name="rule" value="3"/>
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <c:url var="createRule" value="/ImportRule"/>
                <c:url var="viewRule" value="/ViewRuleAssignment"/>
				<c:url var="ruleDesignerURL" value="${ruleDesignerURL}"/>

                <c:choose>
                    <c:when test="${studyModuleStatus.rule == 1}">
                        <a href="${createRule}"><img src="../images/create_new.gif" border="0" alt="<fmt:message key="add2" bundle="${resword}"/>" title="<fmt:message key="add2" bundle="${resword}"/>"/></a>
						<a href="${ruleDesignerURL}access?host=${hostPath}&app=${contextPath}&study_oid=${study.oid}&provider_user=${userBean.name}&path=${path}"><img src="../images/bt_EnterData.gif" border="0" alt="<fmt:message key="rule_designer" bundle="${resword}"/>" title="<fmt:message key="rule_designer" bundle="${resword}"/>"/></a>
                    </c:when>
                    <c:when test="${studyModuleStatus.rule == 2}">
                        <a href="${viewRule}"><img src="../images/bt_Details.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>"/></a>
                        <a href="${createRule}"><img src="../images/create_new.gif" border="0" alt="<fmt:message key="add2" bundle="${resword}"/>" title="<fmt:message key="add2" bundle="${resword}"/>"/></a>
						<a href="${ruleDesignerURL}access?host=${hostPath}&app=${contextPath}&study_oid=${study.oid}&provider_user=${userBean.name}&path=${path}"><img src="../images/bt_EnterData.gif" border="0" alt="<fmt:message key="rule_designer" bundle="${resword}"/>" title="<fmt:message key="rule_designer" bundle="${resword}"/>"/></a>
                        <%-- <a href="${viewRule}"><img src="../images/bt_Edit.gif" border="0"/></a> --%>

                    </c:when>
                    <c:otherwise>
                        <a href="${viewRule}"><img src="../images/bt_Details.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>"/></a>
                        <a href="${createRule}"><img src="../images/create_new.gif" border="0" alt="<fmt:message key="add2" bundle="${resword}"/>" title="<fmt:message key="add2" bundle="${resword}"/>"/></a>
						<a href="${ruleDesignerURL}access?host=${hostPath}&app=${contextPath}&study_oid=${study.oid}&provider_user=${userBean.name}&path=${path}"><img src="../images/bt_EnterData.gif" border="0" alt="<fmt:message key="rule_designer" bundle="${resword}"/>" title="<fmt:message key="rule_designer" bundle="${resword}"/>"/></a>
                        <%-- <a href="${viewRule}"><img src="../images/bt_Edit.gif" border="0"/></a> --%>

                    </c:otherwise>

                </c:choose>

            </td>
        </tr>
        <tr>
            <td>6</td>
            <td><fmt:message key="create_sites" bundle="${resword}"/></td>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.site == 1}">
                        <fmt:message key="not_started" bundle="${resword}"/>
                    </c:when>
                    <c:when test="${studyModuleStatus.site == 2}">
                        <fmt:message key="in_progress" bundle="${resword}"/>
                    </c:when>
                    <c:otherwise>
                        <fmt:message key="completed" bundle="${resword}"/>
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <c:out value="${siteCount}"/>
            </td>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.site == 3}">
                        <a href='javascript:void(0)' onclick="prompt('site')"><img src="../images/icon_DEcomplete.gif" border="0"/></a>
                        <input type="hidden" id="site" name="site" value=""/>
                    </c:when>
                    <c:otherwise>
                        <input type="checkbox" name="site" value="3"/>
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <c:url var="siteList" value="/ListSite"/>
                <c:url var="subGroupUrl" value="/CreateSubStudy"/>

                <c:choose>
                    <c:when test="${studyModuleStatus.site == 1}">
                        <a href="${subGroupUrl}"><img src="../images/create_new.gif" border="0" alt="<fmt:message key="add2" bundle="${resword}"/>" title="<fmt:message key="add2" bundle="${resword}"/>"/></a>
                    </c:when>
                    <c:when test="${studyModuleStatus.site == 2}">
                        <a href="${siteList}"><img src="../images/bt_Details.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>"/></a>
                        <a href="${subGroupUrl}"><img src="../images/create_new.gif" border="0" alt="<fmt:message key="add2" bundle="${resword}"/>" title="<fmt:message key="add2" bundle="${resword}"/>"/></a>
                        <%-- <a href="${siteList}"><img src="../images/bt_Edit.gif" border="0"/></a> --%>

                    </c:when>
                    <c:otherwise>
                        <a href="${siteList}"><img src="../images/bt_Details.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>"/></a>
                        <a href="${subGroupUrl}"><img src="../images/create_new.gif" border="0" alt="<fmt:message key="add2" bundle="${resword}"/>" title="<fmt:message key="add2" bundle="${resword}"/>"/></a>
                        <%-- <a href="${siteList}"><img src="../images/bt_Edit.gif" border="0"/></a> --%>

                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
        <tr>
            <td>7</td>
            <td><fmt:message key="assign_users" bundle="${resword}"/></td>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.users == 1}">
                        <fmt:message key="not_started" bundle="${resword}"/>
                    </c:when>
                    <c:when test="${studyModuleStatus.users == 2}">
                        <fmt:message key="in_progress" bundle="${resword}"/>
                    </c:when>
                    <c:otherwise>
                        <fmt:message key="completed" bundle="${resword}"/>
                    </c:otherwise>
                </c:choose>

            </td>
            <td>
                <fmt:message key="total" bundle="${resword}"/> : <c:out value="${userCount}"/>
                <%-- changed tbh, 09/05/2009 --%>
                <%-- <c:forEach var="childStudy" items="${childStudyUserCount}">
                    <c:out value="${childStudy.key}"></c:out> :
                    <c:out value="${childStudy.value}"></c:out><br><br>
                </c:forEach> --%>
            </td>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.users == 3}">
                        <a href='javascript:void(0)' onclick="prompt('users')"><img src="../images/icon_DEcomplete.gif" border="0"/></a>
                        <input type="hidden" id="users" name="users" value=""/>
                    </c:when>
                    <c:otherwise>
                        <input type="checkbox" name="users" value="3"/>
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <c:url var="assignUrl" value="/AssignUserToStudy"/>
                <c:url var="listStudyUser" value="/ListStudyUser"/>
                <c:choose>
                    <c:when test="${studyModuleStatus.users == 1}">
                        <a href="${assignUrl}"><img src="../images/create_new.gif" border="0" alt="<fmt:message key="add2" bundle="${resword}"/>" title="<fmt:message key="add2" bundle="${resword}"/>"/></a>
                    </c:when>
                    <c:when test="${studyModuleStatus.users == 2}">
                        <a href="${listStudyUser}"><img src="../images/bt_Details.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>"/></a>
                        <a href="${assignUrl}"><img src="../images/create_new.gif" border="0" alt="<fmt:message key="add2" bundle="${resword}"/>" title="<fmt:message key="add2" bundle="${resword}"/>"/></a>
                        <%-- <a href="${listStudyUser}"><img src="../images/bt_Edit.gif" border="0"/></a> --%>

                    </c:when>
                    <c:otherwise>
                        <a href="${listStudyUser}"><img src="../images/bt_Details.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>"/></a>
                        <a href="${assignUrl}"><img src="../images/create_new.gif" border="0" alt="<fmt:message key="add2" bundle="${resword}"/>" title="<fmt:message key="add2" bundle="${resword}"/>"/></a>
                        <%-- <a href="${listStudyUser}"><img src="../images/bt_Edit.gif" border="0"/></a> --%>

                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
      </tbody>
  </table>
  <br>
  <br>


      <table width="78%" class="contenttable" cellspacing="0" cellpadding="2">
          <thead>
          <tr>
              <td width="20">&nbsp;</td>
              <td width="200"><b><fmt:message key="modules" bundle="${resword}"/></b></td>
              <td width="120"><b><fmt:message key="status" bundle="${resword}"/></b></td>
              <td width="164"><b><fmt:message key="url" bundle="${resword}"/></b></td>
              <td><b><fmt:message key="actions" bundle="${resword}"/></b></td>
              </tr>
          </thead>

            <c:if test="${portalURL!= '' && portalURL!= null}">

          <tbody>
              <tr>
                  <td>&nbsp;</td>
                  <td><fmt:message key="participate" bundle="${resword}"/></td>
                  <td>
                      <c:choose>
                          <c:when test="${participateOCStatus == 'disabled'}"><span id="participateStatus" class="participate-inactive-status"><fmt:message key="participate_status_deactivated" bundle="${resword}"/></span></c:when>
                          <c:when test="${empty participateStatus}"><span id="participateStatus"><fmt:message key="participate_status_notfound" bundle="${resword}"/></span></c:when>
                          <c:when test="${participateStatus == 'PENDING'}"><span id="participateStatus"><fmt:message key="participate_status_pending" bundle="${resword}"/></span></c:when>
                          <c:when test="${participateStatus == 'ACTIVE'}"><span id="participateStatus" class="participate-active-status"><fmt:message key="participate_status_active" bundle="${resword}"/></span></c:when>
                          <c:when test="${participateStatus == 'INACTIVE'}"><span id="participateStatus" class="participate-inactive-status"><fmt:message key="participate_status_inactive" bundle="${resword}"/></span></c:when>
                      </c:choose>
                  </td>
                  <td>
                      <span id="participateURL">
                          <c:choose>
                              <c:when test="${!empty participateURLDisplay}">${participateURLDisplay}</c:when>
                              <c:otherwise>&nbsp;</c:otherwise>
                          </c:choose>
                      </span>
                  </td>
                  <td>
                      <c:url var="reactivateParticipate" value="studymodule/${currentStudy.oid}/reactivate"/>
                      <c:url var="deactivateParticipate" value="studymodule/${currentStudy.oid}/deactivate"/>
                      <c:choose>
                          <c:when test="${participateOCStatus == 'disabled' && !empty participateStatus}">
                              <a href="${reactivateParticipate}" id="reactivateParticipateAccess" name="reactivateParticipateAccess"><img src="../images/create_new.gif" border="0" alt="<fmt:message key="enable" bundle="${resword}"/>" title="<fmt:message key="enable" bundle="${resword}"/>"/></a>
                          </c:when>
                          <c:when test="${participateOCStatus == 'disabled'}">
                              <a href="javascript:;" id="requestParticipateAccess" name="requestParticipateAccess"><img src="../images/create_new.gif" border="0" alt="<fmt:message key="enable" bundle="${resword}"/>" title="<fmt:message key="enable" bundle="${resword}"/>"/></a>
                          </c:when>
                          <c:otherwise>
                              <a href="${deactivateParticipate}" id="removeParticipateAccess" name="removeParticipateAccess"><img src="../images/bt_Remove.gif" border="0" alt="<fmt:message key="disable" bundle="${resword}"/>" title="<fmt:message key="disable" bundle="${resword}"/>"/></a>
                          </c:otherwise>
                      </c:choose>
                  </td>
              </tr>
          </tbody>
          </c:if>


         <c:if test="${moduleManager!= '' && moduleManager!= null}">
          <tbody>
              <tr>
                  <td>&nbsp;</td>
                  <td><fmt:message key="randomization" bundle="${resword}"/></td>
                  <td>
                      <c:choose>
                          <c:when test="${randomizationOCStatus == 'disabled'}"><span id="randomizationStatus" class="randomization-inactive-status"><fmt:message key="randomization_status_deactivated" bundle="${resword}"/></span></c:when>
                          <c:when test="${empty randomizationStatus}"><span id="randomizationStatus" class="randomization-inactive-status"><fmt:message key="randomization_status_deactivated" bundle="${resword}"/></span></c:when>
                          <c:when test="${randomizationStatus == 'PENDING'}"><span id="randomizationStatus"><fmt:message key="randomization_status_pending" bundle="${resword}"/></span></c:when>
                          <c:when test="${randomizationStatus == 'ACTIVE'}"><span id="randomizationStatus" class="randomization-active-status"><fmt:message key="randomization_status_active" bundle="${resword}"/></span></c:when>
                          <c:when test="${randomizationStatus == 'INACTIVE'}"><span id="randomizationStatus" class="randomization-inactive-status"><fmt:message key="randomization_status_inactive" bundle="${resword}"/></span></c:when>
                      </c:choose>
                  </td>
                  <td>
                    <span id="randomizeURL">
                              <a href="<c:url value="${randomizeURL}"/>" target="_blank">${randomizeURL}</a>
                    </span>
                  </td>
                  <td>
                      <c:url var="reactivateRandomization" value="studymodule/${currentStudy.oid}/reactivaterandomization"/>
                      <c:url var="deactivateRandomization" value="studymodule/${currentStudy.oid}/deactivaterandomization"/>
                      <c:choose>
                          <c:when test="${randomizationOCStatus == 'disabled' && !empty randomizationStatus}">
                              <a href="${reactivateRandomization}" id="reactivateRandomizationAccess" name="reactivateRandomizationAccess"><img src="../images/create_new.gif" border="0" alt="<fmt:message key="enable" bundle="${resword}"/>" title="<fmt:message key="enable" bundle="${resword}"/>"/></a>
                          </c:when>
                          <c:when test="${randomizationOCStatus == 'disabled'}">
                              <a href="javascript:;" id="requestRandomizationAccess" name="requestRandomizationAccess"><img src="../images/create_new.gif" border="0" alt="<fmt:message key="enable" bundle="${resword}"/>" title="<fmt:message key="enable" bundle="${resword}"/>"/></a>
                          </c:when>
                          <c:when test="${randomizationOCStatus == 'enabled' && empty randomizationStatus}">
                              <a href="javascript:;" id="requestRandomizationAccess" name="requestRandomizationAccess"><img src="../images/create_new.gif" border="0" alt="<fmt:message key="enable" bundle="${resword}"/>" title="<fmt:message key="enable" bundle="${resword}"/>"/></a>
                          </c:when>
                          <c:otherwise>
                              <a href="${deactivateRandomization}" id="removeRandomizeAccess" name="removeRandomizeAccess"><img src="../images/bt_Remove.gif" border="0" alt="<fmt:message key="disable" bundle="${resword}"/>" title="<fmt:message key="disable" bundle="${resword}"/>"/></a>
                          </c:otherwise>
                      </c:choose>
                  </td>
              </tr>
          </tbody>
          </c:if>

      </table>
      <br>
      <br>

  <%-- additional table added tbh, 09/05/2009 --%>
  <c:if test="${!empty childStudyUserCount }">
  <table width="4%" style="border-left: 0px; border-top: 0px; border-bottom: 0px; border-right: 0px;" align="left" cellspacing="0" cellpadding="0">
    <tr><td>&nbsp;&nbsp;</td></tr>
    </table>
  <table width="74%" class="contenttable" cellspacing="0" cellpadding="2">
        <thead>
        <%-- <td width="20" style="border-left: 0px; border-top: 0px; border-bottom: 0px;"></td> --%>
        <td width="475"><b><fmt:message key="site" bundle="${resword}"/></b></td>
        <td><b><fmt:message key="count_of_users" bundle="${resword}"/></b></td>
        </thead>
        <tbody>
        <c:forEach var="childStudy" items="${childStudyUserCount}">
            <tr>
                <%-- <td style="border-left: 0px; border-top: 0px; border-bottom: 0px;"></td> --%>
                <td><c:out value="${childStudy.key}"></c:out></td>
                <td><c:out value="${childStudy.value}"></c:out></td>
            </tr>
        </c:forEach>
        </tbody>
   </table>
   </c:if>
  <%--</c:if>--%>
  <div>
      <input type="submit" name="submitEvent" value="<fmt:message key="save" bundle="${resword}"/>" class="button_long">
      <input type="button" onclick="confirmCancel('${pageContext.request.contextPath}/MainMenu');" name="cancel" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button_long">
  </div>
</form>

<c:if test="${portalURL!= '' && portalURL!= null}">
    <div align="left" id="requestParticipateForm" class="participate-registration-div">
        <form action="studymodule/${currentStudy.oid}/register" method="post">
            <h1>
                <fmt:message key="participate_reg_title" bundle="${resword}"/>
            </h1>
            <p class="participate-text"><fmt:message key="participate_reg_instructions_part1" bundle="${resword}"/></p>
            <p class="participate-text"><fmt:message key="participate_reg_instructions_part2" bundle="${resword}"/></p>
            <span class="participate-text"><c:out value="${participateURL.protocol}"/>:// </span>
            <input type="text" name="hostName" id="hostName"/>
            <span class="participate-text"> .<c:out value="${participateURL.host}"/><c:if test="${participateURL.port > 0}">:<c:out value="${participateURL.port}"/></c:if></span>
            <br>
            <c:if test="${!empty regMessages}">
                <div id="participateWarnings" class="participate-warnings">
                    <c:forEach var="message" items="${regMessages}">
                        <c:out value="${message}" escapeXml="false"/>
                        <br>
                    </c:forEach>
                </div>
            </c:if>
            <input type="submit" id="submitParticipateAccessRequest" class="button_medium" value="Request Access"/>
            <input type="button" id="cancelParticipateAccessRequest" class="button" value="Cancel"/>
        </form>
    </div>
</c:if>

 <c:if test="${moduleManager!= '' && moduleManager!= null}">
    <div align="left" id="requestRandomizationForm" class="randomization-registration-div">
        <form action="studymodule/${currentStudy.oid}/randomize" method="post">
            <h1>
                <fmt:message key="randomization_reg_title" bundle="${resword}"/>
            </h1>
            <p class="randomization-text"><fmt:message key="randomization_reg_instructions_part1" bundle="${resword}"/></p>
            <br>
            <input type="submit" id="submitRandomizationAccessRequest" class="button_medium" value="Request Access"/>
            <input type="button" id="cancelRandomizationAccessRequest" class="button" value="Cancel"/>
        </form>
    </div>
</c:if>


<jsp:include page="include/footer.jsp"/>
