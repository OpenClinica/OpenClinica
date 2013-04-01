function ODMRenderer(json) {
  this.json = json;
  this.fileOID = json["@FileOID"];
  this.description = json["@Description"];
  this.creationDateTime = json["@CreationDateTime"];
  this.fileType = json["@FileType"];
  this.odmVersion = json["@ODMVersion"];
  this.studyRenderer = new StudyRenderer(json); 
  this.renderPrintableStudy= function() { return this.studyRenderer.renderPrintableStudy(renderMode); }
  this.renderStudy = function() { return this.studyRenderer.renderStudy(); }
}
