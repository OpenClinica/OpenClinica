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
<%-- <script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jmesa-original.js"></script> --%>
<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery.blockUI.js"></script>
<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery-migrate-1.1.1.js"></script>
<script type="text/javascript">
  function onInvokeAction(id,action) {
      if(id.indexOf('findSubjects') == -1)  {
          
      setExportToLimit(id, '');
      }
      createHiddenInputFieldsForLimitAndSubmit(id);
  }
  function onInvokeExportAction(id) {
      var parameterString = createParameterStringForLimit(id);
      location.href = '${pageContext.request.contextPath}/ListStudySubjects?'+ parameterString;
  }
  
  jQuery(document).ready(function() {
      jQuery('#addSubject').click(function() {
          jQuery.blockUI({ message: jQuery('#addSubjectForm'), css:{left: "300px", top:"10px" } });
      });
  
      jQuery('input.cancel').click(function() {
          jQuery.unblockUI();
          return false;
      });
  });
</script>
<script type="text/javascript" language="javascript">
  function studySubjectResource()  { return "${study.oid}/${studySub.oid}"; }
  
  function checkCRFLocked(ecId, url){
      jQuery.post("CheckCRFLocked?ecId="+ ecId + "&ran="+Math.random(), function(data){
          if(data == 'true'){
              window.location = url;
          }else{
              alert(data);return false;
          }
      });
  }
  
  function checkCRFLockedInitial(ecId, formName){
      if(ecId==0) {formName.submit(); return;}
      jQuery.post("CheckCRFLocked?ecId="+ ecId + "&ran="+Math.random(), function(data){
          if(data == 'true'){
              formName.submit();
          }else{
              alert(data);
          }
      });
  }
</script>
<script>
  var studyKey = '/study.oid';
  var participantKey = '/views/participants/';

  if (sessionStorage.getItem(studyKey) !== '${study.oid}') {
    function isParticipantData(key) {
        return key.lastIndexOf(participantKey, 0) === 0;
    }
    var keys = [];
    for (var i = 0, len = sessionStorage.length; i < len; i++) {
      keys.push(sessionStorage.key(i));
    }
    keys.forEach(function(key) {
      if (isParticipantData(key))
        sessionStorage.removeItem(key);
    });
    sessionStorage.setItem(studyKey, '${study.oid}');
  }
  function store(callback) {
    if (callback)
      store.data = callback(store.data) || store.data;
    if (!store.dirty) {
      store.dirty = true;
      setTimeout(function() {
        sessionStorage.setItem(store.key, JSON.stringify(store.data));
        if (
          store.data.ocStatusHide !== 'oc-status-removed' ||
          store.data.datatables.some(function(state) {return canReset(state)}) ||
          $('div.section.collapsed').length
        )
          $('#reset-all-filters').removeClass('invisible');
        else
          $('#reset-all-filters').addClass('invisible');
        store.dirty = false;
      }, 1);
    }
  }
  store.key = participantKey + '${studySub.oid}';
  store.data = JSON.parse(sessionStorage.getItem(store.key)) || {
    collapseSections: {},
    datatables: [],
    ocStatusHide: 'oc-status-removed'
  };
  store.dirty = false;

  var defaultPageSize = 10;

  function canReset(state) {
    return state.order.length > 0 
        || state.search.search !== '' 
        || state.start > 0
        || state.length > defaultPageSize;
  }

  function resetAllFilters() {
    $('#oc-status-hide').val('oc-status-removed').change();
    $('table.datatable').each(function() {
      var table = $(this);
      var datatable = table.DataTable();
      datatable.search('');
      datatable.page.len(defaultPageSize);
      table.dataTable().fnSortNeutral();
    });
    clickAllSections('collapsed');
  }

  function showHide() {
    var header = $(this);
    var body = header.next();
    var section = header.parent();
    var n = section.data('section-number');
    if (section.hasClass('collapsed')) {
      body.slideDown('fast');
      header.attr('title', '<fmt:message key="collapse_section" bundle="${resword}"/>');
      store(function(data) {
        delete data.collapseSections[n];
      });
    }
    else {
      body.slideUp('fast');
      header.attr('title', '<fmt:message key="expand_section" bundle="${resword}"/>');
      store(function(data) {
        data.collapseSections[n] = true;
      });
    }
    section.toggleClass('collapsed expanded');
  }

  function clickAllSections(state) {
    $('div.section.' + state + '>.section-header').each(showHide);
  };

  $(document.body).on('click', '.section-header', showHide);
</script>
<style>
  #header {
    display: inline-block;
    margin-bottom: 25px;
  }
  .header-links {
    float: right;
    clear: right;
    margin-top: 6px;
    font-size: .85rem;
  }
  .table_cell {
    padding-top: 6px;
  }
  .section {
    margin-bottom: 3px;
  }
  .section-header {
    color: white;
    background-color: #618ebb;
    border-radius: 6px;
    font-size: 20px;
    padding: .5em 1em .5em .6em;
  }
  .section-header::after {
    float: right;
    margin: 3px;
    font-family: 'icomoon' !important;
    font-size: 30px;
  }
  .section-body {
    padding-top: 25px;
    padding-left: 25px;
  }
  #subjectEvents .section-body {
    padding-top: 0;
    padding-bottom: 25px;
  }
  #studySubjectRecord .section-body {
    padding-top: 0;
  }
  .expanded > .section-header::after {
    content: "\e92a";
  }
  .collapsed > .section-header::after {
    content: "\e92b";
  }
  #reset-all-filters {
    margin-left: 30px;
    background: #cc6600 !important;
  }
  .subnote {
    font-size: 85%;
    color: #618ebb;
    margin-top: 5px;
    margin-left: 5px;
  }
  .full-width {
    width: 100%;
  }
  .left {
    float: left;
  }
  .clear {
    clear: both;
  }
  .hide {
    display: none;
  }
  .invisible {
    visibility: hidden;
  }
  .error {
    color: red;
  }
</style>
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
<h1 id="header">
  <span class="title_manage">
    <fmt:message key="study_subject" bundle="${resword}"/> <c:out value="${studySub.label}"/>
  </span>
</h1>
<input type="button" class="invisible" id="reset-all-filters" value='<fmt:message key="custom_view_on" bundle="${resword}"/> &nbsp; &times;' onclick="resetAllFilters();">
<div class="header-links">
  <span>
    <a href="javascript:openDocWindow('ViewStudySubjectAuditLog?id=<c:out value="${studySub.id}"/>">
      <c:out value="${studySub.label}"/> <fmt:message key="audit_log" bundle="${resword}"/>
    </a>
  </span>
  <span>&nbsp; | &nbsp;</span>
  <span>
    <label for="oc-status-hide"><fmt:message key="showing" bundle="${resword}"/></label>
    <select id="oc-status-hide">
      <option value="oc-status-removed"><fmt:message key="records_active" bundle="${resword}"/></option>
      <option value="oc-status-active"><fmt:message key="records_removed" bundle="${resword}"/></option>
      <option value="null"><fmt:message key="records_all" bundle="${resword}"/></option>
    </select>
    <script>
      $('#oc-status-hide').val(store.data.ocStatusHide).change();
    </script>
  </span>
</div>
<div class="header-links">
  <a href="javascript:clickAllSections('collapsed');"><fmt:message key="expand_all" bundle="${resword}"/></a>
  <span>&nbsp; | &nbsp;</span>
  <a href="javascript:clickAllSections('expanded');"><fmt:message key="collapse_all" bundle="${resword}"/></a>  
