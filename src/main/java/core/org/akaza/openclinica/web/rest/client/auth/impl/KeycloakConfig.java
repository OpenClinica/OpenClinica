package core.org.akaza.openclinica.web.rest.client.auth.impl;

import core.org.akaza.openclinica.dao.core.CoreResources;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.OAuth2Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Configuration class that initializes beans used for interacting with Keycloak server.
 * @author svadla@openclinica.com
 */

@Configuration
public class KeycloakConfig {


    private String authBaseUrl;
    private String authRealm;
    private String authClientId;
    private String authClientSecret;

    private static final int CONNECTION_POOL_SIZE = 10;
    public static final long CONNECTION_TTL = 60000;
    private static final long MIN_TOKEN_VALIDITY = 2000;

    @Bean
    public Keycloak Keycloak() {

        Properties authProperties = CoreResources.loadProperties("datainfo.properties");
        authBaseUrl= authProperties.getProperty("auth.base-url");
        authRealm= authProperties.getProperty("auth.realm");
        authClientId= authProperties.getProperty("auth.client-id");
        authClientSecret= authProperties.getProperty("auth.client-secret");
        ResteasyClient resteasyClient = new ResteasyClientBuilder()
                .connectionPoolSize(CONNECTION_POOL_SIZE)
                .connectionTTL(CONNECTION_TTL, TimeUnit.MILLISECONDS)
                .build();
        Keycloak keycloak = KeycloakBuilder.builder()
            .serverUrl(authBaseUrl)
            .clientId(authClientId)
            .clientSecret(authClientSecret)
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .realm(authRealm)
            .resteasyClient(resteasyClient)
            .build();
        keycloak
            .tokenManager()
            .setMinTokenValidity(MIN_TOKEN_VALIDITY);
        return keycloak;
    }

}
