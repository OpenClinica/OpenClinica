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
