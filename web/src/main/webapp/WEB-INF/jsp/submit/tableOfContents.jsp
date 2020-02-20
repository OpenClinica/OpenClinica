<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<jsp:include page="../include/submit-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		<div class="sidebar_tab_content">

		</div>

		</td>

	</tr>
	<tr id="sidebar_Instructions_closed" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		</td>
  </tr>
<jsp:include page="../include/submitSideInfo.jsp"/>

<jsp:useBean scope="request" id="toc" class="org.akaza.openclinica.bean.submit.DisplayTableOfContentsBean" />

<c:set var="interviewer" value="" />
<c:set var="interviewDate" value="" />

<c:forEach var="presetValue" items="${presetValues}">
	<c:if test='${presetValue.key == "interviewer"}'>
		<c:set var="interviewer" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "interviewDate"}'>
		<c:set var="interviewDate" value="${presetValue.value}" />
	</c:if>
</c:forEach>
<DIV ID="testdiv1" STYLE="position:absolute;visibility:hidden;background-color:white;layer-background-color:white;"></DIV>


<c:choose>
<c:when test="${toc.action == 'ae'}">
<h1><span class="title_manage"><fmt:message key="administrative_editing_on_event_CRF" bundle="${resword}"/></span></h1>
  <c:import url="instructionsAdminEdit.jsp">
	<c:param name="currStep" value="eventCRFOverview" />
 </c:import>
</c:when>
<c:otherwise>
<h1><span class="title_submit"><fmt:message key="event_CRF_data_submission" bundle="${resword}"/></span></h1>
 <c:import url="instructionsEnterData.jsp">
	<c:param name="currStep" value="eventCRFOverview" />
 </c:import>
</c:otherwise>
</c:choose>


<!--<table>
	<tr>
		<td>
			<form method="POST" action="EnterDataForStudyEvent">
			<input type="hidden" name="eventId" value="<c:out value="${toc.studyEvent.id}"/>" />
			<input type="submit" name="Submit" value="View Study Event Overview" class="button_xlong" />
			</form>
		</td>
	<c:if test="${toc.action != 'ae'}">
		<td>
			<form method="POST" action="MarkEventCRFComplete">
			<input type="hidden" name="eventCRFId" value="<c:out value="${toc.eventCRF.id}"/>" />
			<input type="submit" name="Submit" value="Mark this Event CRF Complete" class="button_xlong"> <a href="javascript:openDocWindow('help/2_2_enrollSubject_Help.html#step2d')"><font size="2">(?)</font></a>
			</form>
		</td>
	</c:if>
	</tr>
</table>--!>
<br/>


<p>
<c:if test='${interviewer == ""}'>
Please enter the <b>interviewer name</b> and <b>date of interview</b> before proceeding to enter data for each section of the Subject's Event CRF.
</c:if>
<c:if test="${toc.action != 'ae'}">
After completing data entry, you should mark the CRF as complete (even if all CRF items have been completed). If you do not, your CRF will not be reviewable by administrators, will not show up in reports, and processing/review of the CRF may be delayed.
</c:if>

<!--<div class="table_title_submit">Event CRF Properties</div>-->


<form name="contentForm" action="TableOfContents" method="post">
<jsp:include page="../include/showSubmitted.jsp" />
<input type="hidden" name="ecid" value="<c:out value="${toc.eventCRF.id}"/>" />
<input type="hidden" name="action" value="<c:out value="${toc.action}"/>" />
<input type="hidden" name="editInterview" value="1" />
<input type="hidden" name="interviewer" value="<c:out value="${interviewer}"/>" />
<input type="hidden" name="interviewDate" value="<c:out value="${interviewDate}"/>" />
<table border="0" cellpadding="0" cellspacing="0">
<tr><td>
<div style="width: 400px">

<!-- These DIVs define shaded box borders -->

	<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

		<div class="tablebox_center">


		<!-- Table Contents -->

<table border="0" cellpadding="0" cellspacing="0" width="100%">
	<tr>
		<td class="table_cell"><b><fmt:message key="interviewer_name" bundle="${resword}"/>:</b></td>
		<td class="table_cell"><c:out value="${interviewer}" /></td>
		<td class="table_cell"><b><fmt:message key="interview_date" bundle="${resword}"/>:</b></td>
	  	<td class="table_cell"><c:out value="${interviewDate}" /></td>

	</tr>
