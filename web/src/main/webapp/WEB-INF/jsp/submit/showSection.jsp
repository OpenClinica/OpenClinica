<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
 

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>  
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>

<jsp:useBean scope="request" id="section" class=
  "org.akaza.openclinica.bean.submit.DisplaySectionBean" />
<jsp:useBean scope="request" id="annotations" class="java.lang.String" />

<script type="text/javascript" language="JavaScript">
  <!--
  function checkSectionStatus() {

    objImage=document.getElementById('status_top');
    //alert(objImage.src);
    if (objImage != null && objImage.src.indexOf('images/icon_UnsavedData.gif')>0) {
      return confirm('<fmt:message key="you_have_unsaved_data2" bundle="${resword}"/>');
    }

    return true;
  }


  function checkEntryStatus(strImageName) {
    objImage = MM_findObj(strImageName);
    //alert(objImage.src);
    if (objImage != null && objImage.src.indexOf('images/icon_UnsavedData.gif')>0) {
      return confirm('<fmt:message key="you_have_unsaved_data_exit" bundle="${resword}"/>');
    }
    return true;
  }
  //-->
</script>


<c:set var="stage" value="${param.stage}"/>
<table border="0" cellpadding="0" cellspacing="0">
<tr>
<td>
<div style="width:100%">
<!-- These DIVs define shaded box borders -->
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B">
<div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="tablebox_center">
<c:set var="currPage" value="" />
<c:set var="curCategory" value="" />


<!-- Table Contents -->

<table border="0" cellpadding="0" cellspacing="0">
<c:set var="displayItemNum" value="${0}" />
<c:set var="itemNum" value="${0}" />
<c:set var="numOfTr" value="0"/>
<c:set var="numOfDate" value="1"/>
<c:if test='${section.section.title != ""}'>
  <tr class="aka_stripes">
    <td class="aka_header_border"><b><fmt:message key="title" bundle="${resword}"/>:&nbsp;<c:out value="${section.section.title}" escapeXml="false"/></b> </td>
  </tr>
</c:if>
<c:if test='${section.section.subtitle != ""}'>
  <tr class="aka_stripes">
    <td class="aka_header_border"><fmt:message key="subtitle" bundle="${resword}"/>:&nbsp;<c:out value="${section.section.subtitle}" escapeXml="false"/> </td>
  </tr>
</c:if>
<c:if test='${section.section.instructions != ""}'>
  <tr class="aka_stripes">
    <td class="aka_header_border"><fmt:message key="instructions" bundle="${resword}"/>:&nbsp;<c:out value="${section.section.instructions}" escapeXml="false"/> </td>
  </tr>
</c:if>
<c:forEach var="displayItem" items="${section.items}" varStatus="itemStatus">
<c:if test="${displayItemNum ==0}">
  <!-- always show the button and page above the first item-->
  <!-- to handle the case of no pageNumLabel for all the items-->
  <%--  BWP: corrected "column span="2" "--%>
  <tr class="aka_stripes">
  <%--  <td class="aka_header_border" colspan="2">--%>
    <td class="aka_header_border" colspan="2">
      <table border="0" cellpadding="0" cellspacing="0" width="100%" style="margin-bottom: 6px;">
        <tr>

          <td valign="bottom" nowrap="nowrap" style="padding-right: 50px">

            <a name="top"><fmt:message key="page" bundle="${resword}"/>: <c:out value="${displayItem.metadata.pageNumberLabel}" escapeXml="false"/></a>
          </td>
          <td align="right" valign="bottom">
            <table border="0" cellpadding="0" cellspacing="0">
              <tr>
                <c:choose>
                  <c:when test="${stage !='adminEdit' && section.lastSection}">
                    <td valign="bottom"><input type="checkbox" name="markComplete" value="Yes"
                                               onClick='return confirm("<fmt:message key="marking_CRF_complete_finalize_DE" bundle="${restext}"/>");'>
                    </td>
                    <td valign="bottom" nowrap="nowrap">&nbsp; <fmt:message key="mark_CRF_complete" bundle="${resword}"/> &nbsp;&nbsp;&nbsp;</td>
                  </c:when>
                  <c:otherwise>
                    <td colspan="2">&nbsp;</td>
                  </c:otherwise>
                </c:choose>
                <td><input type="submit" name="submittedResume" value="<fmt:message key="save" bundle="${resword}"/>" class=
                  "button_medium" /></td>
                <td><input type="submit" name="submittedExit" value="<fmt:message key="exit" bundle="${resword}"/>" class=
                  "button_medium" onClick="return checkEntryStatus('DataStatus_top');" /></td>

                <td valign="bottom"><img name=
                  "DataStatus_top" id="status_top" alt="<fmt:message key="data_status" bundle="${resword}"/>" src="images/icon_UnchangedData.gif"></td>

              </tr>
            </table>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</c:if>
