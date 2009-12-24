package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.job.ExampleSpringJob;
import org.akaza.openclinica.web.job.TriggerService;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdScheduler;
import org.springframework.scheduling.quartz.JobDetailBean;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

/**
 * 
 * @author thickerson
 * 
 */
public class CreateJobExportServlet extends SecureController {
    public static final String PERIOD = "periodToRun";
    public static final String TAB = "tab";
    public static final String CDISC = "cdisc";
    public static final String SPSS = "spss";
    public static final String DATASET_ID = "dsId";
    public static final String DATE_START_JOB = "job";
    public static final String EMAIL = "contactEmail";
    public static final String JOB_NAME = "jobName";
    public static final String JOB_DESC = "jobDesc";
    public static final String USER_ID = "user_id";
    public static final String STUDY_NAME = "study_name";

    private static String SCHEDULER = "schedulerFactoryBean";
    // faking out DRY - should we create a super class, Job Servlet, which
    // captures the scheduler?
    private StdScheduler scheduler;

    // private SimpleTrigger trigger;
    // private JobDataMap jobDataMap;

    // private FormProcessor fp;

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {// ?
            // ?
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU, resexception.getString("not_allowed_access_extract_data_servlet"), "1");// TODO
        // above copied from create dataset servlet, needs to be changed to
        // allow only admin-level users

    }

    private StdScheduler getScheduler() {
        scheduler = this.scheduler != null ? scheduler : (StdScheduler) SpringServletAccess.getApplicationContext(context).getBean(SCHEDULER);
        return scheduler;
    }

    private void setUpServlet() {

        // TODO find all the form items and re-populate them if necessary
        FormProcessor fp2 = new FormProcessor(request);
        // Enumeration enumeration = request.getAttributeNames();
        // while (enumeration.hasMoreElements()) {
        // String attrName = (String)enumeration.nextElement();
        // if (fp.getString(attrName) != null) {
        // request.setAttribute(attrName, fp.getString(attrName));
        // }
        // // possible error with dates? yep
        // }
        DatasetDAO dsdao = new DatasetDAO(sm.getDataSource());
        Collection dsList = dsdao.findAllOrderByStudyIdAndName();
        // TODO will have to dress this up to allow for sites then datasets
        request.setAttribute("datasets", dsList);
        request.setAttribute(JOB_NAME, fp2.getString(JOB_NAME));
        request.setAttribute(JOB_DESC, fp2.getString(JOB_DESC));
        request.setAttribute(TAB, fp2.getString(TAB));
        request.setAttribute(CDISC, fp2.getString(CDISC));
        request.setAttribute(ExampleSpringJob.CDISC12, fp2.getString(ExampleSpringJob.CDISC12));
        request.setAttribute(ExampleSpringJob.CDISC13, fp2.getString(ExampleSpringJob.CDISC13));
        request.setAttribute(ExampleSpringJob.CDISC13OC, fp2.getString(ExampleSpringJob.CDISC13OC));
        request.setAttribute(SPSS, fp2.getString(SPSS));
        request.setAttribute(EMAIL, fp2.getString(EMAIL));
        request.setAttribute(PERIOD, fp2.getString(PERIOD));
        request.setAttribute(DATASET_ID, fp2.getInt(DATASET_ID));
        Date jobDate = (fp2.getDateTime(DATE_START_JOB));
        HashMap presetValues = new HashMap();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(jobDate);
        presetValues.put(DATE_START_JOB + "Hour", calendar.get(Calendar.HOUR_OF_DAY));
        presetValues.put(DATE_START_JOB + "Minute", calendar.get(Calendar.MINUTE));
        presetValues.put(DATE_START_JOB + "Date", local_df.format(jobDate));
        // (calendar.get(Calendar.MONTH) + 1) + "/" +
        // calendar.get(Calendar.DATE) + "/" + calendar.get(Calendar.YEAR));
        fp2.setPresetValues(presetValues);
        setPresetValues(fp2.getPresetValues());
        request.setAttribute(DATE_START_JOB, fp2.getDateTime(DATE_START_JOB + "Date"));
        // EMAIL, TAB, CDISC, SPSS, PERIOD, DATE_START_JOB
        // TODO pick out the datasets and the date
    }

    @Override
    protected void processRequest() throws Exception {
        // TODO multi stage servlet which will create export jobs
        // will accept, create, and return the ViewJob servlet
        FormProcessor fp = new FormProcessor(request);
        TriggerService triggerService = new TriggerService();
        scheduler = getScheduler();
        String action = fp.getString("action");
        if (StringUtil.isBlank(action)) {
            // set up list of data sets
            // select by ... active study
            setUpServlet();

            forwardPage(Page.CREATE_JOB_EXPORT);
        } else if ("confirmall".equalsIgnoreCase(action)) {
            // collect form information
            HashMap errors = triggerService.validateForm(fp, request, scheduler.getTriggerNames("DEFAULT"));

            if (!errors.isEmpty()) {
                // set errors to request
                request.setAttribute("formMessages", errors);
                logger.info("has validation errors in the first section");
                logger.info("errors found: " + errors.toString());
                setUpServlet();

                forwardPage(Page.CREATE_JOB_EXPORT);
            } else {
                logger.info("found no validation errors, continuing");

                StudyDAO studyDAO = new StudyDAO(sm.getDataSource());
                StudyBean studyBean = (StudyBean) studyDAO.findByPK(sm.getUserBean().getActiveStudyId());
                SimpleTrigger trigger = triggerService.generateTrigger(fp, sm.getUserBean(), studyBean, request.getLocale().getLanguage());

                String localeStr = request.getLocale().getLanguage();

                System.out.println("found " + localeStr);
                JobDetailBean jobDetailBean = new JobDetailBean();
                jobDetailBean.setGroup("DEFAULT");
                jobDetailBean.setName(trigger.getName());
                jobDetailBean.setJobClass(org.akaza.openclinica.web.job.ExampleStatefulJob.class);
                jobDetailBean.setJobDataMap(trigger.getJobDataMap());
                jobDetailBean.setDurability(true); // need durability?
                jobDetailBean.setVolatility(false);

                // set to the scheduler
                try {
                    Date dateStart = scheduler.scheduleJob(jobDetailBean, trigger);
                    logger.info("== found job date: " + dateStart.toString());
                    // set a success message here
                    addPageMessage("You have successfully created a new job: " + trigger.getName() + " which is now set to run at the time you specified.");
                    forwardPage(Page.VIEW_JOB_SERVLET);
                } catch (SchedulerException se) {
                    se.printStackTrace();
                    // set a message here with the exception message
                    setUpServlet();
                    addPageMessage("There was an unspecified error with your creation, please contact an administrator.");
                    forwardPage(Page.CREATE_JOB_EXPORT);
                }
            }
        } else {
            forwardPage(Page.ADMIN_SYSTEM);
            // forward to form
            // should we even get to this part?
        }

    }
}
