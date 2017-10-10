package org.akaza.openclinica.control.admin;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.extract.ExtractPropertyBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.service.extract.ExtractUtils;
import org.akaza.openclinica.service.extract.XsltTriggerService;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;
import org.akaza.openclinica.web.job.ExampleSpringJob;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdScheduler;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

public class UpdateJobExportServlet extends SecureController {

    private static String SCHEDULER = "schedulerFactoryBean";

    private StdScheduler scheduler;
    private SimpleTrigger trigger;
    private JobDataMap dataMap;
    public static final String PERIOD = "periodToRun";
    public static final String FORMAT_ID = "formatId";
    public static final String DATASET_ID = "dsId";
    public static final String DATE_START_JOB = "job";
    public static final String EMAIL = "contactEmail";
    public static final String JOB_NAME = "jobName";
    public static final String JOB_DESC = "jobDesc";
    public static final String USER_ID = "user_id";
    public static final String STUDY_NAME = "study_name";
    public static final String TRIGGER_GROUP_JOB = "XsltTriggersExportJobs";
    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin() || ub.isTechAdmin()) {
            return;
        }
//        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
//            return;
//        }
        // should it be only study directors and admins, not coordinators?

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_allowed_access_extract_data_servlet"), "1");// TODO
        // above copied from create dataset servlet, needs to be changed to
        // allow only admin-level users

    }

    private StdScheduler getScheduler() {
        scheduler = this.scheduler != null ? scheduler : (StdScheduler) SpringServletAccess.getApplicationContext(context).getBean(SCHEDULER);
        return scheduler;
    }

    private void setUpServlet(Trigger trigger) {
        FormProcessor fp2 = new FormProcessor(request);

        DatasetDAO dsdao = new DatasetDAO(sm.getDataSource());
        Collection dsList = dsdao.findAllOrderByStudyIdAndName();
        // TODO will have to dress this up to allow for sites then datasets
        request.setAttribute("datasets", dsList);
        request.setAttribute(CreateJobExportServlet.JOB_NAME, trigger.getKey().getName());
        request.setAttribute(CreateJobExportServlet.JOB_DESC, trigger.getDescription());

        dataMap = trigger.getJobDataMap();
        String contactEmail = dataMap.getString(ExampleSpringJob.EMAIL);
        int dsId = dataMap.getInt(XsltTriggerService.DATASET_ID);
        int userId = dataMap.getInt(XsltTriggerService.USER_ID);
        String period = dataMap.getString(XsltTriggerService.PERIOD);
        int exportFormatId = dataMap.getInt(XsltTriggerService.EXPORT_FORMAT_ID);

        request.setAttribute(FORMAT_ID, exportFormatId);
        request.setAttribute(ExampleSpringJob.EMAIL, contactEmail);
        // how to find out the period of time???
        request.setAttribute(PERIOD, period);
        request.setAttribute(DATASET_ID, dsId);
        request.setAttribute("extractProperties", CoreResources.getExtractProperties());

        // DatasetBean dataset = (DatasetBean)dsdao.findByPK(dsId);
        // >> tbh 5639: collate the correct study id
        // request.setAttribute("study_id", dataset.getStudyId());
        Date jobDate = trigger.getNextFireTime();
        HashMap presetValues = new HashMap();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(jobDate);
        presetValues.put(CreateJobExportServlet.DATE_START_JOB + "Hour", calendar.get(Calendar.HOUR_OF_DAY));
        presetValues.put(CreateJobExportServlet.DATE_START_JOB + "Minute", calendar.get(Calendar.MINUTE));
        presetValues.put(CreateJobExportServlet.DATE_START_JOB + "Date", local_df.format(jobDate));
        // (calendar.get(Calendar.MONTH) + 1) + "/" +
        // calendar.get(Calendar.DATE) + "/"
        // + calendar.get(Calendar.YEAR));
        fp2.setPresetValues(presetValues);
        setPresetValues(fp2.getPresetValues());
        // request.setAttribute(DATE_START_JOB, fp2.getDateTime(DATE_START_JOB +
        // "Date"));
        // EMAIL, TAB, CDISC, SPSS, PERIOD, DATE_START_JOB
        // TODO pick out the datasets and the date
    }

    @Override
    protected void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        String action = fp.getString("action");
        String triggerName = fp.getString("tname");
        scheduler = getScheduler();
        ExtractUtils extractUtils = new ExtractUtils();
        Trigger updatingTrigger = scheduler.getTrigger(new TriggerKey(triggerName.trim(), XsltTriggerService.TRIGGER_GROUP_NAME));
        if (StringUtil.isBlank(action)) {
            setUpServlet(updatingTrigger);
            forwardPage(Page.UPDATE_JOB_EXPORT);
        } else if ("confirmall".equalsIgnoreCase(action)) {
            // change and update trigger here
            // validate first
            // then update or send back
            String name = XsltTriggerService.TRIGGER_GROUP_NAME;

            Set<TriggerKey> triggerKeySet = scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(name));
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
                StudyDAO studyDAO = new StudyDAO(sm.getDataSource());
                StudyBean study = (StudyBean) studyDAO.findByPK(sm.getUserBean().getActiveStudyId());
                DatasetDAO datasetDao = new DatasetDAO(sm.getDataSource());
                CoreResources cr =  new CoreResources();
                UserAccountBean userBean = (UserAccountBean) request.getSession().getAttribute("userBean");

                int datasetId = fp.getInt(DATASET_ID);
                String period = fp.getString(PERIOD);
                String email = fp.getString(EMAIL);
                String jobName = fp.getString(JOB_NAME);
                String jobDesc = fp.getString(JOB_DESC);
                Date startDateTime = fp.getDateTime(DATE_START_JOB);
                Integer exportFormatId = fp.getInt(FORMAT_ID);

                ExtractPropertyBean epBean = cr.findExtractPropertyBeanById(exportFormatId, "" + datasetId);
                DatasetBean dsBean = (DatasetBean)datasetDao.findByPK(new Integer(datasetId).intValue());
                String[] files = epBean.getFileName();
                String exportFileName;
                int fileSize = files.length;
                int  cnt = 0;
                dsBean.setName(dsBean.getName().replaceAll(" ", "_"));
                String[] exportFiles= epBean.getExportFileName();
                 String pattern = "yyyy" + File.separator + "MM" + File.separator + "dd" + File.separator + "HHmmssSSS" + File.separator;
                 SimpleDateFormat sdfDir = new SimpleDateFormat(pattern);
                int i =0;
                String[] temp = new String[exportFiles.length];
                //JN: The following logic is for comma separated variables, to avoid the second file be treated as a old file and deleted.
                String datasetFilePath = SQLInitServlet.getField("filePath")+"datasets";
                while (i<exportFiles.length) {
                    temp[i] = extractUtils.resolveVars(exportFiles[i], dsBean, sdfDir, datasetFilePath);
                    i++;
                }
                epBean.setDoNotDelFiles(temp);
                epBean.setExportFileName(temp);

                XsltTriggerService xsltService = new XsltTriggerService();
                String generalFileDir = SQLInitServlet.getField("filePath");
                generalFileDir = generalFileDir + "datasets" + File.separator + dsBean.getId() + File.separator + sdfDir.format(new java.util.Date());
                exportFileName = epBean.getExportFileName()[cnt];

                String xsltPath = SQLInitServlet.getField("filePath") + "xslt" + File.separator +files[cnt];
                String endFilePath = epBean.getFileLocation();
                endFilePath  = extractUtils.getEndFilePath(endFilePath, dsBean, sdfDir, datasetFilePath);
              //  exportFileName = resolveVars(exportFileName,dsBean,sdfDir);
                if (epBean.getPostProcExportName() != null) {
                    String preProcExportPathName = extractUtils.resolveVars(epBean.getPostProcExportName(),dsBean,sdfDir, datasetFilePath);
                    epBean.setPostProcExportName(preProcExportPathName);
                }
                if (epBean.getPostProcLocation() != null) {
                    String prePocLoc = extractUtils.getEndFilePath(epBean.getPostProcLocation(), dsBean, sdfDir, datasetFilePath);
                    epBean.setPostProcLocation(prePocLoc);
                }
                extractUtils.setAllProps(epBean, dsBean, sdfDir, datasetFilePath);
                SimpleTrigger trigger = null;

                trigger = xsltService.generateXsltTrigger(scheduler, xsltPath,
                        generalFileDir, // xml_file_path
                        endFilePath + File.separator,
                        exportFileName,
                        dsBean.getId(),
                        epBean, 
                        userBean, 
                        LocaleResolver.getLocale(request).getLanguage(),
                        cnt,  
                        SQLInitServlet.getField("filePath") + "xslt", 
                        TRIGGER_GROUP_JOB,
                        currentPublicStudy,
                        currentStudy);

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

                JobDetailFactoryBean JobDetailFactoryBean = new JobDetailFactoryBean();
                JobDetailFactoryBean.setGroup(xsltService.getTriggerGroupNameForExportJobs());
                JobDetailFactoryBean.setName(trigger.getKey().getName());
                JobDetailFactoryBean.setJobClass(org.akaza.openclinica.job.XsltStatefulJob.class);
                JobDetailFactoryBean.setJobDataMap(trigger.getJobDataMap());
                JobDetailFactoryBean.setDurability(true); // need durability?
                JobDetailFactoryBean.afterPropertiesSet();
                try {
                    scheduler.deleteJob(new JobKey(triggerName, XsltTriggerService.TRIGGER_GROUP_NAME));
                    Date dataStart = scheduler.scheduleJob(JobDetailFactoryBean.getObject(), trigger);
                    addPageMessage("Your job has been successfully modified.");
                    forwardPage(Page.VIEW_JOB_SERVLET);
                } catch (SchedulerException se) {
                    se.printStackTrace();
                    // set a message here with the exception message
                    setUpServlet(trigger);
                    addPageMessage("There was an unspecified error with your creation, please contact an administrator.");
                    forwardPage(Page.UPDATE_JOB_EXPORT);
                }
            }
        }
    }

    public HashMap validateForm(FormProcessor fp, HttpServletRequest request, TriggerKey[] triggerKeys, String properName) {
        Validator v = new Validator(request);
        v.addValidation(JOB_NAME, Validator.NO_BLANKS);
        v.addValidation(JOB_NAME, Validator.NO_LEADING_OR_TRAILING_SPACES);
        // need to be unique too
        v.addValidation(JOB_DESC, Validator.NO_BLANKS);
        v.addValidation(EMAIL, Validator.IS_A_EMAIL);
        v.addValidation(PERIOD, Validator.NO_BLANKS);
        v.addValidation(DATE_START_JOB + "Date", Validator.IS_A_DATE);
        // v.addValidation(DATE_START_JOB + "Date", new Date(), Validator.DATE_IS_AFTER_OR_EQUAL);
        // TODO job names will have to be unique, tbh

        int formatId = fp.getInt(FORMAT_ID);
        Date jobDate = fp.getDateTime(DATE_START_JOB);
        HashMap errors = v.validate();
        if (formatId == 0) {
            // throw an error here, at least one should work
            // errors.put(TAB, "Error Message - Pick one of the below");
            v.addError(errors, FORMAT_ID, "Please pick at least one.");
        }
        for (TriggerKey triggerKey : triggerKeys) {
            if (triggerKey.getName().equals(fp.getString(JOB_NAME)) && !triggerKey.getName().equals(properName)) {
                v.addError(errors, JOB_NAME, "A job with that name already exists.  Please pick another name.");
            }
        }
        if (jobDate.before(new Date())) {
            v.addError(errors, DATE_START_JOB + "Date", "This date needs to be later than the present time.");
        }
        return errors;
    }
}
