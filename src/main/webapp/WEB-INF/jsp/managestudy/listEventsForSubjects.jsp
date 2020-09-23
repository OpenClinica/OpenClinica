<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>

<jsp:include page="../include/submit-header.jsp"/>

<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>

    <script type="text/javascript" language="JavaScript" src="includes/permissionTagAccess.js"></script>

<script type="text/javascript">
    function onInvokeAction(id,action) {
        if(id.indexOf('listEventsForSubject') == -1)  {
        setExportToLimit(id, '');
        }
        createHiddenInputFieldsForLimitAndSubmit(id);
    }
    function onInvokeExportAction(id) {
        var parameterString = createParameterStringForLimit(id);
        location.href = '${pageContext.request.contextPath}/ListEventsForSubjects? + module=manage&defId=' + '${defId}&' + parameterString;
    }

    jQuery(document).ready(function() {
        jQuery('#addSubject').click(function() {
            jQuery.blockUI({ message: jQuery('#addSubjectForm'), css:{left: "300px", top:"10px" } });
        });

        jQuery('#cancel').click(function() {
            jQuery.unblockUI();
            return false;
        });
    });

</script>

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray" border="0" align="right" hspace="10"></span></a>

        <fmt:message key="instructions" bundle="${resword}"/>

        <div class="sidebar_tab_content">

        </div>

    </td>

</tr>
<tr id="sidebar_Instructions_closed" style="display: all">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray" border="0" align="right" hspace="10"></span></a>

        <fmt:message key="instructions" bundle="${resword}"/>

    </td>
</tr>
<tr id="sidebar_Instructions_open" style="display: none">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray" border="0" align="right" hspace="10"></span></a>

        <fmt:message key="info" bundle="${resword}"/>

        <div class="sidebar_tab_content">

        </div>

    </td>

</tr>
<tr id="sidebar_Instructions_closed" style="display: all">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray" border="0" align="right" hspace="10"></span></a>

        <fmt:message key="info" bundle="${resword}"/>

    </td>
</tr>
<c:import url="/WEB-INF/jsp/include/sideIconsSubject.jsp"/>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope='session' id='userBean' class='core.org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='crf' class='core.org.akaza.openclinica.bean.admin.CRFBean'/>


<h1><span class="title_manage">
<fmt:message key="view_subjects_in" bundle="${restext}"/> <c:out value="${study.name}"/>
</span></h1><br/>

<div id="findSubjectsDiv">
    <form  action="${pageContext.request.contextPath}/ListEventsForSubjects">
        <input type="hidden" name="module" value="submit">
        <input type="hidden" name="defId" value="${defId}">
        ${listEventsForSubjectsHtml}
    </form>
</div>
<div id="addSubjectForm" style="display:none;">
      <c:import url="../submit/addNewSubjectExpressNew.jsp">
      </c:import>
</div>


<br>
<input type="button" onclick="confirmExit('MainMenu');"  name="exit" value="<fmt:message key="exit" bundle="${resword}"/>   " class="button_medium"/>

<div>
    <c:forEach var="studySub" items="${participants}">
        <c:if test="${not empty eventsByParticipant.get(studySub.name)}">
            <div>${studySub.name}</div>
            <c:set var="studyRelatedTostudySub" value="${studyByParticipant.get(studySub.name)}"/>
            <div id="actions4${studySub.name}" style="margin-left:40px;">
                <c:forEach var="currRow" items="${eventsByParticipant.get(studySub.name)}">
                    <table>
                        <%@include file="eventActions.jsp"%>
                    </table>
                </c:forEach>
            </div>
        </c:if>
    </c:forEach>
    <script>
        jQuery('#listEventsForSubject').on('click', 'a', function() {
            var menu = jQuery(this).prev('div[id^=S_Event_]');
            if (!menu.length)
                return;

            var parts = menu.attr('id').split('_');
            var participantId = parts[2];
            var extraMenu = jQuery('#actions4' + participantId);
            if (!extraMenu.length)
                return;

            var index = Math.floor(menu.closest('table').index() / 2);
            extraMenu = extraMenu.children().eq(index);
            var actions = extraMenu.find('td');
            actions.each(function() {
                var td = jQuery(this);
                td.css('display', 'block');
                td.children('a').append('&nbsp;&nbsp;&nbsp;' + td.find('span').attr('title'));
            });

            var target = menu.find('table').find('table');
            var tbody = target.children('tbody');
            if (tbody.length)
                target = tbody;

            actions.appendTo(target);
        });
    </script>
</div>

<jsp:include page="../include/footer.jsp"/>