package org.akaza.openclinica.service.extract;

import org.akaza.openclinica.bean.extract.ExtractPropertyBean;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.quartz.JobDataMap;
import org.quartz.SimpleTrigger;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.File;
import java.math.BigInteger;

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

    /**
     * Returns the datetime based on pattern :"yyyy-MM-dd-HHmmssSSS", typically for resolving file name
     * @param endFilePath
     * @param dsBean
     * @param sdfDir
     * @return
     */
    public static String resolveVars(String endFilePath, DatasetBean dsBean, SimpleDateFormat sdfDir, String filePath){

        if(endFilePath.contains("$exportFilePath")) {
            endFilePath = 	endFilePath.replace("$exportFilePath", filePath);// was + File.separator, tbh
        }

         if(endFilePath.contains("${exportFilePath}")) {
            endFilePath = 	endFilePath.replace("${exportFilePath}", filePath);// was + File.separator, tbh
        }
        if(endFilePath.contains("$datasetId")) {
        	endFilePath = endFilePath.replace("$datasetId", dsBean.getId()+"");
        }
        if(endFilePath.contains("${datasetId}")) {
         	endFilePath = endFilePath.replace("${datasetId}", dsBean.getId()+"");
         }
        if(endFilePath.contains("$datasetName")) {
        	endFilePath = endFilePath.replace("$datasetName", dsBean.getName());
        }
        if(endFilePath.contains("${datasetName}"))
       		 {
       	 endFilePath = endFilePath.replace("${datasetName}", dsBean.getName());
       		 }
        if(endFilePath.contains("$datetime")) {
       	 String simpleDatePattern = "yyyy-MM-dd-HHmmssSSS";
             sdfDir = new SimpleDateFormat(simpleDatePattern);
       	endFilePath = endFilePath.replace("$datetime",  sdfDir.format(new java.util.Date()));
       }
       if(endFilePath.contains("${datetime}")){
       	 String simpleDatePattern = "yyyy-MM-dd-HHmmssSSS";
            sdfDir = new SimpleDateFormat(simpleDatePattern);
      		endFilePath = endFilePath.replace("${datetime}",  sdfDir.format(new java.util.Date()));
       }
       if(endFilePath.contains("$dateTime")) {
      	 String simpleDatePattern = "yyyy-MM-dd-HHmmssSSS";
        sdfDir = new SimpleDateFormat(simpleDatePattern);
        	endFilePath = endFilePath.replace("$dateTime",  sdfDir.format(new java.util.Date()));
        }
        if(endFilePath.contains("${dateTime}")){
       	 String simpleDatePattern = "yyyy-MM-dd-HHmmssSSS";
            sdfDir = new SimpleDateFormat(simpleDatePattern);
       		endFilePath = endFilePath.replace("${dateTime}",  sdfDir.format(new java.util.Date()));
        }
        if(endFilePath.contains("$date")) {
        	 String dateFilePattern = "yyyy-MM-dd";
        	  sdfDir = new SimpleDateFormat(dateFilePattern);
        	endFilePath = endFilePath.replace("$date",sdfDir.format(new java.util.Date()) );
        }
        if(endFilePath.contains("${date}"))
        {
        	 String dateFilePattern = "yyyy-MM-dd";
        	  sdfDir = new SimpleDateFormat(dateFilePattern);
       	 endFilePath = endFilePath.replace("${date}",sdfDir.format(new java.util.Date()) );
        }
        //TODO change to dateTime

   	return endFilePath;
   }

    //TODO: ${linkURL} needs to be added
    /**
     *
     * for dateTimePattern, the directory structure is created. "yyyy" + File.separator + "MM" + File.separator + "dd" + File.separator,
     * to resolve location
     */
    public static String getEndFilePath(String endFilePath, DatasetBean dsBean, SimpleDateFormat sdfDir, String filePath){
    	 String simpleDatePattern =  "yyyy" + File.separator + "MM" + File.separator + "dd" + File.separator ;
         SimpleDateFormat sdpDir = new SimpleDateFormat(simpleDatePattern);


         if(endFilePath.contains("$exportFilePath")) {
             endFilePath = 	endFilePath.replace("$exportFilePath", filePath);// was + File.separator, tbh
         }

          if(endFilePath.contains("${exportFilePath}")) {
             endFilePath = 	endFilePath.replace("${exportFilePath}", filePath);// was + File.separator, tbh
         }
         if(endFilePath.contains("$datasetId")) {
         	endFilePath = endFilePath.replace("$datasetId", dsBean.getId()+"");
         }
         if(endFilePath.contains("${datasetId}")) {
          	endFilePath = endFilePath.replace("${datasetId}", dsBean.getId()+"");
          }
         if(endFilePath.contains("$datasetName")) {
         	endFilePath = endFilePath.replace("$datasetName", dsBean.getName());
         }
         if(endFilePath.contains("${datasetName}"))
        		 {
        	 endFilePath = endFilePath.replace("${datasetName}", dsBean.getName());
        		 }
         //TODO change to dateTime
         if(endFilePath.contains("$datetime")) {
         	endFilePath = endFilePath.replace("$datetime",  sdfDir.format(new java.util.Date()));
         }
         if(endFilePath.contains("${datetime}")){
        		endFilePath = endFilePath.replace("${datetime}",  sdfDir.format(new java.util.Date()));
         }
         if(endFilePath.contains("$dateTime")) {
          	endFilePath = endFilePath.replace("$dateTime",  sdfDir.format(new java.util.Date()));
          }
          if(endFilePath.contains("${dateTime}")){
         		endFilePath = endFilePath.replace("${dateTime}",  sdfDir.format(new java.util.Date()));
          }
         if(endFilePath.contains("$date")) {

         	endFilePath = endFilePath.replace("$date",sdpDir.format(new java.util.Date()) );
         }
         if(endFilePath.contains("${date}"))
         {
        	 endFilePath = endFilePath.replace("${date}",sdpDir.format(new java.util.Date()) );
         }

    	return endFilePath;
    }

    public static ExtractPropertyBean setAllProps(ExtractPropertyBean epBean,DatasetBean dsBean,SimpleDateFormat sdfDir, String filePath) {
    	epBean.setFiledescription(XsltTriggerService.resolveVars(epBean.getFiledescription(), dsBean, sdfDir, filePath));
    	epBean.setLinkText(XsltTriggerService.resolveVars(epBean.getLinkText(), dsBean, sdfDir, filePath));
    	epBean.setHelpText(XsltTriggerService.resolveVars(epBean.getHelpText(), dsBean, sdfDir, filePath));
    	epBean.setFileLocation(XsltTriggerService.resolveVars(epBean.getFileLocation(), dsBean, sdfDir, filePath));
    	epBean.setFailureMessage(XsltTriggerService.resolveVars(epBean.getFailureMessage(), dsBean, sdfDir, filePath));
    	epBean.setSuccessMessage(XsltTriggerService.resolveVars(epBean.getSuccessMessage(), dsBean, sdfDir, filePath));
    	epBean.setZipName(XsltTriggerService.resolveVars(epBean.getZipName(), dsBean, sdfDir, filePath));
    	return epBean;
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

}
