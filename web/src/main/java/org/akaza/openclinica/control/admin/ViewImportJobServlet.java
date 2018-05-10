package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.admin.TriggerBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.bean.EntityBeanTable;
import org.akaza.openclinica.web.bean.TriggerRow;
import org.akaza.openclinica.web.job.ExampleSpringJob;
import org.quartz.JobDataMap;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdScheduler;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

/**
 * 
 * @author thickerson purpose: to serve as the main UI for listing
 *         ImportSpringJob.java
 */
public class ViewImportJobServlet extends SecureController {

    private static String TRIGGER_GROUP = "DEFAULT";
    private static String SCHEDULER = "schedulerFactoryBean";
    private static String IMPORT_TRIGGER = "importTrigger";

    private SchedulerFactoryBean schedulerFactoryBean;
    private StdScheduler scheduler;

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin() || ub.isTechAdmin()) {
            return;
        }
//        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
//
//            return;
//        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_allowed_access_extract_data_servlet"), "1");// TODO
        // changed to
        // allow only admin-level users
    }

    private StdScheduler getScheduler() {
        scheduler = this.scheduler != null ? scheduler : (StdScheduler) SpringServletAccess.getApplicationContext(context).getBean(SCHEDULER);
        return scheduler;
    }

    @Override
    protected void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        // First we must get a reference to a scheduler
        scheduler = getScheduler();
        // then we pull all the triggers that are specifically named
        // IMPORT_TRIGGER.

        Set<TriggerKey> triggerKeys = scheduler.getTriggerKeys(GroupMatcher.groupEquals(IMPORT_TRIGGER));

        // the next bit goes out and processes all the triggers
        ArrayList triggerBeans = new ArrayList<TriggerBean>();

        for (TriggerKey triggerKey : triggerKeys) {
            String triggerName = triggerKey.getName();
            Trigger trigger = scheduler.getTrigger(triggerKey);
            logger.debug("found trigger, full name: " + triggerName);
            try {
                logger.debug("prev fire time " + trigger.getPreviousFireTime().toString());
                logger.debug("next fire time " + trigger.getNextFireTime().toString());
                logger.debug("final fire time: " + trigger.getFinalFireTime().toString());
            } catch (NullPointerException npe) {
                // could be nulls in the dates, etc
            }

            TriggerBean triggerBean = new TriggerBean();
            triggerBean.setFullName(triggerName);
            triggerBean.setPreviousDate(trigger.getPreviousFireTime());
            triggerBean.setNextDate(trigger.getNextFireTime());
            if (trigger.getDescription() != null) {
                triggerBean.setDescription(trigger.getDescription());
            }
            // this next bit of code looks at the job data map and pulls out
            // specific items
            JobDataMap dataMap = new JobDataMap();

            if (trigger.getJobDataMap().size() > 0) {
                dataMap = trigger.getJobDataMap();
                triggerBean.setStudyName(dataMap.getString(ExampleSpringJob.STUDY_NAME));
                String oid = dataMap.getString("study_oid");
               
            }

            // this next bit of code looks to see if the trigger is paused
            logger.debug("Trigger Priority: " + triggerName + " " + trigger.getPriority());
            if (scheduler.getTriggerState(new TriggerKey(triggerName, IMPORT_TRIGGER)) == Trigger.TriggerState.PAUSED) {
                triggerBean.setActive(false);
                logger.debug("setting active to false for trigger: " + triggerName);
            } else {
                triggerBean.setActive(true);
                logger.debug("setting active to TRUE for trigger: " + triggerName);
            }
            triggerBeans.add(triggerBean);
            // our wrapper to show triggers
        }

        // set up the table here and get ready to send to the web page

        ArrayList allRows = TriggerRow.generateRowsFromBeans(triggerBeans);

        EntityBeanTable table = fp.getEntityBeanTable();
        String[] columns = { resword.getString("name"), resword.getString("previous_fire_time"), 
            resword.getString("next_fire_time"), resword.getString("description"), resword.getString("study"), 
            resword.getString("actions") };
        table.setColumns(new ArrayList(Arrays.asList(columns)));
        table.hideColumnLink(3);
        table.hideColumnLink(5);
        table.setQuery("ViewImportJob", new HashMap());
        // table.addLink("", "CreateUserAccount");
        table.setSortingColumnInd(0);
        table.setRows(allRows);
        table.computeDisplay();

        request.setAttribute("table", table);

        forwardPage(Page.VIEW_IMPORT_JOB);

    }

}
