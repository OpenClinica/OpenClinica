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
 * This exception should be used when the user attempts to perform a use case,
 * but he is not authorized to do so. The user should be sent to an error page
 * and an error message should be displayed.
 *
 * Typically the error page is Page.MAIN_MENU.
 *
 * @author ssachs
 */
public class InsufficientPermissionException extends OpenClinicaException {
    private Page goTo; // this is the page the user should be forwarded to

    public InsufficientPermissionException(Page goTo, String message, String type, String methodName, String className, String errorid) {
        super(message, type, methodName, className, errorid);
        this.goTo = goTo;
    }

    public InsufficientPermissionException(Page goTo, String message, String errorid) {
        super(message, errorid);
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
