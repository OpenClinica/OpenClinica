<%--
    User: Hamid
  Date: May 8, 2009
  Time: 8:29:31 PM
  To change this template use File | Settings | File Templates.
--%>


<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<jsp:useBean scope="request" id="label" class="java.lang.String"/>

<jsp:useBean scope="session" id="study" class="org.akaza.openclinica.bean.managestudy.StudyBean" />
<jsp:useBean scope="request" id="pageMessages" class="java.util.ArrayList" />
<jsp:useBean scope="request" id="presetValues" class="java.util.HashMap" />

<jsp:useBean scope="request" id="groups" class="java.util.ArrayList" />

<script type="text/javascript">
    $(document).ready(function() {
        // disable right click
        $("#retype_pid").on("contextmenu",function(){
            return false;
        });
        // disable cut copy paste
        $("#retype_pid").bind("cut copy paste", function (e) {
            e.preventDefault();
        });
    });
</script>

<form name="pidVerificationForm">

<table border="0" cellpadding="0" align="center" style="cursor:default;">
    <tr style="height:10px;">
        <td class="formlabel" align="left"><h3 class="addNewSubjectTitle"><fmt:message key="pid_verification" bundle="${resword}"/></h3></td>
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
                        <span class="addNewStudyLayout">
                            <fmt:message key="retype_pid" bundle="${resword}"/>
                        </span>
                        &nbsp;
                        <small class="required">*</small>
                    </td>
                    <td valign="top">
                        <table border="0" cellpadding="0" cellspacing="0" class="full-width">
                            <tr>
                                <td valign="top">
                                    <div class="formfieldXL_BG">
                                        <input type="text" name="label" id="retype_pid" width="30" class="formfieldXL form-control">
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <div id="pidv-err" style="display: none;" class="alert small">
                                        <fmt:message key="pidv_err" bundle="${resword}"/>
                                    </div>
                                    <div id="pidv-match" style="display: none; color: #0cb924;" class="alert small">
                                        <fmt:message key="pidv_match" bundle="${resword}"/>
                                    </div>
                                </td>
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
            <input type="button" value="Cancel" onclick="clearPIDVerificationForm()"/>
            &nbsp;
            <input type="button" value="Checking" onclick="validatePIDVerificationForm()"/>
        </td>
    </tr>

</table>

</form>
