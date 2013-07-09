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
  public JSONObject study;
  public Map<String, String> appBasicDefinitions = new TreeMap<String,String>();
  public Map<String, List> appCodeLists = new TreeMap<String,List>();
  public Map<String, List> appMultiSelectLists = new TreeMap<String,List>();
  public Map<String, ItemGroup> appItemGroupDefs = new TreeMap<String,ItemGroup>();
  public Map<String, Item> appItemGroupMap = new TreeMap<String,Item>();
  
  
  
  public static final int DEFAULT_MAX_REPEAT = 40; 
  public static final boolean NO_PAGE_BREAK = false; 
  public static final boolean PAGE_BREAK = true; 
  
  public JSONObject app_studyDetails;
  public String app_collectSubjectDOB;
  public String app_personIDRequired;
  public String app_showPersonID;
  public String app_interviewerNameRequired;
  public String app_interviewDateRequired;
  public String app_secondaryLabelViewable;
  public String app_eventLocationRequired;
  public String app_secondaryIDs;
  
  
  public StudyRenderer(JSON json, Configuration cfg, Map templateVars) {
    super(json, cfg, templateVars);
    this.study = (JSONObject)json;
  }
  
  
  /* getItemDetails(itemDef, formDef) 
  * A convenience function to get the ItemDetails properties for an Item
  */ 
  public JSONObject getItemDetails(JSONObject itemDef, JSONObject formDef) {
    if (itemDef.getJSONObject("OpenClinica:ItemDetails").get("OpenClinica:PresentInForm") instanceof JSONArray) {
      JSONArray itemPresentInForm = itemDef.getJSONObject("OpenClinica:ItemDetails").getJSONArray("OpenClinica:PresentInForm"); 
      for (int i=0;i< itemPresentInForm.size();i++) {
        if ((itemPresentInForm.getJSONObject(i).getString("@FormOID")).equals(formDef.getString("@OID"))) {
          return itemPresentInForm.getJSONObject(i);
        }
      }
      return null;
    }
    else {
      return  itemDef.getJSONObject("OpenClinica:ItemDetails").getJSONObject("OpenClinica:PresentInForm");
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
  
  

  

  
  
  public String parse(JSON json)  {
    return super.parse(json);
  }

  public String render(JSON json)  {
    JSONObject jsonObject = (JSONObject)json;
    JSONObject metaDataVersion = jsonObject.getJSONObject("MetaDataVersion");
    JSONArray itemDefs = metaDataVersion.getJSONArray("ItemDef");
    
    JSONObject formDef = jsonObject.getJSONObject("MetaDataVersion").getJSONObject("FormDef");
    
    //loadBasicDefinitions(json);
    //loadCodeLists(json);
    //loadItemGroupDefs(json);
    
    // Get Form Wrapper
    FormDefRenderer formDefRenderer = new FormDefRenderer(formDef, this.cfg, this.templateVars);
    String renderString = formDefRenderer.render(formDef);
    String repeatingRenderString = "";
    
    // Get Form Items
    String prevSectionLabel = "";
    String prevItemHeader = null;
    String prevItemSubHeader = null;
    boolean isFirstSection = true;
    
    for (int i=0;i< itemDefs.size();i++) {
      JSONObject itemDef = itemDefs.getJSONObject(i);
      String itemOID = itemDef.getString("@OID");
      
      JSONObject itemDetails = itemDef.getJSONObject("OpenClinica:ItemDetails");
      Object presentInForm = itemDetails.get("OpenClinica:ItemPresentInForm"); 
      JSONObject question;
      
      if (presentInForm instanceof JSONArray) {
        question = ((JSONArray)presentInForm).getJSONObject(1);
      }
      else {
        question = (JSONObject)presentInForm;
      }
      
      String itemNumber = itemDef.getJSONObject("Question").has("@OpenClinica:QuestionNumber") ? itemDef.getJSONObject("Question").getString("@OpenClinica:QuestionNumber") : "";
      String sectionLabel = question.getString("OpenClinica:SectionLabel");
      String itemHeader = question.has("OpenClinica:ItemHeader") ? question.getString("OpenClinica:ItemHeader") : null;
      String itemSubHeader = question.has("OpenClinica:ItemSubHeader")  ? question.getString("OpenClinica:ItemSubHeader") : null;
      String name = question.getString("OpenClinica:LeftItemText");
      int columnNumber = question.has("@ColumnNumber") ? question.getInt("@ColumnNumber") : 1;
      int columns = question.has("OpenClinica:Layout") ? question.getJSONObject("OpenClinica:Layout").getInt("@Columns") : 2; 
      System.out.println("#"+itemNumber+" column/columns: "+columnNumber+"/"+columns+ ", name: "+name+", section: "+sectionLabel+", header: "+itemHeader);
      
      if (sectionLabel.equals(prevSectionLabel) == false) {
        if (isFirstSection == true) {
          renderString += "<div class='blocking gray_bg'><h2>"+sectionLabel+"</h2></div>\n"; 
        }
        else {
          renderString += "<div class='blocking non-first_section_header gray_bg'><h2>"+sectionLabel+"</h2></div>\n"; 
        }
        isFirstSection = false;
      }
      if (itemHeader != null && itemHeader.equals(prevItemHeader) == false) {
        renderString += "<div class='blocking gray_bg'><h3>"+itemHeader+"</h3></div>\n"; 
      }
      if (itemSubHeader != null && itemSubHeader.equals(prevItemSubHeader) == false) {
        renderString += "<div class='blocking gray_bg'><h4>"+itemSubHeader+"</h4></div>\n"; 
      }
      int repeatNumber = appItemGroupMap.get(itemOID) != null ? ((ItemGroup)(appItemGroupDefs.get(appItemGroupMap.get(itemOID)))).repeatNumber : 1;
      boolean repeating = appItemGroupMap.get(itemOID) != null ? ((ItemGroup)(appItemGroupDefs.get(appItemGroupMap.get(itemOID)))).repeating : false;
      
      System.out.println("REPEATING:" + repeating+" REPEATNUMBER: " + repeatNumber);
     
      if (columnNumber == 1) {
        repeatingRenderString = "<div class='blocking'>\n";
      }
      
      ItemDefRenderer itemDefRenderer = new ItemDefRenderer(json, cfg, templateVars, appItemGroupDefs, appItemGroupMap, appBasicDefinitions, appCodeLists);
      repeatingRenderString += itemDefRenderer.render(itemDef);
      if (columns == columnNumber) {
        repeatingRenderString += "</div>";
        for (int repeatCounter=0;repeatCounter<repeatNumber;repeatCounter++) {
          renderString += repeatingRenderString;
        }
      }
     
      prevSectionLabel = sectionLabel;
      prevItemHeader = itemHeader;
    } 
    return renderString;
  }
  

  public String getOID() {
    return OID;
  }
  public void setOID(String OID) {
    this.OID = OID;
  }

}