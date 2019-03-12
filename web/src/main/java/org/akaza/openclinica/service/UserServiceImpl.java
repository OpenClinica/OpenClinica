package org.akaza.openclinica.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.ParticipantDTO;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
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
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
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

    @Autowired
    CryptoConverter cryptoConverter;

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


    private String sbsUrl = CoreResources.getField("SBSUrl");
    private String advanceSearch = CoreResources.getField("module.contacts");

    StudyDAO sdao;


    public StudySubject getStudySubject(String ssid, Study study) {
        return studySubjectDao.findByLabelAndStudyOrParentStudy(ssid, study);
    }

    public Study getStudy(String studyOid) {
        return studyDao.findByOcOID(studyOid);
    }

    public OCUserDTO connectParticipant(String studyOid, String ssid, OCParticipantDTO participantDTO, String accessToken,UserAccountBean ownerUserAccountBean,String customerUuid) {
        OCUserDTO ocUserDTO = null;
        Study tenantStudy = getStudy(studyOid);

        String oid = (tenantStudy.getStudy() != null ? tenantStudy.getStudy().getOc_oid() : tenantStudy.getOc_oid());

        StudySubject studySubject = getStudySubject(ssid, tenantStudy);
        String username = oid + "." + studySubject.getOcOid();
        username = username.replaceAll("\\(", ".").replaceAll("\\)", "");



        String accessCode = "";
        do {
            accessCode = RandomStringUtils.random(Integer.parseInt(PASSWORD_LENGTH), true, true);
        } while (keycloakClient.searchAccessCodeExists(accessToken, accessCode,customerUuid));


        Study publicStudy = studyDao.findPublicStudy(tenantStudy.getOc_oid());

        String studyEnvironment = (publicStudy.getStudy() != null) ? publicStudy.getStudy().getStudyEnvUuid() : publicStudy.getStudyEnvUuid();


        UserAccount userAccount = null;

        if (studySubject != null) {
            if (studySubject.getUserId() == null && isParticipateActive(tenantStudy)) {
                logger.info("Participate has not registered yet");
                // create participant user Account In Keycloak
                String keycloakUserId = keycloakClient.createParticipateUser(accessToken, null, username, accessCode,studyEnvironment,customerUuid);
                // create participant user Account In Runtime
                    userAccount = createUserAccount(participantDTO, studySubject, ownerUserAccountBean, username, publicStudy, keycloakUserId);
                // create study subject detail Account
                    studySubject = saveOrUpdateStudySubject(studySubject, participantDTO, UserStatus.CREATED, userAccount.getUserId(),tenantStudy);
                    logger.info("Participate user_id: {} and user_status: {} are added in study_subject table: ", studySubject.getUserId(), studySubject.getUserStatus());

            } else {
                // update study subject detail Account
                studySubject = saveOrUpdateStudySubject(studySubject, participantDTO, null, null,tenantStudy);
                    logger.info("Participate with user_id: {} ,it's user_status: {} is updated in study_subject table: ", studySubject.getUserId(), studySubject.getUserStatus());

            }
        } else {
            logger.info("Participant does not exists or not added yet in OC ");
        }
        if (participantDTO.isInviteParticipant()) {

            ParticipantAccessDTO accessDTO= getAccessInfo(accessToken,studyOid,ssid,customerUuid);

            sendEmailToParticipant(studySubject,tenantStudy, accessDTO);
            studySubject = saveOrUpdateStudySubject(studySubject, participantDTO, UserStatus.INVITED, null,tenantStudy);

        }
            ocUserDTO = buildOcUserDTO(studySubject);

        return ocUserDTO;
    }

    private StudySubject saveOrUpdateStudySubject(StudySubject studySubject,OCParticipantDTO participantDTO,
                                                  UserStatus userStatus, Integer userId,Study tenantStudy){

        if (userId != null){
            studySubject.setUserId(userId);
        }

        if (userStatus != null){
            studySubject.setUserStatus(userStatus);
        }

        if (studySubject.getStudySubjectDetail() == null){
            StudySubjectDetail studySubjectDetail = new StudySubjectDetail();
            studySubject.setStudySubjectDetail(studySubjectDetail);
        }
        studySubject.getStudySubjectDetail().setFirstName(participantDTO.getFirstName() == null ? "" : participantDTO.getFirstName());
        studySubject.getStudySubjectDetail().setFirstNameForSearchUse(participantDTO.getFirstName() == null ? "" : participantDTO.getFirstName().toLowerCase());

         if( isParticipateActive(tenantStudy)) {
             studySubject.getStudySubjectDetail().setEmail(participantDTO.getEmail() == null ? "" : participantDTO.getEmail());
             studySubject.getStudySubjectDetail().setPhone(participantDTO.getPhoneNumber() == null ? "" : participantDTO.getPhoneNumber());
         }

        if(advanceSearch.equalsIgnoreCase(ENABLED)) {
            studySubject.getStudySubjectDetail().setLastName(participantDTO.getLastName() == null ? "" : participantDTO.getLastName());
            studySubject.getStudySubjectDetail().setLastNameForSearchUse(participantDTO.getLastName() == null ? "" : participantDTO.getLastName().toLowerCase());

            studySubject.getStudySubjectDetail().setIdentifier(participantDTO.getIdentifier() == null ? "" : participantDTO.getIdentifier());
            studySubject.getStudySubjectDetail().setIdentifierForSearchUse(participantDTO.getIdentifier() == null ? "" : participantDTO.getIdentifier().toLowerCase());
        }
        return studySubjectDao.saveOrUpdate(studySubject);

    }


    public OCUserDTO getParticipantAccount(String studyOid, String ssid, String accessToken) {

        OCUserDTO ocUserDTO = null;

        Study study = getStudy(studyOid);
        StudySubject studySubject = getStudySubject(ssid, study);

        if (studySubject!= null && studySubject.getUserId() != null) {
                ocUserDTO = buildOcUserDTO( studySubject);
        }
        return ocUserDTO;
    }



       public List<OCUserDTO> searchParticipantsByFields(String studyOid, String accessToken,String participantId,String firstName,String lastName,String identifier,UserAccountBean userAccountBean){
           Study study = studyDao.findByOcOID(studyOid);
           if(!advanceSearch.equalsIgnoreCase(ENABLED)){
               return null;
           }


      String  firstNameForSearchUse= cryptoConverter.convertToDatabaseColumn(firstName==null ? null: firstName.toLowerCase());
      String lastNameForSearchUse= cryptoConverter.convertToDatabaseColumn(lastName==null ? null: lastName.toLowerCase());
      String identifierForSearchUse= cryptoConverter.convertToDatabaseColumn(identifier==null ? null: identifier.toLowerCase());

        List<OCUserDTO> userDTOS = new ArrayList<>();
        List<StudySubject> studySubjects =studySubjectDao.findByParticipantIdFirstNameLastNameIdentifier(study,participantId,firstNameForSearchUse,lastNameForSearchUse,identifierForSearchUse);

        for(StudySubject studySubject:studySubjects){
            OCUserDTO userDTO = new OCUserDTO();
            StudySubjectDetail studySubjectDetail = studySubject.getStudySubjectDetail();
            userDTO.setFirstName(studySubjectDetail!=null?studySubjectDetail.getFirstName():"");
            userDTO.setLastName(studySubjectDetail!=null?studySubjectDetail.getLastName():"");
            userDTO.setEmail(studySubjectDetail!=null?studySubjectDetail.getEmail():"");
            userDTO.setPhoneNumber(studySubjectDetail!=null?studySubjectDetail.getPhone():"");
            userDTO.setIdentifier(studySubjectDetail!=null?studySubjectDetail.getIdentifier():"");
            userDTO.setParticipantId(studySubject.getLabel());
         userDTOS.add(userDTO);
        }



        return userDTOS;
   }



    private UserAccount createUserAccount(OCParticipantDTO participantDTO, StudySubject studySubject,UserAccountBean ownerUserAccountBean,String username ,Study publicStudy,String keycloakUserId) {
        if (participantDTO == null)
            return null;
       UserAccount userAccount = new UserAccount();
        userAccount.setUserType(new org.akaza.openclinica.domain.user.UserType(4));
        userAccount.setUserName(username);
        userAccount.setActiveStudy(publicStudy);
        userAccount.setStatus(Status.AVAILABLE);
        userAccount.setDateCreated(new Date());
        userAccount.setUserUuid(keycloakUserId);

        UserAccount ownerUserAccount=userAccountDao.findByUserId(ownerUserAccountBean.getId());
        userAccount.setUserAccount(ownerUserAccount);
        StudyUserRole studyUserRole = buildStudyUserRole(username,publicStudy,ownerUserAccount.getUserId());

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

    private void sendEmailToParticipant(StudySubject studySubject, Study tenantStudy,ParticipantAccessDTO accessDTO) {
        ParticipantDTO pDTO = new ParticipantDTO();
        pDTO.setEmailAccount(studySubject.getStudySubjectDetail().getEmail());
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

        sb.append("<a href=\""+accessLink+"\">Click here to begin</a>");
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

        sb.append(studyName+" Team");

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
            helper.setText(pDTO.getMessage(),true);

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




    public ParticipantAccessDTO getAccessInfo( String accessToken,String studyOid, String ssid,String customerUuid) {
        Study tenantStudy = getStudy(studyOid);
        StudySubject studySubject = getStudySubject(ssid, tenantStudy);
        if(studySubject==null || studySubject.getUserId()==null) {
         logger.error("Participant account not found");
            return null;
        }
        UserAccount pUserAccount = userAccountDao.findByUserId(studySubject.getUserId());
        if(pUserAccount==null || pUserAccount.getUserUuid()==null) {
            logger.error("Participant account not found");
            return null;
        }
        String accessCode = keycloakClient.getAccessCode(accessToken,pUserAccount.getUserUuid(),customerUuid);

        if(accessCode==null) {
            logger.error(" Access code from Keycloack returned null ");
            return null;
        }
        if (tenantStudy.getStudy()!=null)
            tenantStudy = tenantStudy.getStudy();

        List<ModuleConfigDTO> moduleConfigDTOs = studyBuildService.getParticipateModuleFromStudyService(accessToken, tenantStudy);
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


    private OCUserDTO buildOcUserDTO( StudySubject studySubject) {
        OCUserDTO ocUserDTO = new OCUserDTO();
        ocUserDTO.setEmail(studySubject.getStudySubjectDetail().getEmail());
        ocUserDTO.setFirstName(studySubject.getStudySubjectDetail().getFirstName());
        ocUserDTO.setLastName(studySubject.getStudySubjectDetail().getLastName());
        ocUserDTO.setPhoneNumber(studySubject.getStudySubjectDetail().getPhone());
        ocUserDTO.setIdentifier(studySubject.getStudySubjectDetail().getIdentifier());
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


    public boolean isParticipateActive(Study tenantStudy) {
               StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
               String participateFormStatus = spvdao.findByHandleAndStudy(tenantStudy.getStudy() != null ? tenantStudy.getStudy().getStudyId() : tenantStudy.getStudyId(), "participantPortal").getValue();
                if (participateFormStatus.equals(ENABLED))
                        return true;
               return false;
            }

}