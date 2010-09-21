<%--
  The Print CRF JSP.
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="com.akazaresearch.tags" prefix="aka_frm" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<html>
<head><title>Print CRF</title>
  <meta http-equiv="content-type" content="text/html; charset=utf-8" />
  <meta http-equiv="X-UA-Compatible" content="IE=8" />
  <script type="text/JavaScript" language="JavaScript" src=
    "includes/global_functions_javascript.js"></script>
  <script type="text/javascript"  language="JavaScript" src=
    "includes/repetition-model/repetition-model.js"></script>
  <link rel="stylesheet" href="includes/styles.css" type="text/css">
  <link rel="stylesheet" href="includes/print_crf.css" type="text/css">
</head>
<jsp:useBean scope="request" id="crfVersionBean" class="org.akaza.openclinica.bean.submit.CRFVersionBean" />
<jsp:useBean scope="request" id="crfBean" class="org.akaza.openclinica.bean.admin.CRFBean" />
<jsp:useBean scope="session" id="studyEvent" class="org.akaza.openclinica.bean.managestudy.StudyEventBean" />
<%-- dataInvolved is a request attribute set in PrintCRFServlet and PrintDataEntryServlet --%>
<c:set var="dataIsInvolved" value="${dataInvolved}" />
<body>
<div class="headerDiv">
<%-- This section was cut-and-pasted from the existing print JSP --%>
<h1><span class="title_manage">
  <c:out value="${crfBean.name}" /> <c:out value="${crfVersionBean.name}" /></span>
  <c:if test="${studySubject != null && studySubject.id>0}">
    <c:choose>
      <c:when test="${EventCRFBean.stage.initialDE}">
        <img src="images/icon_InitialDE.gif" alt="<fmt:message key="initial_data_entry" bundle="${resword}"/>" title="<fmt:message key="initial_data_entry" bundle="${resword}"/>">
      </c:when>
      <c:when test="${EventCRFBean.stage.initialDE_Complete}">
        <img src="images/icon_InitialDEcomplete.gif" alt="<fmt:message key="initial_data_entry_complete" bundle="${resword}"/>" title="<fmt:message key="initial_data_entry_complete" bundle="${resword}"/>">
      </c:when>
      <c:when test="${EventCRFBean.stage.doubleDE}">
        <img src="images/icon_DDE.gif" alt="<fmt:message key="double_data_entry" bundle="${resword}"/>" title="<fmt:message key="double_data_entry" bundle="${resword}"/>">
      </c:when>
      <c:when test="${EventCRFBean.stage.doubleDE_Complete}">
        <img src="images/icon_DEcomplete.gif" alt="<fmt:message key="data_entry_complete" bundle="${resword}"/>" title="<fmt:message key="data_entry_complete" bundle="${resword}"/>">
      </c:when>
      <c:when test="${EventCRFBean.stage.admin_Editing}">
        <img src="images/icon_AdminEdit.gif" alt="<fmt:message key="administrative_editing" bundle="${resword}"/>" title="<fmt:message key="administrative_editing" bundle="${resword}"/>">
      </c:when>
      <c:when test="${EventCRFBean.stage.locked}">
        <img src="images/icon_Locked.gif" alt="<fmt:message key="locked" bundle="${resword}"/>" title="<fmt:message key="locked" bundle="${resword}"/>">
      </c:when>
      <c:otherwise>
        <img src="images/icon_Invalid.gif" alt="<fmt:message key="invalid" bundle="${resword}"/>" title="<fmt:message key="invalid" bundle="${resword}"/>">
      </c:otherwise>
    </c:choose>
  </c:if>
  </h1>
  <jsp:include page="printSubmit.jsp"/>
  </div>
<%-- Begin new group CRF print section. the 'dataIsInvolved' variable
 is a request attribute, true or false, indicating whether the printed CRF is
 associated with an event and data --%>

  <aka_frm:print_tabletag><c:out value="${dataIsInvolved}"/></aka_frm:print_tabletag>

</body>
</html>