</div>
</div>
<div class="section expanded clear hide" id="studySubjectRecord" data-section-number="0">
  <div class="section-header" title='<fmt:message key="collapse_section" bundle="${resword}"/>'>
    <fmt:message key="general_information" bundle="${resword}"/>
  </div>
  <div class="section-body">
    <table border="0" cellpadding="0" cellspacing="0" class="full-width">
      <tbody>
        <tr>
          <td style="padding-right: 20px;" valign="top" width="800">
            <!-- These DIVs define shaded box borders -->
            <div class="box_T">
              <div class="box_L">
                <div class="box_R">
                  <div class="box_B">
                    <div class="box_TL">
                      <div class="box_TR">
                        <div class="box_BL">
                          <div class="box_BR">
                            <div class="tablebox_center">
                              <table width="50%" border="0" cellpadding="0" cellspacing="0" class="left" style="min-width:500px; padding-right:20px;">
                                <!-- Table Actions row (pagination, search, tools) -->
                                <tbody>
                                  <tr>
                                    <!-- Table Tools/Actions cell -->
                                    <td class="table_actions" valign="top">
                                     <c:if test="${study.studyParameterConfig.subjectIdGeneration=='manual' && study.status.available}">
                                        <c:if test="${!userRole.monitor}">
                                          <a href="javascript:;" id="editParticipantID">
                                            <fmt:message key="edit" bundle="${resword}"/>
                                          </a>
                                        </c:if>
                                      </c:if>
                                    </td>
                                    <!-- End Table Tools/Actions cell -->
                                  </tr>
                                  <!-- end Table Actions row (pagination, search, tools) -->
                                  <tr>
                                    <td valign="top">
                                      <!-- Table Contents -->
                                      <table width="100%" border="0" cellpadding="0" cellspacing="0">
                                        <tbody>
                                          <tr>
                                            <td class="table_header_column_top">
                                              <fmt:message key="study_subject_ID" bundle="${resword}"/>
                                            </td>
                                            <td class="table_cell_top">
                                              <c:out value="${studySub.label}"/>
                                            </td>

                                            <td class="table_header_column">
                                              <fmt:message key="status" bundle="${resword}"/>
                                            </td>
                                            <td class="table_cell">
                                              <c:out value="${studySub.status.name}"/>
                                            </td>
                                          </tr>

                                          <tr>
                                            <td class="table_header_column_top">
                                              <fmt:message key="study_name" bundle="${resword}"/>
                                            </td>
                                            <td class="table_cell">
                                              <c:choose>
                                                <c:when test="${subjectStudy.parentStudyId>0}">
                                                  <a href="ViewStudy?id=<c:out value="${parentStudy.id}"/>&amp;viewFull=yes">
                                                    <c:out value="${parentStudy.name}"/>
                                                  </a>
                                                </c:when>
                                                <c:otherwise>
                                                  <a href="ViewStudy?id=<c:out value="${subjectStudy.id}"/>&amp;viewFull=yes">
                                                    <c:out value="${subjectStudy.name}"/>
                                                  </a>
                                                </c:otherwise>
                                              </c:choose>
                                            </td>
                                            <td class="table_header_row">
                                              <fmt:message key="site_name" bundle="${resword}"/>
                                            </td>
                                            <td class="table_cell">
                                              <c:if test="${subjectStudy.parentStudyId>0}">
                                                <a href="ViewSite?id=<c:out value="${subjectStudy.id}"/>">
                                                  <c:out value="${subjectStudy.name}"/>
                                                </a>
                                              </c:if>
                                              &nbsp;
                                            </td>
                                          </tr>
                                        </tbody>
                                      </table>
                                      <!-- End Table Contents -->
                                    </td>
                                  </tr>
                                </tbody>
                              </table>
                              <c:if test="${sessionScope.customUserRole == 'Clinical Research Coordinator' || sessionScope.customUserRole == 'Investigator'}">
                                <table width="50%" border="0" cellpadding="0" cellspacing="0" class="left" style="min-width:600px;">
                                  <!-- Table Actions row (pagination, search, tools) -->
                                  <tbody>
                                    <tr>
                                      <!-- Table Tools/Actions cell -->
                                      <td class="table_actions" valign="top">
                                        <a href="javascript:;" id="contactInformation">
                                          Invite
                                        </a>
                                        &nbsp;|&nbsp;
                                        <a href="javascript:;" id="participateAccess">
                                          View Access Details
                                        </a>
                                      </td>
                                      <!-- End Table Tools/Actions cell -->
                                    </tr>
                                    <!-- end Table Actions row (pagination, search, tools) -->
                                    <tr>
                                      <td valign="top">
                                        <!-- Table Contents -->
                                        <table width="100%" border="0" cellpadding="0" cellspacing="0">
                                          <tbody>
                                            <tr>
                                              <td class="table_header_column_top">
                                                <fmt:message key="first_name" bundle="${resword}"/>
                                              </td>
                                              <td class="table_cell_top" id="info-first-name">
                                                &emsp;&emsp;&emsp;&emsp;
                                              </td>

                                              <td class="table_header_column">
                                                Mobile Number
                                              </td>
                                              <td class="table_cell" id="info-phone-number">
                                                &emsp;&emsp;&emsp;&emsp;
                                              </td>
                                            </tr>

                                            <tr>
                                              <td class="table_header_column_top">
                                                <fmt:message key="participate_status" bundle="${resword}"/>
                                              </td>
                                              <td class="table_cell" id="info-participate-status">
                                                &emsp;&emsp;&emsp;&emsp;
                                              </td>
                                              <td class="table_header_column">
                                                <fmt:message key="email" bundle="${resword}"/>
                                              </td>
                                              <td class="table_cell" id="info-email">
                                                &emsp;&emsp;&emsp;&emsp;
                                              </td>
                                            </tr>
                                          </tbody>
                                        </table>
                                        <!-- End Table Contents -->
                                      </td>
                                    </tr>
                                  </tbody>
                                </table>
                              </c:if>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
    <br>
  </div>
</div>
<script>
  if (store.data.collapseSections[0])
    $('#studySubjectRecord').toggleClass('expanded collapsed').children('.section-body').hide();
  $('#studySubjectRecord').removeClass('hide');
</script>
<div id="loading"><br> &nbsp; Loading...</div>
<c:choose>
  <c:when test="${isAdminServlet == 'admin' && userBean.sysAdmin && module=='admin'}">
    <div class="table_title_Admin">
  </c:when>
  <c:otherwise>
    <c:choose>
      <c:when test="${userRole.manageStudy}">
        <div class="table_titla_manage">
      </c:when>
      <c:otherwise>
        <div class="table_title_submit">
      </c:otherwise>
    </c:choose>
  </c:otherwise>
</c:choose>
<a name="events"></a>
</div>
<div class="section expanded hide" id="subjectEvents" data-section-number="1">
  <div class="section-header" title='<fmt:message key="collapse_section" bundle="${resword}"/>'>
    <fmt:message key="visits" bundle="${resword}"/>
  </div>
  <div class="section-body">
    <c:import url="../include/showTable.jsp">
      <c:param name="rowURL" value="showStudyEventRow.jsp" />
    </c:import>
  </div>
