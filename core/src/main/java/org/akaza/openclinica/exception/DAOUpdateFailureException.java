/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2016-2018 Akaza Research
 */
package org.akaza.openclinica.exception;

public class DAOUpdateFailureException extends OpenClinicaException {

    public static String ERROR_ID = "5102";

    public DAOUpdateFailureException(String message, String type, String methodName, String className) {
        super(message, type, methodName, className, ERROR_ID);
    }

    public DAOUpdateFailureException(String message) {
        super(message, ERROR_ID);
    }
}
