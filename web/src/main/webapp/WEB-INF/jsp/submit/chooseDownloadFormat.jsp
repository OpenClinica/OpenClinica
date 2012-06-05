<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>
<html>
<head><title>Choose a format for downloading notes</title></head>
<link rel="stylesheet" href="includes/styles.css" type="text/css">
<script type="text/JavaScript" language="JavaScript" src="includes/global_functions_javascript.js"></script>
<%--<c:set var="subjectId" value="${requestScope[subjectId]}" />

/discrepancyNoteReport.csv--%>
<body style="margin: 5px">
<h2><fmt:message key="choose_download_format" bundle="${resword}"/></h2>

<div id="downloadDiv">
    <form action="DiscrepancyNoteOutputServlet" name="downloadForm">
        <fmt:message key="format" bundle="${resword}"/>: <select id="fmt" name="fmt">
           <option value="csv">comma separated values</option>
           <option value="pdf">portable document format</option>
        </select><br /><br />

        <input type="hidden" name="list" value="y"/>
        <input type="hidden" name="subjectId" value="${subjectId}"/>
        <input type="hidden" name="fileName" value="dnotes${subjectId}_${studyIdentifier}"/>
        <input type="hidden" name="studyIdentifier" value="${studyIdentifier}"/>
        <input type="hidden" name="eventId" value="${param.eventId}"/>
        <input type="hidden" name="resolutionStatus" value="${param.resolutionStatus}"/>
        <input type="hidden" name="discNoteType" value="${param.discNoteType}"/>

<%
        String[] filterParams = {
             "studySubject.label",
             "siteId",
             "studySubject.labelExact",
             "discrepancyNoteBean.createdDate",
             "discrepancyNoteBean.updatedDate",
             "discrepancyNoteBean.description",
             "discrepancyNoteBean.user",
             "discrepancyNoteBean.disType",
             "discrepancyNoteBean.entityType",
             "discrepancyNoteBean.resolutionStatus",
             "age",
             "days",

             "eventName",
             "crfName",
             "entityName",
             "entityValue",
             "discrepancyNoteBean.description",
             "discrepancyNoteBean.user"
        };
        for (String p: filterParams) {
            String value = request.getParameter(p);
            if (value != null) {
%>              <input type="hidden" name="<%= p %>" value="<%= value %>" />
<%
            }
        }

        String[] sortParams = {
            "sort.studySubject.label",
            "sort.discrepancyNoteBean.createdDate",
            "sort.days",
            "sort.age"
        };
        for (String s: sortParams) {
            String value = request.getParameter(s);
            if (value != null) {
%>              <input type="hidden" name="<%= s %>" value="<%= value %>" />
<%            	
            }
        }
%>
        <input type="submit" name="submitFormat" value="<fmt:message key="download_notes" bundle="${resword}"/>" class=
                                  "button_medium" />

        <br />
         <input type="button" name="clsWin" value="<fmt:message key="close_window" bundle="${resword}"/>" class=
                                  "button_medium" onclick="window.close()"/>
    </form>


</div>

</body>
</html>
