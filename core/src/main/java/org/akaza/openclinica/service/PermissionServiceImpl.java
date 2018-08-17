package org.akaza.openclinica.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.*;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.domain.datamap.FormLayout;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.apache.commons.collections4.CollectionUtils;
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
@Transactional(propagation= Propagation.REQUIRED,isolation= Isolation.DEFAULT)
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

    public List<String> getPermissionTagsList(HttpServletRequest request) {
        HttpSession session = request.getSession();
        ResponseEntity<List<StudyEnvironmentRoleDTO>> roles = getUserRoles(request);
        StudyBean publicStudy = (StudyBean) session.getAttribute("publicStudy");
        String studyEnvironmentUuid = publicStudy.getStudyEnvUuid();

        Optional<StudyEnvironmentRoleDTO> dto =
                roles.getBody().stream().filter(o -> o.getStudyEnvironmentUuid().equals(studyEnvironmentUuid)).findFirst();

        if (!dto.isPresent()) {
            logger.error("Study:" + studyEnvironmentUuid + " not found for this user");
            return new ArrayList<>();
        }

        logger.debug("Response: getUserRoles:" + dto);
        if (CollectionUtils.isEmpty(dto.get().getPermissions())) {
            return new ArrayList<>();
        }
        List<String> tagIds = dto.get().getPermissions().stream().map(PermissionDTO::getTagId).collect(Collectors.toList()).size()!=0 ?dto.get().getPermissions().stream().map(PermissionDTO::getTagId).collect(Collectors.toList()):new ArrayList<>();


        return tagIds;
    }


    public ResponseEntity<List<StudyEnvironmentRoleDTO>> getUserRoles(HttpServletRequest request) {
        Map<String, Object> userContextMap = (LinkedHashMap<String, Object>) request.getSession().getAttribute("userContextMap");
        if (userContextMap == null)
            return null;
        String userUuid = (String) userContextMap.get("userUuid");
        String uri = CoreResources.getField("SBSUrl") + userUuid + "/roles";
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
        ResponseEntity<List<StudyEnvironmentRoleDTO>> response = restTemplate.exchange(uri, HttpMethod.GET, entity, new ParameterizedTypeReference<List<StudyEnvironmentRoleDTO>>() {});
        logger.debug("Response: getUserRoles:" + response);
        if (logger.isDebugEnabled()) {
            for (StudyEnvironmentRoleDTO userRole: response.getBody()) {
                logger.debug("UserRole in updateStudyUserRoles: role: " + userRole.getRoleName() + " uuid:" + userRole.getUuid() );
            }
        }
        return response;
    }

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public String getPermissionTagsString(HttpServletRequest request) {
        List<String> tagsList = getPermissionTagsList(request);
        if (CollectionUtils.isEmpty(tagsList))
            return "";
        String tags = tagsList.stream().collect(Collectors.joining("','", "'", "'"));
        return tags;
    }

    public String[] getPermissionTagsStringArray(HttpServletRequest request) {
        List<String> tagsList = getPermissionTagsList(request);
        if (CollectionUtils.isEmpty(tagsList))
            return null;
        String[] tags = tagsList.toArray(new String[tagsList.size()]);
        return tags;
    }

    public boolean hasFormAccess(EventCrf ec, Integer formLayoutId, Integer studyEventId, HttpServletRequest request) {

        StudyBean currentStudy = (StudyBean) request.getSession().getAttribute("study");
        EventDefinitionCrf eventDefCrf = null;
        final EventCrf eventCrf = ec;
        if (ec == null) {
            StudyEvent studyEvent = studyEventDao.findById(studyEventId);
            FormLayout formLayout = formLayoutDao.findById(formLayoutId);

            if (studyEvent != null && formLayout != null) {
                ec = eventCrfDao.findByStudyEventIdStudySubjectIdFormLayoutId(studyEventId, studyEvent.getStudySubject().getStudySubjectId(), formLayoutId);
                if (ec == null) {
                    eventDefCrf = eventDefinitionCrfDao.findByStudyEventDefinitionIdAndCRFIdAndStudyId(
                            studyEvent.getStudyEventDefinition().getStudyEventDefinitionId(), formLayout.getCrf().getCrfId(), currentStudy.getId());
                }
            }
        } else {
            eventDefCrf = eventDefinitionCrfDao.findByStudyEventDefinitionIdAndCRFIdAndStudyId(
                    eventCrf.getStudyEvent().getStudyEventDefinition().getStudyEventDefinitionId(), eventCrf.getCrfVersion().getCrf().getCrfId(), currentStudy.getId());

        }

        List<String> permissionTagsList = getPermissionTagsList(request);
        List<String> tagsForEDC = permissionTagDao.findTagsForEDC(eventDefCrf);
        if (CollectionUtils.isEmpty(tagsForEDC))
            return true;
        if (CollectionUtils.isNotEmpty(tagsForEDC) && CollectionUtils.isEmpty(permissionTagsList))
            return false;
        List<String> list = tagsForEDC.stream().filter(permissionTagsList::contains).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list))
            return false;

        return true;
    }
}