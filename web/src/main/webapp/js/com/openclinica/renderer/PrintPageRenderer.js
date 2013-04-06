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
      eventName: app_eventName
    });
  }
}