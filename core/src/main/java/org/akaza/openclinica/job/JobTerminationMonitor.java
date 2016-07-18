/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * This class is used to introduce checkpoints in the extract job execution in which a job cancellation request
 * performed by the user can be processed.
 * Upon a cancellation request this monitor will throw a {@link JobInterruptedException} to interrupt the normal
 * job execution flow.
 *
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public class JobTerminationMonitor implements Serializable {

    private static final long serialVersionUID = 361394087982395855L;

    private static final Logger LOG = LoggerFactory.getLogger(JobTerminationMonitor.class);

    /**
     * A ThreadLocal ensures that each thread will have its own monitor instance
     */
    private static ThreadLocal<JobTerminationMonitor> instance = new ThreadLocal<JobTerminationMonitor>() {
        @Override
        protected JobTerminationMonitor initialValue() {
            return new JobTerminationMonitor();
        }
    };

    private String jobName = "<untitled>";

    private JobTerminationMonitor() {
        // Nothing to do
    }

    private JobTerminationMonitor(String jobName) {
        this.jobName = jobName;
    }

    public static JobTerminationMonitor createInstance(String jobName) {
        instance.remove();
        instance.set(new JobTerminationMonitor(jobName));
        return instance.get();
    }

    private boolean running = true;

    /**
     * Verifies if the termination of a job was requested, in which case this method throws a
     * {@link JobInterruptedException}.
     *
     * @see #terminate()
     */
    public static void check() {
        JobTerminationMonitor monitor = instance.get();
        if (!monitor.running) {
            LOG.info("Raising termination exception for job " + monitor.jobName);
            throw new JobInterruptedException();
        }
    }

    /**
     * Request the monitor to throw an interruption exception in the next checkpoint
     */
    public void terminate() {
        running = false;
    }

    public String getJobName() {
        return jobName;
    }

}