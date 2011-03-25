package org.akaza.openclinica.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;

import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.web.table.scheduledjobs.ScheduledJobTableFactory;
import org.apache.commons.dbcp.BasicDataSource;
import org.jmesa.facade.TableFacade;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.quartz.impl.StdScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
/**
 * 
 * @author jnyayapathi
 *
 */
@Controller("ScheduledJobController")
public class ScheduledJobController {
    public final static String SCHEDULED_TABLE_ATTRIBUTE = "scheduledTableAttribute";
    @Autowired
    @Qualifier("dataSource")
    private BasicDataSource dataSource;
    private  String SCHEDULER = "schedulerFactoryBean";
    private StdScheduler scheduler;
    @Autowired
    @Qualifier("scheduledJobTableFactory")
    private  ScheduledJobTableFactory  scheduledJobTableFactory;
    
    
    public ScheduledJobController(){
        
    }
    
    @RequestMapping("/listCurrentScheduledJobs")
    public ModelMap listScheduledJobs(HttpServletRequest request, HttpServletResponse response) throws SchedulerException{

        ModelMap gridMap = new ModelMap();
        scheduler = getScheduler(request);
        List<JobExecutionContext> listCurrentJobs = new ArrayList<JobExecutionContext>();
     // enumerate each job group
        for(String group: scheduler.getJobGroupNames()) {
            // enumerate each job in group
             listCurrentJobs = scheduler.getCurrentlyExecutingJobs();
         /*   
            for(StdScheduler currentJob:listCurrentJobs)
            {
                currentJob.getJobNames(group);
            }
         
                */
       /*  for(String jobName:scheduler.getJobNames(group)){
             
             System.out.println("Found job identified by: " + jobName);
            
         }*/
        }
        for(JobExecutionContext jec:listCurrentJobs)
        {
            jec.getTrigger().getTriggerListenerNames();
            jec.getTrigger().getFullJobName();
            jec.getTrigger().getJobDataMap().getInt("dsID");
        }
        if(listCurrentJobs!=null)
        {
            request.setAttribute("totalJobs", listCurrentJobs.size());
           
           
        }
        else 
        {
            request.setAttribute("totalJobs", new Integer(0));
        }
        request.setAttribute("jobs", listCurrentJobs);
        
        TableFacade facade = scheduledJobTableFactory.createTable(request, response);
        String sdvMatrix = facade.render();
        gridMap.addAttribute(SCHEDULED_TABLE_ATTRIBUTE, sdvMatrix);
        return gridMap;
        
    }
    private StdScheduler getScheduler(HttpServletRequest request) {
        scheduler = this.scheduler != null ? scheduler : (StdScheduler) SpringServletAccess.getApplicationContext(request.getSession().getServletContext()).getBean(SCHEDULER);
        return scheduler;
    }
}
