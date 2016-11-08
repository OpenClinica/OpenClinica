package org.akaza.openclinica.service.crfdata;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.akaza.openclinica.dao.hibernate.DynamicsItemFormMetadataDao;
import org.akaza.openclinica.dao.hibernate.DynamicsItemGroupMetadataDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.domain.datamap.SubjectEventStatus;
import org.akaza.openclinica.domain.rule.action.EventActionBean;
import org.akaza.openclinica.domain.rule.action.PropertyBean;
import org.akaza.openclinica.domain.rule.action.RuleActionBean;
import org.akaza.openclinica.domain.rule.action.RuleActionRunBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionBeanObjectWrapper;
import org.akaza.openclinica.logic.expressionTree.OpenClinicaExpressionParser;
import org.akaza.openclinica.patterns.ocobserver.StudyEventChangeDetails;
import org.akaza.openclinica.patterns.ocobserver.StudyEventContainer;
import org.akaza.openclinica.service.rule.expression.ExpressionBeanService;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanPropertyService{
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String ESCAPED_SEPERATOR = "\\.";
    private DynamicsItemFormMetadataDao dynamicsItemFormMetadataDao;
    private DynamicsItemGroupMetadataDao dynamicsItemGroupMetadataDao;
    DataSource ds;

    private StudyEventDefinitionDao studyEventDefinitionDao;
   

	private StudyEventDao studyEventDAO;
    private ExpressionService expressionService;
    private StudySubjectDao studySubjectDao;

    private UserAccountDao userAccountDao;
    
 private static final Logger LOGGER = LoggerFactory.getLogger(BeanPropertyService.class);
   

	public BeanPropertyService(DataSource ds) {
    	// itemsAlreadyShown = new ArrayList<Integer>();
        this.ds = ds;
        this.expressionService = new ExpressionService(ds);
    }


    /**
     * Complete adding to this Service to evaluate Event action
     * @param ruleActionBean
     * @param eow
     * @param isTransaction 
     */
    public void runAction(RuleActionBean ruleActionBean,ExpressionBeanObjectWrapper eow ,Integer userId, Boolean isTransaction){
    	boolean statusMatch = false;
        OpenClinicaExpressionParser oep = new OpenClinicaExpressionParser(eow);
        ExpressionBeanService ebs = new ExpressionBeanService(eow);
    	StudyEvent studyEvent = ebs.getStudyEventFromOID(((EventActionBean)ruleActionBean).getOc_oid_reference());
    	RuleActionRunBean runOnStatuses = ruleActionBean.getRuleActionRun();

    	if (studyEvent != null)
    	{
	    	switch (SubjectEventStatus.getByCode(studyEvent.getSubjectEventStatusId()))
	    	{
	    	case NOT_SCHEDULED:
	    		if (runOnStatuses.getNot_started()) statusMatch = true;
	    		break;
	    	case SCHEDULED:
	    		if (runOnStatuses.getScheduled()) statusMatch = true;
	    		break;
	    	case DATA_ENTRY_STARTED:
	    		if (runOnStatuses.getData_entry_started()) statusMatch = true;
	    		break;
	    	case COMPLETED:
	    		if (runOnStatuses.getComplete()) statusMatch = true;
	    		break;
	    	case SKIPPED:
	    		if (runOnStatuses.getSkipped()) statusMatch = true;
	    		break;
	    	case STOPPED:
	    		if (runOnStatuses.getStopped()) statusMatch = true;
	    		break;
	    	case SIGNED:
	    	case LOCKED:
	    	default:
	    		statusMatch = false;
	    		break;
	    	}
    	}
    	//Special case if destination study event doesn't exist yet, ie not scheduled.
    	else
    	{
    		if (runOnStatuses.getNot_started()) statusMatch = true;
    	}
    	
    	if (statusMatch)
    	{
	        for(PropertyBean propertyBean: ((EventActionBean) ruleActionBean).getProperties())
	        {
	            // This will execute the contents of <ValueExpression>SS.ENROLLMENT_DATE + 2</ValueExpression>
	        	LOGGER.debug("Values:expression??::"+propertyBean.getValueExpression().getValue());
	        	Object result = oep.parseAndEvaluateExpression(propertyBean.getValueExpression().getValue());
	            executeAction(result,propertyBean,eow,(EventActionBean)ruleActionBean,userId,isTransaction);
	        }
    	}
    }

    /**
     * The Event action runs based on the values set in the rules.
     * @param result
     * @param propertyBean
     * @param eow
     * @param eventAction
     */
    
    private void executeAction(Object result,PropertyBean propertyBean,ExpressionBeanObjectWrapper eow,EventActionBean eventAction,Integer userId,boolean isTransaction){
    	String oid = eventAction.getOc_oid_reference();
    	String eventOID = null;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        int ordinal = 1;
        
        
        if(oid.startsWith(ExpressionService.STUDY_EVENT_OID_START_KEY))
        {
        	StudyEvent studyEvent = null;
        	if (oid.contains("["))
        	{
        		int leftBracketIndex = oid.indexOf("[");
        		int rightBracketIndex = oid.indexOf("]");
        		ordinal =  Integer.valueOf(oid.substring(leftBracketIndex + 1,rightBracketIndex));
        		eventOID = oid.substring(0,leftBracketIndex);
        		studyEvent= getStudyEventDAO().fetchByStudyEventDefOIDAndOrdinal(eventOID, ordinal, eow.getStudySubjectBeanId());
        	}	
        	else 
        	{
        		eventOID = oid;
        		studyEvent = getStudyEventDAO().fetchByStudyEventDefOIDAndOrdinal(oid, ordinal, eow.getStudySubjectBeanId());
        	}
        	StudyEventChangeDetails changeDetails = new StudyEventChangeDetails();
        	if(studyEvent==null){
        		studyEvent = new StudyEvent();//the studyevent may not have been created.
            	StudySubject ss = getStudySubjectDao().findById(eow.getStudySubjectBeanId());
            	StudyEventDefinition sed = getStudyEventDefinitionDao().findByColumnName(eventOID, "oc_oid");
            	studyEvent.setStudyEventDefinition(sed);
            	studyEvent.setStudySubject(ss);
            	studyEvent.setStatusId(1);
            	studyEvent.setSampleOrdinal(getNewEventOrdinal(eventOID,eow.getStudySubjectBeanId(), sed));
            	studyEvent.setSubjectEventStatusId(new Integer(1));//The status is changed to started when it doesnt exist. In other cases, the status remains the same. The case of Signed and locked are prevented from validator and are not again checked here.
            	studyEvent.setStartTimeFlag(false);
            	studyEvent.setEndTimeFlag(false);
                studyEvent.setDateCreated(new Date());
                studyEvent.setUserAccount(getUserAccountDao().findById(userId));
            	changeDetails.setStartDateChanged(true);
            	changeDetails.setStatusChanged(true);
        
        	}else{
            
        		studyEvent.setUpdateId(userId);
                studyEvent.setDateUpdated(new Date());
 	        	changeDetails.setStatusChanged(false);
        	}
        	
        	try {
        		if (studyEvent.getDateStart() == null || studyEvent.getDateStart().compareTo(df.parse((String) result)) != 0)
        			changeDetails.setStartDateChanged(true);
				studyEvent.setDateStart(df.parse((String) result));

			} catch (ParseException e) {
				e.printStackTrace();
				LOGGER.info(e.getMessage());
			}
            changeDetails.setRunningInTransaction(isTransaction);

            StudyEventContainer container = new StudyEventContainer(studyEvent,changeDetails);
            if (isTransaction) getStudyEventDAO().saveOrUpdateTransactional(container);
            else getStudyEventDAO().saveOrUpdate(container);
        }
        
    }
    
    private Integer getNewEventOrdinal(String eventOID,Integer studySubjectId,StudyEventDefinition eventDef)
    {
    	if (eventDef.getRepeating())
    	{
    		List<StudyEvent> events = getStudyEventDAO().fetchListByStudyEventDefOID(eventOID, studySubjectId);
    		return events.size()+1;
    	}
    	else return 1;
    }
    
	private StudyEventDefinition getStudyEventDefinitionBean(String eventOID) {
        return getStudyEventDefinitionDao().findByColumnName(eventOID, "oc_oid");
    	
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

    public StudyEventDefinitionDao getStudyEventDefinitionDao() {
		return studyEventDefinitionDao;
	}


	public void setStudyEventDefinitionDao(
			StudyEventDefinitionDao studyEventDefinitionDao) {
		this.studyEventDefinitionDao = studyEventDefinitionDao;
	}


    public ExpressionService getExpressionService() {
        return expressionService;
    }

    public void setExpressionService(ExpressionService expressionService) {
        this.expressionService = expressionService;
    }

    public StudyEventDao getStudyEventDAO() {
		return studyEventDAO;
	}


	public void setStudyEventDAO(StudyEventDao studyEventDAO) {
		this.studyEventDAO = studyEventDAO;
	}



	 public StudySubjectDao getStudySubjectDao() {
			return studySubjectDao;
		}


		public void setStudySubjectDao(StudySubjectDao studySubjectDao) {
			this.studySubjectDao = studySubjectDao;
		}


		public UserAccountDao getUserAccountDao() {
			return userAccountDao;
		}


		public void setUserAccountDao(UserAccountDao userAccountDao) {
			this.userAccountDao = userAccountDao;
		}

	
}
