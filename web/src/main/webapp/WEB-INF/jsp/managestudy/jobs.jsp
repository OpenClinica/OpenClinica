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
<script type="text/JavaScript" language="JavaScript" src="//cdnjs.cloudflare.com/ajax/libs/handlebars.js/4.0.11/handlebars.js"></script>
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
  .datatable {
    border-bottom: none !important;
    border-collapse: collapse !important;
    font-size: 13.6px;
  }
  .datatable td {
    border: 1px solid #ccc;
    border-bottom-color: #ccc !important;
  }
  .datatable thead td {
    border-color: white !important;
  }
  .datatable thead th {
    background-color: #ccc;
    font-weight: normal !important;
    padding: 3px;
    border-bottom: none !important;
  }
  .datatable tbody td:last-child {
    text-align: center;
  }
  .dataTables_length {
      padding-top: 0.75em;
      padding-left: 1.5em;
  }
</style>

<h1 id="header">
  <span class="title_manage">
    <fmt:message key="jobs" bundle="${resword}"/>
  </span>
</h1>
<br>
<div>
  <fmt:message key="jobs_description" bundle="${resword}"/>
</div>
<br>
<b>
  <fmt:message key="jobs_log" bundle="${resword}"/>
</b>
<table id="tbl-jobs" class="datatable">
  <thead>
    <tr>
      <th><fmt:message key="log_file" bundle="${resword}"/></th>
      <th><fmt:message key="job_type" bundle="${resword}"/></th>
      <th><fmt:message key="created_on" bundle="${resword}"/></th>
      <th><fmt:message key="created_by" bundle="${resword}"/></th>
      <th><fmt:message key="completed_on" bundle="${resword}"/></th>
      <th><fmt:message key="actions" bundle="${resword}"/></th>
    </tr>
  </thead>
  <tbody>
  </tbody>
</table>

<script>
$('#tbl-jobs').DataTable({
  dom: 'frtilp',
  searching: true,
  paging: true,
  pageLength: 50,
  columnDefs: [{
    targets: -1,
    orderable: false
  }],
  language: {
    emptyTable: '<fmt:message key="jobs_noresult" bundle="${resword}"/>',
    paginate: {
        first: '<<',
        previous: '<',
        next: '>',
        last: '>>'
    },
    info: '<fmt:message key="results_m_n_of_total" bundle="${resword}"/>',
    infoEmpty: '<fmt:message key="results_zero_of_zero" bundle="${resword}"/>',
    infoFiltered: '<span class="info-filtered"><fmt:message key="results_filtered" bundle="${resword}"/></span>',
    lengthMenu: '<fmt:message key="results_pagesize" bundle="${resword}"/>'
  }
});

var url = '${pageContext.request.contextPath}/pages/auth/api/studies/${study.oid}/jobs';
jQuery.ajax({
  type: 'get',
  url: url + params,
  success: function(data) {
    console.log(arguments);
  },
  error: function() {
    console.log(arguments);
  }
});
</script>