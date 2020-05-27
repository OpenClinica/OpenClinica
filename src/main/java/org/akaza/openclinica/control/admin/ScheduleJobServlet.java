package org.akaza.openclinica.control.admin;

import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.job.AutowiringSpringBeanJobFactory;
import core.org.akaza.openclinica.job.JobExecutionExceptionListener;
import core.org.akaza.openclinica.job.JobTriggerListener;
import core.org.akaza.openclinica.job.OpenClinicaSchedulerFactoryBean;
import core.org.akaza.openclinica.service.PermissionService;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.view.Page;
import org.apache.commons.lang.StringUtils;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.TriggerKey;
import org.quartz.impl.StdScheduler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import static core.org.akaza.openclinica.dao.hibernate.multitenant.CurrentTenantIdentifierResolverImpl.CURRENT_TENANT_ID;

public abstract class ScheduleJobServlet extends SecureController {
    protected static final String PERIOD = "periodToRun";
    protected static final String FORMAT_ID = "formatId";
    protected static final String DATASET_ID = "dsId";
    protected static final String DATE_START_JOB = "job";
    protected static final String EMAIL = "contactEmail";
    protected static final String JOB_NAME = "jobName";
    protected static final String JOB_DESC = "jobDesc";
    protected static final String USER_ID = "user_id";
    protected static final String NUMBER_OF_FILES_TO_SAVE = "numberOfFilesToSave";
    protected static String TRIGGER_IMPORT_GROUP = "importTrigger";
    protected static String TRIGGER_EXPORT_GROUP = "XsltTriggersExportJobs";
    protected PermissionService permissionService;

    @Override
    protected abstract void processRequest() throws Exception;

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin() || ub.isTechAdmin()) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_allowed_access_extract_data_servlet"), "1");
    }

    protected StdScheduler getScheduler() {
        scheduler = this.scheduler != null ? scheduler : (StdScheduler) SpringServletAccess.getApplicationContext(context).getBean(SCHEDULER);
        return scheduler;
    }

    protected PermissionService getPermissionService() {
        return permissionService = (PermissionService) SpringServletAccess.getApplicationContext(context).getBean("permissionService");
    }

    public Scheduler getSchemaScheduler(HttpServletRequest request, ApplicationContext context, Scheduler jobScheduler) {

        if (request.getAttribute(CURRENT_TENANT_ID) != null) {
            String schema = (String) request.getAttribute(CURRENT_TENANT_ID);
            if (StringUtils.isNotEmpty(schema) &&
                    (schema.equalsIgnoreCase("public") != true)) {
                try {
                    jobScheduler = (Scheduler) context.getBean(schema);
                    logger.debug("Existing schema scheduler found:" + schema);
                } catch (NoSuchBeanDefinitionException e) {
                    createSchedulerFactoryBean(context, schema);
                    try {
                        jobScheduler = (Scheduler) context.getBean(schema);
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

    public void createSchedulerFactoryBean(ApplicationContext context, String schema) {
        logger.debug("Creating a new schema scheduler:" + schema);
        OpenClinicaSchedulerFactoryBean sFBean = new OpenClinicaSchedulerFactoryBean();
        sFBean.setSchedulerName(schema);
        Properties properties = new Properties();
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(context);
        sFBean.setJobFactory(jobFactory);
        sFBean.setDataSource((DataSource) context.getBean("dataSource"));
        sFBean.setTransactionManager((PlatformTransactionManager) context.getBean("transactionManager"));
        sFBean.setApplicationContext(context);
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
        ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) context).getBeanFactory();
        beanFactory.registerSingleton(schema, sFBean);
    }

    public HashMap validateForm(FormProcessor fp, HttpServletRequest request, JobKey[] jobKeys, String properName) {
        Validator v = new Validator(request);
        v.addValidation(JOB_NAME, Validator.NO_BLANKS);
        v.addValidation(JOB_NAME, Validator.NO_LEADING_OR_TRAILING_SPACES);
        v.addValidation(JOB_DESC, Validator.NO_BLANKS);
        v.addValidation(EMAIL, Validator.IS_A_EMAIL);
        v.addValidation(PERIOD, Validator.NO_BLANKS);
        v.addValidation(DATE_START_JOB + "Date", Validator.IS_A_DATE);

        int formatId = fp.getInt(FORMAT_ID);
        Date jobDate = fp.getDateTime(DATE_START_JOB);
        int datasetId = fp.getInt(DATASET_ID);
        HashMap errors = v.validate();
        if (formatId == 0) {
            v.addError(errors, FORMAT_ID, "Please pick a file format.");
        }
        for (JobKey jobKey : jobKeys) {
            if (jobKey.getName().equals(fp.getString(JOB_NAME)) && !jobKey.getName().equals(properName)) {
                v.addError(errors, JOB_NAME, "A job with that name already exists.  Please pick another name.");
            }
        }
        if (jobDate.before(new Date())) {
            v.addError(errors, DATE_START_JOB + "Date", "This date needs to be later than the present time.");
        }
        // limit the job description to 250 characters
        String jobDesc = fp.getString(JOB_DESC);
        if (null != jobDesc && !jobDesc.equals("")) {
            if (jobDesc.length() > 250) {
                v.addError(errors, JOB_DESC, "A job description cannot be more than 250 characters.");
            }
        }
        if (datasetId == 0) {
            v.addError(errors, DATASET_ID, "Please pick a dataset.");
        }
        return errors;
    }
}