<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<jsp:useBean scope='session' id='userBean' class='core.org.akaza.openclinica.bean.login.UserAccountBean'/>

<%
    String currentURL = null;
    if (request.getAttribute("javax.servlet.forward.request_uri") != null) {
        currentURL = (String) request.getAttribute("javax.servlet.forward.request_uri");
    }
    if (currentURL != null && request.getQueryString() != null) {
        currentURL += "?" + request.getQueryString();
        currentURL = StringEscapeUtils.escapeHtml(currentURL);
        currentURL = StringEscapeUtils.escapeJavaScript(currentURL);
    }
%>

<script>
    var myContextPath = "${pageContext.request.contextPath}";
    var sessionTimeoutVal = '<%= session.getMaxInactiveInterval() %>';
    console.log("***********************************sessionTimeoutVal:"+ sessionTimeoutVal);
    var userName = "<%= userBean.getName() %>";
    var currentURL = "<%= currentURL %>";
    var crossStorageURL = '<%= session.getAttribute("crossStorageURL")%>';
    console.log("***********************************Getting crossStorage:"+ crossStorageURL);
    var ocAppTimeoutKey = "OCAppTimeout";
    var firstLoginCheck = '<%= session.getAttribute("firstLoginCheck")%>';
    console.log("First time flag value:" + firstLoginCheck);
    // for forceRenewAuth. It only happens with Home page, so no need to copy this to enketoFormServlet.jsp
    var doNotInvalidate = '<%= request.getParameter("firstLoginCheck")%>';
    if (doNotInvalidate === "true")
        firstLoginCheck = doNotInvalidate;

    console.log("***********************************firstLoginCheck as a parameter:" + firstLoginCheck);
    var CURRENT_USER = "currentUser";
    var appName = "RT";
</script>

<jsp:useBean scope='session' id='tableFacadeRestore' class='java.lang.String'/>
<c:set var="restore" value="true"/>
<c:if test="${tableFacadeRestore=='false'}"><c:set var="restore" value="false"/></c:if>
<c:set var="profilePage" value="${param.profilePage}"/>
<!-- If Controller Spring based append ../ to urls -->
<c:set var="urlPrefix" value=""/>
<c:set var="requestFromSpringController" value="${param.isSpringController}"/>
<c:set var="requestFromSpringControllerCCV" value="${param.isSpringControllerCCV}"/>
<link rel="stylesheet" href="${pageContext.request.contextPath}/includes/jmesa/jmesa.css" type="text/css">
<script type="text/JavaScript" language="JavaScript" src="${pageContext.request.contextPath}/includes/jmesa/jquery.min.js"></script>
<script type="text/javascript" language="JavaScript" src="${pageContext.request.contextPath}/includes/jmesa/jquery-migrate-3.1.0.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="${pageContext.request.contextPath}/includes/jmesa/jmesa.js"></script>
<script type="text/JavaScript" language="JavaScript" src="${pageContext.request.contextPath}/includes/jmesa/jquery.jmesa.js"></script>
<script type="text/javascript" language="JavaScript" src="${pageContext.request.contextPath}/includes/jmesa/jquery.blockUI.js"></script>
<c:choose>
    <c:when test="${requestFromSpringController == 'true' || requestFromSpringControllerCCV == 'true'}">
        <c:set var="urlPrefix" value="${pageContext.request.contextPath}/"/>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/includes/css/icomoon-style.css">
        <script src="https://cdnjs.cloudflare.com/ajax/libs/bluebird/3.3.4/bluebird.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/es6-promise.auto.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/client.js"></script>
        <script type="text/javascript">
            var storage = new CrossStorageClient(crossStorageURL);
        </script>
        <script type="text/javascript" language="JavaScript" src="${pageContext.request.contextPath}/includes/sessionTimeout.js"></script>
        <script type="text/javascript" language="JavaScript" src="${pageContext.request.contextPath}/includes/auth0/captureKeyboardMouseEvents.js"></script>
        <script type="text/javascript">
            console.log("***********************************Getting crossStorage");
            var storage = new CrossStorageClient(crossStorageURL, {
                timeout: 7000
            });
        </script>
        <script type="text/javascript" language="JavaScript" src="${pageContext.request.contextPath}/includes/moment.min.js"></script>
    </c:when>
    <c:otherwise>
        <link rel="stylesheet" href="includes/css/icomoon-style.css">
        <script src="https://cdnjs.cloudflare.com/ajax/libs/bluebird/3.3.4/bluebird.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/es6-promise.auto.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/client.js"></script>
        <script type="text/javascript">
            var storage = new CrossStorageClient(crossStorageURL, {
                timeout: 7000});
        </script>
        <script type="text/javascript" language="JavaScript" src="${pageContext.request.contextPath}/includes/sessionTimeout.js"></script>
        <script type="text/javascript" language="JavaScript" src="${pageContext.request.contextPath}/includes/auth0/captureKeyboardMouseEvents.js"></script>
        <script type="text/javascript" language="JavaScript" src="${pageContext.request.contextPath}/includes/moment.min.js"></script>
    </c:otherwise>
