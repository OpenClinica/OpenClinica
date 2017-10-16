<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<jsp:include page="../include/admin-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<script type="text/javascript">
    function sendUrl() {
       document.getElementById('changeRoles').value = 'true';
       document.forms[1].submit();
    }
</script>
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
<jsp:useBean scope="request" id="roles" class="java.util.LinkedHashMap"/>
<jsp:useBean scope="request" id="studies" class="java.util.ArrayList"/>
<h1><span class="title_manage"><fmt:message key="set_user_role" bundle="${resword}"/></span></h1>

<p><fmt:message key="choose_a_study_from_the_following_study" bundle="${resword}"/></p>
<form action="SetUserRole" method="post">
<input type="hidden" name="action" value="submit">
<input type="hidden" name="userId" value="<c:out value="${user.id}"/>">
<input type="hidden" name="name" value="<c:out value="${user.name}"/>">
<input type="hidden" id="changeRoles" name="changeRoles">

<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr><td class="formlabel"><fmt:message key="first_name" bundle="${resword}"/>:</td><td><c:out value="${user.firstName}"/></td></tr>
  <tr><td class="formlabel"><fmt:message key="last_name" bundle="${resword}"/>:</td><td><c:out value="${user.lastName}"/></td></tr>
  <tr><td class="formlabel"><fmt:message key="study_name" bundle="${resword}"/>:</td>
    <td><div class="formfieldXL_BG">
        <select name="studyId" class="formfieldXL" onchange="sendUrl();">
         <c:forEach var="userStudy" items="${studies}">
           <c:choose>
           <c:when test="${userStudy.parentStudyId > 0}">
                <c:choose>
                <c:when test="${studyId==userStudy.id}">
                   <option value="<c:out value="${userStudy.id}"/>" selected>&nbsp;&nbsp;&nbsp;&nbsp;<c:out value="${userStudy.name}"/>
                </c:when>
                <c:otherwise>
                   <option value="<c:out value="${userStudy.id}"/>">&nbsp;&nbsp;&nbsp;&nbsp;<c:out value="${userStudy.name}"/>
                </c:otherwise>
                </c:choose>
           </c:when>
           <c:otherwise>
                <c:choose>
                   <c:when test="${studyId==userStudy.id}">
                       <option value="<c:out value="${userStudy.id}"/>" selected><c:out value="${userStudy.name}"/>
                   </c:when>
                   <c:otherwise>
                       <option value="<c:out value="${userStudy.id}"/>"><c:out value="${userStudy.name}"/>
                   </c:otherwise>
                </c:choose>
           </c:otherwise>
           </c:choose>
         </c:forEach>
       </select>
       </div>
      </td>
   </tr>
  <tr><td class="formlabel"><fmt:message key="study_user_role" bundle="${resword}"/>:</td>
  <td><div class="formfieldXL_BG">
       <c:set var="role1" value="${uRole.role}"/>
       <select name="roleId" class="formfieldXL">
           <c:forEach var="currRole" items="${roles}">
               <c:choose>
                   <c:when test="${role1.id == currRole.key}">
                       <option value='<c:out value="${currRole.key}" />' selected><c:out value="${currRole.value}" /></option>
                   </c:when>
                   <c:otherwise>
                       <option value='<c:out value="${currRole.key}" />'><c:out value="${currRole.value}" /></option>
                   </c:otherwise>
               </c:choose>
           </c:forEach>
       </select>
       </div>
      </td>
  </tr>

</table>
</div>
</div></div></div></div></div></div></div></div>

</div>
<input type="submit" name="Submit" value="<fmt:message key="submit" bundle="${resword}"/>" class="button_medium">
<input type="button" onclick="confirmCancel('ListUserAccounts');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/>
</form>

<c:import url="../include/workflow.jsp">
 <c:param name="module" value="admin"/>
</c:import>
<jsp:include page="../include/footer.jsp"/>
