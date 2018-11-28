package org.akaza.openclinica.web.pform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.akaza.openclinica.service.OCUserDTO;
import org.akaza.openclinica.service.OCUserRoleDTO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.service.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

@Service("openRosaService")
@Transactional(propagation= Propagation.REQUIRED,isolation= Isolation.DEFAULT)
public class OpenRosaServiceImpl implements OpenRosaService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private String accessToken;

    @Autowired
    OpenRosaXMLUtil openRosaXMLUtil;
    @Autowired
    PermissionService permissionService;

    private static final String AUTH0_ERROR_MESSAGE_ATTRIBUTE = "message";
    public static final String AUTH0_CALL_FAILED = "errorCode.auth0CallFailed";


    public String getErrorMessage(String error){
        String errorMessage = "";
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            JsonNode errorJson = objectMapper.readTree(error);
            errorMessage = errorJson.get(AUTH0_ERROR_MESSAGE_ATTRIBUTE).toString();
        } catch (IOException ioException) {
            logger.error("Failed to read Auth0 error message", ioException);
            throw new CustomParameterizedException(AUTH0_CALL_FAILED);
        }
        return errorMessage;
    }

    public String getUserListFromUserService(StudyAndSiteEnvUuid studyAndSiteEnvUuid) throws Exception {

        List<OCUserRoleDTO> userList = getOcUserRoleDTOs(studyAndSiteEnvUuid.studyEnvUuid);
        return getUsersAsXml(userList, studyAndSiteEnvUuid);
    }

    public OCUserDTO fetchUserInfoFromUserService(StudyAndSiteEnvUuid studyAndSiteEnvUuid, String username) throws Exception {

        List<OCUserRoleDTO> userList = getOcUserRoleDTOs(studyAndSiteEnvUuid.studyEnvUuid);
        for (OCUserRoleDTO ocUser : userList) {
            List<StudyEnvironmentRoleDTO> roles = ocUser.getRoles();
            OCUserDTO userInfo = ocUser.getUserInfo();
            if (userInfo.getUsername().equals(username))
                return userInfo;
        }
        return null;
    }


    public List<OCUserRoleDTO> getOcUserRoleDTOs(String studyEnvUuid) {
        Supplier<ResponseEntity<List<OCUserRoleDTO>>> getUserList = () -> {

            String baseUrl = CoreResources.getField("SBSUrl");

            String uri = baseUrl.replaceAll("/users/", "")
                    + "/study-environments/" + studyEnvUuid + "/users-with-roles" + "?page=0&size=1000";

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            if (StringUtils.isEmpty(accessToken))
                accessToken = permissionService.getAccessToken();
            headers.add("Authorization", "Bearer " + accessToken);
            headers.add("Accept-Charset", "UTF-8");
            HttpEntity<String> entity = new HttpEntity<String>(headers);
            List<HttpMessageConverter<?>> converters = new ArrayList<>();
            MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
            jsonConverter.setObjectMapper(objectMapper);
            converters.add(jsonConverter);
            restTemplate.setMessageConverters(converters);
            Instant start = Instant.now();
            ResponseEntity<List<OCUserRoleDTO>> response =
                    restTemplate.exchange(uri, HttpMethod.GET, entity, new ParameterizedTypeReference<List<OCUserRoleDTO>>() {});
            Instant end = Instant.now();
            logger.info("***** Time execution for {} method : {}   *****", new Object() {
            }.getClass().getEnclosingMethod().getName(), Duration.between(start, end));
            // to test the future.complete(null)
            //throw new RuntimeException("***UserList failed");
            return response;

        };
        return callManagementApi(getUserList);
    }

    private String getUsersAsXml(List<OCUserRoleDTO> userServiceList, StudyAndSiteEnvUuid studyAndSiteEnvUuid) throws Exception {
        Document doc = openRosaXMLUtil.buildDocument();
        Element root = openRosaXMLUtil.appendRootElement(doc);
        List<OCUserDTO> userList = new ArrayList<>();

        if (userServiceList == null)
            return null;
        for (OCUserRoleDTO ocUser : userServiceList) {
            List<StudyEnvironmentRoleDTO> roles = ocUser.getRoles();
            OCUserDTO userInfo = ocUser.getUserInfo();
            if (userInfo.getStatus() != UserStatus.ACTIVE)
                continue;
            if (userInfo.getUsername().equals("root"))
                continue;
            for (StudyEnvironmentRoleDTO role : roles) {
                boolean include = true;
                if (StringUtils.isNotEmpty(studyAndSiteEnvUuid.siteEnvUuid)) {
                    include = false;
                    if (StringUtils.isEmpty(role.getSiteUuid()) || (StringUtils.equals(role.getSiteUuid(), studyAndSiteEnvUuid.siteEnvUuid)))
                        include = true;
                } else {
                    // site level users are anot be dded if the participant is not at the site level
                    if (StringUtils.isNotEmpty(role.getSiteUuid())) {
                        include = false;
                    }
                }
                if (include) {
                    userList.add(userInfo);
                }
            }
        }
        Collections.sort(userList, Comparator.comparing(OCUserDTO::getLastName));
        userList.forEach(userInfo -> {
            Element item = doc.createElement("item");
            Element userName = doc.createElement("user_name");
            userName.appendChild(doc.createTextNode(userInfo.getUsername()));
            Element firstName = doc.createElement("first_name");
            firstName.appendChild(doc.createTextNode(userInfo.getFirstName()));
            Element lastName = doc.createElement("last_name");
            lastName.appendChild(doc.createTextNode(userInfo.getLastName()));
            item.appendChild(userName);
            item.appendChild(firstName);
            item.appendChild(lastName);
            if (studyAndSiteEnvUuid.currentUser != null
                    && userInfo.getUsername().equals(studyAndSiteEnvUuid.currentUser.getUserName())) {
                item.setAttribute("current", "true");
            }
            root.appendChild(item);
        });
        String writer = openRosaXMLUtil.getWriter(doc);
        return writer;
    }

    /**
     * This method calls the given supplier and if the API call returns a 401 (because of expired token), it will
     * re-initialize the token and call the API again with the new token.
     * @param supplier the response type
     * @return the response entity list if successful, otherwise logs an exception and returns null.
     */
    public  <T> List<T>  callManagementApi(Supplier<ResponseEntity<List<T>>> supplier) {
        ResponseEntity<List<T>> response;

        CompletableFuture<ResponseEntity<List<T>>> future
                = CompletableFuture.supplyAsync(supplier);

        try {
            String timeoutStr = CoreResources.getField("queryUserListServiceTimeout");
            int timeout = 1000;
            if (StringUtils.isNotEmpty(timeoutStr)) {
                timeout = new Integer(timeoutStr);
            }
            response = future.get(timeout, TimeUnit.MILLISECONDS);
        }  catch(TimeoutException e) {
            logger.error("User Service Timeout", "User service did not respond within allocated time");
            return null;
        } catch (Exception e) {
            Throwable t = e.getCause();
            logger.error("Exception:" + t);
            if (t instanceof HttpClientErrorException) {
                HttpClientErrorException ex = (HttpClientErrorException) t;
                if (ex.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
                    logger.error("**********Auth0 access token expired. Creating a new one.");
                    accessToken = permissionService.getAccessToken();
                    logger.error("*********Calling the api again with the new token.");
                    response = supplier.get();
                } else {
                    // for all other 4xx errors, extract the error message from Auth0 and throw a 400 error
                    String errorResponse = ex.getResponseBodyAsString();
                    logger.error(AUTH0_CALL_FAILED, errorResponse);
                    return null;
                }
            } else {
                String errorResponse = e.getMessage();
                logger.error(AUTH0_CALL_FAILED, errorResponse);
                return null;
            }

        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new CustomParameterizedException(AUTH0_CALL_FAILED, response.getStatusCode().getReasonPhrase());
        }

        return response.getBody();

    }

}
