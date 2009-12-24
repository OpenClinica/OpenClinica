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
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.StdScheduler;
import org.springframework.scheduling.quartz.JobDetailBean;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class UpdateJobExportServlet extends SecureController {

    private static String SCHEDULER = "schedulerFactoryBean";

    private StdScheduler scheduler;
    private SimpleTrigger trigger;
    private JobDataMap dataMap;

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }
        // should it be only study directors and admins, not coordinators?

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU, resexception.getString("not_allowed_access_extract_data_servlet"), "1");// TODO
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
        request.setAttribute(CreateJobExportServlet.JOB_NAME, trigger.getName());
        request.setAttribute(CreateJobExportServlet.JOB_DESC, trigger.getDescription());

        dataMap = trigger.getJobDataMap();
        String contactEmail = dataMap.getString(ExampleSpringJob.EMAIL);
        System.out.println("found email: " + contactEmail);
        String tab = dataMap.getString(ExampleSpringJob.TAB);
        String cdisc = dataMap.getString(ExampleSpringJob.CDISC);
        String cdisc12 = dataMap.getString(ExampleSpringJob.CDISC12);
        String cdisc13 = dataMap.getString(ExampleSpringJob.CDISC13);
        String cdisc13oc = dataMap.getString(ExampleSpringJob.CDISC13OC);
        String spss = dataMap.getString(ExampleSpringJob.SPSS);
        int dsId = dataMap.getInt(ExampleSpringJob.DATASET_ID);
        int userId = dataMap.getInt(ExampleSpringJob.USER_ID);
        String period = dataMap.getString(ExampleSpringJob.PERIOD);

        request.setAttribute(ExampleSpringJob.TAB, tab);
        request.setAttribute(ExampleSpringJob.CDISC, cdisc);
        request.setAttribute(ExampleSpringJob.CDISC12, cdisc12);
        request.setAttribute(ExampleSpringJob.CDISC13, cdisc13);
        request.setAttribute(ExampleSpringJob.CDISC13OC, cdisc13oc);
        request.setAttribute(ExampleSpringJob.SPSS, spss);
        request.setAttribute(ExampleSpringJob.EMAIL, contactEmail);
        // how to find out the period of time???
        request.setAttribute(ExampleSpringJob.PERIOD, period);
        request.setAttribute(ExampleSpringJob.DATASET_ID, dsId);
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
        TriggerService triggerService = new TriggerService();
        String action = fp.getString("action");
        String triggerName = fp.getString("tname");
        scheduler = getScheduler();
        System.out.println("found trigger name " + triggerName);
        Trigger trigger = scheduler.getTrigger(triggerName.trim(), "DEFAULT");
        System.out.println("found trigger from the other side " + trigger.getFullName());
        if (StringUtil.isBlank(action)) {
            setUpServlet(trigger);
            forwardPage(Page.UPDATE_JOB_EXPORT);
        } else if ("confirmall".equalsIgnoreCase(action)) {
            // change and update trigger here
            // validate first
            // then update or send back
            HashMap errors = triggerService.validateForm(fp, request, scheduler.getTriggerNames("DEFAULT"), trigger.getName());
            if (!errors.isEmpty()) {
                // send back
                addPageMessage("Your modifications caused an error, please see the messages for more information.");
                setUpServlet(trigger);
                System.out.println("errors : " + errors.toString());
                forwardPage(Page.UPDATE_JOB_EXPORT);
            } else {
                // change trigger, update in database
                StudyDAO studyDAO = new StudyDAO(sm.getDataSource());
                StudyBean study = (StudyBean) studyDAO.findByPK(sm.getUserBean().getActiveStudyId());
                trigger = triggerService.generateTrigger(fp, sm.getUserBean(), study, request.getLocale().getLanguage());
                // scheduler = getScheduler();
                JobDetailBean jobDetailBean = new JobDetailBean();
                jobDetailBean.setGroup("DEFAULT");
                jobDetailBean.setName(trigger.getName());
                jobDetailBean.setJobClass(org.akaza.openclinica.web.job.ExampleStatefulJob.class);
                jobDetailBean.setJobDataMap(trigger.getJobDataMap());
                jobDetailBean.setDurability(true); // need durability?
                jobDetailBean.setVolatility(false);

                try {
                    // scheduler.unscheduleJob(triggerName, "DEFAULT");
                    scheduler.deleteJob(triggerName, "DEFAULT");
                    Date dataStart = scheduler.scheduleJob(jobDetailBean, trigger);
                    // Date dateStart = scheduler.rescheduleJob(triggerName,
                    // "DEFAULT", trigger);
                    // scheduler.rescheduleJob(triggerName, groupName,
                    // newTrigger)
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

}
