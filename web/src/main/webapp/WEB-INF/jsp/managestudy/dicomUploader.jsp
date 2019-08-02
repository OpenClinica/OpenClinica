<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<jsp:include page="../include/managestudy-header.jsp"/>

<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<script>
  $('#sidebar_Alerts_open > .sidebar_tab > .sidebar_tab_content').append(
    $('<div>', {id:'upload-failed'})
      .append('<h3><fmt:message key="upload_dicom_failed" bundle="${resword}"/></h3>')
      .append('<p>- <fmt:message key="upload_dicom_failed_1" bundle="${resword}"/></p>')
      .append('<p>- <fmt:message key="upload_dicom_failed_2" bundle="${resword}"/></p>')
      .hide()
  ).append(
    $('<div>', {id:'upload-success'})
      .append('<h3><fmt:message key="upload_dicom_success" bundle="${resword}"/></h3>')
      .append('<p>- <fmt:message key="upload_dicom_success_1" bundle="${resword}"/></p>')
      .hide()
  );
</script>

<!-- then instructions-->
<tr id="sidebar_Instructions_open">
  <td class="sidebar_tab">
    <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
      <span class="icon icon-caret-down gray"></span>
    </a>
    <fmt:message key="instructions" bundle="${restext}"/>
    <div class="sidebar_tab_content">
      <fmt:message key='upload_dicom_instructions' bundle='${resword}'/>
    </div>
  </td>
</tr>
<tr id="sidebar_Instructions_closed" style="display:none;">
  <td class="sidebar_tab">
  <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
    <span class="icon icon-caret-right gray"></span>
  </a>
  <fmt:message key="instructions" bundle="${restext}"/>
  </td>
</tr>

<style>
  .form-inputs td {
    padding-right: 10px;
  }
  .form-inputs .empty {
    width: 50px;
  }
  .form-inputs input {
    width: 125px;
    font-weight: bold;
    color: #bbb;
  }
  #sidebar_Alerts_open .sidebar_tab_content {
    color: #ED7800;
    font-style: italic;
  }
  #success-page {
    text-align: center;
  }
  h1.success {
    color: #70b728;
    margin-top: 110px;
    margin-bottom: 20px;
  }
  h2.success {
    font-size: 14px;
    font-style: italic;
    font-weight: bold;
  }
  #btn-close {
    margin-top: 50px;
  }
</style>

<jsp:include page="../include/sideInfo.jsp"/>
<div id="upload-page">
  <h1>
    <fmt:message key="upload_dicom_title" bundle="${resword}"/>
  </h1>
  <p>
    <fmt:message key="upload_dicom_desc" bundle="${resword}"/>
  </p>

  <form id="form-upload">
    <table class="form-inputs">
      <tr>
        <td>
          <fmt:message key="participant_ID" bundle="${resword}"/>
        </td>
        <td>
          <input type="text" id="participant-id" readonly="readonly" class="readonly">
        </td>
        <td class="empty">&nbsp;</td>
        <td>
          <fmt:message key="accession" bundle="${resword}"/>
        </td>
        <td>
          <input type="text" id="accession-id" readonly="readonly" class="readonly">
        </td>
      <tr>
    </table>
    <div style="width: 400px">
      <div class="box_T">
        <div class="box_L">
          <div class="box_R">
            <div class="box_B">
              <div class="box_TL">
                <div class="box_TR">
                  <div class="box_BL">
                    <div class="box_BR">
                      <div class="textbox_center">
                        <table border="0" cellpadding="0" cellspacing="0">
                          <tbody>
                            <tr>
                              <td class="formlabel"></td>
                              <td>
                                <div class="formfieldFile_BG">
                                  <input type="file" name="file" id="file-input" accept=".zip">
                                </div>
                                <br>
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
    </div>
    <br clear="all">
    <button id="btn-upload" disabled="disabled">
      <span id="btn-label-upload">
        <fmt:message key='upload' bundle='${resword}'/>
      </span>
      <span id="btn-label-uploading" class="hide">
        <fmt:message key='uploading' bundle='${resword}'/>
      </span>
      <img id="loading" src="${pageContext.request.contextPath}/images/25.svg" style="display:none;">
    </button>
    <input type="button" id="btn-cancel" value="<fmt:message key='cancel' bundle='${resword}'/>">
  </form>
</div>
<div id="success-page" class="hide">
  <h1 class="success">
    <fmt:message key='upload_dicom_success_2' bundle='${resword}'/>
  </h1>
  <h2 class="success">
    <fmt:message key='upload_dicom_success_3' bundle='${resword}'/>
  </h2>
  <input type="button" id="btn-close" value="<fmt:message key='close' bundle='${resword}'/>">
</div>

<script>
  var url = new URL(location);
  var participantId = url.searchParams.get('pid');
  var accessionId = url.searchParams.get('accid');
  $("#participant-id").val(participantId);
  $("#accession-id").val(accessionId);

  $('#file-input').on('change', function() {
    if ($(this).val())
      $('#btn-upload').removeAttr('disabled');
    else
      $('#btn-upload').attr('disabled', 'disabled');
  });

  $('#btn-upload').click(function() {
    $('#upload-failed, #upload-success, #btn-label-upload').hide();
    $('#loading, #btn-label-uploading').show();
    var data = new FormData();
    $.each($('#file-input')[0].files, function(i, file) {
      data.append('file', file);
    });

    function success(r) {
      console.log('success', r);
      $('#upload-success, #success-page').show();
      $('#upload-page').slideUp();
      if (!$('#sidebar_Alerts_open').is(':visible')) {
        leftnavExpand('sidebar_Alerts_open');
        leftnavExpand('sidebar_Alerts_closed');
      }
    }

    function failed(r) {
      console.log('error', r);
      $('#upload-failed, #btn-label-upload').show();
      $('#loading, #btn-label-uploading').hide();
      if (!$('#sidebar_Alerts_open').is(':visible')) {
        leftnavExpand('sidebar_Alerts_open');
        leftnavExpand('sidebar_Alerts_closed');
      }
    }
    
    $.ajax({
      url: '${pageContext.request.contextPath}/pages/auth/api/dicom/participantID/' + participantId + '/accessionID/' + accessionId + '/upload',
      method: 'POST',
      type: 'POST',
      data: data,
      processData: false,
      contentType: false,
      success: function(r) {
        if (r == 'UPLOAD SUCCESS')
          success(r);
        else
          failed(r);
      },
      error: failed
    });
    return false;
  });

  $('#btn-cancel').click(function() {
    if (confirm('<fmt:message key="upload_dicom_cancel" bundle="${resword}"/>'))
      $('#btn-close').click();
  });

  $('#btn-close').click(function() {
    window.close();
  });
</script>
