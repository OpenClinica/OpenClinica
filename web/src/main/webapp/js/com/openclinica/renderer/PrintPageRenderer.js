function PrintPageRenderer() {
  this.render = function(pageRenderString) { 
    return RenderUtil.render(RenderUtil.get("print_page"), {pageRenderString: pageRenderString});
  }
}