<c:if test="${currPage != displayItem.metadata.pageNumberLabel && displayItemNum >0}">
  <!-- show page number and buttons -->
  <%--  BWP: corrected "column span="2" "--%>
  <tr class="aka_stripes">
    <td class="aka_header_border" colspan="2">
      <table border="0" cellpadding="0" cellspacing="0" width="100%" style="margin-bottom: 6px;">
        <tr>

          <td valign="bottom" nowrap="nowrap" style="padding-right: 50px">

            <a name="item<c:out value="${displayItem.item.id}"/>">
              <fmt:message key="page" bundle="${resword}"/>: <c:out value="${displayItem.metadata.pageNumberLabel}" escapeXml="false"/>
            </a>
          </td>
          <td align="right" valign="bottom">
            <table border="0" cellpadding="0" cellspacing="0">
              <tr>
                <c:choose>
                  <c:when test="${stage !='adminEdit' && section.lastSection}">
                    <td valign="bottom"><input type="checkbox" name="markComplete" value="Yes"
                                               onClick='return confirm("<fmt:message key="marking_CRF_complete_finalize_DE" bundle="${restext}"/>");'>
                    </td>
                    <td valign="bottom" nowrap>&nbsp; <fmt:message key="mark_CRF_complete" bundle="${resword}"/> &nbsp;&nbsp;&nbsp;</td>
                  </c:when>
                  <c:otherwise>
                    <td colspan="2">&nbsp;</td>
                  </c:otherwise>
                </c:choose>
                <td><input type="submit" name="submittedResume" value="<fmt:message key="save" bundle="${resword}"/>" class="button_medium" /></td>
                <td><input type="submit" name="submittedExit" value="<fmt:message key="exit" bundle="${resword}"/>" class="button_medium" onClick="return checkEntryStatus('DataStatus_top');" /></td>

                  <%--<td valign="bottom"><img name="DataStatus_top" src="images/icon_UnchangedData.gif"></td>--%>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </td>
  </tr>
  <!-- end of page number and buttons-->

</c:if>

<c:set var="currPage" value="${displayItem.metadata.pageNumberLabel}" />

<%-- SHOW THE PARENT FIRST --%>
<c:if test="${displayItem.metadata.parentId == 0}">

<!--ACCORDING TO COLUMN NUMBER, ARRANGE QUESTIONS IN THE SAME LINE-->

<c:if test="${displayItem.metadata.columnNumber <=1}">
  <c:if test="${numOfTr > 0 }">
    </tr>
    </table>
    </td>

    </tr>

  </c:if>
  <c:set var="numOfTr" value="${numOfTr+1}"/>
  <c:if test="${!empty displayItem.metadata.header}">
    <tr class="aka_stripes">
      <%--<td class="table_cell_left" bgcolor="#F5F5F5">--%>
      <td class="table_cell_left aka_stripes"><b><c:out value=
        "${displayItem.metadata.header}" escapeXml="false"/></b></td>
    </tr>
  </c:if>
  <c:if test="${!empty displayItem.metadata.subHeader}">
    <tr class="aka_stripes">
      <td class="table_cell_left"><c:out value="${displayItem.metadata.subHeader}" escapeXml=
        "false"/></td>
    </tr>
  </c:if>
  <tr>
  <td class="table_cell_left">
  <table border="0" >
  <tr>
  <td valign="top">
</c:if>

<c:if test="${displayItem.metadata.columnNumber >1}">
  <td valign="top">
</c:if>
<table border="0">
  <tr>
      <%--	<td valign="top"><c:out value="${displayItem.metadata.questionNumberLabel}" escapeXml="false"/></td>
             <td valign="top"><c:out value="${displayItem.metadata.leftItemText}" escapeXml="false"/></td>--%>
    <td valign="top" class="text_block"><c:out value=
      "${displayItem.metadata.questionNumberLabel}" escapeXml="false"/> <c:out value="${displayItem.metadata.leftItemText}" escapeXml="false"/></td>
    <td valign="top">
        <%-- display the HTML input tag --%>
      <c:set var="displayItem" scope="request" value="${displayItem}" />
      <c:import url="../submit/showItemInput.jsp">
        <c:param name="key" value="${numOfDate}" />
        <c:param name="tabNum" value="${itemNum}"/>
      </c:import>
      <!--<br />--><c:import url="../showMessage.jsp"><c:param name="key" value=
      "input${displayItem.item.id}" /></c:import>
    </td>
    <c:if test='${displayItem.item.units != ""}'>
      <td valign="top">
        <c:out value="(${displayItem.item.units})" escapeXml="false"/>
      </td>
    </c:if>
    <td valign="top"><c:out value="${displayItem.metadata.rightItemText}" escapeXml="false" /></td>
  </tr>
