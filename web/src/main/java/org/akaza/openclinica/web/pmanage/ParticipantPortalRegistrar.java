package org.akaza.openclinica.web.pmanage;

import java.net.URL;

import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.web.pmanage.Authorization;
import org.akaza.openclinica.web.pmanage.Study;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

public class ParticipantPortalRegistrar {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public String getRegistrationStatus(String studyOid) {
        String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
        String pManageUrl = CoreResources.getField("portalURL") + "/app/rest/oc/authorizations?studyoid=" + studyOid + "&instanceurl=" + ocUrl;
        RestTemplate rest = new RestTemplate();
        Authorization[] response = rest.getForObject(pManageUrl, Authorization[].class);
        if (response.length > 0 && response[0].getAuthorizationStatus() != null)
            return response[0].getAuthorizationStatus().getStatus();
        else
            return "";
    }

    public String registerStudy(String studyOid) {
        String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
        String pManageUrl = CoreResources.getField("portalURL") + "/app/rest/oc/authorizations?studyoid=" + studyOid + "&instanceurl=" + ocUrl;
        Authorization authRequest = new Authorization();
        Study authStudy = new Study();
        authStudy.setStudyOid(studyOid);
        authStudy.setInstanceUrl(ocUrl);
        authRequest.setStudy(authStudy);

        RestTemplate rest = new RestTemplate();
        Authorization response = rest.postForObject(pManageUrl, authRequest, Authorization.class);
        if (response != null && response.getAuthorizationStatus() != null)
            return response.getAuthorizationStatus().getStatus();
        else
            return "";
    }

    public String getStudyHost(String studyOid) throws Exception {

        String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
        String pManageUrl = CoreResources.getField("portalURL");
        String pManageUrlFull = pManageUrl + "/app/rest/oc/authorizations?studyoid=" + studyOid + "&instanceurl=" + ocUrl;

        RestTemplate rest = new RestTemplate();
        Authorization[] response = rest.getForObject(pManageUrlFull, Authorization[].class);
        if (response.length > 0 && response[0].getStudy() != null && response[0].getStudy().getHost() != null && !response[0].getStudy().getHost().equals("")) {
            URL url = new URL(pManageUrl);
            String port = "";
            if (url.getPort() > 0)
                port = ":" + String.valueOf(url.getPort());
            return url.getProtocol() + "://" + response[0].getStudy().getHost() + "." + url.getHost() + port + "/#/login";
        } else
            return "";
    }
}
