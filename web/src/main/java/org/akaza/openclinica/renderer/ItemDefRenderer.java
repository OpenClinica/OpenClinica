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

  private String template;
  private JSONObject itemDetails;
  private boolean mandatory;
  private String name;
  private String rightItemText;
  private String dataType;
  private String responseType;
  private String responseLayout;
  private boolean isInline;
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
  
  public ItemDefRenderer(JSON json, Configuration cfg, Map templateVars, 
                         Map appItemGroupDefs, Map appItemGroupMap,
                         Map appBasicDefinitions, Map appCodeLists) {
    super(json, cfg, templateVars);
    this.appItemGroupDefs = appItemGroupDefs;
    this.appItemGroupMap = appItemGroupMap;
    this.appBasicDefinitions = appBasicDefinitions;
    this.appCodeLists = appCodeLists;
  }
  
  
  public String parse(JSON json)  {
    JSONObject itemDef = (JSONObject)json;
    //JSONObject question = itemDef.getJSONObject("OpenClinica:ItemDetails").getJSONArray("OpenClinica:ItemPresentInForm").getJSONObject(1);
    itemDetails = itemDef.getJSONObject("OpenClinica:ItemDetails");
    Object presentInForm = itemDetails.get("OpenClinica:ItemPresentInForm"); 
    JSONObject question;
    
    if (presentInForm instanceof JSONArray) {
      question = ((JSONArray)presentInForm).getJSONObject(1);
    }
    else {
      question = (JSONObject)presentInForm;
    }
    OID = itemDef.getString("@OID");
    dataType = itemDef.getString("@DataType");
    name = question.getString("OpenClinica:LeftItemText");
    itemName = question.getString("@Name");
    responseType = question.getJSONObject("OpenClinica:ItemResponse").getString("@ResponseType");
    responseType = question.getJSONObject("OpenClinica:ItemResponse").getString("@ResponseLayout");
    isInline = true;
    if (responseLayout.equals("Horizontal")) {
      isInline = false;
    }  
    itemNumber = itemDef.getJSONObject("Question").has("@OpenClinica:QuestionNumber") ? itemDef.getJSONObject("Question").getString("@OpenClinica:QuestionNumber") + "." : "";
    unitLabel = itemDef.has("MeasurementUnitRef") ? appBasicDefinitions.get(itemDef.getJSONObject("MeasurementUnitRef").getString("@MeasurementUnitOID")) : ""; 
    codeListOID = itemDef.has("CodeListRef") ? itemDef.getJSONObject("CodeListRef").getString("@CodeListOID") : "";
    codeListOID = itemDef.has("MultiSelectListRef") ? itemDef.getJSONObject("MultiSelectListRef").getString("@MultiSelectListOID") : "";
    columns = question.has("OpenClinica:Layout") ? question.getJSONObject("OpenClinica:Layout").getInt("@Columns") : 2; 
    
   return "";
  }

  public String render(JSON json, boolean isRepeating)  {
    parse(json);
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
      //templateVars.put("multiSelectOptionNames", StudyRenderer.multiSelectLists.get(multiSelectListOID));
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

  public boolean isInline() { return isInline; }
  public void setInline(boolean isInline) { this.isInline = isInline; }

  public String getItemName() { return itemName; }
  public void setItemName(String itemName) { this.itemName = itemName; }

  public String getMultiSelectListOID() { return multiSelectListOID; }
  public void setMultiSelectListOID(String multiSelectListOID) { this.multiSelectListOID = multiSelectListOID; }
    
}  