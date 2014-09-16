<%@page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/taglibs.jsp" %>
<!-- For Mantis Issue 6099 -->
<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
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
 <meta http-equiv="X-UA-Compatible" content="IE=8" />

<link rel="stylesheet" href="<c:url value='/includes/styles.css'/>" type="text/css"/>
<%-- <link rel="stylesheet" href="includes/styles2.css" type="text/css">--%>
<link rel="stylesheet" href="<c:url value='/includes/NewLoginStyles.css'/>" type="text/css"/>
<script type="text/JavaScript" language="JavaScript" src="<c:url value='/includes/jmesa/jquery.min.js'/>"></script>
<script type="text/JavaScript" language="JavaScript" src="<c:url value='/includes/jmesa/jquery-migrate-1.1.1.js'/>"></script>
<script type="text/javascript" language="JavaScript" src="<c:url value='/includes/jmesa/jquery.blockUI.js'/>"></script>
<%-- <script type="text/JavaScript" language="JavaScript" src="includes/global_functions_javascript2.js"></script> --%>
<script type="text/JavaScript" language="JavaScript" src="<c:url value='/includes/global_functions_javascript.js'/>"></script>
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
                if (/Firefox[\/\s](\d+\.\d+)/.test(navigator.userAgent)){
                    var ffversion=new Number(RegExp.$1)
                    if (!(ffversion>=3)){
                        document.write("<tr> <td align='center' ><h4>"+
                        " <fmt:message key="choose_browser" bundle="${restext}"/>"+
                        "</h4></td> </tr>");
                    }
                } else if (/MSIE (\d+\.\d+);/.test(navigator.userAgent)){
                     var ieversion=new Number(RegExp.$1)
                     if (ieversion!=8 && ieversion!=7){
                     document.write("<tr> <td align='center' > <h4>"+
                         "<fmt:message key="choose_browser" bundle="${restext}"/> "+
                         "</h4></td> </tr>");
                     }
                }else{
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
            <div ID="samlBox">
                <!-- News box contents -->
                <h1><a href="${pageContext.request.contextPath}/saml/login/">Universal Login</A></h1>
                <!-- End News box contents -->
            </div>
            </td>
       
            <td class="loginBox">
            <div ID="loginBox">
            <!-- Login box contents -->
                <div ID="login">
                    <form action="<c:url value='/j_spring_security_check'/>" method="post">
                    <h1><fmt:message key="login" bundle="${resword}"/></h1>
                    <b><fmt:message key="user_name" bundle="${resword}"/></b>
                        <div class="formfieldM_BG">
                            <input type="text" id="username" name="j_username" class="formfieldM">
                        </div>

                    <b><fmt:message key="password" bundle="${resword}"/></b>
                        <div class="formfieldM_BG">
                            <input type="password" id="j_password" name="j_password"  class="formfieldM"  autocomplete="off">
                        </div>
                    <input type="submit" name="submit" value="<fmt:message key='login' bundle='${resword}'/>" class="loginbutton" />
                    <a href="#" id="requestPassword"> <fmt:message key="forgot_password" bundle="${resword}"/></a>
                   </form>
                   <br/><jsp:include page="../login-include/login-alertbox.jsp"/>
                   <%-- <a href="<c:url value="/RequestPassword"/>"> <fmt:message key="forgot_password" bundle="${resword}"/></a> --%>
               </div>
            <!-- End Login box contents -->
            </div>
            </td>
            <td class="loginBox">
            <div ID="newsBox">
                <!-- News box contents -->
                <h1><fmt:message key="news" bundle="${resword}"/></h1><fmt:message key="loading" bundle="${resword}"/> ...
                <!-- End News box contents -->
            </div>
            </td>
      </tr>
    </table>

    </center>

    <script type="text/javascript">
        document.getElementById('username').setAttribute( 'autocomplete', 'off' );
        document.getElementById('j_password').setAttribute( 'autocomplete', 'off' );

        jQuery(document).ready(function() {

        	jQuery.get("../../RssReader", function(data){
//                alert("Data Loaded: " + data);
                jQuery("#newsBox").html(data);
            });


            jQuery('#requestPassword').click(function() {
                jQuery.blockUI({ message: jQuery('#requestPasswordForm'), css:{left: "200px", top:"180px" } });
            });

            jQuery('#cancel').click(function() {
                jQuery.unblockUI();
                return false;
            });
        });

    </script>

        <div id="requestPasswordForm" style="display:none;">
              <c:import url="requestPasswordPop.jsp">
              </c:import>
        </div>

<!-- Footer -->
<!-- End Main Content Area -->
<jsp:include page="../login-include/login-footer.jsp"/>