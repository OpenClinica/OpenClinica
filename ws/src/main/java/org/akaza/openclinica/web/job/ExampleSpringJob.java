package org.akaza.openclinica.web.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

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
