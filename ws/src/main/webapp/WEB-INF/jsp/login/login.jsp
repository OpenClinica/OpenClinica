<%@page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/taglibs.jsp" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>

<title>OpenClinica</title>

<meta http-equiv="Content-type" content="text/html; charset=UTF-8"/>
 <meta http-equiv="X-UA-Compatible" content="IE=11" />
<link rel="shortcut icon" type="image/x-icon" href="<c:url value='/images/favicon.png'/>">

<link rel="stylesheet" href="<c:url value='/includes/styles.css'/>" type="text/css"/>
<link rel="stylesheet" href="<c:url value='/includes/styles2.css'/>" type="text/css" />
<link rel="stylesheet" href="<c:url value='/includes/NewLoginStyles.css'/>" type="text/css"/>
<script type="text/JavaScript" language="JavaScript" src="<c:url value='/includes/jmesa/jquery-1.3.2.min.js'/>"></script>
<script type="text/javascript" language="JavaScript" src="<c:url value='/includes/jmesa/jquery.blockUI.js'/>"></script>
<script type="text/JavaScript" language="JavaScript" src="<c:url value='/includes/global_functions_javascript2.js'/>"></script>
<script type="text/JavaScript" language="JavaScript" src="<c:url value='/includes/global_functions_javascript.js'/>"></script>
 
</head>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>


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

    <table border="0" cellpadding="0" cellspacing="0" class="loginBoxes">
        <tr>
            <td class="loginBox_T">&nbsp;</td>
            <td class="loginBox_T">&nbsp;</td>
       </tr>
       <tr>
            <td class="loginBox">
            <div ID="loginBox">
            <!-- Login box contents -->
                <div ID="login">
                     <h1><fmt:message key="login_ws" bundle="${resword}"/></h1>
                    
                   <br/><jsp:include page="../login-include/login-alertbox.jsp"/>
                   <%-- <a href="<c:url value="/RequestPassword"/>"> <fmt:message key="forgot_password" bundle="${resword}"/></a> --%>
               </div>
            <!-- End Login box contents -->
            </div>
            </td>
            <td class="loginBox">
            <div ID="newsBox">
                <!-- News box contents -->
                <h1>News</h1>Loading ...
                <!-- End News box contents -->
            </div>
            </td>
      </tr>
        <script type="text/javascript">
                if (/Firefox[\/\s](\d+\.\d+)/.test(navigator.userAgent)){
                    var ffversion=new Number(RegExp.$1)
                    if (!(ffversion>=3)){
                        document.write("<tr> <td align='center' colspan=2 style='padding-left: 20px;' >"+
                        " <fmt:message key="choose_browser" bundle="${restext}"/>"+
                        "</td> </tr>");
                    }
                } else if (/MSIE (\d+\.\d+);/.test(navigator.userAgent)){
                     var ieversion=new Number(RegExp.$1)
                     if (ieversion!=8 && ieversion!=7){
                     document.write("<tr> <td align='justify' colspan=2 style='padding-left: 20px;' >"+
                         "<div style='width: 600px;' > <fmt:message key="choose_browser" bundle="${restext}"/> </div>"+
                         "</td> </tr>");
                     }
                }else{
                    document.write("<tr> <td align='center' colspan=2 style='padding-left: 20px;' >"+
                    " <fmt:message key="choose_browser" bundle="${restext}"/>"+
                    "</td> </tr>");
                }
             </script>
    </table>

    </center>

    <script type="text/javascript">
        document.getElementById('username').setAttribute( 'autocomplete', 'off' );
        document.getElementById('j_password').setAttribute( 'autocomplete', 'off' );

        jQuery(document).ready(function() {

        	$.get("../../RssReader", function(data){
                //alert("Data Loaded: " + data);
                $("#newsBox").html(data);
            });


           

            jQuery('#cancel').click(function() {
                jQuery.unblockUI();
                return false;
            });
        });

    </script>

    

<!-- Footer -->
<!-- End Main Content Area -->
<jsp:include page="../login-include/login-footer.jsp"/>