package org.akaza.openclinica.config;

import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.job.AutowiringSpringBeanJobFactory;
import core.org.akaza.openclinica.job.JobExecutionExceptionListener;
import core.org.akaza.openclinica.job.JobTriggerListener;
import core.org.akaza.openclinica.job.OpenClinicaSchedulerFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.XmlWebApplicationContext;

import javax.annotation.PostConstruct;
import java.util.Properties;
import java.util.stream.Collectors;

@Component
public class ScheduledJobConfig {
  protected static final Logger logger = LoggerFactory.getLogger(ScheduledJobConfig.class);

  @Autowired
  private StudyDao studyDao;
  @Autowired
  private ApplicationContext applicationContext;

  @PostConstruct
  public void createSchemaSpecificSchedulers() throws BeansException {
    logger.info("In postProcessBeanFactory");
    studyDao.findAll().stream()
            .map(study -> study.getSchemaName())
            .collect(Collectors.toSet())
            .forEach(schema -> {
              createSchedulerFactoryBean(schema);
            });
  }

  public void createSchedulerFactoryBean(String schema) {
    logger.info("Creating a new schema scheduler:" + schema);

    Properties properties = new Properties();

    // use global Quartz properties
    properties.setProperty("org.quartz.jobStore.misfireThreshold",
            CoreResources.getField("org.quartz.jobStore.misfireThreshold"));
    properties.setProperty("org.quartz.jobStore.class",
            CoreResources.getField("org.quartz.jobStore.class"));
    properties.setProperty("org.quartz.jobStore.driverDelegateClass",
            CoreResources.getField("org.quartz.jobStore.driverDelegateClass"));
    properties.setProperty("org.quartz.jobStore.useProperties",
            CoreResources.getField("org.quartz.jobStore.useProperties"));
    properties.setProperty("org.quartz.jobStore.tablePrefix", schema + "." +
            CoreResources.getField("org.quartz.jobStore.tablePrefix"));
    properties.setProperty("org.quartz.threadPool.class",
            CoreResources.getField("org.quartz.threadPool.class"));
    properties.setProperty("org.quartz.threadPool.threadCount",
            CoreResources.getField("org.quartz.threadPool.threadCount"));
    properties.setProperty("org.quartz.threadPool.threadPriority",
            CoreResources.getField("org.quartz.threadPool.threadPriority"));
    AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
    jobFactory.setApplicationContext(applicationContext);

    BeanDefinition beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(OpenClinicaSchedulerFactoryBean.class)
            .addPropertyValue("schedulerName", schema)
            .addPropertyValue("jobFactory", jobFactory)
            .addPropertyValue("dataSource", (applicationContext.getBean("dataSource")))
            .addPropertyValue("transactionManager", applicationContext.getBean("transactionManager"))
            .addPropertyValue("applicationContext", applicationContext)
            .addPropertyValue("applicationContextSchedulerContextKey", "applicationContext")
            .addPropertyValue("globalJobListeners", new JobExecutionExceptionListener())
            .addPropertyValue("globalTriggerListeners", new JobTriggerListener())
            .addPropertyValue("quartzProperties", properties)
            .setInitMethodName("start")
            .getBeanDefinition();
    ((DefaultListableBeanFactory) ((XmlWebApplicationContext) applicationContext).getBeanFactory()).registerBeanDefinition(schema, beanDefinition);
  }

  public void setStudyDao(StudyDao studyDao) {
    this.studyDao = studyDao;
  }
}
