<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>


<jsp:useBean id="id" scope="request" class="java.lang.String"/>
<jsp:useBean id="name" scope="request" class="java.lang.String"/>
<jsp:useBean id="field" scope="request" class="java.lang.String"/>
<jsp:useBean id="column" scope="request" class="java.lang.String"/>
<jsp:useBean id="viewDNLink" scope="request" class="java.lang.String"/>
<jsp:useBean id="boxDNMap" scope="session" class="java.util.HashMap"/>
<jsp:useBean scope='session' id='boxToShow'  class="java.lang.String"/>
<jsp:useBean id="typeID0" scope="request" class="java.lang.String"/>
<jsp:useBean id="y" scope="request" class="java.lang.String"/>
<jsp:useBean id="refresh" scope="request" class="java.lang.String"/>


<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>
<html>
<head>
    <title><fmt:message key="openclinica" bundle="${resword}"/>- <fmt:message key="view_discrepancy_note" bundle="${resword}"/></title>
    <link rel="stylesheet" href="includes/styles.css" type="text/css">
    <script language="JavaScript" src="includes/global_functions_javascript.js"></script>
    <script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.min.js"></script>
    <script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jmesa.js"></script>
    <script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.jmesa.js"></script>
    <script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery-migrate-1.1.1.js"></script>
	<script type="text/javascript" language="javascript">jQuery.noConflict();</script>
	
    <script language="JavaScript">
        function scrollToElement(id) {
            alert(id);
            alert(document.getElementById(id).offsetTop);
            window.scrollTo(0,(document.getElementById(id)).offsetTop);
        }

        function findPosition(obj) {
            var curtop = 0;
            if (obj.offsetParent) {
                do {
                    curtop += obj.offsetTop;
                } while (obj = obj.offsetParent);
            return [curtop];
            }
        }
    </script>

    <style type="text/css">

        .popup_BG { background-image: url(images/main_BG.gif);
            background-repeat: repeat-x;
            background-position: top;
            background-color: #FFFFFF;
        }

        .table_cell_left { padding-left: 8px; padding-right: 8px; }
    </style>

</head>

<body class="popup_BG" style="margin: 0px 12px 0px 12px;" onload="window.scrollTo(0,'<c:out value="${y}"/>');javascript:setStatusWithId('<c:out value="${typeID0}"/>','0','<c:out value="${whichResStatus}"/>','<fmt:message key="New" bundle="${resterm}"/>','<fmt:message key="Updated" bundle="${resterm}"/>','<fmt:message key="Resolution_Proposed" bundle="${resterm}"/>','<fmt:message key="Closed" bundle="${resterm}"/>','<fmt:message key="Not_Applicable" bundle="${resterm}"/>');javascript:refreshSource('<c:out value="${refresh}"/>', '/ViewNotes?');">


<!-- Alert Box -->

<!-- These DIVs define shaded box borders -->

<%-- <jsp:include page="../include/alertbox.jsp"/> --%>

<!-- End Alert Box -->

<div style="float: left;"><h1 class="title_manage"><c:out value="${entityName}"/>: <fmt:message key="view_discrepancy_notes" bundle="${resword}"/></h1></div>
<div style="float: right;"><p><a href="#" onclick="javascript:window.close();"><fmt:message key="exit_window" bundle="${resword}"/></a></p></div>
<br clear="all">


