package org.akaza.openclinica.web.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.JobDetailBean;
import org.springframework.scheduling.quartz.QuartzJobBean;


import javax.sql.DataSource;

public class ExampleSpringJob extends QuartzJobBean {

    // example code here
    private String message;

    // example code here
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        
    }



}
