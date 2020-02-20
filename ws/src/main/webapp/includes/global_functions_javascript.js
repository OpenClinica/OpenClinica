function selectAllChecks(formObj,value){
    if(formObj) {
        var allChecks = formObj.getElementsByTagName("input");
        for(var i = 0; i < allChecks.length; i++){
            if(allChecks[i] && allChecks[i].getAttribute &&
               allChecks[i].getAttribute("type") &&
               allChecks[i].getAttribute("type").indexOf("checkbox") != -1  ) {
               /* if(! allChecks[i].checked==true){
                    allChecks[i].checked=true;
                } */
                allChecks[i].checked = value;
            }
        }
    } else {
        //alert(formObj)
    }
}


function hideCols(tableId,columnNumArray,showTable){
    var theStyle;
    if (showTable) {
        theStyle = '';
        if(document.getElementById('showMore') && document.getElementById('hide')){
            document.getElementById('showMore').style.display='none';
            document.getElementById('hide').style.display='';
        }
    }
    else  {
        theStyle = 'none';
        if(document.getElementById('showMore') && document.getElementById('hide')){
            document.getElementById('showMore').style.display='';
            document.getElementById('hide').style.display='none';
        }
    }
    //alert("found theStyle: "+ theStyle);

    var tbl  = document.getElementById(tableId);
    var tbodys = tbl.getElementsByTagName('tbody');
    //alert("found tbodys: " + tbodys);
    var _tbody = tbodys[0];
    for(var i = 0; i < tbodys.length;i++){
        if(tbodys[i].className &&
           tbodys[i].className.indexOf('tbody') != -1) {
            _tbody=tbodys[i];
        }
    }
    //alert("found tbody: " + _tbody);
    var rows = _tbody.getElementsByTagName('tr');
    //alert("found rows: " + rows);
    var theads = tbl.getElementsByTagName('thead');
    //alert("found theads: " + theads);
    var thead = theads[0];
    //alert("found thead: " + thead);
    var theadRows = thead.getElementsByTagName('tr')

	//alert("found thead rows: "+theadRows):
    for (var th=0; th<theadRows.length;th++) {
		//alert("iterating through loop th: "+th);
		//alert("found class: " + theadRows[th].getAttribute('class'));
		//alert("is it the same as?: " + theadRows[th].className);
		
		if(theadRows[th].className &&
		  (theadRows[th].className.indexOf('filter') != -1 ||
			theadRows[th].className.indexOf('header') != -1)) {
			var headCels = theadRows[th].getElementsByTagName('td');
			for(var k=0; k < headCels.length; k++){
				for(var x=0; x<columnNumArray.length;x++)  {

					if(columnNumArray[x] == k){
						
						headCels[k].style.display=theStyle;
						//alert("set " + columnNumArray[x] + " with the style: " + theStyle);
					}
				}
			}
		}
    }
    //alert("found rows length: "+rows.length);
    for (var row=0; row<rows.length;row++) {
        var cels = rows[row].getElementsByTagName('td');
        //alert("found cels length: "+cels.length);

        var tableRowIds = rows[row].getAttribute('id');
        if ( tableRowIds != null && tableRowIds.match(tableId + "_row") != null ){
        	for(var j=0; j < cels.length; j++){
        		for(var x=0; x<columnNumArray.length;x++)  {
        			if(columnNumArray[x]  == j){
        				cels[j].style.display=theStyle;
        				//alert("set "+j+" with the style: " + theStyle);
        			}
        		}
        	}
        }
    }

}

function toggleName(str){

    if(str.innerHTML == 'Show'){
        str.innerHTML='Hide';
    }  else {
        str.innerHTML='Show';
    }
}
function changeOpenDivButton(imgObject) {

    if(imgObject && imgObject.src.indexOf('sidebar_collapse.gif') != -1){

        $(imgObject.parentNode.nextSibling).hide();
        imgObject.src='../images/sidebar_expand.gif';
    } else if (imgObject)

    {imgObject.src='../images/sidebar_collapse.gif';
        $(imgObject.parentNode.nextSibling).show();}
}
function myCancel(objButtonText) {

    var cancelButton=document.getElementById('cancel');

    if ( cancelButton != null) {
        if(confirm(objButtonText)) {
            window.location.href="ListStudy";
            return true;
        } else {
            return false;
        }
    }
    return true;

}
/*
 This method is called as part of a window.onload handler. It places the focus
 on the first element in a CRF's data-entry form. SEE THE REPETITION-MODEL.JS FILE,
 BEGINNING LINE 1466 FOR WHERE THIS METHOD IS CALLED. The
 REPETITION-MODEL.JS file gets a handle to the window.load event handler,
 so the actual JSP page's body.onload handler cannot be called.
 */
function giveFirstElementFocus(){

    var frm = document.getElementById("mainForm");
    if(frm == null){
        return;
    }
    var selects = frm.getElementsByTagName("SELECT");
    var textareas = frm.getElementsByTagName("TEXTAREA");

    var firstField = document.getElementById("formFirstField");
    var fieldId;
    if(firstField){
        fieldId = firstField.value;
    }
    if(selects) {
        for(var i = 0; i <=selects.length;i++) {
            if(selects[i] &&
               selects[i].id && selects[i].id.indexOf(fieldId) != -1) {
                selects[i].focus();
                return;
            }

        }
    }
    var inputs = frm.getElementsByTagName("INPUT");

    if(inputs){
        for(var j = 0; j <=inputs.length;j++) {
            if(inputs[j] &&
               inputs[j].id &&
               (inputs[j].getAttribute("type")) &&
               (inputs[j].getAttribute("type").indexOf("hidden") == -1)){

                if(inputs[j].id.indexOf(fieldId) != -1) {
                    inputs[j].focus();
                    return;
                }
            }

        }
    }

    if(textareas){
        for(var h = 0; h <=textareas.length;h++) {
            if(textareas[h] &&
               textareas[h].id && textareas[h].id.indexOf(fieldId) != -1) {
                textareas[h].focus();
                return;
            }

        }
    }

}
/**
 * Display a sequence of three tabs (implemented as TD elements) in a JSP view;
 * by selecting only the tabs or TD elements that have a certain class name.
 * @param tabNumber The numbered position of the currently selected tab, like 3
 * @param totalNumberOfTabs  The total number of tabs or TD elements in the collection.
 * @param tabClassName The name of the class that specifies the tab.
 */
