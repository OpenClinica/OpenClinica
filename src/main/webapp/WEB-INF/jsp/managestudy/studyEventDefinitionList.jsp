<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>

<jsp:include page="../include/managestudy-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: all">
    <td class="sidebar_tab">

    <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray"></span></a>

    <fmt:message key="instructions" bundle="${restext}"/>

    <div class="sidebar_tab_content">
        <fmt:message key="study_can_have_many_event_with_more_CRF" bundle="${restext}"/>
        <br><br>
        <fmt:message key="click_up_down_to_order" bundle="${restext}"/>
         <br><br>
        <fmt:message key="event_also_locked_prevent" bundle="${restext}"/>
    </div>

    </td>

  </tr>
  <tr id="sidebar_Instructions_closed" style="display: none">
    <td class="sidebar_tab">

    <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray"></span></a>

    <fmt:message key="instructions" bundle="${restext}"/>

    </td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope='session' id='userBean' class='core.org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='table' class='core.org.akaza.openclinica.web.bean.EntityBeanTable'/>
<jsp:useBean scope="request" id="defSize" type="java.lang.Integer" />
<h1><span class="title_manage"><fmt:message key="manage_all_event_definitions_in_study" bundle="${restext}"/> <c:out value="${study.name}"/>
    </span></h1>

<div style="float:right;padding-right:6px;width:8%;clear:both">
   <a href="javascript:processPrintCRFRequest('rest/metadata/html/print/<c:out value="${study.oc_oid}"/>/*/*')"
   onMouseDown="javascript:setImage('bt_Print1','images/bt_Print_d.gif');"
   onMouseUp="javascript:setImage('bt_Print1','images/bt_Print.gif');"><span
   name="bt_Print1" class="icon icon-print" border="0" alt="<fmt:message key="print_all_available_crf" bundle="${resword}"/>" title="<fmt:message key="print_all_available_crf" bundle="${resword}"/>" align="left" hspace="6"></a>
   </div>
<div style="clear:both">

</div>
<%--<c:if test="${!study.status.locked}">
 
<div class="homebox_bullets"><a href="DefineStudyEvent"><fmt:message key="create_a_new_study_event_definition" bundle="${resworkflow}"/></a></div>

</c:if>--%>
<p></p>
<c:import url="../include/showTableForEventDefinition.jsp">
    <c:param name="rowURL" value="showStudyEventDefinitionRow.jsp" />
    <c:param name="groupNum" value="${0}"/>
</c:import>
<br><br>

<div class="homebox_bullets"><a href="pages/studymodule"><fmt:message key="go_back_build_study_page" bundle="${resword}"/></a></div>
<p>
<jsp:include page="../include/footer.jsp"/>
