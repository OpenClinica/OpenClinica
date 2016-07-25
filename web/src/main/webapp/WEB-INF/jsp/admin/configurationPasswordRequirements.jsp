<%@ page contentType="text/html; charset=UTF-8"
         import="javax.servlet.http.HttpServletRequest,
                 java.util.Map" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<jsp:include page="../include/admin-header.jsp"/>


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

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='presetValues' class='java.util.HashMap' />

<h1><span class="title_manage"><fmt:message key="configure_password_requirements" bundle="${resword}"/></span></h1>

<form action="ConfigurePasswordRequirements" method="post">
<jsp:include page="../include/showSubmitted.jsp" />

<div style="width: 600px">



<!-- These DIVs define shaded box borders -->

	<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

		<div class="tablebox_center">


		<!-- Table Contents -->



<table border="0" cellpadding="2" cellspacing="2" width="100%">
  <tr>
    <td style="width: 200px" class="formlabel">
      <label for="pwd.chars.min"><fmt:message bundle="${resword}" key="pwd_reqs_min_length" /></label></td>
    <td >
      <div class="formfieldM_BG">
        <input class="formfieldM"
               id="pwd.chars.min"
               name="pwd.chars.min"
               type="text"
               value="${presetValues['pwd.chars.min']}" /></div>  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="pwd.chars.min" /></jsp:include></td>
    <td><fmt:message bundle="${resword}" key="pwd_reqs_min_length_note" />
  </td>
  </tr>
  <tr>
    <td class="formlabel"><label for="pwd.chars.max"><fmt:message bundle="${resword}" key="pwd_reqs_max_length" /></label></td>
    <td>
      <div class="formfieldM_BG">
        <input class="formfieldM"
               id="pwd.chars.max"
               name="pwd.chars.max"
               type="text"
               value="${presetValues['pwd.chars.max']}" /></div><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="pwd.chars.max" /></jsp:include></td>
    <td><fmt:message bundle="${resword}" key="pwd_reqs_max_length_note" /></td>
  </tr>
  <tr>
    <td class="formlabel">
      <div><fmt:message bundle="${resword}" key="pwd_reqs_must_contain" /></div>
    </td>
    <td>
      <div>
        <input type="checkbox"
               id="pwd.chars.case.lower"
               name="pwd.chars.case.lower"
               value="true"
               <c:if test="${presetValues['pwd.chars.case.lower']}">checked</c:if>
               />
        <label for="pwd.chars.case.lower"><fmt:message bundle="${resword}" key="pwd_reqs_lower_case" /></label>
      </div>
      <div>
        <input type="checkbox"
               id="pwd.chars.case.upper"
               name="pwd.chars.case.upper"
               value="true"
            
               <c:if test="${presetValues['pwd.chars.case.upper']}">checked</c:if>
               />
        <label for="pwd.chars.case.upper"><fmt:message bundle="${resword}" key="pwd_reqs_upper_case" /></label>
      </div>
      <div>
        <input type="checkbox"
               id="pwd.chars.digits"
               name="pwd.chars.digits"
               value="true"
                 <c:if test="${presetValues['pwd.chars.digits']}">checked</c:if>
                />
        <label for="pwd.chars.digits"><fmt:message bundle="${resword}" key="pwd_reqs_digits" /></label>
        </div>
      <div>
        <input type="checkbox"
               id="pwd.chars.specials"
               name="pwd.chars.specials"
               value="true"
               <c:if test="${presetValues['pwd.chars.specials']}">checked</c:if>
           />
        <label for="pwd.chars.specials"><fmt:message bundle="${resword}" key="pwd_reqs_special_chars" /></label>
      </div>
  </td>
  <td></td>
  </tr>
  <tr>
    <td class="formlabel"><label for="pwd.change.required"><fmt:message bundle="${resword}" key="pwd_reqs_change_required" /></label></td>

    <td>
      <input type="radio"
             id="pwd.change.required_yes"
             name="pwd.change.required"
             value="1"
              <c:if test="${presetValues['pwd.change.required'] == '1'  }">checked</c:if>
            />
      <label for="pwd.change.required_yes"><fmt:message key="yes" bundle="${resword}" /></label>
      <input type="radio"
             id="pwd.change.required_no"
             name="pwd.change.required"
             value="0"
             <c:if test="${presetValues['pwd.change.required'] eq null || presetValues['pwd.change.required'] != '1'}">checked</c:if>
             />
      <label for="pwd.change.required_no" ><fmt:message key="no"  bundle="${resword}" /></label>
  </td>
    <td><fmt:message bundle="${resword}" key="pwd_reqs_server_restart"/></td>
  </tr>

  <tr>
    <td class="formlabel"><label for="pwd.history.size"><fmt:message bundle="${resword}" key="pwd_reqs_expiration_days" /></label></td>
    <td>
      <div class="formfieldM_BG">
        <input class="formfieldM"
               id="pwd.expiration.days"
               name="pwd.expiration.days"
               type="text"
               value="${presetValues['pwd.expiration.days']}" /></div>
    <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="pwd.expiration.days" /></jsp:include></td>
    <td><fmt:message bundle="${resword}" key="pwd_reqs_expiration_days_note"/></td>
  </tr>

</table>

	</div>

	</div></div></div></div></div></div></div></div>

	</div>

<input type="submit" name="Submit" value="<fmt:message key="submit" bundle="${resword}"/>" class="button_medium">
<input type="button" onclick="confirmCancel('ListUserAccounts');"  name="cancel" value="   <fmt:message key="cancel" bundle="${resword}"/>   " class="button_medium"/>

</form>
<br>
<jsp:include page="../include/footer.jsp"/>
