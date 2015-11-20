<%@ page contentType="text/html; charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
 
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.licensing" var="licensing"/>
<jsp:useBean scope='request' id='coreResources' class='org.akaza.openclinica.dao.core.CoreResources' />
<c:set var="basePath" value="${coreResources.getField('sysURL.basePath')}" />

<!-- END MAIN CONTENT AREA -->
</td>
            </tr>
        </table></td></tr></table>
        
<script type="text/javascript" src="${basePath}includes/wz_tooltip/wz_tooltip.js"></script>
<table border="0" cellpadding="0" width="100%"  >
            <tr>
                <td class="footer_bottom" style="width:200px">
                <a href="http://www.openclinica.com" target="new"><fmt:message key="openclinica_portal" bundle="${resword}"/></a>
                &nbsp;&nbsp;&nbsp;
                <a href="javascript:openDocWindow('https://docs.openclinica.com/3.1')"><fmt:message key="help" bundle="${resword}"/></a>
                &nbsp;&nbsp;&nbsp;
           <%-->     <a href="${basePath}Contact"><fmt:message key="contact" bundle="${resword}"/></a>--%>
             <a href="${basePath}Contact"><fmt:message key="contact" bundle="${resword}"/></a>
          
                </td>
                <td class="footer_bottom" >
				<fmt:message key="footer.license.1" bundle="${licensing}"/> 
               <fmt:message key="footer.license.2" bundle="${licensing}"/>
			   <fmt:message key="footer.license.3" bundle="${licensing}"/></td>
				
                <td  class="footer_bottom" style="width:200px;">
				
                    <!-- <a href="javascript:void(0)" onmouseover="Tip('<fmt:message key="footer.tooltip" bundle="${licensing}"/>')" onmouseout="UnTip()">
                    -->
					<div id="footer_tooltip">
                    <span onmouseover="Tip('<fmt:message key="footer.tooltip" bundle="${licensing}"/>')" onmouseout="UnTip()" style="color: #789EC5;"  >
                      <fmt:message key="footer.edition.2" bundle="${licensing}" /></span>
					  </div>
					 <div  id="version"></div><fmt:message key="Version_release" bundle="${licensing}"/> </div>
                </td>
            </tr>
        </table>

<!-- End Footer -->
<link rel="shortcut icon" type="image/x-icon" href="${basePath}images/favicon.ico">

        

</body>

</html>
