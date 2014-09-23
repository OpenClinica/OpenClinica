<%-- 
    
	JSP with the Javascript generation for use in the showItemInput.jsp page.
	The reason this file was created was that the genToolTips java script was
	generated for each and every one of the items in the page. See also the DRY 
	priciple and OC-5819.

--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<jsp:useBean scope="request" id="section" class="org.akaza.openclinica.bean.submit.DisplaySectionBean" />
<script lang="Javascript">
<!--
function genToolTips_ItemInput(itemId){
	var resStatus = new Array();
	var detailedNotes= new Array();
	var discrepancyType = new Array();
	var updatedDates = new Array();
	var i=0;
	var discNotes = new Array();
	var title = '<fmt:message key="tooltip_title1" bundle="${resword}"/>';
	var parentDnIds = new Array();
    var totNotes = 0;
    var footNote = '<fmt:message key="footNote" bundle="${resword}"/>';
    var auditLog = '';
	<c:set var="discrepancyNotes" value="1"/>
		<c:forEach var="itemsSection" items="${section.items}">

	   			if("${itemsSection.item.id}"== itemId)
	   			{
				<c:set var="notesSize" value="${itemsSection.totNew}"/>
	   			title = "<c:out value="${itemsSection.item.name}"/>";
	   				<c:set  var="discrepancyNotes" value="${itemsSection.discrepancyNotes}"/>
	        		<c:forEach var="discrepancyNotes" items="${discrepancyNotes}">
		             resStatus[i] =<c:out value="${discrepancyNotes.resolutionStatusId}"/>;
			      	    detailedNotes[i] ="<c:out value="${discrepancyNotes.description}"/>";
			      	    discrepancyType[i] = "<c:out value="${discrepancyNotes.disType.name}"/>";
			      	    updatedDates[i] = "<c:out value="${discrepancyNotes.createdDate}"/>";
						parentDnIds[i] = "<c:out value="${discrepancyNotes.parentDnId}"/>";
			   	    i++;

			   	 	</c:forEach>
					totNotes = 	 ${notesSize};


				       if(totNotes >0) footNote = totNotes + " " + '<fmt:message key="foot_threads" bundle="${resword}"/>' + " " + '<fmt:message key="footNote_threads" bundle="${resword}"/>';
				       <%--if(totNotes >0) footNote = '<fmt:message key="footNote_threads" bundle="${resword}"/>'+ totNotes+ '<fmt:message key="foot_threads" bundle="${resword}"/>' ;--%>
                       if("${itemsSection.data.auditLog}" == "true"){
                           auditLog = '<fmt:message key="audit_exist" bundle="${resword}" />';
                       }
	   			}
	    </c:forEach>
    //including tool tips for grouped items
        <c:forEach var="group" items="${section.displayFormGroups}">
            <c:forEach var="itemsSection" items="${group.items}">

                   if("${itemsSection.item.id}"== itemId)
                   {

                <c:set var="notesSize" value="${itemsSection.totNew}"/>
                   title = "<c:out value="${itemsSection.item.name}"/>";
                       <c:set  var="discrepancyNotes" value="${itemsSection.discrepancyNotes}"/>
                    <c:forEach var="discrepancyNotes" items="${discrepancyNotes}">
                     resStatus[i] =<c:out value="${discrepancyNotes.resolutionStatusId}"/>;
                          detailedNotes[i] ="<c:out value="${discrepancyNotes.description}"/>";
                          discrepancyType[i] = "<c:out value="${discrepancyNotes.disType.name}"/>";
                          updatedDates[i] = "<c:out value="${discrepancyNotes.createdDate}"/>";
                        parentDnIds[i] = "<c:out value="${discrepancyNotes.parentDnId}"/>";
                       i++;

                        </c:forEach>
                    totNotes = 	 ${notesSize};
                          if(totNotes >0) footNote = totNotes + " " + '<fmt:message key="foot_threads" bundle="${resword}"/>' + " " + '<fmt:message key="footNote_threads" bundle="${resword}"/>';
                       if("${itemsSection.data.auditLog}" == "true"){
                           auditLog = '<fmt:message key="audit_exist" bundle="${resword}" />';
                       }
                   }

            </c:forEach>
        </c:forEach>


		  var htmlgen =
	          '<div class=\"tooltip\">'+
	          '<table  width="95%">'+
	          ' <tr><td  align=\"center\" class=\"header1\">' +title+
	          ' </td></tr><tr></tr></table><table  style="border-collapse:collapse" cellspacing="0" cellpadding="0" width="95%" >'+
	          drawRows(i,resStatus,detailedNotes,discrepancyType,updatedDates,parentDnIds)+
	          '</table><table width="95%"  class="tableborder" align="left">'+
	          '</table><table><tr></tr></table>'+
	          '<table width="95%"><tbody><td height="30" colspan="3"><span class=\"note\">'+footNote +'</span>'+
	          '</td></tr>' +
              '<tr><td align=\"center\">'+ auditLog +'</td></tr>' +
              '</tbody></table></table></div>';
		  return htmlgen;
	}

