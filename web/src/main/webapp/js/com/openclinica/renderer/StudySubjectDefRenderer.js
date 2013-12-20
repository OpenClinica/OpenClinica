function StudySubjectDefRenderer() {
  //this.json = json;
  //this.name = json["@Name"];
  //this.OID = json["@OID"];

  this.studySubjectdns=undefined;
  this.studySubjectaudits=undefined;
  
  
  
  this.renderDiscrepancyNotes = function(discrepancyNotes){
	    var template="print_EventCRF_StudyEvent_StudySubject_dns";
		var s = RenderUtil.render(RenderUtil.get(template),{discrepancyNotes:discrepancyNotes, title:app_study_subjectLabel+" "+ app_nav_notes_and_discrepanciesLabel} );
	    return s[0].outerHTML;
	  }
	  
	  this.renderAuditLogs = function(auditLogs){
	    var template="print_EventCRF_StudyEvent_StudySubject_audits";
	    var s = RenderUtil.render(RenderUtil.get(template),{auditLogs:auditLogs , title:app_study_subjectLabel+" "+app_Audit_HistoryLabel });
	    return s[0].outerHTML;
	  }


	  
  this.renderStudySubjectData = function(title){
		    var template="print_study_subject_details";
		    var s = RenderUtil.render(RenderUtil.get(template),{title:title});
		    return s[0].outerHTML;
	  }
  
}
  
