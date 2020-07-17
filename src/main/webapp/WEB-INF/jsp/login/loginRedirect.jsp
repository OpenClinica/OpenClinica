<%@page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/taglibs.jsp" %>
<!-- For Mantis Issue 6099 -->
<jsp:useBean scope='session' id='userBean' class='core.org.akaza.openclinica.bean.login.UserAccountBean'/>
    <c:if test="${userBean.name!=''}">
    <c:redirect url="/MainMenu"/>
    </c:if>
<!-- End of 6099-->

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>

<title>OpenClinica</title>

<meta http-equiv="Content-type" content="text/html; charset=UTF-8"/>
 <meta http-equiv="X-UA-Compatible" content="IE=11" />

<link rel="stylesheet" href="<c:url value='/includes/styles.css'/>" type="text/css"/>
<%-- <link rel="stylesheet" href="includes/styles2.css" type="text/css">--%>
<link rel="stylesheet" href="<c:url value='/includes/NewLoginStyles.css'/>" type="text/css"/>
<script type="text/JavaScript" language="JavaScript" src="<c:url value='/includes/jmesa/jquery.min.js'/>"></script>
<script type="text/JavaScript" language="JavaScript" src="<c:url value='/includes/jmesa/jquery-migrate-3.1.0.min.js'/>"></script>
<script type="text/javascript" language="JavaScript" src="<c:url value='/includes/jmesa/jquery.blockUI.js'/>"></script>
<%-- <script type="text/JavaScript" language="JavaScript" src="includes/global_functions_javascript2.js"></script> --%>
<script type="text/JavaScript" language="JavaScript" src="<c:url value='/includes/global_functions_javascript.js'/>"></script>
<script type="text/JavaScript" language="JavaScript" src="<c:url value='/includes/ua-parser.min.js'/>"></script>
</head>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>

<%--<c:choose>--%>
    <%--<c:when test="${resword.locale == null}"><fmt:setLocale value="en" scope="session"/></c:when>--%>
    <%--<c:otherwise><fmt:setLocale value="${resword.locale}" scope="session"/></c:otherwise>--%>
<%--</c:choose>--%>

<body class="login_BG" onLoad="document.getElementById('username').focus();">
    <div class="login_BG">
    <form action="LoginRedirect" method="post">
        <center>

        <!-- OpenClinica logo -->
        <%String ua = request.getHeader( "User-Agent" );
        String temp = "";
        String iev = "";
        if( ua != null && ua.indexOf( "MSIE" ) != -1 ) {
            temp = ua.substring(ua.indexOf( "MSIE" ),ua.length());
            iev = temp.substring(4, temp.indexOf(";"));
            iev = iev.trim();
        }
        if(iev.length() > 1 && Double.valueOf(iev)<7) {%>
        <div ID="OClogoIE6">&nbsp;</div>
        <%} else {%>
        <div ID="OClogo">&nbsp;</div>
        <%}%>
        <!-- end OpenClinica logo -->
            <table width="720 px">

        <script type="text/javascript">
            var parser = new UAParser();
            var showMessage = false;

            if (parser.getBrowser().name == 'IE' && parseInt(parser.getBrowser().major) < 11){
                showMessage = true;
            }else if (parser.getBrowser().name != 'Firefox' && parser.getBrowser().name !='Chrome' && parser.getBrowser().name != 'IE'){
                showMessage = true;
            }

            if (showMessage){
                document.write("<tr> <td align='center' ><h4>"+
                            " <fmt:message key="choose_browser" bundle="${restext}"/>"+
                            "</h4></td> </tr>");
            }
        </script>
                </table>

        <table border="0" cellpadding="0" cellspacing="0" class="loginBoxes">
            <tr>
                <td class="loginBox_T">&nbsp;</td>
                <td class="loginBox_T">&nbsp;</td>
            </tr>
            <tr>
                <td class="loginBox">
                <div ID="loginBox">
                <!-- Login error redirect box contents -->
                    <div id="login_redirect">
                        <P><fmt:message key="redirect_login" bundle="${resword}"/></P>
                        <table>
                            <tr>
                                <td><input class="button" name="logout" value="fmt:message key="logout" bundle="${resword}"/>" onClick="javascript:window.location.href='j_spring_security_logout'"></input></td>
                                <td><input class="button" name="continue" value="fmt:message key="continue" bundle="${resword}"/>" onClick="javascript:window.location.href='MainMenu'"></input></td>
                            </tr>
                        </table>
                        <P>
                            <fmt:message key="bookmark_link_1" bundle="${resword}"/>
                            <a href="${pageContext.request.contextPath}MainMenu"/>"><fmt:message key="link" bundle="${restext}"/></a>
                            <fmt:message key="bookmark_link_2" bundle="${resword}"/>
                        </P>
                   </div>
                <!-- End Login box contents -->
                </div>
                </td>
            </tr>
        </table>

        </center>
    </form>
<!-- Footer -->
<!-- End Main Content Area -->
<jsp:include page="../login-include/login-footer.jsp"/>
