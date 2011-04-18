package org.akaza.openclinica.job;

import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.JobDetailBean;
import org.springframework.context.ApplicationContext;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.akaza.openclinica.service.extract.ExtractUtils;
import org.akaza.openclinica.service.extract.XsltTriggerService;
import org.akaza.openclinica.bean.extract.ExtractPropertyBean;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.core.OpenClinicaMailSender;

import javax.sql.DataSource;
import java.io.File;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * Converts all old jobs created under DEFAULT group, to make it use new XSLT transformation code
 *
 * @author: sshamim
 * Date: Apr 1, 2011
 */
public class LegacyJobConverterJob extends QuartzJobBean {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private DataSource dataSource = null;
    private OpenClinicaMailSender mailSender = null;
    private CoreResources coreResources = null;

    public static final String USER_ID = "user_id";
    public static final String DATASET_ID = "dsId";
    public static final String EMAIL = "contactEmail";
    public static final String JOB_NAME = "jobName";
    public static final String JOB_DESC = "jobDesc";
    public static final String PERIOD = "periodToRun";

    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            //Pulling all the trigger under the DEFAULT group
            Scheduler scheduler = context.getScheduler();
            String triggerGroup = "DEFAULT";
            String[] legacyTriggers = scheduler.getTriggerNames(triggerGroup);
ExtractUtils extractUtils = new ExtractUtils();
            if (legacyTriggers == null && legacyTriggers.length == 0) {
                logger.info("No legacy jobs to convert");
                return;
            }

            mailSender = (OpenClinicaMailSender) getApplicationContext(context).getBean("openClinicaMailSender");
            dataSource = (DataSource) getApplicationContext(context).getBean("dataSource");
            coreResources = (CoreResources) getApplicationContext(context).getBean("coreResources");

