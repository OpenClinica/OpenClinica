package org.akaza.openclinica.control.admin;

import core.org.akaza.openclinica.bean.admin.TriggerBean;
import core.org.akaza.openclinica.bean.extract.ArchivedDatasetFileBean;
import core.org.akaza.openclinica.bean.extract.DatasetBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.core.form.StringUtil;
import core.org.akaza.openclinica.dao.extract.ArchivedDatasetFileDAO;
import core.org.akaza.openclinica.dao.extract.DatasetDAO;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.service.extract.XsltTriggerService;
import core.org.akaza.openclinica.web.bean.ArchivedDatasetFileRow;
import core.org.akaza.openclinica.web.bean.EntityBeanTable;
import core.org.akaza.openclinica.web.job.ExampleSpringJob;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.view.Page;
import org.quartz.*;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.*;

public class ViewSingleJobServlet extends ScheduleJobServlet {

    @Override
    protected void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        String action = fp.getString("action");
        int adfId = fp.getInt("adfId");

        ApplicationContext context = null;
        scheduler = getScheduler();
        try {
            context = (ApplicationContext) scheduler.getContext().get("applicationContext");
        } catch (SchedulerException e) {
            logger.error("Error in receiving application context: ", e);
        }
        Scheduler jobScheduler = getSchemaScheduler(request, context, scheduler);


        if (StringUtil.isBlank(action)) {
            loadList(jobScheduler, fp);
            forwardPage(Page.VIEW_SINGLE_JOB);
        } else if ("delete".equalsIgnoreCase(action) && adfId > 0) {
            boolean success = false;

            ArchivedDatasetFileDAO archivedDatasetFileDAO = new ArchivedDatasetFileDAO(sm.getDataSource());
            ArchivedDatasetFileBean adfBean = (ArchivedDatasetFileBean) archivedDatasetFileDAO.findByPK(adfId);


            if (adfBean != null || adfBean.getId() != 0) {
                File file = new File(adfBean.getFileReference());
                if (!file.canWrite()) {
                    addPageMessage(respage.getString("write_protected"));
                } else {
                    success = file.delete();
                    if (success) {
                        archivedDatasetFileDAO.deleteArchiveDataset(adfBean);
                        addPageMessage(respage.getString("file_removed"));
                    } else {
                        addPageMessage(respage.getString("error_removing_file"));
                    }
                }
            }
            loadList(jobScheduler, fp);
            forwardPage(Page.VIEW_SINGLE_JOB);
        }

    }

    public void loadList(Scheduler jobScheduler, FormProcessor fp) throws Exception {
        String jobUuid = fp.getString("jobUuid");
        String triggerName = fp.getString("tname");
        String gName = fp.getString("gname");
        String filterKeyword = fp.getString("ebl_filterKeyword");
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

            if (trigger.getDescription() != null) {
                triggerBean.setDescription(trigger.getDescription());
            }
            if (trigger.getJobDataMap().size() > 0) {
                JobDataMap dataMap = trigger.getJobDataMap();
                String contactEmail = dataMap.getString(XsltTriggerService.EMAIL);
                logger.debug("found email: " + contactEmail);

                if (gName.equals("") || gName.equals("0")) {
                    triggerBean.setFullName(trigger.getJobDataMap().getString(XsltTriggerService.JOB_NAME));
                    triggerBean.setCreatedDate((Date) trigger.getJobDataMap().get(XsltTriggerService.CREATED_DATE));
                    String exportFormat = dataMap.getString(XsltTriggerService.EXPORT_FORMAT);
                    String periodToRun = dataMap.getString(ExampleSpringJob.PERIOD);
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

                ArchivedDatasetFileDAO archivedDatasetFileDAO = new ArchivedDatasetFileDAO(sm.getDataSource());
                ArrayList<ArchivedDatasetFileBean> archivedDatasetFileBeans = archivedDatasetFileDAO.findByJobUuid(jobUuid);
                ArrayList allRows = ArchivedDatasetFileRow.generateRowsFromBeans(archivedDatasetFileBeans);
                EntityBeanTable table = fp.getEntityBeanTable();
                table.setSortingIfNotExplicitlySet(3, false); // sort by date
                String[] columns =
                        {resword.getString("dataset_format"), resword.getString("file_name"), resword.getString("run_time"), resword.getString("file_size"), resword.getString("created_date"),
                                resword.getString("created_by"), resword.getString("status"), resword.getString("action")};
                table.setColumns(new ArrayList(Arrays.asList(columns)));
                table.hideColumnLink(0);
                table.hideColumnLink(1);
                table.hideColumnLink(2);
                table.hideColumnLink(3);
                table.hideColumnLink(4);
                table.hideColumnLink(5);
                table.hideColumnLink(6);
                table.hideColumnLink(7);

                HashMap args = new HashMap();
                args.put("tname", new String(triggerName).toString());
                args.put("gname", new String(gName).toString());
                args.put("jobUuid", new String(jobUuid).toString());
                table.setQuery("ViewSingleJob", args);
                table.setRows(allRows);
                if (filterKeyword != null && !"".equalsIgnoreCase(filterKeyword)) {
                    table.setKeywordFilter(filterKeyword);
                }
                table.computeDisplay();

                request.setAttribute("table", table);
            }

        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            logger.debug(" found NPE " + e);
        }

        request.setAttribute("triggerBean", triggerBean);
        request.setAttribute("jobUuid", jobUuid);
        request.setAttribute("groupName", groupName);
    }
}
