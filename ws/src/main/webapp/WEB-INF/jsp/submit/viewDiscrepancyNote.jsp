<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<html>
<head>
    <title><fmt:message key="openclinica" bundle="${resword}"/>- <fmt:message key="view_discrepancy_note" bundle="${resword}"/></title>
    <link rel="stylesheet" href="includes/styles.css" type="text/css">
    <script language="JavaScript" src="includes/global_functions_javascript.js"></script>
    <style type="text/css">

        .popup_BG { background-image: url(images/main_BG.gif);
            background-repeat: repeat-x;
            background-position: top;
            background-color: #FFFFFF;
        }

        .table_cell_left { padding-left: 8px; padding-right: 8px; }
    </style>

</head>
<body class="popup_BG" style="margin: 0px 12px 0px 12px;">


<!-- Alert Box -->

<!-- These DIVs define shaded box borders -->


<jsp:include page="../include/alertbox.jsp"/>

<!-- End Alert Box -->

<div style="float: left;"><h1 class="title_manage"><fmt:message key="view_discrepancy_notes" bundle="${resword}"/></h1></div>
<div style="float: right;"><p><a href="#" onclick="javascript:window.close();"><fmt:message key="close_window" bundle="${resword}"/></a></p></div>
<br clear="all">


<!-- Entity box -->
<table border="0" cellpadding="0" cellspacing="0">
    <tr>
        <td valign="bottom">
            <table border="0" cellpadding="0" cellspacing="0">
                <tr>
                    <td nowrap style="padding-right: 20px;">
                        <div class="tab_BG_h"><div class="tab_R_h" style="padding-right: 0px;"><div class="tab_L_h" style="padding: 3px 11px 0px 6px; text-align: left;">

                            <b>
                                <c:choose>
                                    <c:when test="${name eq 'itemData' ||name eq 'ItemData'}">
                                        <a href="javascript: openDocWindow('ViewItemDetail?itemId=<c:out value="${item.id}"/>')"><c:out value="${entityName}"/></a>
                                    </c:when>
                                    <c:otherwise>
                                        <c:choose>
                                            <c:when test="${entityName != '' && entityName != null }">
                                                  <c:out value="${entityName}"/>  =  <c:out value="${entityValue}"/>
                                            </c:when>
                                            <c:otherwise>
                                                <%-- nothing here; if entityName is blank --%>
                                            </c:otherwise>
                                        </c:choose>

                                    </c:otherwise>
                                </c:choose>
                            </b>
                        </div></div></div>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
    <tr>

        <td valign="top">

            <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TR"><div class="box_BL"><div class="box_BR">

                <div class="textbox_center">

                    <table border="0" cellpadding="0" cellspacing="0">
                        <tr>
                            <td class="table_cell_noborder" style="color: #789EC5"><b><fmt:message key="subject" bundle="${resword}"/>:&nbsp;&nbsp;</b></td>
                            <td class="table_cell_noborder" style="color: #789EC5"><c:out value="${noteSubject.label}"/></td>
                            <td class="table_cell_noborder" style="color: #789EC5; padding-left: 40px;"><b><fmt:message key="event" bundle="${resword}"/>:&nbsp;&nbsp;</b></td>
                            <td class="table_cell_noborder" style="color: #789EC5">
                                <c:choose>
                                    <c:when test="${studyEvent != null}">
                                        <c:out value="${studyEvent.name}"/>
                                    </c:when>
                                    <c:otherwise>N/A
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                        <tr>
                            <td class="table_cell_noborder" style="color: #789EC5"><b><fmt:message key="event_date" bundle="${resword}"/>:&nbsp;&nbsp;</b></td>
                            <td class="table_cell_noborder" style="color: #789EC5">
                                <c:choose>
                                    <c:when test="${studyEvent != null}">
                                        <fmt:formatDate value="${studyEvent.dateStarted}" pattern="${dteFormat}"/>&nbsp;
                                    </c:when>
                                    <c:otherwise>N/A
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="table_cell_noborder" style="color: #789EC5; padding-left: 40px;"><b><fmt:message key="CRF" bundle="${resword}"/>:&nbsp;&nbsp;</b></td>
                            <td class="table_cell_noborder" style="color: #789EC5">
                                <c:choose>
                                    <c:when test="${crf != null}">
                                        <c:out value="${crf.name}"/>
                                    </c:when>
                                    <c:otherwise>N/A
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </table>

                </div>

            </div></div></div></div></div></div></div>

        </td>
    </tr>

