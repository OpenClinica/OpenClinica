function PrintPageRenderer() {
  this.render = function(pageRenderString, currPage, totalPages, printTime) { 
    return RenderUtil.render(RenderUtil.get("print_page"), {
      pageRenderString: pageRenderString,
      currPage: currPage,
      totalPages: totalPages,
      printTime: printTime 
    });
  }
}