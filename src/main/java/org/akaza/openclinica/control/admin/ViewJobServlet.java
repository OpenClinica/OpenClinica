package org.akaza.openclinica.control.admin;

import java.util.*;

import core.org.akaza.openclinica.bean.admin.TriggerBean;
import core.org.akaza.openclinica.bean.extract.DatasetBean;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.job.AutowiringSpringBeanJobFactory;
import core.org.akaza.openclinica.job.JobExecutionExceptionListener;
import core.org.akaza.openclinica.job.JobTriggerListener;
import core.org.akaza.openclinica.job.OpenClinicaSchedulerFactoryBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import core.org.akaza.openclinica.dao.extract.DatasetDAO;
import core.org.akaza.openclinica.service.extract.XsltTriggerService;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import core.org.akaza.openclinica.web.bean.EntityBeanTable;
import core.org.akaza.openclinica.web.bean.TriggerRow;
import core.org.akaza.openclinica.web.job.ExampleSpringJob;
import org.apache.commons.lang.StringUtils;
import org.quartz.*;
import org.quartz.impl.StdScheduler;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import static core.org.akaza.openclinica.dao.hibernate.multitenant.CurrentTenantIdentifierResolverImpl.CURRENT_TENANT_ID;

/**
 * @author thickerson purpose: to generate the list of jobs and allow us to view them
 */
public class ViewJobServlet extends SecureController {

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
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
    }


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
        Scheduler jobScheduler;
        try {
            context = (ApplicationContext) scheduler.getContext().get("applicationContext");
        } catch (SchedulerException e) {
            logger.error("Error in receiving application context: ", e);
        }
        jobScheduler = getSchemaScheduler(request, context, scheduler);
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
            triggerBean.setFullName(trigger.getKey().getName());
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
                int dsId = dataMap.getInt(ExampleSpringJob.DATASET_ID);
                String periodToRun = dataMap.getString(ExampleSpringJob.PERIOD);
                triggerBean.setPeriodToRun(periodToRun);
                DatasetBean dataset = (DatasetBean) datasetDAO.findByPK(dsId);
                triggerBean.setDataset(dataset);
                triggerBean.setDatasetName(dataset.getName());
                Study study = (Study) getStudyDao().findByPK(dataset.getStudyId());
                triggerBean.setStudyName(study.getName());
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
        String[] columns =
                {resword.getString("name"), resword.getString("previous_fire_time"), resword.getString("next_fire_time"), resword.getString("description"),
                        resword.getString("period_to_run"), resword.getString("dataset"), resword.getString("study"), resword.getString("actions")};
        table.setColumns(new ArrayList(Arrays.asList(columns)));
        table.hideColumnLink(3);
        table.hideColumnLink(7);
        table.setQuery("ViewJob", new HashMap());
        table.setSortingColumnInd(0);
        table.setRows(allRows);
        table.computeDisplay();

        request.setAttribute("table", table);
        forwardPage(Page.VIEW_JOB);

    }

}
