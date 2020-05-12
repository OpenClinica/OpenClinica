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
import core.org.akaza.openclinica.service.dto.ODMFilterDTO;
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

        // TODO will have to dress this up to allow for sites then datasets
        request.setAttribute("datasets", dsList);
        request.setAttribute(JOB_NAME, trigger.getKey().getName());
        request.setAttribute(JOB_DESC, trigger.getDescription());

        JobDataMap dataMap = trigger.getJobDataMap();
        String contactEmail = dataMap.getString(ExampleSpringJob.EMAIL);
        int dsId = dataMap.getInt(XsltTriggerService.DATASET_ID);
        String period = dataMap.getString(XsltTriggerService.PERIOD);
        int exportFormatId = dataMap.getInt(XsltTriggerService.EXPORT_FORMAT_ID);

        request.setAttribute(FORMAT_ID, exportFormatId);
        request.setAttribute(ExampleSpringJob.EMAIL, contactEmail);
        // how to find out the period of time???
        request.setAttribute(PERIOD, period);
        request.setAttribute(DATASET_ID, dsId);
        request.setAttribute("extractProperties", CoreResources.getExtractProperties());

        Date jobDate = trigger.getNextFireTime();
        HashMap presetValues = new HashMap();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(jobDate);
        presetValues.put(DATE_START_JOB + "Hour", calendar.get(Calendar.HOUR_OF_DAY));
        presetValues.put(DATE_START_JOB + "Minute", calendar.get(Calendar.MINUTE));
        presetValues.put(DATE_START_JOB + "Date", local_df.format(jobDate));
        fp2.setPresetValues(presetValues);
        setPresetValues(fp2.getPresetValues());

        // TODO pick out the datasets and the date
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
        ExtractUtils extractUtils = new ExtractUtils();
        Trigger updatingTrigger = jobScheduler.getTrigger(new TriggerKey(triggerName.trim(), TRIGGER_EXPORT_GROUP));
        if (StringUtil.isBlank(action)) {
            setUpServlet(updatingTrigger);
            forwardPage(Page.UPDATE_JOB_EXPORT);
        } else if ("confirmall".equalsIgnoreCase(action)) {
            // change and update trigger here
            // validate first
            // then update or send back
            String name = XsltTriggerService.TRIGGER_GROUP_NAME;
            Set<TriggerKey> triggerKeySet = jobScheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(name));
            TriggerKey[] triggerKeys = triggerKeySet.stream().toArray(TriggerKey[]::new);
            HashMap errors = validateForm(fp, request, triggerKeys, updatingTrigger.getKey().getName());
            if (!errors.isEmpty()) {
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

                ExtractPropertyBean epBean = cr.findExtractPropertyBeanById(exportFormatId, "" + datasetId);
                DatasetBean dsBean = (DatasetBean) datasetDao.findByPK(new Integer(datasetId).intValue());
                String[] files = epBean.getFileName();
                String exportFileName;
                int cnt = 0;
                dsBean.setName(dsBean.getName().replaceAll(" ", "_"));
                String[] exportFiles = epBean.getExportFileName();
                String pattern = "yyyy" + File.separator + "MM" + File.separator + "dd" + File.separator + "HHmmssSSS" + File.separator;
                SimpleDateFormat sdfDir = new SimpleDateFormat(pattern);
                int i = 0;
                String[] temp = new String[exportFiles.length];
                //JN: The following logic is for comma separated variables, to avoid the second file be treated as a old file and deleted.
                String datasetFilePath = SQLInitServlet.getField("filePath") + "datasets";
                while (i < exportFiles.length) {
                    temp[i] = extractUtils.resolveVars(exportFiles[i], dsBean, sdfDir, datasetFilePath);
                    i++;
                }
                epBean.setDoNotDelFiles(temp);
                epBean.setExportFileName(temp);

                XsltTriggerService xsltService = new XsltTriggerService();
                String generalFileDir = SQLInitServlet.getField("filePath");
                generalFileDir = generalFileDir + "datasets" + File.separator + dsBean.getId() + File.separator + sdfDir.format(new java.util.Date());
                exportFileName = epBean.getExportFileName()[cnt];

                String xsltPath = SQLInitServlet.getField("filePath") + "xslt" + File.separator + files[cnt];
                String endFilePath = epBean.getFileLocation();
                endFilePath = extractUtils.getEndFilePath(endFilePath, dsBean, sdfDir, datasetFilePath);
                if (epBean.getPostProcExportName() != null) {
                    String preProcExportPathName = extractUtils.resolveVars(epBean.getPostProcExportName(), dsBean, sdfDir, datasetFilePath);
                    epBean.setPostProcExportName(preProcExportPathName);
                }
                if (epBean.getPostProcLocation() != null) {
                    String prePocLoc = extractUtils.getEndFilePath(epBean.getPostProcLocation(), dsBean, sdfDir, datasetFilePath);
                    epBean.setPostProcLocation(prePocLoc);
                }
                extractUtils.setAllProps(epBean, dsBean, sdfDir, datasetFilePath);
                String permissionTagsString = permissionService.getPermissionTagsString((Study) request.getSession().getAttribute("study"), request);
                String[] permissionTagsStringArray = permissionService.getPermissionTagsStringArray((Study) request.getSession().getAttribute("study"), request);
                List<String> permissionTagsList = permissionService.getPermissionTagsList((Study) request.getSession().getAttribute("study"), request);
                ODMFilterDTO odmFilter = new ODMFilterDTO();

                try {
                    jobScheduler.getContext().put("permissionTagsString", permissionTagsString);
                    jobScheduler.getContext().put("permissionTagsStringArray", permissionTagsStringArray);
                    jobScheduler.getContext().put("permissionTagsList", permissionTagsList);
                    jobScheduler.getContext().put("odmFilter", odmFilter);
                } catch (SchedulerException e) {
                    logger.error("Error in setting the permissions: ", e);
                }

                ArchivedDatasetFileBean archivedDatasetFileBean = new ArchivedDatasetFileBean();
                archivedDatasetFileBean.setStatus(JobStatus.IN_QUEUE.name());
                archivedDatasetFileBean.setFormat(epBean.getFormatDescription());
                archivedDatasetFileBean.setOwnerId(userBean.getId());
                archivedDatasetFileBean.setDatasetId(dsBean.getId());
                archivedDatasetFileBean.setDateCreated(new Date());
                archivedDatasetFileBean.setExportFormatId(1);
                archivedDatasetFileBean.setFileReference("");
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
                        .withIdentity(jobName, xsltService.getTriggerGroupNameForExportJobs())
                        .withDescription(jobDesc)
                        .startAt(startDateTime)
                        .forJob(jobName, xsltService.getTriggerGroupNameForExportJobs())
                        .withSchedule(simpleSchedule()
                                .withIntervalInSeconds(XsltTriggerService.getIntervalTimeInSeconds(period))
                                .withRepeatCount(64000)
                                .withMisfireHandlingInstructionNextWithExistingCount())
                        .build();
                trigger.getJobDataMap().put(XsltTriggerService.EMAIL, email);
                trigger.getJobDataMap().put(XsltTriggerService.PERIOD, period);
                trigger.getJobDataMap().put(XsltTriggerService.EXPORT_FORMAT, epBean.getFiledescription());
                trigger.getJobDataMap().put(XsltTriggerService.EXPORT_FORMAT_ID, exportFormatId);
                trigger.getJobDataMap().put(XsltTriggerService.JOB_NAME, jobName);

                JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
                jobDetailFactoryBean.setGroup(xsltService.getTriggerGroupNameForExportJobs());
                jobDetailFactoryBean.setName(trigger.getKey().getName());
                jobDetailFactoryBean.setJobClass(core.org.akaza.openclinica.job.XsltStatefulJob.class);
                jobDetailFactoryBean.setJobDataMap(trigger.getJobDataMap());
                jobDetailFactoryBean.setDurability(true); // need durability?
                jobDetailFactoryBean.setDescription(trigger.getDescription());
                jobDetailFactoryBean.afterPropertiesSet();

                try {
                    jobScheduler.deleteJob(new JobKey(triggerName, XsltTriggerService.TRIGGER_GROUP_NAME));
                    Date dataStart = jobScheduler.scheduleJob(jobDetailFactoryBean.getObject(), trigger);
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
}
