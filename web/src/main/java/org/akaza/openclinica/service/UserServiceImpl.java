package org.akaza.openclinica.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.login.UserDTO;
import org.akaza.openclinica.bean.managestudy.*;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.FormLayoutBean;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.EventCrfDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.FormLayoutDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.patterns.ocobserver.StudyEventChangeDetails;
import org.akaza.openclinica.patterns.ocobserver.StudyEventContainer;
import org.akaza.openclinica.web.pform.PFormCache;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.cdisc.ns.odm.v130.*;
import org.openclinica.ns.odm_ext_v130.v31.OCodmComplexTypeDefinitionLink;
import org.openclinica.ns.odm_ext_v130.v31.OCodmComplexTypeDefinitionLinks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.spring.web.json.Json;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Comparator;

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
            if (studySubject.getParticipateUuid() == null) {
                // create participant user Account   POST
                object = createOrUpdateParticipantAccount(request, ocUserDTO, HttpMethod.POST);
                if (object instanceof OCUserDTO && object != null) {
                    studySubject.setParticipateUuid(((OCUserDTO) object).getUuid());
                    studySubjectDao.saveOrUpdate(studySubject);
                }
            } else {
                // update participant user Account  PUT
                ocUserDTO.setUuid(studySubject.getParticipateUuid());
                // Get participant
                Object getParticipantObject = getParticipantAccount(request, ocUserDTO, HttpMethod.GET);
                if (getParticipantObject instanceof OCUserDTO) {
                    ocUserDTO.setStatus(((OCUserDTO) getParticipantObject).getStatus());
                }
                object = createOrUpdateParticipantAccount(request, ocUserDTO, HttpMethod.PUT);
            }

        } else {
            logger.info("Participant does not exists or not added yet in OC ");
        }
        return object;
    }


    private Object createOrUpdateParticipantAccount(HttpServletRequest request, OCUserDTO ocUserDTO, HttpMethod
            httpMethod) {
        String createUserUri = CoreResources.getField("SBSUrl");
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
            userResponse = restTemplate.exchange(createUserUri, httpMethod, entity, OCUserDTO.class);
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
        ocUserDTO.setEmail(participantDTO.getEmail());
        ocUserDTO.setFirstName(participantDTO.getFirstName());
        ocUserDTO.setUserType(UserType.USER);
        ocUserDTO.setUsername(ssid);
        ocUserDTO.setLastName("ParticipateAccount");
        ocUserDTO.setStatus(UserStatus.INVITED);

        return ocUserDTO;
    }

    private Object getParticipantAccount(HttpServletRequest request, OCUserDTO ocUserDTO, HttpMethod
            httpMethod) {
        String getUserUri = CoreResources.getField("SBSUrl") + "/" + ocUserDTO.getUuid();
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
            userResponse = restTemplate.exchange(getUserUri, httpMethod, entity, OCUserDTO.class);
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

}