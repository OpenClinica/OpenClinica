<%-- calling syntax:
	(assuming showTable.jsp and userRow.jsp are in the same directory)
	<c:import url="../include/showTable.jsp"><c:param name="rowURL" value="userRow.jsp" /></c:import>
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>

<jsp:useBean scope="request" id="table" class="org.akaza.openclinica.web.bean.EntityBeanTable" />
<c:set var="rowURL" value="${param.rowURL}" />
<c:set var="outerFormName" value="${param.outerFormName}" />
<c:set var="searchFormOnClickJS" value="${param.searchFormOnClickJS}" />
<c:choose>
	<c:when test='${(outerFormName != null) && (outerFormName != "")}'><c:set var="searchFormDisplayed" value="${0}"/></c:when>
	<c:otherwise><c:set var="searchFormDisplayed" value="${1}"/></c:otherwise>
</c:choose>

<%-- transform booleans --%>
<c:choose>
	<c:when test="${table.ascendingSort}"><c:set var="ascending" value="${1}" /></c:when>
	<c:otherwise><c:set var="ascending" value="${0}" /></c:otherwise>
</c:choose>
<c:choose>
	<c:when test="${table.filtered}"><c:set var="filtered" value="${1}" /></c:when>
	<c:otherwise><c:set var="filtered" value="${0}" /></c:otherwise>
</c:choose>
<c:choose>
	<c:when test="${table.paginated}"><c:set var="paginated" value="${1}" /></c:when>
	<c:otherwise><c:set var="paginated" value="${0}" /></c:otherwise>
</c:choose>

<c:set var="firstPageQuery" value="${table.baseGetQuery}&ebl_page=1&ebl_sortColumnInd=${table.sortingColumnInd}&ebl_sortAscending=${ascending}&ebl_filtered=${filtered}&ebl_filterKeyword=${table.keywordFilter}&ebl_paginated=1" />
<c:set var="prevPageQuery" value="${table.baseGetQuery}&ebl_page=${table.currPageNumber - 1}&ebl_sortColumnInd=${table.sortingColumnInd}&ebl_sortAscending=${ascending}&ebl_filtered=${filtered}&ebl_filterKeyword=${table.keywordFilter}&ebl_paginated=1" />
<c:set var="nextPageQuery" value="${table.baseGetQuery}&ebl_page=${table.currPageNumber + 1}&ebl_sortColumnInd=${table.sortingColumnInd}&ebl_sortAscending=${ascending}&ebl_filtered=${filtered}&ebl_filterKeyword=${table.keywordFilter}&ebl_paginated=1" />
<c:set var="lastPageQuery" value="${table.baseGetQuery}&ebl_page=${table.totalPageNumbers}&ebl_sortColumnInd=${table.sortingColumnInd}&ebl_sortAscending=${ascending}&ebl_filtered=${filtered}&ebl_filterKeyword=${table.keywordFilter}&ebl_paginated=1" />
<c:set var="viewAllQuery" value="${table.baseGetQuery}&ebl_page=${1}&ebl_sortColumnInd=${table.sortingColumnInd}&ebl_sortAscending=${ascending}&ebl_filtered=0&ebl_filterKeyword=&ebl_paginated=0" />
<c:set var="doNotPaginateQuery" value="${table.baseGetQuery}&ebl_page=1&ebl_sortColumnInd=${table.sortingColumnInd}&ebl_sortAscending=${ascending}&ebl_filtered=${filtered}&ebl_filterKeyword=${table.keywordFilter}&ebl_paginated=0" />
<c:set var="paginateQuery" value="${table.baseGetQuery}&ebl_page=1&ebl_sortColumnInd=${table.sortingColumnInd}&ebl_sortAscending=${ascending}&ebl_filtered=${filtered}&ebl_filterKeyword=${table.keywordFilter}&ebl_paginated=1" />
<c:set var="removeFilterQuery" value="${table.baseGetQuery}&ebl_page=${table.currPageNumber}&ebl_sortColumnInd=${table.sortingColumnInd}&ebl_sortAscending=${ascending}&ebl_filtered=0&ebl_filterKeyword=&ebl_paginated=${paginated}" />

<!-- These DIVs define shaded box borders -->
<table border="0" cellpadding="0" cellspacing="0" id="Table0" style="display: all">
	<tr>
		<td>
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="tablebox_center">

	<table border="0" cellpadding="0" cellspacing="0">

