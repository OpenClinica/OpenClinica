package core.org.akaza.openclinica.service.randomize;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import core.org.akaza.openclinica.bean.service.StudyParameterValueBean;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import core.org.akaza.openclinica.domain.enumsupport.ModuleStatus;
import org.akaza.openclinica.config.StudyParamNames;
import org.akaza.openclinica.controller.dto.ModuleConfigAttributeDTO;
import org.akaza.openclinica.controller.dto.ModuleConfigDTO;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.RandomizeQueryResult;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.hibernate.StudyEventDao;
import core.org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import core.org.akaza.openclinica.domain.Status;
import core.org.akaza.openclinica.domain.datamap.ItemData;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.domain.enumsupport.EventCrfWorkflowStatusEnum;
import org.akaza.openclinica.dto.randomize.RandomizationConfiguration;
import org.akaza.openclinica.dto.randomize.RandomizationDTO;
import org.akaza.openclinica.dto.randomize.RandomizationTarget;
import core.org.akaza.openclinica.service.StudyBuildService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service("randomizationService")
public class RandomizationServiceImpl implements RandomizationService {
    private final Logger log = LoggerFactory.getLogger(RandomizationServiceImpl.class);
    private static String sbsUrl = CoreResources.getField("SBSBaseUrl");

    @Autowired
    private StudySubjectDao studySubjectDao;

    @Autowired
    private StudyEventDao studyEventDao;
    @Autowired
    StudyParameterValueDAO studyParameterValueDAO;

    @Autowired
    private StudyDao studyDao;

    @Autowired
    private StudyBuildService studyBuildService;

    private static final String SUCCESS_STR = "SUCCESS";

    private static String initServiceUrl() {
        String subDomainUrl = "/randomizeservice/api/";
        return sbsUrl+ subDomainUrl;
    };
    static String randomizeUrl = initServiceUrl();

    private static ConcurrentHashMap<String, RandomizationData> randomizationMap =
            new ConcurrentHashMap<>();

    private class RandomizationData {
        private boolean isEnabled;
        private List<RandomizationConfiguration> configurations;

        public RandomizationData(boolean isEnabled, List<RandomizationConfiguration> configurations) {
            this.isEnabled = isEnabled;
            this.configurations = configurations;
        }

        public RandomizationData(boolean isEnabled, RandomizationConfiguration configuration) {
            this.isEnabled = isEnabled;
            this.configurations = new ArrayList<>();
            configurations.add(configuration);
        }
        public boolean isEnabled() {
            return isEnabled;
        }

        public void setEnabled(boolean enabled) {
            isEnabled = enabled;
        }

        public List<RandomizationConfiguration> getConfigurations() {
            return configurations;
        }

        public void addConfiguration(RandomizationConfiguration configuration) {
            this.configurations.add(configuration);
        }
    }

    public boolean refreshConfigurations(String accessToken, Map<String, String> results) {
        boolean isSuccess = true;
        List<RandomizationConfiguration> randomizationConfigurations = new ArrayList<>();
        ArrayList<Study> studies = studyDao.findAll();
        studies.stream().filter(study->study.getStatus() == Status.AVAILABLE
                && StringUtils.isNotEmpty(study.getStudyEnvUuid()))
                .forEach(study-> {
                    boolean moduleEnabled = isModuleEnabled(study);
                    if (moduleEnabled) {
                        boolean isRetrieveSuccess = false;
                        try {
                            randomizationConfigurations.addAll(retrieveConfigurations(study.getStudyEnvUuid(), accessToken));
                            isRetrieveSuccess = true;
                        } catch (Exception e) {
                            // we just log the exception here since we don't want to prevent user login by throwing this exception
                            log.error("Error getting Randomize configuration for studyEnvUuid:" + study.getStudyEnvUuid());
                            log.error("Exception:" + e);
                            results.put(study.getOc_oid(), "FAILED. Error:" + e.getMessage());
                        }
                        if (isRetrieveSuccess)
                            results.put(study.getOc_oid(), SUCCESS_STR);
                    }

                });

        // if the study config is not in the existing map, then remove it
        // Don't need to do anything crazy with thread safety here as this end point is very restricted and not multi-tenant
        // Need to cycle through and clean out everything for this study event UUID
        //TODO Is there really a reason to do this and not just wipe everything out?
/*        randomizationMap.entrySet().stream().forEach(entry->{
            if (results.get(entry.getValue().getConfiguration().getStudyOID()) == null) {
                randomizationMap.remove(entry.getKey());
            }
        });*/
        randomizationMap = new ConcurrentHashMap<>();

        // Build a new key? Instead of just studyEnvUuid
        // Do? studyEnvUuid-eventOid-formOid
        // Issue with this is how do we remove existing ones where the eventOid has changed?
        randomizationConfigurations.stream().forEach(config->{
            if (randomizationMap.containsKey(config.getStudyEnvUuid())){
                // Add to existing
                randomizationMap.get(config.getStudyEnvUuid()).addConfiguration(config);
            } else {
                // Create new
                randomizationMap.put(config.getStudyEnvUuid(), new RandomizationData(true, config));
            }
        });
        isSuccess = results.size() == randomizationConfigurations.size() ? true: false;
        return isSuccess;
    }

