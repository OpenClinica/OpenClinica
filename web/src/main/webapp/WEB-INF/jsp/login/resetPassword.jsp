<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<jsp:include page="../login-include/login-header.jsp"/>
<jsp:include page="../login-include/resetpwd-sidebar.jsp"/>

<script type="text/JavaScript" language="JavaScript" src="<c:url value='/includes/jmesa/jquery.min.js'/>">
<script type="text/JavaScript" language="JavaScript" src="<c:url value='/includes/jmesa/jquery-migrate-1.1.1.js'/>"></script>
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
    <tr><td class="formlabel"><fmt:message key="old_password" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
    <input type="password" name="oldPasswd" value="" class="formfieldXL"></div>
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
    <c:if test="${mustChangePass=='yes'}">
        <tr><td class="formlabel"><fmt:message key="password_challenge_question" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
           
         <fmt:message key='favourite_pet' bundle='${resword}'  var = "favourite_pet" />
          <fmt:message key='city_of_birth' bundle='${resword}'  var = "city_of_birth" />
          <fmt:message key='mother_maiden_name' bundle='${resword}'  var = "mother_maiden_name" />
         <fmt:message key='favorite_color' bundle='${resword}'  var = "favorite_color" />  
              <select name="passwdChallengeQ" class="formfieldXL">
                <option value="" ><fmt:message key="Please_Select_One" bundle="${resword}"/></option>
            
 <option <c:if test="${userBean1.passwdChallengeQuestion eq favourite_pet }" > selected </c:if> ><fmt:message key="favourite_pet" bundle="${resword}"/></option>
            <option <c:if test="${ userBean1.passwdChallengeQuestion eq city_of_birth}" > selected </c:if>><fmt:message key="city_of_birth" bundle="${resword}"/></option>
            <option <c:if test="${ userBean1.passwdChallengeQuestion eq mother_maiden_name}" > selected </c:if>><fmt:message key="mother_maiden_name" bundle="${resword}"/></option>
            <option <c:if test="${ userBean1.passwdChallengeQuestion eq favorite_color}" > selected </c:if>><fmt:message key="favorite_color" bundle="${resword}"/></option>
       
          
                
            </select>
             </div>
            <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="passwdChallengeQ"/></jsp:include></td><td class="formlabel">*</td>
        </tr>
        <tr><td class="formlabel"><fmt:message key="password_challenge_answer" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
        <input type="text" name="passwdChallengeA" value="${ userBean1.passwdChallengeAnswer}" class="formfieldXL"></div>
            <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="passwdChallengeA"/></jsp:include></td><td class="formlabel">*</td>
        </tr>
     </c:if>
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
