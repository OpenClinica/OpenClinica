package org.akaza.openclinica.ws.cabig.exception;

import org.akaza.openclinica.exception.OpenClinicaException;

public class CCDataValidationFaultException extends OpenClinicaException {
    
    public CCDataValidationFaultException(String message) {
        super(message, "CCDataValidationFault");
    }

}
