package org.akaza.openclinica.logic.rulerunner;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.domain.rule.RuleBean;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.domain.rule.action.ActionProcessor;
import org.akaza.openclinica.domain.rule.action.ActionProcessorFacade;
import org.akaza.openclinica.domain.rule.action.RuleActionBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionObjectWrapper;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.logic.expressionTree.OpenClinicaExpressionParser;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

public class DataEntryRuleRunner extends RuleRunner {

    public DataEntryRuleRunner(DataSource ds, String requestURLMinusServletPath, String contextPath, JavaMailSenderImpl mailSender) {
        super(ds, requestURLMinusServletPath, contextPath, mailSender);
    }

    private HashMap<String, ArrayList<String>> populate(HashMap<String, ArrayList<String>> groupOrdinalPLusItemOid,
            HashMap<String, ArrayList<String>> messagesOfActionsToBeExecuted, String groupOrdinalConcatWithItemOid) {

        if (messagesOfActionsToBeExecuted.size() > 0) {
            ArrayList<String> actionMessages = new ArrayList<String>();

            for (String key : messagesOfActionsToBeExecuted.keySet()) {
                actionMessages.addAll(messagesOfActionsToBeExecuted.get(key));
            }
            if (groupOrdinalPLusItemOid.containsKey(groupOrdinalConcatWithItemOid)) {
                groupOrdinalPLusItemOid.get(groupOrdinalConcatWithItemOid).addAll(actionMessages);
            } else {
                groupOrdinalPLusItemOid.put(groupOrdinalConcatWithItemOid, actionMessages);
            }
        }
        return groupOrdinalPLusItemOid;
    }

    public HashMap<String, ArrayList<String>> runRules(List<RuleSetBean> ruleSets, Boolean dryRun, StudyBean currentStudy,
            HashMap<String, String> variableAndValue, UserAccountBean ub) {

        if (variableAndValue == null || variableAndValue.isEmpty()) {
            logger.warn("You must be executing Rules in Batch");
            variableAndValue = new HashMap<String, String>();
        }

        HashMap<String, ArrayList<String>> groupOrdinalPLusItemOid = new HashMap<String, ArrayList<String>>();
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

                        HashMap<String, ArrayList<String>> messagesOfActionsToBeExecuted = ruleSetRule.getActionsAsKeyPair(result);
                        logger.info("RuleSet with target  : {} , Ran Rule : {}  The Result was : {} , Based on that {} action will be executed ", new Object[] {
                            ruleSet.getTarget().getValue(), rule.getName(), result, messagesOfActionsToBeExecuted.size() });

                        // Write Action messages into groupOrdinalPLusItemOid so we can display on Screen
                        if (dryRun && messagesOfActionsToBeExecuted.size() > 0) {
                            groupOrdinalPLusItemOid =
                                populate(groupOrdinalPLusItemOid, messagesOfActionsToBeExecuted, getExpressionService().getGroupOrdninalConcatWithItemOid(
                                        ruleSet.getTarget().getValue()));
                        }
                        // If not a dryRun(Meaning don't execute Actions) and if actions exist then execute the Action
                        if (!dryRun && messagesOfActionsToBeExecuted.size() > 0) {
                            for (RuleActionBean ruleAction : ruleSetRule.getActions()) {
                                int itemDataBeanId = getExpressionService().getItemDataBeanFromDb(ruleSet.getTarget().getValue()).getId();
                                ruleAction.setCuratedMessage(curateMessage(ruleAction, ruleSetRule));
                                // getDiscrepancyNoteService().saveFieldNotes(ruleAction.getSummary(), itemDataBeanId, "ItemData", currentStudy, ub);
                                ActionProcessor ap = ActionProcessorFacade.getActionProcessor(ruleAction.getActionType(), ds, getMailSender());
                                ap.execute(ruleAction, itemDataBeanId, DiscrepancyNoteBean.ITEM_DATA, currentStudy, ub, prepareEmailContents(ruleSet,
                                        ruleSetRule, currentStudy, ruleAction));
                            }
                        }
                    } catch (OpenClinicaSystemException osa) {
                        // TODO: Auditing might happen here failed rule
                        logger.warn("RuleSet with target  : {} , Ran Rule : {} , It resulted in an error due to : {}", new Object[] {
                            ruleSet.getTarget().getValue(), rule.getName(), osa.getMessage() });
                    }
                }
            }
        }
        return groupOrdinalPLusItemOid;
    }

}
