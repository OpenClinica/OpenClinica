function FormDefRenderer(json) {
  this.json = json;
  this.name = json["@Name"];
  this.OID = json["@OID"];
  this.repeating = ParseUtil.parseYesNo(json["@Repeating"]);
  this.renderPrintableForm = function() { return RenderUtil.render(RenderUtil.get("print_form_def"), {name: this.name});}
  this.renderInteractiveForm = function() { return RenderUtil.render(RenderUtil.get("e_form_def"), {name: this.name});}
}