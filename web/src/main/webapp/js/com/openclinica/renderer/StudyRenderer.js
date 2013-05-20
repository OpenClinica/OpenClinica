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
  this.ITEM_OPTION_HEIGHT = 7;
  this.DEFAULT_ITEM_HEIGHT = 40; 
  this.DEFAULT_GRID_ITEM_HEIGHT = 50; 
  this.DEFAULT_MAX_REPEAT = 40; 
  this.json = json;
  this.study = undefined;
  this.studyDataLoader = undefined;
  this.accumulatedPixelHeight = 0;
  this.renderString = "";
  this.currentRowWidth = 0;
  this.IN_CRF = true;
  this.CHECK_ROW_WIDTH = true;
  this.DONT_CHECK_ROW_WIDTH = false;
  this.NOT_IN_CRF = false;
  var pageTemplateString = "";
  var printPageRenderer;

  
  
 /* getSectionDetails(itemDef, formDef) 
  * A convenience function to get the SectionDetails properties for an Item
  */ 
  this.getSectionDetails = function(itemDetails, formDef) {
    if (formDef["OpenClinica:FormDetails"]["OpenClinica:SectionDetails"]["OpenClinica:Section"][1] != undefined) { 
      var sections = formDef["OpenClinica:FormDetails"]["OpenClinica:SectionDetails"]["OpenClinica:Section"];
      for (var i=0;i< sections.length;i++) {
        if (sections[i]["@SectionLabel"] == itemDetails["OpenClinica:SectionLabel"]) {
          return sections[i];
        }
      }
    } 
    return formDef["OpenClinica:FormDetails"]["OpenClinica:SectionDetails"]["OpenClinica:Section"];
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
  

  
 /* setStudy(renderMode)
  * Set the current study being rendered
  */ 
  this.setStudy = function (renderMode) {
    this.study = this.json["Study"][0] != undefined ? this.json["Study"][0] : this.json["Study"];
    switch (renderMode) {
      case 'UNPOPULATED_FORM_CRF':
      case 'UNPOPULATED_EVENT_CRFS':
      case 'UNPOPULATED_STUDY_CRFS':
        app_studyName = this.study["GlobalVariables"]["StudyName"];
        app_siteName = this.study["MetaDataVersion"]["OpenClinica:StudyDetails"]["@SiteName"];
        app_protocolName = this.study["GlobalVariables"]["ProtocolName"];
      break;  
      case 'UNPOPULATED_GLOBAL_CRF':
        app_studyName = "";
        app_siteName = "";
        app_protocolName = "";
      break;  
    }  
  }
 

  
  /* createStudyEventCoverPage()
   */
  this.createStudyEventCoverPage = function (eventDef) {
    var str = "<h3>" + eventDef["@Name"] + ":</h3>";
    var studyEventFormRefs =  eventDef["FormRef"];
    for (var i=0;i< studyEventFormRefs.length;i++) {
      var formRef = studyEventFormRefs[i];
      for (var j=0;j< app_formDefs.length;j++) {
        if (app_formDefs[j]["@OID"] == formRef["@FormOID"]) {
          var formDef = app_formDefs[j];
          if (formDef["OpenClinica:FormDetails"]["OpenClinica:PresentInEventDefinition"]["@IsDefaultVersion"] != "Yes") {
            continue;
          }
          str += "<div>" + formDef["@Name"] + "</div>";
        }
      }
    }
    return str;
  }
  
  
  /* createStudyCoverPage()
   */ 
  this.createStudyCoverPage = function () {
    var str = "<table border='1' style='margin-top:50px'>";
    
    // create header row of Study Events
    str += "<tr><td style='width:200px'></td>";
    // iterate over study event defs and examine each event
    for (var i=0;i< app_studyEventDefs.length;i++) {
      var eventDef = app_studyEventDefs[i];
      // load event name into column header
      str +="<td style='padding:10px'>" + eventDef["@Name"] + "</td>";
    }  
    str += "</tr>";
    //iterate over each of the formDefs in the study    
    for (var i=0;i< app_formDefs.length;i++) {
      var formDef = app_formDefs[i];
      if (formDef["OpenClinica:FormDetails"]["OpenClinica:PresentInEventDefinition"]["@IsDefaultVersion"] != "Yes") {
        continue;
      }
      // load crf name into the first column of the CRF row
      str +="<tr><td style='padding:10px'>" + formDef["@Name"] + "</td>";
      
      for (var j=0;j< app_studyEventDefs.length;j++) {
        var eventDef = app_studyEventDefs[j];
        var formFound = false; 
        var studyEventFormRefs =  eventDef["FormRef"];
        for (var k=0;k< studyEventFormRefs.length;k++) {
          var formRef = studyEventFormRefs[k];
          if (formRef["@FormOID"] == formDef["@OID"]) {
            str += "<td style='text-align:center'>X</td>";
            formFound = true; 
            break;
          }
        }
        if (formFound == false) {
          str += "<td></td>";
        }
      }
      str += "</tr>";
    }
    str += "</table>";
    return str;
  }
  
  
  /* renderPrintableEventCRFs(renderMode, eventDef)
   * Render all CRFS associated with a StudyEvent
   */
  this.renderPrintableEventCRFs = function(renderMode, eventDef) {
    app_eventName = eventDef["@Name"];
    var studyEventCoverPageString = this.createStudyEventCoverPage(eventDef);
    var currentPage = {};
    currentPage.data = studyEventCoverPageString;
    currentPage.type = app_studyEventCoverPageType;
    currentPage.eventName = app_eventName;
    app_pagesArray.push(currentPage);
    // select all CRFs from StudyEvent
    var studyEventFormRefs =  eventDef["FormRef"];
    if (studyEventFormRefs[0] == undefined) { 
      studyEventFormRefs = new Array();
      studyEventFormRefs.push(eventDef["FormRef"]);
    }
    for (var i=0;i< studyEventFormRefs.length;i++) {
      var formRef = studyEventFormRefs[i];
      for (var j=0;j< app_formDefs.length;j++) {
        if (app_formDefs[j]["@OID"] == formRef["@FormOID"]) {
          var formDef = app_formDefs[j];
          if (formDef["OpenClinica:FormDetails"]["OpenClinica:PresentInEventDefinition"]["@IsDefaultVersion"] != "Yes") {
            continue;
          }
          this.renderPrintableFormDef(formDef);
          break;
        }
      }
    }
  }
  
  
  /* renderPrintableStudy(renderMode)
   * A kind of factory function for the different study
   * rendering scenarios.
   */ 
  this.renderPrintableStudy = function(renderMode) {
    
    printPageRenderer = new PrintPageRenderer();
    this.setStudy(renderMode);  
    this.studyDataLoader = new StudyDataLoader(this.study); 
    this.studyDataLoader.loadStudyLists();   
    var formDef = undefined;
    pageTemplateString = "";
    
    if (renderMode == "UNPOPULATED_FORM_CRF" || renderMode == "UNPOPULATED_GLOBAL_CRF") {
      // select CRF by OID
      for (var i=0;i< app_formDefs.length;i++) {
        if (app_formDefs[i]["@OID"] == app_formVersionOID) {
          formDef = app_formDefs[i];
          break; 
        }
      }
      this.renderPrintableFormDef(formDef);
      this.startNewPage(false);
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
      this.renderPrintableEventCRFs(renderMode, eventDef);
    }
    else if (renderMode == "UNPOPULATED_STUDY_CRFS") {
      var studyCoverPageString = this.createStudyCoverPage();
      var currentPage = {};
      currentPage.data = studyCoverPageString;
      currentPage.eventName = app_eventName;
      currentPage.type = app_studyCoverPageType;
      app_pagesArray.push(currentPage);
      // select all CRFs from study
      for (var i=0;i< app_studyEventDefs.length;i++) {
        eventDef = app_studyEventDefs[i];
        this.renderPrintableEventCRFs(renderMode, eventDef);
      }
    }
    // render loaded pages array
    for (var i=0;i< app_pagesArray.length;i++) {
      var currentPage =  app_pagesArray[i];
      pageTemplateString += 
      printPageRenderer.render(currentPage.data, i+1, app_pagesArray.length, app_printTime, currentPage.type, currentPage.eventName)[0].outerHTML;
    }
    return pageTemplateString;
  }
 
  
  /* renderPrintableFormDef(formDef)
   * The heart of StudyRenderer: render the CRF
   */
  this.renderPrintableFormDef = function(formDef) {
    var orderedItems = new Array();
    
    this.studyDataLoader.loadItemGroupDefs(formDef);
    
    // Get Form Wrapper
    var formDefRenderer = new FormDefRenderer(formDef);
    this.renderString = app_crfHeader = formDefRenderer.renderPrintableForm()[0].outerHTML;
    var itemRenderString = "";
    var repeatingHeaderString = "";
    var repeatingRowString = "";
    var currentItemGroupOID = "";
    var previousItemGroupOID = "";
    var isFirstRepeatingItem = true;
    var lastRepeatingOrderInFormNumber;
 
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
      var repeatingRows = "";
      var itemDef = orderedItems[i];
      var itemOID = itemDef["@OID"];
      
      var itemNumber = itemDef["Question"] && itemDef["Question"]["@OpenClinica:QuestionNumber"] ? itemDef["Question"]["@OpenClinica:QuestionNumber"]+"." : "";
      var itemDetails = this.getItemDetails(itemDef, formDef);
      var sectionDetails = this.getSectionDetails(itemDetails, formDef);
      var sectionTitle = sectionDetails["@SectionTitle"];
      var sectionLabel = itemDetails["OpenClinica:SectionLabel"];
      var itemHeader = itemDetails["OpenClinica:ItemHeader"];
      var itemSubHeader = itemDetails["OpenClinica:ItemSubHeader"];
      var name = itemDetails["OpenClinica:LeftItemText"];
      var columnNumber = itemDetails["@ColumnNumber"];
      var columns = itemDetails["OpenClinica:Layout"] ? itemDetails["OpenClinica:Layout"]["@Columns"] : undefined;
      debug("#"+itemNumber+"column/columns: "+columnNumber+"/"+columns+ ", name: "+name+", section: "+sectionLabel+", header: "+itemHeader, util_logDebug );
      
      var repeatNumber = 1;
      var repeating = false;
      var repeatMax = undefined; 
      var itemGroupName = undefined; 
      
      if (app_itemGroupMap[itemOID] && app_itemGroupDefs[app_itemGroupMap[itemOID].itemGroupKey]) {
        currentItemGroupOID = app_itemGroupMap[itemOID].itemGroupKey;
        repeatNumber = app_itemGroupDefs[app_itemGroupMap[itemOID].itemGroupKey].repeatNumber ? repeatNumber = app_itemGroupDefs[app_itemGroupMap[itemOID].itemGroupKey].repeatNumber : 1;
        repeating = app_itemGroupDefs[app_itemGroupMap[itemOID].itemGroupKey].repeating;
        repeatMax = app_itemGroupDefs[app_itemGroupMap[itemOID].itemGroupKey].repeatMax ? app_itemGroupDefs[app_itemGroupMap[itemOID].itemGroupKey].repeatMax : this.DEFAULT_MAX_REPEAT;
        itemGroupName = app_itemGroupDefs[app_itemGroupMap[itemOID].itemGroupKey].name;
      }
      
      if (sectionLabel != prevSectionLabel) {
        if (isFirstSection == true) {
          this.renderPrintableRow("<div class='section-title'>"+sectionLabel+"</div>", 30, this.IN_CRF, this.DONT_CHECK_ROW_WIDTH);
        }
        else if (this.accumulatedPixelHeight > 0) {
          this.startNewPage(true);
          this.renderPrintableRow("<div class='non-first_section_header section-title'>"+sectionLabel+"</div>", 30, this.IN_CRF, this.DONT_CHECK_ROW_WIDTH); 
        }
        isFirstSection = false;
      }
      if (repeating == false && itemHeader !== undefined && itemHeader != prevItemHeader) {
        this.renderPrintableRow("<div class='header-title'>"+itemHeader+"</div>", 30, this.IN_CRF, this.DONT_CHECK_ROW_WIDTH); 
      }
      if (repeating == false && itemSubHeader !== undefined && itemSubHeader != prevItemSubHeader) {
        this.renderPrintableRow("<div class='header-title'>"+itemSubHeader+"</div>", 30, this.IN_CRF, this.DONT_CHECK_ROW_WIDTH); 
      }
      
      debug(name + " - repeating: " + repeating + ", repeatNumber: " + repeatNumber + ", repeatMax: " + repeatMax, util_logDebug);
      
      var nextItemDef = undefined;
      var nextColumnNumber = undefined;
      if (i+1 < orderedItems.length) {
        nextItemDef = orderedItems[i+1];
        var nextItemDetails = this.getItemDetails(nextItemDef, formDef);
        nextColumnNumber = nextItemDetails["@ColumnNumber"];
        var nextItemOID = nextItemDef["@OID"];
        debug("next item column number: " + nextColumnNumber, util_logDebug);
      }       
      
      itemDefRenderer = new ItemDefRenderer(itemDef, itemDetails);
      var codeListOID = itemDef["CodeListRef"] ? itemDef["CodeListRef"]["@CodeListOID"] : undefined;
      var multiSelectListOID = itemDef["OpenClinica:MultiSelectListRef"] ? itemDef["OpenClinica:MultiSelectListRef"]["@MultiSelectListID"] : undefined;
      
      
      // process repeating group of items 
      if (repeating == true) {
      
        debug("REPEATING current: " + currentItemGroupOID + ", previous: " + previousItemGroupOID + " i: " + i + ", limit: " + lastRepeatingOrderInFormNumber, util_logDebug);
     
        if (currentItemGroupOID != previousItemGroupOID) {
          isFirstRepeatingItem = true;
        }
      
        var itemRowHeightInPixels = codeListOID && app_codeLists[codeListOID] ? app_codeLists[codeListOID].length * this.ITEM_OPTION_HEIGHT : this.DEFAULT_GRID_ITEM_HEIGHT; 
        itemRowHeightInPixels = multiSelectListOID && app_multiSelectLists[multiSelectListOID] ? app_multiSelectLists[multiSelectListOID].length * this.ITEM_OPTION_HEIGHT : this.DEFAULT_GRID_ITEM_HEIGHT; 
        var orderNumber = app_itemGroupMap[itemOID].orderNumber;
        var itemGroupLength = app_itemGroupMap[itemOID].itemGroupLength;
        debug("repeating group: item " + orderNumber + " of " + itemGroupLength + ".  height = " + itemRowHeightInPixels, util_logInfo);
       
        // in first item in repeating group
        if (isFirstRepeatingItem == true) {
          repeatingRowString = "<tr class='repeating_item_group'>";
          repeatingHeaderString = "<tr class='repeating_item_group'>";
          isFirstRepeatingItem = false;
          lastRepeatingOrderInFormNumber = i + itemGroupLength - 1;
          this.currentRowWidth = 0;
        }
        
        repeatingRowString += itemDefRenderer.renderPrintableItem(repeating);
        var responseLayout = itemDetails["OpenClinica:ItemResponse"]["@ResponseLayout"];
        var responseType = itemDetails["OpenClinica:ItemResponse"]["@ResponseType"];
        
        if (responseLayout == "Horizontal") {
          this.currentRowWidth += app_repeatingHorizItemWidth;
          var options = responseType == 'multi-select' ? app_multiSelectLists[multiSelectListOID] : app_codeLists[codeListOID]; 
          var optionsLength = options == undefined ? 0 : options.length;
          var itemNameRow = "<tr class='repeating_item_option_names'><td colspan='" + optionsLength + "' align='center'>" + itemNumber + " " + name + "</td></tr>";
          var optionsRow = "<tr class='repeating_item_group'>";
          for (var j=0;j< optionsLength;j++) {
            optionsRow += "<td valign='top' class='repeating_item_group'>" + options[j].label + "</td>";
          }
          optionsRow += "</tr>";
          repeatingHeaderString += "<td class='repeating_item_header' valign='top'><table border='1'>" + itemNameRow + optionsRow + "</table></td>";
        }
        else {
          this.currentRowWidth += app_repeatingItemWidth;
          repeatingHeaderString += "<td class='repeating_item_header' valign='top'>" + itemNumber + " " + name + "</td>";
        }
         
        // in last item in repeating group
        if (i == lastRepeatingOrderInFormNumber) {
          repeatingRowString += "</tr>";
          repeatingHeaderString += "</tr>";
          if (this.currentRowWidth > app_maxPixelWidth) {
            //this.printMode = this.LANDSCAPE;
          }
          for (var repeatCounter=0;repeatCounter<repeatMax;repeatCounter++) {
            repeatingRows += repeatingRowString;
            this.accumulatedPixelHeight += itemRowHeightInPixels;
 
            if (this.accumulatedPixelHeight > app_maxPixelHeight) {
              this.renderString += RenderUtil.render(RenderUtil.get(
              "print_repeating_item_group"), {headerColspan:itemGroupLength, name:itemGroupName, tableHeader:repeatingHeaderString, tableBody:repeatingRows})[0].outerHTML; 
              this.startNewPage(true);
              repeatingRows = "";
            }
          }
          this.renderString += RenderUtil.render(RenderUtil.get(
          "print_repeating_item_group"), {headerColspan:itemGroupLength, name:itemGroupName, tableHeader:repeatingHeaderString, tableBody:repeatingRows})[0].outerHTML; 
        }
      }
      // standard non-repeating items
      else if (repeating == false) { 
        this.currentRowWidth += app_itemWidth;
        var itemRowHeightInPixels = codeListOID && app_codeLists[codeListOID] ? app_codeLists[codeListOID].length * this.ITEM_OPTION_HEIGHT : this.DEFAULT_ITEM_HEIGHT; 
        itemRowHeightInPixels = multiSelectListOID && app_multiSelectLists[multiSelectListOID] ? app_multiSelectLists[multiSelectListOID].length * this.ITEM_OPTION_HEIGHT : this.DEFAULT_GRID_ITEM_HEIGHT; 
        if (columnNumber === undefined || columnNumber == 1) {
          itemRenderString = "<table class='item-row'>";
        }
        itemRenderString += itemDefRenderer.renderPrintableItem(repeating);
        if (columnNumber === undefined || columnNumber == 2 && columns === undefined || columns == columnNumber || nextColumnNumber == 1) {
          itemRenderString += "</table>";
          this.renderPrintableRow(itemRenderString, itemRowHeightInPixels, this.IN_CRF, this.CHECK_ROW_WIDTH);
        }
      }
      debug("calculated itemRowHeightInPixels for " + name + ": " + itemRowHeightInPixels, util_logInfo);
      
      previousItemGroupOID = currentItemGroupOID;
      prevSectionLabel = sectionLabel;
      prevItemHeader = itemHeader;
    }
  }
  
  
  /* renderPrintableRow(htmlString, rowHeight, inCrf, checkRowWidth)
   * Render each row of a CRF.
   * Decide whether a page break is needed
   * param htmlString: the string to render
   * param rowHeight: the row height in pixels
   * param inCRF: true if we are not at the start of a new CRF
   * param checkRowWidth: true if row width is to be inspected
   */
  this.renderPrintableRow = function(htmlString, rowHeight, inCrf, checkRowWidth) {
    this.renderString += htmlString;
    this.accumulatedPixelHeight += rowHeight;
    debug("this.accumulatedPixelHeight = " + this.accumulatedPixelHeight + ", this.currentRowWidth = " + this.currentRowWidth , util_logInfo);
    if (this.accumulatedPixelHeight > app_maxPixelHeight) {
      if (checkRowWidth == true && this.currentRowWidth > app_maxPixelWidth) {
        //this.printMode = this.LANDSCAPE;
      }
      this.startNewPage(inCrf);
    }
    this.currentRowWidth = 0;
  } 
  
  
  /* startNewPage(inCrf)
   * Starts a new page in the pages array
   * param inCRF: true if we are not at the start of a new CRF
   */
  this.startNewPage = function(inCrf) {
    debug("Starting New Page", util_logDebug); 
    var currentPage = {};
    currentPage.data = this.renderString;
    currentPage.type = app_studyContentPageType;
    currentPage.eventName = app_eventName;
    app_pagesArray.push(currentPage);
    this.accumulatedPixelHeight = 0;
    inCrf ? this.renderString = app_crfHeader : this.renderString = "";
  }

  
  /* renderStudy()
   * When this is implemented it will render the web form
   */
  this.renderStudy = function() {
  }
  
}