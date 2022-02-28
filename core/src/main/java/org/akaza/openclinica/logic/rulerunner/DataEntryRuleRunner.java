package org.akaza.openclinica.logic.rulerunner;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.domain.rule.RuleBean;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.domain.rule.action.ActionProcessor;
import org.akaza.openclinica.domain.rule.action.ActionProcessorFacade;
import org.akaza.openclinica.domain.rule.action.ActionType;
import org.akaza.openclinica.domain.rule.action.RuleActionBean;
import org.akaza.openclinica.domain.rule.action.RuleActionRunLogBean;
import org.akaza.openclinica.domain.rule.action.ShowActionBean;
import org.akaza.openclinica.domain.rule.action.RuleActionRunBean.Phase;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionObjectWrapper;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.logic.expressionTree.OpenClinicaExpressionParser;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

public class DataEntryRuleRunner extends RuleRunner {
    
    EventCRFBean ecb;

    public DataEntryRuleRunner(DataSource ds, String requestURLMinusServletPath, String contextPath, JavaMailSenderImpl mailSender, EventCRFBean ecb) {
        super(ds, requestURLMinusServletPath, contextPath, mailSender);
        this.ecb = ecb;
    }

    public MessageContainer runRules(List<RuleSetBean> ruleSets, ExecutionMode executionMode, StudyBean currentStudy, HashMap<String, String> variableAndValue,
            UserAccountBean ub, Phase phase, HttpServletRequest request) {

        if (variableAndValue == null || variableAndValue.isEmpty()) {
            logger.warn("You must be executing Rules in Batch");
            variableAndValue = new HashMap<String, String>();
        }

        HashMap allItemDatasHm = new HashMap();
        MessageContainer messageContainer = new MessageContainer();
        HashMap<String, ArrayList<RuleActionContainer>> toBeExecuted = new HashMap<String, ArrayList<RuleActionContainer>>();
        switch (executionMode) {
        case SAVE:        {
            toBeExecuted = (HashMap<String, ArrayList<RuleActionContainer>>)request.getAttribute("toBeExecuted");
            
            if(request.getAttribute("insertAction")==null) //Break only if the action is insertAction;
            { break;
            }else {
                toBeExecuted = new HashMap<String, ArrayList<RuleActionContainer>>();
            }
           
        }
        case DRY_RUN:
        {
        for (RuleSetBean ruleSet : ruleSets) {
            String key = getExpressionService().getItemOid(ruleSet.getOriginalTarget().getValue());
            List<RuleActionContainer> allActionContainerListBasedOnRuleExecutionResult = null;
            if (toBeExecuted.containsKey(key)) {
                allActionContainerListBasedOnRuleExecutionResult = toBeExecuted.get(key);
            } else {
                toBeExecuted.put(key, new ArrayList<RuleActionContainer>());
                allActionContainerListBasedOnRuleExecutionResult = toBeExecuted.get(key);
            }
            /**
             *  OC-12523, this is a performance issue, user reported when update one subject form data
             */
            String itemOid = key;
            String itemGroupOid = getExpressionService().getItemGroupOid(ruleSet.getExpressions().get(0).getValue());
            int studySubjectId = ((StudySubjectBean) request.getAttribute("studySubject")).getSubjectId();
            HashMap itemDatasHm = getItemDataDao().findByStudySubjectAndOids(ruleSet.getStudyId(), itemOid, itemGroupOid,studySubjectId);
            allItemDatasHm.putAll(itemDatasHm);
            ItemDataBean itemData = null;

            for (ExpressionBean expressionBean : ruleSet.getExpressions()) {
                ruleSet.setTarget(expressionBean);

                for (RuleSetRuleBean ruleSetRule : ruleSet.getRuleSetRules()) {
                    String result = null;
                    RuleBean rule = ruleSetRule.getRuleBean();
                    ExpressionObjectWrapper eow = new ExpressionObjectWrapper(ds, currentStudy, rule.getExpression(), ruleSet, variableAndValue,ecb);
                    try {
                        OpenClinicaExpressionParser oep = new OpenClinicaExpressionParser(eow);
                        result = (String) oep.parseAndEvaluateExpression(rule.getExpression().getValue());
                      
                        String studyEventId = getExpressionService().getStudyEventDefinitionOrdninalCurated(expressionBean.getValue());
                        itemOid = getExpressionService().getItemOid(expressionBean.getValue());
                        itemGroupOid = getExpressionService().getItemGroupOid(expressionBean.getValue());
                      
                        String itemDataKey = studyEventId + itemGroupOid +itemOid;
                        itemData = (ItemDataBean) itemDatasHm.get(itemDataKey);
                        // Actions
                        List<RuleActionBean> actionListBasedOnRuleExecutionResult = ruleSetRule.getActions(result, phase);

                        if (itemData != null) {
                            Iterator<RuleActionBean> itr = actionListBasedOnRuleExecutionResult.iterator();
                            String firstDDE = "firstDDEInsert_"+ruleSetRule.getOid()+"_"+itemData.getId();
                            while (itr.hasNext()) {
                                RuleActionBean ruleActionBean = itr.next();
                                if(ruleActionBean.getActionType()==ActionType.INSERT) {
                                    request.setAttribute("insertAction", true);
                                    if(phase==Phase.DOUBLE_DATA_ENTRY && itemData.getStatus().getId()==4 
                                            && request.getAttribute(firstDDE)==null) {
                                        request.setAttribute(firstDDE, true);
                                    }
                                }
                                if(request.getAttribute(firstDDE)==Boolean.TRUE) {
                                } else {
                                    String itemDataValueFromForm = "";
                                    if(variableAndValue.containsKey(key)) {
                                        itemDataValueFromForm = variableAndValue.get(key);
                                    } else {
                                        logger.info("Cannot find value from variableAndValue for item="+key+". " +
                                        		"Used itemData.getValue()");
                                        itemDataValueFromForm = itemData.getValue();
                                    }
                                    RuleActionRunLogBean ruleActionRunLog =
                                        new RuleActionRunLogBean(ruleActionBean.getActionType(), itemData, itemDataValueFromForm, ruleSetRule.getRuleBean().getOid());
                                    if (getRuleActionRunLogDao().findCountByRuleActionRunLogBean(ruleActionRunLog) > 0) {
                                        itr.remove();
                                    }
                                }
                            }
                        }

                        for (RuleActionBean ruleActionBean : actionListBasedOnRuleExecutionResult) {
                            RuleActionContainer ruleActionContainer = new RuleActionContainer(ruleActionBean, expressionBean, itemData, ruleSet);
                            allActionContainerListBasedOnRuleExecutionResult.add(ruleActionContainer);
                        }
                        logger.info("RuleSet with target  : {} , Ran Rule : {}  The Result was : {} , Based on that {} action will be executed in {} mode. ",
                                new Object[] { ruleSet.getTarget().getValue(), rule.getName(), result, actionListBasedOnRuleExecutionResult.size(),
                                    executionMode.name() });
                    } catch (OpenClinicaSystemException osa) {
                        // TODO: report something useful 
                    }
                }
            }
        }
        request.setAttribute("toBeExecuted",toBeExecuted);
        break;
        }
        
        }
        for (Map.Entry<String, ArrayList<RuleActionContainer>> entry : toBeExecuted.entrySet()) {
            // Sort the list of actions
            Collections.sort(entry.getValue(), new RuleActionContainerComparator());

            for (RuleActionContainer ruleActionContainer : entry.getValue()) {
                logger.info("START Expression is : {} , RuleAction : {} , ExecutionMode : {} ", new Object[] {
                    ruleActionContainer.getExpressionBean().getValue(), ruleActionContainer.getRuleAction().toString(), executionMode });

                ruleActionContainer.getRuleSetBean().setTarget(ruleActionContainer.getExpressionBean());
                ruleActionContainer.getRuleAction().setCuratedMessage(
                        curateMessage(ruleActionContainer.getRuleAction(), ruleActionContainer.getRuleAction().getRuleSetRule()));
                ActionProcessor ap =
                    ActionProcessorFacade.getActionProcessor(ruleActionContainer.getRuleAction().getActionType(), ds, getMailSender(), dynamicsMetadataService,
                            ruleActionContainer.getRuleSetBean(), getRuleActionRunLogDao(), ruleActionContainer.getRuleAction().getRuleSetRule());
               
              
                
                String expression = ruleActionContainer.getRuleSetBean().getTarget().getValue();
                String studyEventId = getExpressionService().getStudyEventDefinitionOidOrdinalFromExpression(expression);
                String itemOid = getExpressionService().getItemOidFromExpression(expression);
                String itemGroupOid = getExpressionService().getItemGroupOidFromExpression(expression);
              
                String itemDataKey = studyEventId + itemGroupOid +itemOid;
                ItemDataBean itemData = (ItemDataBean) allItemDatasHm.get(itemDataKey);
                
                // may not from dry run first, so still need to diuble check
                if(itemData == null) {
                	itemData =            
                            getExpressionService().getItemDataBeanFromDb(ruleActionContainer.getRuleSetBean().getTarget().getValue());
                        
                }
    
                
                RuleActionBean rab =
                    ap.execute(RuleRunnerMode.DATA_ENTRY, executionMode, ruleActionContainer.getRuleAction(), itemData,
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
                logger.info("END Expression is : {} , RuleAction : {} , ExecutionMode : {} ", new Object[] {
                    ruleActionContainer.getExpressionBean().getValue(), ruleActionContainer.getRuleAction().toString(), executionMode });
            }
        }
        return messageContainer;
    }
}