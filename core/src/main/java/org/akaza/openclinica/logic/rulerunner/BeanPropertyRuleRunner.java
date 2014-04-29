package org.akaza.openclinica.logic.rulerunner;

//import com.ecyrd.speed4j.StopWatch;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.domain.rule.RuleBean;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.domain.rule.action.*;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionBeanObjectWrapper;
import org.akaza.openclinica.domain.rule.expression.ExpressionObjectWrapper;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.logic.expressionTree.OpenClinicaExpressionParser;
import org.akaza.openclinica.service.crfdata.BeanPropertyService;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author jnyayapathi
 *
 */
public class BeanPropertyRuleRunner {

    public void runRules(List<RuleSetBean> ruleSets, UserAccountBean ub,DataSource ds,StudyBean currentStudy,StudySubjectBean studySubjectBean,
                         BeanPropertyService beanPropertyService, StudyEventDao studyEventDaoHib, StudyEventDefinitionDao studyEventDefDaoHib) {
        for (RuleSetBean ruleSet : ruleSets) {

                for (RuleSetRuleBean ruleSetRule : ruleSet.getRuleSetRules()) {
                    Object result = null;
                    RuleBean rule = ruleSetRule.getRuleBean();
                    ExpressionBeanObjectWrapper eow = new ExpressionBeanObjectWrapper(ds, currentStudy, rule.getExpression(), ruleSet,studySubjectBean, studyEventDaoHib, studyEventDefDaoHib);
                    try {
                       // StopWatch sw = new StopWatch();
                        ExpressionObjectWrapper ew = new ExpressionObjectWrapper(ds, currentStudy, rule.getExpression(), ruleSet);
                        ew.setStudyEventDaoHib(studyEventDaoHib);
                        ew.setStudySubjectId(studySubjectBean.getId());
                        OpenClinicaExpressionParser oep = new OpenClinicaExpressionParser(ew);
                        eow.setUserAccountBean(ub);
                        eow.setStudyBean(currentStudy);
                        result = oep.parseAndEvaluateExpression(rule.getExpression().getValue());
                       // sw.stop();
	                    //System.out.println(sw + "Result : " + result);
                        // Actions
                        List<RuleActionBean> actionListBasedOnRuleExecutionResult = ruleSetRule.getActions(result.toString());

                        for (RuleActionBean ruleActionBean: actionListBasedOnRuleExecutionResult){
                            // ActionProcessor ap =ActionProcessorFacade.getActionProcessor(ruleActionBean.getActionType(), ds, null, null,ruleSet, null, ruleActionBean.getRuleSetRule());
                            beanPropertyService.runAction(ruleActionBean,eow);

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
