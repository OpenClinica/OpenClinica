package org.akaza.openclinica.logic.rulerunner;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.domain.rule.RuleBean;
import org.akaza.openclinica.domain.rule.RuleSetBasedViewContainer;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.domain.rule.action.ActionProcessor;
import org.akaza.openclinica.domain.rule.action.ActionProcessorFacade;
import org.akaza.openclinica.domain.rule.action.RuleActionBean;
import org.akaza.openclinica.domain.rule.action.RuleActionRunLogBean;
import org.akaza.openclinica.domain.rule.action.RuleActionRunBean.Phase;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionObjectWrapper;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.logic.expressionTree.OpenClinicaExpressionParser;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

public class RuleSetBulkRuleRunner extends RuleRunner {

    public RuleSetBulkRuleRunner(DataSource ds, String requestURLMinusServletPath, String contextPath, JavaMailSenderImpl mailSender) {
        super(ds, requestURLMinusServletPath, contextPath, mailSender);
    }

    private List<RuleSetBasedViewContainer> populateForRuleSetBasedView(List<RuleSetBasedViewContainer> theList, RuleSetBean ruleSet, RuleBean rule,
            String akey, RuleActionBean ruleAction) {

        StudyEventBean studyEvent =
            (StudyEventBean) getStudyEventDao().findByPK(
                    Integer.valueOf(getExpressionService().getStudyEventDefenitionOrdninalCurated(ruleSet.getTarget().getValue())));

        //for (String akey : actionsToBeExecuted.keySet()) {
        //    for (RuleActionBean ruleAction : actionsToBeExecuted.get(akey)) {
        RuleSetBasedViewContainer container =
            new RuleSetBasedViewContainer(rule.getName(), rule.getExpression().getValue(), akey, ruleAction.getActionType().toString(), ruleAction.getSummary());
        if (!theList.contains(container)) {
            theList.add(container);
        }
        StudySubjectBean studySubject = (StudySubjectBean) getStudySubjectDao().findByPK(studyEvent.getStudySubjectId());
        theList.get(theList.indexOf(container)).addSubject(studySubject.getLabel());
        //   }
        //}
        return theList;
    }

    public List<RuleSetBasedViewContainer> runRulesBulkFromRuleSetScreen(List<RuleSetBean> ruleSets, Boolean dryRun, StudyBean currentStudy,
            HashMap<String, String> variableAndValue, UserAccountBean ub) {

        if (variableAndValue == null || variableAndValue.isEmpty()) {
            logger.warn("You must be executing Rules in Batch");
            variableAndValue = new HashMap<String, String>();
        }

        List<RuleSetBasedViewContainer> ruleSetBasedView = new ArrayList<RuleSetBasedViewContainer>();
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

                        //HashMap<String, ArrayList<RuleActionBean>> actionsToBeExecuted = ruleSetRule.getAllActionsWithEvaluatesToAsKey(result);
                        List<RuleActionBean> actionListBasedOnRuleExecutionResult = ruleSetRule.getActions(result, Phase.BATCH);

                        ItemDataBean itemData = getExpressionService().getItemDataBeanFromDb(ruleSet.getTarget().getValue());
                        if (itemData != null) {
                            Iterator<RuleActionBean> itr = actionListBasedOnRuleExecutionResult.iterator();
                            while (itr.hasNext()) {
                                RuleActionBean ruleActionBean = itr.next();
                                RuleActionRunLogBean ruleActionRunLog =
                                    new RuleActionRunLogBean(ruleActionBean.getActionType(), itemData, itemData.getValue(), ruleSetRule.getRuleBean().getOid());
                                if (getRuleActionRunLogDao().findByRuleActionRunLogBean(ruleActionRunLog) != null) {
                                    itr.remove();
                                }
                            }
                        }

                        logger.info("RuleSet with target  : {} , Ran Rule : {}  The Result was : {} , Based on that {} action will be executed ", new Object[] {
                            ruleSet.getTarget().getValue(), rule.getName(), result, actionListBasedOnRuleExecutionResult.size() });

                        if (actionListBasedOnRuleExecutionResult.size() > 0) {
                            for (RuleActionBean ruleAction : actionListBasedOnRuleExecutionResult) {
                                ruleAction.setCuratedMessage(curateMessage(ruleAction, ruleSetRule));
                                // getDiscrepancyNoteService().saveFieldNotes(ruleAction.getSummary(), itemDataBeanId, "ItemData", currentStudy, ub);
                                ActionProcessor ap =
                                    ActionProcessorFacade.getActionProcessor(ruleAction.getActionType(), ds, getMailSender(), dynamicsMetadataService, ruleSet,
                                            getRuleActionRunLogDao(), ruleSetRule);
                                RuleActionBean rab =
                                    ap.execute(RuleRunnerMode.RULSET_BULK, ExecutionMode.SAVE, ruleAction, itemData, DiscrepancyNoteBean.ITEM_DATA,
                                            currentStudy, ub, prepareEmailContents(ruleSet, ruleSetRule, currentStudy, ruleAction));
                                if (rab != null) {
                                    ruleSetBasedView = populateForRuleSetBasedView(ruleSetBasedView, ruleSet, rule, result, ruleAction);
                                }
                            }
                        }

                    } catch (OpenClinicaSystemException osa) {
                        String errorMessage =
                            "RuleSet with target  : " + ruleSet.getTarget().getValue() + " , Ran Rule : " + rule.getName()
                                + " , It resulted in an error due to : " + osa.getMessage();
                        // log error
                        logger.warn(errorMessage);

                    }
                }
            }
        }
        return ruleSetBasedView;
    }

}
