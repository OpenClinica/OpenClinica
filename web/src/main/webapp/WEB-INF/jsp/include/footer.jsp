<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
 
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
 
<!-- END MAIN CONTENT AREA -->
</td>
            </tr>
        </table>
        </td>
    </tr>
    <tr>
        <td valign="bottom">

<!-- Footer -->
<c:set var="urlPrefix" value=""/>
<c:set var="requestFromSpringController" value="${param.isSpringControllerFooter}" />
<c:if test="${requestFromSpringController == '1' }">
    <c:set var="urlPrefix" value="../"/>
</c:if>
<c:if test="${requestFromSpringController == '2' }">
    <c:set var="urlPrefix" value="../../"/>
</c:if>
<script type="text/javascript" src="includes/wz_tooltip/wz_tooltip.js"></script>
<SCRIPT LANGUAGE="JavaScript">

document.write('<table border="0" cellpadding=0" cellspacing="0" width="' + document.body.clientWidth + '">');

</script>
            <tr>
                <td class="footer">
                <a href="http://www.openclinica.org" target="new"><fmt:message key="openclinica_portal" bundle="${resword}"/></a>
                &nbsp;&nbsp;&nbsp;
                <a href="javascript:openDocWindow('https://docs.openclinica.com/3.1')"><fmt:message key="help" bundle="${resword}"/></a>
                &nbsp;&nbsp;&nbsp;
                <a href="${urlPrefix}Contact"><fmt:message key="contact" bundle="${resword}"/></a>
                &nbsp;&nbsp;&nbsp;
                </td>
                <td class="footer"><fmt:message key="footer.license.1" bundle="${resword}"/> </td>
                <td class="footer" align="right"><fmt:message key="Version_release" bundle="${resword}"/> &nbsp;&nbsp;</td>
                <td width="80" align="right" valign="bottom">
                </td>
            </tr>
            <tr>
                <td class="footer"/>
                <td class="footer"> <fmt:message key="footer.license.2" bundle="${resword}"/></td>
                <td align="right" class="footer">
                    <a href="javascript:void(0)" onmouseover="Tip('<fmt:message key="footer.tooltip" bundle="${resword}"/>')" onmouseout="UnTip()">
                    <center><fmt:message key="footer.edition.2" bundle="${resword}"/></center></a>
                </td>
            </tr>
            <tr>
                <td class="footer"/>
                <td class="footer"><fmt:message key="footer.license.3" bundle="${resword}"/></td>
                <td align="right" class="footer"></td>
            </tr>
        </table>

<!-- End Footer -->

        </td>
    </tr>
</table>
        

</body>

</html>
