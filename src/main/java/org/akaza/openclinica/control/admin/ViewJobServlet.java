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
import org.springframework.context.ApplicationContext;

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
        // TODO single stage servlet where we get the list of jobs
        // and push them out to the JSP page
        // related classes will be required to generate the table rows
        // and eventually links to view and edit the jobs as well
        FormProcessor fp = new FormProcessor(request);
        // First we must get a reference to a scheduler
        ApplicationContext context = null;
        scheduler = getScheduler();
        try {
            context = (ApplicationContext) scheduler.getContext().get("applicationContext");
        } catch (SchedulerException e) {
            logger.error("Error in receiving application context: ", e);
        }
        Scheduler jobScheduler = getSchemaScheduler(request, context, scheduler);
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
            JobDataMap dataMap = new JobDataMap();
            DatasetDAO datasetDAO = new DatasetDAO(sm.getDataSource());
            if (trigger.getJobDataMap().size() > 0) {
                dataMap = trigger.getJobDataMap();
                triggerBean.setFullName(dataMap.getString(XsltTriggerService.JOB_NAME));
                int dsId = dataMap.getInt(ExampleSpringJob.DATASET_ID);
                String periodToRun = dataMap.getString(ExampleSpringJob.PERIOD);
                triggerBean.setPeriodToRun(periodToRun);
                DatasetBean dataset = (DatasetBean) datasetDAO.findByPK(dsId);
                triggerBean.setDataset(dataset);
                triggerBean.setDatasetName(dataset.getName());
                Study study = getStudyDao().findByPK(dataset.getStudyId());
                triggerBean.setStudyName(study.getName());
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
                        resword.getString("period_to_run"), resword.getString("dataset"), resword.getString("study"), resword.getString("actions")};
        table.setColumns(new ArrayList(Arrays.asList(columns)));
        table.hideColumnLink(0);
        table.hideColumnLink(1);
        table.hideColumnLink(2);
        table.hideColumnLink(3);
        table.hideColumnLink(4);
        table.hideColumnLink(5);
        table.hideColumnLink(6);
        table.hideColumnLink(7);
        table.setQuery("ViewJob", new HashMap());
        table.setRows(allRows);
        table.computeDisplay();

        request.setAttribute("table", table);
        forwardPage(Page.VIEW_JOB);

    }

}
