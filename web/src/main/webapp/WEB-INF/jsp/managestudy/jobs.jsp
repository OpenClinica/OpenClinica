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
<script type="text/JavaScript" language="JavaScript" src="//cdnjs.cloudflare.com/ajax/libs/moment-timezone/0.5.23/moment-timezone-with-data-2012-2022.min.js"></script>
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
  .datatable thead th {
    background-color: #ccc;
    font-weight: normal !important;
    padding: 3px;
    border-bottom: none !important;
    border: 1px solid white;
  }
  .datatable tbody td:last-child {
    text-align: center;
  }
  .dataTables_length {
    padding-top: 0.75em;
    padding-left: 1.5em;
  }
  #tbl-jobs_filter {
    margin-bottom: 3px;
  }
  #tbl-jobs_wrapper {
    margin-top: 30px;
  }
  .icon {
    cursor: pointer;
  }
  input[type=search] {
    border: 1px solid #ccc;
    border-radius: 5px;
    margin-bottom: 6px;
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
<table id="tbl-jobs" class="datatable">
  <thead>
    <tr>
      <th><fmt:message key="jobs_source_filename" bundle="${resword}"/></th>
      <th><fmt:message key="job_type" bundle="${resword}"/></th>
      <th><fmt:message key="site_name" bundle="${resword}"/></th>
      <th><fmt:message key="job_status" bundle="${resword}"/></th>
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
$('#jobs-doc').attr('href', '${pageContext.request.contextPath}/pages/swagger-ui.html#/job-controller');
var dateFormat = 'hh:mma MMM DD YYYY';
function formatDate(date) {
  return moment(date).format(dateFormat);
}
$.fn.dataTable.moment(dateFormat);
var datatable = $('#tbl-jobs').DataTable({
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
$('#tbl-jobs_wrapper').prepend('<b><fmt:message key="jobs_log" bundle="${resword}"/></b>');

var url = '${pageContext.request.contextPath}/pages/auth/api/studies/${theStudy.oid}';
var siteOid = '${atSiteLevel ? theSite.oid : null}';
if (siteOid)
  url += '/sites/' + siteOid;
url += '/jobs';
jQuery.ajax({
  type: 'get',
  url: url,
  success: function(data) {
    datatable.rows.add(data.map(function (logEntry) {
      var actionView = '<a target="_blank" href="${pageContext.request.contextPath}/pages/auth/api/jobs/' + logEntry.uuid + '/downloadFile?open=true"><span class="icon icon-search"></span></a> ';
      var actionDownload = '<a href="${pageContext.request.contextPath}/pages/auth/api/jobs/' + logEntry.uuid + '/downloadFile"><span class="icon icon-download"></span></a> ';
      var actionDelete = '<span class="icon icon-trash red" data-uuid="' + logEntry.uuid + '"></span>';
      if (logEntry.status === 'IN_PROGRESS') {
        actionView = actionDownload = actionDelete = '';
      }
      return [
        logEntry.sourceFileName,
        logEntry.type,
        logEntry.siteOid && (logEntry.siteOid != logEntry.studyOid) ? logEntry.siteOid : logEntry.studyOid,
        logEntry.status,
        formatDate(logEntry.dateCreated),
        logEntry.createdByUsername,
        formatDate(logEntry.dateCompleted),
        actionView + actionDownload + actionDelete
      ];
    }));
    datatable.draw();
  },
  error: function() {
    console.log(arguments);
  }
});

$('#tbl-jobs').on('click', '.icon-trash', function() {
  if(confirm('<fmt:message key="jobs_del_confirm" bundle="${resword}"/>')) {
    var uuid = $(this).data('uuid');
    var url = '${pageContext.request.contextPath}/pages/auth/api/jobs/' + uuid;
    jQuery.ajax({
      type: 'delete',
      url: url,
      success: function() {
        window.location.reload();
      },
      error: function() {
        alert('<fmt:message key="jobs_del_failed" bundle="${resword}"/>');
      }
    });
  }
});
</script>