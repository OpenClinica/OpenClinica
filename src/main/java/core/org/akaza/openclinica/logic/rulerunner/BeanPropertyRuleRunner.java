package core.org.akaza.openclinica.logic.rulerunner;

import java.util.List;

import javax.sql.DataSource;

import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.hibernate.StudyEventDao;
import core.org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import core.org.akaza.openclinica.domain.Status;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudyEvent;
import core.org.akaza.openclinica.domain.rule.RuleBean;
import core.org.akaza.openclinica.domain.rule.RuleSetBean;
import core.org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import core.org.akaza.openclinica.domain.rule.action.EventActionBean;
import core.org.akaza.openclinica.domain.rule.action.NotificationActionBean;
import core.org.akaza.openclinica.domain.rule.action.NotificationActionProcessor;
import core.org.akaza.openclinica.domain.rule.action.RuleActionBean;
import core.org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import core.org.akaza.openclinica.domain.rule.expression.ExpressionBeanObjectWrapper;
import core.org.akaza.openclinica.domain.rule.expression.ExpressionObjectWrapper;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import core.org.akaza.openclinica.logic.expressionTree.OpenClinicaExpressionParser;
import core.org.akaza.openclinica.ocobserver.StudyEventChangeDetails;
import core.org.akaza.openclinica.service.NotificationService;
import core.org.akaza.openclinica.service.crfdata.BeanPropertyService;
import core.org.akaza.openclinica.service.rule.expression.ExpressionService;
import core.org.akaza.openclinica.web.rest.client.auth.impl.KeycloakClientImpl;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * 
 * @author jnyayapathi
 *
 */
public class BeanPropertyRuleRunner extends RuleRunner{
	NotificationActionProcessor notificationActionProcessor;
	StudyEventDAO studyEventDAO;

	public BeanPropertyRuleRunner(DataSource ds, String requestURLMinusServletPath, String contextPath, JavaMailSenderImpl mailSender, StudyDao studyDao) {
		super(ds, contextPath, contextPath, mailSender,studyDao);
		// TODO Auto-generated constructor stub
	}

	public void runRules(List<RuleSetBean> ruleSets, DataSource ds,
						 BeanPropertyService beanPropertyService, StudyEventDao studyEventDaoHib, StudyEventDefinitionDao studyEventDefDaoHib,
						 StudyEventChangeDetails changeDetails, Integer userId , JavaMailSenderImpl mailSender, NotificationService notificationService,
						 KeycloakClientImpl keycloakClientImpl)
	{
		for (RuleSetBean ruleSet : ruleSets)
		{
			logger.debug("RuleSetBean Object: "+ ruleSet.toString());
			List<ExpressionBean> expressions = ruleSet.getExpressions();
			for (ExpressionBean expressionBean : expressions) {
				ruleSet.setTarget(expressionBean);

				StudyEvent studyEvent = studyEventDaoHib.findByStudyEventId(
						Integer.valueOf(getExpressionService().getStudyEventDefenitionOrdninalCurated(ruleSet.getTarget().getValue())));
				Study currentStudyForStudySubject = getStudyDao().findStudyWithSPVByStudyId(studyEvent.getStudySubject().getStudy().getStudyId());
				int eventOrdinal = studyEvent.getSampleOrdinal();
				int studySubjectBeanId = studyEvent.getStudySubject().getStudySubjectId();

				List<RuleSetRuleBean> ruleSetRules = ruleSet.getRuleSetRules();
				for (RuleSetRuleBean ruleSetRule : ruleSetRules)
				{
					Object result = null;

					if(ruleSetRule.getStatus()==Status.AVAILABLE)
					{
						RuleBean rule = ruleSetRule.getRuleBean();
						logger.debug("Rule Object"+ rule.toString());
						//       StudyBean currentStudy = rule.getStudy();//TODO:Fix me!
						Study currentStudy = (Study) getStudyDao().findByPK(rule.getStudyId());
						logger.info("StudyOid in Rule :"+currentStudy.getOc_oid());
						ExpressionBeanObjectWrapper eow = new ExpressionBeanObjectWrapper(ds, currentStudy, rule.getExpression(), ruleSet,studySubjectBeanId, studyEventDaoHib, studyEventDefDaoHib);
						try {
							// StopWatch sw = new StopWatch();
							ExpressionObjectWrapper ew = new ExpressionObjectWrapper(ds, currentStudy, rule.getExpression(), ruleSet);
							ew.setStudyEventDaoHib(studyEventDaoHib);
							ew.setStudyEvent(studyEvent);
							ew.setStudySubjectId(studySubjectBeanId);
							ew.setExpressionContext(ExpressionObjectWrapper.CONTEXT_EXPRESSION);
							OpenClinicaExpressionParser oep = new OpenClinicaExpressionParser(ew);
							// eow.setUserAccountBean(ub);
							eow.setStudyBean(currentStudy);
							logger.info("The Rule expression to be parsed and evaluated is {} ",rule.getExpression().getValue());
							result = oep.parseAndEvaluateExpression(rule.getExpression().getValue());
							// sw.stop();
							logger.info( "Rule Expression Evaluation Result: " + result);
							// Actions
							List<RuleActionBean> actionListBasedOnRuleExecutionResult = ruleSetRule.getActions(result.toString());

							for (RuleActionBean ruleActionBean: actionListBasedOnRuleExecutionResult){
								// ActionProcessor ap =ActionProcessorFacade.getActionProcessor(ruleActionBean.getActionType(), ds, null, null,ruleSet, null, ruleActionBean.getRuleSetRule());
								if (ruleActionBean instanceof EventActionBean){
									beanPropertyService.runAction(ruleActionBean,eow,userId,changeDetails.getRunningInTransaction());
								}else if (ruleActionBean instanceof NotificationActionBean){
									logger.info( "Start of Notification Action processor");
									notificationActionProcessor = new NotificationActionProcessor(ds, mailSender, ruleSetRule);
									notificationActionProcessor.runNotificationAction(ruleActionBean,ruleSet,studyEvent.getStudySubject(), currentStudyForStudySubject, eventOrdinal,notificationService, keycloakClientImpl);
								}
							}
						}catch (OpenClinicaSystemException osa) {
							// 	osa.printStackTrace();
							logger.error("Rule Runner received exception: " + osa.getMessage());
							logger.error(ExceptionUtils.getStackTrace(osa));
							// TODO: report something useful
						}
					}
				}
				//     	}
			}
		}
	}

}
