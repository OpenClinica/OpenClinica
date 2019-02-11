package org.akaza.openclinica.domain.rule.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import liquibase.util.StringUtils;
import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.login.ParticipantDTO;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.core.form.StringUtil;
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
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.logic.rulerunner.ExecutionMode;
import org.akaza.openclinica.logic.rulerunner.RuleSetBulkRuleRunner;
import org.akaza.openclinica.logic.rulerunner.RuleRunner.RuleRunnerMode;
import org.akaza.openclinica.patterns.ocobserver.OnStudyEventUpdated;
import org.akaza.openclinica.patterns.ocobserver.StudyEventChangeDetails;
import org.akaza.openclinica.service.*;
import org.akaza.openclinica.service.crfdata.xform.EnketoAccountRequest;
import org.akaza.openclinica.service.crfdata.xform.EnketoAccountResponse;
import org.akaza.openclinica.service.participant.ParticipantServiceImpl;
import org.akaza.openclinica.service.pmanage.Authorization;
import org.akaza.openclinica.service.rule.RuleSetService;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.util.JsonSerialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.security.config.authentication.UserServiceBeanDefinitionParser;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

public class NotificationActionProcessor implements ActionProcessor, Runnable {

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	DataSource ds;
	EmailEngine emailEngine;
	JavaMailSenderImpl mailSender;
	RuleSetRuleBean ruleSetRule;
	StudySubjectDAO ssdao;
	UserAccountDAO udao;
	StudyParameterValueDAO spvdao;
	RuleSetService ruleSetService;
	RuleSetDao ruleSetDao;
	ParticipantDTO pDTO;
	RuleActionBean ruleActionBean;
	String email;
	String[] listOfEmails;
	StudyBean studyBean;
	String message;
	String url;
	String emailSubject;
	String participateStatus;
    StudySubject studySubject;
    String accessToken;
    PermissionService permissionService;

    public NotificationActionProcessor(DataSource ds, JavaMailSenderImpl mailSender, RuleActionBean ruleActionBean, ParticipantDTO pDTO,
			String email) {
		this.ds = ds;
		this.mailSender = mailSender;
		this.ruleActionBean = ruleActionBean;
		this.pDTO = pDTO;
		this.email = email;

	}

