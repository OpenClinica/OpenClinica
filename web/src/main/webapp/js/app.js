var INITIALIZED = false;

var app_itemGroupRepeatLengthMap = {};
var app_itemValuesMap = {};
var app_audits={};
var app_dns={};
var app_eventCRFdns={};
var app_eventCRFaudits={};
var app_globalStudy={};
var app_siteProtocolName;
var app_itemDataMap={};
var app_maxPixelHeight = 610;
var app_maxLandscapePixelHeight = 400;
var app_maxPixelWidth = 850;
var app_maxLandscapePixelWidth = 1350;
var app_itemWidth = 250;
var app_repeatingItemWidth = 250;
var app_repeatingHorizItemWidth = 400;
var JSON_INDENT_LEVEL = 2;
var app_logLevel = util_logInfo;
var app_crfJson;
var app_odmRenderer;
var app_basicDefinitions;
var app_codeLists;
var app_multiSelectLists;
var app_itemGroupDefs;
var app_itemGroupMap;
var app_itemDefs;
var app_formDefs;
var app_formData = undefined;
var app_studyEventDefs;
var app_eventName = undefined;
var app_eventLocation = undefined;
var app_crfHeader;
var app_crfPageNumber = 0;
var app_templateNames = 
['print_page_header','print_form_def','print_item_def','print_repeating_item_group','print_EventCRF_StudyEvent_StudySubject_audits' ,'print_EventCRF_StudyEvent_StudySubject_dns',
 'print_repeating_item','print_repeating_item_horiz','print_item_metadata_info','print_audits','print_dns','print_page_header_global','print_study_event_details','print_study_subject_details','print_subject_table_contents'];
var renderMode;
var app_collectSubjectDOB;
var app_personIDRequired;
var app_showPersonID;
var app_interviewerNameRequired;
var app_interviewDateRequired;
var app_secondaryLabelViewable;
var app_genderRequired;
var app_secondaryLabel;
var app_eventLocationRequired;
var app_studyDetails = undefined;
var app_studySubjectDOB = undefined;
var app_studySubjectStartDateLabel = "Start Date";
var app_studySubjectStatusLabel = "Status";
var app_studySubjectStatus = undefined;
var app_studySubjectStartDate = undefined;

var app_studyEventCoverPageType = "study_event_cover_page_type";
var app_studyCoverPageType = "study_cover_page_type";
var app_studyContentPageType = "study_content_page_type";
var app_thisClinicalData = undefined;
var app_thisStudyEvent = undefined;
var app_thisStudyEventDataMap = undefined;
var app_thisSubjectsData = undefined;
var app_thisFormData = undefined;
var app_studyEventDefMap = {};
var app_renderMode=undefined;
debug("console debugging enabled.", util_logDebug);
var app_userOID = undefined;
var app_userName =undefined;
var app_userData ={};
var app_formDefMap = {};
var app_thisStudyEventRepeatKeyForSingleEvents=undefined;
var app_formDefMap={};
var app_attributes = undefined;
app_attributes = util_ensureArray(app_attributes);	

/***********      @JQUERY INIT    *******************/
$(document).ready(function() {
  if (INITIALIZED == false) {
    INITIALIZED = true;
    RenderUtil.loadTemplates(app_templateNames, function() {
      getPrintableContent();
    });
  }
});
  
  
function getPrintableContent() {
  $('.spinner').css({display: "block"});
  var url = app_contextPath + '/rest/metadata/json/view/' + app_studyOID + '/*/' + app_formVersionOID;
  if (app_studySubjectOID.length > 0) {
    url = app_contextPath + '/rest/clinicaldata/json/view/' + app_studyOID + '/' + app_studySubjectOID + '/' + app_eventOID + '/' + app_formVersionOID+'?includeAudits='+app_displayAudits+'&includeDNs='+app_displayDNs;
  }
  $.get(url, {}, function(data) {
    $('.spinner').css({display: "none"});
    app_pagesArray = new Array();
    setRenderMode();
    app_renderMode = renderMode;
    app_odmRenderer = new ODMRenderer(data);
    var renderString =undefined; 

    try {  
    renderString = app_odmRenderer.renderPrintableStudy(renderMode);
    }
    catch (error)  {
	alert(app_error_print_CRF_Message_at_Loading +"   "+error.message);
    }   
    
     
    $('body').html(renderString);
   });






}
function setRenderMode() {
	
	
	
  renderMode = 'UNPOPULATED_FORM_CRF';
 if(app_studySubjectOID.length<1){
  if (app_studyOID != "*" && app_eventOID == "*" && app_formVersionOID == "*") {
    renderMode = 'UNPOPULATED_STUDY_CRFS';
  }
  else if (app_eventOID != "*" && app_formVersionOID == "*") {
    renderMode = 'UNPOPULATED_EVENT_CRFS';
  }
  else if (app_studyOID == "*" && app_eventOID == "*" && app_formVersionOID != "*") {
    renderMode = 'UNPOPULATED_GLOBAL_CRF';
  }
}

else{
	 if (app_studyOID != "*" && app_eventOID == "*" && app_formVersionOID == "*" && app_studySubjectOID !="*") {
		    renderMode = 'STUDY_SUBJECT_CASE_BOOK';
		  }
	 
	 
		  
	 else if (app_studyOID != "*" && app_eventOID != "*" && app_formVersionOID != "*" && app_studySubjectOID !="*") {
		    renderMode = 'POPULATED_FORM_CRF';
		  }
}
  
}
