<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/> 
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<jsp:include page="../login-include/login-header.jsp"/>
<jsp:useBean scope='session' id='newUserBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='otherStudy' class='java.lang.String'/>
<jsp:useBean scope='request' id='studyName' class='java.lang.String'/>


<jsp:include page="../login-include/request-sidebar.jsp"/>
<!-- Main Content Area -->
<h1>
<fmt:message key="confirm_your_user_account_information" bundle="${resword}"/>
</h1>
<P><fmt:message key="please_check_the_information_below_if_no_errors" bundle="${restext}"/>
<p>
<jsp:include page="../login-include/login-alertbox.jsp"/>
<form action="RequestAccount" method="post">
<!-- These DIVs define shaded box borders -->
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<input type="hidden" name="action" value="submit"> 
<input type="hidden" name="otherStudy" value="<c:out value="${otherStudy}"/>">  
<input type="hidden" name="studyName" value="<c:out value="${studyName}"/>"> 
<table border="0" cellpadding="0" cellspacing="0">
  <tr><td class="table_header_column"><fmt:message key="user_name" bundle="${resword}"/>:</td><td class="table_cell_top">
<c:out value="${newUserBean.name}"/></td></tr>
  <tr><td class="table_header_column"><fmt:message key="first_name" bundle="${resword}"/>:</td><td class="table_cell_top">
<c:out value="${newUserBean.firstName}"/></td></tr>
  <tr><td class="table_header_column"><fmt:message key="last_name" bundle="${resword}"/>:</td><td class="table_cell_top">
<c:out value="${newUserBean.lastName}"/>
</td></tr>
  <tr><td class="table_header_column"><fmt:message key="email" bundle="${resword}"/>:</td><td class="table_cell_top">
<c:out value="${newUserBean.email}"/></td></tr> 
 <tr><td class="table_header_column"><fmt:message key="institutional_affiliation" bundle="${resword}"/>:</td><td class="table_cell_top">
  <c:out value="${newUserBean.institutionalAffiliation}"/><br></td></tr>
 <tr><td class="table_header_column"><fmt:message key="default_active_study" bundle="${resword}"/>:</td>
 <td class="table_cell_top"><c:out value="${studyName}"/></td></tr>
<tr><td class="table_header_column">
<fmt:message key="other_studies_if_any" bundle="${resword}"/>:</td>
 <td class="table_cell_top"><c:out value="${otherStudy}"/></td></tr>
  
  <tr><td class="table_header_column"><fmt:message key="role_of_access_requested" bundle="${resword}"/>:</td>
  <td class="table_cell_top"><c:out value="${newUserBean.activeStudyRoleName}"/></td></tr> 
 
 
</table>
</div>

</div></div></div></div></div></div></div></div>

</div>
 <input type="submit" name="submit" value="<fmt:message key="submit_account_reset" bundle="${resword}"/>" class="button_long">
</form>
<jsp:include page="../login-include/login-footer.jsp"/>
