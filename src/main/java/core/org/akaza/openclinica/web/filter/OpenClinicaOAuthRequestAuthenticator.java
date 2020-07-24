package core.org.akaza.openclinica.web.filter;

import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.OAuthRequestAuthenticator;
import org.keycloak.adapters.OIDCAuthenticationError;
import org.keycloak.adapters.RequestAuthenticator;
import org.keycloak.adapters.spi.AdapterSessionStore;
import org.keycloak.adapters.spi.AuthChallenge;
import org.keycloak.adapters.spi.HttpFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Overwrites challenge method in OAuthRequestAuthtenticator to not sendError in challenge otherwise we can't redirect to login page.
 * Change checkStateCookie to pass with a StateCookieInvalid request attribute even when the state cookies do not match,
 * we get the request attribute later in secure controller to show a redirected login page.
 * 
 * @author Shu Lin Chan
 */
public class OpenClinicaOAuthRequestAuthenticator extends OAuthRequestAuthenticator {

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final String STATE_COOKIE_INVALID = "StateCookieInvalid";

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

    @Override
    protected AuthChallenge checkStateCookie() {
        AuthChallenge challenge = super.checkStateCookie();
        if (challenge != null) {
            String stateCookieValue = getCookieValue(deployment.getStateCookieName());
            String state = getQueryParamValue(OAuth2Constants.STATE);
            if (!state.equals(stateCookieValue)) {
                logger.info("State cookie is not current cookie, user bookmarked old login paged.");
                RequestContextHolder.getRequestAttributes().setAttribute(STATE_COOKIE_INVALID, true, RequestAttributes.SCOPE_SESSION);
                logger.info("Setting redirect attribute so user will be redirected to warning page upon log in.");
                return null;
            }
        }
        return challenge;

    }
}
