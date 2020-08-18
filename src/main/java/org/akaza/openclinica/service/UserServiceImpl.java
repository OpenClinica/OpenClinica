package org.akaza.openclinica.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import core.org.akaza.openclinica.service.*;
import org.akaza.openclinica.ParticipateInviteEnum;
import org.akaza.openclinica.ParticipateInviteStatusEnum;
import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.login.ParticipantDTO;
import core.org.akaza.openclinica.bean.login.StudyParticipantDetailDTO;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.dto.AuditLogEventDTO;
import org.akaza.openclinica.controller.dto.ModuleConfigAttributeDTO;
import org.akaza.openclinica.controller.dto.ModuleConfigDTO;
import core.org.akaza.openclinica.core.EmailEngine;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.domain.Status;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.domain.enumsupport.JobStatus;
import core.org.akaza.openclinica.domain.enumsupport.JobType;
import core.org.akaza.openclinica.domain.user.UserAccount;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import core.org.akaza.openclinica.service.randomize.ModuleProcessor;
import core.org.akaza.openclinica.web.rest.client.auth.impl.KeycloakClientImpl;
import org.akaza.openclinica.web.restful.errors.ErrorConstants;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

import static core.org.akaza.openclinica.domain.rule.action.NotificationActionProcessor.messageServiceUri;
import static core.org.akaza.openclinica.domain.rule.action.NotificationActionProcessor.sbsUrl;

/**
 * This Service class is used with View Study Subject Page
 * @author joekeremian
 */

