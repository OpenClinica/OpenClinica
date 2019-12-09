package core.org.akaza.openclinica.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.controller.dto.ModuleConfigAttributeDTO;
import org.akaza.openclinica.controller.dto.ModuleConfigDTO;
import org.akaza.openclinica.controller.dto.StudyEnvironmentDTO;
import org.apache.commons.lang.StringUtils;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.*;

@Service("notificationService")
@Transactional
public class NotificationService  {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public static final String ACCESS_LINK_PART_URL = "?accessCode=";
    public static final String PARTICIPATE = "participate";
    private static final String ACCESS_CODE_ATTRIBUTE = "accessCode";
    String DB_CONNECTION_KEY = "dbConnection";
    private String sbsUrl = CoreResources.getField("SBSBaseUrl");
    @Autowired
    private Keycloak keycloak;
    DataSource ds;

    public NotificationService() {
    }
    public NotificationService(DataSource ds) {
        this.ds = ds;
    }

    public ParticipantAccessDTO getAccessInfo(String accessToken, Study studyBean, StudySubject studySubject, String userUuid) {
        String accessCode = getAccessCode(accessToken,userUuid);

        if(accessCode==null) {
            logger.error(" Access code from Keycloack returned null ");
            return null;
        }

        List<ModuleConfigDTO> moduleConfigDTOs = getParticipateModuleFromStudyService(accessToken, studyBean);
        if (moduleConfigDTOs != null && moduleConfigDTOs.size() != 0) {
            ModuleConfigDTO moduleConfigDTO = getModuleConfig(moduleConfigDTOs, studyBean);
            if (moduleConfigDTO != null) {
                ModuleConfigAttributeDTO moduleConfigAttributeDTO = getModuleConfigAttribute(moduleConfigDTO.getAttributes(), studyBean);
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



    public List<ModuleConfigDTO> getParticipateModuleFromStudyService(String accessToken, Study studyBean) {
        if (StringUtils.isEmpty(studyBean.getStudyUuid())) {
            // make call to study service to get study uuid
            StudyEnvironmentDTO studyEnvironmentDTO = getStudyUuidFromStudyService(accessToken, studyBean);
            studyBean.setStudyUuid(studyEnvironmentDTO.getStudyUuid());
        }

        String appendUrl = "/study-service/api/studies/" + studyBean.getStudyUuid() + "/module-configs";
        String uri =sbsUrl+ appendUrl;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Accept-Charset", "UTF-8");
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(objectMapper);
        converters.add(jsonConverter);
        restTemplate.setMessageConverters(converters);
        ResponseEntity<List<ModuleConfigDTO>> response = restTemplate.exchange(uri, HttpMethod.GET, entity, new ParameterizedTypeReference<List<ModuleConfigDTO>>() {
        });
        if (response == null)
            return null;

        return response.getBody();
    }

    public StudyEnvironmentDTO getStudyUuidFromStudyService(String accessToken, Study studyBean) {

        String appendUrl = "/study-service/api/study-environments/" + studyBean.getStudyEnvUuid();
        String uri = sbsUrl + appendUrl;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Accept-Charset", "UTF-8");
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(objectMapper);
        converters.add(jsonConverter);
        restTemplate.setMessageConverters(converters);
        ResponseEntity<StudyEnvironmentDTO> response = restTemplate.exchange(uri, HttpMethod.GET, entity, StudyEnvironmentDTO.class);
        return response.getBody();
    }

    public ModuleConfigDTO getModuleConfig(List<ModuleConfigDTO> moduleConfigDTOs, Study studyBean) {
        for (ModuleConfigDTO moduleConfigDTO : moduleConfigDTOs) {
            if (moduleConfigDTO.getStudyUuid().equals(studyBean.getStudyUuid()) && moduleConfigDTO.getModuleName().equalsIgnoreCase(PARTICIPATE)) {
                logger.info("ModuleConfigDTO  is :" + moduleConfigDTO);
                return moduleConfigDTO;
            }
        }
        logger.info("ModuleConfigDTO  is null");
        return null;
    }
    public ModuleConfigAttributeDTO getModuleConfigAttribute(Set<ModuleConfigAttributeDTO> moduleConfigAttributeDTOs, Study studyBean) {
        for (ModuleConfigAttributeDTO moduleConfigAttributeDTO : moduleConfigAttributeDTOs) {
            if (moduleConfigAttributeDTO.getStudyEnvironmentUuid().equals(studyBean.getStudyEnvUuid())) {
                logger.info("ModuleConfigAttributeDTO  is :" + moduleConfigAttributeDTO);
                return moduleConfigAttributeDTO;
            }
        }
        logger.info("ModuleConfigAttributeDTO  is null");
        return null;
    }

    public String getAccessCode(String accessToken, String userUuid ) {
        logger.debug("Calling Keycloak to get participate UserPresentation object");
        String realm = getRealmName(accessToken);
        UserResource userResource = keycloak
                .realm(realm)
                .users()
                .get(userUuid);

        UserRepresentation userRepresentation = userResource.toRepresentation();
        Map<String, List<String>> attributes =  userRepresentation.getAttributes();
        List<String> accessCodes = attributes.get(ACCESS_CODE_ATTRIBUTE);
        return accessCodes.get(0);
    }


    public String getRealmName(String accessToken) {
        int index = sbsUrl.indexOf("//");
        String subDomain= sbsUrl.substring(index+2,sbsUrl.indexOf(".build"));
        String subDomainUrl="/customer-service/api/allowed-connections?subdomain="+subDomain;
        String uri =sbsUrl+subDomainUrl;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Accept-Charset", "UTF-8");
        Study studyBean = null;
        HttpEntity entity = new HttpEntity<OCUserDTO>(headers);
        ResponseEntity<List<String>> response = null;

        try {
            response = restTemplate.exchange(uri, HttpMethod.GET, entity, new ParameterizedTypeReference<List<String>>(){});
        } catch (HttpClientErrorException e) {
            logger.error("KeyCloak error message: {}", e.getResponseBodyAsString());
        }

        if (response == null) {
            return null;
        } else {
            return  response.getBody().get(0);
        }

    }


}
