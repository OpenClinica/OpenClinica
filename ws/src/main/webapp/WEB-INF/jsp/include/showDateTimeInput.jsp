<%@ page contentType="text/html; charset=UTF-8" %>

<%@ page import="org.akaza.openclinica.i18n.util.*" %>
<%@ page import="java.util.ResourceBundle" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/> 
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/> 


<jsp:useBean scope="request" id="presetValues" class="java.util.HashMap" />

<%
    //ResourceBundleProvider rbProvider = new ResourceBundleProvider();
    ResourceBundle resformat = ResourceBundle.getBundle("org.akaza.openclinica.i18n.format", ResourceBundleProvider.getLocale());
%>
<c:set var="prefix" value="${param.prefix}" />
<c:set var="count" value="${param.count}" />
<c:set var="date" value="" />
<c:set var="hour" value="12" />
<c:set var="minute" value="0" />
<c:set var="half" value="" />

<c:set var="dateFieldName" value='${prefix}Date' />
<c:set var="hourFieldName" value='${prefix}Hour' />
<c:set var="minuteFieldName" value='${prefix}Minute' />
<c:set var="halfFieldName" value='${prefix}Half' />

<c:forEach var="presetValue" items="${presetValues}">
    <c:if test='${presetValue.key == dateFieldName}'>
        <c:set var="date" value="${presetValue.value}" />
    </c:if>
    <c:if test='${presetValue.key == hourFieldName}'>
        <c:set var="hour" value="${presetValue.value}" />
        <%--<c:if test="${hour==0}">
            <c:set var="hour" value="12" />
        </c:if>--%>
    </c:if>
    <c:if test='${presetValue.key == minuteFieldName}'>
        <c:set var="minute" value="${presetValue.value}" />
    </c:if>
    <c:if test='${presetValue.key == halfFieldName}'>
        <c:set var="half" value="${presetValue.value}" />
    </c:if>
</c:forEach>

<td valign="top">
<div class="formfieldS_BG">
    <input type="text" name="<c:out value="${dateFieldName}"/>" value="<c:out value="${date}" />" class="formfieldS" id="${prefix}dateField"/>
</div>
</td>
<td valign="top">
    <a href="#" >
        <img src="images/bt_Calendar.gif" alt="<fmt:message key="show_calendar" bundle="${resword}"/>" title="<fmt:message key="show_calendar" bundle="${resword}"/>" border="0" id="${prefix}dateTrigger" />
        <script type="text/javascript">
        Calendar.setup({inputField  : "${prefix}dateField", ifFormat    : "<fmt:message key="date_format_calender" bundle="${resformat}"/>", button      : "${prefix}dateTrigger" });
        </script>
        
    </a>
</td>
<td valign="top">
<div class="formfieldXS_BG">
<select name="<c:out value="${hourFieldName}"/>" class="formfieldXS">
    <option value="<c:out value="-1"/>" ><c:out value=""/></option>

<% if (resformat.getString("date_time_format_string").contains("HH") || resformat.getString("date_time_format_string").contains("kk")) { %>
    <c:forEach var="currHour" begin="0" end="23" step="1">
        <c:choose>
            <c:when test="${hour == currHour}">
                <option value="<c:out value="${currHour}"/>" selected><c:out value="${currHour}"/></option>
            </c:when>
            <c:otherwise>
                <option value="<c:out value="${currHour}"/>"><c:out value="${currHour}"/></option>
            </c:otherwise>
        </c:choose>
    </c:forEach>
<% } else { %>
    <c:forEach var="currHour" begin="1" end="12" step="1">
        <c:choose>
            <c:when test="${hour == currHour}">
                <option value="<c:out value="${currHour}"/>" selected><c:out value="${currHour}"/></option>
            </c:when>
            <c:otherwise>
                <option value="<c:out value="${currHour}"/>"><c:out value="${currHour}"/></option>
            </c:otherwise>
        </c:choose>
    </c:forEach>
    <% } %>
</select>
</div>
</td>

<td class="formlabel">:</td>

<td valign="top">
<div class="formfieldXS_BG">
<select name="<c:out value="${minuteFieldName}"/>" class="formfieldXS">
    <option value="<c:out value="-1"/>" ><c:out value=""/></option>
    <c:forEach var="currMinute" begin="0" end="59" step="1">
        <c:choose>
            <c:when test="${minute == currMinute}">
                <option value="<c:out value="${currMinute}"/>" selected><c:out value="${currMinute}"/></option>
            </c:when>
            <c:otherwise>
                <option value="<c:out value="${currMinute}"/>"><c:out value="${currMinute}"/></option>
            </c:otherwise>
        </c:choose>
    </c:forEach>
</select>
</div>
</td>

<td valign="top">

<% if (!(resformat.getString("date_time_format_string").contains("HH") || resformat.getString("date_time_format_string").contains("kk"))) { %>
<%-- if its not 24h, show the am/pm tbh, 06/2008 --%>
<div class="formfieldXS_BG">
<select name="<c:out value="${halfFieldName}"/>" class="formfieldXS">
    <c:choose>
        <c:when test='${half == "pm"}'>
            <option value=""></option>
            <option value="am">am</option>
            <option value="pm" selected>pm</option>
        </c:when>
        <c:when test='${half == "am"}'>
            <option value=""></option>
            <option value="am" selected>am</option>
            <option value="pm">pm</option>      
        </c:when>
        <c:otherwise>
            <option value=""></option>
            <option value="am">am</option>
            <option value="pm">pm</option>  
        </c:otherwise>
    </c:choose>
</select>
</div>
<% } else { %>
<input type="hidden" name="<c:out value="${halfFieldName}"/>" value=""/>
<% } %>
</td>