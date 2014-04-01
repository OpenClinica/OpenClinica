package org.akaza.openclinica.service.crfdata;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.submit.*;
import org.akaza.openclinica.dao.hibernate.DynamicsItemFormMetadataDao;
import org.akaza.openclinica.dao.hibernate.DynamicsItemGroupMetadataDao;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.submit.*;
import org.akaza.openclinica.domain.crfdata.DynamicsItemFormMetadataBean;
import org.akaza.openclinica.domain.crfdata.DynamicsItemGroupMetadataBean;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.action.InsertActionBean;
import org.akaza.openclinica.domain.rule.action.InsertPropertyActionBean;
import org.akaza.openclinica.domain.rule.action.PropertyBean;
import org.akaza.openclinica.domain.rule.action.RuleActionBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionBeanObjectWrapper;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.logic.expressionTree.OpenClinicaExpressionParser;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.mvel2.MVEL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class BeanPropertyService{
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String ESCAPED_SEPERATOR = "\\.";
    private DynamicsItemFormMetadataDao dynamicsItemFormMetadataDao;
    private DynamicsItemGroupMetadataDao dynamicsItemGroupMetadataDao;
    DataSource ds;
    private EventCRFDAO eventCRFDAO;
    private ItemDataDAO itemDataDAO;
    private ItemDAO itemDAO;
    private ItemGroupDAO itemGroupDAO;
    private SectionDAO sectionDAO;
    // private CRFVersionDAO crfVersionDAO;
    private ItemFormMetadataDAO itemFormMetadataDAO;
    private ItemGroupMetadataDAO itemGroupMetadataDAO;
    private StudyEventDAO studyEventDAO;
    private EventDefinitionCRFDAO eventDefinitionCRFDAO;
    private ExpressionService expressionService;

    public BeanPropertyService(DataSource ds) {
    	// itemsAlreadyShown = new ArrayList<Integer>();
        this.ds = ds;
    }


    /**
     * Complete adding to this Service to evaluate InsertAction
     * @param ruleActionBean
     * @param eow
     */
    public void runAction(RuleActionBean ruleActionBean,ExpressionBeanObjectWrapper eow){
        OpenClinicaExpressionParser oep = new OpenClinicaExpressionParser(eow);
        StudyEventDAO studyEventdao = new StudyEventDAO(this.ds);
        StudyEventBean studyEventBean = new StudyEventBean();
        int index = 0;
        for(PropertyBean propertyBean: ((InsertPropertyActionBean) ruleActionBean).getProperties()){
            // This will execute the contents of <ValueExpression>SS.ENROLLMENT_DATE + 2</ValueExpression>
        	System.out.println("Values:expression??::"+propertyBean.getValueExpression().getValue());
            Object result = oep.parseAndEvaluateExpression(propertyBean.getValueExpression().getValue());
            executeAction(result,propertyBean,eow);
         }


    }


    
    private void executeAction(Object result,PropertyBean propertyBean,ExpressionBeanObjectWrapper eow){
    	String oid = propertyBean.getOid();
    	int index = propertyBean.getOid().indexOf(".");
    	String eventOID = oid.substring(0,index);
    	StudyEventDAO studyEventdao = new StudyEventDAO(this.ds);
        String property = oid.substring(index+1,oid.length());
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        StudyEventDefinitionBean sDefBean =  getStudyEventDefinitionBean(eventOID);
        boolean updateFlag = true;
    	if(oid.startsWith("SE")){
    		StudyEventBean studyEventBean =(StudyEventBean) studyEventdao.findByStudySubjectIdAndDefinitionIdAndOrdinalreturnNull(eow.getStudySubjectBean().getId(), sDefBean.getId(),1);//TODO:hard coding the sample ordinal 1 for now, it needs to read from rules nad expression needs to handle the studyevent ordinal
    		if(studyEventBean == null){
    			updateFlag = false;
    			studyEventBean = new StudyEventBean();
    			studyEventBean.setStatus(Status.AVAILABLE);//setting it first, because its a prerequiste and also its possible that one of the properties to be set could be status.
    		
    		
           
             studyEventBean.setStudySubjectId(eow.getStudySubjectBean().getId());
             studyEventBean.setSampleOrdinal(1);//TODO:how to deal with different ordinals?// add 
             //studyEventBean.setStudyEventDefinition(sDefBean);//doesnt work
             studyEventBean.setStudyEventDefinitionId(sDefBean.getId());
             
             studyEventBean.setOwner(eow.getUserAccountBean());
         
    		}
    		
    		try {
           //   	System.out.println("class?"+result.getClass());
              	if(property.contains("date")||property.contains("Date"))
              	MVEL.setProperty(studyEventBean, property, df.parse((String) result));// for now it is date  i.e enrollment date, any property should be able to be inserted
              	else
              		MVEL.setProperty(studyEventBean, property, result);
              	
  				//studyEventBean.setDateStarted(df.parse(result));
  			} catch (ParseException e) {
  				// TODO Auto-generated catch block
  				e.printStackTrace();
          		MVEL.setProperty(studyEventBean, property, result);

  			}
             if(updateFlag)
            	 {
            	 	studyEventdao.update(studyEventBean);
            	 }
             else             
            	 studyEventdao.create(studyEventBean);
    	}
    }


    private StudyEventDefinitionBean getStudyEventDefinitionBean(String eventOID) {
    	StudyEventDefinitionDAO<String, ArrayList> eventDefDAO = new StudyEventDefinitionDAO<String, ArrayList>(this.ds);
    	return eventDefDAO.findByOid(eventOID);
    	
	}


	public Boolean hasShowingDynGroupInSection(int sectionId, int crfVersionId, int eventCrfId) {
        return dynamicsItemGroupMetadataDao.hasShowingInSection(sectionId, crfVersionId, eventCrfId);
    }

    public Boolean hasShowingDynItemInSection(int sectionId, int crfVersionId, int eventCrfId) {
        return dynamicsItemFormMetadataDao.hasShowingInSection(sectionId, crfVersionId, eventCrfId);
    }

    public DynamicsItemFormMetadataDao getDynamicsItemFormMetadataDao() {
        return dynamicsItemFormMetadataDao;
    }

    public DynamicsItemGroupMetadataDao getDynamicsItemGroupMetadataDao() {
        return dynamicsItemGroupMetadataDao;
    }

    public void setDynamicsItemGroupMetadataDao(DynamicsItemGroupMetadataDao dynamicsItemGroupMetadataDao) {
        this.dynamicsItemGroupMetadataDao = dynamicsItemGroupMetadataDao;
    }

    public void setDynamicsItemFormMetadataDao(DynamicsItemFormMetadataDao dynamicsItemFormMetadataDao) {
        this.dynamicsItemFormMetadataDao = dynamicsItemFormMetadataDao;
    }

    //JN: The following methods were all returning global variables causing an issue in the case of concurrent users. Modified to return new DAO Object. The thread pooling will take care of any heap issues and I do not expect any.


    private EventCRFDAO getEventCRFDAO() {
    /*    eventCRFDAO = this.eventCRFDAO != null ? eventCRFDAO : new EventCRFDAO(ds);
        return eventCRFDAO;*/
        return  new EventCRFDAO(ds);
    }

    private ItemDataDAO getItemDataDAO() {
    /*    itemDataDAO = this.itemDataDAO != null ? itemDataDAO : new ItemDataDAO(ds);
        return itemDataDAO;*/
        return new ItemDataDAO(ds);
    }

    private ItemDAO getItemDAO() {
        /*itemDAO = this.itemDAO != null ? itemDAO : new ItemDAO(ds);
        return itemDAO;*/
        return new ItemDAO(ds);
    }

    private ItemGroupDAO getItemGroupDAO() {
        //itemGroupDAO = this.itemGroupDAO != null ? itemGroupDAO : new ItemGroupDAO(ds);
        return new ItemGroupDAO(ds);
    }

    private SectionDAO getSectionDAO() {
     //   sectionDAO = this.sectionDAO != null ? sectionDAO : new SectionDAO(ds);
        return new SectionDAO(ds);
    }

    private ItemFormMetadataDAO getItemFormMetadataDAO() {
     //   itemFormMetadataDAO = this.itemFormMetadataDAO != null ? itemFormMetadataDAO : new ItemFormMetadataDAO(ds);
        return new ItemFormMetadataDAO(ds);
    }

    private ItemGroupMetadataDAO getItemGroupMetadataDAO() {
        //itemGroupMetadataDAO = this.itemGroupMetadataDAO != null ? itemGroupMetadataDAO : new ItemGroupMetadataDAO(ds);
        return new ItemGroupMetadataDAO(ds);
    }

    public StudyEventDAO getStudyEventDAO() {
        //studyEventDAO = this.studyEventDAO != null ? studyEventDAO : new StudyEventDAO(ds);
        return new StudyEventDAO(ds);
    }

    public EventDefinitionCRFDAO getEventDefinitionCRfDAO() {
        eventDefinitionCRFDAO = this.eventDefinitionCRFDAO != null ? eventDefinitionCRFDAO : new EventDefinitionCRFDAO(ds);
        return eventDefinitionCRFDAO;
    }

    public ExpressionService getExpressionService() {
        return expressionService;
    }

    public void setExpressionService(ExpressionService expressionService) {
        this.expressionService = expressionService;
    }

    class ItemOrItemGroupHolder {

        ItemBean itemBean;
        ItemGroupBean itemGroupBean;

        public ItemOrItemGroupHolder(ItemBean itemBean, ItemGroupBean itemGroupBean) {
            this.itemBean = itemBean;
            this.itemGroupBean = itemGroupBean;
        }

        public ItemBean getItemBean() {
            return itemBean;
        }

        public void setItemBean(ItemBean itemBean) {
            this.itemBean = itemBean;
        }

        public ItemGroupBean getItemGroupBean() {
            return itemGroupBean;
        }

        public void setItemGroupBean(ItemGroupBean itemGroupBean) {
            this.itemGroupBean = itemGroupBean;
        }

    }

}
