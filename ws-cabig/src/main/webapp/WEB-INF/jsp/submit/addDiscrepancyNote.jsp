<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:useBean scope='request' id='strResStatus' class='java.lang.String' />
<jsp:useBean scope='request' id='strUserAccountId' class='java.lang.String' />
<jsp:useBean scope='request' id='writeToDB' class='java.lang.String' />
<jsp:useBean scope='request' id='unlock' class='java.lang.String' />
<jsp:useBean scope='request' id='autoView' class='java.lang.String' />
<jsp:useBean scope='session' id='study' class='org.akaza.openclinica.bean.managestudy.StudyBean' />
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<html>
<head>
<title><fmt:message key="openclinica" bundle="${resword}"/>- <fmt:message key="add_discrepancy_note" bundle="${resword}"/></title>
<link rel="stylesheet" href="includes/styles.css" type="text/css">
<script language="JavaScript" src="includes/global_functions_javascript.js"></script>

<script language="JavaScript" src="includes/CalendarPopup.js"></script>

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

	function hide(strLeftNavRowElementName){

        var objLeftNavRowElement;

        objLeftNavRowElement = MM_findObj(strLeftNavRowElementName);
        if (objLeftNavRowElement != null) {
            if (objLeftNavRowElement.style) { objLeftNavRowElement = objLeftNavRowElement.style; }
            objLeftNavRowElement.display = "none";
        }
    }



    //-->
</script>

<style type="text/css">

.popup_BG { background-image: url(images/main_BG.gif);
	background-repeat: repeat-x;
	background-position: top;
	background-color: #FFFFFF;
	}


</style>

<script language="JavaScript">
<!--

function setStatus(typeId) {

	objtr1=document.getElementById('res1');
	objtr2=document.getElementById('resStatusId');
	if (typeId == 2|| typeId ==4) {//annotation or reason for change

	  	objtr2.value=5;
	  	objtr2.disabled = true;
	} else if (typeId == 3) { //query
		objtr2.value=1; //new
	} else {
  		if (objtr2.value ==5 && objtr2.disabled) {
   		objtr2.value=1;
  	}

  	objtr2.disabled = false;

	}
}

function setResStatus(resStatusId, destinationUserId) {
	objtr1=document.getElementById('resStatusId');
	objtr2=document.getElementById('userAccountId');
	objtr3=document.getElementById('xxx');


	if (resStatusId == 3 || resStatusId == 4) { //Resolutiuon proposed or Closed
		// objtr2.disabled = false;
		objtr2.value = destinationUserId;
		// disable?
		objtr2.disabled = false;
		objtr3.removeAttribute('disabled');
		//objtr3.value = destinationUserId;
		// disable?
		//objtr3.disabled = false;
	}
}


//-->
</script>
</head>
<body class="popup_BG" >
<div style="float: left;"><h1 class="table_title_Submit"><fmt:message key="add_discrepancy_note" bundle="${resword}"/></h1></div>
<div style="float: right;"><p><a href="#" onclick="javascript:window.close();"><fmt:message key="close_window" bundle="${resword}"/></a></p></div>
<br clear="all">
<div class="alert">
<c:forEach var="message" items="${pageMessages}">
 <c:out value="${message}" escapeXml="false"/>