    //TODO We should do away with this method and check the study parameter values table when we move the full randomize configuration to study service.
    private boolean isModuleEnabled(Study study) {

        StudyParameterValueBean randomizeStudyParameterValue = studyParameterValueDAO.findByHandleAndStudy(study.getStudyId(), StudyParamNames.RANDOMIZATION);
        if (randomizeStudyParameterValue.isActive()) {
            if (ModuleStatus.valueOf(randomizeStudyParameterValue.getValue()).equals(ModuleStatus.ACTIVE)) {
                return true;
            }
        }
        return false;
    }

    private List<RandomizationConfiguration> retrieveConfigurations(String studyEnvUuid, String accessToken) {
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
        ResponseEntity<List<ModuleConfigDTO>> response = restTemplate.exchange(randomizeUrl + "study-environments/" + studyEnvUuid
                + "/configurations", HttpMethod.GET, entity, new ParameterizedTypeReference<List<ModuleConfigDTO>>() {});
        if (response.hasBody())
            return mapModuleConfigsToConfigs(response.getBody());
        return null;
    }

    private List<RandomizationConfiguration> mapModuleConfigsToConfigs(List<ModuleConfigDTO> moduleConfigs){
        List<RandomizationConfiguration> randomizationConfigurations = new ArrayList<>();
        for (ModuleConfigDTO moduleConfig : moduleConfigs){
            randomizationConfigurations.add(mapModuleConfigToConfig(moduleConfig));
        }
        return randomizationConfigurations;
    }

    private RandomizationConfiguration mapModuleConfigToConfig(ModuleConfigDTO moduleConfig){
        RandomizationConfiguration randomizationConfiguration = new RandomizationConfiguration();

        randomizationConfiguration.setStudyUuid(moduleConfig.getStudyUuid());

        boolean firstEntry = true;
        for (ModuleConfigAttributeDTO attribute : moduleConfig.getAttributes()){
            if (firstEntry == true){
                randomizationConfiguration.setStudyEnvUuid(attribute.getStudyEnvironmentUuid());
                firstEntry = false;
            }
            if (attribute.getKey().contains("stratificationFactor")){
                randomizationConfiguration.addFactor(attribute.getKey(), attribute.getValue());
            }
            else {
                randomizationConfiguration.addAttribute(attribute.getKey(), attribute.getValue());
            }
        }

        HashMap<String, String> attributes = randomizationConfiguration.getAttributes();
        randomizationConfiguration.setRuntimeURL(attributes.get(RandomizationConfigurationFile.RUNTIME_URL));
        randomizationConfiguration.setManagerURL(attributes.get(RandomizationConfigurationFile.MANAGER_URL));
        randomizationConfiguration.setStudyOID(attributes.get(RandomizationConfigurationFile.STUDY_OID));
        randomizationConfiguration.targetField.setEventOID(attributes.get(RandomizationTarget.TARGET_EVENT));
        randomizationConfiguration.targetField.setFormOID(attributes.get(RandomizationTarget.TARGET_FORM));
        randomizationConfiguration.targetField.setFormLayoutID(attributes.get(RandomizationTarget.TARGET_VERSION));
        randomizationConfiguration.targetField.setItemGroupOID(attributes.get(RandomizationTarget.TARGET_ITEM_GROUP));
        randomizationConfiguration.targetField.setItemOID(attributes.get(RandomizationTarget.TARGET_ITEM));

        log.info("Config: " + randomizationConfiguration.toString());

        return randomizationConfiguration;
    }

    public boolean isEnabled(String studyEnvUuid) {
        RandomizationData data = randomizationMap.get(studyEnvUuid);
        if (data == null)
            return false;
        return data.isEnabled();
    }

