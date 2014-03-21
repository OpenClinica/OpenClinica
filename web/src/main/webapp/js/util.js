var util_logInfo = 1;
var util_logDebug = 2;

var debug = function (logText, logLevel) {
  if (window.console != undefined && logLevel <= app_logLevel) {
    console.log(logText);
  }
}

/* util_ensureArray(jsonObjectToTest)
 * A kind of factory function for the different study
 * rendering scenarios.
 * @param jsonObjectToTest: The passed in json object
 * @return a json object, json array, or undefined
 */ 
function util_ensureArray(jsonObjectToTest) {
  if (jsonObjectToTest == undefined) { 
    return jsonObjectToTest;	
  }
  if (jsonObjectToTest[0] == undefined) { 
    var jsonArray = new Array();
    jsonArray.push(jsonObjectToTest);
  }
  else {
	jsonArray = jsonObjectToTest;
  }
  return jsonArray;
}

function checkRegexp(s, regexp) {
  return regexp.test(s);
}

function checkPassword(s) {
  if (checkRegexp(s,/[a-z]/) && checkRegexp(s,/[A-Z]/) && checkRegexp(s,/\d/) && checkRegexp(s,/\W/)) {
    return true;
  }
  return false;
}

function buildSelectOptions(list, optionTextProperty, choiceLabel) { 
  var html = [];
  if (choiceLabel !== null) {
    html[html.length] = "<option value=''>" + choiceLabel + "</option>";
  }
  for (var i = 0, len = list.length; i < len; i++) {
    html[html.length] = "<option value='";
    html[html.length] = list[i].id;
    html[html.length] = "'>";
    html[html.length] = list[i][optionTextProperty];
    html[html.length] = "</option>";
  }
  return html.join('');
}




function util_buildFullName(first, middle, last) {
  var middleToken = "";
  if (typeof first !== 'undefined' && first.length > 0) {
  if (typeof middle !== 'undefined' && middle.length > 0) {
    middleToken = middle + " ";
  }
  return first + " " + middleToken + last;
  }
  else {
      return "";
  }
}

function util_checkSessionResponse(obj) {
  idleTime = 0;
  if (obj != undefined){
  if(obj.authenticated == false){
    //util_logout(DO_AUTO_LOGOUT);
    return false;
  }
  return true;
  }
  else {
   DO_AUTO_LOGOUT = false;
   util_logout(DO_AUTO_LOGOUT, DO_AUTO_SERVER_LOGOUT);
   return false;   
  }
}


 function util_cleanDate(eventDate){
	
	 if(!eventDate)
		return eventDate;
	
	 if(eventDate){
		  if(eventDate.toString().indexOf("00:00:00")>1) {
			  eventDate = eventDate.substring(0,eventDate.toString().indexOf("00:00:00"));
		  }
		  else
			  {
			  if(eventDate.toString().lastIndexOf(":00")>1){
			  eventDate = eventDate.substring(0,eventDate.toString().lastIndexOf(":00"));
			  }
			  }
			  
	  }
	  
     //  This function is a workaround to display a page with .trim() ( where IE 8 does not support .trim() method)
	 
	 if(typeof String.prototype.trim !== 'function') {
       String.prototype.trim = function() {
            return this.replace(/^\s+|\s+$/g, ''); 
              }
           }   
	 return eventDate.trim();
} 

function util_checkSession() {
  var jsonData = JSON.stringify({sessionId: user.sessionId});
  $.post("auth/checkSession",{data:jsonData}, function(data) {
    var parsedData = $.parseJSON(data);
    if(parsedData.authenticated == false){util_logout(DO_AUTO_LOGOUT);return false;}
  }); 
  
}
