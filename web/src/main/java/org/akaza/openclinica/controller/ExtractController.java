package org.akaza.openclinica.controller;

import static org.akaza.openclinica.dao.hibernate.multitenant.CurrentTenantIdentifierResolverImpl.CURRENT_TENANT_ID;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.extract.ExtractPropertyBean;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.job.AutowiringSpringBeanJobFactory;
import org.akaza.openclinica.job.JobExecutionExceptionListener;
import org.akaza.openclinica.job.JobTriggerListener;
import org.akaza.openclinica.job.OpenClinicaSchedulerFactoryBean;
import org.akaza.openclinica.service.extract.ExtractUtils;
import org.akaza.openclinica.service.extract.XsltTriggerService;
import org.akaza.openclinica.web.SQLInitServlet;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller("extractController")
@RequestMapping("/extract")
public class ExtractController {
    @Autowired
    @Qualifier("sidebarInit")
    private SidebarInit sidebarInit;

    @Autowired
    @Qualifier("dataSource")
    private BasicDataSource dataSource;

    private DatasetDAO datasetDao;
    private StudyDAO studyDao;

    @Autowired
    @Qualifier("schedulerFactoryBean")
    private Scheduler scheduler;

    @Autowired
    private OpenClinicaSchedulerFactoryBean schedulerFactoryBean;

    public static String TRIGGER_GROUP_NAME = "XsltTriggers";
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public ExtractController() {

    }

