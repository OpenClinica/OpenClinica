package org.akaza.openclinica.renderer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import net.sf.json.JSON;
import net.sf.json.JSONObject;

public class FormDefRenderer extends JSONRenderer{

  private String OID;
  private String name;
  private boolean repeating;
  
  private StudyRenderer studyRenderer;
  
  public FormDefRenderer(JSON json, Configuration cfg, Map templateVars) {
    super(json, cfg, templateVars);
  }
  
  
  public String parse(JSON json)  {
    JSONObject jsonObject = (JSONObject)json;
    OID = jsonObject.getString("@OID");
    name = jsonObject.getString("@Name");
    repeating = ParseUtil.parseYesNo(jsonObject.getString("@Repeating"));
    return "";
  }

  public String render(JSON json)  {
    parse(json);
    JSONObject jsonObject = (JSONObject)json;
    try {
      Template t = cfg.getTemplate("form_def.ftl");
      StringWriter sw = new StringWriter(); 
      FormDef formDef = new FormDef(name);
      String formDefName = formDef.name;
      templateVars.put("formDefName", formDefName);
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

  public StudyRenderer getStudyRenderer() {
    return studyRenderer;
  }
  public void setStudyRenderer(StudyRenderer studyRenderer) {
    this.studyRenderer = studyRenderer;
  }
  

}