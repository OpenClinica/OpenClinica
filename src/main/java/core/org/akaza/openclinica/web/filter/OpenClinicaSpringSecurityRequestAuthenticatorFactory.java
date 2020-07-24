package core.org.akaza.openclinica.web.filter;

import org.keycloak.adapters.AdapterTokenStore;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.RequestAuthenticator;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.adapters.springsecurity.authentication.SpringSecurityRequestAuthenticatorFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * Extends SpringSecurityRequestAuthenticatorFactory to use OpenClinicaSpringSecurityRequestAuthenticator
 * to use custom OpenClinicaOAuthRequestAuthenticator
 *
 * @author Shu Lin Chan
 */
public class OpenClinicaSpringSecurityRequestAuthenticatorFactory extends SpringSecurityRequestAuthenticatorFactory {

    @Override
    public RequestAuthenticator createRequestAuthenticator(HttpFacade facade,
                                                           HttpServletRequest request, KeycloakDeployment deployment, AdapterTokenStore tokenStore, int sslRedirectPort) {
        return new OpenClinicaSpringSecurityRequestAuthenticator(facade, request, deployment, tokenStore, sslRedirectPort);
    }
}
