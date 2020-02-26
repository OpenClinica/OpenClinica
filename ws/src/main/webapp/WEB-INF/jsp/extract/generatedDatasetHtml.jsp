<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<jsp:include page="../include/extract-header.jsp"/>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		<div class="sidebar_tab_content">
		<fmt:message key="generate_dataset_HTML_instructions1" bundle="${restext}"/>


        <fmt:message key="generate_dataset_HTML_instructions2" bundle="${restext}"/>
		</div>

		</td>

	</tr>
	<tr id="sidebar_Instructions_closed" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		</td>
  </tr>

<jsp:include page="../include/extractDataSideInfo.jsp"/>



<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope="request" id="dataset" class="org.akaza.openclinica.bean.extract.DatasetBean"/>

<%--
<jsp:useBean scope="request" id="extractBean" class="org.akaza.openclinica.bean.extract.ExtractBean"/>
--%>

<h1><span class="title_manage"><fmt:message key="download_data" bundle="${resword}"/>: <c:out value="${dataset.name}"/></span></h1>

<table border="0" cellpadding="0" cellspacing="0">
	<tr>
		<td>
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center" align="center">
<table border="0" cellpadding="0" cellspacing="0">

  <tr valign="top" ><td class="table_header_column_top"><fmt:message key="dataset_name" bundle="${resword}"/>:</td><td class="table_cell_top">
  <c:out value="${dataset.name}"/>
  </td></tr>
  <tr valign="top" ><td class="table_header_column"><fmt:message key="dataset_description" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${dataset.description}"/>
  </td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="item_status" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${dataset.datasetItemStatus.description}"/>
  </td></tr>
  <tr valign="top" ><td class="table_header_column"><fmt:message key="study_name" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${extractBean.parentStudyName}"/>
   </td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="protocol_ID" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${extractBean.parentProtocolId}"/>
  </td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="date" bundle="${resword}"/>:</td><td class="table_cell">
  <fmt:formatDate value="${extractBean.dateCreated}" pattern="${dteFormat}"/>
  </td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="subjects" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${extractBean.numSubjects}"/>
  </td></tr>
</table>
</div>

</div></div></div></div></div></div></div></div>
		</td>
	</tr>
</table>

