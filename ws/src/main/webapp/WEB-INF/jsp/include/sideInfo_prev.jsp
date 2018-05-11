<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>


<%--<jsp:useBean scope="session" id="panel" class="org.akaza.openclinica.view.StudyInfoPanel" />--%>


<!-- Sidebar Contents after alert-->


<c:choose>
 <c:when test="${userBean != null && userBean.id>0}">
	<tr id="sidebar_Info_open">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Info_open'); leftnavExpand('sidebar_Info_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="info" bundle="${restext}"/></b>

		<div class="sidebar_tab_content">

			<span style="color: #789EC5">



 <%-- begin standard study info --%>
 	<c:if test="${panel.studyInfoShown}">

 	<c:choose>
 	<c:when test="${study.status.name != 'removed' && study.status.name != 'auto-removed'}">

	<c:choose>
	<c:when test="${study.parentStudyId>0}">
	<b><fmt:message key="site" bundle="${resword}"/>:</b>&nbsp;
	 <a href="ViewSite?id=<c:out value="${study.id}"/>">
	</c:when>
	<c:otherwise>
	<b><fmt:message key="study" bundle="${resword}"/>:</b>&nbsp;
	 <a href="ViewStudy?id=<c:out value="${study.id}"/>&viewFull=yes">
	</c:otherwise>
	</c:choose>
	<c:out value="${study.name}"/></a>

	<br><br>

	<b><fmt:message key="start_date" bundle="${resword}"/>:</b>&nbsp;
	 <c:choose>
	  <c:when test="${study.datePlannedStart != null}">
	   <fmt:formatDate value="${study.datePlannedStart}" pattern="${dteFormat}"/>
      </c:when>
	  <c:otherwise>
	   <fmt:message key="na" bundle="${resword}"/>
	 </c:otherwise>
	 </c:choose>
	<br><br>

	<b><fmt:message key="end_date" bundle="${resword}"/>:</b>&nbsp;
	<c:choose>
	  <c:when test="${study.datePlannedEnd != null}">
	   <fmt:formatDate value="${study.datePlannedEnd}" pattern="${dteFormat}"/>
	  </c:when>
	  <c:otherwise>
	   <fmt:message key="na" bundle="${resword}"/>
	  </c:otherwise>
    </c:choose>
	<br><br>

	<b><fmt:message key="pi" bundle="${resword}"/>:</b>&nbsp; <c:out value="${study.principalInvestigator}"/>

	<br><br>

	<b><fmt:message key="protocol_verification" bundle="${resword}"/>:</b>&nbsp;
	<fmt:formatDate value="${study.protocolDateVerification}" pattern="${dteFormat}"/>

	<br><br>



	<b><fmt:message key="collect_subject" bundle="${resword}"/></b>&nbsp;
	<c:choose>
    <c:when test="${study.studyParameterConfig.collectDob == '1'}">
     <fmt:message key="yes" bundle="${resword}"/>
    </c:when>
    <c:when test="${study.studyParameterConfig.collectDob == '2'}">
     <fmt:message key="only_year_of_birth" bundle="${resword}"/>
    </c:when>
    <c:otherwise>
     <fmt:message key="not_used" bundle="${resword}"/>
    </c:otherwise>
   </c:choose>

	</c:when>
   <c:otherwise>
   Your last active study/site was <c:out value="${study.name}"/>, but it has been deleted.
   </c:otherwise>
   </c:choose>

	<br><br>
	</c:if>
	<c:choose>
		<c:when test="${panel.orderedData}">
			<c:forEach var='line' items="${panel.userOrderedData}">
				<b><c:out value="${line.title}" escapeXml="false"/>
				<%--<c:if test="${line.colon}">:</c:if>--%>:</b>&nbsp;
				<c:out value="${line.info}" escapeXml="false"/>
			    <br><br>
		    </c:forEach>
		</c:when>
		<c:otherwise>
			<c:forEach var='line' items="${panel.data}">
				<b><c:out value="${line.key}" escapeXml="false"/>:</b>&nbsp;
				<c:out value="${line.value}" escapeXml="false"/>
			    <br><br>
		    </c:forEach>
		</c:otherwise>
	 </c:choose>

<%-- end standard study info --%>
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

		<b>Info</b>

		</td>
	</tr>
	<c:if test="${panel.iconInfoShown && !panel.manageSubject}">
	 <c:import url="../include/sideIcons.jsp"/>
	</c:if>
	<c:if test="${!panel.iconInfoShown && panel.manageSubject}">
	 <c:import url="../include/sideIconsSubject.jsp"/>
	</c:if>

  </table>
  <c:choose>
  <c:when test="${panel.createDataset}">
     <c:import url="../include/createDatasetSide.jsp"/>
  </c:when>
  <c:when test="${panel.extractData}">

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

  </c:when>

  <c:when test="${panel.submitDataModule}">
     <c:import url="../include/submitDataSide.jsp"/>
  </c:when>

  </c:choose>
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
				<td class="aka_revised_content_preview" valign="top">