	public NotificationActionProcessor(String[] listOfEmails, StudySubject studySubject, StudyBean studyBean, String message, String emailSubject,
			JavaMailSenderImpl mailSender , String participateStatus,String accessToken) {
		this.listOfEmails = listOfEmails;
		this.message = message;
		this.emailSubject = emailSubject;
		this.studySubject = studySubject;
		this.mailSender = mailSender;
		this.studyBean = studyBean;
		this.participateStatus=participateStatus;
		this.accessToken=accessToken;

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
	public RuleActionBean execute(RuleRunnerMode ruleRunnerMode, ExecutionMode executionMode, RuleActionBean ruleAction, ItemDataBean itemDataBean, String itemData, StudyBean currentStudy,
			UserAccountBean ub, Object... arguments) {
		// TODO Auto-generated method stub
		return null;
	}

	public void runNotificationAction(RuleActionBean ruleActionBean, RuleSetBean ruleSet, StudySubject studySubject, int eventOrdinal) {
		String emailList = ((NotificationActionBean) ruleActionBean).getTo();
		String message = ((NotificationActionBean) ruleActionBean).getMessage();
		String emailSubject = ((NotificationActionBean) ruleActionBean).getSubject();

		int sed_Id = ruleSet.getStudyEventDefinitionId();
		int studyId = ruleSet.getStudyId();

		String eventName = getStudyEventDefnBean(sed_Id).getName();
		if (eventOrdinal != 1)
			eventName = eventName + "(" + eventOrdinal + ")";

		String studyName = getStudyBean(studyId).getName();
		if (message==null) message="";
        if (emailSubject==null) emailSubject="";
		message = message.replaceAll("\\$\\{event.name}", eventName);
		message = message.replaceAll("\\$\\{study.name}", studyName);
		emailSubject = emailSubject.replaceAll("\\$\\{event.name}", eventName);
		emailSubject = emailSubject.replaceAll("\\$\\{study.name}", studyName);

		ParticipantDTO pDTO = null;
		StudyBean studyBean = getStudyBean(studyId);
		String[] listOfEmails = emailList.split(",");
		StudyBean parentStudyBean = getParentStudy(ds, studyBean);
        OCUserDTO userDTO=null;

		StudyParameterValueBean pStatus = spvdao.findByHandleAndStudy(parentStudyBean.getId(), "participantPortal");
		String participateStatus = pStatus.getValue().toString(); // enabled , disabled
		String accessToken=getAccessToken();

		Thread thread = new Thread(new NotificationActionProcessor(listOfEmails, studySubject, studyBean, message, emailSubject, mailSender,participateStatus,accessToken));
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

            msg = message.replaceAll("\\$\\{participant.accessCode}", pDTOaccessCode);
			msg = msg.replaceAll("\\$\\{participant.firstname}", pDTOfName);
			msg = msg.replaceAll("\\$\\{participant.url}", pDTOurl);
			msg = msg.replaceAll("\\$\\{participant.loginurl}", pDTOloginUrl);

			eSubject = emailSubject.replaceAll("\\$\\{participant.accessCode}", pDTOaccessCode);
			eSubject = eSubject.replaceAll("\\$\\{participant.firstname}", pDTOfName);
			eSubject = eSubject.replaceAll("\\$\\{participant.url}",pDTOurl);
			eSubject = eSubject.replaceAll("\\$\\{participant.url}",pDTOurl);
			eSubject = eSubject.replaceAll("\\$\\{participant.loginurl}", pDTOloginUrl);

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

		for (String email : listOfEmails) {
			    if (email.trim().equals("${participant}")){
				email = pDTO.getParticipantEmailAccount();
			    }

				// Send Email thru Local Mail Server
			if(email!=null) {
				execute(ExecutionMode.SAVE, ruleActionBean, pDTO, email.trim());
				logger.info(pDTO.getMessage() + "  (Email sent to email address from OC Mail Server :  " + email + ")");
			}else{
				logger.info(pDTO.getMessage() + "  (No Email address available to be forwarded)");
			}
		}
	}

	public ParticipantDTO buildNewPDTO() {
		pDTO = new ParticipantDTO();
		String msg = null;
		msg = message.replaceAll("\\$\\{participant.accessCode}", "");
		msg = msg.replaceAll("\\$\\{participant.firstname}", "");
		msg = msg.replaceAll("\\$\\{participant.loginurl}", "");
		msg = msg.replaceAll("\\$\\{participant.url}", "");
		msg = msg.replaceAll("\\\\n", "\n");
		pDTO.setMessage(msg);
		String eSubject = null;
		eSubject = emailSubject.replaceAll("\\$\\{participant.accessCode}", "");
		eSubject = eSubject.replaceAll("\\$\\{participant.firstname}", "");
		eSubject = eSubject.replaceAll("\\$\\{participant.loginurl}", "");
		eSubject = eSubject.replaceAll("\\$\\{participant.url}", "");
		eSubject = eSubject.replaceAll("\\\\n", "\n");
		pDTO.setEmailSubject(eSubject);

		return pDTO;
	}

	public ParticipantDTO getParticipantInfo(StudySubject studySubject) {
		ParticipantDTO pDTO = null;
		if (studySubject != null && studySubject.getUserId()!=null ) {
			pDTO = new ParticipantDTO();

			pDTO.setfName(studySubject.getStudySubjectDetail().getFirstName());
            pDTO.setParticipantEmailAccount(studySubject.getStudySubjectDetail().getEmail());
            pDTO.setPhone(studySubject.getStudySubjectDetail().getPhone());

            if(studySubject.getUserId()!=null) {
                ParticipantAccessDTO participantAccessDTO = getAccessLink(studySubject.getLabel());
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



	private ParticipantAccessDTO getAccessLink(String ssid){
		String baseUrl = CoreResources.getField("sysURL.base");
		String uri = baseUrl + "pages/auth/api/clinicaldata/studies/" + studyBean.getOid() + "/participants/"+ssid+"/accessLink";

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();

		headers.add("Authorization", "Bearer " + accessToken);
		headers.add("Accept-Charset", "UTF-8");
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		HttpEntity<String> entity = new HttpEntity<String>(headers);

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		List<HttpMessageConverter<?>> converters = new ArrayList<>();
		MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
		jsonConverter.setObjectMapper(objectMapper);
		converters.add(jsonConverter);
		restTemplate.setMessageConverters(converters);

		ResponseEntity<ParticipantAccessDTO> response=null;

		try {
			response = restTemplate.exchange(uri, HttpMethod.GET, entity, ParticipantAccessDTO.class);
		} catch (HttpClientErrorException e) {
			logger.error("Runtime error message: {}", e.getResponseBodyAsString());
		}



		ParticipantAccessDTO participantAccessDTO = null;
		if (response != null) {
			participantAccessDTO = response.getBody();
		}


		return participantAccessDTO;
	}

	public String getAccessToken() {
		logger.debug("Creating Auth0 Api Token");

		try {
			InputStream inputStream = new ClassPathResource("keycloak.json", this.getClass().getClassLoader()).getInputStream();
			AuthzClient authzClient = AuthzClient.create(JsonSerialization.readValue(inputStream, Configuration.class));
			AccessTokenResponse accessTokenResponse = authzClient.obtainAccessToken();
			if (accessTokenResponse != null)
				return accessTokenResponse.getToken();
		} catch (IOException e) {
			logger.error("Could not read keycloak.json", e);
			return null;
		}
		return null;
	}


}
