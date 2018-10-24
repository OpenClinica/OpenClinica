package org.akaza.openclinica.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.akaza.openclinica.bean.managestudy.*;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.EventCrfDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.domain.datamap.*;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import org.apache.commons.lang.StringUtils;

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

    private RestfulServiceHelper restfulServiceHelper;

    public static final String FORM_CONTEXT = "ecid";
    public static final String DASH = "-";
    public static final String PARTICIPATE_EDIT = "participate-edit";
    public static final String PARTICIPATE_ADD_NEW = "participate-add-new";
    public static final String PAGINATION = "?page=0&size=1000";
    public static final String USERSERVICE = "user-service/api";
    public static final String STUDYENVIRONMENT = "study-environments";
    public static final String USERSWITHROLES = "users-with-roles";
    public static final String FORWARDSLASH = "/";
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
            ocUserDTO = buildOCUserDTO(ssid, participantDTO);
            if (studySubject.getUserUuid() == null) {
                // create participant user Account   POST
                object = createOrUpdateParticipantAccount(request, ocUserDTO, HttpMethod.POST);
                if (object instanceof OCUserDTO && object != null) {
                    studySubject.setUserUuid(((OCUserDTO) object).getUuid());
                    studySubjectDao.saveOrUpdate(studySubject);
                }
            } else {
                // update participant user Account  PUT
                ocUserDTO.setUuid(studySubject.getUserUuid());
                // Get participant
                List<OCUserRoleDTO> ocUserRoleDTOs = getParticipantsByStudyFromUserService(request ,studyOid);
                for(OCUserRoleDTO ocUserRoleDTO:ocUserRoleDTOs){
                    if(ocUserRoleDTO.getUserInfo().getUuid().equals(ocUserDTO.getUuid())){
                        ocUserDTO.setStatus(ocUserRoleDTO.getUserInfo().getStatus());
                        break;
                    }
                }


           //     if (object instanceof OCUserDTO) {
           //         ocUserDTO.setStatus(((OCUserDTO) object).getStatus());
           //     }
                object = createOrUpdateParticipantAccount(request, ocUserDTO, HttpMethod.PUT);
            }

        } else {
            logger.info("Participant does not exists or not added yet in OC ");
        }
        return object;
    }


    private Object createOrUpdateParticipantAccount(HttpServletRequest request, OCUserDTO ocUserDTO, HttpMethod
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
            return ocUserDTO = userResponse.getBody();
        }

    }


    private OCUserDTO buildOCUserDTO(String ssid, OCParticipantDTO participantDTO) {
        OCUserDTO ocUserDTO = new OCUserDTO();
        if(participantDTO!=null) {
            ocUserDTO.setEmail(participantDTO.getEmail());
            ocUserDTO.setFirstName(participantDTO.getFirstName());
        }
        ocUserDTO.setUserType(UserType.USER);
        ocUserDTO.setUsername(ssid);
        ocUserDTO.setLastName("ParticipateAccount");
        ocUserDTO.setStatus(UserStatus.INVITED);

        return ocUserDTO;
    }



    public Object getParticipantAccount(String studyOid, String ssid, OCParticipantDTO participantDTO, HttpServletRequest request) {
        Study study = getStudy(studyOid);
        StudySubject studySubject = getStudySubject(ssid, study);
        OCUserDTO ocUserDTO = null;
        Object object = null;

        if (studySubject != null) {
            ocUserDTO = buildOCUserDTO(ssid, participantDTO);
            if(studySubject.getUserUuid()!=null) {
                ocUserDTO.setUuid(studySubject.getUserUuid());
                object = getParticipantsByStudyFromUserService(request, studyOid);
            }else{
                logger.info("Participant has not been connected yet");
                logger.info("userUuid of participant in OC runtime is null");

            }
        } else {
            logger.info("Participant does not exists or not added yet in OC ");
        }
        return object;
    }


    public List<OCUserRoleDTO> getParticipantsByStudyFromUserService(HttpServletRequest request, String studyOid) {

        String uri = buildGetURI(studyOid);

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
        ResponseEntity<List<OCUserRoleDTO>> userResponse = null;
        try {
            userResponse =
                    restTemplate.exchange(uri, HttpMethod.GET, entity, new ParameterizedTypeReference<List<OCUserRoleDTO>>() {
                    });

        } catch (HttpClientErrorException e) {
            logger.error("Auth0 error message: {}", e.getResponseBodyAsString());
            return null;
        }

        if (userResponse == null) {
            return null;
        } else {
            return userResponse.getBody();
        }

    }

    private String buildGetURI(String studyOid) {
        Study study = studyDao.findByOcOID(studyOid);
        if(study.getStudy()!=null){
            study=studyDao.findById(study.getStudy().getStudyId());
        }
        String studyEnvironment = study.getStudyEnvUuid();


        String uri = sbsUrl.substring(0, sbsUrl.indexOf(USERSERVICE)+17) + STUDYENVIRONMENT + FORWARDSLASH + studyEnvironment + FORWARDSLASH + USERSWITHROLES;
        return uri;
    }
}