</c:choose>

<script type="text/javascript">

    var realInterval = 60;
    if (sessionTimeoutVal < realInterval)
        realInterval = sessionTimeoutVal;
    /**
     * Self-adjusting interval to account for drifting
     *
     * @param {function} workFunc  Callback containing the work to be done
     *                             for each interval
     * @param {int}      interval  Interval speed (in milliseconds) - This
     * @param {function} errorFunc (Optional) Callback to run if the drift
     *                             exceeds interval
     */
    /*
    function AdjustingInterval(workFunc, interval, errorFunc) {
        var that = this;
        var expected, timeout;
        this.interval = interval;

        this.start = function() {
            expected = moment().valueOf() + this.interval;
            timeout = setTimeout(step, this.interval);
        }

        this.stop = function() {
            clearTimeout(timeout);
        }

        function step() {
            var drift = moment().valueOf() - expected;
            if (drift > that.interval) {
                // You could have some default stuff here too...
                if (errorFunc) errorFunc();
            }
            workFunc();
            expected += that.interval;
            timeout = setTimeout(step, Math.max(0, that.interval-drift));
        }
    }
    var doWork = function () {
        processTimedOuts(true, false);
    };
    // Define what to do if something goes wrong
    var doError = function() {
        console.warn('The drift exceeded the interval.');
    };

    var ticker = new AdjustingInterval(doWork, realInterval * 1000, doError);
    ticker.start();
    */
    setInterval(function () {
            processTimedOuts(true, false);
        },
        realInterval * 1000
    );

	// hacking OC-13156 Fix border style issues in latest Chrome
    jQuery( document ).ready(function() {
        if (navigator.userAgent.toLowerCase().indexOf('chrome') > -1) {
            jQuery('input[type="text"]').each(function(){
                jQuery(this).css({'background-color':'revert', 'outline':'none'});
            });
            jQuery('input[type="password"]').each(function(){
                jQuery(this).css({'background-color':'revert', 'outline':'none'});
            });
            jQuery('textarea').each(function(){
                jQuery(this).css({'background-color':'revert', 'outline':'none'});
            });
            jQuery('select').each(function(){
	            jQuery(this).css({'outline':'none'});
	        });
	        jQuery('.dynFilter').each(function(){
                jQuery(this).css({'outline':'none'});
            });
        }
    });

</script>

<script type="text/javaScript">
    processTimedOuts(true, false);
    //Piwik
    var _paq = _paq || [];
    /* tracker methods like "setCustomDimension" should be called before "trackPageView" */
    _paq.push(["setDocumentTitle", document.domain + "/" + document.title]);
    _paq.push(['trackPageView']);
    _paq.push(['enableLinkTracking']);
    (function() {
    var u='<c:out value="${sessionScope.piwikURL}" />';
    _paq.push(['setTrackerUrl', u+'piwik.php']);
    _paq.push(['setSiteId', '1']);
    var d=document, g=d.createElement('script'), s=d.getElementsByTagName('script')[0];
    g.type='text/javascript'; g.async=true; g.defer=true; g.src=u+'piwik.js'; s.parentNode.insertBefore(g,s);
    })();

    function confirmCancel(pageName) {
        var confirm1 = confirm('<fmt:message key="sure_to_cancel" bundle="${resword}"/>');
        if (confirm1) {
            window.location = pageName;
        }
    }

    function confirmExit(pageName) {
        var confirm1 = confirm('<fmt:message key="sure_to_exit" bundle="${resword}"/>');
        if (confirm1) {
            window.location = pageName;
        }
    }

    function goBack() {
        var confirm1 = confirm('<fmt:message key="sure_to_cancel" bundle="${resword}"/>');
        if (confirm1) {
            return history.go(-1);
        }
    }

    function lockedCRFAlert(userName) {
        alert('<fmt:message key="CRF_unavailable" bundle="${resword}"/>' + '\n'
            + ' User ' + userName + ' ' + '<fmt:message key="Currently_entering_data" bundle="${resword}"/>' + '\n'
            + '<fmt:message key="Leave_the_CRF" bundle="${resword}"/>');
        return false;
    }

    function confirmCancelAction(pageName, contextPath) {
        var confirm1 = confirm('<fmt:message key="sure_to_cancel" bundle="${resword}"/>');
        if (confirm1) {
            var tform = document.forms["fr_cancel_button"];
            tform.action = contextPath + "/" + pageName;
            tform.submit();
        }
    }

    function confirmExitAction(pageName, contextPath) {
        var confirm1 = confirm('<fmt:message key="sure_to_exit" bundle="${resword}"/>');
        if (confirm1) {
            var tform = document.forms["fr_cancel_button"];
            tform.action = contextPath + "/" + pageName;
            tform.submit();
        }
    }

    function processLogoutClick(returnTo) {
        setCurrentUser("");
        sessionStorage && sessionStorage.clear();
    }
