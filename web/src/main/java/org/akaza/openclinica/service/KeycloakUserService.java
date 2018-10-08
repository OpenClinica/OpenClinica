package org.akaza.openclinica.service;

public interface KeycloakUserService {
    boolean authenticateAuth0User(String username, String password);
}
