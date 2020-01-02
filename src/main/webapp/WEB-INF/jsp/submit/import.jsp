<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterms"/>
<c:choose>
<c:when test="${userBean.sysAdmin && module=='admin'}">
 <c:import url="../include/admin-header.jsp"/>
</c:when>
<c:otherwise>
 <c:import url="../include/submit-header.jsp"/>
</c:otherwise>
</c:choose>


<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<style>
  #sidebar_Alerts_open .sidebar_tab_content {
    color: #ED7800;
    font-style: italic;
  }
</style>
<script>
  $('#sidebar_Alerts_open > .sidebar_tab > .sidebar_tab_content').append(
    $('<div>', {id:'upload-failed'})
      .append('<fmt:message key="import_data_failed" bundle="${resword}"/>')
      .hide()
  ).append(
    $('<div>', {id:'upload-failed-study-oid-invalid'})
      .append('<fmt:message key="import_data_failed_study_oid_invalid" bundle="${resword}"/>')
      .hide()
  ).append(
    $('<div>', {id:'upload-failed_study_oid_missing'})
      .append('<fmt:message key="import_data_failed_study_oid_missing" bundle="${resword}"/>')
      .hide()
  ).append(
    $('<div>', {id:'upload-success'})
      .append('<fmt:message key="import_data_success" bundle="${resword}"/>')
      .append('<strong> <fmt:message key="import_data_success_2" bundle="${resword}"/> </strong>')
      .append('<fmt:message key="import_data_success_3" bundle="${resword}"/>')
      .hide()
  );
</script>

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: all">
	<td class="sidebar_tab">
		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
			<span class="icon icon-caret-down gray"></span>
		</a>
		<fmt:message key="instructions" bundle="${restext}"/>
		<div class="sidebar_tab_content">
			<fmt:message key="import_side_bar_instructions" bundle="${restext}"/>
		</div>
	</td>
</tr>

<tr id="sidebar_Instructions_closed" style="display: none">
	<td class="sidebar_tab">
		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
			<span class="icon icon-caret-right gray"></span>
		</a>
		<fmt:message key="instructions" bundle="${restext}"/>
	</td>
</tr>



<jsp:include page="../include/sideInfo.jsp"/>


<jsp:useBean scope='session' id='version' class='core.org.akaza.openclinica.bean.submit.CRFVersionBean'/>
<jsp:useBean scope='session' id='userBean' class='core.org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='crfName' class='java.lang.String'/>

 <c:out value="${crfName}"/>

<c:choose>
	<c:when test="${userBean.sysAdmin && module=='admin'}">
		<h1><span class="title_manage">
	</c:when>
	<c:otherwise>
		<h1>
		<span class="title_submit">
	</c:otherwise>
</c:choose>

<fmt:message key="import_crf_data" bundle="${resworkflow}"/>
<a href="javascript:openDocWindow('https://docs.openclinica.com/3.1/openclinica-user-guide/submit-data-module-overview/import-data')">
    <span class=""></span>
</a></h1>
<p><fmt:message key="import_instructions" bundle="${restext}"/></p>



<form action="ImportCRFData?action=confirm&crfId=<c:out value="${version.crfId}"/>&name=<c:out value="${version.name}"/>" method="post" ENCTYPE="multipart/form-data">
<div style="width: 400px">

<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="textbox_center">
<table border="0" cellpadding="0" cellspacing="0">

<tr>
	<td class="formlabel"><!--<fmt:message key="xml_file_to_upload" bundle="${resterms}"/>:--></td>
	<td>
		<div class="formfieldFile_BG">
			<input type="file" id="file-input" accept=".xml">
		</div>
		<br><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="xml_file"/></jsp:include>
	</td>
</tr>
<input type="hidden" name="crfId" value="<c:out value="${version.crfId}"/>">

</table>
</div>
</div></div></div></div></div></div></div></div>
</div>

<br clear="all">
<input type="button" id="btn-upload" value="<fmt:message key="submit" bundle="${resword}"/>">
<input type="reset" id="btn-cancel" value="<fmt:message key="cancel" bundle="${resword}"/>"/>

</form>
<br/>
<div class="homebox_bullets"><a href="ImportRule?action=downloadImportTemplate"><b><fmt:message key="download_import_template" bundle="${resword}"/></b></a></div>
<!-- <div class="homebox_bullets"><a href="pages/Log/listFiles"><b>Bulk Job log</b></a></div> -->

<script>
  $('#file-input').on('change', function() {
    if ($(this).val())
      $('#btn-upload').removeAttr('disabled');
    else
      $('#btn-upload').attr('disabled', 'disabled');
  });

  $('#btn-upload').click(function() {
    $('#upload-failed, #upload-failed-study-oid-invalid, #upload-failed_study_oid_missing, #upload-success, #btn-label-upload').hide();
    $('#loading, #btn-label-uploading').show();

    var data = new FormData();
    $.each($('#file-input')[0].files, function(i, file) {
      data.append('file', file);
    });

    function success(r) {
      console.log('success', r);
      $('#upload-success, #btn-label-upload').show();
      $('#loading, #btn-label-uploading').hide();
      if (!$('#sidebar_Alerts_open').is(':visible')) {
        leftnavExpand('sidebar_Alerts_open');
        leftnavExpand('sidebar_Alerts_closed');
      }
    }


    function failed(r) {
      console.log('error', r);
      if (r.responseText=="errorCode.studyNotExist") {
        $('#upload-failed-study-oid-invalid, #btn-label-upload').show();
      } else if (r.responseText=="errorCode.studyOidMissing") {
        $('#upload-failed_study_oid_missing, #btn-label-upload').show();
      } else {
        $('#upload-failed, #btn-label-upload').show();
      }
      $('#loading, #btn-label-uploading').hide();
      if (!$('#sidebar_Alerts_open').is(':visible')) {
        leftnavExpand('sidebar_Alerts_open');
        leftnavExpand('sidebar_Alerts_closed');
      }
    }
    
    $.ajax({
      url: '${pageContext.request.contextPath}/pages/auth/api/clinicaldata/import',
      method: 'POST',
      type: 'POST',
      data: data,
      processData: false,
      contentType: false,
      success: function(r) {
        success(r);
      },
      error: function(r) {
        failed(r);
      }
    });
    return false;
  });

  $('#btn-cancel').click(function() {
  });
</script>

<jsp:include page="../include/footer.jsp"/>