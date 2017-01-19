<%@ page import="org.springframework.security.core.AuthenticationException" %>
<%@ page import="org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter" %>
<%@ page import="org.springframework.security.oauth2.provider.verification.BasicUserApprovalFilter" %>
<%@ page import="org.springframework.security.oauth2.provider.verification.VerificationCodeFilter" %>
<%@ taglib prefix="authz" uri="http://www.springframework.org/security/tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
  <title>OpenClinica</title>
  <link type="text/css" rel="stylesheet" href="<c:url value="/style.css"/>"/>
  <script type="text/JavaScript" language="JavaScript" src="../includes/jmesa/jquery.min.js"></script>
  <script type="text/javascript" language="JavaScript" src="../includes/jmesa/jquery-migrate-1.1.1.js"></script>
  <script type="text/javascript">
      $(document).ready( function() {
          $('#confirmationForm').submit();
      });
  </script>
</head>
<body>

  <!--<h1>OpenClinica</h1>-->

  <div id="content">

    <!-- 
    <c:if test="${!empty sessionScope.SPRING_SECURITY_LAST_EXCEPTION}">
      <div class="error">
        <h2>Woops!</h2>

        <p>Access could not be granted. (<%= ((AuthenticationException) session.getAttribute(AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY)).getMessage() %>)</p>
      </div>
    </c:if>
     -->
    <c:remove scope="session" var="SPRING_SECURITY_LAST_EXCEPTION"/>

    <authz:authorize ifAllGranted="ROLE_USER">
        <!--
      <h2>Please Confirm</h2>
        
      <p>You hereby authorize "<c:out value="${client.clientId}"/>" to access your protected resources.</p>
      -->
      <p>Loading...</p>
      

      <form id="confirmationForm" name="confirmationForm" action="<%=request.getContextPath() + VerificationCodeFilter.DEFAULT_PROCESSING_URL%>" method="POST">
        <input name="<%=BasicUserApprovalFilter.DEFAULT_APPROVAL_REQUEST_PARAMETER%>" value="<%=BasicUserApprovalFilter.DEFAULT_APPROVAL_PARAMETER_VALUE%>" type="hidden"/>
        <!--<label><input name="authorize" value="Authorize" type="submit"></label>-->
      </form>
      <form id="denialForm" name="denialForm" action="<%=request.getContextPath() + VerificationCodeFilter.DEFAULT_PROCESSING_URL%>" method="POST">
        <input name="<%=BasicUserApprovalFilter.DEFAULT_APPROVAL_REQUEST_PARAMETER%>" value="not_<%=BasicUserApprovalFilter.DEFAULT_APPROVAL_PARAMETER_VALUE%>" type="hidden"/>
        <!--<label><input name="deny" value="Deny" type="submit"></label>-->
      </form>
    </authz:authorize>
  </div>


</body>
</html>