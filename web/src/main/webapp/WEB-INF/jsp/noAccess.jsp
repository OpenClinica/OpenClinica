<%--
  Created by IntelliJ IDEA.
  User: yogi
  Date: 8/10/18
  Time: 11:29 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<html>
<head>
    <link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
    <script src="https://code.jquery.com/jquery-1.12.4.js"></script>
    <script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
<style>
    .no-close .ui-dialog-titlebar-close {
        display: none;
    }
</style>

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

<div id="dialog" title="Basic dialog">
    <p><fmt:message key="access_denied_due_to_permission_tag" bundle="${resword}"/></p>
</div>