function replaceSwitch(eventCRFId,itemId,id,attribute,str1,str2,filename,pathAndName,status) {
	var rp = document.getElementById(id+itemId);
	var div = document.getElementById('div'+itemId);
	var a = document.getElementById('a'+itemId);
	var ft = document.getElementById('ft'+itemId);
	var up = document.getElementById('up'+itemId);
	var uploadLink = 'UploadFile?submitted=no&itemId=' + itemId;
	var downloadLink = 'DownloadAttachedFile?eventCRFId=' + eventCRFId + '&fileName=' + pathAndName;
	if(rp.getAttribute('value')=="<fmt:message key="replace" bundle="${resword}"/>") {
		if(a) {
			div.appendChild(a);
			div.removeChild(a);
		}
		if(!ft) {
			var new_ft = document.createElement('input');
			new_ft.setAttribute("id","ft"+itemId);
			new_ft.setAttribute("type","text");
			new_ft.setAttribute("name","fileText"+itemId);
			new_ft.setAttribute("disabled","disabled");
			div.appendChild(new_ft);
			var new_up = document.createElement('input');
			new_up.setAttribute("id", "up"+itemId);
			new_up.setAttribute("type", "button");
			new_up.setAttribute("name", "uploadFile"+itemId);
			new_up.setAttribute("value", '<fmt:message key="click_to_upload" bundle="${resword}"/>');
			new_up.onclick = function(){var itemid=itemId; javascript:openDocWindow(uploadLink)};
			div.appendChild(new_up);
		}
		var fa = document.getElementById('fa'+itemId);
		fa.setAttribute("value","upload");
		div.appendChild(fa);
		switchStr(itemId,"rm","value",'<fmt:message key="cancel_remove" bundle="${resword}"/>','<fmt:message key="remove" bundle="${resword}"/>');
	} else if(rp.getAttribute('value')=="<fmt:message key="cancel_replace" bundle="${resword}"/>") {
		if(ft) {
			div.appendChild(ft);
			div.appendChild(up);
			div.removeChild(ft);
			div.removeChild(up);
		}
		if(!a) {
			if(status=='found') {
				var new_a = document.createElement('a');
				new_a.href = downloadLink;
				new_a.setAttribute("id","a"+itemId);
				new_a.appendChild(document.createTextNode(filename));
				div.appendChild(new_a);
			} else if(status=='notFound') {
				var new_a = document.createElement('del');
				new_a.setAttribute("id","a"+itemId);
				new_a.innerHTML = filename;
				div.appendChild(new_a);
			}
		}
		var fa = document.getElementById('fa'+itemId);
		fa.setAttribute("value","noAction");
		div.appendChild(fa);
	}
	switchAttribute(itemId,id,attribute,str1,str2);
}

