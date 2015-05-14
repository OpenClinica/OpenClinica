<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>

<script type="text/javascript" language="JavaScript">
  <!--
  function checkOverwriteStatus() {
      //return confirm('<fmt:message key="you_will_overwrite_event_CRFs_continue" bundle="${resword}"/>');
      return confirm('<fmt:message key="you_will_overwrite_event_CRFs_continue" bundle="${resword}"/>');
  }
 //-->
</script>

<c:choose>
<c:when test="${userBean.sysAdmin && module=='admin'}">
 <c:import url="../include/admin-header.jsp"/>
</c:when>
<c:otherwise>
 <c:import url="../include/submit-header.jsp"/>
</c:otherwise>
</c:choose>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: all">
	<td class="sidebar_tab">
		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
			<img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10">
		</a>
		<b><fmt:message key="instructions" bundle="${restext}"/></b>
		<div class="sidebar_tab_content">
			<fmt:message key="import_side_bar_instructions" bundle="${restext}"/>
		</div>
	</td>
</tr>

<tr id="sidebar_Instructions_closed" style="display: none">
	<td class="sidebar_tab">
		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
			<img src="images/sidebar_expand.gif" border="0" align="right" hspace="10">
		</a>
		<b><fmt:message key="instructions" bundle="${restext}"/></b>
	</td>
</tr>



<jsp:include page="../include/sideInfo.jsp"/>


<jsp:useBean scope='session' id='version' class='org.akaza.openclinica.bean.submit.CRFVersionBean'/>
<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='importedData' class='java.util.ArrayList'/>
<jsp:useBean scope='session' id='subjectData' class='java.util.ArrayList'/>
<jsp:useBean scope='session' id='validationErrors' class='java.util.HashMap'/>
<jsp:useBean scope='session' id='hardValidationErrors' class='java.util.HashMap'/>
<jsp:useBean scope='session' id='summaryStats' class='org.akaza.openclinica.bean.submit.crfdata.SummaryStatsBean'/>
<jsp:useBean scope='session' id='crfName' class='java.lang.String'/>


<%-- <c:out value="${crfName}"/> --%>

<c:choose>
	<c:when test="${userBean.sysAdmin && module=='admin'}">
		<h1><span class="title_manage">
	</c:when>
	<c:otherwise>
		<h1>
		<span class="title_manage">
	</c:otherwise>
</c:choose>

<fmt:message key="import_crf_data" bundle="${resworkflow}"/></h1>
<p><fmt:message key="import_instructions" bundle="${restext}"/></p>

<!--  summary stats here, tbh -->

<div style="width: 300px">

<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="textbox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">

	<tr valign="top">
		<td class="table_header_row"><fmt:message key="summary_statistics" bundle="${resword}"/>:</td>
	</tr>
	<tr valign="top">
    	<td class="table_cell_left"><fmt:message key="subjects_affected" bundle="${resword}"/>: <c:out value="${summaryStats.studySubjectCount}" /></td>
	</tr>
    <tr valign="top">
        <td class="table_cell_left"><fmt:message key="event_CRFs_affected" bundle="${resword}"/>: <c:out value="${summaryStats.eventCrfCount}" /></td>
    </tr>
	<c:if test="${empty hardValidationErrors}">
	    <tr valign="top">
	        <td class="table_cell_left"><fmt:message key="event_CRFs_available" bundle="${resword}"/>: <c:out value="${summaryStats.eventCrfCount - summaryStats.skippedCrfCount}" /></td>
	    </tr>
	    <tr valign="top">
	        <td class="table_cell_left"><fmt:message key="event_CRFs_skipped" bundle="${resword}"/>: <c:out value="${summaryStats.skippedCrfCount}" /></td>
	    </tr>
	</c:if>
	<tr valign="top">
    	<td class="table_cell_left"><fmt:message key="validation_rules_generated" bundle="${resword}"/>: <c:out value="${summaryStats.discNoteCount}" /></td>
	</tr>



</table>
</div>
</div></div></div></div></div></div></div></div>
</div>
<br/>

<%-- skipped CRFs here --%>
<c:if test="${empty hardValidationErrors}">
<fmt:message key="crf_data_skipped" bundle="${resword}"/>
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="textbox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
 <tr>
 <td class="table_header_row"><fmt:message key="study_oid" bundle="${resword}"/></td>
 <td class="table_header_row"><fmt:message key="study_subject_oid" bundle="${resword}"/></td>
 <td class="table_header_row"><fmt:message key="event_CRF_OID" bundle="${resword}"/></td>
 <td class="table_header_row"><fmt:message key="CRF_version_OID" bundle="${resword}"/></td>
 <td class="table_header_row"><fmt:message key="event_crf_status" bundle="${resword}"/></td>
