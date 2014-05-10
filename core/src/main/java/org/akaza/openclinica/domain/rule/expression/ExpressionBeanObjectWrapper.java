package org.akaza.openclinica.domain.rule.expression;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.domain.rule.RuleSetBean;

import javax.sql.DataSource;
import java.util.HashMap;

public class ExpressionBeanObjectWrapper {

    public StudyEventDao getStudyEventDaoHib() {
		return studyEventDaoHib;
	}

	public void setStudyEventDaoHib(StudyEventDao studyEventDaoHib) {
		this.studyEventDaoHib = studyEventDaoHib;
	}

	public StudyEventDefinitionDao getStudyEventDefDaoHib() {
		return studyEventDefDaoHib;
	}

	public void setStudyEventDefDaoHib(StudyEventDefinitionDao studyEventDefDaoHib) {
		this.studyEventDefDaoHib = studyEventDefDaoHib;
	}

	DataSource ds;
    StudyBean studyBean;
    ExpressionBean expressionBean;
    RuleSetBean ruleSet;
    EventCRFBean eventCrf; // used only in data entry based rule executions
    Integer studySubjectBeanId;
    UserAccountBean userAccountBean;
    StudyEventDao studyEventDaoHib;
    StudyEventDefinitionDao studyEventDefDaoHib;

    public UserAccountBean getUserAccountBean() {
		return userAccountBean;
	}

	public void setUserAccountBean(UserAccountBean userAccountBean) {
		this.userAccountBean = userAccountBean;
	}

	// This will carry item/value pairs used in DataEntry Rule Execution
    HashMap<String, String> itemsAndTheirValues = new HashMap<String, String>();

    public ExpressionBeanObjectWrapper(DataSource ds, StudyBean studyBean, ExpressionBean expressionBean) {
        super();
        this.ds = ds;
        this.studyBean = studyBean;
        this.expressionBean = expressionBean;
    }

    /*public ExpressionBeanObjectWrapper(DataSource ds, StudyBean studyBean, ExpressionBean expressionBean, RuleSetBean ruleSet,StudySubjectBean studySubjectBean, StudyEventDao studyEventDao, StudyEventDefinitionDao studyEventDefDao) {
        super();
        this.ds = ds;
        this.studyBean = studyBean;
        this.expressionBean = expressionBean;
        this.ruleSet = ruleSet;
        this.studySubjectBean = studySubjectBean;
        this.studyEventDefDaoHib=studyEventDefDao;
        this.studyEventDaoHib = studyEventDao;
    }*/
    
    public ExpressionBeanObjectWrapper(DataSource ds, StudyBean studyBean, ExpressionBean expressionBean, RuleSetBean ruleSet,Integer studySubjectBeanId, StudyEventDao studyEventDao, StudyEventDefinitionDao studyEventDefDao) {
        super();
        this.ds = ds;
        this.studyBean = studyBean;
        this.expressionBean = expressionBean;
        this.ruleSet = ruleSet;
        this.studySubjectBeanId = studySubjectBeanId;
        this.studyEventDefDaoHib=studyEventDefDao;
        this.studyEventDaoHib = studyEventDao;
    }
   


    public ExpressionBeanObjectWrapper(DataSource ds, StudyBean studyBean, ExpressionBean expressionBean, RuleSetBean ruleSet,
                                       HashMap<String, String> itemsAndTheirValues) {
        super();
        this.ds = ds;
        this.studyBean = studyBean;
        this.expressionBean = expressionBean;
        this.ruleSet = ruleSet;
        this.itemsAndTheirValues = itemsAndTheirValues;
    }

    public ExpressionBeanObjectWrapper(DataSource ds, StudyBean studyBean, ExpressionBean expressionBean, RuleSetBean ruleSet,
                                       HashMap<String, String> itemsAndTheirValues, EventCRFBean eventCrfBean) {
        super();
        this.ds = ds;
        this.studyBean = studyBean;
        this.expressionBean = expressionBean;
        this.ruleSet = ruleSet;
        this.itemsAndTheirValues = itemsAndTheirValues;
        this.eventCrf = eventCrfBean;
    }

    /**
     * @return the expressionBean
     */
    public ExpressionBean getExpressionBean() {
        return expressionBean;
    }

    /**
     * @param expressionBean
     *            the expressionBean to set
     */
    public void setExpressionBean(ExpressionBean expressionBean) {
        this.expressionBean = expressionBean;
    }

    /**
     * @return the ds
     */
    public DataSource getDs() {
        return ds;
    }

    /**
     * @param ds
     *            the ds to set
     */
    public void setDs(DataSource ds) {
        this.ds = ds;
    }

    /**
     * @return the studyBean
     */
    public StudyBean getStudyBean() {
        return studyBean;
    }

    /**
     * @param studyBean
     *            the studyBean to set
     */
    public void setStudyBean(StudyBean studyBean) {
        this.studyBean = studyBean;
    }

    /**
     * @return the ruleSet
     */
    public RuleSetBean getRuleSet() {
        return ruleSet;
    }

    /**
     * @param ruleSet
     *            the ruleSet to set
     */
    public void setRuleSet(RuleSetBean ruleSet) {
        this.ruleSet = ruleSet;
    }

    /**
     * @return the itemsAndTheirValues
     */
    public HashMap<String, String> getItemsAndTheirValues() {
        return itemsAndTheirValues;
    }

    /**
     * @param itemsAndTheirValues
     *            the itemsAndTheirValues to set
     */
    public void setItemsAndTheirValues(HashMap<String, String> itemsAndTheirValues) {
        this.itemsAndTheirValues = itemsAndTheirValues;
    }

	public EventCRFBean getEventCrf() {
		return eventCrf;
	}

	public void setEventCrf(EventCRFBean eventCrf) {
		this.eventCrf = eventCrf;
	}

    public Integer getStudySubjectBeanId() {
        return studySubjectBeanId;
    }

    public void setStudySubjectBeanId(Integer studySubjectBeanId) {
        this.studySubjectBeanId = studySubjectBeanId;
    }
}