    @Override
    public void processModuleConfig(List<ModuleConfigDTO> moduleConfigDTOs, Study study) {
        Optional<ModuleConfigDTO> randomizeModuleConfig = moduleConfigDTOs.stream().findFirst();

        //TODO At some point we should load the randomizationMap here.
        if (randomizeModuleConfig.isPresent()){
            updateRandomizeStatus(randomizeModuleConfig.get(), study);
        }
    }

    private void updateRandomizeStatus(ModuleConfigDTO randomizeModuleConfig, Study study) {

        StudyParameterValueBean randomizeStudyParameterValue = studyParameterValueDAO.findByHandleAndStudy(study.getStudyId(), StudyParamNames.RANDOMIZATION);
        String moduleStatus = randomizeModuleConfig.getStatus().name();

        if (!randomizeStudyParameterValue.isActive()) {
            randomizeStudyParameterValue = new StudyParameterValueBean();
            randomizeStudyParameterValue.setStudyId(study.getStudyId());
            randomizeStudyParameterValue.setParameter(StudyParamNames.RANDOMIZATION);
            randomizeStudyParameterValue.setValue(moduleStatus);
            studyParameterValueDAO.create(randomizeStudyParameterValue);
        } else if (randomizeStudyParameterValue.isActive() && !randomizeStudyParameterValue.getValue().equals(moduleStatus)) {
            randomizeStudyParameterValue.setValue(moduleStatus);
            studyParameterValueDAO.update(randomizeStudyParameterValue);
        }
    }

    private boolean isItemPartOfStratFactors(List<List<String>> stratGroups, ItemData thisItemData) {
        boolean result = false;
        String currentEventOid = thisItemData.getEventCrf().getStudyEvent().getStudyEventDefinition().getOc_oid();
        String currentItemOid = thisItemData.getItem().getOcOid();
        List<String> eventOids = stratGroups.get(0);
        List<String>itemOids = stratGroups.get(3);
        int index = 0;
        for (String eventOid : eventOids) {
            if (StringUtils.equals(currentEventOid, eventOid)) {
                if (StringUtils.equals(currentItemOid, itemOids.get(index)))
                    return true;
            }
            ++index;
        }
        return result;
    }

    private void sendStratificationFactors(RandomizationDTO randomizationDTO, String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Accept-Charset", "UTF-8");
        HttpEntity<RandomizationDTO> entity = new HttpEntity<>(randomizationDTO, headers);
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(objectMapper);
        converters.add(jsonConverter);
        restTemplate.setMessageConverters(converters);
        ResponseEntity<RandomizationDTO> response = restTemplate.exchange(randomizeUrl + "randomizations", HttpMethod.POST, entity, RandomizationDTO.class);
        if (response.hasBody()) {
            log.debug("Response from sendStratificationFactors:" + response.getBody());
        }
        log.debug("Response status:" + response.getStatusCode());
    }

