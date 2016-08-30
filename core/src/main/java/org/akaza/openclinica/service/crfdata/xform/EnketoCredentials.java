package org.akaza.openclinica.service.crfdata.xform;

import java.io.Serializable;

import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.service.pmanage.Authorization;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

public class EnketoCredentials implements Serializable {
    private String serverUrl = null;
    private String apiKey = null;
    private String ocInstanceUrl = null;
    protected static final Logger logger = LoggerFactory.getLogger(EnketoCredentials.class);

    private EnketoCredentials() {

    }

    public static EnketoCredentials getInstance(String studyOid) {
        EnketoCredentials credentials = new EnketoCredentials();

        String pManageUrl = CoreResources.getField("portalURL") + "/app/rest/oc/authorizations";
        String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
        RestTemplate rest = new RestTemplate();

        try {
            Authorization[] response = rest.getForObject(pManageUrl + "?studyoid=" + studyOid + "&instanceurl=" + ocUrl, Authorization[].class);

            if (response.length > 0) {
                credentials.setServerUrl(response[0].getPformUrl());
                credentials.setApiKey(response[0].getPformApiKey());
                credentials.setOcInstanceUrl(ocUrl);
            } else {
                logger.error("Unexpected response received from Participant Portal while retrieving PForm credentials.  Returning empty credentials.");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
            logger.error("Unexpected Exception received from Participant Portal while retrieving PForm credentials: " + e.getMessage()
                    + ".  Returning empty credentials.");
        }
        return credentials;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getOcInstanceUrl() {
        return ocInstanceUrl;
    }

    public void setOcInstanceUrl(String ocInstanceUrl) {
        this.ocInstanceUrl = ocInstanceUrl;
    }

}