</tr> 
 <c:forEach items="${importCrfInfo.importCRFList}" var="importCRF">
    <c:if test="${!importCRF.processImport}">
    <tr>
    <td class="table_cell_left"><c:out value="${importCRF.studyOID}" /></td>
    <td class="table_cell"><c:out value="${importCRF.studySubjectOID}" /></td>
    <td class="table_cell"><c:out value="${importCRF.studyEventOID}" /></td>
    <td class="table_cell"><c:out value="${importCRF.formOID}" /></td>
    <td class="table_cell">
        <c:choose>
	        <c:when test="${importCRF.preImportStage.initialDE}"><fmt:message key="initial_data_entry" bundle="${resword}"/></c:when>
	        <c:when test="${importCRF.preImportStage.initialDE_Complete}"><fmt:message key="initial_data_entry_complete" bundle="${resword}"/></c:when>
	        <c:when test="${importCRF.preImportStage.doubleDE}"><fmt:message key="double_data_entry" bundle="${resword}"/></c:when>
	        <c:when test="${importCRF.preImportStage.doubleDE_Complete}"><fmt:message key="data_entry_complete" bundle="${resword}"/></c:when>
	        <c:when test="${importCRF.preImportStage.admin_Editing}"><fmt:message key="administrative_editing" bundle="${resword}"/></c:when>
	        <c:when test="${importCRF.preImportStage.locked}"><fmt:message key="locked" bundle="${resword}"/></c:when>
	        <c:otherwise><fmt:message key="invalid" bundle="${resword}"/></c:otherwise>
        </c:choose>
    </td>
    </tr>
    </c:if>
 </c:forEach>
</table>
</div>
</div></div></div></div></div></div></div></div>
</div>
<br/>
</c:if>


