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

    private boolean running = true;

    public void check() {
        if (!running) {
            running = true;
            throw new JobInterruptedException();
        }
    }

    public void terminate() {
        running = false;
    }

}