</div>
<jsp:include page="viewStudySubjectCommon.jsp"/>
<div style="width: 250px">
<c:choose>
  <c:when test="${isAdminServlet == 'admin' && userBean.sysAdmin && module=='admin'}">
    <div class="table_title_Admin">
  </c:when>
  <c:otherwise>
    <c:choose>
      <c:when test="${userRole.manageStudy}">
        <div class="table_titla_manage">
      </c:when>
      <c:otherwise>
        <div class="table_title_submit">
      </c:otherwise>
    </c:choose>
  </c:otherwise>
</c:choose>
</div>
<div id="groups" style="display:none;">
  <div style="width: 600px">
    <!-- These DIVs define shaded box borders -->
    <div class="box_T">
      <div class="box_L">
        <div class="box_R">
          <div class="box_B">
            <div class="box_TL">
              <div class="box_TR">
                <div class="box_BL">
                  <div class="box_BR">
                    <div class="tablebox_center">
                      <table border="0" cellpadding="0" cellspacing="0" width="100%">
                        <!-- Table Actions row (pagination, search, tools) -->
                        <tr>
                          <!-- Table Tools/Actions cell -->
                          <td align="right" valign="top" class="table_actions">
                            <table border="0" cellpadding="0" cellspacing="0">
                              <tr>
                                <td class="table_tools">
                                  <c:if test="${study.status.available && !(empty groups)}">
                                    <a href="UpdateStudySubject?id=<c:out value="${studySub.id}"/>&action=show">
                                      <fmt:message key="assign_subject_to_group" bundle="${resworkflow}"/>
                                    </a>
                                  </c:if>
                                </td>
                              </tr>
                            </table>
                          </td>
                          <!-- End Table Tools/Actions cell -->
                        </tr>
                        <!-- end Table Actions row (pagination, search, tools) -->
                        <tr>
                          <td valign="top">
                            <!-- Table Contents -->
                            <table border="0" cellpadding="0" cellspacing="0" width="100%">
                              <tr>
                                <td class="table_header_row_left">
                                  <fmt:message key="subject_group_class" bundle="${resword}"/>
                                </td>
                                <td class="table_header_row">
                                  <fmt:message key="study_group" bundle="${resword}"/>
                                </td>
                                <td class="table_header_row">
                                  <fmt:message key="notes" bundle="${resword}"/>
                                </td>
                              </tr>
                              <c:choose>
                                <c:when test="${!empty groups}">
                                  <c:forEach var="group" items="${groups}">
                                    <tr>
                                      <td class="table_cell_left">
                                        <c:out value="${group.groupClassName}"/>
                                      </td>
                                      <td class="table_cell">
                                        <c:out value="${group.studyGroupName}"/>
                                      </td>
                                      <td class="table_cell">
                                        <c:out value="${group.notes}"/>
                                        &nbsp;
                                      </td>
                                    </tr>
                                  </c:forEach>
                                </c:when>
                                <c:otherwise>
                                  <tr>
                                    <td class="table_cell" colspan="2">
                                      <fmt:message key="currently_no_groups" bundle="${resword}"/>
                                    </td>
                                  </tr>
                                </c:otherwise>
                              </c:choose>
                            </table>
                            <!-- End Table Contents -->
                          </td>
                        </tr>
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
  <br><br>
</div>
<div style="width: 250px">
<!-- <c:choose>
  <c:when test="${isAdminServlet == 'admin' && userBean.sysAdmin && module=='admin'}">
      <div class="table_title_Admin">
  </c:when>
  <c:otherwise>
  
      <c:choose>
          <c:when test="${userRole.manageStudy}">
              <div class="table_titla_manage">
          </c:when>
          <c:otherwise>
              <div class="table_title_submit">
          </c:otherwise>
      </c:choose>
  
  </c:otherwise>
  </c:choose>
  
  </div> -->
<c:choose>
  <c:when test="${isAdminServlet == 'admin' && userBean.sysAdmin && module=='admin'}">
    <div class="table_title_Admin">
  </c:when>
  <c:otherwise>
    <c:choose>
      <c:when test="${userRole.manageStudy}">
        <div class="table_titla_manage">
      </c:when>
      <c:otherwise>
        <div class="table_title_submit">
      </c:otherwise>
    </c:choose>
  </c:otherwise>
