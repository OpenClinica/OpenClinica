package org.akaza.openclinica.service.rule;

import java.util.List;

import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.dao.hibernate.RuleSetDao;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.rule.RuleSetBean;
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
		Integer studyEventDefId = event.getStudyEvent().getStudyEventDefinition().getStudyEventDefinitionId();
		Integer studyEventOrdinal = event.getStudyEvent().getSampleOrdinal();
		Integer studySubjectId = event.getStudyEvent().getStudySubject().getStudySubjectId();
		getRuleSetService().runRulesInBeanProperty(createRuleSet(studyEventDefId),studySubjectId,studyEventOrdinal);
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
