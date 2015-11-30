package org.akaza.openclinica.validator.rule.action;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.NullValue;
import org.akaza.openclinica.bean.core.ResponseType;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ResponseOptionBean;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.domain.rule.AuditableBeanWrapper;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.action.PropertyBean;
import org.akaza.openclinica.domain.rule.action.RandomizeActionBean;
import org.akaza.openclinica.domain.rule.action.StratificationFactorBean;
import org.akaza.openclinica.domain.rule.expression.Context;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionObjectWrapper;
import org.akaza.openclinica.domain.rule.expression.ExpressionProcessor;
import org.akaza.openclinica.domain.rule.expression.ExpressionProcessorFactory;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.logic.expressionTree.ExpressionTreeHelper;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import javax.sql.DataSource;

public class RandomizeActionValidator implements Validator {

    ItemDAO itemDAO;
    ItemFormMetadataDAO itemFormMetadataDAO;
    EventDefinitionCRFDAO eventDefinitionCRFDAO;
    StudyEventDefinitionDAO studyEventDefinitionDAO;
    CRFDAO crfDAO;
    DataSource dataSource;
    EventDefinitionCRFBean eventDefinitionCRFBean;
    ExpressionService expressionService;
    RuleSetBean ruleSetBean;
    ResourceBundle respage;


    public RandomizeActionValidator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * This Validator validates just Person instances
     */
    public boolean supports(Class clazz) {
        return RandomizeActionBean.class.equals(clazz);
    }

    public void validateOidInPropertyBean(PropertyBean propertyBean, Errors e, String p) {
        if (getExpressionService().isExpressionPartial(getRuleSetBean().getTarget().getValue())) {
            if (getExpressionService().getExpressionSize(propertyBean.getOid()).intValue() > 3) {
                e.rejectValue(p + "oid", "oid.invalid", "OID: " + propertyBean.getOid() + " is Invalid.");
            }
            try {
                getExpressionService().isExpressionValid(propertyBean.getOid());
            } catch (OpenClinicaSystemException ose) {
                e.rejectValue(p + "oid", "oid.invalid", "OID: " + propertyBean.getOid() + " is Invalid.");
            }
            // Use OID in destinationProperty to get CRF
            CRFBean destinationPropertyOidCrf = getExpressionService().getCRFFromExpression(propertyBean.getOid());
            if (destinationPropertyOidCrf == null) {
                ItemBean item = getExpressionService().getItemBeanFromExpression(propertyBean.getOid());
                destinationPropertyOidCrf = getCrfDAO().findByItemOid(item.getOid());
            }
            // Use Target get CRF
            CRFBean targetCrf = getExpressionService().getCRFFromExpression(getRuleSetBean().getTarget().getValue());
            if (targetCrf == null) {
                ItemBean item = getExpressionService().getItemBeanFromExpression(getRuleSetBean().getTarget().getValue());
                targetCrf = getCrfDAO().findByItemOid(item.getOid());

            }
            // Get All event definitions the selected CRF belongs to
            List<StudyEventDefinitionBean> destinationPropertyStudyEventDefinitions = getStudyEventDefinitionDAO().findAllByCrf(destinationPropertyOidCrf);
            List<StudyEventDefinitionBean> targetStudyEventDefinitions = getStudyEventDefinitionDAO().findAllByCrf(targetCrf);
            Collection intersection = CollectionUtils.intersection(destinationPropertyStudyEventDefinitions, targetStudyEventDefinitions);
            if (intersection.size() == 0) {
                e.rejectValue(p + "oid", "oid.invalid", "OID: " + propertyBean.getOid() + " is Invalid.");
            }
        } else {
            String expression = getExpressionService().constructFullExpressionIfPartialProvided(propertyBean.getOid(), getRuleSetBean().getTarget().getValue());
            ItemBean item = getExpressionService().getItemBeanFromExpression(expression);

            if (!getExpressionService().isRandomizeActionExpressionValid(propertyBean.getOid(), getRuleSetBean(), 3) || item == null) {
                e.rejectValue(p + "oid", "oid.invalid", "OID: " + propertyBean.getOid() + " is Invalid.");
            }
        }
    }

    
    public void validate(Object obj, Errors e) {
        RandomizeActionBean randomizeActionBean = (RandomizeActionBean) obj;
        String p="";
        for (int i = 0; i < randomizeActionBean.getProperties().size(); i++) {
             p = "properties[" + i + "].";
            PropertyBean propertyBean = randomizeActionBean.getProperties().get(i);
            ValidationUtils.rejectIfEmpty(e, p + "oid", "oid.empty");
            validateOidInPropertyBean(propertyBean, e, p);
        }

    }



    public ItemDAO getItemDAO() {
        return this.itemDAO != null ? itemDAO : new ItemDAO(dataSource);
    }

    public StudyEventDefinitionDAO getStudyEventDefinitionDAO() {
        return this.studyEventDefinitionDAO != null ? studyEventDefinitionDAO : new StudyEventDefinitionDAO(dataSource);
    }

    public CRFDAO getCrfDAO() {
        return this.crfDAO != null ? crfDAO : new CRFDAO(dataSource);
    }

    public EventDefinitionCRFDAO getEventDefinitionCRFDAO() {
        return this.eventDefinitionCRFDAO != null ? eventDefinitionCRFDAO : new EventDefinitionCRFDAO(dataSource);
    }

    public ItemFormMetadataDAO getItemFormMetadataDAO() {
        return this.itemFormMetadataDAO != null ? itemFormMetadataDAO : new ItemFormMetadataDAO(dataSource);
    }

    public EventDefinitionCRFBean getEventDefinitionCRFBean() {
        return eventDefinitionCRFBean;
    }

    public void setEventDefinitionCRFBean(EventDefinitionCRFBean eventDefinitionCRFBean) {
        this.eventDefinitionCRFBean = eventDefinitionCRFBean;
    }

    public ExpressionService getExpressionService() {
        return expressionService;
    }

    public void setExpressionService(ExpressionService expressionService) {
        this.expressionService = expressionService;
    }

    public RuleSetBean getRuleSetBean() {
        return ruleSetBean;
    }

    public void setRuleSetBean(RuleSetBean ruleSetBean) {
        this.ruleSetBean = ruleSetBean;
    }
}
