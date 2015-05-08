package org.akaza.openclinica.domain.rule.action;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.login.ParticipantDTO;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.RuleActionRunLogDao;
import org.akaza.openclinica.dao.hibernate.RuleSetDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.logic.rulerunner.ExecutionMode;
import org.akaza.openclinica.logic.rulerunner.RuleSetBulkRuleRunner;
import org.akaza.openclinica.logic.rulerunner.RuleRunner.RuleRunnerMode;
import org.akaza.openclinica.patterns.ocobserver.OnStudyEventUpdated;
import org.akaza.openclinica.patterns.ocobserver.StudyEventChangeDetails;
import org.akaza.openclinica.service.pmanage.Authorization;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.akaza.openclinica.service.rule.RuleSetService;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.CommonsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.sql.DataSource;

public class NotificationActionProcessor implements ActionProcessor {

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	DataSource ds;
	EmailEngine emailEngine;
	JavaMailSenderImpl mailSender;
	RuleSetRuleBean ruleSetRule;
	StudySubjectDAO ssdao;
	UserAccountDAO udao;
	RuleSetService ruleSetService;
	RuleSetDao ruleSetDao;
	public static final int PARTICIPATE_READ_TIMEOUT = 5000;

	public NotificationActionProcessor(DataSource ds, JavaMailSenderImpl mailSender, RuleSetRuleBean ruleSetRule) {
		this.ds = ds;
		this.mailSender = mailSender;
		this.ruleSetRule = ruleSetRule;
		ssdao = new StudySubjectDAO(ds);
		udao = new UserAccountDAO(ds);

	}

	@Override
	public RuleActionBean execute(RuleRunnerMode ruleRunnerMode, ExecutionMode executionMode, RuleActionBean ruleAction, ItemDataBean itemDataBean, String itemData, StudyBean currentStudy,
			UserAccountBean ub, Object... arguments) {
		// TODO Auto-generated method stub
		return null;
	}