</c:choose>
</div>
<div id="globalRecord" style="display:none">
  <div style="width: 350px">
    <!-- These DIVs define shaded box borders -->
    <div class="box_T">
      <div class="box_L">
        <div class="box_R">
          <div class="box_B">
            <div class="box_TL">
              <div class="box_TR">
                <div class="box_BL">
                  <div class="box_BR">
                    <div class="tablebox_center">
                      <table border="0" cellpadding="0" cellspacing="0" width="330">
                        <!-- Table Actions row (pagination, search, tools) -->
                        <!-- end Table Actions row (pagination, search, tools) -->
                        <tr>
                          <td valign="top">
                            <!-- Table Contents -->
                            <table width="100%" border="0" cellpadding="0" cellspacing="0">
                              <tbody>
                                <tr>
                                  <td class="table_header_column_top">
                                    <fmt:message key="study_subject_ID" bundle="${resword}"/>
                                  </td>
                                  <td class="table_cell_top">
                                    <c:out value="${studySub.label}"/>
                                  </td>
                                  <td class="table_header_row">
                                    <fmt:message key="person_ID" bundle="${resword}"/>
                                    <%-- DN for person ID goes here --%>
                                    <c:if test="${subjectStudy.studyParameterConfig.discrepancyManagement=='true' && !study.status.locked}">
                                      <c:set var="isNew" value="${hasUniqueIDNote eq 'yes' ? 0 : 1}"/>
                                      <c:choose>
                                        <c:when test="${hasUniqueIDNote eq 'yes'}">
                                          <a href="#" onClick="openDNoteWindow('ViewDiscrepancyNote?writeToDB=1&subjectId=${studySub.id}&id=${subject.id}&name=subject&field=uniqueIdentifier&column=unique_identifier','spanAlert-uniqueIdentifier'); return false;">
                                            <span id="flag_uniqueIdentifier" name="flag_uniqueIdentifier" class="fa fa-bubble-red" border="0" alt="
                                            <fmt:message key="discrepancy_note" bundle="${resword}"/>
                                            " title="
                                            <fmt:message key="discrepancy_note" bundle="${resword}"/>
                                            " >
                                          </a>
                                        </c:when>
                                        <c:otherwise>
                                          <a href="#" onClick="openDNoteWindow('CreateDiscrepancyNote?writeToDB=1&subjectId=${studySub.id}&id=${subject.id}&name=subject&field=uniqueIdentifier&column=unique_identifier','spanAlert-uniqueIdentifier'); return false;">
                                            <span id="flag_uniqueIdentifier" name="flag_uniqueIdentifier" class="fa fa-bubble-white" border="0" alt="
                                            <fmt:message key="discrepancy_note" bundle="${resword}"/>
                                            " title="
                                            <fmt:message key="discrepancy_note" bundle="${resword}"/>
                                            " >
                                          </a>
                                        </c:otherwise>
                                      </c:choose>
                                    </c:if>
                                  </td>
                                  <td class="table_cell_top">
                                    <c:out value="${subject.uniqueIdentifier}"/>
                                  </td>
                                </tr>
                                <tr>
                                  <td class="table_header_column">
                                    <fmt:message key="secondary_ID" bundle="${resword}"/>
                                  </td>
                                  <td class="table_cell">
                                    <c:out value="${studySub.secondaryLabel}"/>
                                  </td>
                                  <c:choose>
                                    <c:when test="${subjectStudy.studyParameterConfig.collectDob == '1'}">
                                      <td class="table_header_row">
                                        <fmt:message key="date_of_birth" bundle="${resword}"/>
                                        <%-- DN for DOB goes here --%>
                                        <c:if test="${subjectStudy.studyParameterConfig.discrepancyManagement=='true' && !study.status.locked}">
                                          <c:set var="isNew" value="${hasDOBNote eq 'yes' ? 0 : 1}"/>
                                          <c:choose>
                                            <c:when test="${hasDOBNote eq 'yes'}">
                                              <a href="#" onClick="openDNoteWindow('ViewDiscrepancyNote?writeToDB=1&subjectId=${studySub.id}&id=${subject.id}&name=subject&field=dob&column=date_of_birth','spanAlert-dob'); return false;">
                                                <span id="flag_dob" name="flag_dob" class="fa fa-bubble-red" border="0" alt="
                                                <fmt:message key="discrepancy_note" bundle="${resword}"/>
                                                " title="
                                                <fmt:message key="discrepancy_note" bundle="${resword}"/>
                                                " >
                                              </a>
                                            </c:when>
                                            <c:otherwise>
                                              <a href="#" onClick="openDNoteWindow('CreateDiscrepancyNote?writeToDB=1&subjectId=${studySub.id}&id=${subject.id}&name=subject&field=dob&column=date_of_birth&new=1','spanAlert-dob'); return false;">
                                                <span id="flag_dob" name="flag_dob" class="fa fa-bubble-white" border="0" alt="
                                                <fmt:message key="discrepancy_note" bundle="${resword}"/>
                                                " title="
                                                <fmt:message key="discrepancy_note" bundle="${resword}"/>
                                                " >
                                              </a>
                                            </c:otherwise>
                                          </c:choose>
                                        </c:if>
                                      </td>
                                      <td class="table_cell">
                                        <fmt:formatDate value="${subject.dateOfBirth}" pattern="${dteFormat}"/>
                                      </td>
                                    </c:when>
                                    <c:when test="${subjectStudy.studyParameterConfig.collectDob == '3'}">
                                      <td class="table_header_row">
                                        <fmt:message key="date_of_birth" bundle="${resword}"/>
                                        <%-- DN for DOB goes here --%>
                                        <c:if test="${subjectStudy.studyParameterConfig.discrepancyManagement=='true' && !study.status.locked}">
                                          <c:set var="isNew" value="${hasDOBNote eq 'yes' ? 0 : 1}"/>
                                          <c:choose>
                                            <c:when test="${hasDOBNote eq 'yes'}">
                                              <a href="#" onClick="openDNoteWindow('ViewDiscrepancyNote?writeToDB=1&subjectId=${studySub.id}&id=${subject.id}&name=subject&field=dob&column=date_of_birth','spanAlert-dob'); return false;">
                                                <span id="flag_dob" name="flag_dob" class="fa fa-bubble-red" border="0" alt="
                                                <fmt:message key="discrepancy_note" bundle="${resword}"/>
                                                " title="
                                                <fmt:message key="discrepancy_note" bundle="${resword}"/>
                                                " >
                                              </a>
                                            </c:when>
                                            <c:otherwise>
                                              <a href="#" onClick="openDNoteWindow('CreateDiscrepancyNote?writeToDB=1&subjectId=${studySub.id}&id=${subject.id}&name=subject&field=dob&column=date_of_birth&new=1','spanAlert-dob'); return false;">
                                                <span id="flag_dob" name="flag_dob" class="fa fa-bubble-white" border="0" alt="
                                                <fmt:message key="discrepancy_note" bundle="${resword}"/>
                                                " title="
                                                <fmt:message key="discrepancy_note" bundle="${resword}"/>
                                                " >
                                              </a>
                                            </c:otherwise>
                                          </c:choose>
                                        </c:if>
                                      </td>
                                      <td class="table_cell">
                                        <fmt:message key="not_used" bundle="${resword}"/>
                                      </td>
                                    </c:when>
                                    <c:otherwise>
                                      <td class="table_header_row">
                                        <fmt:message key="year_of_birth" bundle="${resword}"/>
                                        <%-- DN for DOB goes here --%>
                                        <c:if test="${subjectStudy.studyParameterConfig.discrepancyManagement=='true' && !study.status.locked}">
                                          <c:set var="isNew" value="${hasDOBNote eq 'yes' ? 0 : 1}"/>
                                          <c:choose>
                                            <c:when test="${hasDOBNote eq 'yes'}">
                                              <a href="#" onClick="openDNoteWindow('ViewDiscrepancyNote?writeToDB=1&subjectId=${studySub.id}&id=${subject.id}&name=subject&field=dob&column=date_of_birth','spanAlert-dob'); return false;">
                                                <span id="flag_dob" name="flag_dob" class="fa fa-bubble-red" border="0" alt="
                                                <fmt:message key="discrepancy_note" bundle="${resword}"/>
                                                " title="
                                                <fmt:message key="discrepancy_note" bundle="${resword}"/>
                                                " >
                                              </a>
                                            </c:when>
                                            <c:otherwise>
                                              <a href="#" onClick="openDNoteWindow('CreateDiscrepancyNote?writeToDB=1&subjectId=${studySub.id}&id=${subject.id}&name=subject&field=dob&column=date_of_birth&new=1','spanAlert-dob'); return false;">
                                                <span id="flag_dob" name="flag_dob" class="fa fa-bubble-white" border="0" alt="
                                                <fmt:message key="discrepancy_note" bundle="${resword}"/>
                                                " title="
                                                <fmt:message key="discrepancy_note" bundle="${resword}"/>
                                                " >
                                              </a>
                                            </c:otherwise>
                                          </c:choose>
                                        </c:if>
                                      </td>
                                      <td class="table_cell">
                                        <c:out value="${yearOfBirth}"/>
                                      </td>
                                    </c:otherwise>
                                  </c:choose>
                                </tr>
                                <tr>
                                  <td class="table_header_column">
                                    <fmt:message key="OID" bundle="${resword}"/>
                                  </td>
                                  <td class="table_cell">
                                    <c:out value="${studySub.oid}"/>
                                  </td>
                                  <td class="table_header_row">
                                    <fmt:message key="gender" bundle="${resword}"/>
                                    <%-- DN for Gender goes here --%>
                                    <c:if test="${subjectStudy.studyParameterConfig.discrepancyManagement=='true' && !study.status.locked}">
                                      <c:set var="isNew" value="${hasGenderNote eq 'yes' ? 0 : 1}"/>
                                      <c:choose>
                                        <c:when test="${hasGenderNote eq 'yes'}">
                                          <a href="#" onClick="openDNoteWindow('ViewDiscrepancyNote?writeToDB=1&subjectId=${studySub.id}&id=${subject.id}&name=subject&field=gender&column=gender','spanAlert-gender'); return false;">
                                            <span id="flag_gender" name="flag_gender" class="fa fa-bubble-red" border="0" alt="
                                            <fmt:message key="discrepancy_note" bundle="${resword}"/>
                                            " title="
                                            <fmt:message key="discrepancy_note" bundle="${resword}"/>
                                            " >
                                          </a>
                                        </c:when>
                                        <c:otherwise>
                                          <a href="#" onClick="openDNoteWindow('CreateDiscrepancyNote?subjectId=${studySub.id}&id=${subject.id}&writeToDB=1&name=subject&field=gender&column=gender','spanAlert-gender'); return false;">
                                            <span id="flag_gender" name="flag_gender" class="fa fa-bubble-white" border="0" alt="
                                            <fmt:message key="discrepancy_note" bundle="${resword}"/>
                                            " title="
                                            <fmt:message key="discrepancy_note" bundle="${resword}"/>
                                            " >
                                          </a>
                                        </c:otherwise>
                                      </c:choose>
                                    </c:if>
                                  </td>
                                  <td class="table_cell">
                                    <c:choose>
                                      <c:when test="${subject.gender==32}">
                                        &nbsp;
                                      </c:when>
                                      <c:when test="${subject.gender==109 ||subject.gender==77}">
                                        <fmt:message key="male" bundle="${resword}"/>
                                      </c:when>
                                      <c:otherwise>
                                        <fmt:message key="female" bundle="${resword}"/>
                                      </c:otherwise>
                                    </c:choose>
                                  </td>
                                </tr>
                                <tr>
                                  <td class="table_header_column">
                                    <fmt:message key="status" bundle="${resword}"/>
                                  </td>
                                  <td class="table_cell">
                                    <c:out value="${studySub.status.name}"/>
                                  </td>
                                  <td class="table_header_row">
                                    <fmt:message key="enrollment_date" bundle="${resword}"/>
                                    &nbsp;
                                    <%-- DN for enrollment date goes here --%>
                                    <c:if test="${subjectStudy.studyParameterConfig.discrepancyManagement=='true' && !study.status.locked}">
                                      <c:set var="isNew" value="${hasEnrollmentNote eq 'yes' ? 0 : 1}"/>
                                      <c:choose>
                                        <c:when test="${hasEnrollmentNote eq 'yes'}">
                                          <a href="#" onClick="openDNoteWindow('ViewDiscrepancyNote?writeToDB=1&subjectId=${studySub.id}&id=${studySub.id}&name=studySub&field=enrollmentDate&column=enrollment_date','spanAlert-enrollmentDate'); return false;">
                                            <span id="flag_enrollmentDate" name="flag_enrollmentDate" class="fa fa-bubble-red" border="0" alt="
                                            <fmt:message key="discrepancy_note" bundle="${resword}"/>
                                            " title="
                                            <fmt:message key="discrepancy_note" bundle="${resword}"/>
                                            " >
                                          </a>
                                        </c:when>
                                        <c:otherwise>
                                          <a href="#" onClick="openDNoteWindow('CreateDiscrepancyNote?subjectId=${studySub.id}&id=${studySub.id}&writeToDB=1&name=studySub&field=enrollmentDate&column=enrollment_date','spanAlert-enrollmentDate'); return false;">
                                            <span id="flag_enrollmentDate" name="flag_enrollmentDate" class="fa fa-bubble-white" border="0" alt="
                                            <fmt:message key="discrepancy_note" bundle="${resword}"/>
                                            " title="
                                            <fmt:message key="discrepancy_note" bundle="${resword}"/>
                                            " >
                                          </a>
                                        </c:otherwise>
                                      </c:choose>
                                    </c:if>
                                  </td>
                                  <td class="table_cell">
                                    <fmt:formatDate value="${studySub.enrollmentDate}" pattern="${dteFormat}"/>
                                    &nbsp;
                                  </td>
                                </tr>
                                <tr>
                                  <td class="table_divider" colspan="4">&nbsp;</td>
                                </tr>
                                <tr>
                                  <td class="table_header_column_top">
                                    <fmt:message key="study_name" bundle="${resword}"/>
                                  </td>
                                  <td class="table_cell_top">
                                    <c:choose>
                                      <c:when test="${subjectStudy.parentStudyId>0}">
                                        <a href="ViewStudy?id=<c:out value="${parentStudy.id}"/>&amp;viewFull=yes">
                                          <c:out value="${parentStudy.name}"/>
                                        </a>
                                      </c:when>
                                      <c:otherwise>
                                        <a href="ViewStudy?id=<c:out value="${subjectStudy.id}"/>&amp;viewFull=yes">
                                          <c:out value="${subjectStudy.name}"/>
                                        </a>
                                      </c:otherwise>
                                    </c:choose>
                                  </td>
                                  <td class="table_header_row">
                                    <fmt:message key="site_name" bundle="${resword}"/>
                                  </td>
                                  <td class="table_cell_top">
                                    <c:if test="${subjectStudy.parentStudyId>0}">
                                      <a href="ViewStudy?id=<c:out value="${subjectStudy.id}"/>">
                                        <c:out value="${subjectStudy.name}"/>
                                      </a>
                                    </c:if>
                                    &nbsp;
                                  </td>
                                </tr>
                              </tbody>
                            </table>
                            <!-- End Table Contents -->
                          </td>
                        </tr>
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
</div>
<br>
<c:choose>
  <c:when test="${isAdminServlet == 'admin' && userBean.sysAdmin && module=='admin'}">
    <div class="table_title_Admin">
  </c:when>
  <c:otherwise>
    <c:choose>
      <c:when test="${userRole.manageStudy}">
        <div class="table_titla_manage">
      </c:when>
      <c:otherwise>
        <div class="table_title_submit">
      </c:otherwise>
    </c:choose>
  </c:otherwise>
