package core.org.akaza.openclinica.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.sql.DataSource;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.submit.ItemBean;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.RuleSetDao;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.dao.submit.CRFVersionDAO;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.dao.submit.ItemDAO;
import core.org.akaza.openclinica.dao.submit.ItemDataDAO;
import core.org.akaza.openclinica.dao.submit.ItemGroupDAO;
import core.org.akaza.openclinica.dao.submit.ItemGroupMetadataDAO;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.rule.RuleSetBean;
import core.org.akaza.openclinica.domain.rule.action.NotificationActionProcessor;
import core.org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import core.org.akaza.openclinica.ocobserver.StudyEventChangeDetails;
import core.org.akaza.openclinica.service.rule.RuleSetService;
import org.apache.commons.lang.StringUtils;
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

	public JobTriggerService(DataSource ds, RuleSetDao ruleSetDao, RuleSetService ruleSetService, StudyDao studyDao) {
		this.ds = ds;
		this.ruleSetDao = ruleSetDao;
		this.ruleSetService = ruleSetService;
		this.studyDao = studyDao;
	}

	// @Scheduled(cron = "0 0/2 * * * ?") // trigger every 2 minutes
	// @Scheduled(cron = "0 0/1 * * * ?") // trigger every minute
	 @Scheduled(cron = "0 0 0/1 * * ?") // trigger every hour
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
			if (StringUtils.isEmpty(schema)) {
				logger.error("Schema should not be null");
				continue;
			}
			ArrayList<RuleSetBean> ruleSets = ruleSetDao.findAllRunOnSchedulesPerSchema(true, schema);
			for (RuleSetBean ruleSet : ruleSets) {
				if (ruleSet.getStatus().AVAILABLE != null && ruleSet.isRunSchedule()) {
					CoreResources.tenantSchema.set(schema);

//					if(ruleSet.getItemId()!=null){
//						// item Specific Rule
//						logger.debug("*** Item Specific Rule ***");
//						ArrayList<RuleSetBean> ruleSetBeans = new ArrayList<>();
//						Study currentStudy = (Study) studyDao.findByPK(ruleSet.getStudyId());
//						ResourceBundleProvider.updateLocale(Locale.getDefault());
//						UserAccountBean ub = (UserAccountBean) getUserAccountDao().findByPK(1);
//						ruleSetBeans.add(ruleSet);
//						ruleSetService.runRulesInBulk(ruleSetBeans, false, currentStudy, ub, true);
//					} else {
					if(ruleSet.getItemId() == null){
						// Event Specific Rule
						logger.debug("*** Event Specific Rule ***");
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

}