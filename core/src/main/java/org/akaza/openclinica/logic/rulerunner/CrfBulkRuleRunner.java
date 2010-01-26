package org.akaza.openclinica.logic.rulerunner;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.domain.rule.RuleBean;
import org.akaza.openclinica.domain.rule.RuleBulkExecuteContainer;
import org.akaza.openclinica.domain.rule.RuleBulkExecuteContainerTwo;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.domain.rule.action.ActionProcessor;
import org.akaza.openclinica.domain.rule.action.ActionProcessorFacade;
import org.akaza.openclinica.domain.rule.action.DiscrepancyNoteActionProcessor;
import org.akaza.openclinica.domain.rule.action.RuleActionBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionObjectWrapper;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.logic.expressionTree.OpenClinicaExpressionParser;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

public class CrfBulkRuleRunner extends RuleRunner {

    public CrfBulkRuleRunner(DataSource ds, String requestURLMinusServletPath, String contextPath, JavaMailSenderImpl mailSender) {
        super(ds, requestURLMinusServletPath, contextPath, mailSender);
    }

    /**
     * Organize objects in a certain way so that we can show to Users on UI.
     * step1 : Get StudyEvent , eventCrf , crfVersion from studyEventId.
     * 
     * @param crfViewSpecificOrderedObjects
     * @param ruleSet
     * @param rule
     * @return
     */
    private HashMap<RuleBulkExecuteContainer, HashMap<RuleBulkExecuteContainerTwo, Set<String>>> populateForCrfBasedRulesView(
            HashMap<RuleBulkExecuteContainer, HashMap<RuleBulkExecuteContainerTwo, Set<String>>> crfViewSpecificOrderedObjects, RuleSetBean ruleSet,
            RuleBean rule, String result, StudyBean currentStudy, List<RuleActionBean> actions) {

        // step1
        StudyEventBean studyEvent =
            (StudyEventBean) getStudyEventDao().findByPK(
                    Integer.valueOf(getExpressionService().getStudyEventDefenitionOrdninalCurated(ruleSet.getTarget().getValue())));
        EventCRFBean eventCrf =
            (EventCRFBean) getEventCrfDao().findAllByStudyEventAndCrfOrCrfVersionOid(studyEvent,
                    getExpressionService().getCrfOid(ruleSet.getTarget().getValue())).get(0);
        CRFVersionBean crfVersion = (CRFVersionBean) getCrfVersionDao().findByPK(eventCrf.getCRFVersionId());

        RuleBulkExecuteContainer key = new RuleBulkExecuteContainer(crfVersion.getName(), rule, result, actions);
        String key2String = getExpressionService().getCustomExpressionUsedToCreateView(ruleSet.getTarget().getValue(), studyEvent.getSampleOrdinal());
        String studyEventDefinitionName = getExpressionService().getStudyEventDefinitionFromExpression(ruleSet.getTarget().getValue(), currentStudy).getName();
        studyEventDefinitionName += " [" + studyEvent.getSampleOrdinal() + "]";
        // String itemGroupName = getExpressionService().getItemGroupNameAndOrdinal(ruleSet.getTarget().getValue());
        // String itemName = getExpressionService().getItemGroupExpression(ruleSet.getTarget().getValue()).getName();

        String itemGroupName = getExpressionService().getItemGroupNameAndOrdinal(ruleSet.getTarget().getValue());
        ItemGroupBean itemGroupBean = getExpressionService().getItemGroupExpression(ruleSet.getTarget().getValue());
        ItemBean itemBean = getExpressionService().getItemExpression(ruleSet.getTarget().getValue(), itemGroupBean);
        String itemName = itemBean.getName();

        RuleBulkExecuteContainerTwo key2 = new RuleBulkExecuteContainerTwo(key2String, studyEvent, studyEventDefinitionName, itemGroupName, itemName);
        StudySubjectBean studySubject = (StudySubjectBean) getStudySubjectDao().findByPK(studyEvent.getStudySubjectId());

        if (crfViewSpecificOrderedObjects.containsKey(key)) {
            HashMap<RuleBulkExecuteContainerTwo, Set<String>> k = crfViewSpecificOrderedObjects.get(key);
            if (k.containsKey(key2)) {
                k.get(key2).add(String.valueOf(studySubject.getLabel()));
            } else {
                HashSet<String> values = new HashSet<String>();
                values.add(String.valueOf(studySubject.getLabel()));
                k.put(key2, values);
            }
        } else {
            HashMap<RuleBulkExecuteContainerTwo, Set<String>> k = new HashMap<RuleBulkExecuteContainerTwo, Set<String>>();
            HashSet<String> values = new HashSet<String>();
            values.add(String.valueOf(studySubject.getLabel()));
            k.put(key2, values);
            crfViewSpecificOrderedObjects.put(key, k);
        }
        return crfViewSpecificOrderedObjects;
    }

