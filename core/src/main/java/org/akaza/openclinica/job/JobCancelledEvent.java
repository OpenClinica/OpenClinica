/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.job;

import org.springframework.context.ApplicationEvent;

/**
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public class JobCancelledEvent extends ApplicationEvent {

    private static final long serialVersionUID = -1814684723783125377L;

    /**
     * @param source
     */
    public JobCancelledEvent(Object source) {
        super(source);
    }

}
