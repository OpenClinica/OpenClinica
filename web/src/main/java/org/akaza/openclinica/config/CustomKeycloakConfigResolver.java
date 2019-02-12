package org.akaza.openclinica.config;

import org.apache.http.client.HttpClient;
import org.keycloak.adapters.HttpClientBuilder;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static org.akaza.openclinica.web.rest.client.auth.impl.KeycloakConfig.CONNECTION_TTL;

/**
 * KeycloakConfigResolver that still reads the configuration from keycloak.json in the classpath but allows you to customize the
 * http client to set the connection TTL.
 * @author svadla@openclinica.com
 */
public class CustomKeycloakConfigResolver implements KeycloakConfigResolver {

    private static final Logger logger = LoggerFactory.getLogger(CustomKeycloakConfigResolver.class);

    private final KeycloakDeployment keycloakDeployment;
    public CustomKeycloakConfigResolver() {
        keycloakDeployment = resolveDeployment();
    }

    @Override
    public KeycloakDeployment resolve(HttpFacade.Request facade) {
        return keycloakDeployment != null ? keycloakDeployment : resolveDeployment();
    }

    private KeycloakDeployment resolveDeployment() {
        try {
            logger.info("Initializing keycloak deployment from keycloak.json");
            InputStream inputStream = new ClassPathResource("keycloak.json", this.getClass().getClassLoader()).getInputStream();
            AdapterConfig adapterConfig = KeycloakDeploymentBuilder.loadAdapterConfig(inputStream);
            KeycloakDeployment deployment = KeycloakDeploymentBuilder.build(adapterConfig);
            // Customizing the http client to set time-to-live value on http connections such that they are reinitialized
            // before they get reset by AWS NAT gateway.
            // Note: Time-to-live value has to be lower than the NAT gateway timeout (5 min)
            HttpClient httpClient = new HttpClientBuilder()
                .connectionTTL(CONNECTION_TTL, TimeUnit.MILLISECONDS)
                .build(adapterConfig);
            deployment.setClient(httpClient);
            return deployment;
        } catch (IOException e) {
            logger.error("Error reading keycloak.json", e);
        }
        return null;
    }
}
