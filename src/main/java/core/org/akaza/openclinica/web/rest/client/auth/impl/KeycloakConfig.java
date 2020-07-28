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

    private static final int CONNECTION_POOL_SIZE = 10;
    public static final long CONNECTION_TTL = 60000;
    private static final long MIN_TOKEN_VALIDITY = 2000;

    @Bean
    public Keycloak Keycloak() {
        MasterKeycloakConfig masterKeycloakConfig = CoreResources.getMasterKeyCloakConfig();
        ResteasyClient resteasyClient = new ResteasyClientBuilder()
                .connectionPoolSize(CONNECTION_POOL_SIZE)
                .connectionTTL(CONNECTION_TTL, TimeUnit.MILLISECONDS)
                .build();
        Keycloak keycloak = KeycloakBuilder.builder()
            .serverUrl(masterKeycloakConfig.getBaseUrl())
            .clientId(masterKeycloakConfig.getClientId())
            .clientSecret(masterKeycloakConfig.getClientSecret())
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .realm(masterKeycloakConfig.getRealm())
            .resteasyClient(resteasyClient)
            .build();
        keycloak
            .tokenManager()
            .setMinTokenValidity(MIN_TOKEN_VALIDITY);
        return keycloak;
    }

}
