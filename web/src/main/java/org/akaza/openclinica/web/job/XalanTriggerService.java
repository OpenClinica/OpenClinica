package org.akaza.openclinica.web.job;

import org.quartz.JobDataMap;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.spi.MutableTrigger;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

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

        SimpleTrigger simpleTrigger = (SimpleTrigger) newTrigger()
                .forJob(jobName, TRIGGER_GROUP_NAME)
                .startAt(startDateTime)
                .withSchedule(simpleSchedule().withRepeatCount(1).withIntervalInSeconds(100)
                .withMisfireHandlingInstructionNextWithExistingCount());

        // set job data map
        JobDataMap jobDataMap = new JobDataMap();

        // jobDataMap.put(EMAIL, email);
        // jobDataMap.put(USER_ID, userAccount.getId());
        jobDataMap.put(XSL_FILE_PATH, xslFile);
        jobDataMap.put(XML_FILE_PATH, xmlFile);
        jobDataMap.put(SQL_FILE_PATH, sqlFile);
        // jobDataMap.put(DIRECTORY, directory);
        // jobDataMap.put(ExampleSpringJob.LOCALE, locale);

        simpleTrigger.getTriggerBuilder().usingJobData(jobDataMap);
        
        return simpleTrigger;
    }
}