</table>

<h3 class="title_manage"><fmt:message key="note_details" bundle="${resword}"/></h3>

<c:set var="count" value="${1}"/>
<!-- Thread Heading -->
<c:forEach var="note" items="${discrepancyNotes}">
<table border="0" cellpadding="0" cellspacing="0">
    <tbody>
        <tr>
            <td>
                <!-- These DIVs define shaded box borders -->
                <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
                    <div class="tablebox_center">
                        <table border="0" cellpadding="0" cellspacing="0" width="600">
                            <tr class="aka_stripes">
                                <td class="aka_header_border" colspan="4">
                                    <div style="float: left; font-size: 15px;">

                                        <!-- expand/collapse button -->

                                        <a href="javascript:leftnavExpand('thread<c:out value="${count}"/>');leftnavExpand('thread<c:out value="${count}"/>_expand');leftnavExpand('thread<c:out value="${count}"/>_collapse');">
                                            <div id="thread<c:out value="${count}"/>_collapse" style="width: 8px; height: 8px; border-color: #789EC5; border-width: 1px; border-style: solid; line-height: 6px; text-align: center; float: left; margin: 3px 6px 0px 0px; ">-</div>
                                            <div id="thread<c:out value="${count}"/>_expand" style="width: 8px; height: 8px; border-color: #789EC5; border-width: 1px; border-style: solid; font-size: 10px; line-height: 6px; text-align: center; float: left; margin: 3px 6px 0px 0px; display: none;">+</div>
                                        </a>

                                        <!-- Thread title -->


                                        <b><c:out value="${note.value.description}"/>
                                            <c:if test="${note.value.saved==false}">
                                                <span class="alert">[<fmt:message key="not_saved" bundle="${resword}"/>]</span>
                                            </c:if></b>
                                    </div>
                                    <div style="float: right; padding-left: 30px;"><b>Last updated:</b> <fmt:formatDate value="${note.value.lastDateUpdated}" pattern="${dteFormat}"/> by <c:out value="${note.value.owner.name}"/></div>
                                </td>
                            </tr>
                            <tr class="aka_stripes">
                                <td class="aka_header_border" width="25%"><b><fmt:message key="ID" bundle="${resword}"/>:</b> <c:out value="${note.value.id}"/></td>
                                <td class="aka_header_border" width="25%"><b><fmt:message key="type" bundle="${resword}"/>:</b> <c:out value="${note.value.disType.name}"/></td>
                                <td class="aka_header_border" width="25%"><b><fmt:message key="resolution_status" bundle="${resword}"/>:</b> <c:out value="${note.value.resStatus.name}"/></td>
                                <td class="aka_header_border" width="25%"><b><fmt:message key="of_notes" bundle="${resword}"/>:</b> <c:out value="${note.value.numChildren}" /></td>
                            </tr>
                        </table>
                        <table border="0" cellpadding="0" cellspacing="0" width="600" id="thread<c:out value="${count}"/>">

                            <!-- Spacer row --->
                            <tr>
                                <td class="table_header_row_left" colspan="4"  style="border-top-width: 1px; border-top-color: #CCCCCC; font-size: 1px; line-height: 4px; height: 6px; padding: 0px;">&nbsp;</td>
                            </tr>

                                <%--do not display the parent note itself because there is a child which is same as the parent--%>
                            <!-- First post , the note itself-->

                            <!--
	 <tr>	   
		<td class="table_cell_left" colspan="2" bgcolor="#f5f5f5" width="50%" valign="top"><b><c:out value="${note.value.description}"/></b></td>
		<td class="table_cell" bgcolor="#f5f5f5" width="25%" valign="top" nowrap><fmt:formatDate value="${note.value.createdDate}" pattern="${dteFormat}"/> by <c:out value="${note.value.owner.name}"/></td>
		<td class="table_cell" bgcolor="#f5f5f5" align="right" width="25%" valign="top" nowrap><fmt:message key="status" bundle="${resword}"/>: <c:out value="${note.value.resStatus.name}"/></td>
	   </tr>
	  <tr>
		<td class="table_cell_left" colspan="4">
		<c:out value="${note.value.detailedNotes}"/>
		</td>
	   </tr> 
	   -->
                            <!-- all child notes if any -->
                            <c:forEach var="child" items="${note.value.children}" varStatus="status">
                                <tr>
                                    <td class="table_cell_left" colspan="2" bgcolor="#f5f5f5" width="50%" valign="top"><b><c:out value="${child.description}"/></b></td>
                                    <td class="table_cell" bgcolor="#f5f5f5" width="25%" valign="top" nowrap><fmt:formatDate value="${child.createdDate}" pattern="${dteFormat}"/> by <c:out value="${child.owner.name}"/></td>
                                    <td class="table_cell" bgcolor="#f5f5f5" align="right" width="25%" valign="top" nowrap><fmt:message key="status" bundle="${resword}"/>: <c:out value="${child.resStatus.name}"/></td>
                                </tr>
                                <c:if test="${child.assignedUserId > 0}">
                                <tr>
                                    <td class="table_cell_left" colspan="4">
                                        <b>Assigned to:</b> <c:out value="${child.assignedUser.firstName}"/> <c:out value="${child.assignedUser.lastName}"/> (<c:out value ="${child.assignedUser.name}"/>)
                                    </td>
                                </tr>
                                </c:if>
                                
                                <tr>
                                    <td class="table_cell_left" colspan="4">
                                        <c:out value="${child.detailedNotes}"/>
                                    </td>
                                </tr>

                                <c:if test="${!status.last}">
                                    <!-- Spacer row --->
                                    <tr>
                                        <td class="table_header_row_left" colspan="4"  style="border-top-width: 1px; border-top-color: #CCCCCC; font-size: 1px; line-height: 4px; height: 6px; padding: 0px;">&nbsp;</td>
                                    </tr>
                                </c:if>

                            </c:forEach>
                            <c:if test="${isLocked eq 'no'}">
                                <tr>
                                    <td class="table_cell_left" colspan="4" align="right">
                                        <c:if test="${note.value.id>0 && note.value.resStatus.id != 5}">
                                            <a href="CreateDiscrepancyNote?subjectId=<c:out value="${noteSubject.id}" />&itemId=<c:out value="${item.id}" />&id=<c:out value='${note.value.entityId}'/>&name=<c:out value='${note.value.entityType}'/>&field=<c:out value='${note.value.field}'/>&column=<c:out value='${note.value.column}'/>&parentId=<c:out value='${note.value.id}'/>&enterData=<c:out value='${enterData}'/>&monitor=<c:out value='${monitor}'/>&writeToDB=<c:out value='${writeToDB}'/>">
                                                <fmt:message key="reply_to_thread" bundle="${resword}"/></a>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:if>
                        </table>

                    </div>
                </div></div></div></div></div></div></div></div>
            </td>
        </tr>
    </tbody>
</table>
<c:set var="count" value="${count+1}"/>
<br>
</c:forEach>
<c:if test="${isLocked eq 'no'}">
    <p><a href="CreateDiscrepancyNote?subjectId=<c:out value="${noteSubject.id}" />&itemId=<c:out value="${item.id}" />&id=<c:out value='${id}'/>&name=<c:out value='${name}'/>&field=<c:out value='${field}'/>&column=<c:out value='${column}'/>&parentId=0&enterData=<c:out value='${enterData}'/>&monitor=<c:out value='${monitor}'/>&writeToDB=<c:out value="${writeToDB}"/>&new=1"><b><fmt:message key="begin_new_thread" bundle="${resword}"/></b></a></p>
</c:if>
</body>
</html>

