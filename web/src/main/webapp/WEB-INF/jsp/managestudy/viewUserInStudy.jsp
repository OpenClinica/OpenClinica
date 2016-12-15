<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<jsp:include page="../include/managestudy-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<%-- BWP>> for formatting dates --%>
<c:set var="dateFormatPattern" value="${requestScope['dateFormatPattern']}" />
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

<jsp:useBean scope="request" id="user" class="org.akaza.openclinica.bean.login.UserAccountBean"/>
<jsp:useBean scope="request" id="uRole" class="org.akaza.openclinica.bean.login.StudyUserRoleBean"/>
<jsp:useBean scope="request" id="uStudy" class="org.akaza.openclinica.bean.managestudy.StudyBean"/>

<h1><span class="title_manage"><fmt:message key="view_user_account" bundle="${resword}"/></span></h1>
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr><td class="table_header_column"><fmt:message key="first_name" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${user.firstName}"/></td></tr>
  <tr><td class="table_header_column"><fmt:message key="last_name" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${user.lastName}"/></td></tr>
  <tr><td class="table_header_column"><fmt:message key="email" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${user.email}"/></td></tr>
  <tr><td class="table_header_column"><fmt:message key="institutional_affiliation" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${user.institutionalAffiliation}"/></td></tr>
  <tr><td class="table_header_column"><fmt:message key="user_type" bundle="${resword}"/>:</td>
  <td class="table_cell">
  <c:choose>
  <c:when test="${user.sysAdmin}"><fmt:message key="system_administrator" bundle="${resword}"/></c:when>
  <c:otherwise>
   <fmt:message key="user" bundle="${resword}"/>
  </c:otherwise>
  </c:choose>
  </td></tr>   
  <tr><td class="table_header_column"><fmt:message key="status" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${user.status.name}"/></td></tr>
  <tr><td class="table_header_column"><fmt:message key="date_created" bundle="${resword}"/>:</td><td class="table_cell"><fmt:formatDate value="${user.createdDate}" type="date" pattern="${dteFormat}"/></td></tr>
  <tr><td class="table_header_column"><fmt:message key="created_by" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${user.owner.name}"/></td></tr>
  <tr><td class="table_header_column"><fmt:message key="date_updated" bundle="${resword}"/>:</td><td class="table_cell"><fmt:formatDate value="${user.updatedDate}" type="date" pattern="${dteFormat}"/>&nbsp;</td></tr>
  <tr><td class="table_header_column"><fmt:message key="updated_by" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${user.updater.name}"/>&nbsp;</td></tr>
  <tr><td class="table_header_column"><fmt:message key="role" bundle="${resword}"/>:</td><td class="table_cell">
    <c:if test="${uStudy.parentStudyId > 0}">
              <fmt:message key="${siteRoleMap[uRole.role.id] }" bundle="${resterm}"></fmt:message>
          </c:if>
          <c:if test="${uStudy.parentStudyId == 0}">
              <c:out value="${uRole.role.description}"/>
          </c:if>
  </td></tr>
  <tr><td class="table_header_column"><fmt:message key="role_status" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${uRole.status.name}"/>&nbsp;</td></tr>
  <tr><td class="table_header_column"><fmt:message key="study" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${uStudy.name}"/></td></tr>
 
</table>

</div>
</div></div></div></div></div></div></div></div>

</div>
<br><p><a href="ListStudyUser"><fmt:message key="go_back_to_user_list" bundle="${resword}"/></a></p>
<br><br>


<c:import url="../include/workflow.jsp">
   <c:param name="module" value="manage"/> 
 </c:import>
 
<jsp:include page="../include/footer.jsp"/>