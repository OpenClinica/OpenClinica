package org.akaza.openclinica.domain.rule.action;

import org.akaza.openclinica.bean.login.ParticipantDTO;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.dao.hibernate.RuleActionRunLogDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
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
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

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

	public NotificationActionProcessor(DataSource ds, JavaMailSenderImpl mailSender, RuleSetRuleBean ruleSetRule) {
		this.ds = ds;
		this.mailSender = mailSender;
		this.ruleSetRule = ruleSetRule;
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

	public void runNotificationAction(RuleActionBean ruleActionBean, StudyBean currentStudy) {
		String emailList = ((NotificationActionBean) ruleActionBean).getEmailTo();
		StudyBean parentStudy = getParentStudy(ds, currentStudy);
		String[] listOfEmails = emailList.split(",");
		for (String email : listOfEmails) {

			if (email.trim().equals("$participant")) {
				ArrayList<ParticipantDTO> pDTOList = getAllParticipantPerStudy(ds, currentStudy, ruleSetRule, ruleActionBean);
				for (ParticipantDTO pDTO : pDTOList) {
					String msg = pDTO.getMessage().toString();
					msg = msg.replaceAll("\\$eventName", "'" + pDTO.getEventName().toString() + "'");
					msg = msg.replaceAll("\\$studyName", "'" + pDTO.getStudyName().toString() + "'");
					msg = msg.replaceAll("\\$accessCode", "'" + pDTO.getAccessCode() + "'");
					msg = msg.replaceAll("\\$firstName", "'" + pDTO.getfName().toString() + "'");
					pDTO.setMessage(msg);
					execute(RuleRunnerMode.RULSET_BULK, ExecutionMode.SAVE, ruleActionBean, parentStudy, pDTO);
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

	public ParticipantDTO getParticipantInfo(DataSource ds, StudySubjectBean ssBean, StudyBean currentStudy) {
		StudySubjectDAO ssdao = new StudySubjectDAO(ds);
		UserAccountDAO udao = new UserAccountDAO(ds);
		ParticipantDTO pDTO = null;
		HashMap<String, String> map = new HashMap();
		StudyBean parentStudyBean = getParentStudy(ds, currentStudy);
		String pUserName = parentStudyBean.getOid() + "." + ssBean.getOid();
		UserAccountBean uBean = (UserAccountBean) udao.findByUserName(pUserName);
		if (uBean != null || uBean.isActive()) {
			if (uBean.getEmail()==null)  return null;
			pDTO = new ParticipantDTO();
			pDTO.setAccessCode(uBean.getAccessCode());
			pDTO.setfName(uBean.getFirstName());
			pDTO.setEmailAccount(uBean.getEmail());
			pDTO.setStudyName(parentStudyBean.getName());
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

}