</table>
</td>
<c:if test="${itemStatus.last}">
  </tr>
  </table>
  </td>

  </tr>
</c:if>

<c:if test="${displayItem.numChildren > 0}">
  <tr>
  <%-- indentation --%>
  <!--<td class="table_cell">&nbsp;</td>-->
  <%-- NOW SHOW THE CHILDREN --%>

  <td class="table_cell">
  <table border="0">
  <c:set var="notFirstRow" value="${0}" />
  <c:forEach var="childItem" items="${displayItem.children}">


    <c:set var="currColumn" value="${childItem.metadata.columnNumber}" />
    <c:if test="${currColumn == 1}">
      <c:if test="${notFirstRow != 0}">
        </tr>
      </c:if>
      <tr>
      <c:set var="notFirstRow" value="${1}" />
      <%-- indentation --%>
      <td valign="top">&nbsp;</td>
    </c:if>
    <%--
              this for loop "fills in" columns left blank
              e.g., if the first childItem has column number 2, and the next one has column number 5,
              then we need to insert one blank column before the first childItem, and two blank columns between the second and third children
            --%>
    <c:forEach begin="${currColumn}" end="${childItem.metadata.columnNumber}">
      <td valign="top">&nbsp;</td>
    </c:forEach>

    <td valign="top">
      <table border="0">
        <tr>
          <td valign="top" class="text_block"><c:out value="${childItem.metadata.questionNumberLabel}" escapeXml="false"/> <c:out value="${childItem.metadata.leftItemText}" escapeXml="false"/></td>
          <td valign="top">
              <%-- display the HTML input tag --%>
            <c:set var="itemNum" value="${itemNum + 1}" />
            <c:set var="displayItem" scope="request" value="${childItem}" />
            <c:import url="../submit/showItemInput.jsp" >
              <c:param name="key" value="${numOfDate}" />
              <c:param name="tabNum" value="${itemNum}"/>
            </c:import>
              <%--	<br />--%><c:import url="../showMessage.jsp"><c:param name="key" value="input${childItem.item.id}" /></c:import>
          </td>
          <c:if test='${childItem.item.units != ""}'>
            <td valign="top"> <c:out value="(${childItem.item.units})" escapeXml="false"/> </td>
          </c:if>
          <td valign="top"> <c:out value="${childItem.metadata.rightItemText}" escapeXml="false"/> </td>
        </tr>
      </table>
    </td>
  </c:forEach>
  </tr>
  </table>
  </td>
  </tr>
</c:if>
</c:if>
<c:set var="displayItemNum" value="${displayItemNum + 1}" />
<c:set var="itemNum" value="${itemNum + 1}" />
</c:forEach>
</table>



<table border="0" cellpadding="0" cellspacing="0" width="100%" style="margin-bottom: 6px;">
  <!--   style="padding-right: 50px"-->
  <tr>
    <td valign="bottom" nowrap="nowrap">
      <a href="#top">&nbsp;&nbsp;<fmt:message key="return_to_top" bundle="${resword}"/></a>
    </td>
    <td align="right" valign="bottom">
      <table border="0" cellpadding="0" cellspacing="0">
        <tr>
          <c:choose>
            <c:when test="${stage !='adminEdit' && section.lastSection}">
              <td valign="bottom">
                <input type="checkbox" name="markComplete" value="Yes"
                       onClick='return confirm(<fmt:message key="marking_CRF_complete_finalize_DE" bundle="${restext}"/>);'>
              </td>
              <td valign="bottom" nowrap>&nbsp; <fmt:message key="mark_complete" bundle="${resword}"/>&nbsp;&nbsp;&nbsp;</td>
            </c:when>
            <c:otherwise>
              <td colspan="2">&nbsp;</td>
            </c:otherwise>
          </c:choose>
          <td><input type="submit" name="submittedResume" value="<fmt:message key="save" bundle="${resword}"/>" class=
            "button_medium" /></td>
          <td><input type="submit" name="submittedExit" value="<fmt:message key="exit" bundle="${resword}"/>" class="button_medium" onClick="return checkEntryStatus('DataStatus_bottom');" /></td>

          <td valign="bottom"><img name="DataStatus_bottom" alt="<fmt:message key="data_status" bundle="${resword}"/>" src="images/icon_UnchangedData.gif">&nbsp;</td>


        </tr>
      </table>
    </td>
  </tr>
</table>

<!-- End Table Contents -->

</div>
</div></div></div></div></div></div></div></div>
</div>

</td>
</tr>
</table>

<br>
