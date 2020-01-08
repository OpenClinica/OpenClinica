/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.exception;

/**
 * @author sshamim
 *
 */
public class CRFReadingException extends Exception {
    public String message;

    public CRFReadingException() {
        message = "";
    }

    public CRFReadingException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
