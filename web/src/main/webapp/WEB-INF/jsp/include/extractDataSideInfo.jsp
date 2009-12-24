<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/> 


<%--
<jsp:useBean scope="session" id="panel" class="org.akaza.openclinica.view.StudyInfoPanel" />
--%>


<!-- Sidebar Contents after alert-->

	
<c:choose>
 <c:when test="${userBean != null && userBean.id>0}">	
	<tr id="sidebar_Info_open">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Info_open'); leftnavExpand('sidebar_Info_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="info" bundle="${resword}"/></b>
   
		<div class="sidebar_tab_content">

			<span style="color: #789EC5">

	<c:if test="${panel.extractData}">
     
	<c:if test="${panel.orderedData}">
	   <c:set var="count" value="0"/>
	   <c:set var="newEvent" value="0"/>
	   <c:set var="eventCount" value="0"/>		 
	   <c:forEach var='line' items="${panel.userOrderedData}">
			<c:if test="${line.colon}">
			 <c:choose>
			 <c:when test="${line.title=='Study Event Definition'}">
			  
			  <c:if test="${count >0 && eventCount>0}">
			      </table>
	             </td>
               </tr>
			  </c:if>
			  <c:if test="${eventCount==0}">
			   <table border="0" cellpadding="0" cellspacing="0" width="120">
			  </c:if>
			  <c:set var="count" value="${count+1}"/>
			  <c:set var="newEvent" value="1"/>
			  <c:set var="eventCount" value="${eventCount+1}"/>
			  <tr>
	           <td valign="top" width="10" class="leftmenu"><a href="javascript:leftnavExpand('leftnavSubRow_SubSection<c:out value="${eventCount}"/>'); 
		          javascript:setImage('ExpandGroup<c:out value="${eventCount}"/>','images/bt_Collapse.gif');"><img 
		          name="ExpandGroup<c:out value="${eventCount}"/>" src="images/bt_Expand.gif" border="0"></a></td>
	              <td valign="top" class="leftmenu"><a href="javascript:leftnavExpand('leftnavSubRow_SubSection<c:out value="${eventCount}"/>'); 
		            javascript:setImage('ExpandGroup<c:out value="${eventCount}"/>','images/bt_Collapse.gif');"><b><c:out value="${line.info}" escapeXml="false"/></b></a>
		       </td>
             </tr>  
             </c:when>
             <c:otherwise>
               <b><c:out value="${line.title}" escapeXml="false"/>: <c:out value="${line.info}" escapeXml="false"/></b>
                <br/>             
                <br/>
             </c:otherwise>
             </c:choose>           
             </c:if>
             <c:if test="${!line.colon}">
               <c:if test="${newEvent==1}">
                  <tr id="leftnavSubRow_SubSection<c:out value="${eventCount}"/>" valign="top">
	               <td colspan="3">
	               <table border="0" cellpadding="0" cellspacing="0" width="110">
               </c:if>
                <c:set var="newEvent" value="0"/>
                <c:set var="count" value="${count+1}"/>
                <tr>
                 <c:choose>
                   <c:when test="${line.lastCRF}">                  
		             <td valign="top" class="vline_B">
		           </c:when>
		           <c:otherwise>
		             <td valign="top" class="vline">
		          </c:otherwise> 
		         </c:choose>
		         <img src="images/leftbar_hline.gif"></td>
		         <td valign="top" class="leftmenu" style="font-size:11px; color:#789EC5"><c:out value="${line.info}" escapeXml="false"/></td>
	           </tr>
             </c:if>
			  
		  </c:forEach>
		   <c:if test="${count>0}">
		     </table>
	         </td>
            </tr>
		  </table>  
		  </c:if>
	</c:if>
  
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
  
  
  
  <c:if test="${panel.iconInfoShown}">
	 <c:import url="../include/sideIcons.jsp"/>
	</c:if>
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

