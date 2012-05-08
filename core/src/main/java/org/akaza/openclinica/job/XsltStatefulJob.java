package org.akaza.openclinica.job;

import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.quartz.UnableToInterruptJobException;

public class XsltStatefulJob extends XsltTransformJob
    implements StatefulJob, InterruptableJob {

    private Thread thread;

    public XsltStatefulJob() {
        super();
    }

    @Override
    protected void executeInternal(JobExecutionContext context)
            throws JobExecutionException {
        thread = Thread.currentThread();
        super.executeInternal(context);
    }

    public void interrupt() throws UnableToInterruptJobException {
        if (thread != null) {
            thread.interrupt();
        }
    }

}
