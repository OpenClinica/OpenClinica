package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.admin.TriggerBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.bean.EntityBeanTable;
import org.akaza.openclinica.web.bean.TriggerRow;
import org.akaza.openclinica.web.job.ExampleSpringJob;
import org.akaza.openclinica.service.extract.XsltTriggerService;
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
 * @author thickerson purpose: to generate the list of jobs and allow us to view them
 */
public class ViewJobServlet extends SecureController {

   
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
//            // ?
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
        // TODO single stage servlet where we get the list of jobs
        // and push them out to the JSP page
        // related classes will be required to generate the table rows
        // and eventually links to view and edit the jobs as well
        FormProcessor fp = new FormProcessor(request);
        // First we must get a reference to a scheduler
        scheduler = getScheduler();
        XsltTriggerService xsltTriggerSrvc = new XsltTriggerService();
        // Scheduler sched = sfb.getScheduler();

        Set<TriggerKey> triggerKeys = scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(xsltTriggerSrvc.getTriggerGroupNameForExportJobs()));
        String[] triggerNames = triggerKeys.stream().toArray(String[]::new);

  //      String[]    triggerNames          =           scheduler.getJobNames(XsltTriggerService.TRIGGER_GROUP_NAME);
        // logger.info("trigger list: "+triggerNames.length);
        // logger.info("trigger names: "+triggerNames.toString());


        ArrayList triggerBeans = new ArrayList();
        for (String triggerName : triggerNames) {
            Trigger trigger = scheduler.getTrigger(TriggerKey.triggerKey(triggerName, xsltTriggerSrvc.getTriggerGroupNameForExportJobs()));
            try {
                logger.debug("prev fire time " + trigger.getPreviousFireTime().toString());
                logger.debug("next fire time " + trigger.getNextFireTime().toString());
                logger.debug("final fire time: " + trigger.getFinalFireTime().toString());
            } catch (NullPointerException npe) {
                // could be nulls in the dates, etc
            }

            // logger.info(trigger.getDescription());
            // logger.info("");//getJobDataMap()
            TriggerBean triggerBean = new TriggerBean();
            triggerBean.setFullName(trigger.getKey().getName());
            triggerBean.setPreviousDate(trigger.getPreviousFireTime());
            triggerBean.setNextDate(trigger.getNextFireTime());
            if (trigger.getDescription() != null) {
                triggerBean.setDescription(trigger.getDescription());
            }
            // setting: frequency, dataset name
            JobDataMap dataMap = new JobDataMap();
            DatasetDAO datasetDAO = new DatasetDAO(sm.getDataSource());
            StudyDAO studyDao = new StudyDAO(sm.getDataSource());
            if (trigger.getJobDataMap().size() > 0) {
                dataMap = trigger.getJobDataMap();
                int dsId = dataMap.getInt(ExampleSpringJob.DATASET_ID);
                String periodToRun = dataMap.getString(ExampleSpringJob.PERIOD);
                triggerBean.setPeriodToRun(periodToRun);
                DatasetBean dataset = (DatasetBean) datasetDAO.findByPK(dsId);
                triggerBean.setDataset(dataset);
                triggerBean.setDatasetName(dataset.getName());
                StudyBean study = (StudyBean) studyDao.findByPK(dataset.getStudyId());
                triggerBean.setStudyName(study.getName());
                // triggerBean.setStudyName(dataMap.getString(ExampleSpringJob.STUDY_NAME));
            }
            logger.debug("Trigger Priority: " + trigger.getKey().getName() + " " + trigger.getPriority());
            if (scheduler.getTriggerState(TriggerKey.triggerKey(triggerName, XsltTriggerService.TRIGGER_GROUP_NAME)) == Trigger.TriggerState.PAUSED) {
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
        String[] columns =
            { resword.getString("name"), resword.getString("previous_fire_time"), resword.getString("next_fire_time"), resword.getString("description"),
                resword.getString("period_to_run"), resword.getString("dataset"), resword.getString("study"), resword.getString("actions") };
        table.setColumns(new ArrayList(Arrays.asList(columns)));
        table.hideColumnLink(3);
        table.hideColumnLink(7);
        table.setQuery("ViewJob", new HashMap());
        // table.addLink("", "CreateUserAccount");
        table.setSortingColumnInd(0);
        table.setRows(allRows);
        table.computeDisplay();

        request.setAttribute("table", table);
        // throw new NullPointerException("faking an error here");
        forwardPage(Page.VIEW_JOB);

    }

}
