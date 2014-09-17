package org.akaza.openclinica.web.job;

import org.quartz.JobDataMap;
import org.quartz.SimpleTrigger;

import java.util.Date;

public class XalanTriggerService {
    public XalanTriggerService() {
        
    }

    public static final String DATASET_ID = "dsId";
    public static final String EMAIL = "contactEmail";
    public static final String USER_ID = "user_id";
    public static final String XSL_FILE_PATH = "xslFilePath";
    public static final String XML_FILE_PATH = "xmlFilePath";
    public static final String SQL_FILE_PATH = "sqlFilePath";
    
    public static String TRIGGER_GROUP_NAME = "XalanTriggers";
    
    public SimpleTrigger generateXalanTrigger(String xslFile, String xmlFile, String sqlFile, int datasetId) {
        Date startDateTime = new Date(System.currentTimeMillis());
        String jobName = xmlFile + datasetId;
        SimpleTrigger trigger = new SimpleTrigger(jobName, TRIGGER_GROUP_NAME, 1, 1);
        
        trigger.setStartTime(startDateTime);
        trigger.setName(jobName);// + datasetId);
        trigger.setGroup(TRIGGER_GROUP_NAME);// + datasetId);
        trigger.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT);
        // set job data map
        JobDataMap jobDataMap = new JobDataMap();

        // jobDataMap.put(EMAIL, email);
        // jobDataMap.put(USER_ID, userAccount.getId());
        jobDataMap.put(XSL_FILE_PATH, xslFile);
        jobDataMap.put(XML_FILE_PATH, xmlFile);
        jobDataMap.put(SQL_FILE_PATH, sqlFile);
        // jobDataMap.put(DIRECTORY, directory);
        // jobDataMap.put(ExampleSpringJob.LOCALE, locale);
        
        trigger.setJobDataMap(jobDataMap);
        trigger.setVolatility(false);
        
        return trigger;
    }
}
