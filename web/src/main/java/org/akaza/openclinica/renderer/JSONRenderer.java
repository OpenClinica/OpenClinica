package org.akaza.openclinica.renderer;
import net.sf.json.JSON;
import java.util.Map;
import freemarker.template.Configuration;

public abstract class JSONRenderer {

  protected JSON json;   
  protected Configuration cfg;
  protected Map templateVars;
  
  public JSONRenderer(JSON json, Configuration cfg, Map templateVars) {
    this.json = json;
    this.cfg = cfg;
    this.templateVars = templateVars;
  }
   
  public String parse(JSON json)  {
    return "";
  }

  public String render(JSON json)  {
    return "";
  }

  public JSON getJson() {
    return json;
  }
  public void setJson(JSON json) {
    this.json = json;
  }

  public Map getTemplateVars() {
    return templateVars;
  }
  public void setTemplateVars(Map templateVars) {
    this.templateVars = templateVars;
  }

}