</c:choose>
<a name="global">
<a id="excl_archivableCasebook_open" style="display: all;" href="javascript:leftnavExpand('archivableCasebook'); leftnavExpand('excl_archivableCasebook_open'); leftnavExpand('excl_archivableCasebook_close');">
<img src="images/bt_Expand.gif" border="0" height="20px"> <fmt:message key="viewStudySubject.casebookGenerationForm.title" bundle="${resword}"/>
</a>
<a id="excl_archivableCasebook_close" style="display: none;" href="javascript:leftnavExpand('archivableCasebook'); leftnavExpand('excl_archivableCasebook_open'); leftnavExpand('excl_archivableCasebook_close');">
<img src="images/bt_Collapse.gif" border="0" height="20px"> <fmt:message key="viewStudySubject.casebookGenerationForm.title" bundle="${resword}"/>
</a>
</a>
</div>
<jsp:include page="studySubject/casebookGenerationForm.jsp"/>
<!-- End Main Content Area -->
<jsp:include page="../include/footer.jsp"/>
<script type="text/javascript" src="includes/studySubject/viewStudySubject.js"></script>
<c:choose>
  <c:when test="${!empty errorData}">
    <script type="text/javascript" language="javascript">
        var errorData = "${errorData}";
        alert(errorData);
    </script>
  </c:when>
</c:choose>

