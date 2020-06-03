package org.akaza.openclinica.control.admin;

import core.org.akaza.openclinica.bean.extract.ArchivedDatasetFileBean;
import core.org.akaza.openclinica.bean.extract.DatasetBean;
import core.org.akaza.openclinica.bean.extract.ExtractPropertyBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.core.form.StringUtil;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.extract.ArchivedDatasetFileDAO;
import core.org.akaza.openclinica.dao.extract.DatasetDAO;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.enumsupport.JobStatus;
import core.org.akaza.openclinica.i18n.core.LocaleResolver;
import core.org.akaza.openclinica.service.extract.ExtractUtils;
import core.org.akaza.openclinica.service.extract.XsltTriggerService;
import core.org.akaza.openclinica.web.SQLInitServlet;
import core.org.akaza.openclinica.web.job.ExampleSpringJob;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.view.Page;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.*;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

public class UpdateJobExportServlet extends ScheduleJobServlet {

    private void setUpServlet(Trigger trigger) {
        FormProcessor fp2 = new FormProcessor(request);
        DatasetDAO dsdao = new DatasetDAO(sm.getDataSource());
        Collection dsList = dsdao.findAllOrderByStudyIdAndName();
        ArrayList<Integer> numbers = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));

        JobDataMap dataMap = trigger.getJobDataMap();
        String contactEmail = dataMap.getString(ExampleSpringJob.EMAIL);
        int dsId = dataMap.getInt(XsltTriggerService.DATASET_ID);
        String period = dataMap.getString(XsltTriggerService.PERIOD);
        int exportFormatId = dataMap.getInt(XsltTriggerService.EXPORT_FORMAT_ID);
        int numberOfFilesToSave = dataMap.getInt(XsltTriggerService.NUMBER_OF_FILES_TO_SAVE);

        // Pre-populate with original fields
        request.setAttribute("datasets", dsList);
        request.setAttribute(JOB_NAME, trigger.getJobKey().getName());
        request.setAttribute(JOB_DESC, trigger.getDescription());
        request.setAttribute(FORMAT_ID, exportFormatId);
        request.setAttribute(ExampleSpringJob.EMAIL, contactEmail);
        request.setAttribute(PERIOD, period);
        request.setAttribute(DATASET_ID, dsId);
        request.setAttribute("extractProperties", CoreResources.getExtractProperties());
        request.setAttribute("jobUuid", trigger.getKey().getName());
        request.setAttribute(NUMBER_OF_FILES_TO_SAVE, numberOfFilesToSave);
        request.setAttribute("numbersToChooseFrom", numbers);

        Date jobDate = trigger.getNextFireTime();
        HashMap presetValues = new HashMap();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(jobDate);
        presetValues.put(DATE_START_JOB + "Hour", calendar.get(Calendar.HOUR_OF_DAY));
        presetValues.put(DATE_START_JOB + "Minute", calendar.get(Calendar.MINUTE));
        presetValues.put(DATE_START_JOB + "Date", local_df.format(jobDate));
        fp2.setPresetValues(presetValues);
        setPresetValues(fp2.getPresetValues());
    }

    @Override
    protected void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        scheduler = getScheduler();
        permissionService = getPermissionService();
        ApplicationContext context = null;
        try {
            context = (ApplicationContext) scheduler.getContext().get("applicationContext");
        } catch (SchedulerException e) {
            logger.error("Error in receiving application context: ", e);
        }
        Scheduler jobScheduler = getSchemaScheduler(request, context, scheduler);
        String action = fp.getString("action");
        String triggerName = fp.getString("tname");
        String jobUuid = fp.getString("jobUuid");
        ExtractUtils extractUtils = new ExtractUtils();
        Trigger updatingTrigger = jobScheduler.getTrigger(new TriggerKey(jobUuid, TRIGGER_EXPORT_GROUP));
        Date createdDate = (Date) updatingTrigger.getJobDataMap().get(XsltTriggerService.CREATED_DATE);

        if (StringUtil.isBlank(action)) {
            setUpServlet(updatingTrigger);
            forwardPage(Page.UPDATE_JOB_EXPORT);
        } else if ("confirmall".equalsIgnoreCase(action)) {
            // change and update trigger here
            // validate first
            Set<JobKey> jobKeySet = jobScheduler.getJobKeys(GroupMatcher.jobGroupEquals(XsltTriggerService.TRIGGER_GROUP_NAME));
            JobKey[] jobKeys = jobKeySet.stream().toArray(JobKey[]::new);
            HashMap errors = validateForm(fp, request, jobKeys, updatingTrigger.getJobKey().getName());
            // if original name never changed
            if (!errors.isEmpty()) {
                request.setAttribute("formMessages", errors);
                // send back
                addPageMessage("Your modifications caused an error, please see the messages for more information.");
                setUpServlet(updatingTrigger);
                logger.error("errors : " + errors.toString());
                forwardPage(Page.UPDATE_JOB_EXPORT);
            } else {
                // change trigger, update in database
                Study study = (Study) getStudyBuildService().getPublicStudy(sm.getUserBean().getActiveStudyId());
                DatasetDAO datasetDao = new DatasetDAO(sm.getDataSource());
                CoreResources cr = new CoreResources();
                UserAccountBean userBean = (UserAccountBean) request.getSession().getAttribute("userBean");

                int datasetId = fp.getInt(DATASET_ID);
                String period = fp.getString(PERIOD);
                String email = fp.getString(EMAIL);
                String jobName = fp.getString(JOB_NAME);
                String jobDesc = fp.getString(JOB_DESC);
                Date startDateTime = fp.getDateTime(DATE_START_JOB);
                Integer exportFormatId = fp.getInt(FORMAT_ID);
                Integer numberOfFilesToSave = fp.getInt(NUMBER_OF_FILES_TO_SAVE);

                ExtractPropertyBean epBean = cr.findExtractPropertyBeanById(exportFormatId, "" + datasetId);
                DatasetBean dsBean = (DatasetBean) datasetDao.findByPK(new Integer(datasetId).intValue());
                String[] files = epBean.getFileName();
                int cnt = 0;
                dsBean.setName(dsBean.getName().replaceAll(" ", "_"));
                String[] exportFiles = epBean.getExportFileName();
                String pattern = "yyyy" + File.separator + "MM" + File.separator + "dd" + File.separator + "HHmmssSSS" + File.separator;
                SimpleDateFormat sdfDir = new SimpleDateFormat(pattern);
                int i = 0;
                String[] temp = new String[exportFiles.length];
                //JN: The following logic is for comma separated variables, to avoid the second file be treated as a old file and deleted.
                String datasetFilePath = SQLInitServlet.getField("filePath");
                while (i < exportFiles.length) {
                    temp[i] = extractUtils.resolveVars(exportFiles[i], dsBean, datasetFilePath);
                    i++;
                }
                epBean.setDoNotDelFiles(temp);
                epBean.setExportFileName(temp);

                XsltTriggerService xsltService = new XsltTriggerService();
                String generalFileDir = SQLInitServlet.getField("filePath");
                generalFileDir = generalFileDir + "datasets/scheduled" + File.separator + dsBean.getId() + File.separator + sdfDir.format(new java.util.Date());
                String exportFileName = epBean.getExportFileName()[cnt];

                String xsltPath = SQLInitServlet.getField("filePath") + "xslt" + File.separator + files[cnt];
                String endFilePath = epBean.getFileLocation();
                String beg = endFilePath.substring(0, endFilePath.indexOf("/$datasetName"));
                String end = endFilePath.substring(endFilePath.indexOf("/$datasetName"), endFilePath.length());
                endFilePath = beg + "/scheduled" + end;
                endFilePath = extractUtils.getEndFilePath(endFilePath, dsBean, sdfDir, datasetFilePath);
                if (epBean.getPostProcExportName() != null) {
                    String preProcExportPathName = extractUtils.resolveVars(epBean.getPostProcExportName(), dsBean, datasetFilePath);
                    epBean.setPostProcExportName(preProcExportPathName);
                }
                if (epBean.getPostProcLocation() != null) {
                    String prePocLoc = extractUtils.getEndFilePath(epBean.getPostProcLocation(), dsBean, sdfDir, datasetFilePath);
                    epBean.setPostProcLocation(prePocLoc);
                }
                extractUtils.setAllProps(epBean, dsBean, datasetFilePath);

                ArchivedDatasetFileBean archivedDatasetFileBean = new ArchivedDatasetFileBean();
                archivedDatasetFileBean.setStatus(JobStatus.IN_QUEUE.name());
                archivedDatasetFileBean.setFormat(epBean.getFormatDescription());
                archivedDatasetFileBean.setOwnerId(userBean.getId());
                archivedDatasetFileBean.setDatasetId(dsBean.getId());
                archivedDatasetFileBean.setDateCreated(new Date());
                archivedDatasetFileBean.setExportFormatId(1);
                archivedDatasetFileBean.setFileReference(null);
                archivedDatasetFileBean.setJobUuid(jobUuid);
                archivedDatasetFileBean.setJobExecutionUuid(UUID.randomUUID().toString());
                archivedDatasetFileBean.setJobType("Scheduled");
                ArchivedDatasetFileDAO archivedDatasetFileDAO = new ArchivedDatasetFileDAO(sm.getDataSource());
                archivedDatasetFileBean = (ArchivedDatasetFileBean) archivedDatasetFileDAO.create(archivedDatasetFileBean);

                SimpleTrigger trigger = xsltService.generateXsltTrigger(jobScheduler, xsltPath,
                        generalFileDir, // xml_file_path
                        endFilePath + File.separator,
                        exportFileName,
                        dsBean.getId(),
                        epBean,
                        userBean,
                        LocaleResolver.getLocale(request).getLanguage(),
                        cnt,
                        SQLInitServlet.getField("filePath") + "xslt",
                        xsltService.getTriggerGroupNameForExportJobs(),
                        currentPublicStudy,
                        currentStudy, archivedDatasetFileBean);

                //Updating the original trigger with user given inputs
                trigger = trigger.getTriggerBuilder()
                        .withIdentity(jobUuid, xsltService.getTriggerGroupNameForExportJobs())
                        .withDescription(jobDesc)
                        .startAt(startDateTime)
                        .forJob(jobName, xsltService.getTriggerGroupNameForExportJobs())
                        .withSchedule(simpleSchedule()
                                .withIntervalInSeconds(XsltTriggerService.getIntervalTimeInSeconds(period))
                                .withRepeatCount(64000)
                                .withMisfireHandlingInstructionNextWithExistingCount())
                        .build();

                trigger.getJobDataMap().put(XsltTriggerService.ARCHIVED_DATASET_FILE_BEAN_ID, archivedDatasetFileBean.getId());
                trigger.getJobDataMap().put(XsltTriggerService.EMAIL, email);
                trigger.getJobDataMap().put(XsltTriggerService.PERIOD, period);
                trigger.getJobDataMap().put(XsltTriggerService.EXPORT_FORMAT, epBean.getFiledescription());
                trigger.getJobDataMap().put(XsltTriggerService.EXPORT_FORMAT_ID, exportFormatId);
                trigger.getJobDataMap().put(XsltTriggerService.JOB_NAME, jobName);
                trigger.getJobDataMap().put(XsltTriggerService.JOB_TYPE, "exportJob");
                trigger.getJobDataMap().put(XsltTriggerService.JOB_UUID, jobUuid);
                trigger.getJobDataMap().put(XsltTriggerService.CREATED_DATE, createdDate);
                trigger.getJobDataMap().put(XsltTriggerService.NUMBER_OF_FILES_TO_SAVE, numberOfFilesToSave);

                JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
                jobDetailFactoryBean.setGroup(xsltService.getTriggerGroupNameForExportJobs());
                jobDetailFactoryBean.setName(trigger.getJobKey().getName());
                jobDetailFactoryBean.setJobClass(core.org.akaza.openclinica.job.XsltStatefulJob.class);
                jobDetailFactoryBean.setJobDataMap(trigger.getJobDataMap());
                jobDetailFactoryBean.setDurability(true); // need durability?
                jobDetailFactoryBean.setDescription(trigger.getDescription());
                jobDetailFactoryBean.afterPropertiesSet();

                // then update or send back
                try {
                    //Delete from oc_qrtz_triggers
                    jobScheduler.deleteJob(JobKey.jobKey(triggerName, XsltTriggerService.TRIGGER_GROUP_NAME));
                    //delete the previously created updated job if its IN_QUEUE and not ran yet
                    deletePreviouslyUpdatedTriggerInQueue(archivedDatasetFileDAO, jobUuid, archivedDatasetFileBean.getId());
                    jobScheduler.scheduleJob(jobDetailFactoryBean.getObject(), trigger);
                    addPageMessage("Your job has been successfully modified.");
                    forwardPage(Page.VIEW_JOB_SERVLET);
                } catch (SchedulerException se) {
                    logger.error("Job is not able to be deleted: ", se);
                    // set a message here with the exception message
                    setUpServlet(trigger);
                    addPageMessage("There was an unspecified error with your creation, please contact an administrator.");
                    forwardPage(Page.UPDATE_JOB_EXPORT);
                }
            }
        }
    }

    private void deletePreviouslyUpdatedTriggerInQueue(ArchivedDatasetFileDAO archivedDatasetFileDAO, String jobUuid, int currentId) {
        ArrayList<ArchivedDatasetFileBean> archivedDatasetFileBeans = archivedDatasetFileDAO.findByJobUuid(jobUuid);
        for (ArchivedDatasetFileBean archivedDatasetFileBean : archivedDatasetFileBeans) {
            if (archivedDatasetFileBean.getStatus().equals(JobStatus.IN_QUEUE.name()) && archivedDatasetFileBean.getId() != currentId) {
                archivedDatasetFileDAO.deleteArchiveDataset(archivedDatasetFileBean);
            }
        }
    }
}
