package org.akaza.openclinica.renderer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import freemarker.template.Configuration;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

public class StudyRenderer extends JSONRenderer{

  public String OID;
  public String renderString = "";
  public JSONObject study;
  public Map<String, String> appBasicDefinitions = new TreeMap<String,String>();
  public Map<String, List> appCodeLists = new TreeMap<String,List>();
  public Map<String, List> appMultiSelectLists = new TreeMap<String,List>();
  public Map<String, ItemGroup> appItemGroupDefs = new TreeMap<String,ItemGroup>();
  public Map<String, Item> appItemGroupMap = new TreeMap<String,Item>();
  public JSONArray appStudyEventDefs = new JSONArray();
  public JSONArray appItemDefs = new JSONArray();
  public JSONArray appFormDefs = new JSONArray();
  public PageHeaderRenderer pageHeaderRenderer;
  public StudyDataLoader studyDataLoader;
  
  public static final int DEFAULT_MAX_REPEAT = 40; 
  public static final boolean NO_PAGE_BREAK = false; 
  public static final boolean PAGE_BREAK = true; 
  public static final String UNPOPULATED_FORM_CRF = "unpopulatedFormCrf"; 
  public static final String UNPOPULATED_EVENT_CRFS = "unpopulatedEventCrfs"; 
  public static final String UNPOPULATED_STUDY_CRFS = "unpopulatedStudyCrfs"; 
  public static final String UNPOPULATED_GLOBAL_CRF = "unpopulatedGlobalCrf"; 
  
  public JSONObject app_studyDetails;
  public String app_collectSubjectDOB;
  public String app_personIDRequired;
  public String app_showPersonID;
  public String app_interviewerNameRequired;
  public String app_interviewDateRequired;
  public String app_secondaryLabelViewable;
  public String app_eventLocationRequired;
  public String app_secondaryIDs;
  public String appStudyName;
  public String appSiteName;
  public String appProtocolName;
  public String appInvestigatorNameLabel;
  public String appInvestigatorSignatureLabel;
  public String appDateLabel;
  public String appEventOID;
  public String appPrintTime; 
  public String appEventName;
  
  public String appStudyEventCoverPageType;
  public String appStudyCoverPageType; 
  public String appStudyContentPageType; 
  public String appCrfHeader; 
  public String appSectionTitle; 
  public String appSectionSubtitle; 
  public String appSectionInstructions; 
  public String appSectionPage; 
  
  
  public StudyRenderer(JSONObject json, Configuration cfg, Map templateVars) {
    super(json, cfg, templateVars);
    this.study = (JSONObject) (json.get("Study") instanceof JSONObject ? json.getJSONObject("Study") : json.getJSONArray("Study").get(0));
  }
  
  
  
   
  /* setRenderMode(studyOID, eventOID, formVersionOID) 
  * A convenience function to set the render mode of a study
  */ 
  public String setRenderMode(String studyOID, String eventOID, String formVersionOID) {
    String renderMode = UNPOPULATED_FORM_CRF; 
    if (!studyOID.equals("*") && eventOID.equals("*") && formVersionOID.equals("*")) {
      renderMode = UNPOPULATED_STUDY_CRFS;
    }
    else if (!eventOID.equals("*") && formVersionOID.equals("*")) {
      renderMode = UNPOPULATED_EVENT_CRFS;
    }
    else if (studyOID.equals("*") && eventOID.equals("*") && !formVersionOID.equals("*")) {
      renderMode = UNPOPULATED_GLOBAL_CRF;
    }
    return renderMode;
  }
  
  
  
  /* getItemDetails(itemDef, formDef) 
  * A convenience function to get the ItemDetails properties for an Item
  */ 
  public JSONObject getItemDetails(JSONObject itemDef, JSONObject formDef) {
    if (itemDef.getJSONObject("OpenClinica:ItemDetails").get("OpenClinica:ItemPresentInForm") instanceof JSONArray) {
      JSONArray itemPresentInForm = itemDef.getJSONObject("OpenClinica:ItemDetails").getJSONArray("OpenClinica:ItemPresentInForm"); 
      for (int i=0;i< itemPresentInForm.size();i++) {
        if ((itemPresentInForm.getJSONObject(i).getString("@FormOID")).equals(formDef.getString("@OID"))) {
          return itemPresentInForm.getJSONObject(i);
        }
      }
      return null;
    }
    else {
      return  itemDef.getJSONObject("OpenClinica:ItemDetails").getJSONObject("OpenClinica:ItemPresentInForm");
    }
  }
  
  
  
