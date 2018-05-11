package org.akaza.openclinica.web.job;

import org.quartz.StatefulJob;

/**
 * establishing stateful-ness on the Java side to avoid locking, etc
 */
public class ExampleStatefulJob
    extends ExampleSpringJob
    implements StatefulJob {

    public ExampleStatefulJob() {
        super();
    }
}
