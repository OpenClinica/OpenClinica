<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="core.org.akaza.openclinica.i18n.words" var="reswords"/>

<link rel="stylesheet" href="includes/font-awesome-4.7.0/css/font-awesome.css">

<tr id="sidebar_IconKey_open">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_IconKey_open'); leftnavExpand('sidebar_IconKey_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="icon_key" bundle="${reswords}"/></b><br clear="all"><br>

		<table border="0" cellpadding="4" cellspacing="0">
			<tr>
				<td><span class="fa fa-bubble-red"></td>
				<td><fmt:message key="discrepancy_note" bundle="${reswords}"/></td>
			</tr>
			<tr>
				<td><span class="fa fa-bubble-white"></td>
				<td><fmt:message key="add_discrepancy_note" bundle="${reswords}"/></td>
			</tr>
			<tr>
				<td><img src="images/icon_UnchangedData.gif"></td>
				<td><fmt:message key="form_data_not_modified" bundle="${reswords}"/></td>
			</tr>
			<tr>
				<td><img src="images/icon_UnsavedData.gif"></td>
				<td><fmt:message key="unsaved_data_in_form" bundle="${reswords}"/></td>
			</tr>
		</table>

		<div class="sidebar_tab_content">

		 <a href="#" onClick="openDefWindow('help/iconkey.html'); return false;"><fmt:message key="view_all_icons" bundle="${reswords}"/></a>

		</div>

		</td>
	</tr>
	<tr id="sidebar_IconKey_closed" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_IconKey_open'); leftnavExpand('sidebar_IconKey_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="icon_key" bundle="${reswords}"/></b>

		</td>
	</tr>
