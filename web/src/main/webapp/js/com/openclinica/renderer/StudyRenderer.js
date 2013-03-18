function StudyRenderer(json) {
  this.json = json;
  this.study = undefined;
  // this.OID = json["@OID"];
  
  this.loadBasicDefinitions = function() {
    var basicDefinitions = this.study["BasicDefinitions"]["MeasurementUnit"];
    debug("loading basic definitions");
    app_basicDefinitions = {};
    for (var i=0;i< basicDefinitions.length;i++) {
      var key = basicDefinitions[i]["@OID"]; 
      var value = basicDefinitions[i]["@Name"]; 
      app_basicDefinitions[key] = value;
    }
  }
  
  this.loadCodeLists = function() {
    var codeLists = this.study["MetaDataVersion"]["CodeList"];
    debug("loading code lists");
    app_codeLists = {};
    for (var i=0;i< codeLists.length;i++) {
      var codeListKey = codeLists[i]["@OID"]; 
      var currentCodeList = [];
      var codeListItems = codeLists[i]["CodeListItem"];
      for (var j=0;j< codeListItems.length;j++) {
        var currentCodeListItem = {};
        currentCodeListItem.id = codeListItems[j]["@CodedValue"]; 
        currentCodeListItem.label = codeListItems[j]["Decode"]["TranslatedText"]; 
        currentCodeList.push(currentCodeListItem);
      }
      app_codeLists[codeListKey] = currentCodeList;
    }
  }
  
  this.loadItemGroupRefs = function() {
    var itemGroupDefs = this.study["MetaDataVersion"]["ItemGroupDef"];
    debug("loading item groups");
    app_itemGroupDefs = {};
    app_itemGroupMap = {};
    for (var i=0;i< itemGroupDefs.length;i++) {
      var itemGroupDef = itemGroupDefs[i];
      var itemGroupKey = itemGroupDef["@OID"]; 
      var repeatNumber = 
      itemGroupDef["OpenClinica:ItemGroupDetails"]["OpenClinica:PresentInForm"][1] != undefined ? 
      itemGroupDef["OpenClinica:ItemGroupDetails"]["OpenClinica:PresentInForm"][1]["OpenClinica:ItemGroupRepeat"]["@RepeatNumber"] : 
      itemGroupDef["OpenClinica:ItemGroupDetails"]["OpenClinica:PresentInForm"]["OpenClinica:ItemGroupRepeat"]["@RepeatNumber"];
          
      var repeating = ParseUtil.parseYesNo(itemGroupDef["@Repeating"]);
      debug("Item Group " +itemGroupKey+ " repeating? "+repeating+", repeat number: "+ repeatNumber);
      var currentItemGroup = {};
      currentItemGroup.repeatNumber = repeatNumber;
      currentItemGroup.repeating = repeating;
      app_itemGroupDefs[itemGroupKey] = currentItemGroup;
      for (var j=0;j< itemGroupDef["ItemRef"].length;j++) {
        var itemKey = itemGroupDef["ItemRef"][j]["@ItemOID"]; 
        debug("Attaching " +itemKey);
        app_itemGroupMap[itemKey] = itemGroupKey;
      }
    }
  }
  
  this.loadItemGroupDefs = function() {
    var itemGroupDefs = this.study["MetaDataVersion"]["ItemGroupDef"];
    debug("loading item groups");
    app_itemGroupDefs = {};
    app_itemGroupMap = {};
    for (var i=0;i< itemGroupDefs.length;i++) {
      var itemGroupDef = itemGroupDefs[i];
      var itemGroupKey = itemGroupDef["@OID"]; 
      var repeatNumber = 
      itemGroupDef["OpenClinica:ItemGroupDetails"]["OpenClinica:PresentInForm"][1] != undefined ? 
      itemGroupDef["OpenClinica:ItemGroupDetails"]["OpenClinica:PresentInForm"][1]["OpenClinica:ItemGroupRepeat"]["@RepeatNumber"] : 
      itemGroupDef["OpenClinica:ItemGroupDetails"]["OpenClinica:PresentInForm"]["OpenClinica:ItemGroupRepeat"]["@RepeatNumber"];
          
      var repeating = ParseUtil.parseYesNo(itemGroupDef["@Repeating"]);
      debug("Item Group " +itemGroupKey+ " repeating? "+repeating+", repeat number: "+ repeatNumber);
      var currentItemGroup = {};
      currentItemGroup.repeatNumber = repeatNumber;
      currentItemGroup.repeating = repeating;
      app_itemGroupDefs[itemGroupKey] = currentItemGroup;
      for (var j=0;j< itemGroupDef["ItemRef"].length;j++) {
        var itemKey = itemGroupDef["ItemRef"][j]["@ItemOID"]; 
        debug("Attaching " +itemKey);
        app_itemGroupMap[itemKey] = itemGroupKey;
      }
    }
  }
  
  this.setStudy = function (mode) {
    switch (mode) {
      case 'BLANK_SINGLE_CRF':
        this.study = this.json["Study"][0] != undefined ? this.json["Study"][0] : this.json["Study"];
      break;	
    }	
  }
  
  this.initStudyLists = function () {
    this.loadBasicDefinitions();
    this.loadCodeLists();
    this.loadItemGroupDefs();
  }
  
  
  this.renderPrintableForm = function(mode) {
	  
    this.setStudy(mode);  
	this.initStudyLists();   
  
    var itemDefs = this.study["MetaDataVersion"]["ItemDef"];
    
    var formDefs = this.study["MetaDataVersion"]["FormDef"]; 
    
    var formDef = undefined;
    
    if (formDefs[0] != undefined) { 
      for (var i=0;i< formDefs.length;i++) {
        if (formDefs[i]["@OID"] == app_formOID) {
          formDef = formDefs[i];
        }
      }
    }
    else {
      formDef = this.study["MetaDataVersion"]["FormDef"]; 
    }
    
    // Get Form Wrapper
    var formDefRenderer = new FormDefRenderer(formDef);
    //var renderString = formDefRenderer.render();
    var renderString = formDefRenderer.renderPrintableForm()[0].outerHTML;
    var repeatingRenderString = "";
 
    // Get Form Items
    var prevSectionLabel = undefined;
    var prevItemHeader = undefined;
    var prevItemSubHeader = undefined;
    var isFirstSection = true;
    
    for (var i=0;i< itemDefs.length;i++) {
      var itemDef = itemDefs[i];
      var itemOID = itemDef["@OID"];
      var itemNumber = itemDef["Question"]["@OpenClinica:QuestionNumber"] ? itemDef["Question"]["@OpenClinica:QuestionNumber"]+"." : "";
      var itemDetails = itemDef["OpenClinica:ItemDetails"]["OpenClinica:ItemPresentInForm"][1] != undefined ?
                        itemDef["OpenClinica:ItemDetails"]["OpenClinica:ItemPresentInForm"][1] :
                        itemDef["OpenClinica:ItemDetails"]["OpenClinica:ItemPresentInForm"];
                        
      debug("Form OID: " + itemDetails["@FormOID"]);
      
      if (itemDetails["@FormOID"] != formDef["@OID"]) {
        continue;
      }
      
      var sectionLabel = itemDetails["OpenClinica:SectionLabel"];
      var itemHeader = itemDetails["OpenClinica:ItemHeader"];
      var itemSubHeader = itemDetails["OpenClinica:ItemSubHeader"];
      var name = itemDetails["OpenClinica:LeftItemText"];
      var columnNumber = itemDetails["@ColumnNumber"];
      var columns = itemDetails["OpenClinica:Layout"] ? itemDetails["OpenClinica:Layout"]["@Columns"] : undefined;
      debug("#"+itemNumber+"column/columns: "+columnNumber+"/"+columns+ ", name: "+name+", section: "+sectionLabel+", header: "+itemHeader);
      
      if (sectionLabel != prevSectionLabel) {
        if (isFirstSection == true) {
          renderString += "<div class='blocking gray_bg'><h2>"+sectionLabel+"</h2></div>"; 
        }
        else {
          renderString += "<div class='blocking non-first_section_header gray_bg'><h2>"+sectionLabel+"</h2></div>"; 
        }
        isFirstSection = false;
      }
      if (itemHeader !== undefined && itemHeader != prevItemHeader) {
        renderString += "<div class='blocking gray_bg'><h3>"+itemHeader+"</h3></div>"; 
      }
      if (itemSubHeader !== undefined && itemSubHeader != prevItemSubHeader) {
        renderString += "<div class='blocking gray_bg'><h4>"+itemSubHeader+"</h4></div>"; 
      }
      
      var repeatNumber = app_itemGroupDefs[app_itemGroupMap[itemOID]].repeatNumber;
      var repeating = app_itemGroupDefs[app_itemGroupMap[itemOID]].repeating;
      
      if (columnNumber == 1) {
        repeatingRenderString = "<div class='blocking'>";
      }
      itemDefRenderer = new ItemDefRenderer(itemDef);
      repeatingRenderString += itemDefRenderer.renderPrintableItem();
      if (columnNumber == 2 && columns === undefined || columns == columnNumber) {
        repeatingRenderString += "</div>";
        for (var repeatCounter=0;repeatCounter<repeatNumber;repeatCounter++) {
          renderString += repeatingRenderString;
        }
      }
      prevSectionLabel = sectionLabel;
      prevItemHeader = itemHeader;
    }
    return renderString;
  }
  
  
  this.renderInteractiveForm = function() {
    var itemDefs = this.study["MetaDataVersion"]["ItemDef"];
    
    // Get Form Wrapper
    var formDefRenderer = new FormDefRenderer(this.study["MetaDataVersion"]["FormDef"]);
    //var renderString = formDefRenderer.render();
    var renderString = formDefRenderer.renderInteractiveForm()[0].outerHTML;
    renderString += "<div id='section_tabs'>";
    renderString += "<ul>";
    var repeatingRenderString = "";
    var tabDivsRenderString = "";
 
    // Get Form Items
    var prevSectionLabel = undefined;
    var prevItemHeader = undefined;
    var prevItemSubHeader = undefined;
    var isFirstSection = true;
    
    for (var i=0;i< itemDefs.length;i++) {
      var itemDef = itemDefs[i];
      var itemOID = itemDef["@OID"];
      var itemNumber = itemDef["Question"]["@OpenClinica:QuestionNumber"] ? itemDef["Question"]["@OpenClinica:QuestionNumber"]+"." : "";
      var itemDetails = itemDef["OpenClinica:ItemDetails"]["OpenClinica:ItemPresentInForm"][1] != undefined ?
                        itemDef["OpenClinica:ItemDetails"]["OpenClinica:ItemPresentInForm"][1] :
                        itemDef["OpenClinica:ItemDetails"]["OpenClinica:ItemPresentInForm"];
      var sectionLabel = itemDetails["OpenClinica:SectionLabel"];
      var itemHeader = itemDetails["OpenClinica:ItemHeader"];
      var itemSubHeader = itemDetails["OpenClinica:ItemSubHeader"];
      var name = itemDetails["OpenClinica:LeftItemText"];
      var columnNumber = itemDetails["@ColumnNumber"];
      var columns = itemDetails["OpenClinica:Layout"] ? itemDetails["OpenClinica:Layout"]["@Columns"] : undefined;
      //debug("#"+itemNumber+"column/columns: "+columnNumber+"/"+columns+ ", name: "+name+", section: "+sectionLabel+", header: "+itemHeader);
      
      if (sectionLabel != prevSectionLabel) {
      
        if (isFirstSection == true) {
          isFirstSection = false;
        }
        else {
          tabDivsRenderString += "</div>";
        }
       
        renderString += "<li><a href='#section_"+i+"'>"+sectionLabel+"</a></li>";
        tabDivsRenderString += "<div id='section_"+i+"' class='form_wrapper centered'>";
      }
      if (itemHeader !== undefined && itemHeader != prevItemHeader) {
        tabDivsRenderString += "<div class='e_gray_bg'><h3>"+itemHeader+"</h3></div>"; 
      }
      if (itemSubHeader !== undefined && itemSubHeader != prevItemSubHeader) {
        tabDivsRenderString += "<div class='e_gray_bg'><h4>"+itemSubHeader+"</h4></div>"; 
      }
      
      var repeatNumber = app_itemGroupDefs[app_itemGroupMap[itemOID]].repeatNumber;
      var repeating = app_itemGroupDefs[app_itemGroupMap[itemOID]].repeating;
      
      if (columnNumber == 1) {
        repeatingRenderString = "<div class='blocking'>";
      }
      itemDefRenderer = new ItemDefRenderer(itemDef);
      repeatingRenderString += itemDefRenderer.renderInteractiveItem();
      if (repeatNumber > 1 && columns == columnNumber) {
        repeatingRenderString += "<button stype='remove' type='button' class='button_remove'></button>";
      }
      if (columnNumber == 2 && columns === undefined || columns == columnNumber) {
        repeatingRenderString += "</div>";
        for (var repeatCounter=0;repeatCounter<repeatNumber;repeatCounter++) {
          tabDivsRenderString += repeatingRenderString;
        }
        if (repeatNumber > 1) {
          tabDivsRenderString += "<button stype='add' type='button' class='button_search'>Add</button>";
        }
      }

      prevSectionLabel = sectionLabel;
      prevItemHeader = itemHeader;
    }
    renderString += "</ul>";
    renderString += tabDivsRenderString;
    renderString += "</div>";
    return renderString;
  }
  
}