  /* getSectionDetails(itemDetails, formDef) 
  * A convenience function to get the SectionDetails properties for an Item
  */ 
  public JSONObject getSectionDetails(JSONObject itemDetails, JSONObject formDef) {
	Object sectionObject = formDef.getJSONObject("OpenClinica:FormDetails").getJSONObject("OpenClinica:SectionDetails").get("OpenClinica:Section");  
    if (sectionObject instanceof JSONArray) {
      JSONArray sections = (JSONArray)sectionObject; 
      for (int i=0;i< sections.size();i++) {
        if ( (sections.getJSONObject(i).getString("@SectionLabel").equals(itemDetails.getString("OpenClinica:SectionLabel")))) {
          return sections.getJSONObject(i);
        }
      }
      return null; 
    }
    else {
      return (JSONObject)sectionObject;
    }
  }
  
  
 /* setStudyTitles(renderMode)
  * Set the current study being rendered
  */
  public void setStudyTitles(String renderMode) {
    if (renderMode == UNPOPULATED_FORM_CRF || renderMode == UNPOPULATED_EVENT_CRFS || renderMode == UNPOPULATED_STUDY_CRFS) {
      appStudyName = this.study.getJSONObject("GlobalVariables").getString("StudyName"); 
      appSiteName = this.study.getJSONObject("MetaDataVersion").getJSONObject("OpenClinica:StudyDetails").getString("@SiteName"); 
      appProtocolName = this.study.getJSONObject("GlobalVariables").getString("ProtocolName"); 
    }
    else if (renderMode == UNPOPULATED_GLOBAL_CRF) {
      appStudyName = "";
      appSiteName = "";
      appProtocolName = "";
    }
  } 
  
  /* createStudyEventCoverPage()
   */
  public String createStudyEventCoverPage(JSONObject eventDef) {
    String str = "<h3>" + eventDef.getString("@Name") + ":</h3>";
    JSONArray studyEventFormRefs =  StudyDataLoader.ensureArray(eventDef, "StudyEventDef"); 
    
    for (int i=0;i< studyEventFormRefs.size();i++) {
      JSONObject formRef = studyEventFormRefs.getJSONObject(i);
      boolean formFound = false;
      for (int j=0;j< appFormDefs.size() && !formFound;j++) {
        if (appFormDefs.getJSONObject(j).getString("@OID").equals(formRef.getString("@FormOID"))) {
          JSONObject formDef = appFormDefs.getJSONObject(j);
          JSONArray presentInEventDef = StudyDataLoader.ensureArray(formDef.getJSONObject("OpenClinica:FormDetails"), "OpenClinica:PresentInEventDefinition");
          for (int k=0;k< presentInEventDef.size() && !formFound;k++) {
            JSONObject inEventDef = presentInEventDef.getJSONObject(k);            
            if (inEventDef.getString("@IsDefaultVersion").equals("Yes") && inEventDef.getString("@HideCRF").equals("No") && 
                                      inEventDef.getString("@OID").equals(inEventDef.getString("StudyEvent@OID"))) {
              str += "<div>" + formDef.getString("@Name") + "</div>";
              formFound = true;
            }
            
          }
        }
      }
    }
    str += "<div class='investigator-text'>";
    str += appInvestigatorNameLabel + ": ___________________&nbsp;&nbsp;&nbsp;&nbsp;";
    str += appInvestigatorSignatureLabel +": ___________________&nbsp;&nbsp;&nbsp;&nbsp";
    str += appDateLabel +": ___________________";
    str += "</div>";
    return str;
  }
  
  
    
