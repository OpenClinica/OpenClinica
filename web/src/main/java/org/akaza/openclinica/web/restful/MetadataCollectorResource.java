package org.akaza.openclinica.web.restful;

import java.util.ArrayList;

import javax.sql.DataSource;

import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;

import org.akaza.openclinica.bean.extract.odm.FullReportBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.odmbeans.ODMBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.RuleSetDao;
import org.akaza.openclinica.dao.hibernate.RuleSetRuleDao;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.logic.odmExport.AdminDataCollector;
import org.akaza.openclinica.logic.odmExport.MetaDataCollector;

/**
 * R
 * @author jnyayapathi
 *
 */
public class MetadataCollectorResource {

    private static final int INDENT_LEVEL = 2;
	private DataSource dataSource;
	
	private StudyDAO studyDao;
	
private RuleSetRuleDao ruleSetRuleDao;	

private CoreResources coreResources;

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



	public MetadataCollectorResource(){
		
	}
	
	
	
	public String collectODMMetadata(String studyOID){
		
		StudyBean studyBean = getStudyDao().findByOid(studyOID);
		
	    MetaDataCollector mdc = new MetaDataCollector(this.dataSource, studyBean,getRuleSetRuleDao());
        AdminDataCollector adc = new AdminDataCollector(this.dataSource, studyBean);
        MetaDataCollector.setTextLength(200);

        ODMBean odmb = mdc.getODMBean();
        odmb.setSchemaLocation("http://www.cdisc.org/ns/odm/v1.3 OpenClinica-ODM1-3-0-OC2-0.xsd");
        ArrayList<String> xmlnsList = new ArrayList<String>();
        xmlnsList.add("xmlns=\"http://www.cdisc.org/ns/odm/v1.3\"");
        //xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/openclinica_odm/v1.3\"");
        xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/odm_ext_v130/v3.1\"");
        xmlnsList.add("xmlns:OpenClinicaRules=\"http://www.openclinica.org/ns/rules/v3.1\"");
        odmb.setXmlnsList(xmlnsList);
        odmb.setODMVersion("oc1.3");
        mdc.setODMBean(odmb);
     adc.setOdmbean(odmb);
        mdc.collectFileData("ajhjahsd");
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
	
	
	public String collectODMMetadataJson(String studyOID){
		net.sf.json.xml.XMLSerializer xmlserializer = new XMLSerializer();
		JSON json = xmlserializer.read(collectODMMetadata(studyOID));
		return json.toString(INDENT_LEVEL);
		
	}



	public String collectODMMetadataForForm(String studyOID,String formVersionOID) {
		StudyBean studyBean = getStudyDao().findByOid(studyOID);
		
	    MetaDataCollector mdc = new MetaDataCollector(this.dataSource, studyBean,getRuleSetRuleDao());
        AdminDataCollector adc = new AdminDataCollector(this.dataSource, studyBean);
        MetaDataCollector.setTextLength(200);

        ODMBean odmb = mdc.getODMBean();
        odmb.setSchemaLocation("http://www.cdisc.org/ns/odm/v1.3 OpenClinica-ODM1-3-0-OC2-0.xsd");
        ArrayList<String> xmlnsList = new ArrayList<String>();
        xmlnsList.add("xmlns=\"http://www.cdisc.org/ns/odm/v1.3\"");
        //xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/openclinica_odm/v1.3\"");
        xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/odm_ext_v130/v3.1\"");
        xmlnsList.add("xmlns:OpenClinicaRules=\"http://www.openclinica.org/ns/rules/v3.1\"");
        odmb.setXmlnsList(xmlnsList);
        odmb.setODMVersion("oc1.3");
        mdc.setODMBean(odmb);
        adc.setOdmbean(odmb);
        if(studyBean==null)
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
	
}
