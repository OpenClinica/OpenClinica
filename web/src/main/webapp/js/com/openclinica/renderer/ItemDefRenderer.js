function ItemDefRenderer(json, itemDetails) {
  this.json = json;
  this.itemDetails = itemDetails;
  this.name = this.itemDetails["OpenClinica:LeftItemText"];
  this.dataType = json["@DataType"];
  this.responseType = this.itemDetails["OpenClinica:ItemResponse"]["@ResponseType"];
  this.OID = json["@OID"];
  debug("In ItemDefRenderer: " + this.OID + "  responseType: " + this.responseType, util_logInfo );
  this.repeating = ParseUtil.parseYesNo(json["@Repeating"]);
  this.itemNumber = json["Question"]["@OpenClinica:QuestionNumber"] ? json["Question"]["@OpenClinica:QuestionNumber"]+"." : "";
  this.unitLabel = json["MeasurementUnitRef"] ? "("+app_basicDefinitions[json["MeasurementUnitRef"]["@MeasurementUnitOID"]]+")" : "";
  this.codeListOID = json["CodeListRef"] ? json["CodeListRef"]["@CodeListOID"] : "";
  this.columns = this.itemDetails["OpenClinica:Layout"] ? this.itemDetails["OpenClinica:Layout"]["@Columns"] : undefined;
  
  this.renderPrintableItem = function() { 
    var template = this.columns == 3 ? "print_item_def_3col" : "print_item_def";
    var s = RenderUtil.render(RenderUtil.get(template), 
                       {itemNumber:this.itemNumber, name:this.name, responseType:this.responseType, unitLabel:this.unitLabel, 
                        optionNames: app_codeLists[this.codeListOID], columns:this.columns});
    return s[0].outerHTML;
    //return s;
  }
  
  this.renderInteractiveItem = function() { 
    var template = this.columns == 3 ? "e_item_def_3col" : "e_item_def";
    var s = RenderUtil.render(RenderUtil.get(template), 
                       {itemNumber:this.itemNumber, name:this.name, dataType:this.dataType,responseType:this.responseType, unitLabel:this.unitLabel, 
                        optionNames: app_codeLists[this.codeListOID], columns:this.columns});
    return s[0].outerHTML;
    //return s;
  }
}