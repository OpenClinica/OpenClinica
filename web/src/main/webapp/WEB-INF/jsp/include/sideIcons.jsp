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
				<td><span class="icon icon-trash red"></td>
				<td><fmt:message key="delete" bundle="${reswords}"/></td>
			</tr>
			<tr>
				<td><span class="icon icon-lock"></td>
				<td><fmt:message key="lock" bundle="${reswords}"/></td>
			</tr>
			<tr>
				<td><span class="icon icon-icon-unArchive"></td>
				<td><fmt:message key="unlock" bundle="${reswords}"/></td>
			</tr>

			<tr>
				<td><span class="icon icon-print"></td>
				<td><fmt:message key="print" bundle="${reswords}"/></td>
			</tr>

		</table>

		<div class="sidebar_tab_content">

			<a href="#" onClick="openDefWindow('https://docs.openclinica.com/3.1/openclinica-user-guide/overview-openclinica/home-page#content-title-3610'); return false;"><fmt:message key="view_all_icons" bundle="${reswords}"/></a>

		</div>

		</td>
	</tr>

	<tr id="sidebar_IconKey_closed" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_IconKey_open'); leftnavExpand('sidebar_IconKey_closed');"><span class="icon icon-caret-right gray" border="0" align="right" hspace="10"></a>

		<fmt:message key="icon_key" bundle="${reswords}"/>

		</td>
	</tr>
