/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package core.org.akaza.openclinica.exception;

/**
 * @author thickerson
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class DAOCleanupFailureException extends Exception {
    public String message;

    public DAOCleanupFailureException() {
        message = "";
    }

    public DAOCleanupFailureException(String message) {
        this.message = message;
    }
}
