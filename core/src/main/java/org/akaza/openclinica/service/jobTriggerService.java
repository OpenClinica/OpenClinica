package org.akaza.openclinica.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.dao.hibernate.RuleSetAuditDao;
import org.akaza.openclinica.dao.hibernate.RuleSetDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.rule.RuleSetDAO;
import org.akaza.openclinica.domain.Status;
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
import org.akaza.openclinica.logic.expressionTree.OpenClinicaExpressionParser;
import org.akaza.openclinica.patterns.ocobserver.StudyEventChangeDetails;
import org.akaza.openclinica.service.rule.RuleSetService;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class jobTriggerService {
	RuleSetDao ruleSetDao;
	DataSource ds;
	UserAccountDAO uadao;
	StudyDAO sdao;
	StudySubjectDAO ssdao;
	StudyEventDAO sedao;
	StudyEventDefinitionDAO seddao;
	NotificationActionProcessor notificationActionProcessor;
	RuleSetService ruleSetService;

	private static final SimpleDateFormat currentDateFormat = new SimpleDateFormat("HH:mm:ss");

	public jobTriggerService(DataSource ds, RuleSetDao ruleSetDao, RuleSetService ruleSetService) {
		this.ds = ds;
		this.ruleSetDao = ruleSetDao;
		this.ruleSetService = ruleSetService;
	}

//	 @Scheduled(cron = "0 0/2 * * * ?") // trigger every minute
//	 @Scheduled(cron = "0 0/1 * * * ?") // trigger every minute
	@Scheduled(cron = "0 0 0/1 * * ?")
	// trigger every hour
	public void reportCurrentTime() throws ParseException {
		System.out.println("The time is now " + currentDateFormat.format(new Date()));
		// Time serverTime = now();
		uadao = new UserAccountDAO(ds);
		ssdao = new StudySubjectDAO(ds);
		sdao = new StudyDAO(ds);
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH");
		Date now = new Date();
		int serverTime = Integer.parseInt(dateFormat.format(now));
		TimeZone serverZone = TimeZone.getDefault();
		int runTime = 23;
		ArrayList<RuleSetBean> ruleSets = ruleSetDao.findAllRunOnSchedules(true);
		for (RuleSetBean ruleSet : ruleSets) {
			StudyBean studyBean = (StudyBean) sdao.findByPK(ruleSet.getStudyId());
			ArrayList<UserAccountBean> userAccounts = (ArrayList<UserAccountBean>) uadao.findAllParticipantsByStudyOid(studyBean.getOid());
			ArrayList<StudySubjectBean> ssBeans = (ArrayList<StudySubjectBean>) ssdao.findAllByStudy(studyBean);
			if (ruleSet.getRunTime() != null)
				runTime = Integer.parseInt(dateFormat.format(dateFormat.parse(ruleSet.getRunTime())));
			for (StudySubjectBean ssBean : ssBeans) {

				String ssTimeZone = ssBean.getTime_zone();
				
				if (ssTimeZone != null && ssTimeZone !="") {
				    ssTimeZone = ssTimeZone.trim();
					int timeDifference = 0;
					TimeZone ssZone = TimeZone.getTimeZone(ssTimeZone);
					timeDifference = (serverZone.getRawOffset() + serverZone.getDSTSavings() - (ssZone.getRawOffset() + ssZone.getDSTSavings())) / (1000 * 60 * 60);
					int newSetTime = runTime + timeDifference;
					if (newSetTime > 23)
						newSetTime = newSetTime - 24;
					if (newSetTime < 0)
						newSetTime = newSetTime + 24;

					if (serverTime == newSetTime) {
						trigger(ruleSet, ssBean);
					}
				}else{
					// Non Participant Subjects // Run on Server TimeZone
					if (serverTime == runTime) {
						trigger(ruleSet, ssBean);
					}
				}
			}
		}

	}

	public void trigger(RuleSetBean ruleSet, StudySubjectBean studySubjectBean) {
		ArrayList<RuleSetBean> ruleSets = new ArrayList<>();
		ruleSets.add(ruleSet);
		sedao = new StudyEventDAO(ds);
		seddao = new StudyEventDefinitionDAO(ds);
		StudyEventDefinitionBean sedBean = (StudyEventDefinitionBean) seddao.findByPK(ruleSet.getStudyEventDefinitionId());
		ArrayList<StudyEventBean> seBeans = sedao.findAllByDefinitionAndSubject(sedBean, studySubjectBean);
		StudyEventChangeDetails studyEventChangeDetails = new StudyEventChangeDetails(true, true);
		for (StudyEventBean seBean : seBeans) {
			ruleSetService.runRulesInBeanProperty(ruleSets, studySubjectBean.getId(), 1, seBean.getSampleOrdinal(), studyEventChangeDetails);
		}

	}

}