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
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.logic.rulerunner.ExecutionMode;
import org.akaza.openclinica.logic.rulerunner.RuleSetBulkRuleRunner;
import org.akaza.openclinica.logic.rulerunner.RuleRunner.RuleRunnerMode;
import org.akaza.openclinica.patterns.ocobserver.OnStudyEventUpdated;
import org.akaza.openclinica.patterns.ocobserver.StudyEventChangeDetails;
import org.akaza.openclinica.service.BulkEmailSenderService;
import org.akaza.openclinica.service.OCUserDTO;
import org.akaza.openclinica.service.crfdata.xform.EnketoAccountRequest;
import org.akaza.openclinica.service.crfdata.xform.EnketoAccountResponse;
import org.akaza.openclinica.service.participant.ParticipantServiceImpl;
import org.akaza.openclinica.service.pmanage.Authorization;
import org.akaza.openclinica.service.rule.RuleSetService;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;

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
    private OCUserDTO ocUserDTO;


    public NotificationActionProcessor(DataSource ds, JavaMailSenderImpl mailSender, RuleActionBean ruleActionBean, ParticipantDTO pDTO,
			String email) {
		this.ds = ds;
		this.mailSender = mailSender;
		this.ruleActionBean = ruleActionBean;
		this.pDTO = pDTO;
		this.email = email;

	}

	public NotificationActionProcessor(String[] listOfEmails, OCUserDTO ocUserDTO, StudyBean studyBean, String message, String emailSubject,
			JavaMailSenderImpl mailSender , String participateStatus) {
		this.listOfEmails = listOfEmails;
		this.message = message;
		this.emailSubject = emailSubject;
		this.ocUserDTO = ocUserDTO;
		this.mailSender = mailSender;
		this.studyBean = studyBean;
		this.participateStatus=participateStatus;

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
		if (message==null) message="";
        if (emailSubject==null) emailSubject="";
		message = message.replaceAll("\\$\\{event.name}", eventName);
		message = message.replaceAll("\\$\\{study.name}", studyName);
		emailSubject = emailSubject.replaceAll("\\$\\{event.name}", eventName);
		emailSubject = emailSubject.replaceAll("\\$\\{study.name}", studyName);

		ParticipantDTO pDTO = null;
		StudyBean studyBean = getStudyBean(studyId);
		String[] listOfEmails = emailList.split(",");
		StudySubjectBean ssBean = (StudySubjectBean) ssdao.findByPK(studySubjectBeanId);
		StudyBean parentStudyBean = getParentStudy(ds, studyBean);
        OCUserDTO userDTO=null;

        if(!StringUtils.isEmpty(ssBean.getUserUuid())) {
            userDTO=getAllparticipantsFromUserService(ssBean,studyBean);
        }
		StudyParameterValueBean pStatus = spvdao.findByHandleAndStudy(parentStudyBean.getId(), "participantPortal");
		String participateStatus = pStatus.getValue().toString(); // enabled , disabled

		Thread thread = new Thread(new NotificationActionProcessor(listOfEmails, userDTO, studyBean, message, emailSubject, mailSender,participateStatus));
		thread.start();

	}

	@Override
	public void run() {

		String url = "";


		message = message.replaceAll("\\$\\{participant.url}", url);
		emailSubject = emailSubject.replaceAll("\\$\\{participant.url}", url);

		pDTO = getParticipantInfo(ocUserDTO);


		if (pDTO != null) {
			String msg = null;
			String eSubject = null;
			msg = message.replaceAll("\\$\\{participant.accessCode}", pDTO.getAccessCode());
			msg = msg.replaceAll("\\$\\{participant.firstname}", pDTO.getfName());
			eSubject = emailSubject.replaceAll("\\$\\{participant.accessCode}", pDTO.getAccessCode());
			eSubject = eSubject.replaceAll("\\$\\{participant.firstname}", pDTO.getfName());

			String loginUrl = url + "?access_code=" + pDTO.getAccessCode() + "&auto_login=true";
			msg = msg.replaceAll("\\$\\{participant.loginurl}", loginUrl);
			eSubject = eSubject.replaceAll("\\$\\{participant.loginurl}", loginUrl);

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
				execute(ExecutionMode.SAVE, ruleActionBean, pDTO , email.trim());
				logger.info(pDTO.getMessage() + "  (Email sent to email address from OC Mail Server :  " + email + ")");

		}
	}

	public ParticipantDTO buildNewPDTO() {
		pDTO = new ParticipantDTO();
		String msg = null;
		msg = message.replaceAll("\\$\\{participant.accessCode}", "");
		msg = msg.replaceAll("\\$\\{participant.firstname}", "");
		msg = msg.replaceAll("\\$\\{participant.loginurl}", "");
		msg = msg.replaceAll("\\\\n", "\n");
		pDTO.setMessage(msg);
		String eSubject = null;
		eSubject = emailSubject.replaceAll("\\$\\{participant.accessCode}", "");
		eSubject = eSubject.replaceAll("\\$\\{participant.firstname}", "");
		eSubject = eSubject.replaceAll("\\$\\{participant.loginurl}", "");
		eSubject = eSubject.replaceAll("\\\\n", "\n");
		pDTO.setEmailSubject(eSubject);

		return pDTO;
	}

	public ParticipantDTO getParticipantInfo(OCUserDTO ocUserDTO) {
		ParticipantDTO pDTO = null;
		if (ocUserDTO != null ) {
			pDTO = new ParticipantDTO();
			pDTO.setfName(ocUserDTO.getFirstName());
            pDTO.setParticipantEmailAccount(ocUserDTO.getEmail());
            pDTO.setAccessCode("");

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

	private OCUserDTO getAllparticipantsFromUserService(StudySubjectBean ssBean,StudyBean studyBean){
        String baseUrl = CoreResources.getField("sysURL.base");
        String uri = baseUrl + "pages/auth/api/clinicaldata/studies/" + studyBean.getOid() + "/participantUsers";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        HttpServletRequest request = CoreResources.getRequest();
        String accessToken = (String) request.getSession().getAttribute("accessToken");
        headers.add("Authorization", "Bearer " + accessToken);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        headers.add("Accept-Charset", "UTF-8");
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(objectMapper);
        converters.add(jsonConverter);
        restTemplate.setMessageConverters(converters);
        ResponseEntity<List<OCUserDTO>> response = restTemplate.exchange(uri, HttpMethod.GET, entity, new ParameterizedTypeReference<List<OCUserDTO>>() {
        });

        List<OCUserDTO> oCUserDTOs = null;
        if (response != null) {
            oCUserDTOs = (List<OCUserDTO>) response.getBody();
        }

        for (OCUserDTO userDTO : oCUserDTOs) {
            if (ssBean.getUserUuid().equals(userDTO.getUuid())) {
                return userDTO;
            }
        }
        return null;
    }

}
