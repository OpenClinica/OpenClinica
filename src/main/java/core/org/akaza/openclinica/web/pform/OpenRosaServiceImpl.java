package core.org.akaza.openclinica.web.pform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import core.org.akaza.openclinica.service.OCUserDTO;
import core.org.akaza.openclinica.service.OCUserRoleDTO;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.service.*;
import core.org.akaza.openclinica.web.rest.client.auth.impl.KeycloakClientImpl;
import org.akaza.openclinica.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.stream.Collectors;

@Service("openRosaService")
@Transactional(propagation= Propagation.REQUIRED,isolation= Isolation.DEFAULT)
public class OpenRosaServiceImpl implements OpenRosaService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private String accessToken;

    @Autowired
    OpenRosaXMLUtil openRosaXMLUtil;
    @Autowired
    PermissionService permissionService;
    @Autowired
    private KeycloakClientImpl keycloakClient;
    @Autowired
    private UserService userService;

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

        List<OCUserDTO> userList = userService.getfilteredOCUsersDTOFromUserService(studyAndSiteEnvUuid, accessToken);
        return getUsersAsXml(userList, studyAndSiteEnvUuid);
    }

    public OCUserDTO fetchUserInfoFromUserService(StudyAndSiteEnvUuid studyAndSiteEnvUuid, String username) throws Exception {

        List<OCUserRoleDTO> userList = userService.getOcUserRoleDTOsFromUserService(studyAndSiteEnvUuid.studyEnvUuid, accessToken);
        for (OCUserRoleDTO ocUser : userList) {
            List<StudyEnvironmentRoleDTO> roles = ocUser.getRoles();
            OCUserDTO userInfo = ocUser.getUserInfo();
            if (userInfo.getUsername().equals(username))
                return userInfo;
        }
        return null;
    }

    private String getUsersAsXml(List<OCUserDTO> userList, StudyAndSiteEnvUuid studyAndSiteEnvUuid) throws Exception {
        Document doc = openRosaXMLUtil.buildDocument();
        Element root = openRosaXMLUtil.appendRootElement(doc);

        if (userList == null)
            return null;
        userList = userList.stream().filter(ocUserDTO -> !ocUserDTO.getUsername().equals("root")).collect(Collectors.toList());

        if (userList == null)
            return null;
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


}