function removeSwitch(eventCRFId,itemId,id,attribute,str1,str2,filename,pathAndName,status) {
	var rm = document.getElementById(id+itemId);
	var div = document.getElementById('div'+itemId);
	var a = document.getElementById('a'+itemId);
	var ft = document.getElementById('ft'+itemId);
	var up = document.getElementById('up'+itemId);
	var input = document.getElementById('input'+itemId);
	var downloadLink = 'DownloadAttachedFile?eventCRFId=' + eventCRFId + '&fileName=' + pathAndName;
	if(rm.getAttribute('value')=='<fmt:message key="remove" bundle="${resword}"/>') {
		input.setAttribute("value","");
		if(a) {
			div.appendChild(a);
			div.removeChild(a);
		}
		if(ft) {
			div.appendChild(ft);
			div.appendChild(up);
			div.removeChild(ft);
			div.removeChild(up);
			switchStr(itemId,"rp","value",'<fmt:message key="cancel_replace" bundle="${resword}"/>','<fmt:message key="replace" bundle="${resword}"/>');
		}
		var new_a = document.createElement('del');
		new_a.setAttribute("id","a"+itemId);
		if(navigator.appName=="Microsoft Internet Explorer") {
			new_a.style.setAttribute("color","red");
		} else {
			new_a.setAttribute("style","color:red");
		}
		new_a.innerHTML = filename;
		div.appendChild(new_a);
		var fa = document.getElementById('fa'+itemId);
		fa.setAttribute("value","erase");
		div.appendChild(fa);
	} else if(rm.getAttribute('value')=='<fmt:message key="cancel_remove" bundle="${resword}"/>') {
		input.setAttribute("value",filename);
		if(a) {
			div.appendChild(a);
			div.removeChild(a);
			if(status=='found') {
				var new_a = document.createElement('a');
				new_a.href = downloadLink;
				new_a.setAttribute("id","a"+itemId);
				new_a.appendChild(document.createTextNode(filename));
				div.appendChild(new_a);
			} else if(status=='notFound') {
				var new_a = document.createElement('del');
				new_a.setAttribute("id","a"+itemId);
				new_a.innerHTML = filename;
				div.appendChild(new_a);
			}
		}
		var fa = document.getElementById('fa'+itemId);
		fa.setAttribute("value","noAction");
		div.appendChild(fa);
	}

	switchAttribute(itemId,id,attribute,str1,str2);
}

function switchAttribute(itemId, id, attribute, str1, str2) {
	var e = document.getElementById(id+itemId);
	if(e.getAttribute(attribute)==str1) {
		e.setAttribute(attribute,str2);
	}else if(e.getAttribute(attribute)==str2) {
		e.setAttribute(attribute,str1);
	}
}

function switchStr(itemId, id,attribute,str1,str2) {
	var e = document.getElementById(id+itemId);
	if(e.getAttribute(attribute)==str1) {
		e.setAttribute(attribute,str2);
	}
}

function conditionalShow(strLeftNavRowElementName){
    var objLeftNavRowElement;
    var toShow = "false";

    objLeftNavRowElement = MM_findObj("t"+strLeftNavRowElementName);
    if (objLeftNavRowElement != null) {
        if (objLeftNavRowElement.style) { objLeftNavRowElement = objLeftNavRowElement.style; }
		if (objLeftNavRowElement.display == "none") {
			objLeftNavRowElement.display = "";	toShow = "true";
			showRow(strLeftNavRowElementName);
		}
    }
    if(toShow == "true") {
	    var objLeftNavRowElement1 = MM_findObj("hd"+strLeftNavRowElementName);
	    if (objLeftNavRowElement1 != null) {
	        if (objLeftNavRowElement1.style) { objLeftNavRowElement1 = objLeftNavRowElement1.style; }
			if (objLeftNavRowElement1.display == "none") { objLeftNavRowElement1.display = "";}
	    }
	    var objLeftNavRowElement2 = MM_findObj("sub"+strLeftNavRowElementName);
	    if (objLeftNavRowElement2 != null) {
	        if (objLeftNavRowElement2.style) { objLeftNavRowElement2 = objLeftNavRowElement2.style; }
			if (objLeftNavRowElement2.display == "none") { objLeftNavRowElement2.display = "";}
	    }
    }
}

