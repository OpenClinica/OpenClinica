<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<jsp:include page="../login-include/login-header.jsp"/>
<jsp:include page="../login-include/resetpwd-sidebar.jsp"/>


<jsp:useBean scope="request" id="mustChangePass" class="java.lang.String"/>

<h1><span class="title_manage"><fmt:message key="reset_password" bundle="${resword}"/></span></h1>
<jsp:include page="../login-include/login-alertbox.jsp"/>

<form action="ResetPassword" method="post">
* <fmt:message key="indicates_required_field" bundle="${resword}"/><br>
<input type="hidden" name="mustChangePwd" value=<c:out value="${mustChangePass}"/> >
<!-- These DIVs define shaded box borders -->
<div style="width: 600px">
 <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0">
    <tr height="40"><td colspan="3">&nbsp;</td></tr>
    <tr><td class="formlabel"><fmt:message key="old_password" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG"><input type="password" name="oldPasswd" value="" class="formfieldXL"></div>
    <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="oldPasswd"/></jsp:include></td><td class="formlabel">*</td></tr>
    <tr><td class="formlabel"><fmt:message key="new_password" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG"><input type="password" name="passwd" value="" class="formfieldXL"></div>
  <c:choose>
    <c:when test="${mustChangePass=='yes'}">
        <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="passwd"/></jsp:include></td><td class="formlabel">*</td></tr>
    </c:when>
    <c:otherwise>
        (<fmt:message key="leave_new_password_blank" bundle="${resword}"/>)
        <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="passwd"/></jsp:include></td></tr>
    </c:otherwise>
  </c:choose>
  <tr><td class="formlabel"><fmt:message key="confirm_new_password" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG"><input type="password" name="passwd1" value="" class="formfieldXL"></div>
  <c:choose>
    <c:when test="${mustChangePass=='yes'}">
        <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="passwd1"/></jsp:include></td><td class="formlabel">*</td></tr>
    </c:when>
    <c:otherwise>
        <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="passwd1"/></jsp:include></td></tr>
    </c:otherwise>
  </c:choose>
</table>
</div>

</div></div></div></div></div></div></div></div>

</div>
<table border="0" cellpadding="0">
 <tr><td><input type="submit" name="submit" value="<fmt:message key="change_password" bundle="${resword}"/>" class="button_xlong"></td>
 <td><input type="button" name="exit" value="<fmt:message key="exit" bundle="${resword}"/>" class="button_medium" onclick="javascript:window.location.href='j_spring_security_logout'"></td>
 </tr>
 </table>
</form>
<jsp:include page="../login-include/login-footer.jsp"/>