  /* createStudyCoverPage()
   */
  public String createStudyCoverPage() {
    String str = "<table border='1' style='margin-top:50px'>";
    // create header row of Study Events
    str += "<tr><td style='width:200px'></td>";
    // iterate over study event defs and examine each event
    for (int i=0;i< appStudyEventDefs.size();i++) {
      JSONObject eventDef = appStudyEventDefs.getJSONObject(i);
      // load event name into column header
      str +="<td style='padding:10px'>" + eventDef.getString("@Name") + "</td>";
    }
    str += "</tr>";
    boolean formFound = false;
    //iterate over each of the formDefs in the study    
    for (int i=0;i< appFormDefs.size();i++) {
      JSONObject formDef = appFormDefs.getJSONObject(i);
      boolean formVersionPrinted = false;
      JSONArray presentInEventDef = StudyDataLoader.ensureArray(formDef.getJSONObject("OpenClinica:FormDetails"), "OpenClinica:PresentInEventDefinition");
      int initEventCNTR = 0;
      for (int j=0;j< presentInEventDef.size() && !formFound;j++) {
        formFound = false;
        JSONObject inEventDef = presentInEventDef.getJSONObject(j);            
        if (inEventDef.getString("@IsDefaultVersion").equals("Yes") && inEventDef.getString("@HideCRF").equals("No")) {
          if (formVersionPrinted == false) {
            str +="<tr><td style='padding:10px'>" + formDef.getString("@Name") + "</td>";
          }
          formVersionPrinted = true;
          for (int k=initEventCNTR;k< appStudyEventDefs.size() && !formFound;k++) {
            JSONObject eventDef = appStudyEventDefs.getJSONObject(k);            
            if (eventDef.getString("@OID").equals(inEventDef.getString("@StudyEventOID"))) {
              str += "<td style='text-align:center'>X</td>";
              formFound = true; 
              initEventCNTR = j+1;
            }
            if(formFound == false) {
              str += "<td></td>";
            }
          }
          if(formFound == false) {
            str += "<td></td>";
          }
        }
      }
      
      if (initEventCNTR < appStudyEventDefs.size() && formVersionPrinted) {
        for(int c = initEventCNTR; c < appStudyEventDefs.size();c++){
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
  public void renderPrintableEventCRFs(String renderMode, JSONObject eventDef, boolean pageBreak) {
    appEventName = eventDef.getString("@Name");
    /*
    renderPageHeader(pageBreak,        
       protocolIdLabel,
       studyNameLabel,
       siteNameLabel,
       printTime,
       pageType,
       studyName,
       siteName,
       protocolName,
       eventName,
       collectSubjectDOB,
       personIdRequired,
       studySubjectIdLabel,
       eventDateLabel,
       eventNameLabel,
       eventLocation,
       eventLocationLabel,
       studyCoverPageType,
       personIdLabel,
       studySubjectDOBLabel,
       studySubjectBirthYearLabel,
       interviewerNameRequired,
       interviewerLabel,
       interviewDateRequired,
       interviewDateLabel,
       secondaryLabel,
       secondaryLabelViewable,
       eventLocationRequired,
       showPersonId,
       eventLocationDateRequired );
       */
    renderString += this.createStudyEventCoverPage(eventDef);
    // select all CRFs from StudyEvent
    JSONArray studyEventFormRefs = StudyDataLoader.ensureArray(eventDef, "FormDef");
    
    for (int i=0;i< studyEventFormRefs.size();i++) {
      pageBreak = this.PAGE_BREAK;
      boolean defaultDisplayed = false;
      JSONObject formRef = studyEventFormRefs.getJSONObject(i);
      for (int j=0;j< appFormDefs.size() && !defaultDisplayed;j++) {
        if (appFormDefs.getJSONObject(j).getString("@OID").equals(formRef.getString("@FormOID"))) {
          JSONObject formDef = appFormDefs.getJSONObject(j);
          JSONArray presentInEventDef = StudyDataLoader.ensureArray(formDef.getJSONObject("OpenClinica:FormDetails"), "OpenClinica:PresentInEventDefinition");
          for(int k=0;k<presentInEventDef.size();k++){
            JSONObject inEventDef = presentInEventDef.getJSONObject(k);
            if (inEventDef.getString("@IsDefaultVersion").equals("Yes") && inEventDef.getString("@HideCRF").equals("No") && 
                                     inEventDef.getString("@OID").equals(inEventDef.getString("StudyEvent@OID"))) {
              renderPrintableFormDef(formDef, pageBreak);
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
  public String renderPrintableStudy(JSONObject study, String studyOID, String eventOID, String formVersionOID) {
    String renderMode = setRenderMode(studyOID, eventOID, formVersionOID);
    pageHeaderRenderer = new PageHeaderRenderer(this.study, this.cfg, this.templateVars);
    setStudyTitles(renderMode);  
    studyDataLoader = new StudyDataLoader(this); 
    studyDataLoader.loadStudyLists();   
    JSONObject formDef = null;
    JSONObject eventDef = null;
    
    if (renderMode.equals(UNPOPULATED_FORM_CRF) || renderMode.equals(UNPOPULATED_GLOBAL_CRF)) {
      // select CRF by OID
      for (int i=0;i< appFormDefs.size();i++) {
        if (appFormDefs.getJSONObject(i).getString("@OID").equals(formVersionOID)) {
          formDef = appFormDefs.getJSONObject(i);
          break; 
        }
      }
      renderPrintableFormDef(formDef, this.NO_PAGE_BREAK);
    }
    else if (renderMode.equals(UNPOPULATED_EVENT_CRFS)) {
      // select StudyEvent by OID
      for (int i=0;i< appStudyEventDefs.size();i++) {
        if (appStudyEventDefs.getJSONObject(i).getString("@OID").equals(appEventOID)) {
          eventDef = appStudyEventDefs.getJSONObject(i);
          break;
        }
      }
      renderPrintableEventCRFs(renderMode, eventDef, this.NO_PAGE_BREAK);
    }
    else if (renderMode.equals(UNPOPULATED_STUDY_CRFS)) {
      /* 
      renderPageHeader(this.NO_PAGE_BREAK, 
             protocolIdLabel,
       studyNameLabel,
       siteNameLabel,
       printTime,
       pageType,
       studyName,
       siteName,
       protocolName,
       eventName,
       collectSubjectDOB,
       personIdRequired,
       studySubjectIdLabel,
       eventDateLabel,
       eventNameLabel,
       eventLocation,
       eventLocationLabel,
       studyCoverPageType,
       personIdLabel,
       studySubjectDOBLabel,
       studySubjectBirthYearLabel,
       interviewerNameRequired,
       interviewerLabel,
       interviewDateRequired,
       interviewDateLabel,
       secondaryLabel,
       secondaryLabelViewable,
       eventLocationRequired,
       showPersonId,
       eventLocationDateRequired 
       );
       */
      this.renderString += this.createStudyCoverPage();
      // select all CRFs from study
      for (int i=0;i< appStudyEventDefs.size();i++) {
        eventDef = appStudyEventDefs.getJSONObject(i);
        renderPrintableEventCRFs(renderMode, eventDef, this.PAGE_BREAK);
      }
    }
    return this.renderString;
  }
  
  
  
  public String parse(JSON json)  {
    return super.parse(json);
  }
  
  
    
  /* renderPrintableFormDef(formDef, pageBreak)
   * The heart of StudyRenderer: render the CRF
   */
  public void renderPrintableFormDef(JSONObject formDef, boolean pageBreak) {
  /* 
    renderPageHeader(pageBreak,
       protocolIdLabel,
       studyNameLabel,
       siteNameLabel,
       printTime,
       pageType,
       studyName,
       siteName,
       protocolName,
       eventName,
       collectSubjectDOB,
       personIdRequired,
       studySubjectIdLabel,
       eventDateLabel,
       eventNameLabel,
       eventLocation,
       eventLocationLabel,
       studyCoverPageType,
       personIdLabel,
       studySubjectDOBLabel,
       studySubjectBirthYearLabel,
       interviewerNameRequired,
       interviewerLabel,
       interviewDateRequired,
       interviewDateLabel,
       secondaryLabel,
       secondaryLabelViewable,
       eventLocationRequired,
       showPersonId,
       eventLocationDateRequired ); 
       */
    List orderedItems = new ArrayList();
    studyDataLoader.loadItemGroupDefs(formDef);
    
    // Get Form Wrapper
    FormDefRenderer formDefRenderer = new FormDefRenderer(this.study, this.cfg, this.templateVars);
    formDefRenderer.parse(formDef);
    this.renderString += appCrfHeader = formDefRenderer.renderPrintableForm(formDef);
    String repeatingHeaderString = "";
    String repeatingRowString = "";
    String currentItemGroupOID = "";
    String previousItemGroupOID = "";
    boolean isFirstRepeatingItem = true;
    int lastRepeatingOrderInFormNumber;
 
    // Get Form Items
    String prevSectionLabel = "";
    String prevItemHeader = "";
    String prevItemSubHeader = "";
    boolean isFirstSection = true;
    
    // Sort itemDefs by OrderInForm property
    for (int i=0;i< appItemDefs.size();i++) {
      JSONObject itemDef = appItemDefs.getJSONObject(i);
      JSONObject itemDetails = getItemDetails(itemDef, formDef);
      if (itemDetails.getString("@FormOID").equals(formDef.getString("@OID"))) {
        int orderInForm = itemDetails.getInt("@OrderInForm");
        orderedItems.add(orderInForm-1, itemDef);
      }
    }
    
    for (int i=0;i< orderedItems.size();i++) {
      String repeatingRows = "";
      JSONObject itemDef = (JSONObject)orderedItems.get(i);
      String itemOID = itemDef.getString("@OID");
      String itemNumber = "";
      if (itemDef.has("Question") && itemDef.getJSONObject("Question").has("@OpenClinica:QuestionNumber")) {
        itemNumber = itemDef.getJSONObject("Question").getString("@OpenClinica:QuestionNumber");
      }
      
      JSONObject itemDetails = this.getItemDetails(itemDef, formDef);
      JSONObject sectionDetails = this.getSectionDetails(itemDetails, formDef);
      String sectionTitle = sectionDetails.optString("@SectionTitle");
      String sectionSubTitle = sectionDetails.optString("@SectionSubtitle");
      String sectionInstructions = sectionDetails.optString("@SectionInstructions");
      String sectionPageNumber = sectionDetails.optString("@SectionPageNumber");
      String sectionLabel = itemDetails.optString("OpenClinica:SectionLabel");
      String itemHeader = itemDetails.optString("OpenClinica:ItemHeader");
      String itemSubHeader = itemDetails.optString("OpenClinica:ItemSubHeader");
      String name = itemDetails.optString("OpenClinica:LeftItemText");
      String rightItem = itemDetails.optString("OpenClinica:RightItemText"); 
      int columnNumber = itemDetails.optInt("@ColumnNumber");
      int columns = 0;
      if (itemDetails.has("OpenClinica:Layout")) {
        columns = itemDetails.getJSONObject("OpenClinica:Layout").getInt("@Columns");
      }
      boolean lastItemInRepeatingRow = false;
      int repeatNumber = 1;
      boolean repeating = false;
      boolean mandatory = false;
      int repeatMax = this.DEFAULT_MAX_REPEAT; 
      String itemGroupHeader;
      String itemGroupName;
      String nextGroupOID = "";
      
     if (appItemGroupMap.get(itemOID) != null && appItemGroupDefs.get( appItemGroupMap.get(itemOID).itemGroupKey) != null) {
        currentItemGroupOID = appItemGroupMap.get(itemOID).itemGroupKey;
        mandatory = appItemGroupMap.get(itemOID).mandatory;
        ItemGroup itemGroup = appItemGroupDefs.get( appItemGroupMap.get(itemOID).itemGroupKey);
        repeatNumber = itemGroup.repeatNumber;
        repeating = itemGroup.repeating;
        repeatMax = itemGroup.repeatMax;
        itemGroupName = itemGroup.name;
        itemGroupHeader = itemGroup.groupHeader;
      }
      
      if (sectionLabel.equals(prevSectionLabel) == false) {
        if (isFirstSection == false) {
        /*
       renderPageHeader(this.PAGE_BREAK, 
       protocolIdLabel,
       studyNameLabel,
       siteNameLabel,
       printTime,
       pageType,
       studyName,
       siteName,
       protocolName,
       eventName,
       collectSubjectDOB,
       personIdRequired,
       studySubjectIdLabel,
       eventDateLabel,
       eventNameLabel,
       eventLocation,
       eventLocationLabel,
       studyCoverPageType,
       personIdLabel,
       studySubjectDOBLabel,
       studySubjectBirthYearLabel,
       interviewerNameRequired,
       interviewerLabel,
       interviewDateRequired,
       interviewDateLabel,
       secondaryLabel,
       secondaryLabelViewable,
       eventLocationRequired,
       showPersonId,
       eventLocationDateRequired );
       */
        }
        this.renderString += "<div class='vertical-spacer-30px'></div>";
        this.renderString += "<div class='section-title'>"+appSectionTitle+"&nbsp;"+sectionTitle+"</div>";
        this.renderString += "<div class='section-info'>"+appSectionSubtitle+"&nbsp;"+sectionSubTitle+"</div>";
        this.renderString += "<div class='section-info'>"+appSectionInstructions+"&nbsp;"+sectionInstructions+"</div>";
        this.renderString += "<div class='section-info'>"+appSectionPage+"&nbsp;"+sectionPageNumber+"</div>";
        isFirstSection = false;
      }
      this.renderString += "<div class='vertical-spacer-20px'></div>";
      
     
      // inpect the next ItemDef for look-ahead purposes.
      JSONObject nextItemDef;
      int nextColumnNumber = 0;
      if (i+1 < orderedItems.size()) {
        nextItemDef = (JSONObject)orderedItems.get(i+1);
        JSONObject nextItemDetails = getItemDetails(nextItemDef, formDef);
        nextColumnNumber = nextItemDetails.getInt("@ColumnNumber");
        String nextItemOID = nextItemDef.getString("@OID");
        nextGroupOID = appItemGroupMap.get(nextItemOID).itemGroupKey;
      }       
      
      ItemDefRenderer itemDefRenderer = new ItemDefRenderer(itemDef, this.cfg, this.templateVars,appItemGroupDefs, appItemGroupMap, appBasicDefinitions, appCodeLists, appMultiSelectLists);
      
      String codeListOID = "";
      if (itemDef.has("CodeLisRef")) {
        codeListOID = itemDef.getJSONObject("CodeListRef").getString("@CodeListOID");
      } 
      String multiSelectListOID = "";
      if (itemDef.has("OpenClinica:MultiSelectListRef")) {
        multiSelectListOID = itemDef.getJSONObject("OpenClinica:MultiSelectListRef").getString("@MultiSelectListID");
      }
      
      // process repeating group of items 
      if (repeating == true) {
        if (currentItemGroupOID.equals(previousItemGroupOID) == false) {
          isFirstRepeatingItem = true;
        }
        if(nextGroupOID.equals(currentItemGroupOID) == false ){
          lastItemInRepeatingRow = true;
        }
        int orderNumber = appItemGroupMap.get(itemOID).orderNumber;
        int itemGroupLength = appItemGroupMap.get(itemOID).itemGroupLength;
       
        // in first item in repeating group
        if (isFirstRepeatingItem == true) {
          repeatingRowString = "<tr class='repeating_item_group'>";
          repeatingHeaderString = "<tr class='repeating_item_group'>";
          isFirstRepeatingItem = false;
          lastRepeatingOrderInFormNumber = i + itemGroupLength - 1;
       }
        
        repeatingRowString += itemDefRenderer.renderPrintableItem(repeating);
        String responseLayout = itemDetails.getJSONObject("OpenClinica:ItemResponse").getString("@ResponseLayout");
        String responseType = itemDetails.getJSONObject("OpenClinica:ItemResponse").getString("@ResponseType");
        
        if (responseLayout.equals("Horizontal")) {
          List options;
          if (responseType.equals("multi-select") || responseType.equals("checkbox")) {
            options = appMultiSelectLists.get(multiSelectListOID);
          }
          else {
            options = appCodeLists.get(codeListOID);
          }
          int optionsLength = 0; 
          if (options != null) {
            optionsLength = options.size();
          }
          String itemNameRow = "<tr class='repeating_item_option_names'><td colspan='" + optionsLength + "' align='center'>" + itemNumber + " " + name + "</td></tr>";
          String optionsRow = "<tr class='repeating_item_group'>";
          for (int j=0;j< optionsLength;j++) {
            optionsRow += "<td valign='bottom' align='center' class='repeating_item_group'>" + ((Map)options.get(j)).get("label") + "</td>";
          }
          optionsRow += "</tr>";
          repeatingHeaderString += "<td class='repeating_item_header' valign='bottom'><table>" + itemNameRow + optionsRow + "</table></td>";
        }
        else {
          repeatingHeaderString += "<td class='repeating_item_header' valign='bottom'>" + itemNumber + " " + name + (mandatory == true ? " *" : "") + "</td>";
        }
         
        // in last item in repeating group
        if (lastItemInRepeatingRow) {
          repeatingRowString += "</tr>";
          repeatingHeaderString += "</tr>";
          for (int repeatCounter=0;repeatCounter<repeatNumber;repeatCounter++) {
            repeatingRows += repeatingRowString;
          }
          //this.renderString += RenderUtil.render(RenderUtil.get( "print_repeating_item_group"), {headerColspan:itemGroupLength, name:itemGroupHeader, tableHeader:repeatingHeaderString, tableBody:repeatingRows});
        }
      }
      // standard non-repeating items
      else if (repeating == false) { 
        if ( columnNumber == 1) {
          this.renderString += itemHeader != null ? "<div class='header-title'>"+itemHeader+"</div>" : "";
          this.renderString += itemSubHeader != null ? "<div class='header-title'>"+itemSubHeader+"</div>" : "";
          this.renderString += "<table class='item-row'>";
        }
        this.renderString += itemDefRenderer.renderPrintableItem(repeating);
        if (columns == columnNumber || nextColumnNumber == 1 || i+1 == orderedItems.size()) {
          this.renderString += "</table>";
        }
      }
      previousItemGroupOID = currentItemGroupOID;
      prevSectionLabel = sectionLabel;
      prevItemHeader = itemHeader;
    }
  }

  
  /* renderPageHeader()
   */
  public void renderPageHeader (
    boolean pageBreak, 
    String protocolIdLabel,
    String studyNameLabel,
    String siteNameLabel,
    String printTime,
    String pageType,
    String studyName,
    String siteName,
    String protocolName,
    String eventName,
    String collectSubjectDOB,
    String personIdRequired,
    String studySubjectIdLabel,
    String eventDateLabel,
    String eventNameLabel,
    String eventLocation,
    String eventLocationLabel,
    String studyCoverPageType,
    String personIdLabel,
    String studySubjectDOBLabel,
    String studySubjectBirthYearLabel,
    String interviewerNameRequired,
    String interviewerLabel,
    String interviewDateRequired,
    String interviewDateLabel,
    String secondaryLabel,
    String secondaryLabelViewable,
    String eventLocationRequired,
    String showPersonId,
    String eventLocationDateRequired) {
  
    if (pageBreak == true) {
      renderString += "<div class='page-break-screen'><hr/></div>";
      renderString += "<div class='page-break'></div>";
    }
    renderString += pageHeaderRenderer.render(
       json, 
       protocolIdLabel,
       studyNameLabel,
       siteNameLabel,
       printTime,
       pageType,
       studyName,
       siteName,
       protocolName,
       eventName,
       collectSubjectDOB,
       personIdRequired,
       studySubjectIdLabel,
       eventDateLabel,
       eventNameLabel,
       eventLocation,
       eventLocationLabel,
       studyCoverPageType,
       personIdLabel,
       studySubjectDOBLabel,
       studySubjectBirthYearLabel,
       interviewerNameRequired,
       interviewerLabel,
       interviewDateRequired,
       interviewDateLabel,
       secondaryLabel,
       secondaryLabelViewable,
       eventLocationRequired,
       showPersonId,
       eventLocationDateRequired    
     );
    
  }

  

  public String getOID() {
    return OID;
  }
  public void setOID(String OID) {
    this.OID = OID;
  }

}