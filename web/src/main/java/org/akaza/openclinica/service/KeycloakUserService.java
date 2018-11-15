package org.akaza.openclinica.service;

public interface KeycloakUserService {
    boolean authenticateKeycloakUser(String username, String password);
}
