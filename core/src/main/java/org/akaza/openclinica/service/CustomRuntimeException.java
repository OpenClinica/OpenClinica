package org.akaza.openclinica.service;

import org.springframework.validation.Errors;

public class CustomRuntimeException extends RuntimeException {
    private Errors errors;

    public CustomRuntimeException(String message, Errors errors) {
        super(message);
        this.errors = errors;
    }

    public Errors getErrors() {
        return errors;
    }

    public void setErrors(Errors errors) {
        this.errors = errors;
    }

}
