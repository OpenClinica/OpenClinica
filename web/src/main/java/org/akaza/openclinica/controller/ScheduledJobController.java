package org.akaza.openclinica.controller;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.akaza.openclinica.bean.extract.ExtractPropertyBean;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.extract.XsltTriggerService;
import org.akaza.openclinica.web.table.scheduledjobs.ScheduledJobTableFactory;
import org.akaza.openclinica.web.table.scheduledjobs.ScheduledJobs;
import org.akaza.openclinica.web.table.sdv.SDVUtil;
import org.jmesa.facade.TableFacade;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author jnyayapathi
 *  Controller for listing all the scheduled jobs. Also an interface for canceling the jobs which are running.
 */
@Controller("ScheduledJobController")
public class ScheduledJobController {
    private final static Logger logger = LoggerFactory.getLogger(ScheduledJobController.class);

    public final static String SCHEDULED_TABLE_ATTRIBUTE = "scheduledTableAttribute";

    @Autowired
    @Qualifier("scheduledJobTableFactory")
    private  ScheduledJobTableFactory  scheduledJobTableFactory;
    public static final String EP_BEAN = "epBean";

    @Autowired
    @Qualifier("sdvUtil")
    private SDVUtil sdvUtil;

    @Autowired
    private Scheduler scheduler;

    @RequestMapping("/listCurrentScheduledJobs")
    public ModelMap listScheduledJobs(HttpServletRequest request, HttpServletResponse response) throws SchedulerException{
        Locale locale = LocaleResolver.getLocale(request);
        ResourceBundleProvider.updateLocale(locale);
        ModelMap gridMap = new ModelMap();
        TriggerKey[] triggerKeys;

        boolean showMoreLink = false;
        if(request.getParameter("showMoreLink")!=null){
            showMoreLink = Boolean.parseBoolean(request.getParameter("showMoreLink").toString());
        }else{
            showMoreLink = true;
        }
        request.setAttribute("showMoreLink", showMoreLink+"");


        // request.setAttribute("studySubjectId",studySubjectId);
        /*SubjectIdSDVFactory tableFactory = new SubjectIdSDVFactory();
        * @RequestParam("studySubjectId") int studySubjectId,*/
        request.setAttribute("imagePathPrefix", "../");

        ArrayList<String> pageMessages = (ArrayList<String>) request.getAttribute("pageMessages");
        if (pageMessages == null) {
            pageMessages = new ArrayList<String>();
        }

        request.setAttribute("pageMessages", pageMessages);

        List<JobExecutionContext> listCurrentJobs = new ArrayList<JobExecutionContext>();
        listCurrentJobs = scheduler.getCurrentlyExecutingJobs();
        Iterator<JobExecutionContext> itCurrentJobs = listCurrentJobs.iterator();
        List<String> currentJobList = new ArrayList<String>();
        while(itCurrentJobs.hasNext()){
            JobExecutionContext temp = itCurrentJobs.next();
            currentJobList.add(temp.getTrigger().getJobKey().getName()+temp.getTrigger().getKey().getGroup());
        }

        List<String> triggerGroupNames = scheduler.getTriggerGroupNames();
        String[] triggerGroups = triggerGroupNames.stream().toArray(String[]::new);

        List<SimpleTrigger> simpleTriggers = new ArrayList<SimpleTrigger>();
        int index1 =0;
        for (String triggerGroup : triggerGroups) {
            logger.debug("Group: " + triggerGroup + " contains the following triggers");
            Set<TriggerKey> triggerKeySet = scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(triggerGroup));

            triggerKeys = triggerKeySet.stream().toArray(TriggerKey[]::new);
            
            for (TriggerKey triggerKey : triggerKeys) {
               Trigger.TriggerState state = scheduler.getTriggerState(triggerKey);
               logger.debug("- " + triggerKey.getName());
               if (state != Trigger.TriggerState.PAUSED) {
               simpleTriggers.add(index1,(SimpleTrigger) scheduler.getTrigger(triggerKey));
                 index1++;
               }
            }
         }

       List <ScheduledJobs>jobsScheduled = new ArrayList<ScheduledJobs>();

        int index = 0;

        for (SimpleTrigger st : simpleTriggers) {
            boolean isExecuting = currentJobList.contains(st.getJobKey().getName() + st.getJobKey().getGroup());

            ScheduledJobs jobs = new ScheduledJobs();

            ExtractPropertyBean epBean = null;
            if (st.getJobDataMap() != null) {
                epBean = (ExtractPropertyBean) st.getJobDataMap().get(EP_BEAN);
            }


            if (epBean != null) {
                StringBuilder checkbox = new StringBuilder();
                checkbox.append("<input style='margin-right: 5px' type='checkbox'/>");

                StringBuilder actions = new StringBuilder("<table><tr><td>");
                if (isExecuting) {
                    actions.append("&nbsp;");
                } else {
                    String contextPath = request.getContextPath();
                    StringBuilder jsCodeString = new StringBuilder("this.form.method='GET'; this.form.action='").
                            append(contextPath).append("/pages/cancelScheduledJob").append("';").
                            append("this.form.theJobName.value='").append(st.getJobKey().getName() ).append("';").
                            append("this.form.theJobGroupName.value='").append(st.getJobKey().getGroup()).append("';").
                            append("this.form.theTriggerName.value='").append(st.getJobKey().getName()).append("';").
                            append("this.form.theTriggerGroupName.value='").append(st.getJobKey().getGroup()).append("';").
                            append("this.form.submit();");

                    actions.append("<td><input type=\"submit\" class=\"button\" value=\"Cancel Job\" ").
                            append("name=\"cancelJob\" onclick=\"").append(jsCodeString.toString()).append("\" />");

                }

                actions.append("</td></tr></table>");

                jobs.setCheckbox(checkbox.toString());
                jobs.setDatasetId(epBean.getDatasetName());
                String fireTime = st.getStartTime() != null ? longFormat(locale).format(st.getStartTime()) : "";
                jobs.setFireTime(fireTime);
                if(st.getNextFireTime() != null) {
                    jobs.setScheduledFireTime(longFormat(locale).format(st.getNextFireTime()));
                }
                jobs.setExportFileName(epBean.getExportFileName()[0]);
                jobs.setAction(actions.toString());
                jobs.setJobStatus(isExecuting ? "Currently Executing" : "Scheduled");
                jobsScheduled.add(index, jobs);
                index++;
            }
        }
        logger.debug("totalRows"+index);