<!-- Entity box -->
<table border="0" cellpadding="0" cellspacing="0" style="float:left;">
    <tr>
        <td valign="bottom">
            <table border="0" cellpadding="0" cellspacing="0">
                <tr>
                    <td nowrap style="padding-right: 20px;">
                        <div class="tab_BG_h"><div class="tab_R_h" style="padding-right: 0px;"><div class="tab_L_h" style="padding: 3px 11px 0px 6px; text-align: left;">
						<b><c:choose>
                            <c:when test="${entityName != '' && entityName != null }">
                                  "<c:out value="${entityName}"/>"
                            </c:when>
                            <c:otherwise>
                                <%-- nothing here; if entityName is blank --%>
                            </c:otherwise>
                        </c:choose>
                        <fmt:message key="Properties" bundle="${resword}"/>:</b>
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
                <td class="table_cell_noborder"><fmt:message key="subject" bundle="${resword}"/>:&nbsp;&nbsp;</td>
                <td class="table_cell_noborder"><b><c:out value="${noteSubject.label}"/></b></td>
                <td class="table_cell_noborder" padding-left: 40px;"><fmt:message key="event" bundle="${resword}"/>:&nbsp;&nbsp;</td>
                <td class="table_cell_noborder">
                    <b><c:choose>
                        <c:when test="${studyEvent != null}">
                            <c:out value="${studyEvent.name}"/>
                        </c:when>
                        <c:otherwise>N/A
                        </c:otherwise>
                    </c:choose></b>
                </td>
            	</tr>
            	<tr>
                <td class="table_cell_noborder"><fmt:message key="event_date" bundle="${resword}"/>:&nbsp;&nbsp;</td>
                <td class="table_cell_noborder">
                    <b><c:choose>
                        <c:when test="${studyEvent != null}">
                            <fmt:formatDate value="${studyEvent.dateStarted}" pattern="${dteFormat}"/>&nbsp;
                        </c:when>
                        <c:otherwise>N/A
                        </c:otherwise>
                    </c:choose></b>
                </td>
                <td class="table_cell_noborder" padding-left: 40px;"><fmt:message key="CRF" bundle="${resword}"/>:&nbsp;&nbsp;</td>
                <td class="table_cell_noborder">
                    <b><c:choose>
                        <c:when test="${crf != null}">
                            <c:out value="${crf.name}"/>
                        </c:when>
                        <c:otherwise>N/A
                        </c:otherwise>
                    </c:choose></b>
                </td>
            	</tr>
            	<tr>
            	<td class="table_cell_noborder"><fmt:message key="Current_Value" bundle="${resword}"/>:&nbsp;&nbsp;</td>
                <td class="table_cell_noborder"><b><c:out value="${entityValue}"/>&nbsp;</b></td>
                <td class="table_cell_noborder" padding-left: 40px;"><fmt:message key="More" bundle="${resword}"/>:&nbsp;&nbsp;</td>
                <td class="table_cell_noborder">
                <c:choose>
                <c:when test="${name eq 'itemData' ||name eq 'ItemData'}">
                    <a href="javascript: openDocWindow('ViewItemDetail?itemId=<c:out value="${item.id}"/>')">
                    <fmt:message key="Data_Dictionary" bundle="${resword}"/></a>
                </c:when>
                <c:otherwise>
                    <a href="javascript:scrollToY('audit');"><fmt:message key="Audit_History" bundle="${resword}"/></a>
                </c:otherwise>
                </c:choose>
                </td>
                <c:if test="${name eq 'itemData' ||name eq 'ItemData'}">
	                <tr>
	                <td class="table_cell_noborder">
	                <td class="table_cell_noborder">
	                <td class="table_cell_noborder">
	                <td class="table_cell_noborder">
	                     <a href="javascript:scrollToY('audit');"><fmt:message key="Audit_History" bundle="${resword}"/></a>
	                </td>
	            	</tr>
            	</c:if>
			</table>
			</div>
        </div></div></div></div></div></div></div>
    </td>
    </tr>

</table>


<div style="clear:both;"></div>
<h3 class="title_manage"><fmt:message key="note_details" bundle="${resword}"/></h3>

<div class="alert">    
<c:forEach var="message" items="${pageMessages}">
 <c:out value="${message}" escapeXml="false"/><br><br>
