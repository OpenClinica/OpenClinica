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
                       String eventLocationDateRequired
                      )  {
    parse(json);
    String  template = "page_header.ftl";
    
    JSONObject jsonObject = (JSONObject)json;
    try {
      Template t = cfg.getTemplate(template);
      StringWriter sw = new StringWriter(); 
      templateVars.put("protocolIdLabel", protocolIdLabel);
      templateVars.put("studyNameLabel", studyNameLabel);
      templateVars.put("siteNameLabel", siteNameLabel);
      templateVars.put("printTime", printTime);
      templateVars.put("pageType", pageType);
      templateVars.put("studyName", studyName);
      templateVars.put("siteName", siteName);
      templateVars.put("protocolName", protocolName);
      templateVars.put("eventName", eventName);
      templateVars.put("collectSubjectDOB", collectSubjectDOB);
      templateVars.put("personIdRequired", personIdRequired);
      templateVars.put("studySubjectIdLabel", studySubjectIdLabel);
      templateVars.put("eventDateLabel", eventDateLabel);
      templateVars.put("eventNameLabel", eventNameLabel);
      templateVars.put("eventLocation", eventLocation);
      templateVars.put("eventLocationLabel", eventLocationLabel);
      templateVars.put("studyCoverPageType", studyCoverPageType);
      templateVars.put("personIdLabel", personIdLabel);
      templateVars.put("studySubjectDOBLabel", studySubjectDOBLabel);
      templateVars.put("studySubjectBirthYearLabel", studySubjectBirthYearLabel);
      templateVars.put("interviewerNameRequired", interviewerNameRequired);
      templateVars.put("interviewerLabel", interviewerLabel);
      templateVars.put("interviewDateRequired", interviewDateRequired);
      templateVars.put("interviewDateLabel", interviewDateLabel);
      templateVars.put("secondaryLabel", secondaryLabel);
      templateVars.put("secondaryLabelViewable", secondaryLabelViewable);
      templateVars.put("eventLocationRequired", eventLocationRequired);
      templateVars.put("showPersonId", showPersonId);
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