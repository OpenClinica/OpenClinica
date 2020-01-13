package core.org.akaza.openclinica.domain.rule.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import core.org.akaza.openclinica.bean.login.ParticipantDTO;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.service.StudyParameterValueBean;
import core.org.akaza.openclinica.bean.submit.ItemDataBean;
import core.org.akaza.openclinica.core.EmailEngine;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.RuleSetDao;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudySubject;
import core.org.akaza.openclinica.domain.rule.RuleSetBean;
import core.org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import core.org.akaza.openclinica.logic.rulerunner.ExecutionMode;
import core.org.akaza.openclinica.logic.rulerunner.RuleRunner.RuleRunnerMode;
import core.org.akaza.openclinica.service.*;
import core.org.akaza.openclinica.service.rule.RuleSetService;
import core.org.akaza.openclinica.web.rest.client.auth.impl.KeycloakClientImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.web.client.RestTemplate;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotificationActionProcessor implements ActionProcessor, Runnable {

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	DataSource ds;
	EmailEngine emailEngine;
	JavaMailSenderImpl mailSender;
	RuleSetRuleBean ruleSetRule;
	@Autowired
	StudyDao sdao;
	StudySubjectDAO ssdao;
	UserAccountDAO udao;
	StudyParameterValueDAO spvdao;
	RuleSetService ruleSetService;
	RuleSetDao ruleSetDao;
	ParticipantDTO pDTO;
	RuleActionBean ruleActionBean;
	String email;
	String[] listOfEmails;
	Study studyBean;
	String message;
	String url;
	String emailSubject;
	String participateStatus;
	StudySubject studySubject;
	String accessToken;
	PermissionService permissionService;
	NotificationService notificationService;
	String userUuid;

	public static String sbsUrl = CoreResources.getField("SBSBaseUrl");
	public static String messageServiceUri = sbsUrl+ "/message-service/api/messages/text";
	public static String subDomain = sbsUrl.substring(sbsUrl.indexOf("//")  + 2,  sbsUrl.indexOf("."));

	public NotificationActionProcessor() {
	}

	public NotificationActionProcessor(DataSource ds, JavaMailSenderImpl mailSender, RuleActionBean ruleActionBean, ParticipantDTO pDTO,
									   String email) {
		this.ds = ds;
		this.mailSender = mailSender;
		this.ruleActionBean = ruleActionBean;
		this.pDTO = pDTO;
		this.email = email;

	}

	public NotificationActionProcessor(String[] listOfEmails, StudySubject studySubject, Study studyBean, String message, String emailSubject,
									   JavaMailSenderImpl mailSender , String participateStatus,String accessToken,NotificationService notificationService,String userUuid) {
		this.listOfEmails = listOfEmails;
		this.message = message;
		this.emailSubject = emailSubject;
		this.studySubject = studySubject;
		this.mailSender = mailSender;
		this.studyBean = studyBean;
		this.participateStatus=participateStatus;
		this.accessToken=accessToken;
		this.notificationService=notificationService;
		this.userUuid=userUuid;

	}

	public NotificationActionProcessor(DataSource ds, JavaMailSenderImpl mailSender, RuleSetRuleBean ruleSetRule) {
		this.ds = ds;
		this.mailSender = mailSender;
		this.ruleSetRule = ruleSetRule;
		ssdao = new StudySubjectDAO(ds);
		udao = new UserAccountDAO(ds);
		spvdao = new StudyParameterValueDAO(ds);



	}

	public RuleActionBean execute(ExecutionMode executionMode, RuleActionBean ruleActionBean, ParticipantDTO pDTO , String email  ) {
		switch (executionMode) {
			case DRY_RUN: {
				return ruleActionBean;
			}

			case SAVE: {
				createMimeMessagePreparator(pDTO, email);
				return null;
			}
			default:
				return null;
		}
	}


	private void createMimeMessagePreparator(final ParticipantDTO pDTO, final String email){
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
				message.setFrom(EmailEngine.getAdminEmail());
				message.setTo(email);
				message.setSubject(pDTO.getEmailSubject());
				message.setText(pDTO.getMessage());
			}
		};
		BulkEmailSenderService.addMimeMessage(preparator);
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
	public RuleActionBean execute(RuleRunnerMode ruleRunnerMode, ExecutionMode executionMode, RuleActionBean ruleAction, ItemDataBean itemDataBean, String itemData, Study currentStudy,
								  UserAccountBean ub, Object... arguments) {
		// TODO Auto-generated method stub
		return null;
	}

	public void runNotificationAction(RuleActionBean ruleActionBean, RuleSetBean ruleSet, StudySubject studySubject, Study currentStudy, int eventOrdinal,NotificationService notificationService, KeycloakClientImpl keycloakClientImpl) {
		String emailList = ((NotificationActionBean) ruleActionBean).getTo();
		String message = ((NotificationActionBean) ruleActionBean).getMessage();
		String emailSubject = ((NotificationActionBean) ruleActionBean).getSubject();

		int sed_Id = ruleSet.getStudyEventDefinitionId();
		int studyId = ruleSet.getStudyId();

		String eventName = getStudyEventDefnBean(sed_Id).getName();
		if (eventOrdinal != 1)
			eventName = eventName + "(" + eventOrdinal + ")";

		Study siteBean=null;
		if(currentStudy.isSite()) {    // it is a site level study
			siteBean = currentStudy;
			currentStudy = siteBean.getStudy();
		}

		if (message==null) message="";
		if (emailSubject==null) emailSubject="";
		message = message.replaceAll("\\$\\{event.name}", eventName);

		message = message.replaceAll("\\$\\{study.name}",currentStudy.getName());
		message = message.replaceAll("\\$\\{study.id}", currentStudy.getUniqueIdentifier());

		message = message.replaceAll("\\$\\{site.name}", siteBean!=null ?siteBean.getName():"");
		message = message.replaceAll("\\$\\{site.id}", siteBean !=null?siteBean.getUniqueIdentifier():"");

		emailSubject = emailSubject.replaceAll("\\$\\{event.name}", eventName);

		emailSubject = emailSubject.replaceAll("\\$\\{study.name}", currentStudy.getName());
		emailSubject = emailSubject.replaceAll("\\$\\{study.id}", currentStudy.getUniqueIdentifier());

		emailSubject = emailSubject.replaceAll("\\$\\{site.name}", siteBean!=null?siteBean.getName():"");
		emailSubject = emailSubject.replaceAll("\\$\\{site.id}", siteBean!=null?siteBean.getUniqueIdentifier():"");

		ParticipantDTO pDTO = null;
		String[] listOfEmails = emailList.split(",");
		OCUserDTO userDTO=null;

		String participateStatus = currentStudy.getParticipantPortal(); // enabled , disabled
		String accessToken = keycloakClientImpl.getSystemToken();

		if(studySubject.getUserId()!=null) {
			UserAccountBean userAccountBean = (UserAccountBean) udao.findByPK(studySubject.getUserId());
			userUuid = userAccountBean.getUserUuid();
		}


		Thread thread = new Thread(new NotificationActionProcessor(listOfEmails, studySubject, currentStudy, message, emailSubject, mailSender,participateStatus,accessToken,notificationService, userUuid));
		thread.start();

	}

	@Override
	public void run() {

		String url = "";

		pDTO = getParticipantInfo(studySubject);


		if (pDTO != null) {
			String msg = null;
			String eSubject = null;

			String pDTOaccessCode=(pDTO.getAccessCode()!=null)?pDTO.getAccessCode():"";
			String pDTOurl=(pDTO.getUrl()!=null)?pDTO.getUrl():"";
			String pDTOloginUrl=(pDTO.getLoginUrl()!=null)?pDTO.getLoginUrl():"";
			String pDTOfName=(pDTO.getfName()!=null)?pDTO.getfName():"";
			String pDTOId=(pDTO.getParticipantId()!=null)?pDTO.getParticipantId():"";

			msg = message.replaceAll("\\$\\{participant.accessCode}", pDTOaccessCode);
			msg = msg.replaceAll("\\$\\{participant.firstname}", pDTOfName);
			msg = msg.replaceAll("\\$\\{participant.url}", pDTOurl);
			msg = msg.replaceAll("\\$\\{participant.loginurl}", pDTOloginUrl);
			msg = msg.replaceAll("\\$\\{participant.id}", pDTOId);

			eSubject = emailSubject.replaceAll("\\$\\{participant.accessCode}", pDTOaccessCode);
			eSubject = eSubject.replaceAll("\\$\\{participant.firstname}", pDTOfName);
			eSubject = eSubject.replaceAll("\\$\\{participant.url}",pDTOurl);
			eSubject = eSubject.replaceAll("\\$\\{participant.loginurl}", pDTOloginUrl);
			eSubject = eSubject.replaceAll("\\$\\{participant.id}", pDTOId);

			msg = msg.replaceAll("\\\\n", "\n");
			eSubject = eSubject.replaceAll("\\\\n", "\n");
			message = message.replaceAll("\\\\n", "\n");
			emailSubject = emailSubject.replaceAll("\\\\n", "\n");
			pDTO.setMessage(msg);
			pDTO.setEmailSubject(eSubject);
			pDTO.setUrl(url);
			pDTO.setOrigMessage(message);
			pDTO.setOrigEmailSubject(emailSubject);


		} else {
			pDTO = buildNewPDTO();
			message = message.replaceAll("\\\\n", "\n");
			emailSubject = emailSubject.replaceAll("\\\\n", "\n");
			pDTO.setOrigMessage(message);
			pDTO.setOrigEmailSubject(emailSubject);
		}
		String smsPhone = null;

		for (String email : listOfEmails) {
			smsPhone = null;
			if (email.trim().equals("${participant}")) {
				email = pDTO.getParticipantEmailAccount();
				smsPhone = pDTO.getPhone();
			}

			// Send Email thru Local Mail Server
			if(email!=null) {
				execute(ExecutionMode.SAVE, ruleActionBean, pDTO, email.trim());
				logger.info(pDTO.getMessage() + "  (Email sent to email address from OC Mail Server :  " + email + ")");
			}else{
				logger.info(pDTO.getMessage() + "  (No Email address available to be forwarded)");
			}
			if (StringUtils.isNotEmpty(smsPhone)) {
				sendMessage(pDTO.getMessage());
			}
		}
	}

	public void sendMessage(String message) {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		headers.add("Authorization", "Bearer " + accessToken);
		headers.add("Accept-Charset", "UTF-8");
		OCMessageDTO messageDTO = new OCMessageDTO();
		messageDTO.setReceiverPhone(StringUtils.remove(pDTO.getPhone(), " "));
		messageDTO.setMessage(message);
		messageDTO.setSubdomain(subDomain);
		HttpEntity<OCMessageDTO> request = new HttpEntity<>(messageDTO, headers);

		ResponseEntity<String> result = restTemplate.postForEntity(messageServiceUri, request, String.class);
		if (result.getStatusCode() != HttpStatus.OK) {
			logger.error("sendMessage failed with :" + result.getStatusCode());
		}

	}

	public ParticipantDTO buildNewPDTO() {
		pDTO = new ParticipantDTO();
		String msg = null;
		msg = message.replaceAll("\\$\\{participant.accessCode}", "");
		msg = msg.replaceAll("\\$\\{participant.firstname}", "");
		msg = msg.replaceAll("\\$\\{participant.loginurl}", "");
		msg = msg.replaceAll("\\$\\{participant.url}", "");
		msg = msg.replaceAll("\\$\\{participant.id}", "");

		msg = msg.replaceAll("\\\\n", "\n");
		pDTO.setMessage(msg);
		String eSubject = null;
		eSubject = emailSubject.replaceAll("\\$\\{participant.accessCode}", "");
		eSubject = eSubject.replaceAll("\\$\\{participant.firstname}", "");
		eSubject = eSubject.replaceAll("\\$\\{participant.loginurl}", "");
		eSubject = eSubject.replaceAll("\\$\\{participant.url}", "");
		eSubject = eSubject.replaceAll("\\$\\{participant.id}", "");
		eSubject = eSubject.replaceAll("\\\\n", "\n");
		pDTO.setEmailSubject(eSubject);

		return pDTO;
	}

	public ParticipantDTO getParticipantInfo(StudySubject studySubject) {
		ParticipantDTO pDTO = null;
		if (studySubject != null) {
			pDTO = new ParticipantDTO();
			pDTO.setParticipantId(studySubject.getLabel());

			if(studySubject.getStudySubjectDetail()!=null) {
				pDTO.setfName(studySubject.getStudySubjectDetail().getFirstName());
				pDTO.setParticipantEmailAccount(studySubject.getStudySubjectDetail().getEmail());
				pDTO.setPhone(studySubject.getStudySubjectDetail().getPhone());
				pDTO.setIdentifier(studySubject.getStudySubjectDetail().getIdentifier());
				ParticipantAccessDTO participantAccessDTO =notificationService.getAccessInfo(accessToken,studyBean,studySubject,userUuid) ;

				if (participantAccessDTO != null) {
					pDTO.setAccessCode(participantAccessDTO.getAccessCode());
					pDTO.setLoginUrl(participantAccessDTO.getAccessLink());
					pDTO.setUrl(participantAccessDTO.getHost());
				}
			}
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

	private Study getParentStudy( Study study) {
		if (!study.isSite()) {
			return study;
		} else {
			Study parentStudy = study.getStudy();
			return parentStudy;
		}

	}

	public StudyEventDefinitionBean getStudyEventDefnBean(int sed_Id) {
		StudyEventDefinitionDAO sedao = new StudyEventDefinitionDAO(ds);
		return (StudyEventDefinitionBean) sedao.findByPK(sed_Id);
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