        request.setAttribute("totalJobs", index);

        request.setAttribute("jobs", jobsScheduled);

        TableFacade facade = scheduledJobTableFactory.createTable(request, response);
        String sdvMatrix = facade.render();
        gridMap.addAttribute(SCHEDULED_TABLE_ATTRIBUTE, sdvMatrix);
        return gridMap;

    }

    @RequestMapping("/cancelScheduledJob")
    public String cancelScheduledJob(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("theJobName")   String theJobName,@RequestParam("theJobGroupName")   String theJobGroupName,
            @RequestParam("theTriggerName")   String triggerName,@RequestParam("theTriggerGroupName")   String triggerGroupName,
            @RequestParam("redirection") String redirection, ModelMap model) throws SchedulerException
        {

        scheduler.getJobDetail(JobKey.jobKey(theJobName, theJobGroupName));
        logger.debug("About to pause the job-->"+theJobName+"Job Group Name -->"+theJobGroupName);

        SimpleTrigger oldTrigger = (SimpleTrigger) scheduler.getTrigger(TriggerKey.triggerKey(triggerName, triggerGroupName));
        if(oldTrigger!=null)
        {
        Date startTime = new Date(oldTrigger.getStartTime().getTime()+oldTrigger.getRepeatInterval());
        if(triggerGroupName.equals(ExtractController.TRIGGER_GROUP_NAME))
        {
            interruptQuartzJob(scheduler, theJobName, theJobGroupName);
        }

        scheduler.pauseJob(JobKey.jobKey(theJobName, theJobGroupName));
        
        SimpleTrigger newTrigger = (SimpleTrigger) newTrigger()
                .withIdentity(triggerName, triggerGroupName)
                .forJob(theJobName, theJobGroupName)
                .startAt(startTime)
                .withSchedule(simpleSchedule()
                    .withRepeatCount(oldTrigger.getRepeatCount())
                    .withIntervalInMilliseconds(oldTrigger.getRepeatInterval())
                    .withMisfireHandlingInstructionNextWithRemainingCount())
                .withDescription(oldTrigger.getDescription())
                .usingJobData(oldTrigger.getJobDataMap())
                .build();


        scheduler.unscheduleJob(TriggerKey.triggerKey(triggerName,triggerGroupName));// these are the jobs which are from extract data and are not not required to be rescheduled.

        ArrayList<String> pageMessages = new ArrayList<String>();

        if(triggerGroupName.equals(ExtractController.TRIGGER_GROUP_NAME))
        {
            scheduler.rescheduleJob(TriggerKey.triggerKey(triggerName,triggerGroupName), newTrigger);

            pageMessages.add("The Job  "+theJobName+" has been cancelled");
        }
        else if(triggerGroupName.equals(XsltTriggerService.TRIGGER_GROUP_NAME))
        {

            JobDetailFactoryBean JobDetailFactoryBean = new JobDetailFactoryBean();
            JobDetailFactoryBean.setGroup(XsltTriggerService.TRIGGER_GROUP_NAME);
            JobDetailFactoryBean.setName(newTrigger.getKey().getName());
            JobDetailFactoryBean.setJobClass(org.akaza.openclinica.job.XsltStatefulJob.class);
            JobDetailFactoryBean.setJobDataMap(newTrigger.getJobDataMap());
            JobDetailFactoryBean.setDurability(true); // need durability?
            JobDetailFactoryBean.afterPropertiesSet();


           scheduler.deleteJob(JobKey.jobKey(theJobName, theJobGroupName));
           scheduler.scheduleJob(JobDetailFactoryBean.getObject(), newTrigger);
           pageMessages.add("The Job "+theJobName+"  has been rescheduled");
        }

        request.setAttribute("pageMessages", pageMessages);

        logger.debug("jobDetails>"+ scheduler.getJobDetail(JobKey.jobKey(theJobName, theJobGroupName)));
        }
        sdvUtil.forwardRequestFromController(request, response, "/pages/" + redirection);
        return null;
    }

    private void interruptQuartzJob(Scheduler scheduler, String jobName, String jobGroup) throws SchedulerException {
        scheduler.interrupt(JobKey.jobKey(jobName, jobGroup));
    }

    private String longFormatString() {
        return "EEE MMM dd HH:mm:ss zzz yyyy";
    }

    private SimpleDateFormat longFormat(Locale locale) {
        return new SimpleDateFormat(longFormatString(), locale);
    }

}
