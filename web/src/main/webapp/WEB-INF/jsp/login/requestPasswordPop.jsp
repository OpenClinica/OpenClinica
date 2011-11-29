<%@page import="org.akaza.openclinica.service.user.LdapUserService"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%
ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
LdapUserService ldapUserService = (LdapUserService) ctx.getBean(LdapUserService.class);
%>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<!-- Main Content Area -->
<div align="justify" style="width: 630px; height: 330px;; background:#FFFFFF; padding:5px 8px 0px 8px">
<h1><fmt:message key="request_password_form" bundle="${resword}"/></h1>
<p><fmt:message key="you_must_be_an_openClinica_member_to_receive_a_password" bundle="${resword}"/></p>
<p><a href="<%= ldapUserService.getPasswordRecoveryURL() %>" target="_blank"><fmt:message key="login.requestPassword.retrieveLdapPassword" bundle="${resword}"/></a></p>
<p><fmt:message key="all_fields_are_required" bundle="${resword}"/></p>

<form action="${pageContext.request.contextPath}/RequestPassword" method="post">
<input type="hidden" name="action" value="confirm">
<!-- These DIVs define shaded box borders -->
<div>
 <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0" cellspacing="0">
  <tr><td class="formlabel"><fmt:message key="user_name" bundle="${resword}"/>:</td>
      <td><div class="formfieldXL_BG"><input type="text" name="name" class="formfieldXL"></div></td>
  </tr>
  <tr valign="top">
      <td class="formlabel"><fmt:message key="email" bundle="${resword}"/>:</td>
      <td><div class="formfieldXL_BG"><input type="text" name="email" class="formfieldXL"></div></td>
  </tr>
  <tr valign="top">
      <td class="formlabel"><fmt:message key="password_challenge_question" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
        <select name="passwdChallengeQuestion" class="formfieldXL">
            <option><fmt:message key="favourite_pet" bundle="${resword}"/></option>
            <option><fmt:message key="city_of_birth" bundle="${resword}"/></option>
            <option><fmt:message key="mother_maiden_name" bundle="${resword}"/></option>
            <option><fmt:message key="favorite_color" bundle="${resword}"/></option>
        </select></div></td>
  </tr>
  <tr valign="top"><td class="formlabel"><fmt:message key="password_challenge_answer" bundle="${resword}"/>:</td>
        <td><div class="formfieldXL_BG"><input type="text" name="passwdChallengeAnswer" class="formfieldXL"></div></td>
  </tr>
</table>
</div>
</div></div></div></div></div></div></div></div>
</div>
<table border="0" cellpadding="0">
 <tr><td>
 <input type="submit" name="Submit" value="<fmt:message key="submit_password_request" bundle="${resword}"/>" class="button_xlong">
 </td>
 <td><input type="button" id="cancel" name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>" class="button"/></td>
 </tr>
 </table>
</form>
</div>
