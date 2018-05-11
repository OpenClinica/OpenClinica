package org.akaza.openclinica.logic.rulerunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

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
import org.akaza.openclinica.domain.rule.action.RuleActionRunLogBean;
import org.akaza.openclinica.domain.rule.action.ShowActionBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionObjectWrapper;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.logic.expressionTree.OpenClinicaExpressionParser;
import org.akaza.openclinica.logic.rulerunner.MessageContainer.MessageType;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.transaction.annotation.Transactional;

public class ImportDataRuleRunner extends RuleRunner {

    public ImportDataRuleRunner(DataSource ds, String requestURLMinusServletPath, String contextPath, JavaMailSenderImpl mailSender) {
        super(ds, requestURLMinusServletPath, contextPath, mailSender);
    }

    /**
     * @param containers
     * @param study
     * @param ub
     * @param executionMode
     * @return Returned RuleActionBean summary with key as groupOrdinalPLusItemOid.
     */
    @Transactional
    public HashMap<String, ArrayList<String>> runRules(List<ImportDataRuleRunnerContainer> containers,
            StudyBean study, UserAccountBean ub, ExecutionMode executionMode) {
        HashMap<String, ArrayList<String>> messageMap = new HashMap<String, ArrayList<String>>();

        if(executionMode == ExecutionMode.DRY_RUN) {
            for(ImportDataRuleRunnerContainer container : containers) {
                if(container.getShouldRunRules())
                    container.setRuleActionContainerMap(this.populateToBeExpected(container, study, ub));
            }
        } else if(executionMode == ExecutionMode.SAVE){
            for(ImportDataRuleRunnerContainer container : containers) {
                MessageContainer messageContainer =
                        this.runRules(study, ub, (HashMap<String, String>)container.getVariableAndValue(), container.getRuleActionContainerMap());
                messageMap.putAll(messageContainer.getByMessageType(MessageType.ERROR));
            }
        }
        return messageMap;
    }

    @Transactional
    private HashMap<String, ArrayList<RuleActionContainer>> populateToBeExpected(ImportDataRuleRunnerContainer container,
            StudyBean study, UserAccountBean ub) {
        //copied code for toBeExpected from DataEntryServlet runRules
        HashMap<String, ArrayList<RuleActionContainer>> toBeExecuted = new HashMap<String, ArrayList<RuleActionContainer>>();
        HashMap<String, String> variableAndValue = (HashMap<String, String>)container.getVariableAndValue();

        if (variableAndValue == null || variableAndValue.isEmpty()) {
            logger.warn("No rule target item with value found.");
            return toBeExecuted;
        }

        List<RuleSetBean> ruleSets = container.getImportDataTrueRuleSets();
        for (RuleSetBean ruleSet : ruleSets) {
            String key = getExpressionService().getItemOid(ruleSet.getOriginalTarget().getValue());
            List<RuleActionContainer> allActionContainerListBasedOnRuleExecutionResult = null;
            if (toBeExecuted.containsKey(key)) {
                allActionContainerListBasedOnRuleExecutionResult = toBeExecuted.get(key);
            } else {
                toBeExecuted.put(key, new ArrayList<RuleActionContainer>());
                allActionContainerListBasedOnRuleExecutionResult = toBeExecuted.get(key);
            }
            ItemDataBean itemData = null;

            for (ExpressionBean expressionBean : ruleSet.getExpressions()) {
                ruleSet.setTarget(expressionBean);

                for (RuleSetRuleBean ruleSetRule : ruleSet.getRuleSetRules()) {
                    String result = null;
                    RuleBean rule = ruleSetRule.getRuleBean();
                    //ExpressionObjectWrapper eow = new ExpressionObjectWrapper(ds, currentStudy, rule.getExpression(), ruleSet, variableAndValue,ecb);
                    ExpressionObjectWrapper eow = new ExpressionObjectWrapper(ds, study, rule.getExpression(), ruleSet, variableAndValue);
                    try {
                        OpenClinicaExpressionParser oep = new OpenClinicaExpressionParser(eow);
                        result = (String) oep.parseAndEvaluateExpression(rule.getExpression().getValue());
                        itemData = getExpressionService().getItemDataBeanFromDb(ruleSet.getTarget().getValue());

                        // Actions
                        List<RuleActionBean> actionListBasedOnRuleExecutionResult = ruleSetRule.getActions(result, Phase.IMPORT);

                        if (itemData != null) {
                            Iterator<RuleActionBean> itr = actionListBasedOnRuleExecutionResult.iterator();
                            //String firstDDE = "firstDDEInsert_"+ruleSetRule.getOid()+"_"+itemData.getId();
                            while (itr.hasNext()) {
                                RuleActionBean ruleActionBean = itr.next();
                                /*
                                if(ruleActionBean.getActionType()==ActionType.INSERT) {
                                    request.setAttribute("insertAction", true);
                                    if(phase==Phase.DOUBLE_DATA_ENTRY && itemData.getStatus().getId()==4
                                            && request.getAttribute(firstDDE)==null) {
                                        request.setAttribute(firstDDE, true);
                                    }
                                }
                                if(request.getAttribute(firstDDE)==Boolean.TRUE) {
                                } else {
                                */
                                    String itemDataValueFromImport = "";
                                    if(variableAndValue.containsKey(key)) {
                                        itemDataValueFromImport = variableAndValue.get(key);
                                    } else {
                                        logger.info("Cannot find value from variableAndValue for item="+key+". " +
                                                "Used itemData.getValue()");
                                        itemDataValueFromImport = itemData.getValue();
                                    }
                                    RuleActionRunLogBean ruleActionRunLog =
                                        new RuleActionRunLogBean(ruleActionBean.getActionType(), itemData, itemDataValueFromImport, ruleSetRule.getRuleBean().getOid());
                                    if (getRuleActionRunLogDao().findCountByRuleActionRunLogBean(ruleActionRunLog) > 0) {
                                        itr.remove();
                                    }
                                //}
                            }
                        }

                        for (RuleActionBean ruleActionBean : actionListBasedOnRuleExecutionResult) {
                            RuleActionContainer ruleActionContainer = new RuleActionContainer(ruleActionBean, expressionBean, itemData, ruleSet);
                            allActionContainerListBasedOnRuleExecutionResult.add(ruleActionContainer);
                        }
                        logger.info("RuleSet with target  : {} , Ran Rule : {}  The Result was : {} , Based on that {} action will be executed. ",
                                new Object[] { ruleSet.getTarget().getValue(), rule.getName(), result, actionListBasedOnRuleExecutionResult.size()});
                    } catch (OpenClinicaSystemException osa) {
                        // TODO: report something useful
                    }
                }
            }
        }
        return toBeExecuted;
    }

