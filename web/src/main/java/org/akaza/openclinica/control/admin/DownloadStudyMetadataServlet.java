/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import java.util.ArrayList;

import org.akaza.openclinica.bean.extract.odm.FullReportBean;
import org.akaza.openclinica.bean.odmbeans.ODMBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.submit.SubmitDataServlet;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.EventDefinitionCrfPermissionTagDao;
import org.akaza.openclinica.dao.hibernate.FormLayoutDao;
import org.akaza.openclinica.dao.hibernate.RuleSetRuleDao;
import org.akaza.openclinica.logic.odmExport.AdminDataCollector;
import org.akaza.openclinica.logic.odmExport.MetaDataCollector;
import org.akaza.openclinica.service.PermissionService;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

public class DownloadStudyMetadataServlet extends SecureController {
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

        MetaDataCollector mdc = new MetaDataCollector(sm.getDataSource(), currentStudy, getRuleSetRuleDao());
        AdminDataCollector adc = new AdminDataCollector(sm.getDataSource(), currentStudy);
        MetaDataCollector.setTextLength(200);

        ODMBean odmb = mdc.getODMBean();
        odmb.setSchemaLocation("http://www.cdisc.org/ns/odm/v1.3 OpenClinica-ODM1-3-0-OC3-0.xsd");
        ArrayList<String> xmlnsList = new ArrayList<String>();
        xmlnsList.add("xmlns=\"http://www.cdisc.org/ns/odm/v1.3\"");
        // xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/openclinica_odm/v1.3\"");
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

        request.setAttribute("generate", report.getXmlOutput().toString().trim());
        Page finalTarget = Page.EXPORT_DATA_CUSTOM;
        finalTarget.setFileName("/WEB-INF/jsp/extract/downloadStudyMetadata.jsp");
        forwardPage(finalTarget);
    }

    private CoreResources getCoreResources() {
        return (CoreResources) SpringServletAccess.getApplicationContext(context).getBean("coreResources");
    }

    private RuleSetRuleDao getRuleSetRuleDao() {
        return (RuleSetRuleDao) SpringServletAccess.getApplicationContext(context).getBean("ruleSetRuleDao");
    }

}