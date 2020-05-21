package org.akaza.openclinica.control.admin;

import core.org.akaza.openclinica.bean.admin.AuditEventBean;
import core.org.akaza.openclinica.bean.admin.TriggerBean;
import core.org.akaza.openclinica.bean.extract.DatasetBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.dao.admin.AuditEventDAO;
import core.org.akaza.openclinica.dao.extract.DatasetDAO;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.service.extract.XsltTriggerService;
import core.org.akaza.openclinica.web.bean.AuditEventRow;
import core.org.akaza.openclinica.web.bean.EntityBeanTable;
import core.org.akaza.openclinica.web.job.ExampleSpringJob;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.view.Page;
import org.quartz.*;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class ViewSingleJobServlet extends ScheduleJobServlet {

    @Override
    protected void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        ApplicationContext context = null;
        scheduler = getScheduler();
        try {
            context = (ApplicationContext) scheduler.getContext().get("applicationContext");
        } catch (SchedulerException e) {
            logger.error("Error in receiving application context: ", e);
        }
        Scheduler jobScheduler = getSchemaScheduler(request, context, scheduler);
        // changes to this servlet, we now look at group name too, tbh 05/2009
        String jobUuid = fp.getString("jobUuid");
        String triggerName = fp.getString("tname");
        String gName = fp.getString("gname");
        String groupName = "";
        if (gName.equals("") || gName.equals("0")) {
            groupName = TRIGGER_EXPORT_GROUP;
        } else { // if (gName.equals("1")) {
            groupName = TRIGGER_IMPORT_GROUP;
        }

        Trigger trigger = jobScheduler.getTrigger(new TriggerKey(jobUuid, groupName));

        logger.debug("found job Uuid: " + jobUuid);
        logger.debug("found trigger name: " + triggerName);
        logger.debug("found group name: " + groupName);
        TriggerBean triggerBean = new TriggerBean();
        AuditEventDAO auditEventDAO = new AuditEventDAO(sm.getDataSource(), getStudyDao());

        try {
            triggerBean.setPreviousDate(trigger.getPreviousFireTime());
            triggerBean.setNextDate(trigger.getNextFireTime());
            triggerBean.setJobUuid(jobUuid);

            if (jobScheduler.getTriggerState(new TriggerKey(triggerName, groupName)) == Trigger.TriggerState.PAUSED) {
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
                JobDataMap dataMap = trigger.getJobDataMap();
                String contactEmail = dataMap.getString(XsltTriggerService.EMAIL);
                logger.debug("found email: " + contactEmail);
                // String datasetId =
                // dataMap.getString(ExampleSpringJob.DATASET_ID);
                // int dsId = new Integer(datasetId).intValue();
                if (gName.equals("") || gName.equals("0")) {
                    triggerBean.setFullName(trigger.getJobDataMap().getString(XsltTriggerService.JOB_NAME));
                    triggerBean.setCreatedDate((Date) trigger.getJobDataMap().get(XsltTriggerService.CREATED_DATE));
                    String exportFormat = dataMap.getString(XsltTriggerService.EXPORT_FORMAT);
                    String periodToRun = dataMap.getString(ExampleSpringJob.PERIOD);
                    String createdDate = dataMap.getString("");
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
                String[] columns = {resword.getString("date_and_time"), resword.getString("action_message"), resword.getString("entity_operation"),
                        resword.getString("changes_and_additions")};

                table.setColumns(new ArrayList(Arrays.asList(columns)));
                table.setAscendingSort(false);
                table.hideColumnLink(1);
                table.hideColumnLink(3);
                table.hideColumnLink(4);

                table.setQuery("ViewSingleJob?tname=" + triggerName + "&gname=" + gName + "&jobUuid=" + jobUuid, new HashMap());
                table.setRows(allRows);
                table.computeDisplay();

                request.setAttribute("table", table);
            }

        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            logger.debug(" found NPE " + e);
        }
        // need to show the extract for which this runs, which files, etc
        // in other words the job data map

        request.setAttribute("triggerBean", triggerBean);
        request.setAttribute("jobUuid", jobUuid);
        request.setAttribute("groupName", groupName);

        forwardPage(Page.VIEW_SINGLE_JOB);
    }
}
