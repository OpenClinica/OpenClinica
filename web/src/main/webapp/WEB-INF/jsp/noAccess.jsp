<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="org.akaza.openclinica.web.SQLInitServlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='study' class='org.akaza.openclinica.bean.managestudy.StudyBean' />
<jsp:useBean scope='session' id='userRole' class='org.akaza.openclinica.bean.login.StudyUserRoleBean' />
<jsp:useBean scope="session" id="passwordExpired" class="java.lang.String"/>

<jsp:useBean scope='request' id='formMessages' class='java.util.HashMap'/>

<jsp:include page="include/submit-header.jsp"/>
<jsp:include page="include/sideAlert.jsp"/>
<jsp:include page="include/sideInfo.jsp"/>

<link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
<style>
    .no-close .ui-dialog-titlebar-close {
        display: none;
    }
</style>
<script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>

    <script type="application/javascript">
        var redirectPath = '<%= request.getAttribute("originatingPage") %>';
    </script>
    <script>
        $(function() {
            $("#dialog").dialog({
                dialogClass: "no-close",
                buttons: [
                    {
                        text: "OK",
                        click: function () {
                            location.href =  redirectPath;
                        }
                    }
                ]
            })
        });
    </script>

<div id="dialog" title="">
    <fmt:message key="permission_tag_noaccess_header" bundle="${resword}"/>
    <br><br>
    <fmt:message key="permission_tag_noaccess_body" bundle="${resword}"/>
</div>
