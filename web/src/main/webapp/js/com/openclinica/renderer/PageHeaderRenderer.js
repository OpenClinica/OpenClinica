function PageHeaderRenderer() {
  this.render = function(printTime, pageType, eventName,eventLocation) { 
	  var eventLocation= app_thisStudyEvent?app_thisStudyEvent["@OpenClinica:StudyEventLocation"]:"";
	  var eventEndDate =  app_thisStudyEvent?app_thisStudyEvent["@OpenClinica:EndDate"]:"";
	  var ageAtEnrollment = app_thisStudyEvent?app_thisStudyEvent["@OpenClinica:SubjectAgeAtEvent"]:"";
	  var studyEventStatus = app_thisStudyEvent?app_thisStudyEvent["@OpenClinica:Status"]:"";
	  var personId = app_thisSubjectsData?app_thisSubjectsData["@OpenClinica:UniqueIdentifier"]:"";
	  var secondaryId = app_thisSubjectsData?app_thisSubjectsData["@OpenClinica:SecondaryID"]:"";
	  var subjectStatus = app_thisSubjectsData?app_thisSubjectsData["@OpenClinica:Status"]:"";
	  var subjectBday = app_thisSubjectsData?app_thisSubjectsData["@OpenClinica:DateOfBirth"]:"";
	  var groupClassInfo  =app_thisSubjectsData?app_thisSubjectsData["OpenClinica:SubjectGroupData"]["@OpenClinica:StudyGroupName"]+'-'+app_thisSubjectsData["OpenClinica:SubjectGroupData"]["@OpenClinica:StudyGroupClassName"]:"";
	  var interviewerName = app_formData?app_formData["@OpenClinica:InterviewerName"]:"";
	  var interviewDate = app_formData?app_formData["@OpenClinica:InterviewDate"]:"";
	  return RenderUtil.render(RenderUtil.get("print_page_header"), {
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
      eventLocationRequired: app_eventLocationRequired,eventLocation:eventLocation,eventEndDate:eventEndDate,ageAtEnrollment:ageAtEnrollment,
      studyEventStatus:studyEventStatus,personId:personId,secondaryId:secondaryId,subjectBday:subjectBday,subjectStatus:subjectStatus,groupClassInfo:groupClassInfo,interviewerName:interviewerName,
      interviewDate:interviewDate
      
    });
  }
}