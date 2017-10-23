<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>	
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>

<jsp:include page="../include/managestudy-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray" border="0" align="right" hspace="10"></span></a>

		<fmt:message key="instructions" bundle="${resword}"/>

		<div class="sidebar_tab_content">

		</div>

		</td>
	
	</tr>
	<tr id="sidebar_Instructions_closed" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray" border="0" align="right" hspace="10"></span></a>

		<fmt:message key="instructions" bundle="${resword}"/>

		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope="request" id="user" class="org.akaza.openclinica.bean.login.UserAccountBean"/>
<jsp:useBean scope="request" id="uRole" class="org.akaza.openclinica.bean.login.StudyUserRoleBean"/>
<jsp:useBean scope="request" id="roles" class="java.util.ArrayList"/>
<h1><span class="title_manage"><fmt:message key="set_user_role" bundle="${resword}"/></span></h1>

<form action="SetStudyUserRole" method="post">
<input type="hidden" name="action" value="submit">
<input type="hidden" name="name" value="<c:out value="${user.name}"/>">
<input type="hidden" name="studyId" value="<c:out value="${uRole.studyId}"/>">
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr><td class="formlabel"><fmt:message key="first_name" bundle="${resword}"/>:</td><td><c:out value="${user.firstName}"/></td></tr>
  <tr><td class="formlabel"><fmt:message key="last_name" bundle="${resword}"/>:</td><td><c:out value="${user.lastName}"/></td></tr>
  <tr><td class="formlabel"><fmt:message key="study_name" bundle="${resword}"/>:</td><td><c:out value="${uRole.studyName}"/></td></tr>
  <tr><td class="formlabel"><fmt:message key="study_user_role" bundle="${resword}"/>:</td>
 <td><div class="formfieldXL_BG">
       <c:set var="role1" value="${uRole.role}"/>   
       <select name="roleId" class="formfieldXL">
         <c:forEach var="userRole" items="${roles}">    
          <c:choose>
           <c:when test="${role1.id == userRole.id}">   
           <option value="<c:out value="${userRole.id}"/>" selected><c:out value="${userRole.description}"/>
           </c:when>
           <c:otherwise>
             <option value="<c:out value="${userRole.id}"/>"><c:out value="${userRole.description}"/>      
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
<input type="button" onclick="goBack();"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/>    
</form>
<br><br>

<jsp:include page="../include/footer.jsp"/>