function selectTabs(tabNumber,totalNumberOfTabs,tabClassName) {

    if((! tabNumber) || tabNumber < 1){
        tabNumber = 1;
    }
    if((! tabClassName) || tabClassName == ""){
        tabClassName = "crfHeaderTabs";
    }
    if(totalNumberOfTabs == null || totalNumberOfTabs == 0) return;
    var param = 'TD.'+tabClassName;
    //in terms of array element selection, set tabNumber to tabNumber - 1
    tabNumber = tabNumber - 1;
    var firstTabSelected = tabNumber == 0; //the first three tabs should be selected
    //the last three tabs should be selected
    var lastTabSelected = (tabNumber == (totalNumberOfTabs - 1));
    //fetches all TD elements with a class name of tabClassName
    var allTabs = $$(param);
    var tdCount = allTabs.length;
    //If there are not more than two tabs, just one or two, then all we have to do is
    //select one or two TD elements, and not worry about deselecting others
    var moreThanTwoTabs = (tdCount > 2);

    if(moreThanTwoTabs && firstTabSelected) {
        //"true" means select the first three of the collection
        selectThreeTabs(allTabs,true);
        return;
    }

    if(moreThanTwoTabs && lastTabSelected) {
        //"false" means select the last three of the collection
        selectThreeTabs(allTabs,false);
        return;
    }

    //if there are just 1 to 2 tabs, select the chosen one, and make sure the
    //other tab is displayed
    if(! moreThanTwoTabs) {
        for(var i = 0; i < tdCount; i++) {
            allTabs[i].style.display = "";

        }
        return;
    }
    //select the TD with position tabNumber, and any sibling TDs
    //before and after it, if tab numbers > 2

    if(moreThanTwoTabs) {
        if(! allTabs[tabNumber]){ return;}

        allTabs[tabNumber].style.display = "";

        //cycle through all the tabs; the first sibling of the selected tab
        //will have number tabNumber - 1, the next sibling with have
        //tabNumber + 1; all others should be display = "none"
        for(var i = 0; i < tdCount; i++) {
            if(i == tabNumber)  continue;  //already displayed

            if(i == tabNumber - 1 || i == tabNumber + 1){ //prev sibling or next sibling

                allTabs[i].style.display = "";

            } else {
                allTabs[i].style.display = "none";

            }
        }

    }   // if(tDcount > 2)
}
/*
 true in the second parameter means select the first three tabs; otherwise select the last three
 */
function selectThreeTabs(arrayOfTDElements,firstOrLastThree){
    if(firstOrLastThree){
        for(var i = 0; i < arrayOfTDElements.length; i++){
            if(i >= 0 && i <= 2) {

                arrayOfTDElements[i].style.display="";

            } else {

                arrayOfTDElements[i].style.display="none";

            }
        }
    }//true is select first three
    else {  //select the last three elements
        for(var j = arrayOfTDElements.length-1; j >= 0; j--){
            if(j <= arrayOfTDElements.length-1 && j >= arrayOfTDElements.length-3) {

                arrayOfTDElements[j].style.display="";

            } else {

                arrayOfTDElements[j].style.display="none";

            }

        }//for var j

    }
}
function setNewIconInParentWin(idOfImageElement, imageLocation){

    var imgObject = window.opener.document.getElementById(idOfImageElement);
    //alert(idOfImageElement)
    if(imgObject) {
        imgObject.src = imageLocation;
    }
    //disable further clicking
    imgObject.title = 'Refresh or re-enter this form to view any new discrepancy notes.';
    imgObject.parentNode.onclick=function(){
        return false;
    }
}
function showSummaryBox(divObject,parentLinkObj,showText,hideText){
    //var sumBox = $(divObject);
    var sumBox = document.getElementById(divObject);
    if(sumBox && sumBox.style.display == "none") {

//        sumBox.show();
        sumBox.style.display = "block"
        parentLinkObj.innerHTML= '<img name="ExpandGroup1" src="images/bt_Collapse.gif" border="0"> ' + hideText;
    }  else {
        if(sumBox){
            sumBox.style.display = "none"
//            sumBox.hide();
            parentLinkObj.innerHTML= '<img name="ExpandGroup1" src="images/bt_Expand.gif" border="0"> ' + showText;

        }
    }
}

function getSib(theSibling){
    var sib;
    do {
        sib  = theSibling.previousSibling;
        if(sib.nodeType != 1){
            theSibling = sib;
        }
    } while(! (sib.nodeType == 1))

    return sib;
}
/* Return true, if the browser used is IE6 or IE7. */
function detectIEWindows(userAgentString) {
    return ((userAgentString.indexOf("MSIE") != -1) &&
            (userAgentString.indexOf("Windows") != -1) &&
            (userAgentString.indexOf("compatible") != -1));
}
/* Return true, if the browser used is Firefox on Windows. */
function detectFirefoxWindows(userAgentString) {
    return ((userAgentString.indexOf("Mozilla") != -1) &&
            (userAgentString.indexOf("Windows") != -1) &&
            (userAgentString.indexOf("Firefox") != -1));
}
/*change a button to a new CSS class if the button is in a disabled state.
 THIS METHOD IS USED BY LINES 306 AND 1221 in the file repetition-model.js*/