            for (String triggerName : legacyTriggers) {
                Trigger trigger = scheduler.getTrigger(triggerName, triggerGroup);
                JobDataMap dataMap = trigger.getJobDataMap();

                DatasetDAO datasetDao = new DatasetDAO(dataSource);
                int datasetId = dataMap.getInt(DATASET_ID);
                int userId = dataMap.getInt(USER_ID);
                String period = dataMap.getString(PERIOD);
                String email = dataMap.getString(EMAIL);
                String jobName = dataMap.getString(JOB_NAME);
                if (jobName == null || jobName.length() == 0) {
                    jobName = "LegacyJob";
                }
                String jobDesc = dataMap.getString(JOB_DESC);

                UserAccountBean userBean = new UserAccountBean();
                userBean.setId(userId);
                Map<String, Integer> exportingFormats = getExportingformats(dataMap);

                for (Iterator<String> iterator = exportingFormats.keySet().iterator(); iterator.hasNext();) {
                    String exportFormat = iterator.next();
                    Integer exportFormatId = exportingFormats.get(exportFormat);
                    ExtractPropertyBean epBean = coreResources.findExtractPropertyBeanById(exportFormatId, "" + datasetId);
                    DatasetBean dsBean = (DatasetBean)datasetDao.findByPK(new Integer(datasetId).intValue());
                    String[] files = epBean.getFileName();

                    String exportFileName;
                    int  cnt = 0;
                    dsBean.setName(dsBean.getName().replaceAll(" ", "_"));
                    String[] exportFiles= epBean.getExportFileName();
                    String pattern = "yyyy" + File.separator + "MM" + File.separator + "dd" + File.separator + "HHmmssSSS" + File.separator;
                    SimpleDateFormat sdfDir = new SimpleDateFormat(pattern);
                    int i =0;
                    String[] temp = new String[exportFiles.length];
                    String datasetFilePath = getFilePath(context) + "datasets";

                    while(i<exportFiles.length) {
                        temp[i] = extractUtils.resolveVars(exportFiles[i],dsBean,sdfDir, datasetFilePath);
                        i++;
                    }
                    epBean.setDoNotDelFiles(temp);
                    epBean.setExportFileName(temp);

                    String generalFileDir = getFilePath(context);
                    generalFileDir = generalFileDir + "datasets" + File.separator + dsBean.getId() + File.separator + sdfDir.format(new java.util.Date());
                    exportFileName = epBean.getExportFileName()[cnt];

                    String xsltPath = getFilePath(context) + "xslt" + File.separator + files[0];
                    String endFilePath = epBean.getFileLocation();
                    endFilePath  =  extractUtils.getEndFilePath(endFilePath, dsBean, sdfDir, datasetFilePath);

                    if(epBean.getPostProcExportName() !=null ) {
                        String preProcExportPathName = extractUtils.resolveVars(epBean.getPostProcExportName(),dsBean,sdfDir, datasetFilePath);
                        epBean.setPostProcExportName(preProcExportPathName);
                    }
                    if (epBean.getPostProcLocation()!=null) {
                        String prePocLoc = extractUtils.getEndFilePath(epBean.getPostProcLocation(), dsBean, sdfDir, datasetFilePath);
                        epBean.setPostProcLocation(prePocLoc);
                    }

                    extractUtils.setAllProps(epBean, dsBean, sdfDir, datasetFilePath);
                    XsltTriggerService xsltService = new XsltTriggerService();
                    SimpleTrigger newTrigger = null;
                    newTrigger = xsltService.generateXsltTrigger(xsltPath,
                            generalFileDir, // xml_file_path
                            endFilePath + File.separator,
                            exportFileName,
                            dsBean.getId(),
                            epBean, userBean, Locale.US.getLanguage() , cnt,  getFilePath(context) + "xslt", XsltTriggerService.TRIGGER_GROUP_NAME);
                    //Updating the original trigger with user given inputs
                    newTrigger.setRepeatCount(64000);
                    newTrigger.setRepeatInterval(XsltTriggerService.getIntervalTime(period));
                    newTrigger.setDescription(jobDesc);
                    // set just the start date
                    newTrigger.setName(jobName  + "-" + exportFormat);// + datasetId);
                    newTrigger.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT);
                    newTrigger.getJobDataMap().put(XsltTriggerService.EMAIL, email);
                    newTrigger.getJobDataMap().put(XsltTriggerService.PERIOD, period);
                    newTrigger.getJobDataMap().put(XsltTriggerService.EXPORT_FORMAT, epBean.getFiledescription());
                    newTrigger.getJobDataMap().put(XsltTriggerService.EXPORT_FORMAT_ID, exportFormatId);
                    newTrigger.getJobDataMap().put(XsltTriggerService.JOB_NAME, jobName);

                    JobDetailBean jobDetailBean = new JobDetailBean();
                    jobDetailBean.setGroup(xsltService.TRIGGER_GROUP_NAME);
                    jobDetailBean.setName(newTrigger.getName());
                    jobDetailBean.setJobClass(org.akaza.openclinica.job.XsltStatefulJob.class);
                    jobDetailBean.setJobDataMap(newTrigger.getJobDataMap());
                    jobDetailBean.setDurability(true); // need durability?
                    jobDetailBean.setVolatility(false);

                    scheduler.deleteJob(triggerName, triggerGroup);
                    Date dataStart = scheduler.scheduleJob(jobDetailBean, newTrigger);
                    logger.info("Old job[" + trigger.getName() + "] has been converted to [" + newTrigger.getName() + "]");
                }
            }

        } catch (SchedulerException e) {
            logger.error("Error converting legacy triggers");
            e.printStackTrace();
        }
    }

    private Map<String, Integer> getExportingformats(JobDataMap dataMap) {
        Map<String, Integer> formats = new HashMap<String, Integer>();
        String spss = dataMap.getString("spss");
        if (spss != null && spss.length() > 0) {
            formats.put("spss", CoreResources.SPSS_ID);
        }
        String tab = dataMap.getString("tab");
        if (tab != null && tab.length() > 0) {
            formats.put("tab", CoreResources.TAB_ID);
        }
        String cdisc = dataMap.getString("cdisc");
        if (cdisc != null && cdisc.length() > 0) {
            formats.put("cdisc", CoreResources.CDISC_ODM_1_2_ID);
        }
        String cdisc12 = dataMap.getString("cdisc12");
        if (cdisc12 != null && cdisc12.length() > 0) {
            formats.put("cdisc12", CoreResources.CDISC_ODM_1_2_EXTENSION_ID);
        }
        String cdisc13 = dataMap.getString("cdisc13");
        if (cdisc13 != null && cdisc13.length() >0) {
            formats.put("cdisc13", CoreResources.CDISC_ODM_1_3_ID);
        }
        String cdisc13oc = dataMap.getString("cdisc13oc");
        if (cdisc13oc != null && cdisc13oc.length() >0) {
            formats.put("cdisc13oc", CoreResources.CDISC_ODM_1_3_EXTENSION_ID);
        }

        return formats;
    }

    private ApplicationContext getApplicationContext(JobExecutionContext context) throws SchedulerException {
        return (ApplicationContext) context.getScheduler().getContext().get("applicationContext");
    }

    private String getFilePath(JobExecutionContext context) throws SchedulerException {
        Properties params = (Properties) getApplicationContext(context).getBean("dataInfo");
        String filePath = params.getProperty("filePath");
        if (filePath != null) {
            filePath = filePath.trim();
        }

        return filePath == null ? "" : filePath;
    }
}
