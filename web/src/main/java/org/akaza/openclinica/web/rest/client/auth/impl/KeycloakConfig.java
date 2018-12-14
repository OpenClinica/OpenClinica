package org.akaza.openclinica.web.rest.client.auth.impl;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration class that initializes beans used for interacting with Keycloak server.
 * @author svadla@openclinica.com
 */

@Configuration
@PropertySource("classpath:auth.properties")
public class KeycloakConfig {
    @Value("${auth.base-url}") private String authBaseUrl;
    @Value("${auth.realm}") private String authRealm;
    @Value("${auth.client-id}") private String authClientId;
    @Value("${auth.client-secret}") private String authClientSecret;



    @Bean
    public Keycloak Keycloak() {
        Keycloak keycloak = KeycloakBuilder.builder()
            .serverUrl(authBaseUrl)
            .clientId(authClientId)
            .clientSecret(authClientSecret)
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .realm(authRealm)
            .build();
        return keycloak;
    }

}