function changeBtnDisabledState(buttonObj,cssOnStateClass,
                                cssDisabledClass,onState){
    if(buttonObj == null || buttonObj == undefined) return;
    if(cssOnStateClass == null || cssOnStateClass == undefined) return;
    if(cssDisabledClass == null || cssDisabledClass == undefined) return;

    if(buttonObj && buttonObj.removeClassName && buttonObj.addClassName &&
       buttonObj.disabled && ! onState)  {
        buttonObj.removeClassName(cssOnStateClass);
        buttonObj.addClassName(cssDisabledClass);
    }
    if(buttonObj && buttonObj.removeClassName && buttonObj.addClassName && (! buttonObj.disabled) && onState)  {
        buttonObj.removeClassName(cssDisabledClass);
        buttonObj.addClassName(cssOnStateClass);
    }
}

/*A METHOD CALLED BY THE WEB 2.0 FORMS JS LIBRARY, LINE 942.
 The method clears all the values in a new row added by this library.  All of the
 inputs values are set to empty Strings, or checked inputs are unchecked. */
function clearInputElementValues(trElement) {
    if(! trElement)  { return; }

    var tdElements = trElement.getElementsByTagName('td');


    //variables representing all inputs, selects, textareas, and options
    //in the new row
    var inputs;
    var selects;
    var textareas;
    var options;
    var myDiv;
    var myId="";
    if(tdElements){
        for(var i = 0; i < tdElements.length; i++) {
            if(tdElements[i]) {
			    var rp=-1;
			    var rm=-1;
			    var myDivEls="";
                inputs = tdElements[i].getElementsByTagName('input');
                selects= tdElements[i].getElementsByTagName('select');

                textareas = tdElements[i].getElementsByTagName('textarea');
                //for file datatype, please reference to showGroupItemInput.jsp 
                myDiv = tdElements[i].getElementsByTagName('div');
                if(myDiv) {
	                //for file datatype, which only have one <div> with id as "div+inputname"
	                if(myDiv[0] && myDiv[0].getAttribute("id").startsWith("div")) {
		            	myId = myDiv[0].getAttribute("id").substring(3);
		            	myDivEls = myDiv[0].getElementsByTagName('a');
		            	if(myDivEls.length<=0) {
			            	myDivEls = myDiv[0].getElementsByTagName('del');
		            	}
	           		}
            	}
                if(inputs) {
                    for(var j = 0; j < inputs.length; j++){
                        if(inputs[j]){
                            if(inputs[j].getAttribute("type") &&
                               (inputs[j].getAttribute("type").indexOf("checkbox") != -1 ||
                                inputs[j].getAttribute("type").indexOf("radio") != 1)){
                                inputs[j].removeAttribute("checked");
                                inputs[j].checked=false;
                            }
                            if(inputs[j].getAttribute("type") &&
                               inputs[j].getAttribute("type").indexOf("text") != -1) {
                                inputs[j].setAttribute("value","");
                            }
                            //remove two buttons, Replace, Remove, for File datatype.if(inputs[j].getAttribute("type") &&
                           if(inputs[j].getAttribute("type") &&
                               inputs[j].getAttribute("type").indexOf("button") != -1 &&
                               inputs[j].getAttribute("id") == "rp"+myId) {
	                               rp = j;
                           }
                           if(inputs[j].getAttribute("type") &&
                               inputs[j].getAttribute("type").indexOf("button") != -1 &&
                               inputs[j].getAttribute("id") == "rm"+myId) {
	                               rm = j;
                           }
                           if(inputs[j].getAttribute("type") &&
                               inputs[j].getAttribute("type").indexOf("hidden") != -1 &&
                               inputs[j].getAttribute("id") == "hidft"+myId) {
		                           inputs[j].setAttribute("id", "ft"+myId);
		                           try {
			                           inputs[j].setAttribute("type", "text");
	                               } catch (e) {
		                               var newElement = null;
		                               var nameStr = inputs[j].getAttribute("name");
		                               try {
			                           		newElement = document.createElement("<input type=\"text\" id=\"ft" + myId 
			                           		+ "\" name=\"" + nameStr + "\" disabled=\"disabled\">");
		                               }catch(e){}
		                               inputs[j].parentNode.replaceChild(newElement,inputs[j]);
	                           	   }
                           }
                           if(inputs[j].getAttribute("type") &&
                               inputs[j].getAttribute("type").indexOf("hidden") != -1 &&
                               inputs[j].getAttribute("id") == "hidup"+myId) {
	                               inputs[j].setAttribute("id", "up"+myId);
	                               try {
	                               		inputs[j].setAttribute("type", "button");
                               	   } catch (e) {
	                               	   var newElement = null;
	                               	   var nameStr = inputs[j].getAttribute("name");
	                               	   var valueStr = inputs[j].getAttribute("value");
		                               try {
			                           		newElement = document.createElement("<input type=\"button\" id=\"up\"" + myId 
			                           		+ "\" name=\"" + nameStr + "\" value=\"" + valueStr + "\">");
			                           		newElement.onclick = inputs[j].onclick;
		                               }catch(e){}
		                               inputs[j].parentNode.replaceChild(newElement,inputs[j]);
                               	   }
                           }
                        }
                    }
                }//end if inputs
                
                if(rp>=0) {
                	tdElements[i].removeChild(inputs[rm]);
                	tdElements[i].removeChild(inputs[rp]);
                	if(myDivEls[0]) {
	                	myDiv[0].removeChild(myDivEls[0]);
                	}
            	}
                /* select element behavior removed for 2791: */

                if(selects) {
                    for(var h = 0; h < selects.length; h++){
                        if(selects[h]){
                            options = selects[h].getElementsByTagName("option");
                            if(options){
                                if(! detectIEWindows(navigator.userAgent)){
                                    for(var k = 0; k < options.length; k++){
                                        if(options[k]) {
                                            options[k].selected=false;
                                        }

                                    }
                                }
                                // for IE6/7
                                if(detectIEWindows(navigator.userAgent)){
                                    selects[h].selectedIndex=0;
                                    /* var opt;
                                     for(var p = 0; p < options.length; p++){
                                     opt=document.createElement("option");
                                     opt.selected=false;
                                     opt.setAttribute("value",options[p].getAttribute("value"));
                                     opt.innerHTML=options[p].innerHTML;


                                     //  $(options[p]).remove();
                                     selects[h].removeChild(options[p]);
                                     selects[h].appendChild(opt);
                                     }*/
                                }
                            }
                        }
                    }

                }//end if selects
                if(textareas) {
                    for(var m = 0; m < textareas.length; m++){
                        if(textareas[m]) {
                            textareas[m].innerHTML="";
                        }
                    }
                }
            }//end   if(tdElements[i])
        }//end for
    }//end if (tdElements)
}//end function

