<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>

<c:choose>
    <c:when test="${module eq 'manage'}">
        <jsp:include page="../include/managestudy-header.jsp"/>
    </c:when>
    <c:otherwise><jsp:include page="../include/submit-header.jsp"/>
    </c:otherwise>
</c:choose>

<link rel="stylesheet" href="includes/jmesa/jmesa.css" type="text/css">
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery-1.3.2.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.jmesa.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jmesa.js"></script>

<script type="text/javascript">
    function onInvokeAction(id,action) {
        if(id.indexOf('listNotes') == -1)  {
        setExportToLimit(id, '');
        }
        createHiddenInputFieldsForLimitAndSubmit(id);
    }
    function onInvokeExportAction(id) {
        var parameterString = createParameterStringForLimit(id);
        location.href = '${pageContext.request.contextPath}/ViewNotes?'+ parameterString;
    }
</script>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		<div class="sidebar_tab_content">
			<fmt:message key="this_section_allows_a_study_manager_to_view_and_resolve" bundle="${restext}"/>
		</div>

		</td>

	</tr>
	<tr id="sidebar_Instructions_closed" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='table' class='org.akaza.openclinica.web.bean.EntityBeanTable'/>
<jsp:useBean scope='request' id='message' class='java.lang.String'/>

<h1><span class="title_manage"><fmt:message key="view_discrepancy_notes" bundle="${resword}"/></span></h1>
<%--<a href="javascript:openDocWindow('ViewNotes?print=yes')"--%>
<%--onMouseDown="javascript:setImage('bt_Print0','images/bt_Print_d.gif');"--%>
<%--onMouseUp="javascript:setImage('bt_Print0','images/bt_Print.gif');">--%>
<%--<img name="bt_Print0" src="images/bt_Print.gif" border="0" alt="<fmt:message key="print" bundle="${resword}"/>"></a>--%>

<%--
<div class="dnKey"><strong><fmt:message key="Filter_by_status" bundle="${resword}"/>
:</strong>

    <a href="ViewNotes?module=${module}&viewForOne=${param.viewForOne}&id=${param.id}&type=50" <c:if test="${param.type == 50}">style="color:green"</c:if>>All Notes</a>&nbsp;

    <a href="ViewNotes?module=${module}&viewForOne=${param.viewForOne}&id=${param.id}&resolutionStatus=1&discNoteType=${param.discNoteType}"><img
                      name="icon_Note" src="images/icon_Note.gif" border="0"
                      alt="<fmt:message key="Open" bundle="${resterm}"/>" title="<fmt:message key="Open" bundle="${resterm}"/>"/></a> (<fmt:message key="Open" bundle="${resterm}"/>)&nbsp;

    <a href="ViewNotes?module=${module}&viewForOne=${param.viewForOne}&id=${param.id}&resolutionStatus=2&discNoteType=${param.discNoteType}"><img
                      name="icon_flagYellow" src="images/icon_flagYellow.gif" border="0"
                      alt="<fmt:message key="Updated" bundle="${resterm}"/>" title="<fmt:message key="Updated" bundle="${resterm}"/>"/></a> (<fmt:message key="Updated" bundle="${resterm}"/>)&nbsp;

    <a href="ViewNotes?module=${module}&viewForOne=${param.viewForOne}&id=${param.id}&resolutionStatus=3&discNoteType=${param.discNoteType}"><img
                          name="icon_flagGreen" src="images/icon_flagGreen.gif" border="0"
                          alt="<fmt:message key="Resolved" bundle="${resterm}"/>" title="<fmt:message key="Resolved" bundle="${resterm}"/>"/></a> (<fmt:message key="Resolved" bundle="${resterm}"/>)&nbsp;

    <a href="ViewNotes?module=${module}&viewForOne=${param.viewForOne}&id=${param.id}&resolutionStatus=4&discNoteType=${param.discNoteType}"><img
                             name="icon_flagBlack" src="images/icon_flagBlack.gif" border="0"
                             alt="<fmt:message key="Closed" bundle="${resterm}"/>" title="<fmt:message key="Closed" bundle="${resterm}"/>"/></a> (<fmt:message key="Closed" bundle="${resterm}"/>)&nbsp;

     <a href="ViewNotes?module=${module}&viewForOne=${param.viewForOne}&id=${param.id}&resolutionStatus=5&discNoteType=${param.type}"><img
      name="icon_flagNA" src="images/icon_flagWhite.gif" border="0"
      alt="<fmt:message key="Not_Applicable" bundle="${resterm}"/>" title="<fmt:message key="Not_Applicable" bundle="${resterm}"/>"/></a> (<fmt:message key="Not_Applicable" bundle="${resterm}"/>)&nbsp;
