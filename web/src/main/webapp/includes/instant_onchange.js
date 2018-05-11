
function destNonRepInstant(id, str, delimiter) {
	if(str.length > 0) {var obj = document.getElementById('input'+id); 	if(obj != null && obj.value != "") {
		var arr = str.split(delimiter); for(var j=1; j<arr.length; j+=3) {doChange('input'+arr[j],arr[j+1]);}}}

}

function sameRepGrpInstant(inputName, id, str, delimiter) {
	if(str.length > 0) { var obj = document.getElementById(inputName); if(obj != null && obj.value != "") {
		var arr = str.split(delimiter); for(var j=1; j<arr.length; j+=3) {
			var dest = inputName.substring(0,(inputName.length-id.length))+arr[j]; doChange(dest,arr[j+1]);}} }

}

function doChange(dest,option) {
	var obj1 = document.getElementById(dest); var obj2 = document.getElementById('show'+dest);
	if(obj1 != null && obj2 != null) { onChangeDateTime(obj1, obj2, option); }
}
function oneDigitToTwo(num) {
	var d2 = num.toString(); if(d2.length == 1) { return '0'+d2; } else { return d2 } ;
}
function onChangeDateTime(obj1, obj2, option) {
	var now = new Date();
	var dStr = now.getFullYear()+'-'+oneDigitToTwo(now.getMonth()+1)+'-'+oneDigitToTwo(now.getDate());
	var tStr = 'T'+oneDigitToTwo(now.getHours())+":"+oneDigitToTwo(now.getMinutes())+":"+oneDigitToTwo(now.getSeconds());
	if(option.toLowerCase() == "_current_date_time") {obj1.value = dStr + tStr; obj2.value = dStr + tStr;
	}else if (option.toLowerCase() == "_current_date") {obj1.value = dStr; obj2.value = dStr; }
}

function manualChange(inputName) {
	var obj1 = document.getElementById(inputName); var obj2 = document.getElementById('show'+inputName);
	if(obj1 != null && obj2 != null) { obj1.value = obj2.value;	}
}