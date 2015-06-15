package org.akaza.openclinica.logic.rulerunner;

//import com.ecyrd.speed4j.StopWatch;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.dao.hibernate.RuleActionRunLogDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.rule.RuleBean;
import org.akaza.openclinica.domain.rule.RuleSetBasedViewContainer;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.domain.rule.action.*;
import org.akaza.openclinica.domain.rule.expression.Context;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionBeanObjectWrapper;
import org.akaza.openclinica.domain.rule.expression.ExpressionObjectWrapper;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.logic.expressionTree.OpenClinicaExpressionParser;
import org.akaza.openclinica.logic.rulerunner.RuleRunner.RuleRunnerMode;
import org.akaza.openclinica.patterns.ocobserver.StudyEventChangeDetails;
import org.akaza.openclinica.service.crfdata.BeanPropertyService;
import org.akaza.openclinica.service.crfdata.DynamicsMetadataService;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author jnyayapathi
 *
 */
public class BeanPropertyRuleRunner extends RuleRunner {

	public BeanPropertyRuleRunner(DataSource ds, String requestURLMinusServletPath, String contextPath, JavaMailSenderImpl mailSender) {
		super(ds, requestURLMinusServletPath, contextPath, mailSender);
		// TODO Auto-generated constructor stub
	}

	NotificationActionProcessor notificationActionProcessor;
	EmailActionProcessor emailActionProcessor;
	ExpressionService expressionService;
	RuleActionRunLogDao ruleActionRunLogDao;
	protected DynamicsMetadataService dynamicsMetadataService;
	StudyBean currentStudy = null;
	UserAccountBean ub = null;

	ExpressionBeanObjectWrapper eow;
	UserAccountDAO udao;