    /**
     * process the page from whence you came, i.e. extract a dataset
     * @param id, the id of the extract properties bean, gained from Core Resources
     * @param datasetId, the id of the dataset, found through DatasetDAO
     * @param request, http request
     * @return model map, but more importantly, creates a quartz job which runs right away and generates all output there
     */
    @RequestMapping(method = RequestMethod.GET)
    public ModelMap processSubmit(@RequestParam("id") String id,
            @RequestParam("datasetId") String datasetId, HttpServletRequest request, HttpServletResponse response)  {
        if(!mayProceed(request)){
            try{
                response.sendRedirect(request.getContextPath() + "/MainMenu?message=authentication_failed");
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        ModelMap map = new ModelMap();
        ResourceBundleProvider.updateLocale(LocaleResolver.getLocale(request));
        // String datasetId = (String)request.getAttribute("datasetId");
        // String id = (String)request.getAttribute("id");
        logger.debug("found both id " + id + " and dataset " + datasetId);
        ExtractUtils extractUtils = new ExtractUtils();
        // get extract id
        // get dataset id
        // if id is a number and dataset id is a number ...
        datasetDao = new DatasetDAO(dataSource);
        UserAccountBean userBean = (UserAccountBean) request.getSession().getAttribute("userBean");
        CoreResources cr =  new CoreResources();
        

        ExtractPropertyBean epBean = cr.findExtractPropertyBeanById(new Integer(id).intValue(),datasetId);

        DatasetBean dsBean = (DatasetBean)datasetDao.findByPK(new Integer(datasetId).intValue());
        // set the job in motion
        String[] files = epBean.getFileName();
        String exportFileName;
        int fileSize = files.length;
        int  cnt = 0;
        SimpleTrigger simpleTrigger = null;
        //TODO: if files and export names size is not same... throw an error
        dsBean.setName(dsBean.getName().replaceAll(" ", "_"));
        String[] exportFiles= epBean.getExportFileName();
        String pattern = "yyyy" + File.separator + "MM" + File.separator + "dd" + File.separator + "HHmmssSSS" + File.separator;
        SimpleDateFormat sdfDir = new SimpleDateFormat(pattern);
        int i =0;
        String[] temp = new String[exportFiles.length];
        //JN: The following logic is for comma separated variables, to avoid the second file be treated as a old file and deleted.
        while(i<exportFiles.length)
        {
            temp[i] = resolveVars(exportFiles[i],dsBean,sdfDir, SQLInitServlet.getField("filePath"), extractUtils);
            i++;
        }
        epBean.setDoNotDelFiles(temp);
        epBean.setExportFileName(temp);

        XsltTriggerService xsltService = new XsltTriggerService();

        // TODO get a user bean somehow?
        String generalFileDir = SQLInitServlet.getField("filePath");

        generalFileDir = generalFileDir + "datasets" + File.separator + dsBean.getId() + File.separator + sdfDir.format(new java.util.Date());

        exportFileName = epBean.getExportFileName()[cnt];


        // need to set the dataset path here, tbh
        logger.debug("found odm xml file path " + generalFileDir);
        // next, can already run jobs, translations, and then add a message to be notified later
        //JN all the properties need to have the variables...
        String xsltPath = SQLInitServlet.getField("filePath") + "xslt" + File.separator +files[cnt];
        String endFilePath = epBean.getFileLocation();
        endFilePath  = getEndFilePath(endFilePath,dsBean,sdfDir, SQLInitServlet.getField("filePath"), extractUtils);
        //  exportFileName = resolveVars(exportFileName,dsBean,sdfDir);
        if(epBean.getPostProcExportName()!=null)
        {
            //String preProcExportPathName = getEndFilePath(epBean.getPostProcExportName(),dsBean,sdfDir);
            String preProcExportPathName = resolveVars(epBean.getPostProcExportName(),dsBean,sdfDir, SQLInitServlet.getField("filePath"), extractUtils);
            epBean.setPostProcExportName(preProcExportPathName);
        }
        if(epBean.getPostProcLocation()!=null)
        {
            String prePocLoc = getEndFilePath(epBean.getPostProcLocation(),dsBean,sdfDir, SQLInitServlet.getField("filePath"), extractUtils);
            epBean.setPostProcLocation(prePocLoc);
        }
        setAllProps(epBean,dsBean,sdfDir, extractUtils);
        // also need to add the status fields discussed w/ cc:
        // result code, user message, optional URL, archive message, log file message
        // asdf table: sort most recent at top
        logger.debug("found xslt file name " + xsltPath);
        ApplicationContext context = null;
        Scheduler jobScheduler = scheduler;
        try {
            context = (ApplicationContext) scheduler.getContext().get("applicationContext");
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        jobScheduler = getSchemaScheduler(request, context, jobScheduler);
        // String xmlFilePath = generalFileDir + ODMXMLFileName;
        simpleTrigger = xsltService.generateXsltTrigger(jobScheduler, xsltPath,
                generalFileDir, // xml_file_path
                endFilePath + File.separator,
                exportFileName,
                dsBean.getId(),
                epBean, 
                userBean, 
                LocaleResolver.getLocale(request).getLanguage(),
                cnt,  
                SQLInitServlet.getField("filePath") + "xslt",
                this.TRIGGER_GROUP_NAME,
                (StudyBean) request.getSession().getAttribute("publicStudy"),
                (StudyBean) request.getSession().getAttribute("study"));
        // System.out.println("just set locale: " + LocaleResolver.getLocale(request).getLanguage());

        cnt++;

        //WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
        JobDetailFactoryBean jobDetailFactoryBean = context.getBean(JobDetailFactoryBean.class, simpleTrigger, this.TRIGGER_GROUP_NAME);


        try {
            Date dateStart = jobScheduler.scheduleJob(jobDetailFactoryBean.getObject(), simpleTrigger);
            logger.debug("== found job date: " + dateStart.toString());

        } catch (SchedulerException se) {
            se.printStackTrace();
        }

        request.setAttribute("datasetId", datasetId);
        // set the job name here in the user's session, so that we can ping the scheduler to pull it out later
        if(jobDetailFactoryBean!=null)
            request.getSession().setAttribute("jobName", jobDetailFactoryBean.getObject().getKey().getName());
        if(simpleTrigger!= null)
            request.getSession().setAttribute("groupName", this.TRIGGER_GROUP_NAME);

        request.getSession().setAttribute("datasetId", new Integer(dsBean.getId()));
        return map;
    }

    private Scheduler getSchemaScheduler(HttpServletRequest request, ApplicationContext context, Scheduler jobScheduler) {
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
                        e1.printStackTrace();
                    }
                } catch (BeansException e) {
                    e.printStackTrace();

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
        properties.setProperty("org.quartz.jobStore.tablePrefix", schema + "."+
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
    /**
     * @deprecated Use {@link #setAllProps(ExtractPropertyBean,DatasetBean,SimpleDateFormat,ExtractUtils)} instead
     */
    @Deprecated
    private ExtractPropertyBean setAllProps(ExtractPropertyBean epBean,DatasetBean dsBean,SimpleDateFormat sdfDir) {
        return setAllProps(epBean, dsBean, sdfDir,new ExtractUtils());
    }

    private ExtractPropertyBean setAllProps(ExtractPropertyBean epBean,DatasetBean dsBean,SimpleDateFormat sdfDir, ExtractUtils extractUtils) {



        return extractUtils.setAllProps(epBean, dsBean, sdfDir,  SQLInitServlet.getField("filePath"));


    }


    //TODO: ${linkURL} needs to be added
    /**
     *
     * for dateTimePattern, the directory structure is created. "yyyy" + File.separator + "MM" + File.separator + "dd" + File.separator,
     * to resolve location
     * @param filePath TODO
     * @param extractUtils TODO
     */
    private String getEndFilePath(String endFilePath,DatasetBean dsBean,SimpleDateFormat sdfDir, String filePath, ExtractUtils extractUtils){
        return extractUtils.getEndFilePath(endFilePath, dsBean, sdfDir, filePath);
    }

    /**
     * Returns the datetime based on pattern :"yyyy-MM-dd-HHmmssSSS", typically for resolving file name
     * @param endFilePath
     * @param dsBean
     * @param sdfDir
     * @return
     * @deprecated Use {@link #resolveVars(String,DatasetBean,SimpleDateFormat,String, ExtractUtils)} instead
     */
    @Deprecated
    private String resolveVars(String endFilePath,DatasetBean dsBean,SimpleDateFormat sdfDir){
        return resolveVars(endFilePath, dsBean, sdfDir, SQLInitServlet.getField("filePath"),new ExtractUtils());
    }

    /**
     * Returns the datetime based on pattern :"yyyy-MM-dd-HHmmssSSS", typically for resolving file name
     * @param endFilePath
     * @param dsBean
     * @param sdfDir
     * @param filePath TODO
     * @return
     * @deprecated Use {@link #resolveVars(String,DatasetBean,SimpleDateFormat,String,ExtractUtils)} instead
     */
    @Deprecated
    private String resolveVars(String endFilePath,DatasetBean dsBean,SimpleDateFormat sdfDir, String filePath){
        return resolveVars(endFilePath, dsBean, sdfDir, filePath, new ExtractUtils());
    }

    /**
     * Returns the datetime based on pattern :"yyyy-MM-dd-HHmmssSSS", typically for resolving file name
     * @param endFilePath
     * @param dsBean
     * @param sdfDir
     * @param filePath TODO
     * @param extractUtils TODO
     * @return
     */
    private String resolveVars(String endFilePath,DatasetBean dsBean,SimpleDateFormat sdfDir, String filePath, ExtractUtils extractUtils){
        return extractUtils.resolveVars(endFilePath, dsBean, sdfDir, filePath);

    }
    private void setUpSidebar(HttpServletRequest request) {
        if (sidebarInit.getAlertsBoxSetup() == SidebarEnumConstants.OPENALERTS) {
            request.setAttribute("alertsBoxSetup", true);
        }

        if (sidebarInit.getInfoBoxSetup() == SidebarEnumConstants.OPENINFO) {
            request.setAttribute("infoBoxSetup", true);
        }
        if (sidebarInit.getInstructionsBoxSetup() == SidebarEnumConstants.OPENINSTRUCTIONS) {
            request.setAttribute("instructionsBoxSetup", true);
        }

        if (!(sidebarInit.getEnableIconsBoxSetup() == SidebarEnumConstants.DISABLEICONS)) {
            request.setAttribute("enableIconsBoxSetup", true);
        }
    }

    public SidebarInit getSidebarInit() {
        return sidebarInit;
    }

    public void setSidebarInit(SidebarInit sidebarInit) {
        this.sidebarInit = sidebarInit;
    }

    private String resolveExportFilePath(String  epBeanFileName) {
        // String retMe = "";
        //String epBeanFileName = epBean.getExportFileName();
        // important that this goes first, tbh
        if (epBeanFileName.contains("$datetime")) {
            String dateTimeFilePattern = "yyyy-MM-dd-HHmmssSSS";
            SimpleDateFormat sdfDir = new SimpleDateFormat(dateTimeFilePattern);
            epBeanFileName = epBeanFileName.replace("$datetime", sdfDir.format(new java.util.Date()));
        } else if (epBeanFileName.contains("$date")) {
            String dateFilePattern = "yyyy-MM-dd";
            SimpleDateFormat sdfDir = new SimpleDateFormat(dateFilePattern);
            epBeanFileName = epBeanFileName.replace("$date", sdfDir.format(new java.util.Date()));
            // sdfDir.format(new java.util.Date())
            // retMe = epBean.getFileLocation() + File.separator + epBean.getExportFileName() + "." + epBean.getPostProcessing().getFileType();
        } else {
            // retMe = epBean.getFileLocation() + File.separator + epBean.getExportFileName() + "." + epBean.getPostProcessing().getFileType();
        }
        return epBeanFileName;// + "." + epBean.getPostProcessing().getFileType();// not really the case - might be text to pdf
        // return retMe;
    }

    private boolean mayProceed(HttpServletRequest request) {

        HttpSession session = request.getSession();
        StudyUserRoleBean currentRole = (StudyUserRoleBean) session.getAttribute("userRole");

        Role r = currentRole.getRole();
        UserAccountBean ub = (UserAccountBean) request.getSession().getAttribute("userBean");
        if (currentRole == null || currentRole.getId() <= 0) {
            if (ub.getId() > 0) {
                currentRole = ub.getRoleByStudy(ub.getActiveStudyId());
                session.setAttribute("userRole", currentRole);
            }
        }

        if (r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR) || r.equals(Role.MONITOR)
                || currentRole.getRole().equals(Role.INVESTIGATOR) ) {
            return true;
        }
        return false;
    }

}