<div id="editSubjectForm" style="display: none">
    <form name="subjectForm" action="UpdateStudySubject" method="post">
        <input type="hidden" name="subjectOverlay" value="true">
        <input type="hidden" name="action" value="confirm"/>
        <input type="hidden" name="id" value="<c:out value="${studySub.id}"/>"/>

        <table border="0" cellpadding="0" align="center" style="cursor:default;">
            <tr style="height:10px;">
                <td class="formlabel" align="left"><h3 class="addNewSubjectTitle"><fmt:message key="update_study_subject_details" bundle="${resword}"/></h3></td>
            </tr>
            <tr>
                <td>
                    <div class="lines"></div>
                </td>
            </tr>
            <tr>
                <td>
                    <div style="max-height: 550px; min-width:400px; background:#FFFFFF; overflow-y: auto;">
                        <table>
                            <tr valign="top">
                                <td class="formlabel" align="left">
                                    <jsp:include page="../include/showSubmitted.jsp"/>
                                    <input class="form-control" type="hidden" name="addWithEvent" value="1"/><span class="addNewStudyLayout">
                                <fmt:message key="study_subject_ID" bundle="${resword}"/></span>
                                </td>
                                <td valign="top">
                                    <table border="0" cellpadding="0" cellspacing="0" class="full-width">
                                        <tr>
                                            <td valign="top">
                                                <div class="formfieldXL_BG">
                                                    <c:choose>
                                                        <c:when test="${study.studyParameterConfig.subjectIdGeneration =='auto non-editable'}">
                                                            <input onfocus="this.select()" type="text" value="<c:out value="${label}"/>" size="45"
                                                                   class="formfield form-control" disabled>
                                                            <input class="form-control" type="hidden" name="label" value="<c:out value="${label}"/>">
                                                        </c:when>
                                                        <c:otherwise>
                                                            <input onfocus="this.select()" type="text" name="label" value="<c:out value="${studySub.label}"/>" width="30"
                                                                   class="formfieldXL form-control">
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>
                                                <jsp:include page="../showMessage.jsp">
                                                    <jsp:param name="key" value="label"/>
                                                </jsp:include>
                                            </td>
                                        </tr>

                                    </table>
                                </td>
                            </tr>


                        </table>
                    </div>
                </td>
            </tr>
            <tr>
                <td>
                    <div class="lines"></div>
                </td>
            </tr>
            <tr>
                <td colspan="2" style="text-align: center;">
                    <input type="button" class="cancel" value="Cancel"/>
                    &nbsp;
                    <input type="submit" value="Update"/>


                    <div id="dvForCalander_${rand}" style="width:1px; height:1px;"></div>
                </td>
            </tr>

        </table>

    </form>
</div>

