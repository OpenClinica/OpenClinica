<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.page_messages" var="pagemessage"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<jsp:include page="include/managestudy_top_pages.jsp"/>


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
      <p>
          <fmt:message key="study_module_description_2" bundle="${pagemessage}"/>
      </p>
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
  </div>
  &nbsp;&nbsp;&nbsp;
  <table width="78%" class="contenttable" cellspacing="0" cellpadding="2">
      <thead>
        <td width="20"></td>
        <td width="200"><b>Task</b></td>
        <td width="120"><b>Status</b></td>
        <td width="70"><b>Count</b></td>
        <td width="85"><b>Mark Complete</b></td>
        <td ><b>Actions</b></td>
      </thead>
      <tbody>
        <tr>
            <td>1</td>
            <c:url var="studyUrl" value="/CreateStudy"/>
            <td>Create Study</td>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.study == 3}">
                        Completed
                    </c:when>
                    <c:otherwise>
                        In Progress
                    </c:otherwise>
                </c:choose>
            </td>
            <td>n/a</td>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.study == 3}">
                        <a href='javascript:void(0)' onclick="prompt('study')"><img src="../images/icon_DEcomplete.gif" title="<fmt:message key="complete" bundle="${resword}"/>" border="0"/></a>
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
				<a href="${viewStudy}"><img src="../images/bt_Details.gif" title="<fmt:message key="view" bundle="${resword}"/>" border="0"/></a>
                <a href="${updateStudy}"><img src="../images/bt_Edit.gif" title="<fmt:message key="edit" bundle="${resword}"/>" border="0"/></a>
            </td>
        </tr>
        <tr>
            <td>2</td>
            <c:url var="crfUrl" value="/CreateCRFVersion"/>
            <td>Create CRF</td>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.crf == 1}">
                        Not Started
                    </c:when>
                    <c:when test="${studyModuleStatus.crf == 2}">
                        In Progress
                    </c:when>
                    <c:otherwise>
                        Completed
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <c:out value="${crfCount}"/>
            </td>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.crf == 3}">
                        <a href='javascript:void(0)' onclick="prompt('crf')"><img src="../images/icon_DEcomplete.gif" title="<fmt:message key="complete" bundle="${resword}"/>" border="0"/></a>
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
                        <a href="${crfCreateUrl}"><img src="../images/create_new.gif" title="<fmt:message key="new" bundle="${resword}"/>" border="0"/></a>
                    </c:when>
                    <c:when test="${studyModuleStatus.crf == 2}">
						<a href="${crfListUrl}"><img src="../images/bt_Details.gif" title="<fmt:message key="view" bundle="${resword}"/>" border="0"/></a>
                        <a href="${crfCreateUrl}"><img src="../images/create_new.gif" title="<fmt:message key="new" bundle="${resword}"/>" border="0"/></a>
                        <%-- <a href="${crfListUrl}"><img src="../images/bt_Edit.gif" border="0"/></a> --%>
                        
                    </c:when>
                    <c:otherwise>
						<a href="${crfListUrl}"><img src="../images/bt_Details.gif" title="<fmt:message key="view" bundle="${resword}"/>" border="0"/></a>
                        <a href="${crfCreateUrl}"><img src="../images/create_new.gif" title="<fmt:message key="new" bundle="${resword}"/>" border="0"/></a>
                        <%-- <a href="${crfListUrl}"><img src="../images/bt_Edit.gif" border="0"/></a> --%>
                        
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
        <tr>
            <td>
                3
            </td>
            <td>Create Event Definitions</td>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.eventDefinition == 1}">
                        Not Started
                    </c:when>
                    <c:when test="${studyModuleStatus.eventDefinition == 2}">
                        In Progress
                    </c:when>
                    <c:otherwise>
                        Completed
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <c:out value="${eventDefinitionCount}"/>
            </td>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.eventDefinition == 3}">
                        <a href='javascript:void(0)' onclick="prompt('eventDefinition')"><img src="../images/icon_DEcomplete.gif" title="<fmt:message key="complete" bundle="${resword}"/>" border="0"/></a>
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
                        <a href="${eventUrl}"><img src="../images/create_new.gif" title="<fmt:message key="new" bundle="${resword}"/>" border="0"/></a>
                    </c:when>
                    <c:when test="${studyModuleStatus.eventDefinition == 2}">
						<a href="${edListUrl}"><img src="../images/bt_Details.gif" title="<fmt:message key="view" bundle="${resword}"/>" border="0"/></a>
                        <a href="${eventUrl}"><img src="../images/create_new.gif" title="<fmt:message key="new" bundle="${resword}"/>" border="0"/></a>
                        <%-- <a href="${edListUrl}"><img src="../images/bt_Edit.gif" border="0"/></a> --%>
                        
                    </c:when>
                    <c:otherwise>
						<a href="${edListUrl}"><img src="../images/bt_Details.gif" title="<fmt:message key="view" bundle="${resword}"/>" border="0"/></a>
                        <a href="${eventUrl}"><img src="../images/create_new.gif" title="<fmt:message key="new" bundle="${resword}"/>" border="0"/></a>
                        <%-- <a href="${edListUrl}"><img src="../images/bt_Edit.gif" border="0"/></a> --%>
                        
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
        <tr>
            <td>
                4
            </td>
            <td>Create Subject Group Classes</td>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.subjectGroup == 1}">
                        Not Started
                    </c:when>
                    <c:when test="${studyModuleStatus.subjectGroup == 2}">
                        In Progress
                    </c:when>
                    <c:otherwise>
                        Completed
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <c:out value="${subjectGroupCount}"/>
            </td>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.subjectGroup == 3}">
                        <a href='javascript:void(0)' onclick="prompt('subjectGroup')"><img src="../images/icon_DEcomplete.gif" title="<fmt:message key="complete" bundle="${resword}"/>" border="0"/></a>
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
                        <a href="${createSubGroupUrl}"><img src="../images/create_new.gif" title="<fmt:message key="new" bundle="${resword}"/>" border="0"/></a>
                    </c:when>
                    <c:when test="${studyModuleStatus.subjectGroup == 2}">
						<a href="${listSubGroupUrl}"><img src="../images/bt_Details.gif" title="<fmt:message key="view" bundle="${resword}"/>" border="0"/></a>
                        <a href="${createSubGroupUrl}"><img src="../images/create_new.gif" title="<fmt:message key="new" bundle="${resword}"/>" border="0"/></a>
                        <%-- <a href="${listSubGroupUrl}"><img src="../images/bt_Edit.gif" border="0"/></a> --%>
                        
                    </c:when>
                    <c:otherwise>
						<a href="${listSubGroupUrl}"><img src="../images/bt_Details.gif" title="<fmt:message key="view" bundle="${resword}"/>" border="0"/></a>
                        <a href="${createSubGroupUrl}"><img src="../images/create_new.gif" title="<fmt:message key="new" bundle="${resword}"/>" border="0"/></a>
                        <%-- <a href="${listSubGroupUrl}"><img src="../images/bt_Edit.gif" border="0"/></a> --%>
                        
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
        <tr>
            <td>5</td>
            <td>Create Rules</td>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.rule == 1}">
                        Not Started
                    </c:when>
                    <c:when test="${studyModuleStatus.rule == 2}">
                        In Progress
                    </c:when>
                    <c:otherwise>
                        Completed
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <c:out value="${ruleCount}"/>
            </td>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.rule == 3}">
                        <a href='javascript:void(0)' onclick="prompt('rule')"><img src="../images/icon_DEcomplete.gif" title="<fmt:message key="complete" bundle="${resword}"/>" border="0"/></a>
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

                <c:choose>
                    <c:when test="${studyModuleStatus.rule == 1}">
                        <a href="${createRule}"><img src="../images/create_new.gif" title="<fmt:message key="new" bundle="${resword}"/>" border="0"/></a>
                    </c:when>
                    <c:when test="${studyModuleStatus.rule == 2}">
						<a href="${viewRule}"><img src="../images/bt_Details.gif" title="<fmt:message key="view" bundle="${resword}"/>" border="0"/></a>
                        <a href="${createRule}"><img src="../images/create_new.gif" title="<fmt:message key="new" bundle="${resword}"/>" border="0"/></a>
                        <%-- <a href="${viewRule}"><img src="../images/bt_Edit.gif" border="0"/></a> --%>
                        
                    </c:when>
                    <c:otherwise>
					    <a href="${viewRule}"><img src="../images/bt_Details.gif" title="<fmt:message key="view" bundle="${resword}"/>" border="0"/></a>
                        <a href="${createRule}"><img src="../images/create_new.gif" title="<fmt:message key="new" bundle="${resword}"/>" border="0"/></a>
                        <%-- <a href="${viewRule}"><img src="../images/bt_Edit.gif" border="0"/></a> --%>
                 
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
      </tbody>
  </table>
  <br>
  <br>
  <c:if test="${studyModuleStatus.study == 3 && studyModuleStatus.crf == 3 && studyModuleStatus.eventDefinition == 3 && studyModuleStatus.subjectGroup == 3 && studyModuleStatus.rule == 3}">
  <table width="78%" class="contenttable" cellspacing="0" cellpadding="2">
      <thead>
        <td width="20"></td>
        <td width="200"><b>Task</b></td>
        <td width="120"><b>Status</b></td>
        <td width="70"><b>Count</b></td>
        <td width="85"><b>Mark Complete</b></td>
        <td><b>Actions</b></td>
      </thead>
      <tbody>
        <tr>
            <td>6</td>
            <td>Create Sites</td>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.site == 1}">
                        Not Started
                    </c:when>
                    <c:when test="${studyModuleStatus.site == 2}">
                        In Progress
                    </c:when>
                    <c:otherwise>
                        Completed
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <c:out value="${siteCount}"/>
            </td>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.site == 3}">
                        <a href='javascript:void(0)' onclick="prompt('site')"><img src="../images/icon_DEcomplete.gif" title="<fmt:message key="complete" bundle="${resword}"/>" border="0"/></a>
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
                        <a href="${subGroupUrl}"><img src="../images/create_new.gif" title="<fmt:message key="new" bundle="${resword}"/>" border="0"/></a>
                    </c:when>
                    <c:when test="${studyModuleStatus.site == 2}">
						<a href="${siteList}"><img src="../images/bt_Details.gif" title="<fmt:message key="view" bundle="${resword}"/>" border="0"/></a>
                        <a href="${subGroupUrl}"><img src="../images/create_new.gif" title="<fmt:message key="new" bundle="${resword}"/>" border="0"/></a>
                        <%-- <a href="${siteList}"><img src="../images/bt_Edit.gif" border="0"/></a> --%>
                        
                    </c:when>
                    <c:otherwise>
						<a href="${siteList}"><img src="../images/bt_Details.gif" title="<fmt:message key="view" bundle="${resword}"/>" border="0"/></a>
                        <a href="${subGroupUrl}"><img src="../images/create_new.gif" title="<fmt:message key="new" bundle="${resword}"/>" border="0"/></a>
                        <%-- <a href="${siteList}"><img src="../images/bt_Edit.gif" border="0"/></a> --%>
                        
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
        <td width="20"></td>
        <td width="200"><b>Task</b></td>
        <td width="120"><b>Status</b></td>
        <td width="70"><b>Count</b></td>
        <td width="85"><b>Mark Complete</b></td>
        <td><b>Actions</b></td>
      </thead>
      <tbody>
        <tr>
            <td>7</td>
            <td>Assign Users</td>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.users == 1}">
                        Not Started
                    </c:when>
                    <c:when test="${studyModuleStatus.users == 2}">
                        In Progress
                    </c:when>
                    <c:otherwise>
                        Completed
                    </c:otherwise>
                </c:choose>

            </td>
            <td>
                <fmt:message key="total" bundle="${resword}"/> : <c:out value="${userCount}"/><br><br>
                <%-- changed tbh, 09/05/2009 --%>
                <%-- <c:forEach var="childStudy" items="${childStudyUserCount}">
                    <c:out value="${childStudy.key}"></c:out> :
                    <c:out value="${childStudy.value}"></c:out><br><br>
                </c:forEach> --%>
            </td>
            <td>
                <c:choose>
                    <c:when test="${studyModuleStatus.users == 3}">
                        <a href='javascript:void(0)' onclick="prompt('users')"><img src="../images/icon_DEcomplete.gif" title="<fmt:message key="complete" bundle="${resword}"/>" border="0"/></a>
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
                        <a href="${assignUrl}"><img src="../images/create_new.gif" title="<fmt:message key="new" bundle="${resword}"/>" border="0"/></a>
                    </c:when>
                    <c:when test="${studyModuleStatus.users == 2}">
						<a href="${listStudyUser}"><img src="../images/bt_Details.gif" title="<fmt:message key="view" bundle="${resword}"/>" border="0"/></a>
                        <a href="${assignUrl}"><img src="../images/create_new.gif" title="<fmt:message key="new" bundle="${resword}"/>" border="0"/></a>
                        <%-- <a href="${listStudyUser}"><img src="../images/bt_Edit.gif" border="0"/></a> --%>
                        
                    </c:when>
                    <c:otherwise>
						<a href="${listStudyUser}"><img src="../images/bt_Details.gif" title="<fmt:message key="view" bundle="${resword}"/>" border="0"/></a>
                        <a href="${assignUrl}"><img src="../images/create_new.gif" title="<fmt:message key="new" bundle="${resword}"/>" border="0"/></a>
                        <%-- <a href="${listStudyUser}"><img src="../images/bt_Edit.gif" border="0"/></a> --%>
                        
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>

      </tbody>
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
        <td width="475"><b>Site</b></td>
        <td><b>Count of Users</b></td>
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
  </c:if>
  <div>
      <input type="submit" name="submitEvent" value="Save" class="button_long">
      <input type="button" onclick="confirmCancel('${pageContext.request.contextPath}/MainMenu');" name="cancel" value="Cancel" class="button_long">
  </div>
</form>
<jsp:include page="include/footer.jsp"/>
