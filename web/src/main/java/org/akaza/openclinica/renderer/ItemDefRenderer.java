package org.akaza.openclinica.renderer;

import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import freemarker.template.Configuration;
import freemarker.template.Template;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ItemDefRenderer extends JSONRenderer{
  public JSONObject itemDef;
  private JSONObject itemDetails;
  private boolean mandatory;
  private String template;
  private String name;
  private String rightItemText;
  private String dataType;
  private String responseType;
  private String responseLayout;
  private String isInline;
  private String OID;
  private String itemName;
  private String itemNumber;
  private String unitLabel;
  private String codeListOID;
  private String multiSelectListOID;
  private int columns;
  private  Map<String, ItemGroup> appItemGroupDefs = new TreeMap<String,ItemGroup>();
  private  Map<String, String> appItemGroupMap = new TreeMap<String,String>();
  private  Map<String, String> appBasicDefinitions = new TreeMap<String,String>();
  private  Map<String, List> appCodeLists = new TreeMap<String,List>();
  private  Map<String, List> appMultiSelectLists = new TreeMap<String,List>();
  
  public ItemDefRenderer(JSONObject itemDef, Configuration cfg, Map templateVars, 
                         Map appItemGroupDefs, Map appItemGroupMap,
                         Map appBasicDefinitions, Map appCodeLists, Map appMultiSelectLists) {
    super(itemDef, cfg, templateVars);
    this.itemDef = itemDef;
    this.appItemGroupDefs = appItemGroupDefs;
    this.appItemGroupMap = appItemGroupMap;
    this.appBasicDefinitions = appBasicDefinitions;
    this.appCodeLists = appCodeLists;
    this.appMultiSelectLists = appMultiSelectLists;
  }
  
  
  public String parse(JSONObject itemDef)  {
    itemDetails = itemDef.getJSONObject("OpenClinica:ItemDetails");
    JSONArray presentInForm = StudyDataLoader.ensureArray(itemDef.getJSONObject("OpenClinica:ItemDetails"), "OpenClinica:ItemPresentInForm");
    OID = itemDef.optString("@OID");
    dataType = itemDef.optString("@DataType");
    name = itemDetails.optString("OpenClinica:LeftItemText");
    itemName = itemDetails.optString("@Name");
    responseType = itemDetails.getJSONObject("OpenClinica:ItemPresentInForm").getJSONObject("OpenClinica:ItemResponse").optString("@ResponseType");
    responseLayout = itemDetails.getJSONObject("OpenClinica:ItemPresentInForm").getJSONObject("OpenClinica:ItemResponse").optString("@ResponseLayout");
    isInline = responseLayout == "Horizontal" ? "inline" : "";
    rightItemText = itemDetails.optString("OpenClinica:RightItemText");
    itemNumber = itemDef.getJSONObject("Question").has("@OpenClinica:QuestionNumber") ? itemDef.getJSONObject("Question").getString("@OpenClinica:QuestionNumber") + "." : "";
    unitLabel = itemDef.has("MeasurementUnitRef") ? appBasicDefinitions.get(itemDef.getJSONObject("MeasurementUnitRef").getString("@MeasurementUnitOID")) : ""; 
    codeListOID = itemDef.has("CodeListRef") ? itemDef.getJSONObject("CodeListRef").getString("@CodeListOID") : "";
    multiSelectListOID = itemDef.has("MultiSelectListRef") ? itemDef.getJSONObject("MultiSelectListRef").getString("@MultiSelectListOID") : "";
    //JSONObject question = ((JSONArray)presentInForm).getJSONObject(1);
    //columns = question.has("OpenClinica:Layout") ? question.getJSONObject("OpenClinica:Layout").getInt("@Columns") : 2; 
    
   return "";
  }

  public String renderPrintableItem(boolean isRepeating)  {
    parse(this.itemDef);
    String  template = "item_def.ftl";
    
    if (isRepeating == true) {
      template = responseLayout == "Horizontal" ? "repeating_item_horiz" : "repeating_item";
    }
   
    JSONObject jsonObject = (JSONObject)json;
    try {
      Template t = cfg.getTemplate(template);
      StringWriter sw = new StringWriter(); 
      templateVars.put("itemNumber", itemNumber);
      templateVars.put("name", name);
      templateVars.put("rightItemText", rightItemText);
      templateVars.put("responseType", responseType);
      templateVars.put("unitLabel", unitLabel);
      templateVars.put("optionNames", appCodeLists.get(codeListOID));
      templateVars.put("multiSelectOptionNames", appMultiSelectLists.get(multiSelectListOID));
      templateVars.put("columns", columns);
      templateVars.put("responseLayout", responseLayout);
      templateVars.put("isInline", isInline);
      templateVars.put("mandatory", mandatory);
      templateVars.put("itemName", itemName);
      
      t.process(templateVars, sw);
      return sw.toString();
    } 
    catch (Exception e) {
      e.printStackTrace();
    } 
   return "";
  }

  public String getOID() { return OID; }
  public void setOID(String OID) { this.OID = OID; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getDataType() { return dataType; }
  public void setDataType(String dataType  ) { this.dataType = dataType; }
  
  public String getItemNumber() { return itemNumber; }
  public void setItemNumber(String itemNumber) { this.itemNumber = itemNumber; }
  
  public String getUnitLabel() { return unitLabel; }
  public void setUnitLabel(String unitLabel) { this.unitLabel = unitLabel; }
  
  public String getCodeListOID() { return codeListOID; }
  public void setCodeListOID(String codeListOID) { this.codeListOID = codeListOID; }
  
  public int getColumns() { return columns; }
  public void setColumns(int columns) { this.columns = columns; }
  
  public String getTemplate() { return template; }
  public void setTemplate(String template) { this.template = template; }
  
  public JSONObject getItemDetails() { return itemDetails; }
  public void setItemDetails(JSONObject itemDetails) { this.itemDetails = itemDetails; }

  public boolean isMandatory() { return mandatory; }
  public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }

  public String getRightItemText() { return rightItemText; }
  public void setRightItemText(String rightItemText) { this.rightItemText = rightItemText; }

  public String getResponseType() { return responseType; }
  public void setResponseType(String responseType) { this.responseType = responseType; }

  public String getResponseLayout() { return responseLayout; }
  public void setResponseLayout(String responseLayout) { this.responseLayout = responseLayout; }

  public String isInline() { return isInline; }
  public void setInline(String isInline) { this.isInline = isInline; }

  public String getItemName() { return itemName; }
  public void setItemName(String itemName) { this.itemName = itemName; }

  public String getMultiSelectListOID() { return multiSelectListOID; }
  public void setMultiSelectListOID(String multiSelectListOID) { this.multiSelectListOID = multiSelectListOID; }
    
}  