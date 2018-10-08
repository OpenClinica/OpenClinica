package org.akaza.openclinica.service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.akaza.openclinica.config.AppConfig;
import org.akaza.openclinica.dao.core.CoreResources;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.resource.AuthorizationResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("auth0UserService")
public class KeycloakUserServiceImpl implements KeycloakUserService {
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
            String SBSDomainURL = protocol + SBSUrl.substring(index + 2, SBSUrl.indexOf("/", index + 2)) + "/customer-service/api/allowed-connections?subdomain=" + subDomain;
            response = Unirest.get(SBSDomainURL)
                    .header("content-type", "application/json")
                    .asString();
        } catch (UnirestException e) {
            e.printStackTrace();
        }

        if (response == null || response.getBody() == null)
            return authenticated;
        AuthzClient authzClient = AuthzClient.create();
        final AuthorizationResource authRequest = authzClient.authorization(username, password);
        if (authRequest == null)
            return authenticated;
        return true;
    }
}