<div id="contactInformationForm" class="hide">
  <table border="0" cellpadding="0" align="center" style="cursor:default;">
    <tr style="height:10px;">
      <td class="formlabel" align="left">
        <h3>
          Contact Information
        </h3>
      </td>
    </tr>
    <tr>
      <td><div class="lines"></div></td>
    </tr>
    <tr>
      <td>
        <div style="max-height: 550px; min-width:400px; background:#FFFFFF; overflow-y: auto;">
          <table cellspacing="10">
            <tr valign="top">
              <td class="formlabel" align="left">
                <span><fmt:message key="first_name" bundle="${resword}"/></span>
              </td>
              <td valign="top">
                <input id="name-input" onfocus="this.select()" type="text" value="" size="45" class="formfield form-control">
              </td>
            </tr>
            <tr valign="top">
              <td class="formlabel" align="left">
                <span><fmt:message key="email" bundle="${resword}"/></span>
              </td>
              <td valign="top">
                <input id="email-input" onfocus="this.select()" type="text" value="" size="45" class="formfield form-control">
                <div class="subnote hide error" id="email-input-error">
                  Invalid e-mail address
                </div>
              </td>
            </tr>
            <tr valign="top">
              <td class="formlabel" align="left">
                <span>
                  Mobile
                </span>
              </td>
              <td valign="top">
                <style>
                  #phone-input {
                    padding: 4px !important;
                    padding-left: 100px !important;
                  }
                  #phone-widget {
                    position: relative;
                  }
                  #country-code {
                    position: absolute;
                    top: 4px;
                    left: 54px;
                    width: 37px;
                    text-align: center;
                  }
                  #country-select {
                    position: absolute;
                    height: 26px;
                    top: 2px;
                    left: 2px;                    
                    padding-top: 2px;
                  }
                  #country-select:hover {
                    background-color: #eee;
                  }
                  #country-select-down-arrow {
                    background: url(images/down-arrow.png) no-repeat center center;
                    width: 10px;
                    margin-left: 0px;
                    margin-right: 5px;
                    display: inline-block;
                  }
                  #country-flag {
                    top: 9px;
                    background: url(images/flags.png) no-repeat 0 0;
                    width: 20px;
                    height: 11px;
                    overflow: hidden;
                    margin-left: 11px;
                    display: inline-block;
                    background-position: 0px -44px;
                  }
                </style>
                <div id="phone-widget">
                  <input id="phone-input" type="text" class="formfield form-control" onfocus="this.select()"> 
                  <div id="country-select">
                    <div id="country-flag" class="down-arrow">&nbsp;</div> 
                    <div id="country-select-down-arrow" class="down-arrow">&nbsp;</div> 
                  </div> 
                  <div id="country-code">+1</div> 
                </div>
                <div id="country-options" style="display:none;">
                  <style>
                    #country-options {
                      position: absolute;
                      border: 1px solid #d9d9d9;
                      background-color: white;
                      padding-left: 0px;
                      padding-right: 0px;
                      z-index: 100;
                      overflow: auto;
                      height: 200px;
                    }
                    .country-option:hover {
                      background-color: #618ebb;
                      color: white;
                    }
                    .country-option:hover .the-country-code {
                      color: white;
                    }
                    .the-country-code {
                      color: #666;
                      padding-right: 10px;
                    }
                    .flag-holder {
                      padding-left: 5px;
                      padding-right: 5px;
                      padding-top: 1px;
                    }
                    .the-flag {
                      background: url(images/flags.png) no-repeat 0 0;
                      height: 11px;
                      overflow: hidden;
                      margin-left: 11px;
                      width: 20px;
                    }
                  </style>
                  <table cellspacing="0">
                    <tbody>
                      <tr class="country-option" data-country="AU">
                          <td class="flag-holder">
                              <div class="the-flag" style="background-position: 0px -1716px;"></div>
                          </td>
                          <td>
                              <span>Australia</span>&nbsp;&nbsp;<span class="the-country-code">+61</span>
                          </td>
                      </tr>
                      <tr class="country-option" data-country="AT">
                          <td class="flag-holder">
                              <div class="the-flag" style="background-position: 0px -1331px;"></div>
                          </td>
                          <td>
                              <span>Austria</span>&nbsp;&nbsp;<span class="the-country-code">+43</span>
                          </td>
                      </tr>
                      <tr class="country-option" data-country="BE">
                          <td class="flag-holder">
                              <div class="the-flag" style="background-position: 0px 0px;"></div>
                          </td>
                          <td>
                              <span>Belgium</span>&nbsp;&nbsp;<span class="the-country-code">+32</span>
                          </td>
                      </tr>
                      <tr class="country-option" data-country="BR">
                          <td class="flag-holder">
                              <div class="the-flag" style="background-position: 0px -770px;"></div>
                          </td>
                          <td>
                              <span>Brazil</span>&nbsp;&nbsp;<span class="the-country-code">+55</span>
                          </td>
                      </tr>
                      <tr class="country-option" data-country="CA">
                          <td class="flag-holder">
                              <div class="the-flag" style="background-position: 0px -1375px;"></div>
                          </td>
                          <td>
                              <span>Canada</span>&nbsp;&nbsp;<span class="the-country-code">+1</span>
                          </td>
                      </tr>
                      <tr class="country-option" data-country="CL">
                          <td class="flag-holder">
                              <div class="the-flag" style="background-position: 0px -1342px;"></div>
                          </td>
                          <td>
                              <span>Chile</span>&nbsp;&nbsp;<span class="the-country-code">+56</span>
                          </td>
                      </tr>
                      <tr class="country-option" data-country="CN">
                          <td class="flag-holder">
                              <div class="the-flag" style="background-position: 0px -825px;"></div>
                          </td>
                          <td>
                              <span>China</span>&nbsp;&nbsp;<span class="the-country-code">+86</span>
                          </td>
                      </tr>
                      <tr class="country-option" data-country="DK">
                          <td class="flag-holder">
                              <div class="the-flag" style="background-position: 0px -1386px;"></div>
                          </td>
                          <td>
                              <span>Denmark</span>&nbsp;&nbsp;<span class="the-country-code">+45</span>
                          </td>
                      </tr>
                      <tr class="country-option" data-country="DO">
                          <td class="flag-holder">
                              <div class="the-flag" style="background-position: 0px -1529px;"></div>
                          </td>
                          <td>
                              <span>Dominican Republic</span>&nbsp;&nbsp;<span class="the-country-code">+1</span>
                          </td>
                      </tr>
                      <tr class="country-option" data-country="FR">
                          <td class="flag-holder">
                              <div class="the-flag" style="background-position: 0px -1012px;"></div>
                          </td>
                          <td>
                              <span>France</span>&nbsp;&nbsp;<span class="the-country-code">+33</span>
                          </td>
                      </tr>
                      <tr class="country-option" data-country="DE">
                          <td class="flag-holder">
                              <div class="the-flag" style="background-position: 0px -2509px;"></div>
                          </td>
                          <td>
                              <span>Germany</span>&nbsp;&nbsp;<span class="the-country-code">+49</span>
                          </td>
                      </tr>
                      <tr class="country-option" data-country="IN">
                          <td class="flag-holder">
                              <div class="the-flag" style="background-position: 0px -1694px;"></div>
                          </td>
                          <td>
                              <span>India</span>&nbsp;&nbsp;<span class="the-country-code">+91</span>
                          </td>
                      </tr>
                      <tr class="country-option" data-country="IE">
                          <td class="flag-holder">
                              <div class="the-flag" style="background-position: 0px -1969px;"></div>
                          </td>
                          <td>
                              <span>Ireland</span>&nbsp;&nbsp;<span class="the-country-code">+353</span>
                          </td>
                      </tr>
                      <tr class="country-option" data-country="IT">
                          <td class="flag-holder">
                              <div class="the-flag" style="background-position: 0px -143px;"></div>
                          </td>
                          <td>
                              <span>Italy</span>&nbsp;&nbsp;<span class="the-country-code">+39</span>
                          </td>
                      </tr>
                      <tr class="country-option" data-country="JP">
                          <td class="flag-holder">
                              <div class="the-flag" style="background-position: 0px -429px;"></div>
                          </td>
                          <td>
                              <span>Japan</span>&nbsp;&nbsp;<span class="the-country-code">+81</span>
                          </td>
                      </tr>
                      <tr class="country-option" data-country="NL">
                          <td class="flag-holder">
                              <div class="the-flag" style="background-position: 0px -1441px;"></div>
                          </td>
                          <td>
                              <span>Netherlands</span>&nbsp;&nbsp;<span class="the-country-code">+31</span>
                          </td>
                      </tr>
                      <tr class="country-option" data-country="ES">
                          <td class="flag-holder">
                              <div class="the-flag" style="background-position: 0px -1155px;"></div>
                          </td>
                          <td>
                              <span>Spain</span>&nbsp;&nbsp;<span class="the-country-code">+34</span>
                          </td>
                      </tr>
                      <tr class="country-option" data-country="SE">
                          <td class="flag-holder">
                              <div class="the-flag" style="background-position: 0px -385px;"></div>
                          </td>
                          <td>
                              <span>Sweden</span>&nbsp;&nbsp;<span class="the-country-code">+46</span>
                          </td>
                      </tr>
                      <tr class="country-option" data-country="CH">
                          <td class="flag-holder">
                              <div class="the-flag" style="background-position: 0px -1320px;"></div>
                          </td>
                          <td>
                              <span>Switzerland</span>&nbsp;&nbsp;<span class="the-country-code">+41</span>
                          </td>
                      </tr>
                      <tr class="country-option" data-country="GB">
                          <td class="flag-holder">
                              <div class="the-flag" style="background-position: 0px -55px;"></div>
                          </td>
                          <td>
                              <span>United Kingdom</span>&nbsp;&nbsp;<span class="the-country-code">+44</span>
                          </td>
                      </tr>
                      <tr class="country-option" data-country="US">
                          <td class="flag-holder">
                              <div class="the-flag" style="background-position: 0px -44px;"></div>
                          </td>
                          <td>
                              <span>United States</span>&nbsp;&nbsp;<span class="the-country-code">+1</span>
                          </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </td>
            </tr>
            <tr valign="top">
              <td class="formlabel" align="left">
                <span>Invite Participant</span>
              </td>
              <td valign="top" id="invite-option">
                <label><input type="radio" name="invite-option" value="true">Yes</label>
                &emsp;
                <label><input type="radio" name="invite-option" value="false">No</label>
              </td>
            </tr>
          </table>
        </div>
      </td>
    </tr>
    <tr>
      <td><div class="lines"></div></td>
    </tr>
    <tr>
      <td colspan="2" style="text-align: center;">
        <input type="button" id="connect-button" value="Connect"/>
        <input type="button" class="cancel" value="Cancel"/>
      </td>
    </tr>
  </table>
</div>

<div id="participateAccessForm" class="hide">
  <form method="post">
    <table border="0" cellpadding="0" align="center" style="cursor:default;">
      <tr style="height:10px;">
        <td class="formlabel" align="left">
          <h3>
            View Access Details
          </h3>
        </td>
      </tr>
      <tr>
        <td><div class="lines"></div></td>
      </tr>
      <tr>
        <td>
          <div style="max-height: 550px; min-width:400px; background:#FFFFFF; overflow-y: auto;">
            <table cellspacing="10">
              <tr valign="top">
                <td class="formlabel" align="left">
                  <span>Access Link</span>
                </td>
                <td valign="top">
                  <input onfocus="this.select()" type="text" value="" size="45" class="formfield form-control">
                  <div class="subnote">
                    Please note: viewing access code will be audited.
                  </div>
                </td>
              </tr>
            </table>
          </div>
        </td>
      </tr>
      <tr>
        <td><div class="lines"></div></td>
      </tr>
      <tr>
        <td colspan="2" style="text-align: center;">
          <input type="button" class="cancel" value="Close"/>
        </td>
      </tr>
    </table>
  </form>
</div>