</c:forEach>
</div>
<form name="noteForm" method="POST" action="CreateDiscrepancyNote">
<jsp:include page="../include/showSubmitted.jsp" />
<input type="hidden" name="name" value="<c:out value="${discrepancyNote.entityType}"/>">
<input type="hidden" name="column" value="<c:out value="${discrepancyNote.column}"/>">
<input type="hidden" name="parentId" value="<c:out value="${discrepancyNote.parentDnId}"/>">
<input type="hidden" name="id" value="<c:out value="${discrepancyNote.entityId}"/>">
<input type="hidden" name="subjectId" value="<c:out value="${param.subjectId}"/>">
<input type="hidden" name="field" value="<c:out value="${discrepancyNote.field}"/>">
<input type="hidden" name="writeToDB" value="<c:out value="${writeToDB}" />">
<input type="hidden" name="monitor" value="<c:out value="${monitor}" />">
<input type="hidden" name="new" value="<c:out value="${new}" />">
<input type="hidden" name="enterData" value="<c:out value="${enterData}" />">

   <table border="0">
   <tr valign="top">
    <td><fmt:message key="subject" bundle="${resword}"/></td>
    <td><c:out value="${discrepancyNote.subjectName}" />&nbsp;</td>
   </tr>
   <c:if test="${discrepancyNote.eventName !=''}">
   <tr valign="top">
    <td><fmt:message key="event" bundle="${resword}"/></td>
    <td><c:out value="${discrepancyNote.eventName}"/>&nbsp;</td>
   </tr>
    <tr valign="top">
    <td><fmt:message key="event_date" bundle="${resword}"/></td>
    <td><fmt:formatDate value="${discrepancyNote.eventStart}" pattern="${dteFormat}"/></td>
   </tr>
   </c:if>
   <c:if test="${discrepancyNote.crfName !=''}">
    <tr valign="top">
    <td><fmt:message key="CRF" bundle="${resword}"/></td>
    <td><c:out value="${discrepancyNote.crfName}"/>&nbsp;</td>
   </tr>
   </c:if>
        <tr valign="top">
            <td><fmt:message key="entity_type_field" bundle="${resword}"/></td>
            <td><c:out value="${discrepancyNote.entityType}"/>/<c:out value="${discrepancyNote.column}"/>
             </td>
        </tr>
        <c:if test="${discrepancyNote.entityType == 'itemData'}">
            <tr valign="top">
            <td><fmt:message key="item_name" bundle="${resword}"/></td>
            <td><a href="javascript: openDocWindow('ViewItemDetail?itemId=<c:out value="${item.id}"/>')"><c:out value="${item.name}"/></a></td>
            </tr>
         </c:if>

       <tr valign="top">
            <td><fmt:message key="discrepancy_thread_id" bundle="${resword}"/></td>
            <td>
            <c:out value="${parent.id}"/>
            </td>
        </tr>
        <tr valign="top">
           <td><fmt:message key="type" bundle="${resword}"/></td>
           <td>
            <c:set var="typeId1" value="${discrepancyNote.discrepancyNoteTypeId}"/>
               <c:choose>
                 <c:when test="${parent == null || parent.id ==0 }">
                    <c:forEach var="type" items="${discrepancyTypes}">
                        <c:choose>
                			 <c:when test="${typeId1 == type.id}">
                			 <%-- need to create a special case for Queries, tbh --%>
			                  <c:choose>
			                    <c:when test="${type.id == 3}">
				                   <input type="radio" name="typeId" value="<c:out value="${type.id}"/>" checked  onclick ="javascript:setStatus(<c:out value="${type.id}"/>);javascript:leftnavExpand('user1');javascript:leftnavExpand('user2');"><c:out value="${type.name}"/><br>
			                    </c:when>
			                    <c:otherwise>
                                    <c:choose>
                                    <c:when test="${study.status.frozen && (type.id==2 || type.id==4)}">
				                        <input type="radio" name="typeId" value="<c:out value="${type.id}"/>" disabled="true" checked  onclick ="javascript:setStatus(<c:out value="${type.id}"/>);javascript:hide('user1');javascript:hide('user2');"><c:out value="${type.name}"/><br>
                                    </c:when>
                                    <c:otherwise>
                                        <input type="radio" name="typeId" value="<c:out value="${type.id}"/>" checked  onclick ="javascript:setStatus(<c:out value="${type.id}"/>);javascript:hide('user1');javascript:hide('user2');"><c:out value="${type.name}"/><br>
                                    </c:otherwise>
                                    </c:choose>
			                    </c:otherwise>
			                  </c:choose>
        		              </c:when>
                              <c:otherwise>
        		                <c:choose>
		    	                <c:when test="${type.id == 3}">
			        	            <input type="radio" name="typeId" value="<c:out value="${type.id}"/>" onclick ="javascript:setStatus(<c:out value="${type.id}"/>);javascript:leftnavExpand('user1');javascript:leftnavExpand('user2');"><c:out value="${type.name}"/><br>
			                    </c:when>
			                    <c:otherwise>
                                    <c:choose>
                                    <c:when test="${study.status.frozen && (type.id==2 || type.id==4)}">
				                        <input type="radio" name="typeId" disabled="true" value="<c:out value="${type.id}"/>" onclick ="javascript:setStatus(<c:out value="${type.id}"/>);javascript:hide('user1');javascript:hide('user2');"><c:out value="${type.name}"/><br>
                                    </c:when>
                                    <c:otherwise>
                                        <input type="radio" name="typeId" value="<c:out value="${type.id}"/>" onclick ="javascript:setStatus(<c:out value="${type.id}"/>);javascript:hide('user1');javascript:hide('user2');"><c:out value="${type.name}"/><br>
                                    </c:otherwise>
                                    </c:choose>
			                    </c:otherwise>
			                    </c:choose>
    		                  </c:otherwise>
                            </c:choose>
                    </c:forEach>
                    <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="typeId"/></jsp:include>
                        </c:when>
                    <c:otherwise>
            <input type="hidden" name="typeId" value="${discrepancyNote.discrepancyNoteTypeId}"/>
            <c:forEach var="type" items="${discrepancyTypes}">
                <c:choose>
                    <c:when test="${typeId1 == type.id}">
                        <input type="radio" name="typeId" value="<c:out value="${type.id}"/>" checked disabled><c:out value="${type.name}"/><br>
                    </c:when>
                    <c:otherwise>
                        <input type="radio" name="typeId" value="<c:out value="${type.id}"/>" disabled><c:out value="${type.name}"/><br>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
          </c:otherwise>
          </c:choose>
              </td>

        </tr>
        <tr valign="top">
            <td><fmt:message key="description" bundle="${resword}"/></td>
            <td>
            <div class="formfieldXL_BG"><input type="text" name="description" value="<c:out value="${discrepancyNote.description}"/>" class="formfieldXL"></div>
             <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="description"/></jsp:include>
            </td>
        </tr>
        <tr valign="top">
            <td><fmt:message key="detailed_note" bundle="${resword}"/></td>
            <td>
            <c:choose>
            <c:when test="${discrepancyNote.detailedNotes !=''}">
             <div class="formtextareaXL4_BG">
              <textarea name="detailedDes" rows="4" cols="50" class="formtextareaXL4"><c:out value="${discrepancyNote.detailedNotes}"/></textarea>
             </div>
            </c:when>
            <c:otherwise>
             <div class="formtextareaXL4_BG">
              <textarea name="detailedDes" rows="4" cols="50" class="formtextareaXL4"><%--<c:out value="${param.strErrMsg}"/>--%></textarea>
             </div>
            </c:otherwise>
            </c:choose>
             <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="detailedDes"/></jsp:include>
            </td>
        </tr>
        <tr valign="top" id="res1">

            <td><fmt:message key="resolution_status" bundle="${resword}"/></td>
            <td><div class="formfieldL_BG">
			<c:choose>
				<c:when test='${strResStatus != ""}'>
					<c:set var="resStatusId1" value="${strResStatus}"/>
				</c:when>
				<c:otherwise>
					<c:set var="resStatusId1" value="${discrepancyNote.resolutionStatusId}"/>
				</c:otherwise>
			</c:choose>
            <select name="resStatusId" id="resStatusId" class="formfieldL" onchange="javascript:setResStatus(3, <c:out value="${discrepancyNote.ownerId}"/>);">
              <c:forEach var="status" items="${resolutionStatuses}">
               <c:choose>
                 <c:when test="${resStatusId1 == status.id}">
                   <option value="<c:out value="${status.id}"/>" selected ><c:out value="${status.name}"/>
                 </c:when>
                 <c:otherwise>
                   <option value="<c:out value="${status.id}"/>" ><c:out value="${status.name}"/>
                 </c:otherwise>
               </c:choose>
             </c:forEach>
            </select></div>
              <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="resStatusId"/></jsp:include></td>

        </tr>



		<c:choose>
		<c:when test="${(parent == null || parent.id ==0 || unlock == 1) && autoView == 0}">
        <tr valign="top" id="user1" style="display:none">
		</c:when>
		<c:otherwise>
		<tr valign="top">
      	</c:otherwise>
		</c:choose>
        <c:if test="${discrepancyNote.discrepancyNoteTypeId != 1}">
            <td>Assigned To</td>
            <td><div class="formfieldL_BG">
			<c:choose>
				<c:when test='${strUserAccountId != ""}'>
					<c:set var="userAccountId1" value="${strUserAccountId}"/>
				</c:when>
				<c:otherwise>
					<c:set var="userAccountId1" value="0"/>
				</c:otherwise>
			</c:choose>
			<c:choose>
			<c:when test="${parent == null || parent.id ==0 }">
			<%-- or when the user is not a CDC? --%>
            <select name="userAccountId" id="userAccountId" class="formfieldL">

              <c:forEach var="user" items="${userAccounts}">
               <c:choose>
                 <c:when test="${userAccountId1 == user.userAccountId}">
                   <option value="<c:out value="${user.userAccountId}"/>" selected><c:out value="${user.firstName}"/> <c:out value="${user.lastName}"/> (<c:out value="${user.userName}"/>)
                 </c:when>
                 <c:otherwise>
                   <option value="<c:out value="${user.userAccountId}"/>"><c:out value="${user.firstName}"/> <c:out value="${user.lastName}"/> (<c:out value="${user.userName}"/>)
                 </c:otherwise>
               </c:choose>
             </c:forEach>
            </select>
            <input type="hidden" name="userAccountId" value="<c:out value="${userAccountId1}"/>"/>
			</c:when>
			<c:otherwise>
			<span id="xxx" disabled>
			<select name="userAccountId" id="userAccountId" class="formfieldL" > <%-- will eventually take away select, tbh --%>

              <c:forEach var="user" items="${userAccounts}">
               <c:choose>
                 <c:when test="${userAccountId1 == user.userAccountId}">
                   <option value="<c:out value="${user.userAccountId}"/>" selected><c:out value="${user.firstName}"/> <c:out value="${user.lastName}"/> (<c:out value="${user.userName}"/>)
                 </c:when>
                 <c:otherwise>
                   <option value="<c:out value="${user.userAccountId}"/>"><c:out value="${user.firstName}"/> <c:out value="${user.lastName}"/> (<c:out value="${user.userName}"/>)
                 </c:otherwise>
               </c:choose>
             </c:forEach>
            </select>
            </span>

			<input type="hidden" name="userAccountId" value="<c:out value="${userAccountId1}"/>"/>
			</c:otherwise>
			</c:choose>


			<c:out value="${userAccountId}"/>
			</div>
              <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="userAccountId"/></jsp:include></td>

        </tr>

		<c:choose>
		<c:when test="${(parent == null || parent.id ==0 || unlock == 1) && autoView == 0}">
        <tr valign="top" id="user2" style="display:none">
		</c:when>
		<c:otherwise>
		<tr valign="top">
      	</c:otherwise>
		</c:choose>

            <td>Send an email to the assigned user?</td>
			<%-- should be an option for checked, unchecked, disabled--%>
            <td><input name="sendEmail" value="1" type="checkbox"/></td>
        </tr>

       </c:if>

		<tr valign="top">
            <td><fmt:message key="date" bundle="${resword}"/></td>
            <td><fmt:formatDate value="${discrepancyNote.createdDate}" pattern="${dteFormat}"/></td>
        </tr>

        <tr valign="top">
            <td><fmt:message key="parent_note" bundle="${resword}"/></td>
            <td><c:choose>
             <c:when test="${parent== null || parent.description ==''}">
               <fmt:message key="none" bundle="${resword}"/>
             </c:when>
             <c:otherwise>
             <c:out value="${parent.description}"/>
             </c:otherwise>
             </c:choose>
            </td>
        </tr>
        <%-- Only show the View Parent link if the note has a parent id --%>
        <c:if test="${discrepancyNote.parentDnId > 0 && hasNotes == 'yes'}">
        <tr valign="top">
            <td colspan="2"><a href="ViewDiscrepancyNote?writeToDB=1&subjectId=<c:out value="${discrepancyNote.subjectId}"/>&itemId=<c:out value="${item.id}"/>&id=<c:out value="${discrepancyNote.entityId}"/>&name=<c:out value="${discrepancyNote.entityType}"/>&field=<c:out value="${discrepancyNote.field}"/>&column=<c:out value="${discrepancyNote.column}"/>&enterData=<c:out value="${enterData}"/>&monitor=<c:out value="${monitor}"/>&blank=<c:out value="${blank}"/>">
            <fmt:message key="view_parent_and_related_note" bundle="${resword}"/></a>
           </td>
        </tr>
       </c:if>
    </table>
    <table border="0">
    <tr>
    <c:set var= "noteEntityType" value="${discrepancyNote.entityType}"/>
      <c:if test="${enterData == '1' || canMonitor == '1' || noteEntityType != 'itemData' }">
       <td> <input type="submit" name="B1" value="<fmt:message key="submit" bundle="${resword}"/>" class="button_medium"></td>
       </c:if>


    </tr>
    </table>
</form>
</body>
</html>