	public void runNotificationAction(RuleActionBean ruleActionBean, RuleSetBean ruleSet, int studySubjectBeanId, int eventOrdinal) {
		String emailList = ((NotificationActionBean) ruleActionBean).getTo();
		String message = ((NotificationActionBean) ruleActionBean).getMessage();
		String emailSubject = ((NotificationActionBean) ruleActionBean).getSubject();

		int sed_Id = ruleSet.getStudyEventDefinitionId();
		int studyId = ruleSet.getStudyId();

		String eventName = getStudyEventDefnBean(sed_Id).getName();
		if (eventOrdinal != 1)
			eventName = eventName + "(" + eventOrdinal + ")";

		String studyName = getStudyBean(studyId).getName();
		message = message.replaceAll("\\$\\{event.name}", eventName);
		message = message.replaceAll("\\$\\{study.name}", studyName);

		ParticipantDTO pDTO = null;
		StudyBean studyBean = getStudyBean(studyId);
		String[] listOfEmails = emailList.split(",");
		StudySubjectBean ssBean = (StudySubjectBean) ssdao.findByPK(studySubjectBeanId);
		ParticipantPortalRegistrar participantPortalRegistrar = null;
		String hostname = "";
		String url = "";
		if (message.contains("${participant.url}") || message.contains("${participant.loginurl}")) {
			participantPortalRegistrar = new ParticipantPortalRegistrar();

			try {
				hostname = participantPortalRegistrar.getStudyHost(studyBean.getOid());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			url = hostname.replaceAll("login", "plogin");
			message = message.replaceAll("\\$\\{participant.url}", url);
		}

		for (String email : listOfEmails) {

			if (email.trim().equals("${participant}")) {
				pDTO = getParticipantInfo(ds, ssBean, studyBean);
				if (pDTO != null) {
					String msg = null;
					msg = message.replaceAll("\\$\\{participant.accessCode}", pDTO.getAccessCode());
					msg = msg.replaceAll("\\$\\{participant.firstname}", pDTO.getfName());

					String loginUrl = url + "?access_code=" + pDTO.getAccessCode() + "&auto_login=true";
					msg = msg.replaceAll("\\$\\{participant.loginurl}", loginUrl);

					pDTO.setMessage(msg);
					pDTO.setEmailSubject(emailSubject);

					// Send Email thru Local Mail Server
/*					NotificationActionSendingEmail notificationActionSendEmail = new NotificationActionSendingEmail(ds, mailSender, ruleActionBean, pDTO);
					Thread thread = new Thread(notificationActionSendEmail);
					thread.start();
*/
					// send email using Mandrill
					participantPortalRegistrar.sendEmailThruMandrillViaOcui(pDTO);

					System.out.println(pDTO.getMessage() + "   (Email Send to Participant :  " + pDTO.getEmailAccount() + ")");

				} else {
					pDTO = new ParticipantDTO();
					String msg = null;
					msg = message.replaceAll("\\$\\{participant.accessCode}", "");
					msg = msg.replaceAll("\\$\\{participant.firstname}", "");
					msg = msg.replaceAll("\\$\\{participant.loginurl}", "");
					pDTO.setMessage(msg);
					pDTO.setEmailSubject(emailSubject);
				}

			} else {
				pDTO.setEmailAccount(email.trim());
				System.out.println();
				// Send Email thru Local Mail Server
				/*
				 * NotificationActionSendingEmail notificationActionSendEmail = new NotificationActionSendingEmail(ds, mailSender, ruleActionBean, pDTO); Thread thread = new
				 * Thread(notificationActionSendEmail); thread.start();
				 */
				System.out.println(pDTO.getMessage() + "  (Email sent to Hard Coded email address :  " + pDTO.getEmailAccount() + ")");

			}
		}
	}

	public void runInBatch(final OnStudyEventUpdated event, StudyBean studyBean) {

		// seBeans for loop

		Integer studyEventDefId = 1;
		Integer studyEventOrdinal = 1;
		Integer studySubjectId = 1;
		Integer userId = 1;

		if (userId == null && event.getContainer().getEvent().getUserAccount() != null)
			userId = event.getContainer().getEvent().getUserAccount().getUserId();
		getRuleSetService().runRulesInBeanProperty(createRuleSet(studyEventDefId), studySubjectId, userId, studyEventOrdinal, event.getContainer().getChangeDetails());
	}

	public ParticipantDTO getParticipantInfo(DataSource ds, StudySubjectBean ssBean, StudyBean studyBean) {
		ParticipantDTO pDTO = null;
		StudyBean parentStudyBean = getParentStudy(ds, studyBean);
		String pUserName = parentStudyBean.getOid() + "." + ssBean.getOid();
		UserAccountBean uBean = (UserAccountBean) udao.findByUserName(pUserName);
		if (uBean != null && uBean.isActive()) {
			if (uBean.getEmail() == null)
				return null;
			pDTO = new ParticipantDTO();
			pDTO.setAccessCode(uBean.getAccessCode());
			pDTO.setfName(uBean.getFirstName());
			pDTO.setEmailAccount(uBean.getEmail());
		} else {
			return null;
		}

		return pDTO;
	}

	public ArrayList<StudySubjectBean> getAllParticipantStudySubjectsPerStudy(int studyId, DataSource ds) {
		StudySubjectDAO ssdao = new StudySubjectDAO(ds);
		ArrayList<StudySubjectBean> ssBeans = ssdao.findAllByStudyId(studyId);
		return ssBeans;
	}

	public StudyEventBean getStudyEvent(StudySubjectBean ssBean, DataSource ds) {
		StudyEventDAO studyEventDao = new StudyEventDAO(ds);
		StudyEventBean seBean = (StudyEventBean) studyEventDao.getNextScheduledEvent(ssBean.getOid());
		return seBean;
	}

	private StudyBean getParentStudy(DataSource ds, StudyBean study) {
		StudyDAO sdao = new StudyDAO(ds);
		if (study.getParentStudyId() == 0) {
			return study;
		} else {
			StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
			return parentStudy;
		}

	}

	public StudyEventDefinitionBean getStudyEventDefnBean(int sed_Id) {
		StudyEventDefinitionDAO sedao = new StudyEventDefinitionDAO(ds);
		return (StudyEventDefinitionBean) sedao.findByPK(sed_Id);
	};

	public StudyBean getStudyBean(int studyId) {
		StudyDAO sdao = new StudyDAO(ds);
		return (StudyBean) sdao.findByPK(studyId);

	}

	public RuleSetService getRuleSetService() {
		return ruleSetService;
	}

	private List<RuleSetBean> createRuleSet(Integer studyEventDefId) {
		return getRuleSetDao().findAllByStudyEventDefIdWhereItemIsNull(studyEventDefId);

	}

	public RuleSetDao getRuleSetDao() {
		return ruleSetDao;
	}

}
