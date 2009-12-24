/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.exception;

/**
 * @author Krikor Krumlian
 */
@SuppressWarnings("serial")
public class OpenClinicaSystemException extends RuntimeException {
    private String errorCode;

    public OpenClinicaSystemException(String code, String message) {
        this(message);
        this.errorCode = code;
    }

    public OpenClinicaSystemException(String code, String message, Throwable cause) {
        this(message, cause);
        this.errorCode = code;
    }

    public OpenClinicaSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public OpenClinicaSystemException(Throwable cause) {
        super(cause);
    }

    public OpenClinicaSystemException(String message) {
        super(message);
    }

    public String getErrorCode() {
        return errorCode;
    }
}