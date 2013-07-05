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


  private String OID;
  public  Map<String, String> appBasicDefinitions = new TreeMap<String,String>();
  public  Map<String, List> appCodeLists = new TreeMap<String,List>();
  private  Map<String, ItemGroup> appItemGroupDefs = new TreeMap<String,ItemGroup>();
  private  Map<String, String> appItemGroupMap = new TreeMap<String,String>();
  
  public static final int DEFAULT_MAX_REPEAT = 40; 
  public static final boolean NO_PAGE_BREAK = false; 
  public static final boolean PAGE_BREAK = true; 
  
  
  public StudyRenderer(JSON json, Configuration cfg, Map templateVars) {
    super(json, cfg, templateVars);
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
  
  
  private void loadBasicDefinitions(JSON json) {
    JSONObject jsonObject = (JSONObject)json;
    JSONArray basicDefinitions = jsonObject.getJSONObject("BasicDefinitions").getJSONArray("MeasurementUnit");
    System.out.println("loading basic definitions");
    for (int i=0;i< basicDefinitions.size();i++) {
      String key = basicDefinitions.getJSONObject(i).getString("@OID");
      String value = basicDefinitions.getJSONObject(i).getString("@Name");
      appBasicDefinitions.put(key, value);
    }
  }
  
  
  private void loadCodeLists(JSON json) {
    JSONObject jsonObject = (JSONObject)json;
    JSONArray codeLists = jsonObject.getJSONObject("MetaDataVersion").getJSONArray("CodeList");
    System.out.println("loading code lists");
    for (int i=0;i< codeLists.size();i++) {
      String codeListKey = codeLists.getJSONObject(i).getString("@OID");
      List currentCodeList = new ArrayList();
      JSONArray codeListItems = codeLists.getJSONObject(i).getJSONArray("CodeListItem");
      for (int j=0;j< codeListItems.size();j++) {
        JSONObject codeListItem = codeListItems.getJSONObject(j);
        String id = codeListItem.getString("@CodedValue");
        String label = codeListItem.getJSONObject("Decode").getString("TranslatedText");
        CodeListItem currentCodeListItem = new CodeListItem(id,label);
        currentCodeList.add(currentCodeListItem);
      }
      appCodeLists.put(codeListKey, currentCodeList);
    }
  }
  
  
  private void loadItemGroupDefs(JSON json) {
    JSONObject jsonObject = (JSONObject)json;
    JSONArray itemGroupDefs = jsonObject.getJSONObject("MetaDataVersion").getJSONArray("ItemGroupDef");
    System.out.println("loading item groups");
    for (int i=0;i< itemGroupDefs.size();i++) {
      JSONObject itemGroupDef = itemGroupDefs.getJSONObject(i);
      
      JSON itemRefs = (JSON)itemGroupDef.get("ItemRef"); 
      
      String itemGroupKey = itemGroupDef.getString("@OID");
      JSONObject itemGroupDetails = itemGroupDef.getJSONObject("OpenClinica:ItemGroupDetails"); 
     
     Object presentInForm = itemGroupDetails.get("OpenClinica:PresentInForm"); 
      JSONObject itemGroupRepeat;
      if (presentInForm instanceof JSONArray ) {
        itemGroupRepeat = ((JSONArray)presentInForm).getJSONObject(i).getJSONObject("OpenClinica:ItemGroupRepeat");
      }
      else {
        itemGroupRepeat = ((JSONObject)presentInForm).getJSONObject("OpenClinica:ItemGroupRepeat");
      }
      
      int repeatNumber = itemGroupRepeat.getInt("@RepeatNumber");
      
      
      
      
      boolean repeating = ParseUtil.parseYesNo(itemGroupDef.getString("@Repeating"));
      System.out.println("Item Group " +itemGroupKey+ " repeating? "+repeating+", repeat number: "+ repeatNumber);
      ItemGroup currentItemGroup = new ItemGroup(repeatNumber, repeating);
      appItemGroupDefs.put(itemGroupKey, currentItemGroup); 
      String itemKey = "";
      if (itemRefs instanceof JSONArray ) {
        for (int j=0;j< ((JSONArray)itemRefs).size();j++) {
          itemKey = ((JSONArray)itemRefs).getJSONObject(j).getString("@ItemOID");
          appItemGroupMap.put(itemKey, itemGroupKey);
        }
      }
      else {
         itemKey = ((JSONObject)itemRefs).getString("@ItemOID");
         appItemGroupMap.put(itemKey, itemGroupKey);
      }
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
    
    loadBasicDefinitions(json);
    loadCodeLists(json);
    loadItemGroupDefs(json);
    
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