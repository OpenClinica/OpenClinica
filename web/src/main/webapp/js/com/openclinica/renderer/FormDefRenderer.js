function FormDefRenderer(json,studyEventOID,studyEventRepeatKey) {
  this.json = json;
  this.name = json["@Name"];
  this.OID = json["@OID"];
  this.eventCRFLink = studyEventOID+"/"+studyEventRepeatKey+"/"+this.OID;
  this.repeating = ParseUtil.parseYesNo(json["@Repeating"]);
  this.renderPrintableForm = function() { return RenderUtil.render(RenderUtil.get("print_form_def"), {name: this.name,eventCRFLink:this.eventCRFLink});}
  this.renderInteractiveForm = function() { return RenderUtil.render(RenderUtil.get("e_form_def"), {name: this.name,eventCRFLink:this.eventCRFLink});}
  this.eventCRFdns=undefined;
  this.eventCRFaudits=undefined;
  
  
    
 // this.renderDiscrepancyNotes = function(discrepancyNotes){
  this.renderDiscrepancyNotes = function(discrepancyNotes, attribute){
    var template="print_EventCRF_StudyEvent_StudySubject_dns";
//	var s = RenderUtil.render(RenderUtil.get(template),{discrepancyNotes:discrepancyNotes, title:app_eventCRFLabel +": "+app_nav_notes_and_discrepanciesLabel } );
	var s = RenderUtil.render(RenderUtil.get(template),{discrepancyNotes:discrepancyNotes, title:attribute +": "+app_nav_notes_and_discrepanciesLabel } );
    return s[0].outerHTML;
  }
  

this.renderAuditLogs = function(auditLogs){
    var template="print_EventCRF_StudyEvent_StudySubject_audits";
    var s = RenderUtil.render(RenderUtil.get(template),{auditLogs:auditLogs , title:app_eventCRFLabel + " " + app_Audit_HistoryLabel });
    return s[0].outerHTML;
  }

  

  }
  
