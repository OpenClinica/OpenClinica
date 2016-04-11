/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2016-2018 Akaza Research
 */
package org.akaza.openclinica.exception;

public class DAOInsertFailureException extends OpenClinicaException {

    public static String ERROR_ID = "5101";

    public DAOInsertFailureException(String message, String type, String methodName, String className) {
        super(message, type, methodName, className, ERROR_ID);
    }

    public DAOInsertFailureException(String message) {
        super(message, ERROR_ID);
    }
}
