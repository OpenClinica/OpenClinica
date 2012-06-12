package org.akaza.openclinica.job;

import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.StatefulJob;
import org.quartz.UnableToInterruptJobException;

public class XsltStatefulJob extends XsltTransformJob
    implements StatefulJob, InterruptableJob {

    protected JobTerminationMonitor jobTerminationMonitor;

    public XsltStatefulJob() {
        super();
    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        jobTerminationMonitor = JobTerminationMonitor.createInstance();
        super.executeInternal(context);
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        if (jobTerminationMonitor != null) {
            jobTerminationMonitor.terminate();
        }
    }
}
