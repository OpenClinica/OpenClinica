package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.admin.AuditEventBean;
import org.akaza.openclinica.bean.admin.TriggerBean;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.admin.AuditEventDAO;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.service.extract.XsltTriggerService;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.bean.AuditEventRow;
import org.akaza.openclinica.web.bean.EntityBeanTable;
import org.akaza.openclinica.web.job.ExampleSpringJob;
import org.quartz.JobDataMap;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdScheduler;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ViewSingleJobServlet extends SecureController {

    // DRY consolidate from other servlet?
    private static String TRIGGER_GROUP = "DEFAULT";
    private static String TRIGGER_IMPORT_GROUP = "importTrigger";
    private static String SCHEDULER = "schedulerFactoryBean";
    private static String EXPORT_TRIGGER = "exportTrigger";

    private SchedulerFactoryBean schedulerFactoryBean;
    private StdScheduler scheduler;

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        // TODO copied from CreateJobExport - DRY? tbh
        if (ub.isSysAdmin() || ub.isTechAdmin()) {
            return;
        }
//        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {// ?
//             ?
//            return;
//        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_allowed_access_extract_data_servlet"), "1");// TODO
        // above copied from create dataset servlet, needs to be changed to
        // allow only admin-level users

    }

    private StdScheduler getScheduler() {
        scheduler = this.scheduler != null ? scheduler : (StdScheduler) SpringServletAccess.getApplicationContext(context).getBean(SCHEDULER);
        return scheduler;
    }

    @Override
    protected void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        // changes to this servlet, we now look at group name too, tbh 05/2009
        String triggerName = fp.getString("tname");
        String gName = fp.getString("gname");
        String groupName = "";
        if (gName.equals("") || gName.equals("0")) {
            groupName = XsltTriggerService.TRIGGER_GROUP_NAME;
        } else { // if (gName.equals("1")) {
            groupName = TRIGGER_IMPORT_GROUP;
        }
        // << tbh 09/03/2009 #4143
        scheduler = getScheduler();
        Trigger trigger = scheduler.getTrigger(new TriggerKey(triggerName, groupName));

        // trigger bean is a wrapper for the trigger, to serve as a link btw
        // quartz classes and oc classes
        // is it really necessary? DRY

        if (trigger == null) {
            groupName = XsltTriggerService.TRIGGER_GROUP_NAME;
            trigger = scheduler.getTrigger(new TriggerKey(triggerName.trim(), groupName));
        }
        // << tbh 09/03/2009 #4143
        // above is a hack, if we add more trigger groups this will have
        // to be redone
        logger.debug("found trigger name: " + triggerName);
        logger.debug("found group name: " + groupName);
          TriggerBean triggerBean = new TriggerBean();
        JobDataMap dataMap = new JobDataMap();
        AuditEventDAO auditEventDAO = new AuditEventDAO(sm.getDataSource());

        try {
            triggerBean.setFullName(trigger.getKey().getName());
            triggerBean.setPreviousDate(trigger.getPreviousFireTime());
            triggerBean.setNextDate(trigger.getNextFireTime());
            // >> set active here, tbh 10/08/2009
            if (scheduler.getTriggerState(new TriggerKey(triggerName, groupName)) == Trigger.TriggerState.PAUSED) {
                triggerBean.setActive(false);
                logger.debug("setting active to false for trigger: " + trigger.getKey().getName());
            } else {
                triggerBean.setActive(true);
                logger.debug("setting active to TRUE for trigger: " + trigger.getKey().getName());
            }
            // <<
            if (trigger.getDescription() != null) {
                triggerBean.setDescription(trigger.getDescription());
            }
            if (trigger.getJobDataMap().size() > 0) {
                dataMap = trigger.getJobDataMap();
                String contactEmail = dataMap.getString(XsltTriggerService.EMAIL);
                logger.debug("found email: " + contactEmail);
                // String datasetId =
                // dataMap.getString(ExampleSpringJob.DATASET_ID);
                // int dsId = new Integer(datasetId).intValue();
                if (gName.equals("") || gName.equals("0")) {
                    String exportFormat = dataMap.getString(XsltTriggerService.EXPORT_FORMAT);
                    String periodToRun = dataMap.getString(ExampleSpringJob.PERIOD);
                    // int userId = new Integer(userAcctId).intValue();
                    int dsId = dataMap.getInt(ExampleSpringJob.DATASET_ID);
                    triggerBean.setExportFormat(exportFormat);
                    triggerBean.setPeriodToRun(periodToRun);
                    DatasetDAO datasetDAO = new DatasetDAO(sm.getDataSource());
                    DatasetBean dataset = (DatasetBean) datasetDAO.findByPK(dsId);
                    triggerBean.setDataset(dataset);
                }
                int userId = dataMap.getInt(ExampleSpringJob.USER_ID);
                // need to set information, extract bean, user account bean

                UserAccountDAO userAccountDAO = new UserAccountDAO(sm.getDataSource());

                triggerBean.setContactEmail(contactEmail);

                UserAccountBean userAccount = (UserAccountBean) userAccountDAO.findByPK(userId);

                triggerBean.setUserAccount(userAccount);

                ArrayList<AuditEventBean> triggerLogs = auditEventDAO.findAllByAuditTable(trigger.getKey().getName());

                // set the table for the audit event beans here

                ArrayList allRows = AuditEventRow.generateRowsFromBeans(triggerLogs);

                EntityBeanTable table = fp.getEntityBeanTable();
                String[] columns = { resword.getString("date_and_time"), resword.getString("action_message"), resword.getString("entity_operation"),
                // resword.getString("study_site"),
                    // resword.getString("study_subject_ID"),
                    resword.getString("changes_and_additions"), resword.getString("actions") };

                table.setColumns(new ArrayList(Arrays.asList(columns)));
                table.setAscendingSort(false);
                table.hideColumnLink(1);
                table.hideColumnLink(3);
                table.hideColumnLink(4);

                table.setQuery("ViewSingleJob?tname=" + triggerName + "&gname=" + gName, new HashMap());
                table.setRows(allRows);
                table.computeDisplay();

                request.setAttribute("table", table);
            }

        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            logger.debug(" found NPE " + e.getMessage());
            e.printStackTrace();
        }
        // need to show the extract for which this runs, which files, etc
        // in other words the job data map

        request.setAttribute("triggerBean", triggerBean);

        request.setAttribute("groupName", groupName);

        forwardPage(Page.VIEW_SINGLE_JOB);
    }
}