<%-- hard validation errors here --%>
<%-- if we have hard validation errors here, we stop and don't generate the other two tables --%>
<c:choose>
	<c:when test="${not empty hardValidationErrors}">
	<fmt:message key="hard_validation_error_checks" bundle="${resword}"/>
	<div style="width: 100%">

	<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
	<div class="textbox_center">
	<table border="0" cellpadding="0" cellspacing="0" width="90%">

		<c:forEach var="subjectDataBean" items="${subjectData}" >
			<tr valign="top">
				<td class="table_header_row" colspan="4"><fmt:message key="study_subject" bundle="${resword}"/>: <c:out value="${subjectDataBean.subjectOID}"/></td>
			</tr>
			<c:forEach var="studyEventData" items="${subjectDataBean.studyEventData}">
				<tr valign="top">
		    		<td class="table_header_row"><fmt:message key="event_CRF_OID" bundle="${resword}"/></td>
		    		<td class="table_header_row" colspan="3"></td>
				</tr>
				<tr valign="top">
		    		<td class="table_cell_left"><c:out value="${studyEventData.studyEventOID}"/>
		    		<c:choose>
		    			<c:when test="${studyEventData.studyEventRepeatKey != null}">
		    				(<fmt:message key="repeated_key" bundle="${resword}">
		    					<fmt:param><c:out value="${studyEventData.studyEventRepeatKey}"/></fmt:param>
		    				</fmt:message>)
		    				<c:set var="studyEventRepeatKey" value="${studyEventData.studyEventRepeatKey}"/>
		    			</c:when>
		    			<c:otherwise>
		    				<c:set var="studyEventRepeatKey" value="${1}"/>
		    			</c:otherwise>
		    			</c:choose>
		    		</td>
		    		<td class="table_cell" colspan="3"></td>
				</tr>
				<c:forEach var="formData" items="${studyEventData.formData}">
					<tr valign="top">
			    		<td class="table_header_row"></td>
			    		<td class="table_header_row"><fmt:message key="CRF_version_OID" bundle="${resword}"/></td>
			    		<td class="table_header_row" colspan="2"></td>
					</tr>
					<tr valign="top">
		    			<td class="table_cell_left"></td>
		    			<td class="table_cell"><c:out value="${formData.formOID}"/></td>
		    			<td class="table_cell" colspan="2"></td>
					</tr>
					<c:forEach var="itemGroupData" items="${formData.itemGroupData}">
						<tr valign="top">
				    		<td class="table_header_row"></td>
				    		<td class="table_header_row"></td>
				    		<td class="table_header_row" colspan="2"><c:out value="${itemGroupData.itemGroupOID}"/>
				    		<c:choose>
				    			<c:when test="${itemGroupData.itemGroupRepeatKey != null}">
				    				(<fmt:message key="repeated_key" bundle="${resword}">
		    						 	<fmt:param><c:out value="${itemGroupData.itemGroupRepeatKey}"/></fmt:param>
		    						 </fmt:message> )
				    				<c:set var="groupRepeatKey" value="${itemGroupData.itemGroupRepeatKey}"/>
				    			</c:when>
				    			<c:otherwise>
				    				<c:set var="groupRepeatKey" value="${1}"/>
				    			</c:otherwise>
				    		</c:choose>
				    		</td>
				    		<%-- add repeat key here? --%>
						</tr>
						<c:forEach var="itemData" items="${itemGroupData.itemData}">
						<c:set var="oidKey" value="${itemData.itemOID}_${studyEventRepeatKey}_${groupRepeatKey}_${subjectDataBean.subjectOID }"/>
						<c:if test="${not empty hardValidationErrors[oidKey]}">
							<tr valign="top">
					    		<td class="table_cell_left"></td>
					    		<td class="table_cell"></td>
					    		<td class="table_cell"><font color="red"><c:out value="${itemData.itemOID}"/></font></td>
					    		<%-- or add it here? --%>
					    		<td class="table_cell">
					    			<c:out value="${itemData.value}"/><br/>
					    			<c:out value="${hardValidationErrors[oidKey]}"/>
					    		</td>
							</tr>
						</c:if>
						</c:forEach>
					</c:forEach>
				</c:forEach>
			</c:forEach>
		</c:forEach>



	</table>
	</div>
	</div></div></div></div></div></div></div></div>
	</div>
	<br/>
	<%-- place form here, so that user can go back --%>
	<form action="ImportCRFData?action=confirm&crfId=<c:out value="${version.crfId}"/>&name=<c:out value="${version.name}"/>" method="post" ENCTYPE="multipart/form-data">

	<p><fmt:message key="import_instructions" bundle="${restext}"/></p>
	<div style="width: 400px">

	<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
	<div class="textbox_center">
	<table border="0" cellpadding="0" cellspacing="0">

	<tr>
		<td class="formlabel"><fmt:message key="xml_file_to_upload" bundle="${resterm}"/>:</td>
		<td>
			<div class="formfieldFile_BG"><input type="file" name="xml_file" > </div>
			<br><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="xml_file"/></jsp:include>
		</td>
	</tr>
	<input type="hidden" name="crfId" value="<c:out value="${version.crfId}"/>">


	</table>
	</div>
	</div></div></div></div></div></div></div></div>
	</div>

	<br clear="all">
	<input type="submit" value="<fmt:message key="continue" bundle="${resword}"/>" class="button_long">

	</form>
	</c:when>

	<c:otherwise>
		<%-- place everything else here --%>
		<%-- validation errors here --%>
		<c:if test="${not empty validationErrors}">
			<fmt:message key="validation_error_generated" bundle="${resword}" />

			<div style="width: 600px">

			<div class="box_T">
			<div class="box_L">
			<div class="box_R">
			<div class="box_B">
			<div class="box_TL">
			<div class="box_TR">
			<div class="box_BL">
			<div class="box_BR">
			<div class="textbox_center">
			<table border="0" cellpadding="0" cellspacing="0" width="100%">

				<c:forEach var="subjectDataBean" items="${subjectData}">
                    <c:set var="currSubjectMap" value="${importCrfInfo.importCRFMap[subjectDataBean.subjectOID]}"/>
                    <c:if test="${currSubjectMap != null}">
					<tr valign="top">
						<td class="table_header_row" colspan="4"><fmt:message
							key="study_subject" bundle="${resword}" />: <c:out
							value="${subjectDataBean.subjectOID}" /></td>
					</tr>
					<c:forEach var="studyEventData" items="${subjectDataBean.studyEventData}">
                        <c:set var="currEventMap" value="${currSubjectMap[studyEventData.studyEventOID]}"/>
                        <c:if test="${currEventMap != null}">
						<tr valign="top">
							<td class="table_header_row"><fmt:message
								key="event_CRF_OID" bundle="${resword}" /></td>
							<td class="table_header_row" colspan="3"></td>
						</tr>
						<tr valign="top">
							<td class="table_cell_left"><c:out
								value="${studyEventData.studyEventOID}" /> <c:choose>
								<c:when test="${studyEventData.studyEventRepeatKey != null}">
			    				(<fmt:message key="repeated_key" bundle="${resword}">
										<fmt:param>
											<c:out value="${studyEventData.studyEventRepeatKey}" />
										</fmt:param>
									</fmt:message> )
			    				<c:set var="studyEventRepeatKey"
										value="${studyEventData.studyEventRepeatKey}" />
								</c:when>
								<c:otherwise>
									<c:set var="studyEventRepeatKey" value="${1}" />
								</c:otherwise>
							</c:choose></td>
							<td class="table_cell" colspan="3"></td>
						</tr>
						<c:forEach var="formData" items="${studyEventData.formData}">
                            <c:set var="currFormMap" value="${currEventMap[formData.formOID]}"/>
                            <c:if test="${currFormMap != null}">
							<tr valign="top">
								<td class="table_header_row"></td>
								<td class="table_header_row"><fmt:message
									key="CRF_version_OID" bundle="${resword}" /></td>
								<td class="table_header_row" colspan="2"></td>
							</tr>
							<tr valign="top">
								<td class="table_cell_left"></td>
								<td class="table_cell"><c:out value="${formData.formOID}" /></td>
								<td class="table_cell" colspan="2"></td>
							</tr>
							<c:forEach var="itemGroupData" items="${formData.itemGroupData}">
								<tr valign="top">
									<td class="table_header_row"></td>
									<td class="table_header_row"></td>
									<td class="table_header_row" colspan="2"><c:out
										value="${itemGroupData.itemGroupOID}" /> <c:choose>
										<c:when test="${itemGroupData.itemGroupRepeatKey != null}">
					    				(<fmt:message key="repeated_key" bundle="${resword}">
												<fmt:param>
													<c:out value="${itemGroupData.itemGroupRepeatKey}" />
												</fmt:param>
											</fmt:message> )
					    				<c:set var="groupRepeatKey"
												value="${itemGroupData.itemGroupRepeatKey}" />
										</c:when>
										<c:otherwise>
											<c:set var="groupRepeatKey" value="${1}" />
										</c:otherwise>
									</c:choose></td>
									<%-- add repeat key here? --%>
								</tr>
								<c:forEach var="itemData" items="${itemGroupData.itemData}">
									<c:set var="oidKey"
										value="${itemData.itemOID}_${studyEventRepeatKey}_${groupRepeatKey}_${subjectDataBean.subjectOID}" />
									<c:if test="${not empty validationErrors[oidKey]}">
										<tr valign="top">
											<td class="table_cell_left"></td>
											<td class="table_cell"></td>
											<td class="table_cell"><font color="red"><c:out
												value="${itemData.itemOID}" /></font></td>
											<%-- or add it here? --%>
											<td class="table_cell"><c:out value="${itemData.value}" /><br />
											<c:out value="${validationErrors[oidKey]}" /></td>
										</tr>
									</c:if>
								</c:forEach>
							</c:forEach>
							</c:if>
						</c:forEach>
                        </c:if>
					</c:forEach>
                    </c:if>
				</c:forEach>



			</table>
			</div>
			</div>
			</div>
			</div>
			</div>
			</div>
			</div>
			</div>
			</div>
			</div>
		</c:if>
		<br />



		<!--  valid data section, show all valid data -->
		<fmt:message key="valid_data_imported" bundle="${resword}"/>

			<div style="width: 600px">

			<div class="box_T">
			<div class="box_L">
			<div class="box_R">
			<div class="box_B">
			<div class="box_TL">
			<div class="box_TR">
			<div class="box_BL">
			<div class="box_BR">
			<div class="textbox_center">
			<table border="0" cellpadding="0" cellspacing="0" width="100%">

				<c:forEach var="subjectDataBean" items="${subjectData}">
                    <c:set var="currSubjectMap" value="${importCrfInfo.importCRFMap[subjectDataBean.subjectOID]}"/>
                    <c:if test="${currSubjectMap != null}">
					<tr valign="top">
						<td class="table_header_row" colspan="4"><fmt:message
							key="study_subject" bundle="${resword}" />: <c:out
							value="${subjectDataBean.subjectOID}" /></td>
					</tr>
					<c:forEach var="studyEventData"
						items="${subjectDataBean.studyEventData}">
                        <c:set var="currEventMap" value="${currSubjectMap[studyEventData.studyEventOID]}"/>
                        <c:if test="${currEventMap != null}">
						<tr valign="top">
							<td class="table_header_row"><fmt:message
								key="event_CRF_OID" bundle="${resword}" /></td>
							<td class="table_header_row" colspan="3"></td>
						</tr>
						<tr valign="top">
							<td class="table_cell_left"><c:out
								value="${studyEventData.studyEventOID}" /> <c:choose>
								<c:when test="${studyEventData.studyEventRepeatKey != null}">
			    				(<fmt:message key="repeated_key" bundle="${resword}">
										<fmt:param>
											<c:out value="${studyEventData.studyEventRepeatKey}" />
										</fmt:param>
									</fmt:message>)
			    				<c:set var="studyEventRepeatKey"
										value="${studyEventData.studyEventRepeatKey}" />
								</c:when>
								<c:otherwise>
									<c:set var="studyEventRepeatKey" value="${1}" />
								</c:otherwise>
							</c:choose></td>
							<td class="table_cell" colspan="3"></td>
						</tr>
						<c:forEach var="formData" items="${studyEventData.formData}">
                            <c:set var="currFormMap" value="${currEventMap[formData.formOID]}"/>
                            <c:if test="${currFormMap != null}">


							<tr valign="top">
								<td class="table_header_row"></td>
								<td class="table_header_row"><fmt:message
									key="CRF_version_OID" bundle="${resword}" /></td>
								<td class="table_header_row" colspan="2"></td>
							</tr>
							<tr valign="top">
								<td class="table_cell_left"></td>
								<td class="table_cell"><c:out value="${formData.formOID}" /></td>
								<td class="table_cell" colspan="2"></td>
							</tr>
							<c:forEach var="itemGroupData" items="${formData.itemGroupData}">
								<tr valign="top">
									<td class="table_header_row"></td>
									<td class="table_header_row"></td>
									<td class="table_header_row" colspan="2"><c:out
										value="${itemGroupData.itemGroupOID}" /> <c:choose>
										<c:when test="${itemGroupData.itemGroupRepeatKey != null}">
					    				(<fmt:message key="repeated_key" bundle="${resword}">
												<fmt:param>
													<c:out value="${itemGroupData.itemGroupRepeatKey}" />
												</fmt:param>
											</fmt:message> )
					    				<c:set var="groupRepeatKey"
												value="${itemGroupData.itemGroupRepeatKey}" />
										</c:when>
										<c:otherwise>
											<c:set var="groupRepeatKey" value="${1}" />
										</c:otherwise>
									</c:choose></td>
									<%-- add repeat key here? --%>
								</tr>
								<c:forEach var="itemData" items="${itemGroupData.itemData}">
									<c:set var="oidKey"
										value="${itemData.itemOID}_${studyEventRepeatKey}_${groupRepeatKey}_${subjectDataBean.subjectOID}" />
									<c:if test="${empty validationErrors[oidKey]}">
										<tr valign="top">
											<td class="table_cell_left"></td>
											<td class="table_cell"></td>
											<td class="table_cell"><c:out
												value="${itemData.itemOID}" /></td>
											<%-- or add it here? --%>
											<td class="table_cell"><c:out value="${itemData.value}" /></td>
										</tr>
									</c:if>
								</c:forEach>
							</c:forEach>
							</c:if>
						</c:forEach>
						</c:if>
					</c:forEach>
					</c:if>
				</c:forEach>



			</table>
			</div>
			</div>
			</div>
			</div>
			</div>
			</div>
			</div>
			</div>
			</div>
			</div>
			<br />

			<form action="VerifyImportedCRFData?action=save" method="POST">

			<input type="hidden" name="crfId"
				value="<c:out value="${version.crfId}"/>"> <c:set
				var="overwriteCount" value="${0}" /> <c:forEach
				var="displayItemBeanWrapper" items="${importedData}">
				<c:if test="${displayItemBeanWrapper.overwrite}">
					<c:set var="overwriteCount" value="${overwriteCount + 1}" />
				</c:if>
			</c:forEach> <c:if test="${overwriteCount == 0}">
				<input type="submit"
					value="<fmt:message key="continue" bundle="${resword}"/>"
					class="button_long">
			</c:if> <c:if test="${overwriteCount > 0 }">
				<input type="submit"
					value="<fmt:message key="continue" bundle="${resword}"/>"
					class="button_long" onClick="return checkOverwriteStatus();">
			</c:if></form>

			<%-- added an alert above --%>

			<form action="ListStudySubjects"><input type="submit"
				value="<fmt:message key="cancel" bundle="${resword}"/>"
				class="button_long"></form>
			<%-- end of the other loop --%>
	</c:otherwise>
</c:choose>

<c:choose>
  <c:when test="${userBean.sysAdmin && module=='admin'}">
  <c:import url="../include/workflow.jsp">
   <c:param name="module" value="admin"/>
  </c:import>
 </c:when>
  <c:otherwise>
   <c:import url="../include/workflow.jsp">
   <c:param name="module" value="manage"/>
  </c:import>
  </c:otherwise>
 </c:choose>

<jsp:include page="../include/footer.jsp"/>