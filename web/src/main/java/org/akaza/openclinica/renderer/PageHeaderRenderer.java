package org.akaza.openclinica.renderer;

import java.io.StringWriter;
import java.util.Date;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PageHeaderRenderer extends JSONRenderer{

  
  public PageHeaderRenderer(JSON json, Configuration cfg, Map templateVars) {
    super(json, cfg, templateVars);
  }
  
  
  public String render(JSON json, 
                       String printTime,
                       String pageType,
                       String studyName,
                       String siteName,
                       String protocolName,
                       String eventName,
                       boolean collectSubjectDOB,
                       boolean personIDRequired,
                       boolean interviewerNameRequired,
                       boolean interviewDateRequired,
                       boolean secondaryLabelViewable,
                       boolean eventLocationDateRequired
                      )  {
    parse(json);
    String  template = "page_header.ftl";
    
    JSONObject jsonObject = (JSONObject)json;
    try {
      Template t = cfg.getTemplate(template);
      StringWriter sw = new StringWriter(); 
      templateVars.put("printTime", printTime);
      templateVars.put("pageType", pageType);
      templateVars.put("studyName", studyName);
      templateVars.put("siteName", siteName);
      templateVars.put("protocolName", protocolName);
      templateVars.put("eventName", eventName);
      templateVars.put("collectSubjectDOB", collectSubjectDOB);
      templateVars.put("personIDRequired", personIDRequired);
      templateVars.put("interviewerNameRequired", interviewerNameRequired);
      templateVars.put("interviewDateRequired", interviewDateRequired);
      templateVars.put("secondaryLabelViewable", secondaryLabelViewable);
      templateVars.put("eventLocationRequired", eventLocationDateRequired);
      t.process(templateVars, sw);
      return sw.toString();
    } 
    catch (Exception e) {
      e.printStackTrace();
    } 
   return "";
  }

    
}  