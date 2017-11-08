package org.akaza.openclinica.service;

import com.auth0.client.auth.AuthAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.net.AuthRequest;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.akaza.openclinica.config.AppConfig;
import org.akaza.openclinica.dao.core.CoreResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ResourceBundle;

@Service("auth0UserService")
public class Auth0UserServiceImpl implements Auth0UserService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired
    private AppConfig appConfig;
    public boolean authenticateAuth0User(String username, String password) {
        boolean authenticated = false;
        HttpResponse<String> response = null;
        try {
            String SBSUrl = CoreResources.getField("SBSUrl");
            int index = SBSUrl.indexOf("//");
            String protocol = SBSUrl.substring(0, index) + "//";
            String subDomain = SBSUrl.substring(SBSUrl.indexOf("//")  + 2,  SBSUrl.indexOf("."));
            String SBSDomainURl = protocol + SBSUrl.substring(index + 2, SBSUrl.indexOf("/", index + 2)) + "/customer-service/api/allowed-connections?subdomain=" + subDomain;
            response = Unirest.get(SBSDomainURl)
                    .header("content-type", "application/json")
                    .asString();
        } catch (UnirestException e) {
            e.printStackTrace();
        }

        if (response == null || response.getBody() == null)
            return authenticated;
        String connection = (String) new JsonNode(response.getBody()).getArray().get(0);
        ResourceBundle rb = ResourceBundle.getBundle("auth0");

        AuthAPI authAPI = new AuthAPI(appConfig.getDomain(), appConfig.getClientId(),
                appConfig.getClientSecret());
        authAPI.setLoggingEnabled(true);
        AuthRequest authRequest = authAPI.login(username, password, connection);
        if (authRequest == null)
            return authenticated;

        try {
            TokenHolder tokenHolder = authRequest.execute();
        } catch (Auth0Exception e) {
            logger.error("Error authenticating auth0 user:" + e.getMessage());
            return authenticated;
        }
        return true;
    }
}
