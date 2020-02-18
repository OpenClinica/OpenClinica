<%@ page contentType="text/html; charset=UTF-8" %>
<%@page import="org.akaza.openclinica.bean.core.Status"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>


<jsp:useBean scope='session' id='trail' class='java.util.ArrayList'/>

<!-- Breadcrumbs -->

<!-- End Breadcrumbs -->

	</td>
	<!-- Help and Report Bugs Buttons -->

				<td valign="top" style="padding-left: 12px; width: 52px;">
				<table border="0" cellpadding="0" cellspacing="0">
					<tr>&nbsp;</tr>
					<tr>
            <%-- needed? 	onMouseDown="javascript:setImage('bt_Help','images/bt_ReportIssue.gif');"
							onMouseUp="javascript:setImage('bt_Help','images/bt_ReportIssue.gif');"
														onMouseDown="javascript:setImage('bt_Support','images/bt_Support_d.gif');"
							onMouseUp="javascript:setImage('bt_Support','images/bt_Support.gif');"

							<img
							name="bt_ReportIssue_lit" src="images/bt_ReportIssue_lit.gif" width="76" height="22" border="0" alt="<fmt:message key="help" bundle="${resword}"/>" title="<fmt:message key="openclinica_report_issue" bundle="${resword}"/>" style="margin-top: 2px; margin-left: 2px; margin-right: 2px; margin-bottom: 2px">--%>
            <td style="white-space:nowrap">
				<a href="javascript:openDocWindow('<c:out value="${sessionScope.supportURL}" />')"><span class="aka_font_general" style="font-size: 0.9em"><fmt:message key="openclinica_feedback" bundle="${resword}"/></span></a>
			</td>
					</tr>
				<%--	<tr>
						<td><a href="javascript:openDocWindow('<c:out value="${sessionScope.supportURL}" />')"><img
							name="bt_Support" src="images/bt_Support_lit.gif"  width="76" height="22" border="0" alt="<fmt:message key="openclinica_feedback" bundle="${resword}"/>" title="<fmt:message key="openclinica_feedback" bundle="${resword}"/>" style="margin-top: 2px; margin-left: 2px; margin-right: 2px; margin-bottom: 15px"></a>
						</td>
					</tr>--%>
				</table>
				</td>

<!-- end Help and Report Bugs Buttons -->
	<td valign="top" align="right" style="width: 24em;">

