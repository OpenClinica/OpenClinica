package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import core.org.akaza.openclinica.service.extract.XsltTriggerService;
import org.quartz.*;
import org.quartz.impl.StdScheduler;
import org.springframework.context.ApplicationContext;

/**
 * PauseJobServlet, a small servlet to pause/unpause a trigger in the scehduler.
 * The basic premise, you provide a name which has been validated by JavaScript
 * on the JSP side with a simple confirm dialog box. You get here - the job is
 * either paused or unpaused. Possible complications, if we start using Priority
 * for other things.
 * @author Tom Hickerson, 2009
 */
public class PauseJobServlet extends SecureController {

    private static String groupImportName = "importTrigger";

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        // TODO copied from CreateJobExport - DRY? tbh
        if (ub.isSysAdmin() || ub.isTechAdmin()) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_allowed_access_extract_data_servlet"), "1");// TODO
        // above copied from create dataset servlet, needs to be changed to
        // allow only admin-level users

    }

    private StdScheduler getScheduler() {
        scheduler = this.scheduler != null ? scheduler : (StdScheduler) SpringServletAccess.getApplicationContext(context).getBean(SCHEDULER);
        return scheduler;
    }// also perhaps DRY, tbh

    @Override
    protected void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        String triggerName = fp.getString("tname");
        // String gName = fp.getString("gname");
        String gName = request.getParameter("gname");
        String finalGroupName = "";
        if ("".equals(gName) || "0".equals(gName)) {
            finalGroupName = XsltTriggerService.TRIGGER_GROUP_NAME;
        } else {// should equal 1
            finalGroupName = groupImportName;
        }
        String deleteMe = fp.getString("del");
        ApplicationContext context = null;
        scheduler = getScheduler();
        Scheduler jobScheduler;
        try {
            context = (ApplicationContext) scheduler.getContext().get("applicationContext");
        } catch (SchedulerException e) {
            logger.error("Error in receiving application context: ", e);
        }
        jobScheduler = getSchemaScheduler(request, context, scheduler);
        Trigger trigger = jobScheduler.getTrigger(TriggerKey.triggerKey(triggerName, finalGroupName));
        try {
            if (("y".equals(deleteMe)) && (ub.isSysAdmin())) {
                jobScheduler.deleteJob(JobKey.jobKey(triggerName, finalGroupName));
                // set return message here
                logger.debug("deleted job: " + triggerName);
                addPageMessage("The following job " + triggerName + " and its corresponding Trigger have been deleted from the system.");
            } else {

                if (jobScheduler.getTriggerState(TriggerKey.triggerKey(triggerName, finalGroupName)) == Trigger.TriggerState.PAUSED) {
                    jobScheduler.resumeTrigger(TriggerKey.triggerKey(triggerName, finalGroupName));
                    // trigger.setPriority(Trigger.DEFAULT_PRIORITY);
                    logger.debug("-- resuming trigger! " + triggerName + " " + finalGroupName);
                    addPageMessage("This trigger " + triggerName + " has been resumed and will continue to run until paused or deleted.");
                    // set message here
                } else {
                    jobScheduler.pauseTrigger(TriggerKey.triggerKey(triggerName, finalGroupName));
                    // trigger.setPriority(Trigger.STATE_PAUSED);
                    logger.debug("-- pausing trigger! " + triggerName + " " + finalGroupName);
                    addPageMessage("This trigger " + triggerName + " has been paused, and will not run again until it is restored.");
                    // set message here
                }
            }
        } catch (NullPointerException e) {
            logger.error("Scheduler cannot deleteJob: ", e);
        }
        // all validation done on JSP side
        // forward back to view job servlet here
        // set a message
        if ("".equals(gName) || "0".equals(gName)) {
            forwardPage(Page.VIEW_JOB_SERVLET);
        } else {
            forwardPage(Page.VIEW_IMPORT_JOB_SERVLET);
        }
    }

}
