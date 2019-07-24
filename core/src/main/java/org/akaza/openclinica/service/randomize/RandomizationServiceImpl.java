package org.akaza.openclinica.service.randomize;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.randomize.RandomizationConfiguration;
import org.akaza.openclinica.domain.randomize.RandomizationDTO;
import org.akaza.openclinica.domain.randomize.RandomizationTarget;
import org.akaza.openclinica.service.dto.ModuleConfigAttributeDTO;
import org.akaza.openclinica.service.dto.ModuleConfigDTO;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service("randomizationService")
public class RandomizationServiceImpl implements RandomizationService {
    private final Logger log = LoggerFactory.getLogger(RandomizationServiceImpl.class);
    private static String sbsUrl = CoreResources.getField("SBSUrl");

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
            synchronized (this) {
                randomizationMap.put(study.getStudyEnvUuid(), new RandomizationData(true, configuration));
            }
        } else {
            synchronized (this) {
                randomizationMap.put(study.getStudyEnvUuid(), new RandomizationData(false, null));
            }
        }
    }

    @Override
    public void sendStratificationFactors(RandomizationDTO randomizationDTO, String accessToken) {
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

    @Override
    public RandomizationConfiguration getStudyConfig(String studyEnvUuid) {
        // if there is already an entry in the map, don't put it again
        RandomizationData data = randomizationMap.get(studyEnvUuid);
        if (data == null)
            return null;

        return data.getConfiguration();
    }

}
