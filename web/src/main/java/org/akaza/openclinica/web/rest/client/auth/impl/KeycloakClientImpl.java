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
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.terracotta.modules.ehcache.async.exceptions.ProcessingException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

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

    private static final String USER_TYPE_ATTRIBUTE = "userType";
    String DB_CONNECTION_KEY = "dbConnection";
    public static final String IDENTITY_SERVER_CALL_FAILED = "errorCode.identityServerCallFailed";
    private static final String PATH_SEPARATOR = "/";

    @Autowired
    CustomerServiceClientImpl customerServiceClient;

    @Autowired
    private Keycloak keycloak;

    public String createParticipateUser(HttpServletRequest request, String email, String username, String accessCode) {
        logger.debug("Calling Keycloak to create participate user with username: {}", username);
        Map<String, Object> userContextMap = (LinkedHashMap<String, Object>) request.getSession().getAttribute("userContextMap");
        String customerUuid = (String) userContextMap.get("customerUuid");

        String realm = getRealmName(request, customerUuid);
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setEmail(email);
        userRepresentation.setUsername(username);
        userRepresentation.setEmailVerified(false);

        Map<String, List<String>> userAttributes = new HashMap<>();
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

    public String getRealmName(HttpServletRequest request, String customerUuid) {
        CustomerDTO customerDTO = customerServiceClient.getCustomer(request, customerUuid);
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

}
