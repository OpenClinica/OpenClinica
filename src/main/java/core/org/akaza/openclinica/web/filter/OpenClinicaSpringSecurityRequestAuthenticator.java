package core.org.akaza.openclinica.web.filter;

import org.keycloak.adapters.AdapterTokenStore;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.adapters.springsecurity.authentication.SpringSecurityRequestAuthenticator;

import javax.servlet.http.HttpServletRequest;

public class OpenClinicaSpringSecurityRequestAuthenticator extends SpringSecurityRequestAuthenticator {

    public OpenClinicaSpringSecurityRequestAuthenticator(HttpFacade facade,
                                                         HttpServletRequest request,
                                                         KeycloakDeployment deployment,
                                                         AdapterTokenStore tokenStore,
                                                         int sslRedirectPort) {
        super(facade, request, deployment, tokenStore, sslRedirectPort);

    }

    @Override
    protected OpenClinicaOAuthRequestAuthenticator createOAuthAuthenticator() {
        return new OpenClinicaOAuthRequestAuthenticator(this, facade, deployment, sslRedirectPort, tokenStore);
    }
}
