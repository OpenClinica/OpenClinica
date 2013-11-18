/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2013 OpenClinica
 */


/* StudyDataLoader
 * This class contains all of the data loading functions for a study
 */
function StudyDataLoader(study, json) {
  this.study = study;
  this.json = json;
  
	  
 /* getStudyParamValue(studyParamList, listId) 
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
    app_secondaryIDs = app_studyDetails["OpenClinica:StudyDescriptionAndStatus"]["@SecondaryIDs"];
  }
  
 
  /* loadBasicDefinitions()
   */
  this.loadBasicDefinitions = function() {
    app_basicDefinitions = {};
    if (this.study["BasicDefinitions"] == undefined) {
      return; 
    }
    var basicDefinitions = this.study["BasicDefinitions"]["MeasurementUnit"];
    debug("loading basic definitions", util_logDebug );
    for (var i=0;i< basicDefinitions.length;i++) {
      var key = basicDefinitions[i]["@OID"]; 
      var value = basicDefinitions[i]["@Name"]; 
      app_basicDefinitions[key] = value;
    }
  }
 
  /* loadCodeLists()
   */
  this.loadCodeLists = function() {
    app_codeLists = {};
    debug("loading code lists", util_logDebug );
    var codeLists = util_ensureArray(this.study["MetaDataVersion"]["CodeList"]);
    if (codeLists == undefined) { 
      return;	
    }
    
    for (var i=0;i< codeLists.length;i++) {
      var codeListKey = codeLists[i]["@OID"]; 
      var currentCodeList = [];
      var codeListItems = util_ensureArray(codeLists[i]["CodeListItem"]);
      
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
    app_multiSelectLists = {};
    debug("loading multi select lists", util_logDebug );
    var multiSelectLists = util_ensureArray(this.study["MetaDataVersion"]["OpenClinica:MultiSelectList"]);
    if (multiSelectLists == undefined) { 
      return;	
    }
  
    for (var i=0;i< multiSelectLists.length;i++) {
      var multiSelectListKey = multiSelectLists[i]["@ID"]; 
      var currentMultiSelectList = [];
      var multiSelectListItems = util_ensureArray(multiSelectLists[i]["OpenClinica:MultiSelectListItem"]);
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
    debug("loading item groups", util_logDebug );
    app_itemGroupDefs = {};
    app_itemGroupMap = {};
    var itemGroupDefs = util_ensureArray(this.study["MetaDataVersion"]["ItemGroupDef"]);
    if (itemGroupDefs == undefined) { 
      return;	
    }
    
    for (var i=0;i< itemGroupDefs.length;i++) {
      var itemGroupDef = itemGroupDefs[i];
      var itemGroupKey = itemGroupDef["@OID"]; 
      var itemGroupName = itemGroupDef["@Name"]; 
      var repeatNumber = 1; 
      var repeatMax = 1; 
      var groupHeader = itemGroupDef["OpenClinica:ItemGroupDetails"]["OpenClinica:PresentInForm"]["OpenClinica:ItemGroupHeader"];
      var presentInForm = util_ensureArray(itemGroupDef["OpenClinica:ItemGroupDetails"]["OpenClinica:PresentInForm"]);
  
      for (var j=0;j< presentInForm.length;j++) {
        if (presentInForm[j]["@FormOID"] == formDef["@OID"]) {
          repeatNumber = presentInForm[j]["OpenClinica:ItemGroupRepeat"]["@RepeatNumber"]; 
          repeatMax = presentInForm[j]["OpenClinica:ItemGroupRepeat"]["@RepeatMax"]; 
          var repeating = ParseUtil.parseYesNo(itemGroupDef["@Repeating"]);
           groupHeader = presentInForm[j]["OpenClinica:ItemGroupHeader"];
          debug("Item Group " +itemGroupKey+ " repeating? "+repeating+", repeat number: "+ repeatNumber + ", repeatMax: " + repeatMax, util_logDebug );
          var currentItemGroup = {};
          currentItemGroup.repeatNumber = repeatNumber;
          currentItemGroup.repeatMax = repeatMax;
          currentItemGroup.repeating = repeating;
          currentItemGroup.name = itemGroupName;
          currentItemGroup.groupHeader = groupHeader;
          app_itemGroupDefs[itemGroupKey] = currentItemGroup;
          
          var itemRefs = itemGroupDef["ItemRef"];
          if (itemRefs[0] == undefined) { 
            itemRefs = new Array();
            itemRefs.push(itemGroupDef["ItemRef"]);
          }
            
          for (var j=0;j<itemRefs.length;j++) {
            var itemKey = itemRefs[j]["@ItemOID"]; 
            var orderNumber = itemRefs[j]["@OrderNumber"]; 
            var mandatory = ParseUtil.parseYesNo(itemRefs[j]["@Mandatory"]);
            var currentItem = {};
            currentItem.orderNumber = orderNumber;
            currentItem.mandatory = mandatory;
            currentItem.itemGroupKey = itemGroupKey;
            currentItem.itemGroupLength = itemRefs.length;
            app_itemGroupMap[itemKey] = currentItem;
            debug("Attaching " + itemKey + "[" + orderNumber + ", " + itemRefs.length + "]", util_logDebug);
          }
          break;
        }
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
  
  /* loadSubjectData()
   */
  this.loadSubjectData = function (json) {
    debug("loading subject data", util_logDebug );
    if (app_studySubjectOID.length == 0) {
      return;
    }
    var subjectData = undefined;
    var studyEventData = undefined;
    var clinicalData = this.json["ClinicalData"];
    var subjectsData = util_ensureArray(clinicalData["SubjectData"]);
    
    for (var i=0;i<subjectsData.length;i++) {
      if(subjectsData[i]["@SubjectKey"] == app_studySubjectOID) { 
        subjectData = subjectsData[i];
        break;
      }
    }
    
    app_thisSubjectsData = subjectData;
    app_studySubjectDOB = subjectData["@OpenClinica:DateOfBirth"];
    
    var studyEventsData = util_ensureArray(subjectData["StudyEventData"]);
    if(studyEventsData)
    for (var i=0;i<studyEventsData.length;i++) {
     if(studyEventsData[i]["@StudyEventOID"] == app_eventOID && studyEventsData[i]["@StudyEventRepeatKey"] == app_eventOrdinal) { 
        studyEventData = studyEventsData[i];
        break;
      }
    }
    
    if (studyEventData == undefined) {
      return;
    }
    else
    	app_thisStudyEvent = studyEventData;
 
    app_studySubjectStartDate = studyEventData["@OpenClinica:StartDate"];
    var formsData = util_ensureArray(studyEventData["FormData"]);
    
    if (formsData == undefined) {
      return;
    }
    for (var i=0;i<formsData.length;i++) {
     if(formsData[i]["@FormOID"] == app_formVersionOID) { 
        app_formData = formsData[i];
        break;
      }
    }
    
   // load itemGroupData into a hash map and determine repeating group row lengths 
   var itemGroupData = util_ensureArray(app_formData["ItemGroupData"]);
   
   if (itemGroupData) {
     for (var i=0;i<itemGroupData.length;i++) {
       var repeatKey = itemGroupData[i]["@ItemGroupRepeatKey"];
       var itemGroupOID = itemGroupData[i]["@ItemGroupOID"];
       if (app_itemGroupRepeatLengthMap[itemGroupOID] == undefined || parseInt(app_itemGroupRepeatLengthMap[itemGroupOID]) < parseInt(repeatKey)) {
         app_itemGroupRepeatLengthMap[itemGroupOID] = repeatKey;
       }
       
       var itemsData = util_ensureArray(itemGroupData[i]["ItemData"]);
       for (var j=0;j<itemsData.length;j++) {
         var itemValue = itemsData[j]["@Value"];
         var itemOID = itemsData[j]["@ItemOID"];
         if (itemOID in app_itemValuesMap == false){
           app_itemValuesMap[itemOID] = {}; 
           app_audits[itemOID]={};
         }
         app_itemValuesMap[itemOID][repeatKey] = itemValue; 
         app_audits[itemOID][repeatKey] = itemsData[j]["OpenClinica:AuditLogs"];
       }
     }
   }
    app_studySubjectStatus = app_formData["@OpenClinica:Status"];
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
    this.loadSubjectData();
  }

}