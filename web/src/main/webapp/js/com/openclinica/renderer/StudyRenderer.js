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
  this.DEFAULT_MAX_REPEAT = 40; 
  this.NO_PAGE_BREAK = false; 
  this.PAGE_BREAK = true; 
  this.json = json;
  this.study = undefined;
  this.studyDataLoader = undefined;
  this.renderString = "";
  var pageHeaderRenderer;
  
  
 /* getSectionDetails(itemDetails, formDef) 
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
        if(app_siteName) {
          app_studyName = this.study["MetaDataVersion"]["OpenClinica:StudyDetails"]["@ParentStudyName"];
        }
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
      var formFound = false;
      for (var j=0;j< app_formDefs.length&&!formFound;j++) {
        if (app_formDefs[j]["@OID"] == formRef["@FormOID"]) {
          var formDef = app_formDefs[j];
          var presentInEventDef = util_ensureArray(formDef["OpenClinica:FormDetails"]["OpenClinica:PresentInEventDefinition"]);
          for(var l=0;l<presentInEventDef.length&&!formFound;l++) {
            var inEventDef = presentInEventDef[l];
            if(inEventDef["@IsDefaultVersion"] == "Yes" && inEventDef["@HideCRF"] == "No" && eventDef["@OID"] == inEventDef["@StudyEventOID"]) {
              str += "<div>" + formDef["@Name"] + "</div>";
              formFound = true;
            }
          }
        }
      }
    }
    str += "<div class='investigator-text'>";
    str += app_investigatorNameLabel + ": ___________________&nbsp;&nbsp;&nbsp;&nbsp;";
    str += app_investigatorSignatureLabel +": ___________________&nbsp;&nbsp;&nbsp;&nbsp";
    str += app_dateLabel +": ___________________";
    str += "</div>";
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
    var formFound = false;
    //iterate over each of the formDefs in the study    
    for (var i=0;i< app_formDefs.length;i++) {
      var formDef = app_formDefs[i];
      var formVersionPrinted = false;
      
      var presentInEventDef = util_ensureArray(formDef["OpenClinica:FormDetails"]["OpenClinica:PresentInEventDefinition"]);
      var initEventCNTR = 0;
      for (var l=0;l<presentInEventDef.length;l++) {
        formFound = false;
        var inEventDef = presentInEventDef[l];
         
        if (inEventDef["@IsDefaultVersion"] == "Yes" && inEventDef["@HideCRF"] == "No") {
          if(!formVersionPrinted) str +="<tr><td style='padding:10px'>" + formDef["@Name"] + "</td>";
          formVersionPrinted = true;
        
          for (var j=initEventCNTR;j< app_studyEventDefs.length&&!formFound;j++) {
            var eventDef = app_studyEventDefs[j];
            if(eventDef["@OID"]==inEventDef["@StudyEventOID"]) {
              str += "<td style='text-align:center'>X</td>";
              formFound = true; 
              initEventCNTR = j+1;
            }
            if(!formFound) {
              str += "<td></td>";
            }
          }
          if(!formFound) {
            str += "<td></td>";
          } 
        }
      }
      if (initEventCNTR < app_studyEventDefs.length && formVersionPrinted) {
        for(var c = initEventCNTR; c < app_studyEventDefs.length;c++){
          str += "<td></td>";
        }
      }
      str += "</tr>";
    }
    str += "</table>";
    return str;
  }
  
  
  /* renderPrintableEventCRFs(renderMode, eventDef, pageBreak)
   * Render all CRFS associated with a StudyEvent
   */
  this.renderPrintableEventCRFs = function(renderMode, eventDef, pageBreak) {
    app_eventName = eventDef["@Name"];
    this.renderPageHeader(pageBreak, app_printTime, app_studyEventCoverPageType, app_eventName);
    this.renderString += this.createStudyEventCoverPage(eventDef);
    // select all CRFs from StudyEvent
    var studyEventFormRefs =  eventDef["FormRef"];
    if (studyEventFormRefs[0] == undefined) { 
      studyEventFormRefs = new Array();
      studyEventFormRefs.push(eventDef["FormRef"]);
    }
    for (var i=0;i< studyEventFormRefs.length;i++) {
      pageBreak = this.PAGE_BREAK
      var defaultDisplayed = false;
      var formRef = studyEventFormRefs[i];
      for (var j=0;j< app_formDefs.length && !defaultDisplayed;j++) {
        if (app_formDefs[j]["@OID"] == formRef["@FormOID"]) {
          var formDef = app_formDefs[j];
          var presentInEventDef = util_ensureArray(formDef["OpenClinica:FormDetails"]["OpenClinica:PresentInEventDefinition"]);
          for(var l=0;l<presentInEventDef.length;l++){
            var inEventDef = presentInEventDef[l];
            if(inEventDef["@IsDefaultVersion"] == "Yes" && inEventDef["@HideCRF"] == "No" && inEventDef["@StudyEventOID"]==eventDef["@OID"]) {
              this.renderPrintableFormDef(formDef, pageBreak);
              defaultDisplayed = true;
              break;  
            }
          }
        }
      }
    }
  }
  
  
  /* renderPrintableStudy(renderMode)
   * A kind of factory function for the different study
   * rendering scenarios.
   */ 
  this.renderPrintableStudy = function(renderMode) {
    
    pageHeaderRenderer = new PageHeaderRenderer();
    this.setStudy(renderMode);  
    this.studyDataLoader = new StudyDataLoader(this.study, this.json); 
    this.studyDataLoader.loadStudyLists();   
    var formDef = undefined;
    
    if (renderMode == "UNPOPULATED_FORM_CRF" || renderMode == "UNPOPULATED_GLOBAL_CRF") {
      // select CRF by OID
      for (var i=0;i< app_formDefs.length;i++) {
        if (app_formDefs[i]["@OID"] == app_formVersionOID) {
          formDef = app_formDefs[i];
          break; 
        }
      }
      if(formDef== undefined){
    	  alert("This Case Report Form has been removed, please restore it to continue with Print functionaility");
    	  window.history.back();
    	  window.close();
    	  return;
      }
      this.renderPrintableFormDef(formDef, this.NO_PAGE_BREAK);
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
      this.renderPrintableEventCRFs(renderMode, eventDef, this.NO_PAGE_BREAK);
    }
    else if (renderMode == "UNPOPULATED_STUDY_CRFS") {
      this.renderPageHeader(this.NO_PAGE_BREAK, app_printTime, app_studyCoverPageType, app_eventName);
      this.renderString += this.createStudyCoverPage();
      // select all CRFs from study
      for (var i=0;i< app_studyEventDefs.length;i++) {
        eventDef = app_studyEventDefs[i];
        this.renderPrintableEventCRFs(renderMode, eventDef, this.PAGE_BREAK);
      }
    }
    return this.renderString;
  }
 
  
  /* renderPrintableFormDef(formDef, pageBreak)
   * The heart of StudyRenderer: render the CRF
   */
  this.renderPrintableFormDef = function(formDef, pageBreak) {
  
    this.renderPageHeader(pageBreak, app_printTime, app_studyContentPageType, app_eventName);
    
    var orderedItems = new Array();
    
    this.studyDataLoader.loadItemGroupDefs(formDef);
    
    // Get Form Wrapper
    var formDefRenderer = new FormDefRenderer(formDef);
    this.renderString += app_crfHeader = formDefRenderer.renderPrintableForm()[0].outerHTML;
    var repeatingHeaderString = "";
    var repeatingRowString = "";
    var currentItemGroupOID = "";
    var previousItemGroupOID = "";
    var isFirstRepeatingItem = true;
 
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
    
    var repeatRowNumber = 1;
    var totalRepeatingRows = 1;
    var idxForFirstItem = undefined;
    
    for (var orderedItemIndex=0;orderedItemIndex< orderedItems.length;orderedItemIndex++) {
      var repeatingRows = "";
      var itemDef = orderedItems[orderedItemIndex];
      var itemOID = itemDef["@OID"];
      var itemName = itemDef["@Name"];
      
      var itemNumber = itemDef["Question"] && itemDef["Question"]["@OpenClinica:QuestionNumber"] ? itemDef["Question"]["@OpenClinica:QuestionNumber"] : "";
      var itemDetails = this.getItemDetails(itemDef, formDef);
      var sectionDetails = this.getSectionDetails(itemDetails, formDef);
      var sectionTitle = sectionDetails["@SectionTitle"];
      var sectionSubTitle = sectionDetails["@SectionSubtitle"];
      var sectionInstructions = sectionDetails["@SectionInstructions"];
      var sectionPageNumber = sectionDetails["@SectionPageNumber"];
      var sectionLabel = itemDetails["OpenClinica:SectionLabel"];
      var itemHeader = itemDetails["OpenClinica:ItemHeader"];
      var itemSubHeader = itemDetails["OpenClinica:ItemSubHeader"];
      var name = itemDetails["OpenClinica:LeftItemText"];
      var rightItem =itemDetails["OpenClinica:RightItemText"]; 
      var columnNumber = itemDetails["@ColumnNumber"];
      var columns = itemDetails["OpenClinica:Layout"] ? itemDetails["OpenClinica:Layout"]["@Columns"] : undefined;
      var lastItemInRepeatingRow = false;
      debug("#"+itemNumber+"column/columns: "+columnNumber+"/"+columns+ ", name: "+name+", section: "+sectionLabel+", header: "+itemHeader, util_logDebug );
      
      var repeating = false;
      var manditory = false;
      var repeatMax = undefined; 
      var itemGroupHeader = undefined;
      var itemGroupName = undefined; 
      var nextGroupOID = undefined;
      
      
      if (app_itemGroupMap[itemOID] && app_itemGroupDefs[app_itemGroupMap[itemOID].itemGroupKey]) {
        currentItemGroupOID = app_itemGroupMap[itemOID].itemGroupKey;
        mandatory = app_itemGroupMap[itemOID].mandatory;
        totalRepeatingRows = app_itemGroupDefs[app_itemGroupMap[itemOID].itemGroupKey].repeatNumber ? totalRepeatingRows = app_itemGroupDefs[app_itemGroupMap[itemOID].itemGroupKey].repeatNumber : 1;
        // adjust totalRepeatingRows to number of data rows in a populated form
        var totalRepeatingDataRows = app_itemGroupRepeatLengthMap[app_itemGroupMap[itemOID].itemGroupKey];
          
        if (parseInt(totalRepeatingDataRows) > parseInt(totalRepeatingRows)) {
          totalRepeatingRows = totalRepeatingDataRows;
        }
        repeating = app_itemGroupDefs[app_itemGroupMap[itemOID].itemGroupKey].repeating;
        repeatMax = app_itemGroupDefs[app_itemGroupMap[itemOID].itemGroupKey].repeatMax ? app_itemGroupDefs[app_itemGroupMap[itemOID].itemGroupKey].repeatMax : this.DEFAULT_MAX_REPEAT;
        itemGroupName = app_itemGroupDefs[app_itemGroupMap[itemOID].itemGroupKey].name;
        itemGroupHeader = app_itemGroupDefs[app_itemGroupMap[itemOID].itemGroupKey].groupHeader;
      }
      
      if (sectionLabel != prevSectionLabel) {
        if (isFirstSection == false) {
          this.renderPageHeader(this.PAGE_BREAK, app_printTime, app_studyContentPageType, app_eventName);
        }
        this.renderString += "<div class='vertical-spacer-30px'></div>";
        this.renderString += sectionTitle != '' ? "<div class='section-title'>"+app_sectionTitle+"&nbsp;"+sectionTitle+"</div>" : "";
        this.renderString += sectionSubTitle != '' ? "<div class='section-info'>"+app_sectionSubtitle+"&nbsp;"+sectionSubTitle+"</div>" : "";
        this.renderString += sectionInstructions ? "<div class='section-info'>"+app_sectionInstructions+"&nbsp;"+sectionInstructions+"</div>" : "";
        this.renderString += sectionPageNumber ? "<div class='section-info'>"+app_sectionPage+"&nbsp;"+sectionPageNumber+"</div>" : "";
        isFirstSection = false;
      }
      this.renderString += "<div class='vertical-spacer-20px'></div>";
      
      debug(name + " - repeating: " + repeating + ", totalRepeatingRows: " + totalRepeatingRows + ", repeatMax: " + repeatMax, util_logInfo);
     
      // inspect the next ItemDef for look-ahead purposes.
      var nextItemDef = undefined;
      var nextColumnNumber = undefined;
      if (orderedItemIndex+1 < orderedItems.length) {
        nextItemDef = orderedItems[orderedItemIndex+1];
        var nextItemDetails = this.getItemDetails(nextItemDef, formDef);
        nextColumnNumber = nextItemDetails["@ColumnNumber"];
        var nextItemOID = nextItemDef["@OID"];
        nextGroupOID = app_itemGroupMap[nextItemOID].itemGroupKey;
        debug("next item column number: " + nextColumnNumber, util_logDebug);
      }       
      
      
      if (currentItemGroupOID != previousItemGroupOID) {
        repeatRowNumber = 1;
        isFirstRepeatingItem = true;
        idxForFirstItem =itemDetails["@OrderInForm"]; // this will be the first item in a grid.
      }
      
      itemDefRenderer = new ItemDefRenderer(itemDef, itemDetails, mandatory, formDef["@OID"], repeatRowNumber);
      var codeListOID = itemDef["CodeListRef"] ? itemDef["CodeListRef"]["@CodeListOID"] : undefined;
      var multiSelectListOID = itemDef["OpenClinica:MultiSelectListRef"] ? itemDef["OpenClinica:MultiSelectListRef"]["@MultiSelectListID"] : undefined;
      
      
      /*******************  REPEATING GROUP **********************************/
      // process repeating group of items 
      if (repeating == true) {
        
        if (nextGroupOID != currentItemGroupOID) {
          lastItemInRepeatingRow = true;
        }
        var orderNumber = app_itemGroupMap[itemOID].orderNumber;
        var itemGroupLength = app_itemGroupMap[itemOID].itemGroupLength;
        debug("repeating group: item " + orderNumber + " of " + itemGroupLength, util_logDebug);
       
        // in first item in repeating group
        if (isFirstRepeatingItem == true) {
          repeatingRowString = "<tr class='repeating_item_group'>";
          if (repeatRowNumber == 1) { 
            repeatingHeaderString = "<tr class='repeating_item_group'>";
          }
          isFirstRepeatingItem = false;
       }
        
        debug("REPEATING current: " + currentItemGroupOID + ", previous: " + previousItemGroupOID + " orderedItemIndex: " + 
               orderedItemIndex + ", limit: " + totalRepeatingRows + ", OID: " + itemOID, util_logInfo);
        
        repeatingRowString += itemDefRenderer.renderPrintableItem(repeating);
        var responseLayout = itemDetails["OpenClinica:ItemResponse"]["@ResponseLayout"];
        var responseType = itemDetails["OpenClinica:ItemResponse"]["@ResponseType"];
        
        if (responseLayout == "Horizontal") {
          var options = (responseType == 'multi-select' || responseType == 'checkbox') ? app_multiSelectLists[multiSelectListOID] : app_codeLists[codeListOID]; 
          var optionsLength = options == undefined ? 0 : options.length;
          var itemNameRow = "<tr class='repeating_item_option_names'><td colspan='" + optionsLength + "' align='center' class='repeating_item_option_names'>" + 
                            itemNumber + " " + name + "<br>" + itemName + "</td><td style='width:50px'></td></tr>";
          var optionsRow = "<tr class='repeating_item_group'>";
          for (var j=0;j< optionsLength;j++) {
            optionsRow += "<td valign='bottom' align='center' class='repeating_item_group'>" + options[j].label + "</td>";
          }
          optionsRow += "</tr>";
          
          if (repeatRowNumber == 1) { 
            repeatingHeaderString += "<td class='repeating_item_header' valign='bottom'><table>" + itemNameRow + optionsRow + "</table></td>";
          } 
        }
        else {
          if (repeatRowNumber == 1) { 
            repeatingHeaderString += "<td class='repeating_item_header' valign='bottom'>" + itemNumber + " " + name + (mandatory == true ? " *" : "") +  "<br>" + itemName + "</td>";
          }
        }
         
        // in last item in repeating group
        if (lastItemInRepeatingRow) {
          repeatingRowString += "</tr>";
          repeatingRows += repeatingRowString;
          if (repeatRowNumber == 1) { 
            repeatingHeaderString += "</tr>";
          }
          if (repeatRowNumber == totalRepeatingRows) { 
            this.renderString += RenderUtil.render(RenderUtil.get(
            "print_repeating_item_group"), {headerColspan:itemGroupLength, name:itemGroupHeader, tableHeader:repeatingHeaderString, tableBody:repeatingRows})[0].outerHTML; 
          } 
          else {
            repeatRowNumber++;
            orderedItemIndex = idxForFirstItem-2;// here we are offsetting by 2. This indicates the repeating grid needs to continue;
            
          }
        }
      }
      /*******************  REPEATING GROUP **********************************/
      
      

      // standard non-repeating items
      else if (repeating == false) { 
        if (columnNumber === undefined || columnNumber == 1) {
          this.renderString += itemHeader !== undefined ? "<div class='header-title'>"+itemHeader+"</div>" : "";
          this.renderString += itemSubHeader !== undefined ? "<div class='header-title'>"+itemSubHeader+"</div>" : "";
          this.renderString += "<table class='item-row'>";
        }
        this.renderString += itemDefRenderer.renderPrintableItem(repeating);
        if (columns == columnNumber || nextColumnNumber == 1 || nextColumnNumber === undefined || i+1 == orderedItems.length) {
          this.renderString += "</table>";
        }
      }
      previousItemGroupOID = currentItemGroupOID;
      prevSectionLabel = sectionLabel;
      prevItemHeader = itemHeader;
    }
    if(app_displayAudits)
    if(app_displayAudits=='y')//TODO: add flag for discrepancy notes as  a or clause 
    	{
    	var itemsOids = new Array();
    	this.renderPageHeader(this.PAGE_BREAK, app_printTime, app_studyContentPageType, app_eventName);
    	for (var orderedItemIndex=0;orderedItemIndex< orderedItems.length;orderedItemIndex++){
        	
        	var itemDef = orderedItems[orderedItemIndex];
        	var itemDetails = this.getItemDetails(itemDef, formDef);
        	 var itemGroupHeader = undefined;
      	
    		  itemDefRenderer = new ItemDefRenderer(itemDef, itemDetails, mandatory, formDef["@OID"], repeatRowNumber);
    		  this.renderString+="<div align='center'>"+formDef["@Name"]+"</div>";
    		if(typeof itemOids==='undefined' || itemOids.indexOf(itemDef["ItemOID"])<1)
    		{
    			itemGroupHeader = app_itemGroupDefs[app_itemGroupMap[itemOID].itemGroupKey].name;
    				this.renderString+=itemDefRenderer.renderItemFormMetadata();
    				itemsOids.push(itemDef["ItemOID"]);
    				  if(app_displayAudits=='y' && itemDefRenderer.audits){
    					  var auditLog = itemDefRenderer.audits["OpenClinica:AuditLog"];
    					  if(auditLog){
    		    		 
    						  var currentAuditLogs = [];
    						  auditLog = util_ensureArray(auditLog);
    		    		  for(var i=0;i<auditLog.length;i++){
    		    			  var thisAuditLog = {};
    		    			 
    		    			  var audits = auditLog[i];
    		    			  thisAuditLog.auditType = audits["@AuditType"];
    		    			  thisAuditLog.user = audits["@UserId"];
    		    			  thisAuditLog.dateTimeStamp = audits["@DateTimeStamp"];
    		    			  thisAuditLog.oldValue = audits["@OldValue"];
    		    			  thisAuditLog.newValue = audits["@NewValue"];
    		    			  currentAuditLogs.push(thisAuditLog);

    		    		  } 
    		    		  this.renderString+=itemDefRenderer.renderAuditLogs(currentAuditLogs,itemGroupHeader)
    					 
    					  }
    				  }
    		}
    		
    	}
    	
    	}
    
  }
  
  
  /* renderPageHeader()
   */
  this.renderPageHeader = function(pageBreak, printTime, currentPageType, currentPageEventName) {
    if (pageBreak == true) {
      this.renderString += "<div class='page-break-screen'><hr/></div>";
      this.renderString += "<div class='page-break'></div>";
    }
    this.renderString += pageHeaderRenderer.render(printTime, currentPageType, currentPageEventName)[0].outerHTML;
  }
  
  
  
  

	  this.renderAudits = function(){
		    
		  this.renderString+="<b>AUDITS</b>";
  }

  
  /* renderStudy()
   * When this is implemented it will render the web form
   */
  this.renderStudy = function() {
  }
  
}