</script>

<!-- Help Scout -->
<script type="text/javascript">!function(e,t,n){function a(){var e=t.getElementsByTagName("script")[0],n=t.createElement("script");n.type="text/javascript",n.async=!0,n.src="https://beacon-v2.helpscout.net",e.parentNode.insertBefore(n,e)}if(e.Beacon=n=function(t,n,a){e.Beacon.readyQueue.push({method:t,options:n,data:a})},n.readyQueue=[],"complete"===t.readyState)return a();e.attachEvent?e.attachEvent("onload",a):e.addEventListener("load",a,!1)}(window,document,window.Beacon||function(){});</script>
<script type="text/javascript">window.Beacon('init', 'fd568fcb-41a8-40bd-bcd7-0ba9518cfc49')</script>
<!-- Main Navigation -->
<div class="oc_nav">
    <div class="nav-top-bar">
        <!-- Logo -->

        <div class="logo">
            <c:set var="isLogo"/>
            <c:set var="isHref"/>

            <c:if test="${param.isSpringController}">
                <c:set var="isHref" value="../MainMenu"/>
                <c:set var="isLogo" value="../images/logo-color-on-dark.svg"/>
            </c:if>

            <c:if test="${param.isSpringControllerCCV}">
                <c:set var="isHref" value="../../MainMenu"/>
                <c:set var="isLogo" value="../../images/logo-color-on-dark.svg"/>
            </c:if>

            <c:if test="${!param.isSpringController}">
                <c:set var="isHref" value="MainMenu"/>
                <c:set var="isLogo" value="images/logo-color-on-dark.svg"/>
            </c:if>

            <c:if test="${empty urlPrefix}">
                <c:set var="isHref" value="${pageContext.request.contextPath}/MainMenu"/>
                <c:set var="isLogo" value="${pageContext.request.contextPath}/images/logo-color-on-dark.svg"/>
            </c:if>

            <a href="${isHref}"><img src="${isLogo}" alt="OpenClinica Logo"/></a>
        </div>

        <div id="StudyInfo">
            <c:if test="${empty urlPrefix}">
                <c:set var="urlPrefix" value="${pageContext.request.contextPath}/"/>
            </c:if>
            <c:choose>
                <c:when test='${study.study != null && study.study.studyId > 0}'>
                    <b><a href="${urlPrefix}ViewStudy?id=${study.study.studyId}&viewFull=yes"
                      title="<c:out value='${study.study.name}'/>"
                      alt="<c:out value='${study.study.name}'/>" ><c:out value="${study.abbreviatedParentStudyName}" /></a>
                      :&nbsp;<a href="${urlPrefix}ViewSite?id=${study.studyId}" title="<c:out value='${study.name}'/>" alt="<c:out value='${study.name}'/>"><c:out value="${study.abbreviatedName}" /></a></b>
                </c:when>
                <c:otherwise>
                    <b><a href="${urlPrefix}ViewStudy?id=${study.studyId}&viewFull=yes" title="<c:out value='${study.name}'/>" alt="<c:out value='${study.name}'/>"><c:out value="${study.abbreviatedName}" /></a></b>
                </c:otherwise>
            </c:choose>
            (<c:out value="${study.abbreviatedIdentifier}" />)&nbsp;&nbsp;
            <c:if test="${study.envType == 'PROD'}">
                <c:if test="${study.status.pending}">
                    <span class="status-tag status-${fn:toLowerCase(study.envType)}"><fmt:message key="design" bundle="${resword}"/></span>
                </c:if>
                <c:if test="${study.status.locked}">
                    <span class="status-tag status-${fn:toLowerCase(study.envType)}"><fmt:message key="locked" bundle="${resword}"/></span>
                </c:if>
                <c:if test="${study.status.frozen}">
                    <span class="status-tag status-${fn:toLowerCase(study.envType)}"><fmt:message key="frozen" bundle="${resword}"/></span>
                </c:if>
            </c:if>
            <c:if test="${study.envType == 'TEST'}">
                <c:if test="${study.status.pending}">
                    <span class="status-tag status-${fn:toLowerCase(study.envType)}"><fmt:message key="test_environment" bundle="${resword}"/> | <fmt:message key="design" bundle="${resword}"/></span>
                </c:if>
                <c:if test="${study.status.locked}">
                    <span class="status-tag status-${fn:toLowerCase(study.envType)}"><fmt:message key="test_environment" bundle="${resword}"/> | <fmt:message key="locked" bundle="${resword}"/></span>
                </c:if>
                <c:if test="${study.status.frozen}">
                    <span class="status-tag status-${fn:toLowerCase(study.envType)}"><fmt:message key="test_environment" bundle="${resword}"/> | <fmt:message key="frozen" bundle="${resword}"/></span>
                </c:if>
                <c:if test="${study.status.available}">
                    <span class="status-tag status-${fn:toLowerCase(study.envType)}"><fmt:message key="test_environment" bundle="${resword}"/></span>
                </c:if>
            </c:if>&nbsp;&nbsp;|&nbsp;&nbsp;
            <a href="${urlPrefix}ChangeStudy"><fmt:message key="change" bundle="${resword}"/></a>
            <c:if test="${sessionScope.baseUserRole == 'Data Manager'}">
                &nbsp;&nbsp;|&nbsp;&nbsp;
                <a href="${study.manager.replace('.build.','.design.').replace('/#/account-study', '')}${study.derivedBoardUrl}"><fmt:message key="design" bundle="${resword}"/></a>
            </c:if>
            <c:if test="${sessionScope.baseUserRole == 'Data Manager' || userBean.sysAdmin || userBean.techAdmin}">
                &nbsp;&nbsp;|&nbsp;&nbsp;
                <a href="${study.manager}/${study.uuid}/${study.envUuid}"><fmt:message key="share" bundle="${resword}"/></a>
            </c:if>
            <c:if test="${sessionScope.baseUserRole == 'Data Manager' || userBean.sysAdmin || userBean.techAdmin}">
                &nbsp;&nbsp;|&nbsp;&nbsp;
                <a href="${study.manager.replace('account-study','study-settings')}/${study.uuid}/settings"><fmt:message key="settings" bundle="${resword}"/></a>
            </c:if>
        </div>

        <div id="UserInfo">
            <div id="userDropdown">
                <ul>
                    <li><a href="#"><b><c:out value="${userBean.name}"/></b> (<c:out value="${sessionScope.customUserRole}"/>)<span
                            class="icon icon-caret-down white"></span></a></a>
                        <!-- First Tier Drop Down -->
                        <ul class="dropdown_BG">
                            <c:if test="${userBean.sysAdmin || userBean.techAdmin || userRole.coordinator}">
                                <li><a href="${study.manager}"><fmt:message key="return_to_my_studies" bundle="${resworkflow}"/></a></li>
                            </c:if>
                            <li>
                                <a href="${(study.manager).replace('account-study','my-profile')}?returnTo=${currentPageUrl}"><fmt:message key="return_to_my_profile" bundle="${resworkflow}"/></a></li>
                            <c:if test="${userBean.sysAdmin || userBean.techAdmin}">
                                <li>
                                    <a href="${(study.manager).replace('account-study','admin')}"><fmt:message key="return_to_admin" bundle="${resworkflow}"/></a>
                                </li>
                            </c:if>
                            <li>
                                <a href="javascript:openDocWindow('<c:out value="${sessionScope.supportURL}" />')"><fmt:message key="openclinica_feedback" bundle="${resword}"/></a>
                            </li>
                            <li>
                                <a href="//openclinica.com/openclinica-privacy-policy/" target="_blank"/><fmt:message key="privacy_policy" bundle="${resword}"/></a>
                            </li>
                            <li>
                                <a onClick="javascript:processLogoutClick('<%=currentURL%>');" href="${urlPrefix}pages/logout"><fmt:message key="sign_out" bundle="${resworkflow}"/></a>
                            </li>

                        </ul>
                    </li>
                </ul>
            </div>
        </div>
    </div>

    <div class="box_T">
        <div class="box_L">
            <div class="box_R">
                <div class="box_B">
                    <div class="box_TL">
                        <div class="box_TR">
                            <div class="box_BL">
                                <div class="box_BR">

                                    <div class="navbox_center">
                                        <!-- Top Navigation Row -->
                                        <table border="0" cellpadding="0" cellspacing="0" width="100%">
                                            <tr>
                                                <td>
                                                    <div id="bt_Home" class="nav_bt">
                                                        <div>
                                                            <div>
                                                                <div>
                                                                    <table border="0" cellpadding="0" cellspacing="0" width="100%">
                                                                        <tr>
                                                                            <td>
                                                                                <form METHOD="GET" action="${urlPrefix}ListStudySubjects" style="margin-block-end:0;">
                                                                                    <input type="text" name="findSubjects_f_studySubject.label"
                                                                                           placeholder='<fmt:message key="enter_participant_ID" bundle="${resword}"/>'
                                                                                           class="navSearch"/>
                                                                                    <input type="hidden" name="navBar" value="yes"/>
                                                                                    <input type="submit" value='<fmt:message key="view" bundle="${resword}"/>' class="navSearchButton"/>
                                                                                    <c:choose>
                                                                                        <c:when test="${
                                                                                          (sessionScope.baseUserRole=='Clinical Research Coordinator' || sessionScope.baseUserRole=='Investigator') &&
                                                                                          advsearchStatus=='enabled'
                                                                                        }">
                                                                                            <a href="ParticipantSearch" id="advsearch-link">
                                                                                                <fmt:message key="advanced_search" bundle="${resword}"/>
                                                                                            </a>
                                                                                        </c:when>
                                                                                    </c:choose>
                                                                                </form>
                                                                            </td>
                                                                            <td align="right" style="font-weight: normal;" class="oc-menu-bar">
                                                                                <ul>
                                                                                    <c:if test="${userRole.coordinator || userRole.director}">
                                                                                        <li><a href="${urlPrefix}MainMenu"><fmt:message
                                                                                                key="nav_home" bundle="${resword}"/></a></li>
                                                                                        <li><a href="${urlPrefix}ListStudySubjects"><fmt:message
                                                                                                key="nav_subject_matrix" bundle="${resword}"/></a></li>
                                                                                        <li><a href="${urlPrefix}ViewNotes?module=submit&listNotes_f_discrepancyNoteBean.disType=Query"><fmt:message
                                                                                                key="queries" bundle="${resword}"/></a></li>
                                                                                        <li><a href="${urlPrefix}StudyAuditLog"><fmt:message
                                                                                                key="nav_study_audit_log" bundle="${resword}"/></a></li>
                                                                                    </c:if>
                                                                                    <c:if test="${userRole.researchAssistant ||userRole.researchAssistant2}">
                                                                                        <li><a href="${urlPrefix}MainMenu"><fmt:message key="nav_home"
                                                                                                                                        bundle="${resword}"/></a></li>
                                                                                        <li><a href="${urlPrefix}ListStudySubjects"><fmt:message
                                                                                                key="nav_subject_matrix" bundle="${resword}"/></a></li>
                                                                                        <c:if test="${study.status.available && !enrollmentCapped}">
                                                                                            <li><a href="${urlPrefix}ListStudySubjects?addNewSubject=true" id="navAddSubject""><fmt:message
                                                                                                    key="nav_add_subject" bundle="${resword}"/></a></li>
                                                                                        </c:if>
                                                                                        <li><a href="${urlPrefix}ViewNotes?module=submit&listNotes_f_discrepancyNoteBean.disType=Query"><fmt:message
                                                                                                key="queries" bundle="${resword}"/></a></li>
                                                                                    </c:if>
                                                                                    <c:if test="${userRole.investigator}">
                                                                                        <li><a href="${urlPrefix}MainMenu"><fmt:message key="nav_home"
                                                                                                                                        bundle="${resword}"/></a></li>
                                                                                        <li><a href="${urlPrefix}ListStudySubjects"><fmt:message
                                                                                                key="nav_subject_matrix" bundle="${resword}"/></a></li>
                                                                                        <c:if test="${study.status.available && !enrollmentCapped}">
                                                                                            <li><a href="${urlPrefix}ListStudySubjects?addNewSubject=true" id="navAddSubject""><fmt:message
                                                                                                    key="nav_add_subject" bundle="${resword}"/></a></li>
                                                                                        </c:if>
                                                                                        <li><a href="${urlPrefix}ViewNotes?module=submit&listNotes_f_discrepancyNoteBean.disType=Query"><fmt:message
                                                                                                key="queries" bundle="${resword}"/></a></li>
                                                                                    </c:if>
                                                                                    <c:if test="${userRole.monitor}">
                                                                                        <c:if test="${empty urlPrefix}">
                                                                                            <c:set var="urlPrefix" value="${pageContext.request.contextPath}/"/>
                                                                                        </c:if>
                                                                                        <li><a href="${urlPrefix}MainMenu"><fmt:message key="nav_home" bundle="${resword}"/></a></li>
                                                                                        <li><a href="${urlPrefix}ListStudySubjects"><fmt:message
                                                                                                key="nav_subject_matrix" bundle="${resword}"/></a></li>
                                                                                        <li>
                                                                                            <a href="${urlPrefix}pages/viewAllSubjectSDVtmp?sdv_restore=${restore}&studyId=${study.studyId}&sdv_f_sdvStatus=Ready+to+verify+%2B+Changed+since+verified&sdv_s_4_eventDate=asc&sdv_s_4_eventDate=asc"><fmt:message
                                                                                                    key="nav_sdv" bundle="${resword}"/></a></li>
                                                                                        <li><a href="${urlPrefix}ViewNotes?module=submit&listNotes_f_discrepancyNoteBean.disType=Query"><fmt:message
                                                                                                key="queries" bundle="${resword}"/></a></li>
                                                                                    </c:if>
                                                                                    <li class="nav_TaskB" id="nav_Tasks" style="position: relative; z-index: 1;">
                                                                                        <a href="#" onmouseover="setNav('nav_Tasks');"
                                                                                           id="nav_Tasks_link"><fmt:message key="nav_tasks"
                                                                                                                            bundle="${resword}"/>
                                                                                            <span class="icon icon-caret-down white"></span></a>
                                                                                    </li>
                                                                                </ul>
                                                                            </td>
                                                                        </tr>
                                                                    </table>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </td>
                                            </tr>
                                        </table>
                                    </div>
                                    <!-- End shaded box border DIVs -->
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
</table>
<!-- NAVIGATION DROP-DOWN -->

