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
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.domain.rule.action.RuleActionBean;
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
import java.util.Set;


import static java.util.Collections.*;

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

    private RestfulServiceHelper restfulServiceHelper;

    public static final String FORM_CONTEXT = "ecid";
    public static final String DASH = "-";
    public static final String PARTICIPATE_EDIT = "participate-edit";
    public static final String PARTICIPATE_ADD_NEW = "participate-add-new";
    public static final String PAGINATION = "?page=0&size=1000";
    public static final String PASSWORD_LENGTH = "9";
    public static final String ACCESS_LINK = "accessLink";
    public static final String ACCESS_LINK_PART_URL = "?accessCode=";


    private String sbsUrl = CoreResources.getField("SBSUrl");

    StudyDAO sdao;


    public StudySubject getStudySubject(String ssid, Study study) {
        return studySubjectDao.findByLabelAndStudyOrParentStudy(ssid, study);
    }

    public Study getStudy(String studyOid) {
        return studyDao.findByOcOID(studyOid);
    }

    public OCUserDTO connectParticipant(String studyOid, String ssid, OCParticipantDTO participantDTO, HttpServletRequest request) {
        getRestfulServiceHelper().setSchema(studyOid, request);
        OCUserDTO ocUserDTO = null;
        Study tenantStudy = getStudy(studyOid);

        String oid = (tenantStudy.getStudy() != null ? tenantStudy.getStudy().getOc_oid() : tenantStudy.getOc_oid());

        StudySubject studySubject = getStudySubject(ssid, tenantStudy);
        String username = oid + "." + studySubject.getOcOid();
        username = username.replaceAll("\\(", ".").replaceAll("\\)", "");

        UserAccountBean ownerUserAccountBean = (UserAccountBean) request.getSession().getAttribute("userBean");


        String accessCode = "";
        do {
            accessCode = RandomStringUtils.random(Integer.parseInt(PASSWORD_LENGTH), true, true);
        } while (keycloakClient.searchAccessCodeExists(request, accessCode));


        Study publicStudy = studyDao.findPublicStudy(tenantStudy.getOc_oid());

        UserAccount userAccount = null;

        if (studySubject != null) {
            if (studySubject.getUserId() == null) {
                logger.info("Participate has not registered yet");
                // create participant user Account In Keycloak
                String keycloakUserId = keycloakClient.createParticipateUser(request, null, username, accessCode);
                // create participant user Account In Runtime
                userAccount = createUserAccount(participantDTO, studySubject, ownerUserAccountBean, username, publicStudy, keycloakUserId);

                if (userAccount != null) {
                    studySubject = saveOrUpdateStudySubject(studySubject, participantDTO, UserStatus.CREATED, userAccount.getUserId());

                    //studySubject.setUserId(userAccount.getUserId());
                    //studySubject.setUserStatus(UserStatus.CREATED);
                    //studySubject = studySubjectDao.saveOrUpdate(studySubject);
                    logger.info("Participate user_id: {} and user_status: {} are added in study_subject table: ", studySubject.getUserId(), studySubject.getUserStatus());
                }
            } else {
                // Update participant user Account In Runtime
                userAccount = updateUserAccount(participantDTO, studySubject, ownerUserAccountBean, username, publicStudy, userAccount);
                if (userAccount != null) {
                    //studySubject = studySubjectDao.saveOrUpdate(studySubject);
                    studySubject = saveOrUpdateStudySubject(studySubject, participantDTO, null, null);
                    logger.info("Participate with user_id: {} ,it's user_status: {} is updated in study_subject table: ", studySubject.getUserId(), studySubject.getUserStatus());
                }
            }
        } else {
            logger.info("Participant does not exists or not added yet in OC ");
        }
        if (participantDTO.isInviteParticipant()) {

            ParticipantAccessDTO accessDTO= getAccessInfo(request,studyOid,ssid);

            sendEmailToParticipant(userAccount,tenantStudy, accessDTO);
            //studySubject.setUserStatus(UserStatus.INVITED);
            //studySubject = studySubjectDao.saveOrUpdate(studySubject);
            studySubject = saveOrUpdateStudySubject(studySubject, participantDTO, UserStatus.INVITED, null);

        }
        if (userAccount != null || userAccount.getId() != 0)
            ocUserDTO = buildOcUserDTO(userAccount, studySubject);

        return ocUserDTO;
    }

    private StudySubject saveOrUpdateStudySubject(StudySubject studySubject,OCParticipantDTO participantDTO,
                                                  UserStatus userStatus, Integer userId){

        if (userId != null){
            studySubject.setUserId(userId);
        }

        if (userStatus != null){
            studySubject.setUserStatus(UserStatus.CREATED);
        }

        if (studySubject.getStudySubjectDetail() == null){
            StudySubjectDetail studySubjectDetail = new StudySubjectDetail();
            studySubject.setStudySubjectDetail(studySubjectDetail);
        }
        studySubject.getStudySubjectDetail().setFirstName(participantDTO.getFirstName() == null ? "" : participantDTO.getFirstName());
        studySubject.getStudySubjectDetail().setEmail(participantDTO.getEmail() == null ? "" : participantDTO.getEmail());
        studySubject.getStudySubjectDetail().setPhone(participantDTO.getMobilePhone() == null ? "" : participantDTO.getMobilePhone());
        return studySubjectDao.saveOrUpdate(studySubject);

    }


    public OCUserDTO getParticipantAccount(String studyOid, String ssid, HttpServletRequest request) {

        getRestfulServiceHelper().setSchema(studyOid, request);
        OCUserDTO ocUserDTO = null;

        Study study = getStudy(studyOid);
        StudySubject studySubject = getStudySubject(ssid, study);

        if (studySubject!= null && studySubject.getUserId() != null) {
            String studySchema = CoreResources.getRequestSchema();
            CoreResources.setRequestSchema("public");
            UserAccount userAccount = userAccountDao.findByUserId(studySubject.getUserId());
            CoreResources.setRequestSchema(studySchema);
            if (userAccount != null)
                ocUserDTO = buildOcUserDTO(userAccount, studySubject);
        }
        return ocUserDTO;
    }


    private UserAccount createUserAccount(OCParticipantDTO participantDTO, StudySubject studySubject,UserAccountBean ownerUserAccountBean,String username ,Study publicStudy,String keycloakUserId) {
        if (participantDTO == null)
            return null;
       UserAccount userAccount = new UserAccount();
        userAccount.setFirstName(participantDTO.getFirstName() == null ? "" : participantDTO.getFirstName());
        userAccount.setEmail(participantDTO.getEmail() == null ? "" : participantDTO.getEmail());
        userAccount.setPhone(participantDTO.getMobilePhone() == null ? "" : participantDTO.getMobilePhone());
        userAccount.setUserType(new org.akaza.openclinica.domain.user.UserType(4));
        userAccount.setUserName(username);
        userAccount.setActiveStudy(publicStudy);
        userAccount.setStatus(Status.AVAILABLE);
        userAccount.setDateCreated(new Date());
        userAccount.setUserUuid(keycloakUserId);

        String studySchema = CoreResources.getRequestSchema();
        CoreResources.setRequestSchema("public");
        UserAccount ownerUserAccount=userAccountDao.findByUserId(ownerUserAccountBean.getId());
        userAccount.setUserAccount(ownerUserAccount);
        StudyUserRole studyUserRole = buildStudyUserRole(username,publicStudy,ownerUserAccount.getUserId());
        studyUserRole = studyUserRoleDao.saveOrUpdate(studyUserRole);
        userAccount = userAccountDao.saveOrUpdate(userAccount);
        CoreResources.setRequestSchema(studySchema);

        logger.info("UserAccount has been created for Participate");
        return userAccount;
    }

    private UserAccount updateUserAccount(OCParticipantDTO participantDTO , StudySubject studySubject,UserAccountBean ownerUserAccountBean,String username,Study publicStudy,UserAccount userAccount) {
        if (participantDTO == null)
            return userAccount;

        String tenantSchema = CoreResources.getRequestSchema();
        CoreResources.setRequestSchema("public");
        userAccount = userAccountDao.findByUserId(studySubject.getUserId());
        List<StudyUserRole> studyUserRoles = studyUserRoleDao.findAllUserRolesByUserAccountAndStudy(userAccount,publicStudy.getStudyId());
        if(studyUserRoles.size()==0) {
            StudyUserRole studyUserRole = buildStudyUserRole(username,publicStudy,ownerUserAccountBean.getId());
            studyUserRole = studyUserRoleDao.saveOrUpdate(studyUserRole);
        }else{
            for(StudyUserRole studyUserRole:studyUserRoles){
                updateStudyUserRole(studyUserRole,ownerUserAccountBean.getId());
            }
        }

        userAccount.setFirstName(participantDTO.getFirstName() == null ? "" : participantDTO.getFirstName());
        userAccount.setEmail(participantDTO.getEmail() == null ? "" : participantDTO.getEmail());
        userAccount.setPhone(participantDTO.getMobilePhone() == null ? "" : participantDTO.getMobilePhone());

        userAccount.setDateUpdated(new Date());
        userAccount.setUpdateId(ownerUserAccountBean.getId());

        userAccount = userAccountDao.saveOrUpdate(userAccount);
        CoreResources.setRequestSchema(tenantSchema);

        logger.info("UserAccount has been Updated for Participate");
        return userAccount;
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
            logger.info("Total participate/user numbers: " + userResponse.getBody().size());
            return userResponse.getBody();
        }

    }

    private void sendEmailToParticipant(UserAccount userAccount, Study tenantStudy,ParticipantAccessDTO accessDTO) {
        ParticipantDTO pDTO = new ParticipantDTO();
        pDTO.setEmailAccount(userAccount.getEmail());
        pDTO.setEmailSubject("You've been connected! We're looking forward to your participation.");

        String studyName = (tenantStudy.getStudy() != null ? tenantStudy.getStudy().getName() : tenantStudy.getName());


        String accessLink="";
        String host="";
        String accessCode="";

        if (accessDTO != null) {
            accessLink = (accessDTO.getAccessLink() == null ? "" : accessDTO.getAccessLink());
            host = (accessDTO.getHost() == null ? "" : accessDTO.getHost());
            accessCode = (accessDTO.getAccessCode() == null ? "" : accessDTO.getAccessCode());
        }


        pDTO.setMessage("Hi "+userAccount.getFirstName()+",\n" +
                "\n" +
                "Thanks for participating in "+studyName+"!\n" +
                "\n" +
                "Click here to begin " + accessLink + "\n" +
                "\n" +
                "Or, you may go to: " + host + "\n" +
                "and enter access code " + accessCode + "\n" +
                "\n" +
                "Thank you,\n" +
                "The Study Team");

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

    public RestfulServiceHelper getRestfulServiceHelper() {
        if (restfulServiceHelper == null) {
            restfulServiceHelper = new RestfulServiceHelper(this.dataSource);
        }
        return restfulServiceHelper;
    }


    public ParticipantAccessDTO getAccessInfo( HttpServletRequest request,String studyOid, String ssid) {
        Study tenantStudy = getStudy(studyOid);
        StudySubject studySubject = getStudySubject(ssid, tenantStudy);
        if(studySubject==null || studySubject.getUserId()==null) {
         logger.error("Participant account not found");
            return null;
        }
        String tenantSchema = CoreResources.getRequestSchema();
        CoreResources.setRequestSchema("public");
        UserAccount pUserAccount = userAccountDao.findByUserId(studySubject.getUserId());
        CoreResources.setRequestSchema(tenantSchema);
        if(pUserAccount==null || pUserAccount.getUserUuid()==null) {
            logger.error("Participant account not found");
            return null;
        }
        String accessCode = keycloakClient.getAccessCode(request,pUserAccount.getUserUuid());

        if(accessCode==null) {
            logger.error(" Access code from Keycloack returned null ");
            return null;
        }
        if (tenantStudy.getStudy()!=null)
            tenantStudy = tenantStudy.getStudy();

        List<ModuleConfigDTO> moduleConfigDTOs = studyBuildService.getParticipateModuleFromStudyService(request, tenantStudy);
        if (moduleConfigDTOs != null && moduleConfigDTOs.size() != 0) {
            ModuleConfigDTO moduleConfigDTO = studyBuildService.getModuleConfig(moduleConfigDTOs, tenantStudy);
            if (moduleConfigDTO != null) {
                ModuleConfigAttributeDTO moduleConfigAttributeDTO = studyBuildService.getModuleConfigAttribute(moduleConfigDTO.getAttributes(), tenantStudy);
                if (moduleConfigAttributeDTO != null) {
                    logger.info("Participant Access Link is :{}",moduleConfigAttributeDTO.getValue() + ACCESS_LINK_PART_URL + accessCode);
                    ParticipantAccessDTO participantAccessDTO = new ParticipantAccessDTO();
                    participantAccessDTO.setAccessCode(accessCode);
                    participantAccessDTO.setHost(moduleConfigAttributeDTO.getValue());
                    participantAccessDTO.setAccessLink(moduleConfigAttributeDTO.getValue() + ACCESS_LINK_PART_URL + accessCode);

                    return participantAccessDTO;
                }
            }
        }
        logger.error("Participant Access Link is not found");
        return null;
    }


    private OCUserDTO buildOcUserDTO(UserAccount userAccount, StudySubject studySubject) {
        OCUserDTO ocUserDTO = new OCUserDTO();
        ocUserDTO.setEmail(userAccount.getEmail());
        ocUserDTO.setFirstName(userAccount.getFirstName());
        ocUserDTO.setPhoneNumber(userAccount.getPhone());
        ocUserDTO.setStatus(studySubject.getUserStatus());
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

    private StudyUserRole updateStudyUserRole(StudyUserRole studyUserRole, int updaterId) {
        studyUserRole.setDateUpdated(new Date());
        studyUserRole.setUpdateId(updaterId);
        studyUserRole.setRoleName(Role.PARTICIPATE.getName());
        return studyUserRole;
    }
}