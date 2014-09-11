package org.akaza.openclinica.logic.rulerunner;

//import com.ecyrd.speed4j.StopWatch;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.rule.RuleBean;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.domain.rule.action.*;
import org.akaza.openclinica.domain.rule.expression.ExpressionBeanObjectWrapper;
import org.akaza.openclinica.domain.rule.expression.ExpressionObjectWrapper;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.logic.expressionTree.OpenClinicaExpressionParser;
import org.akaza.openclinica.patterns.ocobserver.StudyEventChangeDetails;
import org.akaza.openclinica.service.crfdata.BeanPropertyService;
import org.akaza.openclinica.service.rule.expression.ExpressionService;

import javax.sql.DataSource;
import java.util.List;

/**
 * 
 * @author jnyayapathi
 *
 */
public class BeanPropertyRuleRunner {

	public void runRules(List<RuleSetBean> ruleSets, DataSource ds,Integer studySubjectBeanId,
                         BeanPropertyService beanPropertyService, StudyEventDao studyEventDaoHib, StudyEventDefinitionDao studyEventDefDaoHib,
                         int eventOrdinal, StudyEventChangeDetails changeDetails,Integer userId) 
	{
        for (RuleSetBean ruleSet : ruleSets) 
        {
        	if (checkTargetMatch(eventOrdinal,ruleSet,changeDetails))
        	{
                for (RuleSetRuleBean ruleSetRule : ruleSet.getRuleSetRules()) 
                {
                    Object result = null;
                    
                    if(ruleSetRule.getStatus()==Status.AVAILABLE)
                    {
	                    RuleBean rule = ruleSetRule.getRuleBean();
	                    StudyBean currentStudy = rule.getStudy();//TODO:Fix me!
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
		                    //System.out.println(sw + "Result : " + result);
	                        // Actions
	                        List<RuleActionBean> actionListBasedOnRuleExecutionResult = ruleSetRule.getActions(result.toString());
	
	                        for (RuleActionBean ruleActionBean: actionListBasedOnRuleExecutionResult){
	                            // ActionProcessor ap =ActionProcessorFacade.getActionProcessor(ruleActionBean.getActionType(), ds, null, null,ruleSet, null, ruleActionBean.getRuleSetRule());
	                            beanPropertyService.runAction(ruleActionBean,eow,userId);
	                        }
	                    }catch (OpenClinicaSystemException osa) {
	                    	osa.printStackTrace();
	                        System.out.println("Something happeneing : " + osa.getMessage());
	                        // TODO: report something useful
	                    }
	                }
	            }
        	}
        }
    }
	
	public boolean checkTargetMatch(Integer eventOrdinal, RuleSetBean ruleSet,StudyEventChangeDetails changeDetails)
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
}
