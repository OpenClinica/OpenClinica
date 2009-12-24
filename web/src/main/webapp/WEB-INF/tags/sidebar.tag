<%@tag body-content="scriptless" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="com.akazaresearch.viewtags" prefix="view" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<%-- request-scoped boolean values indicating whether to display the various internal DIVs,
and whether they should be open or closed --%>
<c:set var="openAlerts" value="${requestScope['alertsBoxSetup']}" />
<c:set var="openInstructions" value="${requestScope['infoBoxSetup']}" />
<c:set var="openInfo" value="${requestScope['instructionsBoxSetup']}" />
<%-- This one is usually optional --%>
<c:set var="showIcons" value="${requestScope['enableIconsBoxSetup']}" />

<div id="sidebarDiv">

    <c:choose>
        <c:when test="${openAlerts}">
            <div id="alertsDivTop"><fmt:message key="alerts_messages" bundle="${resword}"/><img hspace="5" width="9" height="13" border="0" alt="+" src="../images/sidebar_collapse.gif" onclick="changeOpenDivButton(this)"/></div><div id="alertsDiv" class="sideTopBar">
            <view:alertTag />
        </div>
        </c:when>
        <c:otherwise>
            <div id="alertsDivTop"><fmt:message key="alerts_messages" bundle="${resword}"/><img hspace="5" width="9" height="13" border="0" alt="+" src="../images/sidebar_expand.gif" onclick="changeOpenDivButton(this)"/></div><div id="alertsDiv" style="display:none" class="sideTopBar">
            <view:alertTag />
        </div>
        </c:otherwise>
    </c:choose>

    <c:choose>
        <c:when test="${openInstructions}">
            <div id="instructionsDivTop"><fmt:message key="instructions" bundle="${resword}"/><img hspace="5" width="9" height="13" border="0" alt="+" src="../images/sidebar_collapse.gif" onclick="changeOpenDivButton(this)"/></div><div id="instructionsDiv" class="sideTopBar">
            <view:instructions />
        </div>
        </c:when>
        <c:otherwise>
            <div id="instructionsDivTop"><fmt:message key="instructions" bundle="${resword}"/><img hspace="5" width="9" height="13" border="0" alt="+" src="../images/sidebar_expand.gif" onclick="changeOpenDivButton(this)"/></div><div id="instructionsDiv" style="display:none" class="sideTopBar">
            <view:instructions />
        </div>
        </c:otherwise>
    </c:choose>

    <c:choose>
        <c:when test="${openInfo}">
            <div id="infoDivTop"><fmt:message key="info" bundle="${resword}"/><img hspace="5" width="9" height="13" border="0" alt="+" src="../images/sidebar_collapse.gif" onclick="changeOpenDivButton(this)"/></div><div id="infoDiv" class="sideTopBar">
            <view:sideinfo />
        </div>
        </c:when>
        <c:otherwise>
            <div id="infoDivTop"><fmt:message key="info" bundle="${resword}"/><img hspace="5" width="9" height="13" border="0" alt="+" src="../images/sidebar_expand.gif" onclick="changeOpenDivButton(this)"/></div><div id="infoDiv" style="display:none" class="sideTopBar">
            <view:sideinfo />
        </div>
        </c:otherwise>
    </c:choose>

    <%-- Is the icons tab shown at all?--%>
    <c:if test="${showIcons}">
        <%-- If shown, the icons tab is open when the page is first displayed--%>
        <div id="iconKeyDivTop"><fmt:message key="icon_key" bundle="${resword}"/><img hspace="5" width="9" height="13" border="0" alt="+" src="../images/sidebar_collapse.gif" onclick="changeOpenDivButton(this)"/></div><div id="inconKeyDiv" class="sideTopBar">
        <view:sideicons />
        </div>
    </c:if>
</div>
