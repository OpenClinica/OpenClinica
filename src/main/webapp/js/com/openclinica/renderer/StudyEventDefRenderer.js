function StudyEventDefRenderer(json) {
  this.json = json;
  this.name = json["@Name"];
  this.OID = json["@OID"];
  this.studyEventdns=undefined;
  this.studyEventaudits=undefined;
  
  
  
//  this.renderDiscrepancyNotes = function(discrepancyNotes){
  this.renderDiscrepancyNotes = function(discrepancyNotes,attribute){
    var template="print_EventCRF_StudyEvent_StudySubject_dns";
//	var s = RenderUtil.render(RenderUtil.get(template),{discrepancyNotes:discrepancyNotes, title:app_SELabel +": "+ app_nav_notes_and_discrepanciesLabel} );
	var s = RenderUtil.render(RenderUtil.get(template),{discrepancyNotes:discrepancyNotes, title:attribute +": "+ app_nav_notes_and_discrepanciesLabel} );
    return s[0].outerHTML;
  }
  
  this.renderAuditLogs = function(auditLogs){
    var template="print_EventCRF_StudyEvent_StudySubject_audits";
    var s = RenderUtil.render(RenderUtil.get(template),{auditLogs:auditLogs , title:app_SELabel+" "+app_Audit_HistoryLabel });
    return s[0].outerHTML;
  }


  

  }
  
