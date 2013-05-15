/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2013 OpenClinica
 */


/* StudyDataLoader
 * This class contains all of the data loading functions for a study
 */
function StudyDataLoader(study) {
  this.study = study;
  
  
 /* getStudyParamValue(itemDef, formDef) 
  * A convenience function to get the study detail parameter value
  */ 
  this.getStudyParamValue = function(studyParamList, listId) {
    for (var i=0;i< studyParamList.length;i++) {
      if (studyParamList[i]["@StudyParameterListID"] == listId) {
        return studyParamList[i]["@Value"];
      }
    }
  }
  
  
  /* loadStudyDetails()
   */
  this.loadStudyDetails = function() {
    debug("loading study details", util_logDebug );
    app_studyDetails = this.study["MetaDataVersion"]["OpenClinica:StudyDetails"];
    if (!app_studyDetails) {
      return;
    }
    var studyParamList = app_studyDetails["OpenClinica:StudyParameterConfiguration"]["OpenClinica:StudyParameterListRef"];
    app_collectSubjectDOB =  this.getStudyParamValue(studyParamList, "SPL_collectDob");
    app_personIDRequired = this.getStudyParamValue(studyParamList, "SPL_subjectPersonIdRequired");
    app_showPersonID = this.getStudyParamValue(studyParamList, "SPL_personIdShownOnCRF");
    app_interviewerNameRequired = this.getStudyParamValue(studyParamList, "SPL_interviewerNameRequired");
    app_interviewDateRequired = this.getStudyParamValue(studyParamList, "SPL_interviewDateRequired");
    app_secondaryLabelViewable = this.getStudyParamValue(studyParamList, "SPL_secondaryLabelViewable");
    app_eventLocationRequired = this.getStudyParamValue(studyParamList, "SPL_eventLocationRequired");
  }
  
 
  /* loadBasicDefinitions()
   */
  this.loadBasicDefinitions = function() {
    if (this.study["BasicDefinitions"] == undefined) {
      return; 
    }
    var basicDefinitions = this.study["BasicDefinitions"]["MeasurementUnit"];
    debug("loading basic definitions", util_logDebug );
    app_basicDefinitions = {};
    for (var i=0;i< basicDefinitions.length;i++) {
      var key = basicDefinitions[i]["@OID"]; 
      var value = basicDefinitions[i]["@Name"]; 
      app_basicDefinitions[key] = value;
    }
  }
 
  /* loadCodeLists()
   */
  this.loadCodeLists = function() {
    if (this.study["MetaDataVersion"]["CodeList"] == undefined) {
      return; 
    }
    var codeLists = this.study["MetaDataVersion"]["CodeList"];
    debug("loading code lists", util_logDebug );
    app_codeLists = {};
    for (var i=0;i< codeLists.length;i++) {
      var codeListKey = codeLists[i]["@OID"]; 
      var currentCodeList = [];
      var codeListItems = codeLists[i]["CodeListItem"];
      for (var j=0;j< codeListItems.length;j++) {
        var currentCodeListItem = {};
        currentCodeListItem.id = codeListItems[j]["@CodedValue"]; 
        currentCodeListItem.label = codeListItems[j]["Decode"]["TranslatedText"]; 
        currentCodeList.push(currentCodeListItem);
      }
      app_codeLists[codeListKey] = currentCodeList;
    }
  }
  
  
  
  /* loadMultSelectLists()
   */
  this.loadMultiSelectLists = function() {
    if (this.study["MetaDataVersion"]["OpenClinica:MultiSelectList"] == undefined) {
      return; 
    }
    var multiSelectLists = this.study["MetaDataVersion"]["OpenClinica:MultiSelectList"];
    debug("loading multi select lists", util_logDebug );
    app_multiSelectLists = {};
    for (var i=0;i< multiSelectLists.length;i++) {
      var multiSelectListKey = multiSelectLists[i]["@ID"]; 
      var currentMultiSelectList = [];
      var multiSelectListItems = multiSelectLists[i]["OpenClinica:MultiSelectListItem"];
      for (var j=0;j< multiSelectListItems.length;j++) {
        var currentMultiSelectListItem = {};
        currentMultiSelectListItem.id = multiSelectListItems[j]["@CodedOptionValue"]; 
        currentMultiSelectListItem.label = multiSelectListItems[j]["Decode"]["TranslatedText"]; 
        currentMultiSelectList.push(currentMultiSelectListItem);
      }
      app_multiSelectLists[multiSelectListKey] = currentMultiSelectList;
    }
  }
  
  
 /* loadItemGroupDefs(formDef)
  * Associate all Items with their ItemGroups
  */  
  this.loadItemGroupDefs = function(formDef) {
    var itemGroupDefs = this.study["MetaDataVersion"]["ItemGroupDef"];
    debug("loading item groups", util_logDebug );
    app_itemGroupDefs = {};
    app_itemGroupMap = {};
    
    for (var i=0;i< itemGroupDefs.length;i++) {
      var itemGroupDef = itemGroupDefs[i];
      var itemGroupKey = itemGroupDef["@OID"]; 
      var itemGroupName = itemGroupDef["@Name"]; 
      var repeatNumber = undefined; 
      var repeatMax = undefined; 
      if (itemGroupDef["OpenClinica:ItemGroupDetails"]["OpenClinica:PresentInForm"][1] != undefined) {
        var presentInForm = itemGroupDef["OpenClinica:ItemGroupDetails"]["OpenClinica:PresentInForm"];
        for (var j=0;j< presentInForm.length;j++) {
          if (presentInForm[j]["@FormOID"] == formDef["@OID"]) {
           repeatNumber = presentInForm[j].repeatNumber; 
           repeatMax = presentInForm[j].repeatMax; 
           break;
          }
        }
      }
      else {
        repeatNumber =  itemGroupDef["OpenClinica:ItemGroupDetails"]["OpenClinica:PresentInForm"]["OpenClinica:ItemGroupRepeat"]["@RepeatNumber"];
        repeatMax =  itemGroupDef["OpenClinica:ItemGroupDetails"]["OpenClinica:PresentInForm"]["OpenClinica:ItemGroupRepeat"]["@RepeatMax"];
      }
      var repeating = ParseUtil.parseYesNo(itemGroupDef["@Repeating"]);
      debug("Item Group " +itemGroupKey+ " repeating? "+repeating+", repeat number: "+ repeatNumber + ", repeatMax: " + repeatMax, util_logDebug );
      var currentItemGroup = {};
      currentItemGroup.repeatNumber = repeatNumber;
      currentItemGroup.repeatMax = repeatMax;
      currentItemGroup.repeating = repeating;
      currentItemGroup.name = itemGroupName;
      app_itemGroupDefs[itemGroupKey] = currentItemGroup;
      var itemGroupLength = itemGroupDef["ItemRef"].length;
      for (var j=0;j< itemGroupLength;j++) {
        var itemKey = itemGroupDef["ItemRef"][j]["@ItemOID"]; 
        var orderNumber = itemGroupDef["ItemRef"][j]["@OrderNumber"]; 
        var currentItem = {};
        currentItem.orderNumber = orderNumber;
        currentItem.itemGroupKey = itemGroupKey;
        currentItem.itemGroupLength = itemGroupLength;
        app_itemGroupMap[itemKey] = currentItem;
        debug("Attaching " + itemKey + "[" + orderNumber + ", " + itemGroupLength + "]", util_logDebug);
      }
    }
  }
  
  /* loadStudyEventDefs()
   * Load all StudyEvents
   */ 
  this.loadStudyEventDefs = function() {
    debug("loading study events", util_logDebug );
    app_studyEventDefs = this.study["MetaDataVersion"]["StudyEventDef"];
    if (app_studyEventDefs[0] == undefined) { 
      app_studyEventDefs = new Array();
      app_studyEventDefs.push(this.study["MetaDataVersion"]["StudyEventDef"]);
    }
  }
 
  
  /* loadItemDefs()
   * Load all ItemDefs
   */
  this.loadItemDefs = function() {
    debug("loading item items", util_logDebug );
    app_itemDefs = this.study["MetaDataVersion"]["ItemDef"];
    if (app_itemDefs[0] == undefined) { 
      app_itemDefs = new Array();
      app_itemDefs.push(this.study["MetaDataVersion"]["ItemDef"]);
    }
  }
 
  
  /* loadFormDefs()
   * Load all FormDefs
   */
  this.loadFormDefs = function() {
    debug("loading crfs", util_logDebug );
    app_formDefs = this.study["MetaDataVersion"]["FormDef"];
    if (app_formDefs[0] == undefined) { 
      app_formDefs = new Array();
      app_formDefs.push(this.study["MetaDataVersion"]["FormDef"]);
    }
  }
  
  
  /* loadStudyLists()
   */
  this.loadStudyLists = function () {
    this.loadBasicDefinitions();
    this.loadCodeLists();
    this.loadMultiSelectLists();
    this.loadItemDefs();
    this.loadFormDefs();
    this.loadStudyEventDefs();
    this.loadStudyDetails();
  }

  
}