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
		System.out.println("The time is now " + currentDateFormat.format(new Date()));
		triggerJob();
	}

	public void triggerJob() throws NumberFormatException, ParseException {
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
		
					ruleSetService.runRulesInBeanProperty(ruleSets ,1, studyEventChangeDetails);

				}
			}
		}
	}

	public void trigger(RuleSetBean ruleSet, StudySubjectBean studySubjectBean, StudyBean studyBean) {
		ArrayList<RuleSetBean> ruleSets = new ArrayList<>();
		ruleSets.add(ruleSet);
		sedao = new StudyEventDAO(ds);
		seddao = new StudyEventDefinitionDAO(ds);
		StudyEventDefinitionBean sedBean = null;
		ArrayList<StudyEventDefinitionBean> sedBeans = null;
		if (ruleSet.getStudyEventDefinitionId() == null) {
			sedBeans = (ArrayList<StudyEventDefinitionBean>) seddao.findAllActiveByStudy(studyBean);
		} else {
			sedBean = (StudyEventDefinitionBean) seddao.findByPK(ruleSet.getStudyEventDefinitionId());
			sedBeans = new ArrayList<>();
			sedBeans.add(sedBean);
		}
		String fullTargetExpression = "";
		String sedFullTargetExpression = "";
		String groupOid = "";
		String crfOid = "";
		boolean isTargetItemSpecific = false;
		boolean isTargetEventSpecific = false;

		System.out.println(" Original target expression: " + ruleSet.getOriginalTarget().getValue());

		if (ruleSet.getItemId() != null) {
			idao = new ItemDAO(ds);
			iBean = (ItemBean) idao.findByPK(ruleSet.getItemId());
			String itemOid = iBean.getOid();

			igdao = new ItemGroupDAO(ds);
			if (ruleSet.getItemGroupId() != null) {
				ItemGroupBean igBean = (ItemGroupBean) igdao.findByPK(ruleSet.getItemGroupId());
				groupOid = igBean.getOid();
			} else {
				ArrayList<ItemGroupBean> igBeans = (ArrayList<ItemGroupBean>) igdao.findGroupsByItemID(ruleSet.getItemId());
				groupOid = igBeans.get(0).getOid();
			}

			cdao = new CRFDAO(ds);
			if (ruleSet.getCrfId() != null) {
				CRFBean crfBean = (CRFBean) cdao.findByPK(ruleSet.getCrfId());
				crfOid = crfBean.getOid();
			} else {
				CRFBean crfBean = cdao.findByItemOid(iBean.getOid());
				crfOid = crfBean.getOid();

			}
			fullTargetExpression = "." + crfOid + "." + groupOid + "." + itemOid;
		}

		for (StudyEventDefinitionBean sed : sedBeans) {
			ArrayList<StudyEventBean> seBeans = sedao.findAllByDefinitionAndSubject(sed, studySubjectBean);
			StudyEventChangeDetails studyEventChangeDetails = new StudyEventChangeDetails(true, true);
			for (StudyEventBean seBean : seBeans) {
				ResourceBundleProvider.updateLocale(Locale.getDefault());
				edao = new EventCRFDAO<>(ds);
				ArrayList<EventCRFBean> ecrfs = edao.findAllByStudyEventAndCrfOrCrfVersionOid(seBean, crfOid);
				if (ruleSet.getItemId() != null) {
					isTargetItemSpecific = true;
					// sedFullTargetExpression = sed.getOid() + "[" + seBean.getId() + "]" + fullTargetExpression;
					sedFullTargetExpression = ruleSet.getTarget().getValue();
					// for (EventCRFBean ecrf : ecrfs) {
					iddao = new ItemDataDAO(ds);
					// ItemDataBean idBean = iddao.findByItemIdAndEventCRFId(iBean.getId(), ecrf.getId());
					// if (ecrf.getStudySubjectId() == studySubjectBean.getId() && idBean.isActive()) {

					// System.out.println("Item Target Expression:  " + sedFullTargetExpression);
					System.out.println("StudySubject:  " + studySubjectBean.getId());
					ruleSets = (ArrayList<RuleSetBean>) ruleSetService.filterByStatusEqualsAvailable(ruleSets);
					ruleSets = (ArrayList<RuleSetBean>) ruleSetService.filterRuleSetsByStudyEventOrdinal(ruleSets, null);
					ruleSets = (ArrayList<RuleSetBean>) ruleSetService.filterRuleSetsByGroupOrdinal(ruleSets);

//					ruleSetService.runRulesInBeanProperty(ruleSets, studySubjectBean.getId(), 1, seBean.getSampleOrdinal(), studyEventChangeDetails);
					// }
					// }
				} else {
					isTargetEventSpecific = true;
					sedFullTargetExpression = null;
					System.out.println("Event Target Expression");
					System.out.println("StudySubject:  " + studySubjectBean.getId());
//					ruleSetService.runRulesInBeanProperty(ruleSets, studySubjectBean.getId(), 1, seBean.getSampleOrdinal(), studyEventChangeDetails);
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