</c:forEach>
</div>

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
                                    <div style="float: right; padding-left: 30px;">
                                    	<fmt:message key="Last_updated" bundle="${resword}"/>: <b><fmt:formatDate value="${note.value.lastDateUpdated}" pattern="${dteFormat}"/> by <c:out value="${note.value.lastUpdator.name}"/></b><br>
                                    	<fmt:message key="Assigned_to" bundle="${resword}"/>:&nbsp;&nbsp;  <b> <c:out value="${note.value.assignedUser.firstName}"/> <c:out value="${note.value.assignedUser.lastName}"/> (<c:out value ="${note.value.assignedUser.name}"/>)
                                    </div>
                                </td>
                            </tr>
                            <tr class="aka_stripes">
                                <td class="aka_header_border" width="25%"><fmt:message key="ID" bundle="${resword}"/>: <b><c:out value="${note.value.id}"/></b></td>
                                <td class="aka_header_border" width="25%"><fmt:message key="type" bundle="${resword}"/>: <b><c:out value="${note.value.disType.name}"/></b></td>
                                <td class="aka_header_border" width="25%">Current Status: <b><c:out value="${note.value.resStatus.name}"/></b></td>
                                <td class="aka_header_border" width="25%"><fmt:message key="of_notes" bundle="${resword}"/>: <b><c:out value="${note.value.numChildren}" /></b></td>
                            </tr>
                        </table>
                        <table border="0" cellpadding="0" cellspacing="0" width="600" id="thread<c:out value="${count}"/>">

                            <!-- Spacer row --->
                            <tr>
                                <td class="table_header_row_left" colspan="4"  style="border-top-width: 1px; border-top-color: #CCCCCC; font-size: 1px; line-height: 4px; height: 6px; padding: 0px;">&nbsp;</td>
                            </tr>

                                <%--do not display the parent note itself because there is a child which is same as the parent--%>
                            <!-- all child notes if any -->
                            <c:forEach var="child" items="${note.value.children}" varStatus="status">
                                <tr>
                                    <td class="table_cell_left" colspan="2" bgcolor="#f5f5f5" width="50%" valign="top"><b><c:out value="${child.description}"/></b></td>
                                    <td class="table_cell" bgcolor="#f5f5f5" align="left" width="25%" valign="top" nowrap><fmt:message key="status" bundle="${resword}"/>: <c:out value="${child.resStatus.name}"/></td>
                                    <td class="table_cell" bgcolor="#f5f5f5" width="25%" align="right" valign="top" nowrap>
                                    	<fmt:formatDate value="${child.createdDate}" pattern="${dteFormat}"/> by <c:out value="${child.owner.name}"/><br>
                                    	<c:if test="${child.assignedUserId > 0}">
                                        <fmt:message key="Assigned_to" bundle="${resword}"/>: <c:out value="${child.assignedUser.firstName}"/> <c:out value="${child.assignedUser.lastName}"/> (<c:out value ="${child.assignedUser.name}"/>)
                                </tr>
                                </c:if>
                                    </td>
                                </tr>
                                
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
                            <c:set var="showDNBox" value="n"/>
                            <c:if test="${!study.status.locked}">
                            	<tr>
                            	<td class="table_cell_left" colspan="4" align="right">
                            		<c:if test="${(note.value.id>0 && note.value.resStatus.id != 5) && !(note.value.resStatus.id == 4 && whichResStatus == '22')}">
										<c:set var="sindex" value="0"/>
                            			<c:forEach var="status" items="${resolutionStatuses}">
                        					<c:choose>
                        					<c:when test="${status.id == 2}">
                        						<input class="button_medium" type="button" id="resStatus${status.id}${note.value.id}" value="<fmt:message key="updaate_note" bundle="${resterm}"/>" onclick="javascript:boxShowWithDefault('<c:out value="${note.value.id}"/>','<c:out value="${sindex}"/>','<c:out value="${status.id}"/>','<c:out value="${status.name}"/>');/*scrollToElement('<c:out value="submitBtn${note.value.id}"/>');*/"/>
            								</c:when>
            								<c:when test="${status.id == 3}">
                        						<input class="button_medium" type="button" id="resStatus${status.id}${note.value.id}" value="<fmt:message key="Propose_Resolution" bundle="${resterm}"/>" onclick="javascript:boxShowWithDefault('<c:out value="${note.value.id}"/>','<c:out value="${sindex}"/>','<c:out value="${status.id}"/>','<c:out value="${status.name}"/>');"/>
            								</c:when>
            								<c:when test="${status.id == 4}">
                        						<input class="button_medium" type="button" id="resStatus${status.id}${note.value.id}" value="<fmt:message key="close_note" bundle="${resterm}"/>" onclick="javascript:boxShowWithDefault('<c:out value="${note.value.id}"/>','<c:out value="${sindex}"/>','<c:out value="${status.id}"/>','<c:out value="${status.name}"/>');"/>
            								</c:when>
                        					</c:choose>
                        					<c:set var="sindex" value="${sindex+1}"/>
                            			</c:forEach>
                            			<br>
                            			<c:set var="showDNBox" value="y"/>
                            		</c:if>
                            	</td>
                            	</tr>
                                <tr>
                                	<td class="table_cell_left" id="msg${note.value.id}">
                                	<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="newChildAdded${note.value.id}"/></jsp:include>	
                                	</td>
                                </tr>
                            </c:if>
                        </table>
                    </div>
                </div></div></div></div></div></div></div></div>
                
            </td>
        </tr>
             
        
		<c:if test="${showDNBox eq 'y'}">
			<c:import url="./discrepancyNote.jsp">
        		<c:param name="parentId" value="${note.value.id}"/>
				<c:param name="entityId" value="${id}"/>				
				<c:param name="entityType" value="${name}"/>				
				<c:param name="field" value="${field}"/>				
				<c:param name="column" value="${column}"/>
				<c:param name="boxId" value="box${note.value.id}"/>
				<c:param name="typeId" value="${note.value.discrepancyNoteTypeId}"/>
				<c:param name="typeName" value="${note.value.disType.name}"/>
			</c:import>
		</c:if>
    </tbody>
