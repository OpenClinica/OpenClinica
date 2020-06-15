package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.job.AutowiringSpringBeanJobFactory;
import core.org.akaza.openclinica.job.JobExecutionExceptionListener;
import core.org.akaza.openclinica.job.JobTriggerListener;
import core.org.akaza.openclinica.job.OpenClinicaSchedulerFactoryBean;
import org.apache.commons.lang.StringUtils;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.util.Properties;

import static core.org.akaza.openclinica.dao.hibernate.multitenant.CurrentTenantIdentifierResolverImpl.CURRENT_TENANT_ID;

@Service("schedulerUtilService")
public class SchedulerUtilServiceImpl implements SchedulerUtilService {

    public final Logger logger = LoggerFactory.getLogger(SchedulerUtilServiceImpl.class);

    public Scheduler getSchemaScheduler(ApplicationContext applicationContext, HttpServletRequest request) {
        Scheduler jobScheduler = null;

        if (request.getAttribute(CURRENT_TENANT_ID) != null) {
            String schema = (String) request.getAttribute(CURRENT_TENANT_ID);
            if (StringUtils.isNotEmpty(schema) &&
                    (schema.equalsIgnoreCase("public") != true)) {
                try {
                    jobScheduler = (Scheduler) applicationContext.getBean(schema);
                    logger.debug("Existing schema scheduler found:" + schema);
                } catch (NoSuchBeanDefinitionException e) {
                    createSchedulerFactoryBean(applicationContext, schema);
                    try {
                        jobScheduler = (Scheduler) applicationContext.getBean(schema);
                    } catch (BeansException e1) {
                        logger.error("Bean for scheduler is not able to accessed after creating scheduled factory bean: ", e1);

                    }
                } catch (BeansException e) {
                    logger.error("Bean for scheduler is not able to accessed: ", e);

                }
            }
        }
        return jobScheduler;
    }


    public void createSchedulerFactoryBean(ApplicationContext applicationContext, String schema) {
        logger.debug("Creating a new schema scheduler:" + schema);
        OpenClinicaSchedulerFactoryBean sFBean = new OpenClinicaSchedulerFactoryBean();
        sFBean.setSchedulerName(schema);
        Properties properties = new Properties();
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        sFBean.setJobFactory(jobFactory);
        sFBean.setDataSource((DataSource) applicationContext.getBean("dataSource"));
        sFBean.setTransactionManager((PlatformTransactionManager) applicationContext.getBean("transactionManager"));
        sFBean.setApplicationContext(applicationContext);
        sFBean.setApplicationContextSchedulerContextKey("applicationContext");
        sFBean.setGlobalJobListeners(new JobExecutionExceptionListener());
        sFBean.setGlobalTriggerListeners(new JobTriggerListener());

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
        sFBean.setQuartzProperties(properties);
        try {
            sFBean.afterPropertiesSet();
        } catch (Exception e) {
            logger.error("Error creating the scheduler bean:" + schema, e.getMessage(), e);
            return;
        }
        sFBean.start();
        ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
        beanFactory.registerSingleton(schema, sFBean);
    }
}
