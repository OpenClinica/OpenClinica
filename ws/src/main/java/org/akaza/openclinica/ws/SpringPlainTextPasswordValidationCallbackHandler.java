package org.akaza.openclinica.ws;

import com.auth0.Auth0;
import com.auth0.authentication.AuthenticationAPIClient;
import com.auth0.authentication.result.Credentials;
import com.sun.xml.wss.impl.callback.PasswordValidationCallback;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.ws.soap.security.callback.AbstractCallbackHandler;
import org.springframework.ws.soap.security.callback.CleanupCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.util.Properties;

/**
 * Callback handler that validates a user against Auth0. Logic based
 * on Spring Security's <code>BasicProcessingFilter</code>.
 * <p/>
 * This handler requires an auth0 active account to operate.
 * <p/>
 * This class only handles <code>PasswordValidationCallback</code>s that contain a
 * <code>PlainTextPasswordRequest</code>, and throws an <code>UnsupportedCallbackException</code> for others.
 *
 * @author Yogi Shridhare
 */

public class SpringPlainTextPasswordValidationCallbackHandler extends AbstractCallbackHandler implements InitializingBean {

    @Autowired ApplicationContext applicationContext;
    @Autowired Environment env;
    @Value("${auth0.domain}") private String auth0Domain;
    @Value("${auth0.clientId}") private String auth0ClientId;
    private AuthenticationManager authenticationManager;

    private boolean ignoreFailure = false;

    /**
     * Sets the Spring Security authentication manager. Required.
     */
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void setIgnoreFailure(boolean ignoreFailure) {
        this.ignoreFailure = ignoreFailure;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(authenticationManager, "authenticationManager is required");
    }

    /**
     * Handles <code>PasswordValidationCallback</code>s that contain a <code>PlainTextPasswordRequest</code>, and throws
     * an <code>UnsupportedCallbackException</code> for others.
     *
     * @throws javax.security.auth.callback.UnsupportedCallbackException when the callback is not supported
     */
    @Override protected void handleInternal(Callback callback) throws IOException, UnsupportedCallbackException {
        if (callback instanceof PasswordValidationCallback) {
            PasswordValidationCallback validationCallback = (PasswordValidationCallback) callback;
            if (validationCallback.getRequest() instanceof PasswordValidationCallback.PlainTextPasswordRequest) {
                validationCallback.setValidator(new SpringSecurityPlainTextPasswordValidator());
                return;
            }
        } else if (callback instanceof CleanupCallback) {
            SecurityContextHolder.clearContext();
            return;
        }
        throw new UnsupportedCallbackException(callback);
    }

    private class SpringSecurityPlainTextPasswordValidator implements PasswordValidationCallback.PasswordValidator {

        public boolean validate(PasswordValidationCallback.Request request) throws PasswordValidationCallback.PasswordValidationException {
            boolean accessGranted = false;
            PasswordValidationCallback.PlainTextPasswordRequest plainTextRequest = (PasswordValidationCallback.PlainTextPasswordRequest) request;

            try {
                WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
                Properties auth0Properties = (Properties) context.getBean("auth0Properties");

                Auth0 auth = new Auth0(auth0Properties.getProperty("auth0.clientId"), auth0Properties.getProperty("auth0.domain"));
                AuthenticationAPIClient client = auth.newAuthenticationAPIClient();
                Credentials credentials = client.login(plainTextRequest.getUsername(), plainTextRequest.getPassword())
                        .setConnection(auth0Properties.getProperty("auth0.connection")).execute();
                if (StringUtils.isNotEmpty(credentials.getAccessToken())) {
                    Authentication authentication = new UsernamePasswordAuthenticationToken(plainTextRequest.getUsername(), null,
                            AuthorityUtils.createAuthorityList("ROLE_USER"));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    accessGranted = true;
                }
                return accessGranted;

            } catch (Exception failed) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Authentication request for user '" + plainTextRequest.getUsername() + "' failed: " + failed.toString());
                }
                SecurityContextHolder.clearContext();
                return ignoreFailure;
            }
        }
    }

}