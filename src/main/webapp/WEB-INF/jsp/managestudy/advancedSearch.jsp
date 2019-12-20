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
<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/1.10.16/css/jquery.dataTables.min.css"/>
<script type="text/JavaScript" language="JavaScript" src="//cdnjs.cloudflare.com/ajax/libs/handlebars.js/4.0.11/handlebars.js"></script>
<script type="text/JavaScript" language="JavaScript" src="//cdnjs.cloudflare.com/ajax/libs/moment.js/2.8.4/moment.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="//cdn.datatables.net/1.10.16/js/jquery.dataTables.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="//cdn.datatables.net/plug-ins/1.10.16/sorting/datetime-moment.js"></script>
<script type="text/JavaScript" language="JavaScript" src="//cdn.datatables.net/plug-ins/1.10.16/api/fnSortNeutral.js"></script>

<script id="result-tmpl" type="text/x-handlebars-template">
  <tr class="search-result">
    <td>{{result.participantId}}</td>
    <td>{{result.firstName}}</td>
    <td>{{result.lastName}}</td>
    <td>{{result.identifier}}</td>
  </tr>
</script>

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
<jsp:useBean scope='session' id='userBean' class='core.org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='crf' class='core.org.akaza.openclinica.bean.admin.CRFBean'/>

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
  input[type=text] {
    width: 100%;
    padding: 0;
  }
  input[type=button][disabled], input[type=button][disabled]:hover {
    background: none;
    background-color: lightgray;
    color: darkgray;
  }
  #btn-search {
    margin-bottom: 3px;    
  }
  #search-inputs td {
    padding-top: 0;
  }
  #search-message {
    text-align: center;
  }
  #show-all {
    text-decoration: underline;
  }
</style>

<h1 id="header">
  <span class="title_manage">
    <fmt:message key="advanced_search" bundle="${resword}"/>
  </span>
</h1>
<br>
<table id="tbl-search" class="datatable">
  <thead>
    <tr>
      <td>
        <fmt:message key="search_by" bundle="${resword}"/><br>
        <fmt:message key="participant_ID" bundle="${resword}"/>
      </td>
      <td><br><fmt:message key="first_name" bundle="${resword}"/></td>
      <td><br><fmt:message key="last_name" bundle="${resword}"/></td>
      <td><br><fmt:message key="secondary_ID" bundle="${resword}"/></td>
      <td></td>
    </tr>
    <tr id="search-inputs">
      <td><input type="text" id="input-id"    maxlength="35"></td>
      <td><input type="text" id="input-fname" maxlength="35"></td>
      <td><input type="text" id="input-lname" maxlength="35"></td>
      <td><input type="text" id="input-secid" maxlength="35"></td>
      <td><input type="button" value="Search" id="btn-search" disabled="disabled"></td>
    </tr>
    <tr>
      <th><fmt:message key="participant_ID" bundle="${resword}"/></th>
      <th><fmt:message key="first_name" bundle="${resword}"/></th>
      <th><fmt:message key="last_name" bundle="${resword}"/></th>
      <th><fmt:message key="secondary_ID" bundle="${resword}"/></th>
      <th><fmt:message key="actions" bundle="${resword}"/></th>
    </tr>
  </thead>
  <tbody>
  </tbody>
</table>

<script>
var resultTmpl = Handlebars.compile($('#result-tmpl').html());
var tblSearch = $('#tbl-search');
var datatable = tblSearch.DataTable({
  dom: 'tilp',
  searching: false,
  paging: true,
  pageLength: 50,
  columnDefs: [{
    targets: -1,
    orderable: false
  }],
  language: {
    emptyTable: '<fmt:message key="advsearch_noresult" bundle="${resword}"/>',
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

function setEmptyMessage(message) {
  datatable.settings()[0].oLanguage.sEmptyTable = message;
}

function doSearch(params) {
  setEmptyMessage('<fmt:message key="loading" bundle="${resword}"/>...');
  datatable.clear();
  datatable.draw();

  var url = '${pageContext.request.contextPath}/pages/auth/api/clinicaldata/studies/${study.oc_oid}/participants/searchByFields?';
  jQuery.ajax({
    type: 'get',
    url: url + params,
    success: function(data) {
      datatable.rows.add(data.map(function(result) {
        function linkToPDP(s) {
          return '<a href="ViewStudySubject?id=' + result.viewStudySubjectId + '">' + s + '</a>';
        }
        return [
          linkToPDP(result.participantId),
          result.firstName,
          result.lastName,
          result.identifier,
          linkToPDP('<span class="icon icon-search"></span>')
        ];
      }));
      setEmptyMessage('<fmt:message key="advsearch_noresult" bundle="${resword}"/>');
      datatable.draw();
    },
    error: function() {
      console.log(arguments);
    }
  });
}

$('#btn-search').click(function() {
  var queryParams = [];
  function addParam(name, selector) {
    var val = $(selector).val().trim();
    if (val)
      queryParams.push(name + '=' + val);
  }
  addParam('participantId', '#input-id');
  addParam('firstName',     '#input-fname');
  addParam('lastName',      '#input-lname');
  addParam('identifier',    '#input-secid');

  doSearch(queryParams.join('&'));
});

$('#search-inputs').on('change keyup paste', function() {
  var anyFilled = $('#input-id, #input-fname, #input-lname, #input-secid').filter(function() {
    return this.value.trim() !== '';
  }).length > 0;

  if (anyFilled) {
    $('#btn-search').removeAttr('disabled');    
  }
  else {
    $('#btn-search').attr('disabled', 'disabled');
  }
});

$('#tbl-search').on('click', '#show-all', function() {
  $('#search-inputs input[type=text]').val('');
  doSearch('');
});
</script>