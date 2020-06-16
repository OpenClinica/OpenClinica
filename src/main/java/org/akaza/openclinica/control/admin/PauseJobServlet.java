package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.service.extract.XsltTriggerService;
import org.quartz.*;

/**
 * PauseJobServlet, a small servlet to pause/unpause a trigger in the scehduler.
 * The basic premise, you provide a name which has been validated by JavaScript
 * on the JSP side with a simple confirm dialog box. You get here - the job is
 * either paused or unpaused. Possible complications, if we start using Priority
 * for other things.
 * @author Tom Hickerson, 2009
 */
public class PauseJobServlet extends ScheduleJobServlet {

    @Override
    protected void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        String triggerName = fp.getString("tname");
        String jobUuid = fp.getString("jobUuid");
        String gName = request.getParameter("gname");
        String finalGroupName = "";
        if ("".equals(gName) || "0".equals(gName)) {
            finalGroupName = XsltTriggerService.TRIGGER_GROUP_NAME;
        } else {// should equal 1
            finalGroupName = TRIGGER_IMPORT_GROUP;
        }
        String deleteMe = fp.getString("del");
        applicationContext = getApplicationContext();
        schedulerUtilService = getSchedulerUtilService();
        Scheduler jobScheduler = schedulerUtilService.getSchemaScheduler(applicationContext, request);
        try {
            if (("y".equals(deleteMe)) && (ub.isSysAdmin())) {
                jobScheduler.deleteJob(JobKey.jobKey(triggerName, finalGroupName));
                // set return message here
                logger.debug("deleted job: " + triggerName);
                addPageMessage("The following job " + triggerName + " and its corresponding Trigger have been deleted from the system.");

            } else {

                if (jobScheduler.getTriggerState(TriggerKey.triggerKey(jobUuid, finalGroupName)) == Trigger.TriggerState.PAUSED) {
                    jobScheduler.resumeTrigger(TriggerKey.triggerKey(jobUuid, finalGroupName));
                    // trigger.setPriority(Trigger.DEFAULT_PRIORITY);
                    logger.debug("-- resuming trigger! " + triggerName + " " + finalGroupName);
                    addPageMessage("This trigger " + triggerName + " has been resumed and will continue to run until paused or deleted.");
                    // set message here
                } else {
                    jobScheduler.pauseTrigger(TriggerKey.triggerKey(jobUuid, finalGroupName));
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
