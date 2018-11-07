package org.akaza.openclinica.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.akaza.openclinica.bean.login.ParticipantDTO;
import org.akaza.openclinica.bean.managestudy.*;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.EventCrfDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.domain.rule.action.RuleActionBean;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


import static java.util.Collections.*;

/**
 * This Service class is used with View Study Subject Page
 *
 * @author joekeremian
 */

public class UserServiceImpl implements UserService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    @Qualifier( "dataSource" )
    private BasicDataSource dataSource;

    @Autowired
    ServletContext context;

    @Autowired
    EventCrfDao eventCrfDao;

    @Autowired
    StudyEventDao studyEventDao;

    @Autowired
    StudySubjectDao studySubjectDao;

    @Autowired
    StudyDao studyDao;

    @Autowired
    JavaMailSenderImpl mailSender;

    private RestfulServiceHelper restfulServiceHelper;

    public static final String FORM_CONTEXT = "ecid";
    public static final String DASH = "-";
    public static final String PARTICIPATE_EDIT = "participate-edit";
    public static final String PARTICIPATE_ADD_NEW = "participate-add-new";
    public static final String PAGINATION = "?page=0&size=1000";
    private String sbsUrl = CoreResources.getField("SBSUrl");

    StudyDAO sdao;

    public UserServiceImpl(BasicDataSource dataSource) {
        this.dataSource = dataSource;
    }


    public StudySubject getStudySubject(String ssid, Study study) {
        return studySubjectDao.findByLabelAndStudyOrParentStudy(ssid, study);
    }

    public Study getStudy(String studyOid) {
        return studyDao.findByOcOID(studyOid);
    }

    public Object connectParticipant(String studyOid, String ssid, OCParticipantDTO participantDTO, HttpServletRequest request) {
        Study study = getStudy(studyOid);
        StudySubject studySubject = getStudySubject(ssid, study);
        OCUserDTO ocUserDTO = null;
        Object object = null;

        if (studySubject != null) {
            ocUserDTO = buildOCUserDTO(ssid, participantDTO,studySubject,studyOid);
            if (studySubject.getUserUuid() == null) {
                // create participant user Account   POST
                object = createOrUpdateParticipantAccount(request, ocUserDTO, HttpMethod.POST);
                if (object instanceof OCUserDTO && object != null) {
                    studySubject.setUserUuid(((OCUserDTO) object).getUuid());
                    studySubjectDao.saveOrUpdate(studySubject);
                    logger.info("Participate user_uuid added in db: "+studySubject.getUserUuid());
                }
            } else {
                // update participant user Account  PUT
                ocUserDTO.setUuid(studySubject.getUserUuid());
                // Get participant
                object = getParticipantAccountFromUserService(request, ocUserDTO, HttpMethod.GET);
                if (object instanceof OCUserDTO) {
                    ocUserDTO.setStatus(((OCUserDTO) object).getStatus());
                }
                object = createOrUpdateParticipantAccount(request, ocUserDTO, HttpMethod.PUT);
                logger.info("Participate info with user_uuid is updated in db : "+studySubject.getUserUuid());
            }

        } else {
            logger.info("Participant does not exists or not added yet in OC ");
        }
        return object;
    }


    public Object createOrUpdateParticipantAccount(HttpServletRequest request, OCUserDTO ocUserDTO, HttpMethod
            httpMethod) {
        String uri = sbsUrl;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String accessToken = (String) request.getSession().getAttribute("accessToken");
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Accept-Charset", "UTF-8");
        StudyBean studyBean = null;
        HttpEntity<OCUserDTO> entity = new HttpEntity<OCUserDTO>(ocUserDTO, headers);
        ResponseEntity<OCUserDTO> userResponse = null;
        try {
            userResponse = restTemplate.exchange(uri, httpMethod, entity, OCUserDTO.class);
        } catch (HttpClientErrorException e) {
            logger.error("Auth0 error message: {}", e.getResponseBodyAsString());
            return e;
        }


        if (userResponse == null) {
            return null;
        } else {
            if(ocUserDTO.isInviteParticipant()){
                sendEmailToParticipant(ocUserDTO);
            }
            return userResponse.getBody();
        }

    }


    private OCUserDTO buildOCUserDTO(String ssid, OCParticipantDTO participantDTO ,StudySubject studySubject,String studyOid) {
        OCUserDTO ocUserDTO = new OCUserDTO();
        if(participantDTO!=null) {
            ocUserDTO.setEmail(participantDTO.getEmail());
            ocUserDTO.setFirstName(participantDTO.getFirstName());
            ocUserDTO.setPhoneNumber(participantDTO.getMobilePhone());
            ocUserDTO.setInviteParticipant(participantDTO.isInviteParticipant());
        }
        ocUserDTO.setUserType(UserType.USER);
        String username =studyOid+"."+studySubject.getOcOid();
        username=username.replaceAll("\\(",".").replaceAll("\\)","");
        ocUserDTO.setUsername(username);
        ocUserDTO.setLastName("ParticipateAccount");
        ocUserDTO.setStatus(UserStatus.INVITED);

        return ocUserDTO;
    }

    public Object getParticipantAccountFromUserService(HttpServletRequest request, OCUserDTO ocUserDTO, HttpMethod
            httpMethod) {
        String uri =sbsUrl+ ocUserDTO.getUuid();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String accessToken = (String) request.getSession().getAttribute("accessToken");
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Accept-Charset", "UTF-8");
        StudyBean studyBean = null;
        HttpEntity entity = new HttpEntity<OCUserDTO>(headers);
        ResponseEntity<OCUserDTO> userResponse = null;
        try {
            userResponse = restTemplate.exchange(uri, httpMethod, entity, OCUserDTO.class);
        } catch (HttpClientErrorException e) {
            logger.error("Auth0 error message: {}", e.getResponseBodyAsString());
            return e;
        }

        if (userResponse == null) {
            return null;
        } else {
            logger.info("Participate user_uuid from User Service : "+userResponse.getBody().getUuid());
            return ocUserDTO = userResponse.getBody();
        }

    }

    public Object getParticipantAccount(String studyOid, String ssid, OCParticipantDTO participantDTO, HttpServletRequest request) {
        Study study = getStudy(studyOid);
        StudySubject studySubject = getStudySubject(ssid, study);
        OCUserDTO ocUserDTO = null;
        Object object = null;

        if (studySubject != null) {
            ocUserDTO = buildOCUserDTO(ssid, participantDTO,studySubject,studyOid);
            if(studySubject.getUserUuid()!=null) {
                ocUserDTO.setUuid(studySubject.getUserUuid());
                object = getParticipantAccountFromUserService(request, ocUserDTO, HttpMethod.GET);
            }else{
                logger.info("Participant has not been connected yet");
                logger.info("userUuid of participant in OC runtime is null");

            }
        } else {
            logger.info("Participant does not exists or not added yet in OC ");
        }
        return object;
    }


    public List<OCUserDTO> getAllParticipantAccountsFromUserService(HttpServletRequest request) {
        String uri = sbsUrl.substring(0, sbsUrl.length() - 1) + PAGINATION;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String accessToken = (String) request.getSession().getAttribute("accessToken");
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Accept-Charset", "UTF-8");
        StudyBean studyBean = null;
        HttpEntity entity = new HttpEntity<OCUserDTO>(headers);
        ResponseEntity<List<OCUserDTO>> userResponse = null;
        try {
            userResponse =
                    restTemplate.exchange(uri, HttpMethod.GET, entity, new ParameterizedTypeReference<List<OCUserDTO>>() {
                    });

        } catch (HttpClientErrorException e) {
            logger.error("Auth0 error message: {}", e.getResponseBodyAsString());
            return null;
        }

        if (userResponse == null) {
            return null;
        } else {
            logger.info("Total participate/user numbers: "+userResponse.getBody().size());
            return userResponse.getBody();
        }

    }

    private void sendEmailToParticipant(OCUserDTO ocUserDTO) {
        ParticipantDTO pDTO = new ParticipantDTO();
        pDTO.setEmailAccount(ocUserDTO.getEmail());
        pDTO.setEmailSubject("This is the email Subject");
        pDTO.setMessage("This is the Email content message");
        sendEmailToParticipant(pDTO) ;

        }
    private void sendEmailToParticipant( ParticipantDTO pDTO) throws OpenClinicaSystemException {

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

}