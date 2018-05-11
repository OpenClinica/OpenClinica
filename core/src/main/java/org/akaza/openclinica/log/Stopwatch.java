/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class used to measure the execution time between the invocations of the {@link #start()} and
 * {@link #stop()} methods, creating a debug log entry for both of them.
 *
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 */
public class Stopwatch {

    private static final Logger LOG = LoggerFactory.getLogger(Stopwatch.class);

    private static final String START_LABEL = "START: %s";
    private static final String STOP_LABEL  = "STOP:  %s - %dm %.2fs";

    private final String name;

    private final org.apache.commons.lang.time.StopWatch stopwatch = new org.apache.commons.lang.time.StopWatch();

    public static Stopwatch createAndStart(String name) {
        Stopwatch result = new Stopwatch(name);
        result.start();
        return result;
    }

    public Stopwatch(String name) {
        this.name = name;
    }

    public void start() {
        if (LOG.isDebugEnabled()) {
            stopwatch.start();
            LOG.debug(String.format(START_LABEL, name));
        }
    }

    public void stop() {
        if (LOG.isDebugEnabled()) {
            stopwatch.stop();
            long totalTime = stopwatch.getTime();
            LOG.debug(String.format(STOP_LABEL, name, totalTime / 60000,  (float) (totalTime % 60000) / 1000));
        }
    }

}
