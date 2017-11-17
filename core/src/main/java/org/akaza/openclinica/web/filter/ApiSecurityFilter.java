package org.akaza.openclinica.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.service.OCUserDTO;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import sun.security.rsa.RSAPublicKeyImpl;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.*;

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
                } else if (basic.equalsIgnoreCase("Bearer")) {
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
                            request.getSession().setAttribute("accessToken", accessToken);

                            CoreResources.setRootUserAccountBean(request, dataSource);
                            request.getSession().setAttribute("userContextMap", userContextMap);
                            UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);
                            UserAccountBean ub = (UserAccountBean) userAccountDAO.findByUserUuid((String) userContextMap.get("userUuid"));
                            if (StringUtils.isNotEmpty(_username) && ub.getId() != 0) {
                                Authentication authentication = new UsernamePasswordAuthenticationToken(_username, null,
                                        AuthorityUtils.createAuthorityList("ROLE_USER"));
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                request.getSession().setAttribute("userBean",ub);
                            } else {
                                ub = CoreResources.setRootUserAccountBean(request, dataSource);
                                if (StringUtils.isEmpty(ub.getUserUuid())) {
                                    // is the userUuid from the context map of the root user?
                                    ResponseEntity<OCUserDTO> userResponse = getUserDetails(request);
                                    if (userResponse != null) {
                                        OCUserDTO userDTO = userResponse.getBody();
                                        if (StringUtils.equalsIgnoreCase(userDTO.getUsername(), "root")) {
                                            ub.setUserUuid(userDTO.getUuid());
                                            userAccountDAO.update(ub);
                                        }
                                    }
                                }
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

    public ResponseEntity getUserDetails (HttpServletRequest request) {
        Map<String, Object> userContextMap = (LinkedHashMap<String, Object>) request.getSession().getAttribute("userContextMap");
        if (userContextMap == null)
            return null;
        String userUuid = (String) userContextMap.get("userUuid");
        String uri = CoreResources.getField("SBSUrl") + userUuid;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String accessToken = (String) request.getSession().getAttribute("accessToken");
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Accept-Charset", "UTF-8");
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(objectMapper);
        converters.add(jsonConverter);
        restTemplate.setMessageConverters(converters);
        ResponseEntity<OCUserDTO> response = restTemplate.exchange(uri, HttpMethod.GET, entity, OCUserDTO.class);
        return response;
    }

}
