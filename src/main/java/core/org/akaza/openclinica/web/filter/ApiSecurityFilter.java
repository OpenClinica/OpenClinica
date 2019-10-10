package core.org.akaza.openclinica.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import core.org.akaza.openclinica.bean.core.ApplicationConstants;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.StudyBean;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyDAO;
import core.org.akaza.openclinica.service.OCUserDTO;
import core.org.akaza.openclinica.service.UserType;
import core.org.akaza.openclinica.service.auth.TokenService;
import core.org.akaza.openclinica.service.user.CreateUserCoreService;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by krikorkrumlian on 8/7/15.
 */
public class ApiSecurityFilter extends OncePerRequestFilter {
    protected final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass().getName());

    private String realm = "Protected";


    @Autowired
    private DataSource dataSource;

    @Autowired CreateUserCoreService userService;
    @Autowired
    TokenService tokenService;

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
                    // 1. connect to root and update roles
                    // 2. create new user if doesn't exist and update roles
                    try {
                        String accessToken = st.nextToken();
                        final Map<String, Object> decodedToken = tokenService.decodeAndVerify(accessToken);
                        if (accessToken != null ) {
                            String _username = decodedToken.get("sub").toString();
                            LinkedHashMap<String, Object> userContextMap = (LinkedHashMap<String, Object>) decodedToken.get("https://www.openclinica.com/userContext");
                            logger.debug("userContext:" + userContextMap);
                            userContextMap.put("username", _username);
                            request.getSession().setAttribute("accessToken", accessToken);

                            CoreResources.setRootUserAccountBean(request, dataSource);
                            request.getSession().setAttribute("userContextMap", userContextMap);
                            UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);
                            StudyDAO studyDAO = new StudyDAO(dataSource);

                            String ocUserUuid = null;
                            String userType = (String) userContextMap.get("userType");
                            if (userType.equals(UserType.PARTICIPATE.getName())) {
                                ocUserUuid = (String) userContextMap.get("username");
                            } else {
                                ocUserUuid = (String) userContextMap.get("userUuid");
                            }

                            UserAccountBean ub = (UserAccountBean) userAccountDAO.findByUserUuid((ocUserUuid));
                            StudyBean publicStudyBean= (StudyBean) studyDAO.findByPK(ub.getActiveStudyId());

                            if (userType.equals(UserType.SYSTEM.getName())){
                                String clientId = decodedToken.get("clientId").toString();
                                UserAccountBean systemUser = createSystemUser(clientId, userAccountDAO, request);
                                if (systemUser != null) {
                                    ub = systemUser;
                                }
                            }

                            // Username comes back populated but ocUserUuid is "systemuserUuid" so our UserAccountBean ub comes back empty.
                            if (StringUtils.isNotEmpty(_username) && ub.getId() != 0) {
                                Authentication authentication = new UsernamePasswordAuthenticationToken(_username, null,
                                        AuthorityUtils.createAuthorityList("ROLE_USER"));
                                SecurityContextHolder.getContext().setAuthentication(authentication);

                            }
                            else {
                                // If the user doesn't exist then we try to do a lookup from user-service and use that data to create a new user.

                                OCUserDTO userDTO = getUserDetails(request);
                                if (userDTO.getUsername().equalsIgnoreCase("root")) {
                                    ub = CoreResources.setRootUserAccountBean(request, dataSource);
                                    ub.setUserUuid(userDTO.getUuid());
                                    userAccountDAO.update(ub);
                                } else {
                                    try {
                                        Map<String, String> userAccount = createUserAccount(userDTO);
                                          ub = userService.createUser(request, userAccount);
                                    } catch (Exception e) {
                                        logger.error("Failed user creation:" + e.getMessage());
                                    }
                                }
                            }
                            request.getSession().setAttribute("userBean",ub);
                            request.getSession().setAttribute("studyOid",publicStudyBean.getOid());
                        } else {
                            unauthorized(response, "Invalid authentication token");
                            return;
                        }
                    } catch (UnsupportedEncodingException e) {
                        throw new Error("Couldn't retrieve authentication", e);
                    }

                }
            }
        } else if (request.getSession().getAttribute("userBean") != null ) {
            UserAccountBean userAccountBean = (UserAccountBean)
                    request.getSession().getAttribute("userBean");
            logger.debug("This user is already logged in {}", userAccountBean.getName());
        }else {
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




    public OCUserDTO getUserDetails (HttpServletRequest request) {
        Map<String, Object> userContextMap = (LinkedHashMap<String, Object>) request.getSession().getAttribute("userContextMap");
        if (userContextMap == null)
            return null;
        String userUuid = (String) userContextMap.get("userUuid");
        String uri = CoreResources.getField("SBSBaseUrl") + "/user-service/api/users/" + userUuid;
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
        if (response == null)
            return null;
        return response.getBody();
    }
    private HashMap<String, String>  createUserAccount(OCUserDTO user) throws Exception {
        HashMap<String, String> map = new HashMap<>();
        map.put("username", user.getUsername());
        if (StringUtils.isNotEmpty(user.getFirstName()))
            map.put("fName", user.getFirstName());
        else
            map.put("fName", "first");
        if (StringUtils.isNotEmpty(user.getLastName()))
            map.put("lName", user.getLastName());
        else
            map.put("lName", "last");

        map.put("role_name", "Data Manager");
        map.put("user_uuid", user.getUuid());
        core.org.akaza.openclinica.service.UserType userType = user.getUserType();

        map.put("user_type", userType.getName());
        map.put("authorize_soap", "true");
        map.put("email", user.getEmail());
        map.put("institution", "OC");
        return map;
    }

    //TODO Put this somewhere else?
    private HashMap<String, String>  createRandomizeUserAccount() {
        HashMap<String, String> map = new HashMap<>();
        map.put("username", "randomize");
        map.put("fName", "Randomize");
        map.put("lName", "Service");
        map.put("role_name", "Data Manager");
        map.put("user_uuid", "randomizeSystemUserUuid");
        map.put("user_type", core.org.akaza.openclinica.service.UserType.TECH_ADMIN.getName());
        map.put("authorize_soap", "true");
        map.put("email", "openclinica-developers@openclinica.com");
        map.put("institution", "OC");
        return map;
    }
    private HashMap<String, String>  createDicomUserAccount() {
        HashMap<String, String> map = new HashMap<>();
        map.put("username", "dicom");
        map.put("fName", "Dicom");
        map.put("lName", "Service");
        map.put("role_name", "Data Manager");
        map.put("user_uuid", "dicomSystemUserUuid");
        map.put("user_type", core.org.akaza.openclinica.service.UserType.TECH_ADMIN.getName());
        map.put("authorize_soap", "true");
        map.put("email", "openclinica-developers@openclinica.com");
        map.put("institution", "OC");
        return map;
    }

    private UserAccountBean createSystemUser(String clientId, UserAccountDAO userAccountDAO, HttpServletRequest request) {
        UserAccountBean userAccountBean = null;
        Map<String, String> userAccountToCreate = null;
        if (clientId.equals(ApplicationConstants.RANDOMIZE_CLIENT)){
            userAccountBean = (UserAccountBean) userAccountDAO.findByUserName(ApplicationConstants.RANDOMIZE_USERNAME);
            if (userAccountBean.getName().isEmpty()) {
                userAccountToCreate = createRandomizeUserAccount();
            }
        } else if (clientId.equals(ApplicationConstants.DICOM_CLIENT)) {
            userAccountBean = (UserAccountBean) userAccountDAO.findByUserName(ApplicationConstants.DICOM_USERNAME);
            if (userAccountBean.getName().isEmpty()) {
                userAccountToCreate = createDicomUserAccount();
            }
        }

        if (userAccountToCreate != null) {
            try {
                userAccountBean = userService.createUser(request, userAccountToCreate);
            } catch (Exception e) {
                logger.error("Failed user creation:", e.getMessage());
            }
        }
        return userAccountBean;
    }

}