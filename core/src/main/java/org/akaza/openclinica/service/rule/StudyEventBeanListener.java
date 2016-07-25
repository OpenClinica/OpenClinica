package org.akaza.openclinica.service.rule;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.dao.hibernate.RuleSetDao;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.akaza.openclinica.patterns.ocobserver.Listener;
import org.akaza.openclinica.patterns.ocobserver.Observer;
import org.akaza.openclinica.patterns.ocobserver.OnStudyEventJDBCBeanChanged;
import org.akaza.openclinica.patterns.ocobserver.StudyEventBeanContainer;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;

public class StudyEventBeanListener implements Observer,ApplicationContextAware {

	private StudyEventDAO studyEventDao;
	private DataSource dataSource;

	private static ApplicationContext cntxt;
	private static RuleSetDao ruleSetDao;
	private static RuleSetService ruleSetService;
	
	
	public static synchronized RuleSetDao getRuleSetDao() {
		ruleSetDao = cntxt.getBean("ruleSetDao",RuleSetDao.class);
		return ruleSetDao;
	}
	/*public void setRuleSetDao(RuleSetDao ruleSetDao) {
		this.ruleSetDao = ruleSetDao;
	}*/
	public static synchronized RuleSetService getRuleSetService(){
		RuleSetService ruleSetService=cntxt.getBean("ruleSetService",RuleSetService.class);
		return ruleSetService;
	}
	
	public StudyEventBeanListener(StudyEventDAO seDAO){
		this.studyEventDao = seDAO;
		studyEventDao.setObserver(this);
	}
	@Override
	public void update(Listener lstnr) {
		
//	System.out.println("Triggering the rules based on event updates");
		StudyEventBeanContainer studyEventBeanContainer = (StudyEventBeanContainer)lstnr;
		
//	if (studyEventBeanContainer.getChangeDetails().getStartDateChanged() || studyEventBeanContainer.getChangeDetails().getStatusChanged()){
		
		Integer studyEventDefId = studyEventBeanContainer.getEvent().getStudyEventDefinitionId();
//		Integer studySubjectId = studyEventBeanContainer.getEvent().getStudySubjectId();
		Integer userId = studyEventBeanContainer.getEvent().getUpdaterId();
		Integer studyEventOrdinal = studyEventBeanContainer.getEvent().getSampleOrdinal();
		if(userId==0) userId = studyEventBeanContainer.getEvent().getOwnerId();
		StudyEventBean studyEvent = studyEventBeanContainer.getEvent();
        
		
		ArrayList<RuleSetBean> ruleSets = (ArrayList<RuleSetBean>) createRuleSet(studyEventDefId);
		for (RuleSetBean ruleSet : ruleSets) {
			ArrayList<RuleSetBean> ruleSetBeans = new ArrayList();		
			ExpressionBean eBean = new ExpressionBean();
			eBean.setValue(ruleSet.getTarget().getValue() + ".A.B");
			ruleSet.setTarget(eBean);
			ruleSet.addExpression(getRuleSetService().replaceSEDOrdinal(ruleSet.getTarget(), studyEvent));
			ruleSetBeans.add(ruleSet);

			// for (RuleSetBean ruleSet : ruleSetBeans){
			String targetProperty = ruleSet.getTarget().getValue().substring(ruleSet.getTarget().getValue().indexOf("."));

			if ((targetProperty.contains(ExpressionService.STARTDATE + ".A.B") && studyEventBeanContainer.getChangeDetails().getStartDateChanged())
					|| (targetProperty.contains(ExpressionService.STATUS + ".A.B") && studyEventBeanContainer.getChangeDetails().getStatusChanged())) {

				getRuleSetService().runIndividualRulesInBeanProperty(ruleSetBeans, userId, studyEventBeanContainer.getChangeDetails(), studyEventOrdinal);
			}
		}
//	}
		
	
		
	}
	private List<RuleSetBean> createRuleSet(Integer studyEventDefId) {
		return getRuleSetDao().findAllByStudyEventDefIdWhereItemIsNull(studyEventDefId);
		
	}
	public DataSource getDataSource() {
		return dataSource;
	}
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.cntxt = applicationContext;
		
	}

	

}