<div id="nav_hide" style="position: absolute; left: 0px; top: 0px; visibility: hidden; z-index: -1; width: 100%; height: 400px;">

    <a href="#" onmouseover="hideSubnavs();">
        <c:choose>
            <c:when test="${requestFromSpringController == 'true' || requestFromSpringControllerCCV == 'true'}">
                <img src="../images/spacer.gif" alt="" width="1000" height="400" border="0"/>
            </c:when>
            <c:otherwise>
                <img src="images/spacer.gif" alt="" width="1000" height="400" border="0"/>
            </c:otherwise>
        </c:choose>
    </a>
</div>


</div>
<c:choose>
    <c:when test="${requestFromSpringController == 'true' || requestFromSpringControllerCCV == 'true'}">
        <img src="../images/spacer.gif" width="596" height="1"><br>
    </c:when>
    <c:otherwise>
        <img src="images/spacer.gif" width="596" height="1"><br>
    </c:otherwise>
</c:choose>
<!-- End Main Navigation -->
<div id="subnav_Tasks" class="dropdown">
    <div class="dropdown_BG">
        <c:if test="${userRole.monitor}">
            <div class="taskGroup"><fmt:message key="nav_monitor_and_manage_data" bundle="${resword}"/></div>
            <div class="taskLeftColumn">
                <div class="taskLink"><a href="${urlPrefix}ListStudySubjects"><fmt:message key="nav_subject_matrix" bundle="${resword}"/></a></div>
                <div class="taskLink"><a href="${urlPrefix}ViewStudyEvents"><fmt:message key="nav_view_events" bundle="${resword}"/></a></div>
                <div class="taskLink"><a href="${urlPrefix}pages/viewAllSubjectSDVtmp?sdv_restore=${restore}&studyId=${study.studyId}&sdv_f_sdvStatus=Ready+to+verify+%2B+Changed+since+verified&sdv_s_4_eventDate=asc&sdv_s_4_eventDate=asc"><fmt:message
                        key="nav_source_data_verification" bundle="${resword}"/></a></div>
            </div>
            <div class="taskRightColumn">
                <div class="taskLink"><a href="${urlPrefix}ViewNotes?module=submit&listNotes_f_discrepancyNoteBean.disType=Query"><fmt:message key="queries" bundle="${resword}"/></a></div>
                <div class="taskLink"><a href="${urlPrefix}StudyAuditLog"><fmt:message key="nav_study_audit_log" bundle="${resword}"/></a></div>
                <div class="taskLink"><a href="${urlPrefix}Jobs"><fmt:message key="nav_jobs" bundle="${resword}"/></a></div>
            </div>
            <br clear="all">
            <div class="taskGroup"><fmt:message key="nav_extract_data" bundle="${resword}"/></div>
            <div class="taskLeftColumn">
                <div class="taskLink"><a href="${urlPrefix}ViewDatasets"><fmt:message key="nav_view_datasets" bundle="${resword}"/></a></div>
                <c:if test="${userBean.sysAdmin || userBean.techAdmin}">
                    <div class="taskLink"><a href="${urlPrefix}ViewJob"><fmt:message key="nav_jobs" bundle="${resword}"/></a></div>
                </c:if>
            </div>
            <div class="taskRightColumn">
                <div class="taskLink"><a href="${urlPrefix}CreateDataset"><fmt:message key="nav_create_dataset" bundle="${resword}"/></a></div>
            </div>
            <br clear="all">
        </c:if>
        <c:if test="${userRole.researchAssistant ||userRole.researchAssistant2  }">
            <div class="taskGroup"><fmt:message key="nav_submit_data" bundle="${resword}"/></div>
            <div class="taskLeftColumn">
                <div class="taskLink"><a href="${urlPrefix}ListStudySubjects"><fmt:message key="nav_subject_matrix" bundle="${resword}"/></a></div>
                <c:if test="${study.status.available && !enrollmentCapped}">
                    <div class="taskLink"><a href="${urlPrefix}ListStudySubjects?addNewSubject=true" id="navAddSubjectSD"><fmt:message key="nav_add_subject" bundle="${resword}"/></a></div>
                </c:if>
                <div class="taskLink"><a href="${urlPrefix}ViewNotes?module=submit&listNotes_f_discrepancyNoteBean.disType=Query"><fmt:message key="queries" bundle="${resword}"/></a></div>
           </div>
            <div class="taskRightColumn">
                <c:if test="${!study.status.frozen && !study.status.locked}">
                    <div class="taskLink"><a href="${urlPrefix}CreateNewStudyEvent"><fmt:message key="nav_schedule_event" bundle="${resword}"/></a></div>
                </c:if>
                <div class="taskLink"><a href="${urlPrefix}ViewStudyEvents"><fmt:message key="nav_view_events" bundle="${resword}"/></a></div>
                <c:if test="${study.status.available}">
                    <div class="taskLink"><a href="${urlPrefix}ImportData"><fmt:message key="nav_import_data" bundle="${resword}"/></a></div>
                </c:if>
                <div class="taskLink"><a href="${urlPrefix}Jobs"><fmt:message key="nav_bulk_actions_log" bundle="${resword}"/></a></div>
            </div>
            <br clear="all">
        </c:if>
        <c:if test="${userRole.investigator}">
            <div class="taskGroup"><fmt:message key="nav_submit_data" bundle="${resword}"/></div>
            <div class="taskLeftColumn">
                <div class="taskLink"><a href="${urlPrefix}ListStudySubjects"><fmt:message key="nav_subject_matrix" bundle="${resword}"/></a></div>
                <c:if test="${study.status.available && !enrollmentCapped}">
                    <div class="taskLink"><a href="${urlPrefix}ListStudySubjects?addNewSubject=true" id="navAddSubjectSD"><fmt:message key="nav_add_subject" bundle="${resword}"/></a></div>
                </c:if>
                <div class="taskLink"><a href="${urlPrefix}ViewNotes?module=submit&listNotes_f_discrepancyNoteBean.disType=Query"><fmt:message key="queries" bundle="${resword}"/></a></div>
            </div>
            <div class="taskRightColumn">
                <c:if test="${!study.status.frozen && !study.status.locked}">
                    <div class="taskLink"><a href="${urlPrefix}CreateNewStudyEvent"><fmt:message key="nav_schedule_event" bundle="${resword}"/></a></div>
                </c:if>
                <div class="taskLink"><a href="${urlPrefix}ViewStudyEvents"><fmt:message key="nav_view_events" bundle="${resword}"/></a></div>
                <c:if test="${study.status.available}">
                    <div class="taskLink"><a href="${urlPrefix}ImportData"><fmt:message key="nav_import_data" bundle="${resword}"/></a></div>
                    <div class="taskLink"><a href="${urlPrefix}Jobs"><fmt:message key="nav_bulk_actions_log" bundle="${resword}"/></a></div>
                </c:if>
            </div>
            <br clear="all">
            <div class="taskGroup"><fmt:message key="nav_extract_data" bundle="${resword}"/></div>
            <div class="taskLeftColumn">
                <div class="taskLink"><a href="${urlPrefix}ViewDatasets"><fmt:message key="nav_view_datasets" bundle="${resword}"/></a></div>
                <c:if test="${userBean.sysAdmin || userBean.techAdmin}">
                    <div class="taskLink"><a href="${urlPrefix}ViewJob"><fmt:message key="nav_jobs" bundle="${resword}"/></a></div>
                </c:if>
            </div>
            <div class="taskRightColumn">
                <div class="taskLink"><a href="${urlPrefix}CreateDataset"><fmt:message key="nav_create_dataset" bundle="${resword}"/></a></div>
            </div>
            <br clear="all">
        </c:if>
        <c:if test="${userRole.coordinator || userRole.director}">
            <div class="taskGroup"><fmt:message key="nav_submit_data" bundle="${resword}"/></div>
            <div class="taskLeftColumn">
                <div class="taskLink"><a href="${urlPrefix}ListStudySubjects"><fmt:message key="nav_subject_matrix" bundle="${resword}"/></a></div>
                <c:if test="${study.status.available && !enrollmentCapped}">
                    <div class="taskLink"><a href="${urlPrefix}ListStudySubjects?addNewSubject=true" id="navAddSubjectSD"><fmt:message key="nav_add_subject" bundle="${resword}"/></a></div>
                </c:if>
                <div class="taskLink"><a href="${urlPrefix}ViewNotes?module=submit&listNotes_f_discrepancyNoteBean.disType=Query"><fmt:message key="queries" bundle="${resword}"/></a></div>
            </div>
            <div class="taskRightColumn">
                <c:if test="${!study.status.frozen && !study.status.locked}">
                    <div class="taskLink"><a href="${urlPrefix}CreateNewStudyEvent"><fmt:message key="nav_schedule_event" bundle="${resword}"/></a></div>
                </c:if>
                <div class="taskLink"><a href="${urlPrefix}ViewStudyEvents"><fmt:message key="nav_view_events" bundle="${resword}"/></a></div>
                <c:if test="${study.status.available}">
                    <div class="taskLink"><a href="${urlPrefix}ImportData"><fmt:message key="nav_import_data" bundle="${resword}"/></a></div>
                </c:if>
                <div class="taskLink"><a href="${urlPrefix}Jobs"><fmt:message key="nav_bulk_actions_log" bundle="${resword}"/></a></div>
            </div>
            <br clear="all">
            <div class="taskGroup"><fmt:message key="nav_monitor_and_manage_data" bundle="${resword}"/></div>
            <div class="taskLeftColumn">
                <div class="taskLink"><a href="${urlPrefix}StudyAuditLog"><fmt:message key="nav_study_audit_log" bundle="${resword}"/></a></div>
                <div class="taskLink"><a href="${urlPrefix}ViewRuleAssignment?read=true"><fmt:message key="nav_rules" bundle="${resword}"/></a>
                </div>
                <c:choose>
                    <c:when test="${study.study != null && study.study.studyId > 0 && (userRole.coordinator || userRole.director) }">
                    </c:when>
                    <c:otherwise>
                        <div class="taskLink"><a href="${urlPrefix}ViewStudy?id=${study.studyId}&viewFull=yes"><fmt:message key="nav_view_study"
                                                                                                                       bundle="${resword}"/></a></div>
                        <div class="taskLink"></div>
                    </c:otherwise>
                </c:choose>
            </div>
            <div class="taskRightColumn">
                <c:choose>
                    <c:when test="${study.study != null && study.study.studyId > 0 && (userRole.coordinator || userRole.director) }">
                    </c:when>
                    <c:otherwise>
                        <div class="taskLink"><a href="${urlPrefix}ListSite?read=true"><fmt:message key="nav_sites" bundle="${resword}"/></a></div>
                        <div class="taskLink"><a href="${urlPrefix}ListCRF?module=manage"><fmt:message key="nav_crfs" bundle="${resword}"/></a></div>
                        <div class="taskLink"><a href="${urlPrefix}pages/viewAllSubjectSDVtmp?sdv_restore=${restore}&studyId=${study.studyId}&sdv_f_sdvStatus=Ready+to+verify+%2B+Changed+since+verified&sdv_s_4_eventDate=asc"><fmt:message
                                key="nav_source_data_verification" bundle="${resword}"/></a><br/></div>
                    </c:otherwise>
                </c:choose>
            </div>
            <br clear="all">
            <div class="taskGroup"><fmt:message key="nav_extract_data" bundle="${resword}"/></div>
            <div class="taskLeftColumn">
                <div class="taskLink"><a href="${urlPrefix}ViewDatasets"><fmt:message key="nav_view_datasets" bundle="${resword}"/></a></div>
                <c:if test="${userBean.sysAdmin || userBean.techAdmin}">
                    <div class="taskLink"><a href="${urlPrefix}ViewJob"><fmt:message key="nav_jobs" bundle="${resword}"/></a></div>
                </c:if>
            </div>
            <div class="taskRightColumn">
                <div class="taskLink"><a href="${urlPrefix}CreateDataset"><fmt:message key="nav_create_dataset" bundle="${resword}"/></a></div>
            </div>
            <br clear="all">
        </c:if>
        <c:if test="${enableEmbeddedReports}">
            <div class="taskGroup"><fmt:message key="nav_reports" bundle="${resword}"/></div>
            <div class="taskLeftColumn">
                <div class="taskLink"><a href="${urlPrefix}reports?/collection/root"><fmt:message key="nav_reports_view" bundle="${resword}"/></a></div>
            </div>
            <div class="taskRightColumn">
                <div class="taskLink"><a href="${urlPrefix}reports?/question/new"><fmt:message key="nav_reports_create" bundle="${resword}"/></a></div>
            </div>
            <br clear="all">
        </c:if>
    </div>