	public void runRules(List<RuleSetBean> ruleSets, DataSource ds, Integer studySubjectBeanId, BeanPropertyService beanPropertyService, StudyEventDao studyEventDaoHib,
			StudyEventDefinitionDao studyEventDefDaoHib, int eventOrdinal, StudyEventChangeDetails changeDetails, Integer userId, JavaMailSenderImpl mailSender, String fullTargetExpression,
			boolean isTargetItemSpecific, boolean isTargetEventSpecific, RuleActionRunLogDao ruleActionRLDao) {

		HashMap<String, ArrayList<RuleActionContainer>> toBeExecuted = new HashMap<String, ArrayList<RuleActionContainer>>();

		for (RuleSetBean ruleSet : ruleSets) {

			List<RuleActionContainer> allActionContainerListBasedOnRuleExecutionResult = null;
			if (isTargetItemSpecific) {
				String key = getExpressionService().getItemOid(fullTargetExpression);
				if (toBeExecuted.containsKey(key)) {
					allActionContainerListBasedOnRuleExecutionResult = toBeExecuted.get(key);
				} else {
					toBeExecuted.put(key, new ArrayList<RuleActionContainer>());
					allActionContainerListBasedOnRuleExecutionResult = toBeExecuted.get(key);
				}
			} else {
				String key = ruleSet.getTarget().getValue();
				toBeExecuted.put(key, new ArrayList<RuleActionContainer>());
				allActionContainerListBasedOnRuleExecutionResult = toBeExecuted.get(key);
			}

			ItemDataBean itemData = null;

			for (RuleSetRuleBean ruleSetRule : ruleSet.getRuleSetRules()) {
				if (ruleSetRule.getStatus().getCode() == 1) {
					Object result = null;
					RuleBean rule = ruleSetRule.getRuleBean();
					// StudyBean currentStudy = rule.getStudy();//TODO:Fix me!
					StudyDAO sdao = new StudyDAO(ds);
					currentStudy = (StudyBean) sdao.findByPK(rule.getStudyId());
					eow = new ExpressionBeanObjectWrapper(ds, currentStudy, rule.getExpression(), ruleSet, studySubjectBeanId, studyEventDaoHib, studyEventDefDaoHib);
					try {
						// StopWatch sw = new StopWatch();
						ExpressionObjectWrapper ew = new ExpressionObjectWrapper(ds, currentStudy, rule.getExpression(), ruleSet);
						ew.setStudyEventDaoHib(studyEventDaoHib);
						ew.setStudySubjectId(studySubjectBeanId);
						ew.setExpressionContext(ExpressionObjectWrapper.CONTEXT_EXPRESSION);

						if (isTargetItemSpecific)
							ew.getRuleSet().getTarget().setValue(fullTargetExpression);

						OpenClinicaExpressionParser oep = new OpenClinicaExpressionParser(ew);
						// eow.setUserAccountBean(ub);
						eow.setStudyBean(currentStudy);
						result = oep.parseAndEvaluateExpression(rule.getExpression().getValue());
						System.out.println("The result: " + result);
						if (isTargetItemSpecific)
							itemData = getExpressionService().getItemDataBeanFromDb(ruleSet.getTarget().getValue());

						List<RuleActionBean> actionListBasedOnRuleExecutionResult = ruleSetRule.getActions(result.toString());

						if (isTargetItemSpecific) {

							if (itemData != null) {
								Iterator<RuleActionBean> itr = actionListBasedOnRuleExecutionResult.iterator();
								while (itr.hasNext()) {
									RuleActionBean ruleActionBean = itr.next();
									RuleActionRunLogBean ruleActionRunLog = new RuleActionRunLogBean(ruleActionBean.getActionType(), itemData, itemData.getValue(), ruleSetRule.getRuleBean().getOid());

									if (ruleActionRLDao.findCountByRuleActionRunLogBean(ruleActionRunLog) > 0) {
										itr.remove();
									}
								}
							}
						}

						ExpressionBean expressionBean = new ExpressionBean(Context.OC_RULES_V1, fullTargetExpression);
						for (RuleActionBean ruleActionBean : actionListBasedOnRuleExecutionResult) {
							RuleActionContainer ruleActionContainer = new RuleActionContainer(ruleActionBean, expressionBean, itemData, ruleSet);
							allActionContainerListBasedOnRuleExecutionResult.add(ruleActionContainer);
						}

					} catch (OpenClinicaSystemException osa) {
					
					//	osa.printStackTrace();
						System.out.println("Something happeneing..");
						// TODO: report something useful
					}
				}

			}
		}
		for (Map.Entry<String, ArrayList<RuleActionContainer>> entry : toBeExecuted.entrySet()) {
			// Sort the list of actions
			Collections.sort(entry.getValue(), new RuleActionContainerComparator());

			for (RuleActionContainer ruleActionContainer : entry.getValue()) {
				ruleActionContainer.getRuleSetBean().setTarget(ruleActionContainer.getExpressionBean());
				ruleActionContainer.getRuleAction().setCuratedMessage(curateMessage(ruleActionContainer.getRuleAction(), ruleActionContainer.getRuleAction().getRuleSetRule()));
				ActionProcessor ap = ActionProcessorFacade.getActionProcessor(ruleActionContainer.getRuleAction().getActionType(), ds, mailSender, dynamicsMetadataService, ruleActionContainer
						.getRuleSetBean(), ruleActionRLDao, ruleActionContainer.getRuleAction().getRuleSetRule());

				if (ap instanceof EventActionProcessor) {
					System.out.println("Event Action Trigger");
					beanPropertyService.runAction(ruleActionContainer.getRuleAction(), eow, userId);
				} else if (ap instanceof NotificationActionProcessor) {
					System.out.println("Notification Action Trigger");
					notificationActionProcessor = new NotificationActionProcessor(ds, mailSender, ruleActionContainer.getRuleAction().getRuleSetRule());
					notificationActionProcessor.runNotificationAction(ruleActionContainer.getRuleAction(), ruleActionContainer.getRuleSetBean(), studySubjectBeanId, eventOrdinal);
				} else {
					System.out.println("Other Action Trigger");
					udao = new UserAccountDAO(ds);
					ub = (UserAccountBean) udao.findByUserName("root"); // Locale issue in StudyUserRole
					RuleActionBean rab = ap.execute(RuleRunnerMode.RUN_ON_SCHEDULE, ExecutionMode.SAVE, ruleActionContainer.getRuleAction(), ruleActionContainer.getItemDataBean(),
							DiscrepancyNoteBean.ITEM_DATA, currentStudy, ub, prepareEmailContents(ruleActionContainer.getRuleSetBean(), ruleActionContainer.getRuleAction().getRuleSetRule(),
									currentStudy, ruleActionContainer.getRuleAction()));
				}
			}
		}
	}

	public boolean checkTargetMatch(Integer eventOrdinal, RuleSetBean ruleSet, StudyEventChangeDetails changeDetails) {
		Boolean result = true;
		String ruleOrdinal = null;
		String targetOID = ruleSet.getTarget().getValue().substring(0, ruleSet.getTarget().getValue().indexOf("."));
		String targetProperty = ruleSet.getTarget().getValue().substring(ruleSet.getTarget().getValue().indexOf("."));

		// Compare Target rule property (STATUS or STARTDATE) to what has been changed in event.
		// Don't run rule if there isn't a match.
		if (targetProperty.equals(ExpressionService.STARTDATE) && !changeDetails.getStartDateChanged())
			result = false;
		else if (targetProperty.equals(ExpressionService.STATUS) && !changeDetails.getStatusChanged())
			result = false;

		// For repeating study events, run rule if ordinals match or "ALL" is specified.
		// No brackets implies "ALL" or that the it is not a repeating event, in which case rule should run.
		if (targetOID.contains("[")) {
			int leftBracketIndex = targetOID.indexOf("[");
			int rightBracketIndex = targetOID.indexOf("]");
			ruleOrdinal = targetOID.substring(leftBracketIndex + 1, rightBracketIndex);

			if (!(ruleOrdinal.equals("ALL") || Integer.valueOf(ruleOrdinal) == eventOrdinal))
				result = false;
		}
		return result;
	}

}
