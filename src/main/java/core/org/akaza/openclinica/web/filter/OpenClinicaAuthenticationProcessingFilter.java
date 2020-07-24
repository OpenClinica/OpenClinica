package core.org.akaza.openclinica.web.filter;

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
        setRequestAuthenticatorFactory(requestAuthenticatorFactory);
        return super.attemptAuthentication(request, response);
    }
}
