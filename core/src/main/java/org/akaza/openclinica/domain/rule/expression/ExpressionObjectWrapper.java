package org.akaza.openclinica.domain.rule.expression;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.domain.rule.RuleSetBean;

import javax.sql.DataSource;
import java.util.HashMap;

public class ExpressionObjectWrapper {

    DataSource ds;
    StudyBean studyBean;
    ExpressionBean expressionBean;
    RuleSetBean ruleSet;
    EventCRFBean eventCrf; // used only in data entry based rule executions
    String expressionContext = null;  // Used only where context specific validation checking is needed. Not always populated.

    // This will carry item/value pairs used in DataEntry Rule Execution
    HashMap<String, String> itemsAndTheirValues = new HashMap<String, String>();
    StudyEventDao studyEventDaoHib;
    Integer StudySubjectId;
    
    public static final String CONTEXT_EXPRESSION = "expression";
    public static final String CONTEXT_TARGET = "target";
    public static final String CONTEXT_VALUE_EXPRESSION = "valueexpression";

    public Integer getStudySubjectId() {
		return StudySubjectId;
	}

	public void setStudySubjectId(Integer studySubjectId) {
		StudySubjectId = studySubjectId;
	}

	public StudyEventDao getStudyEventDaoHib() {
		return studyEventDaoHib;
	}

	public void setStudyEventDaoHib(StudyEventDao studyEventDaoHib) {
		this.studyEventDaoHib = studyEventDaoHib;
	}

	public ExpressionObjectWrapper(DataSource ds, StudyBean studyBean, ExpressionBean expressionBean) {
        super();
        this.ds = ds;
        this.studyBean = studyBean;
        this.expressionBean = expressionBean;
    }

    public ExpressionObjectWrapper(DataSource ds, StudyBean studyBean, ExpressionBean expressionBean, RuleSetBean ruleSet) {
        super();
        this.ds = ds;
        this.studyBean = studyBean;
        this.expressionBean = expressionBean;
        this.ruleSet = ruleSet;
    }

    public ExpressionObjectWrapper(DataSource ds, StudyBean studyBean, ExpressionBean expressionBean, String expressionContext) {
        super();
        this.ds = ds;
        this.studyBean = studyBean;
        this.expressionBean = expressionBean;
        this.expressionContext = expressionContext;
    }

    public ExpressionObjectWrapper(DataSource ds, StudyBean studyBean, ExpressionBean expressionBean, RuleSetBean ruleSet,
            HashMap<String, String> itemsAndTheirValues) {
        super();
        this.ds = ds;
        this.studyBean = studyBean;
        this.expressionBean = expressionBean;
        this.ruleSet = ruleSet;
        this.itemsAndTheirValues = itemsAndTheirValues;
    }
    
    public ExpressionObjectWrapper(DataSource ds, StudyBean studyBean, ExpressionBean expressionBean, RuleSetBean ruleSet,
            String expressionContext) {
        super();
        this.ds = ds;
        this.studyBean = studyBean;
        this.expressionBean = expressionBean;
        this.ruleSet = ruleSet;
        this.expressionContext = expressionContext;
    }
    
    public ExpressionObjectWrapper(DataSource ds, StudyBean studyBean, ExpressionBean expressionBean, RuleSetBean ruleSet,
            HashMap<String, String> itemsAndTheirValues,EventCRFBean eventCrfBean) {
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

	public String getExpressionContext() {
		return expressionContext;
	}

	public void setExpressionContext(String expressionContext) {
		this.expressionContext = expressionContext;
	}
	
	
}
