package org.akaza.openclinica.config;

import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.service.SchedulerUtilService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.stream.Collectors;

@Component
public class ScheduledJobConfig {
    protected static final Logger logger = LoggerFactory.getLogger(ScheduledJobConfig.class);

    @Autowired
    private StudyDao studyDao;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private SchedulerUtilService schedulerUtilService;

    @PostConstruct
    public void createSchemaSpecificSchedulers() throws BeansException {
        logger.info("In postProcessBeanFactory");
        studyDao.findAll().stream()
                .map(study -> study.getSchemaName())
                .collect(Collectors.toSet())
                .forEach(schema -> {
                    schedulerUtilService.createSchedulerFactoryBean(applicationContext, schema);
                });
    }

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
}