</div>

<div class="dnKey"><strong><fmt:message key="Filter_by_note_type" bundle="${resword}"/>
:</strong>
     <a href="ViewNotes?module=${module}&viewForOne=${param.viewForOne}&id=${param.id}&discNoteType=50&resolutionStatus=${param.resolutionStatus}" <c:if test="${param.discNoteType == 50}">style="color:green"</c:if>><fmt:message key="all_notes" bundle="${resterm}"/></a>&nbsp;|&nbsp;
    <a href="ViewNotes?module=${module}&viewForOne=${param.viewForOne}&id=${param.id}&discNoteType=2&resolutionStatus=${param.resolutionStatus}" <c:if test="${param.discNoteType == 2}">style="color:green"</c:if>><fmt:message key="Annotation" bundle="${resterm}"/></a>&nbsp;|&nbsp;
    <a href="ViewNotes?module=${module}&viewForOne=${param.viewForOne}&id=${param.id}&discNoteType=1&resolutionStatus=${param.resolutionStatus}" <c:if test="${param.discNoteType == 1}">style="color:green"</c:if>><fmt:message key="Failed_Validation_Check" bundle="${resterm}"/></a>&nbsp;|&nbsp;
    <a href="ViewNotes?module=${module}&viewForOne=${param.viewForOne}&id=${param.id}&discNoteType=3&resolutionStatus=${param.resolutionStatus}" <c:if test="${param.discNoteType == 3}">style="color:green"</c:if>><fmt:message key="query" bundle="${resterm}"/></a>&nbsp;|&nbsp;
    <a href="ViewNotes?module=${module}&viewForOne=${param.viewForOne}&id=${param.id}&discNoteType=4&resolutionStatus=${param.resolutionStatus}" <c:if test="${param.discNoteType == 4}">style="color:green"</c:if>><fmt:message key="reason_for_change" bundle="${resterm}"/></a>

</div>
%-->


<%--<c:set var="module" value="${module}" scope="request"/>--%>
<%--
<c:import url="showNotesTable.jsp">
	<c:param name="rowURL" value="showDiscrepancyNoteRow.jsp" />
</c:import>
<br><br>
--%>
<form  action="${pageContext.request.contextPath}/ViewNotes">
        <input type="hidden" name="module" value="submit">
        ${viewNotesHtml}
    </form>
<!-- EXPANDING WORKFLOW BOX -->

<table border="0" cellpadding="0" cellspacing="0" style="position: relative; left: -14px;">
	<tr>
		<td id="sidebar_Workflow_closed" style="display: none">
		<a href="javascript:leftnavExpand('sidebar_Workflow_closed'); leftnavExpand('sidebar_Workflow_open');"><img src="images/<fmt:message key="image_dir" bundle="${resformat}"/>/tab_Workflow_closed.gif" border="0"></a>
	</td>
	<td id="sidebar_Workflow_open">
	<table border="0" cellpadding="0" cellspacing="0" class="workflowBox">
		<tr>
			<td class="workflowBox_T" valign="top">
			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td class="workflow_tab">
					<a href="javascript:leftnavExpand('sidebar_Workflow_closed'); leftnavExpand('sidebar_Workflow_open');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

					<b><fmt:message key="workflow" bundle="${resword}"/></b>

					</td>
				</tr>
			</table>
			</td>
			<td class="workflowBox_T" align="right" valign="top"><img src="images/workflowBox_TR.gif"></td>
		</tr>
		<tr>
			<td colspan="2" class="workflowbox_B">
			<div class="box_R"><div class="box_B"><div class="box_BR">
				<div class="workflowBox_center">


		<!-- Workflow items -->

				<table border="0" cellpadding="0" cellspacing="0">
					<tr>
						<td>

				<!-- These DIVs define shaded box borders -->
						<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

							<div class="textbox_center" align="center">

							 <span class="title_manage">
                               <fmt:message key="manage_study" bundle="${resword}"/>

							</span>

							</div>
						</div></div></div></div></div></div></div></div>

						</td>
						<td><img src="images/arrow.gif"></td>
						<td>

				<!-- These DIVs define shaded box borders -->
						<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

							<div class="textbox_center" align="center">

                             <span class="title_manage">

							<b><fmt:message key="view_discrepancy_notes" bundle="${resword}"/></b>


							</span>

							</div>
						</div></div></div></div></div></div></div></div>

						</td>
					</tr>
				</table>


		<!-- end Workflow items -->

				</div>
			</div></div></div>
			</td>
		</tr>
	</table>
	</td>
   </tr>
</table>

<!-- END WORKFLOW BOX -->
<jsp:include page="../include/footer.jsp"/>
