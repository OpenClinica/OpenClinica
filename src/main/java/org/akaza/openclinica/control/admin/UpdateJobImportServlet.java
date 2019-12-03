package org.akaza.openclinica.control.admin;

import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import core.org.akaza.openclinica.core.form.StringUtil;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import core.org.akaza.openclinica.web.SQLInitServlet;
import core.org.akaza.openclinica.web.job.ImportSpringJob;
import core.org.akaza.openclinica.web.job.TriggerService;
import org.quartz.*;
import org.quartz.impl.StdScheduler;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

public class UpdateJobImportServlet extends SecureController {

    private static String SCHEDULER = "schedulerFactoryBean";
    private static String TRIGGER_IMPORT_GROUP = "importTrigger";
    private StdScheduler scheduler;
    private SimpleTrigger trigger;
    private JobDataMap dataMap;
    private static final String IMPORT_DIR = SQLInitServlet.getField("filePath") + CreateJobImportServlet.DIR_PATH + File.separator;

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin() || ub.isTechAdmin()) {
            return;
        }
//        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
//            return;
//        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_allowed_access_extract_data_servlet"), "1");// TODO

    }

    private StdScheduler getScheduler() {
        scheduler = this.scheduler != null ? scheduler : (StdScheduler) SpringServletAccess.getApplicationContext(context).getBean(SCHEDULER);
        return scheduler;
    }

    private void setUpServlet(Trigger trigger) throws Exception {
        FormProcessor fp2 = new FormProcessor(request);

        request.setAttribute(CreateJobImportServlet.JOB_NAME, trigger.getKey().getName());
        request.setAttribute(CreateJobImportServlet.JOB_DESC, trigger.getDescription());

        dataMap = trigger.getJobDataMap();
        String contactEmail = dataMap.getString(ImportSpringJob.EMAIL);
        logger.debug("found email: " + contactEmail);
        int userId = dataMap.getInt(ImportSpringJob.USER_ID);
        int hours = dataMap.getInt(CreateJobImportServlet.HOURS);
        int minutes = dataMap.getInt(CreateJobImportServlet.MINUTES);
        String directory = dataMap.getString(ImportSpringJob.DIRECTORY);
        String studyName = dataMap.getString(ImportSpringJob.STUDY_NAME);

        request.setAttribute(ImportSpringJob.EMAIL, contactEmail);
        request.setAttribute(ImportSpringJob.STUDY_NAME, studyName);
        request.setAttribute("filePath", directory);
        request.setAttribute("firstFilePath", IMPORT_DIR);
        request.setAttribute("hours", new Integer(hours).toString());
        request.setAttribute("minutes", new Integer(minutes).toString());

        Date jobDate = trigger.getNextFireTime();

        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());

        // ArrayList studies = udao.findStudyByUser(ub.getName(), (ArrayList)
        // sdao.findAll());
        // request.setAttribute("studies", studies);
        ArrayList<Study> all = (ArrayList<Study>) getStudyDao().findAll();
        ArrayList<Study> finalList = new ArrayList<Study>();
        for (Study sb : all) {
            if (!sb.isSite()) {
                finalList.add(sb);
                // System.out.println("found study name: " + sb.getName());
                finalList.addAll(getStudyDao().findAllByParent(sb.getStudyId()));
            }
        }
        // System.out.println("found list of studies: " + finalList.toString());
        addEntityList("studies", finalList, respage.getString("a_user_cannot_be_created_no_study_as_active"), Page.ADMIN_SYSTEM);
        // tbh >>
        // HashMap presetValues = new HashMap();
        // Calendar calendar = new GregorianCalendar();
        // calendar.setTime(jobDate);
        // presetValues.put(CreateJobImportServlet.DATE_START_JOB + "Hour",
        // calendar.get(Calendar.HOUR_OF_DAY));
        // presetValues.put(CreateJobImportServlet.DATE_START_JOB + "Minute",
        // calendar.get(Calendar.MINUTE));
        // // TODO this will have to match l10n formatting
        // presetValues.put(CreateJobImportServlet.DATE_START_JOB + "Date",
        // (calendar.get(Calendar.MONTH) + 1) + "/" +
        // calendar.get(Calendar.DATE) + "/"
        // + calendar.get(Calendar.YEAR));
        // fp2.setPresetValues(presetValues);
        // setPresetValues(fp2.getPresetValues());

    }

    @Override
    protected void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        TriggerService triggerService = new TriggerService();
        String action = fp.getString("action");
        String triggerName = fp.getString("tname");
        scheduler = getScheduler();
        logger.debug("found trigger name " + triggerName);

        Trigger trigger = scheduler.getTrigger(new TriggerKey(triggerName, TRIGGER_IMPORT_GROUP));
        //System.out.println("found trigger from the other side " + trigger.getFullName());
        if (StringUtil.isBlank(action)) {
            setUpServlet(trigger);
            forwardPage(Page.UPDATE_JOB_IMPORT);
        } else if ("confirmall".equalsIgnoreCase(action)) {
            Set<TriggerKey> triggerKeys = scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals("DEFAULT"));
            String[] triggerNames = triggerKeys.stream().toArray(String[]::new);
            HashMap errors = triggerService.validateImportJobForm(fp, request, triggerNames, trigger.getKey().getName());
            if (!errors.isEmpty()) {
                // send back
                addPageMessage("Your modifications caused an error, please see the messages for more information.");
                setUpServlet(trigger);
                forwardPage(Page.UPDATE_JOB_IMPORT);
            } else {
                int studyId = fp.getInt(CreateJobImportServlet.STUDY_ID);
                Study study = (Study) getStudyDao().findByPK(studyId);
                // in the place of a users' current study, tbh
                Date startDate = trigger.getStartTime();
                trigger = triggerService.generateImportTrigger(fp, sm.getUserBean(), study, startDate, LocaleResolver.getLocale(request).getLanguage());
                // scheduler = getScheduler();
                JobDetailFactoryBean JobDetailFactoryBean = new JobDetailFactoryBean();
                JobDetailFactoryBean.setGroup(TRIGGER_IMPORT_GROUP);
                JobDetailFactoryBean.setName(trigger.getKey().getName());
                JobDetailFactoryBean.setJobClass(core.org.akaza.openclinica.web.job.ImportStatefulJob.class);
                JobDetailFactoryBean.setJobDataMap(trigger.getJobDataMap());
                JobDetailFactoryBean.setDurability(true); // need durability?


                try {
                    scheduler.deleteJob(new JobKey(triggerName, TRIGGER_IMPORT_GROUP));
                    Date dateStart = scheduler.scheduleJob(JobDetailFactoryBean.getObject(), trigger);

                    addPageMessage("Your job has been successfully modified.");
                    forwardPage(Page.VIEW_IMPORT_JOB_SERVLET);
                } catch (SchedulerException se) {
                    logger.error("Job is not able to be deleted: ",se);
                    // set a message here with the exception message
                    setUpServlet(trigger);
                    addPageMessage("There was an unspecified error with your creation, please contact an administrator.");
                    forwardPage(Page.UPDATE_JOB_IMPORT);
                }
            }
        }

    }

}
