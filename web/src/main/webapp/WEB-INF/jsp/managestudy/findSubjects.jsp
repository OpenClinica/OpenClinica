<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>


<jsp:include page="../include/submit-header.jsp"/>
<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

<link rel="stylesheet" href="includes/jmesa/jmesa.css" type="text/css">
<style>
    .icon > span {
        font-family: 'Open Sans', arial, helvetica, sans-serif;
    }
</style>

<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.jmesa.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jmesa.js"></script>
<%-- <script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jmesa-original.js"></script> --%>
<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery.blockUI.js"></script>

<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery-migrate-1.4.1.js"></script>

<link href="includes/bootstrap-tour-0.12.0/css/bootstrap-tour-standalone.min.css" rel="stylesheet">
<script src="includes/bootstrap-tour-0.12.0/js/bootstrap-tour-standalone.min.js"></script>

<script type="text/javascript">
    var currentPID = "null";
    var currentPLabel = "null";
    var tourElement = "null";
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

        jQuery('input#cancel').click(function() {
            jQuery.unblockUI();
            return false;
        });

        var params = new URLSearchParams(window.location.search);
        if (params.get('addNewSubject')) {
            jQuery('#addSubject').click();
        }


        jQuery('.pidVerification').click(function() {
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

    window.onload = function() {
        document.getElementById("btn").focus();
            <c:if test="${showOverlay}">
                jQuery.blockUI({ message: jQuery('#addSubjectForm'), css:{left: "300px", top:"10px" } });
            </c:if>
    };


</script>

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


<h1>
    <span class="title_manage">
        <fmt:message key="view_subjects_in" bundle="${restext}"/> <c:out value="${study.name}"/>
    </span>
</h1>
<br/>

<div id="box" class="dialog">
    <span id="mbm">
        <br>
        <c:if test="${(!study.status.pending)}">
            <fmt:message key="study_frozen_locked_note" bundle="${restext}"/>
        </c:if>

        <c:if test="${(study.status.pending)}">
            <fmt:message key="study_design_note" bundle="${restext}"/>
        </c:if>
    </span><br>
    <div style="text-align:center; width:100%;">
        <button id="btn" onclick="hm('box');">OK</button>
    </div>
</div>

<div id="findSubjectsDiv">
    <form  action="${pageContext.request.contextPath}/ListStudySubjects">
        <input type="hidden" name="module" value="admin">
        ${findSubjectsHtml}
    </form>
</div>

<c:if test="${userRole.monitor || userRole.coordinator || userRole.director || userRole.investigator || userRole.researchAssistant || userRole.researchAssistant2}">
    <div id="addSubjectForm" style="display:none;">
          <c:import url="../submit/addNewSubjectExpressNew.jsp">
          </c:import>
    </div>
</c:if>

<br>
<jsp:include page="../include/footer.jsp"/>
