function ODMRenderer(json) {
  this.json = json;
  this.fileOID = json["@FileOID"];
  this.description = json["@Description"];
  this.creationDateTime = json["@CreationDateTime"];
  this.fileType = json["@FileType"];
  this.odmVersion = json["@ODMVersion"];
  this.studyRenderer = new StudyRenderer(json); 
  this.renderPrintableForm= function() { return this.studyRenderer.renderPrintableForm(renderMode); }
  this.renderInteractiveForm= function() { return this.studyRenderer.renderInteractiveForm(); }
}
