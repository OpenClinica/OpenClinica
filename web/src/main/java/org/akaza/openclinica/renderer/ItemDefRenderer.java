package org.akaza.openclinica.renderer;

import java.io.StringWriter;
import java.util.Date;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ItemDefRenderer extends JSONRenderer{

  private String OID;
  private String name;
  private String dataType;
  private String itemNumber;
  private boolean repeating;
  private String unitLabel;
  private String responseType;
  private String codeListOID;
  private int columns;
  private String template;
  
  public ItemDefRenderer(JSON json, Configuration cfg, Map templateVars) {
    super(json, cfg, templateVars);
  }
  
  
  public String parse(JSON json)  {
    JSONObject itemDef = (JSONObject)json;
    //JSONObject question = itemDef.getJSONObject("OpenClinica:ItemDetails").getJSONArray("OpenClinica:ItemPresentInForm").getJSONObject(1);
    JSONObject itemDetails = itemDef.getJSONObject("OpenClinica:ItemDetails");
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
    responseType = question.getJSONObject("OpenClinica:ItemResponse").getString("@ResponseType");
    repeating = question.has("@Repeating") ? ParseUtil.parseYesNo(question.getString("@Repeating")) : false;
    itemNumber = itemDef.getJSONObject("Question").has("@OpenClinica:QuestionNumber") ? itemDef.getJSONObject("Question").getString("@OpenClinica:QuestionNumber") + "." : "";
    unitLabel = itemDef.has("MeasurementUnitRef") ? StudyRenderer.appBasicDefinitions.get(itemDef.getJSONObject("MeasurementUnitRef").getString("@MeasurementUnitOID")) : ""; 
    codeListOID = itemDef.has("CodeListRef") ? itemDef.getJSONObject("CodeListRef").getString("@CodeListOID") : "";
    columns = question.has("OpenClinica:Layout") ? question.getJSONObject("OpenClinica:Layout").getInt("@Columns") : 2; 
    
   return "";
  }

  public String render(JSON json)  {
    parse(json);
    String  template = columns == 3 ? "item_def_3col.ftl" : "item_def.ftl";
   
    JSONObject jsonObject = (JSONObject)json;
    try {
      Template t = cfg.getTemplate(template);
      StringWriter sw = new StringWriter(); 
      templateVars.put("itemNumber", itemNumber);
      templateVars.put("name", name);
      templateVars.put("responseType", responseType);
      templateVars.put("unitLabel", unitLabel);
      templateVars.put("optionNames", StudyRenderer.appCodeLists.get(codeListOID));
      templateVars.put("columns", columns);
      t.process(templateVars, sw);
      return sw.toString();
    } 
    catch (Exception e) {
      e.printStackTrace();
    } 
   return "";
  }

  public String getOID() {
    return OID;
  }
  public void setOID(String OID) {
    this.OID = OID;
  }

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  public boolean isRepeating() {
    return repeating;
  }
  public void setRepeating(boolean repeating) {
    this.repeating = repeating;
  }

  public String getDataType() {
    return dataType;
  }
  public void setDataType(String dataType  ) {
    this.dataType = dataType;
  }
  
  public String getItemNumber() {
    return itemNumber;
  }
  public void setItemNumber(String itemNumber) {
    this.itemNumber = itemNumber;
  }
  
  public String getUnitLabel() {
    return unitLabel;
  }
  public void setUnitLabel(String unitLabel) {
    this.unitLabel = unitLabel;
  }
  
  public String getCodeListOID() {
    return codeListOID;
  }
  public void setCodeListOID(String codeListOID) {
    this.codeListOID = codeListOID;
  }
  
  public int getColumns() {
    return columns;
  }
  public void setColumns(int columns) {
    this.columns = columns;
  }
  
  public String getTemplate() {
    return template;
  }
  public void setTemplate(String template) {
    this.template = template;
  }
    
}  