<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<script>
    jQuery(function() {
        var clickedParticipant;
        jQuery(".pidVerification").click(function(e) {
            jQuery('#pidv-input').val('');
            jQuery('#pidv-err').hide();                
            jQuery('body > table.background').hide();
            jQuery.blockUI({ message: jQuery('#pidv-form'), css:{left: "300px", top:"25px" } });
            clickedParticipant = jQuery(this).closest('tr').children().first().children('a');
            return false;
        });
        jQuery('#cancel-verification').click(function() {
            jQuery.unblockUI();
            jQuery('body > table.background').show();
            return false;
        });
        jQuery('#continue-verification').click(function() {
            if (jQuery('#pidv-input').val() === clickedParticipant.attr('name')) {
                if (document.getElementById("pidv-report-checkbox").checked) {
                    jQuery.ajax({
                        type: 'POST',
                        url: sessionStorage.getItem("pageContextPath") + '/pages/api/insight/report/studies/' + sessionStorage.getItem("studyOid") + '/participantID/' + clickedParticipant.attr('name') + '/create',
                    });
                }
                window.location.href = window.location.toString().replace('ListStudySubjects', clickedParticipant.attr('href'));
            } else {
                jQuery('#pidv-err').show();                
            }
            return false;
        });

        // disable right click
        jQuery("#pidv-input").on('contextmenu',function(){
            return false;
        });
        // disable cut copy paste
        jQuery("#pidv-input").bind('cut copy paste', function (e) {
            e.preventDefault();
        });
        // trigger cancel when esc
        jQuery(document).on('keyup',function(e) {
            if (e.keyCode === 27) {
                jQuery('#cancel-verification').click();
            }
        });

        // is site?

        if (sessionStorage.getItem("studyParentId") > 0) {
            var studyName = sessionStorage.getItem("studyName").trim();
            var siteSubStringMark = sessionStorage.getItem("siteSubStringMark").trim();
            var indexStartMark = studyName.indexOf(siteSubStringMark);
            if (indexStartMark > -1) {
                // is substring on end string?
                if (indexStartMark + (siteSubStringMark).length == studyName.length) {
                    jQuery('#pidv-show-checkbox').show();
                }
            }
        }
    });
</script>

<form id="pidv-form" style="display:none">
<table border="0" cellpadding="0" align="center" style="cursor:default;">
    <tr style="height:10px;">
        <td class="formlabel" align="left"><h3 class="addNewSubjectTitle"><fmt:message key="verify_participant_id" bundle="${resword}"/></h3></td>
    </tr>
    <tr>
        <td><div class="lines"></div></td>
    </tr>
    <tr>
        <td>
            <div style="max-height: 550px; min-width:400px; background:#FFFFFF; overflow-y: auto;">
            <table>
                <tr>
                    <td colspan='2'>
                        <div>
                            <div id='pidv-err' style='display:none; text-align: center; background: #ff9528; color: white; font-weight: 600;' class='alert small'>
                                <fmt:message key="verify_participant_id_failed_line1" bundle="${resword}"/><br>
                                <fmt:message key="verify_participant_id_failed_line2" bundle="${resword}"/>
                            </div>
                        </div>
                    </td>
                </tr>
                <tr valign="top">
                    <td class="formlabel" align="left">
                        <fmt:message key="enter_participant_id" bundle="${resword}"/></span>&nbsp;<small class="required">*</small></td>
                    <td valign="top">
                        <table border="0" cellpadding="0" cellspacing="0" class="full-width">
                            <tr>
                                <div class="formfieldXL_BG">
                                    <td valign="top">
                                       <input onfocus="this.select()" type="text" id="pidv-input" value="" width="30" class="formfieldXL form-control" autocomplete="off">
                                    </td>
                                </div>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr><td> &nbsp; </td></tr>
                <tr>
                    <td valign='top' colspan='2' style='display:none; text-align:left;' id='pidv-show-checkbox'>
                        <div class='formfieldXL_BG'>
                            <div>
                                <input type='checkbox' name='checkboxReport' id='pidv-report-checkbox' value='true'/>
                                <label style='font-size:16px; font-weight:600;'>
                                    <fmt:message key="run_reabstraction_report" bundle="${resword}"/>
                                </label>
                            </div>
                            <div style='font-style:italic; color:#555555; padding-left:25px;'>
                                <fmt:message key="run_reabstraction_desc_line1" bundle="${resword}"/>
                                <a href='Jobs'>
                                    <fmt:message key="run_reabstraction_desc_line2" bundle="${resword}"/>
                                </a>.
                                <br>
                                <fmt:message key="run_reabstraction_desc_line3" bundle="${resword}"/>
                            </div>
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
            <input type="button" id="cancel-verification" name="cancel" value="<fmt:message key='cancel' bundle='${resword}'/>"/>
            &nbsp;
            <input type="submit" id="continue-verification" name="continue" value="<fmt:message key='continue' bundle='${resword}'/>"/>
        </td>
    </tr>

</table>
</form>
