package core.org.akaza.openclinica.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.web.rest.client.auth.impl.KeycloakClientImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@Service("permissionService")
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
public class PermissionServiceImpl implements PermissionService {
    @Autowired
    private EventDefinitionCrfDao eventDefinitionCrfDao;
    @Autowired
    private EventDefinitionCrfPermissionTagDao permissionTagDao;
    @Autowired
    private StudyEventDao studyEventDao;
    @Autowired
    private FormLayoutDao formLayoutDao;
    @Autowired
    private EventCrfDao eventCrfDao;
    @Autowired
    private StudyDao studyDao;
    @Autowired
    private StudyEventDefinitionDao studyEventDefinitionDao;
    @Autowired
    private CrfDao crfDao;
    @Autowired
    private KeycloakClientImpl keycloakClient;

    private static final String CREATE_TOKEN_API_PATH = "/oauth/token";


    private boolean checkStudyUuid(String studyUuid, int parentStudyId) {
        if (parentStudyId == 0) return false;
        Study study = studyDao.findById(parentStudyId);
        if (StringUtils.equals(study.getStudyEnvUuid(), studyUuid))
            return true;
        return false;
    }

    public List<String> getPermissionTagsList(HttpServletRequest request) {
        HttpSession session = request.getSession();
        ResponseEntity<List<StudyEnvironmentRoleDTO>> roles = getUserRoles(request);
        Study study = (Study) session.getAttribute("study");
        return getTagList(roles, study);
    }

    private List<String> getTagList(ResponseEntity<List<StudyEnvironmentRoleDTO>> roles, Study study) {
        String tmpUuid = null;
        if (StringUtils.isNotEmpty(study.getStudyEnvUuid()))
            tmpUuid = study.getStudyEnvUuid();
        else if ((StringUtils.isNotEmpty(study.getStudyEnvSiteUuid())))
            tmpUuid = study.getStudyEnvSiteUuid();

        final String uuId = tmpUuid;
        if (StringUtils.isEmpty(uuId)) {
            logger.error("***********Uuid should not be empty:");
        }
        Optional<StudyEnvironmentRoleDTO> dto =
                roles.getBody().stream().filter(o -> (StringUtils.equals(o.getStudyEnvironmentUuid(), uuId) ||
                        (StringUtils.isNotEmpty(study.getStudyEnvSiteUuid()) && checkStudyUuid(o.getStudyEnvironmentUuid(), study.getStudy().getStudyId())))).findFirst();

        if (!dto.isPresent()) {
            logger.error("Study:" + uuId + " not found for this user");
            return new ArrayList<>();
        }

        logger.debug("Response: getUserRoles:" + dto);
        if (CollectionUtils.isEmpty(dto.get().getPermissions())) {
            return new ArrayList<>();
        }
        List<String> tagIds = dto.get().getPermissions().stream().map(PermissionDTO::getTagId).collect(Collectors.toList());
        return tagIds;
    }


    public ResponseEntity<List<StudyEnvironmentRoleDTO>> getUserRoles(HttpServletRequest request) {
        Map<String, Object> userContextMap = (LinkedHashMap<String, Object>) request.getSession().getAttribute("userContextMap");
        if (userContextMap == null)
            return null;
        String userUuid = (String) userContextMap.get("userUuid");
        String uri = CoreResources.getField("SBSBaseUrl") + "/user-service/api/users/" + userUuid + "/roles";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String accessToken = (String) request.getSession().getAttribute("accessToken");
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Accept-Charset", "UTF-8");
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(objectMapper);
        converters.add(jsonConverter);
        restTemplate.setMessageConverters(converters);
        ResponseEntity<List<StudyEnvironmentRoleDTO>> response = restTemplate.exchange(uri, HttpMethod.GET, entity, new ParameterizedTypeReference<List<StudyEnvironmentRoleDTO>>() {
        });
        logger.debug("Response: getUserRoles:" + response);
        if (logger.isDebugEnabled()) {
            for (StudyEnvironmentRoleDTO userRole : response.getBody()) {
                logger.debug("UserRole in updateStudyUserRoles: role: " + userRole.getRoleName() + " uuid:" + userRole.getUuid());
            }
        }
        return response;
    }

    public ResponseEntity<List<StudyEnvironmentRoleDTO>> getUserRoles(String userUuid, String accessToken) {
        String uri = CoreResources.getField("SBSBaseUrl") + "/user-service/api/users/" + userUuid + "/roles";
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
        ResponseEntity<List<StudyEnvironmentRoleDTO>> response = restTemplate.exchange(uri, HttpMethod.GET, entity, new ParameterizedTypeReference<List<StudyEnvironmentRoleDTO>>() {
        });
        logger.debug("Response: getUserRoles:" + response);
        if (logger.isDebugEnabled()) {
            for (StudyEnvironmentRoleDTO userRole : response.getBody()) {
                logger.debug("UserRole in updateStudyUserRoles: role: " + userRole.getRoleName() + " uuid:" + userRole.getUuid());
            }
        }
        return response;
    }

    public ResponseEntity<List<StudyEnvironmentRoleDTO>> getUserRoles(String userUuid) {
        String accessToken = keycloakClient.getSystemToken();
        return getUserRoles(userUuid, accessToken);
    }

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public String getPermissionTagsString(HttpServletRequest request) {
        List<String> tagsList = getPermissionTagsList(request);
        return getTagsString(tagsList);
    }

