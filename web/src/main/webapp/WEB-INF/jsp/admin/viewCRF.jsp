<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.page_messages" var="respage"/>
<jsp:useBean scope='session' id='study' class='org.akaza.openclinica.bean.managestudy.StudyBean' />


<c:choose>
    <c:when test="${userBean.sysAdmin && module=='admin'}">
        <c:import url="../include/admin-header.jsp"/>
    </c:when>
    <c:otherwise>
        <c:import url="../include/managestudy-header.jsp"/>
    </c:otherwise>
</c:choose>

<link rel="stylesheet" href="includes/jmesa/jmesa.css" type="text/css">
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.jmesa.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jmesa.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery-migrate-1.1.1.js"></script> 


<script type="text/javascript">
    function onInvokeAction(id,action) {
        if(id.indexOf('studies') == -1)  {
        setExportToLimit(id, '');
        }
        createHiddenInputFieldsForLimitAndSubmit(id);
    }
    function onInvokeExportAction(id) {
        var parameterString = createParameterStringForLimit(id);
        location.href = '${pageContext.request.contextPath}/ViewCRF?module=manage&crfId=' + '${crf.id}&' + parameterString;
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
<jsp:useBean scope='request' id='crf' class='org.akaza.openclinica.bean.admin.CRFBean'/>

<h1><span class="title_Manage"><fmt:message key="view_CRF_details" bundle="${resword}"/></span></h1>
<div style="width: 600px">
    <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

        <div class="tablebox_center">
            <table border="0" cellpadding="0" cellspacing="0" width="100%">
                <tr valign="top"><td class="table_header_column_top"><fmt:message key="name" bundle="${resword}"/>:</td><td class="table_cell">
                    <c:out value="${crf.name}"/>
                </td></tr>
                <tr valign="top"><td class="table_header_column"><fmt:message key="description" bundle="${resword}"/>:</td><td class="table_cell">
                    <c:out value="${crf.description}"/>
                </td></tr>
                <tr valign="top"><td class="table_header_column"><fmt:message key="OID" bundle="${resword}"/>:</td><td class="table_cell">
                    <c:out value="${crf.oid}"/>
                </td></tr>
            </table>
        </div>
    </div></div></div></div></div></div></div></div>

</div>
<br>
<span class="title_Manage" style="font-weight: bold;" ><fmt:message key="versions" bundle="${resword}"/></span>
<div style="width: 600px">
    <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

        <div class="tablebox_center">
            <table border="0" cellpadding="0" cellspacing="0" width="100%">
                <tr valign="top">
                    <td class="table_header_row_left"><fmt:message key="version_name" bundle="${resword}"/></td>
                    <td class="table_header_row"><fmt:message key="oid" bundle="${resword}"/></td>
                    <td class="table_header_row"><fmt:message key="description" bundle="${resword}"/></td>
                    <td class="table_header_row"><fmt:message key="status" bundle="${resword}"/></td>
                    <td class="table_header_row"><fmt:message key="revision_notes" bundle="${resword}"/></td>
                    <td class="table_header_row"><fmt:message key="action" bundle="${resword}"/></td>
                </tr>
                <c:forEach var ="version" items="${crf.versions}">
                <tr valign="top">
                    <td class="table_cell_left"><c:out value="${version.name}"/></td>
                    <td class="table_cell"><c:out value="${version.oid}"/></td>
                    <td class="table_cell"><c:out value="${version.description}"/></td>
                    <td class="table_cell"><c:out value="${version.status.name}"/></td>
                    <td class="table_cell"><c:out value="${version.revisionNotes}"/></td>
                    <td class="table_cell">
                        <table border="0" cellpadding="0" cellspacing="0">
                            <tr>
                                <td>
                                   
                                    <a href="ViewSectionDataEntry?module=<c:out value="${module}"/>&crfId=<c:out value="${crf.id}"/>&crfVersionId=<c:out value="${version.id}"/>&tabId=1"
                                       onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
                                       onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><img
                                            name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6"></a>
                                </td>
                                <td>
  <a href="javascript:processPrintCRFRequest('rest/metadata/html/print/*/*/<c:out value="${version.oid}"/>')"
                                       onMouseDown="javascript:setImage('bt_Print1','images/bt_Print_d.gif');"
                                       onMouseUp="javascript:setImage('bt_Print1','images/bt_Print.gif');"><img
                                            name="bt_Print1" src="images/bt_Print.gif" border="0" alt="<fmt:message key="print" bundle="${resword}"/>" title="<fmt:message key="print" bundle="${resword}"/>" align="left" hspace="6"></a>

                                </td>
                                <td>
                                    <a href="ViewCRFVersion?id=<c:out value="${version.id}"/>"><img
                                            name="bt_Metadata" src="images/bt_Metadata.gif" border="0" alt="Metadata" title="Metadata" align="left" hspace="6"></a>

                                </td>
                            </tr>
                        </table>
                        </c:forEach>

            </table>
        </div>
    </div></div></div></div></div></div></div></div>

</div>
<br>
<span class="title_Manage" style="font-weight: bold;"><fmt:message key="Items" bundle="${resword}"/></span>
<div style="width: 600px">
    <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

        <div class="tablebox_center">
            <table border="0" cellpadding="0" cellspacing="0" width="100%">
                <tr valign="top">
                    <td class="table_header_row_left"><fmt:message key="name" bundle="${resword}"/></td>
                    <td class="table_header_row"><fmt:message key="item_oid" bundle="${resword}"/></td>
                    <td class="table_header_row"><fmt:message key="description" bundle="${resword}"/></td>
                    <td class="table_header_row"><fmt:message key="data_type" bundle="${resword}"/></td>
                    <td class="table_header_row"><fmt:message key="versions" bundle="${resword}"/></td>
                    <td class="table_header_row"><fmt:message key="integrity_check" bundle="${resword}"/></td>
                </tr>
                 <c:forEach var ="item" items="${items}">
                <tr valign="top">
                    <td class="table_cell_left">
                     <c:if test="${item.id > 0}"><a href="javascript: openDocWindow('ViewItemDetail?itemId=<c:out value="${item.id}"/>')"></c:if><c:out value="${item.itemName}"/> <c:if test="${item.id > 0}"></a></c:if></td>
                    <td class="table_cell"><c:out value="${item.itemOID}"/></td>
                    <td class="table_cell"><c:out value="${item.itemDescription}"/></td>
                    <td class="table_cell"><c:out value="${item.itemDataType}"/></td>
                    <td class="table_cell"><c:out value="${item.versions}"/></td>
                    <td class="table_cell">
                     <c:choose>
     <c:when test="${empty item.arrErrorMesages}"><span class="aka_green_highlight"><b><fmt:message key="ok" bundle="${respage}"/></b></span>   	
    </c:when>
    <c:otherwise>
    <c:if test="${item.crfVersionStatus == 1 }"><span class="aka_red_highlight"><b><fmt:message key="problem" bundle="${respage}"/></b>
   <fmt:message key="problem_message" bundle="${respage}"/><br></c:if>
     <c:if test="${item.crfVersionStatus != 1 }">  <span class="aka_orange_highlight"><b><fmt:message key="warning" bundle="${respage}"/></b>
      <fmt:message key="warning_message" bundle="${respage}"/></c:if>
  	<ul class="list_a_ul">
  		<c:forEach var = "error" items="${item.arrErrorMesages}">
  			<li class="list_a"><c:out value="${ error}"/>;</li>
  		</c:forEach>
  		</ul>
    </c:otherwise>
    </c:choose>
                   
                    
                    </td>
                </tr>
                </c:forEach>
                </table>
                </div></div></div></div></div></div></div></div></div></div>


<br/>
<span class="title_Manage" style="font-weight: bold;"><fmt:message key="studies_using_crf" bundle="${resword}"/></span>

<div id="studiesDiv">
    <form  action="${pageContext.request.contextPath}/ViewCRF">
        <input type="hidden" name="module" value="admin">
        <input type="hidden" name="crfId" value="${crf.id}">
        ${studiesTableHTML}
    </form>
</div>

 <br/>
<span class="title_Manage" style="font-weight: bold;"><fmt:message key="rule_rules" bundle="${resword}"/></span>
<div>&nbsp;</div>
<div class="homebox_bullets"><a href="RunRule?crfId=<c:out value="${crf.id}"/>&action=dryRun"><fmt:message key="rule_crf_run_all" bundle="${resword}"/></a></div><br/>
<div class="homebox_bullets"><a href="ViewRuleAssignment?ruleAssignments_f_crfName=<c:out value="${crfName}"/>"><fmt:message key="rule_crf_view_rules_for_this_crf" bundle="${resword}"/></a></div><br/>
<br/>
<input type="button" onclick="confirmExit('ListCRF?module=<c:out value="${module}"/>');"  name="exit" value="<fmt:message key="exit" bundle="${resword}"/>   " class="button_medium"/>

<jsp:include page="../include/footer.jsp"/>