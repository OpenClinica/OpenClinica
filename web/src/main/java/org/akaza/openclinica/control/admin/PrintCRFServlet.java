/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.extract.odm.FullReportBean;
import org.akaza.openclinica.bean.odmbeans.ODMBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.submit.SubmitDataServlet;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.RuleSetRuleDao;
import org.akaza.openclinica.logic.odmExport.AdminDataCollector;
import org.akaza.openclinica.logic.odmExport.MetaDataCollector;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

import java.io.PrintWriter;
import java.util.ArrayList;


public class PrintCRFServlet extends SecureController {
  private static final int INDENT_LEVEL = 2;
  public static String STUDY_ID = "studyId";

  /**
   * Checks whether the user has the correct privilege
   */
  @Override
  public void mayProceed() throws InsufficientPermissionException {
    if (ub.isSysAdmin()) {
      return;
    }
    if (SubmitDataServlet.mayViewData(ub, currentRole)) {
      return;
    }
    addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
    throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_admin"), "1");
  }

  @Override
  public void processRequest() throws Exception {
    MetaDataCollector mdc = new MetaDataCollector(sm.getDataSource(), currentStudy,getRuleSetRuleDao());
    AdminDataCollector adc = new AdminDataCollector(sm.getDataSource(), currentStudy);
    MetaDataCollector.setTextLength(200);

    ODMBean odmb = mdc.getODMBean();
    odmb.setSchemaLocation("http://www.cdisc.org/ns/odm/v1.3 OpenClinica-ODM1-3-0-OC2-0.xsd");
    ArrayList<String> xmlnsList = new ArrayList<String>();
    xmlnsList.add("xmlns=\"http://www.cdisc.org/ns/odm/v1.3\"");
    xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/odm_ext_v130/v3.1\"");
    xmlnsList.add("xmlns:OpenClinicaRules=\"http://www.openclinica.org/ns/rules/v3.1\"");
    odmb.setXmlnsList(xmlnsList);
    odmb.setODMVersion("oc1.3");
    mdc.setODMBean(odmb);
    adc.setOdmbean(odmb);
    mdc.collectFileData();
    adc.collectFileData();
      
    FullReportBean report = new FullReportBean();
    report.setAdminDataMap(adc.getOdmAdminDataMap());
    report.setOdmStudyMap(mdc.getOdmStudyMap());
    report.setCoreResources(getCoreResources());
    report.setOdmBean(mdc.getODMBean());
    report.setODMVersion("oc1.3");
    report.createStudyMetaOdmXml(Boolean.FALSE);
     
    XMLSerializer xmlSerializer = new XMLSerializer();
    JSON json = xmlSerializer.read( report.getXmlOutput().toString().trim());
    //json  = ((JSONObject)json).getJSONArray().getJSONObject("F_SAMPLEPHYSIC_ENGLISH");
    //json  = ((JSONObject)json).getJSONArray("FormDef").getJSONObject(1);
    
    PrintWriter out = response.getWriter();
    response.setContentType("text/plain");
    out.println(json.toString(INDENT_LEVEL));
    out.close();
    
  }
  
  private CoreResources getCoreResources() {
    return (CoreResources) SpringServletAccess.getApplicationContext(context).getBean("coreResources");
  }
  
  private RuleSetRuleDao getRuleSetRuleDao() {
    return (RuleSetRuleDao) SpringServletAccess.getApplicationContext(context).getBean("ruleSetRuleDao");
  }
  
}