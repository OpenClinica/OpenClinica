package org.akaza.openclinica.service.extract;

import org.akaza.openclinica.bean.extract.ExtractPropertyBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.quartz.JobDataMap;
import org.quartz.SimpleTrigger;

import java.util.Date;

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
    public static final String ZIPPED="zipped";
    public static final String DELETE_OLD="deleteOld";
    public static final String SUCCESS_MESSAGE="SUCCESS_MESSAGE";
    public static final String FAILURE_MESSAGE="FAILURE_MESSAGE";
    public static final String XSLT_PATH="XSLT_PATH";
    public static final String EP_BEAN="epBean";
    public static String TRIGGER_GROUP_NAME = "XsltTriggers";
    
    //POST PROCESSING VARIABLES
    public static final String POST_PROC_DELETE_OLD="postProcDeleteOld";
    public static final String POST_PROC_ZIP="postProcZip";
    public static final String POST_PROC_LOCATION="postProcLocation";
    public static final String POST_PROC_EXPORT_NAME="postProcExportName";
    public static final String COUNT="count";
    
    public SimpleTrigger generateXsltTrigger(String xslFile, String xmlFile, String endFilePath, 
            String endFile, int datasetId, ExtractPropertyBean epBean, UserAccountBean userAccountBean, String locale,int cnt, String xsltPath) {
        Date startDateTime = new Date(System.currentTimeMillis());
        String jobName = xmlFile + datasetId;
        SimpleTrigger trigger = new SimpleTrigger(jobName, TRIGGER_GROUP_NAME, 0, 1);
        
        trigger.setStartTime(startDateTime);
        trigger.setName(jobName);// + datasetId);
        trigger.setGroup(TRIGGER_GROUP_NAME);// + datasetId);
        trigger.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT);
        // set job data map
        JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put(XSL_FILE_PATH, xslFile);
        jobDataMap.put(XML_FILE_PATH, endFilePath);
        jobDataMap.put(POST_FILE_PATH, endFilePath);
        jobDataMap.put(POST_FILE_NAME, endFile);
        
        jobDataMap.put(EXTRACT_PROPERTY, epBean.getId());
        jobDataMap.put(USER_ID, userAccountBean.getId());
        jobDataMap.put(STUDY_ID, userAccountBean.getActiveStudyId());
        jobDataMap.put(LOCALE, locale);
        jobDataMap.put(DATASET_ID, datasetId);
        jobDataMap.put(EMAIL, userAccountBean.getEmail());
        jobDataMap.put(ZIPPED,epBean.getZipFormat());
        jobDataMap.put(DELETE_OLD,epBean.getDeleteOld());
        jobDataMap.put(SUCCESS_MESSAGE,epBean.getSuccessMessage());
        jobDataMap.put(FAILURE_MESSAGE,epBean.getFailureMessage());
        
        jobDataMap.put(POST_PROC_DELETE_OLD, epBean.getPostProcDeleteOld());
        jobDataMap.put(POST_PROC_ZIP, epBean.getPostProcZip());
        jobDataMap.put(POST_PROC_LOCATION, epBean.getPostProcLocation());
        jobDataMap.put(POST_PROC_EXPORT_NAME, epBean.getPostProcExportName());
        jobDataMap.put(COUNT,cnt);
        jobDataMap.put(XSLT_PATH,xsltPath);
        // jobDataMap.put(DIRECTORY, directory);
        // jobDataMap.put(ExampleSpringJob.LOCALE, locale);
        jobDataMap.put(EP_BEAN, epBean);
        
        trigger.setJobDataMap(jobDataMap);
        trigger.setVolatility(false);
        
        return trigger;
    }
}
