package org.akaza.openclinica.web.rest.client.auth.impl;

/**
 * Class representing errors returned by Keycloak.
 * @author svadla@openclinica.com
 */
public class KeycloakError {
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