<br/>
<table border="0" cellpadding="0" cellspacing="0">
	<tr>
		<td>
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center" align="center">
<table border="0" cellpadding="0" cellspacing="0">
  <tr valign="top">

      <td class="table_header_row_left"><fmt:message key="subject_unique_ID" bundle="${resword}"/></td>

    <td class="table_header_row"><fmt:message key="protocol_ID_site_ID" bundle="${resword}"/></td>
    <c:if test="${dataset.showSubjectDob}">
      <c:choose>
        <c:when test="${study.studyParameterConfig.collectDob =='1'}">
         <td class="table_header_row"><fmt:message key="date_of_birth" bundle="${resword}"/></td>
        </c:when>
        <c:when test="${study.studyParameterConfig.collectDob =='3'}">
         <td class="table_header_row"><fmt:message key="date_of_birth" bundle="${resword}"/></td>
        </c:when>
        <c:otherwise>
          <td class="table_header_row"><fmt:message key="year_of_birth" bundle="${resword}"/></td>
        </c:otherwise>
      </c:choose>
    </c:if>
    <c:if test="${dataset.showSubjectGender}">
       <td class="table_header_row"><fmt:message key="gender" bundle="${resword}"/></td>
    </c:if>

    <%-- place more dataset-subject relationships here, for COLUMN HEADERS tbh --%>

    <c:if test="${dataset.showSubjectStatus}">
       <td class="table_header_row"><fmt:message key="subject_status" bundle="${resword}"/></td>
    </c:if>
    <c:if test="${dataset.showSubjectUniqueIdentifier && extractBean.showUniqueId == '1'}">
       <td class="table_header_row">Unique ID</td>
    </c:if>
    <c:if test="${dataset.showSubjectSecondaryId}">
       <td class="table_header_row"><fmt:message key="secondary_ID" bundle="${resword}"/></td>
    </c:if>
	<%-- order from here on out: subject group info, eventlocation, event start,
		event end, event status,
		subject age at event,
		crf completion date,
		crf interviewed date,
		crf interviewed name,
		event crf status,
		crf version name,
		already in event headers?
		--%>


    <c:if test="${dataset.showSubjectGroupInformation}">
       <%--      --%>
	   <c:forEach var="groupClass" items="${extractBean.studyGroupClasses}">
			<%--<c:if test="${groupClass.selected}">--%>
			<td class="table_header_row"><c:out value="${groupClass.name}"/></td>
			<%--</c:if>--%>
	   </c:forEach>
    </c:if>


    <%--<c:if test="${dataset.showEventLocation}">
       <td class="table_header_row">Event Location</td>
    </c:if>

    <c:if test="${dataset.showSubjectAgeAtEvent}">
       <td class="table_header_row">Age At Event</td>
    </c:if>--%>

    <%-- start event headers and item names HERE--%>

    <c:forEach var="eventHeader" items="${extractBean.eventHeaders}">
       <td class="table_header_row"><c:out value="${eventHeader}"/>&nbsp;</td>
      </c:forEach>
    <c:forEach var="itemName" items="${extractBean.itemNames}">
      <td class="table_header_row"><a href="javascript: openDocWindow('ViewItemDetail?itemId=<c:out value="${itemName.item.id}"/>')"><c:out value="${itemName.itemHeaderName}"/></a></td>
    </c:forEach>
   </tr>

   <c:forEach var="displayItem" items="${extractBean.rowValues}">

     <tr valign="top">

      <td class="table_cell_left"><c:out value="${displayItem.subjectName}"/></td>
      <td class="table_cell"><c:out value="${displayItem.studyLabel}"/></td>
      <%-- ? --%>
      <c:if test="${dataset.showSubjectDob}">
       <td class="table_cell"><c:out value="${displayItem.subjectDob}"/></td>
       </c:if>
       <c:if test="${dataset.showSubjectGender}">
         <td class="table_cell"><c:out value="${displayItem.subjectGender}"/></td>
       </c:if>

       <%-- ADDL SUBJ. COLUMN VALUES, tbh --%>

      	<c:if test="${dataset.showSubjectStatus}">
       		<td class="table_cell"><c:out value="${displayItem.subjectStatus}"/>&nbsp;</td>
    	</c:if>
    	<c:if test="${dataset.showSubjectUniqueIdentifier && extractBean.showUniqueId == '1'}">
       		<td class="table_cell"><c:out value="${displayItem.subjectUniqueId}"/>&nbsp;</td>
    	</c:if>

    	<c:if test="${dataset.showSubjectSecondaryId}">
       		<td class="table_cell"><c:out value="${displayItem.subjectSecondaryId}"/>&nbsp;</td>
    	</c:if>

		<%-- order from here on out: subject group info, eventlocation, event start,
		event end, event status,
		subject age at event,
		crf completion date,
		crf interviewed date,
		crf interviewed name,
		event crf status,
		crf version name,
		--%>
    	<c:if test="${dataset.showSubjectGroupInformation}">
			<c:forEach var="groupClass" items="${extractBean.studyGroupClasses}">
				<c:choose>
					<c:when test="${not empty displayItem.groupNames[groupClass.id]}">
						<td class="table_cell">
							<c:out value="${displayItem.groupNames[groupClass.id]}"/>&nbsp;
						</td>
					</c:when>
					<c:otherwise>
						<td class="table_cell">&nbsp;</td>
					</c:otherwise>
				</c:choose>
       		<%--<td class="table_cell"></td>--%>
			</c:forEach>
    	</c:if>

    	<%-- EVENT COLUMN VALUES, tbh --%>

      <c:forEach var="eventValue" items="${displayItem.eventValues}">
       <td class="table_cell"><c:out value="${eventValue}"/>&nbsp;</td>
      </c:forEach>
      <c:forEach var="itemValue" items="${displayItem.itemValues}">
       <td class="table_cell"><c:out value="${itemValue}"/>&nbsp;</td>
      </c:forEach>
     </tr>
   </c:forEach>
</table>
</div>
</div></div></div></div></div></div></div></div>
		</td>
	</tr>
</table>


<jsp:include page="../include/footer.jsp"/>
