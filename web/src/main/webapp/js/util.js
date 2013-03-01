var debug = function (log_txt) {
  if (window.console != undefined) {
    console.log(log_txt);
  }
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


function util_checkSession() {
  var jsonData = JSON.stringify({sessionId: user.sessionId});
  $.post("auth/checkSession",{data:jsonData}, function(data) {
    var parsedData = $.parseJSON(data);
    if(parsedData.authenticated == false){util_logout(DO_AUTO_LOGOUT);return false;}
  }); 
}