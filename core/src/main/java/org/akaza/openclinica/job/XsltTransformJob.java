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
import org.akaza.openclinica.service.extract.GenerateExtractFileService;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.core.OpenClinicaMailSender;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.extract.ArchivedDatasetFileDAO;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.JobDetailBean;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import javax.sql.DataSource;

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
    
    private OpenClinicaMailSender mailSender;
    private DataSource dataSource;
    private GenerateExtractFileService generateFileService;
    private StudyDAO studyDao;
    private UserAccountDAO userAccountDao;
    
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        // need to generate a Locale for emailing users with i18n
        // TODO make dynamic?
        Locale locale = new Locale("en-US");
        ResourceBundleProvider.updateLocale(locale);
        ResourceBundle pageMessages = ResourceBundleProvider.getPageMessagesBundle();
        JobDataMap dataMap = context.getMergedJobDataMap();
        // get the file information from the job
        String alertEmail = dataMap.getString(EMAIL);
        try {
            ApplicationContext appContext = (ApplicationContext) context.getScheduler().getContext().get("applicationContext");
            mailSender = (OpenClinicaMailSender) appContext.getBean("openClinicaMailSender");
            dataSource = (DataSource) appContext.getBean("dataSource");
            DatasetDAO dsdao = new DatasetDAO(dataSource);
            
            // init all fields from the data map
            int userAccountId = dataMap.getInt(USER_ID);
            String outputPath = dataMap.getString(POST_FILE_PATH);
            // get all user info, generate xml
            System.out.println("found output path: " + outputPath);
            String generalFileDir = dataMap.getString(XML_FILE_PATH);
            
            long sysTimeBegin = System.currentTimeMillis();
            userAccountDao = new UserAccountDAO(dataSource);
            UserAccountBean userBean = (UserAccountBean)userAccountDao.findByPK(userAccountId);
            generateFileService = new GenerateExtractFileService(dataSource, userBean);
            studyDao = new StudyDAO(dataSource);
            StudyBean currentStudy = (StudyBean)studyDao.findByPK(userBean.getActiveStudyId());
            StudyBean parentStudy = (StudyBean)studyDao.findByPK(currentStudy.getParentStudyId());
            // TODO need to get the current study and parent study, somehow
            // DatasetBean dsBean = (DatasetBean)datasetDao.findByPK(new Integer(datasetId).intValue());
            int dsId = dataMap.getInt(DATASET_ID);
            DatasetBean datasetBean = (DatasetBean) dsdao.findByPK(dsId);
            ExtractBean eb = generateFileService.generateExtractBean(datasetBean, currentStudy, parentStudy);
            
            // generate file directory for file service
            
            datasetBean.setName(datasetBean.getName().replaceAll(" ", "_"));
            System.out.println("--> job starting: ");
            HashMap answerMap = generateFileService.createODMFile("oc1.3", sysTimeBegin, generalFileDir, datasetBean, 
                    currentStudy, "", eb, currentStudy.getId(), currentStudy.getParentStudyId(), "99", false, false);
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
            int epBeanId = dataMap.getInt(EXTRACT_PROPERTY);
            ExtractPropertyBean epBean = CoreResources.findExtractPropertyBeanById(epBeanId);
            ProcessingFunction function = epBean.getPostProcessing();
            String subject = "";
            String emailBody = "";
            StringBuffer emailBuffer = new StringBuffer("");
            
            if (function != null) {
                function.setTransformFileName(outputPath + File.separator + dataMap.getString(POST_FILE_NAME));
                function.setODMXMLFileName(endFile);
                function.setXslFileName(dataMap.getString(XSL_FILE_PATH));
                ProcessingResultType message = function.run();
                final long done2 = System.currentTimeMillis() - start;
                System.out.println("--> postprocessing completed in " + done2 + " ms, found result type " + message.getCode());
                emailBody = message.getDescription();
                emailBuffer.append("<p>" + pageMessages.getString("email_header_1") + " " + EmailEngine.getAdminEmail() + " "
                        + pageMessages.getString("email_header_2") + " Job Execution " + pageMessages.getString("email_header_3") + "</p>");
                    emailBuffer.append("<P>Dataset: " + datasetBean.getName() + "</P>");
                    emailBuffer.append("<P>Study: " + currentStudy.getName() + "</P>");
                    emailBuffer.append("<p>" + pageMessages.getString("html_email_body_1") + datasetBean.getName() + pageMessages.getString("html_email_body_2")
                        + CoreResources.getField("sysURL") + pageMessages.getString("html_email_body_3") + "</p>");
                // if the process was a pdf, generate a link to the file
                if (!("").equals(message.getUrl()) && function.getClass().equals(org.akaza.openclinica.bean.service.PdfProcessingFunction.class)) {
                    ArchivedDatasetFileBean fbFinal = generateFileRecord(dataMap.getString(POST_FILE_NAME) + ".pdf", 
                            outputPath, 
                            datasetBean, 
                            done, new File(endFile).length(), ExportFormatBean.PDFFILE,
                            userAccountId);
                    emailBody = emailBody + "<p><a href='" + message.getUrl() + fbFinal.getId() + "'>" + epBean.getLinkText() + "</a><br/>";
                }
                // otherwise don't do it
                if (message.getCode().intValue() == 1) {
                    subject = "Success: " + datasetBean.getName(); 
                } else if (message.getCode().intValue() == 2) { 
                    subject = "Failure: " + datasetBean.getName();
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
                        done, new File(endFile).length(), ExportFormatBean.TXTFILE,
                        userAccountId);
                subject = "Job Ran: " + datasetBean.getName();
                emailBody = datasetBean.getName() + " has run and you can access it ";// add url here
                emailBody = emailBody + "<a href='" + 
                    CoreResources.getField("sysURL.base") + 
                    "AccessFile?fileId=" + 
                    fbFinal.getId() + "'>here</a>.";
                emailBuffer.append("<p>" + pageMessages.getString("html_email_body_4") + " " + ODMXMLFileName
                        + pageMessages.getString("html_email_body_4_5") + CoreResources.getField("sysURL.base") + "AccessFile?fileId="
                        + fId + pageMessages.getString("html_email_body_3") + "</p>");
            }
            // email the message to the user
            // TODO do we need user id?
            
            // UserAccountBean userAccount = (UserAccountBean);
            String email = dataMap.getString(EMAIL);
            
            try {
                mailSender.sendEmail(email, EmailEngine.getAdminEmail(), subject, emailBuffer.toString(), true);
            } catch (OpenClinicaSystemException ose) {
                // Do Nothing, In the future we might want to have an email
                // status added to system.
                System.out.println("exception sending mail: " + ose.getMessage());
            }
            
            System.out.println("just sent email to " + email + ", from " + EmailEngine.getAdminEmail());
              
        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            sendErrorEmail(e.getMessage(), context);
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            sendErrorEmail(e.getMessage(), context);
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerFactoryConfigurationError e) {
            sendErrorEmail(e.getMessage(), context);
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            sendErrorEmail(e.getMessage(), context);
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception ee) {
            sendErrorEmail(ee.getMessage(), context);
            ee.printStackTrace();
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
    private void sendErrorEmail(String message, JobExecutionContext context) {
        String subject = "Warning: " + message;
        String emailBody = "An exception was thrown while running an extract job on your server, please see the logs for more details.";
        try {
            ApplicationContext appContext = (ApplicationContext) context.getScheduler().getContext().get("applicationContext");
            mailSender = (OpenClinicaMailSender) appContext.getBean("openClinicaMailSender");
            
            mailSender.sendEmail(EmailEngine.getAdminEmail(), EmailEngine.getAdminEmail(), subject, emailBody, false);
            
        } catch (SchedulerException se) {
            se.printStackTrace();
        } catch (OpenClinicaSystemException ose) {
            ose.printStackTrace();
        }
        
        
    }
}
