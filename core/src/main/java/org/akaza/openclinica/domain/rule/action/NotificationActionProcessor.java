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
import org.akaza.openclinica.patterns.ocobserver.StudyEventChangeDetails;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
	EventDefinitionCRFDAO edcdao;
	StudyEventDAO sedao;
	CRFDAO cdao;
	EventCRFDAO ecdao;
	StudySubjectDAO ssdao;
	UserAccountDAO udao;

	public NotificationActionProcessor(DataSource ds, JavaMailSenderImpl mailSender, RuleSetRuleBean ruleSetRule) {
		this.ds = ds;
		this.mailSender = mailSender;
		this.ruleSetRule = ruleSetRule;
		edcdao = new EventDefinitionCRFDAO(ds);
		sedao = new StudyEventDAO(ds);
		cdao = new CRFDAO(ds);
		ecdao = new EventCRFDAO(ds);
		ssdao = new StudySubjectDAO(ds);
		udao = new UserAccountDAO(ds);

	}

	public RuleActionBean execute(RuleRunnerMode ruleRunnerMode, ExecutionMode executionMode, RuleActionBean ruleAction, StudyBean currentStudy, ParticipantDTO pDTO) {
		switch (executionMode) {
		case DRY_RUN: {
			return ruleAction;
		}

		case SAVE: {
			sendEmail(ruleAction, pDTO);
			return null;
		}
		default:
			return null;
		}
	}

	private void sendEmail(RuleActionBean ruleAction, ParticipantDTO pDTO) throws OpenClinicaSystemException {

		logger.info("Sending email...");
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();

			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
			helper.setFrom(EmailEngine.getAdminEmail());
			helper.setTo(pDTO.getEmailAccount());
			helper.setSubject(pDTO.getEmailSubject());
			helper.setText(pDTO.getMessage());

			mailSender.send(mimeMessage);
			logger.debug("Email sent successfully on {}", new Date());
		} catch (MailException me) {
			logger.error("Email could not be sent");
			throw new OpenClinicaSystemException(me.getMessage());
		} catch (MessagingException me) {
			logger.error("Email could not be sent");
			throw new OpenClinicaSystemException(me.getMessage());
		}
	}

	@Override
	public RuleActionBean execute(RuleRunnerMode ruleRunnerMode, ExecutionMode executionMode, RuleActionBean ruleAction, ItemDataBean itemDataBean, String itemData, StudyBean currentStudy,
			UserAccountBean ub, Object... arguments) {
		// TODO Auto-generated method stub
		return null;
	}

	public void runNotificationAction(RuleActionBean ruleActionBean, RuleSetBean ruleSet) {
		String emailList = ((NotificationActionBean) ruleActionBean).getTo();
		String message = ((NotificationActionBean) ruleActionBean).getMessage();

		int sed_Id = ruleSet.getStudyEventDefinitionId();
		int studyId = ruleSet.getStudyId();

		String eventName = getStudyEventDefnBean(sed_Id).getName();
		String studyName = getStudyBean(studyId).getName();
		message = message.replaceAll("\\$event.name", "'" + eventName + "'");
		message = message.replaceAll("\\$study.name", "'" + studyName + "'");

		System.out.println("eventName:  " + eventName);
		System.out.println("studyName:  " + studyName);
		ParticipantDTO pDTO = null;
		StudyBean studyBean = getStudyBean(studyId);
		String[] listOfEmails = emailList.split(",");
		for (String email : listOfEmails) {

			if (email.trim().equals("$participant")) {

				ArrayList<EventDefinitionCRFBean> edcList = (ArrayList<EventDefinitionCRFBean>) edcdao.findAllActiveByEventDefinitionIdandStudyId(sed_Id, studyId);
				int count = edcList.size();
				ArrayList<StudyEventBean> seBeans = sedao.findAllByStudyAndEventDefinitionId(studyBean, sed_Id);
				for (StudyEventBean seBean : seBeans) {
					ArrayList<EventCRFBean> ecBeans = ecdao.findAllByStudyEventInParticipantForm(seBean, sed_Id, studyId);
					int noItemDataInEventCRFCount = 0;
					for (EventCRFBean ecBean : ecBeans) {
						ItemDataDAO iddao = new ItemDataDAO(ds);
						ArrayList<ItemDataBean> idBean = iddao.findAllByEventCRFId(ecBean.getId());
						if (idBean.isEmpty())
							noItemDataInEventCRFCount++;
					}

					StudySubjectBean ssBean = (StudySubjectBean) ssdao.findByPK(seBean.getStudySubjectId());

					if (ecBeans.size() - noItemDataInEventCRFCount != count) {
						pDTO = getParticipantInfo(ds, ssBean, studyBean);
						if (pDTO != null && pDTO.getAccessCode() != null && pDTO.getEmailAccount() != null) {
							String msg = null;
							msg = message.replaceAll("\\$participant.accessCode", "'" + pDTO.getAccessCode() + "'");
							msg = msg.replaceAll("\\$participant.firstname", "'" + pDTO.getfName() + "'");
							pDTO.setMessage(msg);
							System.out.println(pDTO.getMessage());
							// execute(RuleRunnerMode.RULSET_BULK, ExecutionMode.SAVE, ruleActionBean, studyBean, pDTO);
						}
					}
				}

			} else {
				pDTO = new ParticipantDTO();
				String msg = null;
				msg = message.replaceAll("\\$participant.accessCode", " ");
				msg = msg.replaceAll("\\$participant.firstname", " ");
				pDTO.setMessage(msg);
				pDTO.setEmailAccount(email);
				System.out.println(pDTO.getMessage());
				// execute(RuleRunnerMode.RULSET_BULK, ExecutionMode.SAVE, ruleActionBean, studyBean, pDTO);
			}
		}
	}

	public void runNotificationActionOld(RuleActionBean ruleActionBean, StudyBean currentStudy) {
		String emailList = ((NotificationActionBean) ruleActionBean).getTo();
		StudyBean parentStudy = getParentStudy(ds, currentStudy);
		String[] listOfEmails = emailList.split(",");
		for (String email : listOfEmails) {

			if (email.trim().equals("$participant")) {
				ArrayList<ParticipantDTO> pDTOList = getAllParticipantPerStudy(ds, currentStudy, ruleSetRule, ruleActionBean);
				for (ParticipantDTO pDTO : pDTOList) {
					String msg = pDTO.getMessage().toString();
					msg = msg.replaceAll("\\$event.name", "'" + pDTO.getEventName() + "'");
					msg = msg.replaceAll("\\$study.name", "'" + pDTO.getStudyName() + "'");
					msg = msg.replaceAll("\\$participant.accessCode", "'" + pDTO.getAccessCode() + "'");
					msg = msg.replaceAll("\\$participant.firstname", "'" + pDTO.getfName() + "'");
					pDTO.setMessage(msg);
					execute(RuleRunnerMode.RULSET_BULK, ExecutionMode.SAVE, ruleActionBean, parentStudy, pDTO);

					RestTemplate rt = new RestTemplate();
					String pUrl = CoreResources.getField("portalURL") + "/rest/oc/email";

					// rt.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

					rt.getMessageConverters().add(new StringHttpMessageConverter());

					try {
						ParticipantDTO returns = rt.postForObject(pUrl, pDTO, ParticipantDTO.class);
					} catch (Exception e) {

					}

				}
			} else {
				String message = ((NotificationActionBean) ruleActionBean).getMessage();
				ParticipantDTO pDTO = new ParticipantDTO();
				pDTO.setMessage(message);
				pDTO.setEmailAccount(email);
				execute(RuleRunnerMode.RULSET_BULK, ExecutionMode.SAVE, ruleActionBean, parentStudy, pDTO);
			}
		}
	}

	public ArrayList<ParticipantDTO> getAllParticipantPerStudy(DataSource ds, StudyBean currentStudy, RuleSetRuleBean ruleSetRule, RuleActionBean ruleActionBean) {
		ParticipantDTO pDTO = null;
		ArrayList<ParticipantDTO> pDTOList = new ArrayList<>();
		ArrayList<StudySubjectBean> ssBeans = getAllParticipantStudySubjectsPerStudy(currentStudy.getId(), ds);
		for (StudySubjectBean ssBean : ssBeans) {
			StudyEventBean seBean = getStudyEvent(ssBean, ds);
			int ordinal = seBean.getSampleOrdinal();
			StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(ds);
			String eventName = seddao.findByPK(seBean.getStudyEventDefinitionId()).getName();
			if (ordinal != 1)
				eventName = eventName + "(" + ordinal + ")";

			pDTO = getParticipantInfo(ds, ssBean, currentStudy);
			if (pDTO.getAccessCode() != null && seBean.isActive()) {
				pDTO.setMessage(((NotificationActionBean) ruleActionBean).getMessage());
				pDTO.setEventName(eventName);
				pDTOList.add(pDTO);
			}
		}
		return pDTOList;
	}

	public ParticipantDTO getParticipantInfo(DataSource ds, StudySubjectBean ssBean, StudyBean studyBean) {
		ParticipantDTO pDTO = null;
		StudyBean parentStudyBean = getParentStudy(ds, studyBean);
		String pUserName = parentStudyBean.getOid() + "." + ssBean.getOid();
		UserAccountBean uBean = (UserAccountBean) udao.findByUserName(pUserName);
		if (uBean != null || uBean.isActive()) {
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

	};

}