    public HashMap<RuleBulkExecuteContainer, HashMap<RuleBulkExecuteContainerTwo, Set<String>>> runRulesBulk(List<RuleSetBean> ruleSets, Boolean dryRun,
            StudyBean currentStudy, HashMap<String, String> variableAndValue, UserAccountBean ub) {

        if (variableAndValue == null || variableAndValue.isEmpty()) {
            logger.warn("You must be executing Rules in Batch");
            variableAndValue = new HashMap<String, String>();
        }

        HashMap<RuleBulkExecuteContainer, HashMap<RuleBulkExecuteContainerTwo, Set<String>>> crfViewSpecificOrderedObjects =
            new HashMap<RuleBulkExecuteContainer, HashMap<RuleBulkExecuteContainerTwo, Set<String>>>();
        for (RuleSetBean ruleSet : ruleSets) {
            for (ExpressionBean expressionBean : ruleSet.getExpressions()) {
                ruleSet.setTarget(expressionBean);

                for (RuleSetRuleBean ruleSetRule : ruleSet.getRuleSetRules()) {
                    String result = null;
                    RuleBean rule = ruleSetRule.getRuleBean();
                    ExpressionObjectWrapper eow = new ExpressionObjectWrapper(ds, currentStudy, rule.getExpression(), ruleSet, variableAndValue);
                    try {
                        OpenClinicaExpressionParser oep = new OpenClinicaExpressionParser(eow);
                        result = oep.parseAndEvaluateExpression(rule.getExpression().getValue());

                        // Actions
                        List<RuleActionBean> actionListBasedOnRuleExecutionResult = ruleSetRule.getActions(result);

                        if (dryRun && actionListBasedOnRuleExecutionResult.size() > 0) {
                            crfViewSpecificOrderedObjects =
                                populateForCrfBasedRulesView(crfViewSpecificOrderedObjects, ruleSet, rule, result, currentStudy,
                                        actionListBasedOnRuleExecutionResult);
                        }

                        // If not a dryRun meaning run Actions
                        if (!dryRun) {
                            for (RuleActionBean ruleAction : actionListBasedOnRuleExecutionResult) {
                                int itemDataBeanId = getExpressionService().getItemDataBeanFromDb(ruleSet.getTarget().getValue()).getId();
                                ruleAction.setCuratedMessage(curateMessage(ruleAction, ruleSetRule));
                                // getDiscrepancyNoteService().saveFieldNotes(ruleAction.getSummary(), itemDataBeanId, "ItemData", currentStudy, ub);
                                ActionProcessor ap = ActionProcessorFacade.getActionProcessor(ruleAction.getActionType(), ds, getMailSender());
                                ap.execute(null, ExecutionMode.SAVE, ruleAction, itemDataBeanId, DiscrepancyNoteBean.ITEM_DATA, currentStudy, ub, prepareEmailContents(ruleSet,
                                        ruleSetRule, currentStudy, ruleAction));

                            }
                        }
                    } catch (OpenClinicaSystemException osa) {
                        String errorMessage =
                            "RuleSet with target  : " + ruleSet.getTarget().getValue() + " , Ran Rule : " + rule.getName()
                                + " , It resulted in an error due to : " + osa.getMessage();
                        // log error
                        logger.warn(errorMessage);
                        // Add a discrepancy note
                        if (!dryRun) {
                            DiscrepancyNoteActionProcessor dnap = new DiscrepancyNoteActionProcessor(ds);
                            int itemDataBeanId = getExpressionService().getItemDataBeanFromDb(ruleSet.getTarget().getValue()).getId();
                            dnap.execute(errorMessage, itemDataBeanId, DiscrepancyNoteBean.ITEM_DATA, currentStudy, ub);
                        }

                    }
                }
            }
        }
        logCrfViewSpecificOrderedObjects(crfViewSpecificOrderedObjects);
        return crfViewSpecificOrderedObjects;
    }

}
