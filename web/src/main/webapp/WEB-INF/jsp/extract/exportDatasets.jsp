<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>

<jsp:include page="../include/extract-refresh-header.jsp"/>
<script type="text/javascript">
    var openDataExtractLink = true;
    function openDoc(inURL) {
        if(openDataExtractLink){
            openDataExtractLink = false;
            openDocWindow(inURL);
        }
    }
</script>

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

<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope="request" id="dataset" class="org.akaza.openclinica.bean.extract.DatasetBean"/>
<jsp:useBean scope="request" id="filelist" class="java.util.ArrayList"/>
<h1><span class="title_manage"><fmt:message key="download_data" bundle="${resword}"/>: <c:out value="${dataset.name}"/> <a href="javascript:openDocWindow('https://docs.openclinica.com/3.1/openclinica-user-guide/export-datasets')"><img src="images/bt_Help_Manage.gif" border="0" alt="<fmt:message key="help" bundle="${resword}"/>" title="<fmt:message key="help" bundle="${resword}"/>"></a></span></h1>

<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center" align="center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">

  <tr valign="top" ><td class="table_header_column"><fmt:message key="dataset_name" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${dataset.name}"/>
  </td></tr>
  <tr valign="top" ><td class="table_header_column"><fmt:message key="dataset_description" bundle="${resword}"/>:</td><td class="table_cell">
  <c:out value="${dataset.description}"/>
  </td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="item_status" bundle="${resword}"/>:</td><td class="table_cell"> <c:out value="${dataset.datasetItemStatus.description}"/>
  </td></tr>
</table>
</div>

</div></div></div></div></div></div></div></div>
</div>

<p><fmt:message key="export_dataset_download1" bundle="${restext}"/></p>

<input type="hidden" name="datasetId" value="<c:out value="${dataset.id}"/>"/>
<table border="0" cellpadding="5" width="525">
<tr valign="top">
    <td class="text">
	<ul><%-- <li><a href="ExportDataset?action=html&datasetId=<c:out value="${dataset.id}"/>"><fmt:message key="view_as_HTML" bundle="${resword}"/></a></li> --%>
	<c:forEach var="extract" items="${extractProperties}">
			<%-- use fn:startsWith(extract.filedescription, '&') here, for i18n --%>
			<li>
				<c:choose>
					<c:when test="${fn:startsWith(extract.filedescription, '&')==true}">
						<fmt:message key="${fn:substringAfter(extract.filedescription, '&')}" bundle="${restext}"/>&nbsp;
					</c:when>
					<c:otherwise>
						<c:out value="${extract.filedescription}"/>&nbsp;
					</c:otherwise>
				</c:choose>
				<a title='
					<c:choose>
						<c:when test="${fn:startsWith(extract.helpText, '&')==true}">
							<fmt:message key="${fn:substringAfter(extract.helpText, '&')}" bundle="${restext}"/>&nbsp;
						</c:when>
						<c:otherwise><c:out value="${extract.helpText}"/></c:otherwise>
					</c:choose>
				' href='pages/extract?id=<c:out value="${extract.id}"/>&datasetId=<c:out value="${dataset.id}"/>'>
				<c:choose>
					<c:when test="${fn:startsWith(extract.linkText, '&')==true}">
						<fmt:message key="${fn:substringAfter(extract.linkText, '&')}" bundle="${restext}"/>&nbsp;
					</c:when>
					<c:otherwise>
						<c:out value="${extract.linkText}"/>&nbsp;
					</c:otherwise>
				</c:choose>
				</a>
				<%--<c:out value="${extract.linkText}"/>--%>
			</li>
    </c:forEach>	
	<%--<li><fmt:message key="download_a_file" bundle="${resword}"/>:
		<ul>
		<li><a href="javascript:openDoc('ExportDataset?action=txt&datasetId=<c:out value="${dataset.id}"/>')"><fmt:message key="tab_delimited_text" bundle="${resword}"/></a></li>
		--%>
		<%--<li><a href="javascript:openDocWindow('ExportDataset?action=csv&datasetId=<c:out value="${dataset.id}"/>')"><fmt:message key="comma_delimited_text" bundle="${resword}"/></a></li>--%>
		<%-- 
		<li><a href="javascript:openDoc('ExportDataset?action=spss&datasetId=<c:out value="${dataset.id}"/>')"><fmt:message key="SPSS_syntax_and_data" bundle="${resword}"/></a>&nbsp;<a href="javascript:openDocWindow('https://docs.openclinica.com/3.1/openclinica-user-guide/spss-file-specifications')"><img src="images/bt_Help_Extract.gif" alt="<fmt:message key="help" bundle="${resword}"/>" title="<fmt:message key="help" bundle="${resword}"/>" border="0"></a></li>
		<li><a href="javascript:openDoc('ExportDataset?action=odm&datasetId=<c:out value="${dataset.id}"/>&odmVersion=1.3')"><fmt:message key="CDISC_ODM_XML_format" bundle="${resword}"/>&nbsp;1.3</a></li>
		<li><a href="javascript:openDoc('ExportDataset?action=odm&datasetId=<c:out value="${dataset.id}"/>&odmVersion=1.3&xalan=1')">Generate the 1.3 XML and SQL Dataset with XSLT</a></li>
		<li><a href="javascript:openDoc('ExportDataset?action=odm&datasetId=<c:out value="${dataset.id}"/>&odmVersion=1.2')"><fmt:message key="CDISC_ODM_XML_format" bundle="${resword}"/>&nbsp;1.2</a></li>
		<c:forEach var="extract" items="${extractProperties}">
			<li><a href='<c:out value="${extract.id}"/>'><c:out value="${extract.linkText}"/></a></li>
		</c:forEach>
		--%>
		<%--
		<li><a href="javascript:openDoc('ExportDataset?action=odm&datasetId=<c:out value="${dataset.id}"/>&odmVersion=1.2&xalan=1')">Generate the 1.2 XML and SQL Dataset with XSLT</a></li>
		--%>
		<%-- 
			<ul><li><fmt:message key="import_CDISC_ODM_XML_into_SAS" bundle="${resword}"/>&nbsp;<a href="javascript:openDoc('help/4_5_sasSpec_Help.html')"><img src="images/bt_Help_Extract.gif" alt="<fmt:message key="help" bundle="${resword}"/>" title="<fmt:message key="help" bundle="${resword}"/>" border="0"></a></li></ul></li>
		<li><fmt:message key="odm_openclinica_extension" bundle="${resword}"/></li>
			<ul>
			<li><a href="javascript:openDoc('ExportDataset?action=odm&datasetId=<c:out value="${dataset.id}"/>&odmVersion=oc1.3')">OpenClinica-Extension-1.3</a></li>
			<li><a href="javascript:openDoc('ExportDataset?action=odm&datasetId=<c:out value="${dataset.id}"/>&odmVersion=oc1.2')">OpenClinica-Extension-1.2</a></li>
			</ul
		</ul>
	</li>--%>
	</ul>
	</td>
