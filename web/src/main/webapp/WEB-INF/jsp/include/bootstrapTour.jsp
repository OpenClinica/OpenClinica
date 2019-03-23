<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>

<script type="text/javascript">
    var currentPID = "null";
    var currentPLabel = "null";
    var tourElement = "null";

    jQuery(document).ready(function() {

        jQuery('.pidVerification').click(function(event) {
            event.preventDefault();
            currentPLabel = jQuery(this)[0].name;
            currentPID = jQuery(this)[0].id.split("pid-")[1];
            tourElement = new Tour({
                name: "pidv",
                backdrop: true,
                backdropPadding: {
                    top: -10,
                    right: 0,
                    bottom: -10,
                    left: 0
                },
                next: 1,
                prev: 1,
                steps:
                [{
                    element: "#" + jQuery(this)[0].id,
                    title: "<span class='addNewSubjectTitle'><fmt:message key='pid_verification' bundle='${resword}'/></span>",
                    content: "<table border='0' cellpadding='0' align='center' style='cursor:default;'> \
                                <tr> \
                                    <td> \
                                        <table border='0' cellpadding='0' cellspacing='0' class='full-width'> \
                                            <tr> \
                                                <td valign='top'> \
                                                    <div class='formfieldXL_BG' style='width: 250px;'> \
                                                        <input type='text' name='label' id='retype_pid' width='30' class='formfieldXL form-control'> \
                                                    </div> \
                                                </td> \
                                            </tr> \
                                            <tr> \
                                                <td> \
                                                    <div style='margin-top: 5px;margin-bottom: 5px'>\
                                                        <div id='pidv-err' style='display: none;' class='alert small'> \
                                                            <fmt:message key='pidv_err' bundle='${resword}'/> \
                                                        </div> \
                                                        <div id='pidv-match' style='display: none; color: #0cb924;' class='alert small'> \
                                                            <fmt:message key='pidv_match' bundle='${resword}'/> \
                                                        </div> \
                                                    </div>\
                                                </td> \
                                            </tr> \
                                        </table> \
                                    </td> \
                                </tr> \
                                <tr> \
                                    <td colspan='2' style='text-align: center;'> \
                                            <input type='button' value='Cancel' onclick='clearPIDVerificationForm()'/> \
                                            &nbsp; \
                                            <input type='button' value='Checking' onclick='validatePIDVerificationForm()'/> \
                                    </td> \
                                </tr> \
                            </table>"
                }],
                template: "<div class='popover tour'> \
                            <div class='arrow'></div> \
                            <h3 class='popover-title'></h3> \
                            <div class='popover-content'></div> \
                            <nav class='popover-navigation' style='display:none;'> \
                                <button class='btn btn-default' data-role='prev'>« Prev</button> \
                                <span data-role='separator'>|</span> \
                                <button class='btn btn-default' data-role='next'>Next »</button> \
                                <button class='btn btn-default' data-role='end'>End tour</button> \
                            </nav> \
                            </div>",
                onEnd: function(t) {
                    // need to found the correct way to handle this default setting
                    jQuery("#pid-" + currentPID).css({'display':'block'});
                },
                onShown: function(t) {
                    // disable right click
                    jQuery("#step-0").find('#retype_pid').on('contextmenu',function(){
                        return false;
                    });
                    // disable cut copy paste
                    jQuery("#step-0").find('#retype_pid').bind('cut copy paste', function (e) {
                        e.preventDefault();
                    });
                }
            });

            tourElement.init();
            tourElement.start(true);
        });
    });

    function clearPIDVerificationForm() {
        tourElement.end();
        jQuery("#step-0").find('input#retype_pid').val("");
        jQuery("#step-0").find('#pidv-err').css({'display':'none'});
        jQuery("#step-0").find('#pidv-match').css({'display':'none'});
    }

    function validatePIDVerificationForm() {
        if (jQuery("#step-0").find('input').val() === currentPLabel) {
            jQuery("#step-0").find('#pidv-err').css({'display':'none'});
            jQuery("#step-0").find('#pidv-match').css({'display':'block'});
            window.location = window.location.origin + "/OpenClinica/ViewStudySubject?id=" + currentPID;
        } else {
            jQuery("#step-0").find('#pidv-err').css({'display':'block'});
            jQuery("#step-0").find('#pidv-match').css({'display':'none'});
        }
    }
</script>
