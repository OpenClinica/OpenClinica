package org.akaza.openclinica.ws;

import com.auth0.Auth0;
import com.auth0.authentication.AuthenticationAPIClient;
import com.auth0.authentication.result.Credentials;
import com.sun.xml.wss.impl.callback.PasswordValidationCallback;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.UserType;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.hibernate.AuthoritiesDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.domain.user.AuthoritiesBean;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
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
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.util.JsonParser;
import org.springframework.security.oauth2.common.util.JsonParserFactory;
import org.springframework.util.Assert;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.ws.soap.security.callback.AbstractCallbackHandler;
import org.springframework.ws.soap.security.callback.CleanupCallback;
import sun.security.rsa.RSAPublicKeyImpl;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Locale;
import java.util.Map;
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

    private JsonParser objectMapper = JsonParserFactory.create();
    final String EXP = "exp";
    private static final String PUBLIC_KEY_LOCATION = "oc4.cer";
    private static final String X509_CERTFICATE = "X509";
    private DataSource dataSource;
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
                Locale locale = new Locale("en_US");
                ResourceBundleProvider.updateLocale(locale);
                WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
                dataSource = (DataSource) context.getBean("dataSource");
                Properties auth0Properties = (Properties) context.getBean("auth0Properties");

                Auth0 auth = new Auth0(auth0Properties.getProperty("auth0.clientId"), auth0Properties.getProperty("auth0.domain"));
                AuthenticationAPIClient client = auth.newAuthenticationAPIClient();
                logger.info("Creating client login in SpringSecurityPlainTextPasswordValidator: user: " +
                        plainTextRequest.getUsername() + ": password: " + plainTextRequest.getPassword()
                        + ": connection: " + auth0Properties.getProperty("auth0.connection"));
                Credentials credentials = client.login(plainTextRequest.getUsername(), plainTextRequest.getPassword())
                        .setConnection(auth0Properties.getProperty("auth0.connection")).execute();
                logger.info("client credentials in SpringSecurityPlainTextPasswordValidator:" + credentials);
                logger.info("client access token in SpringSecurityPlainTextPasswordValidator:" + credentials.getAccessToken());
                logger.info("user username/password in SpringSecurityPlainTextPasswordValidator:" + plainTextRequest.getUsername() + ":" + plainTextRequest.getPassword());
                if (StringUtils.isNotEmpty(credentials.getAccessToken())) {
                    findOrCreateUser(plainTextRequest, credentials);
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

    private UserAccountBean findOrCreateUser(PasswordValidationCallback.PlainTextPasswordRequest plainTextRequest,
            Credentials credentials) throws Exception{
        Map<String, Object> map = decode(credentials.getIdToken());
        UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);

        UserAccountBean ub = (UserAccountBean) userAccountDAO.findByApiKey(plainTextRequest.getUsername());
        if (StringUtils.isEmpty(ub.getName())) {
            ub = (UserAccountBean) userAccountDAO.findByUserName(plainTextRequest.getUsername());
        }
        logger.info("User account:" + ub.getName());
        if (ub.getId() != 0)
            return ub;
        else
            return buildUserAccount(plainTextRequest, map);
    }

    private Map<String, Object> decode(String token) {
        try {
            final ClassLoader classLoader = getClass().getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(PUBLIC_KEY_LOCATION);
            CertificateFactory certificateFactory = CertificateFactory.getInstance(X509_CERTFICATE);
            Certificate certificate = certificateFactory.generateCertificate(inputStream);
            RSAPublicKeyImpl publicKey = (RSAPublicKeyImpl) certificate.getPublicKey();
            RsaVerifier verifier = new RsaVerifier(publicKey);
            Jwt jwt = JwtHelper.decodeAndVerify(token, verifier);
            String content = jwt.getClaims();
            Map<String, Object> map = objectMapper.parseMap(content);
            if (map.containsKey(EXP) && map.get(EXP) instanceof Integer) {
                Integer intValue = (Integer) map.get(EXP);
                map.put(EXP, new Long(intValue));
            }
            return map;
        } catch (Exception e) {
            throw new InvalidTokenException("Cannot convert access token to JSON", e);
        }
    }
    private UserAccountBean buildUserAccount(PasswordValidationCallback.PlainTextPasswordRequest plainTextRequest,
            Map<String, Object> map ) throws Exception {

        UserAccountBean createdUserAccountBean = new UserAccountBean();

        createdUserAccountBean.setName(plainTextRequest.getUsername());
        createdUserAccountBean.setFirstName("fName");
        createdUserAccountBean.setLastName("lName");
        createdUserAccountBean.setEmail("help@openclinica.com");
        createdUserAccountBean.setInstitutionalAffiliation("OC");
        createdUserAccountBean.setLastVisitDate(null);
        createdUserAccountBean.setPasswdTimestamp(null);
        createdUserAccountBean.setPasswdChallengeQuestion("");
        createdUserAccountBean.setPasswdChallengeAnswer("");
        UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);
        UserAccountBean ownerUb = (UserAccountBean) userAccountDAO.findByUserName("root");
        createdUserAccountBean.setOwner(ownerUb);
        createdUserAccountBean.setRunWebservices(true);
        createdUserAccountBean.setPhone("");
        createdUserAccountBean.setAccessCode("");
        createdUserAccountBean.setEnableApiKey(true);
        createdUserAccountBean.setPasswd(plainTextRequest.getPassword());
        createdUserAccountBean.setRunWebservices(true);
        createdUserAccountBean.setApiKey(plainTextRequest.getUsername());

        createdUserAccountBean.addUserType(UserType.SYSADMIN);

        UserAccountDAO uDAO = new UserAccountDAO(dataSource);
        UserAccountBean ub = (UserAccountBean) uDAO.create(createdUserAccountBean);
        return ub;
    }

}