<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<c:choose>
<c:when test="${userBean.sysAdmin && module=='admin'}">
 <c:import url="../include/admin-header.jsp"/>
</c:when>
<c:otherwise>
 <c:import url="../include/managestudy-header.jsp"/>
</c:otherwise>
</c:choose>



<!-- move the alert message to the sidebar-->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
    <td class="sidebar_tab">

    <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray" border="0" align="right" hspace="10"></span></a>

    <fmt:message key="instructions" bundle="${resword}"/>

    <div class="sidebar_tab_content">

    </div>

    </td>

  </tr>
  <tr id="sidebar_Instructions_closed">
    <td class="sidebar_tab">

    <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray" border="0" align="right" hspace="10"></span></a>

    <fmt:message key="instructions" bundle="${resword}"/>

    </td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope='request' id='sections' class='java.util.ArrayList'/>
<jsp:useBean scope='request' id='crfname' class='java.lang.String'/>
<jsp:useBean scope='request' id='version' class='core.org.akaza.openclinica.bean.submit.FormLayoutBean'/>
<c:choose>
<c:when test="${userBean.sysAdmin && module=='admin'}">
<h1><span class="title_manage"><fmt:message key="view_CRF_version_details" bundle="${resword}"/>: <c:out value="${crfname}"/> <c:out value="${version.name}"/>  </span></h1>
</c:when>
<c:otherwise>
<h1><span class="title_manage"><fmt:message key="view_CRF_version_details" bundle="${resword}"/>: <c:out value="${crfname}"/> <c:out value="${version.name}"/> </span></h1>
</c:otherwise>
</c:choose>

<c:forEach var="section" items="${sections}">
<br>
<c:choose>
 <c:when test="${userBean.sysAdmin && module=='admin'}">
  <span class="table_title_Admin">
 </c:when>
 <c:otherwise>
  <span class="table_title_manage">
 </c:otherwise>
</c:choose>
<c:choose>
 <c:when test="${userBean.sysAdmin && module=='admin'}">
  <span class="table_title_Admin">
 </c:when>
 <c:otherwise>
  <span class="table_title_manage">
 </c:otherwise>
</c:choose>
<fmt:message key="groups" bundle="${resword}"/> </span>
<div style="width: 700px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">

<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr valign="top">
  <td class="table_header_row_left"><fmt:message key="group_name" bundle="${resword}"/></td>
  <td class="table_header_row"><fmt:message key="group_space_oid" bundle="${resword}"/></td>
  <td class="table_header_row"><fmt:message key="group_type" bundle="${resword}"/></td>
 </tr>
 <c:forEach var ="group" items="${section.groups}">
  <tr valign="top">
    <td class="table_cell_left"><c:out value="${group.name}"/></td>
    <td class="table_cell"><c:out value="${group.oid}"/></td>
    <td class="table_cell">
       <c:choose>
       <c:when test="${group.meta.repeatingGroup==true}">
        <fmt:message key="repeating" bundle="${resword}"/>
       </c:when>
       <c:otherwise>
         <fmt:message key="non_repeating" bundle="${resword}"/>
       </c:otherwise>
       </c:choose>
    </td>
  </tr>
  </c:forEach>
 </table>
</div>
</div></div></div></div></div></div></div></div>


</div>
<br>
<c:choose>
 <c:when test="${userBean.sysAdmin && module=='admin'}">
  <span class="table_title_Admin">
 </c:when>
 <c:otherwise>
  <span class="table_title_manage">
 </c:otherwise>
</c:choose>
<fmt:message key="Items" bundle="${resword}"/></span>
<div style="width: 100%">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
 <tr valign="top">
    <td class="table_header_row"><fmt:message key="item_name" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="item_space_oid" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="group_space_oid" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="description" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="group_name" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="item_data" bundle="${resword}"/> <fmt:message key="type" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="label" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="response_options" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="response_values" bundle="${resword}"/></td>
    <td class="table_header_row"><fmt:message key="required" bundle="${resword}"/></td>
  </tr>
  <c:forEach var ="item" items="${section.items}">
   <tr valign="top">
    <c:choose>
    <c:when test="${item.id > 0}">
      <td class="table_cell"><a href="javascript: openDocWindow('ViewItemDetail?itemId=<c:out value="${item.id}"/>')"><c:out value="${item.name}"/></a></td>
    </c:when>
    <c:otherwise>
      <td class="table_cell"><c:out value="${item.name}"/></td>
    </c:otherwise>
    </c:choose>
    <td class="table_cell"><c:out value="${item.oid}"/>&nbsp;</td>
    <td class="table_cell">
        <c:forEach var ="group" items="${section.groups}">
            <c:forEach var="itemMetaGroupBean" items="${group.itemGroupMetaBeans}">
                <c:if test="${itemMetaGroupBean.itemId == item.id}">
                    <c:out value="${group.oid}"/>
                </c:if>
            </c:forEach>
        </c:forEach>
    <td class="table_cell"><c:out value="${item.description}"/>&nbsp;</td>
    <c:choose>
    <c:when test="${item.itemMeta.groupLabel != 'Ungrouped'}">
      <td class="table_cell"><c:out value="${item.itemMeta.groupLabel}"/></td>
    </c:when>
    <c:otherwise>
      <td class="table_cell"><c:out value=""/></td>
    </c:otherwise>
    </c:choose>
    <td class="table_cell"><c:out value="${item.dataType.description}"/>&nbsp;</td>
    <td class="table_cell"><c:out value="${item.itemMeta.leftItemText}"/></td>
    <td class="table_cell">
    <c:set var="optionSize" value="0"/>
    <c:forEach var="option" items="${item.itemMeta.responseSet.options}">
      <c:set var="optionSize" value="${optionSize+1}"/>
    </c:forEach>
    <c:forEach var="option" items="${item.itemMeta.responseSet.options}">
      <c:choose>
        <c:when test="${optionSize > 1}">
          <c:out value="${option.text}"/>,
        </c:when>
        <c:otherwise>
            <c:out value="${option.text}"/>
          </c:otherwise>
        </c:choose>
        <c:set var="optionSize" value="${optionSize-1}"/>
    </c:forEach>&nbsp;
    </td>
    <td class="table_cell">
    <c:set var="optionSize" value="0"/>
    <c:forEach var="option" items="${item.itemMeta.responseSet.options}">
      <c:set var="optionSize" value="${optionSize+1}"/>
    </c:forEach>
    <c:forEach var="option" items="${item.itemMeta.responseSet.options}">
      <c:choose>
        <c:when test="${optionSize > 1}">
          <c:out value="${option.value}"/>,
        </c:when>
        <c:otherwise>
            <c:out value="${option.value}"/>
          </c:otherwise>
        </c:choose>
        <c:set var="optionSize" value="${optionSize-1}"/>
    </c:forEach>&nbsp;
    </td>
     <td class="table_cell">
     <c:choose>
      <c:when test="${item.itemMeta.required==true}">
       <fmt:message key="yes" bundle="${resword}"/>
      </c:when>
      <c:otherwise>
        <fmt:message key="no" bundle="${resword}"/>
      </c:otherwise>
      </c:choose>
      </td>
  </tr>

 </c:forEach>
 </table>
 </div>
</div></div></div></div></div></div></div></div>

</div>
<br><br>
</c:forEach>

<jsp:include page="../include/footer.jsp"/>
