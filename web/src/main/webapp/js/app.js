var INITIALIZED = false;

var app_maxPixelHeight = 510;
var JSON_INDENT_LEVEL = 2;
var app_crfJson;
var app_odmRenderer;
var app_basicDefinitions;
var app_codeLists;
var app_itemGroupDefs;
var app_itemGroupMap;
var app_itemDefs;
var app_formDefs;
var app_studyEventDefs;
var app_pagesArray = undefined;
var app_templateNames = 
['print_page','print_form_def','print_item_def','print_item_def_3col','e_form_def','e_item_def','e_item_def_3col'];
var renderMode;

debug("console debugging enabled.");

/***********      @JQUERY INIT    *******************/
$(document).ready(function() {
  if (INITIALIZED == false) {
    INITIALIZED = true;
    RenderUtil.loadTemplates(app_templateNames, function() {
      Backbone.history.start();
      getPrintableContent();
    });
  }
});
  
  
function getPrintableContent() {
    $('body').css({"background-color": "white"});
    $('.spinner').css({display: "block"});
  $.get(app_contextPath + '/rest/metadata/json/view/' + app_studyOID, {}, function(data) {
    $('.spinner').css({display: "none"});
    $('#single-page').css({display: "block"});
    app_pagesArray = new Array();
    setRenderMode();
    app_odmRenderer = new ODMRenderer(data);
    var renderString = app_odmRenderer.renderPrintableStudy(renderMode);
    $('body').html(renderString);
    $('body').css({"background-color": "#AAAAAA"});
   });
}

function setRenderMode() {
  renderMode = 'UNPOPULATED_FORM_CRF';
  if (app_studyOID != "*" && app_eventOID == "*" && app_formOID == "*") {
    renderMode = 'UNPOPULATED_STUDY_CRFS';
  }
  else if (app_eventOID != "*" && app_formOID == "*") {
    renderMode = 'UNPOPULATED_EVENT_CRFS';
  }
}
