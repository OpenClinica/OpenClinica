<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>


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
<jsp:useBean scope="session" id="userBean11" class="org.akaza.openclinica.bean.login.UserAccountBean"/>
<h1><span class="title_manage"><fmt:message key="confirm_user_profile_updates" bundle="${resword}"/></span></h1>

<form action="UpdateProfile?action=submit" method="post">
<!-- These DIVs define shaded box borders -->
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0">

  <tr valign="top"><td class="table_header_column"><fmt:message key="first_name" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${userBean1.firstName}"/></td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="last_name" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${userBean1.lastName}"/></td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="email" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${userBean1.email}"/></td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="institutional_affiliation" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${userBean1.institutionalAffiliation}"/></td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="default_active_study" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${newActiveStudy.name}"/></td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="password_challenge_question" bundle="${resword}"/>:</td><td class="table_cell">
  <%--<c:set var="question1" value="Mother's Maiden Name"/>--%>
  <%--<c:choose>--%>
  <%--<c:when test="${userBean1.passwdChallengeQuestion == question1}">--%>
	  <%--<fmt:message key="mother_maiden_name" bundle="${resword}"/>--%>
  <%--</c:when>--%>
  <%--<c:when test="${userBean1.passwdChallengeQuestion == 'Favorite Pet'}">--%>
	  <%--<fmt:message key="favourite_pet" bundle="${resword}"/>--%>
  <%--</c:when>--%>
  <%--<c:otherwise>--%>
     <%--<fmt:message key="city_of_birth" bundle="${resword}"/>--%>
  <%--</c:otherwise>--%>
   <%--</c:choose>--%>
      <c:out value="${userBean1.passwdChallengeQuestion}"/>
  </td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="password_challenge_answer" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${userBean1.passwdChallengeAnswer}"/></td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="phone" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${userBean1.phone}"/></td></tr>

  </td></tr>
</table>
</div>

</div></div></div></div></div></div></div></div>

</div>
 <input type="submit" name="Submit" value="<fmt:message key="update_profile" bundle="${resword}"/>" class="button_long">
 <input type="button" onclick="confirmCancel('MainMenu');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/>
</form>
<jsp:include page="../include/footer.jsp"/>
