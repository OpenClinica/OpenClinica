function PrintPageRenderer() {
  this.render = function(pageRenderString, currPage, totalPages, printTime) { 
    return RenderUtil.render(RenderUtil.get("print_page"), {
      pageRenderString: pageRenderString,
      currPage: currPage,
      totalPages: totalPages,
      printTime: printTime,
      studyName: app_studyName, 
      siteName: app_siteName, 
      protocolName: app_protocolName,
      eventName: app_eventName,
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