<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<jsp:useBean scope="request" id="label" class="java.lang.String"/>

<jsp:useBean scope="session" id="study" class="core.org.akaza.openclinica.bean.managestudy.StudyBean" />
<jsp:useBean scope="request" id="pageMessages" class="java.util.ArrayList" />
<jsp:useBean scope="request" id="presetValues" class="java.util.HashMap" />

<jsp:useBean scope="request" id="groups" class="java.util.ArrayList" />


<c:set var="label" value="" />
<c:set var="rand"><%= java.lang.Math.round(java.lang.Math.random() * 101) %></c:set>

<c:forEach var="presetValue" items="${presetValues}">

    <c:if test='${presetValue.key == "label"}'>
        <c:set var="label" value="${presetValue.value}" />
    </c:if>
</c:forEach>

<div id="verification-form" style="display:none">

<script>
    jQuery(function() {
        jQuery(".pidVerification").click(function(e) {
            e.preventDefault();
            jQuery('body > table.background').hide();
            jQuery.blockUI({ message: jQuery('#verification-form'), css:{left: "300px", top:"25px" } });
        });
    });
</script>


<form name="subjectForm" action="AddNewSubject" method="post">
<input type="hidden" name="subjectOverlay" value="true">

<table border="0" cellpadding="0" align="center" style="cursor:default;">
    <tr style="height:10px;">
        <td class="formlabel" align="left"><h3 class="addNewSubjectTitle"><fmt:message key="verify_id" bundle="${resword}"/></h3></td>
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
                        <jsp:include page="../include/showSubmitted.jsp" />
                        <input class="form-control" type="hidden" name="addWithEvent" value="1"/><span class="addNewStudyLayout">
                        <fmt:message key="study_subject_ID" bundle="${resword}"/></span>&nbsp;<small class="required">*</small></td>
                    <td valign="top">
                        <table border="0" cellpadding="0" cellspacing="0" class="full-width">
                            <tr>
                                <td valign="top"><div class="formfieldXL_BG">
                                <c:choose>
                                 <c:when test="${study.studyParameterConfig.subjectIdGeneration =='auto non-editable'}">
                                  <input onfocus="this.select()" type="text" value="<c:out value="${label}"/>" size="45" class="formfield form-control" disabled>
                                  <input class="form-control" type="hidden" name="label" value="<c:out value="${label}"/>">
                                 </c:when>
                                 <c:otherwise>
                                   <input onfocus="this.select()" type="text" name="label" value="<c:out value="${label}"/>" width="30" class="formfieldXL form-control">
                                 </c:otherwise>
                                </c:choose>
                                </div></td>
                            </tr>
                            <tr>
                                <td><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="label"/></jsp:include></td>
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
            <input type="button" id="cancel" name="cancel" value="Cancel"/>
            &nbsp;
            <input type="submit" name="addSubject" value="Add"/>

            <div id="dvForCalander_${rand}" style="width:1px; height:1px;"></div>
        </td>
    </tr>

</table>

</form>

</div>
