/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.web.job;

import org.quartz.StatefulJob;

/**
 * Import Stateful Job, by Tom Hickerson 04/2009
 * Establishing stateful-ness on the Java side to avoid locking
 */
public class ImportStatefulJob
    extends ImportSpringJob
    implements StatefulJob {

    public ImportStatefulJob() {
        super();
    }
}
