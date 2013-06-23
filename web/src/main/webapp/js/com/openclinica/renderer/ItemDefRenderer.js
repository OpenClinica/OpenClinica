function ItemDefRenderer(json, itemDetails, mandatory) {
  this.json = json;
  this.itemDetails = itemDetails;
  this.mandatory = mandatory;
  this.name = this.itemDetails["OpenClinica:LeftItemText"];
  this.rightItemText = this.itemDetails["OpenClinica:RightItemText"];
  this.dataType = json["@DataType"];
  this.responseType = this.itemDetails["OpenClinica:ItemResponse"]["@ResponseType"];
  this.responseLayout = this.itemDetails["OpenClinica:ItemResponse"]["@ResponseLayout"];
  this.isInline = this.responseLayout == "Horizontal" ? "inline" : "";
  this.OID = json["@OID"];
  this.itemName = json["@Name"];
  this.itemNumber = json["Question"] && json["Question"]["@OpenClinica:QuestionNumber"] ? json["Question"]["@OpenClinica:QuestionNumber"] : "";
  this.unitLabel = json["MeasurementUnitRef"] ? "("+app_basicDefinitions[json["MeasurementUnitRef"]["@MeasurementUnitOID"]]+")" : "";
  this.codeListOID = json["CodeListRef"] ? json["CodeListRef"]["@CodeListOID"] : "";
  this.multiSelectListOID = json["OpenClinica:MultiSelectListRef"] ? json["OpenClinica:MultiSelectListRef"]["@MultiSelectListID"] : "";
  debug("In ItemDefRenderer: " + this.OID + "  multiSelectListOID: " + this.multiSelectListOID, util_logDebug);
  this.columns = this.itemDetails["OpenClinica:Layout"] ? this.itemDetails["OpenClinica:Layout"]["@Columns"] : undefined;
  
  this.renderPrintableItem = function(isRepeating) { 
    var template = "print_item_def";
    if (isRepeating == true) {
      template = this.responseLayout == "Horizontal" ? "print_repeating_item_horiz" : "print_repeating_item";
    }
    var s = RenderUtil.render(RenderUtil.get(template), 
       {itemNumber:this.itemNumber, name:this.name, rightItemText:this.rightItemText, responseType:this.responseType, unitLabel:this.unitLabel, 
        optionNames: app_codeLists[this.codeListOID], multiSelectOptionNames: app_multiSelectLists[this.multiSelectListOID], columns:this.columns,
        responseLayout:this.responseLayout, isInline: this.isInline, mandatory: this.mandatory,
        itemName:this.itemName});
    return s[0].outerHTML;
  }
  
}