    @Transactional
    private MessageContainer runRules(StudyBean currentStudy, UserAccountBean ub, HashMap<String, String> variableAndValue,
            HashMap<String, ArrayList<RuleActionContainer>> toBeExecuted) {
        //Copied from DataEntryRuleRunner runRules
        MessageContainer messageContainer = new MessageContainer();
        for (Map.Entry<String, ArrayList<RuleActionContainer>> entry : toBeExecuted.entrySet()) {
            // Sort the list of actions
            Collections.sort(entry.getValue(), new RuleActionContainerComparator());

            for (RuleActionContainer ruleActionContainer : entry.getValue()) {
                logger.info("START Expression is : {} , RuleAction : {} ", new Object[] {
                    ruleActionContainer.getExpressionBean().getValue(), ruleActionContainer.getRuleAction().toString() });

                ruleActionContainer.getRuleSetBean().setTarget(ruleActionContainer.getExpressionBean());
                ruleActionContainer.getRuleAction().setCuratedMessage(
                        curateMessage(ruleActionContainer.getRuleAction(), ruleActionContainer.getRuleAction().getRuleSetRule()));
                ActionProcessor ap =
                    ActionProcessorFacade.getActionProcessor(ruleActionContainer.getRuleAction().getActionType(), ds, getMailSender(), dynamicsMetadataService,
                            ruleActionContainer.getRuleSetBean(), getRuleActionRunLogDao(), ruleActionContainer.getRuleAction().getRuleSetRule());

                ItemDataBean  itemData =
                    getExpressionService().getItemDataBeanFromDb(ruleActionContainer.getRuleSetBean().getTarget().getValue());

                RuleActionBean rab =
                    ap.execute(RuleRunnerMode.IMPORT_DATA, ExecutionMode.SAVE, ruleActionContainer.getRuleAction(), itemData,
                            DiscrepancyNoteBean.ITEM_DATA, currentStudy, ub, prepareEmailContents(ruleActionContainer.getRuleSetBean(), ruleActionContainer
                                    .getRuleAction().getRuleSetRule(), currentStudy, ruleActionContainer.getRuleAction()));
                if (rab != null) {
                    if(rab instanceof ShowActionBean) {
                        messageContainer.add(getExpressionService().getGroupOidOrdinal(ruleActionContainer.getRuleSetBean().getTarget().getValue()), rab);
                    } else {
                        messageContainer.add(getExpressionService().getGroupOrdninalConcatWithItemOid(ruleActionContainer.getRuleSetBean().getTarget().getValue()),
                            ruleActionContainer.getRuleAction());
                    }
                }
                logger.info("END Expression is : {} , RuleAction : {} ", new Object[] {
                    ruleActionContainer.getExpressionBean().getValue(), ruleActionContainer.getRuleAction().toString()});
            }
        }
        return messageContainer;
    }

}