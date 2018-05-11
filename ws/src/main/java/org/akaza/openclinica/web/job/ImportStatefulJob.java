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
