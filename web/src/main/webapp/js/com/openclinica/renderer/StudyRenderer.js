/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2013 OpenClinica
 */


/* StudyRenderer
 * This is the main rendering class where most of the processing occurs.
 */
function StudyRenderer(json) {
  this.json = json;
  this.study = undefined;
  this.accumulatedPixelHeight = 0;
  this.renderString = "";
 
  /* loadBasicDefinitions()
   */
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
 
  /* loadCodeLists()
   */
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
  
  
 /* getItemDetails(itemDef, formDef) 
  * A convenience function to get the ItemDetails properties for an Item
  */ 
  this.getItemDetails = function(itemDef, formDef) {
    if (itemDef["OpenClinica:ItemDetails"]["OpenClinica:ItemPresentInForm"][1] != undefined) { 
      var itemPresentInForm = itemDef["OpenClinica:ItemDetails"]["OpenClinica:ItemPresentInForm"];
      for (var i=0;i< itemPresentInForm.length;i++) {
        if (itemPresentInForm[i]["@FormOID"] == formDef["@OID"]) {
           return itemPresentInForm[i]; 
        }
      }
    }
    return itemDef["OpenClinica:ItemDetails"]["OpenClinica:ItemPresentInForm"];
  }
  
  
 /* loadItemGroupDefs(formDef)
  * Associate all Items with their ItemGroups
  */  
  this.loadItemGroupDefs = function(formDef) {
    var itemGroupDefs = this.study["MetaDataVersion"]["ItemGroupDef"];
    debug("loading item groups");
    app_itemGroupDefs = {};
    app_itemGroupMap = {};
    
    for (var i=0;i< itemGroupDefs.length;i++) {
      var itemGroupDef = itemGroupDefs[i];
      var itemGroupKey = itemGroupDef["@OID"]; 
      var repeatNumber = undefined; 
      if (itemGroupDef["OpenClinica:ItemGroupDetails"]["OpenClinica:PresentInForm"][1] != undefined) {
        var presentInForm = itemGroupDef["OpenClinica:ItemGroupDetails"]["OpenClinica:PresentInForm"];
        for (var j=0;j< presentInForm.length;j++) {
          if (presentInForm[j]["@FormOID"] == formDef["@OID"]) {
           repeatNumber = presentInForm[j].repeatNumber; 
           break;
          }
        }
      }
      else {
        repeatNumber =  itemGroupDef["OpenClinica:ItemGroupDetails"]["OpenClinica:PresentInForm"]["OpenClinica:ItemGroupRepeat"]["@RepeatNumber"];
      }
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
  
  /* loadStudyEventDefs()
   * Load all StudyEvents
   */ 
  this.loadStudyEventDefs = function() {
    debug("loading study events");
    app_studyEventDefs = this.study["MetaDataVersion"]["StudyEventDef"];
    if (app_studyEventDefs[0] == undefined) { 
      app_studyEventDefs = new Array();
      app_studyEventDefs.push(this.study["MetaDataVersion"]["StudyEventDef"]);
    }
  }
 
  
  /* loadItemDefs()
   * Load all ItemDefs
   */
  this.loadItemDefs = function() {
    debug("loading item items");
    app_itemDefs = this.study["MetaDataVersion"]["ItemDef"];
    if (app_itemDefs[0] == undefined) { 
      app_itemDefs = new Array();
      app_itemDefs.push(this.study["MetaDataVersion"]["ItemDef"]);
    }
  }
 
  
  /* loadFormDefs()
   * Load all FormDefs
   */
  this.loadFormDefs = function() {
    debug("loading crfs");
    app_formDefs = this.study["MetaDataVersion"]["FormDef"];
    if (app_formDefs[0] == undefined) { 
      app_formDefs = new Array();
      app_formDefs.push(this.study["MetaDataVersion"]["FormDef"]);
    }
  }
  
  
 /* setStudy(renderMode)
  * Set the current study being rendered
  */ 
  this.setStudy = function (renderMode) {
    switch (renderMode) {
      case 'UNPOPULATED_FORM_CRF':
      case 'UNPOPULATED_EVENT_CRFS':
      case 'UNPOPULATED_STUDY_CRFS':
        this.study = this.json["Study"][0] != undefined ? this.json["Study"][0] : this.json["Study"];
        app_studyName = this.study["GlobalVariables"]["StudyName"];
        app_siteName = this.study["GlobalVariables"]["StudyName"];
        app_protocolName = this.study["GlobalVariables"]["ProtocolName"];
      break;  
    }  
  }
 
  
  /* initStudyLists()
   */
  this.initStudyLists = function () {
    this.loadBasicDefinitions();
    this.loadCodeLists();
    this.loadItemDefs();
    this.loadFormDefs();
    this.loadStudyEventDefs();
  }
 
  
  /* renderPrintableEventCRFs(renderMode, eventDef)
   * Render all CRFS associated with a StudyEvent
   */
  this.renderPrintableEventCRFs = function(renderMode, eventDef) {
    // select all CRFs from StudyEvent
    var studyEventFormRefs =  eventDef["FormRef"];
    if (studyEventFormRefs[0] == undefined) { 
      studyEventFormRefs = new Array();
      studyEventFormRefs.push(eventDef["FormRef"]);
    }
    var multipleFormsString = "";
    for (var i=0;i< studyEventFormRefs.length;i++) {
      var formDef = studyEventFormRefs[i];
      for (var j=0;j< app_formDefs.length;j++) {
        if (app_formDefs[j]["@OID"] == formDef["@FormOID"]) {
          formDef = app_formDefs[j];
          multipleFormsString += this.renderPrintableFormDef(formDef);
          break;
        }
      }
    }
    return multipleFormsString;
  }
  
  
  /* renderPrintableStudy(renderMode)
   * A kind of factory function for the different study
   * rendering scenarios.
   */ 
  this.renderPrintableStudy = function(renderMode) {
    
    var printPageRenderer = new PrintPageRenderer();
    this.setStudy(renderMode);  
    this.initStudyLists();   
    var formDef = undefined;
    var studyString = "";
    var pageTemplateString = "";
    
    if (renderMode == "UNPOPULATED_FORM_CRF") {
      // select CRF by OID
      for (var i=0;i< app_formDefs.length;i++) {
        if (app_formDefs[i]["@OID"] == app_formOID) {
          formDef = app_formDefs[i];
          break; 
        }
      }
      studyString += this.renderPrintableFormDef(formDef);
      this.startNewPage();
    }
    else if (renderMode == "UNPOPULATED_EVENT_CRFS") {
      var eventDef = undefined;
      // select StudyEvent by OID
      for (var i=0;i< app_studyEventDefs.length;i++) {
        if (app_studyEventDefs[i]["@OID"] == app_eventOID) {
          eventDef = app_studyEventDefs[i];
          break;
        }
      }
      studyString += this.renderPrintableEventCRFs(renderMode, eventDef);
    }
    else if (renderMode == "UNPOPULATED_STUDY_CRFS") {
      var multipleFormsString = "";
      // select all CRFs from study
      for (var i=0;i< app_studyEventDefs.length;i++) {
        eventDef = app_studyEventDefs[i];
        multipleFormsString += this.renderPrintableEventCRFs(renderMode, eventDef);
      }
      studyString += multipleFormsString;
    }
    for (var i=0;i< app_pagesArray.length;i++) {
      var pageString =  app_pagesArray[i];
      pageTemplateString += printPageRenderer.render(
          pageString, 
          i+1, 
          app_pagesArray.length, 
          app_printTime,
          app_studyName,
          app_siteName,
          app_protocolName
      )[0].outerHTML;
      //debug(pageTemplateString);
    }
    return pageTemplateString;
  }
 
  
  /* renderPrintableRow(htmlString, rowHeight)
   * Render each row of a CRF.
   * Decide whether a page break is needed
   */
  this.renderPrintableRow = function(htmlString, rowHeight) {
    this.renderString += htmlString;
    this.accumulatedPixelHeight += rowHeight;
    debug("this.accumulatedPixelHeight = " + this.accumulatedPixelHeight);
    if (this.accumulatedPixelHeight > app_maxPixelHeight) {
      this.startNewPage();
    }
  } 
 
  
  /* renderPrintableFormDef(formDef)
   * The heart of StudyRenderer: render the CRF
   */
  this.renderPrintableFormDef = function(formDef) {
    var orderedItems = new Array();
    
    this.loadItemGroupDefs(formDef);
    
    // Get Form Wrapper
    var formDefRenderer = new FormDefRenderer(formDef);
    this.renderString = formDefRenderer.renderPrintableForm()[0].outerHTML;
    var repeatingRenderString = "";
 
    // Get Form Items
    var prevSectionLabel = undefined;
    var prevItemHeader = undefined;
    var prevItemSubHeader = undefined;
    var isFirstSection = true;
    
    // Sort itemDefs by OrderInForm property
    for (var i=0;i< app_itemDefs.length;i++) {
      var itemDef = app_itemDefs[i];
      var itemDetails = this.getItemDetails(itemDef, formDef);
      if (itemDetails["@FormOID"] == formDef["@OID"]) {
        var orderInForm = itemDetails["@OrderInForm"];
        orderedItems[orderInForm-1] = itemDef;
      }
    }
    
    for (var i=0;i< orderedItems.length;i++) {
      var accumulatedPixelHeight = 0;
      var itemDef = orderedItems[i];
      var itemOID = itemDef["@OID"];
      var itemNumber = itemDef["Question"]["@OpenClinica:QuestionNumber"] ? itemDef["Question"]["@OpenClinica:QuestionNumber"]+"." : "";
      var itemDetails = this.getItemDetails(itemDef, formDef);
      var sectionLabel = itemDetails["OpenClinica:SectionLabel"];
      var itemHeader = itemDetails["OpenClinica:ItemHeader"];
      var itemSubHeader = itemDetails["OpenClinica:ItemSubHeader"];
      var name = itemDetails["OpenClinica:LeftItemText"];
      var columnNumber = itemDetails["@ColumnNumber"];
      var columns = itemDetails["OpenClinica:Layout"] ? itemDetails["OpenClinica:Layout"]["@Columns"] : undefined;
      debug("#"+itemNumber+"column/columns: "+columnNumber+"/"+columns+ ", name: "+name+", section: "+sectionLabel+", header: "+itemHeader);
      
      if (sectionLabel != prevSectionLabel) {
        if (isFirstSection == true) {
          this.renderPrintableRow("<div class='gray_bg'><h2>"+sectionLabel+"</h2></div>", 15);
        }
        else if (this.accumulatedPixelHeight > 0) {
          this.startNewPage();
          this.renderPrintableRow("<div class='non-first_section_header gray_bg'><h2>"+sectionLabel+"</h2></div>", 15); 
        }
        isFirstSection = false;
      }
      if (itemHeader !== undefined && itemHeader != prevItemHeader) {
        this.renderPrintableRow("<div class='gray_bg'><h3>"+itemHeader+"</h3></div>", 15); 
      }
      if (itemSubHeader !== undefined && itemSubHeader != prevItemSubHeader) {
        this.renderPrintableRow("<div class='gray_bg'><h4>"+itemSubHeader+"</h4></div>", 15); 
      }
      
      var repeatNumber = 1;
      var repeating = false;
      
      if (app_itemGroupDefs[app_itemGroupMap[itemOID]]) {
        repeatNumber = app_itemGroupDefs[app_itemGroupMap[itemOID]].repeatNumber;
      }
      if (app_itemGroupDefs[app_itemGroupMap[itemOID]]) {
        repeating = app_itemGroupDefs[app_itemGroupMap[itemOID]].repeating;
      }
      if (repeatNumber === undefined ) {
        repeatNumber = 1;
      }
      
      var nextItemDef = undefined;
      var nextColumnNumber = undefined;
      if (i+1 < orderedItems.length) {
        nextItemDef = orderedItems[i+1];
        var nextItemDetails = this.getItemDetails(nextItemDef, formDef);
        nextColumnNumber = nextItemDetails["@ColumnNumber"];
        debug("next item column number: " + nextItemDetails["@ColumnNumber"]);
      }       
      
      if (columnNumber === undefined || columnNumber == 1) {
        repeatingRenderString = "<div class='blocking'>";
      }
      itemDefRenderer = new ItemDefRenderer(itemDef, itemDetails);
      repeatingRenderString += itemDefRenderer.renderPrintableItem();
      if (columnNumber === undefined || columnNumber == 2 && columns === undefined || columns == columnNumber || nextColumnNumber == 1) {
        repeatingRenderString += "</div>";
        for (var repeatCounter=0;repeatCounter<repeatNumber;repeatCounter++) {
          this.renderPrintableRow(repeatingRenderString, 50);
        }
      }
      prevSectionLabel = sectionLabel;
      prevItemHeader = itemHeader;
    }
    return this.renderString;
  }
  
  
  /* startNewPage()
   * Starts a new page in the pages array
   */
  this.startNewPage = function() {
    debug("Starting New Page"); 
    app_pagesArray.push(this.renderString);
    this.accumulatedPixelHeight = 0;
    this.renderString = "";
  }

  
  /* renderStudy()
   * When this is implemented it will render the web form
   */
  this.renderStudy = function() {
  }
  
}