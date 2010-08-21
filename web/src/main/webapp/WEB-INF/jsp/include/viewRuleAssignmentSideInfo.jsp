<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<style>

.sidebar_tab_kk {
background-color:#FFFFFF;
background-image:url("../images/sidebar_tab.gif");
background-position:right top;
background-repeat:no-repeat;
border-bottom-color:#B2B2B2;
border-bottom-style:solid;
border-bottom-width:1px;
border-left-color-ltr-source:physical;
border-left-color-rtl-source:physical;
border-left-color-value:#B2B2B2;
border-left-style-ltr-source:physical;
border-left-style-rtl-source:physical;
border-left-style-value:solid;
border-left-width-ltr-source:physical;
border-left-width-rtl-source:physical;
border-left-width-value:0;
border-right-color-ltr-source:physical;
border-right-color-rtl-source:physical;
border-right-color-value:#B2B2B2;
border-right-style-ltr-source:physical;
border-right-style-rtl-source:physical;
border-right-style-value:solid;
border-right-width-ltr-source:physical;
border-right-width-rtl-source:physical;
border-right-width-value:0;
border-top-color:#B2B2B2;
border-top-style:solid;
border-top-width:0;
padding-bottom:4px;
padding-left:6px;
padding-right:8px;
padding-top:4px;
width:160px;
}
</style>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/> 


<%--<jsp:useBean scope="session" id="panel" class="org.akaza.openclinica.view.StudyInfoPanel" />--%>


<!-- Sidebar Contents after alert-->

    
<c:choose>
 <c:when test="${userBean != null && userBean.id>0}">   
    <tr id="sidebar_Info_open">
        <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Info_open'); leftnavExpand('sidebar_Info_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

        <b><fmt:message key="info" bundle="${resword}"/></b>
   
        <div class="sidebar_tab_content">

            <span style="color: #789EC5">
            
      <c:if test="${panel.studyInfoShown}">
                <c:import url="/WEB-INF/jsp/include/viewRuleAssignmentStudySideInfo.jsp"/>
        <br>
        </c:if>       

      <c:if test="${panel.createDataset}">   
        <!--
        <table cellspacing="0" cellpadding="0" border="0" width="142">
            <tr id="sidebar_Info_open">
            <td class="sidebar_tab_kk">&nbsp;</td>
            </tr>
        </table>
        -->

        <c:import url="../include/viewRuleAssignmentSide.jsp"/>
        <br><br>
      </c:if>  
      <br>
      <c:if test="${newDataset.id>0}">
        <c:forEach var='line' items="${panel.data}">
            <b><c:out value="${line.key}" escapeXml="false"/>:</b>&nbsp;
            <c:out value="${line.value}" escapeXml="false"/>
            <br>
        </c:forEach> 
        <br><br>
        <c:import url="../include/studySideInfo.jsp"/> 
      </c:if>
 <script language="JavaScript">
       <!--
         function leftnavExpand(strLeftNavRowElementName){

           var objLeftNavRowElement;

           objLeftNavRowElement = MM_findObj(strLeftNavRowElementName);
           if (objLeftNavRowElement != null) {
             if (objLeftNavRowElement.style) { objLeftNavRowElement = objLeftNavRowElement.style; } 
               objLeftNavRowElement.display = (objLeftNavRowElement.display == "none" ) ? "" : "none";      
             }
           }

       //-->
     </script>  
     
    </div>

    </td>
    </tr>
    <tr id="sidebar_Info_closed" style="display: none">
        <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Info_open'); leftnavExpand('sidebar_Info_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

        <b><fmt:message key="info" bundle="${resword}"/></b>

        </td>
    </tr>   
</table>    
    
</c:when>
<c:otherwise>
    <br><br>
    <a href="MainMenu"><fmt:message key="login" bundle="${resword}"/></a>   
    <br><br>
    <a href="RequestAccount"><fmt:message key="request_an_account" bundle="${resword}"/></a>
    <br><br>
    <a href="RequestPassword"><fmt:message key="forgot_password" bundle="${resword}"/></a>
</c:otherwise>
</c:choose>


<!-- End Sidebar Contents -->

                <br><img src="images/spacer.gif" width="120" height="1">

                </td>
               
                <td class="content" valign="top">
