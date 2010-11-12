package org.akaza.openclinica.job;

import org.akaza.openclinica.bean.extract.ArchivedDatasetFileBean;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.extract.ExportFormatBean;
import org.akaza.openclinica.bean.extract.ExtractBean;
import org.akaza.openclinica.bean.extract.ExtractPropertyBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.service.ProcessingFunction;
import org.akaza.openclinica.bean.service.ProcessingResultType;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.core.OpenClinicaMailSender;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.extract.ArchivedDatasetFileDAO;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.extract.GenerateExtractFileService;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.impl.StdScheduler;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.sql.DataSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Xalan Transform Job, an XSLT transform job using the Xalan classes
 * @author thickerson
 *
 */
public class XsltTransformJob extends QuartzJobBean {
    
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
    public static final String ZIPPED = "zipped";
    public static final String DELETE_OLD="deleteOld";
    public static final String SUCCESS_MESSAGE="SUCCESS_MESSAGE";
    public static final String FAILURE_MESSAGE="FAILURE_MESSAGE";
    private OpenClinicaMailSender mailSender;
    private DataSource dataSource;
    private GenerateExtractFileService generateFileService;
    private StudyDAO studyDao;
    private UserAccountDAO userAccountDao;
    private CoreResources coreResources;
    
    //POST PROCESSING VARIABLES
    public static final String POST_PROC_DELETE_OLD="postProcDeleteOld";
    public static final String POST_PROC_ZIP="postProcZip";
    public static final String POST_PROC_LOCATION="postProcLocation";
    public static final String POST_PROC_EXPORT_NAME="postProcExportName";
    
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        // need to generate a Locale for emailing users with i18n
        // TODO make dynamic?
        Locale locale = new Locale("en-US");
        ResourceBundleProvider.updateLocale(locale);
        ResourceBundle pageMessages = ResourceBundleProvider.getPageMessagesBundle();
        
        JobDataMap dataMap = context.getMergedJobDataMap();
        String localeStr = dataMap.getString(LOCALE);
        if (localeStr != null) {
            locale = new Locale(localeStr);
            ResourceBundleProvider.updateLocale(locale);
            pageMessages = ResourceBundleProvider.getPageMessagesBundle();
        }
        // get the file information from the job
        String alertEmail = dataMap.getString(EMAIL);
        
