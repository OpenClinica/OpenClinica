function StudyEventDefRenderer(json) {
  this.json = json;
  this.name = json["@Name"];
  this.OID = json["@OID"];
  this.studyEventdns=undefined;
  this.studyEventaudits=undefined;
  
  
  
  this.renderDiscrepancyNotes = function(discrepancyNotes){
    var template="print_EventCRF_StudyEvent_StudySubject_dns";
    var title = "Study Event Notes & Discrepancies" ;
	var s = RenderUtil.render(RenderUtil.get(template),{discrepancyNotes:discrepancyNotes, title:title } );
    return s[0].outerHTML;
  }
  
  this.renderAuditLogs = function(auditLogs){
    var template="print_EventCRF_StudyEvent_StudySubject_audits";
    var title = "Study Event Audit History" ;
    var s = RenderUtil.render(RenderUtil.get(template),{auditLogs:auditLogs , title:title });
    return s[0].outerHTML;
  }


  

  }
  
