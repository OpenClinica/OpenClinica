/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.job;

import java.io.Serializable;

/**
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public class JobTerminationMonitor implements Serializable {

    private static final long serialVersionUID = 361394087982395855L;

    private static ThreadLocal<JobTerminationMonitor> instance = new ThreadLocal<JobTerminationMonitor>() {
        @Override
        protected JobTerminationMonitor initialValue() {
            return new JobTerminationMonitor();
        }
    };

    private JobTerminationMonitor() {
        // Prevent instantiation by other classes
    }

    public static JobTerminationMonitor createInstance() {
        instance.remove();
        return instance.get();
    }

    private boolean running = true;

    public static void check() {
        JobTerminationMonitor monitor = instance.get();
        if (!monitor.isRunning()) {
            throw new JobInterruptedException();
        }
    }

    public void terminate() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

}
