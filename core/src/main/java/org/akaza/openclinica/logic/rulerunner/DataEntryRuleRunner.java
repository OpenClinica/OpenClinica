package org.akaza.openclinica.logic.rulerunner;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.domain.rule.RuleBean;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.domain.rule.action.ActionProcessor;
import org.akaza.openclinica.domain.rule.action.ActionProcessorFacade;
import org.akaza.openclinica.domain.rule.action.RuleActionBean;
import org.akaza.openclinica.domain.rule.action.RuleActionRunBean.Phase;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionObjectWrapper;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.logic.expressionTree.OpenClinicaExpressionParser;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

public class DataEntryRuleRunner extends RuleRunner {

    public DataEntryRuleRunner(DataSource ds, String requestURLMinusServletPath, String contextPath, JavaMailSenderImpl mailSender) {
        super(ds, requestURLMinusServletPath, contextPath, mailSender);
    }

    public MessageContainer runRules(List<RuleSetBean> ruleSets, ExecutionMode executionMode, StudyBean currentStudy, HashMap<String, String> variableAndValue,
            UserAccountBean ub, Phase phase) {

        if (variableAndValue == null || variableAndValue.isEmpty()) {
            logger.warn("You must be executing Rules in Batch");
            variableAndValue = new HashMap<String, String>();
        }

        MessageContainer messageContainer = new MessageContainer();
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

                        //HashMap<String, ArrayList<String>> messagesOfActionsToBeExecuted = ruleSetRule.getActionsAsKeyPair(result);
                        // Actions
                        List<RuleActionBean> actionListBasedOnRuleExecutionResult = ruleSetRule.getActions(result, phase);
                        logger.info("RuleSet with target  : {} , Ran Rule : {}  The Result was : {} , Based on that {} action will be executed ", new Object[] {
                            ruleSet.getTarget().getValue(), rule.getName(), result, actionListBasedOnRuleExecutionResult.size() });
                        // System.out.println("ran ruleset with target: " + ruleSet.getTarget().getValue() + 
                        //        " : " + result + 
                        //        " : " + actionListBasedOnRuleExecutionResult.size());
                        // If not a dryRun(Meaning don't execute Actions) and if actions exist then execute the Action
                        if (actionListBasedOnRuleExecutionResult.size() > 0) {
                            for (RuleActionBean ruleAction : actionListBasedOnRuleExecutionResult) {
                                ItemDataBean itemData = getExpressionService().getItemDataBeanFromDb(ruleSet.getTarget().getValue());
                                int itemDataBeanId = itemData != null ? itemData.getId() : 0;
                                ruleAction.setCuratedMessage(curateMessage(ruleAction, ruleSetRule));
                                // getDiscrepancyNoteService().saveFieldNotes(ruleAction.getSummary(), itemDataBeanId, "ItemData", currentStudy, ub);
                                // System.out.println(" shipping rule action type " + ruleAction.getActionType().name());
                                ActionProcessor ap =
                                    ActionProcessorFacade.getActionProcessor(ruleAction.getActionType(), ds, getMailSender(), dynamicsMetadataService);
                                RuleActionBean rab =
                                    ap.execute(RuleRunnerMode.DATA_ENTRY, executionMode, ruleAction, itemDataBeanId, DiscrepancyNoteBean.ITEM_DATA,
                                            currentStudy, ub, prepareEmailContents(ruleSet, ruleSetRule, currentStudy, ruleAction));
                                if (rab != null) {
                                    // System.out.println(" adding message rab " + rab.getCuratedMessage() + " : " + ruleAction.getCuratedMessage());
                                    messageContainer.add(getExpressionService().getGroupOrdninalConcatWithItemOid(ruleSet.getTarget().getValue()), ruleAction);
                                }
                            }
                        }
                    } catch (OpenClinicaSystemException osa) {
                        // TODO: Auditing might happen here failed rule
                        logger.warn("RuleSet with target  : {} , Ran Rule : {} , It resulted in an error due to : {}", new Object[] {
                            ruleSet.getTarget().getValue(), rule.getName(), osa.getMessage() });
                        System.out.println("FAIL ON ruleset with target: " + ruleSet.getTarget().getValue() + " : " + osa.getMessage());
                    }
                }
            }
        }
        return messageContainer;
    }
}
