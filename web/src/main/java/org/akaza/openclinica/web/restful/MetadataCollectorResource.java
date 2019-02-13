package org.akaza.openclinica.web.restful;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.akaza.openclinica.bean.extract.odm.FullReportBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.odmbeans.*;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.EventDefinitionCrfPermissionTagDao;
import org.akaza.openclinica.dao.hibernate.RuleSetRuleDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.service.StudyConfigService;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.logic.odmExport.AdminDataCollector;
import org.akaza.openclinica.logic.odmExport.MetaDataCollector;
import org.akaza.openclinica.service.PermissionService;
import org.cdisc.ns.odm.v130.ODM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;

/**
 * R
 *
 * @author jnyayapathi
 */
@Component
public class MetadataCollectorResource {

    private static final int INDENT_LEVEL = 2;

    @Autowired
    private DataSource dataSource;

    private StudyDAO studyDao;

    @Autowired
    private RuleSetRuleDao ruleSetRuleDao;

    @Autowired
    private EventDefinitionCrfPermissionTagDao eventDefinitionCrfPermissionTagDao;

    @Autowired
    private CoreResources coreResources;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    // Testing purposes TODO:remove me
    private StudyDao studyDaoHib;

    public StudyDao getStudyDaoHib() {
        return studyDaoHib;
    }

    public void setStudyDaoHib(StudyDao studyDaoHib) {
        this.studyDaoHib = studyDaoHib;
    }

    public CoreResources getCoreResources() {
        return coreResources;
    }

    public void setCoreResources(CoreResources coreResources) {
        this.coreResources = coreResources;
    }

    public RuleSetRuleDao getRuleSetRuleDao() {
        return ruleSetRuleDao;
    }

    public void setRuleSetRuleDao(RuleSetRuleDao ruleSetRuleDao) {
        this.ruleSetRuleDao = ruleSetRuleDao;
    }

    public StudyDAO getStudyDao() {
        return new StudyDAO(dataSource);
    }

    public void setStudyDao(StudyDAO studyDao) {
        this.studyDao = studyDao;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public MetadataCollectorResource() {

    }

    public String collectODMMetadata(String studyOID, HttpServletRequest request) {

        StudyBean studyBean = getStudyDao().findByOid(studyOID);
        String permissionTagsString = permissionService.getPermissionTagsString(studyBean,request);
        MetaDataCollector mdc = new MetaDataCollector(this.dataSource, studyBean, getRuleSetRuleDao(),permissionTagsString);
        AdminDataCollector adc = new AdminDataCollector(this.dataSource, studyBean);
        MetaDataCollector.setTextLength(200);

        ODM odm = new ODM();

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

        return report.getXmlOutput().toString().trim();

    }

    public String collectODMMetadataJson(String studyOID, HttpServletRequest request) {
        net.sf.json.xml.XMLSerializer xmlserializer = new XMLSerializer();
        JSON json = xmlserializer.read(collectODMMetadata(studyOID,request));
        return json.toString(INDENT_LEVEL);

    }

    public JSON collectODMMetadataJson(String studyOID, String formVersionOID,HttpServletRequest request) {
        net.sf.json.xml.XMLSerializer xmlserializer = new XMLSerializer();
        JSON json = xmlserializer.read(collectODMMetadataForForm(studyOID, formVersionOID,request));
        return json;
    }

    public String collectODMMetadataJsonString(String studyOID, String formVersionOID,HttpServletRequest request) {
        net.sf.json.xml.XMLSerializer xmlserializer = new XMLSerializer();
        JSON json = xmlserializer.read(collectODMMetadataForForm(studyOID, formVersionOID,request));
        return json.toString(INDENT_LEVEL);
    }

    public String collectODMMetadataForForm(String studyOID, String formVersionOID,HttpServletRequest request) {
        StudyBean studyBean = getStudyDao().findByOid(studyOID);
        if (studyBean != null)
            studyBean = populateStudyBean(studyBean);
        String permissionTagsString = permissionService.getPermissionTagsString(studyBean,request);

        MetaDataCollector mdc = new MetaDataCollector(this.dataSource, studyBean, getRuleSetRuleDao(),permissionTagsString);
        AdminDataCollector adc = new AdminDataCollector(this.dataSource, studyBean);
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
        if (studyBean == null)
            mdc.collectFileData(formVersionOID);
        else
            mdc.collectFileData();
        adc.collectFileData();

        FullReportBean report = new FullReportBean();
        report.setAdminDataMap(adc.getOdmAdminDataMap());
        report.setOdmStudyMap(mdc.getOdmStudyMap());
        report.setCoreResources(getCoreResources());
        report.setOdmBean(mdc.getODMBean());
        report.setODMVersion("oc1.3");
        report.createStudyMetaOdmXml(Boolean.FALSE);

        return report.getXmlOutput().toString().trim();
    }

    public FullReportBean collectODMMetadataForClinicalData(String studyOID, String formVersionOID, LinkedHashMap<String, OdmClinicalDataBean> clinicalDataMap,
                                                             boolean showArchived , String permissionTagsString,boolean meta) {
        FullReportBean report = new FullReportBean();
            StudyBean studyBean = getStudyDao().findByOid(studyOID);
            if (studyBean != null)
                studyBean = populateStudyBean(studyBean);
            MetaDataCollector mdc = new MetaDataCollector(this.dataSource, studyBean, getRuleSetRuleDao(), showArchived, permissionTagsString);
            AdminDataCollector adc = new AdminDataCollector(this.dataSource, studyBean);
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
        if (meta) {
            if (studyBean == null) {
                mdc.collectFileData(formVersionOID);
            }else {
                mdc.collectFileData();
                adc.collectFileData();
            }

                report.setAdminDataMap(adc.getOdmAdminDataMap());
                report.setOdmStudyMap(mdc.getOdmStudyMap());

        }else{
            mdc.collectOdmRoot();
        }
            report.setCoreResources(getCoreResources());
            report.setOdmBean(mdc.getODMBean());
            // report.setClinicalData(odmClinicalDataBean);
            report.setClinicalDataMap(clinicalDataMap);
            report.setODMVersion("oc1.3");

        return report;
    }

    private StudyBean populateStudyBean(StudyBean studyBean) {
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(this.dataSource);
        @SuppressWarnings("rawtypes")
        ArrayList studyParameters = spvdao.findParamConfigByStudy(studyBean);

        studyBean.setStudyParameters(studyParameters);
        StudyConfigService scs = new StudyConfigService(this.dataSource);
        if (studyBean.getParentStudyId() <= 0) {// top study
            studyBean = scs.setParametersForStudy(studyBean);

        } else {
            // YW <<
            studyBean.setParentStudyName(((StudyBean) getStudyDao().findByPK(studyBean.getParentStudyId())).getName());
            // YW >>
            studyBean = scs.setParametersForSite(studyBean);
        }

        return studyBean;
    }

}
