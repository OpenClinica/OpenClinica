package org.akaza.openclinica.web.job;

import java.io.File;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.TriggerBean;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.extract.ExtractBean;
import org.akaza.openclinica.bean.extract.SPSSReportBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.core.OpenClinicaMailSender;
import org.akaza.openclinica.dao.admin.AuditEventDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.dao.hibernate.RuleSetRuleDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.PermissionService;
import org.akaza.openclinica.service.extract.GenerateExtractFileService;
import org.akaza.openclinica.web.SQLInitServlet;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class ExampleSpringJob extends QuartzJobBean {

    // example code here
    private String message;

    // example code here
    public void setMessage(String message) {
        this.message = message;
    }

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    // variables to pull out
    public static final String PERIOD = "periodToRun";
    public static final String TAB = "tab";
    public static final String CDISC = "cdisc";
    public static final String CDISC12 = "cdisc12";
    public static final String CDISC13 = "cdisc13";
    public static final String CDISC13OC = "cdisc13oc";
    public static final String SPSS = "spss";
    public static final String DATASET_ID = "dsId";
    public static final String EMAIL = "contactEmail";
    public static final String USER_ID = "user_id";
    public static final String STUDY_NAME = "study_name";
    public static final String STUDY_ID = "studyId";
    public static final String LOCALE = "locale";

    private static final String DATASET_DIR = SQLInitServlet.getField("filePath") + "datasets" + File.separator;

    // private BasicDataSource basicDataSource;
    private OpenClinicaMailSender mailSender;
    private DataSource dataSource;
    private GenerateExtractFileService generateFileService;
    private UserAccountBean userBean;
    private JobDetailFactoryBean jobDetailBean;
    private CoreResources coreResources;
    private RuleSetRuleDao ruleSetRuleDao;
    private PermissionService permissionService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        // need to generate a Locale so that user beans and other things will
        // generate normally
        Locale locale = new Locale("en-US");
        ResourceBundleProvider.updateLocale(locale);
        ResourceBundle pageMessages = ResourceBundleProvider.getPageMessagesBundle();
        // logger.debug("--");
        // logger.debug("-- executing a job " + message + " at " + new
        // java.util.Date().toString());
        JobDataMap dataMap = context.getMergedJobDataMap();
        SimpleTrigger trigger = (SimpleTrigger) context.getTrigger();
        try {
            ApplicationContext appContext = (ApplicationContext) context.getScheduler().getContext().get("applicationContext");
            String studySubjectNumber = ((CoreResources) appContext.getBean("coreResources")).getField("extract.number");
            coreResources = (CoreResources) appContext.getBean("coreResources");
            ruleSetRuleDao = (RuleSetRuleDao) appContext.getBean("ruleSetRuleDao");
            dataSource = (DataSource) appContext.getBean("dataSource");
            mailSender = (OpenClinicaMailSender) appContext.getBean("openClinicaMailSender");
            AuditEventDAO auditEventDAO = new AuditEventDAO(dataSource);
            // Scheduler scheduler = context.getScheduler();
            // JobDetail detail = context.getJobDetail();
            // jobDetailBean = (JobDetailBean) detail;
            /*
             * data map here should coincide with the job data map found in
             * CreateJobExportServlet, with the following code: jobDataMap = new
             * JobDataMap(); jobDataMap.put(DATASET_ID, datasetId);
             * jobDataMap.put(PERIOD, period); jobDataMap.put(EMAIL, email);
             * jobDataMap.put(TAB, tab); jobDataMap.put(CDISC, cdisc);
             * jobDataMap.put(SPSS, spss);
             */
            String alertEmail = dataMap.getString(EMAIL);
            String localeStr = dataMap.getString(LOCALE);
            if (localeStr != null) {
                locale = new Locale(localeStr);
                ResourceBundleProvider.updateLocale(locale);
                pageMessages = ResourceBundleProvider.getPageMessagesBundle();

            }
            int dsId = dataMap.getInt(DATASET_ID);
            String tab = dataMap.getString(TAB);
            String cdisc = dataMap.getString(CDISC);
            String cdisc12 = dataMap.getString(CDISC12);
            if (cdisc12 == null) {
                cdisc12 = "0";
            }
            String cdisc13 = dataMap.getString(CDISC13);
            if (cdisc13 == null) {
                cdisc13 = "0";
            }
            String cdisc13oc = dataMap.getString(CDISC13OC);
            if (cdisc13oc == null) {
                cdisc13oc = "0";
            }
            String spss = dataMap.getString(SPSS);
            int userId = dataMap.getInt(USER_ID);
            int studyId = dataMap.getInt(STUDY_ID);

            // String datasetId = dataMap.getString(DATASET_ID);
            // int dsId = new Integer(datasetId).intValue();
            // String userAcctId = dataMap.getString(USER_ID);
            // int userId = new Integer(userAcctId).intValue();
            // why the flip-flop? if one property is set to 'true' we can
            // see jobs in another screen but all properties have to be
            // strings

            logger.debug("-- found the job: " + dsId + " dataset id");

            // for (Iterator it = dataMap.entrySet().iterator(); it.hasNext();)
            // {
            // java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
            // Object key = entry.getKey();
            // Object value = entry.getValue();
            // // logger.debug("-- found datamap property: " + key.toString() +
            // // " : " + value.toString());
            // }
            HashMap fileName = new HashMap<String, Integer>();
            if (dsId > 0) {
                // trying to not throw an error if there's no dataset id
                DatasetDAO dsdao = new DatasetDAO(dataSource);
                DatasetBean datasetBean = (DatasetBean) dsdao.findByPK(dsId);
                StudyDAO studyDao = new StudyDAO(dataSource);
                UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);
                // hmm, three lines in the if block DRY?
                String generalFileDir = "";
                String generalFileDirCopy = "";
                String exportFilePath = SQLInitServlet.getField("exportFilePath");
                String pattern = "yyyy" + File.separator + "MM" + File.separator + "dd" + File.separator + "HHmmssSSS" + File.separator;
                SimpleDateFormat sdfDir = new SimpleDateFormat(pattern);
                generalFileDir = DATASET_DIR + datasetBean.getId() + File.separator + sdfDir.format(new java.util.Date());
                if (!"".equals(exportFilePath)) {
                    generalFileDirCopy = SQLInitServlet.getField("filePath") + exportFilePath + File.separator;
                }
                // logger.debug("-- created the following dir: " +
                // generalFileDir);
                long sysTimeBegin = System.currentTimeMillis();
                // set up the user bean here, tbh
                // logger.debug("-- gen tab file 00");

                userBean = (UserAccountBean) userAccountDAO.findByPK(userId);
                // needs to also be captured by the servlet, tbh
                // logger.debug("-- gen tab file 00");
                generateFileService = new GenerateExtractFileService(dataSource, coreResources, ruleSetRuleDao);

                // logger.debug("-- gen tab file 00");

                // tbh #5796 - covers a bug when the user changes studies, 10/2010
                StudyBean activeStudy = (StudyBean) studyDao.findByPK(studyId);
                StudyBean parentStudy = new StudyBean();
                logger.debug("active study: " + studyId + " parent study: " + activeStudy.getParentStudyId());
                if (activeStudy.getParentStudyId() > 0) {
                    // StudyDAO sdao = new StudyDAO(sm.getDataSource());
                    parentStudy = (StudyBean) studyDao.findByPK(activeStudy.getParentStudyId());
                } else {
                    parentStudy = activeStudy;
                    // covers a bug in tab file creation, tbh 01/2009
                }

                logger.debug("-- found extract bean ");

                ExtractBean eb = generateFileService.generateExtractBean(datasetBean, activeStudy, parentStudy);
                MessageFormat mf = new MessageFormat("");
                StringBuffer message = new StringBuffer();
                StringBuffer auditMessage = new StringBuffer();
                // use resource bundle page messages to generate the email, tbh
                // 02/2009
                // message.append(pageMessages.getString("html_email_header_1")
                // + " " + alertEmail +
                // pageMessages.getString("html_email_header_2") + "<br/>");
                message.append("<p>" + pageMessages.getString("email_header_1") + " " + EmailEngine.getAdminEmail() + " "
                    + pageMessages.getString("email_header_2") + " Job Execution " + pageMessages.getString("email_header_3") + "</p>");
                message.append("<P>Dataset: " + datasetBean.getName() + "</P>");
                message.append("<P>Study: " + activeStudy.getName() + "</P>");
                message.append("<p>" + pageMessages.getString("html_email_body_1") + datasetBean.getName() + pageMessages.getString("html_email_body_2")
                    + SQLInitServlet.getField("sysURL") + pageMessages.getString("html_email_body_3") + "</p>");
                // logger.debug("-- gen tab file 00");
                if ("1".equals(tab)) {

                    logger.debug("-- gen tab file 01");
                    fileName =
                        generateFileService.createTabFile(eb, sysTimeBegin, generalFileDir, datasetBean, activeStudy.getId(), parentStudy.getId(),
                                generalFileDirCopy, userBean);
                    message.append("<p>" + pageMessages.getString("html_email_body_4") + " " + getFileNameStr(fileName)
                        + pageMessages.getString("html_email_body_4_5") + SQLInitServlet.getField("sysURL.base") + "AccessFile?fileId="
                        + getFileIdInt(fileName) + pageMessages.getString("html_email_body_3") + "</p>");
                    // MessageFormat mf = new MessageFormat("");
                    // mf.applyPattern(pageMessages.getString(
                    // "you_can_access_tab_delimited"));
                    // Object[] arguments = { getFileIdInt(fileName) };
                    // auditMessage.append(mf.format(arguments));

                    // auditMessage.append(
                    // "You can access your tab-delimited file <a href='AccessFile?fileId="
                    // + getFileIdInt(fileName) + "'>here</a>.<br/>");
                    auditMessage.append(pageMessages.getString("you_can_access_tab_delimited") + getFileIdInt(fileName) + pageMessages.getString("access_end"));
                }

                if ("1".equals(cdisc)) {
                    String odmVersion = "oc1.2";
                    fileName =
                        generateFileService.createODMFile(odmVersion, sysTimeBegin, generalFileDir, datasetBean, activeStudy, generalFileDirCopy, eb,
                                activeStudy.getId(), parentStudy.getId(),studySubjectNumber, true, true, true, null, userBean);
                    logger.debug("-- gen odm file");
                    message.append("<p>" + pageMessages.getString("html_email_body_4") + " " + getFileNameStr(fileName)
                        + pageMessages.getString("html_email_body_4_5") + SQLInitServlet.getField("sysURL.base") + "AccessFile?fileId="
                        + getFileIdInt(fileName) + pageMessages.getString("html_email_body_3") + "</p>");

                    // MessageFormat mf = new MessageFormat("");
                    // mf.applyPattern(pageMessages.getString(
                    // "you_can_access_odm_12"));
                    // Object[] arguments = { getFileIdInt(fileName) };
                    // auditMessage.append(mf.format(arguments));

                    // auditMessage.append(
                    // "You can access your ODM 1.2 w/OpenClinica Extension XML file <a href='AccessFile?fileId="
                    // + getFileIdInt(fileName)
                    // + "'>here</a>.<br/>");
                    auditMessage.append(pageMessages.getString("you_can_access_odm_12") + getFileIdInt(fileName) + pageMessages.getString("access_end"));
                }

                if ("1".equals(cdisc12)) {
                    String odmVersion = "1.2";
                    fileName =
                        generateFileService.createODMFile(odmVersion, sysTimeBegin, generalFileDir, datasetBean, activeStudy, generalFileDirCopy, eb,
                                activeStudy.getId(), parentStudy.getId(),studySubjectNumber, true, true,true, null, userBean);
                    logger.debug("-- gen odm file 1.2 default");
                    message.append("<p>" + pageMessages.getString("html_email_body_4") + " " + getFileNameStr(fileName)
                        + pageMessages.getString("html_email_body_4_5") + SQLInitServlet.getField("sysURL.base") + "AccessFile?fileId="
                        + getFileIdInt(fileName) + pageMessages.getString("html_email_body_3") + "</p>");

                    // mf.applyPattern(pageMessages.getString(
                    // "you_can_access_odm_12_xml"));
                    // Object[] arguments = { getFileIdInt(fileName) };
                    // auditMessage.append(mf.format(arguments));
                    // // auditMessage.append(
                    // "You can access your ODM 1.2 XML file <a href='AccessFile?fileId="
                    // + getFileIdInt(fileName) + "'>here</a>.<br/>");
                    auditMessage.append(pageMessages.getString("you_can_access_odm_12_xml") + getFileIdInt(fileName) + pageMessages.getString("access_end"));
                }

                if ("1".equals(cdisc13)) {
                    String odmVersion = "1.3";
                    fileName =
                        generateFileService.createODMFile(odmVersion, sysTimeBegin, generalFileDir, datasetBean, activeStudy, generalFileDirCopy, eb,
                                activeStudy.getId(), parentStudy.getId(),studySubjectNumber, true, true, true, null, userBean);
                    logger.debug("-- gen odm file 1.3");
                    message.append("<p>" + pageMessages.getString("html_email_body_4") + " " + getFileNameStr(fileName)
                        + pageMessages.getString("html_email_body_4_5") + SQLInitServlet.getField("sysURL.base") + "AccessFile?fileId="
                        + getFileIdInt(fileName) + pageMessages.getString("html_email_body_3") + "</p>");

                    // MessageFormat mf = new MessageFormat("");
                    // mf.applyPattern(pageMessages.getString(
                    // "you_can_access_odm_13"));
                    // Object[] arguments = { getFileIdInt(fileName) };
                    // auditMessage.append(mf.format(arguments));

                    // auditMessage.append(
                    // "You can access your ODM 1.3 XML file <a href='AccessFile?fileId="
                    // + getFileIdInt(fileName) + "'>here</a>.<br/>");
                    auditMessage.append(pageMessages.getString("you_can_access_odm_13") + getFileIdInt(fileName) + pageMessages.getString("access_end"));
                }

                if ("1".equals(cdisc13oc)) {
                    String odmVersion = "oc1.3";
                    fileName =
                        generateFileService.createODMFile(odmVersion, sysTimeBegin, generalFileDir, datasetBean, activeStudy, generalFileDirCopy, eb,
                                activeStudy.getId(), parentStudy.getId(),studySubjectNumber, true, true, true, null, userBean);
                    logger.debug("-- gen odm file 1.3 oc");
                    message.append("<p>" + pageMessages.getString("html_email_body_4") + " " + getFileNameStr(fileName)
                        + pageMessages.getString("html_email_body_4_5") + SQLInitServlet.getField("sysURL.base") + "AccessFile?fileId="
                        + getFileIdInt(fileName) + pageMessages.getString("html_email_body_3") + "</p>");

                    // MessageFormat mf = new MessageFormat("");
                    // mf.applyPattern(pageMessages.getString(
                    // "you_can_access_odm_13_xml"));
                    // Object[] arguments = { getFileIdInt(fileName) };
                    // auditMessage.append(mf.format(arguments));

                    // auditMessage.append(
                    // "You can access your ODM 1.3 w/OpenClinica Extension XML file <a href='AccessFile?fileId="
                    // + getFileIdInt(fileName)
                    // + "'>here</a>.<br/>");
                    auditMessage.append(pageMessages.getString("you_can_access_odm_13_xml") + getFileIdInt(fileName) + pageMessages.getString("access_end"));
                }
                if ("1".equals(spss)) {
                    SPSSReportBean answer = new SPSSReportBean();
                    fileName =
                        generateFileService.createSPSSFile(datasetBean, eb, activeStudy, parentStudy, sysTimeBegin, generalFileDir, answer, generalFileDirCopy, userBean);
                    logger.debug("-- gen spss file");
                    message.append("<p>" + pageMessages.getString("html_email_body_4") + " " + getFileNameStr(fileName)
                        + pageMessages.getString("html_email_body_4_5") + SQLInitServlet.getField("sysURL.base") + "AccessFile?fileId="
                        + getFileIdInt(fileName) + pageMessages.getString("html_email_body_3") + "</p>");

                    // MessageFormat mf = new MessageFormat("");
                    // mf.applyPattern(pageMessages.getString(
                    // "you_can_access_spss"));
                    // Object[] arguments = { getFileIdInt(fileName) };
                    // auditMessage.append(mf.format(arguments));

                    // auditMessage.append(
                    // "You can access your SPSS files <a href='AccessFile?fileId="
                    // + getFileIdInt(fileName) + "'>here</a>.<br/>");
                    auditMessage.append(pageMessages.getString("you_can_access_spss") + getFileIdInt(fileName) + pageMessages.getString("access_end"));
                }

                // wrap up the message, and send the email
                message.append("<p>" + pageMessages.getString("html_email_body_5") + "</P><P>" + pageMessages.getString("email_footer"));
                try {
                    mailSender.sendEmail(alertEmail.trim(), pageMessages.getString("job_ran_for") + " " + datasetBean.getName(), message.toString(), true);
                } catch (OpenClinicaSystemException ose) {
                    // Do Nothing, In the future we might want to have an email
                    // status added to system.
                }
                TriggerBean triggerBean = new TriggerBean();
                triggerBean.setDataset(datasetBean);
                triggerBean.setUserAccount(userBean);
                triggerBean.setFullName(trigger.getKey().getName());
                auditEventDAO.createRowForExtractDataJobSuccess(triggerBean, auditMessage.toString());
            } else {
                TriggerBean triggerBean = new TriggerBean();
                // triggerBean.setDataset(datasetBean);
                triggerBean.setUserAccount(userBean);
                triggerBean.setFullName(trigger.getKey().getName());
                auditEventDAO.createRowForExtractDataJobFailure(triggerBean);
                // logger.debug("-- made it here for some reason, ds id: "
                // + dsId);
            }

            // logger.debug("-- generated file: " + fileNameStr);
            // dataSource.
        } catch (Exception e) {
            // TODO Auto-generated catch block -- ideally should generate a fail
            // msg here, tbh 02/2009
            logger.debug("-- found exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getFileNameStr(HashMap fileName) {
        String fileNameStr = "";
        for (Iterator it = fileName.entrySet().iterator(); it.hasNext();) {
            java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            fileNameStr = (String) key;
            Integer fileID = (Integer) value;
            // fId = fileID.intValue();
        }
        return fileNameStr;
    }

    private int getFileIdInt(HashMap fileName) {
        // String fileNameStr = "";
        Integer fileID = new Integer(0);
        for (Iterator it = fileName.entrySet().iterator(); it.hasNext();) {
            java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            // fileNameStr = (String) key;
            fileID = (Integer) value;
            // fId = fileID.intValue();
        }
        return fileID.intValue();
    }

    private DataSource getDataSource(Scheduler scheduler) {
        try {
            ApplicationContext context = (ApplicationContext) scheduler.getContext().get("applicationContext");// dataMap
            // : (BasicDataSource) context.getBean("dataSource");
            dataSource = this.dataSource != null ? dataSource : (DataSource) context.getBean("dataSource"); // basicDataSource

        } catch (Exception e) {
            // TODO Auto-generated catch block
            // logger.debug("-- found an exception: " + e.getMessage());
            e.printStackTrace();
        }

        return dataSource;
    }

}
