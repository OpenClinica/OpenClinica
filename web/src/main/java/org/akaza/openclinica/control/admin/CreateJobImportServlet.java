/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;
import org.akaza.openclinica.web.job.ImportSpringJob;
import org.akaza.openclinica.web.job.TriggerService;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdScheduler;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

/**
 * Create Job Import Servlet, by Tom Hickerson, 2009
 *
 * @author thickerson Purpose: to create jobs in the 'importTrigger' group,
 *         which will be meant to run the ImportStatefulJob.
 */
public class CreateJobImportServlet extends SecureController {

    private static String SCHEDULER = "schedulerFactoryBean";
    private static String IMPORT_TRIGGER = "importTrigger";

    public static final String DATE_START_JOB = "job";
    public static final String EMAIL = "contactEmail";
    public static final String JOB_NAME = "jobName";
    public static final String JOB_DESC = "jobDesc";
    public static final String USER_ID = "user_id";
    public static final String HOURS = "hours";
    public static final String MINUTES = "minutes";
    public static final String DIRECTORY = "filePathDir";
    public static final String DIR_PATH = ImportSpringJob.DIR_PATH;
    public static final String STUDY_ID = "studyId";

    private StdScheduler scheduler;

    // private SimpleTrigger trigger;
    // private JobDataMap jobDataMap;

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin() || ub.isTechAdmin()) {
            return;
        }
//        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {// ?
//
//            return;
//        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_allowed_access_extract_data_servlet"), "1");// TODO

        // allow only admin-level users

    }

    private StdScheduler getScheduler() {
        scheduler = this.scheduler != null ? scheduler : (StdScheduler) SpringServletAccess.getApplicationContext(context).getBean(SCHEDULER);
        return scheduler;
    }

    /*
     * Find all the form items and re-populate as necessary
     */
    private void setUpServlet() throws Exception {
        String directory = SQLInitServlet.getField("filePath") + DIR_PATH + File.separator;
        logger.debug("found directory: " + directory);
        // find all the form items and re-populate them if necessary
        FormProcessor fp2 = new FormProcessor(request);

        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
        StudyDAO sdao = new StudyDAO(sm.getDataSource());

        // ArrayList studies = udao.findStudyByUser(ub.getName(), (ArrayList)
        // sdao.findAll());
        // request.setAttribute("studies", studies);
        // tbh, replacing the above with another version, 06/2009
        ArrayList<StudyBean> all = (ArrayList<StudyBean>) sdao.findAll();
        ArrayList<StudyBean> finalList = new ArrayList<StudyBean>();
        for (StudyBean sb : all) {
            if (!(sb.getParentStudyId() > 0)) {
                finalList.add(sb);
                // System.out.println("found study name: " + sb.getName());
                finalList.addAll(sdao.findAllByParent(sb.getId()));
            }
        }
        // System.out.println("found list of studies: " + finalList.toString());
        addEntityList("studies", finalList, respage.getString("a_user_cannot_be_created_no_study_as_active"), Page.ADMIN_SYSTEM);
        // YW >>
        // << tbh

        request.setAttribute("filePath", directory);
        // request.setAttribute("activeStudy", activeStudy);

        request.setAttribute(JOB_NAME, fp2.getString(JOB_NAME));
        request.setAttribute(JOB_DESC, fp2.getString(JOB_DESC));
        request.setAttribute(EMAIL, fp2.getString(EMAIL));
        request.setAttribute(HOURS, new Integer(fp2.getInt(HOURS)).toString());
        request.setAttribute(MINUTES, new Integer(fp2.getInt(MINUTES)).toString());

    }

    @Override
    protected void processRequest() throws Exception {
        // TODO multi stage servlet to generate import jobs
        // validate form, create job and return to view jobs servlet
        FormProcessor fp = new FormProcessor(request);
        TriggerService triggerService = new TriggerService();
        scheduler = getScheduler();
        String action = fp.getString("action");
        if (StringUtil.isBlank(action)) {
            // set up list of data sets
            // select by ... active study
            setUpServlet();

            forwardPage(Page.CREATE_JOB_IMPORT);
        } else if ("confirmall".equalsIgnoreCase(action)) {
            // collect form information
            Set<TriggerKey> triggerKeys = scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(IMPORT_TRIGGER));
            String[] triggerNames = triggerKeys.stream().toArray(String[]::new);
            HashMap errors = triggerService.validateImportJobForm(fp, request, triggerNames);

            if (!errors.isEmpty()) {
                // set errors to request
                request.setAttribute("formMessages", errors);
                logger.debug("has validation errors in the first section"  + errors.toString());
                setUpServlet();

                forwardPage(Page.CREATE_JOB_IMPORT);
            } else {
                logger.info("found no validation errors, continuing");
                int studyId = fp.getInt(STUDY_ID);
                StudyDAO studyDAO = new StudyDAO(sm.getDataSource());
                StudyBean studyBean = (StudyBean) studyDAO.findByPK(studyId);
                SimpleTrigger trigger = triggerService.generateImportTrigger(fp, sm.getUserBean(), studyBean, LocaleResolver.getLocale(request).getLanguage());
                
                JobDetailFactoryBean jobDetailBean = new JobDetailFactoryBean();
                jobDetailBean.setGroup(IMPORT_TRIGGER);
                jobDetailBean.setName(trigger.getKey().getName());
                jobDetailBean.setJobClass(org.akaza.openclinica.web.job.ImportStatefulJob.class);
                jobDetailBean.setJobDataMap(trigger.getJobDataMap());
                jobDetailBean.setDurability(true); // need durability?
                jobDetailBean.afterPropertiesSet();

                // set to the scheduler
                try {
                    Date dateStart = scheduler.scheduleJob(jobDetailBean.getObject(), trigger);
                    logger.debug("== found job date: " + dateStart.toString());
                    // set a success message here
                    addPageMessage("You have successfully created a new job: " + trigger.getKey().getName() + " which is now set to run at the time you specified.");
                    forwardPage(Page.VIEW_IMPORT_JOB_SERVLET);
                } catch (SchedulerException se) {
                    logger.error("Scheduler is not able to create a new job: ", se);
                    // set a message here with the exception message
                    setUpServlet();
                    addPageMessage("There was an unspecified error with your creation, please contact an administrator.");
                    forwardPage(Page.CREATE_JOB_IMPORT);
                }
            }
        } else {
            forwardPage(Page.ADMIN_SYSTEM);
            // forward to form
            // should we even get to this part?
        }

    }

}
