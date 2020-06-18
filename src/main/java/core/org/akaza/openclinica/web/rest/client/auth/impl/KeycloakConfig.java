package core.org.akaza.openclinica.web.rest.client.auth.impl;

import core.org.akaza.openclinica.dao.core.CoreResources;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.representations.idm.ClientRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.concurrent.TimeUnit;

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

    private static final int CONNECTION_POOL_SIZE = 10;
    public static final long CONNECTION_TTL = 60000;
    private static final long MIN_TOKEN_VALIDITY = 2000;
    private static final String OC_API_CLIENT_ID = "oc-api";

    @Bean
    public Keycloak keycloak() {
        Keycloak keycloak = KeycloakBuilder.builder()
            .serverUrl(authBaseUrl)
            .clientId(authClientId)
            .clientSecret(authClientSecret)
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .realm(authRealm)
            .resteasyClient(getCustomRestEasyClient())
            .build();
        keycloak
            .tokenManager()
            .setMinTokenValidity(MIN_TOKEN_VALIDITY);
        return keycloak;
    }

    @Bean
    public Keycloak keycloakRealmInstance(Keycloak keycloak) {

        String realm = CoreResources.getKeyCloakConfig().getRealm();
        String keycloakBaseUrl = CoreResources.getKeyCloakConfig().getAuthServerUrl();

        ClientsResource clientsResource = keycloak
                .realm(realm)
                .clients();
        ClientRepresentation ocApiClientRepresentation = clientsResource
                .findByClientId(OC_API_CLIENT_ID)
                .get(0);
        String ocApiClientSecret = clientsResource
                .get(ocApiClientRepresentation.getId())
                .getSecret()
                .getValue();

        // Get the keycloak instance specific to the given realm
        Keycloak keycloakRealmInstance = KeycloakBuilder.builder()
                .serverUrl(keycloakBaseUrl)
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(OC_API_CLIENT_ID)
                .clientSecret(ocApiClientSecret)
                .resteasyClient(getCustomRestEasyClient())
                .build();

        keycloakRealmInstance
                .tokenManager()
                .setMinTokenValidity(MIN_TOKEN_VALIDITY);

        return keycloakRealmInstance;
    }

    private ResteasyClient getCustomRestEasyClient() {
        ResteasyClient resteasyClient = new ResteasyClientBuilder()
                .connectionPoolSize(CONNECTION_POOL_SIZE)
                .connectionTTL(CONNECTION_TTL, TimeUnit.MILLISECONDS)
                .build();
        return resteasyClient;
    }

}
