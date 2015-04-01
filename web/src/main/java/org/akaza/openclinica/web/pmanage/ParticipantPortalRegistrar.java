package org.akaza.openclinica.web.pmanage;

import java.net.URL;

import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.web.pmanage.Authorization;
import org.akaza.openclinica.web.pmanage.Study;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class ParticipantPortalRegistrar {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final String AVAILABLE = "available";
    public static final String UNAVAILABLE = "unavailable";
    public static final String UNKNOWN = "unknown";

    public Authorization getAuthorization(String studyOid) {
        String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
        String pManageUrl = CoreResources.getField("portalURL") + "/app/rest/oc/authorizations?studyoid=" + studyOid + "&instanceurl=" + ocUrl;
        RestTemplate rest = new RestTemplate();
        try {
            Authorization[] response = rest.getForObject(pManageUrl, Authorization[].class);
            if (response.length > 0 && response[0].getAuthorizationStatus() != null)
                return response[0];
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    public String getRegistrationStatus(String studyOid) {
        String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
        String pManageUrl = CoreResources.getField("portalURL") + "/app/rest/oc/authorizations?studyoid=" + studyOid + "&instanceurl=" + ocUrl;
        RestTemplate rest = new RestTemplate();
        try {
            Authorization[] response = rest.getForObject(pManageUrl, Authorization[].class);
            if (response.length > 0 && response[0].getAuthorizationStatus() != null)
                return response[0].getAuthorizationStatus().getStatus();
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return "";
    }

    public String getHostNameAvailability(String hostName) {
        // String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
        String pManageUrl = CoreResources.getField("portalURL") + "/app/permit/studys/name?hostName=" + hostName;
        RestTemplate rest = new RestTemplate();
        try {
            ResponseEntity response = rest.getForEntity(pManageUrl, null);
            if (response.getStatusCode().equals(HttpStatus.OK))
                return AVAILABLE;
            else
                return UNAVAILABLE;
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return UNKNOWN;
    }

    public String registerStudy(String studyOid) {
        return registerStudy(studyOid, null);
    }

    public String registerStudy(String studyOid, String hostName) {
        String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
        String pManageUrl = CoreResources.getField("portalURL") + "/app/rest/oc/authorizations?studyoid=" + studyOid + "&instanceurl=" + ocUrl;
        Authorization authRequest = new Authorization();
        Study authStudy = new Study();
        authStudy.setStudyOid(studyOid);
        authStudy.setInstanceUrl(ocUrl);
        authStudy.setHost(hostName);
        authRequest.setStudy(authStudy);

        RestTemplate rest = new RestTemplate();

        try {
            Authorization response = rest.postForObject(pManageUrl, authRequest, Authorization.class);
            if (response != null && response.getAuthorizationStatus() != null)
                return response.getAuthorizationStatus().getStatus();
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return "";
    }

    public String getStudyHost(String studyOid) throws Exception {

        String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
        String pManageUrl = CoreResources.getField("portalURL");
        String pManageUrlFull = pManageUrl + "/app/rest/oc/authorizations?studyoid=" + studyOid + "&instanceurl=" + ocUrl;

        RestTemplate rest = new RestTemplate();
        try {
            Authorization[] response = rest.getForObject(pManageUrlFull, Authorization[].class);
            if (response.length > 0 && response[0].getStudy() != null && response[0].getStudy().getHost() != null
                    && !response[0].getStudy().getHost().equals("")) {
                URL url = new URL(pManageUrl);
                return buildUrl(url, response);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return "";
    }

    public String buildUrl(URL url, Authorization[] response) throws Exception {

        String[] arrayUrl = url.getHost().split("\\.");
        String[] tmpArrayUrl = new String[arrayUrl.length + 1];
        if (arrayUrl.length > 2) {
            arrayUrl[0] = response[0].getStudy().getHost();
        } else {
            tmpArrayUrl[0] = response[0].getStudy().getHost();
            System.arraycopy(arrayUrl, 0, tmpArrayUrl, 1, arrayUrl.length);
            arrayUrl = tmpArrayUrl;
        }        

        StringBuffer urlBuilder = new StringBuffer();
        urlBuilder.append(url.getProtocol() + "://");
        for (int i = 0; i < arrayUrl.length; i++) {
            urlBuilder.append(arrayUrl[i]);
            if (i != arrayUrl.length - 1) {
                urlBuilder.append(".");
            } else {
                String port = "";
                if (url.getPort() > 0) {
                    urlBuilder.append(":" + String.valueOf(url.getPort()));
                }
            }
        }
        return urlBuilder.toString();
    }
}