</div>

<script type="text/javascript">

    dropdown = document.getElementById("subnav_Tasks");

    //close dropdown using esc
    jQuery(document).keyup(function(e) {
        if (e.keyCode == 27) { // escape key maps to keycode `27`
            dropdown.style.display="none";
            jQuery.unblockUI();
        }
    });

    //we have it open on mouse-over OR click when it is closed
    jQuery(document).ready(function(){
        var participantIDVerification = '<c:out value="${participantIDVerification}"/>';
        if (participantIDVerification == 'true') {
            // disable right click
            jQuery("input[name='findSubjects_f_studySubject.label']").on('contextmenu',function(){
                return false;
            });
            // disable cut copy paste
            jQuery("input[name='findSubjects_f_studySubject.label']").bind('cut copy paste', function (e) {
                e.preventDefault();
            });
        }
        // Show hide popover
        jQuery(".nav_TaskB").click(function(){
            jQuery(this).find(".dropdown").slideToggle("fast");
        });
    });
    jQuery(document).on("click", function(event){
        var $trigger = jQuery(".nav_TaskB");
        if($trigger !== event.target && !$trigger.has(event.target).length){
            jQuery(".dropdown").slideUp("fast");
        }
    });
    function showDropdown() {
        if (dropdown.style.display === 'none') {
            dropdown.style.display = 'block';
        } else {
            droopdown.style.display = 'none';
        }
    }
    
    var currentUrl = window.location.href;
    var urlParam = currentUrl.split('?')[1] || '';
    var params = urlParam.split('&');
    for (var i=0; i<params.length; i++) {
        var parts = params[i].split('=');
        var key = parts[0];
        var value = parts[1];
        if (key === "originatingPage") {
            window.originatingPage = decodeURIComponent(value);
            break;
        }
    }
</script>