/*A METHOD CALLED BY THE WEB 2.0 FORMS JS LIBRARY, AFTER LINE 942.
 BWP: 08/21/2008; The method sets a discrepancy note icon to a certain image, to prevent the copying of
 icons that represent created notes in prior rows. */
function changeDNoteIcon(trElement) {

    if(! trElement)  { return; }
    var tdElements = trElement.getElementsByTagName('td');

    var hrefElements;

    if(tdElements) {
        for(var i =0; i < tdElements.length; i++)  {
            hrefElements = tdElements[i].getElementsByTagName('a');
            if(hrefElements) {
                for(var j =0; j < hrefElements.length; j++)  {
                    if(hrefElements[j].childNodes){
                        for(var h = 0; h < hrefElements[j].childNodes.length; h++){
                            checkImgIcon(hrefElements[j].childNodes[h]);
                        }
                    }
                }
            }
        }
    }


}

function checkImgIcon(imgObject) {

    if(! imgObject) {
        return;
    }
    // alert(imgObject.src)
    if(imgObject.src && (imgObject.src.indexOf("images/icon_Note.gif") != -1)) {

        imgObject.src = "images/icon_noNote.gif";
    }
}
/* Check radio buttons or checkboxes if the browser is IE6 or IE7, and
 the input element's 'checked' attribute equals 'checked''. */
/*function enableDefaultCheckedElements(formObj){
 if(formObj == null || formObj == undefined) { return; }
 var radiosAndChecks = $(formObj).getElementsBySelector('input[type="radio"]','input[type="checkbox"]');
 for(var i = 0; i <= radiosAndChecks.length; i++) {
 if(radiosAndChecks[i] == null || radiosAndChecks[i] == undefined){
 continue;
 }
 var chked = radiosAndChecks[i].getAttribute("checked");
 //IE6 interprets the chked return value as a boolean; FF as a String (correctly)
 //like "checked"  -- "checked".indexOf(chked) != -1
 if(chked) {
 radiosAndChecks[i].defaultChecked=true;
 }
 }
 }*/
/* Dynamically add a new parameter to a form. */
function submitWithNewParam(formElement,paramName,paramValue) {
    if(formElement == null || formElement == undefined) { return; }
    var hiddenElement = document.createElement("input");
    hiddenElement.setAttribute("type","hidden");
    hiddenElement.setAttribute("name",paramName);
    hiddenElement.setAttribute("value",paramValue);
    formElement.appendChild(hiddenElement);
    formElement.submit();

}

/* show or hide using prototype */
function show(objId){
    $(objId).show();
}
function hide(objId){
    $(objId).hide();
}
/* Taking care of IE6 bug vis a vis the repetition model JavaScript library. If
 a radio button is clicked, it's sibling radios are unchecked.  The radioObject parameter is the
 radio input element DOM object;  the configuration refers to the Strings "vertical"  or
 "horizontal".  If the radio buttons have a horizontal configuration, then they are each locate din a different
 TD tag, and the JavaScript has to iterate the DOM differently in order to uncheck the right radio button.
 */
function unCheckSiblings(radioObject,
                         configuration){
    var allSibs;
    if(configuration == null || configuration == undefined) { return;}
    if(radioObject == null || radioObject == undefined) { return;}

    if(configuration.indexOf('horizontal') == -1)  {

        var nextSib = radioObject.nextSibling;
        var preSib = radioObject.previousSibling;
        do{
            unCheckObject(nextSib);
            if(nextSib) {
                nextSib = nextSib.nextSibling;
            }
        }  while(nextSib)

        do{
            unCheckObject(preSib);
            if(preSib) {
                preSib = preSib.previousSibling;
            }
        }  while(preSib)
        /*
         for(var i = 0; i < allSibs.length; i++){

         if(allSibs[i] && allSibs[i].tagName &&
         allSibs[i].tagName.indexOf("INPUT") != -1 &&
         allSibs[i].getAttribute('type').indexOf('radio') != -1){
         allSibs[i].checked=false;
         }
         }*/
    } else {
        var name = radioObject.getAttribute("name");
        //Get radio elements in adjacent TD cells that have the same name
        //then uncheck them
        var allTDs = $(radioObject).up().siblings();
        var _elements;
        if(allTDs)  {
            for(var j = 0; j < allTDs.length; j++){
                if(allTDs[j])   {
                    if($(allTDs[j]).childElements)  {
                        _elements=$(allTDs[j]).childElements();
                    } else {
                        continue;
                    }
                    if(_elements)  {
                        for(var k = 0; k < _elements.length; k++){
                            if(_elements[k] && _elements[k].tagName.indexOf("INPUT") != -1 &&
                               _elements[k].getAttribute('type').indexOf('radio') != -1 &&
                               _elements[k].getAttribute('name') &&
                               _elements[k].getAttribute('name').indexOf(name) != -1){
                                _elements[k].checked=false;
                            }
                        }
                    }
                }  //for j=0
            }
        }//outer if

    }

}

