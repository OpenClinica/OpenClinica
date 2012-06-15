/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.job;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public class JobTerminationMonitor implements Serializable {

    private static final long serialVersionUID = 361394087982395855L;

    private static final Logger LOG = LoggerFactory.getLogger(JobTerminationMonitor.class);

    private static ThreadLocal<JobTerminationMonitor> instance = new ThreadLocal<JobTerminationMonitor>() {
        @Override
        protected JobTerminationMonitor initialValue() {
            return new JobTerminationMonitor();
        }
    };

    private final String jobName;

    private JobTerminationMonitor() {
        this("<untitled>");
    }

    private JobTerminationMonitor(String jobName) {
        // Prevent instantiation by other classes
        this.jobName = jobName;
    }

    public static JobTerminationMonitor createInstance(String jobName) {
        instance.remove();
        instance.set(new JobTerminationMonitor(jobName));
        return instance.get();
    }

    private boolean running = true;

    public static void check() {
        JobTerminationMonitor monitor = instance.get();
        if (!monitor.running) {
            LOG.info("Raising termination exception for job " + monitor.jobName);
            throw new JobInterruptedException();
        }
    }

    public void terminate() {
        running = false;
    }

    public String getJobName() {
        return jobName;
    }

}
