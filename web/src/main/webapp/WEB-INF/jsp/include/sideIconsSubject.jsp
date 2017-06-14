<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="reswords"/>
<%--
View
  Edit
  Remove
  Restore
  Reassigned
  Sign--%>
  <link rel="stylesheet" href="includes/css/icomoon-style.css">

<tr id="sidebar_IconKey_open">
    <td class="sidebar_tab" >

        <a href="javascript:leftnavExpand('sidebar_IconKey_open'); leftnavExpand('sidebar_IconKey_closed');"><span class="icon icon-caret-down gray"></span></a>

        <fmt:message key="icon_key" bundle="${reswords}"/><br clear="all"><br>

        <table border="0" cellpadding="4" cellspacing="0" width="100%" >
            <tr>
                <td><fmt:message key="statuses" bundle="${reswords}"/></td>
            </tr>
            <tr>
                <td>&nbsp;<span class="icon icon-doc"></span></td>
                <td><fmt:message key="not_started" bundle="${reswords}"/></td>
            </tr>
            <tr>
                <td>&nbsp;<span class="icon icon-clock2"></span></td>
                <td><fmt:message key="scheduled" bundle="${reswords}"/></td>
            </tr>
            <tr>
                <td>&nbsp;<span class="icon icon-pencil-squared orange"></span></td>
                <td><fmt:message key="data_entry_started" bundle="${reswords}"/></td>
            </tr>
            <tr>
                <td>&nbsp;<span class="icon icon-stop-circle red"></span></td>
                <td><fmt:message key="stopped" bundle="${reswords}"/></td>
            </tr>
            <tr>
                <td>&nbsp;<span class="icon icon-redo"></span></td>
                <td><fmt:message key="skipped" bundle="${reswords}"/></td>
            </tr>
            <tr>
                <td>&nbsp;<span class="icon icon-checkbox-checked green"></span></td>
                <td><fmt:message key="completed" bundle="${reswords}"/></td>
            </tr>
            <tr>
                <td>&nbsp;<span class="icon icon-icon-sign green"></span></td>
                <td><fmt:message key="signed" bundle="${reswords}"/></td>
            </tr>
            <tr>
                <td>&nbsp;<span class="icon icon-lock"></span></td>
                <td><fmt:message key="locked" bundle="${reswords}"/></td>
            </tr>
            <tr>
                <td>&nbsp;<span class="icon icon-file-excel red"></span></td>
                <td><fmt:message key="invalid" bundle="${reswords}"/></td>
            </tr>
            <tr>
                <td><fmt:message key="actions" bundle="${reswords}"/></td>
            </tr>
            <tr>
                <td>&nbsp;<span class="icon icon-search"></span></td>
                <td><fmt:message key="view" bundle="${reswords}"/></td>
            </tr>
            <tr>
                <td>&nbsp;<span class="icon icon-pencil"></span></td>
                <td><fmt:message key="edit" bundle="${reswords}"/></td>
            </tr>
            <c:if test="${userRole.manageStudy}">
                <tr>
                    <td>&nbsp;<span class="icon icon-cancel"></span></td>
                    <td><fmt:message key="remove" bundle="${reswords}"/></td>
                </tr>
                <tr>
                    <td>&nbsp;<span class="icon icon-ccw"></span></td>
                    <td><fmt:message key="restore" bundle="${reswords}"/></td>
                </tr>
                <tr>
                    <td>&nbsp;<span class="icon icon-icon-reassign2"></span></td>
                    <td><fmt:message key="reassign" bundle="${reswords}"/></td>
                </tr>
                <tr>
                    <td>&nbsp;<span class="icon icon-icon-sign green"></span></td>
                    <td><fmt:message key="sign" bundle="${reswords}"/></td>
                </tr>
            </c:if>
        </table>

        <div class="sidebar_tab_content">

            <u><a href="#" onClick="openDefWindow('https://docs.openclinica.com/3.1/openclinica-user-guide/overview-openclinica/home-page#content-title-3610'); return false;"><fmt:message key="view_all_icons" bundle="${reswords}"/></a></u>

        </div>

    </td>
</tr>

<tr id="sidebar_IconKey_closed" style="display: none">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_IconKey_open'); leftnavExpand('sidebar_IconKey_closed');"><span class="icon icon-caret-right gray"></span></a>

        <fmt:message key="icon_key" bundle="${reswords}"/>

    </td>
</tr>
