package core.org.akaza.openclinica.web.filter;

import org.akaza.openclinica.view.Page;
import org.keycloak.adapters.OIDCAuthenticationError;
import org.keycloak.adapters.spi.AuthenticationError;
import org.keycloak.adapters.springsecurity.KeycloakAuthenticationException;
import org.keycloak.adapters.springsecurity.authentication.RequestAuthenticatorFactory;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class OpenClinicaAuthenticationProcessingFilter extends KeycloakAuthenticationProcessingFilter {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private RequestAuthenticatorFactory requestAuthenticatorFactory = new OpenClinicaSpringSecurityRequestAuthenticatorFactory();

    public OpenClinicaAuthenticationProcessingFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager, DEFAULT_REQUEST_MATCHER);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        try {
            setRequestAuthenticatorFactory(requestAuthenticatorFactory);
            return super.attemptAuthentication(request, response);
        } catch (KeycloakAuthenticationException e) {
            if (e.getMessage().equals("Invalid authorization header, see WWW-Authenticate header for details")) {
                OIDCAuthenticationError error = (OIDCAuthenticationError) request.getAttribute(AuthenticationError.class.getName());
                OIDCAuthenticationError.Reason expectedError = OIDCAuthenticationError.Reason.INVALID_STATE_COOKIE;
                if (error.getReason() == expectedError) {
                    logger.error("State cookie is not current cookie, user bookmarked old log in page");
                    response.sendRedirect(request.getContextPath() + Page.REDIRECT_LOGIN_SERVLET.getFileName());
                }
                return null;
            }
            throw e;
        }
    }
}
