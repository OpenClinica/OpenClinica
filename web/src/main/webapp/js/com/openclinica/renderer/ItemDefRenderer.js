function ItemDefRenderer(json, itemDetails, mandatory, formOID, repeatRowNumber,studyEventOID,studyEventRepeatKey) {
  this.json = json;
  this.itemDetails = itemDetails;
  this.mandatory = mandatory;
  this.name = this.itemDetails["OpenClinica:LeftItemText"];
  this.rightItemText = this.itemDetails["OpenClinica:RightItemText"];
  this.dataType = json["@DataType"];
  this.responseType = this.itemDetails["OpenClinica:ItemResponse"]["@ResponseType"];
  this.responseLayout = this.itemDetails["OpenClinica:ItemResponse"]["@ResponseLayout"];
  this.isInline = this.responseLayout == "Horizontal" ? "inline" : "";
  this.OID = json["@OID"];
  this.itemName = json["@Name"];
  this.description=json["@Comment"]; 
  this.itemNumber = json["Question"] && json["Question"]["@OpenClinica:QuestionNumber"] ? json["Question"]["@OpenClinica:QuestionNumber"] : "";
  this.unitLabel = json["MeasurementUnitRef"] ? "("+app_basicDefinitions[json["MeasurementUnitRef"]["@MeasurementUnitOID"]]+")" : "";
  this.codeListOID = json["CodeListRef"] ? json["CodeListRef"]["@CodeListOID"] : "";
  this.multiSelectListOID = json["OpenClinica:MultiSelectListRef"] ? json["OpenClinica:MultiSelectListRef"]["@MultiSelectListID"] : "";
  debug("In ItemDefRenderer: " + this.OID + "  multiSelectListOID: " + this.multiSelectListOID, util_logDebug);
  this.columns = this.itemDetails["OpenClinica:Layout"] ? this.itemDetails["OpenClinica:Layout"]["@Columns"] : undefined;
  this.itemValue = undefined;
  this.file = undefined;
  this.fileDownloadLink=undefined;
  this.itemTemp= undefined;
  this.audits = undefined;
  this.dns = undefined;
  this.itemNameLink = undefined;
  this.studyEventOID = studyEventOID;
  this.studyEventRepeatKey = studyEventRepeatKey;
  this.dash="-";
  this.key=this.studyEventOID+this.dash+this.studyEventRepeatKey+this.dash+this.OID;
  this.keyAndRepeat=this.key+this.dash+repeatRowNumber;
  
  
  
  if (this.studyEventOID!=undefined && app_itemValuesMap[this.keyAndRepeat]) { 
    this.itemValue = app_itemValuesMap[this.keyAndRepeat]; 
    
    }
  
  if(this.studyEventOID!=undefined && app_displayAudits=='y' && app_audits[this.key])
	    this.audits = app_audits[this.key];
	    if(this.studyEventOID!=undefined && app_displayDNs=='y' && app_dns[this.key])
	    this.dns = app_dns[this.key];
  
  
  if (this.responseType!=undefined) {
    if (this.responseType=='file' && this.itemValue) {
      if (this.itemValue.indexOf("/")==0) {
        this.fileDownloadLink=app_contextPath+"/DownloadAttachedFile?fileName="+this.itemValue;
        this.file = this.itemValue.substring(this.itemValue.lastIndexOf('/')+1);
        this.fileDownloadLink = this.fileDownloadLink.replace(this.file,  encodeURIComponent(this.file));
      }
      else {
        this.fileDownloadLink=app_contextPath+"/DownloadAttachedFile?fileName="+this.itemValue.replace(/\\/g,"//");
        this.file = this.itemValue.substring(this.itemValue.lastIndexOf('\\')+1);
        this.fileDownloadLink = this.fileDownloadLink.replace(this.file,  encodeURIComponent(this.file));
      }
    }
 }  
  
  this.renderPrintableItem = function(isRepeating) { 
    var template = "print_item_def";
    if (isRepeating == true) {
      template = this.responseLayout == "Horizontal" ? "print_repeating_item_horiz" : "print_repeating_item";
    }
    
		  if (app_thisFormData == undefined || app_thisStudyEvent==undefined ||app_thisSubjectsData==undefined) {
	var itemNameLink = this.itemName
	}else{
	var itemNameLink = app_thisSubjectsData["@SubjectKey"]+"/"+app_thisStudyEvent["@StudyEventOID"]+"["+app_thisStudyEvent["@StudyEventRepeatKey"]+"]/"+ app_thisFormData["@FormOID"]+"/"+this.OID;
	}

    if(app_displayAudits=='y' || app_displayDNs=='y' ){
           var s = RenderUtil.render(RenderUtil.get(template), 
           {OID:this.OID, formOID:formOID, itemNumber:this.itemNumber, name:this.name, rightItemText:this.rightItemText, responseType:this.responseType, 
            unitLabel:this.unitLabel, optionNames: app_codeLists[this.codeListOID], multiSelectOptionNames: app_multiSelectLists[this.multiSelectListOID], 
            columns:this.columns, responseLayout:this.responseLayout, isInline: this.isInline, mandatory: this.mandatory,
            itemNameLink:itemNameLink, itemName:this.itemName, itemValue:this.itemValue,file:this.file,fileDownloadLink:this.fileDownloadLink});
          }else{
        var s = RenderUtil.render(RenderUtil.get(template), 
           {OID:this.OID, formOID:formOID, itemNumber:this.itemNumber, name:this.name, rightItemText:this.rightItemText, responseType:this.responseType, 
            unitLabel:this.unitLabel, optionNames: app_codeLists[this.codeListOID], multiSelectOptionNames: app_multiSelectLists[this.multiSelectListOID], 
            columns:this.columns, responseLayout:this.responseLayout, isInline: this.isInline, mandatory: this.mandatory,
            itemValue:this.itemValue,file:this.file,fileDownloadLink:this.fileDownloadLink});
    }	
    return s[0].outerHTML;
  }
  
  
  this.renderItemFormMetadata = function(groupLabel){
    var template = "print_item_metadata_info";
    var subjectOID = app_thisSubjectsData["@SubjectKey"];
    var responseOptions =  app_codeLists[this.codeListOID]?app_codeLists[this.codeListOID]:app_multiSelectLists[this.multiSelectListOID];

    if (app_thisFormData == undefined || app_thisStudyEvent==undefined ||app_thisSubjectsData==undefined) {
	var itemNameLink =this.itemName
	}else{
	var itemNameLink = app_thisSubjectsData["@SubjectKey"]+"/"+app_thisStudyEvent["@StudyEventOID"]+"["+app_thisStudyEvent["@StudyEventRepeatKey"]+"]/"+ app_thisFormData["@FormOID"]+"/"+this.OID;
	}

    var s = RenderUtil.render(RenderUtil.get(template), 
           {itemNameLink:itemNameLink, itemName:this.itemName,leftItemText:this.name,units:this.unitLabel,responseOptions:responseOptions,dataType:this.dataType ,description:this.description,groupLabel:groupLabel});
        return s[0].outerHTML;
        
  }
  this.renderAuditLogs = function(auditLogs,repeatRowNumber,repeating){
    var template="print_audits";
    var s = RenderUtil.render(RenderUtil.get(template),{auditLogs:auditLogs,value:this.itemName,repeatRow:repeatRowNumber,repeating:repeating});
    return s[0].outerHTML;
  }
   
this.renderDiscrepancyNotes = function(discrepancyNotes,repeatRowNumber,repeating){
    var template="print_dns";
    var s = RenderUtil.render(RenderUtil.get(template),{discrepancyNotes:discrepancyNotes,value:this.itemName,repeatRow:repeatRowNumber, repeating:repeating});
    return s[0].outerHTML;
  }

   
}