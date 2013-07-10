package org.akaza.openclinica.renderer;

import java.util.Date;
import java.util.Map;

import freemarker.template.Configuration;
import net.sf.json.JSON;
import net.sf.json.JSONObject;

public class ODMRenderer extends JSONRenderer{


  
  private String fileOID;
  private String description;
  private String creationDateTime;
  private String fileType;
  private String odmVersion;
  
  private StudyRenderer studyRenderer;
  
  
  public ODMRenderer(JSON json, Configuration cfg, Map templateVars) {
    super(json, cfg, templateVars);
  }
  
  public String parse(JSON json)  {
    JSONObject jsonObject = (JSONObject)json;
    fileOID = (String)jsonObject.get("@FileOID");
    description = (String)jsonObject.get("@Description");
    creationDateTime = (String)jsonObject.get("@CreationDateTime");
    fileType = (String)jsonObject.get("@FileType");
    odmVersion = (String)jsonObject.get("@ODMVersion");
    return super.parse(json);
  }

  public String render(JSON json)  {
    JSONObject jsonObject = (JSONObject)json;
    parse(json);
    studyRenderer = new StudyRenderer(jsonObject, this.cfg, this.templateVars);
    return studyRenderer.render(jsonObject.getJSONObject("Study"));
  }

  public String getFileOID() {
    return fileOID;
  }
  public void setFileOID(String fileOID) {
    this.fileOID = fileOID;
  }

  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  public String getCreationDateTime() {
    return creationDateTime;
  }
  public void setCreationDateTime(String creationDateTime) {
    this.creationDateTime = creationDateTime;
  }

  public String getFileType() {
    return fileType;
  }
  public void setFileType(String fileType) {
    this.fileType = fileType;
  }

  public String getOdmVersion() {
    return odmVersion;
  }
  public void setOdmVersion(String odmVersion) {
    this.odmVersion = odmVersion;
  }

  public StudyRenderer getStudyRenderer() {
    return studyRenderer;
  }
  public void setStudyRenderer(StudyRenderer studyRenderer) {
    this.studyRenderer = studyRenderer;
  }
  

}