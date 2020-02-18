<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="reswords"/>

 <tr id="sidebar_IconKey_open">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_IconKey_open'); leftnavExpand('sidebar_IconKey_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="icon_key" bundle="${reswords}"/></b><br clear="all"><br>

		<table border="0" cellpadding="4" cellspacing="0">
		   <tr>
				<td><img src="images/bt_View.gif"></td>
				<td><fmt:message key="view" bundle="${reswords}"/></td>
			</tr>
			<tr>
				<td><img src="images/bt_Edit.gif"></td>
				<td><fmt:message key="edit" bundle="${reswords}"/></td>
			</tr>
			<tr>
				<td><img src="images/bt_Restore.gif"></td>
				<td><fmt:message key="restore" bundle="${reswords}"/></td>
			</tr>

			<tr>
				<td><img src="images/bt_Remove.gif"></td>
				<td><fmt:message key="remove" bundle="${reswords}"/></td>
			</tr>
			<tr>
				<td><img src="images/bt_Delete.gif"></td>
				<td>Delete</td>
			</tr>
			<tr>
				<td><img src="images/bt_Lock.gif"></td>
				<td><fmt:message key="lock" bundle="${reswords}"/></td>
			</tr>
			<tr>
				<td><img src="images/bt_Unlock.gif"></td>
				<td><fmt:message key="unlock" bundle="${reswords}"/></td>
			</tr>

			<tr>
				<td><img src="images/bt_Print.gif"></td>
				<td><fmt:message key="print" bundle="${reswords}"/></td>
			</tr>

		</table>

		<div class="sidebar_tab_content">

		</div>

		</td>
	</tr>

	<tr id="sidebar_IconKey_closed" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_IconKey_open'); leftnavExpand('sidebar_IconKey_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="icon_key" bundle="${reswords}"/></b>

		</td>
	</tr>
