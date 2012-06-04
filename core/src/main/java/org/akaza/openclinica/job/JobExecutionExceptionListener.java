/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public class JobExecutionExceptionListener extends JobListenerSupport {

    private static final Logger LOG = LoggerFactory.getLogger(JobExecutionExceptionListener.class);

    /* (non-Javadoc)
     * @see org.quartz.JobListener#getName()
     */
    @Override
    public String getName() {
        return "JobExecutionExceptionListener";
    }

    /* (non-Javadoc)
     * @see org.quartz.listeners.JobListenerSupport#jobWasExecuted(org.quartz.JobExecutionContext, org.quartz.JobExecutionException)
     */
    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        super.jobWasExecuted(context, jobException);
        if (jobException != null) {
            LOG.warn("Error executing Quartz job", jobException);
        }
    }

}
