package org.akaza.openclinica.validator.rule.action;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.domain.rule.AuditableBeanWrapper;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.action.EventActionBean;
import org.akaza.openclinica.domain.rule.action.PropertyBean;
import org.akaza.openclinica.domain.rule.expression.Context;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionObjectWrapper;
import org.akaza.openclinica.domain.rule.expression.ExpressionProcessor;
import org.akaza.openclinica.domain.rule.expression.ExpressionProcessorFactory;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.sql.DataSource;

public class EventActionValidator implements Validator {

    DataSource dataSource;
	ExpressionService expressionService;
    AuditableBeanWrapper<RuleSetBean> ruleSetBeanWrapper;
    ResourceBundle respage;
    
    public static final String VALUE_EXPRESSION_DATE_FORMAT = "yyyy-MM-dd";
    public static final String BRACKETS_AND_CONTENTS = ".*\\[(END|ALL|[1-9]\\d*)\\]";
    private final String REPEATING = ".*\\[(END|ALL|[1-9]\\d*)\\]";

	public EventActionValidator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * This Validator validates just Person instances
     */
    public boolean supports(Class clazz) {
        return EventActionBean.class.equals(clazz);
    }

    public void validate(Object obj, Errors e) {
        EventActionBean eventActionBean = (EventActionBean) obj;

        validateOidInAction(eventActionBean.getOc_oid_reference(), e);
        
        boolean foundStartDate=false;
        for (int i = 0; i < eventActionBean.getProperties().size(); i++) {
            PropertyBean propertyBean = eventActionBean.getProperties().get(i);
            if (!foundStartDate && propertyBean.getProperty().equals("STARTDATE"))
            {
                foundStartDate=true;
                if (!isEventActionValueExpressionValid(propertyBean, ruleSetBeanWrapper))
                	getRuleSetBeanWrapper().error(createError("OCRERR_0035"));
            }
            //Throw error for getting more than one STARTDATE property 
            else if (propertyBean.getProperty().equals("STARTDATE") && foundStartDate) getRuleSetBeanWrapper().error(createError("OCRERR_0037"));
            //Throw error for unknown Property value
            else getRuleSetBeanWrapper().error(createError("OCRERR_0036"));
        }
    }

    public void validateOidInAction(String oid, Errors e) {
            try {
            	if (oid.contains(".") ||oid.contains("[ALL]")) {
            		getRuleSetBeanWrapper().error(createError("OCRERR_0041", new String[]{oid}));
            		return;
            	}
            	
            	StudyEventDefinitionBean studyEventBean = getExpressionService().getStudyEventDefinitionFromExpressionForEventScheduling(oid,true);
            	if (studyEventBean == null) {
            		getRuleSetBeanWrapper().error(createError("OCRERR_0019", new String[]{oid}));
            		return;
            	}
            	
                if (oid.matches(REPEATING) && !studyEventBean.isRepeating()) {
                	getRuleSetBeanWrapper().error(createError("OCRERR_0039", new String[]{oid}));
                    //throw new OpenClinicaSystemException("OCRERR_0039", new String[] { oid });
                	return;
                }

            } 
            catch (OpenClinicaSystemException ose) {
            	getRuleSetBeanWrapper().error(createError("OCRERR_0019", new String[]{oid}));
            }
    }

    private boolean isEventActionValueExpressionValid(PropertyBean property, AuditableBeanWrapper<RuleSetBean> ruleSetBeanWrapper) {

        StudyDAO studyDAO =  new StudyDAO<String, ArrayList>(getDataSource());
        StudyBean study = (StudyBean) studyDAO.findByPK(ruleSetBeanWrapper.getAuditableBean().getStudyId());
               
        ExpressionBean expressionBean = isExpressionValid(property.getValueExpression(), ruleSetBeanWrapper);
        ExpressionObjectWrapper eow = new ExpressionObjectWrapper(dataSource,study, expressionBean, ruleSetBeanWrapper.getAuditableBean(),
        		ExpressionObjectWrapper.CONTEXT_VALUE_EXPRESSION);
        ExpressionProcessor ep = ExpressionProcessorFactory.createExpressionProcessor(eow);
        ep.setRespage(respage);
        String errorString = ep.isRuleExpressionValid();
        
        if (errorString != null) {
            ruleSetBeanWrapper.error(errorString);
            return false;
        }
        
        //Verify expression generates a valid date string.
        try {

            SimpleDateFormat df = new SimpleDateFormat(VALUE_EXPRESSION_DATE_FORMAT);
            df.setLenient(false);
            
            String valueExpression = ep.testEvaluateExpression();
            Date   valueExpressionDate = df.parse(valueExpression);

            // simpledateformat.parse will accept any date as long as it's in the format
            // you defined, it simply rolls dates over, for example, december 32
            // becomes jan 1 and december 0 becomes november 30
            // This statement will make sure that once the string
            // has been checked for proper formatting that the date is still the
            // date that was entered, if it's not, we assume that the date is invalid
            if (!df.format(valueExpressionDate).equals(valueExpression)) {
            	return false;
            }
        } catch (ParseException ex) {
             return false;
        }

        return true;
    }
    
    private ExpressionBean isExpressionValid(ExpressionBean expressionBean, AuditableBeanWrapper<?> beanWrapper) {

        if (expressionBean.getContextName() == null && expressionBean.getContext() == null) {
            expressionBean.setContext(Context.OC_RULES_V1);
        }
        if (expressionBean.getContextName() != null && expressionBean.getContext() == null) {
            beanWrapper.warning(createError("OCRERR_0029"));
            expressionBean.setContext(Context.OC_RULES_V1);
        }
        return expressionBean;
    }
    
    private String createError(String key) {
    	String[] arguments = {};
        return createError(key,arguments);
    }
    
    private String createError(String key, String[] arguments) {
        MessageFormat mf = new MessageFormat("");
        mf.applyPattern(respage.getString(key));
        return key + ": " + mf.format(arguments);
    }
    
    public ExpressionService getExpressionService() {
        return expressionService;
    }

    public void setExpressionService(ExpressionService expressionService) {
        this.expressionService = expressionService;
    }

    public AuditableBeanWrapper<RuleSetBean> getRuleSetBeanWrapper() {
        return ruleSetBeanWrapper;
    }

    public void setRuleSetBeanWrapper(AuditableBeanWrapper<RuleSetBean> ruleSetBeanWrapper) {
        this.ruleSetBeanWrapper = ruleSetBeanWrapper;
    }

    public ResourceBundle getRespage() {
		return respage;
	}

	public void setRespage(ResourceBundle respage) {
		this.respage = respage;
	}

    public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}


}
