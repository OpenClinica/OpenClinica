package org.akaza.openclinica.service.extract;

import java.math.BigInteger;

import org.akaza.openclinica.bean.extract.ExtractPropertyBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

public class XsltTriggerService {

    public XsltTriggerService() {

    }

    public static final String DATASET_ID = "dsId";
    public static final String EMAIL = "contactEmail";
    public static final String USER_ID = "user_id";
    public static final String XSL_FILE_PATH = "xslFilePath";
    public static final String XML_FILE_PATH = "xmlFilePath";
    public static final String POST_FILE_PATH = "postFilePath";
    public static final String POST_FILE_NAME = "postFileName";
    public static final String EXTRACT_PROPERTY = "extractProperty";
    public static final String LOCALE = "locale";
    public static final String STUDY_ID = "studyId";
    public static final String TENANT_SCHEMA = "tenantSchema";
    public static final String ZIPPED="zipped";
    public static final String DELETE_OLD="deleteOld";
    public static final String SUCCESS_MESSAGE="SUCCESS_MESSAGE";
    public static final String FAILURE_MESSAGE="FAILURE_MESSAGE";
    public static final String XSLT_PATH="XSLT_PATH";
    public static final String EP_BEAN="epBean";
    public static String TRIGGER_GROUP_NAME = "XsltTriggersExportJobs";
    public static final String PERIOD = "periodToRun";
    public static final String EXPORT_FORMAT = "exportFormat";
    public static final String EXPORT_FORMAT_ID = "exportFormatId";
    public static final String JOB_NAME = "jobName";

    //POST PROCESSING VARIABLES
    public static final String POST_PROC_DELETE_OLD="postProcDeleteOld";
    public static final String POST_PROC_ZIP="postProcZip";
    public static final String POST_PROC_LOCATION="postProcLocation";
    public static final String POST_PROC_EXPORT_NAME="postProcExportName";
    public static final String COUNT="count";


    public SimpleTrigger generateXsltTrigger(Scheduler scheduler, String xslFile, String xmlFile, String endFilePath,
            String endFile, int datasetId, ExtractPropertyBean epBean, UserAccountBean userAccountBean, String locale,int cnt, String xsltPath, String triggerGroupName,
            StudyBean currentPublicStudy,StudyBean currentStudy) {
        //Date startDateTime = new Date(System.currentTimeMillis());
        String jobName =  datasetId+ "_"+epBean.getExportFileName()[0];
        if(triggerGroupName!=null)
            TRIGGER_GROUP_NAME = triggerGroupName;

        //WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
        ApplicationContext context = null;
        try {
            context = (ApplicationContext) scheduler.getContext().get("applicationContext");
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        SimpleTriggerFactoryBean triggerFactoryBean = context.getBean(
                SimpleTriggerFactoryBean.class, xslFile, xmlFile, endFilePath, endFile, datasetId, epBean, userAccountBean, locale, cnt, xsltPath, currentPublicStudy, currentStudy);
        SimpleTrigger trigger = triggerFactoryBean.getObject();

        return trigger;
    }

    public static long getIntervalTime(String period) {
        BigInteger interval = new BigInteger("0");
        if ("monthly".equalsIgnoreCase(period)) {
            interval = new BigInteger("2419200000"); // how many
            // milliseconds in
            // a month? should
            // be 24192000000
        } else if ("weekly".equalsIgnoreCase(period)) {
            interval = new BigInteger("604800000"); // how many
            // milliseconds in
            // a week? should
            // be 6048000000
        } else { // daily
            interval = new BigInteger("86400000");// how many
            // milliseconds in a
            // day?
        }
        return interval.longValue();
    }

    public static int getIntervalTimeInSeconds(String period) {
        Integer interval = new Integer("0");
        if ("monthly".equalsIgnoreCase(period)) {
            interval = new Integer("2419200"); // how many
            // milliseconds in
            // a month? should
            // be 24192000000
        } else if ("weekly".equalsIgnoreCase(period)) {
            interval = new Integer("604800"); // how many
            // milliseconds in
            // a week? should
            // be 6048000000
        } else { // daily
            interval = new Integer("86400");// how many
            // milliseconds in a
            // day?
        }
        return interval.intValue();
    }

    public String getTriggerGroupNameForExportJobs()
    {
        return "XsltTriggersExportJobs";
    }

}