</table>
<!-- End Table Contents -->

</div>

</div></div></div></div></div></div></div></div>

</div>
</td>
<td valign="top">
<input type="submit" value="<fmt:message key="edit_interview_info" bundle="${resword}"/>" class="button_long" />
</td></tr>
</table>
</form>

<c:set var="sectionNum" value="0"/>
<c:forEach var="section" items="${toc.sections}">
<c:set var="sectionNum" value="${sectionNum+1}"/>
</c:forEach>

<p>
<c:choose>
  <c:when test="${allowEnterData != 'yes'}">
    <div id="section_property" style="display: none">
  </c:when>
  <c:otherwise>
    <div id="section_property">
  </c:otherwise>
</c:choose>
<div class="table_title_submit"><fmt:message key="section_properties" bundle="${resword}"/></div>

<c:choose>
	<c:when test="${empty toc.sections}">
		<br/><fmt:message key="there_are_no_sections" bundle="${resword}"/>
	</c:when>
	<c:otherwise>
	<div style="width: 600px">

	<!-- These DIVs define shaded box borders -->
		<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

			<div class="tablebox_center">
<!-- Table Contents -->
<table border="0" cellpadding="0" cellspacing="0" width="100%">
	<tr>
<!--		<td class="table_header_row_left">Label</td> -->
		<td class="table_header_row"><fmt:message key="title" bundle="${resword}"/></td>
		<td class="table_header_row"><fmt:message key="incomplete_items" bundle="${resword}"/></td>

		<c:if test="${toc.eventDefinitionCRF.doubleEntry}">
		<td class="table_header_row"><fmt:message key="items_pending_validation" bundle="${resword}"/></td>
		</c:if>

		<td class="table_header_row"><fmt:message key="completed_items" bundle="${resword}"/></td>
		<td class="table_header_row"><fmt:message key="total_items" bundle="${resword}"/></td>
		<td class="table_header_row"><fmt:message key="actions" bundle="${resword}"/></td>
	</tr>
	<c:set var="rowCount" value="${0}" />
	<c:forEach var="section" items="${toc.sections}">
		<c:set var="actionLink" value="${toc.actionServlet}?eventCRFId=${toc.eventCRF.id}&sectionId=${section.id}" />

		<%-- set the action label --%>
		<c:choose>
			<c:when test='${toc.eventCRF.stage.uncompleted}'>
				<c:set var="action" value="<fmt:message key="begin_data_entry" bundle="${resword}"/>" />
			</c:when>
			<c:otherwise>
				<c:choose>
					<c:when test='${toc.eventCRF.stage.initialDE}' >
						<c:choose>
							<c:when test="${section.numItemsCompleted == 0}">
								<c:set var="action" value="<fmt:message key="begin_data_entry" bundle="${resword}"/>" />
							</c:when>
							<c:otherwise><c:set var="action" value="<fmt:message key="continue_data_entry" bundle="${resword}"/>" /></c:otherwise>
						</c:choose>
					</c:when>
					<c:otherwise>
						<c:choose>
							<c:when test='${toc.eventCRF.stage.initialDE_Complete}'>
								<c:set var="action" value="<fmt:message key="begin_validation" bundle="${resword}"/>" />
							</c:when>
							<c:otherwise>
								<c:choose>
									<c:when test='${toc.eventCRF.stage.doubleDE}'>
										<c:choose>
											<c:when test="${section.numItemsCompleted == 0}">
												<c:set var="action" value="<fmt:message key="begin_validation" bundle="${resword}"/>" />
											</c:when>
											<c:otherwise><c:set var="action" value="<fmt:message key="continue_validation" bundle="${resword}"/>" /></c:otherwise>
										</c:choose>
									</c:when>
									<c:otherwise>
										<c:choose>
											<c:when test='${(toc.eventCRF.stage.doubleDE_Complete) || (toc.eventCRF.stage.admin_Editing)}'>
												<c:set var="action" value="<fmt:message key="perform_administrative_edits" bundle="${resword}"/>"/>
											</c:when>
											<c:otherwise><c:set var="action" value="<fmt:message key="view_data" bundle="${resword}"/>" /></c:otherwise>
										</c:choose>
									</c:otherwise>
								</c:choose>
							</c:otherwise>
						</c:choose>
					</c:otherwise>
				</c:choose>
			</c:otherwise>
		</c:choose>
		<tr>
