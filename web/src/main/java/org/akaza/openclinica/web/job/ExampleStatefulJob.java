/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
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
