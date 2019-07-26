package org.akaza.openclinica.service.randomize;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.RandomizeQueryResult;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.ItemData;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.domain.randomize.RandomizationConfiguration;
import org.akaza.openclinica.domain.randomize.RandomizationDTO;
import org.akaza.openclinica.domain.randomize.RandomizationTarget;
import org.akaza.openclinica.service.dto.ModuleConfigAttributeDTO;
import org.akaza.openclinica.service.dto.ModuleConfigDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service("randomizationService")
public class RandomizationServiceImpl implements RandomizationService {
    private final Logger log = LoggerFactory.getLogger(RandomizationServiceImpl.class);
    private static String sbsUrl = CoreResources.getField("SBSUrl");

    @Autowired
    private StudySubjectDao studySubjectDao;

    @Autowired
    private StudyEventDao studyEventDao;

    private static String initServiceUrl() {
        int index = sbsUrl.indexOf("//");
        String protocol = sbsUrl.substring(0, index) + "//";
        String domainUrl=sbsUrl.substring(index + 2, sbsUrl.indexOf("/", index + 2));
        String subDomainUrl = "/randomizeservice/api/";
        return protocol + domainUrl + subDomainUrl;
    };
    static String randomizeUrl = initServiceUrl();

    private static PassiveExpiringMap<String, RandomizationData> randomizationMap =
            new PassiveExpiringMap<>(24, TimeUnit.HOURS);

    private class RandomizationData {
        private boolean isEnabled;
        private RandomizationConfiguration configuration;

        public RandomizationData(boolean isEnabled, RandomizationConfiguration configuration) {
            this.isEnabled = isEnabled;
            this.configuration = configuration;
        }

        public boolean isEnabled() {
            return isEnabled;
        }

        public void setEnabled(boolean enabled) {
            isEnabled = enabled;
        }

        public RandomizationConfiguration getConfiguration() {
            return configuration;
        }

        public void setConfiguration(RandomizationConfiguration configuration) {
            this.configuration = configuration;
        }
    }

