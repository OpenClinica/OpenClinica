package org.akaza.openclinica.service.rule;

import java.util.ArrayList;
import java.util.List;

import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.dao.hibernate.RuleSetDao;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.akaza.openclinica.logic.score.function.GetExternalValue;
import org.akaza.openclinica.patterns.ocobserver.OnStudyEventUpdated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class RuleSetListenerService implements ApplicationListener<OnStudyEventUpdated>  {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());

	private RuleSetService ruleSetService;

	private RuleSetDao ruleSetDao;
	

@Override
	public void onApplicationEvent(final OnStudyEventUpdated event) {
	
		LOGGER.debug("listening");
	if (event.getContainer().getChangeDetails().getStartDateChanged() || event.getContainer().getChangeDetails().getStatusChanged()){ 
       
		
		Integer studyEventDefId = event.getContainer().getEvent().getStudyEventDefinition().getStudyEventDefinitionId();
		Integer studyEventOrdinal = event.getContainer().getEvent().getSampleOrdinal();
	//	Integer studySubjectId = event.getContainer().getEvent().getStudySubject().getStudySubjectId();
		Integer userId = event.getContainer().getEvent().getUpdateId();
		
		if(userId==null && event.getContainer().getEvent().getUserAccount()!=null )userId=  event.getContainer().getEvent().getUserAccount().getUserId();
		  
		StudyEvent studyEvent = event.getContainer().getEvent();
		StudyEventBean studyEventBean = new StudyEventBean();
		studyEventBean.setId(studyEvent.getStudyEventId());
		

		ArrayList<RuleSetBean> ruleSetBeans = new ArrayList();		
		ArrayList<RuleSetBean> ruleSets = (ArrayList<RuleSetBean>) createRuleSet(studyEventDefId);
		for (RuleSetBean ruleSet : ruleSets){
			ExpressionBean eBean = new ExpressionBean();
			eBean.setValue(ruleSet.getTarget().getValue()+".A.B");
			ruleSet.setTarget(eBean);
			ruleSet.addExpression(getRuleSetService().replaceSEDOrdinal(ruleSet.getTarget(), studyEventBean));
			ruleSetBeans.add(ruleSet);
			}
			getRuleSetService().runIndividualRulesInBeanProperty(ruleSetBeans, userId,event.getContainer().getChangeDetails() , studyEventOrdinal);


			
		}

}





public RuleSetService getRuleSetService() {
	return ruleSetService;
}


public void setRuleSetService(RuleSetService ruleSetService) {
	this.ruleSetService = ruleSetService;
}


public RuleSetDao getRuleSetDao() {
	return ruleSetDao;
}


public void setRuleSetDao(RuleSetDao ruleSetDao) {
	this.ruleSetDao = ruleSetDao;
}

private List<RuleSetBean> createRuleSet(Integer studyEventDefId) {
	
	return getRuleSetDao().findAllByStudyEventDefIdWhereItemIsNull(studyEventDefId);
}
}
