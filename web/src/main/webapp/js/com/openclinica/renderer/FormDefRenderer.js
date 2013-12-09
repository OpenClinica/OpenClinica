function FormDefRenderer(json) {
  this.json = json;
  this.name = json["@Name"];
  this.OID = json["@OID"];
  this.repeating = ParseUtil.parseYesNo(json["@Repeating"]);
  this.renderPrintableForm = function() { return RenderUtil.render(RenderUtil.get("print_form_def"), {name: this.name});}
  this.renderInteractiveForm = function() { return RenderUtil.render(RenderUtil.get("e_form_def"), {name: this.name});}
  this.eventCRFdns=undefined;
  this.eventCRFaudits=undefined;
  
  
  if(app_displayDNs=='y')  this.eventCRFdns = app_eventCRFdns[this.OID];
  
  if(app_displayAudits=='y')  this.eventCRFaudits = app_eventCRFaudits[this.OID];
  
  this.renderDiscrepancyNotes = function(discrepancyNotes){
    var template="print_eventCRFdns";
    var s = RenderUtil.render(RenderUtil.get(template),{discrepancyNotes:discrepancyNotes});
    return s[0].outerHTML;
  }
  
  this.renderAuditLogs = function(auditLogs){
    var template="print_eventCRFaudits";
    var s = RenderUtil.render(RenderUtil.get(template),{auditLogs:auditLogs});
    return s[0].outerHTML;
  }

  

  }
  