function unCheckObject(radioObject) {
    if(radioObject && radioObject.tagName &&
       radioObject.tagName.indexOf("INPUT") != -1 &&
       radioObject.getAttribute('type').indexOf('radio') != -1){
        radioObject.checked=false;
    }

}
function isCheckedRadioOrCheckbox(inputObject){
    if(inputObject == null || inputObject == undefined)  { return false; }
    var typ=inputObject.getAttribute('type');
    if(typ != null && (typ.indexOf('checkbox') != -1 ||
                       typ.indexOf('radio') != -1)){

        return inputObject.checked;

    }

    return false;
}
/* Only display the confirm dialogue box if the checkbox was checked
 when the user clicked it; then uncheck the checkbox if the user chooses "cancel"
 in the confirm dialogue. */
function displayMessageFromCheckbox(checkboxObject){
    if(checkboxObject != null && checkboxObject.checked){
        var bool =  confirm(
                "Marking this CRF complete will finalize data entry. You will be allowed to edit the data later but this data entry stage is completed. If Double Data Entry is required, you or another user may need to complete this CRF again before it is verified as complete. Are you sure you want to mark this CRF complete?");
        if(! bool) { checkboxObject.checked=false; }
    }
}

function popUp(strFileName, strTarget) {
    window.open(strFileName, strTarget, 'menubar=yes,toolbar=no,scrollbars=yes,resizable,width=700,height=450,screenX=0,screenY=0');
}

function newImage(arg) {
    if (document.images) {
        rslt = new Image();
        rslt.src = arg;
        return rslt;
    }
}

function changeImages() {
    if (document.images && (preloadFlag == true)) {
        for (var i=0; i<changeImages.arguments.length; i+=2) {
            document[changeImages.arguments[i]].src = changeImages.arguments[i+1];
        }
    }
}
var preloadFlag = false;
function preloadImages() {
    if (document.images) {
        bt_GO_h = newImage("/images/bt_GO_d.gif");
        preloadFlag = true;

    }
}



function MM_jumpMenu(targ,selObj,restore){ //v3.0
    eval(targ+".location='"+selObj.options[selObj.selectedIndex].value+"'");
    if (restore) selObj.selectedIndex=0;
}


/* Specifies the period of time between updates:
 month - once a month
 date - once per every day of the month (repeats the next month)
 weekday - once per every day of the week (repeats the next week)
 hour - once per hour (repeats the next day)
 request - once per browser request (default)
 */

var updatePeriods = new Array("month","date","weekday","hour","request")

// Invoked to display rotated HTML content in a Web page. The period
// argument should be an element of the updatePeriods array.

function displayRotatedContent(period) {
    var updatePeriod = -1
    for(var i=0;i<content.length;++i) {
        if(period.toLowerCase() == updatePeriods[i].toLowerCase()) {
            updatePeriod = i
            break
        }
    }
    var s = selectHTML(updatePeriod)
    document.write(s)
}

function selectHTML(updatePeriod) {
    var n = 0
    var max = content.length
    var d = new Date()
    switch(updatePeriod) {
        case 0: // Month (0 - 11)
            n = d.getMonth()
            break
        case 1: // Date (1 - 31 scaled to 0 - 30)
            n = d.getDate() - 1
            break
        case 2: // Weekday (0 - 6)
            n = d.getDay()
            break
        case 3: // Hour (0 - 23)
            n = d.getHours()
            break
        case 4: // Request (Default)
        default:
            n = selectRandom(max)
    }
    n %= max
    return content[n]
}

// Select a random integer that is between 0 (inclusive) and max (exclusive)
function selectRandom(max) {
    var r = Math.random()
    r *= max
    r = parseInt(r)
    if(isNaN(r)) r = 0
    else r %= max
    return r
}

//parts added 12-18-03, tbh
function confirmSaveAndContinue () {

    var yesno = confirm("Your data will now be saved to the database. This may take a minute or two; \nplease be patient and do not attempt to reload or make changes to the page. \nClick 'OK' to continue or 'Cancel' to return to the page without saving.","");
    //if (yesno == true) { alert ("OK was chosen"); } else { alert("Cancel was chosen"); }
    return yesno;
}

function disableAllButtons (theform) {

    if (document.all || document.getElementById) {
        for (i = 0; i < theform.length; i++) {
            var tempobj = theform.elements[i];
            if (tempobj.type.toLowerCase() == "submit" || tempobj.type.toLowerCase() == "reset") {
                tempobj.disabled = true;
            }
        }
    }

    //alert ("function has ended. getting ready to return");

    return true;
}

function submitFormDataConfirm (theform) {

    if (confirmSaveAndContinue()) {
        return disableAllButtons(theform);
    } else {
        return false;
    }
}

function submitFormReportCheck (theformlist) {
    var number = 0;
    for (i = 0; i < theformlist.length; i++) {
        if (theformlist[i].selected) number++;
    }
    //if (isNaN(number)) number = 0;
    if (number > 50) {
        alert("You are only allowed to choose up to a maximum of fifty (50) variables.  You have picked "+number+".  Please go back to the form and remove some of your selections.  For Data Dumps of more than 50 variables, please contact your Project Administrator or DBA.");
        return false;
    } else {
        return true;
    }
}


//---------------------added by jxu,10-15-2004------------------------

//-------------------------------------------------------------------------
// Function: setfocus
//
// Set the focus to the first form element.
//-------------------------------------------------------------------------

