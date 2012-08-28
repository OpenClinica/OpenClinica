<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/> 
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<jsp:useBean scope='request' id='userBean1' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='challengeQuestions' class='java.util.ArrayList'/>
<jsp:include page="../login-include/login-header.jsp"/>

<jsp:include page="../login-include/request-sidebar.jsp"/>
<!-- Main Content Area -->
<h1><fmt:message key="request_password_form" bundle="${resword}"/></h1>
<jsp:include page="../login-include/login-alertbox.jsp"/>
<p><fmt:message key="you_must_be_an_openClinica_member_to_receive_a_password" bundle="${resword}"/></p>
<form action="RequestPassword" method="post">
<fmt:message key="all_fields_are_required" bundle="${resword}"/><br>
<input type="hidden" name="action" value="confirm">
<!-- These DIVs define shaded box borders -->
<div style="width: 600px">
 <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0" cellspacing="0">  
  <tr><td class="formlabel"><fmt:message key="user_name" bundle="${resword}"/>:</td>
  <td><div class="formfieldXL_BG"><input type="text" name="name" value="<c:out value="${userBean1.name}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="name"/></jsp:include></td></tr>
  <tr valign="top"><td class="formlabel"><fmt:message key="email" bundle="${resword}"/>:</td>
  <td><div class="formfieldXL_BG"><input type="text" name="email" value="<c:out value="${userBean1.email}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="email"/></jsp:include></td></tr>
  <tr valign="top"><td class="formlabel"><fmt:message key="password_challenge_question" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
  
             
           <fmt:message key='favourite_pet' bundle='${resword}'  var = "favourite_pet" />
          <fmt:message key='city_of_birth' bundle='${resword}'  var = "city_of_birth" />
          <fmt:message key='mother_maiden_name' bundle='${resword}'  var = "mother_maiden_name" />
         <fmt:message key='favorite_color' bundle='${resword}'  var = "favorite_color" />  
 <select name="passwdChallengeQuestion" class="formfieldXL">
                <option value="" ><fmt:message key="Please_Select_One" bundle="${resword}"/></option>
            
 <option <c:if test="${userBean1.passwdChallengeQuestion eq favourite_pet }" > selected </c:if> ><fmt:message key="favourite_pet" bundle="${resword}"/></option>
            <option <c:if test="${ userBean1.passwdChallengeQuestion eq city_of_birth}" > selected </c:if>><fmt:message key="city_of_birth" bundle="${resword}"/></option>
            <option <c:if test="${ userBean1.passwdChallengeQuestion eq mother_maiden_name}" > selected </c:if>><fmt:message key="mother_maiden_name" bundle="${resword}"/></option>
            <option <c:if test="${ userBean1.passwdChallengeQuestion eq favorite_color}" > selected </c:if>><fmt:message key="favorite_color" bundle="${resword}"/></option>
       
          
   </select>
   
   
   </div></td></tr>
  <tr valign="top"><td class="formlabel"><fmt:message key="password_challenge_answer" bundle="${resword}"/>:</td>
  <td><div class="formfieldXL_BG"><input type="text" name="passwdChallengeAnswer" value="<c:out value="${userBean1.passwdChallengeAnswer}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="passwdChallengeAnswer"/></jsp:include></td></tr> 
  
</table>
</div>

</div></div></div></div></div></div></div></div>

</div>
<table border="0" cellpadding="0">
 <tr><td>
 <input type="submit" name="Submit" value="<fmt:message key="submit_password_request" bundle="${resword}"/>" class="button_xlong">
 </td>
 <td><input type="button" name="cancel" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button_medium" onclick="javascript:window.location.href='MainMenu'"></td>
 </tr> 
 </table>

</form>
<jsp:include page="../login-include/login-footer.jsp"/>
