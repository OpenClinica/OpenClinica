


/* function genToolTipFromArray(flag){
   	var resStatus = new Array();
   	var detailedNotes= new Array();
   	var discrepancyType = new Array();
   	var i=0;
   	var updatedDates = new Array();
	var noSkip = false;
   	if(flag =='interviewNotes')
   	{
   	
   	<c:forEach var="discrepancyNoteBeans" items="${nameNotes}">
   		  	
   			resStatus[i]=<c:out value="${discrepancyNoteBeans.resolutionStatusId}"/>;
   			detailedNotes[i]= '<c:out value="${discrepancyNoteBeans.description}"/>';   			
			discrepancyType[i] = '<c:out value="${discrepancyNoteBeans.disType.name}"/>';
			updatedDates[i]= '<c:out value="${discrepancyNoteBeans.createdDate}"/>';
			i++;
			
		
   	</c:forEach>
   	}
 	else if(flag =='dateNotes')
   	{
   	<c:forEach var="discrepancyNoteBeans" items="${intrvDates}">
   		
   		  	
   		
   		resStatus[i]=<c:out value="${discrepancyNoteBeans.resolutionStatusId}"/>;
   		detailedNotes[i]= '<c:out value="${discrepancyNoteBeans.description}"/>';   			
		discrepancyType[i] = '<c:out value="${discrepancyNoteBeans.disType.name}"/>';
		updatedDates[i]= '<c:out value="${discrepancyNoteBeans.createdDate}"/>';
		i++;
		
		
   </c:forEach>
   	}
 	
 		  var htmlgen = 
               '<div class=\"tooltip\">'+
               '<table width="220" ><table width="200">'+
               ' <tr><td  align=\"center\" class=\"header1\">' +
               'Notes and Discrepancies </td></tr><tr></tr></table><table width="200">'+
               drawRows(i,resStatus,detailedNotes,discrepancyType,updatedDates)+
               '</table><table width="180"  class="tableborder" align="left">'+  	
	          '</table><table><tr></tr></table>'+
               '<table width="180"><tbody><td height="50" colspan="3"><span class=\"label\">'+
'Click on the flag in the main window for more details. </span>'+
'</td></tr></tbody></table></table></div>';
return htmlgen;
 }

*/
function callTip(html)
{
	Tip(html,BGCOLOR,'#FFFFE5',BORDERCOLOR,'',STICKY,true );
}
