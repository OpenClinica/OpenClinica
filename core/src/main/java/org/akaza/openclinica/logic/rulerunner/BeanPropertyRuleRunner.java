package org.akaza.openclinica.logic.rulerunner;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.rule.RuleBean;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.domain.rule.action.EventActionBean;
import org.akaza.openclinica.domain.rule.action.NotificationActionBean;
import org.akaza.openclinica.domain.rule.action.NotificationActionProcessor;
import org.akaza.openclinica.domain.rule.action.RuleActionBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionBeanObjectWrapper;
import org.akaza.openclinica.domain.rule.expression.ExpressionObjectWrapper;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.logic.expressionTree.OpenClinicaExpressionParser;
import org.akaza.openclinica.patterns.ocobserver.StudyEventChangeDetails;
import org.akaza.openclinica.service.crfdata.BeanPropertyService;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * 
 * @author jnyayapathi
 *
 */
public class BeanPropertyRuleRunner extends RuleRunner{
	NotificationActionProcessor notificationActionProcessor;
	StudyEventDAO studyEventDAO;
	
	public BeanPropertyRuleRunner(DataSource ds, String requestURLMinusServletPath, String contextPath, JavaMailSenderImpl mailSender) {
		super(ds, contextPath, contextPath, mailSender);
		// TODO Auto-generated constructor stub
	}

	public void runRules(List<RuleSetBean> ruleSets, DataSource ds,
                         BeanPropertyService beanPropertyService, StudyEventDao studyEventDaoHib, StudyEventDefinitionDao studyEventDefDaoHib,
                         StudyEventChangeDetails changeDetails,Integer userId , JavaMailSenderImpl mailSender)
	{
        for (RuleSetBean ruleSet : ruleSets)
        {
            List<ExpressionBean> expressions = ruleSet.getExpressions();
            for (ExpressionBean expressionBean : expressions) {
                ruleSet.setTarget(expressionBean);

                StudyEvent studyEvent = studyEventDaoHib.findByStudyEventId(
                        Integer.valueOf(getExpressionService().getStudyEventDefenitionOrdninalCurated(ruleSet.getTarget().getValue())));

                int eventOrdinal = studyEvent.getSampleOrdinal();
                int studySubjectBeanId = studyEvent.getStudySubject().getStudySubjectId();

                List<RuleSetRuleBean> ruleSetRules = ruleSet.getRuleSetRules();
                for (RuleSetRuleBean ruleSetRule : ruleSetRules)
                {
                    Object result = null;

                    if(ruleSetRule.getStatus()==Status.AVAILABLE)
                    {
	                    RuleBean rule = ruleSetRule.getRuleBean();
	             //       StudyBean currentStudy = rule.getStudy();//TODO:Fix me!
	                    StudyDAO sdao = new StudyDAO(ds);
	                    StudyBean currentStudy = (StudyBean) sdao.findByPK(rule.getStudyId());
	                    ExpressionBeanObjectWrapper eow = new ExpressionBeanObjectWrapper(ds, currentStudy, rule.getExpression(), ruleSet,studySubjectBeanId, studyEventDaoHib, studyEventDefDaoHib);
	                    try {
	                       // StopWatch sw = new StopWatch();
	                        ExpressionObjectWrapper ew = new ExpressionObjectWrapper(ds, currentStudy, rule.getExpression(), ruleSet);
	                        ew.setStudyEventDaoHib(studyEventDaoHib);
	                        ew.setStudySubjectId(studySubjectBeanId);
	                        ew.setExpressionContext(ExpressionObjectWrapper.CONTEXT_EXPRESSION);
	                        OpenClinicaExpressionParser oep = new OpenClinicaExpressionParser(ew);
	                       // eow.setUserAccountBean(ub);
	                        eow.setStudyBean(currentStudy);
	                        result = oep.parseAndEvaluateExpression(rule.getExpression().getValue());
	                       // sw.stop();
                            logger.debug( "Rule Expression Evaluation Result: " + result);
	                        // Actions
	                        List<RuleActionBean> actionListBasedOnRuleExecutionResult = ruleSetRule.getActions(result.toString());

	                        for (RuleActionBean ruleActionBean: actionListBasedOnRuleExecutionResult){
	                            // ActionProcessor ap =ActionProcessorFacade.getActionProcessor(ruleActionBean.getActionType(), ds, null, null,ruleSet, null, ruleActionBean.getRuleSetRule());
	                        	if (ruleActionBean instanceof EventActionBean){
	                        		beanPropertyService.runAction(ruleActionBean,eow,userId,changeDetails.getRunningInTransaction());
	                        	}else if (ruleActionBean instanceof NotificationActionBean){
									HttpServletRequest request = CoreResources.getRequest();
									notificationActionProcessor = new NotificationActionProcessor(ds, mailSender, ruleSetRule,request);
                                    notificationActionProcessor.runNotificationAction(ruleActionBean,ruleSet,studyEvent.getStudySubject(),eventOrdinal);
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
	
	public boolean checkTargetMatchOld(Integer eventOrdinal, RuleSetBean ruleSet,StudyEventChangeDetails changeDetails)
	{
		Boolean result = true;
    	String ruleOrdinal = null;
    	String targetOID = ruleSet.getTarget().getValue().substring(0,ruleSet.getTarget().getValue().indexOf("."));
    	String targetProperty = ruleSet.getTarget().getValue().substring(ruleSet.getTarget().getValue().indexOf("."));

    	//Compare Target rule property (STATUS or STARTDATE) to what has been changed in event.
    	//Don't run rule if there isn't a match.
    	if (targetProperty.equals(ExpressionService.STARTDATE) && !changeDetails.getStartDateChanged()) result = false;
    	else if (targetProperty.equals(ExpressionService.STATUS) && !changeDetails.getStatusChanged()) result = false;
    	
    	//For repeating study events, run rule if ordinals match or "ALL" is specified.
    	//No brackets implies "ALL" or that the it is not a repeating event, in which case rule should run.
    	if (targetOID.contains("["))
    	{
    		int leftBracketIndex = targetOID.indexOf("[");
    		int rightBracketIndex = targetOID.indexOf("]");
    		ruleOrdinal =  targetOID.substring(leftBracketIndex + 1,rightBracketIndex);
    	
	    	if (!(ruleOrdinal.equals("ALL") || Integer.valueOf(ruleOrdinal) == eventOrdinal)) result = false;
    	}
		return result;
	}

	public boolean checkTargetMatch(RuleSetBean ruleSet,StudyEventChangeDetails changeDetails)
	{
		Boolean result = true;
    	String targetProperty = ruleSet.getTarget().getValue().substring(ruleSet.getTarget().getValue().indexOf("."));

    	//Compare Target rule property (STATUS or STARTDATE) to what has been changed in event.
    	//Don't run rule if there isn't a match.

    	if (targetProperty.equals(ExpressionService.STARTDATE+".A.B") && !changeDetails.getStartDateChanged()){
    		result = false;
    	}else if (targetProperty.equals(ExpressionService.STATUS+".A.B") && !changeDetails.getStatusChanged()){ 
    		result = false;
    	}
		return result;
	}

	
	public StudyEventDAO getStudyEventDao(DataSource ds) {
		return new StudyEventDAO(ds);
	}

	
}
