<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<link rel="stylesheet" href="includes/font-awesome-4.7.0/css/font-awesome.css">
<jsp:include page="../include/submit-header.jsp"/>
<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<link rel="stylesheet" href="includes/jmesa/jmesa.css" type="text/css">
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.jmesa.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jmesa.js"></script>
<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery.blockUI.js"></script>
<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery-migrate-1.4.1.js"></script>
<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/1.10.16/css/jquery.dataTables.min.css"/>
<script type="text/JavaScript" language="JavaScript" src="//cdnjs.cloudflare.com/ajax/libs/moment.js/2.8.4/moment.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="//cdn.datatables.net/1.10.16/js/jquery.dataTables.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="//cdn.datatables.net/plug-ins/1.10.16/sorting/datetime-moment.js"></script>
<script type="text/JavaScript" language="JavaScript" src="//cdn.datatables.net/plug-ins/1.10.16/api/fnSortNeutral.js"></script>

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
  <td class="sidebar_tab">
    <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray"></span></a>
    <fmt:message key="instructions" bundle="${resword}"/>
    <div class="sidebar_tab_content">
    </div>
  </td>
</tr>
<tr id="sidebar_Instructions_closed" style="display: all">
  <td class="sidebar_tab">
    <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray"></span></a>
    <fmt:message key="instructions" bundle="${resword}"/>
  </td>
</tr>
<jsp:include page="../include/sideInfo.jsp"/>
<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='crf' class='org.akaza.openclinica.bean.admin.CRFBean'/>

<style>
  .gray-bg {
    background-color: lightgrey;
  }
</style>

<h1 id="header">
  <span class="title_manage">
    <fmt:message key="advanced_search" bundle="${resword}"/>
  </span>
</h1>
<div>
  <fmt:message key="advsearch_description" bundle="${resword}"/>
</div>
<br>
<div>
  <fmt:message key="search_by" bundle="${resword}"/>
</div>

<table id="search-table">
  <tr>
    <td><fmt:message key="participant_id" bundle="${resword}"/></td>
    <td><fmt:message key="first_name" bundle="${resword}"/></td>
    <td><fmt:message key="last_name" bundle="${resword}"/></td>
    <td><fmt:message key="secondary_id" bundle="${resword}"/></td>
    <td></td>
  </tr>
  <tr>
    <td><input type="text" id="input-id"></td>
    <td><input type="text" id="input-fname"></td>
    <td><input type="text" id="input-lname"></td>
    <td><input type="text" id="input-secid"></td>
    <td><input type="button" value="Search" id="btn-search"></td>
  </tr>
  <tr class="gray-bg">
    <td><fmt:message key="participant_id" bundle="${resword}"/></td>
    <td><fmt:message key="first_name" bundle="${resword}"/></td>
    <td><fmt:message key="last_name" bundle="${resword}"/></td>
    <td><fmt:message key="secondary_id" bundle="${resword}"/></td>
    <td><fmt:message key="actions" bundle="${resword}"/></td>
  </tr>
</table>

<script>
$('#btn-search').click(function() {
    var queryParams = [];
    function addParam(name, selector) {
        var val = $(selector).val();
        if (val)
            queryParams.push(name + '=' + val);
    }
    addParam('participantId', '#input-id');
    addParam('firstName',     '#input-fname');
    addParam('lastName',      '#input-lname');
    addParam('identifier',    '#input-secid');

    var url = '${pageContext.request.contextPath}/pages/auth/api/clinicaldata/studies/${study.oid}/participants/searchByFields?';
    jQuery.ajax({
        type: 'get',
        url: url + queryParams.join('&'),
        success: function(data) {
          console.log(data);
        },
        error: function() {
          console.log(arguments);
        }
    });
});
</script>