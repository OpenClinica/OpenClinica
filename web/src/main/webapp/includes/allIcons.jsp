<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<html>
<head>
  <meta http-equiv="content-type" content="text/html; charset=utf-8" />
  <meta http-equiv="X-UA-Compatible" content="IE=8" />

<title>OpenClinica</title>

<link rel="stylesheet" href="../includes/styles.css" type="text/css">
<%--<link rel="stylesheet" href="../includes/styles2.css" type="text/css">--%>


<script type="text/JavaScript" language="JavaScript" src="../includes/global_functions_javascript.js"></script>
<script type="text/JavaScript" language="JavaScript" src="../includes/Tabs.js"></script>
<script type="text/JavaScript" language="JavaScript" src="../includes/CalendarPopup.js"></script>
</head>

<body>
 	<b><fmt:message key="icon_key" bundle="${resword}"/></b><br clear="all"><br>

		<table border="0" cellpadding="4" cellspacing="0">
		   <tr>
				<td><img src="../images/bt_View.gif"></td>
				<td><fmt:message key="view" bundle="${resword}"/></td>
			</tr>
			<tr>
				<td><img src="../images/bt_Edit.gif"></td>
				<td><fmt:message key="edit" bundle="${resword}"/></td>
			</tr>
			<tr>
				<td><img src="../images/bt_Restore.gif"></td>
				<td><fmt:message key="restore" bundle="${resword}"/></td>
			</tr>
			<tr>
				<td><img src="../images/bt_Reassign.gif"></td>
				<td><fmt:message key="reassign" bundle="${resword}"/></td>
			</tr>
			<tr>
				<td><img src="../images/bt_Remove.gif"></td>
				<td><fmt:message key="remove" bundle="${resword}"/></td>
			</tr>
			<tr>
				<td><img src="../images/bt_Delete.gif"></td>
				<td><fmt:message key="delete" bundle="${resword}"/></td>
			</tr>
			<tr>
				<td><img src="../images/bt_Lock.gif"></td>
				<td><fmt:message key="lock" bundle="${resword}"/></td>
			</tr>
			<tr>
				<td><img src="../images/bt_Unlock.gif"></td>
				<td><fmt:message key="unlock" bundle="${resword}"/></td>
			</tr>
			
			<tr>
				<td><img src="../images/bt_Print.gif"></td>
				<td><fmt:message key="print" bundle="${resword}"/></td>
			</tr>
			<tr>
				<td><img src="../images/icon_Note.gif"></td>
				<td><fmt:message key="discrepancy_note" bundle="${resword}"/></td>
			</tr>
			<tr>
				<td><img src="../images/icon_noNote.gif"></td>
				<td><fmt:message key="add_discrepancy_note" bundle="${resword}"/></td>
			</tr>
			<tr>
				<td><img src="../images/icon_UnchangedData.gif"></td>
				<td><fmt:message key="form_data_not_modified" bundle="${resword}"/></td>
			</tr>
			<tr>
				<td><img src="../images/icon_UnsavedData.gif"></td>
				<td><fmt:message key="unsaved_data_in_form" bundle="${resword}"/></td>
			</tr>
			<tr>
				<td><img src="../images/icon_DEcomplete.gif"></td>
				<td><fmt:message key="completed" bundle="${resword}"/></td>
			</tr>
			<tr>
				<td><img src="../images/icon_InitialDE.gif"></td>
				<td><fmt:message key="started" bundle="${resword}"/></td>
			</tr>
			<tr>
				<td><img src="../images/icon_DDE.gif"></td>
				<td><fmt:message key="double_data_entry_start" bundle="${resword}"/></td>
			</tr>
			<tr>
				<td><img src="../images/icon_InitialDEcomplete.gif"></td>
				<td><fmt:message key="initial_data_entry_complete" bundle="${resword}"/></td>
			</tr>
			<tr>
				<td><img src="../images/icon_NotStarted.gif"></td>
				<td><fmt:message key="not_started" bundle="${resword}"/></td>
			</tr>
			<tr>
				<td><img src="../images/icon_Scheduled.gif"></td>
				<td><fmt:message key="scheduled" bundle="${resword}"/></td>
			</tr>
			<tr>
				<td><img src="../images/icon_Stopped.gif"></td>
				<td><fmt:message key="stopped" bundle="${resword}"/></td>
			</tr>
			<tr>
				<td><img src="../images/icon_Skipped.gif"></td>
				<td><fmt:message key="skipped" bundle="${resword}"/></td>
			</tr>
			<tr>
				<td><img src="../images/icon_Locked.gif"></td>
				<td><fmt:message key="locked" bundle="${resword}"/></td>
			</tr>
			<tr>
				<td><img src="../images/icon_Invalid.gif"></td>
				<td><fmt:message key="invalid" bundle="${resword}"/></td>
			</tr>
			
		</table>

</body>
</html>	
	