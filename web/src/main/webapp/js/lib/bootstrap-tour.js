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
                title: "<span class='addNewSubjectTitle'>Verify Participant ID</span>",
                content: "<table border='0' cellpadding='0' align='center' style='cursor:default; width: 100%; margin-top: 20px;'> \
                            <tr> \
                                <td> \
                                    <table border='0' cellpadding='0' cellspacing='0' class='full-width'> \
                                        <tr> \
                                            <td colspan='2'> \
                                                <div style='margin-top: 10px;margin-bottom: 5px'>\
                                                    <div id='pidv-err' style='opacity: 0; text-align: center; background: #ff9528; color: white; font-weight: 600; margin-top: -33px;' class='alert small'> \
                                                        <p style='margin: 0;'>The Participant ID entered does not match what you selected.</p> \
                                                        Please check and re-enter, or select a different participant. \
                                                    </div> \
                                                </div>\
                                            </td> \
                                        </tr> \
                                        <tr> \
                                            <td valign='top' style='width: 132px;'> \
                                                <div class='formfieldXL_BG' style='padding-top: 5px;'> \
                                                    Enter Participant ID  \
                                                </div> \
                                            </td> \
                                            <td valign='top'> \
                                                <div class='formfieldXL_BG'> \
                                                    <input type='text' name='label' id='retype_pid' width='30' class='formfieldXL form-control' autofocus> \
                                                </div> \
                                            </td> \
                                        </tr> \
                                        <tr> \
                                            <td valign='top'> \
                                            </td> \
                                            <td valign='top'> \
                                                <div class='formfieldXL_BG' id='checkboxReportId' style='display: none'> \
                                                    <table style='width: 310px; margin-left: -5px;'> \
                                                        <tr> \
                                                            <td style='padding: 7px 0 5px;' > \
                                                                <span> \
                                                                    <input type='checkbox' name='checkboxReport' id='cbReport' value='true'/> \
                                                                </span> \
                                                            </td> \
                                                            <td style='padding: 5px 0;'> \
                                                                <span> \
                                                                    <span style='font-size: 16px; font-weight: 600;'>Run data re-abstraction report</span>\
                                                                </span> \
                                                            </td> \
                                                        </tr> \
                                                        <tr> \
                                                            <td></td> \
                                                            <td> \
                                                                <span style='font-style: italic; color: #555555;'>Finished report can be accessed on <a href='Jobs'>the bulk actions log page</a>.<br></span> \
                                                                <span style='font-style: italic; color: #555555;'>Please allow some time and refresh the page to see the report.</span> \
                                                            </td> \
                                                        </tr> \
                                                    </table> \
                                                </div> \
                                            </td> \
                                        </tr> \
                                    </table> \
                                </td> \
                            </tr> \
                            <tr> \
                                <td colspan='2' style='text-align: right;'> \
                                    <div style='margin-top: 25px;'> \
                                        <input type='button' value='Cancel' onclick='clearPIDVerificationForm()'/> \
                                        &nbsp; \
                                        <input type='button' value='Continue' onclick='validatePIDVerificationForm()'/> \
                                    </div> \
                                </td> \
                            </tr> \
                        </table>"
            }],
            template: "<div class='popover tour pid'> \
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
                // trigger continue when enter
                jQuery("#step-0").find('#retype_pid').on('keypress',function(e) {
                    if (e.which == 13) {
                        validatePIDVerificationForm();
                    }
                });
                // trigger cancel when esc
                jQuery(document).on('keyup',function(e) {
                    if (e.keyCode === 27) {
                        clearPIDVerificationForm();
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
                            jQuery("#step-0").css({'height': '325px'});
                            jQuery("#step-0").find('#checkboxReportId').css({'display': 'block'});
                        }
                    }
                }


            }
        });

        tourElement.init();
        tourElement.start(true);
    });

    // disable right click open
    jQuery('.pidVerification').on('contextmenu',function(){
        return false;
    });
});

function clearPIDVerificationForm() {
    tourElement.end();
    jQuery("#step-0").find('input#retype_pid').val("");
    jQuery("#step-0").find('#pidv-err').css({'opacity':0});
}

function validatePIDVerificationForm() {
    if (jQuery("#step-0").find('input').val() === currentPLabel) {
        jQuery("#step-0").find('#pidv-err').css({'opacity':0});
        if (document.getElementById("cbReport").checked) {
            createReport();
        }
        window.location = window.location.origin + sessionStorage.getItem("pageContextPath") + "/ViewStudySubject?id=" + currentPID;
    } else {
        jQuery("#step-0").find('#pidv-err').css({'opacity':1});
    }
}

function createReport() {
    jQuery.ajax({
        type: 'POST',
        url: sessionStorage.getItem("pageContextPath") + '/pages/api/insight/report/studies/' + sessionStorage.getItem("studyOid") + '/participantID/' + currentPLabel + '/create',
    });
}