</tr>

<%--<tr valign="top">
    <td class="text"><input type="radio" name="action" value="txt" checked/> Tab Delimited Text</td>
</tr>
<tr valign="top">
    <td class="text"><input type="radio" name="action" value="csv"/> Comma Delimited Text</td>
</tr>
<tr valign="top">
    <td class="text"><input type="radio" name="action" value="html"/> Html</td>
</tr>
<tr valign="top">
    <td class="text"><input type="radio" name="action" value="spss"/> SPSS Formats (text)</td>
</tr>  --%>


<%--
<tr valign="top">
    <td class="text"><input type="radio" name="action" value="excel"/> Excel</td>
</tr>
--%>

<%--<tr align="left">
<!--	<td><input type="button" value="Download Data" onClick="sendToPage(theForm.datasetId.value,
      			theForm.action)" class="button_xlong"/></tr>-->
<td><input type="submit" value="Download Data" class="button_xlong"/></td>
</tr>--%>

</table>

<P><b><fmt:message key="note" bundle="${resword}"/>: </b><fmt:message key="export_dataset_download2" bundle="${restext}"/>

<p><fmt:message key="export_dataset_download3" bundle="${restext}"/><a href="javascript:openDocWindow('https://docs.openclinica.com/3.1/openclinica-user-guide/export-datasets')"><img src="images/bt_Help_Extract.gif" border="0" alt="<fmt:message key="help" bundle="${resword}"/>" title="<fmt:message key="help" bundle="${resword}"/>"></a> <fmt:message key="export_dataset_download6" bundle="${restext}"/>
<%--<P>You may select all, copy and paste the text generated into a text editor or excel spreadsheet.
--%>

<p><e><fmt:message key="internet_explorer_users" bundle="${resword}"/></i>: <fmt:message key="export_dataset_download4" bundle="${restext}"/><a href="javascript:openDocWindow('https://docs.openclinica.com/3.1/openclinica-user-guide/export-datasets')"><img src="images/bt_Help_Extract.gif" border="0" alt="<fmt:message key="help" bundle="${resword}"/>" title="<fmt:message key="help" bundle="${resword}"/>"></a> <fmt:message key="export_dataset_download5" bundle="${restext}"/>

<P><fmt:message key="archive_of_exported_dataset_files" bundle="${resword}"/>:</P>

<c:import url="../include/showTable.jsp">
<c:param name="rowURL" value="showArchivedDatasetFileRow.jsp" />
</c:import>

<jsp:include page="../include/footer.jsp"/>
