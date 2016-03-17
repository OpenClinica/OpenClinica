package org.akaza.openclinica.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

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
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.JobDetailBean;
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
        String[] triggerNames;

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
            currentJobList.add(temp.getTrigger().getJobName()+temp.getTrigger().getGroup());
        }

        String[] triggerGroups =  scheduler.getTriggerGroupNames();
        List<SimpleTrigger> simpleTriggers = new ArrayList<SimpleTrigger>();
        int index1 =0;
        for (String triggerGroup : triggerGroups) {
            logger.debug("Group: " + triggerGroup + " contains the following triggers");
              triggerNames = scheduler.getTriggerNames(triggerGroup) ;
            
            for (String triggerName : triggerNames) {
             int state = scheduler.getTriggerState(triggerName, triggerGroup);
               logger.debug("- " + triggerName);
               if (state != Trigger.STATE_PAUSED) {
               simpleTriggers.add(index1,(SimpleTrigger) scheduler.getTrigger(triggerName, triggerGroup));
                 index1++;
               }
            }
         }

       List <ScheduledJobs>jobsScheduled = new ArrayList<ScheduledJobs>();

        int index = 0;

        for (SimpleTrigger st : simpleTriggers) {
            boolean isExecuting = currentJobList.contains(st.getJobName() + st.getGroup());

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
                            append("this.form.theJobName.value='").append(st.getJobName()).append("';").
                            append("this.form.theJobGroupName.value='").append(st.getJobGroup()).append("';").
                            append("this.form.theTriggerName.value='").append(st.getName()).append("';").
                            append("this.form.theTriggerGroupName.value='").append(st.getGroup()).append("';").
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

        scheduler.getJobDetail(theJobName, theJobGroupName);
        logger.debug("About to pause the job-->"+theJobName+"Job Group Name -->"+theJobGroupName);

        SimpleTrigger oldTrigger = (SimpleTrigger) scheduler.getTrigger(triggerName, triggerGroupName);
        if(oldTrigger!=null)
        {
        Date startTime = new Date(oldTrigger.getStartTime().getTime()+oldTrigger.getRepeatInterval());
        if(triggerGroupName.equals(ExtractController.TRIGGER_GROUP_NAME))
        {
            interruptQuartzJob(scheduler, theJobName, theJobGroupName);
        }

        scheduler.pauseJob(theJobName, theJobGroupName);

        SimpleTrigger newTrigger = new SimpleTrigger(triggerName,triggerGroupName);
        newTrigger.setJobName(theJobName);
        newTrigger.setJobGroup(theJobGroupName);
        newTrigger.setJobDataMap(oldTrigger.getJobDataMap());
        newTrigger.setVolatility(false);
        newTrigger.setRepeatCount(oldTrigger.getRepeatCount());
        newTrigger.setRepeatInterval(oldTrigger.getRepeatInterval());
        newTrigger.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT);
        newTrigger.setStartTime(startTime);
        newTrigger.setRepeatInterval(oldTrigger.getRepeatInterval());

        scheduler.unscheduleJob(triggerName,triggerGroupName);// these are the jobs which are from extract data and are not not required to be rescheduled.

        ArrayList<String> pageMessages = new ArrayList<String>();

        if(triggerGroupName.equals(ExtractController.TRIGGER_GROUP_NAME))
        {
            scheduler.rescheduleJob(triggerName, triggerGroupName, newTrigger);

            pageMessages.add("The Job  "+theJobName+" has been cancelled");
        }
        else if(triggerGroupName.equals(XsltTriggerService.TRIGGER_GROUP_NAME))
        {

            JobDetailBean jobDetailBean = new JobDetailBean();
            jobDetailBean.setGroup(XsltTriggerService.TRIGGER_GROUP_NAME);
            jobDetailBean.setName(newTrigger.getName());
            jobDetailBean.setJobClass(org.akaza.openclinica.job.XsltStatefulJob.class);
            jobDetailBean.setJobDataMap(newTrigger.getJobDataMap());
            jobDetailBean.setDurability(true); // need durability?
            jobDetailBean.setVolatility(false);

           scheduler.deleteJob(theJobName, theJobGroupName);
           scheduler.scheduleJob(jobDetailBean, newTrigger);
           pageMessages.add("The Job "+theJobName+"  has been rescheduled");
        }

        request.setAttribute("pageMessages", pageMessages);

        logger.debug("jobDetails>"+ scheduler.getJobDetail(theJobName, theJobGroupName));
        }
        sdvUtil.forwardRequestFromController(request, response, "/pages/" + redirection);
        return null;
    }

    private void interruptQuartzJob(Scheduler scheduler, String jobName, String jobGroup) throws SchedulerException {
        scheduler.interrupt(jobName, jobGroup);
    }

    private String longFormatString() {
        return "EEE MMM dd HH:mm:ss zzz yyyy";
    }

    private SimpleDateFormat longFormat(Locale locale) {
        return new SimpleDateFormat(longFormatString(), locale);
    }

}
