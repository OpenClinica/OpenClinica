package org.akaza.openclinica.web.rest.client.auth.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.service.CustomParameterizedException;
import org.akaza.openclinica.service.OCUserDTO;
import org.akaza.openclinica.service.UserType;
import org.akaza.openclinica.web.rest.client.cs.dto.CustomerDTO;
import org.akaza.openclinica.web.rest.client.cs.impl.CustomerServiceClientImpl;
import org.apache.commons.lang.StringUtils;
import org.javers.common.collections.Lists;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.terracotta.modules.ehcache.async.exceptions.ProcessingException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class KeycloakClientImpl {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private static final String USER_UUID_ATTRIBUTE = "ocUserUuid";
    private static final String CUSTOMER_UUID_ATTRIBUTE = "customerUuid";
    private static final String ACCESS_CODE_ATTRIBUTE = "accessCode";
    private static final String STUDY_ENV_UUID_ATTRIBUTE = "studyEnvUuid";

    private static final String USER_TYPE_ATTRIBUTE = "userType";
    String DB_CONNECTION_KEY = "dbConnection";
    public static final String IDENTITY_SERVER_CALL_FAILED = "errorCode.identityServerCallFailed";
    private static final String PATH_SEPARATOR = "/";
    private static final MessageFormat USERS_PATH = new MessageFormat("/realms/{0}/oc-rest/users");
    private static final String ACCESS_CODE = "access_code";

    @Autowired
    CustomerServiceClientImpl customerServiceClient;

    @Autowired
    private Keycloak keycloak;

    public String createParticipateUser(String accessToken, String email, String username, String accessCode,String studyEnvironment,String customerUuid) {
        logger.debug("Calling Keycloak to create participate user with username: {}", username);


        String realm = getRealmName(accessToken, customerUuid);
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setEmail(email);
        userRepresentation.setUsername(username);
        userRepresentation.setEmailVerified(false);

        Map<String, List<String>> userAttributes = new HashMap<>();
        userAttributes.put(STUDY_ENV_UUID_ATTRIBUTE, Lists.asList(studyEnvironment));
        userAttributes.put(CUSTOMER_UUID_ATTRIBUTE, Lists.asList(customerUuid));
        userAttributes.put(USER_TYPE_ATTRIBUTE, Lists.asList(UserType.PARTICIPATE.getName()));
        userAttributes.put(ACCESS_CODE_ATTRIBUTE, Lists.asList(accessCode));
        userRepresentation.setAttributes(userAttributes);
        Response createUserResponse = keycloak
                .realm(realm)
                .users()
                .create(userRepresentation);

        Response.StatusType statusType = createUserResponse.getStatusInfo();
        if (statusType.equals(Response.Status.CREATED)) {
            logger.debug("Successfully created participate user in Keycloak with username: {}", username);
            String locationHeader = createUserResponse.getHeaderString(HttpHeaders.LOCATION);
            String keycloakUserId = StringUtils.substringAfterLast(locationHeader, PATH_SEPARATOR);
            return keycloakUserId;
        }
        logger.debug("Create user call to Keycloak has failed");
        return handleKeycloakError(createUserResponse);
    }

    public boolean searchAccessCodeExistsOrig(String accessToken, String accessCode,String customerUuid) {
        logger.debug("Calling Keycloak to search for AccessCode uniqueness");
        String realm = getRealmName(accessToken, customerUuid);
        List<UserRepresentation> response = keycloak
                .realm(realm)
                .users()
                .search(null, accessCode, null, null, 0, 100);

        if (response.size() != 0) {
            return true;
        } else {
            logger.debug(" AccessCode is Unique");
            return false;
        }

    }

    public String getAccessCode(String accessToken, String userUuid,String customerUuid ) {
        logger.debug("Calling Keycloak to get participate UserPresentation object");

        String realm = getRealmName(accessToken, customerUuid);
        UserResource userResource = keycloak
                .realm(realm)
                .users()
                .get(userUuid);

        UserRepresentation userRepresentation = userResource.toRepresentation();
        Map<String, List<String>> attributes =  userRepresentation.getAttributes();
        List<String> accessCodes = attributes.get(ACCESS_CODE_ATTRIBUTE);
        logger.info("Access Code : {}",accessCodes.get(0));
        return accessCodes.get(0);
    }


    public String getRealmName(String accessToken, String customerUuid) {
        CustomerDTO customerDTO = customerServiceClient.getCustomer(accessToken, customerUuid);
        String realmName = customerDTO.getMetadata().get(DB_CONNECTION_KEY);
        logger.debug("Realm for customer uuid: {} is {}", customerUuid, realmName);
        return realmName;
    }

    private String handleKeycloakError(Response errorResponse) {
        String errorMessage;
        try {
            KeycloakError keycloakError = errorResponse.readEntity(KeycloakError.class);
            errorMessage = keycloakError.getErrorMessage();
            logger.error("Keycloak error message: {}", errorMessage);
        } catch (Exception processingException) {
            errorMessage = errorResponse.readEntity(String.class);
            logger.error("Failed to parse Keycloak error so returning the error response as a string: {}", errorMessage);
        }
        throw new CustomParameterizedException(IDENTITY_SERVER_CALL_FAILED, errorMessage);
    }


    public boolean searchAccessCodeExists(String accessToken, String accessCode,String customerUuid) {
        logger.debug("Calling Keycloak to search for AccessCode uniqueness");
        RestTemplate restTemplate = new RestTemplate();


        String realm = getRealmName(accessToken, customerUuid);

        AuthzClient authzClient = AuthzClient.create();
        String keycloakBaseUrl = authzClient.getConfiguration().getAuthServerUrl();

        String usersUrlPath = USERS_PATH.format(new String[]{realm});
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(keycloakBaseUrl)
                .path(usersUrlPath)
                .queryParam(ACCESS_CODE, accessCode);

        String uri = uriComponentsBuilder.toUriString();
        logger.debug("Get User Access Code search : {}", uri);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Accept-Charset", "UTF-8");
        HttpEntity entity = new HttpEntity<OCUserDTO>(headers);
        ResponseEntity<List<UserRepresentation>> response = null;
        try {
            response =
                    restTemplate.exchange(uri, HttpMethod.GET, entity, new ParameterizedTypeReference<List<UserRepresentation>>() {
                    });

        } catch (HttpClientErrorException e) {
            logger.error("Auth0 error message: {}", e.getResponseBodyAsString());
        }

        if (response != null && response.getBody().size() != 0) {
            return true;
        } else {
            logger.debug(" AccessCode is Unique");
            return false;
        }

    }




}
