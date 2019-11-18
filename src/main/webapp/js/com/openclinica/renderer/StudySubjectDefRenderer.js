function StudySubjectDefRenderer() {
  //this.json = json;
  //this.name = json["@Name"];
  //this.OID = json["@OID"];

  this.studySubjectdns=undefined;
  this.studySubjectaudits=undefined;
  
  
  
//  this.renderDiscrepancyNotes = function(discrepancyNotes){
	  this.renderDiscrepancyNotes = function(discrepancyNotes,attribute){
	    var template="print_EventCRF_StudyEvent_StudySubject_dns";
//		var s = RenderUtil.render(RenderUtil.get(template),{discrepancyNotes:discrepancyNotes, title:app_study_subjectLabel+" "+ app_nav_notes_and_discrepanciesLabel} );
		var s = RenderUtil.render(RenderUtil.get(template),{discrepancyNotes:discrepancyNotes, title:attribute+": "+ app_nav_notes_and_discrepanciesLabel} );
	    return s[0].outerHTML;
	  }
	  
	  this.renderAuditLogs = function(auditLogs){
	    var template="print_EventCRF_StudyEvent_StudySubject_audits";
	    var s = RenderUtil.render(RenderUtil.get(template),{auditLogs:auditLogs , title:app_study_subjectLabel+" "+app_Audit_HistoryLabel });
	    return s[0].outerHTML;
	  }


	  
  this.renderStudySubjectData = function(app_thisSubjectsData, subjectGroupData){
		    var template="print_study_subject_details";
            var yearOnly="";		    
		    
		    var date = app_thisSubjectsData["@OpenClinica:DateOfBirth"];   
		    if (date != undefined)
		    {yearOnly= date.match(/\d{4}/)} 		       
		       
		    
		    var s = RenderUtil.render(RenderUtil.get(template),{app_thisSubjectsData:app_thisSubjectsData, yearOnly:yearOnly ,subjectGroupData:subjectGroupData});
		    return s[0].outerHTML;
	  }
  
}
  