function conditionalHide(strLeftNavRowElementName){
    var objLeftNavRowElement;
    var toHide = "true";

    objLeftNavRowElement = MM_findObj("t"+strLeftNavRowElementName);
    if (objLeftNavRowElement != null) {
	    var obj = MM_findObj("ft"+strLeftNavRowElementName);
	    if(obj != null) { if(obj.value != "") { toHide = "false";  }
	    } else {
		    obj = MM_findObj("a"+strLeftNavRowElementName);
		    if(obj != null) { if(obj.value != "") { toHide = "false";  }
	    	} else {
			    obj = MM_findObj("input"+strLeftNavRowElementName);
				var type = obj.type;
				if(obj.value != "" && (type=="textarea" || type=="text" || type=="select-one" || type=="select-multiple")) { toHide = "false";
				}else if(obj.length > 0) { for(var i=0; i<obj.length; ++i) { if(obj[i].checked && obj[i].value != "") { toHide = "false"; break;}}}
	    	}
    	}
		if(toHide == "true") {
	        if (objLeftNavRowElement.style) { objLeftNavRowElement = objLeftNavRowElement.style; }
	        if (objLeftNavRowElement.display == "none") { toHide = "false";
        	} else { objLeftNavRowElement.display = "none";		hideRow(strLeftNavRowElementName); }
    	}
    }
    if(toHide == "true") {
	    var objLeftNavRowElement1 = MM_findObj("hd"+strLeftNavRowElementName);
	    if (objLeftNavRowElement1 != null) {
	        if (objLeftNavRowElement1.style) { objLeftNavRowElement1 = objLeftNavRowElement1.style; }
	        objLeftNavRowElement1.display = "none";
	    }
	    var objLeftNavRowElement2 = MM_findObj("sub"+strLeftNavRowElementName);
	    if (objLeftNavRowElement2 != null) {
	        if (objLeftNavRowElement2.style) { objLeftNavRowElement2 = objLeftNavRowElement2.style; }
	        objLeftNavRowElement2.display = "none";
	    }
	}
}

function selectControlShow(element,scdPairStr) {
	var showIds = [];
	var n = 0;
	var m = 0;
	var hideIds = [];
	var arr = scdPairStr.split('-----');
	for(var j=1; j<arr.length; j+=2) {
		hideIds[m] = arr[j];
	    for(var i = 0; i < element.options.length; i++){
			if(element.options[i].selected) {
		        if(element.options[i].value==arr[j+1]){
			        showIds[n] = arr[j];
			        hideIds[m] = -1;
			        ++n;
	        	}
	        }
	    }
	    ++m;
	}
	for(var i=0; i<showIds.length; ++i) {
		conditionalShow(showIds[i]);
	}
	for(var i=0; i<hideIds.length; ++i) {
		if(hideIds[i] != -1) {
			conditionalHide(hideIds[i]);
		}
	}
}

function checkControlShow(element,scdPairStr) {
	var arr = scdPairStr.split('-----');
	for(var j=1; j<arr.length; j+=2) {
		if(element.value==arr[j+1]) {
			if(element.checked) {
				conditionalShow(arr[j]);
	        } else {
		      	conditionalHide(arr[j]);
	        }
	    }
	}
}

function radioControlShow(element,scdPairStr) {
	var m = 0;
	var hideIds = [];
	var arr = scdPairStr.split('-----');
	for(var j=1; j<arr.length; j+=2) {
		hideIds[m] = arr[j];
		if(element.value==arr[j+1]) {
			if(element.checked) {
				conditionalShow(arr[j]);
			    hideIds[m] = -1;
	        }
	    }
	    ++m;
	}
	for(var i=0; i<hideIds.length; ++i) {
		if(hideIds[i] != -1) {
			conditionalHide(hideIds[i]);
		}
	}
}

function showRow(itemId) {
	var objCol = MM_findObj("col" + itemId);
	if(objCol != null) {
		var numOfTr = objCol.value;
		var objIDs = MM_findObj("rowSCDShowIDs" + numOfTr); 	var ids = objIDs.value;
		if(ids.length > 1) {
			if(ids.indexOf("-"+itemId+"-") == -1) { ids = ids + itemId + "-"; objIDs.value = ids;}
		} else { ids = "-" + itemId + "-";	objIDs.value = ids;		}
		var objTr = MM_findObj("tr"+numOfTr);
		if(objTr != null && objTr.style.display == "none") { objTr.style.display = "";	}
	}
}

function hideRow(itemId) {
	var objCol = MM_findObj("col" + itemId);
	if(objCol != null) {
		var numOfTr = objCol.value;
		var objIDs = MM_findObj("rowSCDShowIDs" + numOfTr); 	var ids = objIDs.value;
		if(ids.length > 1) {
			if(ids.indexOf("-"+itemId+"-") != -1) { ids = ids.replace(itemId + "-", ""); objIDs.value = ids;	}
		}
		if(ids.length <= 1) {
			var objTr = MM_findObj("tr"+numOfTr);
			if(objTr != null) { objTr.style.display = "none";	}
		}
	}
}
//-->
</script>