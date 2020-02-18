<%@tag body-content="scriptless" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.licensing" var="licensing"/>
    <div id="footerLinksDiv">
    <a href="https://www.libreclinica.org" target="new"><fmt:message key="openclinica_portal" bundle="${resword}"/></a>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    <a href="https://www.libreclinica.org/documentation" target="new"><fmt:message key="help" bundle="${resword}"/></a>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    <a href="Contact"><fmt:message key="contact" bundle="${resword}"/></a>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    <a href="Enterprise"><fmt:message key="openclinica_enterprise" bundle="${resword}"/></a>
     <span class="version">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<fmt:message key="Version_release" bundle="${licensing}"/>&nbsp;</span>
     <div id="logoFooterDiv">
    <a href="http://www.akazaresearch.com"><img src="../images/Akazalogo.gif" width="60" height="67" border="0" alt="Developed by Akaza Research" title="Developed by Akaza Research"></a>
    </div>

    </div>