function setFocus() {

    var finished = false;
    var index = 0;
    if (document.forms[0] != null)
    {
        while ( finished == false )
        {
            if (document.forms[0].elements[index].type != 'hidden')
            {
                document.forms[0].elements[index].focus();
                finished = true;
            }

            index++;
        }
    }
}

//----------------------------------------------------
function trimString (str) {
    str = this != window? this : str;
    return str.replace(/^\s+/g, '').replace(/\s+$/g, '');
}


//-------------------------------------------------------------------------
// Function: getQueryVariable
//
// returns the value of a key/value pair from the page's URL 'GET' parameters
//-------------------------------------------------------------------------

function getQueryVariable(variable) {
    var query = window.location.search.substring(1);
    var vars = query.split("&");
    for (var i=0;i<vars.length;i++) {
        var pair = vars[i].split("=");
        if (pair[0] == variable) {
            return unescape(pair[1]);
        }
    }
    return '';
}

//-------------------------------------------------------------------------
// Function: getName
//
// returns the 'id' attribute of a DOM element by its ID
//-------------------------------------------------------------------------

function getName(spanId) {

    obj = getRef(spanId);
    str = obj.getAttribute('id');
    str = trimString(str);
    return str;

}

//-------------------------------------------------------------------------
// Function: getContent
//
// returns the html/text content of a DOM element by its ID
//-------------------------------------------------------------------------

function getContent(spanId) {
    obj = getRef(spanId);
    str = obj.innerHTML;
    return str;
}

//-------------------------------------------------------------------------
// Function: openDNoteWindow
//
// Pops up a new browser window for discrepancy notes, including the validation error message text if applicable
//-------------------------------------------------------------------------

function openDNoteWindow(inURL, spanID,strErrMsg) {

    if (spanID) {
        //strErrMsg = getContent(spanID);
        // add the error message to the URL
        // encode it using 'escape'
        if (inURL.match(/\?/)) {
            if (inURL.match(/\?$/)) {
                newURL = inURL + 'strErrMsg=' + escape(strErrMsg);
            } else {
                newURL = inURL + '&strErrMsg=' + escape(strErrMsg);
            }
        } else {
            newURL = inURL + '?strErrMsg=' + escape(strErrMsg);
        }
    } else {
        newURL = inURL;
    }
    openNewWindow(newURL,
            'dnote_win',
            'directories=no,location=no,menubar=no,scrollbars=yes,toolbar=no,status=no,resizable=yes',
            'dnote');

}

//-----------------------------------------------------------
//--------for adding new note
function openDNWindow(inURL, spanID,strErrMsg) {

    if (spanID) {
        //strErrMsg = getContent(spanID);
        // add the error message to the URL
        // encode it using 'escape'
        if (inURL.match(/\?/)) {
            if (inURL.match(/\?$/)) {
                newURL = inURL + 'strErrMsg=' + escape(strErrMsg);
            } else {
                newURL = inURL + '&strErrMsg=' + escape(strErrMsg);
            }
        } else {
            newURL = inURL + '?strErrMsg=' + escape(strErrMsg);
        }
    } else {
        newURL = inURL;
    }
    openNewWindow(newURL,
            'dnote_win',
            'directories=no,location=no,menubar=no,scrollbars=yes,toolbar=no,status=no,resizable=yes',
            'dn');

}

//--------------------------------------
//--pop up a window which is smaller 
//------------------------------------------
function openDSNoteWindow(inURL, spanID) {

    if (spanID) {
        strErrMsg = getContent(spanID);
        // add the error message to the URL
        // encode it using 'escape'
        if (inURL.match(/\?/)) {
            if (inURL.match(/\?$/)) {
                newURL = inURL + 'strErrMsg=' + escape(strErrMsg);
            } else {
                newURL = inURL + '&strErrMsg=' + escape(strErrMsg);
            }
        } else {
            newURL = inURL + '?strErrMsg=' + escape(strErrMsg);
        }
    } else {
        newURL = inURL;
    }
    openNewWindow(newURL,
            'dnote_win',
            'directories=no,location=no,menubar=no,scrollbars=yes,toolbar=no,status=no,resizable=yes',
            'dsnote');

}


function openVNoteWindow(inURL) {

    openNewWindow(inURL,
            'def_win',
            'directories=no,location=no,menubar=no,scrollbars=yes,toolbar=no,status=no,resizable=yes',
            'dnote');

}


//-------------------------------------------------------------------------
// Function: openNewWindow
//
// Pops up a new browser window containing the definitions page, and scrolls
//     to the correct spot
//-------------------------------------------------------------------------

function openDefWindow(inURL) {

    openNewWindow(inURL,
            'def_win',
            'directories=no,location=no,menubar=no,scrollbars=yes,toolbar=no,status=no,resizable=yes',
            'small');

}

//-------------------------------------------------------------------------
// Function: openNctEntryWindow
//
// Pops up a new browser window containing the NCT Entry screen
//-------------------------------------------------------------------------

function openNctEntryWindow(inURL) {

    openNewWindow(inURL,
            '_blank',
            'directories=no,location=no,menubar=no,scrollbars=yes,toolbar=no,status=no,resizable=yes',
            'medium');

}

//-------------------------------------------------------------------------
// Function: openDocWindow
//
// Pops up a new browser window containing a document, such as the 
// PRS Reference Guide.
//-------------------------------------------------------------------------

function openDocWindow(inURL) {

    openNewWindow(inURL,
    		inURL,
            'directories=no,location=no,menubar=yes,scrollbars=yes,toolbar=no,status=no,resizable=yes',
            'medium');

}

//-------------------------------------------------------------------------
// Function: openNewWindow
//
// Pops up a new browser window containing the definitions page, and scrolls
//     to the correct spot
//-------------------------------------------------------------------------

