<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.page_messages" var="respage"/>

<c:choose>
<c:when test="${userBean.sysAdmin}">
 <c:import url="../include/admin-head-prev.jsp"/>
</c:when>
<c:otherwise>
 <c:import url="../include/managestudy-head-prev.jsp"/>
</c:otherwise>
</c:choose>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
		<td class="sidebar_tab">

		<a href=
      "javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		<div class="sidebar_tab_content">

		</div>

		</td>

	</tr>
<%-- remove display?  style="display: all"--%>
  <tr id="sidebar_Instructions_closed">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		</td>
  </tr>

<jsp:include page="../include/sideInfo_prev.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='excelErrors' class='java.util.ArrayList'/>
<jsp:useBean scope='session' id='htmlTable' class='java.lang.String'/>
<jsp:useBean scope='session' id='version' class='org.akaza.openclinica.bean.submit.CRFVersionBean'/>
<jsp:useBean scope='session' id='crfName' class='java.lang.String'/>


<h1><span class="title_manage"><fmt:message key="check_CRF_version_data" bundle="${resword}"/></span></h1>


<c:choose>
<c:when test="${empty excelErrors}">
 <c:if test="${!empty warnings}">
  <p><fmt:message key="warnings" bundle="${resword}"/>:<p>
  <c:forEach var="warning" items="${warnings}">
    <span class="alert"><c:out value="${warning}"/></span><br/>
  </c:forEach>
 </c:if>
  <%-- Move to alerts
<br/><fmt:message key="congratulations_your_spreadsheet" bundle="${resword}"/>
<br/><b><form action=
  "ViewSectionDataEntry?crfVersionId=<c:out value=
"${version.crfId}"/>&tabId=1" method="post">  --%>

<p>
<table border="0">
<tr>
  <%-- ViewSectionDataEntry?crfVersionId=<c:out value=
"${version.crfId}"/>&tabId=1
<form action="javascript:void(0)"><a href="SectionPreview?tabId=1"  target="_blank"><input type="submit" name="submit" value=
   "Preview CRF" class="button_medium"></a></form>--%>
  <td><b><form action="CreateCRFVersion?action=confirmsql&crfId=<c:out value=
"${version.crfId}"/>&name=<c:out value="${version.name}"/>" method="post">
    <%-- The user shouldn't save the new or revised CRF by clicking the button more than once, so the
    button is disabled after it's clicked. --%>
 <input type="submit" name="submit" value="<fmt:message key="continue" bundle="${resword}"/>" class="button_medium"></form></b> </td>
 <td><b><form action="ListCRF" method="post">
 <input type="submit" name="submit" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button_medium"></form></b> </td>
  </tr>
  </table>
<br/>

<jsp:include page="../managestudy/viewSectionDataPreview.jsp"/><br><br>

 <table border="0">
<tr><td><b><form action="CreateCRFVersion?action=confirmsql&crfId=<c:out value=
"${version.crfId}"/>&name=<c:out value="${version.name}"/>" method="post">
    <%-- The user shouldn't save the new or revised CRF by clicking the button more than once, so the
    button is disabled after it's clicked. --%>
 <input type="submit" name="submit" value="<fmt:message key="continue" bundle="${resword}"/>" class="button_medium"></form></b> </td>
 <td><b><form action="ListCRF" method="post">
 <input type="submit" name="submit" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button_medium"></form></b> </td>
  </tr>
  </table>

</c:when>
<c:otherwise>
<br/>
<fmt:message key="there_were_several_invalid_fields" bundle="${restext}"/> <br>
<fmt:message key="click" bundle="${resword}"/> <input type="submit" name="submit" value="<fmt:message key="go_back" bundle="${restext}"/>" class="button" onclick="javascript:window.location.href='CreateCRFVersion?module=&crfId=<c:out value="${version.crfId}"/>'">
<fmt:message key="to_go_back_and_upload" bundle="${restext}"/>
<br/>
<c:forEach var="error" items="${excelErrors}">
<span class="alert"><c:out value="${error}"/><br></span>
</c:forEach>
<br>
<%=htmlTable%>

</c:otherwise>
</c:choose>

<c:choose>
  <c:when test="${userBean.sysAdmin}">
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