    private String getTagsString(List<String> tagsList) {
        if (CollectionUtils.isEmpty(tagsList))
            return "";
        String tags = tagsList.stream().collect(Collectors.joining("','", "'", "'"));
        return tags;
    }

    public String[] getPermissionTagsStringArray(HttpServletRequest request) {
        List<String> tagsList = getPermissionTagsList(request);
        return getStringArray(tagsList);
    }

    private String[] getStringArray(List<String> tagsList) {
        if (CollectionUtils.isEmpty(tagsList))
            return null;
        String[] tags = tagsList.toArray(new String[tagsList.size()]);
        return tags;
    }

    public boolean hasFormAccess(EventCrf ec, Integer formLayoutId, Integer studyEventId, HttpServletRequest request) {

        Study currentStudy = (Study) request.getSession().getAttribute("study");
        EventDefinitionCrf eventDefCrf = null;
        final EventCrf eventCrf = ec;
        int studyId = currentStudy.getStudyId();
        if (currentStudy.getStudy() != null && currentStudy.getStudy().getStudyId() != 0) {
            studyId = currentStudy.getStudy().getStudyId();
        }
        if (ec == null) {
            if (formLayoutId != null && studyEventId != null) {
                StudyEvent studyEvent = studyEventDao.findById(studyEventId);
                // if we don't have studyEvent and EventCrf is null then this is a view request, so return true
                if (studyEvent == null)
                    return true;
                FormLayout formLayout = formLayoutDao.findById(formLayoutId);

                if (studyEvent != null && formLayout != null) {
                    ec = eventCrfDao.findByStudyEventIdStudySubjectIdFormLayoutId(studyEventId, studyEvent.getStudySubject().getStudySubjectId(), formLayoutId);
                    if (ec == null || eventDefCrf == null) {
                        eventDefCrf = eventDefinitionCrfDao.findByStudyEventDefinitionIdAndCRFIdAndStudyId(
                                studyEvent.getStudyEventDefinition().getStudyEventDefinitionId(),
                                formLayout.getCrf().getCrfId(), studyId);
                    }
                }
            }
        } else {
            eventDefCrf = eventDefinitionCrfDao.findByStudyEventDefinitionIdAndCRFIdAndStudyId(
                    eventCrf.getStudyEvent().getStudyEventDefinition().getStudyEventDefinitionId(),
                    eventCrf.getCrfVersion().getCrf().getCrfId(), studyId);
        }

        if (eventDefCrf == null) {
            logger.error("EventDefCrf should not be null");
            return false;
        }

        List<String> permissionTagsList = getPermissionTagsList(request);

        return this.hasFormAccess(eventDefCrf, permissionTagsList);

    }

    public boolean hasFormAccess(EventDefinitionCrf edc, List<String> permissionTagsList) {
        List<String> tagsForEDC = permissionTagDao.findTagsForEDC(edc);
        if (CollectionUtils.isEmpty(tagsForEDC))
            return true;
        if (CollectionUtils.isNotEmpty(tagsForEDC) && CollectionUtils.isEmpty(permissionTagsList))
            return false;
        List<String> list = tagsForEDC.stream().filter(permissionTagsList::contains).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list))
            return false;

        return true;
    }

    public List<String> getPermissionTagsList(Study study, HttpServletRequest request) {
        ResponseEntity<List<StudyEnvironmentRoleDTO>> roles = getUserRoles(request);
        return getTagList(roles, study);
    }

    public List<String> getPermissionTagsList(Study study, String userUuid) {
        ResponseEntity<List<StudyEnvironmentRoleDTO>> roles = getUserRoles(userUuid);
        return getTagList(roles, study);
    }


    public boolean isUserHasPermission(String column, HttpServletRequest request, Study studyBean) {
        String sedOid = column.split("\\.")[0];
        String formOid = column.split("\\.")[1];
        StudyEventDefinition sed = studyEventDefinitionDao.findByOcOID(sedOid);
        CrfBean crf = crfDao.findByOcOID(formOid);
        int studyId;
        if (studyBean.isSite())
            studyId = studyBean.getStudy().getStudyId();
        else
            studyId = studyBean.getStudyId();
        EventDefinitionCrf eventDefCrf = eventDefinitionCrfDao.findByStudyEventDefinitionIdAndCRFIdAndStudyId(sed.getStudyEventDefinitionId(), crf.getCrfId(), studyId);
        List<String> tagsInEDC = permissionTagDao.findTagsForEDC(eventDefCrf);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(tagsInEDC)) {
            List<String> list = tagsInEDC.stream().filter(getPermissionTags(request)::contains).collect(Collectors.toList());
            if (org.apache.commons.collections.CollectionUtils.isEmpty(list))
                return false;
        }
        return true;
    }

    private List<String> getPermissionTags(HttpServletRequest request) {
        return (List<String>) request.getSession().getAttribute("userPermissionTags");
    }

    public String getPermissionTagsString(Study study, HttpServletRequest request) {
        List<String> tagsList = getPermissionTagsList(study, request);
        return getTagsString(tagsList);
    }

    public String getPermissionTagsString(Study study, String userUuid) {
        List<String> tagsList = getPermissionTagsList(study, userUuid);
        return getTagsString(tagsList);
    }

    public String[] getPermissionTagsStringArray(Study study, HttpServletRequest request) {
        List<String> tagsList = getPermissionTagsList(study, request);
        return getStringArray(tagsList);
    }

    public String[] getPermissionTagsStringArray(Study study, String userUuid) {
        List<String> tagsList = getPermissionTagsList(study, userUuid);
        return getStringArray(tagsList);
    }
}