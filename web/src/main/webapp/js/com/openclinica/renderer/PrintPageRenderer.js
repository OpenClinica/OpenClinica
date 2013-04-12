function PrintPageRenderer() {
  this.render = function(pageRenderString, currPage, totalPages, printTime, pageType, eventName) { 
    return RenderUtil.render(RenderUtil.get("print_page"), {
      pageRenderString: pageRenderString,
      currPage: currPage,
      totalPages: totalPages,
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