function openNewWindow(inURL, name, features, windowSize) {

    // Add check for browser capability
    var old_browser = true;
    if (window.screen != null) old_browser = false;
    /*
     Detect Internet Explorer, for the sake of printing CRFs.
     */
    if(inURL && inURL.indexOf("Print") != -1) {
        if(detectIEWindows(navigator.userAgent)) {
            inURL = inURL+"&ie=y";
        }
    }

    if (features == "") {
        features = "toolbar=yes,directories=yes,location=1,status=yes,menubar=yes,scrollbars=yes,resizable=yes";
    }

    var height=250;
    var width=350;
    var screenHeight = 480;
    var screenWidth = 640;

    if(windowSize == 'small')
    {
        height = 150;
        width = 200;
    }
    if(windowSize == 'medium')
    {
        height = 300;
        width = 500;
    }
    if(windowSize == 'dnote')
    {
        height = 350;
        width = 450;
    }
    if(windowSize == 'dsnote')
    {
        height = 350;
        width = 450;
    }
    if(windowSize == 'dn')
    {
        height = 350;
        width = 450;
    }



    if (window.screen != null)
    {
        screenHeight = window.screen.height;
        screenWidth = window.screen.width;
    }

    if (screenWidth > 640)
    {
        width = width + (screenWidth - 640)*.50;
    }

    if(screenHeight > 480)
    {
        height = height + (screenHeight - 480)*.50;
    }

    features += ",width=" + width + ",height=" + height;

    var docView = window.open (inURL, name, features);

    docView.focus();


}


//-------------------------------------------------------------------------
// Function: MM_findObjInParentWin
//
// Finds the specified object in the parent window if it exists
//     Must be called from within a popup window opened by a parent window
//-------------------------------------------------------------------------

function MM_findObjInParentWin(strParentWinImageName) { //v4.0
    var objImage;

    if (window.opener && !window.opener.closed) {
        objImage = MM_findObj(strParentWinImageName, window.opener.document);
    }

    return objImage;
}

//-------------------------------------------------------------------------
// Function: setImageInParentWin
//
// Sets/changes the source file of an image in a parent window
//     Must be called from within a popup window that was opened by the parent window
//-------------------------------------------------------------------------

function setImageInParentWin(strParentWinImageName,strParentWinImageFullPath) {
    var objImage;

    if (window.opener && !window.opener.closed) {
        //alert(strParentWinImageName);
        objImage = MM_findObjInParentWin(strParentWinImageName);
        if (objImage != null) {
            //alert(objImage.name);
            //alert(objImage.src);
            objImage.src = strParentWinImageFullPath;
        }

    }
}




// new functions for View Subjects status menus 9-13-06

function MM_reloadPage(init) {  //reloads the window if Nav4 resized
    if (init==true) with (navigator) {if ((appName=="Netscape")&&(parseInt(appVersion)==4)) {
        document.MM_pgW=innerWidth; document.MM_pgH=innerHeight; onresize=MM_reloadPage; }}
    else if (innerWidth!=document.MM_pgW || innerHeight!=document.MM_pgH) location.reload();
}
MM_reloadPage(true);

function MM_preloadImages() { //v3.0
    var d=document; if(d.images){ if(!d.MM_p) d.MM_p=new Array();
        var i,j=d.MM_p.length,a=MM_preloadImages.arguments; for(i=0; i<a.length; i++)
            if (a[i].indexOf("#")!=0){ d.MM_p[j]=new Image; d.MM_p[j++].src=a[i];}}
}

function MM_findObj(n, d) { //v4.0
    var p,i,x;  if(!d) d=document; if((p=n.indexOf("?"))>0&&parent.frames.length) {
        d=parent.frames[n.substring(p+1)].document; n=n.substring(0,p);}
    if(!(x=d[n])&&d.all) x=d.all[n]; for (i=0;!x&&i<d.forms.length;i++) x=d.forms[i][n];
    for(i=0;!x&&d.layers&&i<d.layers.length;i++) x=MM_findObj(n,d.layers[i].document);
    if(!x && document.getElementById) x=document.getElementById(n); return x;
}

function setImage(strImageName, strImageFullPath) {
    var objImage;

    objImage = MM_findObj(strImageName);
    if (objImage != null) { objImage.src = strImageFullPath; }
}

function leftnavExpand(strLeftNavRowElementName){

    var objLeftNavRowElement;

    objLeftNavRowElement = MM_findObj(strLeftNavRowElementName);
    if (objLeftNavRowElement != null) {
        if (objLeftNavRowElement.style) { objLeftNavRowElement = objLeftNavRowElement.style; }
        objLeftNavRowElement.display = (objLeftNavRowElement.display == "none" ) ? "" : "none";
    }
}






function layersShowOrHide() {
    var arrayArgs = layersShowOrHide.arguments;
    var objLayer;
    var strShowOrHide = arrayArgs[0];
    var i;

    for (i=1;i<=arrayArgs.length-1;i++) {
        if ((objLayer=MM_findObj(arrayArgs[i]))!=null) {
            // for IE and NS compatibility
            if (objLayer.style) { objLayer = objLayer.style; }
            objLayer.visibility = strShowOrHide;
        }
    }
}


/* 
 Functions that swaps images.  These functions were generated by Dreamweaver, but are
 not used by e-guana.
 */
function MM_swapImage() { //v3.0
    var i,j=0,x,a=MM_swapImage.arguments; document.MM_sr=new Array; for(i=0;i<(a.length-2);i+=3)
        if ((x=MM_findObj(a[i]))!=null){document.MM_sr[j++]=x; if(!x.oSrc) x.oSrc=x.src; x.src=a[i+2];}
}
function MM_swapImgRestore() { //v3.0
    var i,x,a=document.MM_sr; for(i=0;a&&i<a.length&&(x=a[i])&&x.oSrc;i++) x.src=x.oSrc;
}

