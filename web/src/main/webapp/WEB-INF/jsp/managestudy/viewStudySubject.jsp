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
  
      jQuery('#cancel').click(function() {
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
  var storageKey = location.pathname + location.search;
  var storage = JSON.parse(sessionStorage.getItem(storageKey)) || {collapseSections: {}};
  function stor() {
    sessionStorage.setItem(storageKey, JSON.stringify(storage));
  }

  $(document.body).on('click', '.section-header', function() {
    var header = $(this);
    var body = header.next();
    var section = header.parent();
    var updown;
    if (section.hasClass('collapsed')) {
      updown = 'slideDown';
      header.attr('title', 'Collapse Section');
    }
    else {
      updown = 'slideUp';
      header.attr('title', 'Expand Section');
    }
    body[updown]('fast', function() {
      section.toggleClass('collapsed expanded');
    });

    var sections = $('div.section');
    var pos = sections.index(section);
    if (pos > 0) {
      storage.collapseSections = {};
      sections.each(function(index) {
        var section = $(this);
        if (section.hasClass('collapsed'))
          storage.collapseSections[index] = true;
      });
    }
    storage.collapseSections[pos] = section.hasClass('expanded');
    stor();
  });

  function clickAllSections(state) {
    $('.section.' + state).children('.section-header').click();
  };

  function resetAllFilters() {
    clickAllSections('collapsed');
    $('input[type=search]').val('');
    $('table.datatable').each(function() {
      var table = $(this);
      table.DataTable().order([]);
      table.DataTable().search('');
      table.dataTable().fnDraw();
    });
    $('#oc-status-hide').val('oc-status-removed').change();
  };
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
  .hide {
    display: none;
  }
  .clear {
    clear: both;
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
    Subject <c:out value="${studySub.label}"/>
  </span>
</h1>
<div class="header-links">
  <span>
    <a href="javascript:openDocWindow('ViewStudySubjectAuditLog?id=<c:out value="${studySub.id}"/>')">
      <c:out value="${studySub.label}"/> <fmt:message key="audit_log" bundle="${resword}"/>
    </a>
  </span>
  <span>&nbsp; | &nbsp;</span>
  <span>
    <label for="oc-status-hide">Showing</label>
    <select id="oc-status-hide">
      <option value="oc-status-removed">Active Records</option>
      <option value="oc-status-active">Removed Records</option>
      <option value="null">All Records</option>
    </select>
    <script>
      $('#oc-status-hide').val(storage.ocStatusHide).change();
    </script>
  </span>
</div>
<div class="header-links">
  <a href="javascript:clickAllSections('collapsed');">Expand All</a>
  <span>&nbsp; | &nbsp;</span>
  <a href="javascript:clickAllSections('expanded');">Collapse All</a>  
  <span>&nbsp; | &nbsp;</span>
  <a href="javascript:resetAllFilters();">Reset All Filters</a>  
</div>
</div>
<div class="section expanded clear hide" id="studySubjectRecord">
  <div class="section-header" title="Collapse Section">
    General Information
  </div>
  <div class="section-body">
    <table border="0" cellpadding="0" cellspacing="0">
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
                              <table width="800" border="0" cellpadding="0" cellspacing="0">
                                <!-- Table Actions row (pagination, search, tools) -->
                                <tbody>
                                  <tr>
                                    <!-- Table Tools/Actions cell -->
                                    <td class="table_actions" valign="top">
                                      <c:if test="${study.status.available}">
                                        <c:if test="${!userRole.monitor}">
                                          <a href="UpdateStudySubject?id=<c:out value="${studySub.id}"/>&amp;action=show">
                                            Edit
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
                                            <td class="table_header_row">
                                              <fmt:message key="person_ID" bundle="${resword}"/>
                                              <%-- DN for person ID goes here --%>
                                              <c:if test="${subjectStudy.studyParameterConfig.discrepancyManagement=='true' && !study.status.locked}">
                                                <c:set var="isNew" value="${hasUniqueIDNote eq 'yes' ? 0 : 1}"/>
                                                <c:choose>
                                                  <c:when test="${hasUniqueIDNote eq 'yes'}">
                                                    <a href="#" onClick="openDNoteWindow('ViewDiscrepancyNote?writeToDB=1&subjectId=${studySub.id}&id=${subject.id}&name=subject&field=uniqueIdentifier&column=unique_identifier','spanAlert-uniqueIdentifier'); return false;">
                                                      <span id="flag_uniqueIdentifier" name="flag_uniqueIdentifier" class="${uniqueIDNote.resStatus.iconFilePath}" border="0" alt="
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
                                                          <span id="flag_dob" name="flag_dob" class="${dOBNote.resStatus.iconFilePath}" border="0" alt="
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
                                                  <c:out value="${subject.dateOfBirth}" />
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
                                                          <span id="flag_dob" name="flag_dob" class="${dOBNote.resStatus.iconFilePath}" border="0" alt="
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
                                                          <span id="flag_dob" name="flag_dob" class="${dOBNote.resStatus.iconFilePath}" border="0" alt="
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
                                                <c:set var="isUpdated" value="${hasGenderNote eq 'yes' ? 0 : 1}"/>
                                                <c:set var="isClosed" value="${hasGenderNote eq 'yes' ? 0 : 1}"/>
                                                <c:set var="isClosedModified" value="${hasGenderNote eq 'yes' ? 0 : 1}"/>
                                                <c:choose>
                                                  <c:when test="${hasGenderNote eq 'yes'}">
                                                    <a href="#" onClick="openDNoteWindow('ViewDiscrepancyNote?writeToDB=1&subjectId=${studySub.id}&id=${subject.id}&name=subject&field=gender&column=gender','spanAlert-gender'); return false;">
                                                      <span id="flag_gender" name="flag_gender" class="${genderNote.resStatus.iconFilePath}" border="0" alt="
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
                                                      <span id="flag_enrollmentDate" name="flag_enrollmentDate" class="${enrollmentNote.resStatus.iconFilePath}" border="0" alt="
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
                                              <c:out value="${studySub.enrollmentDate}"/>
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
          </td>
        </tr>
      </tbody>
    </table>
    <br>
  </div>
</div>
<script>
  if (storage.collapseSections[0])
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
<div class="section expanded hide" id="subjectEvents">
  <div class="section-header" title="Collapse Section">
    Visits
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