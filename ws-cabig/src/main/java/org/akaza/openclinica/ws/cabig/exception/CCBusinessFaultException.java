package org.akaza.openclinica.ws.cabig.exception;

import org.akaza.openclinica.exception.OpenClinicaException;

public class CCBusinessFaultException extends OpenClinicaException {
    
    public CCBusinessFaultException(String message) {
        super(message, "CCBusinessFault");
    }
    
    public CCBusinessFaultException(String message, String idcode) {
        super(message, idcode);
    }

}
