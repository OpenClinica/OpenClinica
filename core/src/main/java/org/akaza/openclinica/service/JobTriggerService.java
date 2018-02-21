package org.akaza.openclinica.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.hibernate.RuleSetDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.dao.submit.ItemGroupMetadataDAO;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.action.NotificationActionProcessor;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.patterns.ocobserver.StudyEventChangeDetails;
import org.akaza.openclinica.service.rule.RuleSetService;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobTriggerService {
	RuleSetDao ruleSetDao;
	DataSource ds;
	UserAccountDAO userAccountDao;
	@Autowired
	StudyDao studyDao;
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
	// @Scheduled(cron = "0 0/1 * * * ?") // trigger every minute
	@Scheduled(cron = "0 0 0/1 * * ?")
	// trigger every hour
	public void hourlyJobTrigger() throws NumberFormatException, ParseException {
	    try {
    		logger.debug("The time is now " + currentDateFormat.format(new Date()));
    		triggerJob();
	    } catch (Exception e) {
	        logger.error(e.getMessage());
	        logger.error(ExceptionUtils.getStackTrace(e));
	        throw e;
	    }
	}

	public void triggerJob(){
		ResourceBundleProvider.updateLocale(new Locale("en_US"));
		List<String> schemas = studyDao.findAllSchemas();
		for (String schema : schemas) {
			ArrayList<RuleSetBean> ruleSets = ruleSetDao.findAllRunOnSchedulesPerSchema(true, schema);
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
					} else {
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