package org.akaza.openclinica.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.RuleSetAuditDao;
import org.akaza.openclinica.dao.hibernate.RuleSetDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.rule.RuleSetDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.dao.submit.ItemGroupMetadataDAO;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.VersioningMap;
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
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.logic.expressionTree.OpenClinicaExpressionParser;
import org.akaza.openclinica.patterns.ocobserver.StudyEventChangeDetails;
import org.akaza.openclinica.service.rule.RuleSetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobTriggerService {
	RuleSetDao ruleSetDao;
	DataSource ds;
	UserAccountDAO userAccountDao;
	StudyDAO studyDao;
	StudySubjectDAO ssdao;
	StudyEventDAO sedao;
	StudyEventDefinitionDAO seddao;
	ItemDAO idao;
	ItemGroupMetadataDAO igmdao;
	ItemGroupDAO igdao;
	CRFDAO cdao;
	CRFVersionDAO cvdao;
	EventCRFDAO edao;
	NotificationActionProcessor notificationActionProcessor;
	RuleSetService ruleSetService;
	ItemDataDAO iddao;
	ItemBean iBean;
	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

	private static final SimpleDateFormat currentDateFormat = new SimpleDateFormat("HH:mm:ss");

	public JobTriggerService(DataSource ds, RuleSetDao ruleSetDao, RuleSetService ruleSetService) {
		this.ds = ds;
		this.ruleSetDao = ruleSetDao;
		this.ruleSetService = ruleSetService;
	}

	// @Scheduled(cron = "0 0/2 * * * ?") // trigger every 2 minutes
//	@Scheduled(cron = "0 0/1 * * * ?")
	// trigger every minute
	 @Scheduled(cron = "0 0 0/1 * * ?")
	// trigger every hour
	public void hourlyJobTrigger() throws NumberFormatException, ParseException {
		logger.debug("The time is : " + new Date());
		triggerJob();
	}

	public void triggerJob(){
		ResourceBundleProvider.updateLocale(new Locale("en_US"));
		ArrayList<RuleSetBean> ruleSets = ruleSetDao.findAllRunOnSchedules(true);
		for (RuleSetBean ruleSet : ruleSets) {
			if (ruleSet.getStatus().AVAILABLE != null && ruleSet.isRunSchedule()) {
				if(ruleSet.getItemId()!=null){ 
                 // item Specific Rule
					System.out.println("*** Item Specific Rule ***");
				ArrayList<RuleSetBean> ruleSetBeans = new ArrayList<>();
				StudyBean currentStudy = (StudyBean) getStudyDao().findByPK(ruleSet.getStudyId());
				ResourceBundleProvider.updateLocale(Locale.getDefault());
				UserAccountBean ub = (UserAccountBean) getUserAccountDao().findByPK(1);
				ruleSetBeans.add(ruleSet);
				ruleSetService.runRulesInBulk(ruleSetBeans, false, currentStudy, ub, true);
				}else{
			// Event Specific Rule		
					System.out.println("*** Event Specific Rule ***");
				    StudyEventChangeDetails studyEventChangeDetails = new StudyEventChangeDetails(true, true);
					ArrayList<RuleSetBean> ruleSetBeans = new ArrayList<>();
					ExpressionBean eBean = new ExpressionBean();
					eBean.setValue(ruleSet.getTarget().getValue()+".A.B");
					
					ruleSet.setTarget(eBean);
					ruleSetBeans.add(ruleSet);
		
					ruleSetService.runRulesInBeanProperty(ruleSetBeans ,1, studyEventChangeDetails);

				}
			}
		}
	}



	public StudySubjectDAO getStudySubjecdao() {
		return new StudySubjectDAO(ds);
	}

	public UserAccountDAO getUserAccountDao() {
		return new UserAccountDAO(ds);
	}

	public StudyDAO getStudyDao() {
		return new StudyDAO(ds);
	}

}