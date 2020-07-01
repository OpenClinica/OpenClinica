package org.akaza.openclinica.control.admin;

import core.org.akaza.openclinica.bean.admin.TriggerBean;
import core.org.akaza.openclinica.bean.extract.DatasetBean;
import core.org.akaza.openclinica.dao.extract.DatasetDAO;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.service.extract.XsltTriggerService;
import core.org.akaza.openclinica.web.bean.EntityBeanTable;
import core.org.akaza.openclinica.web.bean.TriggerRow;
import core.org.akaza.openclinica.web.job.ExampleSpringJob;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.view.Page;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

/**
 * @author thickerson purpose: to generate the list of jobs and allow us to view them
 */
public class ViewJobServlet extends ScheduleJobServlet {

    @Override
    protected void processRequest() throws Exception {

        FormProcessor fp = new FormProcessor(request);
        // First we must get a reference to a scheduler
        schedulerUtilService = getSchedulerUtilService();
        applicationContext = getApplicationContext();
        Scheduler jobScheduler = schedulerUtilService.getSchemaScheduler(applicationContext, request);
        XsltTriggerService xsltTriggerSrvc = new XsltTriggerService();
        Set<TriggerKey> triggerKeySet = jobScheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(xsltTriggerSrvc.getTriggerGroupNameForExportJobs()));
        TriggerKey[] triggerKeys = triggerKeySet.stream().toArray(TriggerKey[]::new);

        ArrayList triggerBeans = new ArrayList();
        for (TriggerKey triggerKey : triggerKeys) {
            Trigger trigger = jobScheduler.getTrigger(triggerKey);
            try {
                logger.debug("prev fire time " + trigger.getPreviousFireTime().toString());
                logger.debug("next fire time " + trigger.getNextFireTime().toString());
                logger.debug("final fire time: " + trigger.getFinalFireTime().toString());
            } catch (NullPointerException npe) {
                // could be nulls in the dates, etc
            }
            TriggerBean triggerBean = new TriggerBean();
            triggerBean.setPreviousDate(trigger.getPreviousFireTime());
            triggerBean.setNextDate(trigger.getNextFireTime());
            if (trigger.getDescription() != null) {
                triggerBean.setDescription(trigger.getDescription());
            }
            // setting: frequency, dataset name
            DatasetDAO datasetDAO = new DatasetDAO(sm.getDataSource());
            if (trigger.getJobDataMap().size() > 0) {
                JobDataMap dataMap = trigger.getJobDataMap();
                triggerBean.setFullName(dataMap.getString(XsltTriggerService.JOB_NAME));
                int dsId = dataMap.getInt(ExampleSpringJob.DATASET_ID);
                String periodToRun = dataMap.getString(ExampleSpringJob.PERIOD);
                triggerBean.setPeriodToRun(periodToRun);
                DatasetBean dataset = (DatasetBean) datasetDAO.findByPK(dsId);
                triggerBean.setDataset(dataset);
                triggerBean.setDatasetName(dataset.getName());
                triggerBean.setJobUuid(trigger.getKey().getName());
            }
            logger.debug("Trigger Priority: " + trigger.getKey().getName() + " " + trigger.getPriority());
            if (jobScheduler.getTriggerState(triggerKey) == Trigger.TriggerState.PAUSED) {
                triggerBean.setActive(false);
                logger.debug("setting active to false for trigger: " + trigger.getKey().getName());
            } else {
                triggerBean.setActive(true);
                logger.debug("setting active to TRUE for trigger: " + trigger.getKey().getName());
            }
            triggerBeans.add(triggerBean);
            // our wrapper to show triggers
        }

        ArrayList allRows = TriggerRow.generateRowsFromBeans(triggerBeans);
        EntityBeanTable table = fp.getEntityBeanTable();
        table.setSortingIfNotExplicitlySet(0, true); // sort by name
        String[] columns =
                {resword.getString("name"), resword.getString("previous_fire_time"), resword.getString("next_fire_time"), resword.getString("description"),
                        resword.getString("period_to_run"), resword.getString("dataset"), resword.getString("actions")};
        table.setColumns(new ArrayList(Arrays.asList(columns)));
        table.hideColumnLink(3);
        table.hideColumnLink(6);
        table.setQuery("ViewJob", new HashMap());
        table.setRows(allRows);
        table.computeDisplay();

        request.setAttribute("table", table);
        forwardPage(Page.VIEW_JOB);

    }

}
