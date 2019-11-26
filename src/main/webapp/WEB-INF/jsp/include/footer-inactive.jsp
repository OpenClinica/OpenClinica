<%@ page contentType="text/html; charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
 
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.licensing" var="licensing"/>


<!-- END MAIN CONTENT AREA -->
</td>
			</tr>
		</table>
		</td>
	</tr>
	<tr>
		<td valign="bottom">

<!-- Footer -->


<SCRIPT LANGUAGE="JavaScript">

document.write('<table border="0" cellpadding=0" cellspacing="0" width="' + document.body.clientWidth + '">');

</script>
			<tr>
				<td class="footer">
				<!--<a href="#">About OpenClinica</a>
				&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				<a href="#">Terms of Use</a>
				&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				<a href="#">Privacy Policy</a>
				&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				-->
				<a href="#"><fmt:message key="openclinica_portal" bundle="${resword}"/></a>
				&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				<a href="javascript:openDocWindow('https://docs.openclinica.com/')"><fmt:message key="help" bundle="${resword}"/></a>
				&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				<a href="javascript:reportBug()"><fmt:message key="openclinica_feedback" bundle="${resword}"/></a>
				&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;				
				<a href="#"><fmt:message key="contact" bundle="${resword}"/></a>
				&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				<a href="#"><fmt:message key="openclinica_enterprise" bundle="${resword}"/></a>
				</td>
				<td class="footer" align="right"><fmt:message key="Version_release" bundle="${licensing}"/> &nbsp;&nbsp;</td>
				<td width="80" align="right" valign="bottom"><a href="http://www.akazaresearch.com"><img src="images/Akazalogo.gif" border="0" alt="<fmt:message key="developed_by_akaza" bundle="${resword}"/>" title="<fmt:message key="developed_by_akaza" bundle="${resword}"/>"></a></td>
			</tr>
		</table>

<!-- End Footer -->

		</td>
	</tr>
</table>
		

</body>

</html>
