<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="core.org.akaza.openclinica.logic.importdata.*" %>
<%@ page import="java.io.*" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.slf4j.Logger" %>
<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="java.net.URLEncoder" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterms"/>
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
			<span class="icon icon-caret-down gray"></span>
		</a>
		<fmt:message key="instructions" bundle="${restext}"/>
		<div class="sidebar_tab_content">
			<fmt:message key="upload_side_bar_instructions" bundle="${restext}"/>
		</div>
	</td>
</tr>

<tr id="sidebar_Instructions_closed" style="display: none">
	<td class="sidebar_tab">
		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
			<span class="icon icon-caret-right gray"></span>
		</a>
		<fmt:message key="instructions" bundle="${restext}"/>
	</td>
</tr>

<jsp:include page="../include/sideInfo.jsp"/>

<div>
  <a href="ImportCRFData">Import CRF Data</a>
  <div>
    Upload an xml file that contains CRF data.
  </div>
</div>

<br>

<div>
	<a href="UploadCRFData">Upload CRF Data</a>
  <div>
    Upload pipe delimited text files which contains CRF data and one data mapping file.
  </div>
</div>

<jsp:include page="../include/footer.jsp"/>
