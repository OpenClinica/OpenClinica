/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.web;

import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.view.Page;

/**
 * This exception should be used when the system is in an inconsistent state,
 * and the user cannot proceed with his desired use case.
 *
 * Typically this happens when the user must choose some entity in a form, but
 * no entities are available to be selected.
 *
 * For example, the user may want to add a new subject to a genetic study.
 * However, in this use case, a father and mother must be specified for the
 * subject. If there are no female subjects in the system, then it will not be
 * possible to specify a mother for the subject. Consequently, the user may not
 * proceed.
 *
 * @author ssachs
 */
public class InconsistentStateException extends OpenClinicaException {
    private Page goTo; // this is the page the user should be forwarded to

    public InconsistentStateException(Page goTo, String message, String type, String methodName, String className) {
        super(message, type, methodName, className, "inconsistentState");
        this.goTo = goTo;
    }

    public InconsistentStateException(Page goTo, String message) {
        super(message, "inconsistentState");
        this.goTo = goTo;
    }

    /**
     * @return Returns the goTo.
     */
    public Page getGoTo() {
        return goTo;
    }

    /**
     * @param goTo
     *            The goTo to set.
     */
    public void setGoTo(Page goTo) {
        this.goTo = goTo;
    }
}