var isDOM = (document.getElementById ? true : false);
var isIE4 = ((document.all && !isDOM) ? true : false);
var isNS4 = (document.layers ? true : false);
function getRef(id) {
    if (isDOM) return document.getElementById(id);
    if (isIE4) return document.all[id];
    if (isNS4) return document.layers[id];
}
function getSty(id) {
    return (isNS4 ? getRef(id) : getRef(id).style);
}





function gotopage(){



    if(document.jumpform.category.options[document.jumpform.category.selectedIndex].value != ""){



        document.location.href = document.jumpform.category.options[document.jumpform.category.selectedIndex].value;}



}


// new functions for revised View Subjects screen 9-13-06


function getObject( obj ) {

    // step 1
    if ( document.getElementById ) {
        obj = document.getElementById( obj );

        // step 2
    } else if ( document.all ) {
        obj = document.all.item( obj );

        //step 3
    } else {
        obj = null;
    }

    //step 4
    return obj;
}

function LockObject( obj, e ) {

    // step 1
    var tempX = 0;
    var tempY = 0;
    var offsetx = -17;
    var offsety = -15;
    var objHolder = obj;

    // step 2
    obj = getObject( obj );
    if (obj==null) return;

    // step 3
    if (!e) var e = window.event;
    if (e.pageX || e.pageY) 	{
        tempX = e.pageX;
        tempY = e.pageY;
    }
    else if (e.clientX || e.clientY) 	{
        tempX = e.clientX + document.body.scrollLeft
                + document.documentElement.scrollLeft;
        tempY = e.clientY + document.body.scrollTop
                + document.documentElement.scrollTop;
    }

    // step 4
    if (tempX < 0){tempX = 0}
    if (tempY < 0){tempY = 0}

    // step 5
    obj.style.top  = (tempY + offsety) + 'px';
    obj.style.left = (tempX + offsetx) + 'px';

    // step 6
    displayObject( objHolder, true );
}



function moveObject( obj, e ) {

    // step 1
    var tempX = 0;
    var tempY = 0;
    var offsetx = -2;
    var offsety = 10;
    var objHolder = obj;

    // step 2
    obj = getObject( obj );
    if (obj==null) return;

    // step 3
    if (!e) var e = window.event;
    if (e.pageX || e.pageY) 	{
        tempX = e.pageX;
        tempY = e.pageY;
    }
    else if (e.clientX || e.clientY) 	{
        tempX = e.clientX + document.body.scrollLeft
                + document.documentElement.scrollLeft;
        tempY = e.clientY + document.body.scrollTop
                + document.documentElement.scrollTop;
    }

    // step 4
    if (tempX < 0){tempX = 0}
    if (tempY < 0){tempY = 0}

    // step 5
    obj.style.top  = (tempY + offsety) + 'px';
    obj.style.left = (tempX + offsetx) + 'px';

    // step 6
    displayObject( objHolder, true );
}



function displayObject( obj, show ) {

    // step 1
    obj = getObject( obj );
    if (obj==null) return;

    // step 2
    obj.style.display = show ? 'block' : 'none';
    obj.style.visibility = show ? 'visible' : 'hidden';
}

function createRequestObject(){
    var req;

    if(window.XMLHttpRequest){
        //For Firefox, Safari, Opera
        req = new XMLHttpRequest();
    }else if(window.ActiveXObject){
        //For IE 5+
        req = new ActiveXObject("Microsoft.XMLHTTP");
    }else{
        //Error for an old browser
        alert('Your browser is not IE 5 or higher, or Firefox or Safari or Opera');
    }

    return req;
}

//Make the XMLHttpRequest Object
var http = createRequestObject();

var checkboxObject;
function sendRequest(method, url){
    if(method == 'get' || method == 'GET'){
        http.open(method,url);
        http.onreadystatechange = handleResponse;
        http.send(null);
    }
}
function handleResponse(){
    if(http.readyState == 4 && http.status == 200){
        var response = http.responseText;
        if(response == null || response != 'true') {
            checkboxObject.checked=false; alert('Your password did not match. Please try again.');
        }
    }
}
//function requestSignatureFromCheckbox(checkboxObject){
//  if(checkboxObject != null && checkboxObject.checked){
//    var password =  prompt(
//      "Signing this CRF will finalize data entry. You will no longer be able to add or modify data unless the CRF is reset by an administrator. If Double Data Entry is required, you or another user may need to complete this CRF again before it is verified as complete. Enter your password to sign this CRF complete. If the password does not match, you will not be able to sign the CRF complete.", "");
//    sendRequest("GET", "MatchPassword?password=" + password);
//    if(resp != 'true') {
//      checkboxObject.checked=false; alert('Your password did not match. Please try again.');
//    }
//  }
//}

function requestSignatureFromCheckbox(password, checkbox){
    checkboxObject = checkbox;
    if (password==null || password==''){
        alert('Your password did not match. Please try again.');
        checkbox.checked=false;
        return;
    }
    if(checkbox != null && checkbox.checked){
        sendRequest("GET", "MatchPassword?password=" + password);
    }
}

function numberGroupRows(){
    alert("test");
    var allGroupDivs = $$("div.tableDiv");
    var allTrTags;
    var rowCounter;

    for(var i = 0; i < allGroupDivs.length; i++){

        allTrTags =  allGroupDivs[i].getElementsByTagName("tr");

        for(var j=0; j < allTrTags.length;j++) {

            if(allTrTags[j]) {
                rowCounter=allTrTags[j].getAttribute("repeat");

                if(rowCounter && rowCounter.indexOf("template") == -1)  {
                    rowCounter++;
                    allTrTags[j].innerHTML=rowCounter+
                                           allTrTags[j].innerHTML;
                    rowCounter=0;//reset
                }
            }
        }


    }
}









