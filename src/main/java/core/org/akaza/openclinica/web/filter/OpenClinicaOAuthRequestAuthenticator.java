package core.org.akaza.openclinica.web.filter;

import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.OAuthRequestAuthenticator;
import org.keycloak.adapters.OIDCAuthenticationError;
import org.keycloak.adapters.RequestAuthenticator;
import org.keycloak.adapters.spi.AdapterSessionStore;
import org.keycloak.adapters.spi.AuthChallenge;
import org.keycloak.adapters.spi.HttpFacade;

public class OpenClinicaOAuthRequestAuthenticator extends OAuthRequestAuthenticator {

    public OpenClinicaOAuthRequestAuthenticator(RequestAuthenticator requestAuthenticator, HttpFacade facade, KeycloakDeployment deployment, int sslRedirectPort, AdapterSessionStore tokenStore) {
        super(requestAuthenticator, facade, deployment, sslRedirectPort, tokenStore);
    }

    @Override
    protected AuthChallenge challenge(final int code, final OIDCAuthenticationError.Reason reason, final String description) {
        return new AuthChallenge() {
            @Override
            public int getResponseCode() {
                return code;
            }

            @Override
            public boolean challenge(HttpFacade exchange) {
                OIDCAuthenticationError error = new OIDCAuthenticationError(reason, description);
                exchange.getRequest().setError(error);
                return true;
            }
        };
    }
}