        try {
            ApplicationContext appContext = (ApplicationContext) context.getScheduler().getContext().get("applicationContext");
            mailSender = (OpenClinicaMailSender) appContext.getBean("openClinicaMailSender");
            dataSource = (DataSource) appContext.getBean("dataSource");
            coreResources = (CoreResources) appContext.getBean("coreResources");
            
            DatasetDAO dsdao = new DatasetDAO(dataSource);
            
            // init all fields from the data map
            int userAccountId = dataMap.getInt(USER_ID);
            int studyId = dataMap.getInt(STUDY_ID);
            String outputPath = dataMap.getString(POST_FILE_PATH);
            // get all user info, generate xml
            System.out.println("found output path: " + outputPath);
            String generalFileDir = dataMap.getString(XML_FILE_PATH);
            int epBeanId = dataMap.getInt(EXTRACT_PROPERTY);
            ExtractPropertyBean epBean = CoreResources.findExtractPropertyBeanById(epBeanId);
            
            long sysTimeBegin = System.currentTimeMillis();
            userAccountDao = new UserAccountDAO(dataSource);
            UserAccountBean userBean = (UserAccountBean)userAccountDao.findByPK(userAccountId);
            generateFileService = new GenerateExtractFileService(dataSource, userBean,coreResources);
            studyDao = new StudyDAO(dataSource);
            StudyBean currentStudy = (StudyBean)studyDao.findByPK(studyId);
            StudyBean parentStudy = (StudyBean)studyDao.findByPK(currentStudy.getParentStudyId());
            String successMsg = epBean.getSuccessMessage();
            String failureMsg = epBean.getFailureMessage();
        
            // DatasetBean dsBean = (DatasetBean)datasetDao.findByPK(new Integer(datasetId).intValue());
            int dsId = dataMap.getInt(DATASET_ID);
            DatasetBean datasetBean = (DatasetBean) dsdao.findByPK(dsId);
            ExtractBean eb = generateFileService.generateExtractBean(datasetBean, currentStudy, parentStudy);
            
            // generate file directory for file service
            
            datasetBean.setName(datasetBean.getName().replaceAll(" ", "_"));
            System.out.println("--> job starting: ");
            
            HashMap answerMap = generateFileService.createODMFile(epBean.getFormat(), sysTimeBegin, generalFileDir, datasetBean, 
                    currentStudy, "", eb, currentStudy.getId(), currentStudy.getParentStudyId(), "99",(Boolean) dataMap.get(ZIPPED), false, (Boolean) dataMap.get(DELETE_OLD));
            // won't save a record of the XML to db
            // won't be a zipped file, so that we can submit it for transformation
            // this will have to be toggled by the export data format? no, the export file will have to be zipped/not zipped
            String ODMXMLFileName = "";
            int fId = 0;
            for (Iterator it = answerMap.entrySet().iterator(); it.hasNext();) {
                java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
                Object key = entry.getKey();
                Object value = entry.getValue();
                ODMXMLFileName = (String) key;
                Integer fileID = (Integer) value;
                fId = fileID.intValue();
                System.out.println("found " + fId + " and " + ODMXMLFileName);
            }
            
            // create dirs 
            
            File output = new File(outputPath);
            if (!output.isDirectory()) {
                output.mkdirs();
            }
            TransformerFactory tFactory = TransformerFactory.newInstance();
            
            // Use the TransformerFactory to instantiate a Transformer that will work with  
            // the stylesheet you specify. This method call also processes the stylesheet
            // into a compiled Templates object.
            java.io.InputStream in = new java.io.FileInputStream(dataMap.getString(XSL_FILE_PATH));
            
            Transformer transformer = tFactory.newTransformer(new StreamSource(in));

            // move xml generation here, tbh
            String xmlFilePath = generalFileDir + ODMXMLFileName;
            String endFile = outputPath + File.separator + dataMap.getString(POST_FILE_NAME);
            final long start = System.currentTimeMillis();
            transformer.transform(new StreamSource(xmlFilePath), 
                    new StreamResult(new FileOutputStream(endFile)));
            final long done = System.currentTimeMillis() - start;
            System.out.println("--> job completed in " + done + " ms");
            // run post processing
            
            ProcessingFunction function = epBean.getPostProcessing();
            String subject = "";
            // String emailBody = "";
            StringBuffer emailBuffer = new StringBuffer("");
            emailBuffer.append("<p>" + pageMessages.getString("email_header_1") + " " + EmailEngine.getAdminEmail() + " "
                    + pageMessages.getString("email_header_2") + " Job Execution " + pageMessages.getString("email_header_3") + "</p>");
                emailBuffer.append("<P>Dataset: " + datasetBean.getName() + "</P>");
                emailBuffer.append("<P>Study: " + currentStudy.getName() + "</P>");
                emailBuffer.append("<p>" + pageMessages.getString("html_email_body_1") + datasetBean.getName() + pageMessages.getString("html_email_body_2_2") + "</p>");
            if (function != null) {
                function.setTransformFileName(outputPath + File.separator + dataMap.getString(POST_FILE_NAME));
                function.setODMXMLFileName(endFile);
                function.setXslFileName(dataMap.getString(XSL_FILE_PATH));
                function.setDeleteOld((Boolean)dataMap.get(POST_PROC_DELETE_OLD));
                function.setZip((Boolean)dataMap.get(POST_PROC_ZIP));
                function.setLocation(dataMap.getString(POST_PROC_LOCATION));
                function.setExportFileName(dataMap.getString(POST_PROC_EXPORT_NAME));
                File oldFiles[] = getOldFiles(outputPath,dataMap.getString(POST_PROC_LOCATION));
                function.setOldFiles(oldFiles);
                ProcessingResultType message = function.run();
                final long done2 = System.currentTimeMillis() - start;
                System.out.println("--> postprocessing completed in " + done2 + " ms, found result type " + message.getCode());
              
              /*  if((Boolean)dataMap.get(POST_PROC_DELETE_OLD))
                {
                	deleteOldFiles(oldFiles);
                }*/
                
                if (!function.getClass().equals(org.akaza.openclinica.bean.service.SqlProcessingFunction.class)) {
                    ArchivedDatasetFileBean fbFinal = generateFileRecord(dataMap.getString(POST_FILE_NAME) + "." + function.getFileType(), 
                            outputPath, 
                            datasetBean, 
                            done, new File(endFile).length(), 
                            ExportFormatBean.PDFFILE,
                            userAccountId);
               
                    if(successMsg==null || successMsg.isEmpty())
                    {
                    	emailBuffer.append("<p>" + pageMessages.getString("html_email_body_4") + " " + fbFinal.getName()
                                + pageMessages.getString("html_email_body_4_5") + CoreResources.getField("sysURL.base") + "AccessFile?fileId="
                                + fbFinal.getId() + pageMessages.getString("html_email_body_3") + "</p>");
                    	 
                    }
                    else if(!successMsg.isEmpty()){
                     	if(successMsg.contains("$linkURL"))
                	  {
                     		successMsg =		successMsg.replace("$linkURL", "<a href=\""+CoreResources.getField("sysURL.base") + "AccessFile?fileId="+ fbFinal.getId()+"\">here </a>");
                	  }
                     	emailBuffer.append("<p>" + successMsg + "</p>");
                    }
                }
                // otherwise don't do it
                if (message.getCode().intValue() == 1) {
                    subject = "Success: " + datasetBean.getName(); 
                } else if (message.getCode().intValue() == 2) { 
                    subject = "Failure: " + datasetBean.getName();
                    if(failureMsg!=null || !failureMsg.equals(""))
                    {
                    	emailBuffer.append(failureMsg);
                    }
                    emailBuffer.append("<P>" + message.getDescription());
                    postErrorMessage(message.getDescription(), context);
                } else if (message.getCode().intValue() == 3) {
                    subject = "Update: " + datasetBean.getName();
                }
                    // subject = "" + datasetBean.getName();
                
            } else {
                // extract ran but no post-processing - we send an email with success and url to link to
                // generate archived dataset file bean here, and use the id to build the URL
                ArchivedDatasetFileBean fbFinal = generateFileRecord(dataMap.getString(POST_FILE_NAME), 
                        outputPath, 
                        datasetBean, 
                        done, new File(endFile).length(), 
                        ExportFormatBean.TXTFILE,
                        userAccountId);
                subject = "Job Ran: " + datasetBean.getName();
                //                emailBody = datasetBean.getName() + " has run and you can access it ";// add url here
                //                emailBody = emailBody + "<a href='" + 
                //                    CoreResources.getField("sysURL.base") + 
                //                    "AccessFile?fileId=" + 
                //                    fbFinal.getId() + "'>here</a>.";
            
                if(successMsg!=null || !successMsg.equals(""))
                {
                	  	if(successMsg.contains("$linkURL"))
                	  		successMsg =		successMsg.replace("$linkURL", "<a href="+CoreResources.getField("sysURL.base") + "AccessFile?fileId="+ fbFinal.getId()+">here </a>");
                	emailBuffer.append("<p>" + successMsg+ pageMessages.getString("html_email_body_3") + "</p>");
                }
                else{
                emailBuffer.append("<p>" + pageMessages.getString("html_email_body_4") + " " + fbFinal.getName()
                        + pageMessages.getString("html_email_body_4_5") + CoreResources.getField("sysURL.base") + "AccessFile?fileId="
                        + fbFinal.getId() + pageMessages.getString("html_email_body_3") + "</p>");
                }
            }
            // email the message to the user
            // String email = dataMap.getString(EMAIL);
            emailBuffer.append("<p>" + pageMessages.getString("html_email_body_5") + "</p>");
            try {
                mailSender.sendEmail(alertEmail, EmailEngine.getAdminEmail(), subject, emailBuffer.toString(), true);
            } catch (OpenClinicaSystemException ose) {
                // Do Nothing, In the future we might want to have an email
                // status added to system.
                System.out.println("exception sending mail: " + ose.getMessage());
            }
            
            System.out.println("just sent email to " + alertEmail + ", from " + EmailEngine.getAdminEmail());
              
        } catch (TransformerConfigurationException e) {
            sendErrorEmail(e.getMessage(), context, alertEmail);
            postErrorMessage(e.getMessage(), context);

            e.printStackTrace();
        } catch (FileNotFoundException e) {
            sendErrorEmail(e.getMessage(), context, alertEmail);
            postErrorMessage(e.getMessage(), context);
            e.printStackTrace();
        } catch (TransformerFactoryConfigurationError e) {
            sendErrorEmail(e.getMessage(), context, alertEmail);
            postErrorMessage(e.getMessage(), context);
            e.printStackTrace();
        } catch (TransformerException e) {
            sendErrorEmail(e.getMessage(), context, alertEmail);
            postErrorMessage(e.getMessage(), context);
            e.printStackTrace();
        } catch (Exception ee) {
            sendErrorEmail(ee.getMessage(), context, alertEmail);
            postErrorMessage(ee.getMessage(), context);
            ee.printStackTrace();
        }
        
        
    }
    //A stub to delete old files.
    private void deleteOldFiles(File[] oldFiles) {
    	//File[] files = complete.listFiles();
		for(int i=0;i<oldFiles.length;i++)
		{

			oldFiles[i].delete();
		}
		
	}
    /**
     * Stub to get the list of all old files.
     * @param outputPath
     * @param postProcLoc
     * @return
     */
	private File[] getOldFiles(String outputPath, String postProcLoc) {
    	File exisitingFiles[] = null;
    	File temp = null;
    	if(postProcLoc!=null)
    		{
    		temp = new File(postProcLoc);
    		if(temp.isDirectory())exisitingFiles = temp.listFiles();
    		}
    	else{
    		temp = new File(outputPath);
    		if(temp.isDirectory())exisitingFiles = temp.listFiles();
    	}
    	
    	return exisitingFiles;
	}

	private void postErrorMessage(String message, JobExecutionContext context) {
        String SCHEDULER = "schedulerFactoryBean";
        try {
            ApplicationContext appContext = (ApplicationContext) context.getScheduler().getContext().get("applicationContext");
            StdScheduler scheduler = (StdScheduler) appContext.getBean(SCHEDULER);
            JobDetail jobDetail = context.getJobDetail();
            JobDataMap dataMap = jobDetail.getJobDataMap();
            dataMap.put("failMessage", message);
            jobDetail.setJobDataMap(dataMap);
            // replace the job with the extra data
            scheduler.addJob(jobDetail, true);
            
        } catch (Exception e) {
            
        }
    }
    
    private ArchivedDatasetFileBean generateFileRecord(String name, 
            String dir,  
            DatasetBean datasetBean, 
            long time, long fileLength, ExportFormatBean efb, 
            int userBeanId) {
        ArchivedDatasetFileBean fbFinal = new ArchivedDatasetFileBean();
        
        ArchivedDatasetFileBean fbInitial = new ArchivedDatasetFileBean();
        
        fbInitial.setName(name);
        fbInitial.setFileReference(dir + name);
        
        fbInitial.setFileSize((int) fileLength);
        // logger.info("ODM setFileSize: " + (int)newFile.length() );
        // set the above to compressed size?
        fbInitial.setRunTime((int) time);
        // logger.info("ODM setRunTime: " + (int)time );
        // need to set this in milliseconds, get it passed from above
        // methods?
        fbInitial.setDatasetId(datasetBean.getId());
        // logger.info("ODM setDatasetid: " + ds.getId() );
        fbInitial.setExportFormatBean(efb);
        // logger.info("ODM setExportFormatBean: success" );
        fbInitial.setExportFormatId(efb.getId());
        // logger.info("ODM setExportFormatId: " + efb.getId());
        // fbInitial.setOwner(userBean);
        // logger.info("ODM setOwner: " + sm.getUserBean());
        fbInitial.setOwnerId(userBeanId);
        // logger.info("ODM setOwnerId: " + sm.getUserBean().getId() );
        fbInitial.setDateCreated(new Date(System.currentTimeMillis()));
        ArchivedDatasetFileDAO asdfDAO = new ArchivedDatasetFileDAO(dataSource);
        fbFinal = (ArchivedDatasetFileBean)asdfDAO.create(fbInitial);
        return fbFinal;
    }
    
    private void sendErrorEmail(String message, JobExecutionContext context, String target) {
        String subject = "Warning: " + message;
        String emailBody = "An exception was thrown while running an extract job on your server, please see the logs for more details.";
        try {
            ApplicationContext appContext = (ApplicationContext) context.getScheduler().getContext().get("applicationContext");
            mailSender = (OpenClinicaMailSender) appContext.getBean("openClinicaMailSender");
            
            mailSender.sendEmail(target, EmailEngine.getAdminEmail(), subject, emailBody, false);
            System.out.println("sending an email to " + target + " from " + EmailEngine.getAdminEmail());
        } catch (SchedulerException se) {
            se.printStackTrace();
        } catch (OpenClinicaSystemException ose) {
            ose.printStackTrace();
        }
        
        
    }
}
