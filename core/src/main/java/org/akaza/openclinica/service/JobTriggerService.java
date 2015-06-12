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
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import org.akaza.openclinica.dao.admin.CRFDAO;
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
	UserAccountDAO uadao;
	StudyDAO sdao;
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


	private static final SimpleDateFormat currentDateFormat = new SimpleDateFormat("HH:mm:ss");

	public JobTriggerService(DataSource ds, RuleSetDao ruleSetDao, RuleSetService ruleSetService) {
		this.ds = ds;
		this.ruleSetDao = ruleSetDao;
		this.ruleSetService = ruleSetService;
	}

	// @Scheduled(cron = "0 0/2 * * * ?") // trigger every 2 minutes
	@Scheduled(cron = "0 0/1 * * * ?")
	// trigger every minute
	// @Scheduled(cron = "0 0 0/1 * * ?")
	// trigger every hour
	public void hourlyJobTrigger() throws NumberFormatException, ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH");
		Date now = new Date();
		int serverTime = Integer.parseInt(dateFormat.format(now));
		System.out.println("The time is now " + currentDateFormat.format(new Date()));
		triggerJob(serverTime);
	}

	public void triggerJob(int serverTime) throws NumberFormatException, ParseException {
		TimeZone serverZone = TimeZone.getDefault();
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH");
		uadao = new UserAccountDAO(ds);
		ssdao = new StudySubjectDAO(ds);
		sdao = new StudyDAO(ds);
		TimeZone ssZone;
		int runTime = 23;
		ArrayList<RuleSetBean> ruleSets = ruleSetDao.findAllRunOnSchedules(true);
		for (RuleSetBean ruleSet : ruleSets) {
			if (ruleSet.getStatus().AVAILABLE != null) {
				StudyBean studyBean = (StudyBean) sdao.findByPK(ruleSet.getStudyId());
				ArrayList<UserAccountBean> userAccounts = (ArrayList<UserAccountBean>) uadao.findAllParticipantsByStudyOid(studyBean.getOid());
				ArrayList<StudySubjectBean> ssBeans = (ArrayList<StudySubjectBean>) ssdao.findAllByStudy(studyBean);
				if (ruleSet.getRunTime() != null)
					runTime = Integer.parseInt(dateFormat.format(dateFormat.parse(ruleSet.getRunTime())));
				for (StudySubjectBean ssBean : ssBeans) {
					Boolean doTrigger = false;
					String ssZoneId = ssBean.getTime_zone().trim();
					if (ssZoneId != "") {
						ssZone = TimeZone.getTimeZone(ssZoneId);
					} else {
						ssZone = serverZone;
					}

					doTrigger = calculateTimezoneDiff(serverZone, ssZone, runTime, serverTime);
					if (doTrigger) {
						trigger(ruleSet, ssBean, studyBean);

					}
				}
			}
		}
	}

	public Boolean calculateTimezoneDiff(TimeZone serverZone, TimeZone ssZone, int runTime, int serverTime) {
		int timeDifference = (serverZone.getRawOffset() + serverZone.getDSTSavings() - (ssZone.getRawOffset() + ssZone.getDSTSavings())) / (1000 * 60 * 60);
		int newSetTime = runTime + timeDifference;
		if (newSetTime > 23)
			newSetTime = newSetTime - 24;
		if (newSetTime < 0)
			newSetTime = newSetTime + 24;
		if (serverTime == newSetTime) {
			return true;
		} else {
			return false;
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
		if (ruleSet.getItemId() != null) {
			idao = new ItemDAO(ds);
			ItemBean iBean = (ItemBean) idao.findByPK(ruleSet.getItemId());
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
					sedFullTargetExpression = sed.getOid() + "[" + seBean.getId() + "]" + fullTargetExpression;

					for (EventCRFBean ecrf : ecrfs) {
						if (ecrf.getStudySubjectId() == studySubjectBean.getId()) {

							System.out.println("Item Target Expression:  " + sedFullTargetExpression);
							System.out.println("StudySubject:  " + studySubjectBean.getId());
							ruleSetService.runRulesInBeanProperty(ruleSets, studySubjectBean.getId(), 1, seBean.getSampleOrdinal(), studyEventChangeDetails, sedFullTargetExpression,
									isTargetItemSpecific, isTargetEventSpecific);
						}
					}
				} else {
					isTargetEventSpecific = true;
					sedFullTargetExpression = null;

					System.out.println("Event Target Expression");
					System.out.println("StudySubject:  " + studySubjectBean.getId());
					ruleSetService.runRulesInBeanProperty(ruleSets, studySubjectBean.getId(), 1, seBean.getSampleOrdinal(), studyEventChangeDetails, sedFullTargetExpression, isTargetItemSpecific,
							isTargetEventSpecific);
				}

			}
		}
	}

}