</table>
<c:set var="count" value="${count+1}"/>
</c:forEach>

<c:if test="${!study.status.locked}">
	<div style="clear:both;"></div>
	<c:choose>
    <c:when test="${boxToShow==0}">
    	<p id="p">
			<a href="javascript:showOnly('box<c:out value='${0}'/>New');javascript:removeText('a0','<b><fmt:message key="begin_new_thread" bundle="${resword}"/></b>');" id="a0"><b><fmt:message key="begin_new_thread" bundle="${resword}"/></b></a>
		</p>
    </c:when>
    <c:otherwise>
		<p id="p">
			<a href="javascript:showOnly('box<c:out value='${0}'/>New');javascript:removeText('a0','<b><fmt:message key="begin_new_thread" bundle="${resword}"/></b>');" id="a0"><b><fmt:message key="begin_new_thread" bundle="${resword}"/></b></a>
		</p>
	</c:otherwise>
	</c:choose>
	<c:import url="./discrepancyNote.jsp">
        <c:param name="parentId" value="0"/>
		<c:param name="entityId" value="${id}"/>				
		<c:param name="entityType" value="${name}"/>				
		<c:param name="field" value="${field}"/>				
		<c:param name="column" value="${column}"/>
		<c:param name="boxId" value="box${0}New"/>
	</c:import>
</c:if>  

<div style="clear:both;"></div>
<div id="audit">
<h3 class="title_manage"><fmt:message key="Audit_History" bundle="${resword}"/></h3>
<c:import url="../admin/auditItem.jsp">
	<c:param name="entityCreatedDate" value="${entityCreatedDate}"/>
</c:import>
</div>
<div style="clear:both;"></div>
</body>
</html>

