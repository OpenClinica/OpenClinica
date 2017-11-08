package org.akaza.openclinica.service;

public interface Auth0UserService {
    boolean authenticateAuth0User(String username, String password);
}