@Service("userService")
public class UserServiceImpl implements UserService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    @Qualifier("dataSource")
    private BasicDataSource dataSource;

    @Autowired
    ServletContext context;

    @Autowired
    UserAccountDao userAccountDao;

    @Autowired
    StudyUserRoleDao studyUserRoleDao;

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

    @Autowired
    KeycloakClientImpl keycloakClient;

    @Autowired
    StudyBuildService studyBuildService;

    @Autowired
    ValidateService validateService;

    @Autowired
    UtilService utilService;

    @Autowired
    AuditLogEventService auditLogEventService;

    @Autowired
    CryptoConverter cryptoConverter;

    @Autowired
    JobService jobService;

    public static final String FORM_CONTEXT = "ecid";
    public static final String DASH = "-";
    public static final String PARTICIPATE_EDIT = "participate-edit";
    public static final String PARTICIPATE_ADD_NEW = "participate-add-new";
    public static final String PAGINATION = "?page=0&size=1000";
    public static final String PASSWORD_LENGTH = "9";
    public static final String ACCESS_LINK = "accessLink";
    public static final String ACCESS_LINK_PART_URL = "/?accessCode=";
    public static final String ENABLED = "enabled";
    public static final String SEPERATOR = ",";
    public static final String PARTICIPANT_ACCESS_CODE = "_Participant Access Code";
    SimpleDateFormat sdf_fileName = new SimpleDateFormat("yyyy-MM-dd'-'HHmmssSSS'Z'");

    private String urlBase = CoreResources.getField("sysURL").split("/MainMenu")[0];

    private StudySubjectDAO ssDao;

    public StudySubject getStudySubject(String ssid, Study study) {
        return studySubjectDao.findByLabelAndStudyOrParentStudy(ssid, study);
    }

    public Study getStudy(String studyOid) {
        return studyDao.findByOcOID(studyOid);
    }


    public OCUserDTO connectParticipant(String studyOid, String ssid, OCParticipantDTO participantDTO, String accessToken,
                                        UserAccountBean userAccountBean, String realm, ResourceBundle restext) {
        OCUserDTO ocUserDTO = null;
        Study tenantStudy = getStudy(studyOid);
        String oid = (tenantStudy.getStudy() != null ? tenantStudy.getStudy().getOc_oid() : tenantStudy.getOc_oid());

        StudySubject studySubject = getStudySubject(ssid, tenantStudy);
        String username = oid + "." + studySubject.getOcOid();
        username = username.replaceAll("\\(", ".").replaceAll("\\)", "");

        String accessCode = "";

        Study publicStudy = studyDao.findPublicStudy(tenantStudy.getOc_oid());

        String studyEnvironment = (publicStudy.getStudy() != null) ? publicStudy.getStudy().getStudyEnvUuid() : publicStudy.getStudyEnvUuid();
        UserAccount userAccount = userAccountDao.findById(userAccountBean.getId());
        UserAccount pUserAccount = null;

        if (studySubject != null) {
            UserStatus userStatus = null;
            Integer userId = null;
            if (validateService.isParticipateActive(tenantStudy)) {
                //This happens during the first invite
                if (studySubject.getUserId() == null) {
                    logger.info("Participate has not registered yet");
                    accessCode = generateAccessCode(accessToken, realm);
                    // show reset access code in participant audit log
                    AuditLogEventDTO auditLogEventDTO = populateAuditLogEventDTO(studySubject.getStudySubjectId(), 58);
                    auditLogEventService.saveAuditLogEvent(auditLogEventDTO, userAccountBean);

                    // create participant user Account In Keycloak
                    String keycloakUserId = keycloakClient.createParticipateUser(accessToken, null, username, accessCode, studyEnvironment, realm);
                    // create participant user Account In Runtime
                    pUserAccount = createUserAccount(participantDTO, studySubject, userAccountBean, username, publicStudy, keycloakUserId);
                    // create study subject detail Account
                    userStatus = UserStatus.CREATED;
                    userId = pUserAccount.getUserId();
                } else if (participantDTO.isResetAccessCode()) {
                    accessCode = generateAccessCode(accessToken, realm);
                    keycloakClient.resetParticipateUserAccessCode(accessToken, null, username, accessCode, studyEnvironment, realm);
                    // show reset access code in participant audit log
                    AuditLogEventDTO auditLogEventDTO = populateAuditLogEventDTO(studySubject.getStudySubjectId(), 58);
                    auditLogEventService.saveAuditLogEvent(auditLogEventDTO, userAccountBean);
                }
            }
            studySubject = saveOrUpdateStudySubject(studySubject, participantDTO, userStatus, userId, tenantStudy, userAccount);
            logger.info("Participate with user_id: {}, its user_status: {} is created/updated in study_subject table", studySubject.getUserId(), studySubject.getUserStatus());
        } else {
            logger.info("Participant does not exists or not added yet in OC ");
        }
        ParticipateInviteEnum inviteEnum = ParticipateInviteEnum.NO_INVITE;
        ParticipateInviteStatusEnum inviteStatusEnum = ParticipateInviteStatusEnum.NO_OP;
        if (participantDTO.isInviteParticipant() || participantDTO.isInviteViaSms()) {

            ParticipantAccessDTO accessDTO = getAccessInfo(accessToken, studyOid, ssid, realm, userAccountBean, false);

            if (participantDTO.isInviteViaSms())
                inviteEnum = ParticipateInviteEnum.SMS_INVITE;
            if (participantDTO.isInviteParticipant())
                inviteEnum = ParticipateInviteEnum.EMAIL_INVITE;
            if (participantDTO.isInviteViaSms() && participantDTO.isInviteParticipant())
                inviteEnum = ParticipateInviteEnum.BOTH_INVITE;

            boolean smsToParticipant = false;
            if (participantDTO.isInviteViaSms()) {
                smsToParticipant = sendSMSToParticipant(accessToken, participantDTO, tenantStudy, accessDTO);
            }
            boolean emailToParticipant = false;
            if (participantDTO.isInviteParticipant()) {
                emailToParticipant = sendEmailToParticipant(studySubject, tenantStudy, accessDTO);
            }
            if (inviteEnum == ParticipateInviteEnum.BOTH_INVITE) {
                if (emailToParticipant)
                    inviteStatusEnum = ParticipateInviteStatusEnum.EMAIL_INVITE_SUCCESS;
                else
                    inviteStatusEnum = ParticipateInviteStatusEnum.EMAIL_INVITE_FAIL;

                if (smsToParticipant) {
                    if (inviteStatusEnum == ParticipateInviteStatusEnum.EMAIL_INVITE_SUCCESS)
                        inviteStatusEnum = ParticipateInviteStatusEnum.BOTH_INVITE_SUCCESS;
                    else if (inviteStatusEnum == ParticipateInviteStatusEnum.EMAIL_INVITE_FAIL)
                        inviteStatusEnum = ParticipateInviteStatusEnum.EMAIL_FAIL_SMS_SUCCESS;
                } else {
                    if (inviteStatusEnum == ParticipateInviteStatusEnum.EMAIL_INVITE_FAIL)
                        inviteStatusEnum = ParticipateInviteStatusEnum.BOTH_INVITE_FAIL;
                    else
                        inviteStatusEnum = ParticipateInviteStatusEnum.EMAIL_SUCCESS_SMS_FAIL;
                }
            } else if (inviteEnum == ParticipateInviteEnum.SMS_INVITE) {
                inviteStatusEnum = smsToParticipant ? ParticipateInviteStatusEnum.SMS_INVITE_SUCCESS : ParticipateInviteStatusEnum.SMS_INVITE_FAIL;
            } else if (inviteEnum == ParticipateInviteEnum.EMAIL_INVITE) {
                inviteStatusEnum = emailToParticipant ? ParticipateInviteStatusEnum.EMAIL_INVITE_SUCCESS : ParticipateInviteStatusEnum.EMAIL_INVITE_FAIL;
            }
            if ((inviteEnum != ParticipateInviteEnum.NO_INVITE) &&
                    ((inviteStatusEnum == ParticipateInviteStatusEnum.BOTH_INVITE_SUCCESS)
                            || (inviteStatusEnum == ParticipateInviteStatusEnum.EMAIL_INVITE_SUCCESS)
                            || (inviteStatusEnum == ParticipateInviteStatusEnum.SMS_INVITE_SUCCESS)
                            || (inviteStatusEnum == ParticipateInviteStatusEnum.EMAIL_SUCCESS_SMS_FAIL)
                            || (inviteStatusEnum == ParticipateInviteStatusEnum.EMAIL_FAIL_SMS_SUCCESS))) {
                // change status only if it was CREATED
                UserStatus newStatus = (studySubject.getUserStatus() == UserStatus.CREATED) ? UserStatus.INVITED : studySubject.getUserStatus();
                studySubject = saveOrUpdateStudySubject(studySubject, participantDTO, newStatus, null, tenantStudy, userAccount);
            }
        }

        ocUserDTO = buildOcUserDTO(studySubject, true);
        ocUserDTO.setErrorMessage(getErrorMessage(inviteEnum, inviteStatusEnum, restext));

        return ocUserDTO;
    }

    private String generateAccessCode(String accessToken, String realm) {
        String accessCode;
        do {
            // OC-11627 Update Access Codes to not include characters that can be confused for each other
            // Exclude : digits - 0, 1, 5; lowercase letters - l, o; uppercase letters - I, O, S
            String allowedChars = "2346789aAbBcCdDeEfFgGhHijJkKLmMnNpPqQrRstTuUvVwWxXyYzZ";
            char[] chars = allowedChars.toCharArray();
            accessCode = RandomStringUtils.random(Integer.parseInt(PASSWORD_LENGTH), 0, chars.length, true, true, chars);
        } while (keycloakClient.searchAccessCodeExists(accessToken, accessCode, realm));
        return accessCode;
    }

    private String getErrorMessage(ParticipateInviteEnum inviteEnum, ParticipateInviteStatusEnum inviteStatusEnum, ResourceBundle restext) {
        String message = null;
        if (inviteEnum == ParticipateInviteEnum.NO_INVITE)
            return message;
        switch (inviteStatusEnum) {
            case EMAIL_INVITE_SUCCESS:
                message = restext.getString("email_invite_success");
                break;
            case SMS_INVITE_SUCCESS:
                message = restext.getString("sms_invite_success");
                break;
            case EMAIL_INVITE_FAIL:
                message = restext.getString("email_invite_fail");
                break;
            case SMS_INVITE_FAIL:
                message = restext.getString("sms_invite_fail");
                break;
            case EMAIL_SUCCESS_SMS_FAIL:
                message = restext.getString("email_success_sms_fail");
                break;
            case EMAIL_FAIL_SMS_SUCCESS:
                message = restext.getString("email_fail_sms_success");
                break;
            case BOTH_INVITE_FAIL:
                message = restext.getString("both_invite_fail");
                break;
            case BOTH_INVITE_SUCCESS:
                message = restext.getString("both_invite_success");
                break;
            default:
                break;
        }
        return message;
    }

    private StudySubject saveOrUpdateStudySubject(StudySubject studySubject, OCParticipantDTO participantDTO,
                                                  UserStatus userStatus, Integer userId, Study tenantStudy, UserAccount userAccount) {

        studySubject.setUpdateId(userAccount.getUserId());
        studySubject.setDateUpdated(new Date());
        if (userId != null) {
            studySubject.setUserId(userId);
        }

        if (userStatus != null) {
            studySubject.setUserStatus(userStatus);
        }

        if (studySubject.getStudySubjectDetail() == null) {
            studySubjectDao.saveOrUpdate(studySubject);
            StudySubjectDetail studySubjectDetail = new StudySubjectDetail();
            studySubject.setStudySubjectDetail(studySubjectDetail);
        }
        if (participantDTO.getFirstName() != null)
            studySubject.getStudySubjectDetail().setFirstName(participantDTO.getFirstName() != null ? participantDTO.getFirstName() : "");

        if (validateService.isParticipateActive(tenantStudy)) {
            if (participantDTO.getEmail() != null)
                studySubject.getStudySubjectDetail().setEmail(participantDTO.getEmail() != null ? participantDTO.getEmail() : "");
            if (participantDTO.getPhoneNumber() != null)
                studySubject.getStudySubjectDetail().setPhone(participantDTO.getPhoneNumber() != null ? participantDTO.getPhoneNumber() : "");
        }

        if (validateService.isAdvanceSearchEnabled(tenantStudy)) {
            if (participantDTO.getLastName() != null)
                studySubject.getStudySubjectDetail().setLastName(participantDTO.getLastName() != null ? participantDTO.getLastName() : "");
            if (participantDTO.getIdentifier() != null)
                studySubject.getStudySubjectDetail().setIdentifier(participantDTO.getIdentifier() != null ? participantDTO.getIdentifier() : "");
        }
        return studySubjectDao.saveOrUpdate(studySubject);

    }

    @Transactional
    public void extractParticipantsInfo(String studyOid, String siteOid, String accessToken, String realm, UserAccountBean userAccountBean, String schema, JobDetail jobDetail, boolean incRelatedInfo, int pageNumber, int pageSize, boolean isStudyLevelUser) {

        CoreResources.setRequestSchema(schema);

        Study site = studyDao.findByOcOID(siteOid);
        Study study = studyDao.findByOcOID(studyOid);

        logger.info("Execute method asynchronously. "
                + Thread.currentThread().getName());

        // Get all list of StudySubjects by studyId
        List<StudySubject> studySubjects = studySubjectDao.findAllByStudyWithAvailableAndSignedStatusOnly(site.getStudyId(), pageNumber, pageSize);
        List<OCUserDTO> userDTOS = new ArrayList<>();
        sdf_fileName.setTimeZone(TimeZone.getTimeZone("GMT"));
        String fileName = study.getUniqueIdentifier() + DASH + study.getEnvType() + PARTICIPANT_ACCESS_CODE + "_" + sdf_fileName.format(new Date()) + ".csv";

        try {
            OCUserDTO userDTO = null;
            for (StudySubject studySubject : studySubjects) {
                userDTO = getOCUserDTO(siteOid, accessToken, realm, userAccountBean, incRelatedInfo, studySubject, isStudyLevelUser);
                if (userDTO != null)
                    userDTOS.add(userDTO);
            }

            // add a new method to write this object into text file
            writeToFile(userDTOS, studyOid, fileName, isStudyLevelUser, incRelatedInfo);
        } catch (Exception e) {
            persistJobFailed(jobDetail, fileName);
            logger.error(" Access code Job Creation Failed ", e);
        }
        persistJobCompleted(jobDetail, fileName);
    }

    @Transactional
    public StudyParticipantDetailDTO extractParticipantInfo(String studyOid, String siteOid, String accessToken, String realm, UserAccountBean userAccountBean, String participantID, boolean incRelatedInfo, boolean isStudyLevelUser) throws OpenClinicaSystemException {


        Study site = studyDao.findByOcOID(siteOid);
        Study study = studyDao.findByOcOID(studyOid);


        // Get the StudySubject by studyId and participantID
        StudySubject ss = studySubjectDao.findByLabelAndStudyOrParentStudy(participantID, study);
        OCUserDTO ocuserDTO = null;
        StudyParticipantDetailDTO spDTO = new StudyParticipantDetailDTO();

        if (ss == null) {
            throw new OpenClinicaSystemException(ErrorConstants.ERR_PARTICIPANT_NOT_FOUND);
        }

        if (ss.getStatus().equals(Status.DELETED)
                || ss.getStatus().equals(Status.AUTO_DELETED)) {
            throw new OpenClinicaSystemException(ErrorConstants.ERR_PARTICIPANT_ID_NOT_AVAILABLE);
        }

        try {
            ocuserDTO = getOCUserDTO(siteOid, accessToken, realm, userAccountBean, incRelatedInfo,
                    ss, isStudyLevelUser);


            spDTO.setSubjectOid(ss.getOcOid());
            spDTO.setSubjectKey(ss.getLabel());
            if (ss.getStudySubjectDetail() != null) {
                spDTO.setSecondaryID(ss.getStudySubjectDetail().getIdentifier());
            }

            spDTO.setCreatedBy(ss.getUserAccount().getUserName());
            spDTO.setLastModifiedBy(userAccountDao.findById(ss.getUpdateId()).getUserName());

            if (ss.getDateCreated() != null) {
                spDTO.setCreatedAt(ss.getDateCreated().toLocaleString());
            }
            if (ss.getDateUpdated() != null) {
                spDTO.setLastModified(ss.getDateUpdated().toLocaleString());
            }
            if (ss.getUserStatus() != null) {
                spDTO.setStatus(ss.getUserStatus().getValue());
            }
            if (!isStudyLevelUser) {
                if (ocuserDTO != null) {
                    spDTO.setFirstName(ocuserDTO.getFirstName());
                    spDTO.setLastName(ocuserDTO.getLastName());
                    spDTO.setEmail(ocuserDTO.getEmail());
                    spDTO.setMobileNumber(ocuserDTO.getPhoneNumber());
                }
                if (incRelatedInfo)
                    spDTO.setAccessCode(ocuserDTO.getAccessCode());
            }
        } catch (Exception e) {

            logger.error(" get access code Failed :", e);
        }
        return spDTO;

    }

    /**
     * @param siteOid
     * @param accessToken
     * @param userAccountBean
     * @param incRelatedInfo
     * @param studySubject
     * @return
     */
    private OCUserDTO getOCUserDTO(String siteOid, String accessToken, String realm,
                                   UserAccountBean userAccountBean, boolean incRelatedInfo, StudySubject studySubject, boolean isStudyLevelUser) {

        OCUserDTO ocuserDTO = null;
        if (!studySubject.getStatus().equals(Status.DELETED)
                && !studySubject.getStatus().equals(Status.AUTO_DELETED)) {

            /**
             * OC-10640
             * AC4: Participant contact information and their Participate related information should only be returned
             *  for participants that are in available or signed status.
             */
            if (studySubject.getStatus().equals(Status.AVAILABLE)
                    || studySubject.getStatus().equals(Status.SIGNED)) {


                //Get accessToken from Keycloak
                ocuserDTO = buildOcUserDTOForExtractInfo(studySubject, isStudyLevelUser);
                ParticipantAccessDTO participantAccessDTO = getAccessInfo(accessToken, siteOid, studySubject.getLabel(), realm, userAccountBean, incRelatedInfo, incRelatedInfo);


                if (participantAccessDTO != null && participantAccessDTO.getAccessCode() != null && incRelatedInfo) {
                    ocuserDTO.setAccessCode(participantAccessDTO.getAccessCode());
                }
            }

        }
        return ocuserDTO;
    }

    public OCUserDTO getParticipantAccount(String studyOid, String ssid, String accessToken) {

        OCUserDTO ocUserDTO = null;

        Study study = getStudy(studyOid);
        StudySubject studySubject = getStudySubject(ssid, study);

        if (studySubject != null) {
            ocUserDTO = buildOcUserDTO(studySubject, true);
        }
        return ocUserDTO;
    }


    public List<OCUserDTO> searchParticipantsByFields(String studyOid, String accessToken, String participantId, String firstName, String lastName, String identifier, UserAccountBean userAccountBean) {
        Study tenantStudy = studyDao.findByOcOID(studyOid);
        if (!validateService.isAdvanceSearchEnabled(tenantStudy)) {
            return null;
        }


        String firstNameForSearchUse = cryptoConverter.convertToDatabaseColumn(firstName == null ? null : firstName.toLowerCase());
        String lastNameForSearchUse = cryptoConverter.convertToDatabaseColumn(lastName == null ? null : lastName.toLowerCase());
        String identifierForSearchUse = cryptoConverter.convertToDatabaseColumn(identifier == null ? null : identifier.toLowerCase());

        List<OCUserDTO> userDTOS = new ArrayList<>();
        List<StudySubject> studySubjects = studySubjectDao.findByParticipantIdFirstNameLastNameIdentifier(tenantStudy, participantId, firstNameForSearchUse, lastNameForSearchUse, identifierForSearchUse);

        for (StudySubject studySubject : studySubjects) {
            OCUserDTO userDTO = new OCUserDTO();
            StudySubjectDetail studySubjectDetail = studySubject.getStudySubjectDetail();
            userDTO.setFirstName(studySubjectDetail != null ? studySubjectDetail.getFirstName() : "");
            userDTO.setLastName(studySubjectDetail != null ? studySubjectDetail.getLastName() : "");
            userDTO.setEmail(studySubjectDetail != null ? studySubjectDetail.getEmail() : "");
            userDTO.setPhoneNumber(studySubjectDetail != null ? studySubjectDetail.getPhone() : "");
            userDTO.setIdentifier(studySubjectDetail != null ? studySubjectDetail.getIdentifier() : "");
            userDTO.setParticipantId(studySubject.getLabel());
            userDTO.setViewStudySubjectId(studySubject.getStudySubjectId());
            userDTOS.add(userDTO);
        }


        return userDTOS;
    }


    private UserAccount createUserAccount(OCParticipantDTO participantDTO, StudySubject studySubject, UserAccountBean ownerUserAccountBean, String username, Study publicStudy, String keycloakUserId) {
        if (participantDTO == null)
            return null;
        UserAccount userAccount = new UserAccount();
        userAccount.setUserType(new core.org.akaza.openclinica.domain.user.UserType(4));
        userAccount.setUserName(username);
        userAccount.setActiveStudy(publicStudy);
        userAccount.setStatus(Status.AVAILABLE);
        userAccount.setDateCreated(new Date());
        userAccount.setUserUuid(keycloakUserId);

        UserAccount ownerUserAccount = userAccountDao.findByUserId(ownerUserAccountBean.getId());
        userAccount.setUserAccount(ownerUserAccount);
        StudyUserRole studyUserRole = buildStudyUserRole(username, publicStudy, ownerUserAccount.getUserId());

        String studySchema = CoreResources.getRequestSchema();
        CoreResources.setRequestSchemaToPublic();
        studyUserRole = studyUserRoleDao.saveOrUpdate(studyUserRole);
        userAccount = userAccountDao.saveOrUpdate(userAccount);
        CoreResources.setRequestSchema(studySchema);

        logger.info("UserAccount has been created for Participate");
        return userAccount;
    }


    public List<OCUserDTO> getAllParticipantAccountsFromUserService(String accessToken) {
        String uri = sbsUrl + "/user-service/api/users" + PAGINATION;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Accept-Charset", "UTF-8");
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
            logger.info("Total participate/user numbers: " + userResponse.getBody().size());
            return userResponse.getBody();
        }

    }

    private boolean sendSMSToParticipant(String accessToken, OCParticipantDTO participantDTO, Study tenantStudy, ParticipantAccessDTO accessDTO) {
        String studyName = (tenantStudy.getStudy() != null ? tenantStudy.getStudy().getName() : tenantStudy.getName());

        StringBuffer buffer1 = new StringBuffer("Hi ").append(participantDTO.getFirstName())
                .append(", Thanks for participating in ").append(studyName).append("! ")
                .append("Please follow the link below to get started. ");

        StringBuffer buffer2 = new StringBuffer(accessDTO.getAccessLink()).append(System.lineSeparator())
                .append("For future reference, your access code is ")
                .append(accessDTO.getAccessCode());


        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Accept-Charset", "UTF-8");
        OCMessageDTO messageDTO1 = new OCMessageDTO();
        messageDTO1.setReceiverPhone(StringUtils.remove(participantDTO.getPhoneNumber(), " "));
        messageDTO1.setMessage(buffer1.toString());
        HttpEntity<OCMessageDTO> request1 = new HttpEntity<>(messageDTO1, headers);

        OCMessageDTO messageDTO2 = new OCMessageDTO();
        messageDTO2.setReceiverPhone(StringUtils.remove(participantDTO.getPhoneNumber(), " "));
        messageDTO2.setMessage(buffer2.toString());
        HttpEntity<OCMessageDTO> request2 = new HttpEntity<>(messageDTO2, headers);

        ResponseEntity<String> result = null;
        try {
            result = restTemplate.postForEntity(messageServiceUri, request1, String.class);
            if(result.getStatusCode() != HttpStatus.OK){
                logger.error("sendMessage failed with :" + result.getStatusCode());
                return false;
            }
            result = restTemplate.postForEntity(messageServiceUri, request2, String.class);
        } catch (RestClientException e) {
            logger.error("sendMessage failed with :" + e);
            return false;
        }
        if (result.getStatusCode() != HttpStatus.OK) {
            logger.error("sendMessage failed with :" + result.getStatusCode());
            return false;
        }

        return true;
    }

    private boolean sendEmailToParticipant(StudySubject studySubject, Study tenantStudy, ParticipantAccessDTO accessDTO) {
        ParticipantDTO pDTO = new ParticipantDTO();
        pDTO.setEmailAccount(studySubject.getStudySubjectDetail().getEmail());
        pDTO.setEmailSubject("You've been connected! We're looking forward to your participation.");

        String studyName = (tenantStudy.getStudy() != null ? tenantStudy.getStudy().getName() : tenantStudy.getName());


        String accessLink = "";
        String host = "";
        String accessCode = "";

        if (accessDTO != null) {
            accessLink = (accessDTO.getAccessLink() == null ? "" : accessDTO.getAccessLink());
            host = (accessDTO.getHost() == null ? "" : accessDTO.getHost());
            accessCode = (accessDTO.getAccessCode() == null ? "" : accessDTO.getAccessCode());
        }
        StringBuffer sb = new StringBuffer();
        sb.append("<div style=\"background-color:#f1f1f1\"><center>");
        sb.append("<table style=\"background-color:#ffffff; width:600px; margin:0; padding:0; " +
                "font-family:'Open Sans',sans-serif; border-collapse:collapse!important; height:100%!important\" " +
                "border=\"0\" cellpadding=\"0\" cellspacing=\"0\" height=\"100%\" width=\"100%\">" +
                "<tbody><tr><td valign=\"top\"" +
                "style=\"margin:0; padding:20px; font-family:'Open Sans',sans-serif; height:100%!important\"><div>");
        sb.append("<h1 style=\"font-family:'Didact Gothic',sans-serif;color:#618ebb\">" +
                "Welcome to " + studyName + " Study!</h1>");
        sb.append("<p style='text-align:left'>Dear " + studySubject.getStudySubjectDetail().getFirstName() + ",</p>");
        sb.append("<p>Thanks for participating in " + studyName + " study! " +
                "Please click the link below to get started.</p>");
        sb.append("<p style='text-align:center;margin:25px'>" +
                "<a href='" + accessLink + "' style=\"display: inline-block; " +
                "text-decoration: none; background: #28a745; color: white; padding: 15px 35px; font-weight: bold; " +
                "font-size: medium; border-radius: 5px;\" " +
                "target='_blank'> Let's Go -></a></p>");
        sb.append("<p style= \"line-height: 2em;\">Thanks!<br><strong>" +
                "The " + studyName + " Study Team</strong></p>");
        sb.append("<p style= \"margin-bottom: 20px; line-height: 2em; font-size:10px; color:#6b6b6b;\">You can also access the application by going to: " +
                "" + host + " and enter the access code: " + accessCode + "</p>" +
                "<hr style=\"border:2px solid #eaeef3;border-bottom:0;margin:20px 0\">");
        sb.append("<p style=\"text-align:right; margin-bottom:30px;\">" +
                "<img src=\"https://cdn.shortpixel.ai/client/q_lossless,ret_img/https://www.openclinica.com/wp-content/uploads/open-clinica.png\"" +
                "width=\"125\" alt=\"OC logo\" style=\"border:0; height:auto; line-height:100%; outline:none; text-decoration:none\" data-image-whitelisted=\"\"></p>");
        sb.append("</div></td></tr></tbody></table></center></div>");

        pDTO.setMessage(sb.toString());

        return sendEmailToParticipant(pDTO);

    }

    private boolean sendEmailToParticipant(ParticipantDTO pDTO) throws OpenClinicaSystemException {

        logger.info("Sending email...");
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
            helper.setFrom(EmailEngine.getAdminEmail());
            helper.setTo(pDTO.getEmailAccount());
            helper.setSubject(pDTO.getEmailSubject());
            helper.setText(pDTO.getMessage(), true);

            mailSender.send(mimeMessage);
            logger.debug("Email sent successfully on {}", new Date());
            return true;
        } catch (MailException me) {
            logger.error("Email could not be sent:" + me.getMessage());
        } catch (MessagingException me) {
            logger.error("Email could not be sent:" + me.getMessage());
        }
        return false;
    }


    public ParticipantAccessDTO getAccessInfo(String accessToken, String studyOid, String ssid, String realm, UserAccountBean userAccountBean, boolean auditAccessCodeViewing) {
        return getAccessInfo(accessToken, studyOid, ssid, realm, userAccountBean, auditAccessCodeViewing, true);
    }

    public ParticipantAccessDTO getAccessInfo(String accessToken, String studyOid, String ssid, String realm, UserAccountBean userAccountBean, boolean auditAccessCodeViewing, boolean includeAccessCode) {
        Study tenantStudy = getStudy(studyOid);
        if (!validateService.isParticipateActive(tenantStudy)) {
            logger.error("Participant account is not Active");
            return null;
        }

        StudySubject studySubject = getStudySubject(ssid, tenantStudy);
        if (studySubject == null || studySubject.getUserId() == null) {
            logger.error("Participant account not found");
            return null;
        }
        UserAccount pUserAccount = userAccountDao.findByUserId(studySubject.getUserId());
        if (pUserAccount == null || pUserAccount.getUserUuid() == null) {
            logger.error("Participant account not found");
            return null;
        }

        String accessCode = null;
        if (includeAccessCode) {
            accessCode = keycloakClient.getAccessCode(accessToken, pUserAccount.getUserUuid(), realm);
            if (accessCode == null) {
                logger.error(" Access code from Keycloak returned null");
                return null;
            }
        }

        if (tenantStudy.getStudy() != null)
            tenantStudy = tenantStudy.getStudy();

        List<ModuleConfigDTO> moduleConfigDTOs = studyBuildService.getModuleConfigsFromStudyService(accessToken, tenantStudy);
        if (moduleConfigDTOs != null && moduleConfigDTOs.size() != 0) {
            ModuleConfigDTO moduleConfigDTO = studyBuildService.getModuleConfig(moduleConfigDTOs, tenantStudy, ModuleProcessor.Modules.PARTICIPATE);
            if (moduleConfigDTO != null) {
                ModuleConfigAttributeDTO moduleConfigAttributeDTO = studyBuildService.getModuleConfigAttribute(moduleConfigDTO.getAttributes(), tenantStudy);
                if (moduleConfigAttributeDTO != null) {
                    ParticipantAccessDTO participantAccessDTO = new ParticipantAccessDTO();

                    participantAccessDTO.setAccessCode(accessCode);
                    participantAccessDTO.setHost(moduleConfigAttributeDTO.getValue());
                    participantAccessDTO.setAccessLink(moduleConfigAttributeDTO.getValue() + ACCESS_LINK_PART_URL + accessCode);

                    if (auditAccessCodeViewing) {
                        AuditLogEventDTO auditLogEventDTO = populateAuditLogEventDTO(studySubject.getStudySubjectId(), 42);
                        auditLogEventService.saveAuditLogEvent(auditLogEventDTO, userAccountBean);
                    }
                    return participantAccessDTO;
                }
            }
        }
        logger.error("Participant Access Link is not found");
        return null;
    }

    private AuditLogEventDTO buildAuditLogEventForViewingAccessCodeDTO(StudySubject studySubject) {
        AuditLogEventDTO auditLogEventDTO = new AuditLogEventDTO();
        auditLogEventDTO.setAuditTable("study_subject");
        auditLogEventDTO.setEntityId(studySubject.getStudySubjectId());
        auditLogEventDTO.setEntityName("Participant access code");
        auditLogEventDTO.setAuditLogEventTypId(42);
        return auditLogEventDTO;
    }

    private OCUserDTO buildOcUserDTO(StudySubject studySubject, boolean incRelatedInfo) {
        OCUserDTO ocUserDTO = new OCUserDTO();
        ocUserDTO.setParticipantId(studySubject.getLabel());
        StudySubjectDetail studySubjectDetail = studySubject.getStudySubjectDetail();
        if (studySubjectDetail != null) {
            ocUserDTO.setFirstName(studySubjectDetail.getFirstName() != null ? studySubjectDetail.getFirstName() : "");
            ocUserDTO.setEmail(studySubjectDetail.getEmail() != null ? studySubjectDetail.getEmail() : "");
            ocUserDTO.setPhoneNumber(studySubjectDetail.getPhone() != null ? studySubjectDetail.getPhone() : "");
            ocUserDTO.setLastName(studySubjectDetail.getLastName() != null ? studySubjectDetail.getLastName() : "");
            ocUserDTO.setIdentifier(studySubjectDetail.getIdentifier() != null ? studySubjectDetail.getIdentifier() : "");

            if (incRelatedInfo) {
                ocUserDTO.setStatus(studySubject.getUserStatus());
            }
        } else {
            ocUserDTO.setFirstName("");
            ocUserDTO.setEmail("");
            ocUserDTO.setPhoneNumber("");
            ocUserDTO.setLastName("");
            ocUserDTO.setIdentifier("");
        }
        return ocUserDTO;
    }

    private OCUserDTO buildOcUserDTOForExtractInfo(StudySubject studySubject, boolean isStudyLevelUser) {
        OCUserDTO ocUserDTO = new OCUserDTO();
        ocUserDTO.setParticipantId(studySubject.getLabel());
        StudySubjectDetail studySubjectDetail = studySubject.getStudySubjectDetail();
        if (studySubjectDetail != null) {
            if (!isStudyLevelUser) {
                ocUserDTO.setFirstName(studySubjectDetail.getFirstName() != null ? studySubjectDetail.getFirstName() : "");
                ocUserDTO.setEmail(studySubjectDetail.getEmail() != null ? studySubjectDetail.getEmail() : "");
                ocUserDTO.setPhoneNumber(studySubjectDetail.getPhone() != null ? studySubjectDetail.getPhone() : "");
                ocUserDTO.setLastName(studySubjectDetail.getLastName() != null ? studySubjectDetail.getLastName() : "");
                ocUserDTO.setIdentifier(studySubjectDetail.getIdentifier() != null ? studySubjectDetail.getIdentifier() : "");
            }
            ocUserDTO.setStatus(studySubject.getUserStatus());
        } else {
            ocUserDTO.setFirstName("");
            ocUserDTO.setEmail("");
            ocUserDTO.setPhoneNumber("");
            ocUserDTO.setLastName("");
            ocUserDTO.setIdentifier("");
        }
        return ocUserDTO;
    }

    private StudyUserRole buildStudyUserRole(String username, Study publicStudy, int ownerId) {
        StudyUserRoleId userRoleId = new StudyUserRoleId();
        userRoleId.setStudyId(publicStudy.getStudyId());
        userRoleId.setUserName(username);

        StudyUserRole studyUserRole = new StudyUserRole();
        studyUserRole.setId(userRoleId);
        studyUserRole.setRoleName(Role.PARTICIPATE.getName());
        studyUserRole.setStatusId(core.org.akaza.openclinica.bean.core.Status.AVAILABLE.getId());
        studyUserRole.setDateCreated(new Date());
        studyUserRole.setOwnerId(ownerId);
        return studyUserRole;
    }


    private void writeToFile(List<OCUserDTO> userDTOs, String studyOid, String fileName, boolean isStudyLevelUser, boolean incRelatedInfo) {
        String filePath = getFilePath(JobType.ACCESS_CODE) + File.separator + fileName;

        File file = new File(filePath);

        PrintWriter writer = null;
        try {
            writer = openFile(file);
            writer.print(writeToTextFile(userDTOs, isStudyLevelUser, incRelatedInfo));
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            logger.error("Error while accessing file for writing: ", e);
        } catch (Exception exception) {
            logger.error("Error while writing to file: ", exception);
        } finally {
            closeFile(writer);
        }
    }


    public String getFilePath(JobType jobType) {
        String dirPath = CoreResources.getField("filePath") + BULK_JOBS + File.separator + jobType.toString().toLowerCase();
        File directory = new File(dirPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return dirPath;
    }

    private PrintWriter openFile(File file) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(file.getPath(), "UTF-8");
        return writer;
    }


    private void closeFile(PrintWriter writer) {
        writer.close();
    }


    private String writeToTextFile(List<OCUserDTO> userDTOS, boolean isStudyLevelUser, boolean incRelatedInfo) {

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("ParticipantId");
        stringBuffer.append(SEPERATOR);
        if (!isStudyLevelUser) {
            stringBuffer.append("First Name");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append("Last Name");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append("Email");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append("Mobile");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append("Identifier");
            stringBuffer.append(SEPERATOR);
        }
        if (!isStudyLevelUser && incRelatedInfo) {
            stringBuffer.append("Access Code");
            stringBuffer.append(SEPERATOR);
        }
        stringBuffer.append("Participate Status");
        stringBuffer.append('\n');
        for (OCUserDTO userDTO : userDTOS) {
            stringBuffer.append(userDTO.getParticipantId() != null ? userDTO.getParticipantId() : "");
            stringBuffer.append(SEPERATOR);
            if (!isStudyLevelUser) {
                stringBuffer.append(userDTO.getFirstName() != null ? userDTO.getFirstName() : "");
                stringBuffer.append(SEPERATOR);
                stringBuffer.append(userDTO.getLastName() != null ? userDTO.getLastName() : "");
                stringBuffer.append(SEPERATOR);
                stringBuffer.append(userDTO.getEmail() != null ? userDTO.getEmail() : "");
                stringBuffer.append(SEPERATOR);
                stringBuffer.append(userDTO.getPhoneNumber() != null ? userDTO.getPhoneNumber() : "");
                stringBuffer.append(SEPERATOR);
                stringBuffer.append(userDTO.getIdentifier() != null ? userDTO.getIdentifier() : "");
                stringBuffer.append(SEPERATOR);
            }
            if (!isStudyLevelUser && incRelatedInfo) {
                stringBuffer.append(userDTO.getAccessCode() != null ? userDTO.getAccessCode() : "");
                stringBuffer.append(SEPERATOR);
            }
            stringBuffer.append(userDTO.getStatus() != null ? userDTO.getStatus() : "");
            stringBuffer.append('\n');
        }

        StringBuilder sb = new StringBuilder();
        sb.append(stringBuffer.toString() + "\n");

        return sb.toString();
    }

    private AuditLogEventDTO populateAuditLogEventDTO(int studySubjectId, int auditLogEventTypeId) {
        AuditLogEventDTO auditLogEventDTO = new AuditLogEventDTO();
        auditLogEventDTO.setAuditLogEventTypId(auditLogEventTypeId);
        auditLogEventDTO.setEntityId(studySubjectId);
        auditLogEventDTO.setEntityName("Participant access code");
        auditLogEventDTO.setAuditTable("study_subject");

        return auditLogEventDTO;
    }

    public JobDetail persistJobCreated(Study study, Study site, UserAccount createdBy, JobType jobType, String sourceFileName) {
        JobDetail jobDetail = new JobDetail();
        jobDetail.setCreatedBy(createdBy);
        jobDetail.setDateCreated(new Date());
        jobDetail.setSite(site);
        jobDetail.setStudy(study);
        jobDetail.setStatus(JobStatus.IN_PROGRESS);
        jobDetail.setType(jobType);
        jobDetail.setUuid(UUID.randomUUID().toString());
        jobDetail.setSourceFileName(sourceFileName);
        jobDetail = jobService.saveOrUpdateJob(jobDetail);
        logger.debug("Job Id {} has started", jobDetail.getJobDetailId());
        return jobDetail;
    }


    public void persistJobCompleted(JobDetail jobDetail, String fileName) {
        jobDetail.setLogPath(fileName);
        jobDetail.setDateCompleted(new Date());
        jobDetail.setStatus(JobStatus.COMPLETED);
        jobDetail = jobService.saveOrUpdateJob(jobDetail);
        logger.debug("Job Id {} has completed", jobDetail.getJobDetailId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persistJobFailed(JobDetail jobDetail, String fileName) {
        jobDetail.setLogPath(fileName);
        jobDetail.setDateCompleted(new Date());
        jobDetail.setStatus(JobStatus.FAILED);
        jobDetail = jobService.saveOrUpdateJob(jobDetail);
        logger.debug("Job Id {} has failed", jobDetail.getJobDetailId());
    }
}