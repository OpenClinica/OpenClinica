package org.akaza.openclinica.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.ParticipantDTO;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.*;
import org.akaza.openclinica.controller.dto.ModuleConfigAttributeDTO;
import org.akaza.openclinica.controller.dto.ModuleConfigDTO;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.*;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.domain.enumsupport.JobStatus;
import org.akaza.openclinica.domain.enumsupport.JobType;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.web.rest.client.auth.impl.KeycloakClientImpl;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.RandomStringUtils;
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
import org.springframework.web.client.RestTemplate;
import org.akaza.openclinica.controller.dto.AuditLogEventDTO;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * This Service class is used with View Study Subject Page
 *
 * @author joekeremian
 */

@Service( "userService" )
public class UserServiceImpl implements UserService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    @Qualifier( "dataSource" )
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

    private RestfulServiceHelper restfulServiceHelper;

    public static final String FORM_CONTEXT = "ecid";
    public static final String DASH = "-";
    public static final String PARTICIPATE_EDIT = "participate-edit";
    public static final String PARTICIPATE_ADD_NEW = "participate-add-new";
    public static final String PAGINATION = "?page=0&size=1000";
    public static final String PASSWORD_LENGTH = "9";
    public static final String ACCESS_LINK = "accessLink";
    public static final String ACCESS_LINK_PART_URL = "?accessCode=";
    public static final String ENABLED = "enabled";
    public static final String SEPERATOR = "|";
    public static final String PARTICIPANT_ACCESS_CODE = "_Participant Access Code";


    private String urlBase = CoreResources.getField("sysURL").split("/MainMenu")[0];

    private static String sbsUrl = CoreResources.getField("SBSUrl");

    StudyDAO sdao;


    public StudySubject getStudySubject(String ssid, Study study) {
        return studySubjectDao.findByLabelAndStudyOrParentStudy(ssid, study);
    }

    public Study getStudy(String studyOid) {
        return studyDao.findByOcOID(studyOid);
    }

    public OCUserDTO connectParticipant(String studyOid, String ssid, OCParticipantDTO participantDTO, String accessToken, UserAccountBean userAccountBean, String customerUuid) {
        OCUserDTO ocUserDTO = null;
        Study tenantStudy = getStudy(studyOid);

        String oid = (tenantStudy.getStudy() != null ? tenantStudy.getStudy().getOc_oid() : tenantStudy.getOc_oid());

        StudySubject studySubject = getStudySubject(ssid, tenantStudy);
        String username = oid + "." + studySubject.getOcOid();
        username = username.replaceAll("\\(", ".").replaceAll("\\)", "");


        String accessCode = "";
        if (validateService.isParticipateActive(tenantStudy)) {
            do {
                accessCode = RandomStringUtils.random(Integer.parseInt(PASSWORD_LENGTH), true, true);
            } while (keycloakClient.searchAccessCodeExists(accessToken, accessCode, customerUuid));
        }

        Study publicStudy = studyDao.findPublicStudy(tenantStudy.getOc_oid());

        String studyEnvironment = (publicStudy.getStudy() != null) ? publicStudy.getStudy().getStudyEnvUuid() : publicStudy.getStudyEnvUuid();
        UserAccount userAccount = userAccountDao.findById(userAccountBean.getId());
        UserAccount pUserAccount = null;

        if (studySubject != null) {
            if (studySubject.getUserId() == null && validateService.isParticipateActive(tenantStudy)) {
                logger.info("Participate has not registered yet");
                // create participant user Account In Keycloak
                String keycloakUserId = keycloakClient.createParticipateUser(accessToken, null, username, accessCode, studyEnvironment, customerUuid);
                // create participant user Account In Runtime
                pUserAccount = createUserAccount(participantDTO, studySubject, userAccountBean, username, publicStudy, keycloakUserId);
                // create study subject detail Account
                studySubject = saveOrUpdateStudySubject(studySubject, participantDTO, UserStatus.CREATED, pUserAccount.getUserId(), tenantStudy, userAccount);
                logger.info("Participate user_id: {} and user_status: {} are added in study_subject table: ", studySubject.getUserId(), studySubject.getUserStatus());

            } else {
                // update study subject detail Account
                studySubject = saveOrUpdateStudySubject(studySubject, participantDTO, null, null, tenantStudy, userAccount);
                logger.info("Participate with user_id: {} ,it's user_status: {} is updated in study_subject table: ", studySubject.getUserId(), studySubject.getUserStatus());
            }
        } else {
            logger.info("Participant does not exists or not added yet in OC ");
        }
        if (participantDTO.isInviteParticipant()) {

            ParticipantAccessDTO accessDTO = getAccessInfo(accessToken, studyOid, ssid, customerUuid, userAccountBean);

            sendEmailToParticipant(studySubject, tenantStudy, accessDTO);
            studySubject = saveOrUpdateStudySubject(studySubject, participantDTO, UserStatus.INVITED, null, tenantStudy, userAccount);

        }
        ocUserDTO = buildOcUserDTO(studySubject);

        return ocUserDTO;
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
    public void extractParticipantsInfo(String studyOid, String siteOid, String accessToken, String customerUuid, UserAccountBean userAccountBean, String schema, JobDetail jobDetail) {

        CoreResources.setRequestSchema(schema);

        Study site = studyDao.findByOcOID(siteOid);
        Study study = studyDao.findByOcOID(studyOid);

        logger.info("Execute method asynchronously. "
                + Thread.currentThread().getName());

        // Get all list of StudySubjects by studyId
        List<StudySubject> studySubjects = studySubjectDao.findAllByStudy(site.getStudyId());
        List<OCUserDTO> userDTOS = new ArrayList<>();
        String fileName = study.getUniqueIdentifier() + DASH + study.getEnvType() + PARTICIPANT_ACCESS_CODE + new SimpleDateFormat("_yyyy-MM-dd-hhmmssS'.txt'").format(new Date());

        try {
            for (StudySubject studySubject : studySubjects) {
                // subject is not removed or auto-removed
                if (!studySubject.getStatus().equals(Status.DELETED)
                        && !studySubject.getStatus().equals(Status.AUTO_DELETED)) {

                    OCUserDTO userDTO = buildOcUserDTO(studySubject);
                    //Get accessToken from Keycloak


                    ParticipantAccessDTO participantAccessDTO = getAccessInfo(accessToken, siteOid, studySubject.getLabel(), customerUuid, userAccountBean);
                    if (participantAccessDTO != null && participantAccessDTO.getAccessCode() != null) {
                        userDTO.setAccessCode(participantAccessDTO.getAccessCode());
                    }
                    userDTOS.add(userDTO);
                }
            }
            // add a new method to write this object into text file
            writeToFile(userDTOS, studyOid, fileName);
        } catch (Exception e) {
            persistJobFailed(jobDetail, fileName);
            logger.error(" Access code Job Creation Failed ");
        }
        persistJobCompleted(jobDetail, fileName);
    }

    public OCUserDTO getParticipantAccount(String studyOid, String ssid, String accessToken) {

        OCUserDTO ocUserDTO = null;

        Study study = getStudy(studyOid);
        StudySubject studySubject = getStudySubject(ssid, study);

        if (studySubject != null) {
            ocUserDTO = buildOcUserDTO(studySubject);
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
        userAccount.setUserType(new org.akaza.openclinica.domain.user.UserType(4));
        userAccount.setUserName(username);
        userAccount.setActiveStudy(publicStudy);
        userAccount.setStatus(Status.AVAILABLE);
        userAccount.setDateCreated(new Date());
        userAccount.setUserUuid(keycloakUserId);

        UserAccount ownerUserAccount = userAccountDao.findByUserId(ownerUserAccountBean.getId());
        userAccount.setUserAccount(ownerUserAccount);
        StudyUserRole studyUserRole = buildStudyUserRole(username, publicStudy, ownerUserAccount.getUserId());

        String studySchema = CoreResources.getRequestSchema();
        CoreResources.setRequestSchema("public");
        studyUserRole = studyUserRoleDao.saveOrUpdate(studyUserRole);
        userAccount = userAccountDao.saveOrUpdate(userAccount);
        CoreResources.setRequestSchema(studySchema);

        logger.info("UserAccount has been created for Participate");
        return userAccount;
    }


    public List<OCUserDTO> getAllParticipantAccountsFromUserService(String accessToken) {
        String uri = sbsUrl.substring(0, sbsUrl.length() - 1) + PAGINATION;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
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
            logger.info("Total participate/user numbers: " + userResponse.getBody().size());
            return userResponse.getBody();
        }

    }

    private void sendEmailToParticipant(StudySubject studySubject, Study tenantStudy, ParticipantAccessDTO accessDTO) {
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
        sb.append("Hi ");
        sb.append(studySubject.getStudySubjectDetail().getFirstName());
        sb.append(",");
        sb.append("<br>");
        sb.append("<br>");

        sb.append("Thanks for participating in ");
        sb.append(studyName);
        sb.append("!");
        sb.append("<br>");
        sb.append("<br>");

        sb.append("<a href=\"" + accessLink + "\">Click here to begin</a>");
        sb.append("<br>");
        sb.append("<br>");

        sb.append("Or, you may go to: ");
        sb.append(host);
        sb.append("<br>");

        sb.append("and enter access code: ");
        sb.append(accessCode);
        sb.append("<br>");
        sb.append("<br>");

        sb.append("Thank you");
        sb.append("<br>");

        sb.append(studyName + " Team");

        pDTO.setMessage(sb.toString());

        sendEmailToParticipant(pDTO);

    }

    private void sendEmailToParticipant(ParticipantDTO pDTO) throws OpenClinicaSystemException {

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
        } catch (MailException me) {
            logger.error("Email could not be sent");
            throw new OpenClinicaSystemException(me.getMessage());
        } catch (MessagingException me) {
            logger.error("Email could not be sent");
            throw new OpenClinicaSystemException(me.getMessage());
        }
    }


    public ParticipantAccessDTO getAccessInfo(String accessToken, String studyOid, String ssid, String customerUuid, UserAccountBean userAccountBean) {
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
        String accessCode = keycloakClient.getAccessCode(accessToken, pUserAccount.getUserUuid(), customerUuid);

        if (accessCode == null) {
            logger.error(" Access code from Keycloack returned null ");
            return null;
        }
        if (tenantStudy.getStudy() != null)
            tenantStudy = tenantStudy.getStudy();

        List<ModuleConfigDTO> moduleConfigDTOs = studyBuildService.getParticipateModuleFromStudyService(accessToken, tenantStudy);
        if (moduleConfigDTOs != null && moduleConfigDTOs.size() != 0) {
            ModuleConfigDTO moduleConfigDTO = studyBuildService.getModuleConfig(moduleConfigDTOs, tenantStudy);
            if (moduleConfigDTO != null) {
                ModuleConfigAttributeDTO moduleConfigAttributeDTO = studyBuildService.getModuleConfigAttribute(moduleConfigDTO.getAttributes(), tenantStudy);
                if (moduleConfigAttributeDTO != null) {
                    logger.info("Participant Access Link is :{}", moduleConfigAttributeDTO.getValue() + ACCESS_LINK_PART_URL + accessCode);
                    ParticipantAccessDTO participantAccessDTO = new ParticipantAccessDTO();
                    participantAccessDTO.setAccessCode(accessCode);
                    participantAccessDTO.setHost(moduleConfigAttributeDTO.getValue());
                    participantAccessDTO.setAccessLink(moduleConfigAttributeDTO.getValue() + ACCESS_LINK_PART_URL + accessCode);

                    AuditLogEventDTO auditLogEventDTO = populateAuditLogEventDTO(studySubject.getStudySubjectId());
                    auditLogEventService.saveAuditLogEvent(auditLogEventDTO, userAccountBean);

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

    private OCUserDTO buildOcUserDTO(StudySubject studySubject) {
        OCUserDTO ocUserDTO = new OCUserDTO();
        ocUserDTO.setParticipantId(studySubject.getLabel());
        StudySubjectDetail studySubjectDetail = studySubject.getStudySubjectDetail();
        if (studySubjectDetail != null) {
            ocUserDTO.setFirstName(studySubjectDetail.getFirstName() != null ? studySubjectDetail.getFirstName() : "");
            ocUserDTO.setEmail(studySubjectDetail.getEmail() != null ? studySubjectDetail.getEmail() : "");
            ocUserDTO.setPhoneNumber(studySubjectDetail.getPhone() != null ? studySubjectDetail.getPhone() : "");
            ocUserDTO.setLastName(studySubjectDetail.getLastName() != null ? studySubjectDetail.getLastName() : "");
            ocUserDTO.setIdentifier(studySubjectDetail.getIdentifier() != null ? studySubjectDetail.getIdentifier() : "");
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
        studyUserRole.setStatusId(org.akaza.openclinica.bean.core.Status.AVAILABLE.getId());
        studyUserRole.setDateCreated(new Date());
        studyUserRole.setOwnerId(ownerId);
        return studyUserRole;
    }


    private void writeToFile(List<OCUserDTO> userDTOs, String studyOid, String fileName) {
        String filePath = getFilePath(JobType.ACCESS_CODE) + File.separator + fileName;

        File file = new File(filePath);

        PrintWriter writer = null;
        try {
            writer = openFile(file);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            writer.print(writeToTextFile(userDTOs));
            closeFile(writer);
        }
        StringBuilder body = new StringBuilder();


        logger.info(body.toString());


    }



    public String getFilePath(JobType jobType) {
      String dirPath= CoreResources.getField("filePath") + BULK_JOBS+ File.separator+ jobType.toString().toLowerCase();
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


    private String writeToTextFile(List<OCUserDTO> userDTOS) {

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("ParticipantId");
        stringBuffer.append(SEPERATOR);
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
        stringBuffer.append("Access Code");
        stringBuffer.append(SEPERATOR);
        stringBuffer.append("Participate Status");
        stringBuffer.append('\n');
        for (OCUserDTO userDTO : userDTOS) {
            stringBuffer.append(userDTO.getParticipantId() != null ? userDTO.getParticipantId() : "");
            stringBuffer.append(SEPERATOR);
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
            stringBuffer.append(userDTO.getAccessCode() != null ? userDTO.getAccessCode() : "");
            stringBuffer.append(SEPERATOR);
            stringBuffer.append(userDTO.getStatus() != null ? userDTO.getStatus() : "");
            stringBuffer.append('\n');
        }

        StringBuilder sb = new StringBuilder();
        sb.append(stringBuffer.toString() + "\n");

        return sb.toString();
    }

    private AuditLogEventDTO populateAuditLogEventDTO(int studySubjectId) {
        AuditLogEventDTO auditLogEventDTO = new AuditLogEventDTO();
        auditLogEventDTO.setAuditLogEventTypId(42);
        auditLogEventDTO.setEntityId(studySubjectId);
        auditLogEventDTO.setEntityName("Participant access code");
        auditLogEventDTO.setAuditTable("study_subject");

        return auditLogEventDTO;
    }

    public JobDetail persistJobCreated(Study study, Study site, UserAccount createdBy,JobType jobType,String sourceFileName) {
        JobDetail jobDetail = new JobDetail();
        jobDetail.setCreatedBy(createdBy);
        jobDetail.setDateCreated(new Date());
        jobDetail.setSite(site);
        jobDetail.setStudy(study);
        jobDetail.setStatus(JobStatus.IN_PROGRESS);
        jobDetail.setType(jobType);
        jobDetail.setUuid(UUID.randomUUID().toString());
        jobDetail.setSourceFileName(sourceFileName);
        return jobService.saveOrUpdateJob(jobDetail);
    }


    public void persistJobCompleted(JobDetail jobDetail, String fileName) {
        jobDetail.setLogPath(fileName);
        jobDetail.setDateCompleted(new Date());
        jobDetail.setStatus(JobStatus.COMPLETED);
        jobDetail =jobService.saveOrUpdateJob(jobDetail);
    }

    public void persistJobFailed(JobDetail jobDetail,String fileName) {
        jobDetail.setLogPath(fileName);
        jobDetail.setDateCompleted(new Date());
        jobDetail.setStatus(JobStatus.FAILED);
        jobDetail =jobService.saveOrUpdateJob(jobDetail);
    }



}