<!--			<td class="table_cell_left"><c:out value="${section.label}"/></td> -->
			<td class="table_cell"><c:out value="${section.title}"/></td>
			<td class="table_cell"><c:out value="${section.numItems - section.numItemsNeedingValidation - section.numItemsCompleted}" /></td>

			<c:if test="${toc.eventDefinitionCRF.doubleEntry}">
			<td class="table_cell"><c:out value="${section.numItemsNeedingValidation}" /></td>
			</c:if>

			<td class="table_cell"><c:out value="${section.numItemsCompleted}" /></td>
			<td class="table_cell"><c:out value="${section.numItems}" /></td>
<%--			<td class="table_cell"><a href="<c:out value="${actionLink}"/>"><c:out value="${action}"/></a>--%>
			<td class="table_cell">
				<a href="<c:out value="${actionLink}"/>"
					onMouseDown="javascript:setImage('bt_EnterData<c:out value="${rowCount}"/>','images/bt_EnterData_d.gif');"
					onMouseUp="javascript:setImage('bt_EnterData<c:out value="${rowCount}"/>','images/bt_EnterData.gif');"
				><img name="bt_EnterData<c:out value="${rowCount}"/>" src="images/bt_EnterData.gif" border="0" alt="<fmt:message key="enter_data" bundle="${resword}"/>" title="<fmt:message key="enter_data" bundle="${resword}"/>" align="left" hspace="6"></a>
			</td>
		</tr>
		<c:set var="rowCount" value="${rowCount + 1}" />
	</c:forEach>
</table>
</div>

</div></div></div></div></div></div></div></div>

</div>
</c:otherwise>
</c:choose>

<table>
<tr>
	<td>
		<form method="POST" action="EnterDataForStudyEvent">
		<input type="hidden" name="eventId" value="<c:out value="${toc.studyEvent.id}"/>" />
		<input type="submit" name="Submit" value="<fmt:message key="view_study_event_overview" bundle="${resword}"/>" class="button_xlong" />
		</form>
	</td>
  <c:if test="${toc.action != 'ae'}">
	<td>
		<form method="POST" action="MarkEventCRFComplete">
		<input type="hidden" name="eventCRFId" value="<c:out value="${toc.eventCRF.id}"/>" />
		<input type="submit" name="Submit" value="<fmt:message key="mark_this_CRF_complete" bundle="${resword}"/>" class="button_xlong">
		</form>
	</td>
  </c:if>
</tr>
</table>

</div>

<br><br>

<!-- EXPANDING WORKFLOW BOX -->

<table border="0" cellpadding="0" cellspacing="0" style="position: relative; left: -14px;">
	<tr>
		<td id="sidebar_Workflow_closed" style="display: none">
		<a href="javascript:leftnavExpand('sidebar_Workflow_closed'); leftnavExpand('sidebar_Workflow_open');"><img src="images/<fmt:message key="image_dir" bundle="${resformat}"/>/tab_Workflow_closed.gif" border="0"></a>
	</td>
	<td id="sidebar_Workflow_open" style="display: all">
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

							<span class="title_submit">



							<fmt:message key="submit_data" bundle="${resword}"/>


							</span>

							</div>
						</div></div></div></div></div></div></div></div>

						</td>
						<td><img src="images/arrow.gif"></td>
						<td>

				<!-- These DIVs define shaded box borders -->
						<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

							<div class="textbox_center" align="center">

							<span class="title_submit">


							<fmt:message key="view_subjects" bundle="${resword}"/>


							</span>

							</div>
						</div></div></div></div></div></div></div></div>

						</td>
						<td><img src="images/arrow.gif"></td>
						<td>

				<!-- These DIVs define shaded box borders -->
						<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

							<div class="textbox_center" align="center">

							<span class="title_submit">


							<b><fmt:message key="data_entry" bundle="${resword}"/></b>


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
