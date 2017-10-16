/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.exception;

/**
 * @author thickerson
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class PreparedStatementFactoryException extends Exception {
    public String message;

    public PreparedStatementFactoryException() {
        message = "";
    }

    public PreparedStatementFactoryException(String message) {
        this.message = message;
    }

}
