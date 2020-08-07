<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="reswords"/>

 <tr id="sidebar_IconKey_open">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_IconKey_open'); leftnavExpand('sidebar_IconKey_closed');"><span class="icon icon-caret-down gray" border="0" align="right" hspace="10"></a>

		<fmt:message key="icon_key" bundle="${reswords}"/><br clear="all"><br>

		<table border="0" cellpadding="4" cellspacing="0">
            <tr>
                <td><fmt:message key="statuses" bundle="${reswords}"/></td>
            </tr>
            <tr>
                <td>&nbsp;<span class="icon icon-doc"></span></td>
                <td><fmt:message key="not_started" bundle="${reswords}"/></td>
            </tr>
            <tr>
                <td>&nbsp;<span class="icon icon-clock"></span></td>
                <td><fmt:message key="not_scheduled" bundle="${reswords}"/></td>
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
                <td>&nbsp;<span class="icon icon-stamp-new black"></span></td>
                <td><fmt:message key="signed" bundle="${reswords}"/></td>
            </tr>
            <tr>
                <td>&nbsp;<span class="icon icon-lock-new black"></span></td>
                <td><fmt:message key="locked" bundle="${reswords}"/></td>
            </tr>
            <tr>
                <td>&nbsp;<span class="icon icon-archived-new black"></span></td>
                <td><fmt:message key="archived" bundle="${reswords}"/></td>
            </tr>
            <tr>
                <td>&nbsp;<span class="icon icon-file-excel red"></span></td>
                <td><fmt:message key="status_removed" bundle="${reswords}"/></td>
            </tr>
            <tr>
                <td><fmt:message key="actions" bundle="${reswords}"/></td>
            </tr>
		   <tr>
				<td><span class="icon icon-search"></td>
				<td><fmt:message key="view" bundle="${reswords}"/></td>
			</tr>
			<tr>
				<td><span class="icon icon-pencil"></td>
				<td><fmt:message key="edit" bundle="${reswords}"/></td>
			</tr>
			<tr>
				<td><span class="icon icon-ccw"></td>
				<td><fmt:message key="restore" bundle="${reswords}"/></td>
			</tr>

			<tr>
				<td><span class="icon icon-cancel"></td>
				<td><fmt:message key="remove" bundle="${reswords}"/></td>
			</tr>
			<tr>
				<td><span class="icon icon-trash"></td>
				<td><fmt:message key="clear" bundle="${reswords}"/></td>
			</tr>
			<tr>
				<td><span class="icon icon-lock"></td>
				<td><fmt:message key="lock" bundle="${reswords}"/></td>
			</tr>
			<tr>
				<td><span class="icon icon-lock-open"></td>
				<td><fmt:message key="unlock" bundle="${reswords}"/></td>
			</tr>

			<tr>
				<td><span class="icon icon-print"></td>
				<td><fmt:message key="print" bundle="${reswords}"/></td>
			</tr>

		</table>

		

		</td>
	</tr>

	<tr id="sidebar_IconKey_closed" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_IconKey_open'); leftnavExpand('sidebar_IconKey_closed');"><span class="icon icon-caret-right gray" border="0" align="right" hspace="10"></a>

		<fmt:message key="icon_key" bundle="${reswords}"/>

		</td>
	</tr>
