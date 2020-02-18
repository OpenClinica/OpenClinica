<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>


<jsp:include page="../include/home-header.jsp"/>


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

<jsp:useBean scope="request" id="studies" class="java.util.ArrayList"/>
<jsp:useBean scope="request" id="roles" class="java.util.ArrayList"/>
<jsp:useBean scope="request" id="newRole" class="org.akaza.openclinica.bean.login.StudyUserRoleBean"/>
<h1><span class="title_manage"><fmt:message key="request_study_access" bundle="${resword}"/></span></h1>
<P><fmt:message key="fill_out_form_to_request_study" bundle="${restext}"/><p>

<form action="RequestStudy" method="post">
<fmt:message key="field_required" bundle="${resword}"/><br>
<input type="hidden" name="action" value="confirm">
<!-- These DIVs define shaded box borders -->
<div style="width: 600px">
 <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0">
  <tr><td class="formlabel"><fmt:message key="first_name" bundle="${resword}"/>:</td>
  <td><div class="formfieldXL_BG">
   &nbsp;<c:out value="${userBean.firstName}"/></div></td>
  </tr>
  <tr><td class="formlabel"><fmt:message key="last_name" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">&nbsp;<c:out value="${userBean.lastName}"/>
  </div></td></tr>
  <tr><td class="formlabel"><fmt:message key="email" bundle="${resword}"/>:</td>
  <td><div class="formfieldXL_BG">&nbsp;<c:out value="${userBean.email}"/></div>
  </td></tr>

  <tr><td class="formlabel"><fmt:message key="study_requested" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
   <c:set var="studyId1" value="${newRole.studyId}"/>
    <select name="studyId" class="formfieldXL">
       <c:forEach var="study" items="${studies}">
        <c:choose>
         <c:when test="${studyId1 == study.id}">
          <option value="<c:out value="${study.id}"/>" selected><c:out value="${study.name}"/>
         </c:when>
         <c:otherwise>
          <option value="<c:out value="${study.id}"/>"><c:out value="${study.name}"/>
         </c:otherwise>
        </c:choose>
     </c:forEach>
    </select></div><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="studyId"/></jsp:include>
  </td></tr>

  <tr><td class="formlabel"><fmt:message key="role_of_access_requested" bundle="${resword}"/>:</td><td>
   <c:set var="role1" value="${newRole.role}"/>
   <div class="formfieldXL_BG">
   <select name="studyRoleId" class="formfieldXL">
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
   </select></div><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="studyRoleId"/></jsp:include>

  </td></tr>


</table>
</div>

</div></div></div></div></div></div></div></div>

</div>
<input type="submit" name="Submit" value="<fmt:message key="confirm_study_access" bundle="${resword}"/>" class="button_xlong">
<input type="button" onclick="confirmCancel('MainMenu');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/>
</form>
<jsp:include page="../include/footer.jsp"/>
