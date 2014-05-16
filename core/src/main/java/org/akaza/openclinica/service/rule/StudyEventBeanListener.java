package org.akaza.openclinica.service.rule;

import java.util.List;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.dao.hibernate.RuleSetDao;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.patterns.ocobserver.Listener;
import org.akaza.openclinica.patterns.ocobserver.Observer;
import org.akaza.openclinica.patterns.ocobserver.OnStudyEventJDBCBeanChanged;
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
	System.out.println("Triggering the rules based on event updates");
	
		System.out.println("RuleSetDao"+ruleSetDao);
		StudyEventBean studyEventBean = (StudyEventBean)lstnr;

		Integer studyEventDefId = studyEventBean.getStudyEventDefinitionId();
		Integer studySubjectId = studyEventBean.getStudySubjectId();
		Integer studyEventOrdinal = studyEventBean.getSampleOrdinal();
		getRuleSetService().runRulesInBeanProperty(createRuleSet(studyEventDefId),studySubjectId,studyEventOrdinal);
		
		
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