    private void setStratFactors(Study publicStudy, RandomizationConfiguration studyConfig, String studySubjectOID,
                                List<RandomizeQueryResult> randomizeQueryResult, String accessToken, boolean isMultipleConfigurations) {

        RandomizationDTO randomizationDTO = new RandomizationDTO();
        randomizationDTO.setStudyUuid(publicStudy.getStudyUuid());
        randomizationDTO.setStudyEnvironmentUuid(publicStudy.getStudyEnvUuid());
        randomizationDTO.setSubjectOid(studySubjectOID);

        if (isMultipleConfigurations){
            randomizationDTO.setStudyEventOid(studyConfig.targetField.getEventOID());
            randomizationDTO.setFormOid(studyConfig.targetField.getFormOID());
        }

        Map<String, String> databaseValues = randomizeQueryResult.stream()
                .collect(Collectors.toMap(entry -> (entry.getStudyEvent().getStudyEventDefinition().getOc_oid() + "."
                                + entry.getEventCrf().getFormLayout().getCrf().getOcOid() + "."
                                + entry.getItemGroup().getOcOid() + "."
                                + entry.getItemData().getItem().getOcOid()),
                        entry -> entry.getItemData().getValue(), (existing, replacement) -> existing));

        Map<String, String> stratFactors = studyConfig.getStratificationFactors().entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> StringUtils.substringAfter(entry.getKey(), STRATIFICATION_FACTOR + "."),
                        entry -> databaseValues.get(entry.getValue())));

        StudySubject studySubject = studySubjectDao.findByOcOID(studySubjectOID);
        stratFactors.put("studyOid", studySubject.getStudy().getOc_oid());
        stratFactors.put("siteId", studySubject.getStudy().getUniqueIdentifier());
        stratFactors.put("siteName", studySubject.getStudy().getName());

        randomizationDTO.setStratificationFactors(stratFactors);
        sendStratificationFactors(randomizationDTO, accessToken);
    }

    public void processRandomization(Study parentPublicStudy, String accessToken, String studySubjectOID, ItemData... optionalItemData) {

        ItemData itemData = null;
        if (optionalItemData.length > 0) {
            itemData = optionalItemData[0];
        }
        boolean isEnabled = isEnabled(parentPublicStudy.getStudyEnvUuid());
        if (!isEnabled)
            return;

        List<RandomizationConfiguration> studyConfigs = getStudyConfigs(parentPublicStudy.getStudyEnvUuid());

        if (studyConfigs == null) {
            log.error("No RandomizeConfiguration found for this study:" + parentPublicStudy.getName());
            return;
        }

        boolean environmentHasMultipleConfigs = (studyConfigs.size() > 1) ? true : false;
        for (RandomizationConfiguration studyConfig : studyConfigs){
            List<List<String>> stratGroups = new ArrayList<>();

            // make an array out of event_oids and item_oids
            studyConfig.getStratificationFactors().values().stream()
                    .collect(Collectors.toCollection(ArrayList::new)).stream()
                    .forEach(line -> {
                        String[] elements = Arrays.stream(line.split("\\.")).toArray(String[]::new);
                        for (int index = 0; index < elements.length; index++) {
                            if (stratGroups.size() < index + 1)
                                stratGroups.add(new ArrayList<>());
                            stratGroups.get(index).add(elements[index]);
                        }
                    });

            if (stratGroups.size() == 0) {
                log.error("Randomize configuration does not have stratification factors defined for this study:" + parentPublicStudy.getName()
                        + " ParticipantId: " + studySubjectOID);
                return;
            }
            // check event and item from thisItemData are part of the strat factors
            if (itemData != null && !isItemPartOfStratFactors(stratGroups, itemData))
                return;


            String eventOID = studyConfig.targetField.getEventOID();
            String formOID = studyConfig.targetField.getFormOID();
            String itemGroup = studyConfig.targetField.getItemGroupOID();
            String itemOID = studyConfig.targetField.getItemOID();

            List<RandomizeQueryResult> randomizeQueryResult =
                    studyEventDao.fetchItemData(new ArrayList<>(Arrays.asList(eventOID)), studySubjectOID,
                            new ArrayList<>(Arrays.asList(formOID)), new ArrayList<>(Arrays.asList(itemGroup)), new ArrayList<>(Arrays.asList(itemOID)));
            List<RandomizeQueryResult> newRandomizeQueryResult;

            // target fields should not be populated if it already has a value
            if ((CollectionUtils.isEmpty(randomizeQueryResult) || StringUtils.isEmpty(randomizeQueryResult.get(0).getItemData().getValue()))) {
                // check values in all strat factors to be not null
                if ((newRandomizeQueryResult = stratFactorValuesAvailable(stratGroups, studyConfig, studySubjectOID)) != null) {
                    // send these values over
                    setStratFactors(parentPublicStudy, studyConfig, studySubjectOID, newRandomizeQueryResult, accessToken, environmentHasMultipleConfigs);
                }
            }
        }
    }

    private List<RandomizeQueryResult> stratFactorValuesAvailable(List<List<String>> stratGroups, RandomizationConfiguration studyConfig, String studySubjectOID) {

        // 0 is event oid, 1 form_oid, 2 itemgroup_oid and 3 is item oid
        List<RandomizeQueryResult> randomizeDataList = studyEventDao.fetchItemData(stratGroups.get(0), studySubjectOID,
                stratGroups.get(1), stratGroups.get(2), stratGroups.get(3));

        // check if all of the items that are specified in the strat factors have values
        if (stratGroups.get(0).size() == randomizeDataList.size()) {
            // are ALL the forms completed?
            if (randomizeDataList.stream().filter(x -> x.getEventCrf().getWorkflowStatus().equals(EventCrfWorkflowStatusEnum.COMPLETED)).count() == randomizeDataList.size())
                return randomizeDataList;
            else
                log.debug("<RANDOMIZE> All forms are not completed for ParticipantId: " + studySubjectOID);
        }
        return null;
    }

    @Override
    public List<RandomizationConfiguration> getStudyConfigs(String studyEnvUuid) {
        // if there is already an entry in the map, don't put it again
        RandomizationData data = randomizationMap.get(studyEnvUuid);
        if (data == null)
            return null;

        return data.getConfigurations();
    }

}
