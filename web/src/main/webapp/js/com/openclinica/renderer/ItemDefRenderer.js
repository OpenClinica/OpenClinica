function ItemDefRenderer(json, itemDetails) {
  this.json = json;
  this.itemDetails = itemDetails;
  this.name = this.itemDetails["OpenClinica:LeftItemText"];
  this.rightItemText = this.itemDetails["OpenClinica:RightItemText"];
  this.dataType = json["@DataType"];
  this.responseType = this.itemDetails["OpenClinica:ItemResponse"]["@ResponseType"];
  this.OID = json["@OID"];
  debug("In ItemDefRenderer: " + this.OID + "  responseItemText: " + this.rightItemText, util_logDebug);
  this.itemNumber = json["Question"]["@OpenClinica:QuestionNumber"] ? json["Question"]["@OpenClinica:QuestionNumber"]+"." : "";
  this.unitLabel = json["MeasurementUnitRef"] ? "("+app_basicDefinitions[json["MeasurementUnitRef"]["@MeasurementUnitOID"]]+")" : "";
  this.codeListOID = json["CodeListRef"] ? json["CodeListRef"]["@CodeListOID"] : "";
  this.columns = this.itemDetails["OpenClinica:Layout"] ? this.itemDetails["OpenClinica:Layout"]["@Columns"] : undefined;
  
  this.renderPrintableItem = function(isRepeating) { 
    var template = this.columns == 3 ? "print_item_def_3col" : "print_item_def";
    if (isRepeating == true) {
      template = "print_repeating_item";
    }
    var s = RenderUtil.render(RenderUtil.get(template), 
                       {itemNumber:this.itemNumber, name:this.name, rightItemText:this.rightItemText, responseType:this.responseType, unitLabel:this.unitLabel, 
                        optionNames: app_codeLists[this.codeListOID], columns:this.columns});
    return s[0].outerHTML;
  }
  
}