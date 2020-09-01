package org.akaza.openclinica.config;

import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.extract.OcQrtzTriggersDAO;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.service.SchedulerUtilService;
import core.org.akaza.openclinica.service.StudyBuildService;
import core.org.akaza.openclinica.service.randomize.RandomizationService;
import core.org.akaza.openclinica.web.rest.client.auth.impl.KeycloakClientImpl;
import org.akaza.openclinica.controller.dto.ModuleConfigDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@DependsOn("liquibase")
public class ModulesConfigurationConfig {
    protected static final Logger logger = LoggerFactory.getLogger(ModulesConfigurationConfig.class);

    @Autowired
    private StudyDao studyDao;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private SchedulerUtilService schedulerUtilService;
    @Autowired
    private OcQrtzTriggersDAO ocQrtzTriggersDAO;
    @Autowired
    private RandomizationService randomizationService;
    @Autowired
    private KeycloakClientImpl keycloakClient;
    @Autowired
    private StudyBuildService studyBuildService;

    @PostConstruct
    public void createSchemaSpecificSchedulers() throws BeansException {
        logger.info("In postProcessBeanFactory");

        Set<String> schemas = studyDao.findAll().stream()
                .map(study -> study.getSchemaName())
                .collect(Collectors.toSet());

        for (String schema : schemas) {
            CoreResources.setRequestSchema(schema);
            if (ocQrtzTriggersDAO.findAll().size() != 0) {
                logger.info("Found triggers in " + schema);
                schedulerUtilService.createSchedulerFactoryBean(applicationContext, schema);
            }
        }
        CoreResources.setRequestSchemaToPublic();
    }

    /***
     * Bean to initialize module configurations at runtime startup.
     *
     * This callback variant is somewhat similar to ContextRefreshedEvent but doesn't require an implementation of ApplicationListener,
     * with no need to filter context references across a context hierarchy etc.
     * It also implies a more minimal dependency on just the beans package and is being honored by standalone ListableBeanFactory implementations,
     * not just in an ApplicationContext environment.
     * @return
     */
    @Bean
    public SmartInitializingSingleton loadModuleConfigurations() {
        return () -> {

            CoreResources.setRequestSchemaToPublic();
            List<Study> studySchemas = studyDao.findAll().stream()
                    .filter(study -> study.checkAndGetParentStudyId() == 0)
                    .collect(Collectors.toList());

            for (Study study : studySchemas){
                logger.info("Studies to process: " + study.getName() + " : " + study.getStudyUuid() + " : " + study.getStudyEnvUuid() + " : " + study.getSchemaName());
            }

            String accessToken = keycloakClient.getSystemToken();

            for (Study study : studySchemas){
                logger.info("Processing: " + study.getName() + " : " + study.getStudyUuid() + " : " + study.getStudyEnvUuid() + " : " + study.getSchemaName());
                List<ModuleConfigDTO> moduleConfigDTOS = studyBuildService.getModuleConfigsFromStudyService(accessToken, study);
                CoreResources.setRequestSchema(study.getSchemaName());
                logger.info("Request schema is currently: " + CoreResources.getRequestSchema());
                studyBuildService.processModules(moduleConfigDTOS, study);
            }

            CoreResources.setRequestSchemaToPublic();

            // Randomize follows a different module workflow for now. The following can be removed once we move to storing
            // randomize configurations in study service as a ModuleConfigDTO.
            Map<String, String> configMap = new HashMap<>();
            boolean isSuccess = false;
            try {
                isSuccess = randomizationService.refreshConfigurations(accessToken, configMap);
            } catch (Exception e) {
                // Since this run at the startup, we need to catch the exception and log it. If we don't do this, it will prevent RT from starting
                logger.error("Refresh configuration failed:" + e);
            }
            if (isSuccess || configMap.size() > 0)
                logger.info("Initialized Randomize configuration with " + configMap.size() + " entries.");
            else {
                logger.error("No studies configured or Randomize  startup configuration failed.");
            }
/*

            logger.info("Calling Randomize service to initialize configuration.");
            Map<String, String> configMap = new HashMap<>();
            boolean isSuccess = false;
            try {
                isSuccess = randomizationService.refreshConfigurations(accessToken, configMap);
            } catch (Exception e) {
                // Since this run at the startup, we need to catch the exception and log it. If we don't do this, it will prevent RT from starting
                logger.error("Refresh configuration failed:" + e);
            }
            if (isSuccess || configMap.size() > 0)
                logger.info("Initialized Randomize configuration with " + configMap.size() + " entries.");
            else {
                logger.error("No studies configured or Randomize  startup configuration failed.");
            }
*/

        };
    }

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
}
