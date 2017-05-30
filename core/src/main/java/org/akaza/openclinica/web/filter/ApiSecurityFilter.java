package org.akaza.openclinica.web.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.openclinica.jwtverifier.authentication.UserContext;
import org.akaza.openclinica.bean.core.UserType;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.filter.OncePerRequestFilter;

import sun.security.rsa.RSAPublicKeyImpl;

import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.logging.Logger;

/**
 * Created by krikorkrumlian on 8/7/15.
 */
public class ApiSecurityFilter extends OncePerRequestFilter {
    protected final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass().getName());

    private String realm = "Protected";

    private JsonParser objectMapper = JsonParserFactory.create();
    final String EXP = "exp";

    @Autowired
    private DataSource dataSource;
    private static final String PUBLIC_KEY_LOCATION = "oc4.cer";
    private static final String X509_CERTFICATE = "X509";
    private static final String API_AUDIENCE = "https://www.openclinica.com";


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            StringTokenizer st = new StringTokenizer(authHeader);
            if (st.hasMoreTokens()) {
                String basic = st.nextToken();

                if (basic.equalsIgnoreCase("Basic")) {
                    try {
                        String credentials = new String(Base64.decodeBase64(st.nextToken().getBytes()), "UTF-8");
                        int p = credentials.indexOf(":");
                        if (p != -1) {
                            String _username = credentials.substring(0, p).trim();
                            String _password = credentials.substring(p + 1).trim();

                            UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);
                            UserAccountBean ub = (UserAccountBean) userAccountDAO.findByApiKey(_username);
                            if (!_username.equals("") && ub.getId() != 0) {
                                request.getSession().setAttribute("userBean",ub);
                            }else{
                                unauthorized(response, "Bad credentials");
                                return;
                            }
                        } else {
                            unauthorized(response, "Invalid authentication token");
                            return;
                        }
                    } catch (UnsupportedEncodingException e) {
                        throw new Error("Couldn't retrieve authentication", e);
                    }
                }else if (basic.equalsIgnoreCase("Bearer")) {
                    // TODO
                    // 1. connect to root and update roles
                    // 2. create new user if doesn't exist and update roles
                    try {
                        String accessToken = st.nextToken();
                        final Map<String, Object> decodedToken = decode(accessToken);
                        if (accessToken != null ) {
                            String _username = decodedToken.get("sub").toString();
                            LinkedHashMap<String, Object> userContextMap = (LinkedHashMap<String, Object>) decodedToken.get("https://www.openclinica.com/userContext");
                            logger.debug("userContext:" + userContextMap);
                            userContextMap.put("username", _username);
                            CoreResources.setRootUserAccountBean(request, dataSource);
                            request.getSession().setAttribute("userContextMap", userContextMap);
                            UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);
                            UserAccountBean ub = (UserAccountBean) userAccountDAO.findByApiKey(_username);
                            if (StringUtils.isNotEmpty(_username) && ub.getId() != 0) {
                                Authentication authentication = new UsernamePasswordAuthenticationToken(_username, null,
                                        AuthorityUtils.createAuthorityList("ROLE_USER"));
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                request.getSession().setAttribute("userBean",ub);
                            } else {
                                CoreResources.setRootUserAccountBean(request, dataSource);
                            }
                        } else {
                            unauthorized(response, "Invalid authentication token");
                            return;
                        }
                    } catch (UnsupportedEncodingException e) {
                        throw new Error("Couldn't retrieve authentication", e);
                    }

                }
            }
        } else {
            unauthorized(response);
        }

        filterChain.doFilter(request, response);
    }

    private void createProtocolServiceUser() {

    }
    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
        response.sendError(401, message);
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        unauthorized(response, "Unauthorized");
    }


    protected Map<String, Object> decode(String token) {
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
        }
        catch (Exception e) {
            throw new InvalidTokenException("Cannot convert access token to JSON", e);
        }
    }

}