<!-- Table Actions row (pagination, search, tools) -->

		<tr>

	<!-- Pagination cell (for multi-page tables) -->

		<td width="25%" valign="top" class="table_actions">
		<table border="0" cellpadding="0" cellspacing="0">
			<tr>
				<td valign="top" class="table_tools">
					<c:if test="${table.paginated && (table.currPageNumber > 1)}">
						<a href="<c:out value="${firstPageQuery}"/>"><img src="images/arrow_first.gif" border="0" alt="<fmt:message key="first_page" bundle="${resword}"/>" title="<fmt:message key="first_page" bundle="${resword}"/>"></a>
						<a href="<c:out value="${prevPageQuery}"/>"><img src="images/arrow_back.gif" border="0" alt="<fmt:message key="back" bundle="${resword}"/>" title="<fmt:message key="back" bundle="${resword}"/>"></a>
					</c:if>
				</td>
				<td valign="top" class="table_tools">
					<c:choose>
						<c:when test="${empty table.rows}"> <fmt:message key="no_pages" bundle="${resword}"/> </c:when>
						<c:otherwise>
						<c:if test="${table.paginated}">
							<fmt:message key="page_x_de_y" bundle="${resword}">
								<fmt:param><c:out value="${table.currPageNumber}" /></fmt:param>
								<fmt:param><c:out value="${table.totalPageNumbers}" /></fmt:param>
							</fmt:message>
						</c:if>
						Found <%= table.getRows().size() %> Definitions
						</td></c:otherwise>
					</c:choose>
				<td valign="top" class="table_tools">
					<c:if test="${table.paginated && (table.currPageNumber < table.totalPageNumbers)}">
						<a href="<c:out value="${nextPageQuery}"/>"><img src="images/arrow_next.gif" border="0" alt="next" title="next"></a>
						<a href="<c:out value="${lastPageQuery}"/>"><img src="images/arrow_last.gif" border="0" alt="last page" title="last page"></a>
					</c:if>
				</td>
			</tr>
		</table>
		</td>

	<!-- End Pagination cell -->

	<!-- Search cell (for multi-page tables) -->

		<c:if test="${searchFormDisplayed != 0}">
			<form action="<c:out value="${table.postAction}" />" method="POST">
		</c:if>

		<td width="55%" valign="top" align="center" class="table_actions">


				<jsp:include page="../include/showSubmitted.jsp" />
				<c:forEach var="postArg" items="${table.postArgs}">
					<input type="hidden" name="<c:out value="${postArg.key}"/>" value="<c:out value="${postArg.value}"/>" />
				</c:forEach>
				<input type="hidden" name="ebl_page" value="<c:out value="${table.currPageNumber}" />" />
				<input type="hidden" name="ebl_sortColumnInd" value="<c:out value="${table.sortingColumnInd}"/>" />
				<input type="hidden" name="ebl_sortAscending" value="<c:out value="${ascending}"/>"/>
				<input type="hidden" name="ebl_filtered" value="1" />
				<input type="hidden" name="ebl_paginated" value="<c:out value="${paginated}"/>" />
			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td valign="top">
						<div class="formfieldM_BG">
						<input name="ebl_filterKeyword" type="text" class="formfieldM" value="<c:out value="${table.keywordFilter}"/>" />
						</div>
					</td>
					<td valign="top">
						<input type="submit" class="button_search" value="<fmt:message key="find" bundle="${resword}"/>"
							<c:choose>
								<c:when test="${searchFormDisplayed == 0}">
									onClick="if (document.<c:out value="${outerFormName}"/>.ebl_filterKeyword.value == '') return false; document.<c:out value="${outerFormName}"/>.elements['submitted'].value=0;document.<c:out value="${outerFormName}"/>.elements['action'].value='';<c:out value="${searchFormOnClickJS}" escapeXml="false" />"
								</c:when>
								<c:otherwise>
									onClick="if (document.forms[0].ebl_filterKeyword.value == '') return false;"
								</c:otherwise>
							</c:choose>
						/>&nbsp;&nbsp;&nbsp;
					</td>
					<td valign="top">
				</td>

				<!-- hide more columns or not-->
				<td class="table_tools" nowrap><b>
		        <!-- These Numbers represent the tab/table number, number of columns needs to hide, and number of data rows in each table -->
			    <a href="javascript:HideGroups(0,11,<c:out value="${defSize}"/> );">
							<div id="HideGroups" style="display: none">&nbsp;&nbsp;<fmt:message key="hide" bundle="${resword}"/></div>
							<div id="ShowGroups" style="display: all">&nbsp;&nbsp;<fmt:message key="show_more" bundle="${resword}"/></div>
							</a></b></td>

			</table>

		</td>

		<c:if test="${searchFormDisplayed != 0}">
			</form>
		</c:if>

	<!-- End Search cell -->

	<!-- Table Tools/Actions cell -->

		<td width="20%" align="right" valign="top" class="table_actions">
			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<c:set var="isFirstLink" value="${true}" />
					<c:if test="${table.filtered}">
						<td class="table_tools"><a href="<c:out value="${removeFilterQuery}"/>">Clear Search Keywords</a></td>
						<c:set var="isFirstLink" value="${false}" />
					</c:if>
					<c:forEach var="link" items="${table.links}">
						<c:if test="${!isFirstLink}">
							<td class="table_tools">&nbsp;&nbsp;|&nbsp;&nbsp;</td>
						</c:if>
						<td class="table_tools"><b><a href="<c:out value="${link.url}"/>"><c:out value="${link.caption}" /></a></b></td>
						<c:set var="isFirstLink" value="${false}" />
					</c:forEach>
				</tr>
			</table>
		</td>

	<!-- End Table Tools/Actions cell -->
	</tr>

	<!-- end Table Actions row (pagination, search, tools) -->

	<tr>
		<td colspan="3" valign="top">

		<!-- Table Contents -->

			<table border="0" cellpadding="0" cellspacing="0" width="100%">
				<!-- Column Headers -->
				<c:choose>
				<c:when test="${empty table.columns}">
					<tr>
						<td>
					<c:choose>
						<c:when test='${table.noColsMessage == ""}'>
					      <fmt:message key="there_are_no_columns_to_display" bundle="${restext}"/>
						</c:when>
						<c:otherwise>
							<c:out value="${table.noColsMessage}" />
						</c:otherwise>
					</c:choose>
						</td>
					</tr>
				</c:when>
				<c:otherwise>
					<tr valign="top">
					<c:set var="i" value="${0}" />
					<c:forEach var="column" items="${table.columns}">
						<%-- BEGIN SET ORDER BY QUERY --%>
						<c:choose>
							<%-- if the user clicks on the column which is already the current sorting column, flip the sorting order; if he clicks on a different column, default to ascending --%>
							<%-- note that (1 - x) flips a boolean value x, if x = 1 or x = 0 --%>
							<c:when test="${table.sortingColumnInd == i}"><c:set var="showAscending" value="${1 - ascending}" /></c:when>
							<c:otherwise><c:set var="showAscending" value="${1}" /></c:otherwise>
						</c:choose>
						<c:set var="orderByQuery" value="${table.baseGetQuery}&ebl_page=1&ebl_sortColumnInd=${i}&ebl_sortAscending=${showAscending}&ebl_filtered=${filtered}&ebl_filterKeyword=${table.keywordFilter}&ebl_paginated=${paginated}" />
						<%-- END SET ORDER BY QUERY --%>

						<%-- PRINT COLUMN HEADING --%>
						<c:choose>
						<c:when test="${i==0}">
						 <td class="table_header_row_left">
						</c:when>
						<c:otherwise>
						  <c:choose>
						  <c:when test="${i>8 && i<11}">
						   <td class="table_header_row" style="display: none" id="Groups_0_<c:out value="${i}"/>_0">
						  </c:when>
						  <c:otherwise>
						    <td class="table_header_row">
						  </c:otherwise>
						  </c:choose>
						</c:otherwise>
						</c:choose>
							<c:out value="${column.name}" />
						</td>
						<c:set var="i" value="${i + 1}" />
					</c:forEach>
					</tr>
					<!-- End Column Headers -->
					<c:choose>
						<c:when test="${empty table.rows}">
							<tr>
								<td colspan="<c:out value="${i}" />">
							<c:choose>
								<c:when test='${table.noRowsMessage == ""}'>
									<fmt:message key="there_are_no_rows_to_display" bundle="${restext}"/>
								</c:when>
								<c:otherwise>
									<c:out value="${table.noRowsMessage}" />
								</c:otherwise>
							</c:choose>
								</td>
							</tr>
						</c:when>
						<c:otherwise>
							<!-- Data -->
							<c:set var="eblRowCount" value="0" />
							<c:set var="prevRow" scope="request" value="" />
							<c:set var="allRows" scope="request" value="${table.rows}" />
							<c:forEach var="row" items="${table.rows}">
								<c:set var="currRow" scope="request" value="${row}" />
								<c:import url="${rowURL}">
									<c:param name="eblRowCount" value="${eblRowCount}" />
									<c:param name="isFirstLink" value="${isFirstLink}" />	
								</c:import>
								<c:set var="eblRowCount" value="${eblRowCount + 1}" />
								<c:set var="prevRow" scope="request" value="${currRow}" />
							</c:forEach>
							<!-- End Data -->
						</c:otherwise>
					</c:choose>
				</c:otherwise>
				</c:choose>
			</table>

			<!-- End Table Contents -->
			</td>

		</tr>
	</table>
</div>
</div></div></div></div></div></div></div></div>
		</td>
	</tr>
</table>