<script type="text/javascript">

    var jsAtt = '${showOverlay}';

    if (jsAtt === "true"){
        jQuery.blockUI({message: jQuery('#editSubjectForm'), css: {left: "300px", top: "10px"}});
    }

    jQuery(document).ready(function () {
        jQuery.ajax({
            type: 'get',
            url: '${pageContext.request.contextPath}/pages/auth/api/clinicaldata/studies/${study.oid}/participants/${studySub.label}',
            success: function(data) {
                data.phoneNumber = data.phoneNumber || '';
                console.log(data.phoneNumber);

                $('#name-input').val(data.firstName)
                $('#email-input').val(data.email)
                var phoneParts = data.phoneNumber.split(' ');
                var countryCode = phoneParts.shift();
                var phoneNumber = phoneParts.join(' ');
                $('#country-code').text(countryCode || '+1');
                $('#phone-input').val(data.phoneNumber);

                $('#info-first-name').text(data.firstName);
                $('#info-email').text(data.email);
                $('#info-phone-number').text(data.phoneNumber);
                $('#info-participate-status').text(data.status[0] + data.status.substr(1).toLowerCase());
            },
            error: function() {
                console.log(arguments);
            }
        });

        jQuery('#editParticipantID').click(function () {
            jQuery.blockUI({message: jQuery('#editSubjectForm'), css: {left: "300px", top: "10px"}});
        });

        jQuery('#connect-button').click(function () {
            var data = {
                firstName: $('#name-input').val(),
                email: $('#email-input').val(),
                phoneNumber: $('#country-code').text() + ' ' + $('#phone-input').val(),
                inviteParticipant: $('#invite-option input:checked').val()
            };
            console.log(data);
            jQuery.ajax({
                type: 'post',
                url: '${pageContext.request.contextPath}/pages/auth/api/clinicaldata/studies/${study.oid}/participants/${studySub.label}/connect',
                contentType: 'application/json',
                data: JSON.stringify(data),
                success: function() {
                    console.log(arguments);
                },
                error: function() {
                    console.log(arguments);
                }
            });
            jQuery.unblockUI();
            return false;
        });

        jQuery('#email-input').blur(function() {
          var emailPattern = /[^\s@]+@[^\s@]+\.[^\s@]+/;
          var input = $(this).val();
          var parts = input.split('@');
          var hasSingleAt = parts.length === 2;
          var afterAt = parts[1] || '';
          var afterAtHasDot = afterAt.includes('.');
          var dotRightAfterAt = afterAt[0] === '.';
          var endsWithDot = afterAt[afterAt.length - 1] === '.';
          var validEmail = emailPattern.test(input) && hasSingleAt && afterAtHasDot && !dotRightAfterAt && !endsWithDot;
          if (validEmail) {
            $('#email-input-error').hide();
          }
          else {
            $('#email-input-error').show();
          }
        });

        jQuery('#contactInformation').click(function() {
            jQuery.blockUI({ message: jQuery('#contactInformationForm'), css:{left: "300px", top:"10px" } });
        });

        jQuery('#participateAccess').click(function() {
            jQuery.blockUI({ message: jQuery('#participateAccessForm'), css:{left: "300px", top:"10px" } });
        });

        jQuery('#phone-widget').on('click', 'div', function() {
            $('#country-options').css('display', 'block');
        });

        jQuery('#country-options').on('click', 'tr', function() {
            var countryCode = $(this).data('country');
            var ctr = getCountryByCountryCode(countryCode);
            if (ctr != null) {
                jQuery('#country-flag').css('background-position', getBackgroundPositionValue(
                    ctr.backgroundPositionLeft, ctr.backgroundPositionTop
                ));
                jQuery('#country-code').html(ctr.phoneCode);
            }
            jQuery('#country-options').css('display', 'none');
            $('#phone-input').focus();
        });
     });

    function getBackgroundPositionValue(left, top) {
        return left.toString() + 'px ' + top.toString() + 'px';
    }
    function getCountryByCountryCode(countryCode) {
        for (var i = 0; i < countries.length; i++) {
            if (countries[i].countryCode == countryCode)
                return countries[i];
        }
        return null;
    }
    var countries = [
        {
            backgroundPositionLeft: 0,
            backgroundPositionTop: -1716,
            name: 'Australia',
            phoneCode: '+61',
            countryCode: 'AU'
        },
        {
            backgroundPositionLeft: 0,
            backgroundPositionTop: -1331,
            name: 'Austria',
            phoneCode: '+43',
            countryCode: 'AT'
        },
        {
            backgroundPositionLeft: 0,
            backgroundPositionTop: 0,
            name: 'Belgium',
            phoneCode: '+32',
            countryCode: 'BE'
        },
        {
            backgroundPositionLeft: 0,
            backgroundPositionTop: -770,
            name: 'Brazil',
            phoneCode: '+55',
            countryCode: 'BR'
        },
        {
            backgroundPositionLeft: 0,
            backgroundPositionTop: -1375,
            name: 'Canada',
            phoneCode: '+1',
            countryCode: 'CA'
        },
        {
            backgroundPositionLeft: 0,
            backgroundPositionTop: -1342,
            name: 'Chile',
            phoneCode: '+56',
            countryCode: 'CL'
        },
        {
            backgroundPositionLeft: 0,
            backgroundPositionTop: -825,
            name: 'China',
            phoneCode: '+86',
            countryCode: 'CN'
        },
        {
            backgroundPositionLeft: 0,
            backgroundPositionTop: -1386,
            name: 'Denmark',
            phoneCode: '+45',
            countryCode: 'DK'
        },
        {
            backgroundPositionLeft: 0,
            backgroundPositionTop: -1529,
            name: 'Dominican Republic',
            phoneCode: '+1',
            countryCode: 'DO'
        },
        {
            backgroundPositionLeft: 0,
            backgroundPositionTop: -1012,
            name: 'France',
            phoneCode: '+33',
            countryCode: 'FR'
        },
        {
            backgroundPositionLeft: 0,
            backgroundPositionTop: -2509,
            name: 'Germany',
            phoneCode: '+49',
            countryCode: 'DE'
        },
        {
            backgroundPositionLeft: 0,
            backgroundPositionTop: -1694,
            name: 'India',
            phoneCode: '+91',
            countryCode: 'IN'
        },
        {
            backgroundPositionLeft: 0,
            backgroundPositionTop: -1969,
            name: 'Ireland',
            phoneCode: '+353',
            countryCode: 'IE'
        },
        {
            backgroundPositionLeft: 0,
            backgroundPositionTop: -143,
            name: 'Italy',
            phoneCode: '+39',
            countryCode: 'IT'
        },
        {
            backgroundPositionLeft: 0,
            backgroundPositionTop: -429,
            name: 'Japan',
            phoneCode: '+81',
            countryCode: 'JP'
        },
        {
            backgroundPositionLeft: 0,
            backgroundPositionTop: -1441,
            name: 'Netherlands',
            phoneCode: '+31',
            countryCode: 'NL'
        },
        {
            backgroundPositionLeft: 0,
            backgroundPositionTop: -1155,
            name: 'Spain',
            phoneCode: '+34',
            countryCode: 'ES'
        },
        {
            backgroundPositionLeft: 0,
            backgroundPositionTop: -385,
            name: 'Sweden',
            phoneCode: '+46',
            countryCode: 'SE'
        },
        {
            backgroundPositionLeft: 0,
            backgroundPositionTop: -1320,
            name: 'Switzerland',
            phoneCode: '+41',
            countryCode: 'CH'
        },
        {
            backgroundPositionLeft: 0,
            backgroundPositionTop: -44,
            name: 'United States',
            phoneCode: '+1',
            countryCode: 'US'
        },
        {
            backgroundPositionLeft: 0,
            backgroundPositionTop: -55,
            name: 'United Kingdom',
            phoneCode: '+44',
            countryCode: 'GB'
        },
    ];

</script>