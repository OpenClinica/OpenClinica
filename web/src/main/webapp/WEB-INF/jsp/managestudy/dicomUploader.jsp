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

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
  <td class="sidebar_tab">
    <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
      <span class="icon icon-caret-down gray"></span>
    </a>
    <fmt:message key="instructions" bundle="${restext}"/>
    <div class="sidebar_tab_content"></div>
  </td>
</tr>
<tr id="sidebar_Instructions_closed" style="display: all">
  <td class="sidebar_tab">
  <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray"></span></a>
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
</style>

<jsp:include page="../include/sideInfo.jsp"/>

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
                                <input type="file" name="file" id="file-input">
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
  <button id="btn-upload" class="button_long">
    <fmt:message key='upload' bundle='${resword}'/>
    <img id="loading" src="${pageContext.request.contextPath}/images/25.svg" style="display:none;">
  </button>
  <input type="button" onclick="window.close();" value="<fmt:message key='cancel' bundle='${resword}'/>" class="button_medium">
</form>

<script>
  var url = new URL(location);
  var participantId = url.searchParams.get("participantId");
  var accessionId = url.searchParams.get("accessionId");
  $("#participant-id").val(participantId);
  $("#accession-id").val(accessionId);

  $('#btn-upload').click(function() {
    $('#loading').show();
    var data = new FormData();
    jQuery.each($('#file-input')[0].files, function(i, file) {
        data.append('file', file);
    });
    
    $.ajax({
      url: '${pageContext.request.contextPath}/pages/api/dicom/participantID/' + participantId + '/accessionID/' + accessionId + '/upload',
      method: 'POST',
      type: 'POST',
      data: data,
      processData: false,
      contentType: false,
      success: function(r) {
        console.log('success', r);
        $('#loading').hide();
      },
      error: function(r) {
        console.log('error', r);
        $('#loading').hide();
      }
    });
    return false;
  });
</script>
