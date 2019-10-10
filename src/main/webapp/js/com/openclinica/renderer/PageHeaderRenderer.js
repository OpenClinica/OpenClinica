function PageHeaderRenderer() {
	

	if(app_renderMode =='UNPOPULATED_STUDY_CRFS'|| app_renderMode =='UNPOPULATED_EVENT_CRFS' || app_renderMode =='UNPOPULATED_GLOBAL_CRF' || app_renderMode =='UNPOPULATED_FORM_CRF' ) {
	this.render = function(printTime, pageType, eventName,eventLocation) { 
	    return RenderUtil.render(RenderUtil.get("print_page_header_global"), {
	      printTime: printTime,
	      pageType: pageType,
	      studyName: app_studyName, 
	      siteName: app_siteName, 
	      protocolName: app_protocolName,
	      eventName: eventName,
	      collectSubjectDOB: app_collectSubjectDOB, 
	      personIDRequired: app_personIDRequired,
	      showPersonID: app_showPersonID,
	      interviewerNameRequired: app_interviewerNameRequired,
	      interviewDateRequired: app_interviewDateRequired,
	      secondaryLabelViewable: app_secondaryLabelViewable,
	      eventLocationRequired: app_eventLocationRequired
	    });
	  }
}

	
else
  this.render = function(printTime, pageType, eventName,eventLocation) { 
	 
	  
	  var eventLocation= app_thisStudyEvent?(app_thisStudyEvent["@OpenClinica:StudyEventLocation"]?app_thisStudyEvent["@OpenClinica:StudyEventLocation"]:""):"";
	  var eventStartDate = app_thisStudyEvent?( app_thisStudyEvent["@OpenClinica:StartDate"]?app_thisStudyEvent["@OpenClinica:StartDate"]:""):"";
	  var repeatOrdinal = app_thisStudyEvent?(app_thisStudyEvent["@StudyEventRepeatKey"]?app_thisStudyEvent["@StudyEventRepeatKey"]:""):"";
	  var eventEndDate = app_thisStudyEvent?( app_thisStudyEvent["@OpenClinica:EndDate"]?app_thisStudyEvent["@OpenClinica:EndDate"]:""):"";
	  var ageAtEnrollment = app_thisStudyEvent?(app_thisStudyEvent["@OpenClinica:SubjectAgeAtEvent"]?app_thisStudyEvent["@OpenClinica:SubjectAgeAtEvent"]:""):"";
	  var studyEventStatus = app_thisStudyEvent?(app_thisStudyEvent["@OpenClinica:Status"]?app_thisStudyEvent["@OpenClinica:Status"]:""):"";
	  var formStatus = app_thisFormData?(app_thisFormData["@OpenClinica:Status"]?app_thisFormData["@OpenClinica:Status"]:""):"";
	  var personId = app_thisSubjectsData?(app_thisSubjectsData["@OpenClinica:UniqueIdentifier"]?app_thisSubjectsData["@OpenClinica:UniqueIdentifier"]:""):"";
	  var secondaryId = app_thisSubjectsData?(app_thisSubjectsData["@OpenClinica:SecondaryID"]?app_thisSubjectsData["@OpenClinica:SecondaryID"]:""):"";
	  var subjectStatus = app_thisSubjectsData?(app_thisSubjectsData["@OpenClinica:Status"]?app_thisSubjectsData["@OpenClinica:Status"]:""):"";
	  var subjectBday =app_thisSubjectsData?( app_thisSubjectsData["@OpenClinica:DateOfBirth"]?app_thisSubjectsData["@OpenClinica:DateOfBirth"]:""):"";
	  var groupClassInfo="";
	  var groupName="";
	 eventStartDate = util_cleanDate(eventStartDate);
	 eventEndDate = util_cleanDate(eventEndDate);
	 var thisStudyEventDef = app_thisStudyEvent?app_studyEventDefMap[app_thisStudyEvent["@StudyEventOID"]]:"";
	
	 if(thisStudyEventDef!=undefined){
	 
	 var repeating = thisStudyEventDef["@Repeating"];
	 
	  if(app_thisSubjectsData &&  app_thisSubjectsData["OpenClinica:SubjectGroupData"]!=undefined)
			 if(  app_thisSubjectsData["OpenClinica:SubjectGroupData"]["@OpenClinica:StudyGroupName"] || app_thisSubjectsData["OpenClinica:SubjectGroupData"]["@OpenClinica:StudyGroupClassName"])
	  { groupClassInfo  =
		  app_thisSubjectsData["OpenClinica:SubjectGroupData"]["@OpenClinica:StudyGroupName"]?app_thisSubjectsData["OpenClinica:SubjectGroupData"]["@OpenClinica:StudyGroupName"]:'';  
		
		  groupName = app_thisSubjectsData["OpenClinica:SubjectGroupData"]["@OpenClinica:StudyGroupClassName"]?app_thisSubjectsData["OpenClinica:SubjectGroupData"]["@OpenClinica:StudyGroupClassName"]:"";
		  
		 groupClassInfo =  groupClassInfo.concat("---");
		 groupClassInfo = groupClassInfo.concat(groupName);
	  }
	 }	  
	  var interviewerName = app_thisFormData?(app_thisFormData["@OpenClinica:InterviewerName"]?app_thisFormData["@OpenClinica:InterviewerName"]:""):"";
	  var interviewDate = app_thisFormData?(app_thisFormData["@OpenClinica:InterviewDate"]?app_thisFormData["@OpenClinica:InterviewDate"]:""):"";
	  var gender = app_thisSubjectsData?(app_thisSubjectsData["@OpenClinica:Sex"]?app_thisSubjectsData["@OpenClinica:Sex"]:""):"";
	  var ssID = app_thisSubjectsData?(app_thisSubjectsData["@OpenClinica:StudySubjectID"]?app_thisSubjectsData["@OpenClinica:StudySubjectID"]:""):"";

	  
	  return RenderUtil.render(RenderUtil.get("print_page_header"), {
      printTime: printTime,
      pageType: pageType,
      studyName: app_studyName, 
      siteName: app_siteName, 
      protocolName: app_protocolName,
      siteProtocolName: app_siteProtocolName,
      eventName: eventName,
      collectSubjectDOB: app_collectSubjectDOB, 
      personIDRequired: app_personIDRequired,
      showPersonID: app_showPersonID,
      interviewerNameRequired: app_interviewerNameRequired,
      interviewDateRequired: app_interviewDateRequired,
      secondaryLabelViewable: app_secondaryLabelViewable,
      eventLocationRequired: app_eventLocationRequired,eventLocation:eventLocation,eventEndDate:eventEndDate,ageAtEnrollment:ageAtEnrollment,
      studyEventStatus:studyEventStatus,personId:personId,secondaryId:secondaryId,subjectBday:subjectBday,subjectStatus:subjectStatus,groupClassInfo:groupClassInfo,interviewerName:interviewerName,
      interviewDate:interviewDate,eventStartDate:eventStartDate,ssID:ssID,repeating:repeating,repeatOrdinal:repeatOrdinal,formStatus:formStatus
      
    });
  }
	
}
