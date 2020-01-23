<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<link rel="stylesheet" href="includes/font-awesome-4.7.0/css/font-awesome.css">
<jsp:include page="../include/submit-header.jsp"/>
<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
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
  #tbl-jobs td:last-child {
    white-space: nowrap;
  }
  #tbl-jobs_filter {
    margin-bottom: 3px;
  }
  .icon {
    cursor: pointer;
  }
  input[type=search] {
    border: 1px solid #ccc;
    border-radius: 5px;
    margin-bottom: 6px;
  }
  .highlight-red {
    color: red;
    font-weight: bold;
  }
  .right {
    float: right;
  }
</style>

<script>
  var dateFormat = 'hh:mma MMM DD YYYY';
  function formatDateWithNull(date){
    if(date == null)
        return ""
    else
        return moment(date).format(dateFormat);
  }
  function formatError(e) {
    return 'ERROR: ' + e.status + ': ' + e.statusText;
  }

  var url = '${pageContext.request.contextPath}/pages/auth/api/studies/${theStudy.oc_oid}';
  var siteOid = '${atSiteLevel ? theSite.oc_oid : null}';
  if (siteOid)
    url += '/sites/' + siteOid;
  url += '/jobs';
  var jobs = jQuery.ajax({type: 'get', url: url});

  function sizetable(selector) {
    return function(settings) {
      const dtWidth = $(selector).width() + 1;
      const sidebarWidth = $("#sidebar_Instructions_closed").width();
      const navbarWidth = dtWidth + sidebarWidth;
      $(selector + "_wrapper").css({"width": "calc(" + dtWidth + "px + 1em)", "padding-right": "1em" });
      let windowWidth = $(window).width();
      if (!(windowWidth > navbarWidth)) {
        $(".oc_nav").css({"width": "calc(" + navbarWidth + "px + 2em)"});
      }
      $(window).resize(function() {
        windowWidth = $(window).width() + 1;
        if (windowWidth > navbarWidth) {
          $(".oc_nav").css({"width": windowWidth + "px"});
        } else {
          $(".oc_nav").css({"width": "calc(" + navbarWidth + "px + 2em)"});
        }
      });
    };
  }

  // https://stackoverflow.com/questions/8493195/how-can-i-parse-a-csv-string-with-javascript-which-contains-comma-in-data
  // Return array of string values, or NULL if CSV string not well formed.
  function CSVtoArray(text) {
	var re_value = /(?!\s*$)\s*(?:'([^'\\]*(?:\\[\S\s][^'\\]*)*)'|"([^"\\]*(?:\\[\S\s][^"\\]*)*)"|([^,'"\s\\]*(?:\s+[^,'"\s\\]+)*))\s*(?:,|$)/g;
	// Return NULL if input string is not well formed CSV string.
	var a = [];                     // Initialize array to receive values.
	text.replace(re_value, // "Walk" the string using replace with callback.
	  function(m0, m1, m2, m3) {
	    // Remove backslash from \' in single quoted values.
	    if (m1 !== undefined) a.push(m1.replace(/\\'/g, "'"));
	    // Remove backslash from \" in double quoted values.
	    else if (m2 !== undefined) a.push(m2.replace(/\\"/g, '"'));
	    else if (m3 !== undefined) a.push(m3);
        return ''; // Return empty string.
	  });
	// Handle special case of empty last value.
	if (/,\s*$/.test(text)) a.push('');
	return a;
  };
</script>

<c:choose>
  <c:when test="${param['uuid'] == null}">
    <h1 id="header">
      <span class="title_manage">
        <fmt:message key="jobs" bundle="${resword}"/>
      </span>
    </h1>
    <br>
    <div>
      <fmt:message key="jobs_description" bundle="${resword}"/>
    </div>
    <table id="tbl-jobs" class="datatable">
      <thead>
        <tr>
          <th><fmt:message key="source" bundle="${resword}"/></th>
          <th><fmt:message key="job_type" bundle="${resword}"/></th>
          <th><fmt:message key="site_name" bundle="${resword}"/></th>
          <th><fmt:message key="job_status" bundle="${resword}"/></th>
          <th><fmt:message key="start_time" bundle="${resword}"/></th>
          <th><fmt:message key="submitted_by" bundle="${resword}"/></th>
          <th><fmt:message key="completion_time" bundle="${resword}"/></th>
          <th><fmt:message key="actions" bundle="${resword}"/></th>
        </tr>
      </thead>
      <tbody>
      </tbody>
    </table>

    <script>
      $('#jobs-doc').attr({href: '${pageContext.request.contextPath}/pages/swagger-ui.html', target: '_blank'});
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
        order: [[4, 'desc'], [6, 'desc']],
        language: {
          emptyTable: 'Loading...',
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
        },
        drawCallback: sizetable('#tbl-jobs')
      });
      jobs.done(function(data) {
        datatable.rows.add(data.map(function (logEntry) {
          var actionView = '<a href="Jobs?uuid=' + logEntry.uuid + '"><span class="icon icon-search"></span></a> ';
          var actionDownload = '<a href="${pageContext.request.contextPath}/pages/auth/api/jobs/' + logEntry.uuid + '/downloadFile"><span class="icon icon-download"></span></a> ';
          var actionDelete = '<span class="icon icon-trash red" data-uuid="' + logEntry.uuid + '"></span>';
          var source = logEntry.sourceFileName;
          if (logEntry.type === 'PARTICIPANT_PDF_CASEBOOK') {
            source = source.split('_');
            source.splice(0, 1);
            source.splice(-2);
            source = source.join('_');
            actionView = '';
          }
          if (logEntry.status === 'IN_PROGRESS') {
            actionView = actionDownload = actionDelete = '';
          }
          return [
            source,
            logEntry.type,
            logEntry.siteOid && (logEntry.siteOid != logEntry.studyOid) ? logEntry.siteOid : logEntry.studyOid,
            logEntry.status,
            formatDateWithNull(logEntry.dateCreated),
            logEntry.createdByUsername,
            formatDateWithNull(logEntry.dateCompleted),
            actionView + actionDownload + actionDelete
          ];
        }));
        datatable.settings()[0].oLanguage.sEmptyTable = '<fmt:message key="jobs_noresult" bundle="${resword}"/>';
        datatable.draw();
        $('#tbl-jobs td:nth-child(4)').each(function() {
          var cell = $(this);
          var text = cell.text();
          if (text !== 'IN_PROGRESS' && text !== 'COMPLETED')
            cell.addClass('highlight-red');
        });
      }).fail(function(e) {
        console.log(arguments);
        datatable.settings()[0].oLanguage.sEmptyTable = formatError(e);
        datatable.draw();
      });
      $('#tbl-jobs')
        .on('click', '.icon-trash', function() {
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
  </c:when>
  <c:otherwise>
    <div id="loading">Loading...</div>
    <div id="job-details" class="hide">
      <a class="right" href="${pageContext.request.contextPath}/Jobs">
        <fmt:message key="go_back" bundle="${resword}"/>
      </a>
      <h1>
        Log For <span id="job-log-for"></span>
      </h1>
      <div class="box_T">
        <div class="box_L">
          <div class="box_R">
            <div class="box_B">
              <div class="box_TL">
                <div class="box_TR">
                  <div class="box_BL">
                    <div class="box_BR">
                      <div class="tablebox_center">
                        <table border="0" cellpadding="0" cellspacing="0">
                          <tbody>
                            <tr>
                              <td class="table_header_column_top">
                                <fmt:message key="site_name" bundle="${resword}"/>
                              </td>
                              <td class="table_cell_top" id="job-site-name"></td>
                            </tr>
                            <tr>
                              <td class="table_header_column_top">
                                <fmt:message key="submitted_by" bundle="${resword}"/>
                              </td>
                              <td class="table_cell_top" id="job-submitted-by"></td>
                            </tr>
                            <tr>
                              <td class="table_header_column_top">
                                <fmt:message key="start_time" bundle="${resword}"/>
                              </td>
                              <td class="table_cell_top" id="job-start-time"></td>
                            </tr>
                            <tr>
                              <td class="table_header_column_top">
                                <fmt:message key="completion_time" bundle="${resword}"/>
                              </td>
                              <td class="table_cell_top" id="job-completion-time"></td>
                            </tr>
                          </tbody>
                        </table>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <table id="tbl-job"></table>
    <a class="right" href="${pageContext.request.contextPath}/Jobs">
      <fmt:message key="go_back" bundle="${resword}"/>
    </a>

    <script>
      var url = '${pageContext.request.contextPath}/pages/auth/api/jobs/${uuid}/downloadFile?open=true';
      var jobResult = $.get(url, function(data) {
        var rows = data.trim().split('\n');
        var header = rows[0];
        function splitby(separator) {
          var titles = header.split(separator);
          return {
            separator: separator,
            titles: titles,
            length: titles.length
          }
        }
        var bycoma = splitby(',');
        var bypipe = splitby('|');
        var cols = bycoma.length > bypipe.length ? bycoma : bypipe;
        $('#tbl-job').DataTable({
          data: rows.slice(1).map(function(row) {
            if (cols.separator === ',') {
              return CSVtoArray(row)
            } else {
              return row.split(cols.separator);
            }
          }),
          columns: cols.titles.map(function(title) {
            return {title: title};
          }),
          paging: false,
          dom: 'ft',
          order: [], // set no default order OC-11342
          drawCallback: sizetable('#tbl-job')
        });
      }).fail(function(e) {
        $('#loading').text(formatError(e));
      });
      jobs.done(function(data) {
        var logEntry;
        for (var i=0; i<data.length; i++) {
          if (data[i].uuid === '${uuid}') {
            logEntry = data[i];
            break;
          }
        }
        if (!logEntry)
          return;
        $('#job-log-for').text(logEntry.sourceFileName || logEntry.type);
        $('#job-site-name').text(logEntry.siteOid && (logEntry.siteOid != logEntry.studyOid) ? logEntry.siteOid : logEntry.studyOid);
        $('#job-start-time').text(formatDateWithNull(logEntry.dateCreated));
        $('#job-submitted-by').text(logEntry.createdByUsername);
        $('#job-completion-time').text(formatDateWithNull(logEntry.dateCompleted));
        $('#job-details').show();
      }).fail(function(e) {
        $('#loading').text(formatError(e));
      });
      $.when(jobs, jobResult).done(function() {
        $('#loading').remove();
      });
    </script>
  </c:otherwise>
</c:choose>
