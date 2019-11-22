<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<script>
    jQuery(function() {
        var clickedParticipant;
        jQuery(".pidVerification").click(function(e) {
            jQuery('#verification-input').val('');
            jQuery('body > table.background').hide();
            jQuery.blockUI({ message: jQuery('#verification-form'), css:{left: "300px", top:"25px" } });
            clickedParticipant = jQuery(this).closest('tr').children().first().children('a');
            return false;
        });
        jQuery('#cancel-verification').click(function() {
            jQuery.unblockUI();
            jQuery('body > table.background').show();
            return false;
        });
        jQuery('#continue-verification').click(function() {
            jQuery.unblockUI();
            jQuery('body > table.background').show();
            if (jQuery('#verification-input').val() === clickedParticipant.attr('name'))
                window.location.href = window.location.toString().replace('ListStudySubjects', clickedParticipant.attr('href'));
            return false;
        });
    });
</script>

<form id="verification-form" style="display:none">
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
                <tr valign="top">
                    <td class="formlabel" align="left">
                        <fmt:message key="enter_participant_id" bundle="${resword}"/></span>&nbsp;<small class="required">*</small></td>
                    <td valign="top">
                        <table border="0" cellpadding="0" cellspacing="0" class="full-width">
                            <tr>
                                <div class="formfieldXL_BG">
                                    <td valign="top">
                                       <input onfocus="this.select()" type="text" id="verification-input" value="" width="30" class="formfieldXL form-control" autocomplete="off">
                                    </td>
                                </div>
                            </tr>
                        </table>
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
