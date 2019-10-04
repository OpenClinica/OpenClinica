package core.org.akaza.openclinica.service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import core.org.akaza.openclinica.dao.core.CoreResources;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.util.HttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("keycloakUserService")
public class KeycloakUserServiceImpl implements KeycloakUserService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public boolean authenticateKeycloakUser(String username, String password) {
        HttpResponse<String> response = null;
        try {
            String SBSUrl = CoreResources.getField("SBSBaseUrl");
            int index = SBSUrl.indexOf("//");
            String subDomain = SBSUrl.substring(index  + 2,  SBSUrl.indexOf("."));
            String SBSDomainURL = SBSUrl + "/customer-service/api/allowed-connections?subdomain=" + subDomain;
            response = Unirest.get(SBSDomainURL)
                    .header("content-type", "application/json")
                    .asString();
        } catch (UnirestException e) {
            logger.error("Error accessing the Unirest: ",e);
        }

        if (response == null || response.getBody() == null)
            return false;
        AuthzClient authzClient = AuthzClient.create(CoreResources.getKeyCloakConfig());
        try {
             authzClient.obtainAccessToken(username, password);
        } catch (HttpResponseException e) {
            logger.error("Authorization:" + e);
            return false;
        }
        return true;
    }
}