    private RandomizationConfiguration retrieveConfiguration(String studyEnvUuid, String accessToken) {
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
        ResponseEntity<ModuleConfigDTO> response = restTemplate.exchange(randomizeUrl + "study-environments/" + studyEnvUuid + "/configuration", HttpMethod.GET, entity, ModuleConfigDTO.class);
        if (response.hasBody())
            return mapModuleConfigToConfig(response.getBody());
        return null;
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
    public void processModule(Study study, String isModuleEnabled, String accessToken) {
        // if there is already an entry in the map, don't put it again
        RandomizationData data = randomizationMap.get(study.getStudyEnvUuid());
        if (data != null)
            return;

        if (StringUtils.equalsIgnoreCase(isModuleEnabled, ENABLED)) {
            RandomizationConfiguration configuration = retrieveConfiguration(study.getStudyEnvUuid(), accessToken);
            synchronized (randomizationMap) {
                randomizationMap.put(study.getStudyEnvUuid(), new RandomizationData(true, configuration));
            }
        } else {
            synchronized (randomizationMap) {
                randomizationMap.put(study.getStudyEnvUuid(), new RandomizationData(false, null));
            }
        }
    }

    public boolean isItemPartOfStratFactors(List<List<String>> stratGroups, ItemData thisItemData) {
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

    private void setStratFactors(List<List<String>> stratGroups, StudyBean publicStudy, RandomizationConfiguration studyConfig, String studySubjectOID,
                                List<RandomizeQueryResult> randomizeQueryResult, String accessToken) {

        RandomizationDTO randomizationDTO = new RandomizationDTO();
        randomizationDTO.setStudyUuid(publicStudy.getStudyUuid());
        randomizationDTO.setStudyEnvironmentUuid(publicStudy.getStudyEnvUuid());
        randomizationDTO.setSubjectOid(studySubjectOID);
        Map<String, String> stratFactors = new LinkedHashMap<>();
        StudySubject studySubject = studySubjectDao.findByOcOID(studySubjectOID);
        stratFactors.put("studyOid",         studySubject.getStudy().getOc_oid());
        stratFactors.put("siteId", studySubject.getStudy().getUniqueIdentifier());
        stratFactors.put("siteName", studySubject.getStudy().getName());
        String[] questions = studyConfig.getStratificationFactors().keySet().toArray(new String[0]);
        List<String> stratFactorValueList = new ArrayList<>();

        // database query results are coming back in different order.
        // Build a list of strings with event, form, itemGroup and item oids concatenated and compare them with the list received from the database to find the matching entry
        for (int stratIndex = 0; stratIndex < stratGroups.get(0).size(); stratIndex++) {
            stratFactorValueList.add(stratGroups.get(0).get(stratIndex)
                    + stratGroups.get(1).get(stratIndex)
                    + stratGroups.get(2).get(stratIndex)
                    + stratGroups.get(3).get(stratIndex));
        }
        List<String> databaseValues = randomizeQueryResult.stream()
                .map(x -> x.getStudyEvent().getStudyEventDefinition().getOc_oid()
                        + x.getEventCrf().getFormLayout().getCrf().getOcOid()
                        + x.getItemGroup().getOcOid()
                        + x.getItemData().getItem().getOcOid())
                .collect(Collectors.toList());

        long count = IntStream.range(0, questions.length)
                .mapToObj(i -> populateStratFactors(stratFactorValueList.get(i), i, StringUtils.substringAfter(questions[i], RandomizationService.STRATIFICATION_FACTOR + "."), databaseValues,
                        randomizeQueryResult, stratFactors)).count();
        randomizationDTO.setStratificationFactors(stratFactors);
        log.debug("Questions processed:" + count);
        sendStratificationFactors(randomizationDTO, accessToken);
    }

    private Map<String, String>  populateStratFactors(String stratFactorValue, int index, String question,  List<String> databaseValues,
                                                      List<RandomizeQueryResult> randomizeQueryResult, Map<String, String> stratFactors) {
        if (randomizeQueryResult.size() <= index) {
            log.error("Index out of bound:" + index);
            return null;
        }
        OptionalInt first = IntStream.range(0, databaseValues.size())
                .filter(i -> stratFactorValue.equals(databaseValues.get(i)))
                .findFirst();

        if (first.isPresent()) {
            int itemDataIndex = first.getAsInt();
            stratFactors.put(question, randomizeQueryResult.get(itemDataIndex).getItemData().getValue());
        }

        return stratFactors;

    }

    public void processRandomization(ItemData thisItemData, StudyBean parentPublicStudy, String accessToken, String studySubjectOID) {

        boolean isEnabled = isEnabled(parentPublicStudy.getStudyEnvUuid());
        if (!isEnabled)
            return;

        RandomizationConfiguration studyConfig = getStudyConfig(parentPublicStudy.getStudyEnvUuid());

        if (studyConfig == null) {
            log.error("No RandomizeConfiguration found for this study:" + parentPublicStudy.getName());
            return;
        }

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
            log.error("Randomize configuration does not have stratification factors defined.");
            return;
        }
        // check event and item from thisItemData are part of the strat factors
        if (!isItemPartOfStratFactors(stratGroups, thisItemData))
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
                setStratFactors(stratGroups, parentPublicStudy, studyConfig, studySubjectOID, newRandomizeQueryResult, accessToken);
            }
        }
    }

    private List<RandomizeQueryResult> stratFactorValuesAvailable(List<List<String>> stratGroups, RandomizationConfiguration studyConfig, String studySubjectOID) {

        // 0 is event oid, 1 form_oid, 2 itemgroup_oid and 3 is item oid
        List<RandomizeQueryResult> randomizeDataList = studyEventDao.fetchItemData(stratGroups.get(0), studySubjectOID,
                stratGroups.get(1), stratGroups.get(2), stratGroups.get(3));

        // if all of the items in strat factors have values then return the itemData list
        if (stratGroups.get(0).size() == randomizeDataList.size()) {
            // are ALL the forms completed?
            if (randomizeDataList.stream().filter(x -> x.getEventCrf().getStatusId()== Status.UNAVAILABLE.getCode()).count() == randomizeDataList.size())
                return randomizeDataList;
        }
        return null;
    }

    @Override
    public RandomizationConfiguration getStudyConfig(String studyEnvUuid) {
        // if there is already an entry in the map, don't put it again
        RandomizationData data = randomizationMap.get(studyEnvUuid);
        if (data == null)
            return null;

        return data.getConfiguration();
    }

}
